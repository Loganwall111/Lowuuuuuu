package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.dabicco.witherstormmod.client.StormAtmosphere;
import net.dabicco.witherstormmod.client.StormBloom;
import net.dabicco.witherstormmod.client.StormImpactLights;
import net.dabicco.witherstormmod.client.StormShadow;
import net.dabicco.witherstormmod.client.StormSunGlow;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public abstract class LevelRendererBloomMixin {
   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void dabyws$bloomAtLevelEnd(
      GraphicsResourceAllocator allocator,
      DeltaTracker deltaTracker,
      boolean renderBlockOutline,
      CameraRenderState cameraState,
      Matrix4fc frustumMatrix,
      GpuBufferSlice fogBuffer,
      Vector4f fogColor,
      boolean skyVisible,
      CallbackInfo ci
   ) {
      StormSunGlow.render(cameraState);
      StormShadow.render(cameraState);
      StormImpactLights.render(cameraState);
      StormAtmosphere.process();
      StormBloom.process();
   }
}
