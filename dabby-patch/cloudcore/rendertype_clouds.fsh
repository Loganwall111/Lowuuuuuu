#version 330

/*
 * rendertype_clouds.fsh -- Minecraft Story Mode volumetric clouds
 * Target: Minecraft 26.2 core shader pipeline.
 *
 * Lights the extruded cloud geometry produced by rendertype_clouds.vsh:
 *
 *   - real-time moving shadows from Light0_Direction, so the shading swings
 *     round as the sun and moon travel across the sky
 *   - dense dark undersides and trailing edges, the Story Mode signature
 *   - highlights tinted by time of day: lavender dawn, warm pink dusk,
 *     deep storm blue-grey at night
 *   - hard alpha cutoff, so cloud edges stay pixel-crisp and blocky
 *
 * Light0_Direction comes from the Lighting uniform block in light.glsl; it is
 * the same directional light vanilla uses on entities, so cloud shading stays
 * consistent with everything else in the world.
 *
 * Place at: assets/minecraft/shaders/core/rendertype_clouds.fsh
 */

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:light.glsl>

in float vertexDistance;
in vec4 vertexColor;
in vec3 worldNormal;
in vec3 worldPos;
in float faceKind;

out vec4 fragColor;

// ------------------------------------------------------------------- tuning
const float AMBIENT      = 0.58;  // floor brightness -- clouds never go black
const float DIFFUSE      = 0.42;  // how hard the light sculpts the shape
const float UNDER_DARK   = 0.46;  // depth of the shadowed undersides
const float TRAIL_DARK   = 0.20;  // extra shade on trailing side walls
const float ALPHA_CUTOFF = 0.1;   // crisp edges, no soft fringe

// Story Mode highlight palette.
const vec3 TINT_DAY   = vec3(1.000, 0.988, 1.000);  // clean white, faint violet
const vec3 TINT_DAWN  = vec3(0.941, 0.800, 0.859);  // lavender-pink morning
const vec3 TINT_DUSK  = vec3(0.980, 0.761, 0.620);  // warm pink sunset
const vec3 TINT_NIGHT = vec3(0.298, 0.361, 0.678);  // deep storm blue-grey

void main() {
    vec4 color = vertexColor;

    // Crisp blocky silhouette: discard almost-transparent fragments outright
    // instead of letting them blend into a soft fringe.
    if (color.a < ALPHA_CUTOFF) {
        discard;
    }

    vec3 n = normalize(worldNormal);
    vec3 l = normalize(Light0_Direction);

    // -------------------------------------------------------------------
    // Directional term -- this is what makes the shadows move.
    //
    // Light0_Direction rotates with the sun, so faces turned away from it
    // darken as the day runs. The 0.65/0.35 wrap keeps unlit faces readable
    // rather than crushing them to pure silhouette.
    // -------------------------------------------------------------------
    float ndl = dot(n, l);
    float lambert = clamp(ndl * 0.65 + 0.35, 0.0, 1.0);
    float light = AMBIENT + DIFFUSE * lambert;

    // -------------------------------------------------------------------
    // Undersides and trailing edges.
    //
    // faceKind says which face we are on. The bottom deck takes a hard
    // multiplicative darkening -- that weight overhead is what sells the
    // clouds as thick slabs rather than paper.
    // -------------------------------------------------------------------
    if (faceKind < -0.5) {
        light *= (1.0 - UNDER_DARK);
    }

    // Side walls facing away from the light pick up extra shadow, giving each
    // cloud block a clear lit face and a clear dark face.
    if (abs(faceKind) < 0.5) {
        float trailing = clamp(-ndl, 0.0, 1.0);
        light *= (1.0 - TRAIL_DARK * trailing);
    }

    // -------------------------------------------------------------------
    // Time-of-day tint.
    //
    // Light0_Direction.y is the light's height, a clean proxy for time of day
    // with no extra uniforms: positive when the sun is up, negative at night,
    // near zero at the horizon. The tint is applied in proportion to how lit
    // the fragment is, so shadows stay neutral instead of turning muddy.
    // -------------------------------------------------------------------
    float sunHeight = l.y;
    float dayW   = clamp(sunHeight * 2.2, 0.0, 1.0);
    float nightW = clamp(-sunHeight * 2.2, 0.0, 1.0);
    float horizW = clamp(1.0 - abs(sunHeight) * 2.2, 0.0, 1.0);

    // Split the horizon band into dawn and dusk by which side the light is on.
    float dusk = step(0.0, l.x);
    vec3 horizonTint = mix(TINT_DAWN, TINT_DUSK, dusk);

    vec3 tint = TINT_DAY * dayW + horizonTint * horizW + TINT_NIGHT * nightW;
    tint /= max(dayW + horizW + nightW, 0.001);

    vec3 lit = color.rgb * light;
    lit = mix(lit, lit * tint, clamp(light, 0.0, 1.0));

    // Night clouds sit cooler and darker overall.
    lit = mix(lit, lit * vec3(0.72, 0.78, 1.0), nightW * 0.55);

    color.rgb = lit;

    // Vanilla cloud fade-out at the far plane, preserved exactly.
    color.a *= 1.0f - linear_fog_value(vertexDistance, 0, FogCloudsEnd);

    fragColor = color;
}
