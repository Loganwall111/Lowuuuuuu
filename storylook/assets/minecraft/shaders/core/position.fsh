#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec3 skyDir;

out vec4 fragColor;

// ---------------------------------------------------------------------------
// Devouring Storms: Story Look -- sky dome (26.2).
// Palettes are linearized samples of the Minecraft Story Mode reference
// shots: dawn (EnderCon gate), midday (Sky City), night (floating island).
// Time of day comes from the world clock (GameTime); the sky-colour hue key
// is only a fallback for the AMD GameTime==0 driver bug.
// ---------------------------------------------------------------------------

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float vnoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash13(i);
    float b = hash13(i + vec3(1.0, 0.0, 0.0));
    float c = hash13(i + vec3(0.0, 1.0, 0.0));
    float d = hash13(i + vec3(1.0, 1.0, 0.0));
    float e = hash13(i + vec3(0.0, 0.0, 1.0));
    float g = hash13(i + vec3(1.0, 0.0, 1.0));
    float h = hash13(i + vec3(0.0, 1.0, 1.0));
    float k = hash13(i + vec3(1.0, 1.0, 1.0));
    return mix(mix(mix(a, b, f.x), mix(c, d, f.x), f.y),
               mix(mix(e, g, f.x), mix(h, k, f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float s = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        s += a * vnoise(p);
        p *= 2.03;
        a *= 0.5;
    }
    return s;
}

void main() {
    // Non-opaque position-shader users (world-select highlight etc.) keep
    // the exact vanilla behaviour.
    if (ColorModulator.a < 0.99) {
        fragColor = apply_fog(ColorModulator, sphericalVertexDistance, cylindricalVertexDistance,
            FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
        return;
    }

    // --- time-of-day weights -------------------------------------------------
    float night = 0.0;
    float dawn = 0.0;
    float day = 1.0;
    // Storm override: the Devouring Storms mod tints the sky dark
    // red/pink/purple/magenta during storm phases. That colour must pass
    // through, not be reinterpreted as a sunrise (1.9.120 shipped that bug:
    // a lavender mush at midday whenever a storm tint was active).
    vec3 C = ColorModulator.rgb;
    float clum = dot(C, vec3(0.2126, 0.7152, 0.0722));
    bool storm = (C.r > C.g * 1.25 && C.b > C.g * 1.05)
              || (C.r > C.g * 1.25 && clum < 0.18);
    if (storm) {
        float t = pow(1.0 - clamp(normalize(skyDir).y, 0.0, 1.0), 1.4);
        vec3 scol = mix(C * 0.45, C * 1.05, t);
        vec3 dirS = normalize(skyDir);
        if (dirS.y > 0.02) {
            vec2 pxz = dirS.xz / dirS.y;
            float cov = fbm(vec3(pxz * 1.6, 3.7));
            float a = smoothstep(0.50, 0.66, cov) * 0.5;
            scol = mix(scol, mix(C * 1.3, vec3(1.0), 0.25), a);
        }
        fragColor = vec4(scol, 1.0);
        return;
    }
    // Night = dark sky. Dawn = STRONG orange only (vanilla sunrise), and
    // never when the sky is cool/lavender (the mod's story grade tints the
    // sky warm at midday; the old keys read that as sunrise and painted the
    // whole dome periwinkle).
    night = 1.0 - smoothstep(0.05, 0.22, clum);
    float orange = C.r - C.b;
    dawn = smoothstep(0.25, 0.50, orange) * step(C.b, C.g) * (1.0 - night);
    day = max(1.0 - night - dawn, 0.0);

    // Gradient stops, linear light, sampled from the reference images.
    vec3 zen = day * vec3(0.108, 0.530, 0.830)
             + dawn * vec3(0.381, 0.456, 0.776)
             + night * vec3(0.0033, 0.0052, 0.029);
    vec3 mid = day * vec3(0.210, 0.610, 0.840)
             + dawn * vec3(0.620, 0.560, 0.810)
             + night * vec3(0.010, 0.014, 0.070);
    vec3 hor = day * vec3(0.420, 0.790, 0.940)
             + dawn * vec3(0.890, 0.680, 0.730)
             + night * vec3(0.019, 0.031, 0.130);

    // Story Mode had a distinct sky per biome. Vanilla hands us the biome's
    // own sky colour in ColorModulator, so its hue family picks a variant
    // palette: lush greens (swamp/jungle), warm sands (desert/badlands),
    // or the default story blue.
    float gk = clamp((C.g - max(C.r, C.b)) * 3.0, 0.0, 0.6) * day;
    float wk = clamp((C.r - C.b) * 1.2, 0.0, 0.6) * day * (1.0 - dawn);
    zen = mix(zen, vec3(0.130, 0.450, 0.500), gk);
    mid = mix(mid, vec3(0.300, 0.600, 0.550), gk);
    hor = mix(hor, vec3(0.550, 0.800, 0.700), gk);
    zen = mix(zen, vec3(0.350, 0.450, 0.700), wk);
    mid = mix(mid, vec3(0.550, 0.600, 0.750), wk);
    hor = mix(hor, vec3(0.850, 0.750, 0.650), wk);

    vec3 dir = normalize(skyDir);
    float t = pow(1.0 - clamp(dir.y, 0.0, 1.0), 1.5);
    vec3 col = mix(zen, mid, smoothstep(0.05, 0.5, t));
    col = mix(col, hor, smoothstep(0.5, 0.95, t));

    // Crisp stars at night.
    vec3 sg = floor(dir * 220.0);
    float sn = hash13(sg);
    float star = smoothstep(0.996, 0.9995, sn) * night;
    col += star * (0.55 + 0.45 * hash13(sg + 7.7)) * vec3(0.92, 0.96, 1.0);

    // Stacked cloud decks: nine heights, adjacent pairs, void gaps between
    // groups, ridge noise nesting clouds inside clouds, front-to-back
    // occlusion so low decks hide the ones far above, and a dense ceiling
    // deck so the stack ends instead of going on forever. Patches are small
    // and opaque white like the reference shots.
    if (dir.y > 0.02) {
        vec2 pxz = dir.xz / dir.y;
        vec2 drift = vec2(0.0);
        float H[9];
        H[0] = 96.0;  H[1] = 146.0; H[2] = 152.0; H[3] = 420.0; H[4] = 430.0;
        H[5] = 1200.0; H[6] = 3500.0; H[7] = 9000.0; H[8] = 16000.0;
        float acc = 0.0;
        for (int i = 0; i < 9; i++) {
            vec2 uv = pxz * (120.0 / pow(H[i] / 96.0, 0.55)) + drift * (1.0 + float(i) * 0.15) + vec2(float(i) * 7.3);
            float cov = fbm(vec3(uv * 0.9, float(i) * 3.1));
            // void gaps: a second low-frequency mask carves empty sky between
            // cloud groups so the gaps read from the ground
            float gapmask = smoothstep(0.34, 0.46, fbm(vec3(uv * 0.33, float(i) * 9.0)));
            float nest = fbm(vec3(uv * 3.4 + 17.0, float(i) * 5.7));
            // low decks sparse, mid decks bold, ceiling dense: distinct
            // layers instead of one occluding blanket
            float th = (i < 3) ? 0.60 : ((i < 7) ? 0.48 : 0.42);
            float ceilBonus = (i == 8) ? 0.25 : 0.0;
            float a = smoothstep(th, th + 0.14, cov) * gapmask
                    * (0.72 + 0.28 * smoothstep(0.35, 0.75, nest))
                    + ceilBonus * smoothstep(0.35, 0.6, cov);
            a = min(a, 0.9) * (1.0 - acc);
            vec3 dc = mix(vec3(1.0), hor, 0.10) * (day * 1.0 + dawn * 0.97 + night * 0.25);
            dc *= (mod(float(i), 2.0) < 0.5) ? 1.0 : 0.955;
            col = mix(col, dc, a);
            acc += a * 0.85;
            if (acc > 0.97) {
                break;
            }
        }
    } else {
        // Below the horizon: fade to a deeper void tone, never the pale wash.
        col = mix(col, hor * 0.5, smoothstep(0.0, -0.3, dir.y));
    }

    fragColor = vec4(col, 1.0);
}
