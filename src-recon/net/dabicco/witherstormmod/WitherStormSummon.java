package net.dabicco.witherstormmod;

import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class WitherStormSummon {
   private WitherStormSummon() {
   }

   public static boolean trySpawn(Level level, BlockPos placedSkull) {
      if (level.isClientSide()) {
         return false;
      } else {
         int[][] axes = new int[][]{{1, 0}, {0, 1}};

         for (int[] a : axes) {
            int dx = a[0];
            int dz = a[1];

            for (int role = -1; role <= 1; role++) {
               BlockPos center = placedSkull.offset(-role * dx, 0, -role * dz);
               if (matches(level, center, dx, dz)) {
                  summon((ServerLevel)level, center, dx, dz);
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static boolean matches(Level level, BlockPos c, int dx, int dz) {
      if (isSkull(level, c) && isSkull(level, c.offset(dx, 0, dz)) && isSkull(level, c.offset(-dx, 0, -dz))) {
         BlockPos below = c.below();
         if (!level.getBlockState(below).is(Blocks.COMMAND_BLOCK)) {
            return false;
         } else if (!isSoul(level, below.offset(dx, 0, dz))) {
            return false;
         } else {
            return !isSoul(level, below.offset(-dx, 0, -dz)) ? false : isSoul(level, below.below());
         }
      } else {
         return false;
      }
   }

   private static boolean isSkull(Level l, BlockPos p) {
      return l.getBlockState(p).is(Blocks.WITHER_SKELETON_SKULL);
   }

   private static boolean isSoul(Level l, BlockPos p) {
      return l.getBlockState(p).is(Blocks.SOUL_SAND);
   }

   private static void summon(ServerLevel level, BlockPos c, int dx, int dz) {
      BlockPos below = c.below();
      BlockPos[] used = new BlockPos[]{c, c.offset(dx, 0, dz), c.offset(-dx, 0, -dz), below, below.offset(dx, 0, dz), below.offset(-dx, 0, -dz), below.below()};

      for (BlockPos p : used) {
         level.removeBlock(p, false);
      }

      WitherStormEntity storm = (WitherStormEntity)ModEntityTypes.WITHER_STORM.create(level, EntitySpawnReason.TRIGGERED);
      if (storm != null) {
         storm.setPhase(0.0);
         storm.setPos(c.getX() + 0.5, below.getY(), c.getZ() + 0.5);
         storm.beginSpawnFreeze();
         level.addFreshEntity(storm);
         ModAdvancements.grantNearby(level, storm, "nothing_built");
      }
   }
}
