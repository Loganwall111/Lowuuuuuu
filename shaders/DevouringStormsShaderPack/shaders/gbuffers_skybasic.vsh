#version 120

varying vec4 intColor;
varying vec3 viewPos;

void main() {
    gl_Position = ftransform();
    intColor = gl_Color;
    viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
