#version 330

/*
 * rendertype_clouds.vsh -- Minecraft Story Mode volumetric clouds
 * Target: Minecraft 26.2 core shader pipeline.
 *
 * IMPORTANT -- this is NOT the old Position/Color/UV0 cloud shader.
 * As of the modern cloud renderer there are no vertex attributes at all:
 * the cloud deck is a texel buffer of packed cell coordinates, and every
 * vertex is generated from gl_VertexID. Writing the legacy version against
 * `in vec3 Position` would compile and then draw nothing, because no such
 * attribute is ever bound. This file follows the real pipeline.
 *
 * What it adds on top of vanilla:
 *   - 2.5x vertical extrusion, so the deck becomes thick blocky geometry
 *     instead of a thin slab
 *   - a real world-space normal per face, handed to the fragment stage for
 *     Light0_Direction shadowing
 *   - face identity (top / bottom / side) so the fragment shader can darken
 *     undersides and trailing edges
 *
 * Place at: assets/minecraft/shaders/core/rendertype_clouds.vsh
 */

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

const int FLAG_MASK_DIR = 7;
const int FLAG_INSIDE_FACE = 1 << 4;
const int FLAG_USE_TOP_COLOR = 1 << 5;
const int FLAG_EXTRA_Z = 1 << 6;
const int FLAG_EXTRA_X = 1 << 7;

layout(std140) uniform CloudInfo {
    vec4 CloudColor;
    vec3 CloudOffset;
    vec3 CellSize;
};

uniform isamplerBuffer CloudFaces;

out float vertexDistance;
out vec4 vertexColor;
out vec3 worldNormal;
out vec3 worldPos;
out float faceKind;     // +1 top, -1 bottom, 0 side wall

// --------------------------------------------------------------- extrusion
// Vertical thickness multiplier. Story Mode clouds are deep, chunky slabs.
const float EXTRUSION = 2.5;

const vec3[] vertices = vec3[](
    // Bottom face
    vec3(1, 0, 0),
    vec3(1, 0, 1),
    vec3(0, 0, 1),
    vec3(0, 0, 0),
    // Top face
    vec3(0, 1, 0),
    vec3(0, 1, 1),
    vec3(1, 1, 1),
    vec3(1, 1, 0),
    // North face
    vec3(0, 0, 0),
    vec3(0, 1, 0),
    vec3(1, 1, 0),
    vec3(1, 0, 0),
    // South face
    vec3(1, 0, 1),
    vec3(1, 1, 1),
    vec3(0, 1, 1),
    vec3(0, 0, 1),
    // West face
    vec3(0, 0, 1),
    vec3(0, 1, 1),
    vec3(0, 1, 0),
    vec3(0, 0, 0),
    // East face
    vec3(1, 0, 0),
    vec3(1, 1, 0),
    vec3(1, 1, 1),
    vec3(1, 0, 1)
);

// Per-face world normals, in the same order as the vertex table above:
// bottom, top, north, south, west, east.
const vec3[] faceNormals = vec3[](
    vec3( 0.0, -1.0,  0.0),
    vec3( 0.0,  1.0,  0.0),
    vec3( 0.0,  0.0, -1.0),
    vec3( 0.0,  0.0,  1.0),
    vec3(-1.0,  0.0,  0.0),
    vec3( 1.0,  0.0,  0.0)
);

const vec4[] faceColors = vec4[](
    vec4(0.7, 0.7, 0.7, 1.0),   // bottom
    vec4(1.0, 1.0, 1.0, 1.0),   // top
    vec4(0.8, 0.8, 0.8, 1.0),   // north
    vec4(0.8, 0.8, 0.8, 1.0),   // south
    vec4(0.9, 0.9, 0.9, 1.0),   // west
    vec4(0.9, 0.9, 0.9, 1.0)    // east
);

void main() {
    int quadVertex = gl_VertexID % 4;
    int index = (gl_VertexID / 4) * 3;

    int cellX = texelFetch(CloudFaces, index).r;
    int cellZ = texelFetch(CloudFaces, index + 1).r;
    int dirAndFlags = texelFetch(CloudFaces, index + 2).r;
    int direction = dirAndFlags & FLAG_MASK_DIR;
    bool isInsideFace = (dirAndFlags & FLAG_INSIDE_FACE) == FLAG_INSIDE_FACE;
    bool useTopColor = (dirAndFlags & FLAG_USE_TOP_COLOR) == FLAG_USE_TOP_COLOR;
    cellX = (cellX << 1) | ((dirAndFlags & FLAG_EXTRA_X) >> 7);
    cellZ = (cellZ << 1) | ((dirAndFlags & FLAG_EXTRA_Z) >> 6);

    vec3 faceVertex = vertices[(direction * 4) + (isInsideFace ? 3 - quadVertex : quadVertex)];

    // ---------------------------------------------------------------------
    // 2.5x extrusion.
    //
    // faceVertex.y is 0 on the bottom deck and 1 on the top deck. Scaling it
    // about the slab midpoint pushes the top up and the bottom down in equal
    // measure, so the deck thickens around its own centre line and stays
    // where you expect in the sky rather than drifting upward.
    // ---------------------------------------------------------------------
    vec3 shaped = faceVertex;
    shaped.y = 0.5 + (faceVertex.y - 0.5) * EXTRUSION;

    vec3 pos = (shaped * CellSize) + (vec3(cellX, 0, cellZ) * CellSize) + CloudOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    // ---------------------------------------------------------------------
    // Hand the fragment stage what it needs for real lighting.
    //
    // Inside faces are wound backwards, so their geometric normal points the
    // other way -- flip it or the interior of a cloud lights inside out.
    // ---------------------------------------------------------------------
    vec3 n = faceNormals[direction];
    worldNormal = isInsideFace ? -n : n;

    // direction 0 = bottom, 1 = top, 2..5 = side walls
    faceKind = (direction == 1) ? 1.0 : ((direction == 0) ? -1.0 : 0.0);

    worldPos = pos;
    vertexDistance = fog_spherical_distance(pos);
    vertexColor = (useTopColor ? faceColors[1] : faceColors[direction]) * CloudColor;
}
