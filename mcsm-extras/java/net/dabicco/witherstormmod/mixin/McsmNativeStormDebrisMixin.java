package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.mcsm.extras.client.McsmNativeStormDebris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces custom StormDebris cube submissions with native block particles. */
@Mixin(WitherStormRenderer.class)
public abstract class McsmNativeStormDebrisMixin {
    @Inject(method = "submit", at = @At("HEAD"), remap = false, require = 1)
    private void mcsm$nativeDebris(WitherStormRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        McsmNativeStormDebris.submit(state);
    }

    /** Keep the retired custom body cube mesh from being submitted by this call. */
    @Inject(method = "submit", at = @At("TAIL"), remap = false, require = 1)
    private void mcsm$clearDebrisState(WitherStormRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        // Native particles are emitted once at HEAD; the base StormDebris
        // entry points are cancelled by McsmStormDebrisMeshGuard.
    }
}
