package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.client.BowelsView;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public class BowelsEntityTiltMixin {
   @Inject(
      method = {"setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V"},
      at = {@At("HEAD")}
   )
   private void dabyws$bowelsTilt(LivingEntityRenderState state, PoseStack pose, float bodyRot, float scale, CallbackInfo ci) {
      Quaternionf frame = BowelsView.frame(state.x, state.y, state.z);
      if (frame != null) {
         pose.mulPose(frame);
      }
   }
}
