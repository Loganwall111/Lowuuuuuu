package net.mcsm.extras;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * BUILD #425 -- THE MOD'S OWN SOUNDS.
 *
 * THE REPORT. "The radio, like, it wasn't using custom sounds -- it was using
 * sounds that were already in the game" and of the distortions, "I would like
 * custom scary sounds". Every cue in this overlay was a vanilla SoundEvent, so
 * nothing the mod did sounded like itself.
 *
 * These are real registered SoundEvents in the mod's own namespace, backed by
 * real Ogg Vorbis files that ci/make_mcsm_sounds.py synthesises from scratch
 * (no sample is copied from anywhere -- see that script for what each one is made
 * of). The registration line is the base mod's own ModSounds.register(), so the
 * API shape is proven on this classpath rather than guessed.
 *
 * The resource files live at assets/mcsm/sounds/**.ogg with the matching
 * assets/mcsm/sounds.json, which the build overlays into the jar as-is.
 */
public final class McsmSounds {

    public static final SoundEvent RADIO_STATIC = register("radio_static");
    public static final SoundEvent RADIO_CARRIER = register("radio_carrier");
    public static final SoundEvent RADIO_VOICE = register("radio_voice");
    public static final SoundEvent RADIO_DISTRESS = register("radio_distress");
    public static final SoundEvent RADIO_MORSE = register("radio_morse");

    public static final SoundEvent MASSG_BREATH = register("massg_breath");
    public static final SoundEvent MASSG_GIGGLE = register("massg_giggle");
    public static final SoundEvent MASSG_WHISPER = register("massg_whisper");
    public static final SoundEvent MASSG_ROAR = register("massg_roar");
    public static final SoundEvent MASSG_HEART = register("massg_heart");

    public static final SoundEvent OBLIVION_DRONE = register("oblivion_drone");
    public static final SoundEvent OBLIVION_GLITCH = register("oblivion_glitch");
    public static final SoundEvent OBLIVION_WARP = register("oblivion_warp");

    // BUILD #485 -- ninth layer: reality-glitch nightmare / uninpossible layer sounds
    public static final SoundEvent CREATOR_STOMP = register("creator_stomp");
    public static final SoundEvent COSMIC_RUMBLE = register("cosmic_rumble");
    public static final SoundEvent REALITY_GLITCH_NIGHTMARE = register("reality_glitch_nightmare");
    public static final SoundEvent VOID_STOMP = register("void_stomp");

    public static final SoundEvent TERMINAL_OPEN = register("terminal_open");
    public static final SoundEvent TERMINAL_KEY = register("terminal_key");
    public static final SoundEvent TERMINAL_DENY = register("terminal_deny");

    /** Everything, in the order the generator writes it. */
    public static final SoundEvent[] ALL = {
            RADIO_STATIC, RADIO_CARRIER, RADIO_VOICE, RADIO_DISTRESS, RADIO_MORSE,
            MASSG_BREATH, MASSG_GIGGLE, MASSG_WHISPER, MASSG_ROAR, MASSG_HEART,
            OBLIVION_DRONE, OBLIVION_GLITCH, OBLIVION_WARP,
            CREATOR_STOMP, COSMIC_RUMBLE, REALITY_GLITCH_NIGHTMARE, VOID_STOMP,
            TERMINAL_OPEN, TERMINAL_KEY, TERMINAL_DENY
    };

    /** The station sounds, in the order the radio steps through them. */
    public static final SoundEvent[] STATIONS = {
            RADIO_CARRIER, RADIO_STATIC, RADIO_VOICE, RADIO_MORSE, RADIO_DISTRESS
    };

    private McsmSounds() {
    }

    /** The base mod's own registration shape (ModSounds.register). */
    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("mcsm", name);
        return (SoundEvent) Registry.register(BuiltInRegistries.SOUND_EVENT, id,
                SoundEvent.createVariableRangeEvent(id));
    }

    /**
     * Touching this class registers everything (the constants above run at class
     * load). Called from the same mod-init window the rest of the overlay uses,
     * before any world exists, which is the only window a registry is open.
     */
    public static void initialize() {
    }

    /** The station sound for a step index, safely wrapped. */
    public static SoundEvent station(int index) {
        return STATIONS[Math.floorMod(index, STATIONS.length)];
    }
}
