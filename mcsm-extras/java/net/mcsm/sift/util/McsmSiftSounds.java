package net.mcsm.sift.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Whale-Song Audio Integration - low-latency Ogg Vorbis engine (McsmUiSounds)
 * Deep, echoing, mechanical whale clicks and songs that reverberate through headset
 */
public final class McsmSiftSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "mcsm");

    // Void Whale songs
    public static final RegistryObject<SoundEvent> VOID_WHALE_SONG = SOUNDS.register("void_whale_song",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "void_whale_song")));

    public static final RegistryObject<SoundEvent> VOID_WHALE_CLICK = SOUNDS.register("void_whale_click",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "void_whale_click")));

    public static final RegistryObject<SoundEvent> VOID_WHALE_ECHO = SOUNDS.register("void_whale_echo",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "void_whale_echo")));

    // Void Dweller dialogue - tactile typewriting
    public static final RegistryObject<SoundEvent> DWELLER_TALK = SOUNDS.register("void_dweller_talk",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "void_dweller_talk")));

    public static final RegistryObject<SoundEvent> DWELLER_HUM = SOUNDS.register("void_dweller_hum",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "void_dweller_hum")));

    // Sift ambience
    public static final RegistryObject<SoundEvent> SIFT_AMBIENT_TIER1 = SOUNDS.register("sift_ambient_tier1",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "sift_ambient_tier1")));

    public static final RegistryObject<SoundEvent> SIFT_AMBIENT_TIER2 = SOUNDS.register("sift_ambient_tier2",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "sift_ambient_tier2")));

    public static final RegistryObject<SoundEvent> SIFT_AMBIENT_TIER3 = SOUNDS.register("sift_ambient_tier3",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "sift_ambient_tier3")));

    public static final RegistryObject<SoundEvent> SIFT_AMBIENT_TIER5 = SOUNDS.register("sift_ambient_tier5",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "sift_ambient_tier5")));

    public static final RegistryObject<SoundEvent> RIFT_OPEN = SOUNDS.register("rift_open",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "rift_open")));

    public static final RegistryObject<SoundEvent> RIFT_CLOSE = SOUNDS.register("rift_close",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "rift_close")));

    public static final RegistryObject<SoundEvent> GEL_SPLASH = SOUNDS.register("gel_splash",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "gel_splash")));

    // UI sounds - typewriter
    public static final RegistryObject<SoundEvent> UI_TYPEWRITER = SOUNDS.register("ui_typewriter",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mcsm", "ui_typewriter")));

    private McsmSiftSounds() {}
}
