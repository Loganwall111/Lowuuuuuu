package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.dabicco.witherstormmod.mixin.RenderTypeInvoker;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class GlowRenderTypes {
   private static RenderPipeline pipeline;
   private static final Map<Identifier, RenderType> TYPES = new HashMap<>();
   private static RenderPipeline translucentPipeline;
   private static final Map<Identifier, RenderType> TRANSLUCENT_TYPES = new HashMap<>();
   private static RenderPipeline markPipeline;
   private static final Map<Identifier, RenderType> MARK_TYPES = new HashMap<>();
   private static final Map<Identifier, RenderType> BLOOM_TYPES = new HashMap<>();
   private static RenderPipeline bloomSourcePipeline;
   private static RenderPipeline erasePipeline;
   private static final Map<Identifier, RenderType> ERASE_TYPES = new HashMap<>();
   private static RenderPipeline occluderPipeline;
   private static final Map<Identifier, RenderType> OCCLUDER_TYPES = new HashMap<>();
   public static final int BLOOM_SOURCE_PASSES = 3;

   private GlowRenderTypes() {
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         pipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$entityEmissiveSnippet()})
            .withLocation(id("pipeline/storm_glow"))
            .withVertexShader(id("core/fogless_entity"))
            .withFragmentShader(id("core/storm_glow"))
            .withShaderDefine("NO_OVERLAY")
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ZERO, BlendFactor.ONE)))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();
      }

      return pipeline;
   }

   private static RenderPipeline markPipeline() {
      if (markPipeline == null) {
         markPipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$entityEmissiveSnippet()})
            .withLocation(id("pipeline/storm_emitter_mark"))
            .withVertexShader(id("core/fogless_entity"))
            .withFragmentShader(id("core/fogless_entity"))
            .withShaderDefine("NO_OVERLAY")
            .withShaderDefine("FOG_MIX", 0.0F)
            .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ZERO)))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();
      }

      return markPipeline;
   }

   private static RenderPipeline bloomSourcePipeline() {
      if (bloomSourcePipeline == null) {
         bloomSourcePipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$entityEmissiveSnippet()})
            .withLocation(id("pipeline/storm_bloom_src"))
            .withVertexShader(id("core/fogless_entity"))
            .withFragmentShader(id("core/fogless_entity"))
            .withShaderDefine("NO_OVERLAY")
            .withShaderDefine("FOG_MIX", 0.0F)
            .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.ADDITIVE), GpuFormat.RGBA16_FLOAT, 15))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
            .withCull(false)
            .build();
      }

      return bloomSourcePipeline;
   }

   private static RenderPipeline erasePipeline() {
      if (erasePipeline == null) {
         erasePipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$entitySnippet()})
            .withLocation(id("pipeline/storm_bloom_erase"))
            .withVertexShader(id("core/fogless_entity"))
            .withFragmentShader(id("core/fogless_entity"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("FOG_MIX", 0.0F)
            .withShaderDefine("NO_OVERLAY")
            .withColorTargetState(
               new ColorTargetState(
                  Optional.of(new BlendFunction(BlendFactor.ZERO, BlendFactor.ZERO, BlendFactor.ZERO, BlendFactor.ZERO)), GpuFormat.RGBA16_FLOAT, 15
               )
            )
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, false))
            .withCull(false)
            .build();
      }

      return erasePipeline;
   }

   public static RenderType bloomEraseOccluded(Identifier texture) {
      return ERASE_TYPES.computeIfAbsent(
         texture,
         tex -> RenderTypeInvoker.dabyws$create(
            "dabywitherstormmod:storm_bloom_erase:" + tex,
            RenderSetup.builder(erasePipeline())
               .withTexture("Sampler0", tex)
               .useLightmap()
               .setOutputTarget(net.dabicco.witherstormmod.client.StormBloomTarget.eraseOutputTarget())
               .createRenderSetup()
         )
      );
   }

   private static RenderPipeline occluderPipeline() {
      if (occluderPipeline == null) {
         occluderPipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$entitySnippet()})
            .withLocation(id("pipeline/storm_bloom_occluder"))
            .withVertexShader(id("core/fogless_entity"))
            .withFragmentShader(id("core/fogless_entity"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("FOG_MIX", 0.0F)
            .withShaderDefine("NO_OVERLAY")
            .withColorTargetState(
               new ColorTargetState(
                  Optional.of(new BlendFunction(BlendFactor.ZERO, BlendFactor.ZERO, BlendFactor.ZERO, BlendFactor.ZERO)), GpuFormat.RGBA16_FLOAT, 15
               )
            )
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, false))
            .withCull(false)
            .build();
      }

      return occluderPipeline;
   }

   public static RenderType bloomOccluder(Identifier texture) {
      return OCCLUDER_TYPES.computeIfAbsent(
         texture,
         tex -> RenderTypeInvoker.dabyws$create(
            "dabywitherstormmod:storm_bloom_occluder:" + tex,
            RenderSetup.builder(occluderPipeline())
               .withTexture("Sampler0", tex)
               .setOutputTarget(net.dabicco.witherstormmod.client.StormBloomTarget.outputTarget())
               .useLightmap()
               .createRenderSetup()
         )
      );
   }

   public static RenderType bloomSource(Identifier texture) {
      return BLOOM_TYPES.computeIfAbsent(
         texture,
         tex -> RenderTypeInvoker.dabyws$create(
            "dabywitherstormmod:storm_bloom_src:" + tex,
            RenderSetup.builder(bloomSourcePipeline())
               .withTexture("Sampler0", tex)
               .setOutputTarget(net.dabicco.witherstormmod.client.StormBloomTarget.outputTarget())
               .createRenderSetup()
         )
      );
   }

   public static RenderType emitterMark(Identifier texture) {
      return net.dabicco.witherstormmod.client.ShaderPackCompat.active()
         ? RenderTypes.eyes(texture)
         : MARK_TYPES.computeIfAbsent(
            texture,
            tex -> RenderTypeInvoker.dabyws$create(
               "dabywitherstormmod:storm_emitter_mark:" + tex, RenderSetup.builder(markPipeline()).withTexture("Sampler0", tex).createRenderSetup()
            )
         );
   }

   public static RenderType glow(Identifier texture) {
      return TYPES.computeIfAbsent(
         texture,
         tex -> RenderTypeInvoker.dabyws$create(
            "dabywitherstormmod:storm_glow:" + tex, RenderSetup.builder(pipeline()).withTexture("Sampler0", tex).createRenderSetup()
         )
      );
   }

   public static RenderType translucent(Identifier texture) {
      return TRANSLUCENT_TYPES.computeIfAbsent(
         texture,
         tex -> RenderTypeInvoker.dabyws$create(
            "dabywitherstormmod:storm_translucent:" + tex, RenderSetup.builder(translucentPipeline()).withTexture("Sampler0", tex).createRenderSetup()
         )
      );
   }

   private static RenderPipeline translucentPipeline() {
      if (translucentPipeline == null) {
         translucentPipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$entityEmissiveSnippet()})
            .withLocation(id("pipeline/storm_translucent"))
            .withVertexShader(id("core/fogless_entity"))
            .withFragmentShader(id("core/fogless_entity"))
            .withShaderDefine("NO_OVERLAY")
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();
      }

      return translucentPipeline;
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
   }
}
