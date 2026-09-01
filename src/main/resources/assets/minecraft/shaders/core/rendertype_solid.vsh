#version 150
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
out vec4 vertexColor;
out vec2 texCoord0;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    float groundShadow = max(0.5, dot(Normal, normalize(vec3(0.3, 1.0, 0.2))));
    vertexColor = vec4(Color.rgb * groundShadow, Color.a);
}