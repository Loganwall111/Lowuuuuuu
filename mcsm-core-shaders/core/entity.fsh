#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:mcsm_visuals.glsl>

// ============================================================================
//  MCSM visuals - entity.fsh  [spec file #4, rebased on real 26.2 entity.fsh]
//  Keeps PER_FACE_LIGHTING + DISSOLVE machinery; adds the attachment gradient
//  and the crisp-alpha policy. Dissolve never eats the storm lattice (it is
//  self-luminous energy, not dying flesh).
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
flat in int mcsmAttach;
in vec3 mcsmLocalPos;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);

    // MCSM stylization rule (spec §4): pixel-perfect blocky edges.
    if (texColor.a < 0.1) {
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
    if (mcsmAttach == 0) {
        if (faceVertexColor.a < texture(DissolveMaskSampler, texCoord0).a) {
            discard;
        }
        // The dissolve effect entirely replaces translucency
        faceVertexColor.a = 1.0;
    }
#endif

    vec4 color = texColor * faceVertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif

    float mcsmP = mcsm_phase(FogSkyEnd, FogColor, FogRenderDistanceEnd);

    // Build #385: shader-based face emissives - purple eyes and white teeth
    // render crisp and brilliant in the dark, fully isolated from background
    // fog and body-light bleed (modeled on colored shader torch illumination).
    float mcsmLum = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    bool mcsmEyePx   = texColor.b > 0.55 && texColor.r > 0.35 && texColor.g < 0.35;
    bool mcsmTeethPx = mcsmLum > 0.85;
    if (mcsmEyePx || mcsmTeethPx) {
        vec3 em = mcsmEyePx ? vec3(0.72, 0.25, 1.0) : vec3(1.0);
        fragColor = vec4(em * 1.35 + 0.08, color.a);
        return; // emissive pass: no fog, no lightmap multiplication
    }

    // Build #385: animated PBR glint - a subtle living sheen that rolls
    // across the storm chassis segments. Pseudo-roughness (1 - luminance)
    // keeps it on the dark "smooth" plates and matte everywhere else.
    float mcsmChassis = (1.0 - smoothstep(0.10, 0.30, mcsmLum)) * step(0.5, texColor.a);
    float mcsmGlint = smoothstep(0.86, 1.0,
        sin((texCoord0.x + texCoord0.y) * 6.0 - mcsm_clock(GameTime) * 2.0));
    color.rgb += vec3(0.55, 0.62, 0.85) * (mcsmGlint * 0.10 * mcsmChassis);

    if (mcsmAttach == 1 && mcsm_active(mcsmP)) {
        float clock = mcsm_clock(GameTime);
        color.rgb = mcsm_attachment_color(mcsmP, clock, mcsmLocalPos,
                                          texCoord0, texColor.a, color.rgb);
        // Self-luminous: only 35% of world fog reaches the vortex.
        float fogv = clamp(total_fog_value(sphericalVertexDistance, cylindricalVertexDistance,
                                           FogEnvironmentalStart, FogEnvironmentalEnd,
                                           mcsm_rd_start(), FogRenderDistanceEnd) * 0.35,
                           0.0, 1.0);
        fragColor = vec4(mix(color.rgb, mcsm_fog_color(mcsmP, FogColor.rgb), fogv * FogColor.a), color.a);
        return;
    }

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
                          FogEnvironmentalStart, FogEnvironmentalEnd,
                          mcsm_rd_start(), FogRenderDistanceEnd, FogColor);
}
