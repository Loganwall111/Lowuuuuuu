package net.dabicco.devouringstorms.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.dabicco.devouringstorms.ModItems;
import net.dabicco.devouringstorms.entity.FormidibombEntity;
import net.dabicco.devouringstorms.entity.state.FormidibombRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FormidibombRenderer extends EntityRenderer<FormidibombEntity, FormidibombRenderState> {
   private static final float BLOCK_SIZE = 4.0F;
   private static final Identifier WHITE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/tractor_beam.png");
   private static final int FULL_BRIGHT = 15728880;
   private final ItemModelResolver itemModelResolver;

   public FormidibombRenderer(Context context) {
      super(context);
      this.shadowRadius = 0.5F;
      this.itemModelResolver = context.getItemModelResolver();
   }

   public FormidibombRenderState createRenderState() {
      return new FormidibombRenderState();
   }

   public void extractRenderState(FormidibombEntity entity, FormidibombRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.outlineColor = 0;
      state.morphed = entity.isMorphed();
      state.spin = entity.getSpin(partialTick);
      state.whiteout = entity.getWhiteout(partialTick);
      state.crackGlow = entity.getCrackGlow(partialTick);
      state.shake = entity.getShake(partialTick);
      state.ticks = entity.getTicks();
      state.seed = (long)entity.getId();
      this.itemModelResolver
         .updateForNonLiving(state.item, new ItemStack(state.morphed ? ModItems.FORMIDIBOMB : Items.CRAFTING_TABLE), ItemDisplayContext.GROUND, entity);
   }

   public void submit(FormidibombRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      poseStack.pushPose();
      if (state.shake > 1.0E-4F) {
         float sx = Mth.sin((double)((float)state.ticks * 12.9898F + (float)state.seed)) * state.shake;
         float sz = Mth.sin((double)((float)state.ticks * 78.233F + (float)state.seed)) * state.shake;
         poseStack.translate((double)sx, 0.0, (double)sz);
      }

      poseStack.pushPose();
      poseStack.translate(0.0, 0.39, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
      poseStack.scale(4.0F, 4.0F, 4.0F);
      poseStack.translate(0.0, -0.12, 0.0);
      int light = state.morphed ? lerpLight(state.lightCoords, state.crackGlow) : state.lightCoords;
      state.item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
      poseStack.popPose();
      if (state.whiteout > 0.01F) {
         int alpha = (int)(Mth.clamp(state.whiteout, 0.0F, 1.0F) * 255.0F);
         poseStack.pushPose();
         poseStack.translate(0.0, 0.45, 0.0);
         poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
         poseStack.translate(0.0, -0.45, 0.0);
         collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(WHITE), (pose, consumer) -> whiteCube(pose, consumer, alpha));
         poseStack.popPose();
      }

      poseStack.popPose();
      super.submit(state, poseStack, collector, camera);
   }

   private static void whiteCube(Pose pose, VertexConsumer c, int alpha) {
      float lo = -0.52F;
      float hi = 0.52F;
      float y0 = 0.13F;
      float y1 = 1.21F;
      quad(pose, c, alpha, lo, y0, lo, lo, y1, lo, hi, y1, lo, hi, y0, lo);
      quad(pose, c, alpha, hi, y0, hi, hi, y1, hi, lo, y1, hi, lo, y0, hi);
      quad(pose, c, alpha, lo, y0, hi, lo, y1, hi, lo, y1, lo, lo, y0, lo);
      quad(pose, c, alpha, hi, y0, lo, hi, y1, lo, hi, y1, hi, hi, y0, hi);
      quad(pose, c, alpha, lo, y1, lo, lo, y1, hi, hi, y1, hi, hi, y1, lo);
      quad(pose, c, alpha, lo, y0, hi, lo, y0, lo, hi, y0, lo, hi, y0, hi);
   }

   private static void quad(
      Pose pose,
      VertexConsumer c,
      int alpha,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3
   ) {
      vert(pose, c, x0, y0, z0, alpha);
      vert(pose, c, x1, y1, z1, alpha);
      vert(pose, c, x2, y2, z2, alpha);
      vert(pose, c, x3, y3, z3, alpha);
   }

   private static void vert(Pose pose, VertexConsumer c, float x, float y, float z, int alpha) {
      c.addVertex(pose, x, y, z)
         .setColor(255, 255, 255, alpha)
         .setUv(0.5F, 0.5F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   private static int lerpLight(int light, float amount) {
      if (amount <= 0.0F) {
         return light;
      } else if (amount >= 1.0F) {
         return 15728880;
      } else {
         int block = light & 65535;
         int sky = light >> 16 & 65535;
         int b = (int)((float)block + (float)(240 - block) * amount);
         int s = (int)((float)sky + (float)(240 - sky) * amount);
         return s << 16 | b;
      }
   }
}
