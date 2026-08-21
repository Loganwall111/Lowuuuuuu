package net.dabicco.witherstormmod.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.entity.animation.witherstormheadanim;
import net.dabicco.witherstormmod.entity.state.WitherStormHeadRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class WitherStormHead extends EntityModel<WitherStormHeadRenderState> {
   private final ModelPart lowerJaw;
   private final ModelPart upperJaw;
   private final KeyframeAnimation spawnAnimation;
   private final KeyframeAnimation idleAnimation;
   private final KeyframeAnimation hurtAnimation;
   private final KeyframeAnimation idleDamagedAnimation;
   private final KeyframeAnimation fireAnimation;
   private final KeyframeAnimation fireDamagedAnimation;
   private final KeyframeAnimation roarAnimation;
   private static final float CENTRE_Y = 19.9F;

   public WitherStormHead(ModelPart root) {
      super(root);
      this.lowerJaw = root.getChild("lower jaw");
      this.upperJaw = root.getChild("Upper Jaw");
      this.spawnAnimation = witherstormheadanim.Spawn.bake(root);
      this.idleAnimation = witherstormheadanim.Idle.bake(root);
      this.hurtAnimation = witherstormheadanim.Hurt.bake(root);
      this.idleDamagedAnimation = witherstormheadanim.IdleDamaged.bake(root);
      this.fireAnimation = witherstormheadanim.Fire.bake(root);
      this.fireDamagedAnimation = witherstormheadanim.FireDamaged.bake(root);
      this.roarAnimation = witherstormheadanim.Roar.bake(root);
   }

   public ModelPart lowerJaw() {
      return this.lowerJaw;
   }

   public ModelPart upperJaw() {
      return this.upperJaw;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshDefinition = new MeshDefinition();
      PartDefinition partDefinition = meshDefinition.getRoot();
      partDefinition.addOrReplaceChild(
         "lower jaw",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-11.0F, 2.25F, -12.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(384, 320)
            .addBox(-9.0F, 2.25F, -14.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.25F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.25F, -5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.25F, -8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-10.0F, 1.25F, -11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-8.0F, 1.25F, -13.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-6.0F, 1.25F, -14.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-4.0F, 1.25F, -14.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-2.0F, 1.25F, -13.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-1.0F, 1.25F, -11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.25F, -8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.25F, -5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.25F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(5.0F, 21.25F, 2.5F, (float) Math.PI, 0.0F, 0.0F)
      );
      partDefinition.addOrReplaceChild(
         "Upper Jaw",
         CubeListBuilder.create()
            .texOffs(8, 510)
            .addBox(0.0F, 1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.0F, -7.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-1.0F, 1.0F, -10.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-3.0F, 1.0F, -12.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-5.0F, 1.0F, -13.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-7.0F, 1.0F, -13.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-9.0F, 1.0F, -12.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-10.0F, 1.0F, -10.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.0F, -7.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-11.0F, -5.0F, -11.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(128, 64)
            .addBox(-9.0F, -7.0F, -9.0F, 8.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(448, 320)
            .addBox(-7.0F, -7.0F, -11.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 508)
            .addBox(-6.0F, -3.0F, -13.05F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(384, 320)
            .addBox(-9.0F, -5.0F, -13.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(5.0F, 21.25F, 2.5F, (float) Math.PI, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshDefinition, 512, 512);
   }

   public static LayerDefinition createGlowLayer() {
      MeshDefinition meshDefinition = new MeshDefinition();
      PartDefinition partDefinition = meshDefinition.getRoot();
      partDefinition.addOrReplaceChild(
         "lower jaw",
         CubeListBuilder.create()
            .texOffs(8, 510)
            .addBox(-11.0F, 1.25F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.25F, -5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.25F, -8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-10.0F, 1.25F, -11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-8.0F, 1.25F, -13.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-6.0F, 1.25F, -14.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-4.0F, 1.25F, -14.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-2.0F, 1.25F, -13.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-1.0F, 1.25F, -11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.25F, -8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.25F, -5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.25F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(5.0F, 21.25F, 2.5F, (float) Math.PI, 0.0F, 0.0F)
      );
      partDefinition.addOrReplaceChild(
         "Upper Jaw",
         CubeListBuilder.create()
            .texOffs(8, 510)
            .addBox(0.0F, 1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(0.0F, 1.0F, -7.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-1.0F, 1.0F, -10.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-3.0F, 1.0F, -12.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-5.0F, 1.0F, -13.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-7.0F, 1.0F, -13.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-9.0F, 1.0F, -12.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-10.0F, 1.0F, -10.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.0F, -7.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 510)
            .addBox(-11.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(5.0F, 21.25F, 2.5F, (float) Math.PI, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshDefinition, 512, 512);
   }

   public static LayerDefinition createEyeGlowLayer() {
      MeshDefinition meshDefinition = new MeshDefinition();
      PartDefinition partDefinition = meshDefinition.getRoot();
      partDefinition.addOrReplaceChild("lower jaw", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 21.25F, 2.5F, (float) Math.PI, 0.0F, 0.0F));
      partDefinition.addOrReplaceChild(
         "Upper Jaw",
         CubeListBuilder.create().texOffs(0, 508).addBox(-6.0F, -3.0F, -13.05F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(5.0F, 21.25F, 2.5F, (float) Math.PI, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshDefinition, 512, 512);
   }

   public void setupAnim(WitherStormHeadRenderState state) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      float spawnScaled = state.spawnElapsedTicks >= 0.0F ? state.spawnElapsedTicks * 0.6666667F : -1.0F;
      float spawnLenTicks = 31.666F;
      if (spawnScaled >= 0.0F && spawnScaled < spawnLenTicks) {
         float spawnW = Mth.clamp((spawnLenTicks - spawnScaled) / 6.0F, 0.0F, 1.0F);
         if (spawnW < 1.0F) {
            if (state.damaged) {
               this.idleDamagedAnimation.apply(toMillis(state.idleTimeTicks), 1.0F - spawnW);
            } else {
               this.idleAnimation.apply(toMillis(state.idleTimeTicks), 1.0F - spawnW);
            }
         }

         this.spawnAnimation.apply(toMillis(spawnScaled), spawnW);
      } else if (state.damaged) {
         this.idleDamagedAnimation.apply(toMillis(state.idleTimeTicks), 1.0F);
      } else {
         this.idleAnimation.apply(toMillis(state.idleTimeTicks), 1.0F);
      }

      float hurtW = blendWeight(state.hurtElapsedTicks, 2.125F);
      if (hurtW > 0.0F) {
         this.hurtAnimation.apply(toMillis(state.hurtElapsedTicks), hurtW);
      }

      float roarW = blendWeight(state.roarElapsedTicks, 2.0F);
      if (roarW > 0.0F) {
         this.roarAnimation.apply(toMillis(state.roarElapsedTicks), roarW);
      }

      float fireW = blendWeight(state.fireElapsedTicks, 1.25F);
      if (fireW > 0.0F) {
         if (state.damaged) {
            this.fireDamagedAnimation.apply(toMillis(state.fireElapsedTicks), fireW);
         } else {
            this.fireAnimation.apply(toMillis(state.fireElapsedTicks), fireW);
         }
      }

      this.lowerJaw.xRot = this.lowerJaw.xRot + (float)Math.toRadians((double)state.jawAngle);
      this.lowerJaw.yRot = this.lowerJaw.yRot + (float)Math.toRadians((double)state.jawLagYaw);
      this.lowerJaw.xRot = this.lowerJaw.xRot + (float)Math.toRadians((double)state.jawLagPitch);
      this.lowerJaw.zRot = this.lowerJaw.zRot + (float)Math.toRadians((double)state.jawLagRoll);
      if (state.upsideDown != 0.0F) {
         this.root.zRot = this.root.zRot + (float)Math.toRadians((double)state.upsideDown);
         this.root.y += 39.8F;
      }
   }

   private static float blendWeight(float elapsedTicks, float lengthSeconds) {
      float len = lengthSeconds * 20.0F;
      if (!(elapsedTicks < 0.0F) && !(elapsedTicks >= len)) {
         float in = Mth.clamp(elapsedTicks / 4.0F, 0.0F, 1.0F);
         float out = Mth.clamp((len - elapsedTicks) / 6.0F, 0.0F, 1.0F);
         return Math.min(in, out);
      } else {
         return 0.0F;
      }
   }

   private static long toMillis(float elapsedTicks) {
      return (long)(elapsedTicks * 50.0F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.root.render(poseStack, buffer, packedLight, packedOverlay);
   }
}
