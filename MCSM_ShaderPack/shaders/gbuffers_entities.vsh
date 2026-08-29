#version 120

precision highp float;
precision highp int;

uniform mat4 gbufferModelViewInverse;

varying vec4 color;
varying vec2 texcoord;
varying vec3 normal;
varying vec3 viewPos;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    normal = normalize(gl_NormalMatrix * gl_Normal);
    viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
