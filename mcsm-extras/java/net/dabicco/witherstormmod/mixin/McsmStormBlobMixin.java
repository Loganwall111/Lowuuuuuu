package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.client.StormBackdrop;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.mcsm.extras.client.McsmEarlyStormBackdrop;
import net.mcsm.extras.client.McsmExperimentalStoryStage;
import net.mcsm.extras.client.McsmSkybox;
import net.mcsm.extras.client.McsmStormBlob;

/**
 * Devouring Storms: mega-phase 2 - hands the storm blob over to the
 * corrected re-submission (pinkish-violet 5.5-5.9, centred dark core,
 * smoothed skybox glide, weaker red ember). Client-only mixin on the base
 * mod's own StormBackdrop; the replacement draws nothing unless the game
 * client is up, and honours every base config toggle.
 */
@Mixin(StormBackdrop.class)
public abstract class McsmStormBlobMixin {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dabyws$correctedBlob(LevelRenderContext ctx, CallbackInfo ci) {
        if (Minecraft.getInstance() != null) {
            // Keep the optional stage hook and compatibility callback, but do
            // not cancel the base method. Restoring StormBackdrop.submit brings
            // back the smooth original phase backdrop instead of the retired
            // generated black rings/oval cards.
            // The native backdrop owns phases 3.9 through 9; this restrained
            // companion only fills the earlier phase-1 to phase-3 buildup.
            McsmEarlyStormBackdrop.submit(ctx);
            McsmExperimentalStoryStage.submit(ctx);
            McsmSkybox.submit(ctx);
            // Build #375: the REAL Telltale glare shells - the three actual
            // glare images layered as big slowly-swirling sky backdrops
            // around every storm (drawn over the sky sphere, under the
            // blob + halo so the body stays in front).
            net.mcsm.extras.client.McsmGlareBackdrop.submit(ctx);
            McsmStormBlob.submit(ctx);
            // Build #374: the restored working blue halo, welded to the
            // storm chassis from phase 4 through every later stage.
            net.mcsm.extras.client.McsmStormHalo.submit(ctx);
        }
        ci.cancel();
    }
}
