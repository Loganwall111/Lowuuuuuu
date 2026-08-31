#version 120
/* DRAWBUFFERS:0 */

#include "/lib.glsl"

/*
  STORY MODE CLOUD LAYER - procedural blocky noise clouds.
  * dynamic 3D noise threshold -> chunky voxel-ish cells (MCSM style)
  * vertical transparency gradient dissolves the bottoms into the sky
  * hue crossfades with the celestial clock: white noon, pinkish-lavender
    sunset, deep royal indigo midnight
  * clouds stay opaque in rain (overcast) and drift with the wind
*/

varying vec2 texcoord;
varying vec4 glcolor;

uniform vec3  cameraPosition;
uniform float frameTimeCounter;
uniform float rainStrength;
uniform int   worldTime;

uniform float CLOUD_SPEED; //settings speed
uniform float CLOUD_DENSITY; //settings density
uniform float CLOUD_COLORIZE; //settings colorize
uniform float CLOUD_COVER; //settings cloudCover
uniform float RAIN_STR; //settings rainStrength

void main() {
    vec2 uv = texcoord;
    float anim = frameTimeCounter * 4.5 * CLOUD_SPEED;

    vec3 p = vec3(uv.x, uv.y, 0.0) * vec3(90.0, 90.0, 1.0) + vec3(anim * 0.14, 0.0, anim * 0.07);

    float n = vnoise(p) * 0.55 + vnoise(p * 2.4 + 31.0) * 0.35 + vnoise(p * 5.0 + 71.0) * 0.10;
    float thresh = mix(0.62, 0.14, CLOUD_COVER);
    float cloud = smoothstep(thresh - 0.44 * CLOUD_DENSITY, thresh, n);

    // vertical dissolve: crisp tops, soft bottoms
    float vGrad = smoothstep(-0.85, 0.85, uv.y);
    cloud *= 0.25 + 0.75 * vGrad;

    // celestial color clock
    float timeF = mod(float(worldTime), 24000.0) / 24000.0;
    float dayW = sstep(0.07, 0.30, timeF) * sstep(0.80, 0.55, timeF);
    float nightW = sstep(0.52, 0.72, timeF) * sstep(0.05, 0.35, timeF);
    float setW = 1.0 - dayW - nightW;
    vec3 cloudCol = vec3(0.97, 0.97, 0.98) * dayW
                  + vec3(0.93, 0.60, 0.68) * setW
                  + vec3(0.16, 0.19, 0.38) * nightW;

    // clouds adapt to the biome fog below (Story Mode color harmony)
    vec4 fogHere = sampledFog(cameraPosition + vec3(uv.x * 300.0, 128.0, uv.y * 300.0));
    float luma = dot(cloudCol, vec3(0.299, 0.587, 0.114));
    cloudCol = mix(vec3(luma, luma, luma), cloudCol, CLOUD_COLORIZE);
    cloudCol = mix(cloudCol, fogHere.rgb, 0.35);

    // rain -> dark overcast blanket
    cloud = mix(cloud, smoothstep(0.05, 0.4, n), (rainStrength + RAIN_STR) * 0.6);
    cloudCol = mix(cloudCol, vec3(0.13, 0.14, 0.17), (rainStrength + RAIN_STR) * 0.85);

    float alpha = cloud * glcolor.a;
    if (alpha < 0.01) discard;
    gl_FragData[0] = vec4(cloudCol, alpha);
}
