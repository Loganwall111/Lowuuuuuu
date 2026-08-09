package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.item.AmuletOfDecayItem;
import com.rewritten.devouringstorms.item.AudioLogItem;
import com.rewritten.devouringstorms.item.MemoryFragmentItem;
import com.rewritten.devouringstorms.item.SeventhTrumpetItem;
import com.rewritten.devouringstorms.item.FormidiBombItem;
import com.rewritten.devouringstorms.item.RiftKeyItem;
import com.rewritten.devouringstorms.item.StormFleshItem;
import com.rewritten.devouringstorms.item.WatcherEyeItem;
import com.rewritten.devouringstorms.item.BrokenRecordItem;
import com.rewritten.devouringstorms.item.RocketKeyItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;

/** All Devouring Storms items (including BlockItems and dev spawn eggs). */
public final class ModItems {

    // ---- Jukebox songs (data-driven since 1.21) -------------------------------------------
    public static final ResourceKey<JukeboxSong> SONG_WE_HAVE_BEEN_CHANGED =
        ResourceKey.create(Registries.JUKEBOX_SONG, DevouringStorms.id("we_have_been_changed"));
    public static final ResourceKey<JukeboxSong> SONG_SIGNAL_TAPE =
        ResourceKey.create(Registries.JUKEBOX_SONG, DevouringStorms.id("signal_tape"));
    public static final ResourceKey<JukeboxSong> SONG_EAOIN =
        ResourceKey.create(Registries.JUKEBOX_SONG, DevouringStorms.id("eaoin"));
    public static final ResourceKey<JukeboxSong> SONG_COUNTDOWN =
        ResourceKey.create(Registries.JUKEBOX_SONG, DevouringStorms.id("countdown"));
    public static final ResourceKey<JukeboxSong> SONG_QUARANTINE =
        ResourceKey.create(Registries.JUKEBOX_SONG, DevouringStorms.id("quarantine"));
    public static final ResourceKey<JukeboxSong> SONG_SHIPS_TO_CARRY_US_HOME =
        ResourceKey.create(Registries.JUKEBOX_SONG, DevouringStorms.id("ships_to_carry_us_home"));

    // ---- Lore / progression items -----------------------------------------------------------
    /** Found deep in Ancient Cities and Strongholds: "The Wither Storm blueprints are corrupted." */
    public static final Item CORRUPTED_BLUEPRINTS = register("corrupted_blueprints", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    /** Awakens a completed Mainframe. Reward for surviving the breached terminal. */
    public static final Item RIFT_KEY = register("rift_key", RiftKeyItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    /** Story Mode's F-bomb. The only way to END the storm for good. */
    public static final Item FORMIDI_BOMB = register("formidibomb", FormidiBombItem::new,
        new Item.Properties().stacksTo(16).rarity(Rarity.EPIC));

    /** An eye that was never human. Reveals the Watcher's mark. */
    public static final Item WATCHER_EYE = register("watcher_eye", WatcherEyeItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    /** Protects its carrier from the Decay plague. */
    public static final Item AMULET_OF_DECAY = register("amulet_of_decay", AmuletOfDecayItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    /** Anna's gift — a fragment of a memory that isn't real. Tames Tazo. */
    public static final Item MEMORY_FRAGMENT = register("memory_fragment", MemoryFragmentItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    // ---- THE SEVEN SCHEDULES (the ARG vault trail) -------------------------------------------
    // Scattered one each: Endertown plaza cache, Mainframe Ruin, Watcher Shrine, Anna's gift,
    // Tazo's trust, MASSG's corpse, the Severed Storms. All seven open a Sealed Vault.
    public static final Item SCHEDULE_1 = register("schedule_1", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final Item SCHEDULE_2 = register("schedule_2", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final Item SCHEDULE_3 = register("schedule_3", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final Item SCHEDULE_4 = register("schedule_4", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final Item SCHEDULE_5 = register("schedule_5", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final Item SCHEDULE_6 = register("schedule_6", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final Item SCHEDULE_7 = register("schedule_7", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    /** The physical proof the vault opened. Its text stays redacted until 2027. */
    public static final Item CLASSIFIED_PAYLOAD = register("classified_payload", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    /** The Watcher's hoarded blade. The only thing that can rend the husk's command block. */
    public static final Item STORM_KILLER = register("storm_killer", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    /** Proof the storm was rent by hand, not by bomb. It still ticks, faintly. */
    public static final Item STORM_HEART = register("storm_heart", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    /** The dormant ritual trigger: sound it and the storm advances one phase, now. */
    public static final Item SEVENTH_TRUMPET = register("seventh_trumpet", SeventhTrumpetItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    // ---- E.P.A. audio logs (field recordings of the last official expedition) -----------------
    public static final Item AUDIO_LOG_1 = register("audio_log_1", p -> new AudioLogItem(p, 0),
        new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final Item AUDIO_LOG_2 = register("audio_log_2", p -> new AudioLogItem(p, 1),
        new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final Item AUDIO_LOG_3 = register("audio_log_3", p -> new AudioLogItem(p, 2),
        new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    /** The seven schedules, in assembly order: they spell out the vault's password. */
    public static final java.util.List<Item> SCHEDULES = java.util.List.of(
        SCHEDULE_1, SCHEDULE_2, SCHEDULE_3, SCHEDULE_4, SCHEDULE_5, SCHEDULE_6, SCHEDULE_7);

    // ---- Storm materials (DR-styled drop set) ------------------------------------------------
    public static final Item STORM_FLESH = register("storm_flesh", StormFleshItem::new,
        new Item.Properties().stacksTo(16));
    public static final Item DECAYED_FLESH = register("decayed_flesh", StormFleshItem::new,
        new Item.Properties().stacksTo(16));
    public static final Item DECAYED_BONE = register("decayed_bone", Item::new,
        new Item.Properties().stacksTo(16));
    public static final Item TENDRIL = register("tendril", Item::new,
        new Item.Properties().stacksTo(4).rarity(Rarity.UNCOMMON));
    public static final Item STORM_DUST = register("storm_dust", Item::new,
        new Item.Properties().stacksTo(32));

    // ---- Victory stars ----------------------------------------------------------------------
    /** Dropped when MASSG finally dies: proof the storm was commanded. */
    public static final Item COMMANDED_STAR = register("commanded_star", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final Item WITHERED_NETHER_STAR = register("withered_nether_star", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    // ---- Travel & warped media ---------------------------------------------------------------
    /** The broken record: the only summons into the Cosmic Abyss. */
    public static final Item BROKEN_RECORD = register("broken_record", BrokenRecordItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    /** The rocket key: one key, three planets, a ring worth of traffic. */
    public static final Item ROCKET_KEY = register("rocket_key", RocketKeyItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    // ---- Music discs ------------------------------------------------------------------------
    /** "We have been changed forever..." — the trailer's lament. */
    public static final Item MUSIC_DISC_CHANGED = register("music_disc_changed", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(SONG_WE_HAVE_BEEN_CHANGED));
    /** "Waiting for the ships to carry us home..." */
    public static final Item MUSIC_DISC_SHIPS = register("music_disc_ships", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(SONG_SHIPS_TO_CARRY_US_HOME));
    /** "The Signal" — the countdown that starts itself. */
    public static final Item MUSIC_DISC_SIGNAL_TAPE = register("music_disc_signal_tape", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(SONG_SIGNAL_TAPE));
    /** "EAOIN, Sing" — the next generation's lullaby. */
    public static final Item MUSIC_DISC_EAOIN = register("music_disc_eaoin", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(SONG_EAOIN));
    /** "Countdown" — 10 months, redacted days, 34 minutes, 78 seconds. */
    public static final Item MUSIC_DISC_COUNTDOWN = register("music_disc_countdown", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(SONG_COUNTDOWN));
    /** "Outside The Quarantine" — storm-synth for those who stepped past line. */
    public static final Item MUSIC_DISC_QUARANTINE = register("music_disc_quarantine", Item::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(SONG_QUARANTINE));

    // ---- Block items ------------------------------------------------------------------------
    public static final Item CORRUPTED_COMMAND_BLOCK_ITEM = blockItem(ModBlocks.CORRUPTED_COMMAND_BLOCK, "corrupted_command_block", new Item.Properties().rarity(Rarity.EPIC));
    public static final Item TERMINAL_ITEM = blockItem(ModBlocks.TERMINAL, "terminal", new Item.Properties().rarity(Rarity.RARE));
    public static final Item MAINFRAME_FRAME_ITEM = blockItem(ModBlocks.MAINFRAME_FRAME, "mainframe_frame", new Item.Properties());
    public static final Item RIFT_PORTAL_ITEM = blockItem(ModBlocks.RIFT_PORTAL, "rift_portal", new Item.Properties());
    public static final Item DECAYED_JUKEBOX_ITEM = blockItem(ModBlocks.DECAYED_JUKEBOX, "decayed_jukebox", new Item.Properties());
    public static final Item DECAYED_SOIL_ITEM = blockItem(ModBlocks.DECAYED_SOIL, "decayed_soil", new Item.Properties());
    public static final Item DECAYED_STONE_ITEM = blockItem(ModBlocks.DECAYED_STONE, "decayed_stone", new Item.Properties());
    public static final Item ROT_LOG_ITEM = blockItem(ModBlocks.ROT_LOG, "rot_log", new Item.Properties());
    public static final Item DECAY_BLOCK_ITEM = blockItem(ModBlocks.DECAY_BLOCK, "decay_block", new Item.Properties());
    public static final Item GLITCH_BLOCK_ITEM = blockItem(ModBlocks.GLITCH_BLOCK, "glitch_block", new Item.Properties());
    public static final Item VHS_JUKEBOX_ITEM = blockItem(ModBlocks.VHS_JUKEBOX, "vhs_jukebox", new Item.Properties());
    public static final Item CRATE_BLOCK_ITEM = blockItem(ModBlocks.CRATE_BLOCK, "crate_block", new Item.Properties());
    public static final Item SEALED_VAULT_ITEM = blockItem(ModBlocks.SEALED_VAULT, "sealed_vault", new Item.Properties().rarity(Rarity.RARE));
    public static final Item FRAYED_TEAR_ITEM = blockItem(ModBlocks.FRAYED_TEAR, "frayed_tear", new Item.Properties().rarity(Rarity.RARE));

    // ---- Spawn eggs (testing / creative) ------------------------------------------------------
    public static final Item MASSG_SPAWN_EGG = spawnEgg("massg_spawn_egg");
    public static final Item WATCHER_SPAWN_EGG = spawnEgg("watcher_spawn_egg");
    public static final Item TAZO_SPAWN_EGG = spawnEgg("tazo_spawn_egg");
    public static final Item ANNA_SPAWN_EGG = spawnEgg("anna_spawn_egg");
    public static final Item SEVERED_SPAWN_EGG = spawnEgg("severed_storm_spawn_egg");
    public static final Item SYMBIONT_SPAWN_EGG = spawnEgg("withered_symbiont_spawn_egg");
    public static final Item PREACHER_SPAWN_EGG = spawnEgg("preacher_spawn_egg");
    public static final Item TOWNSFOLK_SPAWN_EGG = spawnEgg("townsfolk_spawn_egg");
    public static final Item STORM_MITE_SPAWN_EGG = spawnEgg("storm_mite_spawn_egg");
    public static final Item THE_TAKEN_SPAWN_EGG = spawnEgg("the_taken_spawn_egg");
    public static final Item TRAVIS_SPAWN_EGG = spawnEgg("travis_spawn_egg");
    public static final Item TONYA_SPAWN_EGG = spawnEgg("tonya_spawn_egg");
    public static final Item VOID_MAW_SPAWN_EGG = spawnEgg("void_maw_spawn_egg");
    public static final Item CREATOR_SPAWN_EGG = spawnEgg("creator_spawn_egg");
    public static final Item MONSTROSITY_SPAWN_EGG = spawnEgg("monstrosity_spawn_egg");
    public static final Item FORGER_SPAWN_EGG = spawnEgg("forger_spawn_egg");
    public static final Item CART_SHOPPER_SPAWN_EGG = spawnEgg("cart_shopper_spawn_egg");
    public static final Item RESEARCHER_SPAWN_EGG = spawnEgg("researcher_spawn_egg");
    public static final Item EARTH_EATER_SPAWN_EGG = spawnEgg("earth_eater_spawn_egg");

    private ModItems() {
    }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, DevouringStorms.id(name));
        Item item = factory.apply(props.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static Item blockItem(Block block, String name, Item.Properties props) {
        return register(name, p -> new BlockItem(block, p), props);
    }

    private static Item spawnEgg(String name) {
        // 1.21.x moved egg colours into data components; the entity type alone is enough.
        return register(name, props -> {
            var type = switch (name) {
                case "massg_spawn_egg" -> ModEntities.MASSG;
                case "watcher_spawn_egg" -> ModEntities.WATCHER;
                case "tazo_spawn_egg" -> ModEntities.TAZO;
                case "anna_spawn_egg" -> ModEntities.ANNA_APPARITION;
                case "severed_storm_spawn_egg" -> ModEntities.SEVERED_STORM;
                case "preacher_spawn_egg" -> ModEntities.PREACHER;
                case "townsfolk_spawn_egg" -> ModEntities.TOWNSFOLK;
                case "storm_mite_spawn_egg" -> ModEntities.STORM_MITE;
                case "the_taken_spawn_egg" -> ModEntities.THE_TAKEN;
                case "travis_spawn_egg" -> ModEntities.TRAVIS;
                case "tonya_spawn_egg" -> ModEntities.TONYA;
                case "void_maw_spawn_egg" -> ModEntities.VOID_MAW;
                case "creator_spawn_egg" -> ModEntities.CREATOR;
                case "monstrosity_spawn_egg" -> ModEntities.MONSTROSITY;
                case "forger_spawn_egg" -> ModEntities.FORGER;
                case "cart_shopper_spawn_egg" -> ModEntities.CART_SHOPPER;
                case "researcher_spawn_egg" -> ModEntities.RESEARCHER;
                case "earth_eater_spawn_egg" -> ModEntities.EARTH_EATER;
                default -> ModEntities.WITHERED_SYMBIONT;
            };
            return new SpawnEggItem(type, props);
        }, new Item.Properties());
    }

    public static void register() {
        // Static initialisation only.
    }
}
