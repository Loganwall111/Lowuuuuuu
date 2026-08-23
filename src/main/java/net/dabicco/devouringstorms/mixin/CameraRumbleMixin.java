package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.CaveRumbleClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({Camera.class})
public class CameraRumbleMixin {
   @ModifyVariable(
      method = {"setRotation(FF)V"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private float dabyws$rumbleYaw(float yRot) {
      float[] shake = CaveRumbleClient.offset(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
      return shake == null ? yRot : yRot + shake[0];
   }

   @ModifyVariable(
      method = {"setRotation(FF)V"},
      at = @At("HEAD"),
      ordinal = 1,
      argsOnly = true
   )
   private float dabyws$rumblePitch(float xRot) {
      float[] shake = CaveRumbleClient.offset(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
      return shake == null ? xRot : xRot + shake[1];
   }
}
