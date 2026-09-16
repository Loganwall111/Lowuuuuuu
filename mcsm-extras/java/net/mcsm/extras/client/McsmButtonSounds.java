package net.mcsm.extras.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import net.mcsm.extras.McsmUiSounds;

/**
 * Devouring Storms UI sound playback (Build #416 rewrite of the #375 helper).
 *
 * The events themselves live in the common {@code McsmUiSounds} class so they
 * are registered at mod init rather than on the first click, and the assets
 * are Ogg Vorbis (see {@code ci/make_ui_sounds.py}). This class only plays
 * them, using the exact 12-argument {@code SimpleSoundInstance} construction
 * the base mod already uses for its distant vocals
 * ({@code StormDistantVocals} L101) -- a positional one-shot with
 * {@code Attenuation.NONE}, so a UI blip is audible even while the client is
 * still on the title/loading screen and no level exists yet.
 *
 * Every entry point is debounced: the title overhaul drives hover/click from
 * its own render and input passes, and both the global
 * {@code McsmButtonSoundHookMixin} and vanilla button dispatch can reach the
 * same press. Without the debounce the two paths stacked duplicates and read
 * as one loud, clipped noise instead of a clean chime.
 */
public final class McsmButtonSounds {

    private static long lastHoverMs;
    private static long lastClickMs;
    private static long lastOpenMs;

    private McsmButtonSounds() {
    }

    /** Soft tink when the pointer lands on a control. */
    public static void hover() {
        if (!allowed(lastHoverMs, 45L)) {
            return;
        }
        lastHoverMs = System.currentTimeMillis();
        play(McsmUiSounds.BUTTON_HOVER, 0.30F, 1.0F);
    }

    /** Crystal chime when a control is pressed. */
    public static void click() {
        if (!allowed(lastClickMs, 25L)) {
            return;
        }
        lastClickMs = System.currentTimeMillis();
        play(McsmUiSounds.BUTTON_CLICK, 0.55F, 1.0F);
    }

    /** Rising whoosh when a menu unfolds. */
    public static void menuOpen() {
        if (!allowed(lastOpenMs, 120L)) {
            return;
        }
        lastOpenMs = System.currentTimeMillis();
        play(McsmUiSounds.MENU_OPEN, 0.55F, 1.0F);
    }

    private static boolean allowed(long lastMs, long windowMs) {
        return System.currentTimeMillis() - lastMs >= windowMs;
    }

    private static void play(SoundEvent event, float volume, float pitch) {
        try {
            if (event == null) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getSoundManager() == null) {
                return;
            }
            SimpleSoundInstance snd = new SimpleSoundInstance(event.location(),
                    SoundSource.UI, volume, pitch, RandomSource.create(),
                    false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, false);
            mc.getSoundManager().play(snd);
        } catch (Throwable ignored) {
            // a button sound must never take down the menu
        }
    }
}
