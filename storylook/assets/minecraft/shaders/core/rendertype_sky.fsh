#version 150

// Story Look sky: pastel gradient + stacked cloud decks with void gaps.
// Decks accumulate front-to-back, so from the ground the lowest deck
// occludes everything above it; the stack ENDS at the top height.
in vec4 vertexColor;
in vec3 viewDir;

out vec4 fragColor;

uniform float GameTime;

const int DECKS = 13;
// heights above the camera: two adjacent pairs, void gaps between groups,
// and a hard ceiling -- the layers do not go on forever
const float H0 = 96.0;   const float H1 = 140.0;  const float H2 = 152.0;
const float H3 = 260.0;  const float H4 = 420.0;  const float H5 = 430.0;
const float H6 = 700.0;  const float H7 = 1200.0; const float H8 = 2000.0;
const float H9 = 3500.0; const float H10 = 6000.0; const float H11 = 10000.0;
const float H12 = 16000.0;

float heightAt(int i) {
    if (i == 0) return H0;   if (i == 1) return H1;  if (i == 2) return H2;
    if (i == 3) return H3;   if (i == 4) return H4;  if (i == 5) return H5;
    if (i == 6) return H6;   if (i == 7) return H7;  if (i == 8) return H8;
    if (i == 9) return H9;   if (i == 10) return H10; if (i == 11) return H11;
    return H12;
}
float coverAt(int i) {
    if (i == 0) return 0.60; if (i == 1) return 0.50; if (i == 2) return 0.46;
    if (i == 3) return 0.55; if (i == 4) return 0.60; if (i == 5) return 0.52;
    if (i == 6) return 0.45; if (i == 7) return 0.50; if (i == 8) return 0.42;
    if (i == 9) return 0.40; if (i == 10) return 0.36; if (i == 11) return 0.30;
    return 0.24;
}
float scaleAt(int i) {
    return 0.0016 * pow(1.35, float(i));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}
float fbm(vec2 p) {
    float a = 0.5;
    float s = 0.0;
    for (int i = 0; i < 5; i++) {
        s += a * noise(p);
        p = p * 2.03 + vec2(11.3, 7.9);
        a *= 0.5;
    }
    return s;
}

void main() {
    vec3 rd = normalize(viewDir);
    vec3 base = vertexColor.rgb;

    // pastel story gradient, keyed off vanilla's day/night vertex color so
    // dawn stays pink-lavender, noon stays cyan-bright, night stays deep blue
    float up = clamp(rd.y, -1.0, 1.0);
    vec3 zenith = base * vec3(0.80, 0.90, 1.14) + vec3(0.015, 0.025, 0.055);
    vec3 horizon = base * vec3(1.14, 1.02, 1.06) + vec3(0.055, 0.040, 0.050);
    vec3 col = mix(horizon, zenith, smoothstep(-0.02, 0.35, up));
    col = mix(col, base, 0.25);

    if (rd.y > 0.015) {
        float drift = GameTime * 900.0;
        vec3 acc = vec3(0.0);
        float accA = 0.0;
        for (int i = 0; i < DECKS; i++) {
            if (accA > 0.94) break;
            float t = heightAt(i) / rd.y;
            vec2 hit = rd.xz * t;
            hit += vec2(drift * (0.020 + 0.006 * float(i)),
                        drift * (0.013 + 0.004 * float(i)));
            vec2 q = hit * scaleAt(i);
            float body = fbm(q);
            float puffs = fbm(q * 2.7 + 13.7);   // clouds inside clouds
            float d = body * 0.72 + puffs * 0.38;
            float c = coverAt(i);
            float a = smoothstep(c, c + 0.22, d);
            a *= smoothstep(0.015, 0.06, rd.y);          // horizon soften
            a *= 0.72 + 0.28 * noise(q * 5.1 + 3.3);     // nested wisps
            if (a > 0.004) {
                vec3 deckCol = mix(vec3(0.74, 0.72, 0.82), vec3(1.03, 1.03, 1.06),
                                   smoothstep(0.30, 0.70, d));
                deckCol *= mix(vec3(1.0), base * 1.5 + 0.22, 0.35);
                float w = a * (1.0 - accA);
                acc += deckCol * w;
                accA += w;
            }
        }
        col = mix(col, acc, accA);
    }

    fragColor = vec4(col, vertexColor.a);
}
