package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StoryModeSkyTint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LightmapRenderStateExtractor.class})
public class StoryModeLightmapMixin {
   @Inject(
      method = {"extract"},
      at = {@At("TAIL")}
   )
   private void dabyws$storyModeLight(LightmapRenderState state, float partialTick, CallbackInfo ci) {
      float amount = StoryModeSkyTint.lightStrength();
      if (!(amount <= 0.0F)) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            long t = mc.level.getOverworldClockTime();
            float[] light = new float[3];
            StoryModeSkyTint.lightColor(t, light);
            state.skyLightColor = tint(state.skyLightColor, light, amount * 0.75F);
            state.ambientColor = tint(state.ambientColor, light, amount * 0.35F);
            float[] warm = new float[3];
            StoryModeSkyTint.blockLightColor(warm);
            state.blockLightTint = tint(state.blockLightTint, warm, amount * 0.45F);
            state.needsUpdate = true;
         }
      }
   }

   private static Vector3fc tint(Vector3fc base, float[] rgb, float amount) {
      if (base == null) {
         return new Vector3f(rgb[0], rgb[1], rgb[2]);
      } else {
         float keep = 1.0F - amount;
         return new Vector3f(base.x() * keep + rgb[0] * amount, base.y() * keep + rgb[1] * amount, base.z() * keep + rgb[2] * amount);
      }
   }
}
