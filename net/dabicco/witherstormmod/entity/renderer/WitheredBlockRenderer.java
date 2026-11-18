package net.dabicco.witherstormmod.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dabicco.witherstormmod.entity.state.DarkenedMovingBlockRenderState;
import net.dabicco.witherstormmod.entity.withered.WitheredBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class WitheredBlockRenderer extends EntityRenderer<WitheredBlockEntity, WitheredBlockRenderer.State> {
   public WitheredBlockRenderer(Context context) {
      super(context);
   }

   public WitheredBlockRenderer.State createRenderState() {
      return new WitheredBlockRenderer.State();
   }

   public void extractRenderState(WitheredBlockEntity entity, WitheredBlockRenderer.State state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      BlockState blockState = entity.getBlockState();
      BlockPos pos = entity.blockPosition();
      DarkenedMovingBlockRenderState block = new DarkenedMovingBlockRenderState();
      block.blockState = blockState;
      block.blockPos = pos;
      block.randomSeedPos = pos;
      block.brightnessScale = 0.42F;
      if (entity.level() instanceof ClientLevel clientLevel) {
         block.biome = clientLevel.getBiome(pos);
         block.cardinalLighting = clientLevel.cardinalLighting();
         block.lightEngine = clientLevel.getLightEngine();
      }

      state.block = block;
      float age = entity.tickCount + partialTick;
      float rate = entity.isFlung() ? 17.0F : 3.5F;
      state.spin = age * rate;
      state.tilt = Mth.sin(age * 0.11F) * 22.0F;
   }

   public void submit(WitheredBlockRenderer.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      if (state.block != null && state.block.blockState.getRenderShape() == RenderShape.MODEL) {
         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
         poseStack.mulPose(Axis.XP.rotationDegrees(state.tilt));
         poseStack.translate(-0.5, -0.5, -0.5);
         collector.submitMovingBlock(poseStack, state.block, 0);
         poseStack.popPose();
      }

      super.submit(state, poseStack, collector, camera);
   }

   public static class State extends EntityRenderState {
      public DarkenedMovingBlockRenderState block;
      public float spin;
      public float tilt;
   }
}
