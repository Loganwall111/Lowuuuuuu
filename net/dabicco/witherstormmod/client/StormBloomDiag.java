package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;

public final class StormBloomDiag {
   private static final int INTERVAL_FRAMES = 100;
   private static final int MAX_REPORTS = 8;
   private static final int SAMPLE = 1024;
   private static int reports;
   private static int frame;
   private static boolean enabled;

   private StormBloomDiag() {
   }

   public static void setEnabled(boolean on) {
      if (on && !enabled) {
         reports = 0;
      }

      enabled = on;
   }

   public static boolean wanted() {
      return enabled && reports < 8 && frame % 100 == 0;
   }

   public static void tick() {
      ++frame;
   }

   public static void report(RenderTarget data, RenderTarget source, RenderTarget sceneSrc) {
      if (enabled && reports < 8) {
         int w = Math.min(1024, data.width);
         int h = Math.min(1024, data.height);
         if (w > 0 && h > 0) {
            int x = Math.max(0, (data.width - w) / 2);
            int y = Math.max(0, (data.height - h) / 2);
            int var10000 = source.width;
            String sizes = "bloom=" + var10000 + "x" + source.height + " sceneDepthSrc=" + (sceneSrc == null ? "NULL" : sceneSrc.width + "x" + sceneSrc.height) + " maskTarget=" + data.width + "x" + data.height;
            long bytes = (long)w * (long)h * 8L;

            try {
               GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "dabyws bloom diag", 9, bytes);
               RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(data.getColorTexture(), buffer, 0L, () -> {
                  try {
                     print(buffer, w, h, sizes);
                  } finally {
                     buffer.close();
                  }

               }, 0, x, y, w, h);
            } catch (Exception e) {
               System.out.println("[dabywitherstormmod][diag] readback failed: " + String.valueOf(e));
            }

         }
      }
   }

   private static void print(GpuBuffer buffer, int w, int h, String sizes) {
      try {
         GpuBufferSlice.MappedView view = buffer.map(true, false);

         label100: {
            try {
               ByteBuffer bb = view.data();
               int emitters = 0;
               int sceneZero = 0;
               int visible = 0;
               int hidden = 0;
               float sumAbsDiffVisible = 0.0F;
               StringBuilder samples = new StringBuilder();
               int printed = 0;

               for(int i = 0; i < w * h; ++i) {
                  int o = i * 8;
                  float bloomD = half(bb.getShort(o) & '\uffff');
                  float sceneD = half(bb.getShort(o + 2) & '\uffff');
                  float vis = half(bb.getShort(o + 4) & '\uffff');
                  if (!(bloomD <= 0.0F)) {
                     ++emitters;
                     if (sceneD == 0.0F) {
                        ++sceneZero;
                     }

                     if (vis > 0.5F) {
                        ++visible;
                        sumAbsDiffVisible += Math.abs(sceneD - bloomD);
                     } else {
                        ++hidden;
                     }

                     if (printed < 6) {
                        ++printed;
                        samples.append(String.format("%n    px(%d,%d) bloomDepth=%.6f sceneDepth=%.6f -> %s", i % w, i / w, bloomD, sceneD, vis > 0.5F ? "VISIBLE" : "occluded"));
                     }
                  }
               }

               ++reports;
               String tag = "[dabywitherstormmod][diag] ";
               if (emitters == 0) {
                  System.out.println(tag + "no emitter pixels in the sampled " + w + "x" + h + " crop. Centre the storm's head in view before trusting this: a crop that misses the head looks identical to an emitter pass that never wrote depth. " + sizes);
                  break label100;
               }

               System.out.println(tag + emitters + " emitter px (" + visible + " visible, " + hidden + " occluded), sceneDepth==0 at " + sceneZero + " of them. " + sizes + String.valueOf(samples));
               if (sceneZero == emitters) {
                  System.out.println(tag + "VERDICT: sceneDepth is 0 at EVERY emitter pixel -- the depth texture being sampled is empty. Wrong texture, or it is not populated at the time the mask runs. Occlusion cannot work.");
               } else if (visible > 0 && sumAbsDiffVisible / (float)visible > 0.02F) {
                  System.out.println(tag + "VERDICT: for VISIBLE teeth the two depths differ by " + String.format("%.4f", sumAbsDiffVisible / (float)visible) + " on average. The same tooth is in both buffers, so they should nearly match -- they do not, which means the two textures are NOT being sampled at the same place (UV / resolution mismatch).");
               } else if (hidden == 0) {
                  System.out.println(tag + "VERDICT: no emitter pixel was classified as occluded. If teeth are visibly behind terrain right now, the comparison itself is wrong (inverted test or an oversized epsilon).");
               } else {
                  System.out.println(tag + "VERDICT: depths look sane and the comparison is separating visible from occluded. If glow still leaks, it is happening AFTER the mask -- look at the blur/composite, not the depth.");
               }
            } catch (Throwable var19) {
               if (view != null) {
                  try {
                     view.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }
               }

               throw var19;
            }

            if (view != null) {
               view.close();
            }

            return;
         }

         if (view != null) {
            view.close();
         }

      } catch (Exception e) {
         System.out.println("[dabywitherstormmod][diag] map failed: " + String.valueOf(e));
      }
   }

   private static float half(int bits) {
      int exp = bits >>> 10 & 31;
      int frac = bits & 1023;
      float v;
      if (exp == 0) {
         v = (float)frac * (float)Math.pow((double)2.0F, (double)-24.0F);
      } else if (exp == 31) {
         v = frac == 0 ? Float.POSITIVE_INFINITY : Float.NaN;
      } else {
         v = (1.0F + (float)frac / 1024.0F) * (float)Math.pow((double)2.0F, (double)(exp - 15));
      }

      return (bits >>> 15 & 1) == 1 ? -v : v;
   }
}
