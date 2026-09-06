#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

uniform float GameTime;

// ---------------------------------------------------------------------------
// Devouring Storms: Story Look -- terrain grading.
// Vanilla structure untouched (texture * lightmap-baked vertexColor *
// ColorModulator, then fog). Added: soft lavender shadow lift so shades
// never crush to black, a gentle saturation and S-curve contrast matched to
// the reference shots, and distance fog recoloured to the exact horizon
// haze of the current time of day.
// ---------------------------------------------------------------------------

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif
    vec3 c = color.rgb;

    // Soft shadows: lift dark values toward a cool lavender instead of black.
    float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
    float shade = 1.0 - smoothstep(0.0, 0.32, l);
    c = mix(c, c * vec3(0.94, 0.93, 1.07) + vec3(0.010, 0.008, 0.018), shade * 0.85);

    // Saturation and filmic-ish contrast from the reference grading.
    l = dot(c, vec3(0.2126, 0.7152, 0.0722));
    c = mix(vec3(l), c, 1.08);
    c = mix(c, c * c * (3.0 - 2.0 * c), 0.22);

    // Exact fog: blend the game's fog toward the sampled horizon haze of
    // the current time of day (world clock; hue key only as AMD fallback).
    vec3 F = FogColor.rgb;
    // Storm phases keep the mod's own fog colour untouched.
    bool storm = (F.r > F.g * 1.25 && F.b > F.g * 1.05)
              || (F.r > F.g * 1.25 && dot(F, vec3(0.2126, 0.7152, 0.0722)) < 0.18);
    float fnight = 0.0;
    float fdawn = 0.0;
    float fday = 1.0;
    if (GameTime > 0.0001) {
        float sunElev = cos((GameTime - 0.25) * 6.2831853);
        fday = smoothstep(-0.06, 0.28, sunElev);
        fnight = 1.0 - smoothstep(-0.30, -0.06, sunElev);
        fdawn = exp(-(sunElev * sunElev) / 0.0484) * (1.0 - fnight);
        fday = max(fday - fdawn, 0.0);
    } else {
        float fl = dot(F, vec3(0.2126, 0.7152, 0.0722));
        fnight = 1.0 - smoothstep(0.05, 0.22, fl);
        fdawn = clamp((F.r - F.b) * 2.2, 0.0, 1.0) * (1.0 - fnight);
        fday = (1.0 - fnight) * (1.0 - fdawn);
    }
    vec3 phor = fday * vec3(0.420, 0.790, 0.940)
              + fdawn * vec3(0.890, 0.680, 0.730)
              + fnight * vec3(0.019, 0.031, 0.130);
    vec4 fogCol = storm ? vec4(F, FogColor.a)
                        : vec4(mix(F, phor, 0.55), FogColor.a);

    fragColor = apply_fog(vec4(c, color.a), sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, fogCol);
}
