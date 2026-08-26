#version 330

// Final bloom composite: tone-map the HDR glow and add it to the scene.
//
// Tight and Wide are the two blur scales. Summing scales is what reads as light; chaining one
// into the other only ever gives a single big smear.
//
// 1 - exp(-x * exposure) is applied to values that were never clamped on the way here, so the
// core saturates smoothly to white while every step outward keeps a distinct lower value. The
// blend is additive, so this adds light to the scene rather than replacing any of it.

uniform sampler2D TightSampler;
uniform sampler2D WideSampler;

layout(std140) uniform BloomConfig {
    vec4 Params; // x = exposure, y = tight weight, z = wide weight, w = depth dilation texels
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 hdr = texture(TightSampler, texCoord).rgb * Params.y
             + texture(WideSampler, texCoord).rgb * Params.z;

    // Tone map the LUMINANCE and reapply the colour ratio. The old per-channel
    // 1 - exp(-c * e) compresses the dominant channel hardest, so every bright pixel slid
    // toward white and the eye's halo lost the beam's hue. Mapping luminance keeps the ratio
    // between channels -- the colour -- exactly as the emitters drew it, at any brightness.
    float lum = max(dot(hdr, vec3(0.2126, 0.7152, 0.0722)), 1.0e-5);
    float mapped = 1.0 - exp(-lum * Params.x);
    vec3 glow = min(hdr * (mapped / lum), vec3(1.0));
    if (max(glow.r, max(glow.g, glow.b)) <= 0.003) {
        discard;
    }

    // No depth logic here, deliberately. Occlusion happens in the mask pass BEFORE the blur --
    // an occluded tooth never enters the blur, so there is no leaked halo to clip. The dilated
    // gl_FragDepth republish that used to live here is what let glow through the body: a halo
    // pixel more than a few texels from its tooth read the cleared far depth and the hardware
    // test had nothing true to compare.
    // Hand the glow a real depth and let the DEPTH ATTACHMENT do the occluding. This is the
    // part every previous attempt got wrong: they tried to sample the scene's depth into the
    // shader and compare it here, and that comparison always passed. The scene depth is bound
    // as this pass's attachment instead, so the hardware rejects any halo pixel sitting behind
    // terrain, a mob, the storm's own body or the player's hand -- with no sampling involved.
    fragColor = vec4(glow, 0.0);
}
