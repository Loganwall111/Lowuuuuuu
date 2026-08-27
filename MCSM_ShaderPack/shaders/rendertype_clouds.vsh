#version 120

// Identical high precision header to eliminate GPU compiler crashes
precision highp float;
precision highp int;

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform float frameTimeCounter;

varying vec4 vColor;
varying vec2 vTexCoord;
varying vec3 vWorldPos;
varying vec3 vNormal;
varying float vFogFactor;

// Global Story Mode Cloud Extrusion (2.5x vertical scaling)
const float CloudHeight = 2.5;

void main() {
    // 1. Transform vertex to camera-relative world coordinates
    vec3 eyePos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    vec3 worldPos = (gbufferModelViewInverse * vec4(eyePos, 1.0)).xyz;
    vec3 normal = gl_Normal;

    // 2. Vertically scale cloud geometry bounds by 2.5x for Story Mode chunk layout thickness
    float localExtrusion = 4.0 * (CloudHeight - 1.0); // 6.0 blocks expansion
    if (normal.y > 0.3) {
        worldPos.y += localExtrusion;
    } else if (abs(normal.y) < 0.3) {
        int q = int(mod(float(gl_VertexID), 4.0));
        if (q == 1 || q == 2) {
            worldPos.y += localExtrusion;
        }
    }

    // 3. Project back to clip space
    gl_Position = gl_ProjectionMatrix * (gbufferModelView * vec4(worldPos, 1.0));

    // 4. Pass varying attributes cleanly to fragment shader
    vTexCoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vNormal = normalize(gl_NormalMatrix * gl_Normal);
    vColor = gl_Color;
    vWorldPos = worldPos;
    vFogFactor = clamp((length(eyePos) - 160.0) / 180.0, 0.0, 1.0);
}
