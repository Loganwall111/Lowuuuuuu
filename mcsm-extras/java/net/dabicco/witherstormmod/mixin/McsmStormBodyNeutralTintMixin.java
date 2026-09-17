package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Body palette guard: phase atmosphere and emissive colours must never leak
 * into the opaque storm vertex multiplier.
 */
@Mixin(WitherStormRenderer.class)
public abstract class McsmStormBodyNeutralTintMixin {
    @Shadow
    private boolean previewShadowPass;

    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void mcsm$neutralStormBodyTint(WitherStormRenderState state,
                                            CallbackInfoReturnable<Integer> cir) {
        if (!this.previewShadowPass) {
            // BUILD #438 -- THE BODY WEARS THE PHASE'S OWN COLOUR.
            //
            // #404 set a dark NEUTRAL vertex multiplier (0x948F8A: r, g and b
            // within seven of each other) so the traced charcoal would stay
            // near-black under noon sun. It did that -- and it is also why the
            // whole body reads BLACK AND WHITE at every phase: a neutral
            // multiplier cannot carry a hue, so the pink of 6, the amethyst of
            // 5.5 and the teal of 5 all arrive as the same grey. The show's body
            // is charcoal, but it is never colourless: its shadows carry the
            // sky's own colour.
            //
            // So the multiplier keeps #404's brightness and picks up the phase's
            // hue from the same reference columns the sky, the halo, the field
            // and both backdrops read -- at a strength the config can turn down
            // to the old neutral. Below the storm's own onset there is no
            // reference band yet, so the neutral still stands there.
            int tint = NEUTRAL;
            double phase = state.phase;
            if (net.mcsm.extras.McsmExtrasConfig.phaseTintedBody
                    && phase >= net.mcsm.extras.client.McsmStormPhase.PHASE_MIN) {
                float[] c = net.mcsm.extras.client.McsmStormPhase.columnFor((float) phase, 1.0F);
                float max = Math.max(c[0], Math.max(c[1], c[2]));
                if (max > 1.0e-4F) {
                    float gain = 0.580F / max;          // same weight as #404's neutral
                    float mix = (float) net.mcsm.extras.McsmExtrasConfig.bodyPhaseTint;
                    int r = channel(NEUTRAL, 16, c[0] * gain, mix);
                    int g = channel(NEUTRAL, 8, c[1] * gain, mix);
                    int b = channel(NEUTRAL, 0, c[2] * gain, mix);
                    tint = 0xFF000000 | (r << 16) | (g << 8) | b;
                }
            }
            cir.setReturnValue(tint);
        }
    }

    /** #404's neutral, kept as the fallback and as the mix target. */
    private static final int NEUTRAL = 0x948F8A;

    /** One channel: the neutral eased toward the phase colour's channel. */
    private static int channel(int neutral, int shift, float value, float amount) {
        int base = (neutral >> shift) & 0xFF;
        int want = Math.max(0, Math.min(255, Math.round(value * 255.0F)));
        return Math.max(0, Math.min(255, Math.round(base + (want - base) * amount)));
    }
}
