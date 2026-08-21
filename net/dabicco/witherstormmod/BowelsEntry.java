package net.dabicco.witherstormmod;

import net.minecraft.core.BlockPos;
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
   private static final double BORE_RAGGED = (double)0.25F;
   private static final int WALL = 3;
   private static final double INFLUENCE = 11.8;
   private static final double WANDER = 2.2;
   private static final double PULL_STOP_Z = (double)18.0F;
   private static final double CENTRE_GAIN = 0.22;
   private static final double CENTRE_MAX = 0.55;
   private static final double CENTRE_DAMP = 0.68;
   private static final long SALT_PATH = 1317123015L;
   private static final long SALT_BORE = 3527871131L;
   private static final long SALT_SKIN = 2349199982L;

   private BowelsEntry() {
   }

   public static boolean holds(double x, double y, double z) {
      if (!(z < (double)-2.0F) && !(z > (double)78.0F)) {
         double dx = x - (double)28.0F;
         double dy = y - (double)64.0F;
         return dx * dx + dy * dy < 139.24;
      } else {
         return false;
      }
   }

   public static Vec3 arrival() {
      return new Vec3((double)28.5F, (double)64.5F, (double)67.5F);
   }

   public static float arrivalYaw() {
      return 0.0F;
   }

   public static float arrivalPitch() {
      return 70.0F;
   }

   public static void centreDrop(ServerLevel level, Entity entity) {
      double z = entity.getZ();
      if (!(z < (double)18.0F) && !(z > (double)75.0F)) {
         double[] c = centreAt(level.getSeed(), z);
         double dx = c[0] - entity.getX();
         double dy = c[1] - entity.getY();
         double off = Math.sqrt(dx * dx + dy * dy);
         if (!(off < 0.001)) {
            if (!(off > 6.05)) {
               double toward = Math.min(off * 0.22, 0.55);
               Vec3 v = entity.getDeltaMovement();
               double wantX = v.x * 0.68 + dx / off * toward;
               double wantY = v.y * 0.68 + dy / off * toward;
               if (entity instanceof ServerPlayer) {
                  ServerPlayer player = (ServerPlayer)entity;
                  player.push(wantX - v.x, wantY - v.y, (double)0.0F);
                  player.hurtMarked = true;
               } else {
                  entity.setDeltaMovement(wantX, wantY, v.z);
               }

            }
         }
      }
   }

   static double outerFace(long seed, double z, double turns) {
      return bore(seed, Math.min(z, (double)72.0F), turns) + (double)3.0F;
   }

   static double[] centreAt(long seed, double z) {
      return centre(seed, Math.min(z, (double)72.0F));
   }

   private static double[] centre(long seed, double z) {
      double ramp = Mth.clamp(Math.min((double)72.0F - z, z) / (double)22.0F, (double)0.0F, (double)1.0F);
      double dx = 2.2 * (double)BowelsHallway.noise(seed ^ 1317123015L, z / (double)31.0F, (double)0.5F) * ramp;
      double dy = 2.2 * (double)BowelsHallway.noise(seed ^ 1317123015L, z / (double)31.0F, (double)7.5F) * ramp;
      return new double[]{(double)28.0F + dx, (double)64.0F + dy};
   }

   private static double bore(long seed, double z, double turns) {
      return 2.8 + (double)0.25F * (double)BowelsHallway.ringNoise(seed ^ 3527871131L, z / (double)4.0F, turns);
   }

   public static int build(ServerLevel level, long seed) {
      int reach = Mth.ceil(6.05) + 2;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 2349199982L);
      int placed = 0;

      for(int z = 0; z <= 77; ++z) {
         double[] c = centre(seed, (double)Math.min(z, 72));
         double domed = z > 72 ? (double)(z - 72) : (double)0.0F;

         for(int x = Mth.floor(c[0]) - reach; x <= Mth.ceil(c[0]) + reach; ++x) {
            for(int y = Mth.floor(c[1]) - reach; y <= Mth.ceil(c[1]) + reach; ++y) {
               double dx = (double)x - c[0];
               double dy = (double)y - c[1];
               double radial = Math.sqrt(dx * dx + dy * dy);
               double turns = (Math.atan2(dy, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               double d = Math.sqrt(radial * radial + domed * domed);
               double face = bore(seed, (double)Math.min(z, 72), turns);
               if (!(d > face + (double)3.0F)) {
                  pos.set(x, y, z);
                  if (d <= face) {
                     if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        ++placed;
                     }
                  } else if (!BowelsHallway.insideBore(seed, (double)x, (double)y, (double)z)) {
                     level.setBlock(pos, BowelsHallway.wallSkin(seed, rng, x, y, z), 2);
                     ++placed;
                  }
               }
            }
         }
      }

      return placed;
   }
}
