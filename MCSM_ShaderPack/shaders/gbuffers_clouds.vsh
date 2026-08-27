#version 120

varying vec4 color;

void main() {
    gl_Position = ftransform();
    // Flat bright uniform MCSM story mode cloud lighting (all faces 1.0 brightness)
    color = gl_Color;
}
