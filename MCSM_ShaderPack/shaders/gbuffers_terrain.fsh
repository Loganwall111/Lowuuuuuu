#version 120

#define MCSM_LIGHTING // Story Mode warm sunlight and lavender ambient shadows

precision highp float;
precision highp int;

uniform sampler2D gtexture;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 viewPos;

void main() {
    vec4 tex = texture2D(gtexture, texcoord);
    tex *= color;
    if (tex.a < 0.1) {
        discard;
    }

    // Story Mode Block and Sky light levels
    float blockLight = clamp((lmcoord.x - 0.03) * 1.05, 0.0, 1.0);
    float skyLight   = clamp((lmcoord.y - 0.03) * 1.05, 0.0, 1.0);

    // Warm Story Mode sunlight, cool lavender ambient shadow, and warm amber torchlight
    vec3 sunLightColor = vec3(1.12, 1.02, 0.90);
    vec3 shadowAmbientColor = vec3(0.70, 0.62, 0.88); // Lavender ambient shadow tint
    vec3 torchColor = vec3(1.20, 0.78, 0.38);         // Warm amber firelight

    vec3 skyLightTerm = mix(shadowAmbientColor * 0.72, sunLightColor, pow(skyLight, 1.25));
    vec3 blockLightTerm = torchColor * pow(blockLight, 1.35) * 1.35;

    vec3 ambientLighting = skyLightTerm + blockLightTerm;
    tex.rgb *= ambientLighting;

    // Diffuse surface normal shading (pure diffuse, NO reflections)
    float normalShade = clamp(normal.y * 0.35 + 0.65, 0.35, 1.0);
    tex.rgb *= normalShade;

    // Story Mode shadow deepening on shaded faces
    float isShadowed = 1.0 - skyLight;
    if (isShadowed > 0.20) {
        float shadowStr = (isShadowed - 0.20) / 0.80;
        tex.rgb = mix(tex.rgb, tex.rgb * vec3(0.80, 0.74, 0.94), shadowStr * 0.45);
    }

    gl_FragColor = tex;
}
