package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class WitherVeinLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
   private static final Identifier VEINS = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/wither_veins.png");
   private static final int FULL_BRIGHT = 15728880;

   public WitherVeinLayer(RenderLayerParent<S, M> parent) {
      super(parent);
   }

   public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
      float var10000;
      if (state instanceof net.dabicco.witherstormmod.client.InfectionRenderState irs) {
         var10000 = irs.dabyws$getInfection();
      } else {
         var10000 = 0.0F;
      }

      if (!(var10000 <= 0.02F)) {
         float time = state.ageInTicks;
         float throb = 0.88F + 0.12F * Mth.sin(time * 0.09F);
         int alpha = (int)(Mth.clamp(var10000 * throb, 0.0F, 1.0F) * 235.0F);
         int r = (int)(72.0F - 34.0F * var10000);
         int g = (int)(14.0F - 8.0F * var10000);
         int b = (int)(104.0F - 44.0F * var10000);
         int tint = alpha << 24 | r << 16 | g << 8 | b;
         collector.order(1)
            .submitModel(
               this.getParentModel(),
               state,
               poseStack,
               RenderTypes.entityTranslucent(VEINS),
               light,
               OverlayTexture.NO_OVERLAY,
               tint,
               (TextureAtlasSprite)null,
               0,
               (CrumblingOverlay)null
            );
      }
   }
}
