#version 120

/*
 * THE TWO RIFTS + THE MCSM SKY.
 * "When two rifts open in the sky, light flows, but not enough."
 * Story Mode's wither-storm sky: a bank of slow-boiling storm cloud chokes the dome,
 * bruised violet from underneath — and the two rifts keep burning through it.
 */

uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelView;
uniform float frameTimeCounter;

varying vec4 intColor;
varying vec3 viewPos;

/* ---- user tunables -------------------------------------------------------------
 * DS_CLOUD_CHURN   : how fast the MCSM storm-bank roils (1.0 = channel default)
 * DS_CLOUD_COVER   : how much of the dome the bank chokes
 * DS_RIFT_GLOW     : brightness of the two rifts burning through
 * DS_SKY_DARKNESS  : how bruised-night the base sky goes (0 = vanilla-ish sky)
 * DS_MAW_SKY       : a third, unlit disc high in the dome — a maw seen from below
 * -------------------------------------------------------------------------------- */
#ifndef DS_CLOUD_CHURN
#define DS_CLOUD_CHURN 1
#endif
#ifndef DS_CLOUD_COVER
#define DS_CLOUD_COVER 1
#endif
#ifndef DS_RIFT_GLOW
#define DS_RIFT_GLOW 1
#endif
#ifndef DS_SKY_DARKNESS
#define DS_SKY_DARKNESS 1
#endif
#ifndef DS_MAW_SKY
#define DS_MAW_SKY 1
#endif

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
    float amp = 0.55;
    for (int i = 0; i < 4; i++) {
        v += amp * vnoise(p);
        p = p * 2.13 + vec2(17.3, 9.1);
        amp *= 0.5;
    }
    return v;
}

void main() {
    vec3 outCol = intColor.rgb * intColor.a;

    // view-space direction from the sky quad itself, un-rotated into world space
    // (gbuffers sky modelview is rotation-only, so transpose ≈ inverse)
    vec3 dirV = normalize(viewPos);
    vec3 dir = normalize(transpose(mat3(gbufferModelView)) * dirV);

    // two rift gates at fixed bearings
    vec2 rift1 = vec2(0.55, 0.38);  // (bearing-ish x, altitude y)
    vec2 rift2 = vec2(-0.85, 0.55);

    float xComp = dir.x;
    float yComp = dir.y;

    float g1 = exp(-pow((xComp - rift1.x) * 10.0, 2.0)) * exp(-pow((yComp - rift1.y) * 7.0, 2.0));
    float g2 = exp(-pow((xComp - rift2.x) * 9.0, 2.0))  * exp(-pow((yComp - rift2.y) * 6.0, 2.0));

    // they breathe — slowly
    float breathe = 0.85 + 0.15 * sin(frameTimeCounter * 0.25);
    vec3 riftLight = (g1 * vec3(0.55, 0.18, 0.85) + g2 * vec3(0.35, 0.08, 0.55)) * 0.35 * breathe * float(DS_RIFT_GLOW);

    // ---- THE MCSM CLOUD BANK ----
    // project the sky direction onto a virtual cloud ceiling, swirl the domain
    // around the zenith, and boil it slowly. MCSM storms never sit still.
    float horizon = clamp(dir.y * 1.6 + 0.55, 0.0, 1.0);        // clouds thickest overhead
    vec2 cp = dir.xz / max(dir.y + 0.35, 0.12);
    float ang = atan(cp.y, cp.x) + frameTimeCounter * 0.03 * float(DS_CLOUD_CHURN); // the bank turns
    float rad = length(cp);
    cp = vec2(cos(ang), sin(ang)) * rad;
    float cloud = fbm(cp * 1.15 + vec2(frameTimeCounter * 0.045 * float(DS_CLOUD_CHURN),
                                       frameTimeCounter * 0.02 * float(DS_CLOUD_CHURN)));
    cloud = smoothstep(0.38, 0.75, cloud) * horizon * float(DS_CLOUD_COVER);

    // bruised violet underlit bellies, darker crowns — light flows, but not enough
    vec3 cloudDark = vec3(0.045, 0.022, 0.075);
    vec3 cloudLit  = vec3(0.38, 0.14, 0.52);
    float under = fbm(cp * 2.3 - vec2(frameTimeCounter * 0.06, 0.0));
    vec3 cloudCol = mix(cloudDark, cloudLit, smoothstep(0.35, 0.85, under) * 0.55);

    // settle the sky into bruised night even at noon, then lay the bank over it
    outCol = mix(outCol, vec3(0.05, 0.02, 0.09), 0.35 * float(DS_SKY_DARKNESS));
    outCol = mix(outCol, cloudCol, cloud * 0.85);

    // a maw drifts overhead: a black disc with a photon rim where the dome should be
    #if DS_MAW_SKY
    {
        vec2 mawPos = vec2(-0.30, 0.78);
        float mr = distance(vec2(xComp, yComp), mawPos);
        float hole = smoothstep(0.085, 0.055, mr);
        float rim = exp(-pow((mr - 0.075), 2.0) / 0.0004);
        outCol = mix(outCol, vec3(0.0), hole * 0.92 * horizon);
        outCol += rim * vec3(0.9, 0.75, 1.2) * 0.35 * horizon * (0.6 + 0.4 * breathe);
    }
    #endif

    // the rifts burn through the cloud — wounds the weather can't cover
    outCol += riftLight * (1.0 - cloud * 0.45);
    // violet rim where the cloud tears open around the rifts
    outCol += (g1 + g2) * vec3(0.9, 0.5, 1.4) * 0.22 * cloud;

    gl_FragColor = vec4(outCol, 1.0);
}
