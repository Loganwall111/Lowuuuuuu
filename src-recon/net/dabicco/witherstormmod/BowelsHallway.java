package net.dabicco.witherstormmod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;

public final class BowelsHallway {
   public static final int START_X = 0;
   public static final int LENGTH = 160;
   public static final int END_X = 160;
   public static final int AXIS_Y = 64;
   public static final int AXIS_Z = 0;
   public static final Direction FALL = Direction.EAST;
   private static final double BORE = 5.4;
   private static final double BORE_SWELL = 0.9;
   private static final double BORE_RAGGED = 0.75;
   public static final int WALL = 3;
   public static final int FLOOR_Y = 60;
   private static final double FLOOR_BUMP = 1.9;
   private static final int APPROACH = 12;
   private static final double TURN_ROOM = 2.2;
   private static final double WANDER = 13.0;
   private static final double WANDER_Y = 7.0;
   private static final double INFLUENCE = 10.05 + Math.max(13.0, 7.0) + 8.0;
   private static final long SALT_PATH = 2400699041L;
   private static final long SALT_BORE = 1029610247L;
   private static final long SALT_RAGGED = 1906622697L;
   private static final long SALT_FLOOR = 745244115L;
   private static final long SALT_SKIN = 1519642399L;
   private static final long SALT_DIRT = 2653766664L;
   private static final long SALT_LUMPS = 348089381L;
   private static final long SALT_TORN = 1808873892L;
   private static final long SALT_SHROOM = 2692633286L;
   private static final long SALT_DUST = 1065464925L;
   private static final int RING = 16;
   private static final String MARKER = "dabywsmod_bowels_hallway.19.built";

   private BowelsHallway() {
   }

   public static double turnEase(double x) {
      return planeEase(x, 80);
   }

   private static double planeEase(double x, int plane) {
      double d = Math.abs(x - plane) / 14.0;
      if (d >= 1.0) {
         return 0.0;
      } else {
         double t = 1.0 - d;
         return t * t * (3.0 - 2.0 * t);
      }
   }

   public static boolean holds(double x, double y, double z) {
      if (!(x < -12.0) && !(x > 168.0)) {
         double dy = y - 64.0;
         double dz = z - 0.0;
         return dy * dy + dz * dz < INFLUENCE * INFLUENCE;
      } else {
         return false;
      }
   }

   static double axisZ(long seed, double x) {
      double ramp = Mth.clamp(Math.min(x - 0.0, 160.0 - x) / 40.0, 0.0, 1.0) * pinned(x, 28.0, 16.0);
      double sweep = noise(seed ^ 2400699041L, x / 56.0, 0.5);
      double bend = noise(seed ^ 2400699041L, x / 19.0, 3.5) * 0.45;
      return 0.0 + 13.0 * (sweep + bend) * ramp;
   }

   static double axisY(long seed, double x) {
      double after = Mth.clamp((x - 80.0) / 26.0, 0.0, 1.0);
      double ramp = after * Mth.clamp((160.0 - x) / 34.0, 0.0, 1.0);
      return 64.0 + 7.0 * noise(seed ^ 2400699041L, x / 31.0, 7.5) * ramp;
   }

   private static double pinned(double x, double at, double reach) {
      double d = Math.min(Math.abs(x - at) / reach, 1.0);
      return d * d * (3.0 - 2.0 * d);
   }

   private static double bore(long seed, double x, double turns) {
      double swell = 0.9 * noise(seed ^ 1029610247L, x / 26.0, 0.5);
      double ragged = 0.75 * ringNoise(seed ^ 1906622697L, x / 5.0, turns);
      return 5.4 + swell + ragged + 2.2 * turnEase(x);
   }

   static double outerFace(long seed, double x, double turns) {
      return bore(seed, x, turns) + 3.0;
   }

   static boolean insideBore(long seed, double x, double y, double z) {
      if (!(x < 0.0) && !(x > 160.0)) {
         double dy = y - axisY(seed, x);
         double dz = z - axisZ(seed, x);
         double turns = (Math.atan2(dy, dz) / (Math.PI * 2) + 1.0) % 1.0;
         return Math.sqrt(dy * dy + dz * dz) <= bore(seed, x, turns);
      } else {
         return false;
      }
   }

   static double floorTop(long seed, double x, double z) {
      double settled = Mth.clamp((x - 80.0) / 20.0, 0.0, 1.0);
      if (settled <= 0.0) {
         return 0.0;
      } else {
         double axis = axisY(seed, x);
         double arriving = Mth.clamp((160.0 - x) / 14.0, 0.0, 1.0);
         double lump = Math.max(0.0, (double)noise(seed ^ 745244115L, x / 11.0, z / 11.0));
         double top = axis - 4.0 + 1.9 * arriving * lump;
         return Mth.lerp(settled, axis - 5.4 - 0.9 - 0.75 - 1.0, top);
      }
   }

   public static void ensureBuilt(ServerLevel level) {
      if (level.getServer() != null) {
         Path marker = level.getServer().getWorldPath(LevelResource.ROOT).resolve("dabywsmod_bowels_hallway.19.built");
         if (!Files.exists(marker)) {
            try {
               Files.createFile(marker);
            } catch (IOException var7) {
               DabyWitherStormMod.LOGGER.warn("[bowels] couldn't write the hallway marker", var7);
               return;
            }

            long start = System.currentTimeMillis();
            long seed = level.getSeed();
            int placed = shell(level, seed)
               + dome(level, seed)
               + lumps(level, seed)
               + BowelsBackHall.build(level, seed)
               + BowelsEndRoom.build(level, seed)
               + BowelsEntry.build(level, seed)
               + BowelsMantle.build(level, seed)
               + mushrooms(level, seed)
               + dust(level, seed);
            DabyWitherStormMod.LOGGER
               .info("[bowels] hallway built, {} blocks in {} ms (seed {})", new Object[]{placed, System.currentTimeMillis() - start, seed});
         }
      }
   }

   private static int shell(ServerLevel level, long seed) {
      int reach = Mth.ceil(10.05) + 2;
      MutableBlockPos pos = new MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 1519642399L);
      int placed = 0;

      for (int x = 0; x <= 160; x++) {
         double cz = axisZ(seed, x);
         double cy = axisY(seed, x);
         boolean closing = x > 157;
         boolean upright = x >= 80;

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
                     double up = d < 1.0E-6 ? 0.0 : (upright ? dy : dz) / d;
                     BlockState pocket = tornPocket(seed, x, y, z, up, d - face);
                     state = pocket != null ? pocket : skinAt(seed, rng, x, y, z, up);
                  } else if (closing) {
                     state = ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
                  } else if (y <= floorTop(seed, x, z)) {
                     state = floorSkin(seed, rng, x, y, z);
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

      return placed;
   }

   private static int dome(ServerLevel level, long seed) {
      int reach = Mth.ceil(10.05) + 2;
      MutableBlockPos pos = new MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 2653766664L);
      double cz = axisZ(seed, 0.0);
      int placed = 0;

      for (int x = 0 - reach; x < 0; x++) {
         double past = 0 - x;

         for (int y = 64 - reach; y <= 64 + reach; y++) {
            for (int z = (int)Math.floor(cz) - reach; z <= (int)Math.ceil(cz) + reach; z++) {
               double dy = y - 64;
               double dz = z - cz;
               double radial = Math.sqrt(dy * dy + dz * dz);
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2) + 1.0) % 1.0;
               double d = Math.sqrt(radial * radial + past * past);
               double face = bore(seed, 0.0, turns);
               if (!(d <= face) && !(d > face + 3.0)) {
                  pos.set(x, y, z);
                  level.setBlock(pos, wallSkin(seed, rng, x, y, z), 2);
                  placed++;
               }
            }
         }
      }

      return placed;
   }

   private static int lumps(ServerLevel level, long seed) {
      RandomSource rng = RandomSource.create(seed ^ 348089381L);
      int count = 22;
      int placed = 0;
      int clear = 22;

      for (int i = 0; i < count; i++) {
         int x = 0 + clear + rng.nextInt(160 - clear * 2);
         double cz = axisZ(seed, x);
         double axis = axisY(seed, x);
         double turns = rng.nextDouble();
         double face = bore(seed, x, turns);
         double radius = 1.0 + rng.nextDouble() * 1.6;
         double cy;
         double lumpZ;
         if (x < 80) {
            double angle = rng.nextDouble() * Math.PI * 2.0;
            radius = Math.min(radius, 1.5);
            cy = axis + Math.sin(angle) * (face + radius * 0.55);
            lumpZ = cz + Math.cos(angle) * (face + radius * 0.55);
         } else if (rng.nextInt(3) == 0) {
            double angle = (0.2 + rng.nextDouble() * 0.1) * Math.PI * 2.0;
            cy = axis + Math.sin(angle) * (face + radius * 0.5);
            lumpZ = cz + Math.cos(angle) * (face + radius * 0.5);
         } else if (rng.nextInt(2) == 0) {
            double angle = rng.nextBoolean() ? 0.0 : 0.5;
            cy = axis + Math.sin(angle * Math.PI * 2.0) * (face + radius * 0.5);
            lumpZ = cz + Math.cos(angle * Math.PI * 2.0) * (face + radius * 0.5);
         } else {
            radius = Math.min(radius, face * 0.3);
            cy = floorTop(seed, x, cz) + radius * 0.1;
            lumpZ = cz + (rng.nextDouble() - 0.5) * face;
         }

         BlockState state = rng.nextBoolean() ? ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState() : ModBlocks.WITHERED_COBBLESTONE.defaultBlockState();
         placed += blob(level, rng, x, cy, lumpZ, radius, state);
      }

      return placed;
   }

   private static int mushrooms(ServerLevel level, long seed) {
      RandomSource rng = RandomSource.create(seed ^ 2692633286L);
      int placed = 0;
      int from = Math.max(4, 86);

      for (int x = from; x <= 156; x++) {
         if (rng.nextInt(12) == 0) {
            double cz = axisZ(seed, x);
            double face = bore(seed, x, rng.nextDouble());
            double out = face * (0.55 + rng.nextDouble() * 0.4);
            int z = Mth.floor(cz + (rng.nextBoolean() ? out : -out));
            placed += plant(level, x, z, Mth.floor(axisY(seed, x)));
         }
      }

      for (int i = 0; i < 45; i++) {
         double angle = rng.nextDouble() * Math.PI * 2.0;
         double t = rng.nextDouble();
         double r = 9.0 + 11.0 * Math.sqrt(t) * (0.55 + 0.45 * t);
         int xx = Mth.floor(177.0 + Math.cos(angle) * r);
         int z = Mth.floor(0.0 + Math.sin(angle) * r);
         placed += plant(level, xx, z, 62);
      }

      return placed;
   }

   private static int dust(ServerLevel level, long seed) {
      RandomSource rng = RandomSource.create(seed ^ 1065464925L);
      List<BlockPos> laid = new ArrayList<>();
      int from = Math.max(4, 86);

      for (int x = from; x <= 156; x++) {
         if (rng.nextInt(17) == 0) {
            double cz = axisZ(seed, x);
            double face = bore(seed, x, rng.nextDouble());
            int z = Mth.floor(cz + (rng.nextDouble() - 0.5) * face * 1.5);
            strew(level, rng, laid, x, z, Mth.floor(axisY(seed, x)));
         }
      }

      for (int i = 0; i < 26; i++) {
         double angle = rng.nextDouble() * Math.PI * 2.0;
         double r = 8.0 + rng.nextDouble() * 11.5;
         int xx = Mth.floor(177.0 + Math.cos(angle) * r);
         int z = Mth.floor(0.0 + Math.sin(angle) * r);
         strew(level, rng, laid, xx, z, 62);
      }

      for (BlockPos pos : laid) {
         BlockState state = level.getBlockState(pos);
         if (state.is(ModBlocks.WITHERED_DUST)) {
            level.setBlock(pos, Block.updateFromNeighbourShapes(state, level, pos), 2);
         }
      }

      return laid.size();
   }

   private static void strew(ServerLevel level, RandomSource rng, List<BlockPos> laid, int x, int z, int fromY) {
      int run = rng.nextInt(5) == 0 ? 3 + rng.nextInt(5) : 1;
      int dx = rng.nextBoolean() ? 1 : -1;
      int dz = 0;

      for (int i = 0; i < run; i++) {
         BlockPos at = grain(level, x, z, fromY);
         if (at != null) {
            laid.add(at);
            fromY = at.getY();
         }

         if (rng.nextInt(3) == 0) {
            int turn = dx;
            dx = rng.nextBoolean() ? dz : -dz;
            dz = dx == 0 ? (rng.nextBoolean() ? turn : -turn) : 0;
            if (dx == 0 && dz == 0) {
               dx = 1;
            }
         }

         x += dx;
         z += dz;
      }
   }

   private static BlockPos grain(ServerLevel level, int x, int z, int fromY) {
      MutableBlockPos pos = new MutableBlockPos();

      for (int y = fromY + 3; y >= fromY - 9; y--) {
         pos.set(x, y, z);
         if (level.getBlockState(pos).isAir()) {
            BlockPos at = pos.immutable();
            BlockState dust = (BlockState)ModBlocks.WITHERED_DUST.defaultBlockState().setValue(RedStoneWireBlock.POWER, 15);
            if (dust.canSurvive(level, at)) {
               level.setBlock(at, dust, 2);
               return at;
            }
         }
      }

      return null;
   }

   private static int plant(ServerLevel level, int x, int z, int fromY) {
      MutableBlockPos pos = new MutableBlockPos();

      for (int y = fromY + 3; y >= fromY - 9; y--) {
         pos.set(x, y, z);
         if (level.getBlockState(pos).isAir()) {
            BlockPos at = pos.immutable();
            BlockState shroom = ModBlocks.WITHERED_MUSHROOM.defaultBlockState();
            if (shroom.canSurvive(level, at)) {
               level.setBlock(at, shroom, 2);
               return 1;
            }
         }
      }

      return 0;
   }

   static int blob(ServerLevel level, RandomSource rng, double cx, double cy, double cz, double radius, BlockState state) {
      int reach = Mth.ceil(radius) + 1;
      MutableBlockPos pos = new MutableBlockPos();
      int placed = 0;

      for (int x = Mth.floor(cx) - reach; x <= Mth.ceil(cx) + reach; x++) {
         for (int y = Mth.floor(cy) - reach; y <= Mth.ceil(cy) + reach; y++) {
            for (int z = Mth.floor(cz) - reach; z <= Mth.ceil(cz) + reach; z++) {
               double dx = x - cx;
               double dy = y - cy;
               double dz = z - cz;
               double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (!(d > radius + rng.nextDouble() * 0.9 - 0.45)) {
                  pos.set(x, y, z);
                  if (level.getBlockState(pos).isAir()) {
                     level.setBlock(pos, state, 2);
                     placed++;
                  }
               }
            }
         }
      }

      return placed;
   }

   static BlockState tornPocket(long seed, int x, int y, int z, double up, double depth) {
      if (Math.abs(up) > 0.45) {
         return null;
      } else if (depth >= 2.0) {
         return null;
      } else {
         float bit = noise(seed ^ 1808873892L, x / 2.6, (y * 1.15 + z * 0.55) / 2.6);
         if (bit < 0.68F) {
            return null;
         } else {
            return depth < 1.0 ? Blocks.AIR.defaultBlockState() : ModBlocks.TORN_WITHERED_FLESH.defaultBlockState();
         }
      }
   }

   static BlockState wallSkin(long seed, RandomSource rng, int x, int y, int z) {
      return skinAt(seed, rng, x, y, z, 0.0);
   }

   static BlockState skinAt(long seed, RandomSource rng, int x, int y, int z, double up) {
      float patch = noise(seed ^ 1519642399L, x / 9.0, (y * 0.7 + z) / 9.0);
      float fine = noise(seed ^ 2653766664L, x * 1.3 / 3.5, (y + z * 0.6) / 3.5);
      double band = up + fine * 0.22;
      if (band > 0.42) {
         return fine > 0.66F ? ModBlocks.WITHERED_COBBLESTONE.defaultBlockState() : ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
      } else if (band < -0.34) {
         if (patch > 0.05F) {
            return fine > 0.2F ? ModBlocks.WITHERED_SAND.defaultBlockState() : ModBlocks.WITHERED_COBBLESTONE.defaultBlockState();
         } else {
            return fine > 0.55F ? ModBlocks.WITHERED_SAND.defaultBlockState() : ModBlocks.WITHERED_COBBLESTONE.defaultBlockState();
         }
      } else if (patch > 0.3F && fine > -0.2F) {
         return ModBlocks.WITHERED_PLANKS.defaultBlockState();
      } else if (fine > 0.74F) {
         return ModBlocks.WITHERED_SAND.defaultBlockState();
      } else if (patch < -0.7F && rng.nextInt(4) == 0) {
         return ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
      } else {
         return patch > 0.0F ? ModBlocks.WITHERED_NETHERBRICK.defaultBlockState() : ModBlocks.WITHERED_COBBLESTONE.defaultBlockState();
      }
   }

   static BlockState floorSkin(long seed, RandomSource rng, int x, int y, int z) {
      if (y < 58) {
         return ModBlocks.WITHERED_COBBLESTONE.defaultBlockState();
      } else {
         float patch = noise(seed ^ 2653766664L, x / 13.0, z / 13.0);
         float fine = noise(seed ^ 1519642399L, x / 4.5, z / 4.5);
         if (patch < -0.3F) {
            return ModBlocks.WITHERED_COBBLESTONE.defaultBlockState();
         } else {
            return fine > 0.42F ? ModBlocks.WITHERED_COBBLESTONE.defaultBlockState() : ModBlocks.WITHERED_SAND.defaultBlockState();
         }
      }
   }

   static float noise(long seed, double a, double b) {
      int ia = Mth.floor(a);
      int ib = Mth.floor(b);
      float fa = (float)(a - ia);
      float fb = (float)(b - ib);
      float sa = fa * fa * (3.0F - 2.0F * fa);
      float sb = fb * fb * (3.0F - 2.0F * fb);
      return Mth.lerp(
         sb, Mth.lerp(sa, lattice(seed, ia, ib), lattice(seed, ia + 1, ib)), Mth.lerp(sa, lattice(seed, ia, ib + 1), lattice(seed, ia + 1, ib + 1))
      );
   }

   static float ringNoise(long seed, double a, double turns) {
      double b = turns * 16.0;
      int ia = Mth.floor(a);
      int ib = Mth.floor(b);
      float fa = (float)(a - ia);
      float fb = (float)(b - ib);
      float sa = fa * fa * (3.0F - 2.0F * fa);
      float sb = fb * fb * (3.0F - 2.0F * fb);
      int b0 = Math.floorMod(ib, 16);
      int b1 = Math.floorMod(ib + 1, 16);
      return Mth.lerp(sb, Mth.lerp(sa, lattice(seed, ia, b0), lattice(seed, ia + 1, b0)), Mth.lerp(sa, lattice(seed, ia, b1), lattice(seed, ia + 1, b1)));
   }

   static float lattice(long seed, int a, int b) {
      long h = seed + a * -7046029254386353131L + b * -4417276706812531889L;
      h ^= h >>> 33;
      h *= -49064778989728563L;
      h ^= h >>> 33;
      h *= -4265267296055464877L;
      h ^= h >>> 33;
      return (float)(h >>> 40) / 8388608.0F - 1.0F;
   }
}
