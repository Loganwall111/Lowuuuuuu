#version 120

varying vec4 texcoord;
varying vec4 lmcoord;
varying vec4 glcolor;
varying vec3 normal;
varying vec3 worldPos;

void main() {
    gl_Position = ftransform();
    texcoord = gl_MultiTexCoord0;
    lmcoord  = gl_MultiTexCoord1;
    glcolor  = gl_Color;
    normal   = normalize(gl_NormalMatrix * gl_Normal);
    worldPos = gl_Vertex.xyz;
}
