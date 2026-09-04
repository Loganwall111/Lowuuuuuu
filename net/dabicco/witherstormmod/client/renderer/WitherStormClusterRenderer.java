package net.dabicco.witherstormmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.math.Axis;
import java.util.List;
import net.dabicco.witherstormmod.client.ClusterMesh;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.dabicco.witherstormmod.entity.state.DarkenedMovingBlockRenderState;
import net.dabicco.witherstormmod.entity.state.WitherStormClusterRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class WitherStormClusterRenderer extends EntityRenderer<WitherStormClusterEntity, WitherStormClusterRenderState> {
   public WitherStormClusterRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public WitherStormClusterRenderState createRenderState() {
      return new WitherStormClusterRenderState();
   }

   public void extractRenderState(WitherStormClusterEntity entity, WitherStormClusterRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.legacy = DabyWSClientConfig.clusterVolumetricLighting;
      state.mesh = state.legacy ? null : entity.getOrBakeMesh();
      if (state.legacy) {
         this.extractLegacy(entity, state);
      }

      entity.clientScale += (entity.getRenderScale() - entity.clientScale) * 0.08F;
      state.clusterScale = entity.clientScale;
      state.packedLight = scaleLight(state.lightCoords, entity.getDarknessFactor());
      state.yRot = entity.getInterpolatedYaw(partialTick);
      state.xRot = entity.getInterpolatedPitch(partialTick);
      state.roll = entity.getInterpolatedRoll(partialTick);
   }

   private void extractLegacy(WitherStormClusterEntity entity, WitherStormClusterRenderState state) {
      state.legacyBlocks.clear();
      state.legacyOffsets.clear();
      List<BlockState> blocks = entity.getBlocks();
      List<BlockPos> offsets = entity.getBlockOffsets();
      List<boolean[]> faceVis = entity.getBlockFaceVisibility();
      float brightness = entity.getDarknessFactor();
      BlockPos base = entity.blockPosition();

      for(int i = 0; i < blocks.size() && i < offsets.size(); ++i) {
         if (i < faceVis.size()) {
            boolean[] vis = (boolean[])faceVis.get(i);
            boolean any = false;

            for(boolean b : vis) {
               if (b) {
                  any = true;
                  break;
               }
            }

            if (!any) {
               continue;
            }
         }

         BlockPos worldPos = base.offset((Vec3i)offsets.get(i));
         DarkenedMovingBlockRenderState block = new DarkenedMovingBlockRenderState();
         block.brightnessScale = brightness;
         block.blockState = (BlockState)blocks.get(i);
         block.blockPos = worldPos;
         block.randomSeedPos = worldPos;
         Level var18 = entity.level();
         if (var18 instanceof ClientLevel clientLevel) {
            block.biome = clientLevel.getBiome(worldPos);
            block.cardinalLighting = clientLevel.cardinalLighting();
            block.lightEngine = clientLevel.getLightEngine();
         }

         state.legacyBlocks.add(block);
         state.legacyOffsets.add((BlockPos)offsets.get(i));
      }

   }

   private static int withEmission(int packed, int emission) {
      int sky = packed >> 20 & 15;
      int block = Math.max(packed >> 4 & 15, Math.min(15, emission));
      return sky << 20 | block << 4;
   }

   private static int scaleLight(int packed, float factor) {
      int block = Math.round((float)((packed & '\uffff') >> 4) * factor);
      int sky = Math.round((float)((packed >> 16 & '\uffff') >> 4) * factor);
      return Mth.clamp(sky, 0, 15) << 20 | Mth.clamp(block, 0, 15) << 4;
   }

   public void submit(WitherStormClusterRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      if (state.legacy) {
         this.submitLegacy(state, poseStack, submitNodeCollector);
         super.submit(state, poseStack, submitNodeCollector, camera);
      } else {
         ClusterMesh mesh = state.mesh;
         if (mesh != null && !mesh.groups().isEmpty()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.roll));
            poseStack.scale(state.clusterScale, state.clusterScale, state.clusterScale);
            poseStack.translate((double)-0.5F, (double)0.0F, (double)-0.5F);
            int light = state.packedLight;

            for(ClusterMesh.Group group : mesh.groups()) {
               QuadInstance quads = new QuadInstance();
               quads.setLightCoords(light);
               quads.setOverlayCoords(OverlayTexture.NO_OVERLAY);
               submitNodeCollector.submitCustomGeometry(poseStack, group.renderType(), (pose, consumer) -> {
                  for(ClusterMesh.Piece piece : group.pieces()) {
                     quads.setLightCoords(piece.emission() > 0 ? withEmission(light, piece.emission()) : light);
                     quads.setColor(piece.color());
                     consumer.putBakedQuad(pose, piece.quad(), quads);
                  }

               });
            }

            poseStack.popPose();
         }

         super.submit(state, poseStack, submitNodeCollector, camera);
      }
   }

   private void submitLegacy(WitherStormClusterRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
      for(int i = 0; i < state.legacyBlocks.size() && i < state.legacyOffsets.size(); ++i) {
         DarkenedMovingBlockRenderState block = (DarkenedMovingBlockRenderState)state.legacyBlocks.get(i);
         if (block.blockState.getRenderShape() == RenderShape.MODEL) {
            BlockPos offset = (BlockPos)state.legacyOffsets.get(i);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.roll));
            poseStack.scale(state.clusterScale, state.clusterScale, state.clusterScale);
            poseStack.translate((double)offset.getX() - (double)0.5F, (double)offset.getY(), (double)offset.getZ() - (double)0.5F);
            collector.submitMovingBlock(poseStack, block, 0);
            poseStack.popPose();
         }
      }

   }
}
