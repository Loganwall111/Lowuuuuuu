#version 150

// Authentic Minecraft: Story Mode cloud vertex shader
// Flat directional shading + smooth underside atmospheric fade (non-negative)

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

const int FLAG_MASK_DIR      = 7;
const int FLAG_INSIDE_FACE   = 1 << 4;
const int FLAG_USE_TOP_COLOR = 1 << 5;
const int FLAG_EXTRA_Z       = 1 << 6;
const int FLAG_EXTRA_X       = 1 << 7;

layout(std140) uniform CloudInfo {
    vec4 CloudColor;
    vec3 CloudOffset;
    vec3 CellSize;
};

uniform isamplerBuffer CloudFaces;

// Story Mode Shading Knobs
const float CloudHeight      = 1.0;   // Clean slab geometry
const float CloudYOffset     = 0.0;
const float BrightnessBottom = 0.62;  // Softly shaded underside
const float BrightnessTop    = 1.00;  // Full crisp illuminated tops
const float BrightnessNorth  = 0.80;  // Gentle directional side lighting
const float BrightnessSouth  = 0.80;
const float BrightnessWest   = 0.88;
const float BrightnessEast   = 0.88;
const float BottomAlpha      = 0.70;  // Underside translucency floor

out float vertexDistance;
out vec4 vertexColor;

const vec3[] vertices = vec3[](
    vec3(1,0,0),vec3(1,0,1),vec3(0,0,1),vec3(0,0,0),   // Bottom
    vec3(0,1,0),vec3(0,1,1),vec3(1,1,1),vec3(1,1,0),   // Top
    vec3(0,0,0),vec3(0,1,0),vec3(1,1,0),vec3(1,0,0),   // North
    vec3(1,0,1),vec3(1,1,1),vec3(0,1,1),vec3(0,0,1),   // South
    vec3(0,0,1),vec3(0,1,1),vec3(0,1,0),vec3(0,0,0),   // West
    vec3(1,0,0),vec3(1,1,0),vec3(1,1,1),vec3(1,0,1)    // East
);

void main() {
    int quadVertex = gl_VertexID % 4;
    int index = (gl_VertexID / 4) * 3;

    int cellX = texelFetch(CloudFaces, index).r;
    int cellZ = texelFetch(CloudFaces, index + 1).r;
    int dirAndFlags = texelFetch(CloudFaces, index + 2).r;
    int direction = dirAndFlags & FLAG_MASK_DIR;
    bool isInsideFace = (dirAndFlags & FLAG_INSIDE_FACE) != 0;
    bool useTopColor = (dirAndFlags & FLAG_USE_TOP_COLOR) != 0;

    cellX = (cellX << 1) | ((dirAndFlags & FLAG_EXTRA_X) >> 7);
    cellZ = (cellZ << 1) | ((dirAndFlags & FLAG_EXTRA_Z) >> 6);

    vec3 faceVertex = vertices[(direction * 4) + (isInsideFace ? 3 - quadVertex : quadVertex)];

    vec3 scaledVertex = faceVertex * CellSize;
    scaledVertex.y *= CloudHeight;
    vec3 pos = scaledVertex + (vec3(cellX, 0, cellZ) * CellSize) + CloudOffset + vec3(0, CloudYOffset, 0);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexDistance = fog_spherical_distance(pos);

    float brightness = 1.0;
    if (useTopColor || direction == 1) brightness = BrightnessTop;
    else if (direction == 0) brightness = BrightnessBottom;
    else if (direction == 2) brightness = BrightnessNorth;
    else if (direction == 3) brightness = BrightnessSouth;
    else if (direction == 4) brightness = BrightnessWest;
    else if (direction == 5) brightness = BrightnessEast;

    float slabHeight = max(CellSize.y * CloudHeight, 0.001);
    float normalizedY = clamp(faceVertex.y, 0.0, 1.0);
    float alpha = mix(BottomAlpha, 1.0, normalizedY);

    // Apply color and clamp alpha to valid range [0, 1]
    vertexColor = vec4(vec3(brightness) * CloudColor.rgb, clamp(alpha * CloudColor.a, 0.0, 1.0));
}
