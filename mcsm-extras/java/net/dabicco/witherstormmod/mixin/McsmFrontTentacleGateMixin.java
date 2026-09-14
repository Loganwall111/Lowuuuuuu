package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #375: the floating front tentacle is suppressed for Phase 2-3.
 *
 * It sprouts at Phase 3 (transitioning to the Phase 4 body) and was the
 * "just a tentacle floating" element in the broken in-game look. The
 * reference frames for Phase 2-3 show the clean command-block wither with
 * no front tentacle; the Phase 4+ tentacle system (submitTentacles5, which
 * grows from the Phase 4 body) is untouched.
 */
@Mixin(WitherStormRenderer.class)
public class McsmFrontTentacleGateMixin {

    // cancellable=true is REQUIRED: ci.cancel() throws CancellationException
    // on a non-cancellable inject (same bug class as the glowTint crash).
    @Inject(method = "submitFrontTentacle", at = @At("HEAD"), remap = false, require = 0, cancellable = true)
    private void mcsm$frontTentacleGate(WitherStormRenderState state, PoseStack poseStack,
                                        SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        if (state.phase >= 2.0D && state.phase < 4.0D) {
            ci.cancel();
        }
    }
}
