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
