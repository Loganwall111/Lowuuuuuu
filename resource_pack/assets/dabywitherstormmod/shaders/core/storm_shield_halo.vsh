#version 330

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

// EMISSIVE 3D SHIELD HALO — volumetric spherical mesh script
// Persistent Phase 4 through Phase 7; duplicates across all 3 split heads (Phase 6).

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
    // Spherical mesh volume for protective barrier
    float radius = 1.0 * PhaseScale;
    vec3 normalized = normalize(Position.xyz);
    worldPos.xyz = worldPos.xyz + normalized * radius * 0.1;

    gl_Position = ProjMat * worldPos;
    sphereCenter = worldPos.xyz;

    // Edge fading for localized alpha-blending transparency
    float distFromCenter = length(Position.xyz);
    edgeFade = 1.0 - smoothstep(0.0, 1.2, distFromCenter);
    alphaBlend = edgeFade * ShieldIntensity;
}
