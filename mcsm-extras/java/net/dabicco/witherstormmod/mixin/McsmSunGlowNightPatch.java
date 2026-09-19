package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.client.StormSunGlow;
import net.mcsm.extras.McsmReality;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Devouring Storms (D.8 glitch pass): there is no sun in the night sky.
 *
 * The base mod adds a warm bloom around the sun while the storm's gloom is over
 * the sky (StormSunGlow -> post/storm_sun_glow.fsh). It is driven by the gloom
 * factor, and the gloom rides the storm, not the clock -- so at night, with a
 * storm up, the pass still painted a warm patch where the sun would have been.
 * That is the "weird yellow thing at nighttime in the sky".
 *
 * This cancels the pass exactly when there is no sun to glow around:
 *   * the sun is below the horizon (the celestial angle says night), or
 *   * the player is inside the decayed reality, which has no sun at all.
 *
 * require = 0 and a try/catch, so a rename in a future base jar degrades to
 * "the glow stays as it was" instead of refusing to launch the game.
 */
@Mixin(value = StormSunGlow.class, remap = false)
public abstract class McsmSunGlowNightPatch {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mcsm$noSunNoGlow(CameraRenderState camera, CallbackInfo ci) {
        try {
            if (McsmReality.inside(Minecraft.getInstance().level)) {
                ci.cancel();
                return;
            }
            float day = sunElevation(Minecraft.getInstance().level);
            if (day <= 0.02F) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
            // never take the frame down for a sky effect
        }
    }

    /**
     * Cosine of the sun's elevation, 1.0 at noon and negative at night. Read
     * through reflection because the accessor name differs between versions; if
     * it cannot be read, the answer is "daytime" so the effect is left alone
     * rather than silently disabled everywhere.
     */
    private static float sunElevation(Object level) {
        if (level == null) {
            return 1.0F;
        }
        try {
            Object angle = null;
            for (java.lang.reflect.Method m : level.getClass().getMethods()) {
                if (m.getName().equals("getTimeOfDay") && m.getParameterCount() == 1) {
                    angle = m.invoke(level, 0.0F);
                    break;
                }
            }
            if (angle == null) {
                for (java.lang.reflect.Method m : level.getClass().getMethods()) {
                    if (m.getName().equals("getDayTime") && m.getParameterCount() == 0) {
                        long t = ((Number) m.invoke(level)).longValue() % 24000L;
                        angle = (float) (t / 24000.0D);
                        break;
                    }
                }
            }
            if (angle == null) {
                return 1.0F;
            }
            double a = ((Number) angle).doubleValue();
            return (float) Math.cos(a * Math.PI * 2.0D);
        } catch (Throwable t) {
            return 1.0F;
        }
    }
}
