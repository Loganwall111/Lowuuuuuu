#version 330

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

uniform vec3 WitherPosition;     // Boss entity world position
uniform vec3 CameraPosition;   // Camera eye position
uniform mat4 ModelViewProjMat; // Transformation to screen-space

uniform float StormPhase;      // 4.0, 5.1-5.9, 6.0, 6.5, etc.
uniform float FlashIntensity;  // 0.0 to 1.0 driving exponential decay flashbang
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

// 4x4 blocky dithered step function for jagged voxel-aligned edge style
float bayerDither(vec2 coord) {
    vec2 p = floor(coord / 4.0);
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec4 baseColor = texture(MainSampler, texCoord);
    float depth = texture(MainDepthSampler, texCoord).r;
    vec3 col = baseColor.rgb;

    // Project Wither Storm upper back spine position into screen space
    vec3 spinePos = WitherPosition + vec3(0.0, 18.0, 0.0);
    vec4 clipPos = ModelViewProjMat * vec4(spinePos, 1.0);
    vec2 screenBoss = (clipPos.xy / max(clipPos.w, 0.0001)) * 0.5 + 0.5;
    bool behindCam = clipPos.w < 0.0;

    // -------------------------------------------------------------
    // 1. PHASES 5.1 THROUGH 5.9 (Pink Atmosphere & Shadow Occlusion)
    // -------------------------------------------------------------
    if (StormPhase >= 5.1 && StormPhase < 6.0) {
        float phaseWeight = clamp((StormPhase - 5.1) / 0.8, 0.0, 1.0);

        // Wide, horizontally stretched anamorphic ellipsoid ambient glare
        vec2 diff = texCoord - screenBoss;
        float ellipsoid = length(diff * vec2(0.45, 1.35));

        // Intensely saturated pink-magenta (#D81B60) and deep void-violet (#4A148C)
        vec3 pinkMagenta    = vec3(0.847, 0.106, 0.376); // #D81B60
        vec3 deepVoidViolet = vec3(0.290, 0.078, 0.549); // #4A148C

        // High-altitude atmospheric fog / sky transition
        float skyMask = (depth >= 0.9999) ? 1.0 : clamp((1.0 - depth) * 0.45, 0.0, 0.85);
        vec3 atmosFog = mix(deepVoidViolet, pinkMagenta, clamp(texCoord.y * 1.25, 0.0, 1.0));

        // Heavy high-contrast screen-space black shadow silhouette overlay
        float coreShadow = exp(-ellipsoid * 3.5);
        float glareFalloff = exp(-ellipsoid * 1.4);

        if (!behindCam) {
            // Silhouette dynamic dark occlusion effect following boss
            col = mix(col, vec3(0.01, 0.005, 0.02), coreShadow * 0.92 * phaseWeight);
            // Glare bleed into pink-magenta fog
            col = mix(col, atmosFog * 1.25, glareFalloff * 0.65 * phaseWeight * (1.0 - coreShadow));
        }

        // Apply high-altitude pink atmospheric fog to background
        col = mix(col, atmosFog, skyMask * 0.82 * phaseWeight);
    }

    // -------------------------------------------------------------
    // 2. PHASE 6 (Volcanic Red-Orange & Purple Fusion Gradient)
    // -------------------------------------------------------------
    else if (StormPhase >= 6.0 && StormPhase < 6.5) {
        // Deep void-black and electric purple sky matrix
        vec3 voidBlack      = vec3(0.05, 0.01, 0.08);
        vec3 electricPurple = vec3(0.482, 0.122, 0.635); // #7B1FA2

        // Harsh volcanic fire-orange (#FF6D00) and blood-red (#D50000)
        vec3 fireOrange = vec3(1.000, 0.427, 0.000); // #FF6D00
        vec3 bloodRed   = vec3(0.835, 0.000, 0.000); // #D50000

        // Originates from lower horizon / bottom half of viewport and radiates upward
        float verticalGrad = clamp(1.0 - texCoord.y * 1.5, 0.0, 1.0);
        float toBoss = !behindCam ? clamp(1.0 - length(texCoord - screenBoss) * 0.8, 0.0, 1.0) : 0.0;
        float volcanicIntensity = clamp(verticalGrad + toBoss * 0.5, 0.0, 1.0);

        // Noisy blocky dithered step function for jagged voxel-aligned edge style
        float dither = bayerDither(gl_FragCoord.xy);
        float steppedVolcanic = floor((volcanicIntensity + (dither - 0.5) * 0.28) * 4.0) / 4.0;
        steppedVolcanic = clamp(steppedVolcanic, 0.0, 1.0);

        vec3 volcanicMask = mix(bloodRed, fireOrange, clamp(steppedVolcanic * 1.4, 0.0, 1.0));
        vec3 skyMatrix = mix(voidBlack, electricPurple, clamp(texCoord.y, 0.0, 1.0));

        vec3 fusionCol = mix(skyMatrix, volcanicMask, steppedVolcanic);

        float applyFactor = (depth >= 0.9999) ? 0.95 : 0.45;
        col = mix(col, fusionCol, applyFactor);
    }

    // -------------------------------------------------------------
    // 3. PHASE 6.5 (Screen-Space Purple Flashbang: Exponential Decay)
    // -------------------------------------------------------------
    if (FlashIntensity > 0.001) {
        // Maximum white-violet exposure saturation (#E0B0FF)
        vec3 whiteViolet    = vec3(0.878, 0.690, 1.000); // #E0B0FF
        vec3 twilightPurple = vec3(0.320, 0.060, 0.480);

        // Fades smoothly down through deep twilight purple hue
        vec3 flashColor = mix(twilightPurple, whiteViolet, pow(FlashIntensity, 0.65));

        // Blanks out all world geometry at maximum exposure
        col = mix(col, flashColor, clamp(FlashIntensity * 1.25, 0.0, 1.0));
    }

    fragColor = vec4(col, baseColor.a);
}
