package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.SkyMatrices;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({GameRenderer.class})
public class GameRendererSkyMixin {
   /**
    * Capture the frame's projection matrix at the exact point vanilla hands
    * it to ProjectionMatrixBuffer inside renderLevel. The storm sky pass
    * needs the raw Matrix4f to compose its own view-projection UBO (vanilla
    * only keeps the uploaded GPU buffer around).
    */
   @ModifyArg(
      method = {"renderLevel"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
      )}
   )
   private Matrix4f dabyws$captureProjection(Matrix4f matrix) {
      SkyMatrices.setProjection(matrix);
      return matrix;
   }
}
