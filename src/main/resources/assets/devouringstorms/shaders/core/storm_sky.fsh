#version 330

// Fragment stage of the storm sky pass: sample the sky plate and multiply by
// the per-vertex tint/weight. The pipeline blends ONE,ONE (additive), so
// pure-black regions of a plate simply contribute nothing and the glow composes
// over the base sky disc without ever boxing out terrain.

uniform sampler2D Sampler0;

in vec2 vUv;
in vec4 vColor;

out vec4 fragColor;

void main() {
    fragColor = texture(Sampler0, vUv) * vColor;
}
