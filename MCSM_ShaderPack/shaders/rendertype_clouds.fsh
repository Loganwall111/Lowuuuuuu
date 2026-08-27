#version 120

// Identical high precision header to eliminate GPU compiler crashes
precision highp float;
precision highp int;

// Local shader pack texture samplers for all 8 Story Mode cloud presets
uniform sampler2D gtexture;
uniform sampler2D cloudTex0; // 0: Overworld Day
uniform sampler2D cloudTex1; // 1: Sunset / Golden Hour
uniform sampler2D cloudTex2; // 2: Deep Night / Moonlight
uniform sampler2D cloudTex3; // 3: Storm Gathering
uniform sampler2D cloudTex4; // 4: Wither Awakening (Cyan Rim)
uniform sampler2D cloudTex5; // 5: Cataclysm (Pink-Magenta Anamorphic)
uniform sampler2D cloudTex6; // 6: Volcanic Horizon Mask
uniform sampler2D cloudTex7; // 7: Twilight Purple / Flash

// Time and animation uniforms
uniform float frameTimeCounter;

// Varyings from vertex shader
varying vec4 vColor;
varying vec2 vTexCoord;
varying vec3 vWorldPos;
varying vec3 vNormal;
varying float vFogFactor;

// Preset Color Data Struct
struct CloudPreset {
    vec4 baseColor;
    vec3 highlightColor;
    vec3 shadowColor;
    vec2 speed;
    float extrusion;
    float weight;
};

void main() {
    // -------------------------------------------------------------------------
    // 1. DATA PRESERVATION: The 8 Authentic Story Mode Cloud Presets
    // All original asset data, color data, and logic are fully preserved.
    // -------------------------------------------------------------------------
    CloudPreset presets[8];

    // Preset 0: Overworld Day (MCSM Normal / Default)
    presets[0].baseColor      = vec4(1.00, 1.00, 1.00, 0.90);
    presets[0].highlightColor = vec3(1.05, 1.02, 0.98);
    presets[0].shadowColor    = vec3(0.90, 0.88, 0.96);
    presets[0].speed          = vec2(1.0, 0.2) * 0.0006;
    presets[0].extrusion      = 2.5;
    presets[0].weight         = 0.35;

    // Preset 1: Sunset / Golden Hour (Warm Coral & Amber)
    presets[1].baseColor      = vec4(0.98, 0.68, 0.45, 0.88);
    presets[1].highlightColor = vec3(1.00, 0.84, 0.55);
    presets[1].shadowColor    = vec3(0.85, 0.42, 0.48);
    presets[1].speed          = vec2(0.8, 0.6) * 0.0008;
    presets[1].extrusion      = 2.8;
    presets[1].weight         = 0.20;

    // Preset 2: Deep Night / Moonlight (Silver & Periwinkle Indigo)
    presets[2].baseColor      = vec4(0.35, 0.38, 0.58, 0.82);
    presets[2].highlightColor = vec3(0.55, 0.62, 0.88);
    presets[2].shadowColor    = vec3(0.18, 0.16, 0.32);
    presets[2].speed          = vec2(0.5, -0.7) * 0.0005;
    presets[2].extrusion      = 2.4;
    presets[2].weight         = 0.15;

    // Preset 3: Storm Formative (Bruised Charcoal Overcast)
    presets[3].baseColor      = vec4(0.22, 0.18, 0.26, 0.92);
    presets[3].highlightColor = vec3(0.38, 0.30, 0.45);
    presets[3].shadowColor    = vec3(0.10, 0.08, 0.14);
    presets[3].speed          = vec2(1.8, 1.2) * 0.0012;
    presets[3].extrusion      = 3.0;
    presets[3].weight         = 0.12;

    // Preset 4: Awakening (Obsidian Purple with #00E5FF Cyan Rim Glow)
    presets[4].baseColor      = vec4(0.12, 0.08, 0.20, 0.95);
    presets[4].highlightColor = vec3(0.00, 0.90, 1.00); // Electric Turquoise/Cyan Glow
    presets[4].shadowColor    = vec3(0.05, 0.02, 0.08);
    presets[4].speed          = vec2(-1.5, 2.0) * 0.0016;
    presets[4].extrusion      = 3.2;
    presets[4].weight         = 0.10;

    // Preset 5: Cataclysm Core (Pink-Magenta #D81B60 & Void-Violet #4A148C)
    presets[5].baseColor      = vec4(0.35, 0.05, 0.25, 0.98);
    presets[5].highlightColor = vec3(0.85, 0.11, 0.38); // Pink-Magenta Glare
    presets[5].shadowColor    = vec3(0.29, 0.08, 0.55); // Void-Violet Shadow
    presets[5].speed          = vec2(2.5, -1.8) * 0.0020;
    presets[5].extrusion      = 3.6;
    presets[5].weight         = 0.08;

    // Preset 6: Volcanic Horizon Mask (Fire-Orange #FF6D00 & Blood-Red #D50000)
    presets[6].baseColor      = vec4(0.70, 0.15, 0.02, 1.00);
    presets[6].highlightColor = vec3(1.00, 0.43, 0.00); // Volcanic Fire-Orange
    presets[6].shadowColor    = vec3(0.84, 0.00, 0.00); // Blood-Red Mask
    presets[6].speed          = vec2(-3.0, -2.5) * 0.0025;
    presets[6].extrusion      = 4.0;
    presets[6].weight         = 0.06;

    // Preset 7: Twilight Purple / End Flash (Twilight #E0B0FF & Flash Pulse)
    presets[7].baseColor      = vec4(0.88, 0.69, 1.00, 0.90);
    presets[7].highlightColor = vec3(0.98, 0.90, 1.00); // Celestial Flashbang Rim
    presets[7].shadowColor    = vec3(0.45, 0.25, 0.65); // Twilight Violet
    presets[7].speed          = vec2(0.4, 0.4) * 0.0006;
    presets[7].extrusion      = 2.6;
    presets[7].weight         = 0.06;

    // -------------------------------------------------------------------------
    // 2. NO HARDCODED ENVIRONMENT / STAGE CHECKS
    // All conditionals checking LevelIDs, dimensions, or stages are removed.
    // The 8 custom cloud loops execute globally.
    // -------------------------------------------------------------------------
    vec4 accumulatedColor = vec4(0.0);
    float totalWeight = 0.0;

    // Directional shading factor from geometry normal
    float isTop = clamp(vNormal.y, 0.0, 1.0);
    float isBottom = clamp(-vNormal.y, 0.0, 1.0);
    float isSide = clamp(1.0 - abs(vNormal.y), 0.0, 1.0);

    // Global execution of all 8 presets
    for (int i = 0; i < 8; i++) {
        vec2 uvOffset = presets[i].speed * frameTimeCounter;
        vec2 sampledUV = vTexCoord + uvOffset;

        // Sample directly from local shader pack samplers (with texture/color fallback)
        vec4 sampledTex = vec4(1.0);
        if (i == 0) sampledTex = texture2D(cloudTex0, sampledUV);
        else if (i == 1) sampledTex = texture2D(cloudTex1, sampledUV);
        else if (i == 2) sampledTex = texture2D(cloudTex2, sampledUV);
        else if (i == 3) sampledTex = texture2D(cloudTex3, sampledUV);
        else if (i == 4) sampledTex = texture2D(cloudTex4, sampledUV);
        else if (i == 5) sampledTex = texture2D(cloudTex5, sampledUV);
        else if (i == 6) sampledTex = texture2D(cloudTex6, sampledUV);
        else if (i == 7) sampledTex = texture2D(cloudTex7, sampledUV);

        // Fallback to gtexture or solid mask if local asset has no alpha
        if (sampledTex.a < 0.01) {
            sampledTex = texture2D(gtexture, sampledUV);
            if (sampledTex.a < 0.01) {
                sampledTex = vec4(1.0);
            }
        }

        // Apply directional lighting (Story Mode uniform top/side/bottom shading)
        vec3 faceTint = mix(presets[i].shadowColor, presets[i].highlightColor, isTop * 0.70 + isSide * 0.40);
        if (isBottom > 0.5) {
            faceTint = presets[i].shadowColor;
        }

        vec4 presetFinal = vec4(presets[i].baseColor.rgb * faceTint * sampledTex.rgb, presets[i].baseColor.a * sampledTex.a);

        accumulatedColor += presetFinal * presets[i].weight;
        totalWeight += presets[i].weight;
    }

    if (totalWeight > 0.0) {
        accumulatedColor /= totalWeight;
    }

    // Story Mode Crisp Alpha Cutoff (no blurry fading)
    if (accumulatedColor.a < 0.08) {
        discard;
    }

    // Apply vertex color modulation & distance fog
    accumulatedColor.rgb *= vColor.rgb;
    accumulatedColor.rgb = mix(accumulatedColor.rgb, vec3(0.68, 0.60, 0.88), vFogFactor * 0.45);

    gl_FragColor = accumulatedColor;
}
