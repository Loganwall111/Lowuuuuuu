package net.dabicco.witherstormmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.entity.BlackHoleEntity;
import net.dabicco.witherstormmod.entity.model.BlackHoleSphere;
import net.dabicco.witherstormmod.entity.state.BlackHoleRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity, BlackHoleRenderState> {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/black_hole.png");

   public BlackHoleRenderer(Context context) {
      super(context);
   }

   protected AABB getBoundingBoxForCulling(BlackHoleEntity entity) {
      return super.getBoundingBoxForCulling(entity).inflate(256.0);
   }

   public BlackHoleRenderState createRenderState() {
      return new BlackHoleRenderState();
   }

   public void extractRenderState(BlackHoleEntity entity, BlackHoleRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.radius = entity.getRadius();
      state.intensity = entity.getIntensity();
   }

   public void submit(BlackHoleRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      poseStack.pushPose();
      collector.submitCustomGeometry(
         poseStack, RenderTypes.entityCutout(TEXTURE), (pose, buffer) -> BlackHoleSphere.emit(pose, buffer, state.radius, state.lightCoords)
      );
      poseStack.popPose();
      super.submit(state, poseStack, collector, camera);
   }
}
