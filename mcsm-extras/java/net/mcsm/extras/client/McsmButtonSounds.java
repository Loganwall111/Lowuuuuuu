package net.mcsm.extras.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Build #375: the new Story Mode button sounds.
 *
 * Synthesized crystal UI sounds (jar-overrides/assets/mcsm/sounds/*.wav,
 * their own "mcsm" namespace so the base pack's sounds.json is untouched):
 *   - hover : a soft high "tink" when a button is entered
 *   - click : a warm two-partial chime when it is pressed
 *   - open  : a rising whoosh + chime bloom when a big menu opens
 *
 * Registration follows the base mod's own ModSounds pattern exactly
 * (Registry.register on BuiltInRegistries.SOUND_EVENT with
 * SoundEvent.createVariableRangeEvent), and playback is a plain
 * getSoundManager().play(...) one-shot that works with or without a
 * loaded level.
 */
public final class McsmButtonSounds {

    private static final SoundEvent HOVER = register("mcsm.ds_btn_hover");
    private static final SoundEvent CLICK = register("mcsm.ds_btn_click");
    private static final SoundEvent OPEN = register("mcsm.ds_menu_open");

    private McsmButtonSounds() {
    }

    private static SoundEvent register(String name) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath("mcsm", name);
            return (SoundEvent) Registry.register(BuiltInRegistries.SOUND_EVENT, id,
                    SoundEvent.createVariableRangeEvent(id));
        } catch (Throwable t) {
            // duplicate registration (class re-init) or a registry hiccup:
            // sounds are cosmetic, never fatal
            return null;
        }
    }

    private static SoundEvent active(SoundEvent e) {
        return e == null ? null : e;
    }

    public static void hover() {
        play(HOVER, 0.35F, 1.0F);
    }

    public static void click() {
        play(CLICK, 0.7F, 1.0F);
    }

    public static void menuOpen() {
        play(OPEN, 0.8F, 1.0F);
    }

    private static void play(SoundEvent event, float volume, float pitch) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getSoundManager() == null) {
                return;
            }
            SoundEvent active = active(event);
            if (active == null) {
                return;
            }
            // The exact 12-arg SimpleSoundInstance construction the base
            // uses for its distant-vocal one-shots - no position, not
            // relative, no looping: a UI-layer one-shot.
            SimpleSoundInstance snd = new SimpleSoundInstance(active.location(),
                    SoundSource.UI, volume, pitch, RandomSource.create(),
                    false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, false);
            mc.getSoundManager().play(snd);
        } catch (Throwable ignored) {
            // a button sound must never take down the menu
        }
    }
}
