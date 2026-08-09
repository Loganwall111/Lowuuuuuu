#version 120

/*
 * DEVOURING STORMS — composite pass: the storm-fog itself.
 * Pulls the world toward bruised violet by depth, flashes hot when storm light
 * (lightning) tears the sky, and chokes distant geometry away.
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

/* Slow violet far-thunder: the MCSM sky strobes on its own clock — no weather needed. */
float farThunder(float t) {
    float wave = sin(t * 0.23) * sin(t * 0.071 + 1.7);
    float pulse = pow(clamp(wave, 0.0, 1.0), 14.0);
    float calm = smoothstep(0.35, 0.6, fract(t * 0.0087));   // long quiet spells between fronts
    return pulse * (1.0 - calm * 0.7);
}

float linearizeDepth(float z) {
    float ndc = z * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - ndc * (far - near));
}

void main() {
    vec3 col = texture2D(colortex0, texcoord).rgb;
    float depth = texture2D(depthtex0, texcoord).x;

    if (depth < 0.9999) {
        float dist = linearizeDepth(depth);
        float fogAmt = 1.0 - exp(-dist * 0.010 * (1.0 + rainStrength * 1.4));
        fogAmt = clamp(fogAmt, 0.0, 1.0);

        // bruised violet storm-fog, denser than vanilla
        vec3 stormFog = fogColor * vec3(0.55, 0.32, 0.85);
        col = mix(col, stormFog * 0.55, fogAmt * 0.75);
    }

    // storm-white sky tear on lightning strikes
    col += vec3(0.9, 0.8, 1.0) * float(lightningBolt) * 0.05;
    // and the MCSM far-thunder that never needs a bolt: slow violet pulses in the bank
    col += vec3(0.62, 0.3, 0.9) * farThunder(frameTimeCounter) * 0.055;

    gl_FragColor = vec4(col, 1.0);
}
