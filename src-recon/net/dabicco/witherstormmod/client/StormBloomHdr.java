package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.minecraft.resources.Identifier;

public final class StormBloomHdr {
   public static final int DEBUG_OFF = 0;
   public static final int DEBUG_SOURCE = 1;
   public static final int DEBUG_SCENE_DEPTH = 2;
   public static final int DEBUG_BLOOM_DEPTH = 3;
   public static final int DEBUG_MASK = 4;
   public static final int DEBUG_TIGHT_H = 5;
   public static final int DEBUG_TIGHT_V = 6;
   public static final int DEBUG_WIDE_H = 7;
   public static final int DEBUG_BLOOM = 8;
   public static final int DEBUG_UV_CHECK = 9;
   private static ByteBuffer uniformStaging;
   private static RenderTarget tightA;
   private static RenderTarget tightB;
   private static RenderTarget wide;
   private static RenderPipeline blurPipeline;
   private static RenderPipeline compositePipeline;
   private static RenderPipeline debugPipeline;
   private static RenderPipeline maskPipeline;

   private StormBloomHdr() {
   }

   private static ByteBuffer staging(int bytes) {
      if (uniformStaging == null || uniformStaging.capacity() < bytes) {
         uniformStaging = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
      }

      uniformStaging.clear();
      return uniformStaging;
   }

   private static RenderPipeline maskPipeline() {
      if (maskPipeline == null) {
         maskPipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()})
            .withLocation(id("pipeline/storm_hdr_mask"))
            .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
            .withFragmentShader(id("post/storm_hdr_mask"))
            .withBindGroupLayout(
               BindGroupLayout.builder()
                  .withSampler("InSampler")
                  .withSampler("BloomDepthSampler")
                  .withSampler("SceneDepthSampler")
                  .withUniform("MaskConfig", UniformType.UNIFORM_BUFFER)
                  .build()
            )
            .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, 15))
            .build();
      }

      return maskPipeline;
   }

   private static void mask(RenderTarget source, RenderTarget scene, RenderTarget dst, boolean compareView) {
      mask(source, scene, dst, compareView ? 1.0F : 0.0F);
   }

   private static void mask(RenderTarget source, RenderTarget scene, RenderTarget dst, float mode) {
      ByteBuffer data = staging(new Std140SizeCalculator().putVec4().get());
      Std140Builder.intoBuffer(data).putVec4(mode, 0.0F, 0.0F, 0.0F);
      data.rewind();
      GpuBuffer ubo = RenderSystem.getDevice().createBuffer(() -> "dabyws bloom mask config", 128, data);

      try {
         RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "dabyws bloom mask", dst.getColorTextureView(), Optional.empty());

         try {
            pass.setPipeline(maskPipeline());
            pass.setUniform("MaskConfig", ubo);
            GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
            pass.bindTexture("InSampler", source.getColorTextureView(), nearest);
            pass.bindTexture("BloomDepthSampler", source.getDepthTextureView(), nearest);
            pass.bindTexture("SceneDepthSampler", scene.getDepthTextureView(), nearest);
            pass.draw(3, 1, 0, 0);
         } catch (Throwable var11) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }
            }

            throw var11;
         }

         if (pass != null) {
            pass.close();
         }
      } catch (Throwable var121) {
         if (ubo != null) {
            try {
               ubo.close();
            } catch (Throwable var91) {
               var121.addSuppressed(var91);
            }
         }

         throw var121;
      }

      if (ubo != null) {
         ubo.close();
      }
   }

   private static RenderPipeline debugPipeline() {
      if (debugPipeline == null) {
         debugPipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()})
            .withLocation(id("pipeline/storm_hdr_debug"))
            .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
            .withFragmentShader(id("post/storm_hdr_debug"))
            .withBindGroupLayout(BindGroupLayout.builder().withSampler("InSampler").withUniform("DebugConfig", UniformType.UNIFORM_BUFFER).build())
            .build();
      }

      return debugPipeline;
   }

   private static void show(RenderTarget src, RenderTarget scene, float gain, boolean markHdr) {
      show(src.getColorTextureView(), scene, gain, markHdr, false);
   }

   private static void show(GpuTextureView view, RenderTarget scene, float gain, boolean markHdr, boolean depthView) {
      ByteBuffer data = staging(new Std140SizeCalculator().putVec4().get());
      Std140Builder.intoBuffer(data).putVec4(gain, markHdr ? 1.0F : 0.0F, depthView ? 1.0F : 0.0F, 0.0F);
      data.rewind();
      GpuBuffer ubo = RenderSystem.getDevice().createBuffer(() -> "dabyws bloom debug", 128, data);

      try {
         RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "dabyws bloom debug", scene.getColorTextureView(), Optional.empty());

         try {
            pass.setPipeline(debugPipeline());
            pass.setUniform("DebugConfig", ubo);
            pass.bindTexture("InSampler", view, RenderSystem.getSamplerCache().getClampToEdge(depthView ? FilterMode.NEAREST : FilterMode.LINEAR));
            pass.draw(3, 1, 0, 0);
         } catch (Throwable var12) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (pass != null) {
            pass.close();
         }
      } catch (Throwable var131) {
         if (ubo != null) {
            try {
               ubo.close();
            } catch (Throwable var10) {
               var131.addSuppressed(var10);
            }
         }

         throw var131;
      }

      if (ubo != null) {
         ubo.close();
      }
   }

   private static RenderPipeline blurPipeline() {
      if (blurPipeline == null) {
         blurPipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()})
            .withLocation(id("pipeline/storm_hdr_blur"))
            .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
            .withFragmentShader(id("post/storm_hdr_blur"))
            .withBindGroupLayout(BindGroupLayout.builder().withSampler("InSampler").withUniform("BlurConfig", UniformType.UNIFORM_BUFFER).build())
            .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, 15))
            .build();
      }

      return blurPipeline;
   }

   private static RenderPipeline compositePipeline() {
      if (compositePipeline == null) {
         compositePipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()})
            .withLocation(id("pipeline/storm_hdr_composite"))
            .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
            .withFragmentShader(id("post/storm_hdr_composite"))
            .withBindGroupLayout(
               BindGroupLayout.builder().withSampler("TightSampler").withSampler("WideSampler").withUniform("BloomConfig", UniformType.UNIFORM_BUFFER).build()
            )
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .build();
      }

      return compositePipeline;
   }

   private static void ensureTargets(int w, int h) {
      if (tightA == null) {
         tightA = new TextureTarget("dabyws_bloom_a", w, h, false, GpuFormat.RGBA16_FLOAT);
         tightB = new TextureTarget("dabyws_bloom_b", w, h, false, GpuFormat.RGBA16_FLOAT);
         wide = new TextureTarget("dabyws_bloom_wide", w, h, false, GpuFormat.RGBA16_FLOAT);
      } else if (tightA.width != w || tightA.height != h) {
         tightA.resize(w, h);
         tightB.resize(w, h);
         wide.resize(w, h);
      }
   }

   private static void blur(RenderTarget src, RenderTarget dst, float dirX, float dirY, float radius) {
      ByteBuffer data = staging(new Std140SizeCalculator().putVec4().get());
      Std140Builder.intoBuffer(data).putVec4(dirX / dst.width, dirY / dst.height, radius, 0.0F);
      data.rewind();
      GpuBuffer ubo = RenderSystem.getDevice().createBuffer(() -> "dabyws blur config", 128, data);

      try {
         RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "dabyws bloom blur", dst.getColorTextureView(), Optional.empty());

         try {
            pass.setPipeline(blurPipeline());
            pass.setUniform("BlurConfig", ubo);
            pass.bindTexture("InSampler", src.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.draw(3, 1, 0, 0);
         } catch (Throwable var12) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (pass != null) {
            pass.close();
         }
      } catch (Throwable var131) {
         if (ubo != null) {
            try {
               ubo.close();
            } catch (Throwable var10) {
               var131.addSuppressed(var10);
            }
         }

         throw var131;
      }

      if (ubo != null) {
         ubo.close();
      }
   }

   public static void run(
      RenderTarget source,
      RenderTarget scene,
      RenderTarget sceneDepth,
      float tightRadius,
      float wideRadius,
      float exposure,
      float tightWeight,
      float wideWeight,
      int debugStage
   ) {
      ensureTargets(scene.width, scene.height);
      if (debugStage == 1) {
         show(source, scene, 1.0F, true);
      } else if (debugStage == 2) {
         if (sceneDepth != null) {
            show(sceneDepth.getDepthTextureView(), scene, 1.0F, false, true);
         }
      } else if (debugStage == 3) {
         show(source.getDepthTextureView(), scene, 1.0F, false, true);
      } else if (debugStage == 9) {
         if (sceneDepth != null) {
            mask(source, sceneDepth, wide, 3.0F);
            show(wide, scene, 1.0F, false);
         }
      } else {
         RenderTarget sceneDepthOrSelf = sceneDepth != null ? sceneDepth : source;
         if (net.dabicco.witherstormmod.client.StormBloomDiag.wanted()) {
            mask(source, sceneDepthOrSelf, tightA, 2.0F);
            net.dabicco.witherstormmod.client.StormBloomDiag.report(tightA, source, sceneDepth);
         }

         mask(source, sceneDepthOrSelf, wide, debugStage == 4);
         RenderTarget blurInput = wide;
         if (debugStage == 4) {
            show(blurInput, scene, 1.0F, false);
         } else {
            blur(blurInput, tightA, 1.0F, 0.0F, tightRadius);
            if (debugStage == 5) {
               show(tightA, scene, 6.0F, false);
            } else {
               blur(tightA, tightB, 0.0F, 1.0F, tightRadius);
               if (debugStage == 6) {
                  show(tightB, scene, 6.0F, false);
               } else {
                  blur(tightB, tightA, 1.0F, 0.0F, wideRadius);
                  if (debugStage == 7) {
                     show(tightA, scene, 14.0F, false);
                  } else {
                     blur(tightA, wide, 0.0F, 1.0F, wideRadius);
                     if (debugStage == 8) {
                        show(wide, scene, 14.0F, false);
                     } else {
                        ByteBuffer data = staging(new Std140SizeCalculator().putVec4().get());
                        Std140Builder.intoBuffer(data).putVec4(exposure, tightWeight, wideWeight, 2.0F);
                        data.rewind();
                        GpuBuffer ubo = RenderSystem.getDevice().createBuffer(() -> "dabyws bloom config", 128, data);

                        try {
                           RenderPass pass = RenderSystem.getDevice()
                              .createCommandEncoder()
                              .createRenderPass(() -> "dabyws bloom composite", scene.getColorTextureView(), Optional.empty());

                           try {
                              pass.setPipeline(compositePipeline());
                              pass.setUniform("BloomConfig", ubo);
                              GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
                              pass.bindTexture("TightSampler", tightB.getColorTextureView(), linear);
                              pass.bindTexture("WideSampler", wide.getColorTextureView(), linear);
                              pass.draw(3, 1, 0, 0);
                           } catch (Throwable var181) {
                              if (pass != null) {
                                 try {
                                    pass.close();
                                 } catch (Throwable var17) {
                                    var181.addSuppressed(var17);
                                 }
                              }

                              throw var181;
                           }

                           if (pass != null) {
                              pass.close();
                           }
                        } catch (Throwable var19) {
                           if (ubo != null) {
                              try {
                                 ubo.close();
                              } catch (Throwable var16) {
                                 var19.addSuppressed(var16);
                              }
                           }

                           throw var19;
                        }

                        if (ubo != null) {
                           ubo.close();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void close() {
      if (tightA != null) {
         tightA.destroyBuffers();
         tightB.destroyBuffers();
         wide.destroyBuffers();
         wide = null;
         tightB = null;
         tightA = null;
      }
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
   }
}
