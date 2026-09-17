package net.dabicco.witherstormmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BowelsBackHall {
   public static final int START_X = 185;
   public static final int LENGTH = 150;
   public static final int END_X = 335;
   public static final int AXIS_Y = 64;
   public static final int AXIS_Z = 0;
   private static final double BORE = 5.4;
   private static final double BORE_SWELL = 0.9;
   private static final double BORE_RAGGED = (double)0.75F;
   private static final int WALL = 3;
   public static final int FLOOR_Y = 60;
   private static final double FLOOR_BUMP = 1.9;
   private static final double WANDER = (double)13.0F;
   private static final double WANDER_Y = (double)7.0F;
   private static final double INFLUENCE = 10.05 + Math.max((double)13.0F, (double)7.0F) + (double)8.0F;
   private static final long SALT_PATH = 1372043582L;
   private static final long SALT_BORE = 713974993L;
   private static final long SALT_RAGGED = 3056607143L;
   private static final long SALT_FLOOR = 2101725410L;
   private static final long SALT_SKIN = 261268315L;
   private static final long SALT_LUMPS = 3893876288L;

   private BowelsBackHall() {
   }

   public static boolean holds(double x, double y, double z) {
      if (!(x < (double)185.0F) && !(x > (double)343.0F)) {
         double dy = y - (double)64.0F;
         double dz = z - (double)0.0F;
         return dy * dy + dz * dz < INFLUENCE * INFLUENCE;
      } else {
         return false;
      }
   }

   static double axisZ(long seed, double x) {
      double sweep = (double)BowelsHallway.noise(seed ^ 1372043582L, x / (double)56.0F, (double)0.5F);
      double bend = (double)BowelsHallway.noise(seed ^ 1372043582L, x / (double)19.0F, (double)3.5F) * 0.45;
      return (double)0.0F + (double)13.0F * (sweep + bend) * ramp(x);
   }

   static double axisY(long seed, double x) {
      return (double)64.0F + (double)7.0F * (double)BowelsHallway.noise(seed ^ 1372043582L, x / (double)31.0F, (double)7.5F) * ramp(x);
   }

   private static double ramp(double x) {
      return Mth.clamp((x - (double)185.0F - (double)24.0F) / (double)30.0F, (double)0.0F, (double)1.0F) * Mth.clamp(((double)335.0F - x) / (double)34.0F, (double)0.0F, (double)1.0F);
   }

   private static double bore(long seed, double x, double turns) {
      double swell = 0.9 * (double)BowelsHallway.noise(seed ^ 713974993L, x / (double)26.0F, (double)0.5F);
      double ragged = (double)0.75F * (double)BowelsHallway.ringNoise(seed ^ 3056607143L, x / (double)5.0F, turns);
      return 5.4 + swell + ragged;
   }

   static double outerFace(long seed, double x, double turns) {
      return bore(seed, x, turns) + (double)3.0F;
   }

   static double floorTop(long seed, double x, double z) {
      double settled = Mth.clamp((x - (double)185.0F) / (double)14.0F, (double)0.0F, (double)1.0F);
      double lump = Math.max((double)0.0F, (double)BowelsHallway.noise(seed ^ 2101725410L, x / (double)11.0F, z / (double)11.0F));
      return axisY(seed, x) - (double)4.0F + 1.9 * settled * lump;
   }

   public static int build(ServerLevel level, long seed) {
      int reach = Mth.ceil(10.05) + 2;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 261268315L);
      int placed = 0;

      for(int x = 185; x <= 335; ++x) {
         double cz = axisZ(seed, (double)x);
         double cy = axisY(seed, (double)x);
         boolean closing = false;

         for(int y = (int)Math.floor(cy) - reach; y <= (int)Math.ceil(cy) + reach; ++y) {
            for(int z = (int)Math.floor(cz) - reach; z <= (int)Math.ceil(cz) + reach; ++z) {
               double dy = (double)y - cy;
               double dz = (double)z - cz;
               double d = Math.sqrt(dy * dy + dz * dz);
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               double face = bore(seed, (double)x, turns);
               if (!(d > face + (double)3.0F)) {
                  pos.set(x, y, z);
                  BlockState state;
                  if (d > face) {
                     double up = d < 1.0E-6 ? (double)0.0F : dy / d;
                     state = BowelsHallway.skinAt(seed, rng, x, y, z, up);
                  } else if (closing) {
                     state = ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
                  } else if ((double)y <= floorTop(seed, (double)x, (double)z)) {
                     state = BowelsHallway.floorSkin(seed, rng, x, y, z);
                  } else {
                     if (level.getBlockState(pos).isAir()) {
                        continue;
                     }

                     state = Blocks.AIR.defaultBlockState();
                  }

                  level.setBlock(pos, state, 2);
                  ++placed;
               }
            }
         }
      }

      return placed + dome(level, seed) + lumps(level, seed);
   }

   private static int dome(ServerLevel level, long seed) {
      int reach = Mth.ceil(10.05) + 2;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 261268315L);
      double cz = axisZ(seed, (double)335.0F);
      double cy = axisY(seed, (double)335.0F);
      int placed = 0;

      for(int x = 335; x <= 335 + reach; ++x) {
         double past = (double)(x - 335);

         for(int y = (int)Math.floor(cy) - reach; y <= (int)Math.ceil(cy) + reach; ++y) {
            for(int z = (int)Math.floor(cz) - reach; z <= (int)Math.ceil(cz) + reach; ++z) {
               double dy = (double)y - cy;
               double dz = (double)z - cz;
               double radial = Math.sqrt(dy * dy + dz * dz);
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               double d = Math.sqrt(radial * radial + past * past);
               double face = bore(seed, (double)335.0F, turns);
               if (!(d <= face) && !(d > face + (double)3.0F)) {
                  pos.set(x, y, z);
                  double up = d < 1.0E-6 ? (double)0.0F : dy / d;
                  level.setBlock(pos, BowelsHallway.skinAt(seed, rng, x, y, z, up), 2);
                  ++placed;
               }
            }
         }
      }

      return placed;
   }

   private static int lumps(ServerLevel level, long seed) {
      RandomSource rng = RandomSource.create(seed ^ 3893876288L);
      int placed = 0;
      int clear = 22;

      for(int i = 0; i < 75; ++i) {
         int x = 185 + clear + rng.nextInt(150 - clear * 2);
         double cz = axisZ(seed, (double)x);
         double axis = axisY(seed, (double)x);
         double turns = rng.nextDouble();
         double face = bore(seed, (double)x, turns);
         double radius = (double)1.0F + rng.nextDouble() * 1.6;
         double cy;
         double lumpZ;
         if (rng.nextInt(3) == 0) {
            double angle = (0.2 + rng.nextDouble() * 0.1) * Math.PI * (double)2.0F;
            cy = axis + Math.sin(angle) * (face - radius * 0.4);
            lumpZ = cz + Math.cos(angle) * (face - radius * 0.4);
         } else if (rng.nextInt(2) == 0) {
            double angle = rng.nextBoolean() ? (double)0.0F : (double)0.5F;
            cy = axis + Math.sin(angle * Math.PI * (double)2.0F) * (face - radius * 0.4);
            lumpZ = cz + Math.cos(angle * Math.PI * (double)2.0F) * (face - radius * 0.4);
         } else {
            radius = Math.min(radius, face * 0.34);
            cy = floorTop(seed, (double)x, cz) + radius * 0.6;
            lumpZ = cz + (rng.nextDouble() - (double)0.5F) * face;
         }

         BlockState state = rng.nextInt(5) == 0 ? ModBlocks.TORN_WITHERED_FLESH.defaultBlockState() : (rng.nextBoolean() ? ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState() : ModBlocks.WITHERED_COBBLESTONE.defaultBlockState());
         placed += BowelsHallway.blob(level, rng, (double)x, cy, lumpZ, radius, state);
      }

      return placed;
   }
}
