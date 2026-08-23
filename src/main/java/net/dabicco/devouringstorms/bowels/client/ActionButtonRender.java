package net.dabicco.devouringstorms.bowels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class ActionButtonRender {
   private static final float SIZE = 1.15F;
   private static final float ASIDE = 2.4F;
   private static final float LIFT = 1.6F;

   private ActionButtonRender() {
   }

   public static void submit(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, char letter, float left, float side) {
      submit(poseStack, collector, camera, letter, left, side, (double)0.0F);
   }

   public static void submit(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, char letter, float left, float side, double atHeight) {
      Minecraft mc = Minecraft.getInstance();
      Font font = mc.font;
      poseStack.pushPose();
      poseStack.translate((double)(2.4F * side), (double)1.6F + atHeight, (double)0.0F);
      poseStack.mulPose(camera.orientation);
      float urgency = 1.0F + (1.0F - left) * 0.18F;
      poseStack.scale(1.15F * urgency, -1.15F * urgency, 1.15F * urgency);
      collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, buffer) -> {
         quad(pose, buffer, 0.58F, -1451777);
         quad(pose, buffer, 0.5F, -15066598);
      });
      if (letter != 0) {
         String text = String.valueOf(letter);
         poseStack.pushPose();
         poseStack.translate(0.0F, 0.0F, -0.02F);
         poseStack.scale(0.075F, 0.075F, 0.075F);
         collector.submitText(poseStack, (float)(-font.width(text)) / 2.0F, -4.0F, FormattedCharSequence.forward(text, Style.EMPTY), false, DisplayMode.SEE_THROUGH, 15728880, -1, 0, 0);
         poseStack.popPose();
      }

      poseStack.popPose();
   }

   private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float half, int argb) {
      buffer.addVertex(pose, -half, -half, 0.0F).setColor(argb);
      buffer.addVertex(pose, -half, half, 0.0F).setColor(argb);
      buffer.addVertex(pose, half, half, 0.0F).setColor(argb);
      buffer.addVertex(pose, half, -half, 0.0F).setColor(argb);
   }
}
