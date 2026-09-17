package net.dabicco.witherstormmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class CaveRumbleClient {
   private static long startTick = Long.MIN_VALUE;
   private static int duration;
   private static float intensity;
   private static final float MAX_DEGREES = 1.35F;
   private static final int FADE_IN = 10;
   private static final int FADE_OUT = 40;

   private CaveRumbleClient() {
   }

   public static void begin(int durationTicks, float amount) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         startTick = mc.level.getGameTime();
         duration = durationTicks;
         intensity = amount;
      }
   }

   public static void stop() {
      startTick = Long.MIN_VALUE;
   }

   private static float envelope(float elapsed) {
      if (startTick != Long.MIN_VALUE && !(elapsed < 0.0F) && !(elapsed >= duration)) {
         float in = Mth.clamp(elapsed / 10.0F, 0.0F, 1.0F);
         float out = Mth.clamp((duration - elapsed) / 40.0F, 0.0F, 1.0F);
         return Math.min(in, out) * intensity;
      } else {
         return 0.0F;
      }
   }

   public static float[] offset(float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && startTick != Long.MIN_VALUE) {
         float elapsed = (float)(mc.level.getGameTime() - startTick) + partialTick;
         float env = envelope(elapsed);
         if (env <= 0.001F) {
            return null;
         } else {
            float a = 1.35F * env;
            return new float[]{
               a * (Mth.sin(elapsed * 1.31F) * 0.6F + Mth.sin(elapsed * 2.77F) * 0.4F),
               a * (Mth.sin(elapsed * 1.09F) * 0.6F + Mth.sin(elapsed * 3.13F) * 0.4F),
               a * 0.7F * (Mth.sin(elapsed * 0.83F) * 0.6F + Mth.sin(elapsed * 2.29F) * 0.4F)
            };
         }
      } else {
         return null;
      }
   }
}
