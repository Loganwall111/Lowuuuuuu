#version 330

// Bloom, step 1: keep only what is actually GLOWING, and AMPLIFY it.
//
// The gain matters as much as the threshold. Minecraft renders to an 8-bit target, so no
// pixel is ever brighter than 1.0 -- there is no "extra" energy above white to bloom with,
// which is what real HDR bloom feeds on. Whatever is extracted here is at most as bright as
// the pixel itself, and the blur then spreads it over hundreds of pixels, dividing it down
// to nothing. Without a gain the result is mathematically invisible however high you push
// the final intensity.
//
// The weight is also LINEAR across the knee now. It used to be squared, which crushed
// anything that only just passed the threshold to zero -- teeth at luma 0.84 against a
// threshold of 0.82 came out at 0.008 of their own brightness.

uniform sampler2D InSampler;

layout(std140) uniform BloomConfig {
    vec4 Params; // x = threshold, y = knee softness, z = gain, w = 1 => masked to the storm
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 texel = texture(InSampler, texCoord);
    vec3 color = texel.rgb;

    // Perceptual brightness...
    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    // ...but luma alone judges a SATURATED colour as dark: the eye's purple is (1.0, 0.3, 1.0)
    // and scores only 0.49, so it could never bloom at any threshold the teeth also passed.
    // A coloured lamp is still a lamp, so the brightest channel gets a vote too.
    float maxChannel = max(color.r, max(color.g, color.b));
    float brightness = max(luma, maxChannel * 0.85);

    float threshold = Params.x;
    float knee = max(Params.y, 0.0001);
    float gain = Params.z;

    float weight;
    if (Params.w > 1.5) {
        // HEADS-ONLY buffer: it contains the storm's emitters and nothing else, so there is
        // nothing to test for. Pixels the heads didn't cover are already (0,0,0,0) and
        // contribute nothing on their own.
        weight = 1.0;
    } else if (Params.w > 0.5) {
        // MASKED. Three tests, and the alpha one is the important one:
        //
        //  * OPAQUE. Minecraft clears the frame to alpha ZERO, so alpha 1 means solid geometry
        //    wrote this pixel. Fog, clouds, the storm's backlight, particles and translucents
        //    all leave alpha below 1 and are excluded outright -- they were the whole problem.
        //  * SATURATED. The emitters are driven until a channel clips; lit terrain isn't.
        //  * BRIGHT. Excludes ordinary solid blocks, which are opaque but nowhere near this.
        float opaque = step(0.90, texel.a);
        float saturated = step(0.90, maxChannel);
        weight = opaque * saturated * step(threshold, brightness);
    } else {
        // UNMASKED: the old behaviour -- anything bright enough blooms, storm or not.
        // Soft knee so there's no hard ring at the cutoff, but linear.
        weight = clamp((brightness - threshold) / knee, 0.0, 1.0);
    }

    vec3 result = color * weight * gain;

    // ALPHA IS COVERAGE, and for the heads-only path it decides everything.
    //
    // That path's glow is composited with vanilla's ENTITY_OUTLINE_BLIT, which is
    // (SRC_ALPHA, ONE_MINUS_SRC_ALPHA) -- an alpha BLEND, not an add. Writing alpha 1
    // everywhere therefore replaced the entire screen with this buffer, which is almost
    // entirely black: a black screen. Alpha has to say "how much glow is here", so the
    // untouched parts of the buffer (cleared to zero) leave the world untouched.
    //
    // The screen-space chains don't blend -- their last pass writes the frame directly -- so
    // their alpha is irrelevant and stays at 1.
    // The heads-only glow is composited with an ALPHA blend, so coverage is what decides how
    // much of it actually lands on the frame -- a low coverage is a faint glow no matter how
    // high the gain. Boosted well past the raw brightness so the core of the glow blends in at
    // full strength and only the outer falloff is partial.
    float coverage = (Params.w > 1.5)
            ? clamp(max(result.r, max(result.g, result.b)) * 2.5, 0.0, 1.0)
            : 1.0;

    // Deliberately below full scale. Every target in this chain is 8-bit, so anything that
    // reaches 1.0 early clips at the next pass and the round gradient turns into a square
    // plateau. The composite's tone map restores the brightness at the end, where clipping
    // can't happen.
    fragColor = vec4(result * 0.85, coverage);
}
