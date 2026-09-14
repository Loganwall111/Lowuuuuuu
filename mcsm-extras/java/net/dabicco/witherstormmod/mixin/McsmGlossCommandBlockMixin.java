package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.entity.model.WitherCommandBlock;
<<<<<<< HEAD
=======
import net.mcsm.extras.client.McsmGlossSheen;
>>>>>>> 44200e4 (ci: fix(model) repair phase 1 body geometry and wire face emissive glow maps)
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
<<<<<<< HEAD
 * Build #374: the gloss coat on the command-block stage (Phase 1) is REMOVED.
 *
 * The coat re-rendered the whole early-phase model under a sweeping Y-axis
 * rotation (the gloss U-offset driven up to a full 360° per cycle), so a
 * translucent ghost copy of the command block orbited the real one — the
 * "blocks floating apart in mid-air". Phase 1 now renders strictly the base
 * mod's native, perfectly aligned cube parameters, untouched.
 *
 * The phase 4+ storm body keeps its sheen (see McsmGlossP4Mixin), now
 * rendered exactly aligned with the base pass.
=======
 * Build #368: command-block stage body (early phases). Right after the base
 * model is drawn, re-render the same part as the native translucent gloss
 * sheen so the blocky body reads as shiny wet obsidian.
>>>>>>> 44200e4 (ci: fix(model) repair phase 1 body geometry and wire face emissive glow maps)
 */
@Mixin(WitherCommandBlock.class)
public abstract class McsmGlossCommandBlockMixin {

    @Inject(method = "renderToBuffer", at = @At("TAIL"), remap = false, require = 0)
<<<<<<< HEAD
    private void mcsm$glossCoatDisabled(PoseStack poseStack, VertexConsumer consumer,
                                        int light, int overlay, CallbackInfo ci) {
        // Intentionally empty: no sheen on the Phase 1 command block stage.
=======
    private void mcsm$glossCoat(PoseStack poseStack, VertexConsumer consumer,
                                int light, int overlay, CallbackInfo ci) {
        McsmGlossSheen.coatFor(this, poseStack, light, overlay);
>>>>>>> 44200e4 (ci: fix(model) repair phase 1 body geometry and wire face emissive glow maps)
    }
}
