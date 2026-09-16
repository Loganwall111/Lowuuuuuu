#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

// How much of vanilla's fog to apply. 0.0 = fully fogless (crisp), 0.5 = half fog. Set by the
// pipeline via a shader define; defaults to fogless if the pipeline forgets to set it.
#ifndef FOG_MIX
#define FOG_MIX 0.0
#endif

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
    vec4 color = texture(Sampler0, texCoord0);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
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
    // The dissolve effect entirely replaces translucency
    faceVertexColor.a = 1.0;
#endif

    color *= faceVertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif

#ifdef MCSM_VOID_BODY
    // ------------------------------------------------------------------
    // MCSM 1.9.201 -- VOID-BLACK BODY SHADING, locked to the ingested split
    // atlas key textures/misc/body_palette_key.png (left block #0A0E14
    // navy-black, right block #000000 absolute void-black). The storm's
    // pipelines stamp MCSM_VOID_BODY; everything else renders untouched.
    // Inner creases, ambient-occlusion shadow bands and the structural
    // block layers are forced to the void key so every block segment gets
    // a clean, sharp depth line. Legacy light-gray / brown vanilla sheets
    // can no longer leak into the body: anything under the mid-tone floor
    // collapses to #000000 instead of reading as lit flesh.
    const vec3 MCSM_NAVY_BLACK = vec3(0.0392, 0.0549, 0.0784);  // key left
    const vec3 MCSM_VOID_BLACK = vec3(0.0, 0.0, 0.0);          // key right
    float mcsmLum = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    float mcsmCrease = 1.0 - smoothstep(0.015, 0.20, mcsmLum);
    color.rgb = mix(color.rgb, MCSM_VOID_BLACK, mcsmCrease);
    // structural block layers: pull the mid band down toward the navy key
    // (never above it) so facets stay readable but stay cinematic-dark
    float mcsmBand = smoothstep(0.20, 0.42, mcsmLum) * (1.0 - smoothstep(0.42, 0.68, mcsmLum));
    color.rgb = mix(color.rgb, MCSM_NAVY_BLACK * 0.55, mcsmBand * 0.70);
    // Animated glint overlay: a subtle living sheen that rolls across the
    // dark block segments (Telltale reference presentation). Two drifting
    // sine fields, each raised to a high power so only narrow streaks survive,
    // plus a slow cross-roll band -- the sheen therefore travels along the
    // block faces instead of sitting on them. Replaces the old flat shiny
    // plastic gloss coat entirely: there is no static specular term left.
    // (The shared twin of this function is mcsm_glint() in mcsm_visuals.glsl,
    //  used by the core passes; this program cannot import it -- see the
    //  EMISSIVE block below for the bind-group reason.)
    float mcsmSec = GameTime * 1200.0;
    float mcsmSweep = sin((texCoord0.x * 2.3 + texCoord0.y * 1.1) * 6.2831853 - mcsmSec * 0.45)
                    + 0.5 * sin((texCoord0.x * 5.1 - texCoord0.y * 3.7) * 6.2831853 - mcsmSec * 0.31);
    float mcsmGlint = pow(max(mcsmSweep * 0.6666, 0.0), 8.0);
    float mcsmRoll = pow(max(sin((texCoord0.y * 3.9 - texCoord0.x * 1.7) * 6.2831853 - mcsmSec * 0.63), 0.0), 12.0);
    mcsmGlint = (mcsmGlint + mcsmRoll * 0.6)
              * (1.0 - mcsmCrease) * (1.0 - smoothstep(0.55, 0.85, mcsmLum));
    color.rgb += vec3(0.30, 0.38, 0.52) * mcsmGlint * 0.20;
#endif

#ifdef EMISSIVE
    // ------------------------------------------------------------------
    // BUILD #415 -- NATIVE EMISSIVE TEETH / EYE TRACK (this program is also
    // the fallback eyes pipeline used when no shader pack is installed).
    //
    // The canonical per-phase mouth palette lives in mcsm_visuals.glsl
    // (mcsm_mouth_color) for the core entity/terrain passes. THIS program may
    // not import it -- it runs on the storm's own bind group, and declaring a
    // uniform the group does not bind is a hard Vulkan crash (see the
    // storm_glow.fsh header). So the same six-band table is repeated here.
    //
    // BUILD #416 -- THE PHASE IS NOW READ, NOT GUESSED. The phase travels in
    // FogData.skyEnd (1000 + phase*100, stamped by McsmFogCarrierMixin), which
    // minecraft:fog.glsl already declares and this program already imports --
    // no new uniform, so no bind-group risk. The vertex-colour hue
    // (McsmTeethPhaseTint -> eyeColorR/G/B) is kept as the fallback for any
    // frame where the carrier is absent.
    //   P4 cyan-white / P5 pure white / P5.5 cyan-blue / P6 cinematic blue /
    //   P7 toxic green / P8 blinding white
    //
    // Only pixels on the emissive luminance floor are touched, so body-block
    // pixels routed through this program keep their void-black shading.
    float mcsmP = (FogSkyEnd - 1000.0) * 0.01;
    vec3 tint = faceVertexColor.rgb;
    float tmax = max(tint.r, max(tint.g, tint.b));
    vec3 band;
    if (mcsmP > 0.01) {
        // Phase-exact: one smooth weight per band, summed as deltas around the
        // white family so the handover between phases never steps.
        float w4  = 1.0 - smoothstep(4.90, 5.10, mcsmP);
        float w55 = smoothstep(5.45, 5.60, mcsmP) * (1.0 - smoothstep(5.90, 6.10, mcsmP));
        float w6  = smoothstep(5.90, 6.10, mcsmP) * (1.0 - smoothstep(6.90, 7.10, mcsmP));
        float w7  = smoothstep(6.90, 7.10, mcsmP) * (1.0 - smoothstep(7.90, 8.10, mcsmP));
        band = vec3(1.0)
             + (vec3(0.72, 0.98, 1.00) - vec3(1.0)) * w4
             + (vec3(0.40, 0.80, 1.00) - vec3(1.0)) * w55
             + (vec3(0.22, 0.50, 1.00) - vec3(1.0)) * w6
             + (vec3(0.36, 1.00, 0.28) - vec3(1.0)) * w7;
    } else if (tmax <= 0.02) {
        band = vec3(1.0);
    } else if (tint.g > 0.72 * tint.b && tint.r < 0.55 * tint.b) {
        band = vec3(0.36, 1.00, 0.28);                                   // phase 7
    } else if (tint.b > 0.90 * tint.r && tint.g > 0.55 * tint.r) {
        band = tint.g > 0.93 * tint.b ? vec3(0.72, 0.98, 1.00)           // phase 4
             : (tint.g > 0.72 * tint.b ? vec3(0.40, 0.80, 1.00)          // phase 5.5
                                       : vec3(0.22, 0.50, 1.00));        // phase 6
    } else {
        band = vec3(1.00, 1.00, 1.00);                                   // phase 5 / 8
    }
    float emLum = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    float emMask = smoothstep(0.45, 0.80, emLum);
    color.rgb = mix(color.rgb, band * max(color.rgb, vec3(0.0)), emMask);
    // 4.0x emissive amplification: the mouth throws a radiant neon field into
    // the dark air even with no post-processing pack loaded.
    const float MCSM_MOUTH_GAIN = 4.0;
    color.rgb *= 1.0 + (MCSM_MOUTH_GAIN - 1.0) * emMask;
#endif

    fragColor = mix(color, apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor), FOG_MIX);
}
