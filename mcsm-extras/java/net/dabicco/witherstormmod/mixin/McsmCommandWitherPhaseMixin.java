package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.entity.model.WitherCommandBlock;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #375: Phase 2-3 is the authentic command-block wither.
 *
 * The base setupAnim strips the blocky wither at phase >= 2 (hides the main
 * face head, skips the body plate, keeps only the pillar core and the two
 * side heads). Layered on top, the base also draws the hunchback monster and
 * a floating front tentacle - which is exactly what showed in-game as "just
 * a tentacle floating and this monster". The reference frames show the FULL
 * blocky three-headed wither on its platform through Phase 2-3, so force the
 * Phase 1 part configuration for 2.0 <= phase < 4.0. (The hunchback body and
 * the front tentacle are suppressed by McsmHunchbackGateMixin and
 * McsmFrontTentacleGateMixin; the face glow is extended to this window by
 * McsmFaceGlowMixin.)
 */
@Mixin(WitherCommandBlock.class)
public abstract class McsmCommandWitherPhaseMixin {

    @Shadow(remap = false) private ModelPart upperBodyPart1;
    @Shadow(remap = false) private ModelPart upperBodyPart2;
    @Shadow(remap = false) private ModelPart head1;

    @Inject(method = "setupAnim", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$commandWitherPhase23(WitherStormRenderState state, CallbackInfo ci) {
        if (state.phase >= 2.0D && state.phase < 4.0D) {
            head1.visible = true;
            upperBodyPart1.skipDraw = false;
            upperBodyPart2.visible = true;
        }
    }
}
