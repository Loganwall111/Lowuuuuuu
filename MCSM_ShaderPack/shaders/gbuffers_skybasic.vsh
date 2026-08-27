#version 120

precision highp float;
precision highp int;

varying vec4 color;
varying vec3 viewPos;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
