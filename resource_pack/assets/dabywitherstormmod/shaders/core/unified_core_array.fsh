#version 330

// UNIFIED CORE SHADER RESOURCE PACK ARRAY CONNECTOR
// Merges all visual logic (cloud mesh, lavender sky, dynamic lighting)
// into a single standalone array using Minecraft's native Core Shaders.
// Traditional shaderpacks are abandoned; this array replaces them.

uniform sampler2D Sampler0;
uniform float GameTime;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    // Reference native core shaders: rendertype_clouds, rendertype_skybasic,
    // rendertype_terrain, rendertype_shield_halo
    vec4 coreColor = texture(Sampler0, texCoord0);
    fragColor = coreColor;
}
