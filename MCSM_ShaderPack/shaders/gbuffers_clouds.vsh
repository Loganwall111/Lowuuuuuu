#version 120

// ============================================================================
// MCSM gbuffers_clouds.vsh — 2.5x extruded Story Mode cloud mesh (Iris path)
// ============================================================================
// Iris does not provide the vanilla-core CloudFaces buffer / CloudInfo UBO
// (those live in the mod's vanilla-core override at
// assets/dabywitherstormmod/shaders/core/rendertype_clouds.vsh), so the
// shader-pack cloud program must speak Iris's gbuffers dialect. Same math,
// same intent: unproject the cloud mesh into camera-relative world space,
// extrude the slab height by 2.5x, and hand worldPosCoord + vertexColor +
// vertexDistance to the fragment stage, which generates the cloud pattern
// 100% mathematically (zero PNG sheets, zero cloudTex samplers).

#define CLOUD_EXTRUSION // Enable 2.5x thick Story Mode cloud mesh
#define CLOUDS_ACTIVE   // Enable authentic Story Mode extruded clouds

precision highp float;
precision highp int;

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform vec3 sunPosition;
uniform float frameTimeCounter;

varying vec4 vertexColor;
varying vec3 worldPosCoord;
varying float vertexDistance;
varying vec3 vNormal;
varying float vSunY;

void main() {
    // 1. Transform vertex to camera-relative world coordinates (unprojecting).
    vec3 eyePos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    vec3 worldPos = (gbufferModelViewInverse * vec4(eyePos, 1.0)).xyz;

    // 2. Vertically scale mesh height by 2.5x to achieve the thick, boxy MCSM cloud volume.
    worldPos.y *= 2.5;

    // 3. Project back to clip space.
    gl_Position = gl_ProjectionMatrix * (gbufferModelView * vec4(worldPos, 1.0));

    // 4. Pass the channels cleanly to the fragment shader.
    vertexColor = gl_Color;
    worldPosCoord = worldPos;
    vertexDistance = length(eyePos);
    vNormal = normalize(gl_NormalMatrix * gl_Normal);
    vSunY = normalize(sunPosition).y;
}
