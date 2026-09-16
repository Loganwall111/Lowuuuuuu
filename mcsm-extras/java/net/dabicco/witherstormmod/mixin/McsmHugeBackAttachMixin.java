package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.dabicco.witherstormmod.client.CubeReveal;
import net.dabicco.witherstormmod.entity.model.HugeAssBackModel;
import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.client.McsmHugeBackCentre;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BUILD #423 -- THE PHASE-5.5 UPPER BACK IS PUT BACK ON THE BODY.
 *
 * THE REPORT. "I also noticed the big problem phase 5.5 has an upper back
 * disattached from the main body."
 *
 * THE MEASUREMENT (see ci/measure_hugeback.py, which reads the very model source
 * this class drives): the huge back -- the geometry that exists only in the
 * 5.4-5.8 window, i.e. exactly where the user is looking -- is authored with its
 * mass centred about 11.7 blocks BELOW its own model origin (bounds minY -20.9,
 * maxY -2.6 model blocks). The renderer then enlarges it by scaling 1.72x about
 * that origin. Scaling a body whose centre sits 11.7 blocks from the pivot by
 * 1.72 displaces its centre by 0.72 * 11.7 = 8.4 model blocks, and the storm's
 * own -4 body scale (with its y flip) turns that into roughly 34 world blocks --
 * the huge back is lifted a third of a storm clear of the body it belongs to.
 * Nothing about the model is wrong; the pivot is.
 *
 * THE FIX. This mixin measures the huge back exactly the way the renderer's own
 * submitScaled() measures every model it scales (CubeReveal.bounds of the model
 * root, cached) and publishes that centre for the duration of submitGrowth5.
 * McsmHugeBackPoseMixin sees the 1.72x scale go past while the centre is
 * published and adds the single translate that turns "scale about the origin"
 * into "scale about the model's own centre" -- so the back grows where it
 * already is. The body, the mirror copy drawn under the same transform, and the
 * shadow capture all stay welded to one another because they all inherit the
 * same corrected transform.
 *
 * require = 0 on both injections, and the whole thing is behind
 * McsmExtrasConfig.hugeBackCentred (default on): if this build's renderer does
 * not look exactly like the decompilation this was written against, nothing is
 * injected and the huge back draws precisely as it does today.
 */
@Mixin(WitherStormRenderer.class)
public abstract class McsmHugeBackAttachMixin {

    /** The huge back, and the mirror copy drawn under the same transform. */
    @Shadow
    private HugeAssBackModel hugeAssBackModel;

    @Inject(method = "submitGrowth5", at = @At("HEAD"), remap = false, require = 0)
    private void mcsm$publishHugeBackCentre(WitherStormRenderState state, PoseStack poseStack,
                                            SubmitNodeCollector collector, CallbackInfo ci) {
        try {
            if (!McsmExtrasConfig.hugeBackCentred) {
                McsmHugeBackCentre.leave();
                return;
            }
            McsmHugeBackCentre.inside(CubeReveal.bounds(this.hugeAssBackModel.root()));
        } catch (Throwable ignored) {
            // Anything unexpected here means we simply do not correct the pivot.
            McsmHugeBackCentre.leave();
        }
    }

    @Inject(method = "submitGrowth5", at = @At("RETURN"), remap = false, require = 0)
    private void mcsm$retractHugeBackCentre(WitherStormRenderState state, PoseStack poseStack,
                                            SubmitNodeCollector collector, CallbackInfo ci) {
        McsmHugeBackCentre.leave();
    }
}
