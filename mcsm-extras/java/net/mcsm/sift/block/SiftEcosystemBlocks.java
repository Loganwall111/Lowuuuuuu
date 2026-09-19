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
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Ecosystem blocks - blue and pink grass blocks, red grass, red rock, black water, green acid, purple trees, fluor plants, fungus trees, rude vines, blue bun
 * Fabric version - uses BuiltInRegistries like McsmContent does
 */
public final class SiftEcosystemBlocks {

    public static final List<Block> ALL_BLOCKS = new ArrayList<>();
    public static final List<Item> ALL_BLOCK_ITEMS = new ArrayList<>();

    public static final Block BLUE_GRASS_BLOCK = block("blue_grass_block",
        BlockBehaviour.Properties.of().strength(0.6f).lightLevel(s -> 2));
    public static final Block PINK_GRASS_BLOCK = block("pink_grass_block",
        BlockBehaviour.Properties.of().strength(0.6f).lightLevel(s -> 3));
    public static final Block RED_GRASS_BLOCK = block("red_grass_block",
        BlockBehaviour.Properties.of().strength(0.6f).lightLevel(s -> 1));
    public static final Block RED_ROCK = block("red_rock",
        BlockBehaviour.Properties.of().strength(1.5f, 6f).lightLevel(s -> 1));
    public static final Block BLACK_WATER = block("black_water",
        BlockBehaviour.Properties.of().strength(100f).lightLevel(s -> 0));
    public static final Block GREEN_ACID = block("green_acid",
        BlockBehaviour.Properties.of().strength(100f).lightLevel(s -> 8));
    public static final Block PURPLE_TREE_LEAVES = block("purple_tree_leaves",
        BlockBehaviour.Properties.of().strength(0.2f).lightLevel(s -> 2));
    public static final Block PURPLE_TREE_LOG = block("purple_tree_log",
        BlockBehaviour.Properties.of().strength(2f));
    public static final Block FLUOR_PLANT = block("fluor_plant",
        BlockBehaviour.Properties.of().strength(0f).lightLevel(s -> 12).noOcclusion());
    public static final Block FUNGUS_TREE = block("fungus_tree",
        BlockBehaviour.Properties.of().strength(0.5f).lightLevel(s -> 4));
    public static final Block RED_VINES = block("red_vines",
        BlockBehaviour.Properties.of().strength(0.2f).lightLevel(s -> 1).noOcclusion());
    public static final Block BLUE_BUN = block("blue_bun",
        BlockBehaviour.Properties.of().strength(0f).lightLevel(s -> 6).noOcclusion());
    public static final Block RAINBOW_WATER = block("rainbow_water",
        BlockBehaviour.Properties.of().strength(100f).lightLevel(s -> 10));

    // V2 - 30 NEW BLOCKS for Sift dimension per user request
    public static final Block CYAN_MOSS = block("cyan_moss",
        BlockBehaviour.Properties.of().strength(0.2f).lightLevel(s -> 3).noOcclusion());
    public static final Block VOID_LILY = block("void_lily",
        BlockBehaviour.Properties.of().strength(0f).lightLevel(s -> 8).noOcclusion());
    public static final Block GEL_CRYSTAL = block("gel_crystal",
        BlockBehaviour.Properties.of().strength(0.8f).lightLevel(s -> 14));
    public static final Block SPONGE_BLOOM = block("sponge_bloom",
        BlockBehaviour.Properties.of().strength(0.4f).lightLevel(s -> 6));
    public static final Block RIFT_GLASS = block("rift_glass",
        BlockBehaviour.Properties.of().strength(0.3f).lightLevel(s -> 10).noOcclusion());
    public static final Block PRISMATIC_STONE = block("prismatic_stone",
        BlockBehaviour.Properties.of().strength(1.8f).lightLevel(s -> 5));
    public static final Block LUMINOUS_VINE = block("luminous_vine",
        BlockBehaviour.Properties.of().strength(0.1f).lightLevel(s -> 9).noOcclusion());
    public static final Block FABRIC_SHARD = block("fabric_shard",
        BlockBehaviour.Properties.of().strength(1.0f).lightLevel(s -> 12));
    public static final Block ECHO_SOIL = block("echo_soil",
        BlockBehaviour.Properties.of().strength(0.5f).lightLevel(s -> 1));
    public static final Block STARLIT_GRASS = block("starlit_grass",
        BlockBehaviour.Properties.of().strength(0.6f).lightLevel(s -> 4));
    public static final Block VOID_BLOSSOM = block("void_blossom",
        BlockBehaviour.Properties.of().strength(0f).lightLevel(s -> 11).noOcclusion());
    public static final Block GEL_HONEY = block("gel_honey",
        BlockBehaviour.Properties.of().strength(0.3f).lightLevel(s -> 2));
    public static final Block CRYSTALLINE_SPONGE = block("crystalline_sponge",
        BlockBehaviour.Properties.of().strength(0.7f).lightLevel(s -> 7));
    public static final Block RIFT_BLOOM = block("rift_bloom",
        BlockBehaviour.Properties.of().strength(0f).lightLevel(s -> 13).noOcclusion());
    public static final Block DISPLACEMENT_STONE = block("displacement_stone",
        BlockBehaviour.Properties.of().strength(2.0f).lightLevel(s -> 3));
    public static final Block IRIDESCENT_LEAVES = block("iridescent_leaves",
        BlockBehaviour.Properties.of().strength(0.2f).lightLevel(s -> 5));
    public static final Block IRIDESCENT_LOG = block("iridescent_log",
        BlockBehaviour.Properties.of().strength(2f).lightLevel(s -> 2));
    public static final Block VOID_FERN = block("void_fern",
        BlockBehaviour.Properties.of().strength(0f).lightLevel(s -> 6).noOcclusion());
    public static final Block GLOWING_MUSHROOM = block("glowing_mushroom",
        BlockBehaviour.Properties.of().strength(0f).lightLevel(s -> 12).noOcclusion());
    public static final Block FABRIC_ROOTS = block("fabric_roots",
        BlockBehaviour.Properties.of().strength(0.3f).lightLevel(s -> 3).noOcclusion());
    public static final Block COSMIC_SAND = block("cosmic_sand",
        BlockBehaviour.Properties.of().strength(0.5f).lightLevel(s -> 1));
    public static final Block STAR_DUST = block("star_dust",
        BlockBehaviour.Properties.of().strength(0.2f).lightLevel(s -> 9).noOcclusion());
    public static final Block VOID_CRYSTAL_CLUSTER = block("void_crystal_cluster",
        BlockBehaviour.Properties.of().strength(1.2f).lightLevel(s -> 15));
    public static final Block GEL_LANTERN = block("gel_lantern",
        BlockBehaviour.Properties.of().strength(0.8f).lightLevel(s -> 15));
    public static final Block RIFT_VEIN = block("rift_vein",
        BlockBehaviour.Properties.of().strength(0.6f).lightLevel(s -> 8));
    public static final Block PRISMATIC_VINE = block("prismatic_vine",
        BlockBehaviour.Properties.of().strength(0.1f).lightLevel(s -> 7).noOcclusion());
    public static final Block VOID_BERRY_BUSH = block("void_berry_bush",
        BlockBehaviour.Properties.of().strength(0.3f).lightLevel(s -> 4));
    public static final Block ECHO_CRYSTAL = block("echo_crystal",
        BlockBehaviour.Properties.of().strength(1.0f).lightLevel(s -> 13));
    public static final Block FABRIC_BLOOM = block("fabric_bloom",
        BlockBehaviour.Properties.of().strength(0f).lightLevel(s -> 10).noOcclusion());
    public static final Block COSMIC_GRASS = block("cosmic_grass",
        BlockBehaviour.Properties.of().strength(0.6f).lightLevel(s -> 5));

    private SiftEcosystemBlocks() {}

    public static void register() {}

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
            System.err.println("[sift] block mcsm_sift:" + name + " not registered: " + t);
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
            System.err.println("[sift] block item mcsm_sift:" + name + " not registered: " + t);
        }
        return b;
    }
}
