package net.dabicco.witherstormmod.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.dabicco.witherstormmod.entity.CollapseAnim;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.dabicco.witherstormmod.mixin.ModelPartAccessor;
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

public class WitherStormTentaclesDevourer extends EntityModel<WitherStormRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "witherstorm_devour"), "main"
   );
   private final ModelPart tentacles;
   private final ModelPart Tentacle_4;
   private final ModelPart part_02_5;
   private final ModelPart part_02_6;
   private final ModelPart part_01_45;
   private final ModelPart part_01_46;
   private final ModelPart part_01_47;
   private final ModelPart part_01_48;
   private final ModelPart part_01_49;
   private final ModelPart part_01_50;
   private final ModelPart part_01_51;
   private final ModelPart part_01_52;
   private final ModelPart part_01_53;
   private final ModelPart part_01_54;
   private final ModelPart part_01_55;
   private final ModelPart part_01_56;
   private final ModelPart part_01_57;
   private final ModelPart part_01_58;
   private final ModelPart Tentacle_5;
   private final ModelPart part_02_12;
   private final ModelPart part_02_13;
   private final ModelPart part_01_16;
   private final ModelPart part_01_59;
   private final ModelPart part_01_60;
   private final ModelPart part_01_61;
   private final ModelPart part_01_62;
   private final ModelPart part_01_63;
   private final ModelPart part_01_64;
   private final ModelPart part_01_65;
   private final ModelPart part_01_66;
   private final ModelPart part_01_67;
   private final ModelPart part_01_68;
   private final ModelPart part_01_69;
   private final ModelPart part_01_70;
   private final ModelPart part_01_71;
   private final ModelPart Tentacle_6;
   private final ModelPart part_02_7;
   private final ModelPart part_02_8;
   private final ModelPart part_01_75;
   private final ModelPart part_01_76;
   private final ModelPart part_01_77;
   private final ModelPart part_01_78;
   private final ModelPart part_01_79;
   private final ModelPart part_01_80;
   private final ModelPart part_01_81;
   private final ModelPart part_01_82;
   private final ModelPart part_01_83;
   private final ModelPart part_01_84;
   private final ModelPart part_01_85;
   private final ModelPart part_01_86;
   private final ModelPart part_01_87;
   private final ModelPart part_01_88;
   private final ModelPart Tentacle_3;
   private final ModelPart part_02_3;
   private final ModelPart part_02_4;
   private final ModelPart part_01_17;
   private final ModelPart part_01_18;
   private final ModelPart part_01_33;
   private final ModelPart part_01_34;
   private final ModelPart part_01_35;
   private final ModelPart part_01_36;
   private final ModelPart part_01_37;
   private final ModelPart part_01_38;
   private final ModelPart part_01_39;
   private final ModelPart part_01_40;
   private final ModelPart part_01_41;
   private final ModelPart part_01_42;
   private final ModelPart part_01_43;
   private final ModelPart part_01_44;
   private final ModelPart Tentacle_2;
   private final ModelPart part_02_2;
   private final ModelPart part_02_9;
   private final ModelPart part_01_19;
   private final ModelPart part_01_20;
   private final ModelPart part_01_21;
   private final ModelPart part_01_22;
   private final ModelPart part_01_23;
   private final ModelPart part_01_24;
   private final ModelPart part_01_25;
   private final ModelPart part_01_26;
   private final ModelPart part_01_27;
   private final ModelPart part_01_28;
   private final ModelPart part_01_29;
   private final ModelPart part_01_30;
   private final ModelPart part_01_31;
   private final ModelPart part_01_32;
   private final ModelPart Tentacle_1;
   private final ModelPart part_02_10;
   private final ModelPart part_02_11;
   private final ModelPart part_01_2;
   private final ModelPart part_01_3;
   private final ModelPart part_01_4;
   private final ModelPart part_01_5;
   private final ModelPart part_01_6;
   private final ModelPart part_01_7;
   private final ModelPart part_01_8;
   private final ModelPart part_01_9;
   private final ModelPart part_01_10;
   private final ModelPart part_01_11;
   private final ModelPart part_01_12;
   private final ModelPart part_01_13;
   private final ModelPart part_01_14;
   private final ModelPart part_01_15;
   private final List<List<ModelPart>> chains = new ArrayList<>();

   public WitherStormTentaclesDevourer(ModelPart root) {
      super(root);
      this.tentacles = root.getChild("tentacles");
      this.Tentacle_4 = this.tentacles.getChild("Tentacle_4");
      this.part_02_5 = this.Tentacle_4.getChild("part_02_5");
      this.part_02_6 = this.part_02_5.getChild("part_02_6");
      this.part_01_45 = this.part_02_6.getChild("part_01_45");
      this.part_01_46 = this.part_01_45.getChild("part_01_46");
      this.part_01_47 = this.part_01_46.getChild("part_01_47");
      this.part_01_48 = this.part_01_47.getChild("part_01_48");
      this.part_01_49 = this.part_01_48.getChild("part_01_49");
      this.part_01_50 = this.part_01_49.getChild("part_01_50");
      this.part_01_51 = this.part_01_50.getChild("part_01_51");
      this.part_01_52 = this.part_01_51.getChild("part_01_52");
      this.part_01_53 = this.part_01_52.getChild("part_01_53");
      this.part_01_54 = this.part_01_53.getChild("part_01_54");
      this.part_01_55 = this.part_01_54.getChild("part_01_55");
      this.part_01_56 = this.part_01_55.getChild("part_01_56");
      this.part_01_57 = this.part_01_56.getChild("part_01_57");
      this.part_01_58 = this.part_01_57.getChild("part_01_58");
      this.Tentacle_5 = this.tentacles.getChild("Tentacle_5");
      this.part_02_12 = this.Tentacle_5.getChild("part_02_12");
      this.part_02_13 = this.part_02_12.getChild("part_02_13");
      this.part_01_16 = this.part_02_13.getChild("part_01_16");
      this.part_01_59 = this.part_01_16.getChild("part_01_59");
      this.part_01_60 = this.part_01_59.getChild("part_01_60");
      this.part_01_61 = this.part_01_60.getChild("part_01_61");
      this.part_01_62 = this.part_01_61.getChild("part_01_62");
      this.part_01_63 = this.part_01_62.getChild("part_01_63");
      this.part_01_64 = this.part_01_63.getChild("part_01_64");
      this.part_01_65 = this.part_01_64.getChild("part_01_65");
      this.part_01_66 = this.part_01_65.getChild("part_01_66");
      this.part_01_67 = this.part_01_66.getChild("part_01_67");
      this.part_01_68 = this.part_01_67.getChild("part_01_68");
      this.part_01_69 = this.part_01_68.getChild("part_01_69");
      this.part_01_70 = this.part_01_69.getChild("part_01_70");
      this.part_01_71 = this.part_01_70.getChild("part_01_71");
      this.Tentacle_6 = this.tentacles.getChild("Tentacle_6");
      this.part_02_7 = this.Tentacle_6.getChild("part_02_7");
      this.part_02_8 = this.part_02_7.getChild("part_02_8");
      this.part_01_75 = this.part_02_8.getChild("part_01_75");
      this.part_01_76 = this.part_01_75.getChild("part_01_76");
      this.part_01_77 = this.part_01_76.getChild("part_01_77");
      this.part_01_78 = this.part_01_77.getChild("part_01_78");
      this.part_01_79 = this.part_01_78.getChild("part_01_79");
      this.part_01_80 = this.part_01_79.getChild("part_01_80");
      this.part_01_81 = this.part_01_80.getChild("part_01_81");
      this.part_01_82 = this.part_01_81.getChild("part_01_82");
      this.part_01_83 = this.part_01_82.getChild("part_01_83");
      this.part_01_84 = this.part_01_83.getChild("part_01_84");
      this.part_01_85 = this.part_01_84.getChild("part_01_85");
      this.part_01_86 = this.part_01_85.getChild("part_01_86");
      this.part_01_87 = this.part_01_86.getChild("part_01_87");
      this.part_01_88 = this.part_01_87.getChild("part_01_88");
      this.Tentacle_3 = this.tentacles.getChild("Tentacle_3");
      this.part_02_3 = this.Tentacle_3.getChild("part_02_3");
      this.part_02_4 = this.part_02_3.getChild("part_02_4");
      this.part_01_17 = this.part_02_4.getChild("part_01_17");
      this.part_01_18 = this.part_01_17.getChild("part_01_18");
      this.part_01_33 = this.part_01_18.getChild("part_01_33");
      this.part_01_34 = this.part_01_33.getChild("part_01_34");
      this.part_01_35 = this.part_01_34.getChild("part_01_35");
      this.part_01_36 = this.part_01_35.getChild("part_01_36");
      this.part_01_37 = this.part_01_36.getChild("part_01_37");
      this.part_01_38 = this.part_01_37.getChild("part_01_38");
      this.part_01_39 = this.part_01_38.getChild("part_01_39");
      this.part_01_40 = this.part_01_39.getChild("part_01_40");
      this.part_01_41 = this.part_01_40.getChild("part_01_41");
      this.part_01_42 = this.part_01_41.getChild("part_01_42");
      this.part_01_43 = this.part_01_42.getChild("part_01_43");
      this.part_01_44 = this.part_01_43.getChild("part_01_44");
      this.Tentacle_2 = this.tentacles.getChild("Tentacle_2");
      this.part_02_2 = this.Tentacle_2.getChild("part_02_2");
      this.part_02_9 = this.part_02_2.getChild("part_02_9");
      this.part_01_19 = this.part_02_9.getChild("part_01_19");
      this.part_01_20 = this.part_01_19.getChild("part_01_20");
      this.part_01_21 = this.part_01_20.getChild("part_01_21");
      this.part_01_22 = this.part_01_21.getChild("part_01_22");
      this.part_01_23 = this.part_01_22.getChild("part_01_23");
      this.part_01_24 = this.part_01_23.getChild("part_01_24");
      this.part_01_25 = this.part_01_24.getChild("part_01_25");
      this.part_01_26 = this.part_01_25.getChild("part_01_26");
      this.part_01_27 = this.part_01_26.getChild("part_01_27");
      this.part_01_28 = this.part_01_27.getChild("part_01_28");
      this.part_01_29 = this.part_01_28.getChild("part_01_29");
      this.part_01_30 = this.part_01_29.getChild("part_01_30");
      this.part_01_31 = this.part_01_30.getChild("part_01_31");
      this.part_01_32 = this.part_01_31.getChild("part_01_32");
      this.Tentacle_1 = this.tentacles.getChild("Tentacle_1");
      this.part_02_10 = this.Tentacle_1.getChild("part_02_10");
      this.part_02_11 = this.part_02_10.getChild("part_02_11");
      this.part_01_2 = this.part_02_11.getChild("part_01_2");
      this.part_01_3 = this.part_01_2.getChild("part_01_3");
      this.part_01_4 = this.part_01_3.getChild("part_01_4");
      this.part_01_5 = this.part_01_4.getChild("part_01_5");
      this.part_01_6 = this.part_01_5.getChild("part_01_6");
      this.part_01_7 = this.part_01_6.getChild("part_01_7");
      this.part_01_8 = this.part_01_7.getChild("part_01_8");
      this.part_01_9 = this.part_01_8.getChild("part_01_9");
      this.part_01_10 = this.part_01_9.getChild("part_01_10");
      this.part_01_11 = this.part_01_10.getChild("part_01_11");
      this.part_01_12 = this.part_01_11.getChild("part_01_12");
      this.part_01_13 = this.part_01_12.getChild("part_01_13");
      this.part_01_14 = this.part_01_13.getChild("part_01_14");
      this.part_01_15 = this.part_01_14.getChild("part_01_15");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition tentacles = partdefinition.addOrReplaceChild("tentacles", CubeListBuilder.create(), PartPose.offset(29.0F, -466.0F, 86.0F));
      PartDefinition Tentacle_4 = tentacles.addOrReplaceChild(
         "Tentacle_4",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7622F, -24.0486F, -17.2865F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(17.2865F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(28.8108F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(40.3351F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(51.8594F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(63.3837F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-123.0F, 73.0F, 26.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition part_02_5 = Tentacle_4.addOrReplaceChild(
         "part_02_5",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5178F, -11.3126F, -18.8545F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(73.3902F, -12.736F, 1.568F)
      );
      PartDefinition part_02_6 = part_02_5.addOrReplaceChild(
         "part_02_6",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5179F, -14.3126F, -12.3302F, 57.6215F, 34.5729F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5179F, -2.7883F, -23.8545F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(50.6151F, -2.7883F, -23.8545F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5179F, -2.7883F, 10.7185F, 57.6215F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(77.6701F, 3.0F, 5.0F)
      );
      PartDefinition part_01_45 = part_02_6.addOrReplaceChild(
         "part_01_45",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5179F, -14.3126F, -16.3302F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5179F, -2.7883F, -27.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(38.0908F, 20.2603F, -16.3302F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5179F, -2.7883F, -4.8058F, 46.0972F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(58.6215F, 0.0F, 4.0F)
      );
      PartDefinition part_01_46 = part_01_45.addOrReplaceChild(
         "part_01_46",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(5.5179F, -17.3126F, -13.3302F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-6.0065F, -5.7883F, -24.8545F, 57.6216F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(5.5179F, 17.2603F, -13.3302F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(5.5179F, -5.7883F, -1.8058F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(40.0908F, 5.736F, -1.8058F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(40.0908F, -5.7883F, -1.8058F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(44.0972F, 3.0F, -3.0F)
      );
      PartDefinition part_01_47 = part_01_46.addOrReplaceChild(
         "part_01_47",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-8.4821F, -22.3126F, -15.3302F, 23.0486F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(14.5179F, -10.7883F, -15.3302F, 23.0486F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-8.4821F, -10.7883F, -26.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(14.5665F, 0.736F, -26.8545F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-8.4821F, 12.2603F, -15.3302F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-8.4821F, 0.736F, 7.7185F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-8.4821F, -10.7883F, -3.8058F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(60.0972F, 5.0F, 2.0F)
      );
      PartDefinition part_01_48 = part_01_47.addOrReplaceChild(
         "part_01_48",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-7.4821F, -11.7883F, -18.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-7.4821F, -0.264F, -18.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-7.4821F, 11.2603F, -7.3302F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-7.4821F, 11.2603F, 4.1942F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-7.4821F, -11.7883F, -7.3302F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-7.4821F, -11.7883F, 4.1942F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(45.0972F, 1.0F, -8.0F)
      );
      PartDefinition part_01_49 = part_01_48.addOrReplaceChild(
         "part_01_49",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -12.7883F, -6.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -1.264F, -17.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(39.0972F, 1.0F, -1.0F)
      );
      PartDefinition part_01_50 = part_01_49.addOrReplaceChild(
         "part_01_50",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4822F, -12.7883F, -14.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(37.5729F, 0.0F, 8.0F)
      );
      PartDefinition part_01_51 = part_01_50.addOrReplaceChild(
         "part_01_51",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.3792F, -13.7883F, -11.3302F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(29.7116F, 1.0F, -3.0F)
      );
      PartDefinition part_01_52 = part_01_51.addOrReplaceChild(
         "part_01_52",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.6208F, -11.7883F, -12.3302F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(41.7116F, -2.0F, 1.0F)
      );
      PartDefinition part_01_53 = part_01_52.addOrReplaceChild(
         "part_01_53",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-7.4822F, -11.7883F, -15.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(44.5729F, 0.0F, 3.0F)
      );
      PartDefinition part_01_54 = part_01_53.addOrReplaceChild(
         "part_01_54",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.6208F, -13.7883F, -11.3302F, 38.7116F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.6208F, -2.264F, 0.1942F, 38.7116F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(30.7116F, 2.0F, -4.0F)
      );
      PartDefinition part_01_55 = part_01_54.addOrReplaceChild(
         "part_01_55",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -12.7883F, -10.3302F, 34.5729F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -1.264F, 1.1942F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(32.5729F, -1.0F, -1.0F)
      );
      PartDefinition part_01_56 = part_01_55.addOrReplaceChild(
         "part_01_56",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.6565F, -8.0261F, -6.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(35.4343F, 6.7621F, -4.0F)
      );
      PartDefinition part_01_57 = part_01_56.addOrReplaceChild(
         "part_01_57",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.6565F, -8.0261F, -5.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(34.5729F, 0.0F, -1.0F)
      );
      PartDefinition part_01_58 = part_01_57.addOrReplaceChild(
         "part_01_58",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.3435F, -7.0261F, -6.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(36.5729F, -1.0F, 1.0F)
      );
      PartDefinition Tentacle_5 = tentacles.addOrReplaceChild(
         "Tentacle_5",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7622F, -24.0486F, -17.2865F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(17.2865F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(28.8108F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(40.3351F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(51.8594F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(63.3837F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-31.0F, 138.0F, -26.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition part_02_12 = Tentacle_5.addOrReplaceChild(
         "part_02_12",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5178F, -21.3126F, -20.8545F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(74.3902F, -2.736F, 3.568F)
      );
      PartDefinition part_02_13 = part_02_12.addOrReplaceChild(
         "part_02_13",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5178F, -19.3126F, -14.3301F, 57.6216F, 34.5729F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5179F, -7.7883F, -25.8545F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(50.6151F, -7.7883F, -25.8545F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5178F, -7.7883F, 8.7185F, 57.6216F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(76.6701F, -2.0F, 5.0F)
      );
      PartDefinition part_01_16 = part_02_13.addOrReplaceChild(
         "part_01_16",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -19.3126F, -17.3301F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -7.7883F, -28.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(37.0908F, 15.2603F, -17.3301F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -7.7883F, -5.8058F, 46.0972F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(59.6215F, 0.0F, 3.0F)
      );
      PartDefinition part_01_59 = part_01_16.addOrReplaceChild(
         "part_01_59",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5179F, -20.3126F, -18.3301F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-7.0065F, -8.7883F, -29.8545F, 57.6216F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5179F, 14.2603F, -18.3301F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(4.5179F, -8.7883F, -6.8058F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(39.0908F, 2.736F, -6.8058F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(39.0908F, -8.7883F, -6.8058F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(44.0972F, 1.0F, 1.0F)
      );
      PartDefinition part_01_60 = part_01_59.addOrReplaceChild(
         "part_01_60",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -26.3126F, -16.3301F, 23.0486F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(23.5179F, -14.7883F, -16.3301F, 23.0486F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -14.7883F, -27.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(23.5665F, -3.264F, -27.8545F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, 8.2603F, -16.3301F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -3.264F, 6.7185F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -14.7883F, -4.8058F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(50.0972F, 6.0F, -2.0F)
      );
      PartDefinition part_01_61 = part_01_60.addOrReplaceChild(
         "part_01_61",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -20.7883F, -19.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -9.264F, -19.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, 2.2603F, -8.3301F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, 2.2603F, 3.1942F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -20.7883F, -8.3302F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -20.7883F, 3.1942F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(46.0972F, 6.0F, -8.0F)
      );
      PartDefinition part_01_62 = part_01_61.addOrReplaceChild(
         "part_01_62",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5179F, -11.7883F, -4.3301F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5179F, -0.264F, -15.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(45.0972F, -9.0F, -4.0F)
      );
      PartDefinition part_01_63 = part_01_62.addOrReplaceChild(
         "part_01_63",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5178F, -14.7883F, -11.3301F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(35.5729F, 3.0F, 7.0F)
      );
      PartDefinition part_01_64 = part_01_63.addOrReplaceChild(
         "part_01_64",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(3.3792F, -12.7883F, -6.3301F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(31.7116F, -2.0F, -5.0F)
      );
      PartDefinition part_01_65 = part_01_64.addOrReplaceChild(
         "part_01_65",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(2.3792F, -13.7883F, -12.3301F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(39.7116F, 1.0F, 6.0F)
      );
      PartDefinition part_01_66 = part_01_65.addOrReplaceChild(
         "part_01_66",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4822F, -13.7883F, -10.3301F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(42.5729F, 0.0F, -2.0F)
      );
      PartDefinition part_01_67 = part_01_66.addOrReplaceChild(
         "part_01_67",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.6208F, -3.7883F, -9.3301F, 38.7116F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.6208F, 7.736F, 2.1942F, 38.7116F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(36.7116F, -10.0F, -1.0F)
      );
      PartDefinition part_01_68 = part_01_67.addOrReplaceChild(
         "part_01_68",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4821F, -12.7883F, -12.3301F, 34.5729F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4821F, -1.264F, -0.8058F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(38.5729F, 9.0F, 3.0F)
      );
      PartDefinition part_01_69 = part_01_68.addOrReplaceChild(
         "part_01_69",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.6565F, -7.0261F, -7.3301F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(30.4343F, 5.7621F, -5.0F)
      );
      PartDefinition part_01_70 = part_01_69.addOrReplaceChild(
         "part_01_70",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.6565F, -9.0261F, -5.3301F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(34.5729F, 2.0F, -2.0F)
      );
      PartDefinition part_01_71 = part_01_70.addOrReplaceChild(
         "part_01_71",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.6565F, -8.0261F, -6.3301F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(33.5729F, -1.0F, 1.0F)
      );
      PartDefinition Tentacle_6 = tentacles.addOrReplaceChild(
         "Tentacle_6",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7621F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7621F, -24.0486F, -17.2865F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(17.2865F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(28.8108F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(40.3351F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(51.8594F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(63.3837F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-42.0F, 2.0F, 26.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition part_02_7 = Tentacle_6.addOrReplaceChild(
         "part_02_7",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5178F, -18.3126F, -7.8545F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(74.3902F, -5.736F, -9.432F)
      );
      PartDefinition part_02_8 = part_02_7.addOrReplaceChild(
         "part_02_8",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4822F, -21.3126F, -7.3302F, 57.6216F, 34.5729F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -9.7883F, -18.8545F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(45.6151F, -9.7883F, -18.8545F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4822F, -9.7883F, 15.7185F, 57.6216F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(81.6701F, 3.0F, 11.0F)
      );
      PartDefinition part_01_75 = part_02_8.addOrReplaceChild(
         "part_01_75",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -22.3126F, -11.3302F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -10.7883F, -22.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(33.0908F, 12.2603F, -11.3302F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -10.7883F, 0.1942F, 46.0972F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(58.6215F, 1.0F, 4.0F)
      );
      PartDefinition part_01_76 = part_01_75.addOrReplaceChild(
         "part_01_76",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -24.3126F, -12.3302F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-11.0065F, -12.7883F, -23.8545F, 57.6216F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, 10.2603F, -12.3302F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -12.7883F, -0.8058F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(35.0908F, -1.264F, -0.8058F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(35.0908F, -12.7883F, -0.8058F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(44.0972F, 2.0F, 1.0F)
      );
      PartDefinition part_01_77 = part_01_76.addOrReplaceChild(
         "part_01_77",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.4821F, -24.3126F, -4.3302F, 23.0486F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(20.5179F, -12.7883F, -4.3302F, 23.0486F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.4821F, -12.7883F, -15.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(20.5665F, -1.264F, -15.8545F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.4821F, 10.2603F, -4.3302F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.4821F, -1.264F, 18.7185F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.4821F, -12.7883F, 7.1942F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(49.0972F, 0.0F, -8.0F)
      );
      PartDefinition part_01_78 = part_01_77.addOrReplaceChild(
         "part_01_78",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -18.7883F, -17.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -7.264F, -17.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, 4.2603F, -6.3302F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, 4.2603F, 5.1942F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -18.7883F, -6.3302F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -18.7883F, 5.1942F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(44.0972F, 6.0F, 2.0F)
      );
      PartDefinition part_01_79 = part_01_78.addOrReplaceChild(
         "part_01_79",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5179F, -13.7883F, -4.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5179F, -2.264F, -15.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(44.0972F, -5.0F, -2.0F)
      );
      PartDefinition part_01_80 = part_01_79.addOrReplaceChild(
         "part_01_80",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4822F, -13.7883F, -12.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(36.5729F, 0.0F, 8.0F)
      );
      PartDefinition part_01_81 = part_01_80.addOrReplaceChild(
         "part_01_81",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.3792F, -13.7883F, -12.3302F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(32.7116F, 0.0F, 0.0F)
      );
      PartDefinition part_01_82 = part_01_81.addOrReplaceChild(
         "part_01_82",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(2.3792F, -12.7883F, -14.3302F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(37.7116F, -1.0F, 2.0F)
      );
      PartDefinition part_01_83 = part_01_82.addOrReplaceChild(
         "part_01_83",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.4822F, -10.7883F, -13.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(43.5729F, -2.0F, -1.0F)
      );
      PartDefinition part_01_84 = part_01_83.addOrReplaceChild(
         "part_01_84",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.6208F, -12.7883F, -9.3302F, 38.7116F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.6208F, -1.264F, 2.1942F, 38.7116F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(35.7116F, 2.0F, -4.0F)
      );
      PartDefinition part_01_85 = part_01_84.addOrReplaceChild(
         "part_01_85",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-4.4821F, -11.7883F, -7.3302F, 34.5729F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-4.4821F, -0.264F, 4.1942F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(39.5729F, -1.0F, -2.0F)
      );
      PartDefinition part_01_86 = part_01_85.addOrReplaceChild(
         "part_01_86",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.3435F, -6.0261F, -3.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(33.4343F, 5.7621F, -4.0F)
      );
      PartDefinition part_01_87 = part_01_86.addOrReplaceChild(
         "part_01_87",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.3435F, -6.0261F, -4.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(31.5729F, 0.0F, 1.0F)
      );
      PartDefinition part_01_88 = part_01_87.addOrReplaceChild(
         "part_01_88",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.6565F, -7.0261F, -5.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(32.5729F, 1.0F, 1.0F)
      );
      PartDefinition Tentacle_3 = tentacles.addOrReplaceChild(
         "Tentacle_3",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7622F, -24.0486F, -17.2865F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(17.2865F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(28.8108F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(40.3351F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(51.8594F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(63.3837F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(82.0F, 71.0F, 17.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition part_02_3 = Tentacle_3.addOrReplaceChild(
         "part_02_3",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4822F, -17.3126F, -17.8545F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(76.3902F, -6.736F, 0.568F)
      );
      PartDefinition part_02_4 = part_02_3.addOrReplaceChild(
         "part_02_4",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -15.3126F, -7.3302F, 57.6216F, 34.5729F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -3.7883F, -18.8545F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(48.6151F, -3.7883F, -18.8545F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -3.7883F, 15.7185F, 57.6216F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(76.6701F, -2.0F, 1.0F)
      );
      PartDefinition part_01_17 = part_02_4.addOrReplaceChild(
         "part_01_17",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -17.3126F, -11.3302F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -5.7883F, -22.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(33.0908F, 17.2603F, -11.3302F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -5.7883F, 0.1942F, 46.0972F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(61.6216F, 2.0F, 4.0F)
      );
      PartDefinition part_01_18 = part_01_17.addOrReplaceChild(
         "part_01_18",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -20.3126F, -13.3302F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-13.0065F, -8.7883F, -24.8545F, 57.6216F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, 14.2603F, -13.3302F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -8.7883F, -1.8058F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(33.0908F, 2.736F, -1.8058F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(33.0908F, -8.7883F, -1.8058F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(46.0972F, 3.0F, 2.0F)
      );
      PartDefinition part_01_33 = part_01_18.addOrReplaceChild(
         "part_01_33",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5179F, -27.3126F, -12.3302F, 23.0486F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(26.5179F, -15.7883F, -12.3302F, 23.0486F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5179F, -15.7883F, -23.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(26.5665F, -4.264F, -23.8545F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5179F, 7.2603F, -12.3302F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5179F, -4.264F, 10.7185F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5179F, -15.7883F, -0.8058F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(41.0972F, 7.0F, -1.0F)
      );
      PartDefinition part_01_34 = part_01_33.addOrReplaceChild(
         "part_01_34",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -17.7883F, -16.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -6.264F, -16.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, 5.2603F, -5.3302F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, 5.2603F, 6.1942F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -17.7883F, -5.3302F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -17.7883F, 6.1942F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(50.0972F, 2.0F, -7.0F)
      );
      PartDefinition part_01_35 = part_01_34.addOrReplaceChild(
         "part_01_35",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -8.7883F, -9.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, 2.736F, -20.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(45.0972F, -9.0F, 4.0F)
      );
      PartDefinition part_01_36 = part_01_35.addOrReplaceChild(
         "part_01_36",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5179F, -10.7883F, -11.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(33.5729F, 2.0F, 2.0F)
      );
      PartDefinition part_01_37 = part_01_36.addOrReplaceChild(
         "part_01_37",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.6208F, -12.7883F, -13.3302F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(36.7116F, 2.0F, 2.0F)
      );
      PartDefinition part_01_38 = part_01_37.addOrReplaceChild(
         "part_01_38",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.6208F, -13.7883F, -12.3302F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(39.7116F, 1.0F, -1.0F)
      );
      PartDefinition part_01_39 = part_01_38.addOrReplaceChild(
         "part_01_39",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5178F, -12.7883F, -10.3302F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(36.5729F, -1.0F, -2.0F)
      );
      PartDefinition part_01_40 = part_01_39.addOrReplaceChild(
         "part_01_40",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.6208F, -16.7883F, -9.3302F, 38.7116F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.6208F, -5.264F, 2.1942F, 38.7116F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(37.7116F, 4.0F, -1.0F)
      );
      PartDefinition part_01_41 = part_01_40.addOrReplaceChild(
         "part_01_41",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5179F, -11.7883F, -9.3302F, 34.5729F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5179F, -0.264F, 2.1942F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(34.5729F, -5.0F, 0.0F)
      );
      PartDefinition part_01_42 = part_01_41.addOrReplaceChild(
         "part_01_42",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.3435F, -7.0261F, -5.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(36.4343F, 6.7621F, -4.0F)
      );
      PartDefinition part_01_43 = part_01_42.addOrReplaceChild(
         "part_01_43",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.3435F, -5.0261F, -6.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(35.5729F, -2.0F, 1.0F)
      );
      PartDefinition part_01_44 = part_01_43.addOrReplaceChild(
         "part_01_44",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.6565F, -6.0261F, -7.3302F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(32.5729F, 1.0F, 1.0F)
      );
      PartDefinition Tentacle_2 = tentacles.addOrReplaceChild(
         "Tentacle_2",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-5.7622F, -24.0486F, -17.2865F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(5.7622F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(17.2865F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(28.8108F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(40.3351F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(51.8594F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(61.9F, -24.2F, -17.35F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(61.9F, -24.2F, -5.35F, 13.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(48.9F, -24.2F, -5.35F, 13.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(35.9F, -24.2F, -5.35F, 13.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(22.9F, -24.2F, -5.35F, 13.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(9.9F, -24.2F, -5.35F, 13.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(-5.1F, -24.2F, -5.35F, 15.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(61.9F, -24.2F, 5.65F, 13.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(48.9F, -24.2F, 5.65F, 13.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(35.9F, -24.2F, 5.65F, 13.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(22.9F, -24.2F, 5.65F, 13.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(9.9F, -24.2F, 5.65F, 13.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(-5.1F, -24.2F, 5.65F, 15.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(61.9F, -7.3F, -6.35F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(48.9F, -7.3F, -6.35F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(35.9F, -7.3F, -6.35F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(22.9F, -7.3F, -6.35F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(9.9F, -7.3F, -6.35F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(-5.1F, -7.3F, -6.35F, 15.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(61.9F, -7.3F, -17.1F, 13.0F, 18.0F, 10.5F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(48.9F, -7.3F, -17.1F, 13.0F, 18.0F, 10.5F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(35.9F, -7.3F, -17.1F, 13.0F, 18.0F, 10.5F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(22.9F, -7.3F, -17.1F, 13.0F, 18.0F, 10.5F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(9.9F, -7.3F, -17.1F, 13.0F, 18.0F, 10.5F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(-5.1F, -7.3F, -17.1F, 15.0F, 18.0F, 10.5F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(61.9F, -7.3F, 5.65F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(48.9F, -7.3F, 5.65F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(35.9F, -7.3F, 5.65F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(22.9F, -7.3F, 5.65F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(9.9F, -7.3F, 5.65F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(-5.1F, -7.3F, 5.65F, 15.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(48.9F, -24.2F, -17.35F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(35.9F, -24.2F, -17.35F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(22.9F, -24.2F, -17.35F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(9.9F, -24.2F, -17.35F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 352)
            .mirror()
            .addBox(-5.1F, -24.2F, -17.35F, 15.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(63.3837F, -12.5243F, -28.8108F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(51.0F, 23.0F, -63.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition part_02_2 = Tentacle_2.addOrReplaceChild(
         "part_02_2",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4822F, -16.3126F, -14.8545F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(66.5098F, -4.964F, -14.918F, 13.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(53.5098F, -4.964F, -14.918F, 13.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(40.5098F, -4.964F, -14.918F, 13.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(27.5098F, -4.964F, -14.918F, 13.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(14.5098F, -4.964F, -14.918F, 13.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-1.4902F, -4.964F, -14.918F, 16.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(66.5098F, -17.464F, -2.918F, 13.0F, 12.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(53.5098F, -17.464F, -2.918F, 13.0F, 12.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(40.5098F, -16.464F, -2.918F, 13.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(27.5098F, -16.464F, -2.918F, 13.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(14.5098F, -16.464F, -2.918F, 13.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(-1.4902F, -16.464F, -2.918F, 16.0F, 11.5F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(66.5098F, -17.464F, 8.082F, 13.0F, 17.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(53.5098F, -17.464F, 8.082F, 13.0F, 17.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(27.5098F, -16.464F, 8.082F, 13.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(14.5098F, -16.464F, 8.082F, 13.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(-1.4902F, -16.464F, 8.082F, 16.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(40.5098F, -16.464F, 8.082F, 13.0F, 16.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(66.5098F, 0.436F, 8.082F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(66.5098F, 0.436F, -3.918F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(53.5098F, 0.436F, -3.918F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(40.5098F, 0.436F, -3.918F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(27.5098F, 0.436F, -3.918F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(14.5098F, 0.436F, -3.918F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(-1.4902F, 0.436F, -3.918F, 16.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(66.5098F, 6.436F, -14.918F, 13.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(53.5098F, 6.436F, -14.918F, 13.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(40.5098F, 6.436F, -14.918F, 13.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(27.5098F, 6.436F, -14.918F, 13.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(14.5098F, 6.436F, -14.918F, 13.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(-1.4902F, 6.436F, -14.918F, 16.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(53.5098F, 0.436F, 8.082F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(40.5098F, 0.436F, 8.082F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(27.5098F, 0.436F, 8.082F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(14.5098F, 0.436F, 8.082F, 13.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(-1.4902F, 0.436F, 8.082F, 16.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(66.5098F, -16.464F, -14.918F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(53.5098F, -16.464F, -14.918F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(40.5098F, -16.464F, -14.918F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(27.5098F, -16.464F, -14.918F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(14.5098F, -16.464F, -14.918F, 13.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(-1.4902F, -16.464F, -14.918F, 16.0F, 11.5F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(76.3902F, -7.736F, -2.432F)
      );
      PartDefinition part_02_9 = part_02_2.addOrReplaceChild(
         "part_02_9",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4822F, -20.3126F, -4.3301F, 45.6216F, 34.5729F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(43.5178F, -8.3126F, 6.6699F, 12.6216F, 6.0F, 12.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4822F, -8.7883F, -15.8545F, 45.6216F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(43.9397F, -9.964F, -15.668F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(43.5178F, -8.7883F, -15.8545F, 12.6216F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(43.5178F, 2.736F, -4.3301F, 12.6216F, 11.5243F, 24.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4822F, -8.7883F, 18.7185F, 57.6216F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(-2.0603F, -21.564F, -4.568F, 16.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(13.9397F, -21.564F, -4.568F, 16.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(29.9397F, -21.564F, -4.568F, 14.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(43.9397F, -21.564F, -4.568F, 14.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .addBox(29.9397F, -21.564F, 7.432F, 15.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(428, 430)
            .addBox(13.9397F, -21.564F, 7.432F, 16.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .mirror()
            .addBox(30.8397F, 2.336F, -15.918F, 13.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(17.8397F, 2.336F, -15.918F, 13.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-1.1603F, 2.336F, -15.918F, 19.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(18.8397F, -9.964F, -15.918F, 12.0F, 12.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(30.8397F, -9.964F, -15.918F, 13.0F, 12.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-1.1603F, -9.964F, -15.918F, 20.0F, 12.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-1.1603F, -9.964F, 18.082F, 20.0F, 12.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(18.8397F, -9.964F, 18.082F, 13.0F, 12.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(31.8397F, -9.964F, 18.082F, 13.0F, 12.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(44.8397F, -9.964F, 6.082F, 13.0F, 12.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(44.8397F, -9.964F, 18.082F, 13.0F, 12.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(30.8397F, 2.336F, -3.918F, 13.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-1.1603F, 2.336F, -3.918F, 19.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-1.1603F, 2.336F, 8.082F, 19.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-1.1603F, 2.336F, 20.082F, 19.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-1.5603F, -8.664F, 18.082F, 13.0F, 22.9F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(17.8397F, 2.336F, -3.918F, 13.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .addBox(-2.0603F, -21.564F, 7.432F, 16.0F, 17.0F, 12.0F, new CubeDeformation(0.0F)),
         PartPose.offset(80.6701F, 4.0F, 1.0F)
      );
      PartDefinition part_01_19 = part_02_9.addOrReplaceChild(
         "part_01_19",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -18.3126F, -11.3301F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -6.7883F, -22.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(33.0908F, 16.2603F, -11.3301F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4821F, -6.7883F, 0.1941F, 46.0972F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(0.3182F, -19.564F, -11.568F, 14.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(14.3182F, -19.564F, -11.568F, 14.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(18.3182F, -7.964F, -22.668F, 15.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(18.3182F, -7.964F, 11.332F, 15.0F, 8.0F, 11.9F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(18.3182F, -7.964F, 0.332F, 15.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-0.6818F, -7.964F, -22.668F, 19.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-0.6818F, -7.964F, 0.332F, 19.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-0.6818F, -7.964F, 11.332F, 19.0F, 8.0F, 11.9F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(28.3182F, -19.564F, -11.568F, 16.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(57.6215F, -2.0F, 7.0F)
      );
      PartDefinition part_01_20 = part_01_19.addOrReplaceChild(
         "part_01_20",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4821F, -17.3126F, -9.3301F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-15.0065F, -5.7883F, -20.8545F, 57.6216F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4821F, 17.2603F, -9.3301F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4821F, -5.7883F, 2.1941F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(31.0908F, 5.736F, 2.1941F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(31.0908F, -5.7883F, 2.1941F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(22.2209F, -18.464F, -9.568F, 18.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(10.2209F, -18.464F, -9.568F, 12.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-14.7791F, -6.964F, -20.668F, 15.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-14.7791F, -6.964F, 1.332F, 15.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(-14.7791F, -6.964F, 13.332F, 15.0F, 8.0F, 11.9F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 430)
            .mirror()
            .addBox(-5.7791F, -18.464F, -9.568F, 16.0F, 17.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(28.2209F, -6.964F, -20.668F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(28.2209F, -6.964F, 1.332F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(15.2209F, -6.964F, -20.668F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(15.2209F, -6.964F, 1.332F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(15.2209F, -6.964F, 13.332F, 15.0F, 8.0F, 11.9F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(0.2209F, -6.964F, 13.332F, 15.0F, 8.0F, 11.9F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(0.2209F, -6.964F, -20.668F, 15.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(0.2209F, -6.964F, 1.332F, 15.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(48.0972F, -1.0F, -2.0F)
      );
      PartDefinition part_01_21 = part_01_20.addOrReplaceChild(
         "part_01_21",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(7.5179F, -19.3126F, -8.3301F, 23.0486F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(30.5179F, -7.7883F, -8.3301F, 23.0486F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(7.5179F, -7.7883F, -19.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(30.5665F, 3.736F, -19.8545F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(7.5179F, 15.2603F, -8.3301F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(7.5179F, 3.736F, 14.7185F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(7.5179F, -7.7883F, 3.1941F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(5.1237F, -19.464F, -8.568F, 11.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(19.1237F, -8.964F, -19.668F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(19.1237F, -8.964F, 2.332F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(6.1237F, -8.964F, -19.668F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(348, 350)
            .mirror()
            .addBox(6.1237F, -8.964F, 2.332F, 13.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 350)
            .mirror()
            .addBox(16.1237F, -19.464F, -8.568F, 16.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(35.0972F, 2.0F, -1.0F)
      );
      PartDefinition part_01_22 = part_01_21.addOrReplaceChild(
         "part_01_22",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -14.7883F, -18.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -3.264F, -18.8545F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, 8.2603F, -7.3301F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, 8.2603F, 4.1941F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -14.7883F, -7.3302F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -14.7883F, 4.1941F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(54.0972F, 7.0F, -1.0F)
      );
      PartDefinition part_01_23 = part_01_22.addOrReplaceChild(
         "part_01_23",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -15.7883F, -6.3301F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.4821F, -4.264F, -17.8545F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(46.0972F, 1.0F, -1.0F)
      );
      PartDefinition part_01_24 = part_01_23.addOrReplaceChild(
         "part_01_24",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(2.5179F, -15.7883F, -11.3301F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(31.5729F, 0.0F, 5.0F)
      );
      PartDefinition part_01_25 = part_01_24.addOrReplaceChild(
         "part_01_25",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.6208F, -11.7883F, -10.3301F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(39.7116F, -4.0F, -1.0F)
      );
      PartDefinition part_01_26 = part_01_25.addOrReplaceChild(
         "part_01_26",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.3792F, -11.7883F, -11.3301F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(34.7116F, 0.0F, 1.0F)
      );
      PartDefinition part_01_27 = part_01_26.addOrReplaceChild(
         "part_01_27",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.5178F, -13.7883F, -10.3301F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(38.5729F, 2.0F, -1.0F)
      );
      PartDefinition part_01_28 = part_01_27.addOrReplaceChild(
         "part_01_28",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.6208F, -12.7883F, -15.3301F, 38.7116F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.6208F, -1.264F, -3.8058F, 38.7116F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(38.7116F, -1.0F, 5.0F)
      );
      PartDefinition part_01_29 = part_01_28.addOrReplaceChild(
         "part_01_29",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -13.7883F, -13.3301F, 34.5729F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.5179F, -2.264F, -1.8059F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(35.5729F, 1.0F, -2.0F)
      );
      PartDefinition part_01_30 = part_01_29.addOrReplaceChild(
         "part_01_30",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(3.6565F, -8.0261F, -5.3301F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(31.4343F, 5.7621F, -8.0F)
      );
      PartDefinition part_01_31 = part_01_30.addOrReplaceChild(
         "part_01_31",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.3435F, -7.0261F, -5.3301F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(40.5729F, -1.0F, 0.0F)
      );
      PartDefinition part_01_32 = part_01_31.addOrReplaceChild(
         "part_01_32",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.3435F, -7.0261F, -6.3301F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(33.5729F, 0.0F, 1.0F)
      );
      PartDefinition Tentacle_1 = tentacles.addOrReplaceChild(
         "Tentacle_1",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-5.7621F, -12.5243F, 17.2865F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-74.908F, -24.0486F, -17.2865F, 80.6702F, 0.0F, 34.5729F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-17.2865F, -12.5243F, 17.2865F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-28.8108F, -12.5243F, 17.2865F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-40.3351F, -12.5243F, 17.2865F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-51.8594F, -12.5243F, 17.2865F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-63.3837F, -12.5243F, 17.2865F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-74.908F, -12.5243F, 17.2865F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-86.0F, 16.0F, -78.0F, 0.0F, 0.0F, -1.5708F)
      );
      PartDefinition part_02_10 = Tentacle_1.addOrReplaceChild(
         "part_02_10",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-77.188F, -19.3126F, -19.7185F, 80.6702F, 34.5729F, 34.5729F, new CubeDeformation(0.0F))
            .texOffs(428, 430)
            .mirror()
            .addBox(-74.6098F, -8.264F, 3.568F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-62.6098F, -8.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-62.6098F, -20.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-51.6098F, -20.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-40.6098F, -20.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-29.6098F, -20.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-18.6098F, -20.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-7.6098F, -20.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-62.6098F, -20.264F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-51.6098F, -20.264F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-40.6098F, -20.264F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-29.6098F, -20.264F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-18.6098F, -20.264F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-7.6098F, -20.264F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-62.6098F, -20.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-51.6098F, -20.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-40.6098F, -20.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-29.6098F, -20.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-18.6098F, -20.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-7.6098F, -20.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-62.6098F, -8.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-51.6098F, -8.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-40.6098F, -8.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-29.6098F, -8.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-18.6098F, -8.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-7.6098F, -8.264F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-62.6098F, 3.736F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-51.6098F, 3.736F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-40.6098F, 3.736F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-29.6098F, 3.736F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-18.6098F, 3.736F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-7.6098F, 3.736F, -20.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-62.6098F, 3.736F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-51.6098F, 3.736F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-40.6098F, 3.736F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-29.6098F, 3.736F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-18.6098F, 3.736F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-7.6098F, 3.736F, -8.432F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-62.6098F, 3.736F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-51.6098F, 3.736F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-40.6098F, 3.736F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-29.6098F, 3.736F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-18.6098F, 3.736F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-7.6098F, 3.736F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-51.6098F, -8.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-40.6098F, -8.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-29.6098F, -8.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-18.6098F, -8.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-7.6098F, -8.264F, 3.568F, 11.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-77.6098F, -20.264F, 3.568F, 15.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-74.6098F, -20.264F, -8.432F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-74.6098F, -20.264F, -20.432F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-74.6098F, -8.264F, -20.432F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-74.6098F, 3.736F, -20.432F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-74.6098F, 3.736F, -8.432F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 348)
            .mirror()
            .addBox(-74.6098F, 3.736F, 3.568F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-78.3902F, -4.736F, 2.432F)
      );
      PartDefinition part_02_11 = part_02_10.addOrReplaceChild(
         "part_02_11",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-53.1394F, -18.3126F, -17.7185F, 57.6216F, 34.5729F, 23.0486F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-41.6151F, -6.7883F, 5.3301F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-53.1394F, -6.7883F, 5.3301F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-53.1394F, 4.2117F, 5.3301F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-53.1394F, -6.7883F, -29.2428F, 57.6216F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-8.9397F, -19.264F, -18.432F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-24.9397F, -19.264F, -18.432F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-40.9397F, -19.264F, -18.432F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-53.9397F, -19.264F, -18.432F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-53.9397F, -19.264F, -7.432F, 13.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-4.9397F, -19.264F, -7.432F, 12.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-16.9397F, -19.264F, -7.432F, 12.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-41.9397F, -19.264F, -7.432F, 13.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-28.9397F, -19.264F, -7.432F, 12.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-8.9397F, -7.264F, -29.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-8.9397F, 4.736F, -29.432F, 16.0F, 12.0F, 19.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-24.9397F, 4.736F, -29.432F, 16.0F, 12.0F, 19.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-24.9397F, -7.264F, -29.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-53.9397F, -7.264F, -29.432F, 13.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-53.9397F, 4.736F, -29.432F, 13.0F, 12.0F, 19.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-53.9397F, 4.736F, -10.432F, 12.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-40.9397F, -7.264F, -29.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-40.9397F, 4.736F, -29.432F, 16.0F, 12.0F, 19.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-41.9397F, 4.736F, -10.432F, 17.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-24.9397F, 4.736F, -10.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 430)
            .addBox(-8.9397F, 4.736F, -10.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(428, 430)
            .mirror()
            .addBox(-41.9397F, 4.736F, 5.568F, 17.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-53.9397F, 4.736F, 5.568F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-53.9397F, -6.564F, 5.568F, 12.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-41.9397F, -6.564F, 5.568F, 17.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-24.9397F, -6.564F, 5.568F, 16.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-8.9397F, -6.564F, 5.568F, 16.0F, 11.3F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-24.9397F, 4.736F, 5.568F, 16.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(428, 430)
            .mirror()
            .addBox(-8.9397F, 4.736F, 5.568F, 16.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-81.6701F, -1.0F, -2.0F)
      );
      PartDefinition part_01_2 = part_02_11.addOrReplaceChild(
         "part_01_2",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-46.6151F, -18.3126F, 5.8058F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-35.0908F, -6.7883F, 17.3301F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(348, 352)
            .addBox(-36.3182F, -19.264F, 4.568F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-36.3182F, -7.264F, -17.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-36.3182F, 4.736F, -17.432F, 16.0F, 12.0F, 19.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-20.3182F, 4.736F, -17.432F, 19.0F, 12.0F, 19.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-20.3182F, 4.736F, 1.568F, 19.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-36.3182F, 4.736F, 1.568F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 352)
            .addBox(-20.3182F, -19.264F, 4.568F, 19.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-20.3182F, -7.264F, -17.432F, 19.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-46.6151F, 16.2603F, -5.7185F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-50.3182F, -7.264F, -17.432F, 14.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-50.3182F, 4.736F, -17.432F, 14.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-46.6151F, -6.7883F, -17.2428F, 46.0972F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .texOffs(348, 352)
            .addBox(-50.3182F, -19.264F, 4.568F, 14.0F, 16.0F, 13.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-50.3182F, -7.264F, -3.432F, 14.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-36.3182F, -7.264F, -3.432F, 14.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(348, 350)
            .addBox(-22.3182F, -7.264F, -3.432F, 21.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-52.6215F, 0.0F, -12.0F)
      );
      PartDefinition part_01_3 = part_01_2.addOrReplaceChild(
         "part_01_3",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-49.6151F, -22.3126F, -1.1942F, 46.0972F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-38.2209F, 0.736F, -24.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-23.2209F, 0.736F, -24.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-23.2209F, -11.264F, -24.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-49.6151F, -10.7883F, 10.3301F, 57.6216F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-49.6151F, 12.2603F, -12.7185F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-38.0908F, -10.7883F, -24.2428F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-49.6151F, 0.736F, -24.2428F, 11.5243F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-49.6151F, -10.7883F, -12.7185F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-38.2209F, -11.264F, -24.432F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-43.0972F, 4.0F, 7.0F)
      );
      PartDefinition part_01_4 = part_01_3.addOrReplaceChild(
         "part_01_4",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-24.5665F, -29.3126F, -2.1942F, 23.0486F, 34.5729F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-47.5665F, -17.7883F, -2.1942F, 23.0486F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-47.6151F, -17.7883F, 9.3301F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-47.6151F, -6.264F, 9.3301F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-47.6151F, 5.2603F, -13.7185F, 46.0972F, 11.5243F, 23.0486F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-24.5665F, -6.264F, -25.2428F, 23.0486F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-47.6151F, -17.7883F, -13.7185F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offset(-48.0972F, 7.0F, 1.0F)
      );
      PartDefinition part_01_5 = part_01_4.addOrReplaceChild(
         "part_01_5",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-42.6151F, -17.7883F, 4.3301F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-42.6151F, -6.264F, 4.3301F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-42.6151F, 5.2603F, -7.1942F, 46.0972F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-8.0421F, 5.2603F, -18.7185F, 11.5243F, 11.5243F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-42.6151F, -17.7883F, -7.1941F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-42.6151F, -17.7883F, -18.7185F, 46.0972F, 23.0486F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offset(-51.0972F, 0.0F, 5.0F)
      );
      PartDefinition part_01_6 = part_01_5.addOrReplaceChild(
         "part_01_6",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-37.0908F, -11.7883F, -17.7185F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-37.0908F, -0.264F, 5.3301F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offset(-40.0972F, -6.0F, -1.0F)
      );
      PartDefinition part_01_7 = part_01_6.addOrReplaceChild(
         "part_01_7",
         CubeListBuilder.create().texOffs(0, 0).addBox(-35.0908F, -10.7883F, -11.7185F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F)),
         PartPose.offset(-36.5729F, -1.0F, -6.0F)
      );
      PartDefinition part_01_8 = part_01_7.addOrReplaceChild(
         "part_01_8",
         CubeListBuilder.create().texOffs(0, 0).addBox(-38.0908F, -12.7883F, -10.7185F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F)),
         PartPose.offset(-35.7116F, 2.0F, -1.0F)
      );
      PartDefinition part_01_9 = part_01_8.addOrReplaceChild(
         "part_01_9",
         CubeListBuilder.create().texOffs(0, 0).addBox(-39.0908F, -13.7883F, -11.7185F, 38.7116F, 23.0486F, 23.0486F, new CubeDeformation(0.0F)),
         PartPose.offset(-37.7116F, 1.0F, 1.0F)
      );
      PartDefinition part_01_10 = part_01_9.addOrReplaceChild(
         "part_01_10",
         CubeListBuilder.create().texOffs(0, 0).addBox(-33.0908F, -11.7883F, -12.7185F, 34.5729F, 23.0486F, 23.0486F, new CubeDeformation(0.0F)),
         PartPose.offset(-40.5729F, -2.0F, 1.0F)
      );
      PartDefinition part_01_11 = part_01_10.addOrReplaceChild(
         "part_01_11",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-35.0908F, -15.7883F, -2.1942F, 38.7116F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-35.0908F, -4.264F, -13.7185F, 38.7116F, 11.5243F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offset(-36.7116F, 4.0F, 1.0F)
      );
      PartDefinition part_01_12 = part_01_11.addOrReplaceChild(
         "part_01_12",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-34.0908F, -14.7883F, 0.8058F, 34.5729F, 23.0486F, 11.5243F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-34.0908F, -3.264F, -10.7185F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offset(-35.5729F, -1.0F, -3.0F)
      );
      PartDefinition part_01_13 = part_01_12.addOrReplaceChild(
         "part_01_13",
         CubeListBuilder.create().texOffs(0, 0).addBox(-33.2294F, -7.0261F, -8.1942F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offset(-35.4343F, 3.7621F, 9.0F)
      );
      PartDefinition part_01_14 = part_01_13.addOrReplaceChild(
         "part_01_14",
         CubeListBuilder.create().texOffs(0, 0).addBox(-36.2294F, -7.0261F, -5.1942F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offset(-31.5729F, 0.0F, -3.0F)
      );
      PartDefinition part_01_15 = part_01_14.addOrReplaceChild(
         "part_01_15",
         CubeListBuilder.create().texOffs(0, 0).addBox(-35.2294F, -6.0261F, -5.1942F, 34.5729F, 11.5243F, 11.5243F, new CubeDeformation(0.0F)),
         PartPose.offset(-35.5729F, -1.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 512, 512);
   }

   public ModelPart bodyRoot() {
      return this.tentacles;
   }

   private void collectChains() {
      if (this.chains.isEmpty()) {
         for (ModelPart limb : children(this.tentacles).values()) {
            List<ModelPart> run = new ArrayList<>();
            ModelPart cur = limb;

            while (true) {
               run.add(cur);
               Collection<ModelPart> kids = children(cur).values();
               if (kids.size() != 1) {
                  if (run.size() >= 2) {
                     this.chains.add(run);
                  }
                  break;
               }

               cur = kids.iterator().next();
            }
         }

         if (this.chains.isEmpty()) {
            this.chains.add(new ArrayList<>(this.tentacles.getAllParts()));
         }
      }
   }

   private static Map<String, ModelPart> children(ModelPart part) {
      return ((ModelPartAccessor)(Object)part).getChildren();
   }

   public void setupAnim(WitherStormRenderState state) {
      this.tentacles.getAllParts().forEach(ModelPart::resetPose);
      this.collectChains();

      for (int c = 0; c < this.chains.size(); c++) {
         float droop = CollapseAnim.droop(state.collapseTicks);
         WitherStormTentacles5.smallIdle(this.chains.get(c), state.idleTimeTicks, c, 1.0F - droop);
         WitherStormTentacles5.limp(this.chains.get(c), droop, state.idleTimeTicks, c, state.groundBias[c % state.groundBias.length]);
      }
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.tentacles.render(poseStack, buffer, packedLight, packedOverlay);
   }
}
