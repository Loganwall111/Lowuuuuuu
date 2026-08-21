package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.OptionalDouble;
import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.minecraft.resources.Identifier;

public final class StormBloomComposite {
   private static RenderPipeline pipeline;

   private StormBloomComposite() {
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         pipeline = RenderPipeline.builder(new RenderPipeline.Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()}).withLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "pipeline/storm_bloom_add")).withVertexShader(Identifier.withDefaultNamespace("core/screenquad")).withFragmentShader(Identifier.fromNamespaceAndPath("dabywitherstormmod", "post/storm_bloom_add")).withBindGroupLayout(BindGroupLayout.builder().withSampler("InSampler").withSampler("DepthSampler").build()).withBindGroupLayout(BindGroupLayout.builder().withUniform("BloomConfig", UniformType.UNIFORM_BUFFER).build()).withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE)).withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false)).build();
      }

      return pipeline;
   }

   public static void add(RenderTarget scene, RenderTarget glow, float intensity) {
      ByteBuffer data = ByteBuffer.allocateDirect((new Std140SizeCalculator()).putVec4().get()).order(ByteOrder.nativeOrder());
      Std140Builder.intoBuffer(data).putVec4(intensity, 2.0F, 0.0F, 0.0F);
      data.rewind();
      GpuBuffer ubo = RenderSystem.getDevice().createBuffer(() -> "dabyws bloom intensity", 128, data);

      try {
         draw(scene, glow, ubo);
      } catch (Throwable var8) {
         if (ubo != null) {
            try {
               ubo.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (ubo != null) {
         ubo.close();
      }

   }

   private static void draw(RenderTarget scene, RenderTarget glow, GpuBuffer ubo) {
      RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "dabyws storm bloom composite", scene.getColorTextureView(), Optional.empty(), scene.getDepthTextureView(), OptionalDouble.empty());

      try {
         pass.setPipeline(pipeline());
         pass.setUniform("BloomConfig", ubo);
         pass.bindTexture("InSampler", glow.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
         pass.bindTexture("DepthSampler", glow.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
         pass.draw(3, 1, 0, 0);
      } catch (Throwable var7) {
         if (pass != null) {
            try {
               pass.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (pass != null) {
         pass.close();
      }

   }
}
