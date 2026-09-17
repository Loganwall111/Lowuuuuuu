package net.mcsm.sift.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Ecosystem blocks - blue and pink grass blocks, red grass, red rock, black water, green acid, purple trees, fluor plants, fungus trees, rude vines, blue bun
 * For skybox panorama - couple jokest creatures and fish-like creatures and blue and pink grass blocks
 * Some plants and fluor plants and fungus trees rude vines blue bun Red rock redstone not redstone but reddish stone Red grass
 * Colossal octopus rainbow colored water with sparkles and sky panorama and sift inspired sky texture for each area
 * Don't forget the plant and black water and green acid and purple trees
 */
public final class SiftEcosystemBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "mcsm_sift");

    // Grass blocks - blue and pink and red grass blocks
    public static final RegistryObject<Block> BLUE_GRASS_BLOCK = BLOCKS.register("blue_grass_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK).strength(0.6f).lightLevel(s -> 2)));

    public static final RegistryObject<Block> PINK_GRASS_BLOCK = BLOCKS.register("pink_grass_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK).strength(0.6f).lightLevel(s -> 3)));

    public static final RegistryObject<Block> RED_GRASS_BLOCK = BLOCKS.register("red_grass_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK).strength(0.6f).lightLevel(s -> 1)));

    // Red rock - reddish stone, not redstone but reddish stone
    public static final RegistryObject<Block> RED_ROCK = BLOCKS.register("red_rock",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).strength(1.5f, 6f).lightLevel(s -> 1)));

    // Black water - black water block (would be fluid in real implementation, using block for simplicity)
    public static final RegistryObject<Block> BLACK_WATER = BLOCKS.register("black_water",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.WATER).strength(100f).lightLevel(s -> 0).noLootTable()));

    // Green acid - green acid block
    public static final RegistryObject<Block> GREEN_ACID = BLOCKS.register("green_acid",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.WATER).strength(100f).lightLevel(s -> 8).noLootTable()));

    // Purple trees - purple tree leaves, logs would be separate
    public static final RegistryObject<Block> PURPLE_TREE_LEAVES = BLOCKS.register("purple_tree_leaves",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).strength(0.2f).lightLevel(s -> 2)));

    public static final RegistryObject<Block> PURPLE_TREE_LOG = BLOCKS.register("purple_tree_log",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).strength(2f)));

    // Fluor plant - glowing fluorescent plant
    public static final RegistryObject<Block> FLUOR_PLANT = BLOCKS.register("fluor_plant",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.FLOWER).strength(0f).lightLevel(s -> 12).noOcclusion()));

    // Fungus trees - fungus tree block
    public static final RegistryObject<Block> FUNGUS_TREE = BLOCKS.register("fungus_tree",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUSHROOM_STEM).strength(0.5f).lightLevel(s -> 4)));

    // Rude vines - red vines, hanging
    public static final RegistryObject<Block> RED_VINES = BLOCKS.register("red_vines",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.VINE).strength(0.2f).lightLevel(s -> 1).noOcclusion()));

    // Blue bun - blue mushroom
    public static final RegistryObject<Block> BLUE_BUN = BLOCKS.register("blue_bun",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.RED_MUSHROOM).strength(0f).lightLevel(s -> 6).noOcclusion()));

    // Rainbow water - rainbow colored water with sparkles
    public static final RegistryObject<Block> RAINBOW_WATER = BLOCKS.register("rainbow_water",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.WATER).strength(100f).lightLevel(s -> 10).noLootTable()));

    private SiftEcosystemBlocks() {}
}
