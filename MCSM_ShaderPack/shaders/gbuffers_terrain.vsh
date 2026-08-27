#version 120

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 worldPos;

uniform mat4 gbufferModelViewInverse;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = gl_MultiTexCoord0.xy;
    lmcoord = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    normal = normalize(gl_NormalMatrix * gl_Normal);
    worldPos = (gbufferModelViewInverse * (gl_ModelViewMatrix * gl_Vertex)).xyz;
}
