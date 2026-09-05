package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.dabicco.witherstormmod.beacon.WitheredBeacon;
import net.dabicco.witherstormmod.client.FoglessRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BeaconRenderer.class})
public abstract class BeaconCoreTintMixin {
   @Unique
   private static final float DABYWS_LO = 0.11F;
   @Unique
   private static final float DABYWS_HI = 0.89F;
   @Unique
   private static final Identifier DABYWS_SHEET = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/tractor_beam.png");
   @Unique
   private static final int DABYWS_WITHERED = -1333124392;
   @Unique
   private static final int DABYWS_AFFECTED = 1891666670;
   @Unique
   private int dabyws$tint;

   @Inject(
      method = {"extractRenderState"},
      at = {@At("TAIL")}
   )
   private void dabyws$readFlags(BlockEntity beacon, BeaconRenderState state, float partialTick, Vec3 cameraPos, CrumblingOverlay crumbling, CallbackInfo ci) {
      this.dabyws$tint = 0;
      if (beacon instanceof WitheredBeacon flags) {
         if (flags.dabyws$isWithered()) {
            this.dabyws$tint = -1333124392;
         } else if (flags.dabyws$isAffected()) {
            this.dabyws$tint = 1891666670;
         }
      }
   }

   @Inject(
      method = {"submit"},
      at = {@At("TAIL")}
   )
   private void dabyws$submitCore(BeaconRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
      if (this.dabyws$tint != 0) {
         int tint = this.dabyws$tint;
         collector.submitCustomGeometry(poseStack, FoglessRenderTypes.entityTranslucentEmissive(DABYWS_SHEET), (pose, buffer) -> dabyws$box(pose, buffer, tint));
      }
   }

   @Unique
   private static void dabyws$box(Pose pose, VertexConsumer buffer, int tint) {
      float a = 0.11F;
      float b = 0.89F;
      dabyws$quad(pose, buffer, tint, a, a, a, b, a, a, b, a, b, a, a, b);
      dabyws$quad(pose, buffer, tint, a, b, b, b, b, b, b, b, a, a, b, a);
      dabyws$quad(pose, buffer, tint, a, a, a, a, b, a, b, b, a, b, a, a);
      dabyws$quad(pose, buffer, tint, b, a, b, b, b, b, a, b, b, a, a, b);
      dabyws$quad(pose, buffer, tint, a, a, b, a, b, b, a, b, a, a, a, a);
      dabyws$quad(pose, buffer, tint, b, a, a, b, b, a, b, b, b, b, a, b);
   }

   @Unique
   private static void dabyws$quad(
      Pose pose,
      VertexConsumer buffer,
      int tint,
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
      dabyws$vertex(pose, buffer, tint, x0, y0, z0, 0.0F, 0.0F);
      dabyws$vertex(pose, buffer, tint, x1, y1, z1, 1.0F, 0.0F);
      dabyws$vertex(pose, buffer, tint, x2, y2, z2, 1.0F, 1.0F);
      dabyws$vertex(pose, buffer, tint, x3, y3, z3, 0.0F, 1.0F);
   }

   @Unique
   private static void dabyws$vertex(Pose pose, VertexConsumer buffer, int tint, float x, float y, float z, float u, float v) {
      buffer.addVertex(pose, x, y, z).setColor(tint).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
