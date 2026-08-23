#version 330

// Bloom, blur stage output: the light that spills OUTSIDE the shape.
//
// Raw   = the emitters, unblurred
// In    = the TIGHT blur
// Bloom = the WIDE blur
//
// The subtraction is the important part. Blurring a shape spreads light both outwards AND across
// its own interior, so a tooth seen face-on -- a large solid white area -- produced a large solid
// glow sitting on top of itself, which saturates and reads as a bright clipped slab. Seen
// edge-on the same tooth is thin, only the spill shows, and it looked fine. That is exactly the
// "sideways good, head-on bad" split.
//
// Taking the blurred result MINUS the original leaves only what fell outside the source: inside
// the tooth the two are equal and cancel to nothing, and the tooth's own brightness is already on
// screen from its emissive pass. The glow becomes a halo around the shape at every angle instead
// of a wash over it.

uniform sampler2D RawSampler;
uniform sampler2D InSampler;
uniform sampler2D BloomSampler;

layout(std140) uniform BloomConfig {
    vec4 Params; // x = wide weight, y = tight weight
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 raw = texture(RawSampler, texCoord).rgb;
    vec3 tight = texture(InSampler, texCoord).rgb;
    vec3 wide = texture(BloomSampler, texCoord).rgb;

    vec3 glow = tight * Params.y + wide * Params.x;
    fragColor = vec4(max(glow - raw, vec3(0.0)), 1.0);
}
