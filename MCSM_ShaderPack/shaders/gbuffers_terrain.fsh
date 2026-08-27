#version 120

precision highp float;
precision highp int;

uniform sampler2D gtexture;
varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;

void main() {
    vec4 tex = texture2D(gtexture, texcoord) * color;
    if (tex.a < 0.1) {
        discard;
    }

    // Minecraft: Story Mode Colored Lighting & Shadows (No Reflections)
    float blockLight = clamp((lmcoord.x - 0.03) * 1.05, 0.0, 1.0);
    float skyLight   = clamp((lmcoord.y - 0.03) * 1.05, 0.0, 1.0);

    // Warm Story Mode sunlight, cool lavender shadow tint, and warm amber torchlight
    vec3 sunLightColor = vec3(1.08, 1.00, 0.92);
    vec3 shadowAmbientColor = vec3(0.72, 0.65, 0.85); // Story Mode lavender ambient shadow
    vec3 torchColor = vec3(1.15, 0.75, 0.40);         // Warm amber firelight

    // Sky light combines direct sun with ambient shadow
    vec3 skyLightTerm = mix(shadowAmbientColor * 0.70, sunLightColor, pow(skyLight, 1.25));
    // Torch / block light term
    vec3 blockLightTerm = torchColor * pow(blockLight, 1.35) * 1.30;

    vec3 ambientLighting = skyLightTerm + blockLightTerm;
    tex.rgb *= ambientLighting;

    // Story Mode shadow deepening on shaded faces (diffuse shading, NO reflections)
    float isShadowed = 1.0 - skyLight;
    if (isShadowed > 0.30) {
        float shadowStr = (isShadowed - 0.30) / 0.70;
        tex.rgb = mix(tex.rgb, tex.rgb * vec3(0.82, 0.76, 0.92), shadowStr * 0.40);
    }

    gl_FragColor = tex;
}
