#version 330

// Devouring Storms override of the vanilla 26.2 cloud fragment shader.
// Keeps the vanilla fog fade (clouds dissolve into the sky haze at distance)
// and leaves the depth/blend state to the clouds RenderPipeline, which stays
// GL_LEQUAL with translucent alpha blending as in vanilla.

#moj_import <minecraft:fog.glsl>


in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    color.a *= 1.0f - linear_fog_value(vertexDistance, 0, FogCloudsEnd);
    fragColor = color;
}
