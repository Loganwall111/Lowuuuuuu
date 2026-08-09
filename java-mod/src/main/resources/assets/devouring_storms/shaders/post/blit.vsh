#version 150

// Fullscreen blit for the post pipeline.
in vec4 Position;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

out vec2 texCoord;

void main() {
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);
    texCoord = Position.xy / OutSize * (OutSize / InSize) * (InSize / OutSize);
    texCoord = Position.xy / InSize;
}
