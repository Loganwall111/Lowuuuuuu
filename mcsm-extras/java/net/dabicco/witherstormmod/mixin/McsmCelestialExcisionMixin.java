package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.mcsm.extras.client.McsmCoreEngineController;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The old custom celestial slab is retired. The native sun, moon, and stars
 * remain available; only the separate sunrise fan is suppressed by the
 * continuous-sky hook.
 */
@Mixin(value = SkyRenderer.class, priority = 1100)
public abstract class McsmCelestialExcisionMixin {
    @Inject(
            method = "renderSunMoonAndStars",
            at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void mcsm$removeNativeCelestials(PoseStack poseStack, float sunAngle,
            float moonAngle, float starAngle, MoonPhase moonPhase,
            float rainBrightness, float starBrightness, CallbackInfo ci) {
        // Native celestial geometry is authoritative in the OVERWORLD: the
        // former replacement slab is inert, so cancelling it there would remove
        // the regular sky instead of the unwanted orange band.
        //
        // BUILD #416 (D.8 glitch pass): the decayed reality is a different
        // matter. It has no sun, no moon and no stars to draw -- its dimension
        // type ships skybox "none", no skylight and a fixed time -- so leaving
        // the native bodies on paints a vanilla sunrise/sunset fan and a moon
        // into a sky that is supposed to be a torn violet void. The pass asks
        // the same owner that owns the sky colour, so the two can never disagree.
        try {
            if (net.mcsm.extras.McsmReality.inside(net.minecraft.client.Minecraft.getInstance().level)) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
            // never take the frame down for a celestial body
        }
    }
}
