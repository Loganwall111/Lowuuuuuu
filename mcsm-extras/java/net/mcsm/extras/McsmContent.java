package net.mcsm.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Devouring Storms: the Decayed Reality content pack (mandate D.8, phase 1).
 *
 * The user's brief: "there's only like five or 15 blocks in the game ... have
 * new trap doors, doors, items, even some weapons would be a really cool
 * expansion". This is that expansion: the building set for the torn world --
 * decayed terrain, the ruins of the abandoned cities, the reality-tear
 * materials, the storm's own anatomy as blocks -- plus the tools and weapons
 * the storyline hands out.
 *
 * HOW THIS COMPILES AGAINST A FROZEN JAR
 * --------------------------------------
 * mcsm-extras only ever sees the released base jar (ci/api/mod.txt is its javap
 * dump, and check_phase_uniform.py fails the build on any symbol that dump does
 * not declare). Every registration idiom below is therefore copied from the
 * base mod's own compiled ModBlocks/ModItems/ModItemGroups -- same
 * Registry.register on BuiltInRegistries, same Properties.setId(key) call, same
 * FabricCreativeModeTab builder -- so it is proven to exist in this exact
 * Minecraft version. Anything that could not be proven is done by reflection or
 * left to a require = 0 mixin; nothing here is a guess at a signature.
 *
 * Registration runs from the mod's own onInitialize (see McsmBuiltinPackMixin),
 * i.e. while the built-in registries are still open, next to the base mod's own
 * ModBlocks/ModItems/ModItemGroups pass.
 *
 * Namespace: {@code mcsm}. Everything registered here is also listed in the
 * mcsm content tab and has assets under jar-overrides/assets/mcsm.
 */
public final class McsmContent {

    /** Every block we add, in registration order (used by the panel + the gates). */
    public static final List<Block> ALL_BLOCKS = new ArrayList<>();
    /** Every item we add, in registration order. */
    public static final List<Item> ALL_ITEMS = new ArrayList<>();
    /** The block items, in registration order (the tab lists these first).
     *  Kept as a list rather than looked up afterwards: a registry lookup would
     *  be one more frozen-jar method this class cannot compile-prove. */
    public static final List<Item> ALL_BLOCK_ITEMS = new ArrayList<>();

    // ---------------------------------------------------------------------
    // Building set: the decayed world
    // ---------------------------------------------------------------------
    public static final Block DECAYED_STONE = block("decayed_stone",
            stone(2.0F, 6.0F));
    public static final Block DECAYED_COBBLESTONE = block("decayed_cobblestone",
            stone(2.0F, 6.0F));
    public static final Block DECAYED_STONE_BRICKS = block("decayed_stone_bricks",
            stone(2.5F, 6.0F));
    public static final Block DECAYED_DIRT = block("decayed_dirt",
            BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GRASS));
    public static final Block DECAYED_SURFACE = block("decayed_surface",
            BlockBehaviour.Properties.of().strength(0.7F).sound(SoundType.GRASS));
    public static final Block DECAYED_SAND = block("decayed_sand",
            BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.SAND));
    public static final Block DECAYED_LOG = block("decayed_log",
            RotatedPillarBlock::new, wood(3.4F, 5.0F));
    public static final Block DECAYED_STRIPPED_LOG = block("decayed_stripped_log",
            RotatedPillarBlock::new, wood(2.0F, 3.0F));
    public static final Block DECAYED_PLANKS = block("decayed_planks", wood(2.0F, 3.0F));
    public static final Block DECAYED_LEAVES = block("decayed_leaves",
            BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.GRASS).noOcclusion());

    // ---------------------------------------------------------------------
    // Building set: the abandoned cities
    // ---------------------------------------------------------------------
    public static final Block CITY_BRICKS = block("city_bricks", stone(3.0F, 8.0F));
    public static final Block CITY_TILES = block("city_tiles", stone(3.0F, 8.0F));
    public static final Block RUSTED_PLATE = block("rusted_plate",
            BlockBehaviour.Properties.of().strength(3.5F, 10.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final Block REBAR_GRATE = block("rebar_grate",
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.METAL)
                    .noOcclusion().requiresCorrectToolForDrops());
    public static final Block CRACKED_ROAD = block("cracked_road", stone(2.0F, 7.0F));
    public static final Block HOLLOW_WALL = block("hollow_wall", stone(2.0F, 6.0F));
    public static final Block STORM_RIB = block("storm_rib",
            BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.BONE_BLOCK));
    public static final Block TENDON_BLOCK = block("tendon_block",
            BlockBehaviour.Properties.of().strength(0.9F).sound(SoundType.SLIME_BLOCK)
                    .friction(0.72F));
    public static final Block WITHERED_FLESH_BLOCK = block("withered_flesh_block",
            BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.SLIME_BLOCK)
                    .friction(0.72F));

    // ---------------------------------------------------------------------
    // Building set: reality-tear materials (the new tech tier)
    // ---------------------------------------------------------------------
    public static final Block REALITY_GLASS = block("reality_glass",
            BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.GLASS)
                    .noOcclusion().lightLevel(s -> 6).emissiveRendering(s -> true));
    public static final Block GLITCH_LAMP = block("glitch_lamp",
            BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.GLASS)
                    .lightLevel(s -> 15).emissiveRendering(s -> true));
    public static final Block MEMORY_CRYSTAL = block("memory_crystal",
            BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(SoundType.GLASS)
                    .lightLevel(s -> 9).emissiveRendering(s -> true));
    public static final Block VOID_CORE = block("void_core",
            BlockBehaviour.Properties.of().strength(6.0F, 40.0F).sound(SoundType.METAL)
                    .lightLevel(s -> 12).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());
    public static final Block BLACK_HOLE_CORE = block("black_hole_core",
            BlockBehaviour.Properties.of().strength(9.0F, 1200.0F).sound(SoundType.METAL)
                    .lightLevel(s -> 14).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());
    public static final Block RIFT_ANCHOR = block("rift_anchor",
            BlockBehaviour.Properties.of().strength(4.0F, 30.0F).sound(SoundType.METAL)
                    .lightLevel(s -> 11).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());

    // ---------------------------------------------------------------------
    // Building set: THE VOID'S OWN MATERIAL (BUILD #458)
    // ---------------------------------------------------------------------
    // The report this fixes: "each dimension and infinite subdimension
    // completely unique: own blocks ... no re-use, not the decay set in flat
    // worlds." The void -- a dimension of nothing with things in it -- was
    // floating decayed stone, city tile, decayed planks and glitch lamps, i.e.
    // the decayed reality's set in another world. It has its own now, and
    // McsmVoid builds out of nothing else. Cold, near-black violet with the
    // dimension's own green underlight (see McsmIdentity.VOID).
    public static final Block VOID_STONE = block("void_stone",
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final Block VOID_TILES = block("void_tiles",
            BlockBehaviour.Properties.of().strength(2.6F, 8.0F).sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final Block VOID_PLANKS = block("void_planks", wood(2.4F, 4.0F));
    public static final Block VOID_BONE = block("void_bone",
            BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.BONE_BLOCK));
    public static final Block VOID_GLASS = block("void_glass",
            BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GLASS)
                    .noOcclusion().lightLevel(s -> 4).emissiveRendering(s -> true));
    public static final Block VOID_LAMP = block("void_lamp",
            BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.GLASS)
                    .lightLevel(s -> 14).emissiveRendering(s -> true));
    public static final Block VOID_ANCHOR = block("void_anchor",
            BlockBehaviour.Properties.of().strength(4.5F, 30.0F).sound(SoundType.METAL)
                    .lightLevel(s -> 12).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());

    // ---------------------------------------------------------------------
    // Building set: THE INFINITE DIMENSION'S OWN MATERIAL (BUILD #458)
    // ---------------------------------------------------------------------
    // Same report, other side of it: adams was decayed stone bricks with city
    // tiles and glitch lamps on top -- the decayed reality's set with lamps
    // swapped. Its ground is its own now: warm, built, endless, lit amber by
    // the city glow that hangs over its horizon (McsmIdentity.ADAMS).
    public static final Block ADAMS_STONE = block("adams_stone",
            stone(2.5F, 8.0F));
    public static final Block ADAMS_BRICKS = block("adams_bricks",
            stone(3.0F, 9.0F));
    public static final Block ADAMS_TILES = block("adams_tiles",
            stone(2.6F, 8.0F));
    public static final Block ADAMS_SURFACE = block("adams_surface",
            BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.GRASS));
    public static final Block ADAMS_WALL = block("adams_wall",
            stone(2.6F, 8.0F));
    public static final Block ADAMS_GRATE = block("adams_grate",
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.METAL)
                    .noOcclusion().requiresCorrectToolForDrops());
    public static final Block ADAMS_LAMP = block("adams_lamp",
            BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.GLASS)
                    .lightLevel(s -> 15).emissiveRendering(s -> true));
    public static final Block ADAMS_CRYSTAL = block("adams_crystal",
            BlockBehaviour.Properties.of().strength(1.6F, 6.0F).sound(SoundType.GLASS)
                    .lightLevel(s -> 10).emissiveRendering(s -> true));
    public static final Block ADAMS_RUBBLE = block("adams_rubble",
            BlockBehaviour.Properties.of().strength(1.0F, 4.0F).sound(SoundType.GRAVEL));
    /** The dimension's own salvage, with its own loot table (ci/make_mcsm_content_assets.py). */
    public static final Block ADAMS_CRATE = block("adams_crate",
            BlockBehaviour.Properties.of().strength(1.8F, 6.0F).sound(SoundType.WOOD));
    /** The void's own salvage: the only place a void sigil is ever found. */
    public static final Block VOID_CACHE = block("void_cache",
            BlockBehaviour.Properties.of().strength(1.8F, 6.0F).sound(SoundType.WOOD));

    // ---------------------------------------------------------------------
    // Building set: THE CREATOR'S REACH'S OWN MATERIAL (BUILD #462)
    // ---------------------------------------------------------------------
    // The fourth world, and the only one that is not broken: nothing here grew
    // and nothing here rotted -- every block of it was laid. White marble, gold
    // banding, engraved glyph tiles that hold a little of the light, and the
    // plinth the Creator stands on. Its own family, from top to bottom, so that
    // "own blocks ... no re-use" holds for it the way it holds for the other
    // three (see McsmIdentity.CREATOR).
    public static final Block CREATOR_MARBLE = block("creator_marble",
            stone(3.0F, 9.0F));
    public static final Block CREATOR_FLOOR = block("creator_floor",
            stone(3.4F, 10.0F));
    public static final Block CREATOR_TILES = block("creator_tiles",
            stone(3.2F, 9.0F));
    public static final Block CREATOR_GOLD = block("creator_gold",
            BlockBehaviour.Properties.of().strength(3.0F, 12.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final Block CREATOR_PILLAR = block("creator_pillar",
            stone(3.6F, 12.0F));
    public static final Block CREATOR_GLYPH = block("creator_glyph",
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.STONE)
                    .lightLevel(s -> 9).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());
    public static final Block CREATOR_GLASS = block("creator_glass",
            BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GLASS)
                    .noOcclusion().lightLevel(s -> 5).emissiveRendering(s -> true));
    public static final Block CREATOR_LAMP = block("creator_lamp",
            BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.GLASS)
                    .lightLevel(s -> 15).emissiveRendering(s -> true));
    public static final Block CREATOR_PLINTH = block("creator_plinth",
            BlockBehaviour.Properties.of().strength(4.0F, 18.0F).sound(SoundType.STONE)
                    .lightLevel(s -> 10).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());
    /** The reach's own salvage: the only place a creator sigil is ever found. */
    public static final Block CREATOR_RELIQUARY = block("creator_reliquary",
            BlockBehaviour.Properties.of().strength(1.8F, 6.0F).sound(SoundType.WOOD));

    // ---------------------------------------------------------------------
    // MORE BLOCKS: the shape set, world by world (BUILD #465)
    // ---------------------------------------------------------------------
    // The standing ask -- "there's only like five or 15 blocks ... more blocks
    // and items" -- answered the way the identity pass demands: every world gets
    // the building set cut from ITS OWN material. A void room is built out of the
    // void and a reach room out of the reach; none of these names a parent it
    // does not own. Slabs, stairs, walls and fences are the same classes the
    // pack already compiles against (SlabBlock, ExposedStair, WallBlock,
    // FenceBlock, ExposedTrapDoor), so nothing here is a new API bet.
    public static final Block DECAYED_BRICKS = block("decayed_bricks",
            stone(2.6F, 8.0F));
    public static final Block DECAYED_BRICK_SLAB = block("decayed_brick_slab",
            props -> new SlabBlock(props), stone(2.6F, 8.0F));
    public static final Block DECAYED_BRICK_STAIRS = block("decayed_brick_stairs",
            props -> new ExposedStair(DECAYED_BRICKS.defaultBlockState(), props),
            stone(2.6F, 8.0F));
    public static final Block DECAYED_BRICK_WALL = block("decayed_brick_wall",
            props -> new WallBlock(props), stone(2.6F, 8.0F));
    public static final Block CITY_WINDOW = block("city_window",
            BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GLASS)
                    .noOcclusion().lightLevel(s -> 3));
    public static final Block CITY_RAILING = block("city_railing",
            props -> new FenceBlock(props),
            BlockBehaviour.Properties.of().strength(2.4F, 7.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final Block CITY_BRICK_WALL = block("city_brick_wall",
            props -> new WallBlock(props), stone(3.0F, 8.0F));
    public static final Block VOID_SLAB = block("void_slab",
            props -> new SlabBlock(props), stone(2.4F, 7.0F));
    public static final Block VOID_STAIRS = block("void_stairs",
            props -> new ExposedStair(VOID_STONE.defaultBlockState(), props),
            stone(2.4F, 7.0F));
    public static final Block VOID_WALL = block("void_wall",
            props -> new WallBlock(props), stone(2.4F, 7.0F));
    public static final Block VOID_FENCE = block("void_fence",
            props -> new FenceBlock(props), wood(2.4F, 4.0F));
    public static final Block VOID_TRAPDOOR = block("void_trapdoor",
            props -> new ExposedTrapDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(2.4F, 7.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());
    public static final Block ADAMS_SLAB = block("adams_slab",
            props -> new SlabBlock(props), stone(2.0F, 6.0F));
    public static final Block ADAMS_STAIRS = block("adams_stairs",
            props -> new ExposedStair(ADAMS_STONE.defaultBlockState(), props),
            stone(2.0F, 6.0F));
    // (adams_wall itself is the infinite dimension's hollow-wall CUBE, registered
    // in BUILD #458 -- this one is the wall SHAPE, cut from adams tile, so the two
    // do not share an id or a Java field. Found by the duplicate-key check when the
    // dict silently overwrote the cube.)
    public static final Block ADAMS_TILE_WALL = block("adams_tile_wall",
            props -> new WallBlock(props), stone(2.0F, 6.0F));
    public static final Block ADAMS_FENCE = block("adams_fence",
            props -> new FenceBlock(props), stone(2.0F, 6.0F));
    public static final Block ADAMS_TRAPDOOR = block("adams_trapdoor",
            props -> new ExposedTrapDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());
    public static final Block CREATOR_SLAB = block("creator_slab",
            props -> new SlabBlock(props), stone(3.0F, 9.0F));
    public static final Block CREATOR_STAIRS = block("creator_stairs",
            props -> new ExposedStair(CREATOR_MARBLE.defaultBlockState(), props),
            stone(3.0F, 9.0F));
    public static final Block CREATOR_WALL = block("creator_wall",
            props -> new WallBlock(props), stone(3.2F, 9.0F));
    public static final Block CREATOR_FENCE = block("creator_fence",
            props -> new FenceBlock(props), stone(3.2F, 9.0F));
    public static final Block CREATOR_TRAPDOOR = block("creator_trapdoor",
            props -> new ExposedTrapDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());

    // ---------------------------------------------------------------------
    // THE LOCKS (BUILD #459) -- one per world, and only that world's key fits
    // ---------------------------------------------------------------------
    // The identity list asked for "own ... locks". This is that: a seal block per
    // dimension that a player meets in the world, refuses to move without that
    // dimension's own key, and is spent once it opens (see McsmLocks). The key is
    // never named in a menu -- it is found in that world's own crates, which is
    // how a place teaches you what it wants.
    public static final Block DECAYED_LOCK = block("decayed_lock",
            props -> new DimensionLock(McsmIdentity.DECAYED, props),
            lock(3.0F, 9.0F));
    public static final Block ADAMS_LOCK = block("adams_lock",
            props -> new DimensionLock(McsmIdentity.ADAMS, props),
            lock(3.0F, 9.0F));
    public static final Block VOID_LOCK = block("void_lock",
            props -> new DimensionLock(McsmIdentity.VOID, props),
            lock(3.0F, 9.0F));
    public static final Block CREATOR_LOCK = block("creator_lock",
            props -> new DimensionLock(McsmIdentity.CREATOR, props),
            lock(3.0F, 9.0F));

    // ---------------------------------------------------------------------
    // Shapes (slab / stairs / wall / fence share the decayed palette)
    // ---------------------------------------------------------------------
    public static final Block DECAYED_SLAB = block("decayed_slab",
            props -> new SlabBlock(props), stone(2.0F, 6.0F));
    public static final Block DECAYED_STAIRS = block("decayed_stairs",
            props -> new ExposedStair(DECAYED_STONE.defaultBlockState(), props),
            stone(2.0F, 6.0F));
    public static final Block DECAYED_WALL = block("decayed_wall",
            props -> new WallBlock(props), stone(2.0F, 6.0F));
    public static final Block DECAYED_FENCE = block("decayed_fence",
            props -> new FenceBlock(props), wood(2.0F, 3.0F));
    public static final Block CITY_BRICK_SLAB = block("city_brick_slab",
            props -> new SlabBlock(props), stone(3.0F, 8.0F));
    public static final Block CITY_BRICK_STAIRS = block("city_brick_stairs",
            props -> new ExposedStair(CITY_BRICKS.defaultBlockState(), props),
            stone(3.0F, 8.0F));

    // ---------------------------------------------------------------------
    // Doors and trap doors (the user asked for them by name)
    // ---------------------------------------------------------------------
    public static final Block WITHERED_DOOR = block("withered_door",
            props -> new ExposedDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());
    public static final Block WITHERED_TRAPDOOR = block("withered_trapdoor",
            props -> new ExposedTrapDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());
    public static final Block RUSTED_DOOR = block("rusted_door",
            props -> new ExposedDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(4.0F, 12.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());
    public static final Block RUSTED_TRAPDOOR = block("rusted_trapdoor",
            props -> new ExposedTrapDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(4.0F, 12.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());

    // ---------------------------------------------------------------------
    // City crates (phase 2): what the abandoned cities are worth looting.
    // Each one drops through a vanilla block loot table
    // (data/mcsm/loot_table/blocks/<name>.json), so no Java is involved in
    // making them rewarding -- and ci/check_datapack_schema.py checks those
    // tables against vanilla's own examples from client.jar before shipping.
    // ---------------------------------------------------------------------
    public static final Block CITY_CRATE = block("city_crate",
            BlockBehaviour.Properties.of().strength(1.6F).sound(SoundType.WOOD));
    public static final Block SUPPLY_CRATE = block("supply_crate",
            BlockBehaviour.Properties.of().strength(2.0F, 4.0F).sound(SoundType.WOOD));
    public static final Block VAULT_CRATE = block("vault_crate",
            BlockBehaviour.Properties.of().strength(4.0F, 20.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    // ---------------------------------------------------------------------
    // Items: materials
    // ---------------------------------------------------------------------
    public static final Item RIFT_SHARD = item("rift_shard", item(p -> p.rarity(Rarity.UNCOMMON)));
    public static final Item VOID_THREAD = item("void_thread", item(p -> p));
    public static final Item DECAYED_BONE = item("decayed_bone", item(p -> p));
    public static final Item DECAYED_STEEL_INGOT = item("decayed_steel_ingot", item(p -> p));
    public static final Item STORM_HEART_SHARD = item("storm_heart_shard",
            item(p -> p.rarity(Rarity.RARE)));
    public static final Item GLITCH_ECHO = item("glitch_echo", item(p -> p.rarity(Rarity.RARE)));
    public static final Item MEMORY_FRAGMENT = item("memory_fragment",
            glinted(item(p -> p.rarity(Rarity.RARE))));
    public static final Item HALLUCINATION_DUST = item("hallucination_dust", item(p -> p));
    public static final Item CITY_KEYCARD = item("city_keycard", item(p -> p.rarity(Rarity.UNCOMMON)));
    public static final Item GLYPH_CELL = item("glyph_cell", glinted(item(p -> p.rarity(Rarity.RARE))));
    public static final Item CREATOR_FRAGMENT = item("creator_fragment",
            glinted(item(p -> p.rarity(Rarity.EPIC))));
    public static final Item ABYSS_ORB = item("abyss_orb", glinted(item(p -> p.rarity(Rarity.EPIC))));
    public static final Item TENTACLE_HOOK = item("tentacle_hook", item(p -> p.rarity(Rarity.UNCOMMON)));
    /**
     * BUILD #459 -- the two worlds' own materials and keys. "own blocks, items,
     * mobs, locks, VFX": the void shard and Adams amber are what those worlds are
     * made of in the inventory, and the sigils are what their locks take. Found
     * only in that world's own salvage, never handed out by the terminal.
     */
    public static final Item VOID_SHARD = item("void_shard", glinted(item(p -> p.rarity(Rarity.UNCOMMON))));
    public static final Item ADAMS_AMBER = item("adams_amber", item(p -> p.rarity(Rarity.UNCOMMON)));
    public static final Item VOID_SIGIL = item("void_sigil", glinted(item(p -> p.rarity(Rarity.RARE))));
    public static final Item ADAMS_SIGIL = item("adams_sigil", glinted(item(p -> p.rarity(Rarity.RARE))));
    /** BUILD #462 -- the reach's key. Found only in the reach (its reliquary). */
    public static final Item CREATOR_SIGIL = item("creator_sigil", glinted(item(p -> p.rarity(Rarity.RARE))));

    // ---------------------------------------------------------------------
    // Items: the expansion's materials (BUILD #465)
    // ---------------------------------------------------------------------
    // One per world plus the storm's own, each named for what it is made of, so a
    // player who finds a city gear knows which world they are standing in.
    public static final Item CITY_GEAR = item("city_gear", item(p -> p.rarity(Rarity.UNCOMMON)));
    public static final Item VOID_CORD = item("void_cord", item(p -> p.rarity(Rarity.UNCOMMON)));
    public static final Item ADAMS_GLASS_SHARD = item("adams_glass_shard",
            item(p -> p.rarity(Rarity.UNCOMMON)));
    public static final Item CREATOR_DUST = item("creator_dust",
            glinted(item(p -> p.rarity(Rarity.RARE))));
    public static final Item DECAYED_ASH_CLUMP = item("decayed_ash_clump", item(p -> p));
    public static final Item STORM_MARROW = item("storm_marrow", item(p -> p.rarity(Rarity.UNCOMMON)));

    // ---------------------------------------------------------------------
    // Items: the tools and weapons the storyline hands out
    // ---------------------------------------------------------------------
    public static final Item RIFT_KEY = item("rift_key", glinted(item(p -> p.stacksTo(1).rarity(Rarity.EPIC))));
    // BUILD #466 -- these four were already weapons to look at (hand-held models,
    // epic rarity, the storm's own names) and plain Items to hold: they did the
    // damage of a bare hand. They are real tools now. The API for that in this
    // version is Item.Properties -- sword()/axe()/pickaxe() take a ToolMaterial
    // and the damage/speed numbers, which is how vanilla items are built here:
    // SwordItem and Tier (the classes the earlier builds were written against)
    // come back "class not found" from the runner, and ToolMaterial is the
    // material record instead (ci-out/run-581/vanilla-api.txt).
    public static final Item REALITY_RIPPER = item("reality_ripper",
            glinted(props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant()
                    .axe(ToolMaterial.DIAMOND, 6.0F, -3.0F))));
    public static final Item WITHERED_BLADE = item("withered_blade",
            props -> new Item(props.stacksTo(1).rarity(Rarity.RARE)
                    .sword(ToolMaterial.DIAMOND, 4.5F, -2.4F)));
    // The spear property exists in this version (Item.Properties.spear) but takes
    // nine floats whose meaning is not documented anywhere this build can read,
    // and a weapon that swings wrong is worse than one that swings like a sword:
    // the storm spear is a sword's behaviour on the spear's model until a run
    // dumps a vanilla user of that call to copy.
    public static final Item STORM_SPEAR = item("storm_spear",
            props -> new Item(props.stacksTo(1).rarity(Rarity.RARE)
                    .sword(ToolMaterial.NETHERITE, 5.0F, -2.6F)));
    public static final Item CREATORS_JUDGEMENT = item("creators_judgement",
            glinted(props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant()
                    .sword(ToolMaterial.NETHERITE, 7.0F, -2.6F))));

    // ---------------------------------------------------------------------
    // BUILD #466 -- the weapons pass: phase (e)'s other half. The brief was
    // "have new trap doors, doors, items, even some weapons would be a really
    // cool expansion" (#465 shipped the doors and the items). Every world gets a
    // blade and a tool it could have made itself, out of its own materials, at
    // its own strength: the decayed reality's rusted steel, the void's green
    // shards, Adams amber and glass, the Creator's gold. Nothing is shared and
    // nothing is borrowed -- the same rule the blocks and the mobs follow.
    // ---------------------------------------------------------------------
    public static final Item DECAYED_BLADE = item("decayed_blade",
            props -> new Item(props.rarity(Rarity.UNCOMMON)
                    .sword(ToolMaterial.IRON, 4.0F, -2.2F)));
    public static final Item DECAYED_CLEAVER = item("decayed_cleaver",
            props -> new Item(props.rarity(Rarity.UNCOMMON)
                    .axe(ToolMaterial.IRON, 7.0F, -3.2F)));
    public static final Item VOID_EDGE = item("void_edge",
            glinted(props -> new Item(props.rarity(Rarity.RARE)
                    .sword(ToolMaterial.DIAMOND, 5.0F, -2.4F))));
    public static final Item VOID_RIPPER = item("void_ripper",
            props -> new Item(props.rarity(Rarity.RARE)
                    .pickaxe(ToolMaterial.DIAMOND, 2.5F, -2.8F)));
    public static final Item ADAMS_GLAIVE = item("adams_glaive",
            props -> new Item(props.rarity(Rarity.UNCOMMON)
                    .sword(ToolMaterial.DIAMOND, 5.5F, -2.5F)));
    public static final Item ADAMS_MIRROR_AXE = item("adams_mirror_axe",
            props -> new Item(props.rarity(Rarity.UNCOMMON)
                    .axe(ToolMaterial.DIAMOND, 7.5F, -3.1F)));
    public static final Item CREATOR_EDICT = item("creator_edict",
            glinted(props -> new Item(props.rarity(Rarity.EPIC).fireResistant()
                    .sword(ToolMaterial.NETHERITE, 6.5F, -2.5F))));
    public static final Item CREATOR_HAMMER = item("creator_hammer",
            glinted(props -> new Item(props.rarity(Rarity.EPIC).fireResistant()
                    .pickaxe(ToolMaterial.NETHERITE, 3.5F, -2.9F))));
    public static final Item ECHO_TOTEM = item("echo_totem", item(p -> p.stacksTo(1).rarity(Rarity.RARE)));
    /**
     * THE VOID RUDDER -- BUILD #481. A tool, not a weapon: it bites only in the
     * void's air, where it multiplies the tier's own current ({@link McsmVoidTiers})
     * so a fall can be steered instead of endured. The physics are the player's own
     * (see the client's rudder); what is registered here is the thing you hold.
     */
    public static final Item VOID_RUDDER = item("void_rudder",
            glinted(props -> new McsmVoidRudderItem(props.stacksTo(1).rarity(Rarity.EPIC)
                    .fireResistant())));

    // ---------------------------------------------------------------------
    // Items: the guide book and THE ANTENNA (D.8, phase 5)
    // ---------------------------------------------------------------------
    /**
     * THE FIELD GUIDE. Every world this mod generates is a maze of buildings,
     * rifts and one-way doors, and the mod said nothing about any of it in game.
     * This is the book that does: it opens a terminal page with the state of the
     * world the player is standing in -- the storm's band, the nearest city, the
     * dimension they are in, the ladder rung they are on -- and it is also where
     * the antenna's findings are written down.
     */
    public static final Item GUIDE_BOOK = item("guide_book",
            props -> new McsmTerminalItem(props.stacksTo(1).rarity(Rarity.UNCOMMON), "guide"));
    /**
     * THE ANTENNA, and the terminal behind it. The user's design: an item that
     * "you can hear radio signals" while it is held, and that opens a gigantic
     * restricted console when used -- which asks for the admin password. The
     * password is in the world, in one place, in the hands of one character (see
     * McsmTerminal), and the terminal behind it is where the story is told.
     */
    public static final Item ANTENNA = item("antenna",
            props -> new McsmTerminalItem(props.stacksTo(1).rarity(Rarity.RARE), "terminal"));

    private McsmContent() {
    }

    // ---------------------------------------------------------------------
    // Stairs / doors / trap doors need a subclass
    // ---------------------------------------------------------------------
    // The vanilla constructors are PROTECTED in this Minecraft version (proven
    // by the compiler in CI run 487: "StairBlock(BlockState,Properties) has
    // protected access", and the same for DoorBlock/TrapDoorBlock with a
    // BlockSetType). A protected constructor is callable from a subclass in any
    // package, which is exactly how vanilla itself exposes its own stairs,
    // doors and trap doors -- so these three thin subclasses are the supported
    // way in, not a workaround.
    public static class ExposedStair extends StairBlock {
        public ExposedStair(BlockState base, BlockBehaviour.Properties props) {
            super(base, props);
        }
    }

    public static class ExposedDoor extends DoorBlock {
        public ExposedDoor(BlockSetType type, BlockBehaviour.Properties props) {
            super(type, props);
        }
    }

    public static class ExposedTrapDoor extends TrapDoorBlock {
        public ExposedTrapDoor(BlockSetType type, BlockBehaviour.Properties props) {
            super(type, props);
        }
    }

    /**
     * BUILD #459 -- A DIMENSION'S LOCK. The block itself is dumb on purpose: it
     * knows which world it belongs to and hands the interaction straight to
     * {@link McsmLocks}, which owns the key table and the refusal line. The hook is
     * {@code useItemOn}, the same override the mod's own storm beacon block has
     * compiled with since it was written, and the protected constructor problem
     * does not arise here -- the constructor is this class's own.
     */
    public static class DimensionLock extends Block {
        private final String dimension;

        public DimensionLock(String dimension, BlockBehaviour.Properties props) {
            super(props);
            this.dimension = dimension;
        }

        /** The world this seal belongs to ({@link McsmIdentity}). */
        public String dimension() {
            return this.dimension;
        }

        @Override
        protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
            return McsmLocks.use(this.dimension, level, pos, player);
        }
    }

    // ---------------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------------

    /** Class-init trigger; the static fields above are the registration. */
    public static void register() {
        // no-op: touching the class initialises it
    }

    /**
     * Our own creative tab, on the VANILLA builder.
     *
     * The first attempt copied the base mod's own idiom and named
     * net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab -- which is
     * not on this overlay's compile classpath at all (CI run 487: "package ...
     * does not exist"; build.sh probes the compile path every run now).
     *
     * The vanilla API is, but only up to a point, which the compiler said
     * precisely (run 489): "Output has protected access in CreativeModeTab".
     * CreativeModeTab.builder(Row, int), Builder.title/icon/build are public --
     * the classes are even in the run's API dump -- while the generator type and
     * its Output parameter are protected nested types, so the lambda cannot be
     * written here. The generator is therefore a Proxy that implements the
     * interface without naming it, and the output it is handed is filled
     * reflectively. Same result, no Fabric module, and no compile-time
     * dependency on a protected type.
     */
    public static void registerTab() {
        try {
            CreativeModeTab.Builder builder = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.mcsm.content"))
                    .icon(() -> new ItemStack(RIFT_KEY));
            Class<?> generatorType = Class.forName(
                    "net.minecraft.world.item.CreativeModeTab$DisplayItemsGenerator");
            Object generator = java.lang.reflect.Proxy.newProxyInstance(
                    McsmContent.class.getClassLoader(),
                    new Class<?>[]{generatorType},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "hashCode":
                                return 0;
                            case "equals":
                                return proxy == (args == null ? null : args[0]);
                            case "toString":
                                return "mcsm-content-tab-generator";
                            default:
                                break;
                        }
                        if (args != null && args.length == 2 && args[1] != null) {
                            feedTab(args[1]);
                        }
                        return null;
                    });
            for (java.lang.reflect.Method m : CreativeModeTab.Builder.class.getMethods()) {
                if (m.getName().equals("displayItems") && m.getParameterCount() == 1) {
                    m.invoke(builder, generator);
                    break;
                }
            }
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MAIN_TAB, builder.build());
            System.out.println("[ds] mcsm content tab registered (" + ALL_BLOCKS.size()
                    + " blocks, " + (ALL_ITEMS.size() + ALL_BLOCK_ITEMS.size()) + " items)");
        } catch (Throwable t) {
            System.err.println("[ds] mcsm content tab unavailable (" + t
                    + ") -- every block and item is still craftable / minable in the decayed reality");
        }
    }

    /**
     * Feeds our stacks to the tab's output object. The one-arg accept overload is
     * chosen by what it takes: entries.accept(ItemStack) for a stack parameter,
     * accept(ItemLike) for an item parameter (an ItemStack is not an ItemLike).
     */
    private static void feedTab(Object output) {
        try {
            java.lang.reflect.Method accept = null;
            boolean wantsItem = false;
            for (java.lang.reflect.Method m : output.getClass().getMethods()) {
                if (!m.getName().equals("accept") || m.getParameterCount() != 1) {
                    continue;
                }
                Class<?> p = m.getParameterTypes()[0];
                if (p.isAssignableFrom(ItemStack.class)) {
                    accept = m;
                    wantsItem = false;
                    break;
                }
                if (p.isAssignableFrom(Item.class)) {
                    accept = m;
                    wantsItem = true;
                    break;
                }
            }
            if (accept == null) {
                return;
            }
            accept.setAccessible(true);
            for (Item item : ALL_BLOCK_ITEMS) {
                accept.invoke(output, wantsItem ? item : new ItemStack(item));
            }
            for (Item item : ALL_ITEMS) {
                accept.invoke(output, wantsItem ? item : new ItemStack(item));
            }
        } catch (Throwable t) {
            System.err.println("[ds] mcsm content tab fill failed: " + t);
        }
    }

    public static final ResourceKey<CreativeModeTab> MAIN_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("mcsm", "content"));

    // ---------------------------------------------------------------------
    // Helpers (same shape as the base mod's own register(...) helpers)
    // ---------------------------------------------------------------------

    private static Block block(String name, BlockBehaviour.Properties props) {
        return block(name, Block::new, props);
    }

    private static <T extends Block> T block(String name, Function<BlockBehaviour.Properties, T> factory,
                                             BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath("mcsm", name));
        T b;
        try {
            b = factory.apply(props.setId(key));
            Registry.register(BuiltInRegistries.BLOCK, key, b);
        } catch (Throwable t) {
            System.err.println("[ds] block mcsm:" + name + " not registered: " + t);
            return null;
        }
        if (b == null) {
            return null;
        }
        ALL_BLOCKS.add(b);
        // every block gets its block item, so it is placeable and in the tab
        try {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath("mcsm", name));
            Item it = new BlockItem(b, new Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, it);
            ALL_BLOCK_ITEMS.add(it);
        } catch (Throwable t) {
            System.err.println("[ds] block item mcsm:" + name + " not registered: " + t);
        }
        return b;
    }

    private static <T extends Item> T item(String name, Function<Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("mcsm", name));
        T it;
        try {
            it = factory.apply(new Properties().setId(key));
            Registry.register(BuiltInRegistries.ITEM, key, it);
        } catch (Throwable t) {
            System.err.println("[ds] item mcsm:" + name + " not registered: " + t);
            return null;
        }
        ALL_ITEMS.add(it);
        return it;
    }

    private static Function<Properties, Item> item(Function<Properties, Properties> tune) {
        return props -> new Item(tune.apply(props));
    }

    /** The base mod's own glint idiom (ModItems uses ENCHANTMENT_GLINT_OVERRIDE). */
    private static Function<Properties, Item> glinted(Function<Properties, Item> inner) {
        return props -> inner.apply(props.component(
                net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    }

    private static BlockBehaviour.Properties stone(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().strength(hardness, resistance)
                .sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    /**
     * A lock's properties: metal, hard enough to be a door rather than scenery, and
     * lit -- in the world's own accent colour, because the block's texture is
     * painted from the same identity hexes (ci/make_mcsm_textures.py).
     */
    private static BlockBehaviour.Properties lock(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().strength(hardness, resistance)
                .sound(SoundType.METAL).lightLevel(s -> 6)
                .emissiveRendering(s -> true).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties wood(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().strength(hardness, resistance)
                .sound(SoundType.WOOD).ignitedByLava();
    }
}
