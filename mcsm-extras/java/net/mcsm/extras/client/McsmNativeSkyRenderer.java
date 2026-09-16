package net.mcsm.extras.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.dabicco.witherstormmod.client.McsmSkyArtifactGuard;
import net.dabicco.witherstormmod.client.StoryModeSkyTint;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmReality;
import net.mcsm.extras.client.McsmStormPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Keeps Minecraft's native sky pass as the sole sky renderer.
 *
 * The old sky implementation supplied a second upper layer and left the
 * native renderer's zenith endpoint in place.  That is what produced the
 * clipped black daytime strip and the warm nighttime strip at the top of the
 * view.  This hook does not submit geometry or install a texture: it keeps the
 * colour already computed for the current native sky and removes the separate
 * sunrise/sunset fan. The lower/current native colour is therefore the only
 * authority for every sky pixel, including the extreme top of the spherical
 * pass.
 */
public final class McsmNativeSkyRenderer {
    private static volatile boolean ownsSky;

    private McsmNativeSkyRenderer() {
    }

    /**
     * BUILD #416 -- real fading sky, no domes.
     *
     * The storm sky is now painted by the sky PROGRAM (mcsm-core-shaders/core/
     * sky.fsh) as a continuous function of the view ray, so nothing here draws
     * geometry, a dome, a card or a sticker: an edge is impossible by
     * construction. What this hook does is feed that pass and remove the two
     * native primitives that produced the "sky with a top on it" the user
     * reported:
     *
     *   * `skyColor` is set to the phase's reference horizon colour, so the
     *     disc base and the shader gradient agree and no flat band shows;
     *   * `shouldRenderDarkDisc` is cleared -- that dark cap IS the visible
     *     dome/top edge;
     *   * stars fade out as the storm matures (the storyboard kills every
     *     celestial body once phase 5 takes hold), while the sun and moon stay
     *     available for the regular sky, which is why they are not cancelled.
     *
     * It also publishes the phase to {@link McsmStormPhase}, i.e. the
     * WitherStormPhase feed the blueprint asks for.
     */
    public static void apply(ClientLevel level, SkyRenderState state) {
        McsmSkyArtifactGuard.disableExtraSkyLayers();
        if (level == null || state == null) {
            ownsSky = false;
            return;
        }

        float p = McsmStormPhase.resolve();
        McsmStormPhase.publish(p);

        // BUILD #416 (D.8 glitch pass) -- THE DECAYED REALITY HAS ITS OWN SKY.
        //
        // "The skies are still a dome, even the vanilla sky." That is true
        // outside the storm: the native sky is a dome and this pass only took it
        // over from phase 4.45 up. The decayed reality is a dimension we own, so
        // it takes its sky over unconditionally: the horizon colour is the
        // decayed row, the dark disc cap and the sunrise fan are off, the
        // celestials are suppressed by McsmCelestialExcisionMixin, and the storm
        // mass (when one is present) is drawn ON TOP of that sky by the existing
        // backdrop pass. No second dome, no fake cover, one owned sky.
        boolean decayed = McsmReality.inside(level);
        if (decayed) {
            ownsSky = true;
            state.shouldRenderDarkDisc = false;
            state.skyColor = decayedSkyArgb(p);
            state.sunriseAndSunsetColor = state.skyColor;
            state.starBrightness = 0.0F;
            state.rainBrightness = 0.0F;
            return;
        }

        if (p < McsmStormPhase.PHASE_MIN) {
            // No storm: leave the whole regular sky exactly as vanilla built
            // it. The shader still fades it (day/night/sunset columns), but
            // the native state is untouched so nothing can regress.
            ownsSky = false;
            return;
        }

        ownsSky = true;
        // The dark disc cap is the top-of-sky artefact: kill it every frame.
        state.shouldRenderDarkDisc = false;
        // Match the native disc colour to the shader's horizon row.
        state.skyColor = McsmStormPhase.horizonArgb();
        // Bodies: gone once the storm is running.
        if (p >= 5.0F) {
            state.starBrightness = 0.0F;
            state.rainBrightness = Math.min(state.rainBrightness, 1.0F);
        }
        // The sunrise/sunset fan is a second colour ramp across the sky; with
        // the gradient in place it can only draw a band, so it is pinned to the
        // same horizon colour.
        state.sunriseAndSunsetColor = state.skyColor;
    }


    /**
     * The decayed reality's own sky: a torn violet-black gradient that runs from
     * the #1B1026 zenith to the #4A2A6E horizon the dimension type ships, and
     * warms into the storm's phase colour as a storm grows inside it. Kept in
     * Java (not a texture) so it cannot be mistaken for a dome.
     */
    private static int decayedSkyArgb(float phase) {
        float[] zenith = { 0x1B / 255.0F, 0x10 / 255.0F, 0x26 / 255.0F };
        float[] horizon = { 0x4A / 255.0F, 0x2A / 255.0F, 0x6E / 255.0F };
        if (phase >= McsmStormPhase.PHASE_MIN) {
            horizon = new float[]{ 0x5A / 255.0F, 0x24 / 255.0F, 0x74 / 255.0F };
        }
        float t = phase >= McsmStormPhase.PHASE_MIN ? 0.65F : 0.45F;
        int r = Math.round((zenith[0] + (horizon[0] - zenith[0]) * t) * 255.0F);
        int g = Math.round((zenith[1] + (horizon[1] - zenith[1]) * t) * 255.0F);
        int b = Math.round((zenith[2] + (horizon[2] - zenith[2]) * t) * 255.0F);
        return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    private static int mcsm$mixArgb(int current, float[] target, float t) {
        if (t <= 0.0F) return current;
        if (t > 1.0F) t = 1.0F;
        int r = (current >> 16) & 0xFF, g = (current >> 8) & 0xFF, bl = current & 0xFF;
        r = Math.round(r + (target[0] * 255.0F - r) * t);
        g = Math.round(g + (target[1] * 255.0F - g) * t);
        bl = Math.round(bl + (target[2] * 255.0F - bl) * t);
        return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (bl & 0xFF);
    }

    /** Return the storm's native horizon colour and its distance blend. */
    public static float fogColor(ClientLevel level, float[] out) {
        // #409 user directive: regular sky, no horizon band. The storm no
        // longer tints world fog; the shader pack (or vanilla) owns it.
        return 0.0F;
    }

    private static void phaseFogColor(float phase, float[] out) {
        float t;
        float[] green = {0x6E / 255.0F, 0x8F / 255.0F, 0x73 / 255.0F};
        float[] slate = {0x6E / 255.0F, 0x78 / 255.0F, 0x73 / 255.0F};
        float[] purple = {0x7F / 255.0F, 0x3A / 255.0F, 0xA6 / 255.0F};
        float[] plum = {0xA0 / 255.0F, 0x75 / 255.0F, 0x7E / 255.0F};
        if (phase < 5.0F) {
            t = smoothstep(4.45F, 5.0F, phase);
            mix(green, slate, t, out);
        } else if (phase < 5.5F) {
            t = smoothstep(5.0F, 5.5F, phase);
            mix(slate, purple, t, out);
        } else {
            t = smoothstep(5.5F, 6.0F, phase);
            mix(purple, plum, t, out);
        }
    }

    private static void mix(float[] a, float[] b, float t, float[] out) {
        for (int i = 0; i < 3; i++) {
            out[i] = a[i] + (b[i] - a[i]) * t;
        }
    }

    private static float smoothstep(float lo, float hi, float value) {
        float t = Math.max(0.0F, Math.min(1.0F, (value - lo) / (hi - lo)));
        return t * t * (3.0F - 2.0F * t);
    }

    public static boolean ownsSky() {
        return ownsSky;
    }

    /**
     * Compatibility gate for the optional sun-slab feature. The current native
     * sky path intentionally keeps ordinary sun/moon/stars, so this is only
     * true when the opt-in accurate-sun setting explicitly owns the frame.
     */
    public static boolean suppressCelestials() {
        return ownsSky && McsmExtrasConfig.storyModeAccurateSunSun;
    }

    /** True when the opt-in studio stage has put the camera outside its dome. */
    public static boolean stageOutside() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || !McsmExperimentalStoryStage.active()) {
                return false;
            }
            Field rendererField = Minecraft.class.getDeclaredField("gameRenderer");
            rendererField.setAccessible(true);
            Object renderer = rendererField.get(mc);
            if (renderer == null) {
                return false;
            }
            Method cameraMethod = renderer.getClass().getMethod("getMainCamera");
            Object camera = cameraMethod.invoke(renderer);
            if (camera == null) {
                return false;
            }
            Method positionMethod = camera.getClass().getMethod("getPosition");
            Object position = positionMethod.invoke(camera);
            return position instanceof Vec3
                    && McsmExperimentalStoryStage.cameraOutside(mc.level, (Vec3) position);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
