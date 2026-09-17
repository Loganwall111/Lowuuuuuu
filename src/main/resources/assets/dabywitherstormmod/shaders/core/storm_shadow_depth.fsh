#version 330

// Fragment stage of the shadow map. It writes one number: WHAT cast this.
//
// The depth attachment records how far the nearest thing to the sun was; this records whether that
// thing was the storm or the ground. That distinction is the whole reason terrain can be in the map
// at all. Terrain must not DARKEN anything -- Minecraft already lights caves and north faces
// correctly -- it is only there so the shadow pass can tell "the storm is blocking your sun" apart
// from "a hillside was blocking it long before the storm arrived". Doubling up on vanilla's own
// shading would be a worse bug than the one this fixes.

layout(std140) uniform ShadowCasterConfig {
    mat4 LightViewProj;
    vec4 CasterKind;   // x: 1 = the storm, 0 = terrain
};

out vec4 fragColor;

void main() {
    fragColor = vec4(CasterKind.x, 0.0, 0.0, 1.0);
}
