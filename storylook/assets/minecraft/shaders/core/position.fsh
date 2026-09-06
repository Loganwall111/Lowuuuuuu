#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec3 skyDir;

out vec4 fragColor;

// ---------------------------------------------------------------------------
// Devouring Storms: Story Look -- sky dome (26.2), round 5.
// Every palette below is a gradient stop sampled from the Minecraft Story
// Mode reference frames supplied by the player, mapped to the phase whose
// sky colour the mod feeds us through ColorModulator:
//   day      - EnderCon gate / Sky City aerials (soft pastel story blue)
//   dawn     - vanilla-strong-orange sunrise only
//   night    - floating-island night
//   pinkK    - phases 5.5-5.9: violet zenith, magenta mid, SALMON-PINK
//              horizon (the purple body comes from the storm blob, not sky)
//   greenK   - the green-teal frames: desaturated teal dome, pale horizon
//   orangeK  - sunset frames: mauve-brown zenith into burnt orange horizon
//   magK     - deep-purple frames: purple zenith, magenta mid, pink horizon
// Blending is continuous everywhere (no roof/side seam), with a horizon glow
// band, a blue silhouette rim hugging the horizon (phases 4/5), a purple line
// across the upper vault and a darker roof tone, exactly per the notes.
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

// layered cloud decks, shared by calm and storm skies
vec3 paintDecks(vec3 dirS, vec3 col, float acc0, vec3 litCol, vec3 shadeCol,
                float dayness, float warm) {
    if (dirS.y <= 0.02) {
        return col;
    }
    vec2 pxz = dirS.xz / dirS.y;
    float H[9];
    H[0] = 96.0;  H[1] = 146.0; H[2] = 152.0; H[3] = 420.0; H[4] = 430.0;
    H[5] = 1200.0; H[6] = 3500.0; H[7] = 9000.0; H[8] = 16000.0;
    float acc = acc0;
    for (int i = 0; i < 9; i++) {
        int grp = (i < 3) ? 0 : ((i < 6) ? 1 : 2);
        vec2 uv = pxz * (120.0 / pow(H[i] / 96.0, 0.55)) + vec2(float(i) * 7.3);
        float pres = (grp == 0) ? 1.0
                : smoothstep(0.30, 0.44, fbm(vec3(pxz * 0.010 + vec2(float(grp) * 31.7), float(grp) * 13.0)));
        float cov = fbm(vec3(uv * 0.9, float(i) * 3.1));
        float gapmask = smoothstep(0.40, 0.54, fbm(vec3(uv * 0.33, float(i) * 9.0)));
        float nest = fbm(vec3(uv * 3.4 + 17.0, float(i) * 5.7));
        float th = (i < 3) ? 0.62 : ((i < 7) ? 0.50 : 0.44);
        float ceilBonus = (i == 8) ? 0.25 : 0.0;
        float a = smoothstep(th, th + 0.08, cov) * gapmask * pres
                * (0.70 + 0.30 * smoothstep(0.35, 0.75, nest))
                + ceilBonus * smoothstep(0.35, 0.6, cov) * pres;
        // soften the deck edge into the horizon: kills the roof/side seam
        a *= smoothstep(0.02, 0.12, dirS.y);
        a = min(a, 0.92) * (1.0 - acc);
        float core = smoothstep(th - 0.12, th + 0.34, cov);
        vec3 dc = mix(shadeCol, litCol, 0.55 + 0.45 * core);
        dc *= (0.97 + 0.05 * float(i));
        dc = mix(dc, dc * vec3(1.06, 0.98, 0.88), (1.0 - clamp(dirS.y, 0.0, 1.0)) * warm);
        col = mix(col, dc, a);
        acc += a * 0.85;
        if (acc > 0.97) {
            break;
        }
    }
    return col;
}

void main() {
    // Non-opaque position-shader users (world-select highlight etc.) keep
    // the exact vanilla behaviour.
    if (ColorModulator.a < 0.99) {
        fragColor = apply_fog(ColorModulator, sphericalVertexDistance, cylindricalVertexDistance,
            FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
        return;
    }

    vec3 C = ColorModulator.rgb;
    float clum = dot(C, vec3(0.2126, 0.7152, 0.0722));
    vec3 dirS = normalize(skyDir);
    float ty = clamp(dirS.y, -1.0, 1.0);
    float t = pow(1.0 - clamp(ty, 0.0, 1.0), 1.35);

    // --- storm phases: the mod tints the sky per phase; map that tint to the
    //     reference frame whose palette belongs to it -------------------------
    bool storm = (C.r > C.g * 1.25 && C.b > C.g * 1.05)
              || (C.r > C.g * 1.25 && clum < 0.18);
    if (storm) {
        float greenK  = clamp((C.g - max(C.r, C.b)) * 2.5, 0.0, 1.0);
        float orangeK = clamp((C.r - C.g) * 2.2, 0.0, 1.0) * (1.0 - greenK);
        float pinkK   = clamp(1.0 - abs(C.r - C.b) * 3.0, 0.0, 1.0)
                      * step(C.g * 1.05, min(C.r, C.b)) * (1.0 - greenK);
        float magK    = clamp((C.r - C.b) * 2.0, 0.0, 1.0) * (1.0 - orangeK) * (1.0 - greenK);
        float wsum = pinkK + greenK + orangeK + magK;
        if (wsum < 0.02) {
            magK = 1.0;
            wsum = 1.0;
        }
        // 5.5-5.9 pinkish-violet (violet zenith, salmon-pink horizon)
        vec3 z1 = vec3(0.055, 0.022, 0.130);
        vec3 m1 = vec3(0.200, 0.060, 0.230);
        vec3 h1 = vec3(0.640, 0.300, 0.310);
        // green-teal frames
        vec3 z2 = vec3(0.050, 0.110, 0.095);
        vec3 m2 = vec3(0.120, 0.220, 0.180);
        vec3 h2 = vec3(0.440, 0.560, 0.360);
        // sunset-orange frames
        vec3 z3 = vec3(0.120, 0.060, 0.080);
        vec3 m3 = vec3(0.350, 0.140, 0.110);
        vec3 h3 = vec3(0.780, 0.280, 0.100);
        // deep purple / magenta frames
        vec3 z4 = vec3(0.070, 0.022, 0.120);
        vec3 m4 = vec3(0.230, 0.055, 0.220);
        vec3 h4 = vec3(0.560, 0.220, 0.320);
        vec3 zen = (z1 * pinkK + z2 * greenK + z3 * orangeK + z4 * magK) / wsum;
        vec3 mid = (m1 * pinkK + m2 * greenK + m3 * orangeK + m4 * magK) / wsum;
        vec3 hor = (h1 * pinkK + h2 * greenK + h3 * orangeK + h4 * magK) / wsum;
        // keep the mod's own tint in the mix so the blob colour still reads
        zen = mix(zen, C * 0.35, 0.30);
        mid = mix(mid, C * 0.80, 0.30);
        hor = mix(hor, C * 1.35, 0.22);

        vec3 col = mix(zen, mid, smoothstep(0.04, 0.45, t));
        col = mix(col, hor, smoothstep(0.45, 0.95, t));
        // soft horizon glow band, continuous - no seam between vault and rim
        col += hor * 0.22 * exp(-abs(ty) * 6.0);
        // blue silhouette rim hugging the horizon, all the way around
        float rim = exp(-abs(ty - 0.015) * 42.0);
        col = mix(col, vec3(0.16, 0.34, 0.95), rim * 0.50);
        // gigantic purple line across the upper vault
        float topLine = exp(-abs(ty - 0.72) * 26.0);
        col = mix(col, vec3(0.40, 0.15, 0.85), topLine * 0.30);
        // darker back tone so the roof reads heavier than the sides
        col *= 1.0 - 0.38 * smoothstep(0.50, 1.0, ty);
        // mega-phase 3: the storm sky SHRINKS to the sides. Overhead the
        // dome collapses into a dark calm violet instead of stretching the
        // storm palette across the whole sky; the coloured halo around the
        // storm's flanks is carried by the mod's halo ring quad instead.
        float over = smoothstep(0.30, 0.70, ty);
        float olum = dot(col, vec3(0.299, 0.587, 0.114));
        vec3 ocol = mix(vec3(olum) * vec3(0.42, 0.30, 0.52), vec3(0.02, 0.012, 0.03), 0.55);
        col = mix(col, ocol, over * 0.85);

        vec3 litC = mix(vec3(0.90, 0.88, 0.95), hor, 0.30);
        vec3 shadeC = mix(zen, hor, 0.30) * 0.75;
        col = paintDecks(dirS, col, 0.0, litC, shadeC, 0.35, 0.5);

        col = mix(col, hor * 0.45, smoothstep(0.0, -0.35, ty));
        float lum = dot(col, vec3(0.299, 0.587, 0.114));
        col = mix(vec3(lum), col, 1.22);
        col = mix(col, col * col * (3.0 - 2.0 * col), 0.25);
        fragColor = vec4(col, 1.0);
        return;
    }

    // --- calm sky: time-of-day weights ---------------------------------------
    float night = 1.0 - smoothstep(0.05, 0.22, clum);
    float orange = C.r - C.b;
    float dawn = smoothstep(0.25, 0.50, orange) * step(C.b, C.g) * (1.0 - night);
    float day = max(1.0 - night - dawn, 0.0);

    // EnderCon gate / Sky City pastels: soft story blue, pink-warm horizon
    vec3 zen = day * vec3(0.216, 0.394, 0.716)
             + dawn * vec3(0.620, 0.560, 0.810)
             + night * vec3(0.010, 0.014, 0.070);
    vec3 mid = day * vec3(0.394, 0.578, 0.806)
             + dawn * vec3(0.620, 0.560, 0.810)
             + night * vec3(0.010, 0.014, 0.070);
    vec3 hor = day * vec3(0.870, 0.745, 0.690)
             + dawn * vec3(0.890, 0.680, 0.730)
             + night * vec3(0.019, 0.031, 0.130);

    // per-biome variants (vanilla hands us the biome sky hue in ColorModulator)
    float gk = clamp((C.g - max(C.r, C.b)) * 3.0, 0.0, 0.6) * day;
    float wk = clamp((C.r - C.b) * 1.2, 0.0, 0.6) * day * (1.0 - dawn);
    zen = mix(zen, vec3(0.150, 0.420, 0.470), gk);
    mid = mix(mid, vec3(0.320, 0.580, 0.530), gk);
    hor = mix(hor, vec3(0.620, 0.800, 0.700), gk);
    zen = mix(zen, vec3(0.350, 0.450, 0.700), wk);
    mid = mix(mid, vec3(0.560, 0.620, 0.760), wk);
    hor = mix(hor, vec3(0.880, 0.760, 0.640), wk);

    vec3 col = mix(zen, mid, smoothstep(0.05, 0.5, t));
    col = mix(col, hor, smoothstep(0.5, 0.95, t));
    col += hor * 0.14 * exp(-abs(ty) * 7.0);

    // crisp stars at night
    vec3 sg = floor(dirS * 220.0);
    float sn = hash13(sg);
    float star = smoothstep(0.996, 0.9995, sn) * night;
    col += star * (0.55 + 0.45 * hash13(sg + 7.7)) * vec3(0.92, 0.96, 1.0);

    // white story clouds with pale-blue shadowed fringes
    vec3 litC = mix(vec3(0.960, 0.975, 1.000), hor, 0.10);
    vec3 shadeC = mix(zen, hor, 0.35) * 0.85;
    col = paintDecks(dirS, col, 0.0, litC, shadeC, day, 0.5);

    col = mix(col, hor * 0.5, smoothstep(0.0, -0.3, ty));
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(lum), col, 1.15);
    col = mix(col, col * col * (3.0 - 2.0 * col), 0.22);

    fragColor = vec4(col, 1.0);
}
