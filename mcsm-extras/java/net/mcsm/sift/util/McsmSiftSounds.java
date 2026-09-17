package net.mcsm.sift.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

/**
 * Whale-Song Audio Integration - Fabric version
 */
public final class McsmSiftSounds {

    public static final SoundEvent VOID_WHALE_SONG = sound("void_whale_song");
    public static final SoundEvent VOID_WHALE_CLICK = sound("void_whale_click");
    public static final SoundEvent VOID_WHALE_ECHO = sound("void_whale_echo");
    public static final SoundEvent DWELLER_TALK = sound("void_dweller_talk");
    public static final SoundEvent DWELLER_HUM = sound("void_dweller_hum");
    public static final SoundEvent SIFT_AMBIENT_TIER1 = sound("sift_ambient_tier1");
    public static final SoundEvent SIFT_AMBIENT_TIER2 = sound("sift_ambient_tier2");
    public static final SoundEvent SIFT_AMBIENT_TIER3 = sound("sift_ambient_tier3");
    public static final SoundEvent SIFT_AMBIENT_TIER5 = sound("sift_ambient_tier5");
    public static final SoundEvent RIFT_OPEN = sound("rift_open");
    public static final SoundEvent RIFT_CLOSE = sound("rift_close");
    public static final SoundEvent GEL_SPLASH = sound("gel_splash");
    public static final SoundEvent UI_TYPEWRITER = sound("ui_typewriter");

    // V2 new sounds
    public static final SoundEvent BLACK_HOLE_HUM = sound("black_hole_hum");
    public static final SoundEvent BLACK_HOLE_ENTER = sound("black_hole_enter");
    public static final SoundEvent VOID_JELLY_PULSE = sound("void_jelly_pulse");
    public static final SoundEvent PRISMATIC_WISP = sound("prismatic_wisp");

    private McsmSiftSounds() {}

    public static void register() {}

    private static SoundEvent sound(String name) {
        ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, Identifier.fromNamespaceAndPath("mcsm", name));
        SoundEvent ev = SoundEvent.createVariableRangeEvent(key.identifier());
        try {
            Registry.register(BuiltInRegistries.SOUND_EVENT, key, ev);
        } catch (Throwable t) {
            System.err.println("[sift] sound mcsm:" + name + " not registered: " + t);
        }
        return ev;
    }
}
