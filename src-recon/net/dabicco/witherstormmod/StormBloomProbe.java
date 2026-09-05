package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice.MappedView;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;

public final class StormBloomProbe {
   private static final int INTERVAL_FRAMES = 120;
   private static final int MAX_REPORTS = 6;
   private static int reports;
   private static final int SAMPLE = 1024;
   private static int frame;

   private StormBloomProbe() {
   }

   public static void probe(RenderTarget target) {
      if (target != null && frame++ % 120 == 0 && reports < 6) {
         int tw = target.width;
         int th = target.height;
         int w = Math.min(1024, target.width);
         int h = Math.min(1024, target.height);
         if (w > 0 && h > 0) {
            int x = Math.max(0, (target.width - w) / 2);
            int y = Math.max(0, (target.height - h) / 2);
            long bytes = (long)w * h * 8L;

            try {
               GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "dabyws bloom probe", 9, bytes);
               RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(target.getColorTexture(), buffer, 0L, () -> {
                  try {
                     report(buffer, w, h, tw, th);
                  } finally {
                     buffer.close();
                  }
               }, 0, x, y, w, h);
            } catch (Exception var10) {
               System.out.println("[dabywitherstormmod] bloom probe failed: " + var10);
            }
         }
      }
   }

   private static float halfToFloat(int bits) {
      int sign = bits >>> 15 & 1;
      int exp = bits >>> 10 & 31;
      int frac = bits & 1023;
      float value;
      if (exp == 0) {
         value = frac * (float)Math.pow(2.0, -24.0);
      } else if (exp == 31) {
         value = frac == 0 ? Float.POSITIVE_INFINITY : Float.NaN;
      } else {
         value = (1.0F + frac / 1024.0F) * (float)Math.pow(2.0, exp - 15);
      }

      return sign == 1 ? -value : value;
   }

   private static void report(GpuBuffer buffer, int w, int h, int fullW, int fullH) {
      try {
         MappedView view = buffer.map(true, false);

         try {
            ByteBuffer data = view.data();
            int maxR = 0;
            int maxG = 0;
            int maxB = 0;
            int lit = 0;
            int pixels = w * h;

            for (int i = 0; i < pixels; i++) {
               int base = i * 8;
               int r = data.getShort(base) & '\uffff';
               int g = data.getShort(base + 2) & '\uffff';
               int b = data.getShort(base + 4) & '\uffff';
               if (r != 0 || g != 0 || b != 0) {
                  lit++;
               }

               maxR = Math.max(maxR, r);
               maxG = Math.max(maxG, g);
               maxB = Math.max(maxB, b);
            }

            reports++;
            if (lit == 0) {
               System.out
                  .println(
                     "[dabywitherstormmod] no lit pixels over the centre "
                        + w
                        + "x"
                        + h
                        + " of "
                        + fullW
                        + "x"
                        + fullH
                        + " ("
                        + 100 * w / Math.max(1, fullW)
                        + "% of width) -- if the storm is off to one side this crop can miss it entirely; centre the head and re-check before concluding the buffer is empty. The blur and the composite are irrelevant until this reports pixels."
                  );
            } else {
               float peak = Math.max(halfToFloat(maxR), Math.max(halfToFloat(maxG), halfToFloat(maxB)));
               System.out
                  .println(
                     "[dabywitherstormmod] bloom buffer OK: "
                        + lit
                        + "/"
                        + pixels
                        + " lit pixels in the centre "
                        + w
                        + "x"
                        + h
                        + ", peak channel "
                        + String.format("%.2f", peak)
                        + (peak > 1.0F ? " (HDR, good)" : " (NOT above 1.0 -- the source has no headroom, so the blur has nothing to spread)")
                  );
            }
         } catch (Throwable var181) {
            if (view != null) {
               try {
                  view.close();
               } catch (Throwable var171) {
                  var181.addSuppressed(var171);
               }
            }

            throw var181;
         }

         if (view != null) {
            view.close();
         }
      } catch (Exception var19) {
         System.out.println("[dabywitherstormmod] bloom probe read failed: " + var19);
      }
   }
}
