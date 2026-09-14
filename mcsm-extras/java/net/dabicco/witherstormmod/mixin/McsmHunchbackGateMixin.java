package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.entity.model.HunchbackGrowth;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #375: the hunchback monster is suppressed for Phase 2-3.
 *
 * The base design layers the growing hunchback body over the command-block
 * wither from phase 2 up - the user's reference frames show the clean
 * blocky wither instead. Hiding every part of the model (instead of only
 * the draw call) also kills the base face-glow pass, which re-submits this
 * same model on the emissive channel: no glowing hunchback silhouette can
 * survive in Phase 2-3. Phase 1 keeps the hunchback buildup untouched.
 */
@Mixin(HunchbackGrowth.class)
public abstract class McsmHunchbackGateMixin {

    @Shadow(remap = false) private ModelPart root;

    @Inject(method = "setupAnim", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$hunchbackGate(WitherStormRenderState state, CallbackInfo ci) {
        if (state.phase >= 2.0D && state.phase < 4.0D) {
            for (ModelPart part : root.getAllParts()) {
                part.visible = false;
            }
        }
    }
}
