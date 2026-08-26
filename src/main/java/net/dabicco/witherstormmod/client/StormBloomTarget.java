package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.dabicco.witherstormmod.mixin.LevelRendererTargetsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import org.joml.Vector4f;

public final class StormBloomTarget {
   private static final Vector4f CLEAR = new Vector4f(0.0F, 0.0F, 0.0F, 0.0F);
   private static SharedDepthTarget target;
   private static OutputTarget outputTarget;
   private static RenderTarget capturedScene;
   private static EraseTarget eraseTarget;
   private static OutputTarget eraseOutput;

   private StormBloomTarget() {
   }

   public static RenderTarget capturedScene() {
      return capturedScene;
   }

   public static RenderTarget eraseTarget() {
      return eraseTarget;
   }

   public static OutputTarget eraseOutputTarget() {
      if (eraseOutput == null) {
         eraseOutput = new OutputTarget("dabyws_bloom_erase", StormBloomTarget::eraseTarget);
      }

      return eraseOutput;
   }

   public static RenderTarget target() {
      return target;
   }

   public static OutputTarget outputTarget() {
      if (outputTarget == null) {
         outputTarget = new OutputTarget("dabyws_bloom", StormBloomTarget::target);
      }

      return outputTarget;
   }

   public static void beginFrame(Minecraft mc) {
      RenderTarget main = mc.gameRenderer.mainRenderTarget();
      int w = main.width;
      int h = main.height;
      if (w > 0 && h > 0) {
         if (target == null) {
            target = new SharedDepthTarget("dabyws_bloom", w, h, GpuFormat.RGBA16_FLOAT);
            eraseTarget = new EraseTarget();
            eraseTarget.createBuffers(w, h);
         } else if (target.width != w || target.height != h) {
            target.resize(w, h);
            eraseTarget.createBuffers(w, h);
         }

         RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(target.getColorTexture(), CLEAR, target.ownDepthTexture(), (double)0.0F);
      }
   }

   public static void close() {
      if (target != null) {
         target.destroyBuffers();
         target = null;
      }

   }

   private static final class SharedDepthTarget extends TextureTarget {
      SharedDepthTarget(String label, int width, int height, GpuFormat format) {
         super(label, width, height, true, format);
      }

      GpuTexture ownDepthTexture() {
         return super.getDepthTexture();
      }
   }

   private static final class EraseTarget extends RenderTarget {
      EraseTarget() {
         super("dabyws_bloom_erase", true, GpuFormat.RGBA16_FLOAT);
      }

      public void createBuffers(int width, int height) {
         this.width = width;
         this.height = height;
      }

      private static RenderTarget scene() {
         Minecraft mc = Minecraft.getInstance();

         try {
            LevelTargetBundle targets = ((LevelRendererTargetsAccessor)mc.levelRenderer).dabyws$targets();
            if (targets != null && targets.main != null) {
               RenderTarget t = (RenderTarget)targets.main.get();
               if (t != null && t.useDepth) {
                  StormBloomTarget.capturedScene = t;
                  return t;
               }
            }
         } catch (Throwable var3) {
         }

         return mc.gameRenderer == null ? null : mc.gameRenderer.mainRenderTarget();
      }

      public GpuTexture getColorTexture() {
         return StormBloomTarget.target == null ? null : StormBloomTarget.target.getColorTexture();
      }

      public GpuTextureView getColorTextureView() {
         return StormBloomTarget.target == null ? null : StormBloomTarget.target.getColorTextureView();
      }

      public GpuTexture getDepthTexture() {
         RenderTarget s = scene();
         if (s != null && s.useDepth && s.width == this.width && s.height == this.height) {
            return s.getDepthTexture();
         } else {
            return StormBloomTarget.target == null ? null : StormBloomTarget.target.getDepthTexture();
         }
      }

      public GpuTextureView getDepthTextureView() {
         RenderTarget s = scene();
         if (s != null && s.useDepth && s.width == this.width && s.height == this.height) {
            return s.getDepthTextureView();
         } else {
            return StormBloomTarget.target == null ? null : StormBloomTarget.target.getDepthTextureView();
         }
      }
   }
}
