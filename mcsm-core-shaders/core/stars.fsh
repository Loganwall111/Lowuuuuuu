#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:mcsm_visuals.glsl>

// ============================================================================
//  MCSM visuals - stars.fsh  (bonus: keeps the sky state machine coherent --
//  stars must drown in the 5.0/5.1 teal murk and burn orange at 6.0;
//  no companion .vsh change needed, stars.fsh takes no varyings in vanilla)
// ============================================================================

out vec4 fragColor;

void main() {
    float mcsmP = mcsm_phase(FogSkyEnd, FogColor, FogRenderDistanceEnd);
    fragColor = vec4(ColorModulator.rgb * mcsm_star_tint(mcsmP), ColorModulator.a);
}
