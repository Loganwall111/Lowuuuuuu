#version 120

varying vec4 intColor;
varying vec2 texcoord;
varying vec3 viewPos;

void main() {
    gl_Position = ftransform();
    intColor = gl_Color;
    texcoord = gl_MultiTexCoord0.xy;
    viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
