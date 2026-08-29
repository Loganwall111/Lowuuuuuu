#version 330

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

// FORCE NATIVE CLOUD RE-ANCHORING
// Custom unprojection + vertical scaling for 3D blocky cloud mesh arrays.
// Discards loose image registers; noise is generated mathematically inside the fragment stage.

in vec4 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out float cloudDepth;
out vec3 cloudCoord;

void main() {
    vec4 worldPos = ModelViewMat * Position;
    // Vertical extrude cloud coordinate bounds by 2.5x to lock Story Mode cloud block volume thickness
    worldPos.y *= 2.5;
    cloudCoord = worldPos.xyz;
    gl_Position = ProjMat * worldPos;
    cloudDepth = worldPos.z;
}
