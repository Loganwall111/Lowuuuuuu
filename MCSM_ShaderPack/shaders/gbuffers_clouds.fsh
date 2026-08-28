#version 120

#define CLOUD_EXTRUSION 2.5 // [1.0 1.5 2.0 2.5 3.0 3.5 4.0]
#define CLOUDS_ACTIVE // Enable authentic Story Mode extruded clouds

// Identical high precision header to eliminate GPU compiler crashes
precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform sampler2D cloudTex0; // 0: Day
uniform sampler2D cloudTex1; // 1: Sunset
uniform sampler2D cloudTex2; // 2: Night
uniform sampler2D cloudTex3; // 3: Storm
uniform sampler2D cloudTex4; // 4: Awakening
uniform sampler2D cloudTex5; // 5: Cataclysm
uniform sampler2D cloudTex6; // 6: Volcanic
uniform sampler2D cloudTex7; // 7: Twilight

uniform float frameTimeCounter;
uniform int worldTime;

varying vec4 vColor;
varying vec2 vTexCoord;
varying vec3 vWorldPos;
varying vec3 vNormal;
varying float vFogFactor;

struct CloudPreset {
    vec4 baseColor;
    vec3 highlightColor;
    vec3 shadowColor;
    vec2 speed;
    float weight;
};

void main() {
    CloudPreset presets[8];

    // Preset 0: Overworld Day (MCSM Normal / Default)
    presets[0].baseColor      = vec4(1.00, 1.00, 1.00, 0.92);
    presets[0].highlightColor = vec3(1.05, 1.02, 0.98);
    presets[0].shadowColor    = vec3(0.88, 0.86, 0.95);
    presets[0].speed          = vec2(1.0, 0.2) * 0.0006;
    presets[0].weight         = 0.35;

    // Preset 1: Sunset / Golden Hour (Warm Coral & Amber)
    presets[1].baseColor      = vec4(0.98, 0.70, 0.48, 0.90);
    presets[1].highlightColor = vec3(1.00, 0.85, 0.58);
    presets[1].shadowColor    = vec3(0.85, 0.42, 0.48);
    presets[1].speed          = vec2(0.8, 0.6) * 0.0008;
    presets[1].weight         = 0.20;

    // Preset 2: Deep Night / Moonlight (Silver & Periwinkle Indigo)
    presets[2].baseColor      = vec4(0.38, 0.40, 0.60, 0.85);
    presets[2].highlightColor = vec3(0.55, 0.62, 0.88);
    presets[2].shadowColor    = vec3(0.18, 0.16, 0.32);
    presets[2].speed          = vec2(0.5, -0.7) * 0.0005;
    presets[2].weight         = 0.15;

    // Preset 3: Storm Gathering (Bruised Charcoal Overcast)
    presets[3].baseColor      = vec4(0.24, 0.20, 0.28, 0.94);
    presets[3].highlightColor = vec3(0.40, 0.32, 0.48);
    presets[3].shadowColor    = vec3(0.10, 0.08, 0.14);
    presets[3].speed          = vec2(1.8, 1.2) * 0.0012;
    presets[3].weight         = 0.12;

    // Preset 4: Awakening (Obsidian Purple with #00E5FF Cyan Rim)
    presets[4].baseColor      = vec4(0.15, 0.10, 0.22, 0.95);
    presets[4].highlightColor = vec3(0.00, 0.90, 1.00);
    presets[4].shadowColor    = vec3(0.05, 0.02, 0.08);
    presets[4].speed          = vec2(-1.5, 2.0) * 0.0016;
    presets[4].weight         = 0.10;

    // Preset 5: Cataclysm Core (Pink-Magenta #D81B60 & Void-Violet)
    presets[5].baseColor      = vec4(0.38, 0.06, 0.26, 0.98);
    presets[5].highlightColor = vec3(0.85, 0.11, 0.38);
    presets[5].shadowColor    = vec3(0.29, 0.08, 0.55);
    presets[5].speed          = vec2(2.5, -1.8) * 0.0020;
    presets[5].weight         = 0.08;

    // Preset 6: Volcanic Horizon Mask (Fire-Orange #FF6D00 & Blood-Red)
    presets[6].baseColor      = vec4(0.72, 0.16, 0.02, 1.00);
    presets[6].highlightColor = vec3(1.00, 0.43, 0.00);
    presets[6].shadowColor    = vec3(0.84, 0.00, 0.00);
    presets[6].speed          = vec2(-3.0, -2.5) * 0.0025;
    presets[6].weight         = 0.06;

    // Preset 7: Twilight Purple / Flash
    presets[7].baseColor      = vec4(0.88, 0.70, 1.00, 0.92);
    presets[7].highlightColor = vec3(0.98, 0.90, 1.00);
    presets[7].shadowColor    = vec3(0.45, 0.25, 0.65);
    presets[7].speed          = vec2(0.4, 0.4) * 0.0006;
    presets[7].weight         = 0.06;

    vec4 accumulatedColor = vec4(0.0);
    float totalWeight = 0.0;

    float isTop = clamp(vNormal.y, 0.0, 1.0);
    float isBottom = clamp(-vNormal.y, 0.0, 1.0);
    float isSide = clamp(1.0 - abs(vNormal.y), 0.0, 1.0);

    for (int i = 0; i < 8; i++) {
        vec2 uvOffset = presets[i].speed * frameTimeCounter;
        vec2 sampledUV = vTexCoord + uvOffset;

        vec4 sampledTex = vec4(1.0);
        if (i == 0) sampledTex = texture2D(cloudTex0, sampledUV);
        else if (i == 1) sampledTex = texture2D(cloudTex1, sampledUV);
        else if (i == 2) sampledTex = texture2D(cloudTex2, sampledUV);
        else if (i == 3) sampledTex = texture2D(cloudTex3, sampledUV);
        else if (i == 4) sampledTex = texture2D(cloudTex4, sampledUV);
        else if (i == 5) sampledTex = texture2D(cloudTex5, sampledUV);
        else if (i == 6) sampledTex = texture2D(cloudTex6, sampledUV);
        else if (i == 7) sampledTex = texture2D(cloudTex7, sampledUV);

        if (sampledTex.a < 0.01) {
            sampledTex = texture2D(gtexture, sampledUV);
            if (sampledTex.a < 0.01) sampledTex = vec4(1.0);
        }

        vec3 faceTint = mix(presets[i].shadowColor, presets[i].highlightColor, isTop * 0.70 + isSide * 0.40);
        if (isBottom > 0.5) faceTint = presets[i].shadowColor;

        vec4 presetFinal = vec4(presets[i].baseColor.rgb * faceTint * sampledTex.rgb, presets[i].baseColor.a * sampledTex.a);
        accumulatedColor += presetFinal * presets[i].weight;
        totalWeight += presets[i].weight;
    }

    if (totalWeight > 0.0) {
        accumulatedColor /= totalWeight;
    }

    if (accumulatedColor.a < 0.08) {
        discard;
    }

    accumulatedColor.rgb *= vColor.rgb;
    accumulatedColor.rgb = mix(accumulatedColor.rgb, vec3(0.68, 0.60, 0.88), vFogFactor * 0.45);
    gl_FragColor = accumulatedColor;
}
