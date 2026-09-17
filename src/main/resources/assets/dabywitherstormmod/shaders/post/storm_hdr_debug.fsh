#version 330

// Shows ONE stage of the bloom pipeline on screen, by itself, so it is visible where the glow is
// lost instead of inferred from the finished frame.
//
// Written opaque and unblended on purpose: the stage replaces the frame rather than being added
// to it, so what is on screen is exactly the contents of that target and nothing else.
//
// The blurred stages hold small values -- a gaussian normalises, so spreading a bright point over
// a wide kernel leaves each pixel with a fraction of the original -- and would read as pure black
// at a gain of 1. Params.x amplifies them for inspection only; it is not part of the real path.

uniform sampler2D InSampler;

layout(std140) uniform DebugConfig {
    vec4 Params;   // x = gain, y = 1 to mark values above 1.0, z = 1 to show depth as greyscale
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 raw = texture(InSampler, texCoord).rgb;
    // Depth mode: reversed-Z, so near is white and far is black. A fully black screen here
    // means the scene depth buffer is not populated at composite time -- the mask can't work.
    if (Params.z > 0.5) {
        // Reversed-Z is extremely non-linear: near geometry sits close to 1.0 and everything
        // past a few blocks collapses toward 0. Shown raw, an ordinary scene is almost entirely
        // black and reveals nothing -- so the curve is stretched to bring the far range into a
        // visible band. Black now means genuinely nothing drawn (an EMPTY depth buffer), while
        // any structure at all means the buffer is populated.
        float d = clamp(raw.r, 0.0, 1.0);
        fragColor = vec4(vec3(pow(d, 0.15)), 1.0);
        return;
    }
    vec3 c = raw * Params.x;

    // HDR marker: anything genuinely above 1.0 in the SOURCE is tinted green, so "is the source
    // actually HDR" is answerable by looking at it. If the teeth show up white-not-green, they
    // are at or below 1.0 and the blur has no headroom to spread.
    if (Params.y > 0.5 && max(raw.r, max(raw.g, raw.b)) > 1.0) {
        c = mix(c, vec3(0.0, 1.0, 0.0), 0.5);
    }

    fragColor = vec4(c, 1.0);
}
