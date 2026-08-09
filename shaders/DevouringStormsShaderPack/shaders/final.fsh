#version 120

/*
 * DEVOURING STORMS — final present pass.
 * The signature look: decay grade, row tearing, RGB split, scanlines, film grain,
 * and the invasive violet vignette. Corruption breathes with rain and storm weather
 * (rainStrength), slowly waking when the world turns against you.
 */

uniform sampler2D colortex0;
uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;
uniform float rainStrength;
uniform float frameTime;

varying vec2 texcoord;

/* ---- user tunables (also overridable in shaders.properties) ----
 * DS_LENSING   : gravitational lensing while storm weather stirs — the Void Maw's
 *                presence bends the frame around screen-centre. 0=off, 1=full.
 * DS_GRAIN     : film grain amount multiplier.
 * DS_VIGNETTE  : invasive violet vignette strength.
 * DS_CHROMA    : chromatic aberration intensity multiplier.
 */
#ifndef DS_LENSING
#define DS_LENSING 1
#endif
#ifndef DS_GRAIN
#define DS_GRAIN 1
#endif
#ifndef DS_VIGNETTE
#define DS_VIGNETTE 1
#endif
#ifndef DS_CHROMA
#define DS_CHROMA 1
#endif
/* DS_VHS: the crater vision. Adds the tracking band, the PLAY counter's cold white,
 * and tape dropout during heavy storm weather. 0=off, 1=on. */
#ifndef DS_VHS
#define DS_VHS 1
#endif

float hash(float x) {
    return fract(sin(x * 127.1 + 311.7) * 43758.5453);
}

void main() {
    vec2 uv = texcoord;

    // corruption: a living base pulse, sharpened by storm weather
    float pulse = 0.5 + 0.5 * sin(frameTimeCounter * 0.32);
    float inten = clamp(0.18 + 0.10 * pulse + 0.55 * rainStrength, 0.0, 1.0);

    // ---- row tearing ----
    float row = floor(uv.y * viewHeight / 4.0);
    float gate = step(0.965 - 0.30 * inten, hash(row + floor(frameTimeCounter * 24.0) * 0.137));
    float tear = (hash(row * 3.71 + floor(frameTimeCounter * 24.0)) - 0.5) * 0.05 * gate * inten;
    uv.x = fract(uv.x + tear);

    // ---- gravitational lensing: black holes do this for free ----
    // A slow precessing maw sinks into the frame during storm weather: light that
    // passes nearby wraps around the singularity — the classic Einstein ring,
    // done honest-to-GLSL-120 with two samples and no apologies to physics.
    #if DS_LENSING
    {
        float t = frameTimeCounter * 0.05;
        vec2 maw = vec2(0.5 + 0.22 * cos(t), 0.6 + 0.13 * sin(t * 1.7));
        vec2 dd = uv - maw;
        float r = length(dd);
        float ring = exp(-pow((r - 0.16), 2.0) / 0.004) * 2.2;          // Einstein ring pullback
        float lens = (ring + 1.6 / max(r * 14.0, 0.6) ) * rainStrength * inten;
        uv = uv - normalize(dd + 0.0001) * lens * 0.045;
        // inside the photon sphere: nothing. a bite of pure dark.
        float dark = smoothstep(0.05, 0.015, r) * rainStrength * 0.9;
        uv = mix(uv, maw + dd * 0.3, dark);
    }
    #endif

    // ---- RGB split ----
    float split = (0.0006 + 0.004 * inten) * float(DS_CHROMA);
    vec3 col;
    col.r = texture2D(colortex0, uv + vec2(split, 0.0)).r;
    col.g = texture2D(colortex0, uv).g;
    col.b = texture2D(colortex0, uv - vec2(split, 0.0)).b;

    // ---- decay grade: poison greens, crush mids, feed purples ----
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 grade = mix(col, vec3(lum), 0.30 * inten);
    grade *= vec3(1.0 - 0.08 * inten, 1.0 - 0.24 * inten, 1.0 + 0.20 * inten);
    col = grade;

    // ---- scanlines + grain ----
    float scan = 0.97 + 0.03 * sin(uv.y * viewHeight * 3.14159);
    col *= mix(1.0, scan, 0.3 + 0.3 * inten);
    col += (hash(uv.x * 913.0 + uv.y * 719.0 + frameTimeCounter * 61.0) - 0.5) * 0.05 * (0.25 + inten) * float(DS_GRAIN);

    // ---- invasive violet vignette ----
    float d = distance(uv, vec2(0.5));
    float vig = smoothstep(0.32, 0.95, d + 0.12 * inten);
    col = mix(col, vec3(0.08, 0.0, 0.13), vig * (0.30 + 0.45 * inten) * float(DS_VIGNETTE));

    // ---- THE VISION (DS_VHS): during storm weather your feed goes to tape.
    // A tracking band crawls up the screen, smearing any row it touches; the cold
    // white counter sits at the bottom right because someone somewhere is logging this.
    #if DS_VHS
    {
        float drive = rainStrength * (0.6 + 0.4 * pulse);
        float bandPos = fract(frameTimeCounter * 0.05);
        float band = exp(-pow((uv.y - bandPos) * viewHeight / 8.0, 2.0));
        col = mix(col, vec3(dot(col, vec3(0.33))), band * 0.35 * drive);
        // dropouts: a scanline that lost its job
        float do1 = step(0.992, hash(floor(uv.y * viewHeight) + floor(frameTimeCounter * 17.0) * 0.31));
        col = mix(col, vec3(0.96, 0.97, 1.0), do1 * 0.18 * drive);
        col.r = mix(col.r, col.g, band * 0.25 * drive);   // colour bleeding when the band passes
        // letterbox hint: the feed always knew its aspect
        float bars = step(uv.y, 0.06) + step(0.94, uv.y);
        col = mix(col, vec3(0.0), bars * 0.85 * drive);
    }
    #endif

    gl_FragColor = vec4(col, 1.0);
}
