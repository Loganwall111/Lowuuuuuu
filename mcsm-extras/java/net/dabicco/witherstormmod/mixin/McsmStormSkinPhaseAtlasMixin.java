package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormSkins;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BUILD #399 -- per-phase TRACED-SHADING body atlases.
 *
 * The reference frames (user shots 2026-09-14 065011 / 2775803 for phases
 * 4-5.9, gigantic-wither-storms close-up for phase 6) show the storm body in
 * traced charcoal shading that shifts subtly per phase band. The base
 * StormSkins binds ONE atlas (phase_4_assets_p6) to every Phase 1+ piece, so
 * early phases wore the phase-6 material. This mixin re-routes the selectors
 * onto the traced family that ships in jar-overrides:
 *
 *   phase 4.0-5.4   phase_4_assets[_og].png        teal-black traced charcoal
 *   phase 5.5-5.9   phase_4_assets[_og]_p55.png    darker traced charcoal
 *   phase 6.0-6.9   phase_4_assets[_og]_p6.png     blue-black traced (split)
 *   phase 7+        phase_4_assets[_og]_p7.png
 *
 * The OG/custom profile keeps its _og variants exactly as traced (carried
 * acceptance criterion); the default profile uses the same traced art without
 * the _og suffix. Emissive/teeth atlases are untouched.
 */
@Mixin(StormSkins.class)
public abstract class McsmStormSkinPhaseAtlasMixin {

    @Inject(method = "body", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mcsm$tracedBodyPerPhase(double phase, CallbackInfoReturnable<Identifier> cir) {
        Identifier id = mcsm$tracedFor(phase);
        if (id != null) cir.setReturnValue(id);
    }

    @Inject(method = {"phase6Body", "phase4", "legacy"}, at = @At("HEAD"), cancellable = true, remap = false)
    private static void mcsm$tracedBodyFromHint(CallbackInfoReturnable<Identifier> cir) {
        Identifier id = mcsm$tracedFor(StormSkins.phaseHint());
        if (id != null) cir.setReturnValue(id);
    }

    private static Identifier mcsm$tracedFor(double phase) {
        if (phase < 1.0D) return null; // Phase 0 starter atlas stays native
        boolean og = DabyWSClientConfig.stormSkin >= 0.5D;
        int band = mcsm$band(phase);
        String suffix;
        switch (band) {
            case 3:  suffix = og ? "_og_p7" : "_p7"; break;
            case 2:  suffix = og ? "_og_p6" : "_p6"; break;
            case 1:  suffix = og ? "_og_p55" : "_p55"; break;
            default: suffix = og ? "_og" : ""; break;
        }
        return Identifier.fromNamespaceAndPath("dabywitherstormmod",
                "textures/entity/phase_4_assets" + suffix + ".png");
    }

    /** Raw band for a phase: 0 = 4-5.4, 1 = 5.5-5.9, 2 = 6-6.9, 3 = 7+. */
    private static int mcsm$raw(double p) {
        return p >= 7.0D ? 3 : p >= 6.0D ? 2 : p >= 5.5D ? 1 : 0;
    }

    private static final double[] BAND_LO = {4.0D, 5.5D, 6.0D, 7.0D};
    private static final double[] BAND_HI = {5.5D, 6.0D, 7.0D, 99.0D};
    private static volatile int mcsm$lastBand = -1;

    /**
     * BUILD #400 -- hysteresis: the phase hint jitters by a few hundredths as
     * the storm moves, which made the body atlas flip-flop across a band
     * boundary ("colours change every time it moves"). A band only changes
     * once the phase is 0.15 past the boundary, so motion never pops the
     * material; genuine phase progressions still cross within one tick.
     */
    private static int mcsm$band(double p) {
        int last = mcsm$lastBand;
        int band;
        if (last < 0) {
            band = mcsm$raw(p);
        } else if (p >= BAND_HI[last] + 0.15D || p < BAND_LO[last] - 0.15D) {
            band = mcsm$raw(p);
        } else {
            band = last;
        }
        mcsm$lastBand = band;
        return band;
    }
}
