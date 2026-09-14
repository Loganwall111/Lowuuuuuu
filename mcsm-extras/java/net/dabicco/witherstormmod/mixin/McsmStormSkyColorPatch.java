package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.mcsm.extras.client.McsmNativeSkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the native SkyRenderer the only sky colour path.
 *
 * This intentionally does not cancel or replace the native sky pass.  It only
 * normalizes the state before that pass draws, so the whole spherical sky is
 * covered without a custom dome, card, or texture-pack sky layer.
 */
@Mixin(value = SkyRenderer.class, priority = 1200)
public abstract class McsmStormSkyColorPatch {
    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void mcsm$continuousNativeSky(ClientLevel level, float partialTick,
            Camera camera, SkyRenderState state, CallbackInfo ci) {
        McsmNativeSkyRenderer.apply(level, state);
    }

    /**
     * BUILD #403: the sunrise/sunset fan is no longer cancelled. It carries
     * the tinted horizon colour and is what makes vanilla's sky interpolate
     * continuously zenith -> horizon; deleting it left the flat-dome seam
     * the user reported as "that band at the top".
     */
    @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"),
            cancellable = true, require = 1)
    private void mcsm$removeSunriseBand(PoseStack poseStack, float angle,
            int color, CallbackInfo ci) {
        // intentionally a no-op now
    }
}
