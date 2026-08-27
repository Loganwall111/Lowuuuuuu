#version 120

/*
 * MINECRAFT: STORY MODE — OFFICIAL DAY SKY & ROILING CLOUDS
 * Calibrated directly to day_sky.png:
 * Periwinkle zenith -> soft lilac -> pastel mauve-pink -> warm peach -> golden amber horizon
 * Plus Telltale-style roiling storm cumulus clouds with warm lit undersides.
 */

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform float frameTimeCounter;

varying vec4 intColor;
varying vec3 viewPos;

#ifndef MCSM_DAY_SKY
#define MCSM_DAY_SKY 1
#endif
#ifndef MCSM_ROILING_CLOUDS
#define MCSM_ROILING_CLOUDS 1
#endif

// Fast 2D procedural noise
float hash2(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash2(i);
    float b = hash2(i + vec2(1.0, 0.0));
    float c = hash2(i + vec2(0.0, 1.0));
    float d = hash2(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float cloudFbm(vec2 p) {
    float v = 0.0;
    float amp = 0.52;
    for (int i = 0; i < 4; i++) {
        v += amp * vnoise(p);
        p = p * 2.18 + vec2(5.3, 11.7);
        amp *= 0.5;
    }
    return v;
}

// Official day_sky.png 6-stop Story Mode gradient
vec3 getStoryModeDaySky(float elev) {
    vec3 cZenith   = vec3(0.549, 0.529, 0.910); // #8c87e8 soft periwinkle lavender
    vec3 cLilac    = vec3(0.686, 0.608, 0.886); // #af9be2 soft lilac
    vec3 cMauve    = vec3(0.835, 0.682, 0.839); // #d5aed6 pastel mauve-pink
    vec3 cPeach    = vec3(0.957, 0.722, 0.604); // #f4b89a warm peach
    vec3 cApricot  = vec3(0.969, 0.769, 0.451); // #f7c473 golden apricot
    vec3 cHorizon  = vec3(0.973, 0.714, 0.282); // #f8b648 rich golden amber horizon
    vec3 cVoid     = vec3(0.350, 0.220, 0.150); // warm underside ground tone

    if (elev < 0.0) {
        float t = clamp(-elev / 0.20, 0.0, 1.0);
        return mix(cHorizon, cVoid, t);
    } else if (elev < 0.06) {
        return mix(cHorizon, cApricot, smoothstep(0.0, 1.0, elev / 0.06));
    } else if (elev < 0.18) {
        return mix(cApricot, cPeach, smoothstep(0.0, 1.0, (elev - 0.06) / 0.12));
    } else if (elev < 0.38) {
        return mix(cPeach, cMauve, smoothstep(0.0, 1.0, (elev - 0.18) / 0.20));
    } else if (elev < 0.65) {
        return mix(cMauve, cLilac, smoothstep(0.0, 1.0, (elev - 0.38) / 0.27));
    } else {
        return mix(cLilac, cZenith, smoothstep(0.0, 1.0, (elev - 0.65) / 0.35));
    }
}

void main() {
    // Transform view direction into world coordinates
    vec3 dirV = normalize(viewPos);
    vec3 dir = normalize(mat3(gbufferModelViewInverse) * dirV);

    float elev = dir.y;

    #if MCSM_DAY_SKY
    vec3 skyCol = getStoryModeDaySky(elev);

    // Warm luminous horizon glow accent
    float horizBand = exp(-pow(max(elev, 0.0) * 10.0, 2.0));
    skyCol += vec3(0.98, 0.76, 0.45) * horizBand * 0.22;

    // Stylized Telltale Story Mode Roiling Clouds
    #if MCSM_ROILING_CLOUDS
    if (elev > 0.04) {
        vec2 cp = dir.xz / max(elev + 0.18, 0.08);
        float time = frameTimeCounter * 0.018;

        // Roiling swirl
        float c = cloudFbm(cp * 0.85 + vec2(time * 0.4, time * 0.15));
        float detail = cloudFbm(cp * 1.8 - vec2(time * 0.6, time * 0.2));
        float density = c + detail * 0.35;

        float cloudMask = smoothstep(0.48, 0.76, density) * clamp(elev * 3.0, 0.0, 1.0);

        // Story Mode clouds: underlit by warm amber/peach horizon, soft lilac-tinted crowns
        vec3 cloudUnderside = vec3(0.96, 0.72, 0.58);
        vec3 cloudCrown     = vec3(0.98, 0.96, 1.00);
        vec3 cloudCol = mix(cloudUnderside, cloudCrown, smoothstep(0.40, 0.82, detail));

        skyCol = mix(skyCol, cloudCol, cloudMask * 0.72);
    }
    #endif

    gl_FragColor = vec4(skyCol, 1.0);
    #else
    gl_FragColor = intColor;
    #endif
}
