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
const vec3 SKY_REF_TEAL[32] = vec3[](
    vec3(0.047, 0.071, 0.086), vec3(0.050, 0.075, 0.091), vec3(0.053, 0.079, 0.095),
    vec3(0.055, 0.083, 0.100), vec3(0.058, 0.087, 0.104), vec3(0.061, 0.091, 0.109),
    vec3(0.064, 0.095, 0.114), vec3(0.067, 0.099, 0.118), vec3(0.069, 0.103, 0.123),
    vec3(0.072, 0.107, 0.127), vec3(0.075, 0.111, 0.132), vec3(0.078, 0.115, 0.136),
    vec3(0.080, 0.119, 0.141), vec3(0.083, 0.123, 0.145), vec3(0.086, 0.127, 0.150),
    vec3(0.089, 0.131, 0.155), vec3(0.091, 0.135, 0.158), vec3(0.094, 0.138, 0.161),
    vec3(0.096, 0.141, 0.164), vec3(0.098, 0.144, 0.167), vec3(0.100, 0.147, 0.171),
    vec3(0.103, 0.150, 0.174), vec3(0.105, 0.153, 0.177), vec3(0.107, 0.156, 0.180),
    vec3(0.110, 0.159, 0.183), vec3(0.112, 0.162, 0.186), vec3(0.114, 0.165, 0.189),
    vec3(0.116, 0.168, 0.192), vec3(0.119, 0.171, 0.195), vec3(0.121, 0.174, 0.198),
    vec3(0.123, 0.177, 0.201), vec3(0.125, 0.180, 0.204)
);
const vec3 SKY_REF_PURPLE[32] = vec3[](
    vec3(0.086, 0.039, 0.129), vec3(0.095, 0.043, 0.141), vec3(0.104, 0.046, 0.152),
    vec3(0.114, 0.050, 0.164), vec3(0.123, 0.053, 0.175), vec3(0.132, 0.057, 0.186),
    vec3(0.141, 0.060, 0.198), vec3(0.150, 0.064, 0.209), vec3(0.159, 0.068, 0.220),
    vec3(0.168, 0.071, 0.232), vec3(0.177, 0.075, 0.243), vec3(0.186, 0.078, 0.255),
    vec3(0.196, 0.082, 0.266), vec3(0.205, 0.085, 0.277), vec3(0.214, 0.089, 0.289),
    vec3(0.223, 0.092, 0.300), vec3(0.231, 0.096, 0.311), vec3(0.240, 0.099, 0.320),
    vec3(0.248, 0.102, 0.330), vec3(0.256, 0.105, 0.340), vec3(0.264, 0.108, 0.349),
    vec3(0.272, 0.111, 0.359), vec3(0.280, 0.114, 0.368), vec3(0.288, 0.117, 0.378),
    vec3(0.296, 0.120, 0.388), vec3(0.304, 0.123, 0.397), vec3(0.312, 0.126, 0.407),
    vec3(0.321, 0.129, 0.416), vec3(0.329, 0.132, 0.426), vec3(0.337, 0.135, 0.436),
    vec3(0.345, 0.138, 0.445), vec3(0.353, 0.141, 0.455)
);
const vec3 SKY_REF_ROSE[32] = vec3[](
    vec3(0.114, 0.082, 0.098), vec3(0.123, 0.088, 0.106), vec3(0.132, 0.094, 0.113),
    vec3(0.142, 0.101, 0.121), vec3(0.151, 0.107, 0.128), vec3(0.161, 0.113, 0.136),
    vec3(0.170, 0.119, 0.144), vec3(0.179, 0.125, 0.151), vec3(0.189, 0.131, 0.159),
    vec3(0.198, 0.137, 0.166), vec3(0.207, 0.143, 0.174), vec3(0.217, 0.149, 0.182),
    vec3(0.226, 0.155, 0.189), vec3(0.235, 0.161, 0.197), vec3(0.245, 0.167, 0.204),
    vec3(0.254, 0.173, 0.212), vec3(0.263, 0.179, 0.219), vec3(0.272, 0.185, 0.227),
    vec3(0.280, 0.190, 0.234), vec3(0.289, 0.196, 0.241), vec3(0.298, 0.202, 0.249),
    vec3(0.306, 0.207, 0.256), vec3(0.315, 0.213, 0.263), vec3(0.323, 0.218, 0.271),
    vec3(0.332, 0.224, 0.278), vec3(0.341, 0.229, 0.285), vec3(0.349, 0.235, 0.293),
    vec3(0.358, 0.240, 0.300), vec3(0.366, 0.246, 0.307), vec3(0.375, 0.252, 0.315),
    vec3(0.384, 0.257, 0.322), vec3(0.392, 0.263, 0.329)
);

// BUILD #476 -- the sheet columns are 32 rows here too (palette_tables.STOPS), so
// this program and the sky program read the same sheet the same way.
vec3 mcsm_position_column32(const vec3[32] col, float t) {
    float u = clamp(t, 0.0, 1.0) * 31.0;
    int i = int(floor(u));
    float f = u - floor(u);
    if (i >= 31) {
        return col[31];
    }
    if (i < 0) {
        return col[0];
    }
    return mix(col[i], col[i + 1], f);
}

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
    vec3 teal = mcsm_position_column32(SKY_REF_TEAL, t);
    vec3 purple = mcsm_position_column32(SKY_REF_PURPLE, t);
    vec3 rose = mcsm_position_column32(SKY_REF_ROSE, t);
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
