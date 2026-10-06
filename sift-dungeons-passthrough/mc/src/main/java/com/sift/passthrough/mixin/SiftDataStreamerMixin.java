package com.sift.passthrough.mixin;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.sift.passthrough.SiftDataStreamer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Hooks the end of every world render frame to stream real-time SIFT Camera
 * X, Y, Z, Pitch, Yaw, FOV, and DeltaTime to Port 8080, while checking the
 * Escape-key (GLFW_KEY_ESCAPE = 256) kill switch.
 */
@Mixin(LevelRenderer.class)
public abstract class SiftDataStreamerMixin {

    @Inject(method = "render", at = @At("RETURN"), remap = false, require = 0)
    private void sift$onLevelRenderReturn(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            CameraRenderState camera,
            Matrix4fc modelViewMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f skyColor,
            boolean skyVisible,
            CallbackInfo ci
    ) {
        SiftDataStreamer.onFrame(camera, skyVisible);
    }
}
