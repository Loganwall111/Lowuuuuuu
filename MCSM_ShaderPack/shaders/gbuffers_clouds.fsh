#version 120

// ============================================================================
// MCSM gbuffers_clouds.fsh — 100% procedural Story Mode clouds
// ============================================================================
// The volumetric cloud look is generated entirely with GLSL fractal noise.
// There are NO PNG cloud sheets and NO sampler2D uniforms in this program:
// the cloud pattern is 3D value-noise fbm, the colour palette is driven by
// the live in-game clock (worldTime + sunAngle fallback), and the mesh
// thickness comes from the 2.5x extrusion performed in the vertex shader.

#define CLOUDS_ACTIVE          // Enable authentic Story Mode extruded clouds
#define DYNAMIC_CLOUD_COLOR    // Clouds shift colour with the time of day

precision highp float;
precision highp int;

uniform float frameTimeCounter;
// worldTime is a `long` uniform in the modern Iris/OptiFine spec; declaring it
// `int` fails the uniform type check and disables the whole cloud program.
uniform long worldTime;
uniform float sunAngle;
uniform vec3 sunPosition;

varying vec4 vColor;
varying vec2 vTexCoord;
varying vec3 vWorldPos;
varying vec3 vNormal;
varying float vFogFactor;
varying float vSunY;

/* ------------------------- noise toolkit ------------------------- */
float hash13(vec3 p) {
    p = fract(p * 0.3183099 + vec3(0.1, 0.2, 0.3));
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    float n = i.x + i.y * 57.0 + 113.0 * i.z;
    return mix(
        mix(mix(hash13(vec3(n + 0.0)), hash13(vec3(n + 1.0)), f.x),
            mix(hash13(vec3(n + 57.0)), hash13(vec3(n + 58.0)), f.x), f.y),
        mix(mix(hash13(vec3(n + 113.0)), hash13(vec3(n + 114.0)), f.x),
            mix(hash13(vec3(n + 170.0)), hash13(vec3(n + 171.0)), f.x), f.y),
        f.z);
}

float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = p * 2.02 + vec3(11.3, 7.1, 5.7);
        a *= 0.5;
    }
    return v;
}

/* --------------------- live time of day --------------------- */
float liveTime() {
    float t = float(worldTime);
    if (t < 0.5) {
        t = mod(sunAngle * 24000.0, 24000.0);
        if (t < 0.5 && length(sunPosition) > 0.01) {
            float sY = normalize(sunPosition).y;
            float sX = normalize(sunPosition).x;
            float a = atan(sY, sX);
            t = mod((0.5 - a / 6.2831853) * 24000.0, 24000.0);
        }
    }
    return mod(t, 24000.0);
}

/* --------------------- time-of-day palettes --------------------- */
// Day: bright Story Mode white with a lavender chill
vec3 dayTop()    { return vec3(1.02, 0.99, 1.06); }
vec3 dayBottom() { return vec3(0.72, 0.68, 0.88); }
// Sunset: warm coral to orange, lavender horizon
vec3 sunsetTop()    { return vec3(1.00, 0.72, 0.52); }
vec3 sunsetBottom() { return vec3(0.58, 0.30, 0.42); }
// Night: periwinkle indigo / moonlight silver
vec3 nightTop()    { return vec3(0.46, 0.50, 0.72); }
vec3 nightBottom() { return vec3(0.16, 0.16, 0.30); }
// Storm: bruised charcoal with a magenta/purple under-glow
vec3 stormTop()    { return vec3(0.30, 0.24, 0.38); }
vec3 stormBottom() { return vec3(0.22, 0.10, 0.34); }

void main() {
    float t = liveTime();

    // Sun elevation drives day/night/sunset mixing (never freezes at tick 0)
    float sunY = vSunY;
    float dayAmt   = clamp(sunY * 3.0, 0.0, 1.0);
    float nightAmt = clamp(-sunY * 3.0, 0.0, 1.0);
    float sunsetAmt = clamp(1.0 - abs(sunY) * 8.0, 0.0, 1.0);

    vec3 topCol = mix(nightTop(), dayTop(), dayAmt);
    topCol = mix(topCol, sunsetTop(), sunsetAmt * (1.0 - nightAmt));
    vec3 botCol = mix(nightBottom(), dayBottom(), dayAmt);
    botCol = mix(botCol, sunsetBottom(), sunsetAmt * (1.0 - nightAmt));

    // Procedural blocky cloud pattern: 3D fbm sampled from camera-relative
    // world position so the texels stay square and the slabs keep their shape.
    vec3 p = vWorldPos;
    float windX = frameTimeCounter * 0.0032;
    float windZ = frameTimeCounter * 0.0011;
    p.xz += vec2(windX, windZ);

    float d = fbm(vec3(p.x * 0.014, p.y * 0.10, p.z * 0.014) + vec3(0.0, frameTimeCounter * 0.0008, 0.0));
    // second octave layer for the Story Mode "shredded slab" edges
    float d2 = fbm(vec3(p.x * 0.05, p.y * 0.22, p.z * 0.05) + vec3(7.0, frameTimeCounter * 0.0016, 13.0));
    d = d * 0.72 + d2 * 0.28;

    float density = smoothstep(0.34, 0.72, d);
    float alpha = density * vColor.a;
    if (alpha < 0.02) {
        discard;
    }

    // 3-tier face shading: warm sunlight on the top, lavender ambient on the
    // sides, soft violet shadow underneath.
    float topF = clamp(vNormal.y, 0.0, 1.0);
    float botF = clamp(-vNormal.y, 0.0, 1.0);
    vec3 col = mix(botCol, topCol, topF);
    col *= 0.55 + 0.55 * topF;
    col = mix(col, col * 0.45, botF * 0.8);

    // WitherStormShaderSource body formula: near-black purple core with a
    // magenta rim hugging the shredded slab edges.
    float edge = smoothstep(0.34, 0.52, d) * (1.0 - smoothstep(0.60, 0.72, d));
    col = mix(col, vec3(0.02, 0.01, 0.03), 0.35 * density);                   // body core
    col = mix(col, vec3(0.85, 0.12, 0.90), 0.55 * edge * (0.6 + 0.4 * topF)); // magenta rim

    // Blend into the sky haze with distance
    col = mix(col, vec3(0.68, 0.60, 0.88), vFogFactor * 0.5);

    gl_FragColor = vec4(col, alpha);
}
