package net.dabicco.devouringstorms.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * McsmSky — the Story-Mode look for the REGULAR game (no storm).
 *
 * While no phase-4+ storm owns the sky, this drives:
 *  - the vanilla sky-disc colour toward the MCSM dual-tone palette:
 *    day = lavender/purple upper sky (#7B68EE -> #9370DB) melting into a
 *    light cyan/powder-blue horizon (#87CEFA);
 *    night = deep indigo/navy dome with a glowing bright-cyan horizon band;
 *  - the horizon glow accent ring + the soft moon bloom halo, drawn by
 *    StormSkyBox.renderMainSkyAccents() at the tail of the vanilla celestial
 *    pass;
 *  - the time-of-day tint the clouds wear (CloudColorMixin reads the same
 *    palette, so the clouds stay the SAME clouds and only their colour
 *    follows the clock — and the storm phases whenever a storm takes over).
 *
 * The wither-storm skybox itself is untouched: whenever
 * SkyAtmosphereController.active() is true the storm palette owns everything
 * and this class stands down.
 */
public final class McsmSky {
   /** Lavender upper sky (day). */
   private static final float[] DAY_UPPER = new float[]{0.482F, 0.408F, 0.933F};
   /** Medium purple upper sky (day, secondary stop). */
   private static final float[] DAY_UPPER_2 = new float[]{0.576F, 0.439F, 0.859F};
   /** Powder-blue / light-cyan horizon (day). */
   private static final float[] DAY_HORIZON = new float[]{0.529F, 0.808F, 0.980F};
   /** Deep indigo/navy upper dome (night). */
   private static final float[] NIGHT_UPPER = new float[]{0.043F, 0.055F, 0.165F};
   /** Glowing bright-cyan horizon emission (night). */
   private static final float[] NIGHT_HORIZON = new float[]{0.290F, 0.870F, 0.910F};
   /** Dawn/dusk warm kiss for the horizon ring. */
   private static final float[] DUSK_HORIZON = new float[]{0.980F, 0.620F, 0.420F};

   /** Captured from the sky render state each frame (SkyRendererMixin). */
   private static float moonAngle;
   private static float rainBrightness = 1.0F;

   private McsmSky() {
   }

   public static void capture(float moonAngleRad, float rainBright) {
      moonAngle = moonAngleRad;
      rainBrightness = rainBright;
   }

   public static float moonAngle() {
      return moonAngle;
   }

   public static float rainBrightness() {
      return rainBrightness;
   }

   /** 0 at dawn/dusk transitions... 1 in the middle of the day block. */
   public static float dayFactor(long time) {
      long t = ((time % 24000L) + 24000L) % 24000L;
      if (t < 11000L) {
         return Mth.clamp((float)t / 1600.0F, 0.0F, 1.0F);
      }
      return Mth.clamp((float)(13500L - t) / 1600.0F, 0.0F, 1.0F);
   }

   /** 0..1 through the night, ramping in after sunset and out before dawn. */
   public static float nightFactor(long time) {
      long t = ((time % 24000L) + 24000L) % 24000L;
      if (t >= 12500L && t <= 23500L) {
         float in = (float)(t - 12500L) / 1000.0F;
         float out = (float)(23500L - t) / 1000.0F;
         return Mth.clamp(Math.min(in, out), 0.0F, 1.0F);
      }
      return 0.0F;
   }

   /** Peaks around sunrise/sunset (~ 500 and ~12500). */
   public static float duskFactor(long time) {
      long t = ((time % 24000L) + 24000L) % 24000L;
      float morn = Math.max(0.0F, 1.0F - Math.abs((float)t - 500.0F) / 1800.0F);
      float eve = Math.max(0.0F, 1.0F - Math.abs((float)t - 12500.0F) / 1800.0F);
      return Math.max(morn, eve);
   }

   /** Blend the vanilla sky-disc colour toward the MCSM day/night dome. */
   public static int blendSkyColor(int argb, long time) {
      float day = dayFactor(time);
      float night = nightFactor(time);
      float r = ARGB.red(argb) / 255.0F;
      float g = ARGB.green(argb) / 255.0F;
      float b = ARGB.blue(argb) / 255.0F;
      if (day > 0.0F) {
         float[] target = new float[]{(DAY_UPPER[0] + DAY_UPPER_2[0]) * 0.5F, (DAY_UPPER[1] + DAY_UPPER_2[1]) * 0.5F, (DAY_UPPER[2] + DAY_UPPER_2[2]) * 0.5F};
         float mix = 0.42F * day;
         r = Mth.lerp(mix, r, target[0]);
         g = Mth.lerp(mix, g, target[1]);
         b = Mth.lerp(mix, b, target[2]);
      }

      if (night > 0.0F) {
         float mix = 0.62F * night;
         r = Mth.lerp(mix, r, NIGHT_UPPER[0]);
         g = Mth.lerp(mix, g, NIGHT_UPPER[1]);
         b = Mth.lerp(mix, b, NIGHT_UPPER[2]);
      }

      return ARGB.color(ARGB.alpha(argb), Mth.floor(r * 255.0F), Mth.floor(g * 255.0F), Mth.floor(b * 255.0F));
   }

   /** Blend the sunrise/sunset (horizon) colour toward cyan day / cyan night / warm dusk. */
   public static int blendHorizonColor(int argb, long time) {
      float day = dayFactor(time);
      float night = nightFactor(time);
      float dusk = duskFactor(time);
      float r = ARGB.red(argb) / 255.0F;
      float g = ARGB.green(argb) / 255.0F;
      float b = ARGB.blue(argb) / 255.0F;
      float[] target = DAY_HORIZON;
      float mix = 0.35F * day;
      if (night > day) {
         target = NIGHT_HORIZON;
         mix = 0.45F * night;
      }

      r = Mth.lerp(mix, r, target[0]);
      g = Mth.lerp(mix, g, target[1]);
      b = Mth.lerp(mix, b, target[2]);
      if (dusk > 0.0F) {
         float k = 0.5F * dusk;
         r = Mth.lerp(k, r, DUSK_HORIZON[0]);
         g = Mth.lerp(k, g, DUSK_HORIZON[1]);
         b = Mth.lerp(k, b, DUSK_HORIZON[2]);
      }

      return ARGB.color(ARGB.alpha(argb), Mth.floor(r * 255.0F), Mth.floor(g * 255.0F), Mth.floor(b * 255.0F));
   }

   /** The tint the horizon glow accent ring wears right now (rgb 0..1 + strength). */
   public static float[] horizonGlowTint(long time) {
      float day = dayFactor(time);
      float night = nightFactor(time);
      float dusk = duskFactor(time);
      float r = Mth.lerp(day, DAY_HORIZON[0], DAY_HORIZON[0]);
      float g = Mth.lerp(day, DAY_HORIZON[1], DAY_HORIZON[1]);
      float b = Mth.lerp(day, DAY_HORIZON[2], DAY_HORIZON[2]);
      if (night > 0.0F) {
         float k = Math.max(night, 0.35F);
         r = Mth.lerp(k, r, NIGHT_HORIZON[0]);
         g = Mth.lerp(k, g, NIGHT_HORIZON[1]);
         b = Mth.lerp(k, b, NIGHT_HORIZON[2]);
      }

      if (dusk > 0.0F) {
         r = Mth.lerp(dusk * 0.6F, r, DUSK_HORIZON[0]);
         g = Mth.lerp(dusk * 0.6F, g, DUSK_HORIZON[1]);
         b = Mth.lerp(dusk * 0.6F, b, DUSK_HORIZON[2]);
      }

      float strength = Math.max(day, Math.max(night, dusk * 0.8F));
      return new float[]{r, g, b, strength};
   }

   /** The tint the clouds wear by time of day (storm palettes override this). */
   public static float[] cloudTint(long time) {
      float day = dayFactor(time);
      float night = nightFactor(time);
      float dusk = duskFactor(time);
      float r = Mth.lerp(night * 0.65F, 1.0F, 0.34F);
      float g = Mth.lerp(night * 0.65F, 1.0F, 0.38F);
      float b = Mth.lerp(night * 0.65F, 1.0F, 0.56F);
      if (day > 0.0F) {
         float k = 0.16F * day;
         r = Mth.lerp(k, r, 0.95F);
         g = Mth.lerp(k, g, 0.93F);
         b = Mth.lerp(k, b, 1.0F);
      }

      if (dusk > 0.0F) {
         float k = 0.35F * dusk;
         r = Mth.lerp(k, r, 1.0F);
         g = Mth.lerp(k, g, 0.80F);
         b = Mth.lerp(k, b, 0.66F);
      }

      return new float[]{r, g, b};
   }
}
