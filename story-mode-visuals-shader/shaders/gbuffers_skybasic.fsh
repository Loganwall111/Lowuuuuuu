#version 120

#include "/lib.glsl"

/*
  SEAMLESS INFINITE SKY DOME — no geometry, no cube, no seams.
  Everything is computed per-ray: horizon-melted biome gradient dome,
  dramatic Story Mode sunset band, moon with craters and halo, stars,
  God-rays from the sun/moon.
*/

varying vec3 viewDir;

uniform vec3  sunPosition;
uniform vec3  moonPosition;
uniform vec3  upPosition;
uniform mat4  gbufferModelViewInverse;
uniform vec3  skyColor;
uniform vec3  fogColor;
uniform float rainStrength;
uniform float sunAngle;
uniform float frameTimeCounter;
uniform int   worldTime;
uniform float moonPhase;

uniform float SUNSET; //settings intensity
uniform float MOONSHINE; //settings moonlight
uniform float SKY_FOG_MIX; //settings skyFog
uniform float MOON_SIZE; //settings moonSize
uniform float CLOUD_COVER; //settings cloudCover
uniform int SKY_PRESET; //settings preset

void main() {
    vec3 dir = normalize(viewDir);
    // world-space direction (horizontal = horizon)
    vec3 wdir = normalize(mat3(gbufferModelViewInverse) * dir);

    // ------------- day/night/sunset clock (preset-blended phases)
    float timeF = mod(float(worldTime), 24000.0) / 24000.0;
    float dayW = sstep(0.07, 0.30, timeF) * sstep(0.80, 0.55, timeF);
    float nightW = sstep(0.52, 0.72, timeF) * sstep(0.05, 0.35, timeF);
    float setW = 1.0 - dayW - nightW;

    vec3 zenSel[3]; vec3 horSel[3]; float sunSel[3];
    zenSel[0] = mix(vec3(0.16, 0.38, 0.75), fog.rgb, 0.35);
    zenSel[1] = vec3(0.55, 0.36, 0.60);
    zenSel[2] = vec3(0.012, 0.016, 0.075);
    horSel[0] = mix(vec3(0.50, 0.70, 0.92), fog.rgb, 0.7);
    horSel[1] = mix(fog.rgb, vec3(0.98, 0.52, 0.22), 0.65);
    horSel[2] = fog.rgb * 0.5;
    sunSel[0] = 0.55; sunSel[1] = 1.0; sunSel[2] = 0.0;
    // sky preset: 0 classic | 1 bright | 2 cinematic (alt sunset, saturated day)
    if (SKY_PRESET == 1) { zenSel[0] = vec3(0.20, 0.55, 0.95); sunSel[0] = 0.8; }
    if (SKY_PRESET == 2) { zenSel[0] = vec3(0.10, 0.30, 0.55); horSel[1] = vec3(1.0, 0.35, 0.30); sunSel[1] = 1.25; }

    // ------------- biome gradient (the same profiles the fog uses)
    vec4 fog = sampledFog(cameraPosition + wdir * 300.0);

    // ------------- horizon melt: lower sky dissolves into fog
    float hUp = clamp(wdir.y, 0.0, 1.0);
    float hDown = clamp(-wdir.y, 0.0, 1.0);

    // zenith colors per phase
    vec3 dayZen = mix(vec3(0.16, 0.38, 0.75), fog.rgb, 0.35);
    vec3 setZen = vec3(0.55, 0.36, 0.60);
    vec3 nightZen = vec3(0.012, 0.016, 0.075);
    vec3 zen = zenSel[0] * dayW + zenSel[1] * setW + zenSel[2] * nightW;
    zen = mix(zen, vec3(0.045, 0.055, 0.09), rainStrength * 0.9);   // stormy overcast

    vec3 below = fog.rgb * 0.55;
    vec3 hor = horSel[0] * dayW + horSel[1] * setW + horSel[2] * nightW;
    hor = mix(hor, vec3(0.16, 0.17, 0.19), rainStrength * 0.8);

    vec3 sky = mix(hor, zen, pow(hUp, 0.85));
    sky = mix(sky, below, sstep(0.0, 0.12, hDown));

    // ------------- dramatic Story Mode sunset: sun-following warm glow
    vec3 sunDir = normalize(sunPosition);
    float sunDot = dot(wdir, sunDir);
    float sunGlow = pow(max(sunDot, 0.0), 260.0) * 1.4;                  // disc
    float sunHalo = pow(max(sunDot, 0.0), 6.0) * 0.55 * sunSel[0];
    vec3 sunsetBand = mix(vec3(1.0, 0.52, 0.24), vec3(0.85, 0.30, 0.55), hUp);
    sky += sunsetBand * SUNSET * (sunHalo * setW * (1.0 - rainStrength) + sunGlow * dayW);

    // ------------- moon: procedural disk + craters + big soft halo
    vec3 moonDir = normalize(moonPosition);
    float moonDot = dot(wdir, moonDir);
    float halo = pow(max(moonDot, 0.0), 16.0) * 0.35 * nightW * MOONSHINE;
    vec3 moon = moonGlow(wdir, moonDir, moonPhase, MOON_SIZE);
    float moonVis = sstep(-0.12, 0.02, wdir.y) * nightW * (1.0 - rainStrength);
    sky += moon * moonVis * MOONSHINE;
    sky += vec3(0.65, 0.75, 1.0) * halo * (1.0 - rainStrength);

    // ------------- moonlit horizon / moonlight sky wash
    sky += vec3(0.10, 0.14, 0.26) * MOONSHINE * (1.0 - hUp) * nightW * (1.0 - rainStrength);

    // ------------- aurora borealis: ribbons in cold biomes at night
    float auroraW = biomeMatch(cameraPosition + wdir * 300.0, 8.0)
                  + biomeMatch(cameraPosition + wdir * 300.0, 9.0) * 0.8;
    float aBand = smoothstep(0.0, 0.55, wdir.y) * smoothstep(0.85, 0.55, wdir.y);
    float aDir = dot(normalize(wdir.xz + 0.0001), vec2(0.3, 0.95));
    float aRibbon = smoothstep(0.2, 0.9, fbm3(vec3(aDir * 3.5, wdir.y * 4.0 - frameTimeCounter * 0.06)));
    float aurora = aRibbon * aBand * auroraW * nightW * (1.0 - rainStrength) * 0.8;
    sky += vec3(0.15, 0.85, 0.55) * aurora;
    sky += vec3(0.65, 0.25, 0.75) * aurora * 0.5;

    // ------------- milky way band (rotates with the celestial clock)
    vec3 bandN = normalize(vec3(0.0, 1.0, 0.0));
    float galaxy = smoothstep(0.35, 0.75, fbm3(vec3(wdir.xy * 8.0, wdir.z * 2.0 + frameTimeCounter * 0.004)));
    float bandDist = abs(dot(wdir, bandN));
    galaxy *= smoothstep(0.55, 0.0, bandDist) * nightW * sstep(-0.1, 0.1, wdir.y) * (1.0 - rainStrength);
    sky += vec3(0.75, 0.72, 0.95) * galaxy * 0.20;

    // ------------- stars (clouds dim them - overcast nights hide the sky)
    float stars = starLayer(wdir, 0.60, frameTimeCounter * 2.0)
                + starLayer(wdir, 0.85, frameTimeCounter * 2.0 + 40.0) * 0.8;
    float starMask = sstep(-0.08, 0.25, wdir.y) * nightW * (1.0 - rainStrength);
    float cloudDim = 1.0 - smoothstep(0.25, 0.85, CLOUD_COVER) * 0.9;
    sky += vec3(0.85, 0.9, 1.0) * stars * starMask * cloudDim;

    // ------------- God rays: sun/moon column blooms through the dome
    vec3 celest = mix(sunDir, moonDir, nightW);
    float ray = pow(max(dot(wdir, celest), 0.0), 4.0);
    ray *= pow(max(wdir.y, 0.0), 0.7);
    sky += vec3(1.0, 0.82, 0.62) * ray * 0.18 * (dayW + setW) * (1.0 - rainStrength);
    sky += vec3(0.55, 0.7, 1.0) * ray * 0.10 * nightW * MOONSHINE;

    // merge per-preset sky bias (bright = boost, cinematic = deepen)
    if (SKY_PRESET == 1) {
        sky = mix(sky, sky * 1.06 + vec3(0.03), 0.6);
    } else if (SKY_PRESET == 2) {
        sky = mix(sky, sky * 0.92 + vec3(0.01), 0.7);
    }

    gl_FragData[0] = vec4(sky, 1.0);
}
