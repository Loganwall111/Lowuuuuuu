package net.dabicco.witherstormmod.bowels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.bowels.BowelsMawEntity;
import net.dabicco.witherstormmod.entity.WitherStormHeadEntity;
import net.dabicco.witherstormmod.entity.renderer.WitherStormHeadRenderer;
import net.dabicco.witherstormmod.entity.state.WitherStormHeadRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class BowelsMawRenderer extends WitherStormHeadRenderer {
   private char prompt;
   private float promptLeft;
   private float promptSide;

   public BowelsMawRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public void extractRenderState(WitherStormHeadEntity entity, WitherStormHeadRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      this.prompt = 0;
      if (entity instanceof BowelsMawEntity maw) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && maw.getPrompt() > 0 && maw.getPromptFor() == mc.player.getId()) {
            this.prompt = (char)(maw.isRightHand() ? 69 : 81);
            this.promptLeft = (float)maw.getPrompt() / 60.0F;
            this.promptSide = maw.isRightHand() ? 1.0F : -1.0F;
         }
      }
   }

   public void submit(WitherStormHeadRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      super.submit(state, poseStack, collector, camera);
      if (this.prompt != 0) {
         ActionButtonRender.submit(poseStack, collector, camera, this.prompt, this.promptLeft, this.promptSide);
      }
   }
}
