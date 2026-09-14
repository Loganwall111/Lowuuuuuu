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

   public static void update(Vec3 camera) {
      float selected = 0.0F;
      float selectedYaw = 0.0F;
      float selectedPitch = 0.0F;
      net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData state =
         ClientDistantStormManager.nearestCustomWeather(camera);
      if (state != null) {
         Vec3 origin = state.getStormOrigin();
         double dx = origin.x - camera.x;
         double dy = origin.y - camera.y;
         double dz = origin.z - camera.z;
         double horizontal = Math.sqrt(dx * dx + dz * dz);
         if (horizontal <= 1400.0D) {
            float distanceWeight = horizontal <= 700.0D
               ? 1.0F : smooth((float) (1.0D - (horizontal - 700.0D) / 700.0D));
            selected = distanceWeight * ramp(state.phase, 5.0F, 5.12F);
            selectedYaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI));
            selectedPitch = (float) (Math.atan2(dy, Math.max(horizontal, 1.0D)) * (180.0D / Math.PI));
         }
      }

      if (selected <= 0.0F) {
         strength = 0.0F;
         phase = 0.0F;
         active = false;
         return;
      }
      strength += (selected - strength) * 0.05F;
      if (strength < 0.003F) strength = 0.0F;
      if (selected > 0.0F) {
         phase = state.phase;
         yawDeg = selectedYaw;
         pitchDeg = selectedPitch;
      } else if (strength <= 0.0F) {
         phase = 0.0F;
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
