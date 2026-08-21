package net.dabicco.witherstormmod.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Clean rewrite of the Wither Storm death cinematic (matches the video finale).
 *
 * Sequence, in ticks:
 *  0-6     white pulse ramps in
 *  6-30    pure white hold (world is completely white)
 *  30-52   white fades out, revealing the aftermath
 *  parallel: a screen "glitch" (RGB channel offset) flickers during the blast, and
 *  purple glass shard particles are spawned server-side by the storm entity.
 *
 * The purple shards themselves are spawned as particles by the server when the storm
 * dies; this class only drives the screen-wide white flash + glitch so it can be a
 * self-contained client effect (registered via HudElementRegistry).
 */
public final class StormDeathCinematic {
   private static final int PULSE_TICKS = 6;
   private static final int HOLD_TICKS = 24;
   private static final int FADE_TICKS = 22;
   private static final int TOTAL_TICKS = PULSE_TICKS + HOLD_TICKS + FADE_TICKS;

   private static long startMs = -1L;
   private static float intensity = 1.0F;

   private StormDeathCinematic() {
   }

   public static void trigger(double x, double y, double z, boolean fromBomb) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return;
      }
      double dist = Math.sqrt(mc.player.distanceToSqr(x, y, z));
      // Full white only if close enough to be overwhelmed by the blast; scale by distance.
      float reach = fromBomb ? 160.0F : 200.0F;
      intensity = Mth.clamp(1.0F - (float) (dist / reach) * 0.35F, 0.6F, 1.0F);
      startMs = System.currentTimeMillis();
   }

   public static void clear() {
      startMs = -1L;
   }

   public static boolean active() {
      return startMs >= 0L;
   }

   /** Draw the white flash + glitch overlay (call from a HUD element). */
   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      if (startMs < 0L) {
         return;
      }
      long now = System.currentTimeMillis();
      if (now < startMs) {
         return;
      }
      float ticks = (float) (now - startMs) / 50.0F;
      if (ticks > TOTAL_TICKS) {
         clear();
         return;
      }

      int w = g.guiWidth();
      int h = g.guiHeight();
      float a;
      if (ticks <= PULSE_TICKS) {
         a = intensity * (ticks / PULSE_TICKS);
      } else if (ticks <= PULSE_TICKS + HOLD_TICKS) {
         a = intensity;
      } else {
         float t = (ticks - PULSE_TICKS - HOLD_TICKS) / FADE_TICKS;
         a = intensity * (1.0F - t);
      }
      int alpha = (int) (Mth.clamp(a, 0.0F, 1.0F) * 255.0F);
      if (alpha > 2) {
         g.fill(0, 0, w, h, alpha << 24 | 0xFFFFFF);
      }

      // Glitch: brief RGB-channel offset flashes during the pulse/fade for a
      // "glitching out" effect like in the video.
      if (ticks <= PULSE_TICKS + 4 || (ticks >= PULSE_TICKS + HOLD_TICKS - 3 && ticks <= TOTAL_TICKS)) {
         if ((int) ticks % 3 == 0) {
            int off = 6 + (int) ticks % 10;
            g.fill(off, 0, w, h, 0x40FF0000);
            g.fill(0, 0, w - off, h, 0x4000FF00);
         }
      }
   }
}
