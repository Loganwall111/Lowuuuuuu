#version 150

#moj_import <minecraft:fog.glsl>

in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    color.a *= clamp(1.0 - linear_fog_value(vertexDistance, 0.0, FogCloudsEnd), 0.0, 1.0);
    fragColor = color;
}
