package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * All sound events. Audio files are synthesized by tools/generate_assets.py
 * (pure mathematics — no samples from the series or other mods).
 */
public final class ModSounds {

    // Ambience
    public static final SoundEvent AMBIENT_DECAYED_LOOP = register("ambient.decayed_loop");
    public static final SoundEvent AMBIENT_RIFT_HUM = register("ambient.rift_hum");

    // MASSG
    public static final SoundEvent MASSG_AWAKENING = register("massg.awakening");
    public static final SoundEvent MASSG_ROAR = register("massg.roar");
    public static final SoundEvent MASSG_DEVOUR = register("massg.devour");
    public static final SoundEvent MASSG_PULL_LOOP = register("massg.pull_loop");
    public static final SoundEvent MASSG_DEVOLVE_STING = register("massg.devolve_sting");
    public static final SoundEvent MASSG_PLAY_DEAD = register("massg.play_dead");
    public static final SoundEvent MASSG_REBIRTH = register("massg.rebirth");
    public static final SoundEvent MASSG_TRUE_DEATH = register("massg.true_death");

    // Phase music (client music director)
    public static final SoundEvent MUSIC_SIGNAL = register("music.signal");
    public static final SoundEvent MUSIC_HUNGER = register("music.hunger");
    public static final SoundEvent MUSIC_DEVOURER = register("music.devourer");
    public static final SoundEvent MUSIC_SUNDERER = register("music.sunderer");
    public static final SoundEvent MUSIC_GENESIS = register("music.genesis");
    public static final SoundEvent MUSIC_CRITICAL = register("music.critical");

    // The Watcher
    public static final SoundEvent WATCHER_HEARTBEAT = register("watcher.heartbeat");
    public static final SoundEvent WATCHER_WHISPER = register("watcher.whisper");
    public static final SoundEvent WATCHER_VANISH = register("watcher.vanish");

    // Anna
    public static final SoundEvent ANNA_GIGGLE = register("anna.giggle");
    public static final SoundEvent GLITCH = register("glitch");

    // Terminal / mainframe
    public static final SoundEvent TERMINAL_BOOT = register("terminal.boot");
    public static final SoundEvent TERMINAL_TRANSMISSION = register("terminal.transmission");
    public static final SoundEvent RIFT_OPEN = register("rift.open");

    // Jukebox songs
    public static final SoundEvent SONG_WE_HAVE_BEEN_CHANGED = register("record.we_have_been_changed");
    public static final SoundEvent SONG_SHIPS_TO_CARRY_US_HOME = register("record.ships_to_carry_us_home");
    public static final SoundEvent SONG_SIGNAL_TAPE = register("record.signal_tape");
    public static final SoundEvent SONG_EAOIN = register("record.eaoin");
    public static final SoundEvent SONG_COUNTDOWN = register("record.countdown");
    public static final SoundEvent SONG_QUARANTINE = register("record.quarantine");

    private ModSounds() {
    }

    private static SoundEvent register(String path) {
        ResourceLocation id = DevouringStorms.id(path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
        // Static initialisation only.
    }
}
