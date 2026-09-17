#version 330

// Occlusion mask, run on the emissive source BEFORE any blur.
//
// This is where the bloom learns about geometry. The old approach tried to occlude the finished
// halo in the composite, but a halo pixel doesn't know which tooth it came from, so it can't
// know whether that tooth is hidden -- which is exactly why glow leaked through the head, the
// body and the terrain. Masking the SOURCE instead means an occluded tooth contributes nothing
// to the blur at all: no source, no halo. A visible tooth's halo still spills softly over the
// edges of whatever is next to it, which is how real screen-space glare behaves.

uniform sampler2D InSampler;          // emissive colour, HDR
uniform sampler2D BloomDepthSampler;  // depth the emitters wrote (reversed-Z; 0.0 = nothing here)
uniform sampler2D SceneDepthSampler;  // the finished scene's depth

layout(std140) uniform MaskConfig {
    vec4 Params; // x: 0 = mask, 1 = compare visualisation, 2 = RAW values for readback,
                 //    3 = UV alignment checkerboard
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 c = texture(InSampler, texCoord).rgb;
    float bloomD = texture(BloomDepthSampler, texCoord).r;
    float sceneD = texture(SceneDepthSampler, texCoord).r;

    // Reversed-Z: LARGER is NEARER. The emitter is visible when nothing in the scene is nearer
    // than it. The epsilon is relative: the tooth is drawn into both depth buffers with the same
    // matrices, so the two values agree to rounding, and the tolerance only has to absorb that.
    float eps = max(5.0e-7, sceneD * 3.0e-5);
    float visible = bloomD > 0.0 ? step(sceneD - eps, bloomD) : 0.0;

    // RAW: the two depths and the verdict as DATA, for StormBloomDiag to read back and print.
    // A picture cannot answer "is sceneDepth exactly zero here"; a number can.
    if (Params.x > 1.5 && Params.x < 2.5) {
        fragColor = vec4(bloomD, sceneD, visible, 1.0);
        return;
    }

    // UV alignment: a checkerboard keyed to the SOURCE texture's own texel grid, tinted by
    // whether the scene depth sampled at the same coordinate is populated. If the two textures
    // are sampled at the same place this lines up with the teeth; if it shears or repeats at a
    // different scale, the resolutions disagree and the mask is comparing unrelated pixels.
    if (Params.x > 2.5) {
        vec2 srcTexel = vec2(textureSize(InSampler, 0));
        vec2 dstTexel = vec2(textureSize(SceneDepthSampler, 0));
        vec2 g = floor(texCoord * srcTexel / 8.0);
        float checker = mod(g.x + g.y, 2.0);
        // Red channel: source grid. Green: scene-depth grid. They must coincide.
        vec2 g2 = floor(texCoord * dstTexel / 8.0);
        float checker2 = mod(g2.x + g2.y, 2.0);
        fragColor = vec4(checker, checker2, sceneD > 0.0 ? 1.0 : 0.0, 1.0);
        return;
    }

    if (Params.x > 0.5) {
        // Compare view: green = emitter visible, red = emitter occluded by the scene,
        // dark blue ramp = no emitter here (brightness follows scene depth as a sanity check
        // that the scene depth buffer is actually populated).
        if (bloomD <= 0.0) {
            fragColor = vec4(0.0, 0.0, 0.15 + 0.6 * sceneD, 1.0);
        } else if (visible > 0.5) {
            fragColor = vec4(0.0, 1.0, 0.0, 1.0);
        } else {
            fragColor = vec4(1.0, 0.0, 0.0, 1.0);
        }
        return;
    }

    fragColor = vec4(c * visible, 1.0);
}
