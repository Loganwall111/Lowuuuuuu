package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.client.StormSkins;
import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #369 repair: locks the storm body's texture to the entity's ACTUAL
 * phase. The base renderer resolves skins through StormSkins' shared
 * volatile phase hint, which other code paths (off-frame ticks that stamp the
 * max phase of every tracked storm, detached-head passes, first frame) can
 * leave stale or polluted — so the Phase 6 cosmic atlas could silently never
 * bind and the body kept rendering the old canonical skin.
 *
 * Two hooks:
 *  1. HEAD of submit: stamp this storm's real phase for the whole frame, so
 *     every StormSkins call in the frame (body, growth pieces, tentacles,
 *     devourer, mini heads) resolves the correct skin.
 *  2. getTextureLocation: re-resolve the main body texture directly from
 *     state.phase / state.devourer — no volatile involved.
 *
 * Client-only, cosmetic. remap=false require=0 per house style.
 */
@Mixin(WitherStormRenderer.class)
public abstract class McsmPhaseTextureLockMixin {

    @Inject(
        method = "submit(Lnet/dabicco/witherstormmod/entity/state/WitherStormRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void mcsm$phaseStamp(WitherStormRenderState state, PoseStack poseStack,
                                 SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        StormSkins.body(state.phase);
    }

    @ModifyReturnValue(
        method = "getTextureLocation(Lnet/dabicco/witherstormmod/entity/state/WitherStormRenderState;)Lnet/minecraft/resources/Identifier;",
        at = @At("RETURN"),
        remap = false,
        require = 0
    )
    private Identifier mcsm$phaseAccurateBody(Identifier original, WitherStormRenderState state) {
        return state.devourer ? StormSkins.devourer(state.phase) : StormSkins.body(state.phase);
    }
}
