package net.dabicco.witherstormmod.bowels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dabicco.witherstormmod.bowels.BowelsHeartEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BowelsHeartRenderer extends EntityRenderer<BowelsHeartEntity, State> {
   private final BowelsCrackModel cracks;

   public BowelsHeartRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.cracks = new BowelsCrackModel(context.bakeLayer(BowelsCrackModel.LAYER));
   }

   public State createRenderState() {
      return new State();
   }

   public void extractRenderState(BowelsHeartEntity entity, State state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.cracks = entity.getCracks();
      BlockPos pos = entity.blockPosition();
      MovingBlockRenderState block = new MovingBlockRenderState();
      block.blockState = Blocks.COMMAND_BLOCK.defaultBlockState();
      block.blockPos = pos;
      block.randomSeedPos = pos;
      Level var7 = entity.level();
      if (var7 instanceof ClientLevel clientLevel) {
         block.biome = clientLevel.getBiome(pos);
         block.cardinalLighting = clientLevel.cardinalLighting();
         block.lightEngine = clientLevel.getLightEngine();
      }

      state.block = block;
      float hurt = (float)entity.getHurtTime() - partialTick;
      state.flinch = hurt <= 0.0F ? 0.0F : Mth.sin((double)(hurt * 2.7F)) * hurt * 1.4F;
      state.bob = 0.0F;
   }

   public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      if (state.block == null) {
         super.submit(state, poseStack, collector, camera);
      } else {
         poseStack.pushPose();
         poseStack.translate((double)0.0F, (double)0.5F, (double)0.0F);
         poseStack.mulPose(Axis.YP.rotationDegrees(state.bob));
         poseStack.mulPose(Axis.ZP.rotationDegrees(state.flinch));
         poseStack.pushPose();
         poseStack.translate((double)-0.5F, (double)-0.5F, (double)-0.5F);
         collector.submitMovingBlock(poseStack, state.block, 0);
         poseStack.popPose();
         this.cracks.submit(state.cracks, 4, poseStack, collector, 15728880);
         poseStack.popPose();
         super.submit(state, poseStack, collector, camera);
      }
   }

   public static class State extends EntityRenderState {
      public MovingBlockRenderState block;
      public int cracks;
      public float flinch;
      public float bob;
   }
}
