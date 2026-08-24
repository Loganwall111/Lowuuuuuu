package net.dabicco.devouringstorms;

import net.dabicco.devouringstorms.config.WitherStormWorldConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class StormCorruption {
   private StormCorruption() {
   }

   public static void tick(ServerLevel level, WitherStormEntity storm, WitherStormWorldConfig cfg) {
      if (cfg.voidCorruption == 0 || storm.isCollapsed() || storm.getPhase() < 5.0) {
         return;
      }

      RandomSource random = level.getRandom();
      double expansion = Math.max(storm.getPhase(), storm.getExpansionPhase());
      double growthScale = storm.currentGrowthScale();
      int bursts = Math.max(1, cfg.voidCorruptionBursts + Math.max(0, (int)Math.floor((growthScale - 1.0) * 3.0)));
      double phaseGain = storm.getPhase() >= 6.0 ? 1.4 : (storm.getPhase() >= 5.8 ? 1.2 : 1.0);
      double spreadGain = 0.95 + Math.max(0.0, growthScale - 1.0) * 0.42 + Math.max(0.0, expansion - 5.8) * 0.05;
      int radius = Math.max(6, (int)Math.round((double)cfg.voidCorruptionRadius * phaseGain * spreadGain));
      boolean late = storm.getPhase() >= 5.8 || expansion >= 6.6;

      for(int i = 0; i < bursts; ++i) {
         double ang = random.nextDouble() * Math.PI * 2.0;
         double dist = Math.sqrt(random.nextDouble()) * (double)radius;
         int x = Mth.floor(storm.getX() + Math.cos(ang) * dist);
         int z = Mth.floor(storm.getZ() + Math.sin(ang) * dist);
         int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
         BlockPos pos = new BlockPos(x, y, z);
         if (level.hasChunkAt(pos)) {
            corruptColumn(level, pos, late, random, cfg);
         }
      }
   }

   private static void corruptColumn(ServerLevel level, BlockPos pos, boolean late, RandomSource random, WitherStormWorldConfig cfg) {
      BlockPos cursor = pos;

      for(int i = 0; i < 4 && level.getBlockState(cursor).isAir(); ++i) {
         cursor = cursor.below();
      }

      BlockState state = level.getBlockState(cursor);
      BlockState replacement = corruptState(state, late);
      if (replacement != null && !state.is(replacement.getBlock())) {
         level.setBlock(cursor, replacement, 3);
      }

      BlockPos above = cursor.above();
      BlockState aboveState = level.getBlockState(above);
      if (aboveState.isAir() && random.nextInt(late ? 3 : 5) == 0) {
         level.setBlock(above, ModBlocks.WITHERED_MUSHROOM.defaultBlockState(), 3);
      } else if (!aboveState.isAir() && aboveState.canBeReplaced() && random.nextInt(4) == 0) {
         level.setBlock(above, ModBlocks.WITHERED_MUSHROOM.defaultBlockState(), 3);
      }

      if (cfg.voidCorruptionGrass != 0) {
         spreadSurfaceScars(level, cursor, late, random);
      }

      if (cfg.voidCorruptionTrees != 0) {
         blackenTrees(level, cursor, late, random);
      }
   }

   private static void spreadSurfaceScars(ServerLevel level, BlockPos origin, boolean late, RandomSource random) {
      int radius = late ? 4 : 2;
      for(int dx = -radius; dx <= radius; ++dx) {
         for(int dz = -radius; dz <= radius; ++dz) {
            if (dx * dx + dz * dz > radius * radius) {
               continue;
            }

            if (random.nextInt(late ? 2 : 3) != 0) {
               continue;
            }

            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;
            BlockPos ground = new BlockPos(x, level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
            if (!level.hasChunkAt(ground)) {
               continue;
            }

            BlockState groundState = level.getBlockState(ground);
            BlockState replacement = corruptSurfaceState(groundState, late);
            if (replacement != null && !groundState.is(replacement.getBlock())) {
               level.setBlock(ground, replacement, 3);
            }

            BlockPos above = ground.above();
            BlockState aboveState = level.getBlockState(above);
            if ((aboveState.isAir() || aboveState.canBeReplaced() || aboveState.is(Blocks.SNOW)) && random.nextInt(late ? 2 : 3) == 0) {
               BlockState dust = ModBlocks.WITHERED_DUST.defaultBlockState();
               BlockState growth = random.nextInt(late ? 3 : 5) == 0 ? ModBlocks.WITHERED_MUSHROOM.defaultBlockState() : dust;
               if (growth.canSurvive(level, above) || growth.getBlock() == ModBlocks.WITHERED_MUSHROOM) {
                  level.setBlock(above, growth, 3);
               }
            }
         }
      }
   }

   private static void blackenTrees(ServerLevel level, BlockPos origin, boolean late, RandomSource random) {
      int radius = late ? 5 : 3;
      int top = late ? 10 : 7;
      for(int dx = -radius; dx <= radius; ++dx) {
         for(int dz = -radius; dz <= radius; ++dz) {
            if (dx * dx + dz * dz > radius * radius) {
               continue;
            }

            for(int dy = -1; dy <= top; ++dy) {
               BlockPos target = origin.offset(dx, dy, dz);
               BlockState state = level.getBlockState(target);
               if (state.isAir()) {
                  continue;
               }

               if (state.is(BlockTags.LOGS)) {
                  BlockState log = copyAxis(state, ModBlocks.WITHERED_LOG.defaultBlockState());
                  if (!state.is(log.getBlock())) {
                     level.setBlock(target, log, 3);
                  }
                  continue;
               }

               if (state.is(BlockTags.LEAVES) && random.nextInt(late ? 2 : 3) == 0) {
                  level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
               }
            }
         }
      }
   }

   private static BlockState corruptSurfaceState(BlockState state, boolean late) {
      if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.FARMLAND) || state.is(Blocks.MUD) || state.is(Blocks.CLAY)) {
         return late ? ModBlocks.TORN_WITHERED_FLESH.defaultBlockState() : ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
      }
      if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
         return ModBlocks.WITHERED_SAND.defaultBlockState();
      }
      if (state.is(BlockTags.LOGS)) {
         return copyAxis(state, ModBlocks.WITHERED_LOG.defaultBlockState());
      }
      return null;
   }

   private static BlockState corruptState(BlockState state, boolean late) {
      if (state.isAir() || !state.getFluidState().isEmpty()) {
         return null;
      }

      Block block = state.getBlock();
      if (block == ModBlocks.WITHERED_FLESH_BLOCK || block == ModBlocks.TORN_WITHERED_FLESH || block == ModBlocks.WITHERED_BEDROCK || block == ModBlocks.WITHERED_COBBLESTONE || block == ModBlocks.WITHERED_NETHERBRICK || block == ModBlocks.WITHERED_SAND || block == ModBlocks.WITHERED_LOG || block == ModBlocks.WITHERED_PLANKS || block == ModBlocks.WITHERED_STONE) {
         return null;
      }

      if (state.is(BlockTags.LOGS)) {
         return copyAxis(state, ModBlocks.WITHERED_LOG.defaultBlockState());
      }

      if (state.is(BlockTags.PLANKS) || block == Blocks.CRAFTING_TABLE || block == Blocks.CHEST || block == Blocks.BARREL || block == Blocks.FLETCHING_TABLE) {
         return ModBlocks.WITHERED_PLANKS.defaultBlockState();
      }

      if (block == Blocks.COBBLESTONE || block == Blocks.MOSSY_COBBLESTONE || block == Blocks.STONE_BRICKS || block == Blocks.MOSSY_STONE_BRICKS || block == Blocks.CRACKED_STONE_BRICKS || block == Blocks.CHISELED_STONE_BRICKS) {
         return ModBlocks.WITHERED_COBBLESTONE.defaultBlockState();
      }

      if (block == Blocks.NETHER_BRICKS || block == Blocks.RED_NETHER_BRICKS || block == Blocks.CRACKED_NETHER_BRICKS || block == Blocks.CHISELED_NETHER_BRICKS || block == Blocks.NETHERRACK || block == Blocks.BLACKSTONE || block == Blocks.BASALT) {
         return ModBlocks.WITHERED_NETHERBRICK.defaultBlockState();
      }

      if (block == Blocks.SAND || block == Blocks.RED_SAND || block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL) {
         return ModBlocks.WITHERED_SAND.defaultBlockState();
      }

      if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT || block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.FARMLAND || block == Blocks.MUD || block == Blocks.CLAY) {
         return late ? ModBlocks.TORN_WITHERED_FLESH.defaultBlockState() : ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
      }

      if (block == Blocks.STONE || block == Blocks.ANDESITE || block == Blocks.DIORITE || block == Blocks.GRANITE || block == Blocks.TUFF || block == Blocks.CALCITE || block == Blocks.DRIPSTONE_BLOCK || block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) {
         return ModBlocks.WITHERED_STONE.defaultBlockState();
      }

      return null;
   }

   private static BlockState copyAxis(BlockState from, BlockState to) {
      if (from.hasProperty(RotatedPillarBlock.AXIS) && to.hasProperty(RotatedPillarBlock.AXIS)) {
         Axis axis = (Axis)from.getValue(RotatedPillarBlock.AXIS);
         return (BlockState)to.setValue(RotatedPillarBlock.AXIS, axis);
      }

      if (from.hasProperty(BlockStateProperties.AXIS) && to.hasProperty(BlockStateProperties.AXIS)) {
         Axis axis = (Axis)from.getValue(BlockStateProperties.AXIS);
         return (BlockState)to.setValue(BlockStateProperties.AXIS, axis);
      }

      return to;
   }
}
