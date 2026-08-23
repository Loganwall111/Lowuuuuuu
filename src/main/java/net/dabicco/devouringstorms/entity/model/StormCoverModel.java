package net.dabicco.devouringstorms.entity.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class StormCoverModel {
   private final ModelPart root;
   private final ModelPart back;
   private final ModelPart coverNew1;
   private final ModelPart coverNew2;
   private final ModelPart coverNew3;
   private final ModelPart coverNew4;
   public static final int COVERS = 4;

   public StormCoverModel(ModelPart root) {
      this.root = root;
      this.back = root.getChild("back");
      this.coverNew4 = root.getChild("cover-new4");
      this.coverNew3 = root.getChild("cover-new3");
      this.coverNew1 = root.getChild("cover-new1");
      this.coverNew2 = root.getChild("cover-new2");
   }

   public ModelPart root() {
      return this.root;
   }

   public ModelPart back() {
      return this.back;
   }

   public ModelPart cover(int which) {
      return switch (which) {
         case 2 -> this.coverNew2;
         case 3 -> this.coverNew3;
         case 4 -> this.coverNew4;
         default -> this.coverNew1;
      };
   }

   public void applyPeel(int plates) {
      for (int i = 1; i <= 4; i++) {
         this.cover(i).visible = i > plates;
      }
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild(
         "back",
         CubeListBuilder.create()
            .texOffs(0, 48)
            .addBox(-9.0F, 290.0F, -178.0F, 16.0F, 16.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(0, 32)
            .addBox(39.0F, 354.0F, -162.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(7.0F, 290.0F, -162.0F, 64.0F, 64.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 32)
            .addBox(39.0F, 370.0F, -146.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 32)
            .addBox(39.0F, 370.0F, -130.0F, 32.0F, 32.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(0, 32)
            .addBox(7.0F, 290.0F, -178.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 48)
            .addBox(-41.0F, 386.0F, -162.0F, 64.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 48)
            .addBox(-57.0F, 354.0F, -178.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 48)
            .addBox(23.0F, 386.0F, -146.0F, 16.0F, 16.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(0, 48)
            .addBox(-41.0F, 402.0F, -146.0F, 64.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 48)
            .addBox(-57.0F, 370.0F, -162.0F, 32.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 48)
            .addBox(7.0F, 402.0F, -130.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 32)
            .addBox(-41.0F, 306.0F, -162.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 32)
            .addBox(-41.0F, 338.0F, -178.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-57.0F, 306.0F, -162.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-73.0F, 354.0F, -162.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-89.0F, 354.0F, -130.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-25.0F, 290.0F, -162.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 32)
            .addBox(-57.0F, 386.0F, -146.0F, 16.0F, 32.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-89.0F, 290.0F, -114.0F, 64.0F, 112.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-57.0F, 402.0F, -114.0F, 80.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-41.0F, 418.0F, -130.0F, 64.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 32)
            .addBox(-73.0F, 386.0F, -130.0F, 16.0F, 32.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-73.0F, 306.0F, -146.0F, 16.0F, 96.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(71.0F, 290.0F, -130.0F, 16.0F, 80.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(55.0F, 274.0F, -114.0F, 32.0F, 112.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(55.0F, 290.0F, -146.0F, 16.0F, 80.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-73.0F, 306.0F, -130.0F, 16.0F, 80.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(19.0F, -538.0F, 106.0F)
      );
      partdefinition.addOrReplaceChild(
         "cover-new4",
         CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(10.0F, -272.0F, -88.0F, 32.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(42.0F, -256.0F, -72.0F, 48.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(74.0F, -240.0F, -72.0F, 16.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(58.0F, -208.0F, -72.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-6.0F, -272.0F, -72.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      partdefinition.addOrReplaceChild(
         "cover-new3",
         CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-52.0F, 8.0F, -72.0F, 32.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-52.0F, -8.0F, -88.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-36.0F, -24.0F, -88.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-36.0F, -56.0F, -72.0F, 16.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-20.0F, -56.0F, -88.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(44.0F, 8.0F, -56.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(28.0F, 24.0F, -56.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-36.0F, 24.0F, -72.0F, 64.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(14.0F, -176.0F, 0.0F)
      );
      partdefinition.addOrReplaceChild(
         "cover-new1",
         CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(12.0F, 8.0F, -72.0F, 32.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(12.0F, -56.0F, -88.0F, 16.0F, 64.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-4.0F, -56.0F, -88.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(14.0F, -176.0F, 0.0F)
      );
      partdefinition.addOrReplaceChild(
         "cover-new2",
         CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-20.0F, 8.0F, -88.0F, 32.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-20.0F, -40.0F, -88.0F, 16.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(28.0F, -40.0F, -88.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(44.0F, -40.0F, -88.0F, 16.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 16)
            .addBox(-36.0F, -8.0F, -88.0F, 48.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(14.0F, -176.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 512, 512);
   }
}
