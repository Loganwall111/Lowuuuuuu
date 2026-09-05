package net.dabicco.witherstormmod;

import java.util.function.Function;
import net.dabicco.witherstormmod.block.FurnaceFilterBlock;
import net.dabicco.witherstormmod.block.SuperTntBlock;
import net.dabicco.witherstormmod.block.WitheredDustBlock;
import net.dabicco.witherstormmod.block.WitheredMushroomBlock;
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
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {
   public static final Block SUPER_TNT = register("super_tnt", SuperTntBlock::new, Properties.of().instabreak().sound(SoundType.GRASS).ignitedByLava());
   public static final Block FURNACE_FILTER = register(
      "furnace_filter",
      FurnaceFilterBlock::new,
      Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion().lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 5 : 0)
   );
   private static final float FLESH_FRICTION = 0.72F;
   public static final Block WITHERED_FLESH_BLOCK = register(
      "withered_flesh_block", Block::new, Properties.of().strength(0.8F).sound(SoundType.SLIME_BLOCK).friction(0.72F)
   );
   public static final Block TORN_WITHERED_FLESH = register(
      "torn_withered_flesh",
      Block::new,
      Properties.of().strength(0.8F).sound(SoundType.SLIME_BLOCK).friction(0.72F).lightLevel(s -> 5).emissiveRendering(state -> true)
   );
   public static final Block WITHERED_BEDROCK = register(
      "withered_bedrock", Block::new, Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.STONE).noLootTable()
   );
   public static final Block WITHERED_COBBLESTONE = register(
      "withered_cobblestone", Block::new, Properties.of().strength(2.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
   );
   public static final Block WITHERED_NETHERBRICK = register(
      "withered_netherbrick", Block::new, Properties.of().strength(2.0F, 6.0F).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops()
   );
   public static final Block WITHERED_SAND = register(
      "withered_sand", props -> new ColoredFallingBlock(new ColorRGBA(10051214), props), Properties.of().strength(0.5F).sound(SoundType.SAND)
   );
   public static final Block WITHERED_COBBLESTONE_STAIRS = register(
      "withered_cobblestone_stairs", props -> new StairBlock(WITHERED_COBBLESTONE.defaultBlockState(), props) {}, stoneProps(SoundType.STONE)
   );
   public static final Block WITHERED_COBBLESTONE_SLAB = register("withered_cobblestone_slab", SlabBlock::new, stoneProps(SoundType.STONE));
   public static final Block WITHERED_COBBLESTONE_WALL = register("withered_cobblestone_wall", WallBlock::new, stoneProps(SoundType.STONE).noOcclusion());
   public static final Block WITHERED_NETHERBRICK_STAIRS = register(
      "withered_netherbrick_stairs", props -> new StairBlock(WITHERED_NETHERBRICK.defaultBlockState(), props) {}, stoneProps(SoundType.NETHER_BRICKS)
   );
   public static final Block WITHERED_NETHERBRICK_SLAB = register("withered_netherbrick_slab", SlabBlock::new, stoneProps(SoundType.NETHER_BRICKS));
   public static final Block WITHERED_NETHERBRICK_WALL = register(
      "withered_netherbrick_wall", WallBlock::new, stoneProps(SoundType.NETHER_BRICKS).noOcclusion()
   );
   public static final Block WITHERED_NETHERBRICK_FENCE = register(
      "withered_netherbrick_fence", FenceBlock::new, stoneProps(SoundType.NETHER_BRICKS).noOcclusion()
   );
   private static final SoundType WITHERED_WOOD_SOUND = SoundType.NETHER_WOOD;
   public static final Block STRIPPED_WITHERED_LOG = register(
      "stripped_withered_log",
      RotatedPillarBlock::new,
      Properties.of().instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
   );
   public static final Block WITHERED_PLANKS = register("withered_planks", Block::new, woodProps());
   public static final Block WITHERED_STAIRS = register("withered_stairs", props -> new StairBlock(WITHERED_PLANKS.defaultBlockState(), props) {}, woodProps());
   public static final Block WITHERED_SLAB = register("withered_slab", SlabBlock::new, woodProps());
   public static final Block WITHERED_FENCE = register("withered_fence", FenceBlock::new, woodProps());
   public static final Block WITHERED_BUTTON = register(
      "withered_button", props -> new ButtonBlock(BlockSetType.OAK, 30, props) {}, Properties.of().noCollision().strength(0.85F).sound(WITHERED_WOOD_SOUND)
   );
   public static final Block STRIPPED_WITHERED_PLANKS = register("stripped_withered_planks", Block::new, strippedWoodProps());
   public static final Block STRIPPED_WITHERED_STAIRS = register(
      "stripped_withered_stairs", props -> new StairBlock(STRIPPED_WITHERED_PLANKS.defaultBlockState(), props) {}, strippedWoodProps()
   );
   public static final Block STRIPPED_WITHERED_SLAB = register("stripped_withered_slab", SlabBlock::new, strippedWoodProps());
   public static final Block STRIPPED_WITHERED_FENCE = register("stripped_withered_fence", FenceBlock::new, strippedWoodProps());
   public static final Block STRIPPED_WITHERED_BUTTON = register(
      "stripped_withered_button", props -> new ButtonBlock(BlockSetType.OAK, 30, props) {}, Properties.of().noCollision().strength(0.5F).sound(SoundType.WOOD)
   );
   public static final Block WITHERED_MUSHROOM = register(
      "withered_mushroom",
      WitheredMushroomBlock::new,
      Properties.of().noCollision().instabreak().sound(SoundType.GRASS).lightLevel(s -> 6).emissiveRendering(state -> true).pushReaction(PushReaction.DESTROY)
   );
   public static final Block WITHERED_LOG = register(
      "withered_log", RotatedPillarBlock::new, Properties.of().instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
   );
   public static final Block WITHERED_DUST = register(
      "withered_dust", WitheredDustBlock::new, Properties.of().noCollision().instabreak().pushReaction(PushReaction.DESTROY)
   );
   public static final Block WITHERED_STONE = register(
      "withered_stone", Block::new, Properties.of().strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)
   );
   public static final Block WITHERED_STONE_STAIRS = register(
      "withered_stone_stairs",
      props -> new StairBlock(WITHERED_STONE.defaultBlockState(), props) {},
      Properties.of().strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)
   );
   public static final Block WITHERED_STONE_SLAB = register(
      "withered_stone_slab", SlabBlock::new, Properties.of().strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)
   );
   public static final Block COMMAND_CORE_BLOCK = register(
      "command_core_block", Block::new, Properties.of().strength(50.0F, 1200.0F).sound(SoundType.METAL).lightLevel(s -> 15).emissiveRendering(state -> true)
   );
   public static final Block TAINTED_OBSIDIAN = register(
      "tainted_obsidian", Block::new, Properties.of().strength(60.0F, 1400.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
   );
   public static final Block WITHER_STORM_EYE_BLOCK = register(
      "wither_storm_eye_block",
      Block::new,
      Properties.of().strength(10.0F, 100.0F).sound(SoundType.SLIME_BLOCK).lightLevel(s -> 12).emissiveRendering(state -> true)
   );
   public static final Block STORM_DEBRIS_BLOCK = register(
      "storm_debris_block", Block::new, Properties.of().strength(3.0F, 9.0F).sound(SoundType.BASALT).requiresCorrectToolForDrops()
   );
   public static final Block WITHERED_BONE_BLOCK = register(
      "withered_bone_block", RotatedPillarBlock::new, Properties.of().strength(2.0F, 4.0F).sound(SoundType.BONE_BLOCK).requiresCorrectToolForDrops()
   );
   public static final Block SUPER_COMMAND_BLOCK = register(
      "super_command_block", Block::new, Properties.of().strength(80.0F, 2000.0F).sound(SoundType.METAL).lightLevel(s -> 15).emissiveRendering(state -> true)
   );

   public static <T extends Block> T register(String name, Function<Properties, T> factory, Properties props) {
      ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("dabywitherstormmod", name));
      T block = (T)factory.apply(props.setId(key));
      Registry.register(BuiltInRegistries.BLOCK, key, block);
      return block;
   }

   private static Properties stoneProps(SoundType sound) {
      return Properties.of().strength(2.0F, 6.0F).sound(sound).requiresCorrectToolForDrops();
   }

   private static Properties woodProps() {
      return Properties.of().instrument(NoteBlockInstrument.BASS).strength(3.4F, 5.0F).sound(WITHERED_WOOD_SOUND).ignitedByLava();
   }

   private static Properties strippedWoodProps() {
      return Properties.of().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
   }

   public static void initialize() {
   }
}
