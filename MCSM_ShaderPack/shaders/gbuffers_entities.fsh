#version 120

// ============================================================================
// MCSM gbuffers_entities.fsh — Story Mode entity lighting + the Wither Storm's
// luminescent turquoise teeth aura (emissive, pulsing) + magenta accents.
// ============================================================================

#define EMISSIVE_TEETH_GLOW // Bright cyan (#00E5FF) bloom on Wither Storm teeth

precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform float frameTimeCounter;
uniform vec3 sunPosition;

varying vec4 color;
varying vec2 texcoord;
varying vec3 normal;

void main() {
    vec4 col = texture2D(gtexture, texcoord);
    col *= color;
    if (col.a < 0.1) {
        discard;
    }

    float isTurquoise = step(0.65, col.g) * step(0.75, col.b) * (1.0 - step(0.40, col.r));
    float isMagenta   = step(0.60, col.r) * step(0.60, col.b) * (1.0 - step(0.50, col.g));

    if (isTurquoise > 0.5) {
        // Turquoise aura on the teeth: emissive core + breathing glow
        float pulse = 0.90 + 0.10 * sin(frameTimeCounter * 4.0);
        float halo = 0.30 + 0.20 * sin(frameTimeCounter * 2.3);
        col.rgb = vec3(0.0, 0.92, 1.0) * (3.5 * pulse + halo);
    } else if (isMagenta > 0.5) {
        float pulse = 0.92 + 0.08 * sin(frameTimeCounter * 3.0);
        col.rgb = vec3(0.85, 0.12, 0.95) * 3.0 * pulse;
    }

    gl_FragColor = col;
}
