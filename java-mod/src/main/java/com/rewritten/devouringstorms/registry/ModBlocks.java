package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.block.CorruptedCommandBlockBlock;
import com.rewritten.devouringstorms.block.DecayBlock;
import com.rewritten.devouringstorms.block.DecayedJukeboxBlock;
import com.rewritten.devouringstorms.block.FrayedTearBlock;
import com.rewritten.devouringstorms.block.RiftPortalBlock;
import com.rewritten.devouringstorms.block.SealedVaultBlock;
import com.rewritten.devouringstorms.block.TerminalBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * All Devouring Storms blocks.
 *
 * BlockItems are created in {@link ModItems} (blocks must exist first).
 */
public final class ModBlocks {

    /** The ritual anchor. Place it, use Corrupted Blueprints on it, and MASSG wakes. */
    public static final Block CORRUPTED_COMMAND_BLOCK = register(
        "corrupted_command_block",
        CorruptedCommandBlockBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(50.0f, 3600000.0f)
            .lightLevel(s -> 7)
            .sound(SoundType.METAL)
    );

    /** Mainframe terminal — the centre of the 5x5 Mainframe foundation. "The mainframe has been breached." */
    public static final Block TERMINAL = register(
        "terminal",
        TerminalBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(8.0f, 1200.0f)
            .requiresCorrectToolForDrops()
            .lightLevel(s -> s.getValue(TerminalBlock.ACTIVE) ? 11 : 4)
            .sound(SoundType.METAL)
    );

    /** A tear that frays on its own — rides the multiverse ring: Decayed ⟷ The Fray ⟷ Echo Fields. */
    public static final Block FRAYED_TEAR = register(
        "frayed_tear",
        FrayedTearBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_MAGENTA)
            .strength(-1.0f, 3600000.0f)
            .noCollission()
            .lightLevel(s -> 12)
            .sound(SoundType.AMETHYST)
    );

    /** The ARG vault. Seven schedules scattered across the quarantine open it: M.A.S.S.G.O.O.S. */
    public static final Block SEALED_VAULT = register(
        "sealed_vault",
        SealedVaultBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(40.0f, 3600000.0f)
            .lightLevel(s -> s.getValue(SealedVaultBlock.OPEN) ? 10 : 3)
            .sound(SoundType.METAL)
    );

    /** Frame blocks that make up the 5x5 Mainframe foundation around a Terminal. */
    public static final Block MAINFRAME_FRAME = register(
        "mainframe_frame",
        Block::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(8.0f, 1200.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.DEEPSLATE)
    );

    /** The portal to the Decayed Reality. Standing in it pulls you through the rift. */
    public static final Block RIFT_PORTAL = register(
        "rift_portal",
        RiftPortalBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_MAGENTA)
            .strength(-1.0f)
            .noCollission()
            .lightLevel(s -> 13)
            .sound(SoundType.GLASS)
    );

    /** "Decayed jukebox, which also supports vanilla discs." */
    public static final Block DECAYED_JUKEBOX = register(
        "decayed_jukebox",
        DecayedJukeboxBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(2.0f, 6.0f)
            .sound(SoundType.WOOD)
    );

    /** Rotting ground of the Decayed Reality. */
    public static final Block DECAYED_SOIL = register(
        "decayed_soil",
        Block::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BROWN)
            .strength(0.6f)
            .sound(SoundType.ROOTED_DIRT)
    );

    public static final Block DECAYED_STONE = register(
        "decayed_stone",
        Block::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_PURPLE)
            .strength(1.8f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.DEEPSLATE)
    );

    public static final Block ROT_LOG = register(
        "rot_log",
        props -> new Block(props),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
            .mapColor(MapColor.COLOR_PURPLE)
            .sound(SoundType.STEM)
    );

    /** Ghost of a broadcast: pastel static that refuses to hold still (the Monstrosity's signature). */
    public static final Block GLITCH_BLOCK = register(
        "glitch_block",
        GlitchBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_MAGENTA)
            .strength(0.9f)
            .randomTicks()
            .lightLevel(s0 -> 7)
            .sound(SoundType.SCULK)
    );

    /** THE VHS JUKEBOX: plays the tape, and plays the truth (client overlay reads PLAYING). */
    public static final Block VHS_JUKEBOX = register(
        "vhs_jukebox",
        VhsJukeboxBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(2.0f, 6.0f)
            .sound(SoundType.WOOD)
    );

    /** Shipping crates of the BHS limitless-spaces aisles. */
    public static final Block CRATE_BLOCK = register(
        "crate_block",
        Block::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.RAW_IRON)
            .strength(1.2f, 3.0f)
            .sound(SoundType.WOOD)
    );

    /** The plague. Spreads, converts terrain, and inflicts the Decay effect. */
    public static final Block DECAY_BLOCK = register(
        "decay_block",
        DecayBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_MAGENTA)
            .strength(0.8f)
            .randomTicks()
            .lightLevel(s -> 3)
            .sound(SoundType.SCULK)
    );

    private ModBlocks() {
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, com.rewritten.devouringstorms.DevouringStorms.id(name));
        Block block = factory.apply(props.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static void register() {
        // Static initialisation only.
    }
}
