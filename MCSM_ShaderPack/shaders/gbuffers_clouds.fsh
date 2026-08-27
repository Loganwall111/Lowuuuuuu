#version 120

varying vec4 color;
varying vec2 texcoord;

void main() {
    if (color.a < 0.05) {
        discard;
    }
    // Crisp flat Story Mode cloud blocks
    gl_FragColor = color;
}
