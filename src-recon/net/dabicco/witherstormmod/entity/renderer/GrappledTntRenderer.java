package net.dabicco.witherstormmod.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.dabicco.witherstormmod.client.GrappleAnchor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class GrappledTntRenderer extends EntityRenderer<net.dabicco.witherstormmod.entity.GrappledTntEntity, GrappledTntRenderer.State> {
   private final ItemModelResolver itemModelResolver;

   public GrappledTntRenderer(Context context) {
      super(context);
      this.shadowRadius = 0.5F;
      this.itemModelResolver = context.getItemModelResolver();
   }

   public GrappledTntRenderer.State createRenderState() {
      return new GrappledTntRenderer.State();
   }

   public void extractRenderState(net.dabicco.witherstormmod.entity.GrappledTntEntity entity, GrappledTntRenderer.State state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.outlineColor = 0;
      state.showTnt = entity.getProbeState() == 0;
      if (state.showTnt) {
         this.itemModelResolver.updateForNonLiving(state.item, new ItemStack(Items.TNT), ItemDisplayContext.GROUND, entity);
      }

      state.hasString = false;
      Entity ex = entity.level().getEntity(entity.getOwnerId());
      if (ex instanceof Entity) {
         double exx = Mth.lerp(partialTick, entity.xOld, entity.getX());
         double ey = Mth.lerp(partialTick, entity.yOld, entity.getY());
         double ez = Mth.lerp(partialTick, entity.zOld, entity.getZ());
         Vec3 muzzle = GrappleAnchor.muzzle(ex, partialTick);
         state.hasString = true;
         state.stringX = muzzle.x - exx;
         state.stringY = muzzle.y - ey;
         state.stringZ = muzzle.z - ez;
      }
   }

   public void submit(GrappledTntRenderer.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      if (state.showTnt) {
         poseStack.pushPose();
         poseStack.translate(0.0, 0.35, 0.0);
         poseStack.scale(0.7F, 0.7F, 0.7F);
         state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
         poseStack.popPose();
      }

      if (state.hasString) {
         collector.submitCustomGeometry(
            poseStack,
            RenderTypes.leash(),
            (pose, consumer) -> drawString(pose, consumer, (float)state.stringX, (float)state.stringY, (float)state.stringZ, state.lightCoords)
         );
      }

      super.submit(state, poseStack, collector, camera);
   }

   private static void drawString(Pose pose, VertexConsumer c, float ex, float ey, float ez, int light) {
      int seg = 16;
      float w = 0.05F;

      for (int i = 0; i < seg; i++) {
         float t0 = (float)i / seg;
         float t1 = (float)(i + 1) / seg;
         float x0 = ex * t0;
         float y0 = ey * t0 + 0.4F;
         float z0 = ez * t0;
         float x1 = ex * t1;
         float y1 = ey * t1 + 0.4F;
         float z1 = ez * t1;
         vert(pose, c, x0 - w, y0, z0, light);
         vert(pose, c, x0 + w, y0, z0, light);
         vert(pose, c, x1 + w, y1, z1, light);
         vert(pose, c, x1 - w, y1, z1, light);
      }
   }

   private static void vert(Pose pose, VertexConsumer c, float x, float y, float z, int light) {
      c.addVertex(pose, x, y, z)
         .setColor(120, 80, 40, 255)
         .setUv(0.0F, 0.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(light)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   public static class State extends EntityRenderState {
      public final ItemStackRenderState item = new ItemStackRenderState();
      public boolean hasString;
      public boolean showTnt;
      public double stringX;
      public double stringY;
      public double stringZ;
   }
}
