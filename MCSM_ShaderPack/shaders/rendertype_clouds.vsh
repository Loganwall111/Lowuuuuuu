#version 150

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

const float CloudFadeAlpha = 0; // 0 = a full 0 alpha fade
const float CloudHeight = 2.5; // vertical scaling
const float CloudYOffset = 0.0; // Y offset
const float BrightnessBottom = 1.0;
const float BrightnessTop = 1.0;
const float BrightnessNorth = 1.0;
const float BrightnessSouth = 1.0;
const float BrightnessWest = 1.0;
const float BrightnessEast = 1.0;

out float vertexDistance;
out vec4 vertexColor;

const vec3[] NORMAL_DIRECTIONS = vec3[](
    vec3(0, -1, 0),
    vec3(0, 1, 0),
    vec3(0, 0, -1),
    vec3(0, 0, 1),
    vec3(-1, 0, 0),
    vec3(1, 0, 0)
);

const vec3[][] VERTICES = vec3[][](
    vec3[](vec3(0, 0, 0), vec3(1, 0, 0), vec3(1, 0, 1), vec3(0, 0, 1)),
    vec3[](vec3(0, 1, 1), vec3(1, 1, 1), vec3(1, 1, 0), vec3(0, 1, 0)),
    vec3[](vec3(1, 1, 0), vec3(1, 0, 0), vec3(0, 0, 0), vec3(0, 1, 0)),
    vec3[](vec3(0, 1, 1), vec3(0, 0, 1), vec3(1, 0, 1), vec3(1, 1, 1)),
    vec3[](vec3(0, 1, 0), vec3(0, 0, 0), vec3(0, 0, 1), vec3(0, 1, 1)),
    vec3[](vec3(1, 1, 1), vec3(1, 0, 1), vec3(1, 0, 0), vec3(1, 1, 0))
);

vec3 lerp(vec3 a, vec3 b, float t) {
    return a + t * (b - a);
}

float fog_spherical_distance(vec3 pos) {
    return length(pos);
}

void main() {
    int faceIndex = gl_VertexID / 4;
    int vertexIndex = gl_VertexID % 4;

    int faceData = texelFetch(CloudFaces, faceIndex).r;
    int dir = faceData & FLAG_MASK_DIR;

    vec3 baseVertex = VERTICES[dir][vertexIndex];
    vec3 normal = NORMAL_DIRECTIONS[dir];

    // Decode position from faceData
    int posX = (faceData >> 8) & 0xFF;
    int posY = (faceData >> 16) & 0xFF;
    int posZ = (faceData >> 24) & 0xFF;

    vec3 cellPos = vec3(posX, posY, posZ) * CellSize + CloudOffset;
    vec3 worldPos = cellPos + baseVertex * CellSize;
    worldPos.y = (worldPos.y + CloudYOffset) * CloudHeight;

    vec3 viewPos = (ModelViewMat * vec4(worldPos, 1.0)).xyz;
    gl_Position = ProjMat * vec4(viewPos, 1.0);

    vertexDistance = fog_spherical_distance(viewPos);

    // Flat MCSM story mode cloud coloring
    vec4 faceColor = CloudColor;
    if (dir == 0) faceColor.rgb *= BrightnessBottom;
    else if (dir == 1) faceColor.rgb *= BrightnessTop;
    else if (dir == 2) faceColor.rgb *= BrightnessNorth;
    else if (dir == 3) faceColor.rgb *= BrightnessSouth;
    else if (dir == 4) faceColor.rgb *= BrightnessWest;
    else if (dir == 5) faceColor.rgb *= BrightnessEast;

    vertexColor = faceColor;
}
