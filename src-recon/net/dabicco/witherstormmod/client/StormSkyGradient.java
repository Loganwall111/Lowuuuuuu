package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormSkyGradient {
   private static final float[] DUSK = new float[]{0.239F, 0.192F, 0.341F};
   private static final float[] PURP = new float[]{0.29F, 0.145F, 0.42F};
   private static final float[] DEEP = new float[]{0.212F, 0.118F, 0.353F};
   private static final float[] MAGE = new float[]{0.52F, 0.14F, 0.47F};
   private static final float[] PINK = new float[]{0.69F, 0.2F, 0.62F};
   private static final float[] RED = new float[]{0.52F, 0.11F, 0.19F};
   private static final double RANGE = 1400.0;
   private static float strength;
   private static float yawDeg;
   private static float pitchDeg;
   private static float phase;
   private static boolean active;

   private StormSkyGradient() {
   }

   public static void update(Vec3 var0) {
      float var1 = 0.0F;
      float var2 = 0.0F;
      float var3 = 0.0F;
      float var4 = 0.0F;

      for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData var6 : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
         if (!(var6.phase < 4.5F)) {
            double var7 = var6.dispX - var0.x;
            double var9 = var6.dispZ - var0.z;
            double var11 = var6.dispY - var0.y;
            double var13 = Math.sqrt(var7 * var7 + var9 * var9);
            if (!(var13 > 1400.0)) {
               float var15 = var13 <= 700.0 ? 1.0F : smooth((float)(1.0 - (var13 - 700.0) / 700.0));
               float var16 = ramp(var6.phase, 4.45F, 4.9F);
               float var17 = var15 * var16;
               if (var17 > var1) {
                  var1 = var17;
                  var3 = (float)(Math.atan2(var11, Math.max(var13, 1.0)) * (180.0 / Math.PI));
                  var2 = (float)(Math.atan2(var9, var7) * (180.0 / Math.PI));
                  var4 = var6.phase;
               }
            }
         }
      }

      strength = strength + (var1 - strength) * 0.05F;
      if (strength < 0.003F) {
         strength = 0.0F;
      }

      if (var4 > 0.0F) {
         phase = var4;
         yawDeg = var2;
         pitchDeg = var3;
      }

      active = strength > 0.0F;
   }

   public static boolean active() {
      return active && DabyWSClientConfig.stormBackdrop;
   }

   public static float yaw() {
      return yawDeg;
   }

   public static float pitch() {
      return pitchDeg;
   }

   public static float phase() {
      return phase;
   }

   public static boolean fogStampActive() {
      return active;
   }

   public static int color() {
      float var0 = phase;
      float[][] var1 = new float[][]{
         {0.15F, 0.65F, 0.42F},
         {0.22F, 0.145F, 0.325F},
         {0.639F, 0.18F, 0.573F},
         {0.4F, 0.075F, 0.145F},
         {0.463F, 0.102F, 0.404F},
         {0.639F, 0.18F, 0.573F},
         {0.3F, 0.27F, 0.31F},
         {0.8F, 0.2F, 0.55F},
         {0.72F, 0.18F, 0.1F}
      };
      float[] var2 = new float[]{
         1.0F - ramp(var0, 5.05F, 5.12F),
         ramp(var0, 5.05F, 5.12F) * (1.0F - ramp(var0, 5.22F, 5.28F)),
         ramp(var0, 5.22F, 5.28F) * (1.0F - ramp(var0, 5.28F, 5.34F)),
         ramp(var0, 5.28F, 5.34F) * (1.0F - ramp(var0, 5.36F, 5.44F)),
         ramp(var0, 5.36F, 5.44F) * (1.0F - ramp(var0, 5.44F, 5.6F)),
         ramp(var0, 5.44F, 5.6F) * (1.0F - ramp(var0, 5.9F, 6.0F)),
         ramp(var0, 5.9F, 6.0F) * (1.0F - ramp(var0, 6.0F, 6.06F)),
         ramp(var0, 6.0F, 6.06F) * (1.0F - ramp(var0, 6.06F, 6.14F)),
         ramp(var0, 6.06F, 6.14F) * (1.0F - ramp(var0, 6.9F, 7.1F)) + ramp(var0, 6.9F, 7.1F)
      };
      float var3 = 0.0F;
      float var4 = 0.0F;
      float var5 = 0.0F;
      float var6 = 0.0F;

      for (int var7 = 0; var7 < 9; var7++) {
         var3 += var2[var7];
         var4 += var1[var7][0] * var2[var7];
         var5 += var1[var7][1] * var2[var7];
         var6 += var1[var7][2] * var2[var7];
      }

      if (var3 <= 1.0E-4F) {
         var4 = var1[0][0];
         var5 = var1[0][1];
         var6 = var1[0][2];
      } else {
         var4 /= var3;
         var5 /= var3;
         var6 /= var3;
      }

      float var16 = 1.0F - 0.22F * ramp(var0, 4.45F, 5.6F);
      var4 *= var16;
      var5 *= var16;
      var6 *= var16;
      float var8 = (float)DabyWSClientConfig.stormBackdropStrength;
      float var9 = Mth.clamp(Math.max(strength * var8, 0.35F + 0.45F * strength), 0.0F, 1.0F);
      return (int)(var9 * 255.0F) << 24
         | (int)(Mth.clamp(var4, 0.0F, 1.0F) * 255.0F) << 16
         | (int)(Mth.clamp(var5, 0.0F, 1.0F) * 255.0F) << 8
         | (int)(Mth.clamp(var6, 0.0F, 1.0F) * 255.0F);
   }

   private static float ramp(float var0, float var1, float var2) {
      if (var2 <= var1) {
         return var0 >= var2 ? 1.0F : 0.0F;
      } else {
         return smooth(Mth.clamp((var0 - var1) / (var2 - var1), 0.0F, 1.0F));
      }
   }

   private static float smooth(float var0) {
      var0 = Mth.clamp(var0, 0.0F, 1.0F);
      return var0 * var0 * (3.0F - 2.0F * var0);
   }
}
