#version 150

#moj_import <mcsm_visuals.glsl>

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;
uniform vec3 PlayerPos;

out vec4 vertexColor;
out vec3 worldPos;
out float depthFactor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    worldPos = Position + PlayerPos;
    depthFactor = clamp(( -251.0 - worldPos.y) / 1781.0, 0.0, 1.0);
}
