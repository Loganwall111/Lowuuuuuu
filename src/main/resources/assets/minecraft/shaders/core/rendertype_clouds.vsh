#version 330

// Devouring Storms override of the vanilla 26.2 cloud vertex shader.
//
// Pipeline contract identical to vanilla (CloudInfo UBO, CloudFaces buffer,
// vertex expansion, fog outputs). This is the port of the uploaded official
// Telltale-tweak look, with the math corrected for the 26.2 geometry:
//
//   - Flat story-mode shading: TOP faces crisp and full-bright, SIDE faces
//     softly directional, BOTTOM faces shaded darker so undersides read as
//     translucent cloud instead of a hard white lid.
//   - Vertical translucency fade (the official fade): undersides melt into
//     the sky haze. Alpha is clamped to [0,1] — the first port could emit
//     NEGATIVE alpha on cloud undersides, which inverted the blend and made
//     rain clouds look like glitchy black boxes.
//   - Vertical scale stays 1.0: the official 2.5x multiplier was tuned for a
//     different base geometry; on 26.2's extruded rain cells it produced
//     towering box slabs. Chunkiness now comes from the flat shading, and
//     the storm deck handles the towering look.
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

// --- official Telltale-tweak knobs (26.2-corrected) ---
const float CloudHeight      = 1.0;   // no stretch: 26.2 cells are already extruded
const float CloudYOffset     = 0.0;
const float BrightnessBottom = 0.62;  // shaded, translucent undersides
const float BrightnessTop    = 1.0;   // crisp, full-bright tops
const float BrightnessNorth  = 0.78;
const float BrightnessSouth  = 0.78;
const float BrightnessWest   = 0.88;
const float BrightnessEast   = 0.88;
const float BottomAlpha      = 0.72;  // underside translucency (official fade floor)

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

    // World position (vanilla transform, offset knob kept)
    vec3 scaledVertex = faceVertex * CellSize;
    scaledVertex.y *= CloudHeight;
    vec3 pos = scaledVertex + (vec3(cellX, 0, cellZ) * CellSize) + CloudOffset + vec3(0, CloudYOffset, 0);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexDistance = fog_spherical_distance(pos);

    // Flat story-mode brightness per face direction
    float brightness = 1.0;
    if (useTopColor || direction == 1) brightness = BrightnessTop;
    else if (direction == 0) brightness = BrightnessBottom;
    else if (direction == 2) brightness = BrightnessNorth;
    else if (direction == 3) brightness = BrightnessSouth;
    else if (direction == 4) brightness = BrightnessWest;
    else if (direction == 5) brightness = BrightnessEast;

    // Official vertical translucency fade, normalized by the REAL slab height
    // and clamped to [0,1]: solid tops, translucent undersides. Never negative.
    float slabHeight = max(CellSize.y * CloudHeight, 0.001);
    float normalizedY = clamp(pos.y / slabHeight, 0.0, 1.0);
    float alpha = mix(BottomAlpha, 1.0, normalizedY);

    // Contract with the mod's rendertype_clouds.fsh: rgb = shade, a = alpha.
    // (CloudColor applied exactly once — the first port multiplied it twice,
    // which crushed cloud alpha and turned distant cells into flicker.)
    vertexColor = vec4(vec3(brightness) * CloudColor.rgb, alpha * CloudColor.a);
}
