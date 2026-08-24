package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Lifts the vanilla cloud plane from Y=192 to Y=256 so it lines up with the
 * elevated Story-Mode cloud ceiling even in the rare configuration where the
 * vanilla renderer is still the one drawing clouds (storm off and the ambient
 * MCSM deck disabled).
 *
 * The vanilla cloud height is a hard-coded 192.0 (no dimension or data-driven
 * field exists for it in 26.2). Rather than pin an injection to one exact
 * method name - which would hard-crash at class-load if the shape of the
 * renderer ever drifts - this redirects the 192.0 constant wherever it appears
 * in CloudRenderer and, crucially, declares require = 0: if no constant
 * matches, the mixin silently does nothing and the game boots normally. The
 * deck itself always sits at 256+ regardless of this hook.
 */
@Mixin({CloudRenderer.class})
public abstract class VanillaCloudHeightMixin {
   @ModifyConstant(
      method = {"*"},
      constant = {@Constant(floatValue = 192.0F)},
      require = 0,
      expect = 0
   )
   private float dabyws$elevateVanillaClouds(float original) {
      return DevouringStormsClientConfig.elevateVanillaClouds && original == 192.0F ? 256.0F : original;
   }
}
