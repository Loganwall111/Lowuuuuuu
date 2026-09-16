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
#ifdef MCSM_VOID_BODY
    // captured BEFORE the platform light multiplies in. The crease / AO masks
    // must key off the block's own albedo: if they keyed off the lit colour,
    // midnight would drag every pixel under the crease floor and crush the
    // whole body to void black instead of the navy night key.
    float mcsmAlbedo = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
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
    // BUILD #416 -- the four tone keys of the light-adaptive shade. The first
    // two are the ingested split-atlas keys; the second two are the plate tones
    // the facets slide between as the world's light changes.
    const vec3 MCSM_NAVY_BLACK = vec3(0.0392, 0.0549, 0.0784);   // #0A0E14  key left / night plates
    const vec3 MCSM_VOID_BLACK = vec3(0.0, 0.0, 0.0);            // #000000  key right / permanent void
    const vec3 MCSM_ASH_GRAY   = vec3(0.1412, 0.1647, 0.2118);   // #242A36  hard daylight
    const vec3 MCSM_TWILIGHT   = vec3(0.0510, 0.1059, 0.1647);   // #0D1B2A  dusk / dawn light

    float mcsmLum = mcsmAlbedo;

    // ---- (1) PERMANENT VOID: crevices, structural joints, deep AO ---------
    // Light-impenetrable. These pixels are #000000 at noon, at midnight and
    // under a lightning flash -- nothing below can lift them again.
    float mcsmCrease = 1.0 - smoothstep(0.015, 0.20, mcsmLum);
    color.rgb = mix(color.rgb, MCSM_VOID_BLACK, mcsmCrease);
    // structural block layers: pull the mid band down toward the navy key
    // (never above it) so facets stay readable but stay cinematic-dark
    float mcsmBand = smoothstep(0.20, 0.42, mcsmLum) * (1.0 - smoothstep(0.42, 0.68, mcsmLum));
    color.rgb = mix(color.rgb, MCSM_NAVY_BLACK * 0.55, mcsmBand * 0.70);

    // ---- (2) AMBIENT LIGHT ADAPTATION ------------------------------------
    // The surface plates read the platform's own global ambient light -- the
    // lightmap the vertex already carries -- and bilinearly interpolate a 2x2
    // matrix of tones instead of a flat texture colour:
    //
    //              cool ambient          warm ambient
    //   night      navy-black #0A0E14     navy-black #0A0E14
    //   day        twilight   #0D1B2A     ash-gray   #242A36
    //
    // so a dark night cycle sits on the navy key, ordinary daylight lifts the
    // plates to ash-gray, and the low warm light of a sunset (or our own
    // sunset filter) leans them toward twilight blue. A lightning flash simply
    // drives the ambient term to the top of the ramp on its own -- no flash
    // detection needed, because the flash IS what the lightmap measures.
    // The world clock, mirrored from the sky shader's own sun maths (this file
    // imports only globals.glsl, so the values are recomputed locally rather
    // than pulled from mcsm_visuals -- the extras shaders must never acquire a
    // new uniform binding). Same numbers as the sky, so body and sky agree on
    // what time it is. The second reason this exists: the storm head is
    // submitted full-bright, so the lightmap on its own would read "noon" at
    // midnight and the dawn hull would never get its night key.
    float mcsmDay01 = fract(GameTime / 1200.0);
    float mcsmSunY  = sin(mcsmDay01 * 6.2831853) * 0.9 + 0.02;
    float mcsmSkyDay = clamp(mcsmSunY * 3.2, 0.0, 1.0);          // 1 = noon
    float mcsmDuskW  = 1.0 - clamp(abs(mcsmSunY) * 3.2, 0.0, 1.0); // 1 = sun on the horizon

    float mcsmAmb = 0.0;
    float mcsmWarm = 0.5;
#ifndef EMISSIVE
    float mcsmLit = max(max(lightMapColor.r, lightMapColor.g), lightMapColor.b);
    // platform ambient x the sky's actual light level: a full-bright midnight
    // submission lands on 0.12 (deep night), a torch-lit face still contributes
    // its block light, and noon lands on 1.0
    mcsmAmb = mcsmLit * mix(0.12, 1.0, max(mcsmSkyDay, mcsmDuskW * 0.55));
    // warm/cool ratio of the ambient light: a sunset has far more red than
    // blue, noon is neutral, moonlight is blue-biased -- plus the low warm sun
    // the clock can see even in a full-bright submission
    mcsmWarm = clamp((lightMapColor.r - lightMapColor.b) * 3.0 + 0.5 + mcsmDuskW * 0.35, 0.0, 1.0);
#endif
    float mcsmDay = smoothstep(0.16, 0.78, mcsmAmb);
    vec3 mcsmCoolPlate = mix(MCSM_NAVY_BLACK, MCSM_TWILIGHT, mcsmDay);
    vec3 mcsmWarmPlate = mix(MCSM_NAVY_BLACK, MCSM_ASH_GRAY, mcsmDay);
    vec3 mcsmPlate = mix(mcsmCoolPlate, mcsmWarmPlate, mcsmWarm);
    // only the lit facets move: the creases were just locked to black and are
    // excluded by (1.0 - mcsmCrease), so the adaptation can never touch them
    // The mid band is deliberately NOT excluded here. The mandate is that the
    // mid-tones adapt: the band pre-pass above has already shaped the small
    // slice of texture that survives the plate blend, and blocking the blend
    // here would leave the midnight hull reading as half-lit daylight texture.
    float mcsmFacet = (1.0 - mcsmCrease) * (1.0 - smoothstep(0.70, 0.95, mcsmLum));
    // The plate is mixed in unscaled, so the key tones are exact: #0A0E14 at
    // night, #242A36 under a warm low sun, #0D1B2A under a cool sky. Neutral
    // ambient reads as the midpoint of the two permitted tones, never a tone of
    // its own. The gate is strongest in the dark (0.90) -- a midnight hull has
    // to read as the navy key, not as 28% of the daylight texture -- and backs
    // off to 0.72 in daylight so the block detail stays legible.
    color.rgb = mix(color.rgb, mcsmPlate, mcsmFacet * mix(0.90, 0.72, mcsmDay));
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
    // BUILD #416 -- the sheen answers the light too: it is nearly invisible on
    // a black midnight hull and rolls visibly across plates that daylight or a
    // lightning flash has lifted, which is what keeps the body reading as a
    // material rather than a flat silhouette.
    color.rgb += mix(vec3(0.26, 0.32, 0.44), vec3(0.34, 0.40, 0.54), mcsmWarm)
               * mcsmGlint * (0.08 + 0.30 * mcsmDay);
#endif

#ifdef EMISSIVE
    // ------------------------------------------------------------------
    // BUILD #415/#416 -- NATIVE EMISSIVE TEETH / EYE TRACK (this program is
    // also the fallback eyes pipeline used when no shader pack is installed).
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
    //   BUILD #416 user spec, phase 4 -> 8: WHITE TEETH with a bluish aura at
    //   4, white at 5, bluish again from 5.2 to 5.9, pure blue at 6, toxic
    //   green at 7, blue at 8.
    //
    // Only pixels on the emissive luminance floor are touched, so body-block
    // pixels routed through this program keep their void-black shading.
    float mcsmP = (FogSkyEnd - 1000.0) * 0.01;
    vec3 tint = faceVertexColor.rgb;
    float tmax = max(tint.r, max(tint.g, tint.b));
    vec3 band;
    // Set inside the phase branch below; 0.0 means "this pixel is not an eye".
    float eyeLike = 0.0;
    if (mcsmP > 0.01) {
        // BUILD #416 -- WHITE TEETH, PHASE-COLOURED AURA (user spec, 4 -> 8):
        //   bluish aura at 4, white at 5, bluish 5.2-5.9, pure blue at 6,
        //   toxic green at 7, blue at 8 -- teeth white at every one of them.
        // Same ramp boundaries as mcsm_aura_color() in mcsm_visuals.glsl, kept
        // local because this program cannot import it (see above).
        vec3 aura = vec3(0.55, 0.80, 1.00);
        aura = mix(aura, vec3(1.00, 1.00, 1.00), smoothstep(4.92, 5.00, mcsmP));
        aura = mix(aura, vec3(0.50, 0.78, 1.00), smoothstep(5.15, 5.25, mcsmP));
        aura = mix(aura, vec3(0.22, 0.42, 1.00), smoothstep(5.85, 6.00, mcsmP));
        aura = mix(aura, vec3(0.36, 1.00, 0.28), smoothstep(6.90, 7.05, mcsmP));
        aura = mix(aura, vec3(0.35, 0.58, 1.00), smoothstep(7.90, 8.00, mcsmP));
        // tooth core -> white, the emissive skirt around it -> the aura
        float core = smoothstep(0.72, 0.98,
                dot(color.rgb, vec3(0.2126, 0.7152, 0.0722)));
        band = mix(aura, vec3(1.0), core);
        // BUILD #416 -- leave the already-saturated EYE pixels alone: their
        // violet is the phase eye tint, not a tooth, and the snap below must
        // not drain it. Grey/white teeth have near-zero saturation and are
        // always caught.
        float mxE = max(color.r, max(color.g, color.b));
        float mnE = min(color.r, min(color.g, color.b));
        float satE = mxE > 1.0e-4 ? (mxE - mnE) / mxE : 0.0;
        float lumE = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
        eyeLike = smoothstep(0.35, 0.55, satE)
                * (1.0 - smoothstep(0.88, 1.0, lumE));
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
    float emMask = smoothstep(0.45, 0.80, emLum) * (1.0 - eyeLike);
    color.rgb = mix(color.rgb, band * max(color.rgb, vec3(0.0)), emMask);
    // 4.0x emissive amplification: the mouth throws a radiant neon field into
    // the dark air even with no post-processing pack loaded.
    const float MCSM_MOUTH_GAIN = 4.0;
    color.rgb *= 1.0 + (MCSM_MOUTH_GAIN - 1.0) * emMask;
#endif

    fragColor = mix(color, apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor), FOG_MIX);
}
