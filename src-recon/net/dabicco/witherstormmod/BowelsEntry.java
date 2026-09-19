package net.dabicco.witherstormmod;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class BowelsEntry {
   public static final int AT_X = 28;
   public static final int AT_Y = 64;
   public static final int TOP_Z = 72;
   private static final double BORE = 2.8;
   private static final double BORE_RAGGED = 0.25;
   private static final int WALL = 3;
   private static final double INFLUENCE = 11.8;
   private static final double WANDER = 2.2;
   private static final double PULL_STOP_Z = 18.0;
   private static final double CENTRE_GAIN = 0.22;
   private static final double CENTRE_MAX = 0.55;
   private static final double CENTRE_DAMP = 0.68;
   private static final long SALT_PATH = 1317123015L;
   private static final long SALT_BORE = 3527871131L;
   private static final long SALT_SKIN = 2349199982L;

   private BowelsEntry() {
   }

   public static boolean holds(double x, double y, double z) {
      if (!(z < -2.0) && !(z > 78.0)) {
         double dx = x - 28.0;
         double dy = y - 64.0;
         return dx * dx + dy * dy < 139.24;
      } else {
         return false;
      }
   }

   public static Vec3 arrival() {
      return new Vec3(28.5, 64.5, 67.5);
   }

   public static float arrivalYaw() {
      return 0.0F;
   }

   public static float arrivalPitch() {
      return 70.0F;
   }

   public static void centreDrop(ServerLevel level, Entity entity) {
      double z = entity.getZ();
      if (!(z < 18.0) && !(z > 75.0)) {
         double[] c = centreAt(level.getSeed(), z);
         double dx = c[0] - entity.getX();
         double dy = c[1] - entity.getY();
         double off = Math.sqrt(dx * dx + dy * dy);
         if (!(off < 0.001) && !(off > 6.05)) {
            double toward = Math.min(off * 0.22, 0.55);
            Vec3 v = entity.getDeltaMovement();
            double wantX = v.x * 0.68 + dx / off * toward;
            double wantY = v.y * 0.68 + dy / off * toward;
            if (entity instanceof ServerPlayer player) {
               player.push(wantX - v.x, wantY - v.y, 0.0);
               player.hurtMarked = true;
            } else {
               entity.setDeltaMovement(wantX, wantY, v.z);
            }
         }
      }
   }

   static double outerFace(long seed, double z, double turns) {
      return bore(seed, Math.min(z, 72.0), turns) + 3.0;
   }

   static double[] centreAt(long seed, double z) {
      return centre(seed, Math.min(z, 72.0));
   }

   private static double[] centre(long seed, double z) {
      double ramp = Mth.clamp(Math.min(72.0 - z, z) / 22.0, 0.0, 1.0);
      double dx = 2.2 * BowelsHallway.noise(seed ^ 1317123015L, z / 31.0, 0.5) * ramp;
      double dy = 2.2 * BowelsHallway.noise(seed ^ 1317123015L, z / 31.0, 7.5) * ramp;
      return new double[]{28.0 + dx, 64.0 + dy};
   }

   private static double bore(long seed, double z, double turns) {
      return 2.8 + 0.25 * BowelsHallway.ringNoise(seed ^ 3527871131L, z / 4.0, turns);
   }

   public static int build(ServerLevel level, long seed) {
      int reach = Mth.ceil(6.05) + 2;
      MutableBlockPos pos = new MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 2349199982L);
      int placed = 0;

      for (int z = 0; z <= 77; z++) {
         double[] c = centre(seed, Math.min(z, 72));
         double domed = z > 72 ? z - 72 : 0.0;

         for (int x = Mth.floor(c[0]) - reach; x <= Mth.ceil(c[0]) + reach; x++) {
            for (int y = Mth.floor(c[1]) - reach; y <= Mth.ceil(c[1]) + reach; y++) {
               double dx = x - c[0];
               double dy = y - c[1];
               double radial = Math.sqrt(dx * dx + dy * dy);
               double turns = (Math.atan2(dy, dx) / (Math.PI * 2) + 1.0) % 1.0;
               double d = Math.sqrt(radial * radial + domed * domed);
               double face = bore(seed, Math.min(z, 72), turns);
               if (!(d > face + 3.0)) {
                  pos.set(x, y, z);
                  if (d <= face) {
                     if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        placed++;
                     }
                  } else if (!BowelsHallway.insideBore(seed, x, y, z)) {
                     level.setBlock(pos, BowelsHallway.wallSkin(seed, rng, x, y, z), 2);
                     placed++;
                  }
               }
            }
         }
      }

      return placed;
   }
}
