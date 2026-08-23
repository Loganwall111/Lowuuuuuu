package net.dabicco.devouringstorms.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public final class FormidibombFlash {
   private static final double FULL_RADIUS = (double)350.0F;
   private static final double SEE_RADIUS = (double)2000.0F;
   private static final float PEAK_TICKS = 2.0F;
   private static final float HOLD_TICKS = 100.0F;
   private static final float FADE_TICKS = 50.0F;
   private static long startTimeMs = -1L;
   private static float peakIntensity = 0.0F;
   private static float lifeTicks = 0.0F;

   private FormidibombFlash() {
   }

   public static void trigger(double x, double y, double z) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         double dist = Math.sqrt(mc.player.distanceToSqr(x, y, z));
         if (!(dist > (double)350.0F)) {
            peakIntensity = Mth.clamp(1.0F - 0.12F * (float)(dist / (double)350.0F), 0.0F, 1.0F);
            lifeTicks = 152.0F;
            startTimeMs = System.currentTimeMillis();
         }
      }
   }

   public static void clear() {
      startTimeMs = -1L;
      peakIntensity = 0.0F;
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      if (startTimeMs >= 0L && !(peakIntensity <= 0.0F)) {
         long now = System.currentTimeMillis();
         if (now >= startTimeMs) {
            float ticks = (float)(now - startTimeMs) / 50.0F;
            if (ticks > lifeTicks) {
               clear();
            } else {
               float a;
               if (ticks <= 2.0F) {
                  a = peakIntensity * (ticks / 2.0F);
               } else if (ticks <= 102.0F) {
                  a = peakIntensity;
               } else {
                  float t = (ticks - 2.0F - 100.0F) / 50.0F;
                  float fade = 1.0F - t;
                  a = peakIntensity * fade * fade;
               }

               int alpha = (int)(Mth.clamp(a, 0.0F, 1.0F) * 255.0F);
               if (alpha > 2) {
                  g.fill(0, 0, g.guiWidth(), g.guiHeight(), alpha << 24 | 16777215);
               }
            }
         }
      }
   }
}
