package net.dabicco.witherstormmod.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dabicco.witherstormmod.client.ClientConfigCache;
import net.dabicco.witherstormmod.client.FoglessRenderTypes;
import net.dabicco.witherstormmod.client.PreviewScene;
import net.dabicco.witherstormmod.client.TractorBeamRenderer;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.model.ModEntityModelLayers;
import net.dabicco.witherstormmod.entity.model.WitherStormHead;
import net.dabicco.witherstormmod.entity.state.WitherStormHeadRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class PreviewHeads {
   private static final int SLOTS = 3;
   private static final int FULL_BRIGHT = 15728880;
   private final WitherStormHead[] solid = new WitherStormHead[3];
   private final WitherStormHead[] glow = new WitherStormHead[3];
   private final WitherStormHead[] shadow = new WitherStormHead[3];
   private final WitherStormHeadRenderState[] states = new WitherStormHeadRenderState[3];
   private static final float LOOK_YAW = 30.0F;
   private static final float LOOK_PITCH = 16.0F;
   private static final float LOOK_DOWN = 17.0F;
   private static final float LOOK_SPEED = 0.034F;
   private static final float BEAM_PITCH_MIN = 8.0F;
   private static final float BEAM_PITCH_MAX = 72.0F;

   public PreviewHeads(Context context) {
      for (int i = 0; i < 3; i++) {
         this.solid[i] = new WitherStormHead(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_HEAD));
         this.glow[i] = new WitherStormHead(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_HEAD_GLOW));
         this.shadow[i] = new WitherStormHead(context.bakeLayer(ModEntityModelLayers.WITHER_STORM_HEAD));
         this.states[i] = new WitherStormHeadRenderState();
      }
   }

   public static float lookYaw(float timeTicks, int head) {
      float t = timeTicks * 0.034F + head * 2.7F;
      return 30.0F * (0.62F * Mth.sin(t * 0.53F) + 0.38F * Mth.sin(t * 1.17F + 1.9F));
   }

   public static float lookPitch(float timeTicks, int head) {
      float t = timeTicks * 0.034F + head * 4.1F;
      return 17.0F + 16.0F * (0.65F * Mth.sin(t * 0.41F + 2.3F) + 0.35F * Mth.sin(t * 0.87F));
   }

   private static float jaw(float timeTicks, int head) {
      float t = timeTicks * 0.034F + head * 1.6F;
      return 2.4F + 2.4F * Mth.sin(t * 0.33F);
   }

   public void submit(
      PreviewScene scene,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      boolean shadowPass,
      Vec3[] offsets,
      float[] scales,
      float[] restYaw,
      float[] restRoll,
      float bodyRot,
      float idleTimeTicks,
      boolean devourer,
      boolean earlyPhase,
      Identifier texture
   ) {
      int count = Math.min(3, Math.min(offsets.length, scales.length));
      float emit = (float)Mth.clamp(DabyWSClientConfig.glowStrength, 0.0, 1.0);

      for (int i = 0; i < count; i++) {
         WitherStormHeadRenderState head = this.states[i];
         head.lightCoords = 15728880;
         head.idleTimeTicks = idleTimeTicks;
         head.spawnElapsedTicks = Float.MAX_VALUE;
         head.fireElapsedTicks = -1.0F;
         head.hurtElapsedTicks = -1.0F;
         head.roarElapsedTicks = -1.0F;
         head.devourer = devourer;
         head.earlyPhase = earlyPhase;
         head.headScale = scales[i];
         head.jawAngle = jaw(idleTimeTicks, i);
         Vec3 off = offsets[i];
         boolean firing = !shadowPass && (scene.beams & 1 << i) != 0;
         float yaw = restYaw[i] + lookYaw(idleTimeTicks, i);
         float pitch = lookPitch(idleTimeTicks, i);
         if (firing) {
            pitch = Mth.clamp(pitch, 8.0F, 72.0F);
         }

         float roll = restRoll[i] * (0.65F + 0.35F * Mth.sin(idleTimeTicks * 0.017F + i * 2.4F));
         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(-bodyRot));
         poseStack.translate(off.x, off.y, off.z);
         if (firing) {
            float py = pitch * (float) (Math.PI / 180.0);
            float yy = yaw * (float) (Math.PI / 180.0);
            double dirX = -Mth.cos(py) * Mth.sin(yy);
            double dirY = -Mth.sin(py);
            double dirZ = Mth.cos(py) * Mth.cos(yy);
            double reach = (scene.groundY - off.y) / dirY;
            this.solid[i].setupAnim(head);
            Vec3 apex = TractorBeamRenderer.computeEyeApex(this.solid[i].upperJaw(), yaw, pitch, roll, scales[i]);
            Vec3 end = new Vec3(dirX * reach, dirY * reach, dirZ * reach);
            float radius = ClientConfigCache.cfg.beamGroundRadius;
            TractorBeamRenderer.submitBeam(poseStack, collector, apex, end, radius, idleTimeTicks, 1.0F);
            TractorBeamRenderer.submitPreviewMotes(poseStack, collector, apex, end, radius, idleTimeTicks, 1.0F);
         }

         poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
         poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
         poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
         poseStack.scale(scales[i], scales[i], scales[i]);
         poseStack.translate(0.0, -1.25, 0.0);
         collector.submitModel(
            shadowPass ? this.shadow[i] : this.solid[i],
            head,
            poseStack,
            shadowPass ? PreviewScene.shadowType() : FoglessRenderTypes.bodyCutout(texture),
            15728880,
            OverlayTexture.NO_OVERLAY,
            shadowPass ? 940578856 : -1,
            null,
            0,
            null
         );
         if (!shadowPass && emit > 0.02F) {
            int tint = WitherStormHeadRenderer.glowTint();
            int gr = (int)((tint >> 16 & 0xFF) * emit);
            int gg = (int)((tint >> 8 & 0xFF) * emit);
            int gb = (int)((tint & 0xFF) * emit);
            collector.submitModel(
               this.glow[i],
               head,
               poseStack,
               RenderTypes.eyes(texture),
               15728880,
               OverlayTexture.NO_OVERLAY,
               0xFF000000 | gr << 16 | gg << 8 | gb,
               null,
               0,
               null
            );
         }

         poseStack.popPose();
      }
   }
}
