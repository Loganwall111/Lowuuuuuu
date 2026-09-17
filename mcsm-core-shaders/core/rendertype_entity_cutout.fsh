#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:mcsm_visuals.glsl>

// ============================================================================
//  Devouring Storms - rendertype_entity_cutout.fsh   [BUILD #416]
//
//  Blueprint component 1, fragment half: the selective full-bright override
//  for the storm's teeth and eyes.
//
//  The blueprint's rule, kept exactly, is the whole point of this file:
//  a pixel of the entity pass is left UNLIT (no lightmap multiply) when it is
//  a pure-white tooth (luminance > 0.82) or a vivid purple/cyan eye
//  (r > 0.45, b > 0.45, g < 0.25). Everything else keeps the normal cutout
//  shading, which is what stops the rest of the body from turning into a
//  glowing slab. #415 keyed the same layer on the EMISSIVE define and on the
//  per-phase mouth palette; this module makes the rule hold on the DEFINE-FREE
//  path too, so a pipeline that forgets EMISSIVE still gets glowing teeth.
//
//  The per-phase hues are then snapped onto the canonical track, because the
//  blueprint's fixed cyan/purple pair is only phases 4-5.5 of the story.
// ============================================================================

uniform sampler2D Sampler0;

#ifdef DISSOLVE
uniform sampler2D DissolveMaskSampler;
#endif

in float sphericalVertexDistance;
in float cylindricalVertexDistance;

#ifdef PER_FACE_LIGHTING
in vec4 vertexPerFaceColorBack;
in vec4 vertexPerFaceColorFront;
#else
in vec4 vertexColor;
#endif

#ifndef EMISSIVE
in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
in vec4 overlayColor;
#endif

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);

    // Blueprint alpha cut, plus the soft cut the mod's own bodies need.
    if (texColor.a < 0.05) {
        discard;
    }
#ifdef ALPHA_CUTOUT
    if (texColor.a < ALPHA_CUTOUT) {
        discard;
    }
#endif

#ifdef PER_FACE_LIGHTING
    vec4 faceVertexColor = gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack;
#else
    vec4 faceVertexColor = vertexColor;
#endif

#ifdef DISSOLVE
    if (faceVertexColor.a < texture(DissolveMaskSampler, texCoord0).a) {
        discard;
    }
    faceVertexColor.a = 1.0;
#endif

    vec4 color = texColor * faceVertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif

    // ---- blueprint selection rule -----------------------------------------
    float luminance = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    bool isPureWhiteTooth = luminance > 0.82;
    bool isVividEye = texColor.r > 0.45 && texColor.b > 0.45 && texColor.g < 0.25;
    bool emissive = isPureWhiteTooth || isVividEye;

    // The phase the storm is actually in, so the override colour follows the
    // evolution track instead of stopping at the blueprint's cyan/purple pair.
    float p = mcsm_witherstorm_phase();

#ifdef EMISSIVE
    // Emissive pipelines are already unlit; the colour track is all that is
    // left to apply.
    if (mcsm_active(p)) {
        color.rgb = mcsm_mouth_emissive(color.rgb, p);
    }
#else
    if (emissive && mcsm_active(p)) {
        // Snap onto the canonical band, then amplify 4.0x: the teeth stay
        // white (mcsm_mouth_color) and the aura around them takes the phase
        // colour, so the mouth throws a radiant field instead of reading as
        // flat white blocks.
        vec3 band = mix(mcsm_aura_color(p), mcsm_mouth_color(p), 0.75);
        float peak = max(max(texColor.r, texColor.g), texColor.b);
        color.rgb = mix(color.rgb, band * peak, 0.75) * MCSM_MOUTH_GAIN;
    } else {
        color *= lightMapColor;
    }
#endif

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
                          FogEnvironmentalStart, FogEnvironmentalEnd,
                          FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
