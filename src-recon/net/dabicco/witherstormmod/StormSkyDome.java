package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormSkyDome {
   private static final float[] TURQ = new float[]{0.094F, 0.184F, 0.18F};
   private static final float[] PURP = new float[]{0.22F, 0.145F, 0.325F};
   private static final float[] MAGE = new float[]{0.463F, 0.102F, 0.404F};
   private static final float[] PINK = new float[]{0.639F, 0.18F, 0.573F};
   private static final float[] RED = new float[]{0.4F, 0.075F, 0.145F};
   private static final double RANGE = 900.0;
   private static float displayed;
   private static float displayedCore;
   private static float phaseSeen;

   private StormSkyDome() {
   }

   public static void update(Vec3 var0) {
      float var1 = 0.0F;
      float var2 = 0.0F;
      float var3 = 0.0F;

      for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData var5 : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
         if (!(var5.phase < 4.5F)) {
            double var6 = var5.dispX - var0.x;
            double var8 = var5.dispY - var0.y;
            double var10 = var5.dispZ - var0.z;
            double var12 = Math.sqrt(var6 * var6 + var8 * var8 + var10 * var10);
            if (!(var12 > 900.0)) {
               double var14 = var12 / 900.0;
               float var16 = var14 <= 0.55 ? 1.0F : smooth((float)(1.0 - (var14 - 0.55) / 0.45));
               float var17 = ramp(var5.phase, 4.45F, 4.9F);
               float var18 = var16 * var17;
               if (var18 > var1) {
                  var1 = var18;
                  var3 = var5.phase;
               }

               var2 = Math.max(var2, var16 * ramp(var5.phase, 4.45F, 5.2F));
            }
         }
      }

      displayed = displayed + (var1 - displayed) * 0.05F;
      displayedCore = displayedCore + (var2 - displayedCore) * 0.05F;
      if (displayed < 0.002F) {
         displayed = 0.0F;
      }

      if (displayedCore < 0.002F) {
         displayedCore = 0.0F;
      }

      if (var3 > 0.0F) {
         phaseSeen = var3;
      }
   }

   public static float strength() {
      return !DabyWSClientConfig.stormBackdrop ? 0.0F : Mth.clamp(displayed * (float)DabyWSClientConfig.stormBackdropStrength, 0.0F, 1.0F);
   }

   public static float coreStrength() {
      return Mth.clamp(displayedCore, 0.0F, 1.0F);
   }

   public static float phase() {
      return phaseSeen;
   }

   public static void skyColor(float[] var0) {
      float var1 = phaseSeen;
      float var2 = 1.0F - ramp(var1, 5.04F, 5.12F);
      float var3 = ramp(var1, 5.04F, 5.12F) * (1.0F - ramp(var1, 5.15F, 5.23F));
      float var4 = ramp(var1, 5.15F, 5.23F) * (1.0F - ramp(var1, 5.26F, 5.34F));
      float var5 = ramp(var1, 5.26F, 5.34F) * (1.0F - ramp(var1, 5.42F, 5.52F));
      float var6 = ramp(var1, 5.42F, 5.52F) * (1.0F - ramp(var1, 5.96F, 6.1F));
      float var7 = ramp(var1, 5.96F, 6.1F) * (1.0F - ramp(var1, 6.85F, 7.05F));
      float var8 = ramp(var1, 6.85F, 7.05F) * (1.0F - ramp(var1, 7.8F, 8.0F));
      float var9 = ramp(var1, 7.8F, 8.0F);
      float var10 = var2 + var3 + var4 + var5 + var6 + var7 + var8 + var9;
      if (var10 <= 1.0E-4F) {
         var0[0] = TURQ[0];
         var0[1] = TURQ[1];
         var0[2] = TURQ[2];
      } else {
         float var11 = 0.16F * var2 + 0.52F * var3 + 0.66F * var4 + 0.16F * var5 + 0.34F * var6 + 0.353F * var7 + 0.52F * var8 + 0.2F * var9;
         float var12 = 0.58F * var2 + 0.18F * var3 + 0.24F * var4 + 0.04F * var5 + 0.1F * var6 + 0.235F * var7 + 0.3F * var8 + 0.02F * var9;
         float var13 = 0.62F * var2 + 0.52F * var3 + 0.6F * var4 + 0.24F * var5 + 0.34F * var6 + 0.345F * var7 + 0.46F * var8 + 0.05F * var9;
         var0[0] = var11 / var10;
         var0[1] = var12 / var10;
         var0[2] = var13 / var10;
      }
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
