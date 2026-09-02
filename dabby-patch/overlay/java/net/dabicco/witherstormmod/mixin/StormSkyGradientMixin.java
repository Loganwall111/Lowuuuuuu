package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.client.StormSkyGradient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paints the storm's gradient onto the sky by running vanilla's own
 * sunrise/sunset pass a second time, aimed at the Wither Storm.
 *
 * The user described what this needed to be better than I had: a second sky
 * that follows the storm and blends into the vanilla sky, so a huge
 * black-and-purple gradient sits behind the creature and moves with it -- not
 * a skybox that swings around the camera, and not an object that can clip.
 *
 * Vanilla's sunrise is exactly that mechanism. It is a radial fan drawn on the
 * inside of the sky dome, rotated to the sun's compass bearing, alpha-fading to
 * nothing at its edges. That is why a sunset glows on one horizon and melts
 * seamlessly into blue everywhere else.
 *
 * So we simply call {@code renderSunriseAndSunset} again with the storm's
 * bearing and colour. Reusing the vanilla pass means:
 *   - it draws at sky depth, so terrain, clouds and the storm sit in front
 *   - it blends instead of cutting, because the fan's rim is transparent
 *   - there is no geometry in the world, so nothing can clip through it
 *
 * Injected immediately after the vanilla sunrise call inside the sky pass, so
 * the storm gradient layers on top of the normal sky rather than replacing it.
 */
@Mixin(LevelRenderer.class)
public abstract class StormSkyGradientMixin {

   @Shadow
   private SkyRenderer skyRenderer;

   @Inject(
      method = {"lambda$addSkyPass$0"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;FI)V",
         shift = At.Shift.AFTER
      ),
      require = 1
   )
   private void dabyws$stormSkyGradient(GpuBufferSlice buffer, SkyRenderState state, CallbackInfo ci) {
      if (!StormSkyGradient.active()) {
         return;
      }
      int color = StormSkyGradient.color();
      if ((color >>> 24) < 3) {
         return;                    // fully transparent, skip the draw
      }
      /* renderSunriseAndSunset takes a *sun angle* in radians and derives the
       * bearing from it, so convert the storm's compass bearing back into the
       * same space. The method also flips 180 degrees when sin(angle) < 0,
       * which we compensate for by keeping the angle in the positive half. */
      float radians = (float)Math.toRadians(StormSkyGradient.yaw());
      PoseStack pose = new PoseStack();
      this.skyRenderer.renderSunriseAndSunset(pose, radians, color);
   }
}
