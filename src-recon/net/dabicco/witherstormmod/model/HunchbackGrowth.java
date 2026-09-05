package net.dabicco.witherstormmod.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
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

public class HunchbackGrowth extends EntityModel<WitherStormRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "hunchback_growth"), "main"
   );
   public static final int MAX_GROWTH = 21;
   private final ModelPart root;
   private final ModelPart upperBodyPart1;
   private final ModelPart[] growths = new ModelPart[22];
   private final ModelPart covers;
   private final ModelPart[] coverParts;

   public HunchbackGrowth(ModelPart root) {
      super(root);
      this.root = root;
      this.upperBodyPart1 = root.getChild("upperBodyPart1");
      ModelPart hunch = this.upperBodyPart1.getChild("hunchmassivelowtaperfade");

      for (int i = 1; i <= 21; i++) {
         this.growths[i] = hunch.getChild("growth" + i);
      }

      this.covers = this.upperBodyPart1.getChild("covers");
      this.coverParts = new ModelPart[]{
         this.covers.getChild("cover1"),
         this.covers.getChild("cover6"),
         this.covers.getChild("cover7"),
         this.covers.getChild("cover8"),
         this.covers.getChild("cover9")
      };
   }

   public static int growthCountFor(double phase) {
      if (phase < 0.2) {
         return 0;
      } else if (phase < 2.0) {
         return Math.min(9, (int)Math.floor(phase / 0.2 + 1.0E-6));
      } else if (phase < 2.2) {
         return 9;
      } else if (phase < 3.0) {
         return 10 + (int)Math.floor((phase - 2.2) / 0.2 + 1.0E-6);
      } else {
         return phase < 3.1 ? 14 : Math.min(21, 14 + (int)Math.floor((phase - 3.0) / 0.1 + 1.0E-6));
      }
   }

   public static int coverCountFor(double phase) {
      return phase < 2.2 ? 0 : Math.min(5, 1 + (int)Math.floor((phase - 2.2) / 0.04 + 1.0E-6));
   }

   public void setupAnim(WitherStormRenderState state) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      int shown = growthCountFor(state.phase);

      for (int i = 1; i <= 21; i++) {
         this.growths[i].visible = i <= shown;
      }

      int covered = coverCountFor(state.phase);
      this.covers.visible = covered > 0;

      for (int i = 0; i < this.coverParts.length; i++) {
         this.coverParts[i].visible = i < covered;
      }
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition upperBodyPart1 = partdefinition.addOrReplaceChild("upperBodyPart1", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition hunchmassivelowtaperfade = upperBodyPart1.addOrReplaceChild(
         "hunchmassivelowtaperfade", CubeListBuilder.create(), PartPose.offset(4.0F, -10.0F, 9.0F)
      );
      PartDefinition covers = upperBodyPart1.addOrReplaceChild("covers", CubeListBuilder.create(), PartPose.offset(11.0F, -8.0F, -1.0F));
      addGrowths0(hunchmassivelowtaperfade);
      addGrowths1(hunchmassivelowtaperfade);
      addGrowths2(hunchmassivelowtaperfade);
      addGrowths3(hunchmassivelowtaperfade);
      addGrowths4(hunchmassivelowtaperfade);
      addGrowths5(hunchmassivelowtaperfade);
      addGrowths6(hunchmassivelowtaperfade);
      addGrowths7(hunchmassivelowtaperfade);
      addCovers0(covers);
      addCovers1(covers);
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   private static void addGrowths0(PartDefinition hunchmassivelowtaperfade) {
      PartDefinition growth = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(1.0F, -12.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -2.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -4.0F, -3.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -4.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -4.0F, -1.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -4.0F, 3.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -4.0F, 5.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -10.0F, -5.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -10.0F, -3.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -10.0F, -3.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -10.0F, -5.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -10.0F, 1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -10.0F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -8.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -10.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -10.0F, -3.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -12.0F, -1.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -12.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -12.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -12.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -12.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -14.0F, -9.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -14.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -14.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -10.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -14.0F, -7.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -14.0F, -7.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -16.0F, -5.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -16.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -18.0F, -3.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -18.0F, -5.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -16.0F, -7.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -10.0F, -11.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -16.0F, -5.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -16.0F, -3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -14.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -10.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -10.0F, -11.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -8.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -6.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -8.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -14.0F, -9.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -14.0F, -11.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -12.0F, -3.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -10.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -10.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -12.0F, -9.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -14.0F, -11.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -14.0F, -11.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -14.0F, -9.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition growth1 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth1",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(3.0F, -14.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -4.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -4.0F, 3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -6.0F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -10.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -8.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -4.0F, 5.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -2.0F, 5.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -4.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -14.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -12.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -16.0F, 1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -16.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -18.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -14.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -12.0F, 1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -12.0F, 3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -18.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -18.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -20.0F, -7.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -20.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -18.0F, -3.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -18.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -14.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -12.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -10.0F, -7.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -10.0F, -9.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -10.0F, -11.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -10.0F, -9.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -10.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -12.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -14.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -16.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -18.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -14.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -12.0F, -3.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -12.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -18.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -12.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -10.0F, 1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -10.0F, 3.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -8.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -12.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -10.0F, -3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -8.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -10.0F, -1.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -10.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -10.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -8.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -6.0F, -1.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -14.0F, 3.0F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -14.0F, 1.0F, 4.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -8.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -8.0F, 5.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -8.0F, 7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -10.0F, 7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -14.0F, 5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -18.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -20.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -12.0F, 3.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 2.0F, -2.0F)
      );
      PartDefinition growth2 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth2",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(-14.0F, -10.0F, -10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.0F, -2.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-6.0F, -4.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.0F, -4.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.0F, -4.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.0F, -6.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.0F, -4.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.0F, -4.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.0F, -12.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.0F, -14.0F, 2.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0F, -16.0F, 0.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.0F, -8.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.0F, -12.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.0F, -16.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0F, -18.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.0F, -14.0F, -4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0F, -8.0F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.0F, -16.0F, -8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.0F, -20.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.0F, -16.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.0F, -16.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.0F, -14.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.0F, -18.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-14.0F, -12.0F, 8.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.0F, -10.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -10.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -10.0F, 4.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-14.0F, -14.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -18.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.0F, -18.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.0F, -16.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -18.0F, 0.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -12.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -20.0F, -2.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -22.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.0F, -18.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.0F, -16.0F, 2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.0F, -18.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.0F, -14.0F, -2.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.0F, -14.0F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-24.0F, -14.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.0F, -12.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.0F, -10.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-24.0F, -10.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -8.0F, -8.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.0F, -6.0F, -8.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.0F, -8.0F, -8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -4.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -6.0F, -12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.0F, -10.0F, -10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-1.0F, 2.0F, -3.0F)
      );
   }

   private static void addGrowths1(PartDefinition hunchmassivelowtaperfade) {
      PartDefinition growth3 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth3",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(-7.0F, -12.0F, -10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -2.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -2.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -8.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -10.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -10.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -14.0F, 6.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -16.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -16.0F, 4.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -18.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -14.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -18.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -14.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -14.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -18.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -18.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -18.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -16.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -12.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -12.0F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -10.0F, -2.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.0F, -10.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -8.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -6.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -6.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(17.0F, -8.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -8.0F, 6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -6.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -4.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -6.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -4.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -4.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -4.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -6.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -22.0F, -8.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -22.0F, -8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -24.0F, -4.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -24.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -24.0F, -6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -24.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -20.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -24.0F, -8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -20.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -18.0F, -6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -18.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -18.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -16.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -14.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.0F, -16.0F, 0.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-29.0F, -16.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -12.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -18.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -18.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -12.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -10.0F, -12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -6.0F, -10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -6.0F, -8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -4.0F, -8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -6.0F, -2.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -4.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 2.0F, -1.0F)
      );
      PartDefinition growth4 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth4",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(-5.0F, -16.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -2.0F, -3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.0F, -4.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.0F, -6.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(17.0F, -6.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -6.0F, -5.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -8.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -6.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.0F, -2.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -2.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(17.0F, -10.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.0F, -10.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.0F, -12.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(17.0F, -14.0F, -9.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.0F, -14.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.0F, -16.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(17.0F, -16.0F, -13.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.0F, -18.0F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.0F, -14.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(17.0F, -8.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.0F, -18.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -18.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -22.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -14.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -22.0F, -15.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -20.0F, -15.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -22.0F, -9.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -20.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -24.0F, -15.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -24.0F, -11.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -24.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -24.0F, -15.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -20.0F, -13.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -24.0F, -15.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -22.0F, -11.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -24.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -24.0F, -11.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -24.0F, -11.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -22.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -24.0F, -7.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -22.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -18.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -16.0F, -13.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -18.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -18.0F, -5.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -16.0F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -12.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -10.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -4.0F, -17.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -2.0F, -15.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -2.0F, -13.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, 2.0F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -8.0F, -13.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -12.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -8.0F, -5.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -12.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -14.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -16.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-6.0F, 2.0F, 4.0F)
      );
      PartDefinition growth5 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth5",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(-1.0F, -32.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -6.0F, 0.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -6.0F, 2.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -2.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -8.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -8.0F, 2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -12.0F, 2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -6.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -4.0F, 2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -2.0F, 2.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -10.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -8.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -8.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -10.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -12.0F, -2.0F, 16.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -14.0F, -2.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -12.0F, 2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -10.0F, 4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -12.0F, 8.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -12.0F, 6.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -12.0F, 4.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -12.0F, 4.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -10.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -12.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -14.0F, 6.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -16.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -14.0F, 4.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -14.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -14.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -14.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -16.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -16.0F, 2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -16.0F, 0.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -16.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -18.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -12.0F, -2.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -10.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -12.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.0F, -10.0F, 0.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -18.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -18.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -18.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -20.0F, 2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -22.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -22.0F, 2.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -24.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -24.0F, 0.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -26.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -24.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -24.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -24.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -22.0F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -22.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -22.0F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -20.0F, -8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -20.0F, -8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -22.0F, -4.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -18.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -20.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -18.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -16.0F, 0.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -20.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -22.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -18.0F, -2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -26.0F, 0.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -22.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -18.0F, 0.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -18.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -16.0F, 2.0F, 8.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -16.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -16.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -14.0F, 6.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -16.0F, 8.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -12.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-17.0F, -16.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -16.0F, 8.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -12.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-21.0F, -14.0F, 8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -16.0F, 10.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -14.0F, 10.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -28.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -30.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -32.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -28.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -28.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -28.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -28.0F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -28.0F, -2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -28.0F, -6.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -26.0F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -24.0F, -10.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -24.0F, -12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -26.0F, -10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -30.0F, -14.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -30.0F, -6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -30.0F, -12.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -28.0F, -12.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -28.0F, -10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -26.0F, -10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -26.0F, -14.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -26.0F, -14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -24.0F, -10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -24.0F, -8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -24.0F, -12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -4.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -4.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -4.0F, -8.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -6.0F, -8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -6.0F, -12.0F, 12.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -4.0F, -12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.0F, -2.0F, -8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -8.0F, -12.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -8.0F, -12.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -10.0F, -12.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -12.0F, -14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -30.0F, -8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -32.0F, -10.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 2.0F, 3.0F)
      );
   }

   private static void addGrowths2(PartDefinition hunchmassivelowtaperfade) {
      PartDefinition growth6 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth6",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(-27.0F, -22.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -8.0F, 1.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -10.0F, 1.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(17.0F, -8.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.0F, -12.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.0F, -14.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -24.0F, -9.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -22.0F, -3.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -22.0F, 3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -28.0F, 3.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.0F, -26.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -22.0F, -5.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -24.0F, -3.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -24.0F, -11.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -22.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -26.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -22.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -30.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -30.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -32.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -30.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -30.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -30.0F, 9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.0F, -32.0F, -9.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -32.0F, -13.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -32.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.0F, -20.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -20.0F, -9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -18.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -18.0F, 3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -16.0F, 5.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.0F, -18.0F, 7.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.0F, -18.0F, 9.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -18.0F, 11.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -20.0F, 13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -18.0F, 13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-29.0F, -20.0F, 13.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.0F, -20.0F, 9.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.0F, -18.0F, 9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-29.0F, -20.0F, 1.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.0F, -20.0F, 9.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -20.0F, 3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.0F, -22.0F, 5.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -22.0F, 7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-29.0F, -22.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.0F, -18.0F, 3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition growth7 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth7",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(-29.0F, -38.0F, 13.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.0F, -34.0F, 13.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.0F, -36.0F, 15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -8.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -8.0F, -13.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -4.0F, -11.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -8.0F, -17.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -10.0F, -17.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -20.0F, -15.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.0F, -16.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.0F, -22.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -24.0F, -15.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -22.0F, -13.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.0F, -22.0F, -7.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -22.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.0F, -26.0F, -5.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0F, -30.0F, -7.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -28.0F, -13.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-3.0F, -30.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -28.0F, -13.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -28.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -30.0F, -15.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -30.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -30.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -28.0F, -17.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -24.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -32.0F, -5.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -30.0F, -3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -26.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -32.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.0F, -30.0F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0F, -24.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.0F, -32.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -28.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.0F, -34.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -28.0F, -15.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -30.0F, -17.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -28.0F, -17.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.0F, -30.0F, -13.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0F, -30.0F, -13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -32.0F, -19.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -34.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -34.0F, -9.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.0F, -32.0F, -7.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-29.0F, -24.0F, -11.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-29.0F, -24.0F, -15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-29.0F, -36.0F, -7.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.0F, -38.0F, -5.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-29.0F, -38.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.0F, -18.0F, -11.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.0F, -14.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.0F, -18.0F, -3.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.0F, -18.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.0F, -18.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.0F, -18.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.0F, -12.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(6.0F, 2.0F, 6.0F)
      );
      PartDefinition growth8 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth8",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -14.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -10.0F, 34.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -22.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -20.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -18.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 32.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -14.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -14.0F, 32.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -30.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -16.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -12.0F, 30.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -12.0F, 30.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -10.0F, 30.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -46.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -40.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -40.0F, 28.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -38.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -38.0F, 28.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -30.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -20.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -16.0F, 28.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -12.0F, 28.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -10.0F, 28.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -8.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -6.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -8.0F, 28.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -6.0F, 28.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -44.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -44.0F, 26.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -40.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -40.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -40.0F, 26.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -12.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -12.0F, 26.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -10.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -8.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -6.0F, 26.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -4.0F, 26.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -42.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -40.0F, 24.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -40.0F, 24.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -38.0F, 24.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -32.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -32.0F, 24.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -30.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -16.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -14.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -8.0F, 24.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -10.0F, 24.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -8.0F, 24.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -42.0F, 22.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -40.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -40.0F, 22.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -38.0F, 22.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -16.0F, 22.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -12.0F, 22.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -14.0F, 22.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -10.0F, 22.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -4.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -8.0F, 22.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 20.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -4.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -40.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -26.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -36.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -26.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -26.0F, 16.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -24.0F, 14.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -20.0F, 12.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -12.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -24.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -14.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -12.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -10.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -12.0F, 10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -44.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -42.0F, 8.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -8.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -46.0F, 6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -42.0F, 6.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -38.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -38.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -34.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -24.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -22.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -12.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -12.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -12.0F, 6.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -12.0F, 6.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -8.0F, 6.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -44.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -42.0F, 4.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -40.0F, 4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -38.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -26.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -24.0F, 4.0F, 4.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -18.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -10.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -42.0F, 2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -42.0F, 2.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -34.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -28.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -24.0F, 2.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -8.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -4.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-29.0F, 2.0F, -17.0F, 1.5708F, -1.5708F, 0.0F)
      );
   }

   private static void addGrowths3(PartDefinition param0) {
      param0.addOrReplaceChild(
         "growth9",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -28.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -20.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -16.0F, 36.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -12.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -28.0F, 34.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -26.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -24.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -18.0F, 34.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -14.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -12.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -24.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -24.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -24.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -26.0F, 32.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -18.0F, 32.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -14.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -10.0F, 32.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -24.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -22.0F, 30.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -18.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -18.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -16.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -14.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -12.0F, 30.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -14.0F, 30.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 28.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -22.0F, 28.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -20.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -16.0F, 28.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -14.0F, 28.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -8.0F, 28.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -26.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -26.0F, 26.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -20.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -18.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -4.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -24.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -22.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -20.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -20.0F, 24.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -14.0F, 24.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -8.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -6.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -4.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 22.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -24.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -22.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 22.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -20.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -24.0F, 22.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -14.0F, 22.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -10.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -12.0F, 22.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -6.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -4.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -18.0F, 20.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -18.0F, 20.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -12.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -10.0F, 20.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -6.0F, 20.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -6.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -4.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -10.0F, 20.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -26.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -24.0F, 18.0F, 4.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -24.0F, 18.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -14.0F, 18.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -8.0F, 18.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -10.0F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -10.0F, 18.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -6.0F, 18.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -6.0F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -4.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -46.0F, 16.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -44.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -42.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -44.0F, 16.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -38.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -38.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -36.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -38.0F, 16.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -34.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -26.0F, 16.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -18.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -16.0F, 16.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 16.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -10.0F, 16.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -8.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -6.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -6.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -4.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -14.0F, 16.0F, 4.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -46.0F, 14.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -44.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -46.0F, 14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -42.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -40.0F, 14.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -38.0F, 14.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -38.0F, 14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -38.0F, 14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -34.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -38.0F, 14.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -28.0F, 14.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -20.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -16.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -16.0F, 14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -12.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -46.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -48.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -46.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -42.0F, 12.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -44.0F, 12.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -40.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -42.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -38.0F, 12.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -32.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -30.0F, 12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -28.0F, 12.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -24.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -20.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -18.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -16.0F, 12.0F, 2.0F, 2.0F, 22.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -16.0F, 12.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -12.0F, 12.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -10.0F, 12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -48.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -42.0F, 10.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -40.0F, 10.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -36.0F, 10.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -38.0F, 10.0F, 2.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -28.0F, 10.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -26.0F, 10.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -22.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -22.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 10.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -20.0F, 10.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 10.0F, 2.0F, 6.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -20.0F, 10.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -18.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -18.0F, 10.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -16.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -16.0F, 10.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -14.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -14.0F, 10.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -12.0F, 10.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -46.0F, 8.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -32.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -28.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -26.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -24.0F, 8.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -20.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -18.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -18.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -16.0F, 8.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -16.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -14.0F, 8.0F, 6.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -36.0F, 6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -32.0F, 6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -26.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -26.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -24.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -26.0F, 6.0F, 2.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -20.0F, 6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -16.0F, 6.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -42.0F, 4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -40.0F, 4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -32.0F, 4.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -32.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -30.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -28.0F, 4.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -26.0F, 4.0F, 8.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -32.0F, 4.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -22.0F, 4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 4.0F, 12.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -16.0F, 4.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -36.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -32.0F, 2.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -28.0F, 2.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-33.0F, 8.0F, -15.0F, 0.0F, -1.5708F, 1.5708F)
      );
      param0.addOrReplaceChild(
         "growth10",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -18.0F, 54.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -16.0F, 54.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 54.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -12.0F, 54.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -18.0F, 52.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -16.0F, 52.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -16.0F, 52.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -14.0F, 52.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -14.0F, 52.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -12.0F, 52.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -12.0F, 52.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -14.0F, 52.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -18.0F, 50.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -14.0F, 50.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -12.0F, 50.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -14.0F, 50.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -10.0F, 50.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -10.0F, 50.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -26.0F, 48.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -24.0F, 48.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -20.0F, 48.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -20.0F, 48.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -14.0F, 48.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -12.0F, 48.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -10.0F, 48.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -28.0F, 46.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -28.0F, 46.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -26.0F, 46.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -24.0F, 46.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -24.0F, 46.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -18.0F, 46.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -14.0F, 46.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -38.0F, 44.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -36.0F, 44.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -34.0F, 44.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -34.0F, 44.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -32.0F, 44.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -34.0F, 44.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -30.0F, 44.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -30.0F, 44.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -30.0F, 44.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -26.0F, 44.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -20.0F, 44.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -22.0F, 44.0F, 2.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -12.0F, 44.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -8.0F, 44.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -48.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -38.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -38.0F, 42.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -38.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -38.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -36.0F, 42.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -36.0F, 42.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -34.0F, 42.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -32.0F, 42.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -32.0F, 42.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -32.0F, 42.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -30.0F, 42.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -30.0F, 42.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -28.0F, 42.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -28.0F, 42.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 42.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -24.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -24.0F, 42.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -22.0F, 42.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -22.0F, 42.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -12.0F, 42.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -6.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -6.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -48.0F, 40.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -40.0F, 40.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -36.0F, 40.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -36.0F, 40.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -36.0F, 40.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 40.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -34.0F, 40.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -30.0F, 40.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -30.0F, 40.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -36.0F, 40.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 40.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -28.0F, 40.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -24.0F, 40.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -22.0F, 40.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -18.0F, 40.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -10.0F, 40.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -8.0F, 40.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -6.0F, 40.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -40.0F, 38.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -38.0F, 38.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -32.0F, 38.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -30.0F, 38.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -26.0F, 38.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -26.0F, 38.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -26.0F, 38.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -22.0F, 38.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -12.0F, 38.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -10.0F, 38.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -8.0F, 38.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -6.0F, 38.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -38.0F, 36.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -36.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -36.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -36.0F, 36.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -34.0F, 36.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -32.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -32.0F, 36.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -30.0F, 36.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -30.0F, 36.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -28.0F, 36.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -28.0F, 36.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -26.0F, 36.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -22.0F, 36.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -20.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -10.0F, 36.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -6.0F, 36.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -8.0F, 36.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -42.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -42.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -40.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -38.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -36.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -34.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -38.0F, 34.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -34.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -34.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -32.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -30.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -30.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -30.0F, 34.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -28.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -26.0F, 34.0F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -20.0F, 34.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -10.0F, 34.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -8.0F, 34.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -6.0F, 34.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -40.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -40.0F, 32.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -38.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -36.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -32.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -32.0F, 32.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -34.0F, 32.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -30.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -32.0F, 32.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -28.0F, 32.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -30.0F, 32.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -28.0F, 32.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -22.0F, 32.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -24.0F, 32.0F, 2.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -20.0F, 32.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -16.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -12.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -10.0F, 32.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -10.0F, 32.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -6.0F, 32.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -46.0F, 30.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -38.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -36.0F, 30.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -36.0F, 30.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -32.0F, 30.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -32.0F, 30.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -32.0F, 30.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -26.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -26.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -20.0F, 30.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -16.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -14.0F, 30.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -10.0F, 30.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -10.0F, 30.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -6.0F, 30.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -48.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -42.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -40.0F, 28.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -36.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -38.0F, 28.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -34.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -34.0F, 28.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 28.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -38.0F, 28.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -32.0F, 28.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -30.0F, 28.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -30.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -32.0F, 28.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -30.0F, 28.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -26.0F, 28.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -24.0F, 28.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -18.0F, 28.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -14.0F, 28.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -14.0F, 28.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -6.0F, 28.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -52.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -50.0F, 26.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -46.0F, 26.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -46.0F, 26.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -42.0F, 26.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -42.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -38.0F, 26.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -40.0F, 26.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -36.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -38.0F, 26.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -34.0F, 26.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -40.0F, 26.0F, 4.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -34.0F, 26.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -32.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -30.0F, 26.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -30.0F, 26.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -32.0F, 26.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 26.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -28.0F, 26.0F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -24.0F, 26.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -26.0F, 26.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -20.0F, 26.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -14.0F, 26.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -8.0F, 26.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -36.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -36.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -36.0F, 24.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -14.0F, 24.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -10.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -8.0F, 24.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -30.0F, 22.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -26.0F, 22.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -14.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -12.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -10.0F, 22.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -10.0F, 22.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -8.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -8.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -22.0F, 20.0F, 2.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -20.0F, 20.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -12.0F, 20.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -12.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -10.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -10.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -8.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -8.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -6.0F, 20.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -48.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -46.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -46.0F, 18.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -42.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -40.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -42.0F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 18.0F, 2.0F, 2.0F, 20.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -12.0F, 18.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -12.0F, 18.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -12.0F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -10.0F, 18.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -8.0F, 18.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -10.0F, 18.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -6.0F, 18.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -46.0F, 16.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -44.0F, 16.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -42.0F, 16.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -34.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -24.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -12.0F, 16.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -12.0F, 16.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -10.0F, 16.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -8.0F, 16.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -26.0F, 14.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -24.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -22.0F, 14.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -20.0F, 14.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -18.0F, 14.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -24.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -20.0F, 12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -36.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -24.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -22.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -28.0F, 10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -26.0F, 10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 10.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -36.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -36.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -24.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -26.0F, 4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -28.0F, 4.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -32.0F, 0.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -32.0F, 2.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -32.0F, 2.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 2.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-33.0F, 16.0F, -17.0F, 0.0F, -1.5708F, 1.5708F)
      );
      param0.addOrReplaceChild(
         "growth11",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -26.0F, 60.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 60.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -22.0F, 60.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -20.0F, 60.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -18.0F, 60.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -14.0F, 60.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -12.0F, 60.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -26.0F, 56.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -24.0F, 56.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -20.0F, 56.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -44.0F, 50.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -42.0F, 50.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -48.0F, 48.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -44.0F, 48.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -44.0F, 48.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -42.0F, 48.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -46.0F, 48.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -42.0F, 48.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -50.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -52.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -52.0F, 30.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -50.0F, 28.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -8.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -6.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -4.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -52.0F, 26.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -8.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -8.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -6.0F, 26.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -6.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -4.0F, 26.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -8.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -6.0F, 24.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -38.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -26.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -38.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -38.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -28.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -34.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -34.0F, 4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -34.0F, 2.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -30.0F, 2.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-35.0F, 22.0F, -17.0F, 0.0F, -1.5708F, 1.5708F)
      );
   }

   private static void addGrowths4(PartDefinition param0) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.OutOfMemoryError: Java heap space
      //   at java.base/java.util.Arrays.copyOf(Arrays.java:3619)
      //   at java.base/java.util.BitSet.ensureCapacity(BitSet.java:342)
      //   at java.base/java.util.BitSet.or(BitSet.java:948)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.addBytecodeOffsets(Exprent.java:182)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent.<init>(ConstExprent.java:155)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent.<init>(ConstExprent.java:146)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent.copy(ConstExprent.java:194)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.copyEntries(ExprProcessor.java:159)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:186)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.<init>(InvocationExprent.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.copy(InvocationExprent.java:684)
      //
      // Bytecode:
      // 0000: aload 0
      // 0001: ldc_w "growth12"
      // 0004: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0007: bipush 44
      // 0009: bipush 29
      // 000b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 000e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0011: ldc 10.0
      // 0013: ldc -20.0
      // 0015: ldc_w 34.0
      // 0018: fconst_2
      // 0019: fconst_2
      // 001a: fconst_2
      // 001b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 001e: dup
      // 001f: fconst_0
      // 0020: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0023: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0026: bipush 0
      // 0027: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 002a: bipush 44
      // 002c: bipush 29
      // 002e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0031: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0034: ldc 12.0
      // 0036: ldc -4.0
      // 0038: ldc_w 34.0
      // 003b: ldc 4.0
      // 003d: fconst_2
      // 003e: fconst_2
      // 003f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0042: dup
      // 0043: fconst_0
      // 0044: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0047: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 004a: bipush 0
      // 004b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 004e: bipush 44
      // 0050: bipush 29
      // 0052: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0055: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0058: ldc 12.0
      // 005a: ldc -12.0
      // 005c: ldc_w 32.0
      // 005f: fconst_2
      // 0060: fconst_2
      // 0061: fconst_2
      // 0062: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0065: dup
      // 0066: fconst_0
      // 0067: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 006a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 006d: bipush 0
      // 006e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0071: bipush 44
      // 0073: bipush 29
      // 0075: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0078: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 007b: ldc 8.0
      // 007d: ldc -12.0
      // 007f: ldc_w 32.0
      // 0082: fconst_2
      // 0083: fconst_2
      // 0084: fconst_2
      // 0085: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0088: dup
      // 0089: fconst_0
      // 008a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 008d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0090: bipush 0
      // 0091: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0094: bipush 44
      // 0096: bipush 29
      // 0098: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 009b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 009e: ldc 18.0
      // 00a0: ldc -12.0
      // 00a2: ldc_w 32.0
      // 00a5: fconst_2
      // 00a6: ldc 4.0
      // 00a8: fconst_2
      // 00a9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 00ac: dup
      // 00ad: fconst_0
      // 00ae: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 00b1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00b4: bipush 0
      // 00b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00b8: bipush 44
      // 00ba: bipush 29
      // 00bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00c2: ldc_w 14.0
      // 00c5: ldc -12.0
      // 00c7: ldc_w 32.0
      // 00ca: fconst_2
      // 00cb: ldc 4.0
      // 00cd: fconst_2
      // 00ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 00d1: dup
      // 00d2: fconst_0
      // 00d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 00d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00d9: bipush 0
      // 00da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00dd: bipush 44
      // 00df: bipush 29
      // 00e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00e7: ldc 10.0
      // 00e9: ldc -12.0
      // 00eb: ldc_w 32.0
      // 00ee: fconst_2
      // 00ef: ldc 4.0
      // 00f1: fconst_2
      // 00f2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 00f5: dup
      // 00f6: fconst_0
      // 00f7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 00fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00fd: bipush 0
      // 00fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0101: bipush 44
      // 0103: bipush 29
      // 0105: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0108: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 010b: ldc 6.0
      // 010d: ldc -12.0
      // 010f: ldc_w 32.0
      // 0112: fconst_2
      // 0113: ldc 4.0
      // 0115: fconst_2
      // 0116: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0119: dup
      // 011a: fconst_0
      // 011b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 011e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0121: bipush 0
      // 0122: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0125: bipush 44
      // 0127: bipush 29
      // 0129: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 012c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 012f: ldc 10.0
      // 0131: ldc -8.0
      // 0133: ldc_w 32.0
      // 0136: ldc 6.0
      // 0138: ldc 4.0
      // 013a: fconst_2
      // 013b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 013e: dup
      // 013f: fconst_0
      // 0140: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0143: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0146: bipush 0
      // 0147: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 014a: bipush 44
      // 014c: bipush 29
      // 014e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0151: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0154: ldc 16.0
      // 0156: ldc -12.0
      // 0158: ldc_w 32.0
      // 015b: fconst_2
      // 015c: ldc 10.0
      // 015e: fconst_2
      // 015f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0162: dup
      // 0163: fconst_0
      // 0164: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0167: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 016a: bipush 0
      // 016b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 016e: bipush 44
      // 0170: bipush 29
      // 0172: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0175: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0178: ldc 10.0
      // 017a: ldc -4.0
      // 017c: ldc_w 32.0
      // 017f: fconst_2
      // 0180: fconst_2
      // 0181: ldc 4.0
      // 0183: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0186: dup
      // 0187: fconst_0
      // 0188: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 018b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 018e: bipush 0
      // 018f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0192: bipush 44
      // 0194: bipush 29
      // 0196: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0199: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 019c: ldc 8.0
      // 019e: ldc -28.0
      // 01a0: ldc_w 30.0
      // 01a3: fconst_2
      // 01a4: fconst_2
      // 01a5: fconst_2
      // 01a6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01a9: dup
      // 01aa: fconst_0
      // 01ab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 01ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01b1: bipush 0
      // 01b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01b5: bipush 44
      // 01b7: bipush 29
      // 01b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01bf: ldc 4.0
      // 01c1: ldc -26.0
      // 01c3: ldc_w 30.0
      // 01c6: fconst_2
      // 01c7: fconst_2
      // 01c8: ldc 4.0
      // 01ca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01cd: dup
      // 01ce: fconst_0
      // 01cf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 01d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01d5: bipush 0
      // 01d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01d9: bipush 44
      // 01db: bipush 29
      // 01dd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01e3: ldc 6.0
      // 01e5: ldc -26.0
      // 01e7: ldc_w 30.0
      // 01ea: fconst_2
      // 01eb: ldc 4.0
      // 01ed: fconst_2
      // 01ee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01f1: dup
      // 01f2: fconst_0
      // 01f3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 01f6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01f9: bipush 0
      // 01fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01fd: bipush 44
      // 01ff: bipush 29
      // 0201: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0204: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0207: ldc 6.0
      // 0209: ldc -22.0
      // 020b: ldc_w 30.0
      // 020e: ldc 4.0
      // 0210: fconst_2
      // 0211: fconst_2
      // 0212: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0215: dup
      // 0216: fconst_0
      // 0217: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 021a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 021d: bipush 0
      // 021e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0221: bipush 44
      // 0223: bipush 29
      // 0225: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0228: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 022b: fconst_2
      // 022c: ldc -20.0
      // 022e: ldc_w 30.0
      // 0231: fconst_2
      // 0232: fconst_2
      // 0233: ldc 4.0
      // 0235: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0238: dup
      // 0239: fconst_0
      // 023a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 023d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0240: bipush 0
      // 0241: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0244: bipush 44
      // 0246: bipush 29
      // 0248: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 024b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 024e: ldc 8.0
      // 0250: ldc -18.0
      // 0252: ldc_w 30.0
      // 0255: fconst_2
      // 0256: fconst_2
      // 0257: ldc 6.0
      // 0259: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 025c: dup
      // 025d: fconst_0
      // 025e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0261: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0264: bipush 0
      // 0265: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0268: bipush 44
      // 026a: bipush 29
      // 026c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 026f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0272: ldc 4.0
      // 0274: ldc -20.0
      // 0276: ldc_w 30.0
      // 0279: ldc 4.0
      // 027b: ldc 4.0
      // 027d: ldc 4.0
      // 027f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0282: dup
      // 0283: fconst_0
      // 0284: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0287: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 028a: bipush 0
      // 028b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 028e: bipush 44
      // 0290: bipush 29
      // 0292: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0295: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0298: ldc 4.0
      // 029a: ldc -12.0
      // 029c: ldc_w 30.0
      // 029f: ldc 4.0
      // 02a1: fconst_2
      // 02a2: fconst_2
      // 02a3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02a6: dup
      // 02a7: fconst_0
      // 02a8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02ae: bipush 0
      // 02af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02b2: bipush 44
      // 02b4: bipush 29
      // 02b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02bc: ldc 12.0
      // 02be: ldc -10.0
      // 02c0: ldc_w 30.0
      // 02c3: fconst_2
      // 02c4: fconst_2
      // 02c5: fconst_2
      // 02c6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02c9: dup
      // 02ca: fconst_0
      // 02cb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02d1: bipush 0
      // 02d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02d5: bipush 44
      // 02d7: bipush 29
      // 02d9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02df: ldc 4.0
      // 02e1: ldc -10.0
      // 02e3: ldc_w 30.0
      // 02e6: fconst_2
      // 02e7: fconst_2
      // 02e8: ldc 4.0
      // 02ea: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02ed: dup
      // 02ee: fconst_0
      // 02ef: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02f2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02f5: bipush 0
      // 02f6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02f9: bipush 44
      // 02fb: bipush 29
      // 02fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0300: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0303: ldc_w 20.0
      // 0306: ldc -10.0
      // 0308: ldc_w 30.0
      // 030b: fconst_2
      // 030c: ldc 4.0
      // 030e: ldc 4.0
      // 0310: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0313: dup
      // 0314: fconst_0
      // 0315: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0318: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 031b: bipush 0
      // 031c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 031f: bipush 44
      // 0321: bipush 29
      // 0323: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0326: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0329: ldc 6.0
      // 032b: ldc -8.0
      // 032d: ldc_w 30.0
      // 0330: fconst_2
      // 0331: fconst_2
      // 0332: ldc 4.0
      // 0334: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0337: dup
      // 0338: fconst_0
      // 0339: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 033c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 033f: bipush 0
      // 0340: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0343: bipush 44
      // 0345: bipush 29
      // 0347: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 034a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 034d: ldc 18.0
      // 034f: ldc -8.0
      // 0351: ldc_w 30.0
      // 0354: fconst_2
      // 0355: ldc 4.0
      // 0357: ldc 4.0
      // 0359: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 035c: dup
      // 035d: fconst_0
      // 035e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0361: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0364: bipush 0
      // 0365: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0368: bipush 44
      // 036a: bipush 29
      // 036c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 036f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0372: ldc 8.0
      // 0374: ldc -10.0
      // 0376: ldc_w 30.0
      // 0379: fconst_2
      // 037a: ldc 6.0
      // 037c: ldc 4.0
      // 037e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0381: dup
      // 0382: fconst_0
      // 0383: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0386: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0389: bipush 0
      // 038a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 038d: bipush 44
      // 038f: bipush 29
      // 0391: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0394: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0397: ldc 10.0
      // 0399: ldc -8.0
      // 039b: ldc_w 30.0
      // 039e: ldc 8.0
      // 03a0: ldc 6.0
      // 03a2: fconst_2
      // 03a3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03a6: dup
      // 03a7: fconst_0
      // 03a8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03ae: bipush 0
      // 03af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03b2: bipush 44
      // 03b4: bipush 29
      // 03b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03bc: ldc_w 14.0
      // 03bf: ldc -28.0
      // 03c1: ldc_w 28.0
      // 03c4: fconst_2
      // 03c5: fconst_2
      // 03c6: fconst_2
      // 03c7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03ca: dup
      // 03cb: fconst_0
      // 03cc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03d2: bipush 0
      // 03d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03d6: bipush 44
      // 03d8: bipush 29
      // 03da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03dd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03e0: ldc 10.0
      // 03e2: ldc -26.0
      // 03e4: ldc_w 28.0
      // 03e7: ldc 4.0
      // 03e9: fconst_2
      // 03ea: fconst_2
      // 03eb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03ee: dup
      // 03ef: fconst_0
      // 03f0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03f6: bipush 0
      // 03f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03fa: bipush 44
      // 03fc: bipush 29
      // 03fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0401: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0404: ldc 8.0
      // 0406: ldc -24.0
      // 0408: ldc_w 28.0
      // 040b: ldc 4.0
      // 040d: fconst_2
      // 040e: fconst_2
      // 040f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0412: dup
      // 0413: fconst_0
      // 0414: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0417: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 041a: bipush 0
      // 041b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 041e: bipush 44
      // 0420: bipush 29
      // 0422: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0425: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0428: fconst_2
      // 0429: ldc -26.0
      // 042b: ldc_w 28.0
      // 042e: fconst_2
      // 042f: ldc 4.0
      // 0431: fconst_2
      // 0432: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0435: dup
      // 0436: fconst_0
      // 0437: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 043a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 043d: bipush 0
      // 043e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0441: bipush 44
      // 0443: bipush 29
      // 0445: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0448: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 044b: fconst_2
      // 044c: ldc -22.0
      // 044e: ldc_w 28.0
      // 0451: ldc 10.0
      // 0453: fconst_2
      // 0454: fconst_2
      // 0455: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0458: dup
      // 0459: fconst_0
      // 045a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 045d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0460: bipush 0
      // 0461: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0464: bipush 44
      // 0466: bipush 29
      // 0468: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 046b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 046e: ldc 10.0
      // 0470: ldc -18.0
      // 0472: ldc_w 28.0
      // 0475: ldc 6.0
      // 0477: fconst_2
      // 0478: ldc 8.0
      // 047a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 047d: dup
      // 047e: fconst_0
      // 047f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0482: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0485: bipush 0
      // 0486: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0489: bipush 44
      // 048b: bipush 29
      // 048d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0490: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0493: ldc 6.0
      // 0495: ldc -14.0
      // 0497: ldc_w 28.0
      // 049a: ldc 6.0
      // 049c: fconst_2
      // 049d: ldc 4.0
      // 049f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04a2: dup
      // 04a3: fconst_0
      // 04a4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04aa: bipush 0
      // 04ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04ae: bipush 44
      // 04b0: bipush 29
      // 04b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04b8: fconst_2
      // 04b9: ldc -14.0
      // 04bb: ldc_w 28.0
      // 04be: fconst_2
      // 04bf: fconst_2
      // 04c0: ldc 4.0
      // 04c2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04c5: dup
      // 04c6: fconst_0
      // 04c7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04ca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04cd: bipush 0
      // 04ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04d1: bipush 44
      // 04d3: bipush 29
      // 04d5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04db: fconst_2
      // 04dc: ldc -12.0
      // 04de: ldc_w 28.0
      // 04e1: ldc 4.0
      // 04e3: fconst_2
      // 04e4: fconst_2
      // 04e5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04e8: dup
      // 04e9: fconst_0
      // 04ea: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04f0: bipush 0
      // 04f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04f4: bipush 44
      // 04f6: bipush 29
      // 04f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04fe: ldc 4.0
      // 0500: ldc -10.0
      // 0502: ldc_w 28.0
      // 0505: ldc 4.0
      // 0507: fconst_2
      // 0508: fconst_2
      // 0509: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 050c: dup
      // 050d: fconst_0
      // 050e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0511: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0514: bipush 0
      // 0515: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0518: bipush 44
      // 051a: bipush 29
      // 051c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 051f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0522: ldc 4.0
      // 0524: ldc -32.0
      // 0526: ldc_w 26.0
      // 0529: fconst_2
      // 052a: fconst_2
      // 052b: fconst_2
      // 052c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 052f: dup
      // 0530: fconst_0
      // 0531: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0534: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0537: bipush 0
      // 0538: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 053b: bipush 44
      // 053d: bipush 29
      // 053f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0542: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0545: ldc 4.0
      // 0547: ldc -30.0
      // 0549: ldc_w 26.0
      // 054c: ldc 4.0
      // 054e: fconst_2
      // 054f: fconst_2
      // 0550: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0553: dup
      // 0554: fconst_0
      // 0555: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0558: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 055b: bipush 0
      // 055c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 055f: bipush 44
      // 0561: bipush 29
      // 0563: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0566: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0569: ldc_w 20.0
      // 056c: ldc -28.0
      // 056e: ldc_w 26.0
      // 0571: fconst_2
      // 0572: fconst_2
      // 0573: fconst_2
      // 0574: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0577: dup
      // 0578: fconst_0
      // 0579: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 057c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 057f: bipush 0
      // 0580: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0583: bipush 44
      // 0585: bipush 29
      // 0587: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 058a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 058d: ldc 10.0
      // 058f: ldc -28.0
      // 0591: ldc_w 26.0
      // 0594: ldc 4.0
      // 0596: fconst_2
      // 0597: ldc 4.0
      // 0599: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 059c: dup
      // 059d: fconst_0
      // 059e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05a1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05a4: bipush 0
      // 05a5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05a8: bipush 44
      // 05aa: bipush 29
      // 05ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05b2: ldc 18.0
      // 05b4: ldc -28.0
      // 05b6: ldc_w 26.0
      // 05b9: fconst_2
      // 05ba: ldc 4.0
      // 05bc: fconst_2
      // 05bd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 05c0: dup
      // 05c1: fconst_0
      // 05c2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05c8: bipush 0
      // 05c9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05cc: bipush 44
      // 05ce: bipush 29
      // 05d0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05d6: ldc 4.0
      // 05d8: ldc -24.0
      // 05da: ldc_w 26.0
      // 05dd: fconst_2
      // 05de: fconst_2
      // 05df: ldc 4.0
      // 05e1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 05e4: dup
      // 05e5: fconst_0
      // 05e6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05ec: bipush 0
      // 05ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05f0: bipush 44
      // 05f2: bipush 29
      // 05f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05fa: ldc_w 14.0
      // 05fd: ldc -22.0
      // 05ff: ldc_w 26.0
      // 0602: fconst_2
      // 0603: fconst_2
      // 0604: fconst_2
      // 0605: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0608: dup
      // 0609: fconst_0
      // 060a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 060d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0610: bipush 0
      // 0611: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0614: bipush 44
      // 0616: bipush 29
      // 0618: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 061b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 061e: ldc 8.0
      // 0620: ldc -20.0
      // 0622: ldc_w 26.0
      // 0625: ldc 4.0
      // 0627: ldc 4.0
      // 0629: fconst_2
      // 062a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 062d: dup
      // 062e: fconst_0
      // 062f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0632: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0635: bipush 0
      // 0636: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0639: bipush 44
      // 063b: bipush 29
      // 063d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0640: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0643: ldc 4.0
      // 0645: ldc -18.0
      // 0647: ldc_w 26.0
      // 064a: fconst_2
      // 064b: fconst_2
      // 064c: fconst_2
      // 064d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0650: dup
      // 0651: fconst_0
      // 0652: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0655: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0658: bipush 0
      // 0659: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 065c: bipush 44
      // 065e: bipush 29
      // 0660: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0663: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0666: ldc 6.0
      // 0668: ldc -20.0
      // 066a: ldc_w 26.0
      // 066d: fconst_2
      // 066e: ldc 6.0
      // 0670: fconst_2
      // 0671: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0674: dup
      // 0675: fconst_0
      // 0676: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0679: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 067c: bipush 0
      // 067d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0680: bipush 44
      // 0682: bipush 29
      // 0684: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0687: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 068a: fconst_2
      // 068b: ldc -22.0
      // 068d: ldc_w 26.0
      // 0690: fconst_2
      // 0691: ldc 8.0
      // 0693: fconst_2
      // 0694: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0697: dup
      // 0698: fconst_0
      // 0699: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 069c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 069f: bipush 0
      // 06a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06a3: bipush 44
      // 06a5: bipush 29
      // 06a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06ad: ldc 12.0
      // 06af: ldc -14.0
      // 06b1: ldc_w 26.0
      // 06b4: ldc 6.0
      // 06b6: fconst_2
      // 06b7: ldc 6.0
      // 06b9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 06bc: dup
      // 06bd: fconst_0
      // 06be: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 06c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06c4: bipush 0
      // 06c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06c8: bipush 44
      // 06ca: bipush 29
      // 06cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06d2: ldc 4.0
      // 06d4: ldc -14.0
      // 06d6: ldc_w 26.0
      // 06d9: fconst_2
      // 06da: fconst_2
      // 06db: ldc 6.0
      // 06dd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 06e0: dup
      // 06e1: fconst_0
      // 06e2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 06e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06e8: bipush 0
      // 06e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06ec: bipush 44
      // 06ee: bipush 29
      // 06f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06f6: ldc_w 20.0
      // 06f9: ldc -12.0
      // 06fb: ldc_w 26.0
      // 06fe: fconst_2
      // 06ff: fconst_2
      // 0700: ldc 8.0
      // 0702: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0705: dup
      // 0706: fconst_0
      // 0707: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 070a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 070d: bipush 0
      // 070e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0711: bipush 44
      // 0713: bipush 29
      // 0715: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0718: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 071b: ldc 6.0
      // 071d: ldc -14.0
      // 071f: ldc_w 26.0
      // 0722: ldc 6.0
      // 0724: ldc 4.0
      // 0726: fconst_2
      // 0727: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 072a: dup
      // 072b: fconst_0
      // 072c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 072f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0732: bipush 0
      // 0733: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0736: bipush 44
      // 0738: bipush 29
      // 073a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 073d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0740: ldc 8.0
      // 0742: ldc -10.0
      // 0744: ldc_w 26.0
      // 0747: ldc_w 14.0
      // 074a: fconst_2
      // 074b: ldc 4.0
      // 074d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0750: dup
      // 0751: fconst_0
      // 0752: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0755: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0758: bipush 0
      // 0759: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 075c: bipush 44
      // 075e: bipush 29
      // 0760: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0763: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0766: fconst_2
      // 0767: ldc -10.0
      // 0769: ldc_w 26.0
      // 076c: fconst_2
      // 076d: fconst_2
      // 076e: ldc 4.0
      // 0770: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0773: dup
      // 0774: fconst_0
      // 0775: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0778: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 077b: bipush 0
      // 077c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 077f: bipush 44
      // 0781: bipush 29
      // 0783: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0786: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0789: ldc_w 14.0
      // 078c: ldc -30.0
      // 078e: ldc 24.0
      // 0790: ldc 4.0
      // 0792: fconst_2
      // 0793: fconst_2
      // 0794: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0797: dup
      // 0798: fconst_0
      // 0799: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 079c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 079f: bipush 0
      // 07a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07a3: bipush 44
      // 07a5: bipush 29
      // 07a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07ad: ldc_w 22.0
      // 07b0: ldc -28.0
      // 07b2: ldc 24.0
      // 07b4: fconst_2
      // 07b5: fconst_2
      // 07b6: fconst_2
      // 07b7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 07ba: dup
      // 07bb: fconst_0
      // 07bc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 07bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07c2: bipush 0
      // 07c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07c6: bipush 44
      // 07c8: bipush 29
      // 07ca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07d0: ldc 8.0
      // 07d2: ldc -30.0
      // 07d4: ldc 24.0
      // 07d6: fconst_2
      // 07d7: ldc 4.0
      // 07d9: fconst_2
      // 07da: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 07dd: dup
      // 07de: fconst_0
      // 07df: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 07e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07e5: bipush 0
      // 07e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07e9: bipush 44
      // 07eb: bipush 29
      // 07ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07f3: ldc 18.0
      // 07f5: ldc -28.0
      // 07f7: ldc 24.0
      // 07f9: ldc 4.0
      // 07fb: ldc 4.0
      // 07fd: fconst_2
      // 07fe: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0801: dup
      // 0802: fconst_0
      // 0803: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0806: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0809: bipush 0
      // 080a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 080d: bipush 44
      // 080f: bipush 29
      // 0811: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0814: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0817: ldc 12.0
      // 0819: ldc -32.0
      // 081b: ldc 24.0
      // 081d: fconst_2
      // 081e: ldc 8.0
      // 0820: fconst_2
      // 0821: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0824: dup
      // 0825: fconst_0
      // 0826: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0829: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 082c: bipush 0
      // 082d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0830: bipush 44
      // 0832: bipush 29
      // 0834: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0837: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 083a: ldc 4.0
      // 083c: ldc -26.0
      // 083e: ldc 24.0
      // 0840: ldc 6.0
      // 0842: fconst_2
      // 0843: ldc 6.0
      // 0845: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0848: dup
      // 0849: fconst_0
      // 084a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 084d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0850: bipush 0
      // 0851: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0854: bipush 44
      // 0856: bipush 29
      // 0858: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 085b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 085e: ldc_w 20.0
      // 0861: ldc -24.0
      // 0863: ldc 24.0
      // 0865: ldc 4.0
      // 0867: fconst_2
      // 0868: fconst_2
      // 0869: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 086c: dup
      // 086d: fconst_0
      // 086e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0871: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0874: bipush 0
      // 0875: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0878: bipush 44
      // 087a: bipush 29
      // 087c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 087f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0882: ldc_w 14.0
      // 0885: ldc -28.0
      // 0887: ldc 24.0
      // 0889: ldc 4.0
      // 088b: ldc 6.0
      // 088d: ldc 4.0
      // 088f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0892: dup
      // 0893: fconst_0
      // 0894: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0897: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 089a: bipush 0
      // 089b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 089e: bipush 44
      // 08a0: bipush 29
      // 08a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08a5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08a8: ldc 10.0
      // 08aa: ldc -30.0
      // 08ac: ldc 24.0
      // 08ae: fconst_2
      // 08af: ldc 8.0
      // 08b1: fconst_2
      // 08b2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 08b5: dup
      // 08b6: fconst_0
      // 08b7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 08ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08bd: bipush 0
      // 08be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08c1: bipush 44
      // 08c3: bipush 29
      // 08c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08cb: ldc 12.0
      // 08cd: ldc -24.0
      // 08cf: ldc 24.0
      // 08d1: fconst_2
      // 08d2: ldc 4.0
      // 08d4: ldc 4.0
      // 08d6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 08d9: dup
      // 08da: fconst_0
      // 08db: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 08de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08e1: bipush 0
      // 08e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08e5: bipush 44
      // 08e7: bipush 29
      // 08e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08ef: fconst_2
      // 08f0: ldc -24.0
      // 08f2: ldc 24.0
      // 08f4: ldc 8.0
      // 08f6: ldc 4.0
      // 08f8: fconst_2
      // 08f9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 08fc: dup
      // 08fd: fconst_0
      // 08fe: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0901: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0904: bipush 0
      // 0905: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0908: bipush 44
      // 090a: bipush 29
      // 090c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 090f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0912: ldc 10.0
      // 0914: ldc -20.0
      // 0916: ldc 24.0
      // 0918: ldc 4.0
      // 091a: fconst_2
      // 091b: fconst_2
      // 091c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 091f: dup
      // 0920: fconst_0
      // 0921: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0924: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0927: bipush 0
      // 0928: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 092b: bipush 44
      // 092d: bipush 29
      // 092f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0932: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0935: ldc 4.0
      // 0937: ldc -20.0
      // 0939: ldc 24.0
      // 093b: fconst_2
      // 093c: fconst_2
      // 093d: ldc 4.0
      // 093f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0942: dup
      // 0943: fconst_0
      // 0944: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0947: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 094a: bipush 0
      // 094b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 094e: bipush 44
      // 0950: bipush 29
      // 0952: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0955: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0958: ldc 4.0
      // 095a: ldc -16.0
      // 095c: ldc 24.0
      // 095e: fconst_2
      // 095f: fconst_2
      // 0960: ldc 4.0
      // 0962: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0965: dup
      // 0966: fconst_0
      // 0967: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 096a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 096d: bipush 0
      // 096e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0971: bipush 44
      // 0973: bipush 29
      // 0975: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0978: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 097b: ldc 18.0
      // 097d: ldc -14.0
      // 097f: ldc 24.0
      // 0981: fconst_2
      // 0982: fconst_2
      // 0983: ldc 8.0
      // 0985: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0988: dup
      // 0989: fconst_0
      // 098a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 098d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0990: bipush 0
      // 0991: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0994: bipush 44
      // 0996: bipush 29
      // 0998: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 099b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 099e: ldc 12.0
      // 09a0: ldc -12.0
      // 09a2: ldc 24.0
      // 09a4: ldc 4.0
      // 09a6: fconst_2
      // 09a7: fconst_2
      // 09a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 09ab: dup
      // 09ac: fconst_0
      // 09ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 09b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09b3: bipush 0
      // 09b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09b7: bipush 44
      // 09b9: bipush 29
      // 09bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09c1: ldc 10.0
      // 09c3: ldc -22.0
      // 09c5: ldc_w 22.0
      // 09c8: fconst_2
      // 09c9: fconst_2
      // 09ca: ldc 4.0
      // 09cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 09cf: dup
      // 09d0: fconst_0
      // 09d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 09d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09d7: bipush 0
      // 09d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09db: bipush 44
      // 09dd: bipush 29
      // 09df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09e5: ldc 12.0
      // 09e7: ldc -22.0
      // 09e9: ldc_w 22.0
      // 09ec: ldc 4.0
      // 09ee: ldc 4.0
      // 09f0: fconst_2
      // 09f1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 09f4: dup
      // 09f5: fconst_0
      // 09f6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 09f9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09fc: bipush 0
      // 09fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a00: bipush 44
      // 0a02: bipush 29
      // 0a04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a0a: ldc 6.0
      // 0a0c: ldc -20.0
      // 0a0e: ldc_w 22.0
      // 0a11: ldc 4.0
      // 0a13: fconst_2
      // 0a14: fconst_2
      // 0a15: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a18: dup
      // 0a19: fconst_0
      // 0a1a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a1d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a20: bipush 0
      // 0a21: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a24: bipush 44
      // 0a26: bipush 29
      // 0a28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a2b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a2e: ldc 8.0
      // 0a30: ldc -18.0
      // 0a32: ldc_w 22.0
      // 0a35: ldc 4.0
      // 0a37: fconst_2
      // 0a38: fconst_2
      // 0a39: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a3c: dup
      // 0a3d: fconst_0
      // 0a3e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a41: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a44: bipush 0
      // 0a45: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a48: bipush 44
      // 0a4a: bipush 29
      // 0a4c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a52: fconst_2
      // 0a53: ldc -18.0
      // 0a55: ldc_w 22.0
      // 0a58: ldc 6.0
      // 0a5a: ldc 4.0
      // 0a5c: fconst_2
      // 0a5d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a60: dup
      // 0a61: fconst_0
      // 0a62: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a65: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a68: bipush 0
      // 0a69: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a6c: bipush 44
      // 0a6e: bipush 29
      // 0a70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a76: ldc 18.0
      // 0a78: ldc -14.0
      // 0a7a: ldc_w 22.0
      // 0a7d: ldc 4.0
      // 0a7f: fconst_2
      // 0a80: fconst_2
      // 0a81: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a84: dup
      // 0a85: fconst_0
      // 0a86: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a8c: bipush 0
      // 0a8d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a90: bipush 44
      // 0a92: bipush 29
      // 0a94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a9a: fconst_2
      // 0a9b: ldc -14.0
      // 0a9d: ldc_w 22.0
      // 0aa0: fconst_2
      // 0aa1: fconst_2
      // 0aa2: ldc 4.0
      // 0aa4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0aa7: dup
      // 0aa8: fconst_0
      // 0aa9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0aac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0aaf: bipush 0
      // 0ab0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ab3: bipush 44
      // 0ab5: bipush 29
      // 0ab7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0aba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0abd: ldc 16.0
      // 0abf: ldc -12.0
      // 0ac1: ldc_w 22.0
      // 0ac4: ldc 6.0
      // 0ac6: fconst_2
      // 0ac7: ldc 4.0
      // 0ac9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0acc: dup
      // 0acd: fconst_0
      // 0ace: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ad1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ad4: bipush 0
      // 0ad5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ad8: bipush 44
      // 0ada: bipush 29
      // 0adc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0adf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ae2: ldc_w 14.0
      // 0ae5: ldc -10.0
      // 0ae7: ldc_w 22.0
      // 0aea: ldc 6.0
      // 0aec: fconst_2
      // 0aed: fconst_2
      // 0aee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0af1: dup
      // 0af2: fconst_0
      // 0af3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0af6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0af9: bipush 0
      // 0afa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0afd: bipush 44
      // 0aff: bipush 29
      // 0b01: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b07: ldc 10.0
      // 0b09: ldc -8.0
      // 0b0b: ldc_w 22.0
      // 0b0e: ldc 8.0
      // 0b10: fconst_2
      // 0b11: fconst_2
      // 0b12: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b15: dup
      // 0b16: fconst_0
      // 0b17: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b1a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b1d: bipush 0
      // 0b1e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b21: bipush 44
      // 0b23: bipush 29
      // 0b25: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b2b: ldc 4.0
      // 0b2d: ldc -26.0
      // 0b2f: ldc_w 20.0
      // 0b32: ldc_w 14.0
      // 0b35: fconst_2
      // 0b36: fconst_2
      // 0b37: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b3a: dup
      // 0b3b: fconst_0
      // 0b3c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b42: bipush 0
      // 0b43: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b46: bipush 44
      // 0b48: bipush 29
      // 0b4a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b4d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b50: ldc 18.0
      // 0b52: ldc -26.0
      // 0b54: ldc_w 20.0
      // 0b57: ldc 6.0
      // 0b59: ldc 4.0
      // 0b5b: ldc 4.0
      // 0b5d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b60: dup
      // 0b61: fconst_0
      // 0b62: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b65: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b68: bipush 0
      // 0b69: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b6c: bipush 44
      // 0b6e: bipush 29
      // 0b70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b76: ldc 16.0
      // 0b78: ldc -22.0
      // 0b7a: ldc_w 20.0
      // 0b7d: fconst_2
      // 0b7e: fconst_2
      // 0b7f: ldc 4.0
      // 0b81: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b84: dup
      // 0b85: fconst_0
      // 0b86: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b8c: bipush 0
      // 0b8d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b90: bipush 44
      // 0b92: bipush 29
      // 0b94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b9a: ldc 10.0
      // 0b9c: ldc -22.0
      // 0b9e: ldc_w 20.0
      // 0ba1: ldc 6.0
      // 0ba3: ldc 4.0
      // 0ba5: fconst_2
      // 0ba6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ba9: dup
      // 0baa: fconst_0
      // 0bab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bb1: bipush 0
      // 0bb2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bb5: bipush 44
      // 0bb7: bipush 29
      // 0bb9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bbc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bbf: fconst_2
      // 0bc0: ldc -24.0
      // 0bc2: ldc_w 20.0
      // 0bc5: fconst_2
      // 0bc6: ldc 6.0
      // 0bc8: fconst_2
      // 0bc9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0bcc: dup
      // 0bcd: fconst_0
      // 0bce: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bd1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bd4: bipush 0
      // 0bd5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bd8: bipush 44
      // 0bda: bipush 29
      // 0bdc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bdf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0be2: ldc 12.0
      // 0be4: ldc -16.0
      // 0be6: ldc_w 20.0
      // 0be9: fconst_2
      // 0bea: ldc 4.0
      // 0bec: fconst_2
      // 0bed: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0bf0: dup
      // 0bf1: fconst_0
      // 0bf2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bf5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bf8: bipush 0
      // 0bf9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bfc: bipush 44
      // 0bfe: bipush 29
      // 0c00: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c06: ldc_w 14.0
      // 0c09: ldc -14.0
      // 0c0b: ldc_w 20.0
      // 0c0e: fconst_2
      // 0c0f: ldc 4.0
      // 0c11: fconst_2
      // 0c12: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c15: dup
      // 0c16: fconst_0
      // 0c17: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c1a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c1d: bipush 0
      // 0c1e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c21: bipush 44
      // 0c23: bipush 29
      // 0c25: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c2b: ldc 12.0
      // 0c2d: ldc -10.0
      // 0c2f: ldc_w 20.0
      // 0c32: fconst_2
      // 0c33: fconst_2
      // 0c34: ldc 4.0
      // 0c36: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c39: dup
      // 0c3a: fconst_0
      // 0c3b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c3e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c41: bipush 0
      // 0c42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c45: bipush 44
      // 0c47: bipush 29
      // 0c49: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c4c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c4f: ldc 6.0
      // 0c51: ldc -8.0
      // 0c53: ldc_w 20.0
      // 0c56: ldc 4.0
      // 0c58: fconst_2
      // 0c59: fconst_2
      // 0c5a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c5d: dup
      // 0c5e: fconst_0
      // 0c5f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c62: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c65: bipush 0
      // 0c66: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c69: bipush 44
      // 0c6b: bipush 29
      // 0c6d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c73: ldc 16.0
      // 0c75: ldc -30.0
      // 0c77: ldc 18.0
      // 0c79: fconst_2
      // 0c7a: fconst_2
      // 0c7b: ldc 4.0
      // 0c7d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c80: dup
      // 0c81: fconst_0
      // 0c82: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c85: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c88: bipush 0
      // 0c89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c8c: bipush 44
      // 0c8e: bipush 29
      // 0c90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c93: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c96: ldc 18.0
      // 0c98: ldc -28.0
      // 0c9a: ldc 18.0
      // 0c9c: ldc 4.0
      // 0c9e: fconst_2
      // 0c9f: ldc 4.0
      // 0ca1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ca4: dup
      // 0ca5: fconst_0
      // 0ca6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ca9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cac: bipush 0
      // 0cad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cb0: bipush 44
      // 0cb2: bipush 29
      // 0cb4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cb7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cba: ldc 6.0
      // 0cbc: ldc -22.0
      // 0cbe: ldc 18.0
      // 0cc0: fconst_2
      // 0cc1: fconst_2
      // 0cc2: fconst_2
      // 0cc3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0cc6: dup
      // 0cc7: fconst_0
      // 0cc8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ccb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cce: bipush 0
      // 0ccf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cd2: bipush 44
      // 0cd4: bipush 29
      // 0cd6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cd9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cdc: ldc_w 22.0
      // 0cdf: ldc -22.0
      // 0ce1: ldc 18.0
      // 0ce3: fconst_2
      // 0ce4: ldc 4.0
      // 0ce6: fconst_2
      // 0ce7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0cea: dup
      // 0ceb: fconst_0
      // 0cec: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0cef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cf2: bipush 0
      // 0cf3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cf6: bipush 44
      // 0cf8: bipush 29
      // 0cfa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cfd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d00: ldc 18.0
      // 0d02: ldc -20.0
      // 0d04: ldc 18.0
      // 0d06: fconst_2
      // 0d07: fconst_2
      // 0d08: fconst_2
      // 0d09: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d0c: dup
      // 0d0d: fconst_0
      // 0d0e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d11: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d14: bipush 0
      // 0d15: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d18: bipush 44
      // 0d1a: bipush 29
      // 0d1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d22: ldc_w 20.0
      // 0d25: ldc -20.0
      // 0d27: ldc 18.0
      // 0d29: fconst_2
      // 0d2a: ldc 4.0
      // 0d2c: fconst_2
      // 0d2d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d30: dup
      // 0d31: fconst_0
      // 0d32: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d35: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d38: bipush 0
      // 0d39: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d3c: bipush 44
      // 0d3e: bipush 29
      // 0d40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d43: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d46: ldc 4.0
      // 0d48: ldc -24.0
      // 0d4a: ldc 18.0
      // 0d4c: fconst_2
      // 0d4d: ldc 8.0
      // 0d4f: ldc 4.0
      // 0d51: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d54: dup
      // 0d55: fconst_0
      // 0d56: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d59: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d5c: bipush 0
      // 0d5d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d60: bipush 44
      // 0d62: bipush 29
      // 0d64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d6a: ldc 16.0
      // 0d6c: ldc -18.0
      // 0d6e: ldc 18.0
      // 0d70: fconst_2
      // 0d71: ldc 4.0
      // 0d73: fconst_2
      // 0d74: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d77: dup
      // 0d78: fconst_0
      // 0d79: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d7c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d7f: bipush 0
      // 0d80: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d83: bipush 44
      // 0d85: bipush 29
      // 0d87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d8a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d8d: ldc 6.0
      // 0d8f: ldc -16.0
      // 0d91: ldc 18.0
      // 0d93: fconst_2
      // 0d94: fconst_2
      // 0d95: fconst_2
      // 0d96: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d99: dup
      // 0d9a: fconst_0
      // 0d9b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d9e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0da1: bipush 0
      // 0da2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0da5: bipush 44
      // 0da7: bipush 29
      // 0da9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0daf: ldc 8.0
      // 0db1: ldc -14.0
      // 0db3: ldc 18.0
      // 0db5: fconst_2
      // 0db6: fconst_2
      // 0db7: ldc 4.0
      // 0db9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0dbc: dup
      // 0dbd: fconst_0
      // 0dbe: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0dc1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dc4: bipush 0
      // 0dc5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dc8: bipush 44
      // 0dca: bipush 29
      // 0dcc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dcf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dd2: ldc 18.0
      // 0dd4: ldc -12.0
      // 0dd6: ldc 18.0
      // 0dd8: fconst_2
      // 0dd9: fconst_2
      // 0dda: fconst_2
      // 0ddb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0dde: dup
      // 0ddf: fconst_0
      // 0de0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0de3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0de6: bipush 0
      // 0de7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dea: bipush 44
      // 0dec: bipush 29
      // 0dee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0df1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0df4: ldc 6.0
      // 0df6: ldc -12.0
      // 0df8: ldc 18.0
      // 0dfa: ldc 6.0
      // 0dfc: fconst_2
      // 0dfd: ldc 4.0
      // 0dff: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e02: dup
      // 0e03: fconst_0
      // 0e04: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e0a: bipush 0
      // 0e0b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e0e: bipush 44
      // 0e10: bipush 29
      // 0e12: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e15: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e18: ldc_w 14.0
      // 0e1b: ldc -10.0
      // 0e1d: ldc 18.0
      // 0e1f: fconst_2
      // 0e20: fconst_2
      // 0e21: fconst_2
      // 0e22: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e25: dup
      // 0e26: fconst_0
      // 0e27: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e2a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e2d: bipush 0
      // 0e2e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e31: bipush 44
      // 0e33: bipush 29
      // 0e35: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e3b: fconst_2
      // 0e3c: ldc -10.0
      // 0e3e: ldc 18.0
      // 0e40: ldc 8.0
      // 0e42: fconst_2
      // 0e43: ldc 4.0
      // 0e45: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e48: dup
      // 0e49: fconst_0
      // 0e4a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e4d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e50: bipush 0
      // 0e51: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e54: bipush 44
      // 0e56: bipush 29
      // 0e58: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e5b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e5e: ldc_w 14.0
      // 0e61: ldc -30.0
      // 0e63: ldc 16.0
      // 0e65: fconst_2
      // 0e66: fconst_2
      // 0e67: fconst_2
      // 0e68: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e6b: dup
      // 0e6c: fconst_0
      // 0e6d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e73: bipush 0
      // 0e74: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e77: bipush 44
      // 0e79: bipush 29
      // 0e7b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e7e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e81: ldc_w 20.0
      // 0e84: ldc -28.0
      // 0e86: ldc 16.0
      // 0e88: fconst_2
      // 0e89: ldc 4.0
      // 0e8b: fconst_2
      // 0e8c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e8f: dup
      // 0e90: fconst_0
      // 0e91: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e97: bipush 0
      // 0e98: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e9b: bipush 44
      // 0e9d: bipush 29
      // 0e9f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ea2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ea5: fconst_2
      // 0ea6: ldc -26.0
      // 0ea8: ldc 16.0
      // 0eaa: ldc 6.0
      // 0eac: fconst_2
      // 0ead: fconst_2
      // 0eae: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0eb1: dup
      // 0eb2: fconst_0
      // 0eb3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0eb6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0eb9: bipush 0
      // 0eba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ebd: bipush 44
      // 0ebf: bipush 29
      // 0ec1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ec4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ec7: ldc 6.0
      // 0ec9: ldc -20.0
      // 0ecb: ldc 16.0
      // 0ecd: fconst_2
      // 0ece: ldc 4.0
      // 0ed0: ldc 4.0
      // 0ed2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ed5: dup
      // 0ed6: fconst_0
      // 0ed7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0eda: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0edd: bipush 0
      // 0ede: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ee1: bipush 44
      // 0ee3: bipush 29
      // 0ee5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ee8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0eeb: ldc 18.0
      // 0eed: ldc -14.0
      // 0eef: ldc 16.0
      // 0ef1: fconst_2
      // 0ef2: fconst_2
      // 0ef3: ldc 4.0
      // 0ef5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ef8: dup
      // 0ef9: fconst_0
      // 0efa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0efd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f00: bipush 0
      // 0f01: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f04: bipush 44
      // 0f06: bipush 29
      // 0f08: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f0b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f0e: ldc 10.0
      // 0f10: ldc -14.0
      // 0f12: ldc 16.0
      // 0f14: fconst_2
      // 0f15: fconst_2
      // 0f16: ldc 6.0
      // 0f18: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f1b: dup
      // 0f1c: fconst_0
      // 0f1d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f23: bipush 0
      // 0f24: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f27: bipush 44
      // 0f29: bipush 29
      // 0f2b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f2e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f31: ldc_w 20.0
      // 0f34: ldc -12.0
      // 0f36: ldc 16.0
      // 0f38: fconst_2
      // 0f39: fconst_2
      // 0f3a: ldc 4.0
      // 0f3c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f3f: dup
      // 0f40: fconst_0
      // 0f41: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f47: bipush 0
      // 0f48: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f4b: bipush 44
      // 0f4d: bipush 29
      // 0f4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f52: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f55: ldc 12.0
      // 0f57: ldc -12.0
      // 0f59: ldc 16.0
      // 0f5b: fconst_2
      // 0f5c: fconst_2
      // 0f5d: ldc 6.0
      // 0f5f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f62: dup
      // 0f63: fconst_0
      // 0f64: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f6a: bipush 0
      // 0f6b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f6e: bipush 44
      // 0f70: bipush 29
      // 0f72: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f75: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f78: ldc 16.0
      // 0f7a: ldc -10.0
      // 0f7c: ldc 16.0
      // 0f7e: ldc 4.0
      // 0f80: fconst_2
      // 0f81: ldc 4.0
      // 0f83: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f86: dup
      // 0f87: fconst_0
      // 0f88: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f8b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f8e: bipush 0
      // 0f8f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f92: bipush 44
      // 0f94: bipush 29
      // 0f96: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f99: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f9c: ldc 10.0
      // 0f9e: ldc -10.0
      // 0fa0: ldc 16.0
      // 0fa2: fconst_2
      // 0fa3: fconst_2
      // 0fa4: ldc 6.0
      // 0fa6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0fa9: dup
      // 0faa: fconst_0
      // 0fab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0fae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fb1: bipush 0
      // 0fb2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fb5: bipush 44
      // 0fb7: bipush 29
      // 0fb9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fbc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fbf: ldc_w 14.0
      // 0fc2: ldc -8.0
      // 0fc4: ldc 16.0
      // 0fc6: fconst_2
      // 0fc7: fconst_2
      // 0fc8: ldc 4.0
      // 0fca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0fcd: dup
      // 0fce: fconst_0
      // 0fcf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0fd2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fd5: bipush 0
      // 0fd6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fd9: bipush 44
      // 0fdb: bipush 29
      // 0fdd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fe0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fe3: ldc 16.0
      // 0fe5: ldc -28.0
      // 0fe7: ldc_w 14.0
      // 0fea: fconst_2
      // 0feb: fconst_2
      // 0fec: fconst_2
      // 0fed: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ff0: dup
      // 0ff1: fconst_0
      // 0ff2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ff5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ff8: bipush 0
      // 0ff9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ffc: bipush 44
      // 0ffe: bipush 29
      // 1000: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1003: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1006: ldc 8.0
      // 1008: ldc -26.0
      // 100a: ldc_w 14.0
      // 100d: fconst_2
      // 100e: fconst_2
      // 100f: ldc 4.0
      // 1011: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1014: dup
      // 1015: fconst_0
      // 1016: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1019: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 101c: bipush 0
      // 101d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1020: bipush 44
      // 1022: bipush 29
      // 1024: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1027: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 102a: ldc_w 22.0
      // 102d: ldc -26.0
      // 102f: ldc_w 14.0
      // 1032: fconst_2
      // 1033: ldc 4.0
      // 1035: ldc 4.0
      // 1037: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 103a: dup
      // 103b: fconst_0
      // 103c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 103f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1042: bipush 0
      // 1043: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1046: bipush 44
      // 1048: bipush 29
      // 104a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 104d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1050: ldc 4.0
      // 1052: ldc -26.0
      // 1054: ldc_w 14.0
      // 1057: ldc 4.0
      // 1059: ldc 4.0
      // 105b: fconst_2
      // 105c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 105f: dup
      // 1060: fconst_0
      // 1061: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1064: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1067: bipush 0
      // 1068: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 106b: bipush 44
      // 106d: bipush 29
      // 106f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1072: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1075: ldc 8.0
      // 1077: ldc -24.0
      // 1079: ldc_w 14.0
      // 107c: ldc 4.0
      // 107e: ldc 4.0
      // 1080: fconst_2
      // 1081: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1084: dup
      // 1085: fconst_0
      // 1086: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1089: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 108c: bipush 0
      // 108d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1090: bipush 44
      // 1092: bipush 29
      // 1094: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1097: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 109a: ldc_w 22.0
      // 109d: ldc -18.0
      // 109f: ldc_w 14.0
      // 10a2: fconst_2
      // 10a3: ldc 4.0
      // 10a5: ldc 6.0
      // 10a7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 10aa: dup
      // 10ab: fconst_0
      // 10ac: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 10af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10b2: bipush 0
      // 10b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10b6: bipush 44
      // 10b8: bipush 29
      // 10ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10bd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10c0: ldc 8.0
      // 10c2: ldc -18.0
      // 10c4: ldc_w 14.0
      // 10c7: fconst_2
      // 10c8: ldc 4.0
      // 10ca: ldc 6.0
      // 10cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 10cf: dup
      // 10d0: fconst_0
      // 10d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 10d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10d7: bipush 0
      // 10d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10db: bipush 44
      // 10dd: bipush 29
      // 10df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10e5: ldc_w 20.0
      // 10e8: ldc -16.0
      // 10ea: ldc_w 14.0
      // 10ed: fconst_2
      // 10ee: ldc 4.0
      // 10f0: ldc 6.0
      // 10f2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 10f5: dup
      // 10f6: fconst_0
      // 10f7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 10fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10fd: bipush 0
      // 10fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1101: bipush 44
      // 1103: bipush 29
      // 1105: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1108: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 110b: ldc 16.0
      // 110d: ldc -14.0
      // 110f: ldc_w 14.0
      // 1112: fconst_2
      // 1113: fconst_2
      // 1114: ldc 8.0
      // 1116: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1119: dup
      // 111a: fconst_0
      // 111b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 111e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1121: bipush 0
      // 1122: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1125: bipush 44
      // 1127: bipush 29
      // 1129: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 112c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 112f: ldc 18.0
      // 1131: ldc -14.0
      // 1133: ldc_w 14.0
      // 1136: fconst_2
      // 1137: ldc 4.0
      // 1139: fconst_2
      // 113a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 113d: dup
      // 113e: fconst_0
      // 113f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1142: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1145: bipush 0
      // 1146: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1149: bipush 44
      // 114b: bipush 29
      // 114d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1150: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1153: ldc_w 14.0
      // 1156: ldc -12.0
      // 1158: ldc_w 14.0
      // 115b: ldc 4.0
      // 115d: fconst_2
      // 115e: ldc 6.0
      // 1160: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1163: dup
      // 1164: fconst_0
      // 1165: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1168: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 116b: bipush 0
      // 116c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 116f: bipush 44
      // 1171: bipush 29
      // 1173: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1176: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1179: ldc_w 20.0
      // 117c: ldc -10.0
      // 117e: ldc_w 14.0
      // 1181: fconst_2
      // 1182: fconst_2
      // 1183: fconst_2
      // 1184: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1187: dup
      // 1188: fconst_0
      // 1189: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 118c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 118f: bipush 0
      // 1190: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1193: bipush 44
      // 1195: bipush 29
      // 1197: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 119a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 119d: ldc 10.0
      // 119f: ldc -10.0
      // 11a1: ldc_w 14.0
      // 11a4: ldc 8.0
      // 11a6: fconst_2
      // 11a7: fconst_2
      // 11a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11ab: dup
      // 11ac: fconst_0
      // 11ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 11b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11b3: bipush 0
      // 11b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11b7: bipush 44
      // 11b9: bipush 29
      // 11bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11c1: ldc 12.0
      // 11c3: ldc -8.0
      // 11c5: ldc_w 14.0
      // 11c8: fconst_2
      // 11c9: fconst_2
      // 11ca: ldc 6.0
      // 11cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11cf: dup
      // 11d0: fconst_0
      // 11d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 11d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11d7: bipush 0
      // 11d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11db: bipush 44
      // 11dd: bipush 29
      // 11df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11e5: ldc 16.0
      // 11e7: ldc -30.0
      // 11e9: ldc 12.0
      // 11eb: fconst_2
      // 11ec: fconst_2
      // 11ed: ldc 4.0
      // 11ef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11f2: dup
      // 11f3: fconst_0
      // 11f4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 11f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11fa: bipush 0
      // 11fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11fe: bipush 44
      // 1200: bipush 29
      // 1202: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1205: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1208: ldc 18.0
      // 120a: ldc -28.0
      // 120c: ldc 12.0
      // 120e: fconst_2
      // 120f: fconst_2
      // 1210: fconst_2
      // 1211: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1214: dup
      // 1215: fconst_0
      // 1216: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1219: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 121c: bipush 0
      // 121d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1220: bipush 44
      // 1222: bipush 29
      // 1224: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1227: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 122a: ldc_w 14.0
      // 122d: ldc -30.0
      // 122f: ldc 12.0
      // 1231: fconst_2
      // 1232: ldc 4.0
      // 1234: ldc 4.0
      // 1236: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1239: dup
      // 123a: fconst_0
      // 123b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 123e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1241: bipush 0
      // 1242: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1245: bipush 44
      // 1247: bipush 29
      // 1249: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 124c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 124f: ldc 8.0
      // 1251: ldc -28.0
      // 1253: ldc 12.0
      // 1255: fconst_2
      // 1256: fconst_2
      // 1257: ldc 4.0
      // 1259: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 125c: dup
      // 125d: fconst_0
      // 125e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1261: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1264: bipush 0
      // 1265: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1268: bipush 44
      // 126a: bipush 29
      // 126c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 126f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1272: ldc 12.0
      // 1274: ldc -26.0
      // 1276: ldc 12.0
      // 1278: fconst_2
      // 1279: fconst_2
      // 127a: fconst_2
      // 127b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 127e: dup
      // 127f: fconst_0
      // 1280: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1283: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1286: bipush 0
      // 1287: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 128a: bipush 44
      // 128c: bipush 29
      // 128e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1291: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1294: ldc 12.0
      // 1296: ldc -24.0
      // 1298: ldc 12.0
      // 129a: fconst_2
      // 129b: ldc 4.0
      // 129d: ldc 4.0
      // 129f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12a2: dup
      // 12a3: fconst_0
      // 12a4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 12a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12aa: bipush 0
      // 12ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12ae: bipush 44
      // 12b0: bipush 29
      // 12b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12b8: ldc 12.0
      // 12ba: ldc -20.0
      // 12bc: ldc 12.0
      // 12be: ldc 6.0
      // 12c0: fconst_2
      // 12c1: fconst_2
      // 12c2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12c5: dup
      // 12c6: fconst_0
      // 12c7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 12ca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12cd: bipush 0
      // 12ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12d1: bipush 44
      // 12d3: bipush 29
      // 12d5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12db: ldc 10.0
      // 12dd: ldc -16.0
      // 12df: ldc 12.0
      // 12e1: fconst_2
      // 12e2: fconst_2
      // 12e3: ldc 8.0
      // 12e5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12e8: dup
      // 12e9: fconst_0
      // 12ea: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 12ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12f0: bipush 0
      // 12f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12f4: bipush 44
      // 12f6: bipush 29
      // 12f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12fe: ldc 8.0
      // 1300: ldc -14.0
      // 1302: ldc 12.0
      // 1304: ldc 4.0
      // 1306: fconst_2
      // 1307: ldc 4.0
      // 1309: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 130c: dup
      // 130d: fconst_0
      // 130e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1311: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1314: bipush 0
      // 1315: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1318: bipush 44
      // 131a: bipush 29
      // 131c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 131f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1322: ldc 18.0
      // 1324: ldc -10.0
      // 1326: ldc 12.0
      // 1328: ldc 4.0
      // 132a: fconst_2
      // 132b: fconst_2
      // 132c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 132f: dup
      // 1330: fconst_0
      // 1331: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1334: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1337: bipush 0
      // 1338: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 133b: bipush 44
      // 133d: bipush 29
      // 133f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1342: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1345: ldc 18.0
      // 1347: ldc -8.0
      // 1349: ldc 12.0
      // 134b: ldc 4.0
      // 134d: fconst_2
      // 134e: ldc 4.0
      // 1350: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1353: dup
      // 1354: fconst_0
      // 1355: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1358: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 135b: bipush 0
      // 135c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 135f: bipush 44
      // 1361: bipush 29
      // 1363: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1366: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1369: ldc 16.0
      // 136b: ldc -28.0
      // 136d: ldc 10.0
      // 136f: ldc 4.0
      // 1371: fconst_2
      // 1372: fconst_2
      // 1373: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1376: dup
      // 1377: fconst_0
      // 1378: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 137b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 137e: bipush 0
      // 137f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1382: bipush 44
      // 1384: bipush 29
      // 1386: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1389: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 138c: ldc 12.0
      // 138e: ldc -28.0
      // 1390: ldc 10.0
      // 1392: fconst_2
      // 1393: fconst_2
      // 1394: ldc 4.0
      // 1396: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1399: dup
      // 139a: fconst_0
      // 139b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 139e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13a1: bipush 0
      // 13a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13a5: bipush 44
      // 13a7: bipush 29
      // 13a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13af: ldc 16.0
      // 13b1: ldc -26.0
      // 13b3: ldc 10.0
      // 13b5: fconst_2
      // 13b6: fconst_2
      // 13b7: ldc 4.0
      // 13b9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 13bc: dup
      // 13bd: fconst_0
      // 13be: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 13c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13c4: bipush 0
      // 13c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13c8: bipush 44
      // 13ca: bipush 29
      // 13cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13d2: ldc 10.0
      // 13d4: ldc -28.0
      // 13d6: ldc 10.0
      // 13d8: fconst_2
      // 13d9: ldc 4.0
      // 13db: ldc 6.0
      // 13dd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 13e0: dup
      // 13e1: fconst_0
      // 13e2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 13e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13e8: bipush 0
      // 13e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13ec: bipush 44
      // 13ee: bipush 29
      // 13f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13f6: ldc 18.0
      // 13f8: ldc -24.0
      // 13fa: ldc 10.0
      // 13fc: ldc 4.0
      // 13fe: fconst_2
      // 13ff: fconst_2
      // 1400: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1403: dup
      // 1404: fconst_0
      // 1405: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1408: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 140b: bipush 0
      // 140c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 140f: bipush 44
      // 1411: bipush 29
      // 1413: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1416: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1419: ldc_w 14.0
      // 141c: ldc -26.0
      // 141e: ldc 10.0
      // 1420: fconst_2
      // 1421: ldc 4.0
      // 1423: fconst_2
      // 1424: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1427: dup
      // 1428: fconst_0
      // 1429: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 142c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 142f: bipush 0
      // 1430: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1433: bipush 44
      // 1435: bipush 29
      // 1437: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 143a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 143d: ldc_w 14.0
      // 1440: ldc -22.0
      // 1442: ldc 10.0
      // 1444: fconst_2
      // 1445: fconst_2
      // 1446: ldc 4.0
      // 1448: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 144b: dup
      // 144c: fconst_0
      // 144d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1450: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1453: bipush 0
      // 1454: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1457: bipush 44
      // 1459: bipush 29
      // 145b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 145e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1461: ldc_w 20.0
      // 1464: ldc -18.0
      // 1466: ldc 10.0
      // 1468: fconst_2
      // 1469: fconst_2
      // 146a: fconst_2
      // 146b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 146e: dup
      // 146f: fconst_0
      // 1470: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1473: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1476: bipush 0
      // 1477: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 147a: bipush 44
      // 147c: bipush 29
      // 147e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1481: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1484: ldc_w 14.0
      // 1487: ldc -20.0
      // 1489: ldc 10.0
      // 148b: ldc 4.0
      // 148d: ldc 4.0
      // 148f: fconst_2
      // 1490: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1493: dup
      // 1494: fconst_0
      // 1495: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1498: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 149b: bipush 0
      // 149c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 149f: bipush 44
      // 14a1: bipush 29
      // 14a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14a6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14a9: ldc 18.0
      // 14ab: ldc -18.0
      // 14ad: ldc 10.0
      // 14af: fconst_2
      // 14b0: ldc 4.0
      // 14b2: fconst_2
      // 14b3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14b6: dup
      // 14b7: fconst_0
      // 14b8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 14bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14be: bipush 0
      // 14bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14c2: bipush 44
      // 14c4: bipush 29
      // 14c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14c9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14cc: ldc 10.0
      // 14ce: ldc -12.0
      // 14d0: ldc 10.0
      // 14d2: fconst_2
      // 14d3: fconst_2
      // 14d4: ldc 4.0
      // 14d6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14d9: dup
      // 14da: fconst_0
      // 14db: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 14de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14e1: bipush 0
      // 14e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14e5: bipush 44
      // 14e7: bipush 29
      // 14e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14ef: ldc 12.0
      // 14f1: ldc -12.0
      // 14f3: ldc 10.0
      // 14f5: fconst_2
      // 14f6: ldc 4.0
      // 14f8: fconst_2
      // 14f9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14fc: dup
      // 14fd: fconst_0
      // 14fe: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1501: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1504: bipush 0
      // 1505: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1508: bipush 44
      // 150a: bipush 29
      // 150c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 150f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1512: ldc 18.0
      // 1514: ldc -26.0
      // 1516: ldc 8.0
      // 1518: fconst_2
      // 1519: fconst_2
      // 151a: ldc 6.0
      // 151c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 151f: dup
      // 1520: fconst_0
      // 1521: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1524: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1527: bipush 0
      // 1528: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 152b: bipush 44
      // 152d: bipush 29
      // 152f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1532: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1535: ldc 12.0
      // 1537: ldc -26.0
      // 1539: ldc 8.0
      // 153b: ldc 4.0
      // 153d: fconst_2
      // 153e: fconst_2
      // 153f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1542: dup
      // 1543: fconst_0
      // 1544: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1547: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 154a: bipush 0
      // 154b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 154e: bipush 44
      // 1550: bipush 29
      // 1552: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1555: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1558: ldc 16.0
      // 155a: ldc -24.0
      // 155c: ldc 8.0
      // 155e: ldc 6.0
      // 1560: fconst_2
      // 1561: fconst_2
      // 1562: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1565: dup
      // 1566: fconst_0
      // 1567: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 156a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 156d: bipush 0
      // 156e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1571: bipush 44
      // 1573: bipush 29
      // 1575: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1578: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 157b: ldc 10.0
      // 157d: ldc -24.0
      // 157f: ldc 8.0
      // 1581: ldc 4.0
      // 1583: fconst_2
      // 1584: ldc 4.0
      // 1586: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1589: dup
      // 158a: fconst_0
      // 158b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 158e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1591: bipush 0
      // 1592: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1595: bipush 44
      // 1597: bipush 29
      // 1599: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 159c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 159f: ldc_w 22.0
      // 15a2: ldc -18.0
      // 15a4: ldc 8.0
      // 15a6: fconst_2
      // 15a7: fconst_2
      // 15a8: ldc 4.0
      // 15aa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15ad: dup
      // 15ae: fconst_0
      // 15af: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15b5: bipush 0
      // 15b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15b9: bipush 44
      // 15bb: bipush 29
      // 15bd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15c3: ldc 18.0
      // 15c5: ldc -18.0
      // 15c7: ldc 8.0
      // 15c9: ldc 4.0
      // 15cb: ldc 4.0
      // 15cd: fconst_2
      // 15ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15d1: dup
      // 15d2: fconst_0
      // 15d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15d9: bipush 0
      // 15da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15dd: bipush 44
      // 15df: bipush 29
      // 15e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15e7: ldc_w 14.0
      // 15ea: ldc -10.0
      // 15ec: ldc 8.0
      // 15ee: fconst_2
      // 15ef: fconst_2
      // 15f0: fconst_2
      // 15f1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15f4: dup
      // 15f5: fconst_0
      // 15f6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15f9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15fc: bipush 0
      // 15fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1600: bipush 44
      // 1602: bipush 29
      // 1604: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1607: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 160a: ldc 12.0
      // 160c: ldc -10.0
      // 160e: ldc 8.0
      // 1610: fconst_2
      // 1611: ldc 4.0
      // 1613: fconst_2
      // 1614: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1617: dup
      // 1618: fconst_0
      // 1619: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 161c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 161f: bipush 0
      // 1620: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1623: bipush 44
      // 1625: bipush 29
      // 1627: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 162a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 162d: ldc_w 14.0
      // 1630: ldc -24.0
      // 1632: ldc 6.0
      // 1634: ldc 4.0
      // 1636: fconst_2
      // 1637: fconst_2
      // 1638: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 163b: dup
      // 163c: fconst_0
      // 163d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1640: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1643: bipush 0
      // 1644: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1647: bipush 44
      // 1649: bipush 29
      // 164b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 164e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1651: ldc_w 20.0
      // 1654: ldc -22.0
      // 1656: ldc 6.0
      // 1658: ldc 4.0
      // 165a: fconst_2
      // 165b: ldc 4.0
      // 165d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1660: dup
      // 1661: fconst_0
      // 1662: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1665: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1668: bipush 0
      // 1669: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 166c: bipush 44
      // 166e: bipush 29
      // 1670: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1673: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1676: ldc 12.0
      // 1678: ldc -22.0
      // 167a: ldc 6.0
      // 167c: ldc 4.0
      // 167e: fconst_2
      // 167f: ldc 4.0
      // 1681: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1684: dup
      // 1685: fconst_0
      // 1686: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1689: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 168c: bipush 0
      // 168d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1690: bipush 44
      // 1692: bipush 29
      // 1694: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1697: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 169a: ldc 18.0
      // 169c: ldc -20.0
      // 169e: ldc 6.0
      // 16a0: fconst_2
      // 16a1: fconst_2
      // 16a2: fconst_2
      // 16a3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16a6: dup
      // 16a7: fconst_0
      // 16a8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16ae: bipush 0
      // 16af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16b2: bipush 44
      // 16b4: bipush 29
      // 16b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16bc: ldc 16.0
      // 16be: ldc -20.0
      // 16c0: ldc 6.0
      // 16c2: fconst_2
      // 16c3: ldc 4.0
      // 16c5: fconst_2
      // 16c6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16c9: dup
      // 16ca: fconst_0
      // 16cb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16d1: bipush 0
      // 16d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16d5: bipush 44
      // 16d7: bipush 29
      // 16d9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16df: ldc 16.0
      // 16e1: ldc -22.0
      // 16e3: ldc 4.0
      // 16e5: fconst_2
      // 16e6: fconst_2
      // 16e7: ldc 10.0
      // 16e9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16ec: dup
      // 16ed: fconst_0
      // 16ee: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16f4: bipush 0
      // 16f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16f8: bipush 44
      // 16fa: bipush 29
      // 16fc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1702: ldc_w 22.0
      // 1705: ldc -20.0
      // 1707: ldc 4.0
      // 1709: fconst_2
      // 170a: fconst_2
      // 170b: ldc 6.0
      // 170d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1710: dup
      // 1711: fconst_0
      // 1712: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1715: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1718: bipush 0
      // 1719: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 171c: bipush 44
      // 171e: bipush 29
      // 1720: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1723: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1726: ldc_w 14.0
      // 1729: ldc -20.0
      // 172b: ldc 4.0
      // 172d: fconst_2
      // 172e: fconst_2
      // 172f: ldc 4.0
      // 1731: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1734: dup
      // 1735: fconst_0
      // 1736: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1739: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 173c: bipush 0
      // 173d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1740: bipush 44
      // 1742: bipush 29
      // 1744: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1747: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 174a: ldc 18.0
      // 174c: ldc -16.0
      // 174e: ldc 4.0
      // 1750: fconst_2
      // 1751: fconst_2
      // 1752: fconst_2
      // 1753: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1756: dup
      // 1757: fconst_0
      // 1758: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 175b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 175e: bipush 0
      // 175f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1762: bipush 44
      // 1764: bipush 29
      // 1766: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1769: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 176c: ldc_w 20.0
      // 176f: ldc -18.0
      // 1771: fconst_2
      // 1772: fconst_2
      // 1773: fconst_2
      // 1774: fconst_2
      // 1775: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1778: dup
      // 1779: fconst_0
      // 177a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 177d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1780: bipush 0
      // 1781: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1784: bipush 44
      // 1786: bipush 29
      // 1788: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 178b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 178e: ldc_w 22.0
      // 1791: ldc -16.0
      // 1793: fconst_2
      // 1794: fconst_2
      // 1795: fconst_2
      // 1796: fconst_2
      // 1797: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 179a: dup
      // 179b: fconst_0
      // 179c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 179f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17a2: bipush 0
      // 17a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17a6: bipush 44
      // 17a8: bipush 29
      // 17aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17ad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17b0: ldc 16.0
      // 17b2: ldc -18.0
      // 17b4: fconst_2
      // 17b5: fconst_2
      // 17b6: ldc 4.0
      // 17b8: ldc 4.0
      // 17ba: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 17bd: dup
      // 17be: fconst_0
      // 17bf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 17c2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17c5: bipush 0
      // 17c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17c9: bipush 44
      // 17cb: bipush 29
      // 17cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17d0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17d3: ldc 18.0
      // 17d5: ldc -16.0
      // 17d7: fconst_2
      // 17d8: ldc 4.0
      // 17da: ldc 4.0
      // 17dc: fconst_2
      // 17dd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 17e0: dup
      // 17e1: fconst_0
      // 17e2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 17e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17e8: bipush 0
      // 17e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17ec: ldc -19.0
      // 17ee: ldc -4.0
      // 17f0: ldc -15.0
      // 17f2: fconst_0
      // 17f3: ldc_w -1.5708
      // 17f6: ldc_w 1.5708
      // 17f9: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 17fc: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 17ff: pop
      // 1800: aload 0
      // 1801: ldc_w "growth13"
      // 1804: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1807: bipush 44
      // 1809: bipush 29
      // 180b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 180e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1811: ldc 10.0
      // 1813: ldc -32.0
      // 1815: ldc_w 32.0
      // 1818: fconst_2
      // 1819: fconst_2
      // 181a: ldc 4.0
      // 181c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 181f: dup
      // 1820: fconst_0
      // 1821: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1824: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1827: bipush 0
      // 1828: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 182b: bipush 44
      // 182d: bipush 29
      // 182f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1832: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1835: ldc 6.0
      // 1837: ldc -28.0
      // 1839: ldc_w 32.0
      // 183c: fconst_2
      // 183d: fconst_2
      // 183e: fconst_2
      // 183f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1842: dup
      // 1843: fconst_0
      // 1844: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1847: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 184a: bipush 0
      // 184b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 184e: bipush 44
      // 1850: bipush 29
      // 1852: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1855: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1858: ldc 8.0
      // 185a: ldc -6.0
      // 185c: ldc_w 32.0
      // 185f: fconst_2
      // 1860: fconst_2
      // 1861: ldc 4.0
      // 1863: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1866: dup
      // 1867: fconst_0
      // 1868: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 186b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 186e: bipush 0
      // 186f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1872: bipush 44
      // 1874: bipush 29
      // 1876: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1879: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 187c: ldc 8.0
      // 187e: ldc -36.0
      // 1880: ldc_w 30.0
      // 1883: fconst_2
      // 1884: fconst_2
      // 1885: ldc 6.0
      // 1887: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 188a: dup
      // 188b: fconst_0
      // 188c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 188f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1892: bipush 0
      // 1893: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1896: bipush 44
      // 1898: bipush 29
      // 189a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 189d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18a0: ldc_w 14.0
      // 18a3: ldc -34.0
      // 18a5: ldc_w 30.0
      // 18a8: fconst_2
      // 18a9: fconst_2
      // 18aa: ldc 4.0
      // 18ac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 18af: dup
      // 18b0: fconst_0
      // 18b1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18b7: bipush 0
      // 18b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18bb: bipush 44
      // 18bd: bipush 29
      // 18bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18c2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18c5: ldc 10.0
      // 18c7: ldc -34.0
      // 18c9: ldc_w 30.0
      // 18cc: fconst_2
      // 18cd: fconst_2
      // 18ce: ldc 4.0
      // 18d0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 18d3: dup
      // 18d4: fconst_0
      // 18d5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18db: bipush 0
      // 18dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18df: bipush 44
      // 18e1: bipush 29
      // 18e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18e9: ldc 6.0
      // 18eb: ldc -34.0
      // 18ed: ldc_w 30.0
      // 18f0: fconst_2
      // 18f1: fconst_2
      // 18f2: ldc 4.0
      // 18f4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 18f7: dup
      // 18f8: fconst_0
      // 18f9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18fc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18ff: bipush 0
      // 1900: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1903: bipush 44
      // 1905: bipush 29
      // 1907: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 190a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 190d: ldc 16.0
      // 190f: ldc -32.0
      // 1911: ldc_w 30.0
      // 1914: ldc 4.0
      // 1916: fconst_2
      // 1917: fconst_2
      // 1918: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 191b: dup
      // 191c: fconst_0
      // 191d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1920: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1923: bipush 0
      // 1924: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1927: bipush 44
      // 1929: bipush 29
      // 192b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 192e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1931: ldc 12.0
      // 1933: ldc -32.0
      // 1935: ldc_w 30.0
      // 1938: ldc 4.0
      // 193a: fconst_2
      // 193b: ldc 4.0
      // 193d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1940: dup
      // 1941: fconst_0
      // 1942: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1945: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1948: bipush 0
      // 1949: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 194c: bipush 44
      // 194e: bipush 29
      // 1950: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1953: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1956: ldc 8.0
      // 1958: ldc -32.0
      // 195a: ldc_w 30.0
      // 195d: fconst_2
      // 195e: fconst_2
      // 195f: ldc 6.0
      // 1961: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1964: dup
      // 1965: fconst_0
      // 1966: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1969: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 196c: bipush 0
      // 196d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1970: bipush 44
      // 1972: bipush 29
      // 1974: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1977: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 197a: ldc_w 22.0
      // 197d: ldc -20.0
      // 197f: ldc_w 30.0
      // 1982: fconst_2
      // 1983: fconst_2
      // 1984: fconst_2
      // 1985: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1988: dup
      // 1989: fconst_0
      // 198a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 198d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1990: bipush 0
      // 1991: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1994: bipush 44
      // 1996: bipush 29
      // 1998: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 199b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 199e: ldc 4.0
      // 19a0: ldc -16.0
      // 19a2: ldc_w 30.0
      // 19a5: fconst_2
      // 19a6: ldc 6.0
      // 19a8: fconst_2
      // 19a9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 19ac: dup
      // 19ad: fconst_0
      // 19ae: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 19b1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19b4: bipush 0
      // 19b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19b8: bipush 44
      // 19ba: bipush 29
      // 19bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19c2: fconst_2
      // 19c3: ldc -12.0
      // 19c5: ldc_w 30.0
      // 19c8: fconst_2
      // 19c9: ldc 4.0
      // 19cb: fconst_2
      // 19cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 19cf: dup
      // 19d0: fconst_0
      // 19d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 19d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19d7: bipush 0
      // 19d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19db: bipush 44
      // 19dd: bipush 29
      // 19df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19e5: ldc 10.0
      // 19e7: ldc -10.0
      // 19e9: ldc_w 30.0
      // 19ec: fconst_2
      // 19ed: ldc 4.0
      // 19ef: ldc 6.0
      // 19f1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 19f4: dup
      // 19f5: fconst_0
      // 19f6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 19f9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19fc: bipush 0
      // 19fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a00: bipush 44
      // 1a02: bipush 29
      // 1a04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a0a: ldc_w 22.0
      // 1a0d: ldc -36.0
      // 1a0f: ldc_w 28.0
      // 1a12: fconst_2
      // 1a13: fconst_2
      // 1a14: fconst_2
      // 1a15: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a18: dup
      // 1a19: fconst_0
      // 1a1a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a1d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a20: bipush 0
      // 1a21: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a24: bipush 44
      // 1a26: bipush 29
      // 1a28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a2b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a2e: ldc 10.0
      // 1a30: ldc -36.0
      // 1a32: ldc_w 28.0
      // 1a35: ldc 6.0
      // 1a37: fconst_2
      // 1a38: fconst_2
      // 1a39: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a3c: dup
      // 1a3d: fconst_0
      // 1a3e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a41: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a44: bipush 0
      // 1a45: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a48: bipush 44
      // 1a4a: bipush 29
      // 1a4c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a52: ldc 16.0
      // 1a54: ldc -34.0
      // 1a56: ldc_w 28.0
      // 1a59: ldc 6.0
      // 1a5b: fconst_2
      // 1a5c: fconst_2
      // 1a5d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a60: dup
      // 1a61: fconst_0
      // 1a62: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a65: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a68: bipush 0
      // 1a69: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a6c: bipush 44
      // 1a6e: bipush 29
      // 1a70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a76: ldc 12.0
      // 1a78: ldc -34.0
      // 1a7a: ldc_w 28.0
      // 1a7d: fconst_2
      // 1a7e: fconst_2
      // 1a7f: ldc 6.0
      // 1a81: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a84: dup
      // 1a85: fconst_0
      // 1a86: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a8c: bipush 0
      // 1a8d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a90: bipush 44
      // 1a92: bipush 29
      // 1a94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a9a: ldc_w 20.0
      // 1a9d: ldc -32.0
      // 1a9f: ldc_w 28.0
      // 1aa2: ldc 4.0
      // 1aa4: fconst_2
      // 1aa5: fconst_2
      // 1aa6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1aa9: dup
      // 1aaa: fconst_0
      // 1aab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1aae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ab1: bipush 0
      // 1ab2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ab5: bipush 44
      // 1ab7: bipush 29
      // 1ab9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1abc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1abf: ldc 8.0
      // 1ac1: ldc -34.0
      // 1ac3: ldc_w 28.0
      // 1ac6: ldc 4.0
      // 1ac8: ldc 4.0
      // 1aca: fconst_2
      // 1acb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ace: dup
      // 1acf: fconst_0
      // 1ad0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ad3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ad6: bipush 0
      // 1ad7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ada: bipush 44
      // 1adc: bipush 29
      // 1ade: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ae1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ae4: ldc 10.0
      // 1ae6: ldc -30.0
      // 1ae8: ldc_w 28.0
      // 1aeb: fconst_2
      // 1aec: fconst_2
      // 1aed: ldc 6.0
      // 1aef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1af2: dup
      // 1af3: fconst_0
      // 1af4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1af7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1afa: bipush 0
      // 1afb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1afe: bipush 44
      // 1b00: bipush 29
      // 1b02: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b05: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b08: ldc 4.0
      // 1b0a: ldc -24.0
      // 1b0c: ldc_w 28.0
      // 1b0f: ldc 4.0
      // 1b11: fconst_2
      // 1b12: ldc 4.0
      // 1b14: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b17: dup
      // 1b18: fconst_0
      // 1b19: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b1f: bipush 0
      // 1b20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b23: bipush 44
      // 1b25: bipush 29
      // 1b27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b2a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b2d: ldc_w 22.0
      // 1b30: ldc -22.0
      // 1b32: ldc_w 28.0
      // 1b35: fconst_2
      // 1b36: fconst_2
      // 1b37: fconst_2
      // 1b38: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b3b: dup
      // 1b3c: fconst_0
      // 1b3d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b43: bipush 0
      // 1b44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b47: bipush 44
      // 1b49: bipush 29
      // 1b4b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b4e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b51: ldc_w 26.0
      // 1b54: ldc -20.0
      // 1b56: ldc_w 28.0
      // 1b59: fconst_2
      // 1b5a: fconst_2
      // 1b5b: fconst_2
      // 1b5c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b5f: dup
      // 1b60: fconst_0
      // 1b61: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b67: bipush 0
      // 1b68: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b6b: bipush 44
      // 1b6d: bipush 29
      // 1b6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b72: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b75: ldc_w 22.0
      // 1b78: ldc -18.0
      // 1b7a: ldc_w 28.0
      // 1b7d: ldc 6.0
      // 1b7f: fconst_2
      // 1b80: ldc 4.0
      // 1b82: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b85: dup
      // 1b86: fconst_0
      // 1b87: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b8a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b8d: bipush 0
      // 1b8e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b91: bipush 44
      // 1b93: bipush 29
      // 1b95: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b98: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b9b: ldc_w 20.0
      // 1b9e: ldc -14.0
      // 1ba0: ldc_w 28.0
      // 1ba3: fconst_2
      // 1ba4: fconst_2
      // 1ba5: ldc 4.0
      // 1ba7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1baa: dup
      // 1bab: fconst_0
      // 1bac: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1baf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bb2: bipush 0
      // 1bb3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bb6: bipush 44
      // 1bb8: bipush 29
      // 1bba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bbd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bc0: ldc 6.0
      // 1bc2: ldc -14.0
      // 1bc4: ldc_w 28.0
      // 1bc7: fconst_2
      // 1bc8: fconst_2
      // 1bc9: ldc 4.0
      // 1bcb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1bce: dup
      // 1bcf: fconst_0
      // 1bd0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1bd3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bd6: bipush 0
      // 1bd7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bda: bipush 44
      // 1bdc: bipush 29
      // 1bde: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1be1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1be4: ldc 6.0
      // 1be6: ldc -12.0
      // 1be8: ldc_w 28.0
      // 1beb: ldc 4.0
      // 1bed: fconst_2
      // 1bee: ldc 4.0
      // 1bf0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1bf3: dup
      // 1bf4: fconst_0
      // 1bf5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1bf8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bfb: bipush 0
      // 1bfc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bff: bipush 44
      // 1c01: bipush 29
      // 1c03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c06: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c09: ldc 4.0
      // 1c0b: ldc -8.0
      // 1c0d: ldc_w 28.0
      // 1c10: fconst_2
      // 1c11: fconst_2
      // 1c12: fconst_2
      // 1c13: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c16: dup
      // 1c17: fconst_0
      // 1c18: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c1b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c1e: bipush 0
      // 1c1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c22: bipush 44
      // 1c24: bipush 29
      // 1c26: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c29: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c2c: ldc 16.0
      // 1c2e: ldc -36.0
      // 1c30: ldc_w 26.0
      // 1c33: ldc 6.0
      // 1c35: fconst_2
      // 1c36: ldc 4.0
      // 1c38: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c3b: dup
      // 1c3c: fconst_0
      // 1c3d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c43: bipush 0
      // 1c44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c47: bipush 44
      // 1c49: bipush 29
      // 1c4b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c4e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c51: ldc_w 26.0
      // 1c54: ldc -34.0
      // 1c56: ldc_w 26.0
      // 1c59: fconst_2
      // 1c5a: fconst_2
      // 1c5b: fconst_2
      // 1c5c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c5f: dup
      // 1c60: fconst_0
      // 1c61: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c67: bipush 0
      // 1c68: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c6b: bipush 44
      // 1c6d: bipush 29
      // 1c6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c72: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c75: ldc_w 14.0
      // 1c78: ldc -34.0
      // 1c7a: ldc_w 26.0
      // 1c7d: ldc 6.0
      // 1c7f: fconst_2
      // 1c80: fconst_2
      // 1c81: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c84: dup
      // 1c85: fconst_0
      // 1c86: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c8c: bipush 0
      // 1c8d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c90: bipush 44
      // 1c92: bipush 29
      // 1c94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c9a: ldc_w 22.0
      // 1c9d: ldc -34.0
      // 1c9f: ldc_w 26.0
      // 1ca2: ldc 4.0
      // 1ca4: ldc 4.0
      // 1ca6: fconst_2
      // 1ca7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1caa: dup
      // 1cab: fconst_0
      // 1cac: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1caf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cb2: bipush 0
      // 1cb3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cb6: bipush 44
      // 1cb8: bipush 29
      // 1cba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cbd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cc0: ldc 12.0
      // 1cc2: ldc -32.0
      // 1cc4: ldc_w 26.0
      // 1cc7: ldc 6.0
      // 1cc9: fconst_2
      // 1cca: fconst_2
      // 1ccb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1cce: dup
      // 1ccf: fconst_0
      // 1cd0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1cd3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cd6: bipush 0
      // 1cd7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cda: bipush 44
      // 1cdc: bipush 29
      // 1cde: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ce1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ce4: ldc 12.0
      // 1ce6: ldc -30.0
      // 1ce8: ldc_w 26.0
      // 1ceb: ldc 16.0
      // 1ced: fconst_2
      // 1cee: ldc 4.0
      // 1cf0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1cf3: dup
      // 1cf4: fconst_0
      // 1cf5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1cf8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cfb: bipush 0
      // 1cfc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cff: bipush 44
      // 1d01: bipush 29
      // 1d03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d06: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d09: ldc 16.0
      // 1d0b: ldc -26.0
      // 1d0d: ldc_w 26.0
      // 1d10: fconst_2
      // 1d11: fconst_2
      // 1d12: ldc 8.0
      // 1d14: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d17: dup
      // 1d18: fconst_0
      // 1d19: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d1f: bipush 0
      // 1d20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d23: bipush 44
      // 1d25: bipush 29
      // 1d27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d2a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d2d: ldc 8.0
      // 1d2f: ldc -28.0
      // 1d31: ldc_w 26.0
      // 1d34: fconst_2
      // 1d35: ldc 4.0
      // 1d37: fconst_2
      // 1d38: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d3b: dup
      // 1d3c: fconst_0
      // 1d3d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d43: bipush 0
      // 1d44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d47: bipush 44
      // 1d49: bipush 29
      // 1d4b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d4e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d51: ldc 18.0
      // 1d53: ldc -24.0
      // 1d55: ldc_w 26.0
      // 1d58: ldc 6.0
      // 1d5a: fconst_2
      // 1d5b: ldc 6.0
      // 1d5d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d60: dup
      // 1d61: fconst_0
      // 1d62: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d65: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d68: bipush 0
      // 1d69: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d6c: bipush 44
      // 1d6e: bipush 29
      // 1d70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d76: fconst_2
      // 1d77: ldc -28.0
      // 1d79: ldc_w 26.0
      // 1d7c: ldc 6.0
      // 1d7e: ldc 6.0
      // 1d80: fconst_2
      // 1d81: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d84: dup
      // 1d85: fconst_0
      // 1d86: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d8c: bipush 0
      // 1d8d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d90: bipush 44
      // 1d92: bipush 29
      // 1d94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d9a: fconst_2
      // 1d9b: ldc_w -40.0
      // 1d9e: ldc 24.0
      // 1da0: fconst_2
      // 1da1: fconst_2
      // 1da2: fconst_2
      // 1da3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1da6: dup
      // 1da7: fconst_0
      // 1da8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1dab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dae: bipush 0
      // 1daf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1db2: bipush 44
      // 1db4: bipush 29
      // 1db6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1db9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dbc: ldc_w 22.0
      // 1dbf: ldc -36.0
      // 1dc1: ldc 24.0
      // 1dc3: ldc 4.0
      // 1dc5: fconst_2
      // 1dc6: ldc 4.0
      // 1dc8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1dcb: dup
      // 1dcc: fconst_0
      // 1dcd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1dd0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dd3: bipush 0
      // 1dd4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dd7: bipush 44
      // 1dd9: bipush 29
      // 1ddb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dde: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1de1: ldc_w 20.0
      // 1de4: ldc -34.0
      // 1de6: ldc 24.0
      // 1de8: ldc 8.0
      // 1dea: fconst_2
      // 1deb: fconst_2
      // 1dec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1def: dup
      // 1df0: fconst_0
      // 1df1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1df4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1df7: bipush 0
      // 1df8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dfb: bipush 44
      // 1dfd: bipush 29
      // 1dff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e02: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e05: ldc 18.0
      // 1e07: ldc -32.0
      // 1e09: ldc 24.0
      // 1e0b: ldc 8.0
      // 1e0d: fconst_2
      // 1e0e: fconst_2
      // 1e0f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e12: dup
      // 1e13: fconst_0
      // 1e14: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e17: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e1a: bipush 0
      // 1e1b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e1e: bipush 44
      // 1e20: bipush 29
      // 1e22: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e25: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e28: ldc_w 28.0
      // 1e2b: ldc -30.0
      // 1e2d: ldc 24.0
      // 1e2f: fconst_2
      // 1e30: fconst_2
      // 1e31: ldc 6.0
      // 1e33: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e36: dup
      // 1e37: fconst_0
      // 1e38: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e3e: bipush 0
      // 1e3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e42: bipush 44
      // 1e44: bipush 29
      // 1e46: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e49: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e4c: ldc 24.0
      // 1e4e: ldc -30.0
      // 1e50: ldc 24.0
      // 1e52: ldc 4.0
      // 1e54: ldc 4.0
      // 1e56: fconst_2
      // 1e57: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e5a: dup
      // 1e5b: fconst_0
      // 1e5c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e5f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e62: bipush 0
      // 1e63: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e66: bipush 44
      // 1e68: bipush 29
      // 1e6a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e6d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e70: ldc_w 32.0
      // 1e73: ldc -26.0
      // 1e75: ldc 24.0
      // 1e77: fconst_2
      // 1e78: fconst_2
      // 1e79: fconst_2
      // 1e7a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e7d: dup
      // 1e7e: fconst_0
      // 1e7f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e82: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e85: bipush 0
      // 1e86: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e89: bipush 44
      // 1e8b: bipush 29
      // 1e8d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e93: ldc_w 28.0
      // 1e96: ldc -28.0
      // 1e98: ldc 24.0
      // 1e9a: ldc 4.0
      // 1e9c: ldc 4.0
      // 1e9e: fconst_2
      // 1e9f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ea2: dup
      // 1ea3: fconst_0
      // 1ea4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ea7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eaa: bipush 0
      // 1eab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eae: bipush 44
      // 1eb0: bipush 29
      // 1eb2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eb5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eb8: ldc 24.0
      // 1eba: ldc -24.0
      // 1ebc: ldc 24.0
      // 1ebe: ldc 10.0
      // 1ec0: fconst_2
      // 1ec1: fconst_2
      // 1ec2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ec5: dup
      // 1ec6: fconst_0
      // 1ec7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1eca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ecd: bipush 0
      // 1ece: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ed1: bipush 44
      // 1ed3: bipush 29
      // 1ed5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ed8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1edb: ldc 16.0
      // 1edd: ldc -30.0
      // 1edf: ldc 24.0
      // 1ee1: ldc 8.0
      // 1ee3: ldc 8.0
      // 1ee5: fconst_2
      // 1ee6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ee9: dup
      // 1eea: fconst_0
      // 1eeb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1eee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ef1: bipush 0
      // 1ef2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ef5: bipush 44
      // 1ef7: bipush 29
      // 1ef9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1efc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eff: ldc 8.0
      // 1f01: ldc -24.0
      // 1f03: ldc 24.0
      // 1f05: ldc 6.0
      // 1f07: fconst_2
      // 1f08: ldc 10.0
      // 1f0a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f0d: dup
      // 1f0e: fconst_0
      // 1f0f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f12: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f15: bipush 0
      // 1f16: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f19: bipush 44
      // 1f1b: bipush 29
      // 1f1d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f23: ldc 24.0
      // 1f25: ldc -22.0
      // 1f27: ldc 24.0
      // 1f29: ldc 4.0
      // 1f2b: fconst_2
      // 1f2c: ldc 6.0
      // 1f2e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f31: dup
      // 1f32: fconst_0
      // 1f33: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f36: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f39: bipush 0
      // 1f3a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f3d: bipush 44
      // 1f3f: bipush 29
      // 1f41: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f47: ldc_w 22.0
      // 1f4a: ldc -20.0
      // 1f4c: ldc 24.0
      // 1f4e: ldc 4.0
      // 1f50: fconst_2
      // 1f51: ldc 6.0
      // 1f53: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f56: dup
      // 1f57: fconst_0
      // 1f58: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f5b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f5e: bipush 0
      // 1f5f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f62: bipush 44
      // 1f64: bipush 29
      // 1f66: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f69: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f6c: ldc 4.0
      // 1f6e: ldc_w -46.0
      // 1f71: ldc_w 22.0
      // 1f74: fconst_2
      // 1f75: fconst_2
      // 1f76: fconst_2
      // 1f77: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f7a: dup
      // 1f7b: fconst_0
      // 1f7c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f7f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f82: bipush 0
      // 1f83: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f86: bipush 44
      // 1f88: bipush 29
      // 1f8a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f8d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f90: ldc 4.0
      // 1f92: ldc_w -42.0
      // 1f95: ldc_w 22.0
      // 1f98: fconst_2
      // 1f99: fconst_2
      // 1f9a: fconst_2
      // 1f9b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f9e: dup
      // 1f9f: fconst_0
      // 1fa0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1fa3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fa6: bipush 0
      // 1fa7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1faa: bipush 44
      // 1fac: bipush 29
      // 1fae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fb1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fb4: ldc 4.0
      // 1fb6: ldc -38.0
      // 1fb8: ldc_w 22.0
      // 1fbb: fconst_2
      // 1fbc: fconst_2
      // 1fbd: fconst_2
      // 1fbe: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1fc1: dup
      // 1fc2: fconst_0
      // 1fc3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1fc6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fc9: bipush 0
      // 1fca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fcd: bipush 44
      // 1fcf: bipush 29
      // 1fd1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fd4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fd7: ldc 6.0
      // 1fd9: ldc_w -42.0
      // 1fdc: ldc_w 22.0
      // 1fdf: fconst_2
      // 1fe0: ldc 8.0
      // 1fe2: fconst_2
      // 1fe3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1fe6: dup
      // 1fe7: fconst_0
      // 1fe8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1feb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fee: bipush 0
      // 1fef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ff2: bipush 44
      // 1ff4: bipush 29
      // 1ff6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ff9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ffc: fconst_2
      // 1ffd: ldc -34.0
      // 1fff: ldc_w 22.0
      // 2002: fconst_2
      // 2003: fconst_2
      // 2004: ldc 4.0
      // 2006: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2009: dup
      // 200a: fconst_0
      // 200b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 200e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2011: bipush 0
      // 2012: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2015: bipush 44
      // 2017: bipush 29
      // 2019: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 201c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 201f: ldc_w 30.0
      // 2022: ldc -32.0
      // 2024: ldc_w 22.0
      // 2027: fconst_2
      // 2028: fconst_2
      // 2029: ldc 6.0
      // 202b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 202e: dup
      // 202f: fconst_0
      // 2030: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2033: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2036: bipush 0
      // 2037: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 203a: bipush 44
      // 203c: bipush 29
      // 203e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2041: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2044: ldc_w 32.0
      // 2047: ldc -28.0
      // 2049: ldc_w 22.0
      // 204c: fconst_2
      // 204d: fconst_2
      // 204e: ldc 4.0
      // 2050: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2053: dup
      // 2054: fconst_0
      // 2055: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2058: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 205b: bipush 0
      // 205c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 205f: bipush 44
      // 2061: bipush 29
      // 2063: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2066: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2069: ldc 24.0
      // 206b: ldc -26.0
      // 206d: ldc_w 22.0
      // 2070: fconst_2
      // 2071: fconst_2
      // 2072: ldc 4.0
      // 2074: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2077: dup
      // 2078: fconst_0
      // 2079: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 207c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 207f: bipush 0
      // 2080: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2083: bipush 44
      // 2085: bipush 29
      // 2087: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 208a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 208d: fconst_2
      // 208e: ldc -24.0
      // 2090: ldc_w 22.0
      // 2093: fconst_2
      // 2094: fconst_2
      // 2095: fconst_2
      // 2096: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2099: dup
      // 209a: fconst_0
      // 209b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 209e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20a1: bipush 0
      // 20a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20a5: bipush 44
      // 20a7: bipush 29
      // 20a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20af: ldc_w 20.0
      // 20b2: ldc -22.0
      // 20b4: ldc_w 22.0
      // 20b7: fconst_2
      // 20b8: fconst_2
      // 20b9: ldc 8.0
      // 20bb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 20be: dup
      // 20bf: fconst_0
      // 20c0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 20c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20c6: bipush 0
      // 20c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20ca: bipush 44
      // 20cc: bipush 29
      // 20ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20d4: ldc 4.0
      // 20d6: ldc_w -44.0
      // 20d9: ldc_w 20.0
      // 20dc: fconst_2
      // 20dd: fconst_2
      // 20de: fconst_2
      // 20df: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 20e2: dup
      // 20e3: fconst_0
      // 20e4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 20e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20ea: bipush 0
      // 20eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20ee: bipush 44
      // 20f0: bipush 29
      // 20f2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20f8: ldc 4.0
      // 20fa: ldc_w -40.0
      // 20fd: ldc_w 20.0
      // 2100: fconst_2
      // 2101: fconst_2
      // 2102: ldc 4.0
      // 2104: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2107: dup
      // 2108: fconst_0
      // 2109: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 210c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 210f: bipush 0
      // 2110: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2113: bipush 44
      // 2115: bipush 29
      // 2117: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 211a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 211d: ldc 4.0
      // 211f: ldc -36.0
      // 2121: ldc_w 20.0
      // 2124: fconst_2
      // 2125: fconst_2
      // 2126: ldc 4.0
      // 2128: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 212b: dup
      // 212c: fconst_0
      // 212d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2130: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2133: bipush 0
      // 2134: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2137: bipush 44
      // 2139: bipush 29
      // 213b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 213e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2141: ldc_w 30.0
      // 2144: ldc -30.0
      // 2146: ldc_w 20.0
      // 2149: fconst_2
      // 214a: fconst_2
      // 214b: ldc 6.0
      // 214d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2150: dup
      // 2151: fconst_0
      // 2152: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2155: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2158: bipush 0
      // 2159: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 215c: bipush 44
      // 215e: bipush 29
      // 2160: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2163: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2166: ldc_w 28.0
      // 2169: ldc -28.0
      // 216b: ldc_w 20.0
      // 216e: fconst_2
      // 216f: fconst_2
      // 2170: fconst_2
      // 2171: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2174: dup
      // 2175: fconst_0
      // 2176: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2179: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 217c: bipush 0
      // 217d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2180: bipush 44
      // 2182: bipush 29
      // 2184: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2187: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 218a: ldc_w 26.0
      // 218d: ldc -24.0
      // 218f: ldc_w 20.0
      // 2192: ldc 4.0
      // 2194: fconst_2
      // 2195: fconst_2
      // 2196: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2199: dup
      // 219a: fconst_0
      // 219b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 219e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21a1: bipush 0
      // 21a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21a5: bipush 44
      // 21a7: bipush 29
      // 21a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21af: ldc_w 22.0
      // 21b2: ldc -24.0
      // 21b4: ldc_w 20.0
      // 21b7: fconst_2
      // 21b8: fconst_2
      // 21b9: fconst_2
      // 21ba: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 21bd: dup
      // 21be: fconst_0
      // 21bf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 21c2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21c5: bipush 0
      // 21c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21c9: bipush 44
      // 21cb: bipush 29
      // 21cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21d0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21d3: ldc 24.0
      // 21d5: ldc -26.0
      // 21d7: ldc_w 20.0
      // 21da: fconst_2
      // 21db: ldc 6.0
      // 21dd: fconst_2
      // 21de: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 21e1: dup
      // 21e2: fconst_0
      // 21e3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 21e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21e9: bipush 0
      // 21ea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21ed: bipush 44
      // 21ef: bipush 29
      // 21f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21f7: ldc_w 22.0
      // 21fa: ldc -22.0
      // 21fc: ldc_w 20.0
      // 21ff: fconst_2
      // 2200: ldc 4.0
      // 2202: ldc 4.0
      // 2204: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2207: dup
      // 2208: fconst_0
      // 2209: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 220c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 220f: bipush 0
      // 2210: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2213: bipush 44
      // 2215: bipush 29
      // 2217: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 221a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 221d: ldc_w 26.0
      // 2220: ldc -16.0
      // 2222: ldc_w 20.0
      // 2225: fconst_2
      // 2226: fconst_2
      // 2227: fconst_2
      // 2228: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 222b: dup
      // 222c: fconst_0
      // 222d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2230: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2233: bipush 0
      // 2234: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2237: bipush 44
      // 2239: bipush 29
      // 223b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 223e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2241: ldc_w 22.0
      // 2244: ldc -16.0
      // 2246: ldc_w 20.0
      // 2249: fconst_2
      // 224a: fconst_2
      // 224b: ldc 12.0
      // 224d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2250: dup
      // 2251: fconst_0
      // 2252: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2255: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2258: bipush 0
      // 2259: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 225c: bipush 44
      // 225e: bipush 29
      // 2260: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2263: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2266: ldc 12.0
      // 2268: ldc -14.0
      // 226a: ldc_w 20.0
      // 226d: fconst_2
      // 226e: fconst_2
      // 226f: fconst_2
      // 2270: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2273: dup
      // 2274: fconst_0
      // 2275: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2278: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 227b: bipush 0
      // 227c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 227f: bipush 44
      // 2281: bipush 29
      // 2283: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2286: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2289: ldc_w 14.0
      // 228c: ldc -14.0
      // 228e: ldc_w 20.0
      // 2291: fconst_2
      // 2292: ldc 4.0
      // 2294: fconst_2
      // 2295: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2298: dup
      // 2299: fconst_0
      // 229a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 229d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22a0: bipush 0
      // 22a1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22a4: bipush 44
      // 22a6: bipush 29
      // 22a8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22ae: ldc 6.0
      // 22b0: ldc -14.0
      // 22b2: ldc_w 20.0
      // 22b5: fconst_2
      // 22b6: ldc 4.0
      // 22b8: fconst_2
      // 22b9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 22bc: dup
      // 22bd: fconst_0
      // 22be: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 22c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22c4: bipush 0
      // 22c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22c8: bipush 44
      // 22ca: bipush 29
      // 22cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22d2: ldc_w 14.0
      // 22d5: ldc -8.0
      // 22d7: ldc_w 20.0
      // 22da: fconst_2
      // 22db: ldc 4.0
      // 22dd: fconst_2
      // 22de: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 22e1: dup
      // 22e2: fconst_0
      // 22e3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 22e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22e9: bipush 0
      // 22ea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22ed: bipush 44
      // 22ef: bipush 29
      // 22f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22f7: ldc 6.0
      // 22f9: ldc_w -44.0
      // 22fc: ldc 18.0
      // 22fe: fconst_2
      // 22ff: fconst_2
      // 2300: fconst_2
      // 2301: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2304: dup
      // 2305: fconst_0
      // 2306: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2309: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 230c: bipush 0
      // 230d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2310: bipush 44
      // 2312: bipush 29
      // 2314: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2317: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 231a: ldc 4.0
      // 231c: ldc_w -42.0
      // 231f: ldc 18.0
      // 2321: fconst_2
      // 2322: fconst_2
      // 2323: fconst_2
      // 2324: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2327: dup
      // 2328: fconst_0
      // 2329: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 232c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 232f: bipush 0
      // 2330: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2333: bipush 44
      // 2335: bipush 29
      // 2337: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 233a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 233d: ldc_w 26.0
      // 2340: ldc -26.0
      // 2342: ldc 18.0
      // 2344: fconst_2
      // 2345: fconst_2
      // 2346: ldc 8.0
      // 2348: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 234b: dup
      // 234c: fconst_0
      // 234d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2350: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2353: bipush 0
      // 2354: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2357: bipush 44
      // 2359: bipush 29
      // 235b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 235e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2361: ldc_w 32.0
      // 2364: ldc -20.0
      // 2366: ldc 18.0
      // 2368: fconst_2
      // 2369: fconst_2
      // 236a: fconst_2
      // 236b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 236e: dup
      // 236f: fconst_0
      // 2370: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2373: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2376: bipush 0
      // 2377: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 237a: bipush 44
      // 237c: bipush 29
      // 237e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2381: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2384: ldc_w 30.0
      // 2387: ldc -20.0
      // 2389: ldc 18.0
      // 238b: fconst_2
      // 238c: ldc 4.0
      // 238e: fconst_2
      // 238f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2392: dup
      // 2393: fconst_0
      // 2394: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2397: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 239a: bipush 0
      // 239b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 239e: bipush 44
      // 23a0: bipush 29
      // 23a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23a5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23a8: ldc_w 26.0
      // 23ab: ldc -18.0
      // 23ad: ldc 18.0
      // 23af: fconst_2
      // 23b0: fconst_2
      // 23b1: fconst_2
      // 23b2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 23b5: dup
      // 23b6: fconst_0
      // 23b7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 23ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23bd: bipush 0
      // 23be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23c1: bipush 44
      // 23c3: bipush 29
      // 23c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23cb: ldc_w 28.0
      // 23ce: ldc -18.0
      // 23d0: ldc 18.0
      // 23d2: fconst_2
      // 23d3: ldc 4.0
      // 23d5: fconst_2
      // 23d6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 23d9: dup
      // 23da: fconst_0
      // 23db: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 23de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23e1: bipush 0
      // 23e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23e5: bipush 44
      // 23e7: bipush 29
      // 23e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23ef: ldc 24.0
      // 23f1: ldc -16.0
      // 23f3: ldc 18.0
      // 23f5: fconst_2
      // 23f6: fconst_2
      // 23f7: ldc 6.0
      // 23f9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 23fc: dup
      // 23fd: fconst_0
      // 23fe: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2401: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2404: bipush 0
      // 2405: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2408: bipush 44
      // 240a: bipush 29
      // 240c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 240f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2412: ldc 4.0
      // 2414: ldc -16.0
      // 2416: ldc 18.0
      // 2418: fconst_2
      // 2419: ldc 4.0
      // 241b: fconst_2
      // 241c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 241f: dup
      // 2420: fconst_0
      // 2421: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2424: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2427: bipush 0
      // 2428: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 242b: bipush 44
      // 242d: bipush 29
      // 242f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2432: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2435: ldc_w 22.0
      // 2438: ldc -16.0
      // 243a: ldc 18.0
      // 243c: fconst_2
      // 243d: ldc 6.0
      // 243f: fconst_2
      // 2440: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2443: dup
      // 2444: fconst_0
      // 2445: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2448: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 244b: bipush 0
      // 244c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 244f: bipush 44
      // 2451: bipush 29
      // 2453: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2456: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2459: ldc 16.0
      // 245b: ldc -14.0
      // 245d: ldc 18.0
      // 245f: fconst_2
      // 2460: ldc 4.0
      // 2462: ldc 4.0
      // 2464: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2467: dup
      // 2468: fconst_0
      // 2469: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 246c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 246f: bipush 0
      // 2470: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2473: bipush 44
      // 2475: bipush 29
      // 2477: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 247a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 247d: ldc 8.0
      // 247f: ldc -14.0
      // 2481: ldc 18.0
      // 2483: ldc 4.0
      // 2485: ldc 4.0
      // 2487: ldc 4.0
      // 2489: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 248c: dup
      // 248d: fconst_0
      // 248e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2491: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2494: bipush 0
      // 2495: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2498: bipush 44
      // 249a: bipush 29
      // 249c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 249f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24a2: fconst_2
      // 24a3: ldc -16.0
      // 24a5: ldc 18.0
      // 24a7: fconst_2
      // 24a8: ldc 6.0
      // 24aa: fconst_2
      // 24ab: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 24ae: dup
      // 24af: fconst_0
      // 24b0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 24b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24b6: bipush 0
      // 24b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24ba: bipush 44
      // 24bc: bipush 29
      // 24be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24c4: ldc 16.0
      // 24c6: ldc -10.0
      // 24c8: ldc 18.0
      // 24ca: ldc 6.0
      // 24cc: fconst_2
      // 24cd: fconst_2
      // 24ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 24d1: dup
      // 24d2: fconst_0
      // 24d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 24d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24d9: bipush 0
      // 24da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24dd: bipush 44
      // 24df: bipush 29
      // 24e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24e7: ldc_w 14.0
      // 24ea: ldc -8.0
      // 24ec: ldc 18.0
      // 24ee: ldc 4.0
      // 24f0: ldc 4.0
      // 24f2: fconst_2
      // 24f3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 24f6: dup
      // 24f7: fconst_0
      // 24f8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 24fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24fe: bipush 0
      // 24ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2502: bipush 44
      // 2504: bipush 29
      // 2506: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2509: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 250c: ldc 18.0
      // 250e: ldc -6.0
      // 2510: ldc 18.0
      // 2512: fconst_2
      // 2513: ldc 4.0
      // 2515: fconst_2
      // 2516: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2519: dup
      // 251a: fconst_0
      // 251b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 251e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2521: bipush 0
      // 2522: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2525: bipush 44
      // 2527: bipush 29
      // 2529: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 252c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 252f: ldc 8.0
      // 2531: ldc_w -42.0
      // 2534: ldc 16.0
      // 2536: fconst_2
      // 2537: fconst_2
      // 2538: fconst_2
      // 2539: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 253c: dup
      // 253d: fconst_0
      // 253e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2541: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2544: bipush 0
      // 2545: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2548: bipush 44
      // 254a: bipush 29
      // 254c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 254f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2552: ldc 6.0
      // 2554: ldc_w -40.0
      // 2557: ldc 16.0
      // 2559: fconst_2
      // 255a: fconst_2
      // 255b: fconst_2
      // 255c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 255f: dup
      // 2560: fconst_0
      // 2561: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2564: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2567: bipush 0
      // 2568: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 256b: bipush 44
      // 256d: bipush 29
      // 256f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2572: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2575: ldc 4.0
      // 2577: ldc -36.0
      // 2579: ldc 16.0
      // 257b: fconst_2
      // 257c: fconst_2
      // 257d: fconst_2
      // 257e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2581: dup
      // 2582: fconst_0
      // 2583: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2586: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2589: bipush 0
      // 258a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 258d: bipush 44
      // 258f: bipush 29
      // 2591: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2594: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2597: ldc 24.0
      // 2599: ldc -30.0
      // 259b: ldc 16.0
      // 259d: fconst_2
      // 259e: ldc 4.0
      // 25a0: fconst_2
      // 25a1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 25a4: dup
      // 25a5: fconst_0
      // 25a6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 25a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25ac: bipush 0
      // 25ad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25b0: bipush 44
      // 25b2: bipush 29
      // 25b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25ba: ldc 24.0
      // 25bc: ldc -14.0
      // 25be: ldc 16.0
      // 25c0: ldc 4.0
      // 25c2: fconst_2
      // 25c3: ldc 4.0
      // 25c5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 25c8: dup
      // 25c9: fconst_0
      // 25ca: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 25cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25d0: bipush 0
      // 25d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25d4: bipush 44
      // 25d6: bipush 29
      // 25d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25de: ldc 12.0
      // 25e0: ldc -14.0
      // 25e2: ldc 16.0
      // 25e4: fconst_2
      // 25e5: fconst_2
      // 25e6: fconst_2
      // 25e7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 25ea: dup
      // 25eb: fconst_0
      // 25ec: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 25ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25f2: bipush 0
      // 25f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25f6: bipush 44
      // 25f8: bipush 29
      // 25fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2600: ldc 18.0
      // 2602: ldc -14.0
      // 2604: ldc 16.0
      // 2606: ldc 4.0
      // 2608: ldc 4.0
      // 260a: ldc 4.0
      // 260c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 260f: dup
      // 2610: fconst_0
      // 2611: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2614: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2617: bipush 0
      // 2618: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 261b: bipush 44
      // 261d: bipush 29
      // 261f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2622: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2625: ldc 12.0
      // 2627: ldc -10.0
      // 2629: ldc 16.0
      // 262b: ldc 4.0
      // 262d: fconst_2
      // 262e: fconst_2
      // 262f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2632: dup
      // 2633: fconst_0
      // 2634: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2637: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 263a: bipush 0
      // 263b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 263e: bipush 44
      // 2640: bipush 29
      // 2642: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2645: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2648: ldc 18.0
      // 264a: ldc -4.0
      // 264c: ldc 16.0
      // 264e: ldc 4.0
      // 2650: fconst_2
      // 2651: fconst_2
      // 2652: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2655: dup
      // 2656: fconst_0
      // 2657: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 265a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 265d: bipush 0
      // 265e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2661: bipush 44
      // 2663: bipush 29
      // 2665: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2668: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 266b: ldc 12.0
      // 266d: ldc_w -40.0
      // 2670: ldc_w 14.0
      // 2673: fconst_2
      // 2674: fconst_2
      // 2675: fconst_2
      // 2676: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2679: dup
      // 267a: fconst_0
      // 267b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 267e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2681: bipush 0
      // 2682: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2685: bipush 44
      // 2687: bipush 29
      // 2689: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 268c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 268f: ldc 4.0
      // 2691: ldc_w -40.0
      // 2694: ldc_w 14.0
      // 2697: fconst_2
      // 2698: fconst_2
      // 2699: ldc 4.0
      // 269b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 269e: dup
      // 269f: fconst_0
      // 26a0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 26a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26a6: bipush 0
      // 26a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26aa: bipush 44
      // 26ac: bipush 29
      // 26ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26b1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26b4: ldc 8.0
      // 26b6: ldc_w -40.0
      // 26b9: ldc_w 14.0
      // 26bc: fconst_2
      // 26bd: ldc 4.0
      // 26bf: fconst_2
      // 26c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 26c3: dup
      // 26c4: fconst_0
      // 26c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 26c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26cb: bipush 0
      // 26cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26cf: bipush 44
      // 26d1: bipush 29
      // 26d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26d9: ldc_w 28.0
      // 26dc: ldc -26.0
      // 26de: ldc_w 14.0
      // 26e1: fconst_2
      // 26e2: fconst_2
      // 26e3: ldc 8.0
      // 26e5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 26e8: dup
      // 26e9: fconst_0
      // 26ea: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 26ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26f0: bipush 0
      // 26f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26f4: bipush 44
      // 26f6: bipush 29
      // 26f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26fe: ldc_w 26.0
      // 2701: ldc -24.0
      // 2703: ldc_w 14.0
      // 2706: fconst_2
      // 2707: fconst_2
      // 2708: ldc 4.0
      // 270a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 270d: dup
      // 270e: fconst_0
      // 270f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2712: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2715: bipush 0
      // 2716: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2719: bipush 44
      // 271b: bipush 29
      // 271d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2720: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2723: ldc 24.0
      // 2725: ldc -22.0
      // 2727: ldc_w 14.0
      // 272a: fconst_2
      // 272b: fconst_2
      // 272c: ldc 4.0
      // 272e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2731: dup
      // 2732: fconst_0
      // 2733: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2736: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2739: bipush 0
      // 273a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 273d: bipush 44
      // 273f: bipush 29
      // 2741: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2744: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2747: ldc 24.0
      // 2749: ldc -16.0
      // 274b: ldc_w 14.0
      // 274e: ldc 4.0
      // 2750: fconst_2
      // 2751: ldc 4.0
      // 2753: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2756: dup
      // 2757: fconst_0
      // 2758: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 275b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 275e: bipush 0
      // 275f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2762: bipush 44
      // 2764: bipush 29
      // 2766: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2769: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 276c: ldc_w 30.0
      // 276f: ldc -14.0
      // 2771: ldc_w 14.0
      // 2774: fconst_2
      // 2775: fconst_2
      // 2776: fconst_2
      // 2777: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 277a: dup
      // 277b: fconst_0
      // 277c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 277f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2782: bipush 0
      // 2783: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2786: bipush 44
      // 2788: bipush 29
      // 278a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 278d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2790: ldc_w 26.0
      // 2793: ldc -14.0
      // 2795: ldc_w 14.0
      // 2798: ldc 4.0
      // 279a: ldc 4.0
      // 279c: fconst_2
      // 279d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 27a0: dup
      // 27a1: fconst_0
      // 27a2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 27a5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27a8: bipush 0
      // 27a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27ac: bipush 44
      // 27ae: bipush 29
      // 27b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27b6: ldc 8.0
      // 27b8: ldc_w -46.0
      // 27bb: ldc 12.0
      // 27bd: fconst_2
      // 27be: ldc 4.0
      // 27c0: fconst_2
      // 27c1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 27c4: dup
      // 27c5: fconst_0
      // 27c6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 27c9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27cc: bipush 0
      // 27cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27d0: bipush 44
      // 27d2: bipush 29
      // 27d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27da: ldc 10.0
      // 27dc: ldc_w -42.0
      // 27df: ldc 12.0
      // 27e1: fconst_2
      // 27e2: fconst_2
      // 27e3: ldc 6.0
      // 27e5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 27e8: dup
      // 27e9: fconst_0
      // 27ea: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 27ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27f0: bipush 0
      // 27f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27f4: bipush 44
      // 27f6: bipush 29
      // 27f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27fe: ldc 10.0
      // 2800: ldc_w -40.0
      // 2803: ldc 12.0
      // 2805: ldc 4.0
      // 2807: fconst_2
      // 2808: fconst_2
      // 2809: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 280c: dup
      // 280d: fconst_0
      // 280e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2811: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2814: bipush 0
      // 2815: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2818: bipush 44
      // 281a: bipush 29
      // 281c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 281f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2822: ldc 6.0
      // 2824: ldc_w -40.0
      // 2827: ldc 12.0
      // 2829: fconst_2
      // 282a: ldc 4.0
      // 282c: ldc 4.0
      // 282e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2831: dup
      // 2832: fconst_0
      // 2833: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2836: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2839: bipush 0
      // 283a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 283d: bipush 44
      // 283f: bipush 29
      // 2841: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2844: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2847: ldc_w 26.0
      // 284a: ldc -26.0
      // 284c: ldc 12.0
      // 284e: ldc 4.0
      // 2850: fconst_2
      // 2851: fconst_2
      // 2852: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2855: dup
      // 2856: fconst_0
      // 2857: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 285a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 285d: bipush 0
      // 285e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2861: bipush 44
      // 2863: bipush 29
      // 2865: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2868: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 286b: ldc 24.0
      // 286d: ldc -26.0
      // 286f: ldc 12.0
      // 2871: fconst_2
      // 2872: ldc 4.0
      // 2874: ldc 6.0
      // 2876: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2879: dup
      // 287a: fconst_0
      // 287b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 287e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2881: bipush 0
      // 2882: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2885: bipush 44
      // 2887: bipush 29
      // 2889: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 288c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 288f: ldc_w 22.0
      // 2892: ldc -22.0
      // 2894: ldc 12.0
      // 2896: fconst_2
      // 2897: fconst_2
      // 2898: ldc 6.0
      // 289a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 289d: dup
      // 289e: fconst_0
      // 289f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 28a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28a5: bipush 0
      // 28a6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28a9: bipush 44
      // 28ab: bipush 29
      // 28ad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28b3: ldc 8.0
      // 28b5: ldc_w -46.0
      // 28b8: ldc 10.0
      // 28ba: ldc 4.0
      // 28bc: fconst_2
      // 28bd: fconst_2
      // 28be: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 28c1: dup
      // 28c2: fconst_0
      // 28c3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 28c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28c9: bipush 0
      // 28ca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28cd: bipush 44
      // 28cf: bipush 29
      // 28d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28d7: ldc 6.0
      // 28d9: ldc_w -44.0
      // 28dc: ldc 10.0
      // 28de: ldc 6.0
      // 28e0: fconst_2
      // 28e1: fconst_2
      // 28e2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 28e5: dup
      // 28e6: fconst_0
      // 28e7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 28ea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28ed: bipush 0
      // 28ee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28f1: bipush 44
      // 28f3: bipush 29
      // 28f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28fb: ldc 12.0
      // 28fd: ldc_w -40.0
      // 2900: ldc 10.0
      // 2902: ldc 4.0
      // 2904: fconst_2
      // 2905: fconst_2
      // 2906: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2909: dup
      // 290a: fconst_0
      // 290b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 290e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2911: bipush 0
      // 2912: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2915: bipush 44
      // 2917: bipush 29
      // 2919: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 291c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 291f: ldc 8.0
      // 2921: ldc -38.0
      // 2923: ldc 10.0
      // 2925: ldc 4.0
      // 2927: fconst_2
      // 2928: ldc 4.0
      // 292a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 292d: dup
      // 292e: fconst_0
      // 292f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2932: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2935: bipush 0
      // 2936: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2939: bipush 44
      // 293b: bipush 29
      // 293d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2940: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2943: ldc_w 26.0
      // 2946: ldc -32.0
      // 2948: ldc 10.0
      // 294a: fconst_2
      // 294b: fconst_2
      // 294c: ldc 6.0
      // 294e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2951: dup
      // 2952: fconst_0
      // 2953: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2956: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2959: bipush 0
      // 295a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 295d: bipush 44
      // 295f: bipush 29
      // 2961: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2964: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2967: ldc 24.0
      // 2969: ldc -30.0
      // 296b: ldc 10.0
      // 296d: fconst_2
      // 296e: fconst_2
      // 296f: ldc 4.0
      // 2971: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2974: dup
      // 2975: fconst_0
      // 2976: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2979: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 297c: bipush 0
      // 297d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2980: bipush 44
      // 2982: bipush 29
      // 2984: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2987: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 298a: ldc_w 22.0
      // 298d: ldc -16.0
      // 298f: ldc 10.0
      // 2991: fconst_2
      // 2992: fconst_2
      // 2993: fconst_2
      // 2994: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2997: dup
      // 2998: fconst_0
      // 2999: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 299c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 299f: bipush 0
      // 29a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29a3: bipush 44
      // 29a5: bipush 29
      // 29a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29ad: ldc 6.0
      // 29af: ldc_w -46.0
      // 29b2: ldc 8.0
      // 29b4: fconst_2
      // 29b5: fconst_2
      // 29b6: ldc 8.0
      // 29b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 29bb: dup
      // 29bc: fconst_0
      // 29bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 29c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29c3: bipush 0
      // 29c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29c7: bipush 44
      // 29c9: bipush 29
      // 29cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29d1: ldc 4.0
      // 29d3: ldc_w -44.0
      // 29d6: ldc 8.0
      // 29d8: ldc 8.0
      // 29da: fconst_2
      // 29db: fconst_2
      // 29dc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 29df: dup
      // 29e0: fconst_0
      // 29e1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 29e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29e7: bipush 0
      // 29e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29eb: bipush 44
      // 29ed: bipush 29
      // 29ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29f2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29f5: ldc_w 26.0
      // 29f8: ldc -30.0
      // 29fa: ldc 8.0
      // 29fc: fconst_2
      // 29fd: fconst_2
      // 29fe: ldc 10.0
      // 2a00: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a03: dup
      // 2a04: fconst_0
      // 2a05: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a08: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a0b: bipush 0
      // 2a0c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a0f: bipush 44
      // 2a11: bipush 29
      // 2a13: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a16: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a19: ldc_w 26.0
      // 2a1c: ldc -28.0
      // 2a1e: ldc 8.0
      // 2a20: ldc 4.0
      // 2a22: fconst_2
      // 2a23: ldc 10.0
      // 2a25: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a28: dup
      // 2a29: fconst_0
      // 2a2a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a2d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a30: bipush 0
      // 2a31: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a34: bipush 44
      // 2a36: bipush 29
      // 2a38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a3e: ldc 24.0
      // 2a40: ldc -24.0
      // 2a42: ldc 6.0
      // 2a44: fconst_2
      // 2a45: fconst_2
      // 2a46: fconst_2
      // 2a47: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a4a: dup
      // 2a4b: fconst_0
      // 2a4c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a52: bipush 0
      // 2a53: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a56: bipush 44
      // 2a58: bipush 29
      // 2a5a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a5d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a60: ldc 24.0
      // 2a62: ldc -26.0
      // 2a64: ldc 4.0
      // 2a66: fconst_2
      // 2a67: fconst_2
      // 2a68: ldc 4.0
      // 2a6a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a6d: dup
      // 2a6e: fconst_0
      // 2a6f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a72: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a75: bipush 0
      // 2a76: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a79: bipush 44
      // 2a7b: bipush 29
      // 2a7d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a80: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a83: ldc 24.0
      // 2a85: ldc -28.0
      // 2a87: fconst_2
      // 2a88: fconst_2
      // 2a89: fconst_2
      // 2a8a: ldc 4.0
      // 2a8c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a8f: dup
      // 2a90: fconst_0
      // 2a91: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a97: bipush 0
      // 2a98: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a9b: ldc -27.0
      // 2a9d: ldc -6.0
      // 2a9f: ldc -15.0
      // 2aa1: fconst_0
      // 2aa2: ldc_w -1.5708
      // 2aa5: ldc_w 1.5708
      // 2aa8: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 2aab: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 2aae: pop
      // 2aaf: aload 0
      // 2ab0: ldc_w "growth14"
      // 2ab3: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ab6: bipush 44
      // 2ab8: bipush 29
      // 2aba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2abd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ac0: ldc 12.0
      // 2ac2: ldc -38.0
      // 2ac4: ldc_w 28.0
      // 2ac7: fconst_2
      // 2ac8: ldc 6.0
      // 2aca: fconst_2
      // 2acb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ace: dup
      // 2acf: fconst_0
      // 2ad0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ad3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ad6: bipush 0
      // 2ad7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ada: bipush 44
      // 2adc: bipush 29
      // 2ade: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ae1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ae4: ldc 18.0
      // 2ae6: ldc -32.0
      // 2ae8: ldc_w 28.0
      // 2aeb: fconst_2
      // 2aec: fconst_2
      // 2aed: fconst_2
      // 2aee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2af1: dup
      // 2af2: fconst_0
      // 2af3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2af6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2af9: bipush 0
      // 2afa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2afd: bipush 44
      // 2aff: bipush 29
      // 2b01: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b07: ldc 6.0
      // 2b09: ldc -32.0
      // 2b0b: ldc_w 28.0
      // 2b0e: fconst_2
      // 2b0f: fconst_2
      // 2b10: fconst_2
      // 2b11: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b14: dup
      // 2b15: fconst_0
      // 2b16: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2b19: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b1c: bipush 0
      // 2b1d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b20: bipush 44
      // 2b22: bipush 29
      // 2b24: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b2a: ldc 12.0
      // 2b2c: ldc -32.0
      // 2b2e: ldc_w 28.0
      // 2b31: ldc 4.0
      // 2b33: ldc 4.0
      // 2b35: fconst_2
      // 2b36: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b39: dup
      // 2b3a: fconst_0
      // 2b3b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2b3e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b41: bipush 0
      // 2b42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b45: bipush 44
      // 2b47: bipush 29
      // 2b49: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b4c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b4f: ldc 8.0
      // 2b51: ldc -32.0
      // 2b53: ldc_w 28.0
      // 2b56: fconst_2
      // 2b57: ldc 4.0
      // 2b59: fconst_2
      // 2b5a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b5d: dup
      // 2b5e: fconst_0
      // 2b5f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2b62: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b65: bipush 0
      // 2b66: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b69: bipush 44
      // 2b6b: bipush 29
      // 2b6d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b73: ldc 4.0
      // 2b75: ldc -30.0
      // 2b77: ldc_w 28.0
      // 2b7a: fconst_2
      // 2b7b: fconst_2
      // 2b7c: fconst_2
      // 2b7d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b80: dup
      // 2b81: fconst_0
      // 2b82: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2b85: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b88: bipush 0
      // 2b89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b8c: bipush 44
      // 2b8e: bipush 29
      // 2b90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b93: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b96: ldc 16.0
      // 2b98: ldc -30.0
      // 2b9a: ldc_w 28.0
      // 2b9d: ldc 4.0
      // 2b9f: ldc 4.0
      // 2ba1: fconst_2
      // 2ba2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ba5: dup
      // 2ba6: fconst_0
      // 2ba7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2baa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bad: bipush 0
      // 2bae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bb1: bipush 44
      // 2bb3: bipush 29
      // 2bb5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bb8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bbb: ldc 10.0
      // 2bbd: ldc -32.0
      // 2bbf: ldc_w 28.0
      // 2bc2: fconst_2
      // 2bc3: ldc 6.0
      // 2bc5: fconst_2
      // 2bc6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2bc9: dup
      // 2bca: fconst_0
      // 2bcb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2bce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bd1: bipush 0
      // 2bd2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bd5: bipush 44
      // 2bd7: bipush 29
      // 2bd9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bdc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bdf: ldc 10.0
      // 2be1: ldc -26.0
      // 2be3: ldc_w 28.0
      // 2be6: ldc 4.0
      // 2be8: ldc 4.0
      // 2bea: fconst_2
      // 2beb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2bee: dup
      // 2bef: fconst_0
      // 2bf0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2bf3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bf6: bipush 0
      // 2bf7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bfa: bipush 44
      // 2bfc: bipush 29
      // 2bfe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c01: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c04: ldc 6.0
      // 2c06: ldc -24.0
      // 2c08: ldc_w 28.0
      // 2c0b: fconst_2
      // 2c0c: fconst_2
      // 2c0d: fconst_2
      // 2c0e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2c11: dup
      // 2c12: fconst_0
      // 2c13: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2c16: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c19: bipush 0
      // 2c1a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c1d: bipush 44
      // 2c1f: bipush 29
      // 2c21: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c24: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c27: ldc_w 14.0
      // 2c2a: ldc -26.0
      // 2c2c: ldc_w 28.0
      // 2c2f: ldc 4.0
      // 2c31: ldc 6.0
      // 2c33: fconst_2
      // 2c34: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2c37: dup
      // 2c38: fconst_0
      // 2c39: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2c3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c3f: bipush 0
      // 2c40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c43: bipush 44
      // 2c45: bipush 29
      // 2c47: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c4a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c4d: ldc 10.0
      // 2c4f: ldc -38.0
      // 2c51: ldc_w 26.0
      // 2c54: ldc 4.0
      // 2c56: fconst_2
      // 2c57: fconst_2
      // 2c58: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2c5b: dup
      // 2c5c: fconst_0
      // 2c5d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2c60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c63: bipush 0
      // 2c64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c67: bipush 44
      // 2c69: bipush 29
      // 2c6b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c6e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c71: ldc 4.0
      // 2c73: ldc -38.0
      // 2c75: ldc_w 26.0
      // 2c78: fconst_2
      // 2c79: fconst_2
      // 2c7a: fconst_2
      // 2c7b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2c7e: dup
      // 2c7f: fconst_0
      // 2c80: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2c83: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c86: bipush 0
      // 2c87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c8a: bipush 44
      // 2c8c: bipush 29
      // 2c8e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c91: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c94: ldc 10.0
      // 2c96: ldc -36.0
      // 2c98: ldc_w 26.0
      // 2c9b: fconst_2
      // 2c9c: fconst_2
      // 2c9d: ldc 4.0
      // 2c9f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ca2: dup
      // 2ca3: fconst_0
      // 2ca4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ca7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2caa: bipush 0
      // 2cab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cae: bipush 44
      // 2cb0: bipush 29
      // 2cb2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cb5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cb8: ldc_w 14.0
      // 2cbb: ldc -36.0
      // 2cbd: ldc_w 26.0
      // 2cc0: fconst_2
      // 2cc1: ldc 4.0
      // 2cc3: fconst_2
      // 2cc4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2cc7: dup
      // 2cc8: fconst_0
      // 2cc9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ccc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ccf: bipush 0
      // 2cd0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cd3: bipush 44
      // 2cd5: bipush 29
      // 2cd7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cda: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cdd: ldc 6.0
      // 2cdf: ldc -34.0
      // 2ce1: ldc_w 26.0
      // 2ce4: ldc 4.0
      // 2ce6: fconst_2
      // 2ce7: fconst_2
      // 2ce8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ceb: dup
      // 2cec: fconst_0
      // 2ced: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2cf0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cf3: bipush 0
      // 2cf4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cf7: bipush 44
      // 2cf9: bipush 29
      // 2cfb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cfe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d01: ldc 16.0
      // 2d03: ldc -32.0
      // 2d05: ldc_w 26.0
      // 2d08: fconst_2
      // 2d09: fconst_2
      // 2d0a: ldc 4.0
      // 2d0c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d0f: dup
      // 2d10: fconst_0
      // 2d11: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2d14: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d17: bipush 0
      // 2d18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d1b: bipush 44
      // 2d1d: bipush 29
      // 2d1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d22: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d25: ldc 4.0
      // 2d27: ldc -32.0
      // 2d29: ldc_w 26.0
      // 2d2c: fconst_2
      // 2d2d: fconst_2
      // 2d2e: fconst_2
      // 2d2f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d32: dup
      // 2d33: fconst_0
      // 2d34: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2d37: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d3a: bipush 0
      // 2d3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d3e: bipush 44
      // 2d40: bipush 29
      // 2d42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d45: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d48: ldc_w 20.0
      // 2d4b: ldc -30.0
      // 2d4d: ldc_w 26.0
      // 2d50: fconst_2
      // 2d51: ldc 4.0
      // 2d53: fconst_2
      // 2d54: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d57: dup
      // 2d58: fconst_0
      // 2d59: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2d5c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d5f: bipush 0
      // 2d60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d63: bipush 44
      // 2d65: bipush 29
      // 2d67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d6a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d6d: fconst_2
      // 2d6e: ldc -28.0
      // 2d70: ldc_w 26.0
      // 2d73: fconst_2
      // 2d74: fconst_2
      // 2d75: fconst_2
      // 2d76: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d79: dup
      // 2d7a: fconst_0
      // 2d7b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2d7e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d81: bipush 0
      // 2d82: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d85: bipush 44
      // 2d87: bipush 29
      // 2d89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d8c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d8f: ldc 4.0
      // 2d91: ldc -26.0
      // 2d93: ldc_w 26.0
      // 2d96: fconst_2
      // 2d97: fconst_2
      // 2d98: ldc 4.0
      // 2d9a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d9d: dup
      // 2d9e: fconst_0
      // 2d9f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2da2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2da5: bipush 0
      // 2da6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2da9: bipush 44
      // 2dab: bipush 29
      // 2dad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2db0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2db3: ldc_w 20.0
      // 2db6: ldc -24.0
      // 2db8: ldc_w 26.0
      // 2dbb: fconst_2
      // 2dbc: fconst_2
      // 2dbd: fconst_2
      // 2dbe: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2dc1: dup
      // 2dc2: fconst_0
      // 2dc3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2dc6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2dc9: bipush 0
      // 2dca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2dcd: bipush 44
      // 2dcf: bipush 29
      // 2dd1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2dd4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2dd7: ldc 18.0
      // 2dd9: ldc -30.0
      // 2ddb: ldc_w 26.0
      // 2dde: fconst_2
      // 2ddf: ldc 10.0
      // 2de1: fconst_2
      // 2de2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2de5: dup
      // 2de6: fconst_0
      // 2de7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2dea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ded: bipush 0
      // 2dee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2df1: bipush 44
      // 2df3: bipush 29
      // 2df5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2df8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2dfb: ldc 16.0
      // 2dfd: ldc -20.0
      // 2dff: ldc_w 26.0
      // 2e02: ldc 6.0
      // 2e04: fconst_2
      // 2e05: fconst_2
      // 2e06: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e09: dup
      // 2e0a: fconst_0
      // 2e0b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e0e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e11: bipush 0
      // 2e12: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e15: bipush 44
      // 2e17: bipush 29
      // 2e19: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e1f: ldc 18.0
      // 2e21: ldc -12.0
      // 2e23: ldc_w 26.0
      // 2e26: fconst_2
      // 2e27: fconst_2
      // 2e28: fconst_2
      // 2e29: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e2c: dup
      // 2e2d: fconst_0
      // 2e2e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e31: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e34: bipush 0
      // 2e35: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e38: bipush 44
      // 2e3a: bipush 29
      // 2e3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e42: ldc_w 14.0
      // 2e45: ldc -12.0
      // 2e47: ldc_w 26.0
      // 2e4a: ldc 4.0
      // 2e4c: ldc 4.0
      // 2e4e: fconst_2
      // 2e4f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e52: dup
      // 2e53: fconst_0
      // 2e54: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e57: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e5a: bipush 0
      // 2e5b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e5e: bipush 44
      // 2e60: bipush 29
      // 2e62: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e65: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e68: ldc 8.0
      // 2e6a: ldc -10.0
      // 2e6c: ldc_w 26.0
      // 2e6f: fconst_2
      // 2e70: fconst_2
      // 2e71: fconst_2
      // 2e72: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e75: dup
      // 2e76: fconst_0
      // 2e77: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e7a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e7d: bipush 0
      // 2e7e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e81: bipush 44
      // 2e83: bipush 29
      // 2e85: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e88: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e8b: ldc 10.0
      // 2e8d: ldc -10.0
      // 2e8f: ldc_w 26.0
      // 2e92: ldc 4.0
      // 2e94: ldc 4.0
      // 2e96: fconst_2
      // 2e97: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e9a: dup
      // 2e9b: fconst_0
      // 2e9c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e9f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ea2: bipush 0
      // 2ea3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ea6: bipush 44
      // 2ea8: bipush 29
      // 2eaa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ead: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2eb0: ldc 6.0
      // 2eb2: ldc -8.0
      // 2eb4: ldc_w 26.0
      // 2eb7: fconst_2
      // 2eb8: fconst_2
      // 2eb9: fconst_2
      // 2eba: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ebd: dup
      // 2ebe: fconst_0
      // 2ebf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ec2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ec5: bipush 0
      // 2ec6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ec9: bipush 44
      // 2ecb: bipush 29
      // 2ecd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ed0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ed3: ldc_w 14.0
      // 2ed6: ldc -38.0
      // 2ed8: ldc 24.0
      // 2eda: fconst_2
      // 2edb: fconst_2
      // 2edc: ldc 6.0
      // 2ede: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ee1: dup
      // 2ee2: fconst_0
      // 2ee3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ee6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ee9: bipush 0
      // 2eea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2eed: bipush 44
      // 2eef: bipush 29
      // 2ef1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ef4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ef7: ldc 16.0
      // 2ef9: ldc -36.0
      // 2efb: ldc 24.0
      // 2efd: fconst_2
      // 2efe: fconst_2
      // 2eff: ldc 6.0
      // 2f01: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f04: dup
      // 2f05: fconst_0
      // 2f06: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f09: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f0c: bipush 0
      // 2f0d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f10: bipush 44
      // 2f12: bipush 29
      // 2f14: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f17: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f1a: ldc 12.0
      // 2f1c: ldc -38.0
      // 2f1e: ldc 24.0
      // 2f20: fconst_2
      // 2f21: ldc 4.0
      // 2f23: fconst_2
      // 2f24: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f27: dup
      // 2f28: fconst_0
      // 2f29: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f2c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f2f: bipush 0
      // 2f30: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f33: bipush 44
      // 2f35: bipush 29
      // 2f37: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f3a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f3d: ldc 6.0
      // 2f3f: ldc -36.0
      // 2f41: ldc 24.0
      // 2f43: fconst_2
      // 2f44: fconst_2
      // 2f45: ldc 4.0
      // 2f47: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f4a: dup
      // 2f4b: fconst_0
      // 2f4c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f52: bipush 0
      // 2f53: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f56: bipush 44
      // 2f58: bipush 29
      // 2f5a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f5d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f60: fconst_2
      // 2f61: ldc -32.0
      // 2f63: ldc 24.0
      // 2f65: fconst_2
      // 2f66: ldc 4.0
      // 2f68: ldc 4.0
      // 2f6a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f6d: dup
      // 2f6e: fconst_0
      // 2f6f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f72: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f75: bipush 0
      // 2f76: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f79: bipush 44
      // 2f7b: bipush 29
      // 2f7d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f80: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f83: ldc_w 22.0
      // 2f86: ldc -28.0
      // 2f88: ldc 24.0
      // 2f8a: fconst_2
      // 2f8b: fconst_2
      // 2f8c: ldc 4.0
      // 2f8e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f91: dup
      // 2f92: fconst_0
      // 2f93: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f96: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f99: bipush 0
      // 2f9a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f9d: bipush 44
      // 2f9f: bipush 29
      // 2fa1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fa4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fa7: ldc_w 20.0
      // 2faa: ldc -26.0
      // 2fac: ldc 24.0
      // 2fae: fconst_2
      // 2faf: fconst_2
      // 2fb0: ldc 4.0
      // 2fb2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2fb5: dup
      // 2fb6: fconst_0
      // 2fb7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2fba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fbd: bipush 0
      // 2fbe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fc1: bipush 44
      // 2fc3: bipush 29
      // 2fc5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fc8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fcb: ldc 4.0
      // 2fcd: ldc -24.0
      // 2fcf: ldc 24.0
      // 2fd1: fconst_2
      // 2fd2: fconst_2
      // 2fd3: fconst_2
      // 2fd4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2fd7: dup
      // 2fd8: fconst_0
      // 2fd9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2fdc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fdf: bipush 0
      // 2fe0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fe3: bipush 44
      // 2fe5: bipush 29
      // 2fe7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fed: ldc 24.0
      // 2fef: ldc -24.0
      // 2ff1: ldc 24.0
      // 2ff3: fconst_2
      // 2ff4: ldc 4.0
      // 2ff6: fconst_2
      // 2ff7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ffa: dup
      // 2ffb: fconst_0
      // 2ffc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2fff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3002: bipush 0
      // 3003: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3006: bipush 44
      // 3008: bipush 29
      // 300a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 300d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3010: ldc_w 20.0
      // 3013: ldc -22.0
      // 3015: ldc 24.0
      // 3017: fconst_2
      // 3018: fconst_2
      // 3019: fconst_2
      // 301a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 301d: dup
      // 301e: fconst_0
      // 301f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3022: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3025: bipush 0
      // 3026: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3029: bipush 44
      // 302b: bipush 29
      // 302d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3030: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3033: ldc_w 22.0
      // 3036: ldc -24.0
      // 3038: ldc 24.0
      // 303a: fconst_2
      // 303b: ldc 8.0
      // 303d: fconst_2
      // 303e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3041: dup
      // 3042: fconst_0
      // 3043: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3046: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3049: bipush 0
      // 304a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 304d: bipush 44
      // 304f: bipush 29
      // 3051: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3054: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3057: ldc 4.0
      // 3059: ldc -16.0
      // 305b: ldc 24.0
      // 305d: ldc_w 20.0
      // 3060: fconst_2
      // 3061: fconst_2
      // 3062: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3065: dup
      // 3066: fconst_0
      // 3067: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 306a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 306d: bipush 0
      // 306e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3071: bipush 44
      // 3073: bipush 29
      // 3075: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3078: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 307b: ldc 16.0
      // 307d: ldc -38.0
      // 307f: ldc_w 22.0
      // 3082: fconst_2
      // 3083: fconst_2
      // 3084: fconst_2
      // 3085: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3088: dup
      // 3089: fconst_0
      // 308a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 308d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3090: bipush 0
      // 3091: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3094: bipush 44
      // 3096: bipush 29
      // 3098: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 309b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 309e: ldc_w 14.0
      // 30a1: ldc -36.0
      // 30a3: ldc_w 22.0
      // 30a6: ldc 4.0
      // 30a8: fconst_2
      // 30a9: fconst_2
      // 30aa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 30ad: dup
      // 30ae: fconst_0
      // 30af: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 30b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30b5: bipush 0
      // 30b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30b9: bipush 44
      // 30bb: bipush 29
      // 30bd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30c3: ldc 8.0
      // 30c5: ldc -36.0
      // 30c7: ldc_w 22.0
      // 30ca: fconst_2
      // 30cb: fconst_2
      // 30cc: ldc 6.0
      // 30ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 30d1: dup
      // 30d2: fconst_0
      // 30d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 30d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30d9: bipush 0
      // 30da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30dd: bipush 44
      // 30df: bipush 29
      // 30e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30e7: ldc 18.0
      // 30e9: ldc -34.0
      // 30eb: ldc_w 22.0
      // 30ee: fconst_2
      // 30ef: fconst_2
      // 30f0: ldc 8.0
      // 30f2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 30f5: dup
      // 30f6: fconst_0
      // 30f7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 30fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30fd: bipush 0
      // 30fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3101: bipush 44
      // 3103: bipush 29
      // 3105: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3108: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 310b: ldc 12.0
      // 310d: ldc -34.0
      // 310f: ldc_w 22.0
      // 3112: ldc 4.0
      // 3114: fconst_2
      // 3115: fconst_2
      // 3116: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3119: dup
      // 311a: fconst_0
      // 311b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 311e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3121: bipush 0
      // 3122: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3125: bipush 44
      // 3127: bipush 29
      // 3129: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 312c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 312f: ldc_w 20.0
      // 3132: ldc -32.0
      // 3134: ldc_w 22.0
      // 3137: fconst_2
      // 3138: fconst_2
      // 3139: ldc 6.0
      // 313b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 313e: dup
      // 313f: fconst_0
      // 3140: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3143: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3146: bipush 0
      // 3147: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 314a: bipush 44
      // 314c: bipush 29
      // 314e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3151: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3154: ldc_w 22.0
      // 3157: ldc -30.0
      // 3159: ldc_w 22.0
      // 315c: fconst_2
      // 315d: fconst_2
      // 315e: ldc 6.0
      // 3160: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3163: dup
      // 3164: fconst_0
      // 3165: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3168: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 316b: bipush 0
      // 316c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 316f: bipush 44
      // 3171: bipush 29
      // 3173: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3176: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3179: fconst_2
      // 317a: ldc -30.0
      // 317c: ldc_w 22.0
      // 317f: ldc 4.0
      // 3181: fconst_2
      // 3182: fconst_2
      // 3183: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3186: dup
      // 3187: fconst_0
      // 3188: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 318b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 318e: bipush 0
      // 318f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3192: bipush 44
      // 3194: bipush 29
      // 3196: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3199: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 319c: ldc 24.0
      // 319e: ldc -28.0
      // 31a0: ldc_w 22.0
      // 31a3: fconst_2
      // 31a4: fconst_2
      // 31a5: ldc 6.0
      // 31a7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 31aa: dup
      // 31ab: fconst_0
      // 31ac: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 31af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31b2: bipush 0
      // 31b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31b6: bipush 44
      // 31b8: bipush 29
      // 31ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31bd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31c0: ldc_w 22.0
      // 31c3: ldc -26.0
      // 31c5: ldc_w 22.0
      // 31c8: fconst_2
      // 31c9: fconst_2
      // 31ca: fconst_2
      // 31cb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 31ce: dup
      // 31cf: fconst_0
      // 31d0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 31d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31d6: bipush 0
      // 31d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31da: bipush 44
      // 31dc: bipush 29
      // 31de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31e4: ldc_w 28.0
      // 31e7: ldc -24.0
      // 31e9: ldc_w 22.0
      // 31ec: fconst_2
      // 31ed: fconst_2
      // 31ee: fconst_2
      // 31ef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 31f2: dup
      // 31f3: fconst_0
      // 31f4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 31f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31fa: bipush 0
      // 31fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31fe: bipush 44
      // 3200: bipush 29
      // 3202: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3205: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3208: fconst_2
      // 3209: ldc -26.0
      // 320b: ldc_w 22.0
      // 320e: fconst_2
      // 320f: ldc 4.0
      // 3211: ldc 4.0
      // 3213: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3216: dup
      // 3217: fconst_0
      // 3218: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 321b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 321e: bipush 0
      // 321f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3222: bipush 44
      // 3224: bipush 29
      // 3226: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3229: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 322c: ldc 10.0
      // 322e: ldc -22.0
      // 3230: ldc_w 22.0
      // 3233: fconst_2
      // 3234: fconst_2
      // 3235: ldc 8.0
      // 3237: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 323a: dup
      // 323b: fconst_0
      // 323c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 323f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3242: bipush 0
      // 3243: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3246: bipush 44
      // 3248: bipush 29
      // 324a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 324d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3250: ldc_w 26.0
      // 3253: ldc -22.0
      // 3255: ldc_w 22.0
      // 3258: fconst_2
      // 3259: ldc 4.0
      // 325b: fconst_2
      // 325c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 325f: dup
      // 3260: fconst_0
      // 3261: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3264: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3267: bipush 0
      // 3268: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 326b: bipush 44
      // 326d: bipush 29
      // 326f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3272: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3275: ldc 24.0
      // 3277: ldc -20.0
      // 3279: ldc_w 22.0
      // 327c: fconst_2
      // 327d: ldc 4.0
      // 327f: fconst_2
      // 3280: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3283: dup
      // 3284: fconst_0
      // 3285: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3288: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 328b: bipush 0
      // 328c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 328f: bipush 44
      // 3291: bipush 29
      // 3293: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3296: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3299: ldc_w 26.0
      // 329c: ldc -16.0
      // 329e: ldc_w 22.0
      // 32a1: fconst_2
      // 32a2: fconst_2
      // 32a3: fconst_2
      // 32a4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 32a7: dup
      // 32a8: fconst_0
      // 32a9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 32ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32af: bipush 0
      // 32b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32b3: bipush 44
      // 32b5: bipush 29
      // 32b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32bd: ldc_w 20.0
      // 32c0: ldc_w -40.0
      // 32c3: ldc_w 20.0
      // 32c6: fconst_2
      // 32c7: fconst_2
      // 32c8: fconst_2
      // 32c9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 32cc: dup
      // 32cd: fconst_0
      // 32ce: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 32d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32d4: bipush 0
      // 32d5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32d8: bipush 44
      // 32da: bipush 29
      // 32dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32e2: ldc 18.0
      // 32e4: ldc -36.0
      // 32e6: ldc_w 20.0
      // 32e9: fconst_2
      // 32ea: fconst_2
      // 32eb: fconst_2
      // 32ec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 32ef: dup
      // 32f0: fconst_0
      // 32f1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 32f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32f7: bipush 0
      // 32f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32fb: bipush 44
      // 32fd: bipush 29
      // 32ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3302: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3305: ldc 8.0
      // 3307: ldc -32.0
      // 3309: ldc_w 20.0
      // 330c: ldc 10.0
      // 330e: fconst_2
      // 330f: fconst_2
      // 3310: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3313: dup
      // 3314: fconst_0
      // 3315: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3318: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 331b: bipush 0
      // 331c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 331f: bipush 44
      // 3321: bipush 29
      // 3323: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3326: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3329: ldc 12.0
      // 332b: ldc -22.0
      // 332d: ldc_w 20.0
      // 3330: fconst_2
      // 3331: fconst_2
      // 3332: ldc 10.0
      // 3334: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3337: dup
      // 3338: fconst_0
      // 3339: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 333c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 333f: bipush 0
      // 3340: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3343: bipush 44
      // 3345: bipush 29
      // 3347: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 334a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 334d: ldc_w 28.0
      // 3350: ldc -22.0
      // 3352: ldc_w 20.0
      // 3355: fconst_2
      // 3356: ldc 4.0
      // 3358: fconst_2
      // 3359: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 335c: dup
      // 335d: fconst_0
      // 335e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3361: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3364: bipush 0
      // 3365: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3368: bipush 44
      // 336a: bipush 29
      // 336c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 336f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3372: ldc_w 26.0
      // 3375: ldc -18.0
      // 3377: ldc_w 20.0
      // 337a: ldc 4.0
      // 337c: ldc 4.0
      // 337e: fconst_2
      // 337f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3382: dup
      // 3383: fconst_0
      // 3384: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3387: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 338a: bipush 0
      // 338b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 338e: bipush 44
      // 3390: bipush 29
      // 3392: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3395: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3398: fconst_2
      // 3399: ldc -10.0
      // 339b: ldc_w 20.0
      // 339e: fconst_2
      // 339f: fconst_2
      // 33a0: ldc 4.0
      // 33a2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 33a5: dup
      // 33a6: fconst_0
      // 33a7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 33aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33ad: bipush 0
      // 33ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33b1: bipush 44
      // 33b3: bipush 29
      // 33b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33bb: ldc 6.0
      // 33bd: ldc -8.0
      // 33bf: ldc_w 20.0
      // 33c2: fconst_2
      // 33c3: fconst_2
      // 33c4: fconst_2
      // 33c5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 33c8: dup
      // 33c9: fconst_0
      // 33ca: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 33cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33d0: bipush 0
      // 33d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33d4: bipush 44
      // 33d6: bipush 29
      // 33d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33de: ldc 8.0
      // 33e0: ldc -6.0
      // 33e2: ldc_w 20.0
      // 33e5: ldc 4.0
      // 33e7: fconst_2
      // 33e8: fconst_2
      // 33e9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 33ec: dup
      // 33ed: fconst_0
      // 33ee: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 33f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33f4: bipush 0
      // 33f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33f8: bipush 44
      // 33fa: bipush 29
      // 33fc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3402: fconst_2
      // 3403: ldc -6.0
      // 3405: ldc_w 20.0
      // 3408: fconst_2
      // 3409: fconst_2
      // 340a: ldc 4.0
      // 340c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 340f: dup
      // 3410: fconst_0
      // 3411: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3414: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3417: bipush 0
      // 3418: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 341b: bipush 44
      // 341d: bipush 29
      // 341f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3422: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3425: ldc_w 22.0
      // 3428: ldc -38.0
      // 342a: ldc 18.0
      // 342c: fconst_2
      // 342d: fconst_2
      // 342e: fconst_2
      // 342f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3432: dup
      // 3433: fconst_0
      // 3434: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3437: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 343a: bipush 0
      // 343b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 343e: bipush 44
      // 3440: bipush 29
      // 3442: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3445: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3448: fconst_2
      // 3449: ldc -36.0
      // 344b: ldc 18.0
      // 344d: fconst_2
      // 344e: fconst_2
      // 344f: fconst_2
      // 3450: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3453: dup
      // 3454: fconst_0
      // 3455: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3458: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 345b: bipush 0
      // 345c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 345f: bipush 44
      // 3461: bipush 29
      // 3463: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3466: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3469: ldc 10.0
      // 346b: ldc -34.0
      // 346d: ldc 18.0
      // 346f: fconst_2
      // 3470: fconst_2
      // 3471: ldc 10.0
      // 3473: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3476: dup
      // 3477: fconst_0
      // 3478: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 347b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 347e: bipush 0
      // 347f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3482: bipush 44
      // 3484: bipush 29
      // 3486: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3489: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 348c: fconst_2
      // 348d: ldc -34.0
      // 348f: ldc 18.0
      // 3491: ldc 4.0
      // 3493: fconst_2
      // 3494: ldc 10.0
      // 3496: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3499: dup
      // 349a: fconst_0
      // 349b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 349e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34a1: bipush 0
      // 34a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34a5: bipush 44
      // 34a7: bipush 29
      // 34a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34af: ldc 18.0
      // 34b1: ldc -32.0
      // 34b3: ldc 18.0
      // 34b5: ldc 4.0
      // 34b7: fconst_2
      // 34b8: ldc 4.0
      // 34ba: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 34bd: dup
      // 34be: fconst_0
      // 34bf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 34c2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34c5: bipush 0
      // 34c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34c9: bipush 44
      // 34cb: bipush 29
      // 34cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34d0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34d3: ldc 6.0
      // 34d5: ldc -32.0
      // 34d7: ldc 18.0
      // 34d9: ldc 10.0
      // 34db: fconst_2
      // 34dc: fconst_2
      // 34dd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 34e0: dup
      // 34e1: fconst_0
      // 34e2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 34e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34e8: bipush 0
      // 34e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34ec: bipush 44
      // 34ee: bipush 29
      // 34f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34f6: fconst_2
      // 34f7: ldc -30.0
      // 34f9: ldc 18.0
      // 34fb: ldc 6.0
      // 34fd: fconst_2
      // 34fe: ldc 4.0
      // 3500: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3503: dup
      // 3504: fconst_0
      // 3505: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3508: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 350b: bipush 0
      // 350c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 350f: bipush 44
      // 3511: bipush 29
      // 3513: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3516: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3519: ldc_w 14.0
      // 351c: ldc -10.0
      // 351e: ldc 18.0
      // 3520: fconst_2
      // 3521: fconst_2
      // 3522: ldc 4.0
      // 3524: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3527: dup
      // 3528: fconst_0
      // 3529: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 352c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 352f: bipush 0
      // 3530: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3533: bipush 44
      // 3535: bipush 29
      // 3537: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 353a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 353d: ldc 4.0
      // 353f: ldc -10.0
      // 3541: ldc 18.0
      // 3543: ldc 4.0
      // 3545: fconst_2
      // 3546: ldc 4.0
      // 3548: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 354b: dup
      // 354c: fconst_0
      // 354d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3550: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3553: bipush 0
      // 3554: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3557: bipush 44
      // 3559: bipush 29
      // 355b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 355e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3561: ldc_w 20.0
      // 3564: ldc -8.0
      // 3566: ldc 18.0
      // 3568: fconst_2
      // 3569: fconst_2
      // 356a: fconst_2
      // 356b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 356e: dup
      // 356f: fconst_0
      // 3570: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3573: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3576: bipush 0
      // 3577: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 357a: bipush 44
      // 357c: bipush 29
      // 357e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3581: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3584: ldc 12.0
      // 3586: ldc -8.0
      // 3588: ldc 18.0
      // 358a: fconst_2
      // 358b: fconst_2
      // 358c: ldc 4.0
      // 358e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3591: dup
      // 3592: fconst_0
      // 3593: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3596: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3599: bipush 0
      // 359a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 359d: bipush 44
      // 359f: bipush 29
      // 35a1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35a7: ldc 8.0
      // 35a9: ldc -10.0
      // 35ab: ldc 18.0
      // 35ad: fconst_2
      // 35ae: ldc 4.0
      // 35b0: ldc 4.0
      // 35b2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 35b5: dup
      // 35b6: fconst_0
      // 35b7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 35ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35bd: bipush 0
      // 35be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35c1: bipush 44
      // 35c3: bipush 29
      // 35c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35cb: ldc 4.0
      // 35cd: ldc -6.0
      // 35cf: ldc 18.0
      // 35d1: ldc 4.0
      // 35d3: fconst_2
      // 35d4: ldc 4.0
      // 35d6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 35d9: dup
      // 35da: fconst_0
      // 35db: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 35de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35e1: bipush 0
      // 35e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35e5: bipush 44
      // 35e7: bipush 29
      // 35e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35ef: ldc 4.0
      // 35f1: ldc -38.0
      // 35f3: ldc 16.0
      // 35f5: fconst_2
      // 35f6: fconst_2
      // 35f7: fconst_2
      // 35f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 35fb: dup
      // 35fc: fconst_0
      // 35fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3600: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3603: bipush 0
      // 3604: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3607: bipush 44
      // 3609: bipush 29
      // 360b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 360e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3611: ldc 6.0
      // 3613: ldc -34.0
      // 3615: ldc 16.0
      // 3617: ldc 4.0
      // 3619: fconst_2
      // 361a: ldc 8.0
      // 361c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 361f: dup
      // 3620: fconst_0
      // 3621: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3624: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3627: bipush 0
      // 3628: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 362b: bipush 44
      // 362d: bipush 29
      // 362f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3632: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3635: ldc_w 30.0
      // 3638: ldc -24.0
      // 363a: ldc 16.0
      // 363c: fconst_2
      // 363d: fconst_2
      // 363e: ldc 4.0
      // 3640: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3643: dup
      // 3644: fconst_0
      // 3645: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3648: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 364b: bipush 0
      // 364c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 364f: bipush 44
      // 3651: bipush 29
      // 3653: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3656: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3659: ldc 16.0
      // 365b: ldc -10.0
      // 365d: ldc 16.0
      // 365f: fconst_2
      // 3660: fconst_2
      // 3661: ldc 6.0
      // 3663: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3666: dup
      // 3667: fconst_0
      // 3668: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 366b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 366e: bipush 0
      // 366f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3672: bipush 44
      // 3674: bipush 29
      // 3676: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3679: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 367c: ldc 12.0
      // 367e: ldc -10.0
      // 3680: ldc 16.0
      // 3682: fconst_2
      // 3683: fconst_2
      // 3684: ldc 6.0
      // 3686: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3689: dup
      // 368a: fconst_0
      // 368b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 368e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3691: bipush 0
      // 3692: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3695: bipush 44
      // 3697: bipush 29
      // 3699: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 369c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 369f: fconst_2
      // 36a0: ldc -10.0
      // 36a2: ldc 16.0
      // 36a4: ldc 8.0
      // 36a6: fconst_2
      // 36a7: fconst_2
      // 36a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 36ab: dup
      // 36ac: fconst_0
      // 36ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 36b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36b3: bipush 0
      // 36b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36b7: bipush 44
      // 36b9: bipush 29
      // 36bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36c1: ldc 18.0
      // 36c3: ldc -8.0
      // 36c5: ldc 16.0
      // 36c7: ldc 6.0
      // 36c9: fconst_2
      // 36ca: fconst_2
      // 36cb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 36ce: dup
      // 36cf: fconst_0
      // 36d0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 36d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36d6: bipush 0
      // 36d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36da: bipush 44
      // 36dc: bipush 29
      // 36de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36e4: ldc_w 14.0
      // 36e7: ldc -10.0
      // 36e9: ldc 16.0
      // 36eb: fconst_2
      // 36ec: ldc 4.0
      // 36ee: fconst_2
      // 36ef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 36f2: dup
      // 36f3: fconst_0
      // 36f4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 36f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36fa: bipush 0
      // 36fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36fe: bipush 44
      // 3700: bipush 29
      // 3702: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3705: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3708: ldc 10.0
      // 370a: ldc -10.0
      // 370c: ldc 16.0
      // 370e: fconst_2
      // 370f: ldc 4.0
      // 3711: ldc 6.0
      // 3713: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3716: dup
      // 3717: fconst_0
      // 3718: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 371b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 371e: bipush 0
      // 371f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3722: bipush 44
      // 3724: bipush 29
      // 3726: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3729: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 372c: fconst_2
      // 372d: ldc -8.0
      // 372f: ldc 16.0
      // 3731: fconst_2
      // 3732: fconst_2
      // 3733: ldc 8.0
      // 3735: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3738: dup
      // 3739: fconst_0
      // 373a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 373d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3740: bipush 0
      // 3741: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3744: bipush 44
      // 3746: bipush 29
      // 3748: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 374b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 374e: ldc 12.0
      // 3750: ldc -6.0
      // 3752: ldc 16.0
      // 3754: ldc 4.0
      // 3756: fconst_2
      // 3757: fconst_2
      // 3758: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 375b: dup
      // 375c: fconst_0
      // 375d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3760: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3763: bipush 0
      // 3764: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3767: bipush 44
      // 3769: bipush 29
      // 376b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 376e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3771: fconst_2
      // 3772: ldc -6.0
      // 3774: ldc 16.0
      // 3776: ldc 8.0
      // 3778: fconst_2
      // 3779: fconst_2
      // 377a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 377d: dup
      // 377e: fconst_0
      // 377f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3782: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3785: bipush 0
      // 3786: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3789: bipush 44
      // 378b: bipush 29
      // 378d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3790: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3793: ldc_w 30.0
      // 3796: ldc -20.0
      // 3798: ldc_w 14.0
      // 379b: fconst_2
      // 379c: fconst_2
      // 379d: fconst_2
      // 379e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 37a1: dup
      // 37a2: fconst_0
      // 37a3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 37a6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37a9: bipush 0
      // 37aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37ad: bipush 44
      // 37af: bipush 29
      // 37b1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37b7: ldc 18.0
      // 37b9: ldc -10.0
      // 37bb: ldc_w 14.0
      // 37be: fconst_2
      // 37bf: fconst_2
      // 37c0: ldc 8.0
      // 37c2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 37c5: dup
      // 37c6: fconst_0
      // 37c7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 37ca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37cd: bipush 0
      // 37ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37d1: bipush 44
      // 37d3: bipush 29
      // 37d5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37db: ldc 16.0
      // 37dd: ldc -8.0
      // 37df: ldc_w 14.0
      // 37e2: ldc 10.0
      // 37e4: fconst_2
      // 37e5: fconst_2
      // 37e6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 37e9: dup
      // 37ea: fconst_0
      // 37eb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 37ee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37f1: bipush 0
      // 37f2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37f5: bipush 44
      // 37f7: bipush 29
      // 37f9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37fc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37ff: ldc_w 14.0
      // 3802: ldc -4.0
      // 3804: ldc_w 14.0
      // 3807: fconst_2
      // 3808: fconst_2
      // 3809: fconst_2
      // 380a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 380d: dup
      // 380e: fconst_0
      // 380f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3812: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3815: bipush 0
      // 3816: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3819: bipush 44
      // 381b: bipush 29
      // 381d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3820: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3823: ldc_w 30.0
      // 3826: ldc -16.0
      // 3828: ldc 12.0
      // 382a: fconst_2
      // 382b: fconst_2
      // 382c: fconst_2
      // 382d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3830: dup
      // 3831: fconst_0
      // 3832: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3835: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3838: bipush 0
      // 3839: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 383c: bipush 44
      // 383e: bipush 29
      // 3840: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3843: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3846: ldc_w 26.0
      // 3849: ldc -24.0
      // 384b: ldc 10.0
      // 384d: fconst_2
      // 384e: fconst_2
      // 384f: fconst_2
      // 3850: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3853: dup
      // 3854: fconst_0
      // 3855: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3858: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 385b: bipush 0
      // 385c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 385f: bipush 44
      // 3861: bipush 29
      // 3863: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3866: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3869: ldc_w 28.0
      // 386c: ldc -22.0
      // 386e: ldc 10.0
      // 3870: fconst_2
      // 3871: fconst_2
      // 3872: ldc 6.0
      // 3874: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3877: dup
      // 3878: fconst_0
      // 3879: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 387c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 387f: bipush 0
      // 3880: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3883: bipush 44
      // 3885: bipush 29
      // 3887: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 388a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 388d: fconst_2
      // 388e: ldc -10.0
      // 3890: ldc 10.0
      // 3892: ldc 4.0
      // 3894: fconst_2
      // 3895: fconst_2
      // 3896: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3899: dup
      // 389a: fconst_0
      // 389b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 389e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38a1: bipush 0
      // 38a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38a5: bipush 44
      // 38a7: bipush 29
      // 38a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38af: ldc 8.0
      // 38b1: ldc -6.0
      // 38b3: ldc 10.0
      // 38b5: fconst_2
      // 38b6: fconst_2
      // 38b7: fconst_2
      // 38b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 38bb: dup
      // 38bc: fconst_0
      // 38bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 38c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38c3: bipush 0
      // 38c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38c7: bipush 44
      // 38c9: bipush 29
      // 38cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38d1: fconst_2
      // 38d2: ldc -6.0
      // 38d4: ldc 10.0
      // 38d6: ldc 4.0
      // 38d8: fconst_2
      // 38d9: fconst_2
      // 38da: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 38dd: dup
      // 38de: fconst_0
      // 38df: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 38e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38e5: bipush 0
      // 38e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38e9: bipush 44
      // 38eb: bipush 29
      // 38ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38f3: ldc 8.0
      // 38f5: ldc_w -46.0
      // 38f8: ldc 8.0
      // 38fa: fconst_2
      // 38fb: fconst_2
      // 38fc: fconst_2
      // 38fd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3900: dup
      // 3901: fconst_0
      // 3902: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3905: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3908: bipush 0
      // 3909: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 390c: bipush 44
      // 390e: bipush 29
      // 3910: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3913: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3916: ldc 6.0
      // 3918: ldc_w -44.0
      // 391b: ldc 8.0
      // 391d: fconst_2
      // 391e: fconst_2
      // 391f: fconst_2
      // 3920: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3923: dup
      // 3924: fconst_0
      // 3925: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3928: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 392b: bipush 0
      // 392c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 392f: bipush 44
      // 3931: bipush 29
      // 3933: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3936: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3939: ldc_w 28.0
      // 393c: ldc -24.0
      // 393e: ldc 8.0
      // 3940: ldc 4.0
      // 3942: fconst_2
      // 3943: fconst_2
      // 3944: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3947: dup
      // 3948: fconst_0
      // 3949: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 394c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 394f: bipush 0
      // 3950: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3953: bipush 44
      // 3955: bipush 29
      // 3957: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 395a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 395d: ldc_w 30.0
      // 3960: ldc -22.0
      // 3962: ldc 8.0
      // 3964: fconst_2
      // 3965: fconst_2
      // 3966: ldc 4.0
      // 3968: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 396b: dup
      // 396c: fconst_0
      // 396d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3970: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3973: bipush 0
      // 3974: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3977: bipush 44
      // 3979: bipush 29
      // 397b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 397e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3981: ldc_w 28.0
      // 3984: ldc -20.0
      // 3986: ldc 8.0
      // 3988: fconst_2
      // 3989: fconst_2
      // 398a: ldc 8.0
      // 398c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 398f: dup
      // 3990: fconst_0
      // 3991: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3994: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3997: bipush 0
      // 3998: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 399b: bipush 44
      // 399d: bipush 29
      // 399f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39a5: ldc 6.0
      // 39a7: ldc -10.0
      // 39a9: ldc 8.0
      // 39ab: ldc 4.0
      // 39ad: fconst_2
      // 39ae: ldc 4.0
      // 39b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 39b3: dup
      // 39b4: fconst_0
      // 39b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 39b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39bb: bipush 0
      // 39bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39bf: bipush 44
      // 39c1: bipush 29
      // 39c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39c9: ldc_w 22.0
      // 39cc: ldc -8.0
      // 39ce: ldc 8.0
      // 39d0: fconst_2
      // 39d1: fconst_2
      // 39d2: fconst_2
      // 39d3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 39d6: dup
      // 39d7: fconst_0
      // 39d8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 39db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39de: bipush 0
      // 39df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39e2: bipush 44
      // 39e4: bipush 29
      // 39e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39ec: ldc 18.0
      // 39ee: ldc -8.0
      // 39f0: ldc 8.0
      // 39f2: fconst_2
      // 39f3: fconst_2
      // 39f4: fconst_2
      // 39f5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 39f8: dup
      // 39f9: fconst_0
      // 39fa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 39fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a00: bipush 0
      // 3a01: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a04: bipush 44
      // 3a06: bipush 29
      // 3a08: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a0b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a0e: ldc 10.0
      // 3a10: ldc -8.0
      // 3a12: ldc 8.0
      // 3a14: fconst_2
      // 3a15: fconst_2
      // 3a16: fconst_2
      // 3a17: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3a1a: dup
      // 3a1b: fconst_0
      // 3a1c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3a1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a22: bipush 0
      // 3a23: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a26: bipush 44
      // 3a28: bipush 29
      // 3a2a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a2d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a30: fconst_2
      // 3a31: ldc -8.0
      // 3a33: ldc 8.0
      // 3a35: ldc 4.0
      // 3a37: fconst_2
      // 3a38: ldc 4.0
      // 3a3a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3a3d: dup
      // 3a3e: fconst_0
      // 3a3f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3a42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a45: bipush 0
      // 3a46: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a49: bipush 44
      // 3a4b: bipush 29
      // 3a4d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a50: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a53: ldc 16.0
      // 3a55: ldc -6.0
      // 3a57: ldc 8.0
      // 3a59: fconst_2
      // 3a5a: fconst_2
      // 3a5b: fconst_2
      // 3a5c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3a5f: dup
      // 3a60: fconst_0
      // 3a61: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3a64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a67: bipush 0
      // 3a68: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a6b: bipush 44
      // 3a6d: bipush 29
      // 3a6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a72: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a75: ldc 6.0
      // 3a77: ldc -6.0
      // 3a79: ldc 8.0
      // 3a7b: fconst_2
      // 3a7c: fconst_2
      // 3a7d: ldc 4.0
      // 3a7f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3a82: dup
      // 3a83: fconst_0
      // 3a84: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3a87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a8a: bipush 0
      // 3a8b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a8e: bipush 44
      // 3a90: bipush 29
      // 3a92: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a95: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a98: ldc 8.0
      // 3a9a: ldc -6.0
      // 3a9c: ldc 8.0
      // 3a9e: fconst_2
      // 3a9f: ldc 4.0
      // 3aa1: fconst_2
      // 3aa2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3aa5: dup
      // 3aa6: fconst_0
      // 3aa7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3aaa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3aad: bipush 0
      // 3aae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ab1: bipush 44
      // 3ab3: bipush 29
      // 3ab5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ab8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3abb: fconst_2
      // 3abc: ldc -4.0
      // 3abe: ldc 8.0
      // 3ac0: ldc 4.0
      // 3ac2: fconst_2
      // 3ac3: ldc 4.0
      // 3ac5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3ac8: dup
      // 3ac9: fconst_0
      // 3aca: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3acd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ad0: bipush 0
      // 3ad1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ad4: bipush 44
      // 3ad6: bipush 29
      // 3ad8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3adb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ade: ldc 10.0
      // 3ae0: ldc_w -46.0
      // 3ae3: ldc 6.0
      // 3ae5: fconst_2
      // 3ae6: fconst_2
      // 3ae7: fconst_2
      // 3ae8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3aeb: dup
      // 3aec: fconst_0
      // 3aed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3af0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3af3: bipush 0
      // 3af4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3af7: bipush 44
      // 3af9: bipush 29
      // 3afb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3afe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b01: ldc 6.0
      // 3b03: ldc_w -42.0
      // 3b06: ldc 6.0
      // 3b08: fconst_2
      // 3b09: fconst_2
      // 3b0a: fconst_2
      // 3b0b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b0e: dup
      // 3b0f: fconst_0
      // 3b10: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3b13: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b16: bipush 0
      // 3b17: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b1a: bipush 44
      // 3b1c: bipush 29
      // 3b1e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b21: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b24: ldc_w 30.0
      // 3b27: ldc -20.0
      // 3b29: ldc 6.0
      // 3b2b: fconst_2
      // 3b2c: fconst_2
      // 3b2d: fconst_2
      // 3b2e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b31: dup
      // 3b32: fconst_0
      // 3b33: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3b36: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b39: bipush 0
      // 3b3a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b3d: bipush 44
      // 3b3f: bipush 29
      // 3b41: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b47: ldc_w 28.0
      // 3b4a: ldc -22.0
      // 3b4c: ldc 6.0
      // 3b4e: fconst_2
      // 3b4f: ldc 6.0
      // 3b51: fconst_2
      // 3b52: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b55: dup
      // 3b56: fconst_0
      // 3b57: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3b5a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b5d: bipush 0
      // 3b5e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b61: bipush 44
      // 3b63: bipush 29
      // 3b65: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b68: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b6b: ldc_w 28.0
      // 3b6e: ldc -8.0
      // 3b70: ldc 6.0
      // 3b72: ldc 4.0
      // 3b74: fconst_2
      // 3b75: fconst_2
      // 3b76: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b79: dup
      // 3b7a: fconst_0
      // 3b7b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3b7e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b81: bipush 0
      // 3b82: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b85: bipush 44
      // 3b87: bipush 29
      // 3b89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b8c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b8f: ldc 6.0
      // 3b91: ldc -10.0
      // 3b93: ldc 6.0
      // 3b95: ldc 4.0
      // 3b97: ldc 4.0
      // 3b99: fconst_2
      // 3b9a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b9d: dup
      // 3b9e: fconst_0
      // 3b9f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3ba2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ba5: bipush 0
      // 3ba6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ba9: bipush 44
      // 3bab: bipush 29
      // 3bad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bb0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bb3: ldc 24.0
      // 3bb5: ldc -8.0
      // 3bb7: ldc 6.0
      // 3bb9: ldc 4.0
      // 3bbb: ldc 4.0
      // 3bbd: fconst_2
      // 3bbe: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3bc1: dup
      // 3bc2: fconst_0
      // 3bc3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3bc6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bc9: bipush 0
      // 3bca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bcd: bipush 44
      // 3bcf: bipush 29
      // 3bd1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bd4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bd7: fconst_2
      // 3bd8: ldc -8.0
      // 3bda: ldc 6.0
      // 3bdc: ldc 4.0
      // 3bde: ldc 4.0
      // 3be0: fconst_2
      // 3be1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3be4: dup
      // 3be5: fconst_0
      // 3be6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3be9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bec: bipush 0
      // 3bed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bf0: bipush 44
      // 3bf2: bipush 29
      // 3bf4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bf7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bfa: ldc 18.0
      // 3bfc: ldc -6.0
      // 3bfe: ldc 6.0
      // 3c00: fconst_2
      // 3c01: ldc 4.0
      // 3c03: ldc 4.0
      // 3c05: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c08: dup
      // 3c09: fconst_0
      // 3c0a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c0d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c10: bipush 0
      // 3c11: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c14: bipush 44
      // 3c16: bipush 29
      // 3c18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c1b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c1e: ldc_w 14.0
      // 3c21: ldc_w -50.0
      // 3c24: ldc 4.0
      // 3c26: fconst_2
      // 3c27: fconst_2
      // 3c28: fconst_2
      // 3c29: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c2c: dup
      // 3c2d: fconst_0
      // 3c2e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c31: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c34: bipush 0
      // 3c35: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c38: bipush 44
      // 3c3a: bipush 29
      // 3c3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c42: ldc 12.0
      // 3c44: ldc_w -48.0
      // 3c47: ldc 4.0
      // 3c49: ldc 4.0
      // 3c4b: fconst_2
      // 3c4c: fconst_2
      // 3c4d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c50: dup
      // 3c51: fconst_0
      // 3c52: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c55: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c58: bipush 0
      // 3c59: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c5c: bipush 44
      // 3c5e: bipush 29
      // 3c60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c63: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c66: ldc 8.0
      // 3c68: ldc_w -44.0
      // 3c6b: ldc 4.0
      // 3c6d: fconst_2
      // 3c6e: fconst_2
      // 3c6f: ldc 4.0
      // 3c71: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c74: dup
      // 3c75: fconst_0
      // 3c76: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c79: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c7c: bipush 0
      // 3c7d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c80: bipush 44
      // 3c82: bipush 29
      // 3c84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c8a: ldc 8.0
      // 3c8c: ldc_w -42.0
      // 3c8f: ldc 4.0
      // 3c91: ldc 4.0
      // 3c93: fconst_2
      // 3c94: fconst_2
      // 3c95: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c98: dup
      // 3c99: fconst_0
      // 3c9a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c9d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ca0: bipush 0
      // 3ca1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ca4: bipush 44
      // 3ca6: bipush 29
      // 3ca8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cae: ldc_w 30.0
      // 3cb1: ldc -18.0
      // 3cb3: ldc 4.0
      // 3cb5: fconst_2
      // 3cb6: fconst_2
      // 3cb7: ldc 4.0
      // 3cb9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3cbc: dup
      // 3cbd: fconst_0
      // 3cbe: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3cc1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cc4: bipush 0
      // 3cc5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cc8: bipush 44
      // 3cca: bipush 29
      // 3ccc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ccf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cd2: ldc_w 20.0
      // 3cd5: ldc -8.0
      // 3cd7: ldc 4.0
      // 3cd9: ldc 4.0
      // 3cdb: fconst_2
      // 3cdc: fconst_2
      // 3cdd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3ce0: dup
      // 3ce1: fconst_0
      // 3ce2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3ce5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ce8: bipush 0
      // 3ce9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cec: bipush 44
      // 3cee: bipush 29
      // 3cf0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cf3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cf6: ldc_w 26.0
      // 3cf9: ldc -8.0
      // 3cfb: ldc 4.0
      // 3cfd: ldc 6.0
      // 3cff: ldc 4.0
      // 3d01: fconst_2
      // 3d02: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d05: dup
      // 3d06: fconst_0
      // 3d07: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d0a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d0d: bipush 0
      // 3d0e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d11: bipush 44
      // 3d13: bipush 29
      // 3d15: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d1b: ldc 10.0
      // 3d1d: ldc -6.0
      // 3d1f: ldc 4.0
      // 3d21: fconst_2
      // 3d22: fconst_2
      // 3d23: ldc 6.0
      // 3d25: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d28: dup
      // 3d29: fconst_0
      // 3d2a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d2d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d30: bipush 0
      // 3d31: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d34: bipush 44
      // 3d36: bipush 29
      // 3d38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d3e: ldc_w 20.0
      // 3d41: ldc -6.0
      // 3d43: ldc 4.0
      // 3d45: fconst_2
      // 3d46: ldc 4.0
      // 3d48: ldc 4.0
      // 3d4a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d4d: dup
      // 3d4e: fconst_0
      // 3d4f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d52: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d55: bipush 0
      // 3d56: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d59: bipush 44
      // 3d5b: bipush 29
      // 3d5d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d63: ldc 10.0
      // 3d65: ldc -4.0
      // 3d67: ldc 4.0
      // 3d69: ldc 4.0
      // 3d6b: fconst_2
      // 3d6c: ldc 6.0
      // 3d6e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d71: dup
      // 3d72: fconst_0
      // 3d73: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d76: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d79: bipush 0
      // 3d7a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d7d: bipush 44
      // 3d7f: bipush 29
      // 3d81: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d87: ldc 12.0
      // 3d89: ldc_w -46.0
      // 3d8c: fconst_2
      // 3d8d: fconst_2
      // 3d8e: fconst_2
      // 3d8f: fconst_2
      // 3d90: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d93: dup
      // 3d94: fconst_0
      // 3d95: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d98: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d9b: bipush 0
      // 3d9c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d9f: bipush 44
      // 3da1: bipush 29
      // 3da3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3da6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3da9: ldc_w 14.0
      // 3dac: ldc_w -46.0
      // 3daf: fconst_2
      // 3db0: fconst_2
      // 3db1: ldc 4.0
      // 3db3: fconst_2
      // 3db4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3db7: dup
      // 3db8: fconst_0
      // 3db9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3dbc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3dbf: bipush 0
      // 3dc0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3dc3: bipush 44
      // 3dc5: bipush 29
      // 3dc7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3dca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3dcd: ldc 10.0
      // 3dcf: ldc_w -44.0
      // 3dd2: fconst_2
      // 3dd3: ldc 4.0
      // 3dd5: ldc 4.0
      // 3dd7: fconst_2
      // 3dd8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3ddb: dup
      // 3ddc: fconst_0
      // 3ddd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3de0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3de3: bipush 0
      // 3de4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3de7: bipush 44
      // 3de9: bipush 29
      // 3deb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3dee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3df1: ldc_w 26.0
      // 3df4: ldc -24.0
      // 3df6: fconst_2
      // 3df7: fconst_2
      // 3df8: fconst_2
      // 3df9: ldc 6.0
      // 3dfb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3dfe: dup
      // 3dff: fconst_0
      // 3e00: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3e03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e06: bipush 0
      // 3e07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e0a: bipush 44
      // 3e0c: bipush 29
      // 3e0e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e11: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e14: ldc 12.0
      // 3e16: ldc -6.0
      // 3e18: fconst_2
      // 3e19: fconst_2
      // 3e1a: fconst_2
      // 3e1b: ldc 4.0
      // 3e1d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3e20: dup
      // 3e21: fconst_0
      // 3e22: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3e25: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e28: bipush 0
      // 3e29: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e2c: ldc -21.0
      // 3e2e: ldc -12.0
      // 3e30: ldc -13.0
      // 3e32: fconst_0
      // 3e33: ldc_w -1.5708
      // 3e36: ldc_w 1.5708
      // 3e39: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 3e3c: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 3e3f: pop
      // 3e40: return
   }

   private static void addGrowths5(PartDefinition hunchmassivelowtaperfade) {
      PartDefinition growth15 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth15",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -22.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -26.0F, 40.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -24.0F, 40.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -22.0F, 40.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 38.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -26.0F, 38.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -16.0F, 38.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -28.0F, 36.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -26.0F, 36.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -22.0F, 36.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -22.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 36.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -20.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -18.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -20.0F, 36.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -16.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -18.0F, 36.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -14.0F, 36.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -14.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -16.0F, 36.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -10.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -18.0F, 36.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -18.0F, 36.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -8.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -6.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -24.0F, 34.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 34.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -22.0F, 34.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -22.0F, 34.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -20.0F, 34.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -20.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -26.0F, 34.0F, 4.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -20.0F, 34.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -20.0F, 34.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -20.0F, 34.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -12.0F, 34.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -10.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -8.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -6.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -26.0F, 32.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -26.0F, 32.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 32.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -22.0F, 32.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 32.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -18.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -16.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -14.0F, 32.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -12.0F, 32.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -8.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -6.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -18.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -16.0F, 30.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -14.0F, 30.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -12.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -10.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -10.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -8.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -10.0F, 30.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -6.0F, 30.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -16.0F, 28.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -14.0F, 28.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -16.0F, 28.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -8.0F, 28.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -16.0F, 28.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -8.0F, 28.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -18.0F, 24.0F, 2.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -42.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -42.0F, 2.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-45.0F, 4.0F, -17.0F, 0.0F, -1.5708F, 1.5708F)
      );
      PartDefinition growth16 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth16",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -52.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -48.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -38.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -34.0F, 20.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -30.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -30.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -28.0F, 20.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -26.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -26.0F, 20.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 22.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -26.0F, 22.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -36.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -30.0F, 18.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 18.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -28.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -26.0F, 18.0F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 22.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -34.0F, 16.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -30.0F, 16.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -30.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -22.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -22.0F, 16.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -20.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -20.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -20.0F, 16.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -20.0F, 16.0F, 10.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -36.0F, 14.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -34.0F, 14.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -36.0F, 14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -32.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -30.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -30.0F, 14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -30.0F, 14.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -30.0F, 14.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -22.0F, 14.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -18.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -14.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -36.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -36.0F, 12.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -34.0F, 12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -32.0F, 12.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -30.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -30.0F, 12.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 12.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -32.0F, 12.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -30.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -26.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -24.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -14.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -8.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -38.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -34.0F, 10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -32.0F, 10.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -28.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -30.0F, 10.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 10.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -22.0F, 10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -22.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -18.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -10.0F, 10.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -4.0F, 10.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -36.0F, 8.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -32.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -34.0F, 8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -30.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -28.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -28.0F, 8.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -28.0F, 8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 8.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -26.0F, 8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -22.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -18.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 8.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -12.0F, 8.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -10.0F, 8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -10.0F, 8.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 6.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -34.0F, 6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -32.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -32.0F, 6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -30.0F, 6.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -30.0F, 6.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -26.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -24.0F, 6.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -24.0F, 6.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -20.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -20.0F, 6.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -20.0F, 6.0F, 2.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -10.0F, 6.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -34.0F, 4.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -36.0F, 4.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -32.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -32.0F, 4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -30.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -32.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -32.0F, 4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -26.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -30.0F, 4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -22.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -36.0F, 2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -36.0F, 2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -32.0F, 2.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -32.0F, 2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -32.0F, 2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -28.0F, 2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -26.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -24.0F, 2.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -24.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -24.0F, 2.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -24.0F, 2.0F, 2.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-51.0F, -18.0F, -15.0F, 0.0F, -1.5708F, 1.5708F)
      );
      PartDefinition growth17 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth17",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -46.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -46.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -44.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -38.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -38.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -40.0F, 34.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -32.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -32.0F, 34.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -30.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -32.0F, 34.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -32.0F, 34.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -30.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -28.0F, 34.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -28.0F, 34.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -52.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -50.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -46.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -44.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -42.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -40.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -42.0F, 32.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -42.0F, 32.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -40.0F, 32.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -34.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -34.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -34.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -34.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -32.0F, 32.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 32.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -40.0F, 32.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -30.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -28.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 32.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 32.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -26.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -32.0F, 32.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -26.0F, 32.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -32.0F, 32.0F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -58.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -52.0F, 30.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -46.0F, 30.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -42.0F, 30.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -42.0F, 30.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -42.0F, 30.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -42.0F, 30.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -40.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -38.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -38.0F, 30.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -36.0F, 30.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -36.0F, 30.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -40.0F, 30.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -36.0F, 30.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -36.0F, 30.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -34.0F, 30.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -34.0F, 30.0F, 18.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -44.0F, 30.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -30.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 30.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -34.0F, 30.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -28.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -56.0F, 28.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -50.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -48.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -46.0F, 28.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -42.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -46.0F, 28.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -38.0F, 28.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -36.0F, 28.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -32.0F, 28.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -36.0F, 28.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -28.0F, 28.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -26.0F, 28.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -40.0F, 26.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -40.0F, 26.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -34.0F, 26.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -32.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -30.0F, 26.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -30.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -28.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -14.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -14.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -52.0F, 24.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -50.0F, 24.0F, 34.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -48.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -52.0F, 24.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -44.0F, 24.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -40.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -34.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -34.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -34.0F, 24.0F, 12.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -30.0F, 24.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -30.0F, 24.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -30.0F, 24.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -24.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -22.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -20.0F, 24.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -16.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -16.0F, 24.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -12.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -52.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -46.0F, 22.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -38.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -34.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -34.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -30.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -28.0F, 22.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -26.0F, 22.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -28.0F, 22.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -24.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -24.0F, 22.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 22.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -14.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -12.0F, 22.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -12.0F, 22.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -44.0F, 20.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -44.0F, 20.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -38.0F, 20.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -36.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 20.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -34.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -32.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -32.0F, 20.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -28.0F, 20.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -26.0F, 20.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -18.0F, 20.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -10.0F, 20.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -10.0F, 20.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -46.0F, 18.0F, 6.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -46.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -42.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -40.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -40.0F, 18.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -38.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -34.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -32.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -34.0F, 18.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -32.0F, 18.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -28.0F, 18.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -4.0F, 18.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -42.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -34.0F, 16.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -30.0F, 16.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -30.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -30.0F, 16.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -56.0F, 14.0F, 2.0F, 2.0F, 20.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -44.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -44.0F, 14.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -44.0F, 14.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -40.0F, 14.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -42.0F, 14.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -38.0F, 14.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -32.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -38.0F, 12.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -40.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -40.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -38.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -36.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -34.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -36.0F, 12.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -32.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -32.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -32.0F, 12.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -32.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -30.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -28.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -28.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -46.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -40.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(36.0F, -40.0F, 10.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -36.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -30.0F, 10.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(40.0F, -38.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(38.0F, -38.0F, 8.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -32.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -30.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -56.0F, 6.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(34.0F, -40.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -40.0F, 6.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -38.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -38.0F, 6.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -40.0F, 6.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -32.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -40.0F, 4.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -34.0F, 4.0F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -32.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -56.0F, 2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -34.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -32.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -30.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-59.0F, -4.0F, -17.0F, 0.0F, -1.5708F, 1.5708F)
      );
   }

   private static void addGrowths6(PartDefinition hunchmassivelowtaperfade) {
      PartDefinition growth18 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth18",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -30.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -28.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -30.0F, 34.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -26.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -24.0F, 34.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -24.0F, 34.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -6.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -34.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -24.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -22.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -10.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -32.0F, 30.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -30.0F, 30.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -22.0F, 30.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -22.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -10.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -8.0F, 30.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -30.0F, 28.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 28.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -10.0F, 28.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -8.0F, 28.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -28.0F, 26.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -32.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -32.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -24.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -18.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -32.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -32.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -30.0F, 22.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -28.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -26.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -22.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -22.0F, 22.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -18.0F, 22.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -14.0F, 22.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -38.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -40.0F, 20.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -34.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -32.0F, 20.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -32.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -28.0F, 20.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -26.0F, 20.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 20.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -24.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 20.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -22.0F, 20.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -18.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -20.0F, 20.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -36.0F, 18.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -34.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -34.0F, 18.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -30.0F, 18.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -30.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -28.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -30.0F, 18.0F, 2.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -26.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -26.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -26.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -24.0F, 18.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -24.0F, 18.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -24.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -24.0F, 18.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -22.0F, 18.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -24.0F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -18.0F, 18.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -16.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -20.0F, 18.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -14.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -12.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -38.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -36.0F, 16.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -34.0F, 16.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -32.0F, 16.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -34.0F, 16.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -30.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -28.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 16.0F, 2.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -26.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 16.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 16.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -30.0F, 16.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -24.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -22.0F, 16.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -22.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -20.0F, 16.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -18.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -18.0F, 16.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -14.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 16.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -12.0F, 16.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -30.0F, 14.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -26.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 14.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -26.0F, 14.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -34.0F, 14.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -34.0F, 14.0F, 4.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 14.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -16.0F, 14.0F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -12.0F, 14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -22.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -20.0F, 12.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -22.0F, 12.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 12.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -18.0F, 12.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -4.0F, 12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -14.0F, 10.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -8.0F, 10.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -18.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -16.0F, 8.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -10.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -8.0F, 8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -8.0F, 8.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -16.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -16.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -16.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -16.0F, 6.0F, 2.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -10.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -10.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -10.0F, 6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -16.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -16.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -16.0F, 2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-9.0F, 0.0F, -13.0F, 0.0F, -1.5708F, 1.5708F)
      );
      PartDefinition growth19 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth19",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -32.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -30.0F, 26.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -32.0F, 26.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -28.0F, 26.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 26.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -26.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -26.0F, 26.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -24.0F, 26.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -26.0F, 26.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -24.0F, 26.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -32.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -34.0F, 24.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -26.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -24.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -28.0F, 24.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -22.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -20.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -20.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -20.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -30.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -28.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -24.0F, 22.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -20.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -18.0F, 22.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 20.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -22.0F, 20.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -18.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -16.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -14.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 20.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -8.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -30.0F, 18.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -30.0F, 18.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 18.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -24.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -22.0F, 18.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 18.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -18.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -16.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -14.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -12.0F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -6.0F, 18.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -30.0F, 16.0F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -28.0F, 16.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -20.0F, 16.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -16.0F, 16.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -14.0F, 16.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -8.0F, 16.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -6.0F, 16.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -30.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -30.0F, 14.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -24.0F, 14.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -22.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -20.0F, 14.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -18.0F, 14.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -16.0F, 14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -10.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -6.0F, 14.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -38.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -34.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -32.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -30.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -26.0F, 12.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -22.0F, 12.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -12.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -10.0F, 12.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -6.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -34.0F, 10.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 10.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -20.0F, 10.0F, 4.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 10.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -10.0F, 10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -36.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -34.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -32.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -30.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 8.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -26.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 8.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -24.0F, 8.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -22.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -20.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -18.0F, 8.0F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -16.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -8.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -30.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -34.0F, 6.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -26.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -32.0F, 6.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -24.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -20.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -18.0F, 6.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -28.0F, 4.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -24.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -16.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -12.0F, 4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -16.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -14.0F, 2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-7.0F, -8.0F, -11.0F, 0.0F, -1.5708F, 1.5708F)
      );
      PartDefinition growth20 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth20",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -18.0F, 36.0F, 18.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -24.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -24.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 34.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 34.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -8.0F, 34.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -4.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -24.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -22.0F, 32.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -24.0F, 32.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -4.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -20.0F, 30.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -20.0F, 30.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -18.0F, 30.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -30.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -26.0F, 28.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -26.0F, 28.0F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -22.0F, 28.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 28.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -20.0F, 28.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 28.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -10.0F, 28.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -10.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -8.0F, 28.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -8.0F, 28.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -8.0F, 28.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -8.0F, 28.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -4.0F, 28.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -30.0F, 26.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 26.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -20.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -20.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -18.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -16.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -12.0F, 26.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -30.0F, 24.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -30.0F, 24.0F, 10.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -28.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -26.0F, 24.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -26.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -24.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -18.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -20.0F, 24.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -14.0F, 24.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -14.0F, 24.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -34.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -32.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -32.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -28.0F, 22.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -26.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -26.0F, 22.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -24.0F, 22.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -16.0F, 22.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -14.0F, 22.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -34.0F, 20.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -32.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -28.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -28.0F, 20.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 20.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -26.0F, 20.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -24.0F, 20.0F, 14.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -22.0F, 20.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -18.0F, 20.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -12.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -30.0F, 18.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -28.0F, 18.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -28.0F, 18.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -26.0F, 18.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -24.0F, 18.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -26.0F, 18.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -22.0F, 18.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -20.0F, 18.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -20.0F, 18.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -18.0F, 18.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -20.0F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -16.0F, 18.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -30.0F, 16.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -28.0F, 16.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 16.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -24.0F, 16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -26.0F, 16.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -22.0F, 16.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -20.0F, 16.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -18.0F, 16.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -28.0F, 14.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -22.0F, 14.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -20.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -18.0F, 14.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -18.0F, 14.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -16.0F, 14.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -14.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -12.0F, 14.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -20.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -18.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -16.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -16.0F, 12.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -18.0F, 12.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -14.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -12.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -10.0F, 12.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -12.0F, 12.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -24.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -24.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -22.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -22.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -22.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -20.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -20.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -20.0F, 10.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -20.0F, 10.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -18.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -20.0F, 10.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -16.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -18.0F, 10.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -14.0F, 10.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -14.0F, 10.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -16.0F, 10.0F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -10.0F, 10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -24.0F, 8.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 8.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -22.0F, 8.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -20.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -20.0F, 8.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -22.0F, 8.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -18.0F, 8.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -16.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -16.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -16.0F, 8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -16.0F, 8.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -12.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -12.0F, 8.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -10.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -10.0F, 8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -24.0F, 6.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -22.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -22.0F, 6.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -18.0F, 6.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -22.0F, 6.0F, 6.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -16.0F, 6.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -14.0F, 6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -12.0F, 6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -10.0F, 6.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -10.0F, 6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -12.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -10.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -10.0F, 4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -8.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -8.0F, 4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -12.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -10.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -8.0F, 2.0F, 6.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(1.0F, -2.0F, -13.0F, 0.0F, -1.5708F, 1.5708F)
      );
   }

   private static void addGrowths7(PartDefinition hunchmassivelowtaperfade) {
      PartDefinition growth21 = hunchmassivelowtaperfade.addOrReplaceChild(
         "growth21",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -26.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -26.0F, 36.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -22.0F, 36.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -24.0F, 36.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -22.0F, 36.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 36.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -18.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -18.0F, 36.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -12.0F, 36.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -28.0F, 34.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -26.0F, 34.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -26.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -24.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -24.0F, 34.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -24.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -22.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -22.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -20.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -20.0F, 34.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -18.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -16.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -16.0F, 34.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -20.0F, 34.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -18.0F, 34.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -6.0F, 34.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -24.0F, 32.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -24.0F, 32.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -26.0F, 32.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -22.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -24.0F, 32.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -22.0F, 32.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -20.0F, 32.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -20.0F, 32.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -22.0F, 32.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -20.0F, 32.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -18.0F, 32.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -6.0F, 32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -26.0F, 30.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -24.0F, 30.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -22.0F, 30.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -4.0F, 30.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -32.0F, 28.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -30.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -30.0F, 28.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -28.0F, 28.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -28.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -28.0F, 28.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -24.0F, 28.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -18.0F, 28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -32.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -34.0F, 26.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -32.0F, 26.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -30.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -28.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -28.0F, 26.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -26.0F, 26.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -20.0F, 26.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -40.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -38.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -40.0F, 24.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -30.0F, 24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -44.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -42.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -40.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -36.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -36.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -32.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(26.0F, -30.0F, 22.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(28.0F, -26.0F, 22.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -46.0F, 20.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -46.0F, 20.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -42.0F, 20.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -44.0F, 20.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -40.0F, 20.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -34.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -34.0F, 20.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -40.0F, 18.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -40.0F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -34.0F, 18.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(30.0F, -24.0F, 18.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -20.0F, 14.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(32.0F, -18.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -28.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -24.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -28.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -26.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -24.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -22.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(20.0F, -28.0F, 2.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(22.0F, -22.0F, 2.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(18.0F, -20.0F, 2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(24.0F, -16.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-3.0F, -4.0F, -13.0F, 0.0F, -1.5708F, 1.5708F)
      );
   }

   private static void addCovers0(PartDefinition covers) {
      PartDefinition cover1 = covers.addOrReplaceChild(
         "cover1",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -10.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -8.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -10.0F, 8.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -4.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -4.0F, 8.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -10.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -8.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -8.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -4.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -4.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -6.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -6.0F, 6.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -4.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -4.0F, 10.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -6.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -8.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -4.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -4.0F, 6.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -8.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -6.0F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -4.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -4.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -4.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -4.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -4.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -4.0F, 0.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -2.0F, 0.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -4.0F, 2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(0.0F, 2.0F, -6.0F, 0.0F, -1.5708F, 1.5708F)
      );
      PartDefinition cover6 = covers.addOrReplaceChild(
         "cover6",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -8.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -6.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -6.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -4.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -14.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -12.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -10.0F, 10.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -8.0F, 10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -6.0F, 10.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -6.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -6.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -4.0F, 10.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -4.0F, 10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -10.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -8.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -8.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -6.0F, 8.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -6.0F, 8.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -14.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -10.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -10.0F, 6.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(16.0F, -10.0F, 6.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -10.0F, 6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -10.0F, 6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -8.0F, 6.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -6.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -6.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -2.0F, 2.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -2.0F, 0.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -8.0F, 0.0F, 12.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -2.0F, 10.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -6.0F, 6.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -6.0F, 6.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -4.0F, 6.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -8.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -6.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -8.0F, 4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -10.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -8.0F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -8.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -8.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -4.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -8.0F, 2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-8.0F, 2.0F, -8.0F, 0.0F, -1.5708F, 1.5708F)
      );
      PartDefinition cover7 = covers.addOrReplaceChild(
         "cover7",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -8.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -8.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -6.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -6.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -6.0F, 10.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -14.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -10.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -10.0F, 8.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -8.0F, 6.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -10.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -2.0F, 4.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -2.0F, 2.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -4.0F, 0.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -10.0F, 8.0F, 8.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -6.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -6.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -6.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -6.0F, 6.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -4.0F, 6.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -8.0F, 12.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -12.0F, 12.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -8.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -8.0F, 12.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -10.0F, 12.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -8.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -2.0F, 10.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -4.0F, 6.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -10.0F, 4.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -8.0F, 4.0F, 10.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -4.0F, 4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(14.0F, -12.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -10.0F, 2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(12.0F, -10.0F, 2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -6.0F, 2.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -4.0F, 2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -6.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-14.0F, 2.0F, -10.0F, 0.0F, -1.5708F, 1.5708F)
      );
   }

   private static void addCovers1(PartDefinition covers) {
      PartDefinition cover8 = covers.addOrReplaceChild(
         "cover8",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -14.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -8.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -8.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -8.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -8.0F, 10.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -8.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -10.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -10.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -14.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -6.0F, 10.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -10.0F, 8.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(10.0F, -10.0F, 8.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -6.0F, 8.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -14.0F, 6.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -14.0F, 6.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -8.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -6.0F, 6.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -8.0F, 4.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -6.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -4.0F, 4.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -2.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -2.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -8.0F, 0.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -6.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -14.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -10.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -8.0F, 2.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(8.0F, -6.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -8.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -4.0F, 2.0F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-22.0F, 4.0F, -10.0F, 0.0F, -1.5708F, 1.5708F)
      );
      PartDefinition cover9 = covers.addOrReplaceChild(
         "cover9",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -8.0F, 12.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -10.0F, 10.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -4.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -2.0F, 10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -10.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -6.0F, 8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(6.0F, -4.0F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -8.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(0.0F, -14.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -12.0F, 14.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -4.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -8.0F, 4.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(2.0F, -12.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(44, 29)
            .mirror()
            .addBox(4.0F, -6.0F, 2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-24.0F, 4.0F, -10.0F, 0.0F, -1.5708F, 1.5708F)
      );
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.upperBodyPart1.render(poseStack, buffer, packedLight, packedOverlay);
   }
}
