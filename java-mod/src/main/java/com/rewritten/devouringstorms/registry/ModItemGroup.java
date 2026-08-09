package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** The DEVOURING STORMS creative tab. */
public final class ModItemGroup {

    public static final CreativeModeTab GROUP = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        DevouringStorms.id("devouring_storms"),
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.devouring_storms"))
            .icon(() -> new ItemStack(ModItems.COMMANDED_STAR))
            .displayItems((params, entries) -> {
                entries.accept(ModItems.CORRUPTED_BLUEPRINTS);
                entries.accept(ModItems.RIFT_KEY);
                entries.accept(ModItems.FORMIDI_BOMB);
                entries.accept(ModItems.WATCHER_EYE);
                entries.accept(ModItems.AMULET_OF_DECAY);
                entries.accept(ModItems.MEMORY_FRAGMENT);
                for (net.minecraft.world.item.Item schedule : ModItems.SCHEDULES) {
                    entries.accept(schedule);
                }
                entries.accept(ModItems.CLASSIFIED_PAYLOAD);
                entries.accept(ModItems.STORM_KILLER);
                entries.accept(ModItems.STORM_HEART);
                entries.accept(ModItems.SEVENTH_TRUMPET);
                entries.accept(ModItems.AUDIO_LOG_1);
                entries.accept(ModItems.AUDIO_LOG_2);
                entries.accept(ModItems.AUDIO_LOG_3);
                entries.accept(ModItems.STORM_FLESH);
                entries.accept(ModItems.DECAYED_FLESH);
                entries.accept(ModItems.DECAYED_BONE);
                entries.accept(ModItems.TENDRIL);
                entries.accept(ModItems.STORM_DUST);
                entries.accept(ModItems.COMMANDED_STAR);
                entries.accept(ModItems.WITHERED_NETHER_STAR);
                entries.accept(ModItems.MUSIC_DISC_CHANGED);
                entries.accept(ModItems.MUSIC_DISC_SHIPS);
                entries.accept(ModItems.MUSIC_DISC_SIGNAL_TAPE);
                entries.accept(ModItems.MUSIC_DISC_EAOIN);
                entries.accept(ModItems.MUSIC_DISC_COUNTDOWN);
                entries.accept(ModItems.MUSIC_DISC_QUARANTINE);
                entries.accept(ModItems.BROKEN_RECORD);
                entries.accept(ModItems.ROCKET_KEY);
                entries.accept(ModItems.GLITCH_BLOCK_ITEM);
                entries.accept(ModItems.VHS_JUKEBOX_ITEM);
                entries.accept(ModItems.CRATE_BLOCK_ITEM);
                entries.accept(ModItems.CORRUPTED_COMMAND_BLOCK_ITEM);
                entries.accept(ModItems.TERMINAL_ITEM);
                entries.accept(ModItems.MAINFRAME_FRAME_ITEM);
                entries.accept(ModItems.RIFT_PORTAL_ITEM);
                entries.accept(ModItems.DECAYED_JUKEBOX_ITEM);
                entries.accept(ModItems.DECAYED_SOIL_ITEM);
                entries.accept(ModItems.DECAYED_STONE_ITEM);
                entries.accept(ModItems.ROT_LOG_ITEM);
                entries.accept(ModItems.DECAY_BLOCK_ITEM);
                entries.accept(ModItems.SEALED_VAULT_ITEM);
                entries.accept(ModItems.FRAYED_TEAR_ITEM);
                entries.accept(ModItems.MASSG_SPAWN_EGG);
                entries.accept(ModItems.WATCHER_SPAWN_EGG);
                entries.accept(ModItems.TAZO_SPAWN_EGG);
                entries.accept(ModItems.ANNA_SPAWN_EGG);
                entries.accept(ModItems.SEVERED_SPAWN_EGG);
                entries.accept(ModItems.SYMBIONT_SPAWN_EGG);
                entries.accept(ModItems.PREACHER_SPAWN_EGG);
                entries.accept(ModItems.TOWNSFOLK_SPAWN_EGG);
                entries.accept(ModItems.STORM_MITE_SPAWN_EGG);
                entries.accept(ModItems.THE_TAKEN_SPAWN_EGG);
                entries.accept(ModItems.TRAVIS_SPAWN_EGG);
                entries.accept(ModItems.TONYA_SPAWN_EGG);
                entries.accept(ModItems.VOID_MAW_SPAWN_EGG);
                entries.accept(ModItems.CREATOR_SPAWN_EGG);
                entries.accept(ModItems.MONSTROSITY_SPAWN_EGG);
                entries.accept(ModItems.FORGER_SPAWN_EGG);
                entries.accept(ModItems.CART_SHOPPER_SPAWN_EGG);
                entries.accept(ModItems.RESEARCHER_SPAWN_EGG);
                entries.accept(ModItems.EARTH_EATER_SPAWN_EGG);
            })
            .build()
    );

    private ModItemGroup() {
    }

    public static void register() {
        // Static initialisation only.
    }
}
