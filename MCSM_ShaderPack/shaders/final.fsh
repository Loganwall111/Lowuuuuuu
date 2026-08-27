#version 120

/*
 * Minecraft: Story Mode — Cinematic Final Presentation
 * Applies clean, vibrant Telltale color grading, rich saturation, and filmic tone curve.
 */

uniform sampler2D colortex0;
uniform float viewWidth;
uniform float viewHeight;

varying vec2 texcoord;

#ifndef MCSM_CINEMATIC_GRADE
#define MCSM_CINEMATIC_GRADE 1
#endif
#ifndef MCSM_VIGNETTE
#define MCSM_VIGNETTE 0
#endif

void main() {
    vec2 uv = texcoord;
    vec3 col = texture2D(colortex0, uv).rgb;

    #if MCSM_CINEMATIC_GRADE
    // Story Mode Vibrancy & Saturation Push
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(lum), col, 1.18);
    // Warm filmic tone curve
    col = pow(col, vec3(0.96, 0.95, 0.98));
    #endif

    #if MCSM_VIGNETTE
    float d = distance(uv, vec2(0.5));
    float vig = smoothstep(0.42, 0.95, d);
    col = mix(col, col * 0.72, vig * 0.35);
    #endif

    gl_FragColor = vec4(col, 1.0);
}
