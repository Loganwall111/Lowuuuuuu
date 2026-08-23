package net.dabicco.devouringstorms.bowels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dabicco.devouringstorms.bowels.BowelsTentacleEntity;
import net.dabicco.devouringstorms.entity.model.Tentacle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class BowelsTentacleRenderer extends EntityRenderer<BowelsTentacleEntity, BowelsTentacleRenderer.State> {
   public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/phase_4_assets.png");
   private final EntityModelSet modelSet;

   public BowelsTentacleRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.modelSet = context.getModelSet();
   }

   public State createRenderState() {
      return new State();
   }

   public void extractRenderState(BowelsTentacleEntity entity, State state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.entityId = entity.getId();
      state.prompt = 0;
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && entity.getGrabPrompt() > 0 && entity.getGrabFor() == mc.player.getId()) {
         state.prompt = 'Q';
         state.promptLeft = (float)entity.getGrabPrompt() / 40.0F;
         state.promptSide = entity.isGrabRight() ? 1.0F : -1.0F;
         state.promptAt = mc.player.getY() - entity.getY();
      }

      state.mountYaw = entity.getMountYaw();
      state.bones = entity.getBones();
      state.scale = entity.getScale();
      state.joints = entity.joints(partialTick);
      state.mount = entity.mountOffset();
   }

   public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      Tentacle model = BowelsLimbModels.forEntity(this.modelSet, state.entityId);
      BowelsLimbPose.apply(model, state.joints, state.bones, 0, state.scale);
      poseStack.pushPose();
      poseStack.translate(state.mount.x, (double)0.0F, state.mount.z);
      poseStack.mulPose(Axis.YP.rotationDegrees(-state.mountYaw));
      collector.submitModelPart(model.base(), poseStack, RenderTypes.entityCutout(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, (TextureAtlasSprite)null);
      poseStack.popPose();
      if (state.prompt != 0) {
         ActionButtonRender.submit(poseStack, collector, camera, state.prompt, state.promptLeft, state.promptSide, state.promptAt);
      }

      super.submit(state, poseStack, collector, camera);
   }

   public static class State extends EntityRenderState {
      public char prompt;
      public float promptLeft;
      public float promptSide;
      public double promptAt;
      public float[][] joints = new float[0][];
      public int bones;
      public float mountYaw;
      public float scale = 1.0F;
      public int entityId;
      public Vec3 mount;

      public State() {
         this.mount = Vec3.ZERO;
      }
   }
}
