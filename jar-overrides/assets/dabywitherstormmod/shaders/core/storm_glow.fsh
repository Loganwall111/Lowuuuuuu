#version 330

#moj_import <minecraft:dynamictransforms.glsl>

// -----------------------------------------------------------------------------------
// STORM GLOW -- the light around the Wither Storm's teeth.
//
// Each glow is ONE camera-facing quad; this shader turns it into a round pool of light
// whose brightness falls off smoothly from the centre. Doing the falloff per PIXEL is
// the whole point: the old glow was a 20-segment triangle fan with vertex alpha, so the
// gradient was linear and polygonal -- that is what read as a flat 2D decal stuck on
// the head. A gaussian body + a tight hot core + a white-hot centre is what real
// over-exposed light looks like.
//
// Paired with the ENTITY_EMISSIVE snippet (Sampler0 only, EMISSIVE) so it runs on the
// same vertex shader as everything else: core/fogless_entity with NO_OVERLAY +
// NO_CARDINAL_LIGHTING. The shader must never declare a uniform the pipeline's bind
// group layout lacks -- that crashes on Vulkan even where OpenGL shrugs.
//
// Deliberately UNFOGGED: this is emitted light, so it punches through the murk instead
// of being washed out by it (same call as the storm's night glow). Fog can't simply be
// mixed in here anyway -- the blend is ADDITIVE (ONE, ONE), so mixing toward the fog
// colour would add a bright square across the whole quad.
// -----------------------------------------------------------------------------------

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    // UV0 spans the whole glow quad, so this is just the offset from its centre.
    vec2 p = texCoord0 * 2.0 - 1.0;
    float d2 = dot(p, p);
    if (d2 >= 1.0) {
        discard; // outside the disc: keeps the quad's corners from showing
    }
    float d = sqrt(d2);
    float edge = 1.0 - d; // 1 at the centre, 0 exactly on the rim

    // Soft body (gaussian, forced to zero at the rim so there is never a visible circle)
    // plus a gentle core. Kept SOFT on purpose: a tight, steep core turns each quad into a
    // hard little bead, and a row of those reads as beads rather than as one light.
    float halo = exp(-d2 * 2.0) * edge;
    float core = edge * edge;

    float intensity = clamp(halo * 0.90 + core * 0.35, 0.0, 1.0) * vertexColor.a;
    if (intensity <= 0.003) {
        discard;
    }

    // MCSM 1.9.96 -- SILHOUETTE GOES BLUE (user: "I would like that silhouette glow
    // to be blue"). The Java side feeds warm/purple vertex colours; the palette now
    // lives HERE so every caller goes blue at once. Luminance-preserving re-hue:
    // the pool keeps exactly the brightness it had, only the chroma moves.
    vec3 tint = vertexColor.rgb * texture(Sampler0, texCoord0).rgb;
    float mcsmLum = max(dot(tint, vec3(0.2126, 0.7152, 0.0722)), 0.0005);
    const vec3 MCSM_BLUE = vec3(0.20, 0.45, 0.95);
    tint = MCSM_BLUE * (mcsmLum / dot(MCSM_BLUE, vec3(0.2126, 0.7152, 0.0722)));
    // The brightest part still washes toward white but ONLY barely (0.22 -> 0.08):
    // the old 0.22 white core is what covered the turquoise teeth marks with a white
    // glow -- the user asked to "take off the white glowing teeth, leave the teeth
    // white, and put a turquoise glow on top". The turquoise mark textures draw
    // through the nearly-unwashed centre now; this pool supplies the blue halo.
    vec3 rgb = mix(tint, vec3(1.0), core * 0.08);

    // ADDITIVE blending is (ONE, ONE) -- the alpha channel is ignored by the blender,
    // so the falloff has to be premultiplied into the colour itself.
    fragColor = vec4(rgb * intensity, intensity) * ColorModulator;
}
