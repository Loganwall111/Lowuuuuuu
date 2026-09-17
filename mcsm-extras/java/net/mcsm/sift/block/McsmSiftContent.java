package net.mcsm.sift.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * V2 - 30 new blocks/items for all dimensions + terrain parts per dimension
 * - Void dimension gets own unique blocks (no reuse)
 * - Sift tiers each get own terrain
 * - Black hole backdrop blocks
 */
public final class McsmSiftContent {

    public static final List<Block> ALL_BLOCKS = new ArrayList<>();
    public static final List<Item> ALL_ITEMS = new ArrayList<>();
    public static final List<Item> ALL_BLOCK_ITEMS = new ArrayList<>();

    // VOID DIMENSION TERRAIN - unique, not decayed reuse
    public static final Block VOID_CRYSTAL = block("void_crystal",
        BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).strength(1.5f, 6f).sound(SoundType.AMETHYST).lightLevel(s -> 12).emissiveRendering((s, l, p) -> true));
    public static final Block VOID_ESSENCE_BLOCK = block("void_essence_block",
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(0.8f, 3f).sound(SoundType.GLASS).lightLevel(s -> 10).noOcclusion());
    public static final Block STARLIT_VOID_STONE = block("starlit_void_stone",
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).strength(2.5f, 8f).lightLevel(s -> 4).sound(SoundType.DEEPSLATE));
    public static final Block PRISMATIC_CRYSTAL = block("prismatic_crystal",
        BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER).strength(1f).lightLevel(s -> 14).sound(SoundType.AMETHYST_CLUSTER).noOcclusion());
    public static final Block NEBULA_BLOCK = block("nebula_block",
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(1f, 5f).lightLevel(s -> 11).sound(SoundType.GLASS).noOcclusion());
    public static final Block VOID_FIBER = block("void_fiber",
        BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).strength(0.8f).lightLevel(s -> 6).sound(SoundType.SCULK));
    public static final Block VOID_SPINE = block("void_spine",
        BlockBehaviour.Properties.ofFullCopy(Blocks.BONE_BLOCK).strength(1.5f, 4f).sound(SoundType.BONE_BLOCK).lightLevel(s -> 2));

    // SIFT TIER 1 - GEL HORIZON - floating, god rays, blue/pink grass
    public static final Block SIFT_SPIRE = block("sift_spire",
        BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).strength(1.2f, 6f).lightLevel(s -> 9).sound(SoundType.AMETHYST));
    public static final Block FLOATING_SIFT_STONE = block("floating_sift_stone",
        BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(1.5f, 6f).lightLevel(s -> 3).sound(SoundType.STONE));
    public static final Block GEL_HORIZON_GLASS = block("gel_horizon_glass",
        BlockBehaviour.Properties.ofFullCopy(Blocks.TINTED_GLASS).strength(0.5f).lightLevel(s -> 8).noOcclusion());
    public static final Block LUMEN_MOSS = block("lumen_moss",
        BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK).strength(0.2f).lightLevel(s -> 7).sound(SoundType.MOSS));

    // SIFT TIER 2 - MENGER MAZE - orange to pink emissive + starry sponge
    public static final Block MAZE_ORANGE_CORE = block("maze_orange_core",
        BlockBehaviour.Properties.ofFullCopy(Blocks.ORANGE_CONCRETE).strength(2f, 8f).lightLevel(s -> 12).sound(SoundType.STONE));
    public static final Block MAZE_PINK_SHELL = block("maze_pink_shell",
        BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_CONCRETE).strength(2f, 8f).lightLevel(s -> 10).sound(SoundType.STONE));
    public static final Block MAZE_VOID_BRICKS = block("maze_void_bricks",
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS).strength(2.2f, 7f).lightLevel(s -> 1).sound(SoundType.DEEPSLATE_BRICKS));
    public static final Block FRACTAL_SPONGE = block("fractal_sponge",
        BlockBehaviour.Properties.ofFullCopy(Blocks.SPONGE).strength(1f, 5f).lightLevel(s -> 6).sound(SoundType.SPONGE));

    // SIFT TIER 3 - RIFT FIELD - dark purple neon rifts
    public static final Block RIFT_STONE = block("rift_stone",
        BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).strength(3f, 12f).lightLevel(s -> 5).sound(SoundType.DEEPSLATE));
    public static final Block RIFT_GLASS = block("rift_glass",
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(0.5f).lightLevel(s -> 13).sound(SoundType.GLASS).noOcclusion());
    public static final Block NEON_RIFT_VEIN = block("neon_rift_vein",
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLOWSTONE).strength(0.8f).lightLevel(s -> 15).sound(SoundType.GLASS));
    public static final Block COSMIC_WINDOW = block("cosmic_window",
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(0.6f).lightLevel(s -> 8).noOcclusion());

    // SIFT TIER 4 - DISPLACEMENT - rainbow wavy bands
    public static final Block DISPLACEMENT_CRYSTAL = block("displacement_crystal",
        BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE).strength(1.8f, 8f).lightLevel(s -> 11).sound(SoundType.AMETHYST));
    public static final Block WAVY_SPACETIME = block("wavy_spacetime",
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(0.7f).lightLevel(s -> 9).noOcclusion());
    public static final Block RAINBOW_BAND = block("rainbow_band",
        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE).strength(1.2f).lightLevel(s -> 12).sound(SoundType.STONE));

    // SIFT TIER 5 - IRIDESCENT GEL - rainbow water pools
    public static final Block GEL_CRYSTAL = block("gel_crystal",
        BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).strength(1f, 5f).lightLevel(s -> 13).sound(SoundType.GLASS));
    public static final Block IRIDESCENT_SAND = block("iridescent_sand",
        BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).strength(0.5f).lightLevel(s -> 6).sound(SoundType.SAND));
    public static final Block BLACK_WATER_CRYSTAL = block("black_water_crystal",
        BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).strength(2f, 10f).lightLevel(s -> 3).sound(SoundType.AMETHYST));
    public static final Block GREEN_ACID_CRYSTAL = block("green_acid_crystal",
        BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK).strength(1.5f).lightLevel(s -> 9).sound(SoundType.AMETHYST));

    // BOTTOM FABRIC + UNKNOWN - purple pink brown rainbow, bouncy
    public static final Block FABRIC_SHARD = block("fabric_shard",
        BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).strength(2f, 10f).lightLevel(s -> 10).sound(SoundType.AMETHYST));
    public static final Block UNKNOWN_DECAY = block("unknown_decay",
        BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).strength(1f).lightLevel(s -> 4).sound(SoundType.SCULK));
    public static final Block BOUNCY_VOID_MATTER = block("bouncy_void_matter",
        BlockBehaviour.Properties.ofFullCopy(Blocks.SLIME_BLOCK).strength(0.8f).lightLevel(s -> 5).sound(SoundType.SLIME_BLOCK).noOcclusion());

    // BLACK HOLE BACKDROP - dynamic skybox blocks
    public static final Block BLACK_HOLE_FRAME = block("black_hole_frame",
        BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).strength(5f, 1200f).lightLevel(s -> 2).sound(SoundType.METAL));
    public static final Block EVENT_HORIZON = block("event_horizon",
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CONCRETE).strength(-1f, 3600000f).lightLevel(s -> 1).sound(SoundType.GLASS).noOcclusion());
    public static final Block SINGULARITY_SHARD = block("singularity_shard",
        BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).strength(3f, 20f).lightLevel(s -> 15).sound(SoundType.AMETHYST).emissiveRendering((s,l,p)->true));
    public static final Block LENSED_STARLIGHT = block("lensed_starlight",
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(0.3f).lightLevel(s -> 14).noOcclusion().sound(SoundType.GLASS));

    // ITEMS - 30 new items part of blocks already, plus extra items
    public static final Item VOID_HEART = item("void_heart", props -> new Item(props.fireResistant()));
    public static final Item PRISMATIC_SHARD = item("prismatic_shard", props -> new Item(props));
    public static final Item BLACK_HOLE_ESSENCE = item("black_hole_essence", props -> new Item(props.fireResistant().component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final Item SIFT_FIBER = item("sift_fiber", props -> new Item(props));
    public static final Item NEBULA_DUST = item("nebula_dust", props -> new Item(props));
    public static final Item RIFT_KEY = item("rift_key", props -> new Item(props.fireResistant()));
    public static final Item VOID_JELLY_TENTACLE = item("void_jelly_tentacle", props -> new Item(props));
    public static final Item CRYSTAL_MANTA_SCALE = item("crystal_manta_scale", props -> new Item(props));

    private McsmSiftContent() {}

    public static void register() {
        System.out.println("[sift V2] Registered " + ALL_BLOCKS.size() + " new blocks, " + (ALL_ITEMS.size() + ALL_BLOCK_ITEMS.size()) + " items - terrain parts per dimension");
    }

    private static Block block(String name, BlockBehaviour.Properties props) {
        return block(name, Block::new, props);
    }

    private static <T extends Block> T block(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("mcsm_sift", name));
        T b;
        try {
            b = factory.apply(props.setId(key));
            Registry.register(BuiltInRegistries.BLOCK, key, b);
        } catch (Throwable t) {
            System.err.println("[sift V2] block mcsm_sift:" + name + " not registered: " + t);
            return null;
        }
        if (b == null) return null;
        ALL_BLOCKS.add(b);
        try {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("mcsm_sift", name));
            Item it = new BlockItem(b, new Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, it);
            ALL_BLOCK_ITEMS.add(it);
        } catch (Throwable t) {
            System.err.println("[sift V2] block item mcsm_sift:" + name + " not registered: " + t);
        }
        return b;
    }

    private static <T extends Item> T item(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("mcsm_sift", name));
        T it;
        try {
            it = factory.apply(new Item.Properties().setId(key));
            Registry.register(BuiltInRegistries.ITEM, key, it);
        } catch (Throwable t) {
            System.err.println("[sift V2] item mcsm_sift:" + name + " not registered: " + t);
            return null;
        }
        ALL_ITEMS.add(it);
        return it;
    }
}
