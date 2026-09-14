package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.entity.renderer.WitherStormHeadRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Build #375: the eye/teeth glow is too weak.
 *
 * The reference close-up (bright white-cyan tooth ring + cyan eyes) shows
 * the emissive channel at full blast. The base glow tint already applies the
 * user's eye colour + the shader glow gain; Build #384 (user directive)
 * multiplies the pixel light output for the eyes and teeth by 3.0x so they
 * emit a true, brilliant glow in the dark instead of rendering dim.
 * Channels clamp at 255 so the additive eyes channel never blows out.
 */
@Mixin(WitherStormHeadRenderer.class)
public class McsmGlowBoostMixin {

    private static final double GAIN = 3.0D;

    // cancellable=true is REQUIRED: setReturnValue() internally cancels the
    // callback and throws CancellationException on a non-cancellable inject
    // (crashed the render thread, Build #381 fix).
    @Inject(method = "glowTint", at = @At("RETURN"), remap = false, require = 0, cancellable = true)
    private static void mcsm$glowBoost(CallbackInfoReturnable<Integer> cir) {
        int c = cir.getReturnValueI();
        int r = Math.min(255, (int) ((c >> 16 & 0xFF) * GAIN));
        int g = Math.min(255, (int) ((c >> 8 & 0xFF) * GAIN));
        int b = Math.min(255, (int) ((c & 0xFF) * GAIN));
        cir.setReturnValue(0xFF000000 | (r << 16) | (g << 8) | b);
    }
}
