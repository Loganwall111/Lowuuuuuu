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
in float mcsmRim;

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

    // BUILD #415 -- NATIVE EMISSIVE FACE TRACK (teeth + eye lenses).
    // This program is the one 26.2 uses for RenderTypes.eyes(): the emissive
    // teeth grids and the eye masks arrive here full-bright with the EMISSIVE
    // define, so this is the shader-side hook that re-keys the mouth every
    // phase. BUILD #416: the TEETH stay white in every phase and the AURA
    // around them takes the phase colour -- bluish at 4, white at 5, bluish at
    // 5.2-5.9, pure blue at 6, toxic green at 7, blue at 8
    // (mcsm_mouth_emissive blends the two by pixel luminance). The 4.0x
    // emissive amplification still lands before the post pass sees it. mcsm_mouth_mask() keys on
    // the emissive luminance floor, so ordinary entity shading is untouched.
#ifdef EMISSIVE
    if (mcsm_active(mcsmP)) {
        color.rgb = mcsm_mouth_emissive(color.rgb, mcsmP);
    }
#endif

    // Purple specular rim overlay: #6A24D9, tight at block edges with the
    // required pow(rim, 3.5) falloff. Attachments keep their own lattice color.
    if (mcsmAttach == 0 && mcsm_active(mcsmP)) {
        vec3 rimLight = vec3(106.0, 36.0, 217.0) / 255.0; // #6A24D9
        color.rgb += rimLight * mcsmRim;
    }

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
