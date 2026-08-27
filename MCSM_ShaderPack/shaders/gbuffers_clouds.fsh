#version 120

varying vec4 color;

void main() {
    if (color.a < 0.05) {
        discard;
    }
    // Crisp flat Story Mode clouds without raymarching or alpha fade
    gl_FragColor = color;
}
