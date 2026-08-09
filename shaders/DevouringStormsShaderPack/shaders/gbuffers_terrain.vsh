#version 120

varying vec4 intColor;
varying vec2 texcoord;
varying vec3 viewPos;
varying float fogDepth;

void main() {
    gl_Position = ftransform();
    intColor = gl_Color;
    texcoord = gl_MultiTexCoord0.xy;
    vec4 mv = gl_ModelViewMatrix * gl_Vertex;
    viewPos = mv.xyz;
    fogDepth = gl_FogCoord;
}
