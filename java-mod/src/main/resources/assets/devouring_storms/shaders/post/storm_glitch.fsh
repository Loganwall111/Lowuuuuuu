#version 150

// DEVOURING STORMS — storm-glitch post pass.
// Analog-horror presentation: row-slice tearing, RGB split, purple decay grade,
// scanline crawl and invasive purple vignette. All amounts scale with DsIntensity
// (storm proximity / critical state), so idle worlds stay clean.

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float DsTime;
uniform float DsIntensity;

in vec2 texCoord;
out vec4 fragColor;

float hash(float x) {
    return fract(sin(x * 127.1 + 311.7) * 43758.5453);
}

void main() {
    vec2 uv = texCoord;
    float inten = clamp(DsIntensity, 0.0, 1.0);

    // ---- row tearing ----
    float row = floor(uv.y * OutSize.y / 4.0);
    float tearGate = step(0.965 - 0.30 * inten, hash(row + floor(DsTime * 24.0) * 0.137));
    float tearAmt = (hash(row * 3.71 + floor(DsTime * 24.0)) - 0.5) * 0.05 * tearGate * inten;
    uv.x = fract(uv.x + tearAmt);

    // ---- RGB split grows with corruption ----
    float split = 0.0006 + 0.004 * inten;
    vec3 col;
    col.r = texture(DiffuseSampler, uv + vec2(split, 0.0)).r;
    col.g = texture(DiffuseSampler, uv).g;
    col.b = texture(DiffuseSampler, uv - vec2(split, 0.0)).b;

    // ---- decay grade: crush mid-tones, poison greens, feed purples ----
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 grade = mix(col, vec3(lum), 0.35 * inten);
    grade *= vec3(1.0 - 0.08 * inten, 1.0 - 0.28 * inten, 1.0 + 0.22 * inten);
    col = grade;

    // ---- scanlines + hud of static ----
    float scan = 0.96 + 0.04 * sin(uv.y * OutSize.y * 3.14159);
    col *= mix(1.0, scan, 0.35 + 0.4 * inten);
    col += (hash(uv.x * 913.0 + uv.y * 719.0 + DsTime * 61.0) - 0.5) * 0.06 * (0.3 + inten);

    // ---- invasive violet vignette ----
    float d = distance(uv, vec2(0.5));
    float vig = smoothstep(0.30, 0.95, d + 0.15 * inten);
    col = mix(col, vec3(0.09, 0.0, 0.14), vig * (0.35 + 0.45 * inten));

    fragColor = vec4(col, 1.0);
}
