#version 120

/*
 * Minecraft: Story Mode — Coloured Lighting & Ground Shadows
 * From Telltale Games:
 * - Direct sunlight: Warm amber/golden illumination (#FFF2D8)
 * - Shadows on ground: Atmospheric cool lavender/purple bounce tint (#6B5885)
 * - Torch / blocklight: Rich warm firelight (#FFA347)
 */

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 worldPos;

#ifndef MCSM_COLOURED_LIGHTING
#define MCSM_COLOURED_LIGHTING 1
#endif
#ifndef MCSM_SHADOW_TINT
#define MCSM_SHADOW_TINT 1
#endif

void main() {
    vec4 tex = texture2D(texture, texcoord) * color;

    #if MCSM_COLOURED_LIGHTING
    float blockLight = clamp((lmcoord.x - 0.03) * 1.05, 0.0, 1.0);
    float skyLight   = clamp((lmcoord.y - 0.03) * 1.05, 0.0, 1.0);

    // Warm direct sun illumination
    vec3 sunLightColor = vec3(1.08, 1.00, 0.92);
    // Cool Telltale lavender/purple ambient shadow tint
    vec3 shadowAmbientColor = vec3(0.68, 0.58, 0.82);
    // Warm fire / torch blocklight color
    vec3 torchColor = vec3(1.15, 0.74, 0.40);

    // Light calculation
    vec3 skyLightTerm = mix(shadowAmbientColor * 0.75, sunLightColor, pow(skyLight, 1.3));
    vec3 blockLightTerm = torchColor * pow(blockLight, 1.4) * 1.35;

    vec3 ambient = skyLightTerm + blockLightTerm;
    tex.rgb *= ambient;

    #if MCSM_SHADOW_TINT
    // Accentuate ground shadows with Story Mode purple tone
    float isShadowed = 1.0 - skyLight;
    if (isShadowed > 0.35) {
        float shadowStr = (isShadowed - 0.35) / 0.65;
        tex.rgb = mix(tex.rgb, tex.rgb * vec3(0.85, 0.78, 0.95), shadowStr * 0.45);
    }
    #endif

    #endif

    gl_FragColor = tex;
}
