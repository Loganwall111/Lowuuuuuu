package net.dabicco.devouringstorms.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.devouringstorms.ModItems;
import net.dabicco.devouringstorms.entity.SuperTntEntity;
import net.dabicco.devouringstorms.entity.state.SuperTntRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SuperTntRenderer extends EntityRenderer<SuperTntEntity, SuperTntRenderState> {
   private final ItemModelResolver itemModelResolver;
   private static final float BLOCK_SIZE = 4.0F;

   public SuperTntRenderer(Context context) {
      super(context);
      this.shadowRadius = 0.5F;
      this.itemModelResolver = context.getItemModelResolver();
   }

   public SuperTntRenderState createRenderState() {
      return new SuperTntRenderState();
   }

   public void extractRenderState(SuperTntEntity entity, SuperTntRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.outlineColor = 0;
      state.fuse = entity.getFuse();
      state.flash = entity.getFlash(partialTick);
      this.itemModelResolver.updateForNonLiving(state.item, new ItemStack(ModItems.SUPER_TNT), ItemDisplayContext.GROUND, entity);
   }

   public void submit(SuperTntRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      poseStack.pushPose();
      poseStack.translate(0.0, -0.05, 0.0);
      float flash = state.flash;
      float scale = 4.0F;
      if (flash > 0.0F) {
         float wobble = 1.0F + Mth.sin((double)((float)state.fuse * 1.5F)) * 0.1F * flash;
         scale = 4.0F * wobble;
      }

      poseStack.scale(scale, scale, scale);
      boolean blinkOn = flash > 0.0F && state.fuse % 2 == 0;
      int light = blinkOn ? 15728880 : state.lightCoords;
      state.item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
      poseStack.popPose();
      super.submit(state, poseStack, collector, camera);
   }
}
