package net.dabicco.witherstormmod;

import net.dabicco.witherstormmod.BowelsMantle.Face;
import net.dabicco.witherstormmod.BowelsMantle.Line;
import net.dabicco.witherstormmod.BowelsMantle.RingFace;
import net.minecraft.core.BlockPos.MutableBlockPos;
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
      placed += alongX(
         level, seed, -16, 164, x -> BowelsHallway.axisY(seed, x), x -> BowelsHallway.axisZ(seed, x), (x, turns) -> BowelsHallway.outerFace(seed, x, turns)
      );
      placed += alongX(
         level, seed, 185, 351, x -> BowelsBackHall.axisY(seed, x), x -> BowelsBackHall.axisZ(seed, x), (x, turns) -> BowelsBackHall.outerFace(seed, x, turns)
      );
      placed += entry(level, seed);
      return placed + aroundRoom(level, seed);
   }

   private static int alongX(ServerLevel level, long seed, int from, int to, Line ys, Line zs, Face face) {
      MutableBlockPos pos = new MutableBlockPos();
      int placed = 0;

      for (int x = from; x <= to; x++) {
         double cy = ys.at(x);
         double cz = zs.at(x);
         int reach = Mth.ceil(face.at(x, 0.0)) + 6 + 2 + 2;

         for (int y = Mth.floor(cy) - reach; y <= Mth.ceil(cy) + reach; y++) {
            for (int z = Mth.floor(cz) - reach; z <= Mth.ceil(cz) + reach; z++) {
               double dy = y - cy;
               double dz = z - cz;
               double d = Math.sqrt(dy * dy + dz * dz);
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2) + 1.0) % 1.0;
               placed += fill(level, seed, pos, x, y, z, d, face.at(x, turns));
            }
         }
      }

      return placed;
   }

   private static int entry(ServerLevel level, long seed) {
      MutableBlockPos pos = new MutableBlockPos();
      int placed = 0;

      for (int z = -4; z <= 82; z++) {
         double[] centre = BowelsEntry.centreAt(seed, z);
         double cx = centre[0];
         double cy = centre[1];
         int reach = Mth.ceil(BowelsEntry.outerFace(seed, z, 0.0)) + 6 + 2 + 2;

         for (int x = Mth.floor(cx) - reach; x <= Mth.ceil(cx) + reach; x++) {
            for (int y = Mth.floor(cy) - reach; y <= Mth.ceil(cy) + reach; y++) {
               double dx = x - cx;
               double dy = y - cy;
               double d = Math.sqrt(dx * dx + dy * dy);
               double turns = (Math.atan2(dy, dx) / (Math.PI * 2) + 1.0) % 1.0;
               placed += fill(level, seed, pos, x, y, z, d, BowelsEntry.outerFace(seed, z, turns));
            }
         }
      }

      return placed;
   }

   private static int aroundRoom(ServerLevel level, long seed) {
      int cx = Mth.floor(177.0);
      int cz = Mth.floor(0.0);
      int reach = Mth.ceil(BowelsEndRoom.outerFace(seed, 60.0, 0.0)) + 6 + 2 + 3;
      int lowY = 47;
      int capY = 99;
      int topY = 181;
      MutableBlockPos pos = new MutableBlockPos();
      int placed = 0;

      for (int x = cx - reach; x <= cx + reach; x++) {
         for (int z = cz - reach; z <= cz + reach; z++) {
            double dx = x - 177.0;
            double dz = z - 0.0;
            double r = Math.sqrt(dx * dx + dz * dz);
            double turns = (Math.atan2(dz, dx) / (Math.PI * 2) + 1.0) % 1.0;

            for (int y = lowY; y <= topY; y++) {
               double face = y > capY ? BowelsEndRoom.shaftFace(seed, y, turns) : BowelsEndRoom.outerFace(seed, y, turns);
               placed += fill(level, seed, pos, x, y, z, r, face);
            }
         }
      }

      return placed;
   }

   private static boolean insideBuild(long seed, int x, int y, int z) {
      if (x >= -20
         && x <= 168
         && within(y - BowelsHallway.axisY(seed, x), z - BowelsHallway.axisZ(seed, x), (dy, dzx, turnsx) -> BowelsHallway.outerFace(seed, x, turnsx))) {
         return true;
      } else if (x >= 177
         && x <= 355
         && within(y - BowelsBackHall.axisY(seed, x), z - BowelsBackHall.axisZ(seed, x), (dy, dzx, turnsx) -> BowelsBackHall.outerFace(seed, x, turnsx))) {
         return true;
      } else {
         if (z >= -8 && z <= 84) {
            double[] centre = BowelsEntry.centreAt(seed, z);
            if (within(y - centre[1], x - centre[0], (dy, dxx, turnsx) -> BowelsEntry.outerFace(seed, z, turnsx))) {
               return true;
            }
         }

         double dx = x - 177.0;
         double dz = z - 0.0;
         double r = Math.sqrt(dx * dx + dz * dz);
         double turns = (Math.atan2(dz, dx) / (Math.PI * 2) + 1.0) % 1.0;
         return y >= 52 && y <= 99 && r <= BowelsEndRoom.outerFace(seed, y, turns) ? true : y > 96 && y <= 171 && r <= BowelsEndRoom.shaftFace(seed, y, turns);
      }
   }

   private static boolean within(double a, double b, RingFace face) {
      double d = Math.sqrt(a * a + b * b);
      double turns = (Math.atan2(a, b) / (Math.PI * 2) + 1.0) % 1.0;
      return d <= face.at(a, b, turns);
   }

   private static int fill(ServerLevel level, long seed, MutableBlockPos pos, int x, int y, int z, double d, double shell) {
      if (!(d <= shell) && !(d > shell + 6.0 + 2.0)) {
         pos.set(x, y, z);
         if (!level.getBlockState(pos).isAir()) {
            return 0;
         } else if (insideBuild(seed, x, y, z)) {
            return 0;
         } else {
            BlockState state = d > shell + 6.0 ? ModBlocks.WITHERED_BEDROCK.defaultBlockState() : ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState();
            level.setBlock(pos, state, 2);
            return 1;
         }
      } else {
         return 0;
      }
   }
}
