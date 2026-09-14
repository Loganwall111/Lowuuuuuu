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
        String suffix;
        if (phase >= 7.0D) {
            suffix = og ? "_og_p7" : "_p7";
        } else if (phase >= 6.0D) {
            suffix = og ? "_og_p6" : "_p6";
        } else if (phase >= 5.5D) {
            suffix = og ? "_og_p55" : "_p55";
        } else if (phase >= 4.0D) {
            suffix = og ? "_og" : "";
        } else {
            suffix = og ? "_og" : "";
        }
        return Identifier.fromNamespaceAndPath("dabywitherstormmod",
                "textures/entity/phase_4_assets" + suffix + ".png");
    }
}
