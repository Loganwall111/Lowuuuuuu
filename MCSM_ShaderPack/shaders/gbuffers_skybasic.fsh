#version 120

/*
 * MINECRAFT: STORY MODE — AUTHENTIC PINK TWILIGHT SKY & ATMOSPHERE
 * Recreates the iconic Story Mode sky gradient (deep navy -> rich purple ->
 * vibrant rose pink -> warm glowing peach horizon) with roiling storm clouds.
 */

uniform mat4 gbufferModelView;
uniform float frameTimeCounter;

varying vec4 intColor;
varying vec3 viewPos;

#ifndef MCSM_PINK_SKY
#define MCSM_PINK_SKY 1
#endif
#ifndef MCSM_HORIZON_GLOW
#define MCSM_HORIZON_GLOW 1
#endif
#ifndef MCSM_ROILING_CLOUDS
#define MCSM_ROILING_CLOUDS 1
#endif

// Fast 2D hash and noise
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += amp * vnoise(p);
        p = p * 2.15 + vec2(11.3, 7.1);
        amp *= 0.5;
    }
    return v;
}

// Authentic Minecraft: Story Mode Sky Gradient (calibrated to sky_only_no_clouds.png)
vec3 getStoryModeSky(float elev) {
    vec3 cVoid    = vec3(0.047, 0.024, 0.094); // #0c0618 under-horizon void
    vec3 cPeach   = vec3(0.992, 0.765, 0.549); // #fdc38c warm peach horizon
    vec3 cCoral   = vec3(0.925, 0.596, 0.569); // #ec9891 soft coral pink
    vec3 cRose    = vec3(0.773, 0.447, 0.557); // #c5728e rich rose pink
    vec3 cPink    = vec3(0.592, 0.290, 0.502); // #974a80 vibrant magenta pink
    vec3 cMagenta = vec3(0.416, 0.192, 0.459); // #6a3175 royal magenta purple
    vec3 cViolet  = vec3(0.267, 0.110, 0.416); // #441c6a rich violet purple
    vec3 cPurple  = vec3(0.137, 0.059, 0.310); // #230f4f deep purple
    vec3 cIndigo  = vec3(0.063, 0.035, 0.188); // #100930 deep midnight indigo zenith

    if (elev < 0.0) {
        float t = clamp(-elev / 0.25, 0.0, 1.0);
        return mix(cPeach, cVoid, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.08) {
        float t = elev / 0.08;
        return mix(cPeach, cCoral, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.18) {
        float t = (elev - 0.08) / 0.10;
        return mix(cCoral, cRose, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.30) {
        float t = (elev - 0.18) / 0.12;
        return mix(cRose, cPink, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.42) {
        float t = (elev - 0.30) / 0.12;
        return mix(cPink, cMagenta, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.58) {
        float t = (elev - 0.42) / 0.16;
        return mix(cMagenta, cViolet, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.75) {
        float t = (elev - 0.58) / 0.17;
        return mix(cViolet, cPurple, smoothstep(0.0, 1.0, t));
    } else {
        float t = clamp((elev - 0.75) / 0.25, 0.0, 1.0);
        return mix(cPurple, cIndigo, smoothstep(0.0, 1.0, t));
    }
}

void main() {
    // Un-rotate view vector to world space unit vector
    vec3 dirV = normalize(viewPos);
    vec3 dir = normalize(transpose(mat3(gbufferModelView)) * dirV);

    float elev = dir.y;

    #if MCSM_PINK_SKY
    vec3 skyCol = getStoryModeSky(elev);

    // Warm luminous horizon glow accent
    #if MCSM_HORIZON_GLOW
    float horizBand = exp(-pow(max(elev, 0.0) * 8.0, 2.0));
    skyCol += vec3(0.99, 0.75, 0.52) * horizBand * 0.28;
    #endif

    // Roiling Minecraft Story Mode storm clouds
    #if MCSM_ROILING_CLOUDS
    if (elev > 0.05) {
        vec2 cp = dir.xz / max(elev + 0.22, 0.1);
        float time = frameTimeCounter * 0.025;
        float ang = atan(cp.y, cp.x) + time * 0.4;
        float rad = length(cp);
        vec2 swirlPos = vec2(cos(ang), sin(ang)) * rad;

        float c = fbm(swirlPos * 1.1 + vec2(time * 0.5, time * 0.3));
        float cloudMask = smoothstep(0.42, 0.78, c) * clamp(elev * 2.2, 0.0, 1.0);

        // Story Mode clouds: underlit by pink/magenta horizon, darker crowns
        vec3 cloudBase = vec3(0.12, 0.05, 0.20);
        vec3 cloudRim  = vec3(0.85, 0.45, 0.68);
        float under = fbm(swirlPos * 2.2 - vec2(time * 0.7, 0.0));
        vec3 cloudCol = mix(cloudBase, cloudRim, smoothstep(0.35, 0.80, under) * 0.75);

        skyCol = mix(skyCol, cloudCol, cloudMask * 0.65);
    }
    #endif

    // Subtle twinkling stars in the high indigo dome
    if (elev > 0.45) {
        vec2 starPos = dir.xz / (elev + 0.2);
        float s = hash(floor(starPos * 180.0));
        if (s > 0.991) {
            float twinkle = 0.5 + 0.5 * sin(frameTimeCounter * 3.5 + s * 6.28);
            skyCol += vec3(0.9, 0.85, 1.0) * twinkle * (elev - 0.45) * 1.4;
        }
    }

    gl_FragColor = vec4(skyCol, 1.0);
    #else
    gl_FragColor = intColor;
    #endif
}
