package net.dabicco.devouringstorms.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.devouringstorms.client.BowelsView;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntityRenderer.class})
public class BowelsItemTiltMixin {
   @Inject(
      method = {"submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"},
      at = {@At("HEAD")}
   )
   private void dabyws$bowelsItemTilt(ItemEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
      Quaternionf frame = BowelsView.frame(state.x, state.y, state.z);
      if (frame != null) {
         pose.mulPose(frame);
      }
   }
}
