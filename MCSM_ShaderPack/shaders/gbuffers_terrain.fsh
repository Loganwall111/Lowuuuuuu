#version 120

// ============================================================================
// MCSM gbuffers_terrain.fsh — Story Mode warm/cool lighting, lavender ambient
// shadows, and live sun-cast shadows that sweep the ground with the time of day
// ============================================================================

#define MCSM_LIGHTING

precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform sampler2D shadowtex0;
uniform vec3 sunPosition;
uniform float frameTimeCounter;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 viewPos;
varying vec3 worldPos;
varying vec4 shadowPos;
varying float vSunY;

void main() {
    vec4 tex = texture2D(gtexture, texcoord);
    tex *= color;
    if (tex.a < 0.1) {
        discard;
    }

    // Story Mode Block and Sky light levels
    float blockLight = clamp((lmcoord.x - 0.03) * 1.05, 0.0, 1.0);
    float skyLight   = clamp((lmcoord.y - 0.03) * 1.05, 0.0, 1.0);

    // Warm Story Mode sunlight, cool lavender ambient shadow, warm amber torchlight
    vec3 sunLightColor = vec3(1.12, 1.02, 0.90);
    vec3 shadowAmbientColor = vec3(0.70, 0.62, 0.88);
    vec3 torchColor = vec3(1.20, 0.78, 0.38);

    vec3 skyLightTerm = mix(shadowAmbientColor * 0.72, sunLightColor, pow(skyLight, 1.25));
    vec3 blockLightTerm = torchColor * pow(blockLight, 1.35) * 1.35;

    vec3 ambientLighting = skyLightTerm + blockLightTerm;
    tex.rgb *= ambientLighting;

    // Diffuse surface normal shading (pure diffuse, NO reflections)
    float normalShade = clamp(normal.y * 0.35 + 0.65, 0.35, 1.0);
    tex.rgb *= normalShade;

    // ---- Live sun shadow (moves with the time of day) ----
    // Sun elevation gates the shadow strength so it fades at dawn/dusk and is
    // gone at night; the shadow map itself is rendered from the sun each frame.
    float sunVis = clamp(vSunY * 14.0, 0.0, 1.0);
    float shadow = 1.0;
    if (sunVis > 0.02) {
        vec3 sp = shadowPos.xyz * 0.5 + 0.5;
        if (sp.x >= 0.0 && sp.x <= 1.0 && sp.y >= 0.0 && sp.y <= 1.0 && sp.z <= 1.0) {
            float depth = texture2D(shadowtex0, sp.xy).x;
            shadow = (depth >= sp.z - 0.0025) ? 1.0 : 0.55;
        }
        tex.rgb = mix(tex.rgb, tex.rgb * 0.55, (1.0 - shadow) * sunVis * 0.85);
    }

    // Story Mode shadow deepening on shaded faces
    float isShadowed = 1.0 - skyLight;
    if (isShadowed > 0.20) {
        float shadowStr = (isShadowed - 0.20) / 0.80;
        tex.rgb = mix(tex.rgb, tex.rgb * vec3(0.80, 0.74, 0.94), shadowStr * 0.45);
    }

    gl_FragColor = tex;
}
