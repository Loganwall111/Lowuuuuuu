package net.dabicco.witherstormmod.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.entity.animation.WitherCommandBlockAnim;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

/**
 * Devouring Storms 1.9.116 -- the Phase 1-3 "command wither" skeleton, realigned.
 *
 * This shadows the base jar's WitherCommandBlock (same package, same part
 * names, same UVs, same animation logic) so the box parameters match the
 * authoritative MCSM Stage A blueprint again (gggggrff archive,
 * Stage_A/witherstormStageA.bbmodel). The shipped base transcription had
 * broken its own part chain:
 *
 *   - the neck (upperBodyPart2_r2) was rotated 37.5 deg and offset 8.4
 *     blocks forward, so its tip pointed into empty space;
 *   - the CENTRE head (head1, 8x8x8) sat 20 blocks BELOW the neck tip,
 *     dangling under the command-block platform in mid-air;
 *   - the side heads (head2/head3, 6x6x6) hung under the platform ends
 *     instead of resting on the shoulder beams;
 *   - seven of the small rib joints floated 0.5-3 blocks above the beams.
 *
 * Repairs (geometry only -- every texOffs, part name, animation and the
 * setupAnim rules are byte-identical to the base):
 *   1. neck straightened (0 deg) and centred over the trunk column;
 *   2. head1 sits on the neck top (world y 16.9-24.9, centred on the
 *      neck) -- the classic wither centre head;
 *   3. head2/head3 rest on the shoulder beam ends (2-block overlap);
 *   4. the floating rib joints are dropped onto the beam/arm tops.
 *
 * Result: platform -> trunk -> beams -> side heads, and trunk -> neck ->
 * centre head, every cube touching the cube below it.
 */
public class WitherCommandBlock extends EntityModel<WitherStormRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm"), "main");
   private final ModelPart upperBodyPart1;
   private final ModelPart upperBodyPart2;
   private final ModelPart head1;
   private final ModelPart head2;
   private final ModelPart head3;
   private final ModelPart root;
   private final KeyframeAnimation idleAnimation;

   public WitherCommandBlock(ModelPart root) {
      super(root);
      this.root = root;
      this.upperBodyPart1 = root.getChild("upperBodyPart1");
      this.upperBodyPart2 = this.upperBodyPart1.getChild("upperBodyPart2");
      this.head1 = this.upperBodyPart1.getChild("head1");
      this.head2 = this.upperBodyPart1.getChild("head2");
      this.head3 = this.upperBodyPart1.getChild("head3");
      this.idleAnimation = WitherCommandBlockAnim.idle.bake(root);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition upperBodyPart1 = partdefinition.addOrReplaceChild(
         "upperBodyPart1",
         CubeListBuilder.create().texOffs(0, 16).addBox(-10.0F, -20.1F, -0.5F, 20.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      PartDefinition upperBodyPart2 = upperBodyPart1.addOrReplaceChild(
         "upperBodyPart2",
         CubeListBuilder.create()
            .texOffs(0, 22)
            .addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(24, 22)
            .addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(24, 26)
            .addBox(-5.0F, 1.5F, -8.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(33, 34)
            .addBox(-5.0F, 3.5F, 0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(33, 34)
            .addBox(-5.0F, 1.5F, 0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(33, 34)
            .addBox(-5.0F, 3.5F, 0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(36, 33)
            .addBox(-4.0F, 1.5F, -9.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(37, 32)
            .addBox(6.0F, 3.5F, -9.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(37, 32)
            .addBox(6.0F, 1.5F, -9.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(36, 33)
            .addBox(6.0F, 3.5F, -9.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(37, 33)
            .addBox(-4.0F, 3.5F, -9.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(37, 32)
            .addBox(-4.0F, 3.5F, -9.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(33, 34)
            .addBox(7.0F, 3.5F, 0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(33, 34)
            .addBox(7.0F, 1.5F, 0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(33, 34)
            .addBox(7.0F, 3.5F, 0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(24, 12)
            .addBox(-3.0F, 1.5F, -9.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(24, 22)
            .addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(24, 26)
            .addBox(-5.0F, 4.0F, -8.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(24, 12)
            .addBox(-3.0F, 4.0F, -9.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(24, 22)
            .addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(24, 26)
            .addBox(-5.0F, 6.5F, -8.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(24, 12)
            .addBox(-3.0F, 6.5F, -9.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(24, 26)
            .addBox(6.0F, 1.5F, -8.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(24, 12)
            .addBox(4.0F, 1.5F, -9.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(24, 26)
            .addBox(6.0F, 4.0F, -8.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(24, 12)
            .addBox(4.0F, 4.0F, -9.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(24, 26)
            .addBox(6.0F, 6.5F, -8.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(24, 12)
            .addBox(4.0F, 6.5F, -9.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -17.1F, -0.5F)
      );
      PartDefinition upperBodyPart2_r1 = upperBodyPart2.addOrReplaceChild(
         "upperBodyPart2_r1",
         CubeListBuilder.create().texOffs(0, 64).addBox(-8.5F, -20.0F, -12.5F, 16.0F, 16.0F, 16.0F, new CubeDeformation(-3.6F)),
         PartPose.offsetAndRotation(2.0F, 0.35F, 8.5F, 1.5708F, 0.0F, 0.0F)
      );
      PartDefinition upperBodyPart2_r2 = upperBodyPart2.addOrReplaceChild(
         "upperBodyPart2_r2",
         CubeListBuilder.create().texOffs(12, 22).addBox(-2.0F, -13.1F, -0.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(2.0F, 20.1F, 0.5F, 0.0F, 0.0F, 0.0F)
      );
      PartDefinition head1 = upperBodyPart1.addOrReplaceChild(
         "head1",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-0.5F, 0.9F, 1.0F)
      );
      PartDefinition head2 = upperBodyPart1.addOrReplaceChild(
         "head2",
         CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-9.0F, -11.6F, 0.0F)
      );
      PartDefinition head3 = upperBodyPart1.addOrReplaceChild(
         "head3",
         CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(8.0F, -11.6F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 64, 96);
   }

   public void setupAnim(WitherStormRenderState state) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      this.idleAnimation.apply((long)(state.idleTimeTicks * 50.0F), 1.0F);
      float DEG = (float) (Math.PI / 180.0);
      this.head1.yRot = state.yRot * (float) (Math.PI / 180.0);
      this.head1.xRot = state.xRot * (float) (Math.PI / 180.0);
      this.head1.visible = state.phase < 2.0;
      boolean covered = state.phase >= 2.0;
      this.upperBodyPart1.skipDraw = covered;
      this.upperBodyPart2.visible = !covered;
      if (state.phase >= 3.0) {
         this.head2.yRot = 0.0F;
         this.head2.xRot = 0.0F;
         this.head3.yRot = 0.0F;
         this.head3.xRot = 0.0F;
      } else {
         this.head2.yRot = (state.headYRot[0] - state.bodyRot) * (float) (Math.PI / 180.0);
         this.head2.xRot = state.headXRot[0] * (float) (Math.PI / 180.0);
         this.head3.yRot = (state.headYRot[1] - state.bodyRot) * (float) (Math.PI / 180.0);
         this.head3.xRot = state.headXRot[1] * (float) (Math.PI / 180.0);
      }
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
      this.upperBodyPart1.render(poseStack, vertexConsumer, packedLight, packedOverlay);
   }
}
