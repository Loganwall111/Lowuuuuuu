#version 330

// One axis of a separable gaussian blur. Run horizontally then vertically, twice, to build the
// soft halo.
//
// Steps ONE texel per tap, not two. The two-texel trick (sampling between texels and letting
// linear filtering average a pair) halves the cost, but at a small radius it leaves only a
// handful of samples across the whole kernel -- and a handful of coarse samples smeared along
// two axes turns a small bright shape into a blocky SQUARE instead of a round glow. This is a
// glow; it has to be smooth, and the extra taps are cheap on a buffer this sparse.

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 sampleStep = (1.0 / InSize) * BlurDir;
    float radius = max(Radius, 1.0);

    // sigma set so the kernel has faded to nothing by the time it reaches the radius, which is
    // what stops a visible hard edge at the end of the blur.
    float sigma = radius * 0.45;
    float twoSigmaSq = 2.0 * sigma * sigma;

    vec4 sum = texture(InSampler, texCoord);
    float total = 1.0;

    for (float i = 1.0; i <= radius; i += 1.0) {
        float w = exp(-(i * i) / twoSigmaSq);
        sum += texture(InSampler, texCoord + sampleStep * i) * w;
        sum += texture(InSampler, texCoord - sampleStep * i) * w;
        total += 2.0 * w;
    }

    fragColor = sum / total;
}
