#version 330

// One axis of a gaussian blur, run over FLOATING-POINT targets.
//
// The whole point of doing this ourselves rather than through a PostChain: the chain's
// intermediate targets are 8-bit, so anything brighter than white was clipped at every pass. A
// clipped separable blur is not a blur -- the horizontal pass saturates a bar and the vertical
// pass saturates that into a rectangle, which is why the glow kept coming out as a hard square
// no matter how the weights were tuned. In a float target the emitters can sit well above 1.0,
// the blur stays a true gaussian, and the single tone map at the end turns that into a smooth
// falloff.

uniform sampler2D InSampler;

layout(std140) uniform BlurConfig {
    vec4 Step;   // xy = one texel along the blur axis, z = radius in texels
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float radius = max(Step.z, 1.0);
    float sigma = radius * 0.5;
    float twoSigmaSq = 2.0 * sigma * sigma;

    // Fixed tap BUDGET rather than one tap per texel. A one-texel stride made cost grow
    // linearly with radius -- a wide radius meant ~70 taps per pass, four passes, full
    // resolution, which is where the frame spikes came from. Striding more than one texel for
    // large radii keeps the cost flat; the sampler is LINEAR, so each tap already averages the
    // texels it lands between and the curve stays smooth instead of banding.
    const float MAX_TAPS_PER_SIDE = 12.0;
    float stride = max(1.0, radius / MAX_TAPS_PER_SIDE);

    vec3 sum = texture(InSampler, texCoord).rgb;
    float total = 1.0;

    for (float i = stride; i <= radius; i += stride) {
        float w = exp(-(i * i) / twoSigmaSq);
        sum += texture(InSampler, texCoord + Step.xy * i).rgb * w;
        sum += texture(InSampler, texCoord - Step.xy * i).rgb * w;
        total += 2.0 * w;
    }

    fragColor = vec4(sum / total, 1.0);
}
