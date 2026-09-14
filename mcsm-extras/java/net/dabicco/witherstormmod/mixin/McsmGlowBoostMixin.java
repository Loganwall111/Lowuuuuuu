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
 * user's eye colour + the shader glow gain; here it is lifted a further step
 * toward full brightness on the additive eyes channel, so the eyes and teeth
 * read as genuinely glowing instead of faintly lit. Flat +40 per channel:
 * a cyan (100,220,230) becomes (140,255,255) - noticeably brighter, never a
 * blown-out white blob.
 */
@Mixin(WitherStormHeadRenderer.class)
public class McsmGlowBoostMixin {

    private static final int LIFT = 40;

    // cancellable=true is REQUIRED: setReturnValue() internally cancels the
    // callback and throws CancellationException on a non-cancellable inject
    // (crashed the render thread, Build #381 fix).
    @Inject(method = "glowTint", at = @At("RETURN"), remap = false, require = 0, cancellable = true)
    private static void mcsm$glowBoost(CallbackInfoReturnable<Integer> cir) {
        int c = cir.getReturnValueI();
        int r = Math.min(255, (c >> 16 & 0xFF) + LIFT);
        int g = Math.min(255, (c >> 8 & 0xFF) + LIFT);
        int b = Math.min(255, (c & 0xFF) + LIFT);
        cir.setReturnValue(0xFF000000 | (r << 16) | (g << 8) | b);
    }
}
