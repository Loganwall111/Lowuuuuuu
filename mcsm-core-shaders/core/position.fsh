#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:mcsm_visuals.glsl>

// ============================================================================
//  Devouring Storms - position.fsh   [BUILD #416]
//
//  The authored replacement for the `cp block.fsh position.fsh` fallback (see
//  position.vsh for why the copy existed and what this pass actually is).
//
//  Behaviour, in one line: block-safe shading that matches the block/terrain
//  programs, plus the Story Mode grade and the multi-phase fog, and -- only
//  when MCSM_SKY_POSITION is defined by the pipeline -- the same view-ray sky
//  gradient the sky pass uses. Default is the safe path, so no platform can
//  end up with its world geometry painted as sky.
// ============================================================================

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 mcsmWorldPos;

out vec4 fragColor;

#ifdef MCSM_SKY_POSITION
// The blueprint's component 2, available for a platform whose `position`
// program IS the sky pass. Colour is a function of the view ray, so there is
// no dome silhouette and no clipped top: the fade runs zenith -> horizon and
// then folds into the live fog colour, which removes the horizon seam.
const vec3 SKY_REF_TEAL[6] = vec3[](
    vec3(0.047, 0.071, 0.086), vec3(0.064, 0.096, 0.115), vec3(0.082, 0.121, 0.143),
    vec3(0.097, 0.143, 0.166), vec3(0.111, 0.162, 0.185), vec3(0.125, 0.180, 0.204)
);
const vec3 SKY_REF_PURPLE[6] = vec3[](
    vec3(0.086, 0.039, 0.129), vec3(0.143, 0.061, 0.200), vec3(0.199, 0.083, 0.271),
    vec3(0.253, 0.104, 0.336), vec3(0.303, 0.122, 0.395), vec3(0.353, 0.141, 0.455)
);
const vec3 SKY_REF_ROSE[6] = vec3[](
    vec3(0.114, 0.082, 0.098), vec3(0.172, 0.120, 0.145), vec3(0.230, 0.158, 0.192),
    vec3(0.285, 0.194, 0.238), vec3(0.339, 0.228, 0.284), vec3(0.392, 0.263, 0.329)
);

vec3 mcsm_position_column(const vec3[6] col, float t) {
    float u = clamp(t, 0.0, 1.0) * 5.0;
    int i = int(floor(u));
    float f = u - floor(u);
    vec3 a;
    vec3 b;
    if (i <= 0)      { a = col[0]; b = col[1]; }
    else if (i == 1) { a = col[1]; b = col[2]; }
    else if (i == 2) { a = col[2]; b = col[3]; }
    else if (i == 3) { a = col[3]; b = col[4]; }
    else             { a = col[4]; b = col[5]; }
    return mix(a, b, f);
}

vec3 mcsm_position_sky(vec3 ray) {
    float p = mcsm_witherstorm_phase();
    float t = 1.0 - ray.y;
    vec3 teal = mcsm_position_column(SKY_REF_TEAL, t);
    vec3 purple = mcsm_position_column(SKY_REF_PURPLE, t);
    vec3 rose = mcsm_position_column(SKY_REF_ROSE, t);
    vec3 col = mix(mix(teal, purple, mcsm_ramp(p, 5.1, 5.5)),
                   rose, mcsm_ramp(p, 5.75, 6.05));
    vec3 ember = vec3(0.550, 0.160, 0.030);
    col = mix(col, ember, mcsm_ramp(p, 7.0, 8.05));
    float seam = 1.0 - clamp(abs(ray.y) * 7.0, 0.0, 1.0);
    col = mix(col, FogColor.rgb, seam * 0.85 * FogColor.a);
    return mcsm_story_grade(col);
}
#endif

void main() {
#ifdef MCSM_SKY_POSITION
    // Camera-space ray: normalizing the interpolated world position against the
    // camera is what makes the fade independent of any geometry.
    fragColor = vec4(mcsm_position_sky(normalize(mcsmWorldPos)), 1.0);
#else
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }

    float p = mcsm_witherstorm_phase();
    if (mcsm_fog_active(p)) {
        vec3 fogRGB = mcsm_fog_color(p, FogColor.rgb);
        float fogv = clamp(total_fog_value(sphericalVertexDistance, cylindricalVertexDistance,
                                           FogEnvironmentalStart, FogEnvironmentalEnd,
                                           mcsm_rd_start(), FogRenderDistanceEnd)
                           * mcsm_fog_density(p), 0.0, 1.0);
        color.rgb = mcsm_story_grade(mix(color.rgb, fogRGB, fogv * FogColor.a));
        fragColor = color;
        return;
    }

    color.rgb = mcsm_story_grade(color.rgb);
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
                          FogEnvironmentalStart, FogEnvironmentalEnd,
                          FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#endif
}
