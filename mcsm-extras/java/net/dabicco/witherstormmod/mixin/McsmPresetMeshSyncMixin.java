package net.dabicco.witherstormmod.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * BUILD #371 — DUAL GEOMETRY PRESET BINDING.
 *
 * The clean 495-cube Blockbench/StageB body is bound STRICTLY to the
 * "Netflix / Custom Preset" config path:
 *
 *   - Netflix preset (4) applied  -> customMeshModel = true  (high-detail mesh)
 *   - MCSM OG / Legacy / Cinematic (1-3) applied -> customMeshModel = false
 *   - Custom preset (0) / manual panel toggle -> left alone (user's choice)
 *
 * Both entry points are hooked: the preset dropdown calls
 * {@code applyPreset(int, Collection)}; editing any preset-covered key calls
 * {@code refreshPreset()} which re-detects the active preset. The default
 * (Custom, effectsPreset 0) keeps the ORIGINAL base-mod geometry, which is
 * what fixes the summon-time vertex-overflow crash.
 */
@Mixin(DabyWSClientConfig.class)
public abstract class McsmPresetMeshSyncMixin {

    private static void dabyws$syncMesh(int preset) {
        if (preset >= 4) {
            if (!McsmExtrasConfig.customMeshModel) {
                McsmExtrasConfig.customMeshModel = true;
                McsmExtrasConfig.save();
            }
        } else if (preset >= 1) {
            if (McsmExtrasConfig.customMeshModel) {
                McsmExtrasConfig.customMeshModel = false;
                McsmExtrasConfig.save();
            }
        }
        // preset == 0 (Custom): respect the manual panel toggle — leave as-is.
    }

    @Inject(method = "applyPreset(ILjava/util/Collection;)V", at = @At("TAIL"), remap = false, require = 0)
    private static void dabyws$meshFromApply(int preset, Collection<String> tabKeys, CallbackInfo ci) {
        dabyws$syncMesh(preset);
    }

    @Inject(method = "refreshPreset()V", at = @At("TAIL"), remap = false, require = 0)
    private static void dabyws$meshFromRefresh(CallbackInfo ci) {
        dabyws$syncMesh((int) Math.round(DabyWSClientConfig.effectsPreset));
    }

    /**
     * Startup path: load() finishes by calling refreshPreset() (whose HEAD is
     * replaced by McsmPresetFixPatch, which may cancel the base body — so the
     * refreshPreset TAIL is not the reliable hook at load time). load() itself
     * is never cancelled, so its TAIL is the guaranteed place to sync the mesh
     * flag to the preset the user saved.
     */
    @Inject(method = "load()V", at = @At("TAIL"), remap = false, require = 0)
    private static void dabyws$meshFromLoad(CallbackInfo ci) {
        dabyws$syncMesh((int) Math.round(DabyWSClientConfig.effectsPreset));
    }
}
