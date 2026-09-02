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

/**
 * Coloured lighting — the real thing, not a resource-pack hint.
 *
 * The `colorful_lighting` asset folder that ships in the mod is dead weight:
 * nothing depends on that mod, so `lights.json` is never read. This does the
 * job properly by tinting Minecraft's own lightmap.
 *
 * The lightmap is the texture that decides what colour every lit surface in the
 * world is. Three fields matter:
 *
 *   skyLightColor  — the colour of daylight. Tinting this is what makes
 *                    shadowed ground read cool blue at night and warm at dusk,
 *                    because shadowed faces receive sky light only.
 *   blockLightTint — the colour of torch/lamp light.
 *   ambientColor   — the flat floor colour everything sits on.
 *
 * Because we shift sky light rather than block light, torches stay warm while
 * the shade around them goes blue — which is exactly the look in the Story Mode
 * night reference, and it makes shadows visibly move and change colour as the
 * sun crosses the sky.
 */
@Mixin({LightmapRenderStateExtractor.class})
public class StoryModeLightmapMixin {

   @Inject(method = {"extract"}, at = {@At("TAIL")})
   private void dabyws$storyModeLight(LightmapRenderState state, float partialTick, CallbackInfo ci) {
      float amount = StoryModeSkyTint.lightStrength();
      if (amount <= 0.0F) {
         return;
      }
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return;
      }

      long t = mc.level.getOverworldClockTime();
      float[] light = new float[3];
      StoryModeSkyTint.lightColor(t, light);

      state.skyLightColor = tint(state.skyLightColor, light, amount * 0.75F);
      state.ambientColor = tint(state.ambientColor, light, amount * 0.35F);

      // Block light keeps its warmth; nudge it only slightly so torches still
      // read as fire rather than adopting the sky colour.
      float[] warm = new float[3];
      StoryModeSkyTint.blockLightColor(warm);
      state.blockLightTint = tint(state.blockLightTint, warm, amount * 0.45F);

      state.needsUpdate = true;
   }

   private static Vector3fc tint(Vector3fc base, float[] rgb, float amount) {
      if (base == null) {
         return new Vector3f(rgb[0], rgb[1], rgb[2]);
      }
      float keep = 1.0F - amount;
      return new Vector3f(
         base.x() * keep + rgb[0] * amount,
         base.y() * keep + rgb[1] * amount,
         base.z() * keep + rgb[2] * amount);
   }
}
