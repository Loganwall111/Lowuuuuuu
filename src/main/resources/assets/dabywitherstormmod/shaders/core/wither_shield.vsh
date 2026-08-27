#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;
out vec3 normalVec;
out vec3 viewDir;
out vec3 worldPos;

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    vertexColor = Color;
    texCoord0 = UV0;
    normalVec = normalize(mat3(ModelViewMat) * Normal);
    viewDir = normalize(-viewPos.xyz);
    worldPos = Position;
}
