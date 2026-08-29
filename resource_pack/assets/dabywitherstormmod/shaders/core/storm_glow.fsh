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

// UNIFIED ARRAY REFERENCE — integrates native rendertype_clouds, rendertype_skybasic,
// rendertype_terrain, and rendertype_shield_halo shaders.
// All loose image registers discarded; mathematical noise + native core assets.
// See assets/minecraft/shaders/core/ for the native core shader array.

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

    // The brightest part of a real light washes out toward white; only the spill keeps the
    // tint. Kept SUBTLE -- pushed hard, every tooth turns into a white dot and the whole
    // mouth reads as one blown-out blob.
    vec3 tint = vertexColor.rgb * texture(Sampler0, texCoord0).rgb;
    vec3 rgb = mix(tint, vec3(1.0), core * 0.22);

    // ADDITIVE blending is (ONE, ONE) -- the alpha channel is ignored by the blender,
    // so the falloff has to be premultiplied into the colour itself.
    fragColor = vec4(rgb * intensity, intensity) * ColorModulator;
}
