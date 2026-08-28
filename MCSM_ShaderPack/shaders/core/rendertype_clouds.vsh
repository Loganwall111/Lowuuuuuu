#version 120

#define CLOUD_EXTRUSION // Enable 2.5x thick Story Mode cloud mesh
#define CLOUDS_ACTIVE // Enable authentic Story Mode extruded clouds

precision highp float;
precision highp int;

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform float frameTimeCounter;
uniform int worldTime;

varying vec4 vColor;
varying vec2 vTexCoord;
varying vec3 vWorldPos;
varying vec3 vNormal;
varying float vFogFactor;

void main() {
    // 1. Transform vertex to camera-relative world coordinates (unprojecting)
    vec3 eyePos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    vec3 worldPos = (gbufferModelViewInverse * vec4(eyePos, 1.0)).xyz;

    // 2. Vertically scale mesh height by 2.5x to achieve the thick, boxy MCSM cloud volume
    worldPos.y *= 2.5;

    // 3. Project back to clip space
    gl_Position = gl_ProjectionMatrix * (gbufferModelView * vec4(worldPos, 1.0));

    // 4. Pass varying attributes cleanly to fragment shader
    vTexCoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vNormal = normalize(gl_NormalMatrix * gl_Normal);
    vColor = gl_Color;
    vWorldPos = worldPos;
    vFogFactor = clamp((length(eyePos) - 160.0) / 180.0, 0.0, 1.0);
}
