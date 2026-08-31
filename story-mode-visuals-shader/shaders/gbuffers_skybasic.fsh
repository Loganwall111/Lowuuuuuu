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

void main() {
    vec3 dir = normalize(viewDir);
    // world-space direction (horizontal = horizon)
    vec3 wdir = normalize(mat3(gbufferModelViewInverse) * dir);

    // ------------- day/night/sunset clock
    float timeF = mod(float(worldTime), 24000.0) / 24000.0;
    float dayW = sstep(0.07, 0.30, timeF) * sstep(0.80, 0.55, timeF);
    float nightW = sstep(0.52, 0.72, timeF) * sstep(0.05, 0.35, timeF);
    float setW = 1.0 - dayW - nightW;

    // ------------- biome gradient (the same profiles the fog uses)
    vec4 fog = sampledFog(cameraPosition + wdir * 300.0);

    // ------------- horizon melt: lower sky dissolves into fog
    float hUp = clamp(wdir.y, 0.0, 1.0);
    float hDown = clamp(-wdir.y, 0.0, 1.0);

    // zenith colors per phase
    vec3 dayZen = mix(vec3(0.16, 0.38, 0.75), fog.rgb, 0.35);
    vec3 setZen = vec3(0.55, 0.36, 0.60);
    vec3 nightZen = vec3(0.012, 0.016, 0.075);
    vec3 zen = dayZen * dayW + setZen * setW + nightZen * nightW;
    zen = mix(zen, vec3(0.045, 0.055, 0.09), rainStrength * 0.9);   // stormy overcast

    vec3 below = fog.rgb * 0.55;
    vec3 dayHor = mix(vec3(0.50, 0.70, 0.92), fog.rgb, 0.7);
    vec3 setHor = mix(fog.rgb, vec3(0.98, 0.52, 0.22), 0.65);
    vec3 nightHor = fog.rgb * 0.5;
    vec3 hor = dayHor * dayW + setHor * setW + nightHor * nightW;
    hor = mix(hor, vec3(0.16, 0.17, 0.19), rainStrength * 0.8);

    vec3 sky = mix(hor, zen, pow(hUp, 0.85));
    sky = mix(sky, below, sstep(0.0, 0.12, hDown));

    // ------------- dramatic Story Mode sunset: sun-following warm glow
    vec3 sunDir = normalize(sunPosition);
    float sunDot = dot(wdir, sunDir);
    float sunGlow = pow(max(sunDot, 0.0), 260.0) * 1.4;                  // disc
    float sunHalo = pow(max(sunDot, 0.0), 6.0) * 0.55;
    vec3 sunsetBand = mix(vec3(1.0, 0.52, 0.24), vec3(0.85, 0.30, 0.55), hUp);
    sky += sunsetBand * SUNSET * (sunHalo * setW * (1.0 - rainStrength) + sunGlow * dayW);

    // ------------- moon: procedural disk + craters + big soft halo
    vec3 moonDir = normalize(moonPosition);
    float moonDot = dot(wdir, moonDir);
    float halo = pow(max(moonDot, 0.0), 16.0) * 0.35 * nightW;
    vec3 moon = moonGlow(wdir, moonDir, moonPhase);
    float moonVis = sstep(-0.12, 0.02, wdir.y) * nightW * (1.0 - rainStrength);
    sky += moon * moonVis;
    sky += vec3(0.65, 0.75, 1.0) * halo * (1.0 - rainStrength);

    // ------------- stars (only high in the sky, twinkle, no stars in rain)
    float stars = starLayer(wdir, 0.60, frameTimeCounter * 2.0)
                + starLayer(wdir, 0.85, frameTimeCounter * 2.0 + 40.0) * 0.8;
    float starMask = sstep(-0.08, 0.25, wdir.y) * nightW * (1.0 - rainStrength);
    sky += vec3(0.85, 0.9, 1.0) * stars * starMask;

    // ------------- God rays: sun/moon column blooms through the dome
    vec3 celest = mix(sunDir, moonDir, nightW);
    float ray = pow(max(dot(wdir, celest), 0.0), 4.0);
    ray *= pow(max(wdir.y, 0.0), 0.7);
    sky += vec3(1.0, 0.82, 0.62) * ray * 0.18 * (dayW + setW) * (1.0 - rainStrength);
    sky += vec3(0.55, 0.7, 1.0) * ray * 0.10 * nightW;

    gl_FragData[0] = vec4(sky, 1.0);
}
