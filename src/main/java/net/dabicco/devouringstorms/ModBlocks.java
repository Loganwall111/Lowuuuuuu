package net.dabicco.devouringstorms;

import java.util.function.Function;
import net.dabicco.devouringstorms.block.FurnaceFilterBlock;
import net.dabicco.devouringstorms.block.SuperTntBlock;
import net.dabicco.devouringstorms.block.WitheredDustBlock;
import net.dabicco.devouringstorms.block.WitheredMushroomBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {
   public static final Block SUPER_TNT;
   public static final Block FURNACE_FILTER;
   private static final float FLESH_FRICTION = 0.72F;
   public static final Block WITHERED_FLESH_BLOCK;
   public static final Block TORN_WITHERED_FLESH;
   public static final Block WITHERED_BEDROCK;
   public static final Block WITHERED_COBBLESTONE;
   public static final Block WITHERED_NETHERBRICK;
   public static final Block WITHERED_SAND;
   public static final Block WITHERED_COBBLESTONE_STAIRS;
   public static final Block WITHERED_COBBLESTONE_SLAB;
   public static final Block WITHERED_COBBLESTONE_WALL;
   public static final Block WITHERED_NETHERBRICK_STAIRS;
   public static final Block WITHERED_NETHERBRICK_SLAB;
   public static final Block WITHERED_NETHERBRICK_WALL;
   public static final Block WITHERED_NETHERBRICK_FENCE;
   private static final SoundType WITHERED_WOOD_SOUND;
   public static final Block STRIPPED_WITHERED_LOG;
   public static final Block WITHERED_PLANKS;
   public static final Block WITHERED_STAIRS;
   public static final Block WITHERED_SLAB;
   public static final Block WITHERED_FENCE;
   public static final Block WITHERED_BUTTON;
   public static final Block STRIPPED_WITHERED_PLANKS;
   public static final Block STRIPPED_WITHERED_STAIRS;
   public static final Block STRIPPED_WITHERED_SLAB;
   public static final Block STRIPPED_WITHERED_FENCE;
   public static final Block STRIPPED_WITHERED_BUTTON;
   public static final Block WITHERED_MUSHROOM;
   public static final Block WITHERED_LOG;
   public static final Block WITHERED_DUST;
   public static final Block WITHERED_STONE;
   public static final Block WITHERED_STONE_STAIRS;
   public static final Block WITHERED_STONE_SLAB;

   public static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties props) {
      ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("devouringstorms", name));
      T block = (T)(factory.apply(props.setId(key)));
      Registry.register(BuiltInRegistries.BLOCK, key, block);
      return block;
   }

   private static BlockBehaviour.Properties stoneProps(SoundType sound) {
      return Properties.of().strength(2.0F, 6.0F).sound(sound).requiresCorrectToolForDrops();
   }

   private static BlockBehaviour.Properties woodProps() {
      return Properties.of().instrument(NoteBlockInstrument.BASS).strength(3.4F, 5.0F).sound(WITHERED_WOOD_SOUND).ignitedByLava();
   }

   private static BlockBehaviour.Properties strippedWoodProps() {
      return Properties.of().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
   }

   public static void initialize() {
   }

   static {
      SUPER_TNT = register("super_tnt", SuperTntBlock::new, Properties.of().instabreak().sound(SoundType.GRASS).ignitedByLava());
      FURNACE_FILTER = register("furnace_filter", FurnaceFilterBlock::new, Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion().lightLevel((s) -> (Boolean)s.getValue(BlockStateProperties.LIT) ? 5 : 0));
      WITHERED_FLESH_BLOCK = register("withered_flesh_block", Block::new, Properties.of().strength(0.8F).sound(SoundType.SLIME_BLOCK).friction(0.72F));
      TORN_WITHERED_FLESH = register("torn_withered_flesh", Block::new, Properties.of().strength(0.8F).sound(SoundType.SLIME_BLOCK).friction(0.72F).lightLevel((s) -> 5).emissiveRendering((state) -> true));
      WITHERED_BEDROCK = register("withered_bedrock", Block::new, Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.STONE).noLootTable());
      WITHERED_COBBLESTONE = register("withered_cobblestone", Block::new, Properties.of().strength(2.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
      WITHERED_NETHERBRICK = register("withered_netherbrick", Block::new, Properties.of().strength(2.0F, 6.0F).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops());
      WITHERED_SAND = register("withered_sand", (props) -> new ColoredFallingBlock(new ColorRGBA(10051214), props), Properties.of().strength(0.5F).sound(SoundType.SAND));
      WITHERED_COBBLESTONE_STAIRS = register("withered_cobblestone_stairs", (props) -> new StairBlock(WITHERED_COBBLESTONE.defaultBlockState(), props), stoneProps(SoundType.STONE));
      WITHERED_COBBLESTONE_SLAB = register("withered_cobblestone_slab", SlabBlock::new, stoneProps(SoundType.STONE));
      WITHERED_COBBLESTONE_WALL = register("withered_cobblestone_wall", WallBlock::new, stoneProps(SoundType.STONE).noOcclusion());
      WITHERED_NETHERBRICK_STAIRS = register("withered_netherbrick_stairs", (props) -> new StairBlock(WITHERED_NETHERBRICK.defaultBlockState(), props), stoneProps(SoundType.NETHER_BRICKS));
      WITHERED_NETHERBRICK_SLAB = register("withered_netherbrick_slab", SlabBlock::new, stoneProps(SoundType.NETHER_BRICKS));
      WITHERED_NETHERBRICK_WALL = register("withered_netherbrick_wall", WallBlock::new, stoneProps(SoundType.NETHER_BRICKS).noOcclusion());
      WITHERED_NETHERBRICK_FENCE = register("withered_netherbrick_fence", FenceBlock::new, stoneProps(SoundType.NETHER_BRICKS).noOcclusion());
      WITHERED_WOOD_SOUND = SoundType.NETHER_WOOD;
      STRIPPED_WITHERED_LOG = register("stripped_withered_log", RotatedPillarBlock::new, Properties.of().instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
      WITHERED_PLANKS = register("withered_planks", Block::new, woodProps());
      WITHERED_STAIRS = register("withered_stairs", (props) -> new StairBlock(WITHERED_PLANKS.defaultBlockState(), props), woodProps());
      WITHERED_SLAB = register("withered_slab", SlabBlock::new, woodProps());
      WITHERED_FENCE = register("withered_fence", FenceBlock::new, woodProps());
      WITHERED_BUTTON = register("withered_button", (props) -> new ButtonBlock(BlockSetType.OAK, 30, props), Properties.of().noCollision().strength(0.85F).sound(WITHERED_WOOD_SOUND));
      STRIPPED_WITHERED_PLANKS = register("stripped_withered_planks", Block::new, strippedWoodProps());
      STRIPPED_WITHERED_STAIRS = register("stripped_withered_stairs", (props) -> new StairBlock(STRIPPED_WITHERED_PLANKS.defaultBlockState(), props), strippedWoodProps());
      STRIPPED_WITHERED_SLAB = register("stripped_withered_slab", SlabBlock::new, strippedWoodProps());
      STRIPPED_WITHERED_FENCE = register("stripped_withered_fence", FenceBlock::new, strippedWoodProps());
      STRIPPED_WITHERED_BUTTON = register("stripped_withered_button", (props) -> new ButtonBlock(BlockSetType.OAK, 30, props), Properties.of().noCollision().strength(0.5F).sound(SoundType.WOOD));
      WITHERED_MUSHROOM = register("withered_mushroom", WitheredMushroomBlock::new, Properties.of().noCollision().instabreak().sound(SoundType.GRASS).lightLevel((s) -> 6).emissiveRendering((state) -> true).pushReaction(PushReaction.DESTROY));
      WITHERED_LOG = register("withered_log", RotatedPillarBlock::new, Properties.of().instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
      WITHERED_DUST = register("withered_dust", WitheredDustBlock::new, Properties.of().noCollision().instabreak().pushReaction(PushReaction.DESTROY));
      WITHERED_STONE = register("withered_stone", Block::new, Properties.of().strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE));
      WITHERED_STONE_STAIRS = register("withered_stone_stairs", (props) -> new StairBlock(WITHERED_STONE.defaultBlockState(), props), Properties.of().strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE));
      WITHERED_STONE_SLAB = register("withered_stone_slab", SlabBlock::new, Properties.of().strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE));
   }
}
