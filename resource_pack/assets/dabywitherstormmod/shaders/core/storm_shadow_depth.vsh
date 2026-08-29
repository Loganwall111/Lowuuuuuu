#version 330

// Vertex stage of the shadow map: the storm's own geometry, drawn from the sun.
//
// POSITION ONLY. The body model is textured, lit and shaded, and none of that matters here -- all
// this pass records is how far each surface is from the sun. Capturing only the position means the
// vertex format is one attribute, which keeps this shader trivial and, more to the point, keeps it
// identical under OpenGL and Vulkan: there are no interpolants to match up and nothing that depends
// on how the entity format happens to be laid out this version.

in vec3 Position;

layout(std140) uniform ShadowCasterConfig {
    // Camera-relative world space -> the sun's clip space. Built on the CPU each frame around
    // wherever the storm currently is, so the map is always packed with the storm rather than
    // spending most of its resolution on empty ground.
    mat4 LightViewProj;
    // Declared here too because the two stages share one block and must agree on its layout,
    // even though only the fragment stage reads it.
    vec4 CasterKind;
};

void main() {
    gl_Position = LightViewProj * vec4(Position, 1.0);
}
