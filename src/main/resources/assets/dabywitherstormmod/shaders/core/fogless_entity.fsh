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
    // dark block segments (Telltale reference presentation). Replaces the
    // old flat shiny plastic gloss coat -- this is a moving highlight, not
    // a static specular layer.
    float mcsmSec = GameTime * 1200.0;
    float mcsmSweep = sin((texCoord0.x * 2.3 + texCoord0.y * 1.1) * 6.2831853 - mcsmSec * 0.45)
                    + 0.5 * sin((texCoord0.x * 5.1 - texCoord0.y * 3.7) * 6.2831853 - mcsmSec * 0.31);
    float mcsmGlint = pow(max(mcsmSweep * 0.6666, 0.0), 8.0) * (1.0 - mcsmCrease) * (1.0 - smoothstep(0.55, 0.85, mcsmLum));
    color.rgb += vec3(0.30, 0.38, 0.52) * mcsmGlint * 0.20;
#endif

    fragColor = mix(color, apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor), FOG_MIX);
}
