#version 120

/*
 * Story Mode Atmospheric Distance Fog
 * Melts terrain smoothly into the pink/coral horizon haze instead of drab grey.
 */

uniform sampler2D colortex0;
uniform sampler2D depthtex0;
uniform float near;
uniform float far;
uniform vec3 fogColor;
uniform float rainStrength;
uniform int lightningBolt;
uniform float frameTimeCounter;

varying vec2 texcoord;

#ifndef MCSM_STORM_FOG
#define MCSM_STORM_FOG 1
#endif

float linearizeDepth(float z) {
    float ndc = z * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - ndc * (far - near));
}

void main() {
    vec3 col = texture2D(colortex0, texcoord).rgb;
    float depth = texture2D(depthtex0, texcoord).x;

    #if MCSM_STORM_FOG
    if (depth < 0.9999) {
        float dist = linearizeDepth(depth);
        float fogFactor = 1.0 - exp(-dist * 0.007 * (1.0 + rainStrength * 0.8));
        fogFactor = clamp(fogFactor, 0.0, 0.88);

        // Story Mode Rose-Coral Atmospheric Fog
        vec3 storyModeFog = vec3(0.85, 0.48, 0.58) * (0.85 + 0.15 * fogColor);
        col = mix(col, storyModeFog, fogFactor);
    }
    #endif

    // Lightning storm sky flash
    if (lightningBolt > 0) {
        col += vec3(0.95, 0.70, 0.90) * 0.15;
    }

    gl_FragColor = vec4(col, 1.0);
}
