/*
============================================================================
 STORY MODE VISUALS — shared library
 Telltale "Minecraft: Story Mode" style renderer for Iris / Oculus 1.20.1
 ----------------------------------------------------------------------------
 Contents:
   hashes & 3D value noise        (procedural sky / clouds / water jitter)
   RGB <-> HSV, smoothstep curves (time-of-day color grading)
   gamma helpers, SDR tonemapper  (cinematic contrast)
   14 hardcoded BIOME FOG PROFILES with smooth crossfade weights
   procedural star field
   Story Mode LUT (grade)         (warm high-contrast Season-1 grade)
============================================================================
*/

// ---------------------------------------------------------------- hashes
float hash12(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}
float hash13(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453123);
}
vec3  hash33(vec3 p) {
    return vec3(hash13(p), hash13(p + 91.7), hash13(p + 213.3));
}
vec2  hash22(vec2 p) {
    return vec2(hash12(p), hash12(p + 91.7));
}

// ------------------------------------------------------------- 3D noise
float vnoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(mix(hash13(i + vec3(0,0,0)), hash13(i + vec3(1,0,0)), u.x),
            mix(hash13(i + vec3(0,1,0)), hash13(i + vec3(1,1,0)), u.x), u.y),
        mix(mix(hash13(i + vec3(0,0,1)), hash13(i + vec3(1,0,1)), u.x),
            mix(hash13(i + vec3(0,1,1)), hash13(i + vec3(1,1,1)), u.x), u.y),
        u.z);
}
float fbm3(vec3 p) {
    return vnoise(p) * 0.55 + vnoise(p * 2.13) * 0.30 + vnoise(p * 4.71) * 0.15;
}

// ------------------------------------------------------------- color ops
vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}
vec3 hsv2rgb(vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

// smootherstep + gamma helpers
float sstep(float a, float b, float x) {
    x = clamp((x - a) / max(b - a, 1e-5), 0.0, 1.0);
    return x * x * (3.0 - 2.0 * x);
}
vec3 toLinear(vec3 c)  { return pow(max(c, vec3(0.0)), vec3(2.2)); }
vec3 toGamma(vec3 c)   { return pow(max(c, vec3(0.0)), vec3(1.0 / 2.2)); }

// ------------------------------------------------------------- SDR grade
vec3 sdrTonemap(vec3 c) {
    vec3 t = toLinear(c);
    t = t * (2.51 * t + vec3(0.03)) / (t * (2.43 * t + vec3(0.59)) + vec3(0.14)); // ACES fit
    return toGamma(clamp(t, 0.0, 1.0));
}

// ------------------------------------------------ Story Mode LUT (Season 1)
// High saturation, warm highlights, teal-purple shadow split.
vec3 grade(vec3 c) {
    float sat = 1.18;
    float luma = dot(c, vec3(0.2126, 0.7152, 0.0722));
    vec3 graded = mix(vec3(luma), c, sat);
    graded *= vec3(1.02, 1.00, 0.97);
    graded = pow(graded, vec3(1.04, 1.00, 0.96));              // warm lift
    float shad = sstep(1.0, 0.0, luma);
    graded += vec3(-0.015, 0.004, 0.028) * shad;               // cool shadows
    float hi = sstep(0.78, 1.0, luma);
    graded += vec3(0.022, 0.010, -0.006) * hi;                 // warm highlights
    return sdrTonemap(graded);
}

// ------------------------------------------------------------------ stars
float starLayer(vec3 dir, float density, float twinkle) {
    vec3 p = normalize(dir + 0.0001) * 160.0;
    vec3 id = floor(p);
    vec3 f = fract(p) - 0.5;
    float star = 0.0;
    for (int i = 0; i < 3; i++) {
        vec3 o = vec3(float(i), float(i * 2 + 1), float(i * 3 + 2));
        vec3 h = hash33(id + o) - 0.5;
        float d = length(f - h * 1.4);
        star += smoothstep(0.06, 0.0, d) * step(density, hash13(id + o + 7.0));
    }
    float tw = 0.75 + 0.25 * sin(twinkle * 0.6 + hash13(id) * 6.283);
    return clamp(star * tw, 0.0, 1.0);
}

// --------------------------------------------------- procedural moon disk
vec3 moonGlow(vec3 dir, vec3 moonDir, float moonPhase) {
    float disk = smoothstep(0.9955, 0.9975, dot(dir, moonDir));
    vec2 lp = dir.xy * 24.0;
    float crater = vnoise(vec3(lp * 1.7, 3.0)) * 0.12 + vnoise(vec3(lp * 5.0, 9.0)) * 0.05;
    float terminator = mix(1.0 - moonPhase, moonPhase, step(0.0, dir.x));
    float shade = clamp(terminator + crater, 0.0, 1.0);
    return vec3(1.0, 1.0, 0.97) * shade * (0.25 + 0.75 * disk);
}

// =================================================================
// BIOME FOG PROFILES - 14 hardcoded presets, smoothly crossfaded
// =================================================================
vec4 fogProfile(int id) {
    // .rgb = fog color, .a = fog density
    if (id == 0)  return vec4(0.31, 0.38, 0.48, 0.55); // plains - clean cyan-violet
    if (id == 1)  return vec4(0.28, 0.42, 0.40, 0.62); // forest - deep green
    if (id == 2)  return vec4(0.92, 0.66, 0.72, 0.60); // cherry - pink blossom haze
    if (id == 3)  return vec4(0.35, 0.60, 0.42, 0.72); // jungle - humid emerald
    if (id == 4)  return vec4(0.93, 0.72, 0.33, 0.46); // desert - golden heat-glare
    if (id == 5)  return vec4(0.85, 0.50, 0.25, 0.44); // badlands - rust glare
    if (id == 6)  return vec4(0.88, 0.72, 0.38, 0.52); // savanna - dry gold
    if (id == 7)  return vec4(0.28, 0.40, 0.27, 0.95); // swamp - thick mossy mist
    if (id == 8)  return vec4(0.62, 0.72, 0.88, 0.68); // snowy - crisp lavender
    if (id == 9)  return vec4(0.55, 0.66, 0.80, 0.66); // taiga - cool pine
    if (id == 10) return vec4(0.58, 0.60, 0.84, 0.60); // mountains - lavender fade
    if (id == 11) return vec4(0.28, 0.50, 0.68, 0.50); // ocean - blue abyss
    if (id == 12) return vec4(0.72, 0.56, 0.85, 0.70); // mushroom - spore haze
    return            vec4(0.30, 0.28, 0.22, 0.78);     // caves - pitch mist
}

// Continuous biome crossfade: weights for the nearest 8 voxel corners
void biomeWeights(vec3 pos, out float w[8], out ivec4 id[8]) {
    vec3 c = pos * 0.004 + 1000.0;               // per-biome = per-64-block cell
    vec3 f = fract(c);
    for (int k = 0; k < 8; k++) {
        vec3 o = vec3(float(k & 1), float((k >> 1) & 1), float((k >> 2) & 1));
        vec3 d = f - o;
        w[k] = (1.0 - abs(d.x)) * (1.0 - abs(d.y)) * (1.0 - abs(d.z));
        id[k] = ivec4(int(hash13(floor(c) + o) * 14.0), 0, 0, 0);
    }
}
vec4 sampledFog(vec3 pos) {
    float w[8]; ivec4 id[8];
    biomeWeights(pos, w, id);
    vec4 fog = vec4(0.0);
    float sum = 0.0;
    for (int k = 0; k < 8; k++) {
        vec4 p = fogProfile(id[k].x);
        fog += p * w[k];
        sum += w[k];
    }
    return fog / max(sum, 1e-4);
}
