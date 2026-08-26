package net.dabicco.witherstormmod.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.entity.GrabTentacleEntity;
import net.dabicco.witherstormmod.entity.state.GrabTentacleRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class GrabTentacleRenderer extends EntityRenderer<GrabTentacleEntity, GrabTentacleRenderState> {
   public GrabTentacleRenderer(Context context) {
      super(context);
   }

   public GrabTentacleRenderState createRenderState() {
      return new GrabTentacleRenderState();
   }

   public void submit(GrabTentacleRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
   }
}
