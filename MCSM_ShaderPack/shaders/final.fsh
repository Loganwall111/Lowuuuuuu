#version 120

/*
 * Minecraft: Story Mode — Cinematic Final Presentation
 * Applies clean, vibrant Telltale color grading, optional subtle vignette,
 * and frames the world cleanly without screen tears or noise.
 */

uniform sampler2D colortex0;
uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;

varying vec2 texcoord;

#ifndef MCSM_CINEMATIC_GRADE
#define MCSM_CINEMATIC_GRADE 1
#endif
#ifndef MCSM_VIGNETTE
#define MCSM_VIGNETTE 0
#endif
#ifndef MCSM_GRAIN
#define MCSM_GRAIN 0
#endif
#ifndef MCSM_RETRO_VHS
#define MCSM_RETRO_VHS 0
#endif

float hash(float x) {
    return fract(sin(x * 127.1 + 311.7) * 43758.5453);
}

void main() {
    vec2 uv = texcoord;
    vec3 col = texture2D(colortex0, uv).rgb;

    #if MCSM_CINEMATIC_GRADE
    // Story Mode Vibrancy & Saturation Push
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(lum), col, 1.15); // Enhanced color pop
    // Warm tone curve
    col = pow(col, vec3(0.96, 0.94, 0.98));
    #endif

    #if MCSM_VIGNETTE
    float d = distance(uv, vec2(0.5));
    float vig = smoothstep(0.40, 0.92, d);
    col = mix(col, col * 0.70, vig * 0.40);
    #endif

    #if MCSM_GRAIN
    float noise = (hash(uv.x * 913.0 + uv.y * 719.0 + frameTimeCounter * 60.0) - 0.5) * 0.025;
    col += noise;
    #endif

    #if MCSM_RETRO_VHS
    float scan = 0.98 + 0.02 * sin(uv.y * viewHeight * 3.14159);
    col *= scan;
    #endif

    gl_FragColor = vec4(col, 1.0);
}
