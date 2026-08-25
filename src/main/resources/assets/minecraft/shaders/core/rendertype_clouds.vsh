#version 330

// Devouring Storms override of the vanilla 26.2 cloud vertex shader.
//
// The pipeline contract is kept identical to vanilla (CloudInfo UBO,
// CloudFaces buffer, vertex expansion, fog outputs) - only the per-face
// shading table changes, so the deck and any vanilla clouds that are still
// visible share the same Story-Mode look:
//
//   - TOP faces: crisp, full-bright white.
//   - SIDE faces: soft directional shading (east/west catch more light than
//     north/south), reading as sun-scattered edges.
//   - BOTTOM faces: deep ambient shadow at ~55% alpha, so undersides stay
//     translucent and melt into the sky haze instead of forming a hard lid.
//
// Depth function (GL_LEQUAL) and translucent alpha blending are properties of
// the clouds RenderPipeline and remain vanilla-correct for this pass.

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

// --- official Telltale-tweak knobs, ported from the uploaded shader ---
const float CloudFadeAlpha   = 0.0;  // 0 = a full 0 alpha fade at the far side
const float CloudHeight      = 2.5;  // vertical scaling (the chunky look)
const float CloudYOffset     = 0.0;
const float BrightnessBottom = 1.0;
const float BrightnessTop    = 1.0;
const float BrightnessNorth  = 1.0;
const float BrightnessSouth  = 1.0;
const float BrightnessWest   = 1.0;
const float BrightnessEast   = 1.0;

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

    // World position with the official scaling & offset
    vec3 scaledVertex = faceVertex * CellSize;
    scaledVertex.y *= CloudHeight;
    vec3 pos = scaledVertex + (vec3(cellX, 0, cellZ) * CellSize) + CloudOffset + vec3(0, CloudYOffset, 0);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexDistance = fog_spherical_distance(pos);

    // Official flat brightness per face
    float brightness = 1.0;
    if (useTopColor || direction == 1) brightness = BrightnessTop;
    else if (direction == 0) brightness = BrightnessBottom;
    else if (direction == 2) brightness = BrightnessNorth;
    else if (direction == 3) brightness = BrightnessSouth;
    else if (direction == 4) brightness = BrightnessWest;
    else if (direction == 5) brightness = BrightnessEast;

    // Official vertical alpha fade
    float vertexY = pos.y - CloudOffset.y;
    float normalizedY = clamp(vertexY / CloudHeight, 0.0, 1.0);
    float dirRel = clamp(CloudOffset.y / CloudHeight, -1.0, 1.0);
    float fadeBelow = mix(normalizedY, 1.0, CloudFadeAlpha);
    float fadeAbove = mix(1.0 - normalizedY, 1.0, CloudFadeAlpha);
    float mixFactor = (dirRel + 1.0) * 0.5;
    float fade = mix(fadeBelow, fadeAbove, mixFactor);

    vec3 rgb = vec3(brightness);
    float finalA = CloudColor.a * (0.8 - fade);
    vertexColor = vec4(rgb, finalA) * CloudColor;
}
