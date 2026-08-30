#version 330

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

// EMISSIVE 3D SHIELD HALO — native core shader resource pack array
// Persistent Phase 4-7; duplicates across all 3 split heads during Phase 6.
// Volumetric spherical mesh with localized alpha-blending transparency and edge fading.

in vec4 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float ShieldIntensity;
uniform float PhaseScale;

out vec3 sphereCenter;
out float edgeFade;
out float alphaBlend;

void main() {
    vec4 worldPos = ModelViewMat * Position;
    float radius = 1.0 * PhaseScale;
    vec3 normalized = normalize(Position.xyz);
    worldPos.xyz = worldPos.xyz + normalized * radius * 0.08;
    gl_Position = ProjMat * worldPos;
    sphereCenter = worldPos.xyz;
    float distFromCenter = length(Position.xyz);
    edgeFade = 1.0 - smoothstep(0.0, 1.2, distFromCenter);
    alphaBlend = edgeFade * ShieldIntensity;
}
