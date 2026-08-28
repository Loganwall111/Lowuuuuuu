#version 120

// ============================================================================
// MCSM rendertype_clouds.vsh — 2.5x extruded Story Mode cloud mesh
// (mirror of gbuffers_clouds.vsh for pipelines that route clouds through the
//  rendertype program)
// ============================================================================

#define CLOUD_EXTRUSION // Enable 2.5x thick Story Mode cloud mesh
#define CLOUDS_ACTIVE   // Enable authentic Story Mode extruded clouds

precision highp float;
precision highp int;

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform float frameTimeCounter;
uniform vec3 sunPosition;

varying vec4 vColor;
varying vec2 vTexCoord;
varying vec3 vWorldPos;
varying vec3 vNormal;
varying float vFogFactor;
varying float vSunY;

void main() {
    vec3 eyePos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    vec3 worldPos = (gbufferModelViewInverse * vec4(eyePos, 1.0)).xyz;

    // 2.5x thick MCSM cloud volume
    worldPos.y *= 2.5;

    gl_Position = gl_ProjectionMatrix * (gbufferModelView * vec4(worldPos, 1.0));

    vTexCoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vNormal = normalize(gl_NormalMatrix * gl_Normal);
    vColor = gl_Color;
    vWorldPos = worldPos;
    vFogFactor = clamp((length(eyePos) - 160.0) / 180.0, 0.0, 1.0);
    vSunY = normalize(sunPosition).y;
}
