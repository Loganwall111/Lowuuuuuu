package net.dabicco.witherstormmod;

import net.minecraft.core.BlockPos.MutableBlockPos;
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
   private static final double BORE_RAGGED = 0.75;
   private static final int WALL = 3;
   public static final int FLOOR_Y = 60;
   private static final double FLOOR_BUMP = 1.9;
   private static final double WANDER = 13.0;
   private static final double WANDER_Y = 7.0;
   private static final double INFLUENCE = 10.05 + Math.max(13.0, 7.0) + 8.0;
   private static final long SALT_PATH = 1372043582L;
   private static final long SALT_BORE = 713974993L;
   private static final long SALT_RAGGED = 3056607143L;
   private static final long SALT_FLOOR = 2101725410L;
   private static final long SALT_SKIN = 261268315L;
   private static final long SALT_LUMPS = 3893876288L;

   private BowelsBackHall() {
   }

   public static boolean holds(double x, double y, double z) {
      if (!(x < 185.0) && !(x > 343.0)) {
         double dy = y - 64.0;
         double dz = z - 0.0;
         return dy * dy + dz * dz < INFLUENCE * INFLUENCE;
      } else {
         return false;
      }
   }

   static double axisZ(long seed, double x) {
      double sweep = BowelsHallway.noise(seed ^ 1372043582L, x / 56.0, 0.5);
      double bend = BowelsHallway.noise(seed ^ 1372043582L, x / 19.0, 3.5) * 0.45;
      return 0.0 + 13.0 * (sweep + bend) * ramp(x);
   }

   static double axisY(long seed, double x) {
      return 64.0 + 7.0 * BowelsHallway.noise(seed ^ 1372043582L, x / 31.0, 7.5) * ramp(x);
   }

   private static double ramp(double x) {
      return Mth.clamp((x - 185.0 - 24.0) / 30.0, 0.0, 1.0) * Mth.clamp((335.0 - x) / 34.0, 0.0, 1.0);
   }

   private static double bore(long seed, double x, double turns) {
      double swell = 0.9 * BowelsHallway.noise(seed ^ 713974993L, x / 26.0, 0.5);
      double ragged = 0.75 * BowelsHallway.ringNoise(seed ^ 3056607143L, x / 5.0, turns);
      return 5.4 + swell + ragged;
   }

   static double outerFace(long seed, double x, double turns) {
      return bore(seed, x, turns) + 3.0;
   }

   static double floorTop(long seed, double x, double z) {
      double settled = Mth.clamp((x - 185.0) / 14.0, 0.0, 1.0);
      double lump = Math.max(0.0, (double)BowelsHallway.noise(seed ^ 2101725410L, x / 11.0, z / 11.0));
      return axisY(seed, x) - 4.0 + 1.9 * settled * lump;
   }

   public static int build(ServerLevel level, long seed) {
      int reach = Mth.ceil(10.05) + 2;
      MutableBlockPos pos = new MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 261268315L);
      int placed = 0;

      for (int x = 185; x <= 335; x++) {
         double cz = axisZ(seed, x);
         double cy = axisY(seed, x);
         boolean closing = false;

         for (int y = (int)Math.floor(cy) - reach; y <= (int)Math.ceil(cy) + reach; y++) {
            for (int z = (int)Math.floor(cz) - reach; z <= (int)Math.ceil(cz) + reach; z++) {
               double dy = y - cy;
               double dz = z - cz;
               double d = Math.sqrt(dy * dy + dz * dz);
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2) + 1.0) % 1.0;
               double face = bore(seed, x, turns);
               if (!(d > face + 3.0)) {
                  pos.set(x, y, z);
                  BlockState state;
                  if (d > face) {
                     double up = d < 1.0E-6 ? 0.0 : dy / d;
                     state = BowelsHallway.skinAt(seed, rng, x, y, z, up);
                  } else if (closing) {
                     state = ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
                  } else if (y <= floorTop(seed, x, z)) {
                     state = BowelsHallway.floorSkin(seed, rng, x, y, z);
                  } else {
                     if (level.getBlockState(pos).isAir()) {
                        continue;
                     }

                     state = Blocks.AIR.defaultBlockState();
                  }

                  level.setBlock(pos, state, 2);
                  placed++;
               }
            }
         }
      }

      return placed + dome(level, seed) + lumps(level, seed);
   }

   private static int dome(ServerLevel level, long seed) {
      int reach = Mth.ceil(10.05) + 2;
      MutableBlockPos pos = new MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 261268315L);
      double cz = axisZ(seed, 335.0);
      double cy = axisY(seed, 335.0);
      int placed = 0;

      for (int x = 335; x <= 335 + reach; x++) {
         double past = x - 335;

         for (int y = (int)Math.floor(cy) - reach; y <= (int)Math.ceil(cy) + reach; y++) {
            for (int z = (int)Math.floor(cz) - reach; z <= (int)Math.ceil(cz) + reach; z++) {
               double dy = y - cy;
               double dz = z - cz;
               double radial = Math.sqrt(dy * dy + dz * dz);
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2) + 1.0) % 1.0;
               double d = Math.sqrt(radial * radial + past * past);
               double face = bore(seed, 335.0, turns);
               if (!(d <= face) && !(d > face + 3.0)) {
                  pos.set(x, y, z);
                  double up = d < 1.0E-6 ? 0.0 : dy / d;
                  level.setBlock(pos, BowelsHallway.skinAt(seed, rng, x, y, z, up), 2);
                  placed++;
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

      for (int i = 0; i < 75; i++) {
         int x = 185 + clear + rng.nextInt(150 - clear * 2);
         double cz = axisZ(seed, x);
         double axis = axisY(seed, x);
         double turns = rng.nextDouble();
         double face = bore(seed, x, turns);
         double radius = 1.0 + rng.nextDouble() * 1.6;
         double cy;
         double lumpZ;
         if (rng.nextInt(3) == 0) {
            double angle = (0.2 + rng.nextDouble() * 0.1) * Math.PI * 2.0;
            cy = axis + Math.sin(angle) * (face - radius * 0.4);
            lumpZ = cz + Math.cos(angle) * (face - radius * 0.4);
         } else if (rng.nextInt(2) == 0) {
            double angle = rng.nextBoolean() ? 0.0 : 0.5;
            cy = axis + Math.sin(angle * Math.PI * 2.0) * (face - radius * 0.4);
            lumpZ = cz + Math.cos(angle * Math.PI * 2.0) * (face - radius * 0.4);
         } else {
            radius = Math.min(radius, face * 0.34);
            cy = floorTop(seed, x, cz) + radius * 0.6;
            lumpZ = cz + (rng.nextDouble() - 0.5) * face;
         }

         BlockState state = rng.nextInt(5) == 0
            ? ModBlocks.TORN_WITHERED_FLESH.defaultBlockState()
            : (rng.nextBoolean() ? ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState() : ModBlocks.WITHERED_COBBLESTONE.defaultBlockState());
         placed += BowelsHallway.blob(level, rng, x, cy, lumpZ, radius, state);
      }

      return placed;
   }
}
