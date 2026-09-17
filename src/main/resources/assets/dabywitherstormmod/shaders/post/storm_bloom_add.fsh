#version 330

// The composite: scene + glow, DEPTH-CORRECT.
//
// The glow is a fullscreen quad, so on its own it would add light over everything -- terrain in
// front of the storm, mobs, the head's own skull, your hand. To stop that, the emitters write
// their DEPTH into the bloom buffer, and this shader republishes that depth as gl_FragDepth so
// the hardware can depth-test the glow against the scene exactly like any other geometry.
//
// The catch, and why the depth is dilated: the blur spreads light well beyond the pixels the
// teeth actually covered, and out there the buffer's depth is still the cleared far value. Test
// that naively and the entire halo is rejected by anything at all. Taking the NEAREST depth from
// a small neighbourhood (reversed-Z, so nearest = largest) gives the halo the depth of the tooth
// it came from, which is what it should be occluded as.

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform BloomConfig {
    vec4 Params; // x = intensity, y = dilation radius in texels
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    // TONE MAP, do not multiply.
    //
    // A linear gain is what turned this into a blob: multiply the blurred glow by 2 and
    // everything inside the blur radius lands past 1.0, where the output clamps -- a wide
    // uniformly white area with no gradient anywhere in it. The brighter you make it, the bigger
    // the flat white region gets.
    //
    // 1 - exp(-x * exposure) rises steeply from zero and approaches 1 without ever reaching it,
    // so the tooth's core reads white-hot while every step outward keeps a distinct, lower
    // value: bloom, light, dimmer, dimmer. Nothing clips, so the falloff survives at any
    // brightness.
    vec3 blurred = texture(InSampler, texCoord).rgb;
    vec3 glow = vec3(1.0) - exp(-blurred * Params.x);
    if (max(glow.r, max(glow.g, glow.b)) <= 0.002) {
        discard;
    }

    vec2 texel = 1.0 / vec2(textureSize(DepthSampler, 0));
    float step = max(Params.y, 1.0);

    // Nearest depth in a 5x5 neighbourhood, so the halo inherits its tooth's depth.
    float depth = 0.0;
    for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
            depth = max(depth, texture(DepthSampler, texCoord + vec2(x, y) * texel * step).r);
        }
    }

    // Nothing emitted anywhere near here: no glow to place, and no depth to place it at.
    if (depth <= 0.0) {
        discard;
    }

    gl_FragDepth = depth;
    fragColor = vec4(glow, 0.0);
}
