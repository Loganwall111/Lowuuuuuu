package net.dabicco.devouringstorms.bowels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.devouringstorms.bowels.SeveredTentacleEntity;
import net.dabicco.devouringstorms.entity.model.Tentacle;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SeveredTentacleRenderer extends EntityRenderer<SeveredTentacleEntity, SeveredTentacleRenderer.State> {
   private final EntityModelSet modelSet;

   protected AABB getBoundingBoxForCulling(SeveredTentacleEntity entity) {
      double reach = entity.ropeReach();
      return new AABB(entity.getX() - reach, entity.getY() - reach, entity.getZ() - reach, entity.getX() + reach, entity.getY() + reach, entity.getZ() + reach);
   }

   public SeveredTentacleRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.modelSet = context.getModelSet();
   }

   public State createRenderState() {
      return new State();
   }

   public void extractRenderState(SeveredTentacleEntity entity, State state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.entityId = entity.getId();
      state.bones = entity.getCount();
      state.from = entity.getStart();
      state.scale = entity.getScale();
      state.joints = entity.ropeJoints();
      state.anchor = entity.ropeAnchor(partialTick);
   }

   public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      Tentacle model = BowelsLimbModels.forEntity(this.modelSet, state.entityId);
      BowelsLimbPose.apply(model, state.joints, state.bones, state.from, state.scale);
      poseStack.pushPose();
      poseStack.translate(state.anchor.x, state.anchor.y, state.anchor.z);
      collector.submitModelPart(model.base(), poseStack, RenderTypes.entityCutout(BowelsTentacleRenderer.TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, (TextureAtlasSprite)null);
      poseStack.popPose();
      super.submit(state, poseStack, collector, camera);
   }

   public static class State extends EntityRenderState {
      public float[][] joints = new float[0][];
      public int bones;
      public int from;
      public float scale = 1.0F;
      public int entityId;
      public Vec3 anchor;

      public State() {
         this.anchor = Vec3.ZERO;
      }
   }
}
