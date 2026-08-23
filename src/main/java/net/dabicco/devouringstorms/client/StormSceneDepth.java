package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.textures.GpuTextureView;

public final class StormSceneDepth {
   private static RenderTarget copy;
   private static boolean valid;

   private StormSceneDepth() {
   }

   public static GpuTextureView depthView() {
      return copy != null && valid ? copy.getDepthTextureView() : null;
   }

   public static RenderTarget target() {
      return valid ? copy : null;
   }

   public static void beginFrame() {
      valid = false;
   }

   public static void capture(RenderTarget src) {
      if (src != null && src.useDepth) {
         int w = src.width;
         int h = src.height;
         if (w > 0 && h > 0) {
            if (copy == null) {
               copy = new TextureTarget("dabyws_scene_depth", w, h, true, GpuFormat.RGBA8_UNORM);
            } else if (copy.width != w || copy.height != h) {
               copy.resize(w, h);
            }

            copy.copyDepthFrom(src);
            valid = true;
         }
      }
   }

   public static void close() {
      if (copy != null) {
         copy.destroyBuffers();
         copy = null;
      }

      valid = false;
   }
}
