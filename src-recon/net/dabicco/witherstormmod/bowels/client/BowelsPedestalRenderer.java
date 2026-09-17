package net.dabicco.witherstormmod.bowels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.dabicco.witherstormmod.client.ClusterMesh;
import net.dabicco.witherstormmod.client.ClusterMesh.Group;
import net.dabicco.witherstormmod.client.ClusterMesh.Piece;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class BowelsPedestalRenderer extends EntityRenderer<net.dabicco.witherstormmod.bowels.BowelsPedestalEntity, BowelsPedestalRenderer.State> {
   public BowelsPedestalRenderer(Context context) {
      super(context);
   }

   public BowelsPedestalRenderer.State createRenderState() {
      return new BowelsPedestalRenderer.State();
   }

   public void extractRenderState(net.dabicco.witherstormmod.bowels.BowelsPedestalEntity entity, BowelsPedestalRenderer.State state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.pedestal = entity.pedestalMesh();
      state.sand = entity.sandMesh();
      state.pedestalLift = entity.pedestalLift(partialTick);
      state.sandLift = entity.sandLift(partialTick);
      state.packedLight = state.lightCoords;
   }

   public void submit(BowelsPedestalRenderer.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      this.draw(state.sand, state.sandLift, state, poseStack, collector);
      this.draw(state.pedestal, state.pedestalLift, state, poseStack, collector);
      super.submit(state, poseStack, collector, camera);
   }

   private void draw(ClusterMesh mesh, float lift, BowelsPedestalRenderer.State state, PoseStack poseStack, SubmitNodeCollector collector) {
      if (mesh != null && !mesh.groups().isEmpty()) {
         poseStack.pushPose();
         poseStack.translate(-0.5, lift, -0.5);

         for (Group group : mesh.groups()) {
            QuadInstance quads = new QuadInstance();
            quads.setLightCoords(state.packedLight);
            quads.setOverlayCoords(OverlayTexture.NO_OVERLAY);
            collector.submitCustomGeometry(poseStack, group.renderType(), (pose, consumer) -> {
               for (Piece piece : group.pieces()) {
                  quads.setColor(piece.color());
                  consumer.putBakedQuad(pose, piece.quad(), quads);
               }
            });
         }

         poseStack.popPose();
      }
   }

   public static class State extends EntityRenderState {
      public ClusterMesh pedestal;
      public ClusterMesh sand;
      public float pedestalLift;
      public float sandLift;
      public int packedLight;
   }
}
