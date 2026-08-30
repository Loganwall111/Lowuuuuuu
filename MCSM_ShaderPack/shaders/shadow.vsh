#version 120

// ============================================================================
// MCSM shadow.vsh — sun shadow map pass
// ============================================================================
// Renders the world from the sun's point of view. The sun direction is driven
// by the live in-game clock, so the cast shadow sweeps the ground and water
// through the day/night cycle. Terrain, water and entities all receive it in
// their gbuffers fragment shaders via shadowtex0.

precision highp float;
precision highp int;

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform mat4 shadowModelView;
uniform mat4 shadowProjection;

void main() {
    // World space position (unproject from the camera-relative view)
    vec4 worldPos = gbufferModelViewInverse * (gl_ModelViewMatrix * gl_Vertex);
    gl_Position = shadowProjection * shadowModelView * worldPos;
}
