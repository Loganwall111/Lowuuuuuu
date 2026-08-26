package net.dabicco.witherstormmod.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dabicco.witherstormmod.entity.SuperSkullEntity;
import net.dabicco.witherstormmod.entity.model.ModEntityModelLayers;
import net.dabicco.witherstormmod.entity.model.SuperSkull;
import net.dabicco.witherstormmod.entity.state.SuperSkullRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class SuperSkullRenderer extends EntityRenderer<SuperSkullEntity, SuperSkullRenderState> {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/super_skull.png");
   private static final int FULL_BRIGHT = 15728880;
   private final SuperSkull model;

   public SuperSkullRenderer(Context context) {
      super(context);
      this.model = new SuperSkull(context.bakeLayer(ModEntityModelLayers.SUPER_SKULL));
   }

   public SuperSkullRenderState createRenderState() {
      return new SuperSkullRenderState();
   }

   public void extractRenderState(SuperSkullEntity entity, SuperSkullRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.yRot = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
      state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
      state.extinguished = entity.isExtinguished();
   }

   public void submit(SuperSkullRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      poseStack.pushPose();
      poseStack.translate(0.0, 0.5, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot + 180.0F));
      poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
      poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
      poseStack.translate(0.0, -1.0, 0.0);
      this.model.setupAnim(state);
      submitNodeCollector.submitModel(
         this.model, state, poseStack, RenderTypes.entityCutout(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null
      );
      poseStack.popPose();
      super.submit(state, poseStack, submitNodeCollector, camera);
   }
}
