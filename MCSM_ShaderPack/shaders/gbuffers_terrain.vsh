#version 120

// ============================================================================
// MCSM gbuffers_terrain.vsh — Story Mode lighting + sun shadow projection
// ============================================================================

#define MCSM_LIGHTING

precision highp float;
precision highp int;

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform mat4 shadowModelView;
uniform mat4 shadowProjection;
uniform vec3 sunPosition;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 viewPos;
varying vec3 worldPos;
varying vec4 shadowPos;
varying float vSunY;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmcoord = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    normal = normalize(gl_NormalMatrix * gl_Normal);
    viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    // Unproject to world space for shadow projection + procedural shading
    worldPos = (gbufferModelViewInverse * vec4(viewPos, 1.0)).xyz;
    shadowPos = shadowProjection * shadowModelView * vec4(worldPos, 1.0);
    vSunY = normalize(sunPosition).y;
}
