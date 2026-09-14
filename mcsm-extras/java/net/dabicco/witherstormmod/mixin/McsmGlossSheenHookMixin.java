package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.mcsm.extras.client.McsmGlossSheen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #368: frames the Wither Storm's submit pass so the native Tattletale
 * gloss sheen (McsmGlossSheen) knows when a storm body is being rendered and
 * can re-submit its translucent scrolling coat. Client-only, cosmetic.
 */
@Mixin(WitherStormRenderer.class)
public abstract class McsmGlossSheenHookMixin {

    @Inject(
        method = "submit(Lnet/dabicco/witherstormmod/entity/state/WitherStormRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void mcsm$glossBegin(WitherStormRenderState state, PoseStack poseStack,
                                 SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        // Build #385: gloss sheen pass removed (animated PBR glint replaces it).
        return;
    }

    @Inject(
        method = "submit(Lnet/dabicco/witherstormmod/entity/state/WitherStormRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("TAIL"),
        remap = false,
        require = 0
    )
    private void mcsm$glossEnd(WitherStormRenderState state, PoseStack poseStack,
                               SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        // Build #385: gloss sheen pass removed.
        return;
    }
}
