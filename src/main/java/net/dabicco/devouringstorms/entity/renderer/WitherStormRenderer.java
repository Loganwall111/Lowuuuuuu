package net.dabicco.devouringstorms.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.Arrays;
import java.util.HashMap;
import net.dabicco.devouringstorms.BowelsPortal;
import net.dabicco.devouringstorms.client.CubeReveal;
import net.dabicco.devouringstorms.client.FoglessRenderTypes;
import net.dabicco.devouringstorms.client.GlowRenderTypes;
import net.dabicco.devouringstorms.client.GroundProbe;
import net.dabicco.devouringstorms.client.PreviewScene;
import net.dabicco.devouringstorms.client.StormDebris;
import net.dabicco.devouringstorms.client.StormGlowRenderer;
import net.dabicco.devouringstorms.client.StormPalettes;
import net.dabicco.devouringstorms.client.StormShadowMap;
import net.dabicco.devouringstorms.client.StormStageShells;
import net.dabicco.devouringstorms.client.TentacleMeasure;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.CollapseAnim;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.dabicco.devouringstorms.entity.model.HugeAssBackModel;
import net.dabicco.devouringstorms.entity.model.HunchbackGrowth;
import net.dabicco.devouringstorms.entity.model.ModEntityModelLayers;
import net.dabicco.devouringstorms.entity.model.StormCoverModel;
import net.dabicco.devouringstorms.entity.model.Tentacle;
import net.dabicco.devouringstorms.entity.model.WitherCommandBlock;
import net.dabicco.devouringstorms.entity.model.WitherStormDevourer;
import net.dabicco.devouringstorms.entity.model.WitherStormGrowth5;
import net.dabicco.devouringstorms.entity.model.WitherStormHead;
import net.dabicco.devouringstorms.entity.model.WitherStormP4;
import net.dabicco.devouringstorms.entity.model.WitherStormTentacles5;
import net.dabicco.devouringstorms.entity.model.WitherStormTentaclesDevourer;
import net.dabicco.devouringstorms.entity.state.WitherStormHeadRenderState;
import net.dabicco.devouringstorms.entity.state.WitherStormRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.dabicco.devouringstorms.client.StormSkins;

public class WitherStormRenderer extends MobRenderer<WitherStormEntity, WitherStormRenderState, EntityModel<WitherStormRenderState>> {
   private final StormCoverModel coverModel;
   private final WitherCommandBlock commandBlockModel;
   private final WitherStormP4 stormP4Model;
   private final WitherStormDevourer devourerModel;
   private final WitherStormTentaclesDevourer devourerTentaclesModel;
   private final WitherStormHead miniHeadModel;
   private final WitherStormHead miniHeadGlowModel;
   private final WitherStormGrowth5 growth5Model;
   private final Tentacle frontTentacleModel;
   private final HunchbackGrowth hunchbackModel;
   private final HugeAssBackModel hugeAssBackModel;
   private final WitherStormTentacles5 tentacles5Model;
   private final WitherStormGrowth5 growth5BackModel;
   private final HugeAssBackModel hugeAssBackMirrorModel;
   private final WitherCommandBlock commandBlockShadowModel;
   private final WitherStormP4 stormP4ShadowModel;
   private final WitherStormDevourer devourerShadowModel;
   private final WitherStormTentacles5 tentacles5ShadowModel;
   private final WitherStormTentaclesDevourer devourerTentaclesShadowModel;
   private final HunchbackGrowth hunchbackShadowModel;
   private final PreviewHeads previewHeads;
   private final Tentacle frontTentacleShadowModel;
   private static final float MINI_HEAD_Y = 3.05F;
   private static final float MINI_HEAD_SCALE = 1.55F;
   private static final float MINI_HEAD_BITE_ANGLE = 42.0F;
   private static final int FULL_BRIGHT = 15728880;
   private static final double COLLAPSE_PIVOT_Y = 17.0;
   public static final double COLLAPSE_PIVOT_Y_PUBLIC = 17.0;
   private static final double CHANGEOVER_FROM = 3.82;
   private static final float SHAKE_AMPLITUDE = 0.55F;
   private static final float HATCH_TICKS = 55.0F;
   private static final float HATCH_MIN = 0.22F;
   private static final float WHITEOUT_FADE_TICKS = 38.0F;
   public static final double COLLAPSE_DROP_Y = 18.5;
   private static final double GROWTH5_START_PHASE = 4.5;
   private static final float GROWTH5_SEED_SCALE = 0.06F;
   private static final float GROWTH5_MIRROR_PUSH = 0.85F;
   private static final float GROWTH5_MIRROR_UP = 0.0F;
   private static final float GROWTH5_MIRROR_LAG = 0.1F;
   private static final int HUGEBACK_GROUP_DEPTH = 3;
   private static final float HUGEBACK_MIRROR_LAG = 0.06F;
   private static final double HUGEBACK_START = 5.4;
   private static final double HUGEBACK_FULL = 5.8;
   private static final HashMap<Integer, float[]> GROWTH5_DISPLAY = new HashMap<>();
   private static final HashMap<Integer, float[]> YAW_SMOOTH = new HashMap<>();
   private static final int PORTAL_R = 148;
   private static final int PORTAL_G = 58;
   private static final int PORTAL_B = 224;
   private static final int PORTAL_ALPHA_SEALED = 105;
   private static final int PORTAL_ALPHA_OPEN = 170;
   private static final Identifier PORTAL_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/tractor_beam.png");
   private static final Identifier INNER_GLOW_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/fx_witherCubeInnerGlow.png");
   private static final Identifier HALO_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_ring.png");
   private static final Identifier HALO_GRADIENT_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_gradient.png");
   private static final Identifier HALO_BAND_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_band.png");
   private static final Identifier VORTEX_RING_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/tile_witherstormVortexA_alp.png");
   private static final Identifier PHASE4_EMISSIVE_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/phase_4_assets_e.png");
   private static final float[] COLLAPSE_GLOW_SIZES = new float[]{0.9F, 1.25F, 1.7F};
   private static final float[] COLLAPSE_GLOW_ALPHAS = new float[]{0.52F, 0.24F, 0.10F};
   private static final int[][] COLLAPSE_GLOW_COLOURS = new int[][]{{255, 255, 255}, {242, 240, 255}, {222, 214, 255}};
   private SubmitNodeCollector frameCollector;
   private static final float FRONT_TENTACLE_SPROUT_TICKS = 70.0F;
   private static final float FRONT_TENTACLE_UP = -1.1F;
   private static final float FRONT_TENTACLE_FORWARD = -0.5F;
   private static final float FRONT_TENTACLE_SIZE = 0.42F;
   private static final float FRONT_TENTACLE_TURN_IN = -14.0F;
   private static final float FRONT_TENTACLE_ANGLE_OUT = 36.0F;
   private boolean previewShadowPass = false;
   private static final float[] NIGHT_LAYER_SIZES = new float[]{0.45F, 0.75F, 1.0F};
   private static final float[] NIGHT_LAYER_ALPHAS = new float[]{0.85F, 0.45F, 0.22F};
   private static final int[][] NIGHT_LAYER_COLOURS = new int[][]{{216, 233, 255}, {168, 205, 255}, {122, 170, 255}};
   private static final double NIGHT_RADIUS_BASE = 26.0;
   private static final double NIGHT_RADIUS_PER_PHASE = 9.0;

   private Identifier bodyTexture(WitherStormRenderState state) {
      return this.getTextureLocation(state);
   }

   protected AABB getBoundingBoxForCulling(WitherStormEntity entity) {
      return super.getBoundingBoxForCulling(entity).inflate(512.0);
   }

   protected int getModelTint(WitherStormRenderState state) {
      return this.previewShadowPass ? 940578856 : this.applyStormTint(super.getModelTint(state), state);
   }

   private int applyStormTint(int argb, WitherStormRenderState state) {
      float white = Mth.clamp(state.changeover + state.collapseWhiteout, 0.0F, 1.0F);
      int a = (int)((float)(argb >>> 24 & 255) * Mth.clamp(state.collapseFade, 0.0F, 1.0F));
      int r = (int)Mth.lerp(white, (float)(argb >> 16 & 255), 255.0F);
      int g = (int)Mth.lerp(white, (float)(argb >> 8 & 255), 255.0F);
      int b = (int)Mth.lerp(white, (float)(argb & 255), 255.0F);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int scaleTint(int argb, float f) {
      int r = Math.min(255, (int)((float)(argb >> 16 & 0xFF) * f));
      int g = Math.min(255, (int)((float)(argb >> 8 & 0xFF) * f));
      int b = Math.min(255, (int)((float)(argb & 0xFF) * f));
      return argb & 0xFF000000 | r << 16 | g << 8 | b;
   }

   private static void applyChangeoverShake(PoseStack poseStack, WitherStormRenderState state) {
      float w = Mth.clamp(state.changeover, 0.0F, 1.0F);
      if (!(w <= 0.001F)) {
         float amp = 0.55F * w * w * (float)DevouringStormsClientConfig.changeoverShake;
         float t = state.idleTimeTicks;
         poseStack.translate(Mth.sin((double)(t * 3.1F)) * amp, Mth.sin((double)(t * 4.7F + 1.3F)) * amp * 0.6F, Mth.sin((double)(t * 2.6F + 2.9F)) * amp);
      }
   }

   static void readGround(GroundProbe probe, Level level, WitherStormRenderState state, double x, double y, double z, double radius) {
      float down = CollapseAnim.down(state.collapseTicks);
      if (down <= 0.001F) {
         state.slopePitch = 0.0F;
         state.slopeRoll = 0.0F;
         Arrays.fill(state.groundBias, 0.0F);
      } else {
         probe.update(level, x, y, z, state.bodyRot, radius);
         state.slopePitch = probe.pitch * down;
         state.slopeRoll = probe.roll * down;

         for (int i = 0; i < state.groundBias.length; i++) {
            state.groundBias[i] = probe.bias(i, 14.0F) * down;
         }
      }
   }

   private static float growth5SmoothPerSec() {
      return (float)DevouringStormsClientConfig.growthSmoothRate;
   }

   private static float hugeBackProgress(double phase) {
      if (phase >= 6.0) {
         return 0.0F;
      } else if (phase >= 5.8) {
         return 1.0F;
      } else if (phase <= 5.4) {
         return 0.0F;
      } else {
         float p = (float)((phase - 5.4) / 0.39999999999999947);
         return p * p * (3.0F - 2.0F * p);
      }
   }

   protected RenderType getRenderType(WitherStormRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
      if (this.previewShadowPass) {
         return this.pieceType(this.getTextureLocation(state));
      } else {
         boolean ours = FoglessRenderTypes.fogless() || FoglessRenderTypes.reverseShading();
         return ours && showBody && !translucent
            ? FoglessRenderTypes.bodyCutout(this.getTextureLocation(state))
            : super.getRenderType(state, showBody, translucent, showOutline);
      }
   }

   static void applyCollapse(PoseStack poseStack, WitherStormRenderState state, double pivot) {
      float topple = CollapseAnim.bodyPitch(state.collapseTicks);
      if (topple != 0.0F) {
         float flat = CollapseAnim.down(state.collapseTicks);
         poseStack.translate(0.0, -18.5 * (double)flat, 0.0);
         poseStack.translate(0.0, pivot, 0.0);
         poseStack.mulPose(Axis.XP.rotationDegrees(-topple));
         poseStack.translate(0.0, -pivot, 0.0);
      }
   }

   private void submitMirroredGroups(
      SubmitNodeCollector collector, PoseStack poseStack, ModelPart root, WitherStormRenderState state, float progress, int groupDepth
   ) {
      if (!(progress <= 0.0F)) {
         int light = state.lightCoords;
         int tint = this.pieceTint(state);
         poseStack.pushPose();
         poseStack.translate(0.0, -0.0, 0.0);
         collector.submitCustomGeometry(poseStack, this.pieceType(this.bodyTexture(state)), (pose, consumer) -> {
            PoseStack local = new PoseStack();
            local.last().pose().set(pose.pose());
            local.last().normal().set(pose.normal());
            CubeReveal.renderMirroredGroups(root, local, consumer, light, OverlayTexture.NO_OVERLAY, tint, progress, true, 0.85F, groupDepth);
         });
         poseStack.popPose();
      }
   }

   private void submitScaled(PoseStack poseStack, ModelPart root, float grow, Runnable body) {
      float[] b = CubeReveal.bounds(root);
      float cx = (b[0] + b[1]) * 0.5F;
      float cy = (b[2] + b[3]) * 0.5F;
      float anchorZ = b[4];
      float s = Math.max(grow, 0.02F);
      poseStack.pushPose();
      poseStack.translate(cx, cy, anchorZ);
      poseStack.scale(s, s, s);
      poseStack.translate(-cx, -cy, -anchorZ);
      body.run();
      poseStack.popPose();
   }

   private void submitMirroredBack(SubmitNodeCollector collector, PoseStack poseStack, ModelPart root, WitherStormRenderState state, float progress) {
      poseStack.pushPose();
      this.applyBackMirror(poseStack, root);
      this.submitCubeReveal(collector, poseStack, root, state, progress, true);
      poseStack.popPose();
   }

   private void applyBackMirror(PoseStack poseStack, ModelPart root) {
      float[] b = CubeReveal.bounds(root);
      float zCentre = (b[4] + b[5]) * 0.5F;
      float depth = b[5] - b[4];
      float zPlane = zCentre + depth * 0.5F * 0.85F;
      float xCentre = (b[0] + b[1]) * 0.5F;
      poseStack.translate(0.0, -0.0, 0.0);
      poseStack.translate(0.0, 0.0, (double)zPlane);
      poseStack.scale(1.0F, 1.0F, -1.0F);
      poseStack.translate(0.0, 0.0, (double)(-zPlane));
      poseStack.translate((double)xCentre, 0.0, 0.0);
      poseStack.scale(-1.0F, 1.0F, 1.0F);
      poseStack.translate((double)(-xCentre), 0.0, 0.0);
   }

   private void submitCubeReveal(SubmitNodeCollector collector, PoseStack poseStack, ModelPart root, WitherStormRenderState state, float progress) {
      this.submitCubeReveal(collector, poseStack, root, state, progress, false);
   }

   private void submitCubeReveal(
      SubmitNodeCollector collector, PoseStack poseStack, ModelPart root, WitherStormRenderState state, float progress, boolean dropSheets
   ) {
      int light = state.lightCoords;
      int tint = this.pieceTint(state);
      collector.submitCustomGeometry(
         poseStack, this.pieceType(this.bodyTexture(state), !dropSheets && DevouringStormsClientConfig.stormBackfaceCull), (pose, consumer) -> {
            PoseStack local = new PoseStack();
            local.last().pose().set(pose.pose());
            local.last().normal().set(pose.normal());
            CubeReveal.render(root, local, consumer, light, OverlayTexture.NO_OVERLAY, tint, progress, true, dropSheets);
         }
      );
   }

   private static float yawSmoothTime() {
      return (float)DevouringStormsClientConfig.yawSmoothTime;
   }

   private static float yawSnapDegrees() {
      return (float)DevouringStormsClientConfig.yawSnapDegrees;
   }

   public static float smoothBodyYaw(int stormId, float target) {
      long nowMs = Util.getMillis();
      float[] slot = YAW_SMOOTH.get(stormId);
      if (slot == null) {
         slot = new float[]{target, 0.0F, (float)nowMs};
         YAW_SMOOTH.put(stormId, slot);
         if (YAW_SMOOTH.size() > 64) {
            YAW_SMOOTH.clear();
         }

         return target;
      } else {
         float diff = Mth.degreesDifference(slot[0], target);
         if (Math.abs(diff) > yawSnapDegrees()) {
            slot[0] = target;
            slot[1] = 0.0F;
            slot[2] = (float)nowMs;
            return target;
         } else {
            float dt = Mth.clamp(((float)nowMs - slot[2]) / 1000.0F, 0.0F, 0.25F);
            slot[2] = (float)nowMs;
            if (dt <= 0.0F) {
               return slot[0];
            } else {
               float omega = 2.0F / Math.max(0.02F, yawSmoothTime());
               float x = omega * dt;
               float expo = 1.0F / (1.0F + x + 0.48F * x * x + 0.235F * x * x * x);
               float change = -diff;
               float temp = (slot[1] + omega * change) * dt;
               slot[1] = (slot[1] - omega * temp) * expo;
               slot[0] = Mth.wrapDegrees(target + (change + temp) * expo);
               return slot[0];
            }
         }
      }
   }

   private static float debrisSettle(double phase) {
      double full = 6.15;
      double start = full - 0.05;
      if (phase < start) {
         return 0.0F;
      } else {
         float t = (float)Mth.clamp((phase - start) / (full - start), 0.0, 1.0);
         return t * t * (3.0F - 2.0F * t);
      }
   }

   private static float growth5Target(double phase) {
      if (phase >= 5.0) {
         return 1.0F;
      } else {
         return phase <= 4.5 ? 0.0F : (float)((phase - 4.5) / 0.5);
      }
   }

   private static float growth5Smoothed(int stormId, double phase) {
      float target = growth5Target(phase);
      long nowMs = Util.getMillis();
      float[] slot = GROWTH5_DISPLAY.get(stormId);
      if (slot == null) {
         slot = new float[]{target, (float)nowMs};
         GROWTH5_DISPLAY.put(stormId, slot);
         if (GROWTH5_DISPLAY.size() > 64) {
            GROWTH5_DISPLAY.clear();
         }

         return target;
      } else {
         float dt = Mth.clamp(((float)nowMs - slot[1]) / 1000.0F, 0.0F, 0.25F);
         slot[1] = (float)nowMs;
         float a = 1.0F - (float)Math.exp((double)(-dt * growth5SmoothPerSec()));
         slot[0] += (target - slot[0]) * a;
         return slot[0];
      }
   }

   public WitherStormRenderer(Context context) {
      super(context, new WitherCommandBlock(context.bakeLayer(ModEntityModelLayers.WITHER_STORM)), 1.0F);
      this.coverModel = new StormCoverModel(context.bakeLayer(ModEntityModelLayers.STORM_COVER));
      this.commandBlockModel = new WitherCommandBlock(context.bakeLayer(ModEntityModelLayers.WITHER_STORM));
      this.stormP4Model = new WitherStormP4(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_P4));
      this.miniHeadModel = new WitherStormHead(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_HEAD));
      this.miniHeadGlowModel = new WitherStormHead(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_HEAD_GLOW));
      this.growth5Model = new WitherStormGrowth5(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_GROWTH5));
      this.frontTentacleModel = new Tentacle(context.bakeLayer(ModEntityModelLayers.TENTACLE));
      this.hunchbackModel = new HunchbackGrowth(context.bakeLayer(ModEntityModelLayers.HUNCHBACK_GROWTH));
      this.hugeAssBackModel = new HugeAssBackModel(context.bakeLayer(ModEntityModelLayers.HUGE_ASS_BACK));
      this.devourerModel = new WitherStormDevourer(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_DEVOURER));
      this.devourerTentaclesModel = new WitherStormTentaclesDevourer(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_TENTACLES_DEVOURER));
      this.tentacles5Model = new WitherStormTentacles5(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_TENTACLES5));
      this.growth5BackModel = new WitherStormGrowth5(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_GROWTH5));
      this.hugeAssBackMirrorModel = new HugeAssBackModel(context.bakeLayer(ModEntityModelLayers.HUGE_ASS_BACK));
      this.previewHeads = new PreviewHeads(context);
      this.commandBlockShadowModel = new WitherCommandBlock(context.bakeLayer(ModEntityModelLayers.WITHER_STORM));
      this.stormP4ShadowModel = new WitherStormP4(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_P4));
      this.devourerShadowModel = new WitherStormDevourer(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_DEVOURER));
      this.tentacles5ShadowModel = new WitherStormTentacles5(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_TENTACLES5));
      this.devourerTentaclesShadowModel = new WitherStormTentaclesDevourer(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_TENTACLES_DEVOURER));
      this.hunchbackShadowModel = new HunchbackGrowth(context.bakeLayer(ModEntityModelLayers.HUNCHBACK_GROWTH));
      this.frontTentacleShadowModel = new Tentacle(context.bakeLayer(ModEntityModelLayers.TENTACLE));
   }

   private void submitCover(PoseStack poseStack, SubmitNodeCollector collector, WitherStormRenderState state) {
      if (!(state.phase < 6.8)) {
         this.coverModel.applyPeel(BowelsPortal.platesFor(state.phase));
         RenderType type = this.getRenderType(state, true, false, false);
         if (type != null) {
            collector.submitModelPart(this.coverModel.back(), poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, null);

            for (int i = 1; i <= 4; i++) {
               ModelPart plate = this.coverModel.cover(i);
               if (plate.visible) {
                  collector.submitModelPart(plate, poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
               }
            }
         }
      }
   }

   private void submitPortal(PoseStack poseStack, SubmitNodeCollector collector, WitherStormRenderState state) {
      Vec3[] c = state.portalCorners;
      if (c != null && !this.previewShadowPass) {
         int alpha = state.portalOpen ? 170 : 105;
         collector.submitCustomGeometry(poseStack, FoglessRenderTypes.entityTranslucentEmissive(PORTAL_TEXTURE), (pose, buffer) -> {
            portalQuad(pose, buffer, c[0], c[1], c[2], c[3], alpha);
            portalQuad(pose, buffer, c[3], c[2], c[1], c[0], alpha);
         });
      }
   }

   private static void portalQuad(Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int alpha) {
      portalVertex(pose, buffer, a, 0.0F, 0.0F, alpha);
      portalVertex(pose, buffer, b, 1.0F, 0.0F, alpha);
      portalVertex(pose, buffer, c, 1.0F, 1.0F, alpha);
      portalVertex(pose, buffer, d, 0.0F, 1.0F, alpha);
   }

   private static void portalVertex(Pose pose, VertexConsumer buffer, Vec3 at, float u, float v, int alpha) {
      buffer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z)
         .setColor(148, 58, 224, alpha)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   public WitherStormRenderState createRenderState() {
      return new WitherStormRenderState();
   }

   public void extractRenderState(WitherStormEntity entity, WitherStormRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.phase4 = entity.isPhase4();
      state.devourer = entity.isDevourer();
      state.phase = entity.getPhase();
      state.expansionPhase = entity.getExpansionPhase();
      if (entity.hasCover()) {
         state.portalCorners = BowelsPortal.corners(entity.getYRot());
         state.portalOpen = BowelsPortal.open(state.phase);
      } else {
         state.portalCorners = null;
         state.portalOpen = false;
      }

      state.nightFactor = StormGlowRenderer.nightFactor(entity.level());
      state.worldX = Mth.lerp((double)partialTick, entity.xOld, entity.getX());
      state.worldY = Mth.lerp((double)partialTick, entity.yOld, entity.getY());
      state.worldZ = Mth.lerp((double)partialTick, entity.zOld, entity.getZ());
      state.bodyRoll = entity.getBodyRoll();
      state.bodyRoll = state.bodyRoll * (float)DevouringStormsClientConfig.bodyBankGain;
      state.xRot = state.xRot * (float)DevouringStormsClientConfig.bodyLeanGain;
      state.underSiege = entity.isUnderSiege();
      state.siegeProgress = (float)entity.siegeProgress() / 100.0F;
      state.stormId = entity.getId();
      long collapse = entity.getCollapseGameTime();
      state.collapseTicks = collapse < 0L ? -1.0F : (float)(entity.level().getGameTime() - collapse) + partialTick;
      if (state.collapseTicks >= 0.0F) {
         state.collapseWhiteout = Mth.clamp((state.collapseTicks - 1000.0F) / 34.0F, 0.0F, 1.0F);
         state.collapseFade = 1.0F - Mth.clamp((state.collapseTicks - 1038.0F) / 132.0F, 0.0F, 1.0F);
      } else {
         state.collapseWhiteout = 0.0F;
         state.collapseFade = 1.0F;
      }
      state.slopePitch = 0.0F;
      state.slopeRoll = 0.0F;
      Arrays.fill(state.groundBias, 0.0F);
      state.bodyRot = smoothBodyYaw(entity.getId(), state.bodyRot);
      Vec3 vel = entity.getDeltaMovement();
      state.velX = vel.x;
      state.velY = vel.y;
      state.velZ = vel.z;
      long gt = entity.level().getGameTime();
      float now = (float)(gt % 100000L) + partialTick;
      if (CollapseAnim.down(state.collapseTicks) >= 0.999F) {
         state.idleTimeTicks = 0.0F;
      } else {
         state.idleTimeTicks = now;
      }

      long phase5Start = entity.getPhase5AnimGameTime();
      state.phase5ElapsedTicks = phase5Start < 0L ? -1.0F : (float)(gt - phase5Start) + partialTick;
      long phase58Start = entity.getPhase58AnimGameTime();
      state.phase58ElapsedTicks = phase58Start < 0L ? -1.0F : (float)(gt - phase58Start) + partialTick;
      long spawnStart = entity.getSpawnAnimGameTime();
      if (state.phase4 && spawnStart >= 0L) {
         state.spawnElapsedTicks = (float)(gt - spawnStart) + partialTick;
         state.playingSpawnAnimation = state.spawnElapsedTicks >= 0.0F && state.spawnElapsedTicks < 80.0F;
      } else {
         state.spawnElapsedTicks = Float.MAX_VALUE;
         state.playingSpawnAnimation = false;
      }

      if (!state.phase4) {
         state.changeover = (float)Mth.clamp((state.phase - 3.82) / 0.18000000000000016, 0.0, 1.0);
         state.hatch = 1.0F;
      } else {
         float since = state.spawnElapsedTicks;
         float grow = since < 0.0F ? 1.0F : Mth.clamp(since / 55.0F, 0.0F, 1.0F);
         state.hatch = 0.22F + 0.78F * grow * grow * (3.0F - 2.0F * grow);
         state.changeover = since < 0.0F ? 0.0F : 1.0F - Mth.clamp(since / 38.0F, 0.0F, 1.0F);
      }

      long tentacleStamp = entity.getFrontTentacleAnimGameTime();
      state.frontTentacleElapsedTicks = tentacleStamp < 0L ? -1.0F : (float)(gt - tentacleStamp) + partialTick;
      long miniStamp = entity.getMiniHeadAnimGameTime();
      state.miniHeadElapsedTicks = miniStamp < 0L ? -1.0F : (float)(gt - miniStamp) + partialTick;
      float[] headXRots = entity.getHeadXRots();
      float[] headYRots = entity.getHeadYRots();

      for (int i = 0; i < 2; i++) {
         state.headXRot[i] = headXRots[i];
         state.headYRot[i] = headYRots[i];
      }

      state.snatchActive = false;
      int snatchId = entity.getSnatchId();
      if (snatchId >= 0 && entity.level().getEntity(snatchId) instanceof LivingEntity victim) {
         double sx = Mth.lerp((double)partialTick, entity.xOld, entity.getX());
         double sy = Mth.lerp((double)partialTick, entity.yOld, entity.getY());
         double sz = Mth.lerp((double)partialTick, entity.zOld, entity.getZ());
         state.snatchActive = true;
         state.snatchRelX = Mth.lerp((double)partialTick, victim.xOld, victim.getX()) - sx;
         state.snatchRelY = Mth.lerp((double)partialTick, victim.yOld, victim.getY()) - sy;
         state.snatchRelZ = Mth.lerp((double)partialTick, victim.zOld, victim.getZ()) - sz;
      }
   }

   public void submit(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      poseStack.pushPose();
      applyChangeoverShake(poseStack, state);
      this.frameCollector = submitNodeCollector;
      this.submitPortal(poseStack, submitNodeCollector, state);

      try {
         if (state.preview != null && !this.previewShadowPass) {
            state.preview.submitGround(poseStack, submitNodeCollector);
            state.preview.submitSun(poseStack, submitNodeCollector);
            if (state.preview.castShadow) {
               this.previewShadowPass = true;
               state.preview.pushShadow(poseStack);

               try {
                  this.submit(state, poseStack, submitNodeCollector, camera);
               } finally {
                  poseStack.popPose();
                  this.previewShadowPass = false;
               }
            }
         }

         if (state.preview != null) {
            this.submitPreviewHeads(state, poseStack, submitNodeCollector);
         }

         this.model = (EntityModel)(this.previewShadowPass
            ? (state.devourer ? this.devourerShadowModel : (state.phase4 ? this.stormP4ShadowModel : this.commandBlockShadowModel))
            : (state.devourer ? this.devourerModel : (state.phase4 ? this.stormP4Model : this.commandBlockModel)));
         if (state.phase4) {
            float debris58 = state.devourer ? -1.0F : state.phase58ElapsedTicks;
            if (CollapseAnim.down(state.collapseTicks) < 0.999F && !this.previewShadowPass) {
               StormDebris.submit(
                  poseStack,
                  submitNodeCollector,
                  state.idleTimeTicks,
                  state.lightCoords,
                  state.phase5ElapsedTicks,
                  debris58,
                  state.stormId,
                  state.devourer,
                  debrisSettle(state.phase),
                  growthScale(state),
                  state.preview != null
               );
            }

            if (state.preview == null) {
               // LAYER 3 (Telltale architecture): the entity pass is strictly
               // FOREGROUND now - the sky backdrop, night aura shells and the
               // legacy glare planes all moved to the native sky pass
               // (SkyRendererMixin + StormSkyBox). The only atmosphere left
               // here is the single 2D core glow billboard riding the storm's
               // centre, plus the collapse event glow.
               this.submitCoreGlow(state, poseStack, submitNodeCollector, camera);
               this.submitCollapseGlow(state, poseStack, submitNodeCollector, camera);
            }

            if (state.phase >= 4.5) {
               this.submitGrowth5(state, poseStack, submitNodeCollector);
            }

            if (state.phase >= 5.0) {
               this.submitTentacles5(state, poseStack, submitNodeCollector);
            }
         } else {
            this.submitHunchback(state, poseStack, submitNodeCollector);
            if (state.phase >= 3.0 && state.phase < 4.0) {
               this.submitFrontTentacle(state, poseStack, submitNodeCollector);
            }

            if (!this.previewShadowPass) {
               StormDebris.submitEarly(poseStack, submitNodeCollector, state.idleTimeTicks, state.lightCoords, (float)state.phase, state.stormId);
            }
         }

         super.submit(state, poseStack, submitNodeCollector, camera);
      } finally {
         poseStack.popPose();
      }
   }

   private void submitHunchback(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      if (!(state.phase < 0.2)) {
         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
         applyCollapse(poseStack, state, 17.0);
         if (state.bodyRoll != 0.0F) {
            poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
         }

         poseStack.scale(-2.0F, -2.0F, 2.0F);
         poseStack.translate(0.0, -1.501, 0.0);
         submitNodeCollector.submitModel(
            this.previewShadowPass ? this.hunchbackShadowModel : this.hunchbackModel,
            state,
            poseStack,
            this.pieceType(StormSkins.legacy()),
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            this.pieceTint(state),
            null,
            0,
            null
         );
         poseStack.popPose();
      }
   }

   private void submitFrontTentacle(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      float since = state.frontTentacleElapsedTicks;
      if (!(since < 0.0F)) {
         Tentacle tentacle = this.previewShadowPass ? this.frontTentacleShadowModel : this.frontTentacleModel;
         tentacle.revealProgress = 1.0F;
         tentacle.sprout = Mth.clamp(since / 70.0F, 0.0F, 1.0F);
         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
         applyCollapse(poseStack, state, 17.0);
         if (state.bodyRoll != 0.0F) {
            poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
         }

         poseStack.scale(-2.0F, -2.0F, 2.0F);
         poseStack.translate(0.0, -1.501, 0.0);
         poseStack.translate(0.0, -1.1F, -0.5);
         poseStack.mulPose(Axis.YP.rotationDegrees(-14.0F));
         poseStack.mulPose(Axis.XP.rotationDegrees(36.0F));
         poseStack.scale(0.42F, 0.42F, 0.42F);
         submitNodeCollector.submitModel(
            tentacle, state, poseStack, this.pieceType(StormSkins.legacy()), state.lightCoords, OverlayTexture.NO_OVERLAY, this.pieceTint(state), null, 0, null
         );
         poseStack.popPose();
      }
   }

   private RenderType pieceType(Identifier texture) {
      return this.previewShadowPass ? PreviewScene.shadowType() : FoglessRenderTypes.bodyCutout(texture);
   }

   private RenderType pieceType(Identifier texture, boolean cull) {
      return this.previewShadowPass ? PreviewScene.shadowType() : FoglessRenderTypes.bodyCutout(texture, cull);
   }

   private int pieceTint(WitherStormRenderState state) {
      return this.previewShadowPass ? 940578856 : this.applyStormTint(-1, state);
   }

   private void submitPreviewHeads(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
      int count = previewHeadCount(state.phase);
      if (count > 0) {
         boolean early = !state.phase4;
         float scale = early ? 1.35F : 6.0F;
         Vec3[] offsets = new Vec3[count];
         float[] scales = new float[count];
         float[] restYaw = new float[count];
         float[] restRoll = new float[count];

         for (int i = 0; i < count; i++) {
            restYaw[i] = early ? 0.0F : WitherStormEntity.restYawFor(i);
            restRoll[i] = early ? 0.0F : WitherStormEntity.restRollFor(i);
            offsets[i] = early ? new Vec3(0.0, 3.05, 0.14) : WitherStormEntity.headOffset(i, state.devourer);
            scales[i] = scale;
         }

         this.previewHeads
            .submit(
               state.preview,
               poseStack,
               collector,
               this.previewShadowPass,
               offsets,
               scales,
               restYaw,
               restRoll,
               state.bodyRot,
               state.idleTimeTicks,
               state.devourer,
               early,
               early ? StormSkins.legacy() : StormSkins.phase4()
            );
      }
   }

   private static int previewHeadCount(double phase) {
      if (phase < 2.0) {
         return 0;
      } else {
         return phase < 4.0 ? 1 : 3;
      }
   }

   private static float growthScale(WitherStormRenderState state) {
      return (float)WitherStormEntity.clientGrowthScaleForPhase(Math.max(state.phase, state.expansionPhase));
   }

   /** Overall body scale: capped shortly after late phase 5 so the whole storm stops ballooning. */
   private static float bodyScale(WitherStormRenderState state) {
      return (float)WitherStormEntity.clientBodyScaleForPhase(Math.max(state.phase, state.expansionPhase));
   }

   /** Outward back/cube-mass scale: keeps expanding forever past late phase 5 and through phase 6+. */
   private static float backScale(WitherStormRenderState state) {
      return (float)WitherStormEntity.clientBackScaleForPhase(Math.max(state.phase, state.expansionPhase));
   }

   private static double auraRadius(WitherStormRenderState state) {
      // follows the OUTWARD back/cube mass, not the whole-body scale: the aura
      // is the reach of the storm's presence, so if back growth and body growth
      // ever diverge the aura must track the back mass
      return (26.0 + 9.0 * Math.max(0.0, state.phase - 4.0)) * (double)backScale(state);
   }

   private static float shadedModelAmount(WitherStormRenderState state) {
      if (!StormSkins.shaded()) {
         return 0.0F;
      } else {
         float phaseRamp = Mth.clamp((float)((state.phase - 4.15) / 1.45), 0.0F, 1.0F);
         float growth = Mth.clamp(growthScale(state) - 0.9F, 0.0F, 1.0F);
         return Math.max(phaseRamp, growth * 0.7F) * state.collapseFade;
      }
   }

   private static float phase4EmissiveGain(WitherStormRenderState state) {
      float hatch = Mth.clamp(state.hatch, 0.0F, 1.0F);
      float late = Mth.clamp((float)((state.phase - 4.75) / 0.95), 0.0F, 1.0F);
      float whiteout = Mth.clamp(state.collapseWhiteout * 0.85F, 0.0F, 0.85F);
      float shaded = shadedModelAmount(state);
      return (0.26F + late * 0.28F + whiteout + shaded * 0.16F) * hatch * state.collapseFade;
   }

   private static int phase4EmissiveTint(WitherStormRenderState state) {
      float late = Mth.clamp((float)((state.phase - 5.38) / 0.47), 0.0F, 1.0F);
      float shaded = shadedModelAmount(state) * 0.55F;
      float[] cloud = StormPalettes.cloudColor(state.phase, new float[3]);
      // Phase 4 keeps a clean light-blue emissive core (the references show a
      // blue glow, never green or plain white); the atmosphere palette only
      // starts bleeding in from late phase 4.75 onward.
      float r = Mth.lerp(late * 0.7F + shaded * 0.22F, 0.62F, Mth.clamp(cloud[0] + 0.16F, 0.0F, 1.0F));
      float g = Mth.lerp(late * 0.55F + shaded * 0.16F, 0.80F, Mth.clamp(cloud[1] + 0.10F, 0.0F, 1.0F));
      float b = Mth.lerp(late * 0.82F + shaded * 0.28F, 1.0F, Mth.clamp(cloud[2] + 0.18F, 0.0F, 1.0F));
      return 0xFF000000 | (int)(r * 255.0F) << 16 | (int)(g * 255.0F) << 8 | (int)(b * 255.0F);
   }

   private static int[][] nightLightColours(WitherStormRenderState state) {
      if (!StormSkins.shaded()) {
         return NIGHT_LAYER_COLOURS;
      } else {
         float[] halo = StormPalettes.haloUnderColor(new float[3]);
         float[] pulse = StormPalettes.pulseColor(state.phase, new float[3]);
         float[] cloud = StormPalettes.cloudColor(state.phase, new float[3]);
         return new int[][]{
            {(int)(Mth.lerp(0.42F, halo[0], pulse[0]) * 255.0F), (int)(Mth.lerp(0.42F, halo[1], pulse[1]) * 255.0F), (int)(Mth.lerp(0.42F, halo[2], pulse[2]) * 255.0F)},
            {(int)(Mth.lerp(0.24F, pulse[0], cloud[0]) * 255.0F), (int)(Mth.lerp(0.24F, pulse[1], cloud[1]) * 255.0F), (int)(Mth.lerp(0.24F, pulse[2], cloud[2]) * 255.0F)},
            {(int)(cloud[0] * 255.0F), (int)(cloud[1] * 255.0F), (int)(cloud[2] * 255.0F)}
         };
      }
   }

   private void submitNightLight(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
      if (DevouringStormsClientConfig.blackGlare && state.phase >= 5.0) {
         return;
      }
      float night = Math.min(state.nightFactor, 1.0F);
      float phaseRamp = Mth.clamp((float)((state.phase - 4.0) / 0.6), 0.0F, 1.0F);
      float shaded = shadedModelAmount(state);
      float daylightBacklight = 0.10F * phaseRamp + shaded * (0.04F + 0.08F * phaseRamp);
      float strength = (float)DevouringStormsClientConfig.stormGlowStrength * state.collapseFade;
      if (!(strength <= 0.01F) && !(night <= 0.01F && daylightBacklight <= 0.01F)) {
         float pulse = 0.94F + 0.06F * Mth.sin((double)(state.idleTimeTicks * 0.04F));
         float amount = (daylightBacklight + night * (StormSkins.shaded() ? 0.80F : 0.72F)) * strength * pulse;
         double radius = auraRadius(state) * (0.88 + shaded * 0.06);
         // world-space gradient shell around the storm's sides - never a
         // camera-facing quad
         pushStormWorld(poseStack, state);
         submitGradientCylinder(collector, poseStack, radius * 1.06, radius * 0.16, radius * 0.78, nightLightColours(state)[0][0], nightLightColours(state)[0][1], nightLightColours(state)[0][2], (int)(126.0F * amount * 0.62F), GlowRenderTypes.glow(HALO_BAND_TEXTURE), 7);
         submitGradientCylinder(collector, poseStack, radius * 1.34, radius * 0.02, radius * 0.94, nightLightColours(state)[2][0], nightLightColours(state)[2][1], nightLightColours(state)[2][2], (int)(62.0F * amount * 0.62F), GlowRenderTypes.glow(HALO_BAND_TEXTURE), 7);
         poseStack.popPose();
      }
   }

   private static String stageShellName(WitherStormRenderState state) {
      // the traced Blockbench stage bodies render in EVERY skin now - the OG
      // look is the traced models with the original textures, not just the
      // separate shaded preset
      return DevouringStormsClientConfig.stormStageShells ? StormStageShells.shellForPhase(state.phase) : null;
   }

   private static float stageShellAlpha(WitherStormRenderState state) {
      if (!DevouringStormsClientConfig.stormStageShells) {
         return 0.0F;
      } else if (!state.phase4) {
         float early = 0.34F + 0.18F * Mth.clamp((float)(state.phase / 3.0), 0.0F, 1.0F);
         return early * state.collapseFade;
      } else {
         float ramp = Mth.clamp((float)((state.phase - 4.15) / 0.55), 0.0F, 1.0F);
         float devourerBoost = state.devourer ? 0.18F + 0.18F * StormPalettes.phaseAmount(state.phase, 6.0F, 6.25F) : 0.0F;
         return Mth.clamp(ramp + devourerBoost, 0.0F, 1.0F) * state.hatch * state.collapseFade;
      }
   }

   private void submitStageShell(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
      String shell = stageShellName(state);
      float alpha = stageShellAlpha(state);
      if (shell != null && alpha > 0.01F && !this.previewShadowPass) {
         StormStageShells.submit(shell, state.phase, poseStack, collector, state.lightCoords, this.pieceTint(state), alpha, state.phase >= 5.0);
      }
   }

   /**
    * The traced inner-glow core (fx_witherCubeInnerGlow from the Blockbench
    * FX project): an emissive cube nested inside the body, breathing slowly.
    * This is the storm's glowing heart from the Story-Mode shots -- the last
    * piece of the Traced_shading_Textures set that had a texture but no
    * renderer. Only in the shaded-shell presentation, phase 4 onward.
    */
   private void submitInnerGlow(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
      if (this.previewShadowPass || !StormSkins.shaded() || !DevouringStormsClientConfig.stormStageShells || state.phase < 4.0) {
         return;
      }
      float ramp = Mth.clamp((float)((state.phase - 4.0) / 0.35), 0.0F, 1.0F) * state.collapseFade * state.hatch;
      if (ramp <= 0.01F) {
         return;
      }
      float growth = growthScale(state);
      float half = (float)((12.0 + Math.max(0.0, state.phase - 4.0) * 3.6) * (double)growth * 0.46);
      float pulse = 0.82F + 0.18F * Mth.sin((float)(Util.getMillis() % 1000000L) * 0.0045F);
      int a = (int)(190.0F * ramp * pulse);
      if (a <= 3) {
         return;
      }
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.emitterMark(INNER_GLOW_TEXTURE), (pose, consumer) -> {
         emitGlowCube(consumer, pose, half, a);
      });
   }

   private static void emitGlowCube(VertexConsumer consumer, PoseStack.Pose pose, float h, int a) {
      glowFace(consumer, pose, -h, h, -h, h, h, -h, h, h, h, -h, h, h, a);
      glowFace(consumer, pose, -h, -h, -h, -h, -h, h, h, -h, h, h, -h, h, a);
      glowFace(consumer, pose, -h, -h, -h, h, -h, -h, h, h, -h, -h, h, -h, a);
      glowFace(consumer, pose, h, -h, -h, h, -h, h, h, h, h, h, h, -h, a);
      glowFace(consumer, pose, -h, -h, -h, -h, h, -h, -h, h, h, -h, -h, h, a);
      glowFace(consumer, pose, -h, -h, h, h, -h, h, h, h, h, -h, h, h, a);
   }

   private static void glowFace(VertexConsumer consumer, PoseStack.Pose pose, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, int a) {
      consumer.addVertex(pose, ax, ay, az).setColor(255, 255, 255, a).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
      consumer.addVertex(pose, bx, by, bz).setColor(255, 255, 255, a).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
      consumer.addVertex(pose, cx, cy, cz).setColor(255, 255, 255, a).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
      consumer.addVertex(pose, dx, dy, dz).setColor(255, 255, 255, a).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   private void submitSkyBackdrop(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
      if (this.previewShadowPass || !DevouringStormsClientConfig.blackGlare || state.phase < 4.0) {
         return;
      }
      float strength = (float)DevouringStormsClientConfig.blackGlareStrength * state.collapseFade * Mth.clamp((float)((state.phase - 4.0) / 0.32), 0.0F, 1.0F);
      if (strength <= 0.004F) {
         return;
      }

      float growth = growthScale(state);
      double bodyR = (12.0 + Math.max(0.0, state.phase - 4.0) * 3.6) * (double)growth;
      float phase4Halo = StormPalettes.phaseAmount(state.phase, 4.0F, 4.38F) * (1.0F - StormPalettes.phaseAmount(state.phase, 4.88F, 5.05F));
      float turquoiseHalo = StormPalettes.phaseAmount(state.phase, 4.95F, 5.12F) * (1.0F - StormPalettes.phaseAmount(state.phase, 5.18F, 5.34F));
      float purpleHalo = StormPalettes.phaseAmount(state.phase, 5.12F, 5.42F);
      float blueHalo = StormPalettes.phaseAmount(state.phase, 5.48F, 5.72F);
      float pinkHalo = StormPalettes.phaseAmount(state.phase, 5.45F, 5.95F);
      float phase6Halo = StormPalettes.phaseAmount(state.phase, 5.95F, 6.18F);
      float riseBlack = state.phase >= 6.0 ? 1.0F - Mth.clamp(state.hatch * 1.75F, 0.0F, 1.0F) : 0.0F;

      float darkR = 0.04F;
      float darkG = 0.03F;
      float darkB = 0.06F;
      darkR = Mth.lerp(turquoiseHalo * 0.65F, darkR, 0.02F);
      darkG = Mth.lerp(turquoiseHalo * 0.65F, darkG, 0.08F);
      darkB = Mth.lerp(turquoiseHalo * 0.65F, darkB, 0.10F);
      darkR = Mth.lerp(purpleHalo, darkR, 0.06F);
      darkG = Mth.lerp(purpleHalo, darkG, 0.02F);
      darkB = Mth.lerp(purpleHalo, darkB, 0.13F);
      darkR = Mth.lerp(blueHalo * 0.72F, darkR, 0.03F);
      darkG = Mth.lerp(blueHalo * 0.72F, darkG, 0.05F);
      darkB = Mth.lerp(blueHalo * 0.72F, darkB, 0.16F);
      darkR = Mth.lerp(pinkHalo * 0.40F, darkR, 0.10F);
      darkG = Mth.lerp(pinkHalo * 0.40F, darkG, 0.03F);
      darkB = Mth.lerp(pinkHalo * 0.40F, darkB, 0.11F);
      darkR = Mth.lerp(riseBlack, darkR, 0.0F);
      darkG = Mth.lerp(riseBlack, darkG, 0.0F);
      darkB = Mth.lerp(riseBlack, darkB, 0.0F);

      float rimR = Mth.lerp(phase4Halo, 0.18F, 0.92F);
      float rimG = Mth.lerp(phase4Halo, 0.24F, 0.96F);
      float rimB = Mth.lerp(phase4Halo, 0.32F, 1.0F);
      rimR = Mth.lerp(turquoiseHalo, rimR, 0.18F);
      rimG = Mth.lerp(turquoiseHalo, rimG, 0.28F);
      rimB = Mth.lerp(turquoiseHalo, rimB, 0.34F);
      rimR = Mth.lerp(purpleHalo, rimR, 0.58F);
      rimG = Mth.lerp(purpleHalo, rimG, 0.23F);
      rimB = Mth.lerp(purpleHalo, rimB, 0.76F);
      rimR = Mth.lerp(blueHalo, rimR, 0.30F);
      rimG = Mth.lerp(blueHalo, rimG, 0.42F);
      rimB = Mth.lerp(blueHalo, rimB, 0.98F);
      rimR = Mth.lerp(pinkHalo, rimR, 0.96F);
      rimG = Mth.lerp(pinkHalo, rimG, 0.42F);
      rimB = Mth.lerp(pinkHalo, rimB, 0.84F);
      rimR = Mth.lerp(riseBlack, rimR, 0.04F);
      rimG = Mth.lerp(riseBlack, rimG, 0.04F);
      rimB = Mth.lerp(riseBlack, rimB, 0.06F);

      int dr = (int)(Mth.clamp(darkR, 0.0F, 1.0F) * 255.0F);
      int dg = (int)(Mth.clamp(darkG, 0.0F, 1.0F) * 255.0F);
      int db = (int)(Mth.clamp(darkB, 0.0F, 1.0F) * 255.0F);
      int rr = (int)(Mth.clamp(rimR, 0.0F, 1.0F) * 255.0F);
      int rg = (int)(Mth.clamp(rimG, 0.0F, 1.0F) * 255.0F);
      int rb = (int)(Mth.clamp(rimB, 0.0F, 1.0F) * 255.0F);
      int phase4A = (int)((36.0F + 52.0F * phase4Halo) * strength);
      int darkA = (int)((42.0F + 78.0F * (turquoiseHalo + purpleHalo * 0.65F + phase6Halo * 0.35F)) * strength);
      int rimA = (int)((28.0F + 64.0F * (phase4Halo + purpleHalo * 0.65F + blueHalo * 0.55F + pinkHalo * 0.45F + phase6Halo * 0.35F)) * strength);
      int outerA = (int)((18.0F + 46.0F * (turquoiseHalo + purpleHalo + blueHalo * 0.7F + phase6Halo * 0.4F)) * strength);
      double fillW = bodyR * (1.34 + phase6Halo * 0.12);
      double fillH = bodyR * (0.98 + phase6Halo * 0.08);

      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
      applyCollapse(poseStack, state, 17.0);
      if (state.bodyRoll != 0.0F) {
         poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
      }
      poseStack.translate(0.0, bodyR * 0.76, -bodyR * 1.02);
      submitHaloPlane(collector, poseStack, fillW, fillH, dr, dg, db, darkA, GlowRenderTypes.translucent(PORTAL_TEXTURE));
      submitHaloPlane(collector, poseStack, fillW * 1.12, fillH * 1.08, dr, dg, db, outerA, GlowRenderTypes.translucent(HALO_TEXTURE));

      for (float yaw : new float[]{-28.0F, 28.0F}) {
         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
         submitHaloPlane(collector, poseStack, bodyR * 1.10, bodyR * 0.84, rr, rg, rb, rimA, GlowRenderTypes.translucent(HALO_TEXTURE));
         poseStack.popPose();
      }
      if (phase4A > 2) {
         submitHaloPlane(collector, poseStack, bodyR * 1.18, bodyR * 0.88, 238, 244, 255, phase4A, GlowRenderTypes.glow(HALO_TEXTURE));
      }
      if (phase6Halo > 0.01F) {
         int riseMaskA = (int)((54.0F + riseBlack * 96.0F) * strength);
         poseStack.pushPose();
         poseStack.translate(0.0, -bodyR * 0.24, -bodyR * 0.08);
         submitHaloPlane(collector, poseStack, bodyR * 1.28, bodyR * 0.36, 255, 112, 28, (int)((58.0F + 44.0F * phase6Halo) * strength), GlowRenderTypes.translucent(PORTAL_TEXTURE));
         submitHaloPlane(collector, poseStack, bodyR * 1.10, bodyR * 0.24, 214, 28, 54, (int)((48.0F + 38.0F * phase6Halo) * strength), GlowRenderTypes.translucent(HALO_TEXTURE));
         poseStack.popPose();
         poseStack.pushPose();
         poseStack.translate(0.0, bodyR * 0.18, 0.0);
         submitHaloPlane(collector, poseStack, bodyR * 1.20, bodyR * 0.50, 152, 70, 232, (int)((42.0F + 44.0F * phase6Halo) * strength), GlowRenderTypes.translucent(HALO_TEXTURE));
         poseStack.popPose();
         poseStack.pushPose();
         poseStack.translate(0.0, bodyR * 0.70, bodyR * 0.04);
         submitHaloPlane(collector, poseStack, bodyR * 1.44, bodyR * 0.72, 10, 8, 14, riseMaskA, GlowRenderTypes.translucent(PORTAL_TEXTURE));
         poseStack.popPose();
      }
      if (DevouringStormsClientConfig.earlyVortexRings || state.phase >= 7.5) {
         float vortex = DevouringStormsClientConfig.earlyVortexRings ? Mth.clamp((float)((state.phase - 5.8) / 0.65), 0.0F, 1.0F) : Mth.clamp((float)((state.phase - 7.5) / 0.9), 0.0F, 1.0F);
         if (vortex > 0.01F) {
            for (int layer = 0; layer < 3; layer++) {
               float lf = layer / 2.0F;
               poseStack.pushPose();
               poseStack.translate(0.0, bodyR * (0.18 + lf * 0.44), -bodyR * (0.50 + lf * 0.24));
               poseStack.mulPose(Axis.YP.rotationDegrees(state.idleTimeTicks * (2.0F + layer * 0.55F) + layer * 57.0F));
               submitHaloPlane(collector, poseStack, bodyR * (1.22 + lf * 0.68), bodyR * (0.52 + lf * 0.18), 18, 6, 32, (int)((50.0F + 18.0F * layer) * vortex * strength), GlowRenderTypes.translucent(VORTEX_RING_TEXTURE));
               poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
               submitHaloPlane(collector, poseStack, bodyR * (1.22 + lf * 0.68), bodyR * (0.52 + lf * 0.18), rr, rg, rb, (int)((34.0F + 14.0F * layer) * vortex * strength), GlowRenderTypes.translucent(VORTEX_RING_TEXTURE));
               poseStack.popPose();
            }
         }
      }
      poseStack.popPose();
   }

   /**
    * The storm's ONE and only 2D billboard: the BLUE HALO. In phase 4 it is
    * the classic reference look - a light-blue halo pinned to the very
    * middle of the storm so the whole mass reads as lit from its centre,
    * blue light looming off its sides. The same single glow then simply
    * follows the sky palette (teal -> purple -> pink -> mutated magenta-red).
    * Everything else atmospheric about the storm lives in the native sky pass
    * (StormSkyBox), never on the entity renderer.
    *
    * Very late (7.5+/8) the horizontal purple + dark-pink vortex rings still
    * appear around the giant.
    */
   private void submitCoreGlow(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      if (this.previewShadowPass || !DevouringStormsClientConfig.cataclysmHalos || state.phase < 4.0) {
         return;
      }
      float strength = (float)DevouringStormsClientConfig.haloStrength * state.collapseFade;
      if (strength <= 0.01F) {
         return;
      }

      float phase = (float)state.phase;
      double radius = auraRadius(state);
      float breathe = 0.9F + 0.1F * Mth.sin((double)(state.idleTimeTicks * 0.028F));

      // (billboard math removed: the core light is physical now — a glowing
      // sphere of additive world-space rings in the storm's own frame, so it
      // can never detach, flip, or clip inside the mass as it moves)

      // THE one billboard, per the reference shots: the BLUE halo riding the
      // very middle of the storm in phase 4 - the mass lit from its centre
      // with light blue, looming off its sides. Later phases keep the exact
      // same single glow, only its colour follows the sky palette.
      float blueHalo = StormPalettes.phaseAmount(phase, 4.0F, 4.2F) * (1.0F - StormPalettes.phaseAmount(phase, 4.9F, 5.15F));
      float tealWash = StormPalettes.phaseAmount(phase, 4.92F, 5.1F) * (1.0F - StormPalettes.phaseAmount(phase, 5.16F, 5.3F));
      float purpleWash = StormPalettes.phaseAmount(phase, 5.12F, 5.42F);
      float pinkWash = StormPalettes.phaseAmount(phase, 5.55F, 5.95F);
      float mutation = StormPalettes.phaseAmount(phase, 5.95F, 6.3F);
      // halo edge colour: light blue (4) -> teal -> purple -> pink -> mutated magenta-red
      float er = 0.42F;
      float eg = 0.72F;
      float eb = 1.0F;
      er = Mth.lerp(tealWash * 0.6F, er, 0.24F);
      eg = Mth.lerp(tealWash * 0.6F, eg, 0.66F);
      er = Mth.lerp(purpleWash, er, 0.56F);
      eg = Mth.lerp(purpleWash, eg, 0.24F);
      eb = Mth.lerp(purpleWash, eb, 0.86F);
      er = Mth.lerp(pinkWash * 0.5F, er, 0.96F);
      eg = Mth.lerp(pinkWash * 0.5F, eg, 0.42F);
      eb = Mth.lerp(pinkWash * 0.5F, eb, 0.84F);
      er = Mth.lerp(mutation, er, 1.0F);
      eg = Mth.lerp(mutation, eg, 0.32F);
      eb = Mth.lerp(mutation, eb, 0.78F);
      // hot centre: near-white with the same drift
      float cr = Mth.lerp(mutation, 0.94F, 1.0F);
      float cg = Mth.lerp(mutation, 0.97F, 0.64F);
      float cb = Mth.lerp(mutation, 1.0F, 0.96F);
      int eR = (int)(Mth.clamp(er, 0.0F, 1.0F) * 255.0F);
      int eG = (int)(Mth.clamp(eg, 0.0F, 1.0F) * 255.0F);
      int eB = (int)(Mth.clamp(eb, 0.0F, 1.0F) * 255.0F);
      int cR = (int)(Mth.clamp(cr, 0.0F, 1.0F) * 255.0F);
      int cG = (int)(Mth.clamp(cg, 0.0F, 1.0F) * 255.0F);
      int cB = (int)(Mth.clamp(cb, 0.0F, 1.0F) * 255.0F);
      float ramp = StormPalettes.phaseAmount(phase, 4.0F, 4.3F);
      int glowA = (int)((72.0F + 92.0F * ramp) * strength * breathe);
      // the blue phase-4 halo looms larger - it lights the storm's sides
      float discScale = Mth.lerp(blueHalo, 0.98F, 1.22F);

      pushStormWorld(poseStack, state);

      if (glowA > 2) {
         // THE core light, physically attached to the storm: a glowing sphere
         // built from stacked world-space rings (plus one soft outer shell),
         // all in the storm's own frame. Reads as a light volume from every
         // angle and follows the storm wherever it goes.
         double ry = radius * 0.72 * discScale;
         double rx = radius * 0.92 * discScale;
         int shells = 7;
         for (int k = 0; k < shells; k++) {
            float frac = (float)k / (float)(shells - 1);
            double y = (double)(frac - 0.5F) * 2.0 * ry;
            double profile = Math.sqrt(Math.max(0.0, 1.0 - (double)((frac - 0.5F) * (frac - 0.5F)) * 4.0));
            double ringR = rx * profile;
            if (ringR < radius * 0.02) {
               continue;
            }

            float band = 0.55F + 0.45F * (float)profile;
            boolean core = Math.abs(frac - 0.5F) < 0.34F;
            int rr = core ? Math.min(255, cR + 18) : eR;
            int rg = core ? Math.min(255, cG + 10) : eG;
            int rb = core ? cB : eB;
            int ra = (int)((float)glowA * (core ? 1.0F : 0.62F) * band);
            submitWorldRing(collector, poseStack, state.idleTimeTicks, ringR, radius * 0.16, y, rr, rg, rb, ra);
         }

         // one wide, faint outer shell so the light "looms" off the mass
         submitWorldRing(collector, poseStack, state.idleTimeTicks * 0.6F, rx * 1.22, radius * 0.3, 0.0, eR, eG, eB, (int)((float)glowA * 0.3F));
      }

      // --- phase 7.5+/8: the ringed giant, purple + dark-pink vortex rings ---
      if (DevouringStormsClientConfig.earlyVortexRings || state.phase >= 7.5) {
         float vortex = DevouringStormsClientConfig.earlyVortexRings ? Mth.clamp((float)((state.phase - 5.8) / 0.65), 0.0F, 1.0F) : Mth.clamp((float)((state.phase - 7.5) / 0.9), 0.0F, 1.0F);
         if (vortex > 0.01F) {
            for (int layer = 0; layer < 3; layer++) {
               float lf = layer / 2.0F;
               boolean darkPink = layer % 2 == 1;
               submitWorldRing(collector, poseStack, state.idleTimeTicks, radius * (1.14 + lf * 0.5), radius * (0.1 + lf * 0.03), radius * (0.3 + lf * 0.4), darkPink ? 226 : 148, darkPink ? 62 : 52, darkPink ? 168 : 224, (int)((44.0F + 15.0F * layer) * vortex * strength));
            }
         }
      }

      poseStack.popPose();
   }

   /** Push the entity transform into world axes (yaw/collapse/roll) for storm-attached world-space effects. */
   private static void pushStormWorld(PoseStack poseStack, WitherStormRenderState state) {
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
      applyCollapse(poseStack, state, 17.0);
      if (state.bodyRoll != 0.0F) {
         poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
      }
   }

   /**
    * A soft gradient light shell wrapped around the storm's sides: N vertical
    * gradient quads standing on a circle around the entity, world-space and
    * storm-attached, depth-tested but never writing depth.
    */
   private static void submitGradientCylinder(SubmitNodeCollector collector, PoseStack poseStack, double radius, double y0, double y1, int r, int g, int b, int alpha, RenderType type, int segments) {
      if (alpha <= 2 || radius <= 0.01 || y1 <= y0) {
         return;
      }
      double seg = Math.PI * 2.0 / (double)segments;
      double halfArc = radius * Math.tan(seg / 2.0) * 1.3;
      for (int i = 0; i < segments; i++) {
         double ang = seg * (double)i;
         double cx = Math.cos(ang) * radius;
         double cz = Math.sin(ang) * radius;
         double tx = -Math.sin(ang);
         double tz = Math.cos(ang);
         double ax = cx + tx * halfArc;
         double az = cz + tz * halfArc;
         double bx = cx - tx * halfArc;
         double bz = cz - tz * halfArc;
         float nx = (float)Math.cos(ang);
         float nz = (float)Math.sin(ang);
         double ya = y0;
         double yb = y1;
         collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            consumer.addVertex(pose, (float)ax, (float)ya, (float)az).setColor(r, g, b, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, 0.0F, nz);
            consumer.addVertex(pose, (float)bx, (float)ya, (float)bz).setColor(r, g, b, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, 0.0F, nz);
            consumer.addVertex(pose, (float)bx, (float)yb, (float)bz).setColor(r, g, b, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, 0.0F, nz);
            consumer.addVertex(pose, (float)ax, (float)yb, (float)az).setColor(r, g, b, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, 0.0F, nz);
         });
      }
   }

   /** A large soft radial-gradient back-light plate strictly behind the storm mass (world-space, depth-tested). */
   private static void submitBackPlate(SubmitNodeCollector collector, PoseStack poseStack, double radius, int r, int g, int b, int alpha) {
      if (alpha <= 2) {
         return;
      }
      double halfW = radius * 1.15;
      double halfH = radius * 0.72;
      double z = -radius * 1.12;
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(HALO_GRADIENT_TEXTURE), (pose, consumer) -> {
         consumer.addVertex(pose, (float)-halfW, (float)(radius * 0.2 - halfH), (float)z).setColor(r, g, b, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F);
         consumer.addVertex(pose, (float)halfW, (float)(radius * 0.2 - halfH), (float)z).setColor(r, g, b, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F);
         consumer.addVertex(pose, (float)halfW, (float)(radius * 0.2 + halfH), (float)z).setColor(r, g, b, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F);
         consumer.addVertex(pose, (float)-halfW, (float)(radius * 0.2 + halfH), (float)z).setColor(r, g, b, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F);
      });
   }

   /** Flat world-space gradient ring lying in the storm's horizontal plane (late vortex layers). */
   private static void submitWorldRing(SubmitNodeCollector collector, PoseStack poseStack, float timeTicks, double radius, double thickness, double y, int r, int g, int b, int alpha) {
      if (alpha <= 2 || radius <= 0.01) {
         return;
      }
      int segments = 26;
      double spin = (double)(timeTicks * 0.031F);
      double rIn = radius - thickness * 0.5;
      double rOut = radius + thickness * 0.5;
      for (int i = 0; i < segments; i++) {
         double a0 = Math.PI * 2.0 * (double)i / (double)segments + spin;
         double a1 = Math.PI * 2.0 * (double)(i + 1) / (double)segments + spin;
         int segAlpha = (int)((float)alpha * (0.74F + 0.26F * (float)Math.sin(a0 * 3.0)));
         if (segAlpha <= 2) {
            continue;
         }
         double c0 = Math.cos(a0);
         double s0 = Math.sin(a0);
         double c1 = Math.cos(a1);
         double s1 = Math.sin(a1);
         collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(HALO_GRADIENT_TEXTURE), (pose, consumer) -> {
            consumer.addVertex(pose, (float)(c0 * rIn), (float)y, (float)(s0 * rIn)).setColor(r, g, b, segAlpha).setUv(0.15F, 0.15F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
            consumer.addVertex(pose, (float)(c1 * rIn), (float)y, (float)(s1 * rIn)).setColor(r, g, b, segAlpha).setUv(0.85F, 0.15F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
            consumer.addVertex(pose, (float)(c1 * rOut), (float)y, (float)(s1 * rOut)).setColor(r, g, b, segAlpha).setUv(0.85F, 0.85F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
            consumer.addVertex(pose, (float)(c0 * rOut), (float)y, (float)(s0 * rOut)).setColor(r, g, b, segAlpha).setUv(0.15F, 0.85F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         });
      }
   }

   private void submitCollapseGlow(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      if (this.previewShadowPass || state.collapseWhiteout <= 0.004F || state.collapseFade <= 0.0F) {
         return;
      }
      double radius = auraRadius(state) * (0.95 + 0.40 * state.collapseWhiteout);
      float amount = state.collapseWhiteout * state.collapseFade * 1.45F;
      pushStormWorld(poseStack, state);
      submitGradientCylinder(collector, poseStack, radius * 0.92, radius * 0.1, radius * 0.86, COLLAPSE_GLOW_COLOURS[0][0], COLLAPSE_GLOW_COLOURS[0][1], COLLAPSE_GLOW_COLOURS[0][2], (int)(150.0F * amount), GlowRenderTypes.glow(HALO_BAND_TEXTURE), 7);
      submitGradientCylinder(collector, poseStack, radius * 1.18, radius * 0.02, radius * 0.94, COLLAPSE_GLOW_COLOURS[1][0], COLLAPSE_GLOW_COLOURS[1][1], COLLAPSE_GLOW_COLOURS[1][2], (int)(70.0F * amount), GlowRenderTypes.glow(HALO_BAND_TEXTURE), 7);
      poseStack.popPose();
   }

   private static void submitHaloPlane(SubmitNodeCollector collector, PoseStack poseStack, double halfW, double halfH, int r, int g, int b, int a, RenderType type) {
      if (a <= 2) {
         return;
      }
      collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
         consumer.addVertex(pose, (float)-halfW, (float)-halfH, 0.0F).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
         consumer.addVertex(pose, (float)halfW, (float)-halfH, 0.0F).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
         consumer.addVertex(pose, (float)halfW, (float)halfH, 0.0F).setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
         consumer.addVertex(pose, (float)-halfW, (float)halfH, 0.0F).setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
      });
   }

   private void submitGrowth5(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      float GROWTH5_SCALE = 4.0F;
      float HUGE_BACK_SCALE = 1.72F;
      // The huge back is the part that keeps expanding while the storm is held
      // at late phase 5 (and keeps going through phase 6+ at the growth speed).
      float HUGE_BACK_GROWTH = HUGE_BACK_SCALE * Math.max(1.0F, backScale(state));
      float GROWTH5_WIDE = 1.0F;
      float GROWTH5_UP = 4.0F;
      float GROWTH5_FORWARD = -6.5F;
      float GROWTH5_YAW_TRIM = 0.0F;
      boolean filled = DevouringStormsClientConfig.filledSubphases;
      float grow;
      if (filled) {
         float p = growth5Smoothed(state.stormId, state.phase);
         grow = p * p * (3.0F - 2.0F * p);
      } else {
         grow = state.phase >= 5.0 ? 1.0F : 0.0F;
      }

      boolean scaled = filled && DevouringStormsClientConfig.scaledSubphaseGrowth;
      if (!(grow <= 0.0F) || !((filled ? hugeBackProgress(state.phase) : 0.0F) <= 0.0F) || filled || !(state.phase < 5.0)) {
         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot + 0.0F));
         applyCollapse(poseStack, state, 17.0);
         poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
         poseStack.translate(0.0, 4.0, -6.5);
         poseStack.scale(-4.0F, -4.0F, 4.0F);
         if (state.preview == null && StormShadowMap.wanted()) {
            StormShadowMap.capture(poseStack, this.growth5Model.root());
            if (DevouringStormsClientConfig.flatbackFlipFix) {
               poseStack.pushPose();
               this.applyBackMirror(poseStack, this.growth5BackModel.root());
               StormShadowMap.capture(poseStack, this.growth5BackModel.root());
               poseStack.popPose();
            }

            if (hugeBackProgress(state.phase) > 0.0F) {
               poseStack.pushPose();
               poseStack.scale(HUGE_BACK_GROWTH, HUGE_BACK_GROWTH, HUGE_BACK_GROWTH);
               StormShadowMap.capture(poseStack, this.hugeAssBackModel.root());
               poseStack.pushPose();
               this.applyBackMirror(poseStack, this.hugeAssBackMirrorModel.root());
               StormShadowMap.capture(poseStack, this.hugeAssBackMirrorModel.root());
               poseStack.popPose();
               poseStack.popPose();
            }
         }

         if (scaled) {
            this.submitScaled(
               poseStack, this.growth5Model.root(), grow, () -> this.submitCubeReveal(submitNodeCollector, poseStack, this.growth5Model.root(), state, 1.0F)
            );
         } else {
            this.submitCubeReveal(submitNodeCollector, poseStack, this.growth5Model.root(), state, grow);
         }

         boolean flip = DevouringStormsClientConfig.flatbackFlipFix && !this.previewShadowPass;
         if (flip) {
            if (scaled) {
               this.submitScaled(
                  poseStack,
                  this.growth5BackModel.root(),
                  grow,
                  () -> this.submitMirroredBack(submitNodeCollector, poseStack, this.growth5BackModel.root(), state, 1.0F)
               );
            } else {
               float mirrorGrow = Math.max(0.0F, grow - 0.1F) / 0.9F;
               this.submitMirroredBack(submitNodeCollector, poseStack, this.growth5BackModel.root(), state, mirrorGrow);
            }
         }

         float hugeGrow = filled ? hugeBackProgress(state.phase) : (state.phase >= 5.8 && state.phase < 6.0 ? 1.0F : 0.0F);
         if (hugeGrow > 0.0F) {
            poseStack.pushPose();
            poseStack.scale(HUGE_BACK_GROWTH, HUGE_BACK_GROWTH, HUGE_BACK_GROWTH);
            if (scaled) {
               this.submitScaled(
                  poseStack,
                  this.hugeAssBackModel.root(),
                  hugeGrow,
                  () -> this.submitCubeReveal(submitNodeCollector, poseStack, this.hugeAssBackModel.root(), state, 1.0F)
               );
            } else {
               this.submitCubeReveal(submitNodeCollector, poseStack, this.hugeAssBackModel.root(), state, hugeGrow);
            }

            if (flip) {
               if (scaled) {
                  this.submitScaled(
                     poseStack,
                     this.hugeAssBackMirrorModel.root(),
                     hugeGrow,
                     () -> this.submitMirroredGroups(submitNodeCollector, poseStack, this.hugeAssBackMirrorModel.root(), state, 1.0F, 3)
                  );
               } else {
                  float hugeMirror = Math.max(0.0F, hugeGrow - 0.06F) / 0.94F;
                  this.submitMirroredGroups(submitNodeCollector, poseStack, this.hugeAssBackMirrorModel.root(), state, hugeMirror, 3);
               }
            }

            poseStack.popPose();
         }

         poseStack.popPose();
      }
   }

   private void submitTentacles5(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      float UP = 6.0F;
      float BACK = 1.25F;
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
      applyCollapse(poseStack, state, 17.0);
      poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
      if (state.devourer) {
         float DEV_UP = -9.0F;
         float DEV_BACK = -2.0F;
         float DEV_SCALE = 0.8022922F;
         poseStack.translate(0.0, -9.0, -2.0);
         poseStack.scale(-0.8022922F, -0.8022922F, 0.8022922F);
         WitherStormTentaclesDevourer devTentacles = this.previewShadowPass ? this.devourerTentaclesShadowModel : this.devourerTentaclesModel;
         devTentacles.setupAnim(state);
         if (state.preview == null) {
            StormShadowMap.capture(poseStack, this.devourerTentaclesModel);
         }

         TentacleMeasure.measure(poseStack, this.devourerTentaclesModel, state.stormId);
         submitNodeCollector.submitModel(
            devTentacles, state, poseStack, this.pieceType(StormSkins.devourer()), state.lightCoords, OverlayTexture.NO_OVERLAY, this.pieceTint(state), null, 0, null
         );
      } else {
         poseStack.translate(0.0, 6.0, 1.25);
         float ts = 5.0F;
         poseStack.scale(-ts, -ts, ts);
         if (state.preview == null) {
            StormShadowMap.capture(poseStack, this.tentacles5Model);
         }

         TentacleMeasure.measure(poseStack, this.tentacles5Model, state.stormId);
         submitNodeCollector.submitModel(
            this.previewShadowPass ? this.tentacles5ShadowModel : this.tentacles5Model,
            state,
            poseStack,
            this.pieceType(this.bodyTexture(state)),
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            this.pieceTint(state),
            null,
            0,
            null
         );
      }

      poseStack.popPose();
   }

   private void submitMiniHead(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      WitherStormHeadRenderState headState = new WitherStormHeadRenderState();
      headState.lightCoords = state.lightCoords;
      headState.idleTimeTicks = state.idleTimeTicks;
      headState.spawnElapsedTicks = Float.MAX_VALUE;
      headState.fireElapsedTicks = -1.0F;
      headState.hurtElapsedTicks = -1.0F;
      headState.roarElapsedTicks = -1.0F;
      headState.jawAngle = 0.0F;
      headState.damaged = false;
      float worldYaw = state.bodyRot + state.yRot;
      poseStack.pushPose();
      if (state.bodyRoll != 0.0F) {
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
         applyCollapse(poseStack, state, 17.0);
         poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
         poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - state.bodyRot)));
      }

      poseStack.translate(0.0, 3.05F, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(-worldYaw));
      poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
      poseStack.scale(1.55F, 1.55F, 1.55F);
      poseStack.translate(0.0, -1.25, 0.0);
      Identifier miniHeadTexture = state.phase4 ? StormSkins.phase4() : StormSkins.legacy();
      submitNodeCollector.submitModel(
         this.miniHeadModel,
         headState,
         poseStack,
         FoglessRenderTypes.bodyCutout(miniHeadTexture),
         state.lightCoords,
         OverlayTexture.NO_OVERLAY,
         -1,
         null,
         0,
         null
      );
      submitNodeCollector.submitModel(
         this.miniHeadGlowModel,
         headState,
         poseStack,
         RenderTypes.eyes(miniHeadTexture),
         15728880,
         OverlayTexture.NO_OVERLAY,
         WitherStormHeadRenderer.glowTint(),
         null,
         0,
         null
      );
      poseStack.popPose();
   }

   protected void scale(WitherStormRenderState state, PoseStack poseStack) {
      if (state.phase4) {
         poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
         poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
         float topple = CollapseAnim.bodyPitch(state.collapseTicks);
         float flat = CollapseAnim.down(state.collapseTicks);
         if (flat > 0.0F) {
            poseStack.translate(0.0, 18.5 * (double)flat, 0.0);
         }

         if (topple != 0.0F) {
            poseStack.translate(0.0, -17.0, 0.0);
            poseStack.mulPose(Axis.XP.rotationDegrees(topple));
            poseStack.translate(0.0, 17.0, 0.0);
         }

         float bodyScale = (state.devourer ? 1.1009175F : 2.0F) * state.hatch * bodyScale(state);
         poseStack.scale(bodyScale, bodyScale, bodyScale);
         poseStack.translate(0.0, 6.0, 0.0);
      } else {
         if (state.bodyRoll != 0.0F) {
            poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
         }

         if (state.xRot != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
         }

         poseStack.scale(2.0F, 2.0F, 2.0F);
      }

      if (state.phase4 && state.preview == null && StormShadowMap.wanted()) {
         poseStack.pushPose();
         poseStack.translate(0.0F, -1.501F, 0.0F);
         this.model.setupAnim(state);
         StormShadowMap.capture(poseStack, this.model);
         String shell = stageShellName(state);
         if (shell != null) {
            StormStageShells.captureShadow(shell, state.phase, poseStack);
         }
         poseStack.popPose();
      }

      if (this.frameCollector != null) {
         poseStack.pushPose();
         poseStack.translate(0.0F, -1.501F, 0.0F);
         this.submitCover(poseStack, this.frameCollector, state);
         this.submitStageShell(state, poseStack, this.frameCollector);
         this.submitInnerGlow(state, poseStack, this.frameCollector);
         if (state.phase4 && !state.devourer && !this.previewShadowPass) {
            float emit = phase4EmissiveGain(state);
            if (emit > 0.02F) {
               this.frameCollector.order(3)
                  .submitModel(
                     this.stormP4Model,
                     state,
                     poseStack,
                     GlowRenderTypes.emitterMark(PHASE4_EMISSIVE_TEXTURE),
                     15728880,
                     OverlayTexture.NO_OVERLAY,
                     scaleTint(phase4EmissiveTint(state), emit),
                     null,
                     0,
                     null
                  );
            }
         }
         poseStack.popPose();
      }
   }

   public Identifier getTextureLocation(WitherStormRenderState state) {
      if (state.devourer) {
         return StormSkins.devourer();
      } else {
         return state.phase4 ? StormSkins.phase4() : StormSkins.legacy();
      }
   }
}
