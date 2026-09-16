#version 150

in vec3 Position;
out vec2 vertexUV;

void main() {
    // The sky renderer submits a clip-space quad, so this pass stays behind all world geometry.
    vertexUV = Position.xy * 0.5 + 0.5;
    gl_Position = vec4(Position.xy, 0.999, 1.0);
}
