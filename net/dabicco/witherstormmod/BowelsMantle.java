package net.dabicco.witherstormmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public final class BowelsMantle {
   private static final int FLESH = 6;
   private static final int BEDROCK = 2;

   private BowelsMantle() {
   }

   public static int build(ServerLevel level, long seed) {
      int placed = 0;
      placed += alongX(level, seed, -16, 164, (x) -> BowelsHallway.axisY(seed, x), (x) -> BowelsHallway.axisZ(seed, x), (x, turns) -> BowelsHallway.outerFace(seed, x, turns));
      placed += alongX(level, seed, 185, 351, (x) -> BowelsBackHall.axisY(seed, x), (x) -> BowelsBackHall.axisZ(seed, x), (x, turns) -> BowelsBackHall.outerFace(seed, x, turns));
      placed += entry(level, seed);
      placed += aroundRoom(level, seed);
      return placed;
   }

   private static int alongX(ServerLevel level, long seed, int from, int to, Line ys, Line zs, Face face) {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int placed = 0;

      for(int x = from; x <= to; ++x) {
         double cy = ys.at((double)x);
         double cz = zs.at((double)x);
         int reach = Mth.ceil(face.at((double)x, (double)0.0F)) + 6 + 2 + 2;

         for(int y = Mth.floor(cy) - reach; y <= Mth.ceil(cy) + reach; ++y) {
            for(int z = Mth.floor(cz) - reach; z <= Mth.ceil(cz) + reach; ++z) {
               double dy = (double)y - cy;
               double dz = (double)z - cz;
               double d = Math.sqrt(dy * dy + dz * dz);
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               placed += fill(level, seed, pos, x, y, z, d, face.at((double)x, turns));
            }
         }
      }

      return placed;
   }

   private static int entry(ServerLevel level, long seed) {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int placed = 0;

      for(int z = -4; z <= 82; ++z) {
         double[] centre = BowelsEntry.centreAt(seed, (double)z);
         double cx = centre[0];
         double cy = centre[1];
         int reach = Mth.ceil(BowelsEntry.outerFace(seed, (double)z, (double)0.0F)) + 6 + 2 + 2;

         for(int x = Mth.floor(cx) - reach; x <= Mth.ceil(cx) + reach; ++x) {
            for(int y = Mth.floor(cy) - reach; y <= Mth.ceil(cy) + reach; ++y) {
               double dx = (double)x - cx;
               double dy = (double)y - cy;
               double d = Math.sqrt(dx * dx + dy * dy);
               double turns = (Math.atan2(dy, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               placed += fill(level, seed, pos, x, y, z, d, BowelsEntry.outerFace(seed, (double)z, turns));
            }
         }
      }

      return placed;
   }

   private static int aroundRoom(ServerLevel level, long seed) {
      int cx = Mth.floor((double)177.0F);
      int cz = Mth.floor((double)0.0F);
      int reach = Mth.ceil(BowelsEndRoom.outerFace(seed, (double)60.0F, (double)0.0F)) + 6 + 2 + 3;
      int lowY = 47;
      int capY = 99;
      int topY = 181;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int placed = 0;

      for(int x = cx - reach; x <= cx + reach; ++x) {
         for(int z = cz - reach; z <= cz + reach; ++z) {
            double dx = (double)x - (double)177.0F;
            double dz = (double)z - (double)0.0F;
            double r = Math.sqrt(dx * dx + dz * dz);
            double turns = (Math.atan2(dz, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;

            for(int y = lowY; y <= topY; ++y) {
               double face = y > capY ? BowelsEndRoom.shaftFace(seed, (double)y, turns) : BowelsEndRoom.outerFace(seed, (double)y, turns);
               placed += fill(level, seed, pos, x, y, z, r, face);
            }
         }
      }

      return placed;
   }

   private static boolean insideBuild(long seed, int x, int y, int z) {
      if (x >= -20 && x <= 168 && within((double)y - BowelsHallway.axisY(seed, (double)x), (double)z - BowelsHallway.axisZ(seed, (double)x), (dy, dzx, turnsx) -> BowelsHallway.outerFace(seed, (double)x, turnsx))) {
         return true;
      } else if (x >= 177 && x <= 355 && within((double)y - BowelsBackHall.axisY(seed, (double)x), (double)z - BowelsBackHall.axisZ(seed, (double)x), (dy, dzx, turnsx) -> BowelsBackHall.outerFace(seed, (double)x, turnsx))) {
         return true;
      } else {
         if (z >= -8 && z <= 84) {
            double[] centre = BowelsEntry.centreAt(seed, (double)z);
            if (within((double)y - centre[1], (double)x - centre[0], (dy, dxx, turnsx) -> BowelsEntry.outerFace(seed, (double)z, turnsx))) {
               return true;
            }
         }

         double dx = (double)x - (double)177.0F;
         double dz = (double)z - (double)0.0F;
         double r = Math.sqrt(dx * dx + dz * dz);
         double turns = (Math.atan2(dz, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
         if (y >= 52 && y <= 99 && r <= BowelsEndRoom.outerFace(seed, (double)y, turns)) {
            return true;
         } else {
            return y > 96 && y <= 171 && r <= BowelsEndRoom.shaftFace(seed, (double)y, turns);
         }
      }
   }

   private static boolean within(double a, double b, RingFace face) {
      double d = Math.sqrt(a * a + b * b);
      double turns = (Math.atan2(a, b) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
      return d <= face.at(a, b, turns);
   }

   private static int fill(ServerLevel level, long seed, BlockPos.MutableBlockPos pos, int x, int y, int z, double d, double shell) {
      if (!(d <= shell) && !(d > shell + (double)6.0F + (double)2.0F)) {
         pos.set(x, y, z);
         if (!level.getBlockState(pos).isAir()) {
            return 0;
         } else if (insideBuild(seed, x, y, z)) {
            return 0;
         } else {
            BlockState state = d > shell + (double)6.0F ? ModBlocks.WITHERED_BEDROCK.defaultBlockState() : ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
            level.setBlock(pos, state, 2);
            return 1;
         }
      } else {
         return 0;
      }
   }

   private interface Face {
      double at(double var1, double var3);
   }

   private interface Line {
      double at(double var1);
   }

   private interface RingFace {
      double at(double var1, double var3, double var5);
   }
}
