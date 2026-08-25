#version 330

// World emissive glow, final combine: ADDITIVE.
//
// This is the whole-screen variant of the bloom combine. Unlike the storm's
// heads-only combiner (which subtracts the raw frame to keep only the spill,
// because there the glow is layered on separately), this pass OWNS the frame:
// it writes the finished image, so it must carry the scene through untouched
// and add the glow on top. A subtractive combiner here turns the entire
// screen into "glow minus world" -- black everywhere but the halos, which is
// exactly the black-screen bug this shader exists to prevent.

uniform sampler2D InSampler;    // the scene (minecraft:main)
uniform sampler2D BloomSampler; // the blurred bright-pass

layout(std140) uniform BloomConfig {
    vec4 Params; // x = glow intensity, y = unused, z = unused, w = unused
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 glow = texture(BloomSampler, texCoord).rgb;
    // SATURATE the glow before adding it. A blurred halo averages toward grey,
    // and a grey add reads as WHITE light -- the "lighting is there but not
    // coloured" look. Pushing each pixel toward its dominant hue first is
    // what makes a torch halo read orange and lava read red.
    float luma = dot(glow, vec3(0.2126, 0.7152, 0.0722));
    glow = mix(vec3(luma), glow, 1.75);
    fragColor = vec4(base + glow * Params.x, 1.0);
}
