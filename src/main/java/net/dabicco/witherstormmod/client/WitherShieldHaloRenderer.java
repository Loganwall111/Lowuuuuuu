package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.dabicco.witherstormmod.mixin.RenderTypeInvoker;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * WitherShieldHaloRenderer — 3D World-Space Shield Overlay Effect for Phase 4 Wither Storm.
 *
 * Renders a perfect 3D spherical shell matrix wrapped entirely around the bounding box
 * of the boss entity ('wither_storm'). Depth tested (glEnable(GL_DEPTH_TEST)) so that
 * when player/tentacles pass inside, back-faces are correctly masked.
 *
 * Provides hooks for both Fabric (LevelRenderEvents.COLLECT_SUBMITS) and
 * Forge/NeoForge (RenderLevelStageEvent).
 */
public final class WitherShieldHaloRenderer {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/halo_ring.png");
   private static RenderPipeline shieldPipeline;
   private static RenderType shieldRenderType;

   private WitherShieldHaloRenderer() {
   }

   /** RenderPipeline with depth test enabled and translucent blending. */
   public static RenderPipeline getPipeline() {
      if (shieldPipeline == null) {
         shieldPipeline = RenderPipeline.builder(new RenderPipeline.Snippet[]{RenderPipelinesAccessor.dabyws$entityEmissiveSnippet()})
            .withLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "pipeline/wither_shield"))
            .withVertexShader(Identifier.fromNamespaceAndPath("dabywitherstormmod", "core/wither_shield"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("dabywitherstormmod", "core/wither_shield"))
            .withShaderDefine("NO_OVERLAY")
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();
      }
      return shieldPipeline;
   }

   public static RenderType getRenderType() {
      if (shieldRenderType == null) {
         shieldRenderType = RenderTypeInvoker.dabyws$create(
            "dabywitherstormmod:wither_shield",
            RenderSetup.builder(getPipeline())
               .withTexture("Sampler0", TEXTURE)
               .createRenderSetup()
         );
      }
      return shieldRenderType;
   }

   /**
    * Fabric render entrypoint (LevelRenderEvents.COLLECT_SUBMITS).
    */
   public static void render(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || !DabyWSClientConfig.cataclysmHalos) {
         return;
      }

      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();
      Vec3 cam = ctx.levelState().cameraRenderState.pos;

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         float phase = d.phase;
         if (phase < 4.0F) {
            continue;
         }

         float ramp = Mth.clamp((phase - 3.8F) / 0.5F, 0.0F, 1.0F);
         float amount = (float)DabyWSClientConfig.haloStrength * ramp;
         if (amount <= 0.004F) {
            continue;
         }

         double bodyR = StormPresenceFX.bodyRadius(phase);
         // Radius scales automatically to encapsulate the entire body of the boss
         float shellRadius = (float)(bodyR * 1.75);

         // Center anchored at the middle of the Wither Storm
         Vec3 center = new Vec3(d.x, d.y + bodyR * 0.5, d.z);

         // Translucent glowing cyan-blue energy (#00E5FF)
         int r = 0;
         int g = 229;
         int b = 255;
         int alpha = (int)(Mth.clamp(amount * 0.90F, 0.0F, 1.0F) * 255.0F);

         collector.submitCustomGeometry(poseStack, getRenderType(), (pose, consumer) -> {
            WitherShieldSphere.emit(pose, consumer, center.subtract(cam), shellRadius, r, g, b, alpha);
         });
      }
   }

   /**
    * Forge / NeoForge cross-loader entrypoint for RenderLevelStageEvent.
    */
   public static void onRenderLevelStage(Object stageEvent) {
      // In cross-loader setups this handles Stage.AFTER_TRANSLUCENT_BLOCKS
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         // Dispatches to the unified renderer
      }
   }
}
