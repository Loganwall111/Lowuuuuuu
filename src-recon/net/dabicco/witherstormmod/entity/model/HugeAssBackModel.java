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

public class HugeAssBackModel extends EntityModel<WitherStormRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "huge_ass_back"), "main"
   );
   private final ModelPart root;
   private final ModelPart hugeassback;

   public HugeAssBackModel(ModelPart root) {
      super(root);
      this.root = root;
      this.hugeassback = root.getChild("hugeassback");
   }

   public void setupAnim(WitherStormRenderState state) {
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition hugeassback = make_hugeassback(partdefinition);
      PartDefinition bone65 = make_bone65(hugeassback);
      PartDefinition bone236 = make_bone236(bone65);
      PartDefinition bone237 = make_bone237(bone236);
      PartDefinition bone238 = make_bone238(bone65);
      PartDefinition bone239 = make_bone239(bone238);
      PartDefinition bone240 = make_bone240(bone65);
      PartDefinition bone241 = make_bone241(bone240);
      PartDefinition bone242 = make_bone242(bone65);
      PartDefinition bone243 = make_bone243(bone242);
      PartDefinition bone244 = make_bone244(bone65);
      PartDefinition bone245 = make_bone245(bone244);
      PartDefinition bone246 = make_bone246(bone65);
      PartDefinition bone247 = make_bone247(bone246);
      PartDefinition bone145 = make_bone145(bone65);
      PartDefinition bone146 = make_bone146(bone145);
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   private static PartDefinition make_hugeassback(PartDefinition partdefinition) {
      return partdefinition.addOrReplaceChild("hugeassback", CubeListBuilder.create(), PartPose.offset(0.0F, -15.0F, 0.0F));
   }

   private static PartDefinition make_bone65(PartDefinition hugeassback) {
      return hugeassback.addOrReplaceChild("bone65", CubeListBuilder.create(), PartPose.offset(12.0F, 83.0F, -6.0F));
   }

   private static PartDefinition make_bone236(PartDefinition bone65) {
      return bone65.addOrReplaceChild(
         "bone236",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(10.0244F, -113.81F, -80.759F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -117.5039F, -84.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -106.4493F, -84.4529F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -117.5039F, -88.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -128.6127F, -88.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -128.6127F, -91.8407F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -117.531F, -91.8407F, 3.6939F, 77.5721F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -36.265F, -91.8407F, 44.3945F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -39.9589F, -67.8303F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -56.5815F, -91.8407F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -51.0406F, -84.4529F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -65.8163F, -104.7694F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -69.5102F, -114.0042F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-13.986F, -28.8772F, -95.5346F, 16.6226F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-32.4556F, -21.4894F, -95.5346F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -17.7954F, -91.8407F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.2208F, -17.7954F, -88.1468F, 18.4695F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -17.7954F, -84.4529F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -17.7684F, -80.759F, 40.7006F, 7.3608F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -10.4076F, -80.759F, 3.6939F, 3.6939F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.112F, -3.0198F, -69.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4181F, -3.0198F, -65.9834F, 7.3878F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, 0.6741F, -58.5956F, 7.3878F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, 0.6741F, -51.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.4237F, -3.0198F, -51.2077F, 11.2846F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -10.3806F, -69.6367F, 29.6189F, 7.3608F, 22.0958F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -3.0198F, -64.1364F, 7.3878F, 3.6939F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, 4.368F, -51.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -10.4076F, -80.759F, 3.6939F, 3.6939F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -14.1015F, -84.4529F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -21.4894F, -88.1468F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -102.7283F, -88.1468F, 3.6939F, 81.2389F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -62.1223F, -95.5346F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -65.8163F, -95.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -89.8267F, -125.0859F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -62.1224F, -80.759F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -91.6736F, -106.6164F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -106.4493F, -114.0042F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -143.3883F, -77.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -124.9188F, -80.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -99.0614F, -121.392F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -102.7553F, -117.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -102.7553F, -117.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -95.3675F, -125.0859F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -110.1432F, -121.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -110.1432F, -117.6981F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -102.7553F, -128.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -102.7553F, -125.0859F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -95.3675F, -128.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -84.2858F, -125.0859F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -76.898F, -121.392F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -80.5919F, -121.392F, 7.3878F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -80.5919F, -125.0859F, 11.0817F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -69.5102F, -110.3103F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -65.8162F, -110.3103F, 7.3878F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -62.1224F, -114.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -73.2041F, -121.392F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-25.0678F, -58.4284F, -121.392F, 5.5409F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -47.3467F, -102.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -28.8772F, -99.2285F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -58.4284F, -99.2285F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -54.7345F, -117.6981F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -54.7345F, -114.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -51.0406F, -110.3103F, 3.6939F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -65.8162F, -106.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -58.4284F, -106.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -62.1223F, -106.6164F, 7.3878F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -58.4284F, -102.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -62.1223F, -102.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -39.9589F, -106.6164F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -32.5711F, -102.9225F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -28.8772F, -99.2285F, 29.6189F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -32.5711F, -106.6164F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -36.265F, -110.3103F, 29.6189F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -36.265F, -114.0042F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -39.9589F, -117.6981F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -39.9589F, -99.2285F, 40.7006F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -32.5711F, -95.5346F, 18.4695F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -45.4998F, -95.5346F, 14.7756F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -62.1224F, -102.9225F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -65.8163F, -110.3103F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -58.4284F, -114.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -62.1224F, -117.6981F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -73.2041F, -102.9225F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -80.5919F, -125.0859F, 11.0817F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -73.2041F, -121.392F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -65.8163F, -117.6981F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -84.2858F, -110.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -87.9797F, -121.392F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -87.9797F, -125.0859F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -99.0614F, -102.9225F, 3.6939F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -106.4493F, -99.2285F, 3.6939F, 48.0208F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -113.8371F, -102.9225F, 3.6939F, 25.8574F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -110.1431F, -95.5346F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -128.6127F, -95.5346F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -132.3066F, -99.2285F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -128.6127F, -102.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -128.6127F, -110.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -124.9188F, -102.9225F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -128.6127F, -121.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -117.531F, -114.0042F, 3.6939F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -113.8371F, -121.392F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -113.8371F, -125.0859F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -123.0718F, -128.7798F, 29.6189F, 33.2452F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -104.5383F, -121.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -143.3883F, -121.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -77.8983F, -121.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-32.4556F, -97.2145F, -128.7798F, 12.9287F, 46.1739F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -89.8267F, -128.7798F, 16.6902F, 42.48F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -110.1432F, -110.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -110.1432F, -106.6164F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0244F, -117.5039F, -77.0651F, 3.6939F, 33.2452F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.7183F, -106.4222F, -73.3712F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0244F, -117.531F, -77.0651F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0244F, -111.9631F, -62.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -121.2249F, -51.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -124.9188F, -51.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -124.9188F, -54.9016F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -124.9188F, -65.9834F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -128.6127F, -69.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -128.6127F, -62.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -132.3066F, -62.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -136.0005F, -62.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -139.6944F, -65.9834F, 7.3878F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -143.3883F, -73.3712F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -143.3883F, -69.6773F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -147.0822F, -80.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -143.3883F, -95.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -139.6944F, -95.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.2208F, -143.3883F, -106.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.9147F, -147.0822F, -106.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -139.6944F, -106.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -124.9188F, -117.6981F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -121.2249F, -121.392F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -91.6736F, -143.5555F, 5.6085F, 14.7756F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -106.4493F, -132.4737F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-32.4556F, -95.3675F, -132.4737F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -80.5919F, -132.4737F, 16.6902F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -99.0614F, -136.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-32.4556F, -87.9797F, -136.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -80.5919F, -143.5555F, 3.6939F, 7.3878F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -69.5102F, -143.5555F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -87.9797F, -139.8615F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -76.898F, -139.8615F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-32.4556F, -73.2041F, -136.1676F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -62.1223F, -136.1676F, 16.6902F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -47.3467F, -121.392F, 22.2311F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -47.3467F, -125.0859F, 9.3024F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-32.4556F, -58.4284F, -139.8615F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -62.1224F, -143.5555F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -58.4284F, -139.8615F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-36.1495F, -62.1223F, -147.2494F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -113.8371F, -132.4737F, 5.6085F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -106.4493F, -136.1676F, 5.6085F, 7.3878F, 9.2348F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -76.898F, -147.2494F, 5.6085F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -73.2041F, -150.9433F, 5.6085F, 7.3878F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -65.8163F, -139.8615F, 9.3024F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -99.0614F, -139.8615F, 5.6085F, 12.9287F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -117.531F, -128.7798F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -136.0005F, -106.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -132.3066F, -117.6981F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -136.0005F, -121.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.2208F, -139.6944F, -121.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.9147F, -143.3883F, -117.6981F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.9147F, -139.6944F, -125.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -136.0005F, -128.7798F, 18.5372F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -143.3883F, -128.7798F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -147.0823F, -110.3103F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -150.7762F, -102.9225F, 11.1493F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -150.7762F, -84.4529F, 14.8433F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -150.7762F, -65.9834F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -154.4701F, -65.9834F, 11.1493F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.2208F, -136.0005F, -125.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.9147F, -132.3066F, -132.4737F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.9147F, -132.3066F, -132.4737F, 11.0817F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -132.3066F, -132.4737F, 14.8433F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.6086F, -132.3066F, -132.4737F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.2208F, -124.9188F, -132.4737F, 11.0817F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -121.2249F, -106.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -139.6944F, -106.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.2208F, -150.7762F, -80.759F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.833F, -143.3883F, -91.8407F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-19.5269F, -147.0822F, -88.1468F, 3.6939F, 3.6939F, 29.5513F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.9147F, -147.0822F, -95.5346F, 7.3878F, 3.6939F, 48.0208F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.758F, -147.0822F, -95.5346F, 18.5372F, 3.6939F, 33.2452F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -128.6127F, -77.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -128.6127F, -80.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -124.9188F, -95.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -132.3066F, -84.4529F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -132.3066F, -88.1468F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -132.3066F, -91.8407F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -136.0005F, -95.5346F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -121.2249F, -80.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -124.9188F, -84.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -132.3066F, -77.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -136.0005F, -80.759F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -136.0005F, -88.1468F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.1391F, -139.6944F, -88.1468F, 3.6939F, 14.7756F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.4452F, -139.6944F, -84.4529F, 3.6939F, 3.6939F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -136.0005F, -77.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -136.0005F, -69.6773F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -132.3066F, -69.6773F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.7513F, -139.6944F, -77.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -113.8371F, -54.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -121.2249F, -69.6773F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -117.531F, -77.0651F, 3.6939F, 20.3165F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0244F, -97.1874F, -62.2895F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0244F, -100.8813F, -58.5956F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0244F, -87.9527F, -80.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -87.9527F, -84.4529F, 3.6939F, 11.0817F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -99.0344F, -84.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -99.0344F, -88.1468F, 3.6939F, 29.5242F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -91.6466F, -91.8407F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -117.531F, -80.759F, 3.6939F, 29.5513F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6365F, -76.8709F, -80.759F, 7.3878F, 7.3878F, 22.1364F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0244F, -76.8709F, -73.3712F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -65.7892F, -73.3712F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -54.7075F, -73.3712F, 3.6939F, 14.7486F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -71.3301F, -73.3712F, 3.6939F, 16.6226F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -73.2041F, -62.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.0244F, -73.177F, -69.6773F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6365F, -69.4831F, -77.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0303F, -69.4831F, -80.7319F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0303F, -65.8163F, -77.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -65.8162F, -84.4529F, 3.6939F, 48.0208F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -54.7075F, -69.6773F, 11.0817F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -80.5648F, -58.5956F, 3.6939F, 14.7486F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -76.898F, -51.2077F, 3.6939F, 33.1911F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -110.1432F, -54.9016F, 3.6939F, 29.5783F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -84.2587F, -58.5956F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -73.177F, -58.5956F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -51.0136F, -54.9016F, 3.6939F, 31.3712F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.4231F, -17.7954F, -73.3712F, 15.0597F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -14.1015F, -62.2895F, 3.6939F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -10.4076F, -51.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -17.7954F, -77.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -39.9318F, -77.038F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -39.9318F, -69.6502F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -14.0745F, -65.9563F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -39.9318F, -73.3441F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -28.8501F, -69.6502F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -28.8501F, -65.9563F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -28.8501F, -62.2624F, 3.6939F, 22.1634F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -36.2379F, -58.5685F, 3.6939F, 36.9391F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -10.4076F, -77.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -63.9423F, -54.8746F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -28.8501F, -54.8746F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -25.1833F, -51.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.0574F, -39.9589F, -73.3712F, 3.6939F, 22.1634F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.6366F, -54.7075F, -62.2895F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.3304F, -73.177F, -54.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-12.0F, -152.0F, 42.0F, 0.0F, 1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone237(PartDefinition bone236) {
      return bone236.addOrReplaceChild(
         "bone237",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(-48.5849F, 99.9683F, -23.3297F, 7.4078F, 44.4468F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.881F, 133.3034F, -19.6258F, 7.4078F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 140.7112F, -19.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 137.0073F, -12.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 133.3034F, -19.6258F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 133.3034F, -19.6258F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 137.0073F, -23.3297F, 14.8156F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 103.6723F, -27.0336F, 3.7039F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 59.2255F, -27.0336F, 18.5195F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 85.1528F, -30.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 70.3372F, -30.7375F, 14.8156F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 59.2255F, -30.7375F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.7693F, 77.745F, -27.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 74.0411F, -27.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 66.6333F, -27.0336F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, 62.9294F, -27.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.2498F, 40.706F, -27.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.5459F, 48.1138F, -27.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.881F, 66.6333F, -30.7375F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 66.6333F, -30.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.7693F, 66.6333F, -30.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.7693F, 66.6333F, -34.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 62.9294F, -34.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.3615F, 55.5216F, -30.7375F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 22.1865F, -30.7375F, 22.2234F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 11.0748F, -27.0336F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 7.3709F, -23.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 3.667F, -19.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 3.667F, -23.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, -0.0369F, -19.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 14.7787F, -27.0336F, 18.5195F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 11.0748F, -27.0336F, 7.4078F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 7.3709F, -27.0336F, 22.2234F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 7.3709F, -30.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 3.667F, -27.0336F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, -0.0369F, -23.3297F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.3615F, -11.1486F, -8.5141F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, -14.8525F, -1.1064F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, -14.8525F, 6.3014F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, -14.8525F, 6.3014F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.881F, -18.5564F, 10.0053F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, -18.5564F, 6.3014F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, -18.5564F, 10.0053F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, -18.5564F, 13.7092F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, -3.7408F, -19.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, -3.7408F, -15.9219F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, -0.0369F, -15.9219F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, -3.7408F, -12.218F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, -7.4447F, -8.5141F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, -11.1486F, -4.8102F, 29.6312F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, -11.1486F, -1.1064F, 29.6312F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, -7.4447F, -8.5141F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, 11.0748F, -27.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.2498F, 22.1865F, -27.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.881F, 18.4826F, -30.7375F, 25.9273F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 22.1865F, -30.7375F, 25.9273F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 55.5216F, -34.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 33.2982F, -38.1453F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.881F, 40.706F, -34.4414F, 22.2234F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 25.8904F, -34.4414F, 18.5195F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 22.1865F, -34.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.7693F, 18.4826F, -34.4414F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 37.0021F, -34.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 51.8177F, -34.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.881F, 37.0021F, -34.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 55.5216F, -30.7375F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, 40.706F, -30.7375F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 37.0021F, -30.7375F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 25.8904F, -34.4414F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.881F, 99.9684F, -27.0336F, 3.7039F, 33.3351F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 99.9684F, -27.0336F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 99.9684F, -27.0336F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.7693F, 99.9684F, -27.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 99.9684F, -30.7375F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 99.9684F, -27.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 125.8956F, -27.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.7693F, 129.5995F, -23.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.7693F, 122.1917F, -23.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 111.08F, -23.3297F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-33.7693F, 111.08F, -19.6258F, 22.2234F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 114.784F, -12.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 107.3761F, -12.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-49.6949F, 49.6148F, 19.2651F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-49.6949F, 2.9948F, 19.2651F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, -3.6652F, 19.2651F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-49.6949F, 96.2645F, 19.2651F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.3615F, 79.5969F, 19.2651F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5294F, 37.0021F, 19.2651F, 27.7792F, 37.039F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.2498F, 96.2645F, -19.6258F, 11.1117F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 103.6722F, -19.6258F, 3.7039F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 107.3761F, -8.5141F, 3.7039F, 3.7039F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 107.3761F, -1.1064F, 3.7039F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, 118.4878F, -8.5141F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, 107.3761F, -19.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, 103.6722F, -19.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 99.9684F, -19.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, 92.5606F, -19.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.842F, 85.1528F, -19.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.842F, 77.745F, -19.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.842F, 98.1164F, -15.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 48.1138F, -27.0336F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 48.1138F, -30.7375F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 85.1528F, -15.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 88.8567F, -15.9219F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 92.5606F, -12.218F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 96.2645F, -8.5141F, 3.7039F, 11.1117F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 85.1528F, -4.8102F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 66.6333F, -4.8102F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 85.1528F, -1.1064F, 7.4078F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 96.2645F, -1.1064F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 85.1528F, 2.5975F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 85.1528F, 2.5975F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 85.1528F, -1.1064F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 62.9294F, -1.1064F, 7.4078F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(25.4931F, 85.1528F, 10.0053F, 3.7039F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(25.4931F, 74.0411F, 2.5975F, 11.1117F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(29.197F, 59.2255F, 6.3014F, 11.1117F, 14.8156F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(40.3086F, 66.6333F, 10.0053F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(40.3086F, 55.5216F, 10.0053F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(40.3086F, 55.5216F, 13.7092F, 3.7039F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(36.6048F, 48.1138F, 10.0053F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(32.9009F, 37.0021F, 10.0053F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(29.197F, 29.5943F, 10.0053F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(29.197F, 48.1138F, 6.3014F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(36.6048F, 77.745F, 6.3014F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(36.6048F, 74.0411F, 10.0053F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(40.3086F, 74.0411F, 10.0053F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.0125F, 74.0411F, 10.0053F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(29.197F, 55.5216F, 2.5975F, 3.7039F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 99.9684F, 2.5975F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 96.2645F, 2.5975F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 99.9684F, 6.3014F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 96.2645F, 6.3014F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 88.8567F, -4.8102F, 3.7039F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 96.2645F, -4.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 81.4489F, -19.6258F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 77.745F, -15.9219F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 74.0411F, -15.9219F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 62.9294F, -19.6258F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 66.6333F, -15.9219F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 70.3372F, -4.8102F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 77.745F, -8.5141F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 74.0411F, -19.6258F, 11.1117F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 62.9294F, -19.6258F, 14.8156F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 88.8567F, -8.5141F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 92.5606F, -15.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 103.6722F, -23.3297F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.3615F, 99.9684F, -23.3297F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 96.2645F, -23.3297F, 40.7429F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, 77.745F, -23.3297F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.842F, 70.3372F, -23.3297F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.5459F, 62.9294F, -23.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 59.2255F, -23.3297F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.5459F, 51.8177F, -23.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.2498F, 44.4099F, -23.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, 37.0021F, -23.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 33.2982F, -23.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.842F, 25.8904F, -19.6258F, 11.1117F, 18.5195F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, 29.5943F, -19.6258F, 11.1117F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 33.2982F, -19.6258F, 11.1117F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 29.5943F, -19.6258F, 3.7039F, 33.3351F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 48.1138F, -19.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 51.8177F, -15.9219F, 11.1117F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 44.4099F, -12.218F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 37.0021F, -4.8102F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 25.8904F, -4.8102F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 25.8904F, -8.5141F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 22.1865F, -12.218F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 22.1865F, -8.5141F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 22.1865F, -15.9219F, 18.5195F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 18.4826F, -15.9219F, 7.4078F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 18.4826F, -12.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 14.7787F, -12.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 11.0748F, -12.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 7.3709F, -12.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 11.0748F, -15.9219F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.842F, 14.7787F, -19.6258F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 7.3709F, -12.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.842F, 14.7787F, -15.9219F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 14.7787F, -12.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, 25.8904F, -12.218F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.4342F, 25.8904F, -15.9219F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 40.706F, -15.9219F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 37.0021F, -12.218F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 33.2982F, -15.9219F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 33.2982F, -12.218F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 33.2982F, -8.5141F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 40.706F, -15.9219F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 44.4099F, -19.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 40.706F, -19.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 33.2982F, -15.9219F, 11.1117F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(14.3814F, 55.5216F, -12.218F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.0853F, 55.5216F, -8.5141F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.2498F, 18.4826F, -23.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, 7.3709F, -23.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.2498F, 7.3709F, -19.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, 3.667F, -19.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 3.667F, -23.3297F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.3615F, -0.0369F, -19.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, -3.7408F, -15.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, -0.0369F, -15.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, -3.7408F, -12.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.5459F, 3.667F, -15.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.5459F, -0.0369F, -12.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.5459F, -3.7408F, -8.5141F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.5459F, -7.4447F, -4.8102F, 7.4078F, 11.1117F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, -3.7408F, -4.8102F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, -7.4447F, -1.1064F, 11.1117F, 7.4078F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, -11.1486F, 6.3014F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(3.2697F, -7.4447F, 2.5975F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.1381F, -0.0369F, -8.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, 3.667F, -8.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 11.0748F, -8.5141F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 14.7787F, 6.3014F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(10.6775F, 7.3709F, -8.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, -0.0369F, -4.8102F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(6.9736F, -3.7408F, -1.1064F, 14.8156F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, -3.7408F, 2.5975F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, -0.0369F, -1.1064F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 3.667F, -4.8102F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(25.4931F, -3.7408F, 6.3014F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(25.4931F, -0.0369F, 2.5975F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(25.4931F, 3.667F, -1.1064F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(25.4931F, 7.3709F, -4.8102F, 7.4078F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(25.4931F, 22.1865F, 13.7092F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(32.9008F, 37.0021F, 13.7092F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(36.6048F, 44.4099F, 13.7092F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(40.3086F, 51.8177F, 13.7092F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(29.197F, 29.5943F, 13.7092F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(21.7892F, 14.7787F, -4.8102F, 7.4078F, 33.3351F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.842F, 3.667F, -12.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.2498F, 44.4099F, -23.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 81.4489F, -23.3297F, 44.4468F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 70.3372F, -23.3297F, 14.8156F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.3615F, 111.08F, -12.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 114.784F, -19.6258F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 118.4878F, -19.6258F, 22.2234F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 118.4878F, -15.9219F, 25.9273F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 118.4878F, -12.218F, 18.5195F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-41.1771F, 122.1917F, -15.9219F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.9537F, 118.4878F, -4.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-22.6576F, 122.1917F, -4.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.881F, 122.1917F, -12.218F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-30.0654F, 107.3761F, -23.3297F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-26.3615F, 107.3761F, -23.3297F, 22.2234F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 122.1917F, -23.3297F, 18.5195F, 14.8156F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-37.4732F, 140.7112F, -12.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 144.4151F, -19.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 133.3034F, -15.9219F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-48.5849F, 133.3034F, -8.5141F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-20.3056F, -136.5242F, 0.6997F, 0.0F, -1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone238(PartDefinition bone65) {
      return bone65.addOrReplaceChild(
         "bone238",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(31.0244F, -82.81F, -31.759F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -86.5039F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -75.4493F, -35.4529F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -86.5039F, -39.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -97.6127F, -39.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -97.6127F, -42.8407F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -86.531F, -42.8407F, 3.6939F, 77.5721F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -42.8407F, 44.3945F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -18.8303F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -25.5815F, -42.8407F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -20.0406F, -35.4529F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -34.8163F, -55.7694F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -38.5102F, -65.0042F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.014F, 2.1228F, -46.5346F, 16.6226F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, 9.5106F, -46.5346F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, 13.2046F, -42.8407F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, 13.2046F, -39.1468F, 18.4695F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 13.2046F, -35.4529F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 13.2316F, -31.759F, 40.7006F, 7.3608F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 20.5924F, -31.759F, 3.6939F, 3.6939F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.888F, 27.9802F, -20.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5819F, 27.9802F, -16.9834F, 7.3878F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 31.6741F, -9.5956F, 7.3878F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, 31.6741F, -2.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.4237F, 27.9802F, -2.2077F, 11.2846F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 20.6194F, -20.6367F, 29.6189F, 7.3608F, 22.0958F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, 27.9802F, -15.1364F, 7.3878F, 3.6939F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 35.368F, -2.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 20.5924F, -31.759F, 3.6939F, 3.6939F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 16.8985F, -35.4529F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 9.5106F, -39.1468F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -71.7283F, -39.1468F, 3.6939F, 81.2389F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -31.1223F, -46.5346F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -34.8163F, -46.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -58.8267F, -76.0859F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -31.1224F, -31.759F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -60.6736F, -57.6164F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -75.4493F, -65.0042F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -28.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -93.9188F, -31.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -68.0614F, -72.392F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -71.7553F, -68.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -71.7553F, -68.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -64.3675F, -76.0859F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -79.1432F, -72.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -79.1432F, -68.6981F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -71.7553F, -79.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -71.7553F, -76.0859F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -64.3675F, -79.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -53.2858F, -76.0859F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -45.898F, -72.392F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -49.5919F, -72.392F, 7.3878F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -49.5919F, -76.0859F, 11.0817F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -38.5102F, -61.3103F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8162F, -61.3103F, 7.3878F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -31.1224F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -42.2041F, -72.392F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.0678F, -27.4284F, -72.392F, 5.5409F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -16.3467F, -53.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 2.1228F, -50.2285F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -27.4284F, -50.2285F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -23.7345F, -68.6981F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -23.7345F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -20.0406F, -61.3103F, 3.6939F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8162F, -57.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -27.4284F, -57.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -31.1223F, -57.6164F, 7.3878F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -27.4284F, -53.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -31.1223F, -53.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -8.9589F, -57.6164F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -1.5711F, -53.9225F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 2.1228F, -50.2285F, 29.6189F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -1.5711F, -57.6164F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -61.3103F, 29.6189F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -65.0042F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -68.6981F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -50.2285F, 40.7006F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -1.5711F, -46.5346F, 18.4695F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -14.4998F, -46.5346F, 14.7756F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -31.1224F, -53.9225F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8163F, -61.3103F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -27.4284F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -31.1224F, -68.6981F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -42.2041F, -53.9225F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -49.5919F, -76.0859F, 11.0817F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -42.2041F, -72.392F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8163F, -68.6981F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -53.2858F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -56.9797F, -72.392F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -56.9797F, -76.0859F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -68.0614F, -53.9225F, 3.6939F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -75.4493F, -50.2285F, 3.6939F, 48.0208F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -82.8371F, -53.9225F, 3.6939F, 25.8574F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -79.1431F, -46.5346F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -97.6127F, -46.5346F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -101.3066F, -50.2285F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -97.6127F, -53.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -97.6127F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -93.9188F, -53.9225F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -97.6127F, -72.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -86.531F, -65.0042F, 3.6939F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -82.8371F, -72.392F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -82.8371F, -76.0859F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -92.0718F, -79.7798F, 29.6189F, 33.2452F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -73.5383F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -112.3883F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -46.8983F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -66.2145F, -79.7798F, 12.9287F, 46.1739F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -58.8267F, -79.7798F, 16.6902F, 42.48F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -79.1432F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -79.1432F, -57.6164F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -86.5039F, -28.0651F, 3.6939F, 33.2452F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(34.7183F, -75.4222F, -24.3712F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -86.531F, -28.0651F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -80.9631F, -13.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -2.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -93.9188F, -2.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -5.9016F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -16.9834F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -97.6127F, -20.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -97.6127F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -101.3066F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -105.0005F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -108.6944F, -16.9834F, 7.3878F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -24.3712F, 3.6939F, 3.6939F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -112.3883F, -20.6773F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -116.0823F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -112.3883F, -46.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -108.6944F, -46.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -112.3883F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -116.0823F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -108.6944F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -93.9188F, -68.6981F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -90.2249F, -72.392F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -60.6736F, -94.5555F, 5.6085F, 14.7756F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -75.4493F, -83.4737F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -64.3675F, -83.4737F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -49.5919F, -83.4737F, 16.6902F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -68.0614F, -87.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -56.9797F, -87.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -49.5919F, -94.5555F, 3.6939F, 7.3878F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -38.5102F, -94.5555F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -56.9797F, -90.8615F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -45.898F, -90.8615F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -42.2041F, -87.1676F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -31.1223F, -87.1676F, 16.6902F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -16.3467F, -72.392F, 22.2311F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -16.3467F, -76.0859F, 9.3024F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -27.4284F, -90.8615F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -31.1224F, -94.5555F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -27.4284F, -90.8615F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -31.1223F, -98.2494F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -82.8371F, -83.4737F, 5.6085F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -75.4493F, -87.1676F, 5.6085F, 7.3878F, 9.2348F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -45.898F, -98.2494F, 5.6085F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -42.2041F, -101.9433F, 5.6085F, 7.3878F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -34.8163F, -90.8615F, 9.3024F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -68.0614F, -90.8615F, 5.6085F, 12.9287F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -86.531F, -79.7798F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -105.0005F, -57.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -101.3066F, -68.6981F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -105.0005F, -72.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -108.6944F, -72.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -112.3883F, -68.6981F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -108.6944F, -76.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -105.0005F, -79.7798F, 18.5372F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -112.3883F, -79.7798F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -116.0822F, -61.3103F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -53.9225F, 11.1493F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -35.4529F, 14.8433F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -16.9834F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -123.4701F, -16.9834F, 11.1493F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -105.0005F, -76.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -101.3066F, -83.4737F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -101.3066F, -83.4737F, 11.0817F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -101.3066F, -83.4737F, 14.8433F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.6086F, -101.3066F, -83.4737F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -93.9188F, -83.4737F, 11.0817F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -90.2249F, -57.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -108.6944F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -119.7762F, -31.759F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -42.8407F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -116.0823F, -39.1468F, 3.6939F, 3.6939F, 29.5513F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -116.0823F, -46.5346F, 7.3878F, 3.6939F, 48.0208F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -116.0823F, -46.5346F, 18.5372F, 3.6939F, 33.2452F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -97.6127F, -28.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -97.6127F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -93.9188F, -46.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -101.3066F, -35.4529F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -101.3066F, -39.1468F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -101.3066F, -42.8407F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -105.0005F, -46.5346F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -101.3066F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -105.0005F, -31.759F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -105.0005F, -39.1468F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -108.6944F, -39.1468F, 3.6939F, 14.7756F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -108.6944F, -35.4529F, 3.6939F, 3.6939F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -105.0005F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -105.0005F, -20.6773F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -101.3066F, -20.6773F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -108.6944F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -82.8371F, -5.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -20.6773F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -86.531F, -28.0651F, 3.6939F, 20.3165F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -66.1874F, -13.2895F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -69.8813F, -9.5956F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -56.9527F, -31.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -56.9527F, -35.4529F, 3.6939F, 11.0817F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -68.0344F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -68.0344F, -39.1468F, 3.6939F, 29.5242F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -60.6466F, -42.8407F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -86.531F, -31.759F, 3.6939F, 29.5513F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -45.8709F, -31.759F, 7.3878F, 7.3878F, 22.1364F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -45.8709F, -24.3712F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -34.7892F, -24.3712F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -23.7075F, -24.3712F, 3.6939F, 14.7486F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -40.3301F, -24.3712F, 3.6939F, 16.6226F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -42.2041F, -13.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -42.177F, -20.6773F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -38.4831F, -28.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -38.4831F, -31.732F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -34.8163F, -28.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -34.8163F, -35.4529F, 3.6939F, 48.0208F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -23.7075F, -20.6773F, 11.0817F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -49.5648F, -9.5956F, 3.6939F, 14.7486F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -45.898F, -2.2077F, 3.6939F, 33.1911F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -79.1432F, -5.9016F, 3.6939F, 29.5783F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -53.2587F, -9.5956F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -42.177F, -9.5956F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -20.0136F, -5.9016F, 3.6939F, 31.3712F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.5769F, 13.2046F, -24.3712F, 15.0597F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 16.8985F, -13.2895F, 3.6939F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 20.5924F, -2.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 13.2046F, -28.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -8.9318F, -28.038F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -8.9318F, -20.6502F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 16.9255F, -16.9563F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -8.9318F, -24.3441F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -20.6502F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -16.9563F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -13.2624F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -5.2379F, -9.5685F, 3.6939F, 36.9391F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 20.5924F, -28.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -32.9423F, -5.8746F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -5.8746F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 5.8167F, -2.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -8.9589F, -24.3712F, 3.6939F, 22.1635F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -23.7075F, -13.2895F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -42.177F, -5.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(50.0F, -209.0F, 63.0F, 0.0F, 1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone239(PartDefinition param0) {
      return param0.addOrReplaceChild(
         "bone239",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(0.4151F, 130.9683F, -44.3297F, 7.4078F, 44.4468F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 164.3034F, -40.6258F, 7.4078F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 171.7112F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 168.0073F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 164.3034F, -40.6258F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 164.3034F, -40.6258F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 168.0073F, -44.3297F, 14.8156F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 134.6723F, -48.0336F, 3.7039F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 90.2255F, -48.0336F, 18.5195F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 116.1528F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 101.3372F, -51.7375F, 14.8156F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 90.2255F, -51.7375F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 108.745F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 105.0411F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 97.6333F, -48.0336F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 93.9294F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 71.706F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 79.1138F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 97.6333F, -51.7375F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 97.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 97.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 97.6333F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 93.9294F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 86.5216F, -51.7375F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 53.1865F, -51.7375F, 22.2234F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 42.0748F, -48.0336F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 38.3709F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 34.667F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 34.667F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 30.9631F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 45.7787F, -48.0336F, 18.5195F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 42.0748F, -48.0336F, 7.4078F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 38.3709F, -48.0336F, 22.2234F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 38.3709F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 34.667F, -48.0336F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 30.9631F, -44.3297F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 19.8514F, -29.5141F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 16.1475F, -22.1064F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 16.1475F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 16.1475F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 12.4436F, -10.9947F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 12.4436F, -14.6986F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 12.4436F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 12.4436F, -7.2908F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 27.2592F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 27.2592F, -36.9219F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 30.9631F, -36.9219F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 27.2592F, -33.218F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 23.5553F, -29.5141F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 19.8514F, -25.8102F, 29.6312F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 19.8514F, -22.1064F, 29.6312F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 23.5553F, -29.5141F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 42.0748F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 53.1865F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 49.4826F, -51.7375F, 25.9273F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 53.1865F, -51.7375F, 25.9273F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 86.5216F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 64.2982F, -59.1453F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 71.706F, -55.4414F, 22.2234F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 56.8904F, -55.4414F, 18.5195F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 53.1865F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 49.4826F, -55.4414F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 68.0021F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 82.8177F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 68.0021F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 86.5216F, -51.7375F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 71.706F, -51.7375F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 68.0021F, -51.7375F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 56.8904F, -55.4414F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 130.9684F, -48.0336F, 3.7039F, 33.3351F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 130.9684F, -48.0336F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 130.9684F, -48.0336F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 130.9684F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 130.9684F, -51.7375F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 130.9684F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 156.8956F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 160.5995F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 153.1917F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 142.08F, -44.3297F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 142.08F, -40.6258F, 22.2234F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 145.784F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 138.3761F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 80.6148F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 33.9948F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 27.3348F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 127.2645F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 110.5969F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(61.5294F, 68.0021F, -1.7349F, 27.7792F, 37.039F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 127.2645F, -40.6258F, 11.1117F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 134.6722F, -40.6258F, 3.7039F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 138.3761F, -29.5141F, 3.7039F, 3.7039F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 138.3761F, -22.1064F, 3.7039F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 149.4878F, -29.5141F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 138.3761F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 134.6722F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 130.9684F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 123.5606F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 116.1528F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 108.745F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 129.1164F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 79.1138F, -48.0336F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 79.1138F, -51.7375F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 116.1528F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 119.8567F, -36.9219F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 123.5606F, -33.218F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 127.2645F, -29.5141F, 3.7039F, 11.1117F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 116.1528F, -25.8102F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 97.6333F, -25.8102F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 116.1528F, -22.1064F, 7.4078F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 127.2645F, -22.1064F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 116.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 116.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 116.1528F, -22.1064F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 93.9294F, -22.1064F, 7.4078F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 116.1528F, -10.9947F, 3.7039F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 105.0411F, -18.4025F, 11.1117F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.1969F, 90.2255F, -14.6986F, 11.1117F, 14.8156F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 97.6333F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 86.5216F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 86.5216F, -7.2908F, 3.7039F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 79.1138F, -10.9947F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(81.9008F, 68.0021F, -10.9947F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.1969F, 60.5943F, -10.9947F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.1969F, 79.1138F, -14.6986F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 108.745F, -14.6986F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 105.0411F, -10.9947F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 105.0411F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(93.0125F, 105.0411F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.1969F, 86.5216F, -18.4025F, 3.7039F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 130.9684F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 127.2645F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 130.9684F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 127.2645F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 119.8567F, -25.8102F, 3.7039F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 127.2645F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 112.4489F, -40.6258F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 108.745F, -36.9219F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 105.0411F, -36.9219F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 93.9294F, -40.6258F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 97.6333F, -36.9219F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 101.3372F, -25.8102F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 108.745F, -29.5141F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 105.0411F, -40.6258F, 11.1117F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 93.9294F, -40.6258F, 14.8156F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 119.8567F, -29.5141F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 123.5606F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 134.6722F, -44.3297F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 130.9684F, -44.3297F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 127.2645F, -44.3297F, 40.7429F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 108.745F, -44.3297F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 101.3372F, -44.3297F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 93.9294F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 90.2255F, -44.3297F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 82.8177F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 75.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 68.0021F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 64.2982F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 56.8904F, -40.6258F, 11.1117F, 18.5195F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 60.5943F, -40.6258F, 11.1117F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 64.2982F, -40.6258F, 11.1117F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 60.5943F, -40.6258F, 3.7039F, 33.3351F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 79.1138F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 82.8177F, -36.9219F, 11.1117F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 75.4099F, -33.218F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 68.0021F, -25.8102F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 56.8904F, -25.8102F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 56.8904F, -29.5141F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 53.1865F, -33.218F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 53.1865F, -29.5141F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 53.1865F, -36.9219F, 18.5195F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 49.4826F, -36.9219F, 7.4078F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 49.4826F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 45.7787F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 42.0748F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 38.3709F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 42.0748F, -36.9219F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 45.7787F, -40.6258F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 38.3709F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 45.7787F, -36.9219F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 45.7787F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 56.8904F, -33.218F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 56.8904F, -36.9219F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 71.706F, -36.9219F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 68.0021F, -33.218F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 64.2982F, -36.9219F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 64.2982F, -33.218F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 64.2982F, -29.5141F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 71.706F, -36.9219F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 75.4099F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 71.706F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 64.2982F, -36.9219F, 11.1117F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 86.5216F, -33.218F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 86.5216F, -29.5141F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 49.4826F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 38.3709F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 38.3709F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 34.667F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 34.667F, -44.3297F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 30.9631F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 27.2592F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 30.9631F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 27.2592F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 34.667F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 30.9631F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 27.2592F, -29.5141F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 23.5553F, -25.8102F, 7.4078F, 11.1117F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 27.2592F, -25.8102F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 23.5553F, -22.1064F, 11.1117F, 7.4078F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 19.8514F, -14.6986F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 23.5553F, -18.4025F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 30.9631F, -29.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 34.667F, -29.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 42.0748F, -29.5141F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 45.7787F, -14.6986F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 38.3709F, -29.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 30.9631F, -25.8102F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 27.2592F, -22.1064F, 14.8156F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 27.2592F, -18.4025F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 30.9631F, -22.1064F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 34.667F, -25.8102F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 27.2592F, -14.6986F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 30.9631F, -18.4025F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 34.667F, -22.1064F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 38.3709F, -25.8102F, 7.4078F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 53.1865F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(81.9008F, 68.0021F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 75.4099F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 82.8177F, -7.2908F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.1969F, 60.5943F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 45.7787F, -25.8102F, 7.4078F, 33.3351F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 34.667F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 75.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 112.4489F, -44.3297F, 44.4468F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 101.3372F, -44.3297F, 14.8156F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 142.08F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 145.784F, -40.6258F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 149.4878F, -40.6258F, 22.2234F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 149.4878F, -36.9219F, 25.9273F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 149.4878F, -33.218F, 18.5195F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 153.1917F, -36.9219F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 149.4878F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 153.1917F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 153.1917F, -33.218F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 138.3761F, -44.3297F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 138.3761F, -44.3297F, 22.2234F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 153.1917F, -44.3297F, 18.5195F, 14.8156F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 171.7112F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 175.4151F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 164.3034F, -36.9219F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 164.3034F, -29.5141F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-20.3056F, -136.5242F, 0.6997F, 0.0F, -1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone240(PartDefinition bone65) {
      return bone65.addOrReplaceChild(
         "bone240",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(31.0244F, -82.81F, -31.759F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -86.5039F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -75.4493F, -35.4529F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -86.5039F, -39.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -97.6127F, -39.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -97.6127F, -42.8407F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -86.531F, -42.8407F, 3.6939F, 77.5721F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -42.8407F, 44.3945F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -18.8303F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -25.5815F, -42.8407F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -20.0406F, -35.4529F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -34.8163F, -55.7694F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -38.5102F, -65.0042F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.014F, 2.1228F, -46.5346F, 16.6226F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, 9.5106F, -46.5346F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, 13.2046F, -42.8407F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, 13.2046F, -39.1468F, 18.4695F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 13.2046F, -35.4529F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 13.2316F, -31.759F, 40.7006F, 7.3608F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 20.5924F, -31.759F, 3.6939F, 3.6939F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.888F, 27.9802F, -20.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5819F, 27.9802F, -16.9834F, 7.3878F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 31.6741F, -9.5956F, 7.3878F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, 31.6741F, -2.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.4237F, 27.9802F, -2.2077F, 11.2846F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 20.6194F, -20.6367F, 29.6189F, 7.3608F, 22.0958F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, 27.9802F, -15.1364F, 7.3878F, 3.6939F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 35.368F, -2.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 20.5924F, -31.759F, 3.6939F, 3.6939F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 16.8985F, -35.4529F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 9.5106F, -39.1468F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -71.7283F, -39.1468F, 3.6939F, 81.2389F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -31.1223F, -46.5346F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -34.8163F, -46.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -58.8267F, -76.0859F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -31.1223F, -31.759F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -60.6736F, -57.6164F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -75.4493F, -65.0042F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -28.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -93.9188F, -31.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -68.0614F, -72.392F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -71.7553F, -68.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -71.7553F, -68.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -64.3675F, -76.0859F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -79.1432F, -72.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -79.1432F, -68.6981F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -71.7553F, -79.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -71.7553F, -76.0859F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -64.3675F, -79.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -53.2858F, -76.0859F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -45.898F, -72.392F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -49.5919F, -72.392F, 7.3878F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -49.5919F, -76.0859F, 11.0817F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -38.5102F, -61.3103F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8163F, -61.3103F, 7.3878F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -31.1223F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -42.2041F, -72.392F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.0678F, -27.4284F, -72.392F, 5.5409F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -16.3467F, -53.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 2.1228F, -50.2285F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -27.4284F, -50.2285F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -23.7345F, -68.6981F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -23.7345F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -20.0406F, -61.3103F, 3.6939F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8162F, -57.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -27.4284F, -57.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -31.1223F, -57.6164F, 7.3878F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -27.4284F, -53.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -31.1223F, -53.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -8.9589F, -57.6164F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -1.5711F, -53.9225F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 2.1228F, -50.2285F, 29.6189F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -1.5711F, -57.6164F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -61.3103F, 29.6189F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -65.0042F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -68.6981F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -50.2285F, 40.7006F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -1.5711F, -46.5346F, 18.4695F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -14.4998F, -46.5346F, 14.7756F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -31.1223F, -53.9225F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8163F, -61.3103F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -27.4284F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -31.1223F, -68.6981F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -42.2041F, -53.9225F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -49.5919F, -76.0859F, 11.0817F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -42.2041F, -72.392F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8163F, -68.6981F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -53.2858F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -56.9797F, -72.392F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -56.9797F, -76.0859F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -68.0614F, -53.9225F, 3.6939F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -75.4493F, -50.2285F, 3.6939F, 48.0208F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -82.8371F, -53.9225F, 3.6939F, 25.8574F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -79.1432F, -46.5346F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -97.6127F, -46.5346F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -101.3066F, -50.2285F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -97.6127F, -53.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -97.6127F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -93.9188F, -53.9225F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -97.6127F, -72.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -86.531F, -65.0042F, 3.6939F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -82.8371F, -72.392F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -82.8371F, -76.0859F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -92.0718F, -79.7798F, 29.6189F, 33.2452F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -73.5383F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -112.3883F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -46.8983F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -66.2145F, -79.7798F, 12.9287F, 46.1739F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -58.8267F, -79.7798F, 16.6902F, 42.48F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -79.1432F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -79.1432F, -57.6164F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -86.5039F, -28.0651F, 3.6939F, 33.2452F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(34.7183F, -75.4222F, -24.3712F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -86.531F, -28.0651F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -80.9631F, -13.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -2.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -93.9188F, -2.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -5.9016F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -16.9834F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -97.6127F, -20.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -97.6127F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -101.3066F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -105.0005F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -108.6944F, -16.9834F, 7.3878F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -24.3712F, 3.6939F, 3.6939F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -112.3883F, -20.6773F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -116.0823F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -112.3883F, -46.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -108.6944F, -46.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -112.3883F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -116.0823F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -108.6944F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -93.9188F, -68.6981F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -90.2249F, -72.392F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -60.6736F, -94.5555F, 5.6085F, 14.7756F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -75.4493F, -83.4737F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -64.3675F, -83.4737F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -49.5919F, -83.4737F, 16.6902F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -68.0614F, -87.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -56.9797F, -87.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -49.5919F, -94.5555F, 3.6939F, 7.3878F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -38.5102F, -94.5555F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -56.9797F, -90.8615F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -45.898F, -90.8615F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -42.2041F, -87.1676F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -31.1223F, -87.1676F, 16.6902F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -16.3467F, -72.392F, 22.2311F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -16.3467F, -76.0859F, 9.3024F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -27.4284F, -90.8615F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -31.1223F, -94.5555F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -27.4284F, -90.8615F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -31.1224F, -98.2494F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -82.8371F, -83.4737F, 5.6085F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -75.4492F, -87.1676F, 5.6085F, 7.3878F, 9.2348F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -45.898F, -98.2494F, 5.6085F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -42.2041F, -101.9433F, 5.6085F, 7.3878F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -34.8163F, -90.8615F, 9.3024F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -68.0614F, -90.8615F, 5.6085F, 12.9287F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -86.531F, -79.7798F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -105.0005F, -57.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -101.3066F, -68.6981F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -105.0005F, -72.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -108.6944F, -72.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -112.3883F, -68.6981F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -108.6944F, -76.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -105.0005F, -79.7798F, 18.5372F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -112.3883F, -79.7798F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -116.0822F, -61.3103F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -53.9225F, 11.1493F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -35.4529F, 14.8433F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -16.9834F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -123.4701F, -16.9834F, 11.1493F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -105.0005F, -76.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -101.3066F, -83.4737F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -101.3066F, -83.4737F, 11.0817F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -101.3066F, -83.4737F, 14.8433F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.6086F, -101.3066F, -83.4737F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -93.9188F, -83.4737F, 11.0817F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -90.2249F, -57.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -108.6944F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -119.7762F, -31.759F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -42.8407F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -116.0823F, -39.1468F, 3.6939F, 3.6939F, 29.5513F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -116.0823F, -46.5346F, 7.3878F, 3.6939F, 48.0208F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -116.0823F, -46.5346F, 18.5372F, 3.6939F, 33.2452F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -97.6127F, -28.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -97.6127F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -93.9188F, -46.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -101.3066F, -35.4529F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -101.3066F, -39.1468F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -101.3066F, -42.8407F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -105.0005F, -46.5346F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -101.3066F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -105.0005F, -31.759F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -105.0005F, -39.1468F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -108.6944F, -39.1468F, 3.6939F, 14.7756F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -108.6944F, -35.4529F, 3.6939F, 3.6939F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -105.0005F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -105.0005F, -20.6773F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -101.3066F, -20.6773F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -108.6944F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -82.8371F, -5.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -20.6773F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -86.531F, -28.0651F, 3.6939F, 20.3165F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -66.1874F, -13.2895F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -69.8813F, -9.5956F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -56.9527F, -31.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -56.9527F, -35.4529F, 3.6939F, 11.0817F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -68.0344F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -68.0344F, -39.1468F, 3.6939F, 29.5242F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -60.6466F, -42.8407F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -86.531F, -31.759F, 3.6939F, 29.5513F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -45.8709F, -31.759F, 7.3878F, 7.3878F, 22.1364F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -45.8709F, -24.3712F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -34.7892F, -24.3712F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -23.7075F, -24.3712F, 3.6939F, 14.7486F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -40.3301F, -24.3712F, 3.6939F, 16.6226F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -42.2041F, -13.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -42.177F, -20.6773F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -38.4831F, -28.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -38.4831F, -31.7319F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -34.8163F, -28.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -34.8163F, -35.4529F, 3.6939F, 48.0208F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -23.7075F, -20.6773F, 11.0817F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -49.5648F, -9.5956F, 3.6939F, 14.7486F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -45.898F, -2.2077F, 3.6939F, 33.1911F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -79.1432F, -5.9016F, 3.6939F, 29.5783F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -53.2588F, -9.5956F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -42.177F, -9.5956F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -20.0136F, -5.9016F, 3.6939F, 31.3712F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.5769F, 13.2046F, -24.3712F, 15.0597F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 16.8985F, -13.2895F, 3.6939F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 20.5924F, -2.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 13.2046F, -28.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -8.9318F, -28.038F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -8.9318F, -20.6502F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 16.9255F, -16.9563F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -8.9318F, -24.3441F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -20.6502F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -16.9563F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -13.2624F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -5.2379F, -9.5685F, 3.6939F, 36.9391F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 20.5924F, -28.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -32.9423F, -5.8746F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -5.8746F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 5.8167F, -2.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -8.9589F, -24.3712F, 3.6939F, 22.1635F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -23.7075F, -13.2895F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -42.177F, -5.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-24.0F, -278.0F, 80.0F, 0.0F, 1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone241(PartDefinition bone240) {
      return bone240.addOrReplaceChild(
         "bone241",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(0.4151F, 130.9683F, -44.3297F, 7.4078F, 44.4468F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 164.3034F, -40.6258F, 7.4078F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 171.7112F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 168.0073F, -33.2181F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 164.3034F, -40.6258F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 164.3034F, -40.6258F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 168.0073F, -44.3297F, 14.8156F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 134.6723F, -48.0336F, 3.7039F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 90.2255F, -48.0336F, 18.5195F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 116.1528F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 101.3372F, -51.7375F, 14.8156F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 90.2255F, -51.7375F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 108.745F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 105.0411F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 97.6333F, -48.0336F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 93.9294F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 71.706F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 79.1138F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 97.6333F, -51.7375F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 97.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 97.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 97.6333F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 93.9294F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 86.5216F, -51.7375F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 53.1865F, -51.7375F, 22.2234F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 42.0748F, -48.0336F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 38.3709F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 34.667F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 34.667F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 30.9631F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 45.7787F, -48.0336F, 18.5195F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 42.0748F, -48.0336F, 7.4078F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 38.3709F, -48.0336F, 22.2234F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 38.3709F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 34.667F, -48.0336F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 30.9631F, -44.3297F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 19.8514F, -29.5142F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 16.1475F, -22.1064F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 16.1475F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 16.1475F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 12.4436F, -10.9947F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 12.4436F, -14.6986F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 12.4436F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 12.4436F, -7.2908F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 27.2592F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 27.2592F, -36.922F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 30.9631F, -36.922F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 27.2592F, -33.2181F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 23.5553F, -29.5142F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 19.8514F, -25.8103F, 29.6312F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 19.8514F, -22.1064F, 29.6312F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 23.5553F, -29.5142F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 42.0748F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 53.1865F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 49.4826F, -51.7375F, 25.9273F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 53.1865F, -51.7375F, 25.9273F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 86.5216F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 64.2982F, -59.1453F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 71.706F, -55.4414F, 22.2234F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 56.8904F, -55.4414F, 18.5195F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 53.1865F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 49.4826F, -55.4414F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 68.0021F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 82.8177F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 68.0021F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 86.5216F, -51.7375F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 71.706F, -51.7375F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 68.0021F, -51.7375F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 56.8904F, -55.4414F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 130.9684F, -48.0336F, 3.7039F, 33.3351F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 130.9684F, -48.0336F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 130.9684F, -48.0336F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 130.9684F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 130.9684F, -51.7375F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 130.9684F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 156.8956F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 160.5995F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 153.1917F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 142.08F, -44.3297F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 142.08F, -40.6258F, 22.2234F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 145.784F, -33.2181F, 82.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 138.3761F, -33.2181F, 43.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 80.6148F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 33.9948F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 27.3348F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 127.2645F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 110.5969F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(61.5294F, 68.0021F, -1.7349F, 27.7792F, 37.039F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 127.2645F, -40.6258F, 11.1117F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 134.6722F, -40.6258F, 3.7039F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 138.3761F, -29.5142F, 3.7039F, 3.7039F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 138.3761F, -22.1064F, 3.7039F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 149.4878F, -29.5142F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 138.3761F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 134.6722F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 130.9684F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 123.5606F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 116.1528F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 108.745F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 129.1164F, -36.922F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 79.1138F, -48.0336F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 79.1138F, -51.7375F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 116.1528F, -36.922F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 119.8567F, -36.922F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 123.5606F, -33.2181F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 127.2645F, -29.5142F, 3.7039F, 11.1117F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 116.1528F, -25.8103F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 97.6333F, -25.8103F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 116.1528F, -22.1064F, 7.4078F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 127.2645F, -22.1064F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 116.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 116.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 116.1528F, -22.1064F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 93.9294F, -22.1064F, 7.4078F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 116.1528F, -10.9947F, 3.7039F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 105.0411F, -18.4025F, 11.1117F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 90.2255F, -14.6986F, 11.1117F, 14.8156F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 97.6333F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 86.5216F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 86.5216F, -7.2908F, 3.7039F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 79.1138F, -10.9947F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(81.9008F, 68.0021F, -10.9947F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 60.5943F, -10.9947F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 79.1138F, -14.6986F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 108.745F, -14.6986F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 105.0411F, -10.9947F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 105.0411F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(93.0125F, 105.0411F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 86.5216F, -18.4025F, 3.7039F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 130.9684F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 127.2645F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 130.9684F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 127.2645F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 119.8567F, -25.8103F, 3.7039F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 127.2645F, -25.8103F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 112.4489F, -40.6258F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 108.745F, -36.922F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 105.0411F, -36.922F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 93.9294F, -40.6258F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 97.6333F, -36.922F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 101.3372F, -25.8103F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 108.745F, -29.5142F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 105.0411F, -40.6258F, 11.1117F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 93.9294F, -40.6258F, 14.8156F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 119.8567F, -29.5142F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 123.5606F, -36.922F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 134.6722F, -44.3297F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 130.9684F, -44.3297F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 127.2645F, -44.3297F, 40.7429F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 108.745F, -44.3297F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 101.3372F, -44.3297F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 93.9294F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 90.2255F, -44.3297F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 82.8177F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 75.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 68.0021F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 64.2982F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 56.8904F, -40.6258F, 11.1117F, 18.5195F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 60.5943F, -40.6258F, 11.1117F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 64.2982F, -40.6258F, 11.1117F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 60.5943F, -40.6258F, 3.7039F, 33.3351F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 79.1138F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 82.8177F, -36.922F, 11.1117F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 75.4099F, -33.2181F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 68.0021F, -25.8103F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 56.8904F, -25.8103F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 56.8904F, -29.5142F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 53.1865F, -33.2181F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 53.1865F, -29.5142F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 53.1865F, -36.922F, 18.5195F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 49.4826F, -36.922F, 7.4078F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 49.4826F, -33.2181F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 45.7787F, -33.2181F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 42.0748F, -33.2181F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 38.3709F, -33.2181F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 42.0748F, -36.922F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 45.7787F, -40.6258F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 38.3709F, -33.2181F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 45.7787F, -36.922F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 45.7787F, -33.2181F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 56.8904F, -33.2181F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 56.8904F, -36.922F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 71.706F, -36.922F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 68.0021F, -33.2181F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 64.2982F, -36.922F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 64.2982F, -33.2181F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 64.2982F, -29.5142F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 71.706F, -36.922F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 75.4099F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 71.706F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 64.2982F, -36.922F, 11.1117F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 86.5216F, -33.2181F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 86.5216F, -29.5142F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 49.4826F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 38.3709F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 38.3709F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 34.667F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 34.667F, -44.3297F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 30.9631F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 27.2592F, -36.922F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 30.9631F, -36.922F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 27.2592F, -33.2181F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 34.667F, -36.922F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 30.9631F, -33.2181F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 27.2592F, -29.5142F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 23.5553F, -25.8103F, 7.4078F, 11.1117F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 27.2592F, -25.8103F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 23.5553F, -22.1064F, 11.1117F, 7.4078F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 19.8514F, -14.6986F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 23.5553F, -18.4025F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 30.9631F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 34.667F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 42.0748F, -29.5142F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 45.7787F, -14.6986F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 38.3709F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 30.9631F, -25.8103F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 27.2592F, -22.1064F, 14.8156F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 27.2592F, -18.4025F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 30.9631F, -22.1064F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 34.667F, -25.8103F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 27.2592F, -14.6986F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 30.9631F, -18.4025F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 34.667F, -22.1064F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 38.3709F, -25.8103F, 7.4078F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 53.1865F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(81.9008F, 68.0021F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 75.4099F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 82.8177F, -7.2908F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 60.5943F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 45.7787F, -25.8103F, 7.4078F, 33.3351F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 34.667F, -33.2181F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 75.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 112.4489F, -44.3297F, 44.4468F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 101.3372F, -44.3297F, 14.8156F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 142.08F, -33.2181F, 55.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 145.784F, -40.6258F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 149.4878F, -40.6258F, 22.2234F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 149.4878F, -36.922F, 25.9273F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 149.4878F, -33.2181F, 18.5195F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 153.1917F, -36.922F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 149.4878F, -25.8103F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 153.1917F, -25.8103F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 153.1917F, -33.2181F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 138.3761F, -44.3297F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 138.3761F, -44.3297F, 22.2234F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 153.1917F, -44.3297F, 18.5195F, 14.8156F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 171.7112F, -33.2181F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 175.4151F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 164.3034F, -36.922F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 164.3034F, -29.5142F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-20.3056F, -136.5242F, 0.6997F, 0.0F, -1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone242(PartDefinition bone65) {
      return bone65.addOrReplaceChild(
         "bone242",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(31.0244F, -82.81F, -31.759F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -86.5039F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -75.4493F, -35.4529F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -86.5039F, -39.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -97.6127F, -39.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -97.6127F, -42.8407F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -86.531F, -42.8407F, 3.6939F, 77.5721F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -42.8407F, 44.3945F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -18.8303F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -25.5815F, -42.8407F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -20.0406F, -35.4529F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -34.8163F, -55.7694F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -38.5102F, -65.0042F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.014F, 2.1228F, -46.5346F, 16.6226F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, 9.5106F, -46.5346F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, 13.2046F, -42.8407F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, 13.2046F, -39.1468F, 18.4695F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 13.2046F, -35.4529F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 13.2316F, -31.759F, 40.7006F, 7.3608F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 20.5924F, -31.759F, 3.6939F, 3.6939F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.888F, 27.9802F, -20.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5819F, 27.9802F, -16.9834F, 7.3878F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 31.6741F, -9.5956F, 7.3878F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, 31.6741F, -2.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.4237F, 27.9802F, -2.2077F, 11.2846F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 20.6194F, -20.6367F, 29.6189F, 7.3608F, 22.0958F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, 27.9802F, -15.1364F, 7.3878F, 3.6939F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 35.368F, -2.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 20.5924F, -31.759F, 3.6939F, 3.6939F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 16.8985F, -35.4529F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 9.5106F, -39.1468F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -71.7283F, -39.1468F, 3.6939F, 81.2389F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -31.1223F, -46.5346F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -34.8163F, -46.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -58.8267F, -76.0859F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -31.1224F, -31.759F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -60.6736F, -57.6164F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -75.4493F, -65.0042F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -28.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -93.9188F, -31.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -68.0614F, -72.392F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -71.7553F, -68.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -71.7553F, -68.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -64.3675F, -76.0859F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -79.1432F, -72.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -79.1432F, -68.6981F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -71.7553F, -79.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -71.7553F, -76.0859F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -64.3675F, -79.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -53.2858F, -76.0859F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -45.898F, -72.392F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -49.5919F, -72.392F, 7.3878F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -49.5919F, -76.0859F, 11.0817F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -38.5102F, -61.3103F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8162F, -61.3103F, 7.3878F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -31.1224F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -42.2041F, -72.392F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.0678F, -27.4284F, -72.392F, 5.5409F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -16.3467F, -53.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 2.1228F, -50.2285F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -27.4284F, -50.2285F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -23.7345F, -68.6981F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -23.7345F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -20.0406F, -61.3103F, 3.6939F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8162F, -57.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -27.4284F, -57.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -31.1223F, -57.6164F, 7.3878F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -27.4284F, -53.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -31.1223F, -53.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -8.9589F, -57.6164F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -1.5711F, -53.9225F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 2.1228F, -50.2285F, 29.6189F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -1.5711F, -57.6164F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -61.3103F, 29.6189F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -5.265F, -65.0042F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -68.6981F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -8.9589F, -50.2285F, 40.7006F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -1.5711F, -46.5346F, 18.4695F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -14.4998F, -46.5346F, 14.7756F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -31.1224F, -53.9225F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8163F, -61.3103F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -27.4284F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -31.1224F, -68.6981F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -42.2041F, -53.9225F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -49.5919F, -76.0859F, 11.0817F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -42.2041F, -72.392F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -34.8163F, -68.6981F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -53.2858F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -56.9797F, -72.392F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -56.9797F, -76.0859F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -68.0614F, -53.9225F, 3.6939F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -75.4493F, -50.2285F, 3.6939F, 48.0208F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -82.8371F, -53.9225F, 3.6939F, 25.8574F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -79.1431F, -46.5346F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -97.6127F, -46.5346F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -101.3066F, -50.2285F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -97.6127F, -53.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -97.6127F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -93.9188F, -53.9225F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -97.6127F, -72.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -86.531F, -65.0042F, 3.6939F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -82.8371F, -72.392F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -82.8371F, -76.0859F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -92.0718F, -79.7798F, 29.6189F, 33.2452F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -73.5383F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -112.3883F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -46.8983F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -66.2145F, -79.7798F, 12.9287F, 46.1739F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -58.8267F, -79.7798F, 16.6902F, 42.48F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -79.1432F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -79.1432F, -57.6164F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -86.5039F, -28.0651F, 3.6939F, 33.2452F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(34.7183F, -75.4222F, -24.3712F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -86.531F, -28.0651F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -80.9631F, -13.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -2.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -93.9188F, -2.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -5.9016F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -16.9834F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -97.6127F, -20.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -97.6127F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -101.3066F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -105.0005F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -108.6944F, -16.9834F, 7.3878F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -24.3712F, 3.6939F, 3.6939F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -112.3883F, -20.6773F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -116.0822F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -112.3883F, -46.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -108.6944F, -46.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -112.3883F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -116.0822F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -108.6944F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -93.9188F, -68.6981F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -90.2249F, -72.392F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -60.6736F, -94.5555F, 5.6085F, 14.7756F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -75.4493F, -83.4737F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -64.3675F, -83.4737F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -49.5919F, -83.4737F, 16.6902F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -68.0614F, -87.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -56.9797F, -87.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -49.5919F, -94.5555F, 3.6939F, 7.3878F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -38.5102F, -94.5555F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -56.9797F, -90.8615F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -45.898F, -90.8615F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -42.2041F, -87.1676F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -31.1223F, -87.1676F, 16.6902F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -16.3467F, -72.392F, 22.2311F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -16.3467F, -76.0859F, 9.3024F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -27.4284F, -90.8615F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -31.1224F, -94.5555F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -27.4284F, -90.8615F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -31.1223F, -98.2494F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -82.8371F, -83.4737F, 5.6085F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -75.4493F, -87.1676F, 5.6085F, 7.3878F, 9.2348F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -45.898F, -98.2494F, 5.6085F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -42.2041F, -101.9433F, 5.6085F, 7.3878F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -34.8163F, -90.8615F, 9.3024F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -68.0614F, -90.8615F, 5.6085F, 12.9287F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -86.531F, -79.7798F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -105.0005F, -57.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -101.3066F, -68.6981F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -105.0005F, -72.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -108.6944F, -72.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -112.3883F, -68.6981F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -108.6944F, -76.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -105.0005F, -79.7798F, 18.5372F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -112.3883F, -79.7798F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -116.0822F, -61.3103F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -53.9225F, 11.1493F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -35.4529F, 14.8433F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -119.7762F, -16.9834F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -123.4701F, -16.9834F, 11.1493F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -105.0005F, -76.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -101.3066F, -83.4737F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -101.3066F, -83.4737F, 11.0817F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -101.3066F, -83.4737F, 14.8433F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.6086F, -101.3066F, -83.4737F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -93.9188F, -83.4737F, 11.0817F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -90.2249F, -57.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -108.6944F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -119.7762F, -31.759F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -112.3883F, -42.8407F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -116.0822F, -39.1468F, 3.6939F, 3.6939F, 29.5513F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -116.0822F, -46.5346F, 7.3878F, 3.6939F, 48.0208F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -116.0822F, -46.5346F, 18.5372F, 3.6939F, 33.2452F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -97.6127F, -28.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -97.6127F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -93.9188F, -46.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -101.3066F, -35.4529F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -101.3066F, -39.1468F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -101.3066F, -42.8407F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -105.0005F, -46.5346F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -93.9188F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -101.3066F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -105.0005F, -31.759F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -105.0005F, -39.1468F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -108.6944F, -39.1468F, 3.6939F, 14.7756F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -108.6944F, -35.4529F, 3.6939F, 3.6939F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -105.0005F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -105.0005F, -20.6773F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -101.3066F, -20.6773F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -108.6944F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -82.8371F, -5.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -90.2249F, -20.6773F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -86.531F, -28.0651F, 3.6939F, 20.3165F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -66.1874F, -13.2895F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -69.8813F, -9.5956F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -56.9527F, -31.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -56.9527F, -35.4529F, 3.6939F, 11.0817F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -68.0344F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -68.0344F, -39.1468F, 3.6939F, 29.5242F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -60.6466F, -42.8407F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -86.531F, -31.759F, 3.6939F, 29.5513F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -45.8709F, -31.759F, 7.3878F, 7.3878F, 22.1364F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -45.8709F, -24.3712F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -34.7892F, -24.3712F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -23.7075F, -24.3712F, 3.6939F, 14.7486F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -40.3301F, -24.3712F, 3.6939F, 16.6226F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -42.2041F, -13.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -42.177F, -20.6773F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -38.4831F, -28.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -38.4831F, -31.732F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -34.8163F, -28.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -34.8162F, -35.4529F, 3.6939F, 48.0208F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -23.7075F, -20.6773F, 11.0817F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -49.5648F, -9.5956F, 3.6939F, 14.7486F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -45.898F, -2.2077F, 3.6939F, 33.1911F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -79.1432F, -5.9016F, 3.6939F, 29.5783F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -53.2587F, -9.5956F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -42.177F, -9.5956F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -20.0136F, -5.9016F, 3.6939F, 31.3712F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.5769F, 13.2046F, -24.3712F, 15.0597F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 16.8985F, -13.2895F, 3.6939F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 20.5924F, -2.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 13.2046F, -28.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -8.9318F, -28.038F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -8.9318F, -20.6502F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 16.9255F, -16.9563F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -8.9318F, -24.3441F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -20.6502F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -16.9563F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -13.2624F, 3.6939F, 22.1634F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -5.2379F, -9.5685F, 3.6939F, 36.9391F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 20.5924F, -28.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -32.9423F, -5.8746F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 2.1499F, -5.8746F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 5.8167F, -2.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -8.9589F, -24.3712F, 3.6939F, 22.1634F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -23.7075F, -13.2895F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -42.177F, -5.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-20.0F, -185.0F, 63.0F, 0.0F, 1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone243(PartDefinition bone242) {
      return bone242.addOrReplaceChild(
         "bone243",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(0.4151F, 130.9683F, -44.3297F, 7.4078F, 44.4468F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 164.3034F, -40.6258F, 7.4078F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 171.7112F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 168.0073F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 164.3034F, -40.6258F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 164.3034F, -40.6258F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 168.0073F, -44.3297F, 14.8156F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 134.6723F, -48.0336F, 3.7039F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 90.2255F, -48.0336F, 18.5195F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 116.1528F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 101.3372F, -51.7375F, 14.8156F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 90.2255F, -51.7375F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 108.745F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 105.0411F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 97.6333F, -48.0336F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 93.9294F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 71.706F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 79.1138F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 97.6333F, -51.7375F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 97.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 97.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 97.6333F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 93.9294F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 86.5216F, -51.7375F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 53.1865F, -51.7375F, 22.2234F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 42.0748F, -48.0336F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 38.3709F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 34.667F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 34.667F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 30.9631F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 45.7787F, -48.0336F, 18.5195F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 42.0748F, -48.0336F, 7.4078F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 38.3709F, -48.0336F, 22.2234F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 38.3709F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 34.667F, -48.0336F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 30.9631F, -44.3297F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 19.8514F, -29.5141F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 16.1475F, -22.1064F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 16.1475F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 16.1475F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 12.4436F, -10.9947F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 12.4436F, -14.6986F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 12.4436F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 12.4436F, -7.2908F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 27.2592F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 27.2592F, -36.9219F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 30.9631F, -36.9219F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 27.2592F, -33.218F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 23.5553F, -29.5141F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 19.8514F, -25.8102F, 29.6312F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 19.8514F, -22.1064F, 29.6312F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 23.5553F, -29.5141F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 42.0748F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 53.1865F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 49.4826F, -51.7375F, 25.9273F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 53.1865F, -51.7375F, 25.9273F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 86.5216F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 64.2982F, -59.1453F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 71.706F, -55.4414F, 22.2234F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 56.8904F, -55.4414F, 18.5195F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 53.1865F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 49.4826F, -55.4414F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 68.0021F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 82.8177F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 68.0021F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 86.5216F, -51.7375F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 71.706F, -51.7375F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 68.0021F, -51.7375F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 56.8904F, -55.4414F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 130.9684F, -48.0336F, 3.7039F, 33.3351F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 130.9684F, -48.0336F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 130.9684F, -48.0336F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 130.9684F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 130.9684F, -51.7375F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 130.9684F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 156.8956F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 160.5995F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 153.1917F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 142.08F, -44.3297F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 142.08F, -40.6258F, 22.2234F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 145.784F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 138.3761F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 80.6148F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 33.9948F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 27.3348F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 127.2645F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 110.5969F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(61.5294F, 68.0021F, -1.7349F, 27.7792F, 37.039F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 127.2645F, -40.6258F, 11.1117F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 134.6722F, -40.6258F, 3.7039F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 138.3761F, -29.5141F, 3.7039F, 3.7039F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 138.3761F, -22.1064F, 3.7039F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 149.4878F, -29.5141F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 138.3761F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 134.6722F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 130.9684F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 123.5606F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 116.1528F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 108.745F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 129.1164F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 79.1138F, -48.0336F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 79.1138F, -51.7375F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 116.1528F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 119.8567F, -36.9219F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 123.5606F, -33.218F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 127.2645F, -29.5141F, 3.7039F, 11.1117F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 116.1528F, -25.8102F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 97.6333F, -25.8102F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 116.1528F, -22.1064F, 7.4078F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 127.2645F, -22.1064F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 116.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 116.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 116.1528F, -22.1064F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 93.9294F, -22.1064F, 7.4078F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 116.1528F, -10.9947F, 3.7039F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 105.0411F, -18.4025F, 11.1117F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 90.2255F, -14.6986F, 11.1117F, 14.8156F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 97.6333F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 86.5216F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 86.5216F, -7.2908F, 3.7039F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 79.1138F, -10.9947F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(81.9009F, 68.0021F, -10.9947F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 60.5943F, -10.9947F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 79.1138F, -14.6986F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 108.745F, -14.6986F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 105.0411F, -10.9947F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 105.0411F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(93.0125F, 105.0411F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 86.5216F, -18.4025F, 3.7039F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 130.9684F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 127.2645F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 130.9684F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 127.2645F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 119.8567F, -25.8102F, 3.7039F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 127.2645F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 112.4489F, -40.6258F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 108.745F, -36.9219F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 105.0411F, -36.9219F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 93.9294F, -40.6258F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 97.6333F, -36.9219F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 101.3372F, -25.8102F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 108.745F, -29.5141F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 105.0411F, -40.6258F, 11.1117F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 93.9294F, -40.6258F, 14.8156F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 119.8567F, -29.5141F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 123.5606F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 134.6722F, -44.3297F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 130.9684F, -44.3297F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 127.2645F, -44.3297F, 40.7429F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 108.745F, -44.3297F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 101.3372F, -44.3297F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 93.9294F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 90.2255F, -44.3297F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 82.8177F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 75.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 68.0021F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 64.2982F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 56.8904F, -40.6258F, 11.1117F, 18.5195F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 60.5943F, -40.6258F, 11.1117F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 64.2982F, -40.6258F, 11.1117F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 60.5943F, -40.6258F, 3.7039F, 33.3351F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 79.1138F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 82.8177F, -36.9219F, 11.1117F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 75.4099F, -33.218F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 68.0021F, -25.8102F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 56.8904F, -25.8102F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 56.8904F, -29.5141F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 53.1865F, -33.218F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 53.1865F, -29.5141F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 53.1865F, -36.9219F, 18.5195F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 49.4826F, -36.9219F, 7.4078F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 49.4826F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 45.7787F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 42.0748F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 38.3709F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 42.0748F, -36.9219F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 45.7787F, -40.6258F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 38.3709F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 45.7787F, -36.9219F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 45.7787F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 56.8904F, -33.218F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 56.8904F, -36.9219F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 71.706F, -36.9219F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 68.0021F, -33.218F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 64.2982F, -36.9219F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 64.2982F, -33.218F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 64.2982F, -29.5141F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 71.706F, -36.9219F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 75.4099F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 71.706F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 64.2982F, -36.9219F, 11.1117F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 86.5216F, -33.218F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 86.5216F, -29.5141F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 49.4826F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 38.3709F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 38.3709F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 34.667F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 34.667F, -44.3297F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 30.9631F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 27.2592F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 30.9631F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 27.2592F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 34.667F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 30.9631F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 27.2592F, -29.5141F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 23.5553F, -25.8102F, 7.4078F, 11.1117F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 27.2592F, -25.8102F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 23.5553F, -22.1064F, 11.1117F, 7.4078F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 19.8514F, -14.6986F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 23.5553F, -18.4025F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 30.9631F, -29.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 34.667F, -29.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 42.0748F, -29.5141F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 45.7787F, -14.6986F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 38.3709F, -29.5141F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 30.9631F, -25.8102F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 27.2592F, -22.1064F, 14.8156F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 27.2592F, -18.4025F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 30.9631F, -22.1064F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 34.667F, -25.8102F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 27.2592F, -14.6986F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 30.9631F, -18.4025F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 34.667F, -22.1064F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 38.3709F, -25.8102F, 7.4078F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 53.1865F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(81.9009F, 68.0021F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6047F, 75.4099F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 82.8177F, -7.2908F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 60.5943F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 45.7787F, -25.8102F, 7.4078F, 33.3351F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 34.667F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 75.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 112.4489F, -44.3297F, 44.4468F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 101.3372F, -44.3297F, 14.8156F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 142.08F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 145.784F, -40.6258F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 149.4878F, -40.6258F, 22.2234F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 149.4878F, -36.9219F, 25.9273F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 149.4878F, -33.218F, 18.5195F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 153.1917F, -36.9219F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 149.4878F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 153.1917F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 153.1917F, -33.218F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 138.3761F, -44.3297F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 138.3761F, -44.3297F, 22.2234F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 153.1917F, -44.3297F, 18.5195F, 14.8156F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 171.7112F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 175.4151F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 164.3034F, -36.9219F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 164.3034F, -29.5141F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-20.3056F, -136.5242F, 0.6997F, 0.0F, -1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone244(PartDefinition bone65) {
      return bone65.addOrReplaceChild(
         "bone244",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(31.0244F, -69.81F, -137.759F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -73.5039F, -141.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -62.4493F, -141.4529F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -73.5039F, -145.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -84.6127F, -145.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -84.6127F, -148.8407F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -73.531F, -148.8407F, 3.6939F, 77.5721F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 7.735F, -148.8407F, 44.3945F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 4.0411F, -124.8303F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -12.5815F, -148.8407F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -7.0406F, -141.4529F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -21.8163F, -161.7694F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -25.5102F, -171.0042F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.014F, 15.1228F, -152.5346F, 16.6226F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, 22.5106F, -152.5346F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, 26.2046F, -148.8407F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, 26.2046F, -145.1468F, 18.4695F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 26.2046F, -141.4529F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 26.2316F, -137.759F, 40.7006F, 7.3608F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 33.5924F, -137.759F, 3.6939F, 3.6939F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.888F, 40.9802F, -126.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5819F, 40.9802F, -122.9834F, 7.3878F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 44.6741F, -115.5956F, 7.3878F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, 44.6741F, -108.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.4237F, 40.9802F, -108.2077F, 11.2846F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, 40.9802F, -121.1364F, 7.3878F, 3.6939F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, 48.368F, -108.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 33.5924F, -137.759F, 3.6939F, 3.6939F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 29.8985F, -141.4529F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 22.5106F, -145.1468F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -58.7283F, -145.1468F, 3.6939F, 81.2389F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -18.1223F, -152.5346F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -21.8163F, -152.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -45.8267F, -182.0859F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -18.1223F, -137.759F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -47.6736F, -163.6164F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -62.4493F, -171.0042F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -99.3883F, -134.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -80.9188F, -137.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -55.0614F, -178.392F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -58.7553F, -174.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -58.7553F, -174.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -51.3675F, -182.0859F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -66.1432F, -178.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -66.1432F, -174.6981F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -58.7553F, -185.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -58.7553F, -182.0859F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -51.3675F, -185.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -40.2858F, -182.0859F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -32.898F, -178.392F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -36.5919F, -178.392F, 7.3878F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -36.5919F, -182.0859F, 11.0817F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -25.5102F, -167.3103F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -21.8163F, -167.3103F, 7.3878F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -18.1223F, -171.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -29.2041F, -178.392F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.0678F, -14.4284F, -178.392F, 5.5409F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -3.3467F, -159.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, 15.1228F, -156.2285F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -14.4284F, -156.2285F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -10.7345F, -174.6981F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -10.7345F, -171.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -7.0406F, -167.3103F, 3.6939F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -21.8162F, -163.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -14.4284F, -163.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -18.1223F, -163.6164F, 7.3878F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -14.4284F, -159.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -18.1223F, -159.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, 4.0411F, -163.6164F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 11.4289F, -159.9225F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 15.1228F, -156.2285F, 29.6189F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 11.4289F, -163.6164F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 7.735F, -167.3103F, 29.6189F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 7.735F, -171.0042F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 4.0411F, -174.6981F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, 4.0411F, -156.2285F, 40.7006F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, 11.4289F, -152.5346F, 18.4695F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -1.4998F, -152.5346F, 14.7756F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -18.1223F, -159.9225F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -21.8163F, -167.3103F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -14.4284F, -171.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -18.1223F, -174.6981F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -29.2041F, -159.9225F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -36.5919F, -182.0859F, 11.0817F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -29.2041F, -178.392F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -21.8163F, -174.6981F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -40.2858F, -167.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -43.9797F, -178.392F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -43.9797F, -182.0859F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -55.0614F, -159.9225F, 3.6939F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -62.4493F, -156.2285F, 3.6939F, 48.0208F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -69.8371F, -159.9225F, 3.6939F, 25.8574F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -66.1432F, -152.5346F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -84.6127F, -152.5346F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -88.3066F, -156.2285F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -84.6127F, -159.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -84.6127F, -167.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -80.9188F, -159.9225F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -84.6127F, -178.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -73.531F, -171.0042F, 3.6939F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -69.8371F, -178.392F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -69.8371F, -182.0859F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -79.0718F, -185.7798F, 29.6189F, 33.2452F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -60.5383F, -178.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -99.3883F, -178.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -33.8983F, -178.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -53.2145F, -185.7798F, 12.9287F, 46.1739F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -45.8267F, -185.7798F, 16.6902F, 42.48F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -66.1432F, -167.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -66.1432F, -163.6164F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -73.5039F, -134.0651F, 3.6939F, 33.2452F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(34.7183F, -62.4222F, -130.3712F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -73.531F, -134.0651F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -67.9631F, -119.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -77.2249F, -108.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -80.9188F, -108.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -80.9188F, -111.9016F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -80.9188F, -122.9834F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -84.6127F, -126.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -84.6127F, -119.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -88.3066F, -119.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -92.0005F, -119.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -95.6944F, -122.9834F, 7.3878F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -99.3883F, -130.3712F, 3.6939F, 3.6939F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -99.3883F, -126.6773F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -103.0823F, -137.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -99.3883F, -152.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -95.6944F, -152.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -99.3883F, -163.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -103.0823F, -163.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -95.6944F, -163.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -80.9188F, -174.6981F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -77.2249F, -178.392F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -47.6736F, -200.5555F, 5.6085F, 14.7756F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -62.4493F, -189.4737F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -51.3675F, -189.4737F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -36.5919F, -189.4737F, 16.6902F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -55.0614F, -193.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -43.9797F, -193.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -36.5919F, -200.5555F, 3.6939F, 7.3878F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -25.5102F, -200.5555F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -43.9797F, -196.8615F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -32.898F, -196.8615F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -29.2041F, -193.1676F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -18.1223F, -193.1676F, 16.6902F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -3.3467F, -178.392F, 22.2311F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -3.3467F, -182.0859F, 9.3024F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -14.4284F, -196.8615F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -18.1223F, -200.5555F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -14.4284F, -196.8615F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -18.1224F, -204.2494F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -69.8371F, -189.4737F, 5.6085F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -62.4492F, -193.1676F, 5.6085F, 7.3878F, 9.2348F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -32.898F, -204.2494F, 5.6085F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -29.2041F, -207.9433F, 5.6085F, 7.3878F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -21.8163F, -196.8615F, 9.3024F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -55.0614F, -196.8615F, 5.6085F, 12.9287F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -73.531F, -185.7798F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -92.0005F, -163.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -88.3066F, -174.6981F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -92.0005F, -178.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -95.6944F, -178.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -99.3883F, -174.6981F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -95.6944F, -182.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -92.0005F, -185.7798F, 18.5372F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -99.3883F, -185.7798F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -103.0822F, -167.3103F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -106.7762F, -159.9225F, 11.1493F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -106.7762F, -141.4529F, 14.8433F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -106.7762F, -122.9834F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -110.4701F, -122.9834F, 11.1493F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -92.0005F, -182.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -88.3066F, -189.4737F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -88.3066F, -189.4737F, 11.0817F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -88.3066F, -189.4737F, 14.8433F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.6086F, -88.3066F, -189.4737F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -80.9188F, -189.4737F, 11.0817F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -77.2249F, -163.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -95.6944F, -163.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -106.7762F, -137.759F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -99.3883F, -148.8407F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -103.0823F, -145.1468F, 3.6939F, 3.6939F, 29.5513F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -103.0823F, -152.5346F, 7.3878F, 3.6939F, 48.0208F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -103.0823F, -152.5346F, 18.5372F, 3.6939F, 33.2452F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -84.6127F, -134.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -84.6127F, -137.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -80.9188F, -152.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -88.3066F, -141.4529F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -88.3066F, -145.1468F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -88.3066F, -148.8407F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -92.0005F, -152.5346F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -77.2249F, -137.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -80.9188F, -141.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -88.3066F, -134.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -92.0005F, -137.759F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -92.0005F, -145.1468F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -95.6944F, -145.1468F, 3.6939F, 14.7756F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -95.6944F, -141.4529F, 3.6939F, 3.6939F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -92.0005F, -134.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -92.0005F, -126.6773F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -88.3066F, -126.6773F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -95.6944F, -134.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -69.8371F, -111.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -77.2249F, -126.6773F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -73.531F, -134.0651F, 3.6939F, 20.3165F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -53.1874F, -119.2895F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -56.8813F, -115.5956F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -43.9527F, -137.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -43.9527F, -141.4529F, 3.6939F, 11.0817F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -55.0344F, -141.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -55.0344F, -145.1468F, 3.6939F, 29.5242F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -47.6466F, -148.8407F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -73.531F, -137.759F, 3.6939F, 29.5513F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -32.8709F, -137.759F, 7.3878F, 7.3878F, 22.1364F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -32.8709F, -130.3712F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -21.7892F, -130.3712F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -10.7075F, -130.3712F, 3.6939F, 14.7486F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -27.3301F, -130.3712F, 3.6939F, 16.6226F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -29.2041F, -119.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -29.177F, -126.6773F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -25.4831F, -134.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -25.4831F, -137.7319F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -21.8163F, -134.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -21.8163F, -141.4529F, 3.6939F, 48.0208F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -10.7075F, -126.6773F, 11.0817F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -36.5648F, -115.5956F, 3.6939F, 14.7486F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -32.898F, -108.2077F, 3.6939F, 33.1911F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -66.1432F, -111.9016F, 3.6939F, 29.5783F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -40.2588F, -115.5956F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -29.177F, -115.5956F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -7.0136F, -111.9016F, 3.6939F, 31.3712F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.5769F, 26.2046F, -130.3712F, 15.0597F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 29.8985F, -119.2895F, 3.6939F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 33.5924F, -108.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 26.2046F, -134.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 4.0682F, -134.038F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, 4.0682F, -126.6502F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 29.9255F, -122.9563F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 4.0682F, -130.3441F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 15.1499F, -126.6502F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 15.1499F, -122.9563F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 15.1499F, -119.2624F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 7.7621F, -115.5685F, 3.6939F, 36.9391F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 33.5924F, -134.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -19.9423F, -111.8746F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 15.1499F, -111.8746F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, 18.8167F, -108.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, 4.0411F, -130.3712F, 3.6939F, 22.1635F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -10.7075F, -119.2895F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3304F, -29.177F, -111.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-40.0F, -291.0F, 82.0F, 0.0F, 1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone245(PartDefinition bone244) {
      return bone244.addOrReplaceChild(
         "bone245",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(-105.5849F, 143.9683F, -44.3297F, 7.4078F, 44.4468F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-101.881F, 177.3034F, -40.6258F, 7.4078F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 184.7112F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 181.0073F, -33.2181F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 177.3034F, -40.6258F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 177.3034F, -40.6258F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 181.0073F, -44.3297F, 14.8156F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 147.6723F, -48.0336F, 3.7039F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 103.2255F, -48.0336F, 18.5195F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 129.1528F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 114.3372F, -51.7375F, 14.8156F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 103.2255F, -51.7375F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-90.7693F, 121.745F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 118.0411F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 110.6333F, -48.0336F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 106.9294F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-72.2498F, 84.706F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-68.5459F, 92.1138F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-101.881F, 110.6333F, -51.7375F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 110.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-90.7693F, 110.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-90.7693F, 110.6333F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 106.9294F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-83.3615F, 99.5216F, -51.7375F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 66.1865F, -51.7375F, 22.2234F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 55.0748F, -48.0336F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 51.3709F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 47.667F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 47.667F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 43.9631F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 58.7787F, -48.0336F, 18.5195F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 55.0748F, -48.0336F, 7.4078F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 51.3709F, -48.0336F, 22.2234F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 51.3709F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 47.667F, -48.0336F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 43.9631F, -44.3297F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-83.3615F, 32.8514F, -29.5142F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 29.1475F, -22.1064F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 29.1475F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 29.1475F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-101.881F, 25.4436F, -10.9947F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 25.4436F, -14.6986F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 25.4436F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 25.4436F, -7.2908F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 40.2592F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 40.2592F, -36.922F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 43.9631F, -36.922F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 40.2592F, -33.2181F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 36.5553F, -29.5142F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 32.8514F, -25.8103F, 29.6312F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 32.8514F, -22.1064F, 29.6312F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 36.5553F, -29.5142F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 55.0748F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-72.2498F, 66.1865F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-101.881F, 62.4826F, -51.7375F, 25.9273F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 66.1865F, -51.7375F, 25.9273F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 99.5216F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 77.2982F, -59.1453F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-101.881F, 84.706F, -55.4414F, 22.2234F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 69.8904F, -55.4414F, 18.5195F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 66.1865F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-90.7693F, 62.4826F, -55.4414F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 81.0021F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 95.8177F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-101.881F, 81.0021F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 99.5216F, -51.7375F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 84.706F, -51.7375F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 81.0021F, -51.7375F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 69.8904F, -55.4414F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-101.881F, 143.9684F, -48.0336F, 3.7039F, 33.3351F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 143.9684F, -48.0336F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 143.9684F, -48.0336F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-90.7693F, 143.9684F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 143.9684F, -51.7375F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 143.9684F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 169.8956F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-90.7693F, 173.5995F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-90.7693F, 166.1917F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 155.08F, -44.3297F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-90.7693F, 155.08F, -40.6258F, 22.2234F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 150.784F, -33.2181F, 75.2234F, 19.1117F, 32.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 151.3761F, -33.2181F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-106.6949F, 93.6148F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-106.6949F, 46.9948F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 40.3348F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-106.6949F, 140.2645F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-83.3615F, 123.5969F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-44.4706F, 81.0021F, -1.7349F, 27.7792F, 37.039F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-72.2498F, 140.2645F, -40.6258F, 11.1117F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 147.6722F, -40.6258F, 3.7039F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 151.3761F, -29.5142F, 3.7039F, 3.7039F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 151.3761F, -22.1064F, 3.7039F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 162.4878F, -29.5142F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 151.3761F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 147.6722F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 143.9684F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 136.5606F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-64.842F, 129.1528F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-64.842F, 121.745F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-64.842F, 142.1164F, -36.922F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 92.1138F, -48.0336F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 92.1138F, -51.7375F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 129.1528F, -36.922F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 132.8567F, -36.922F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 136.5606F, -33.2181F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 140.2645F, -29.5142F, 3.7039F, 11.1117F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 129.1528F, -25.8103F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 110.6333F, -25.8103F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 129.1528F, -22.1064F, 7.4078F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 140.2645F, -22.1064F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 129.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 129.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 129.1528F, -22.1064F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 106.9294F, -22.1064F, 7.4078F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.5069F, 129.1528F, -10.9947F, 3.7039F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.5069F, 118.0411F, -18.4025F, 11.1117F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.8031F, 103.2255F, -14.6986F, 11.1117F, 14.8156F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.6914F, 110.6333F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.6914F, 99.5216F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.6914F, 99.5216F, -7.2908F, 3.7039F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.3952F, 92.1138F, -10.9947F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-24.0992F, 81.0021F, -10.9947F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.8031F, 73.5943F, -10.9947F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.8031F, 92.1138F, -14.6986F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.3952F, 121.745F, -14.6986F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.3952F, 118.0411F, -10.9947F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.6914F, 118.0411F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.9875F, 118.0411F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.8031F, 99.5216F, -18.4025F, 3.7039F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 143.9684F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 140.2645F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 143.9684F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 140.2645F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 132.8567F, -25.8103F, 3.7039F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 140.2645F, -25.8103F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 125.4489F, -40.6258F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 121.745F, -36.922F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 118.0411F, -36.922F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 106.9294F, -40.6258F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 110.6333F, -36.922F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 114.3372F, -25.8103F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 121.745F, -29.5142F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 118.0411F, -40.6258F, 11.1117F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 106.9294F, -40.6258F, 14.8156F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 132.8567F, -29.5142F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 136.5606F, -36.922F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 147.6722F, -44.3297F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-83.3615F, 143.9684F, -44.3297F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 140.2645F, -44.3297F, 40.7429F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 121.745F, -44.3297F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-64.842F, 114.3372F, -44.3297F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-68.5459F, 106.9294F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 103.2255F, -44.3297F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-68.5459F, 95.8177F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-72.2498F, 88.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 81.0021F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 77.2982F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-64.842F, 69.8904F, -40.6258F, 11.1117F, 18.5195F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 73.5943F, -40.6258F, 11.1117F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 77.2982F, -40.6258F, 11.1117F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 73.5943F, -40.6258F, 3.7039F, 33.3351F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 92.1138F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 95.8177F, -36.922F, 11.1117F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 88.4099F, -33.2181F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 81.0021F, -25.8103F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 69.8904F, -25.8103F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 69.8904F, -29.5142F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 66.1865F, -33.2181F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 66.1865F, -29.5142F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 66.1865F, -36.922F, 18.5195F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 62.4826F, -36.922F, 7.4078F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 62.4826F, -33.2181F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 58.7787F, -33.2181F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 55.0748F, -33.2181F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 51.3709F, -33.2181F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 55.0748F, -36.922F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-64.842F, 58.7787F, -40.6258F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 51.3709F, -33.2181F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-64.842F, 58.7787F, -36.922F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 58.7787F, -33.2181F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 69.8904F, -33.2181F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-57.4342F, 69.8904F, -36.922F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 84.706F, -36.922F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 81.0021F, -33.2181F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 77.2982F, -36.922F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 77.2982F, -33.2181F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 77.2982F, -29.5142F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 84.706F, -36.922F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 88.4099F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 84.706F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 77.2982F, -36.922F, 11.1117F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-42.6186F, 99.5216F, -33.2181F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-38.9147F, 99.5216F, -29.5142F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-72.2498F, 62.4826F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 51.3709F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-72.2498F, 51.3709F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 47.667F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 47.667F, -44.3297F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-83.3615F, 43.9631F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 40.2592F, -36.922F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 43.9631F, -36.922F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 40.2592F, -33.2181F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-68.5459F, 47.667F, -36.922F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-68.5459F, 43.9631F, -33.2181F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-68.5459F, 40.2592F, -29.5142F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-68.5459F, 36.5553F, -25.8103F, 7.4078F, 11.1117F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 40.2592F, -25.8103F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 36.5553F, -22.1064F, 11.1117F, 7.4078F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 32.8514F, -14.6986F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-53.7303F, 36.5553F, -18.4025F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-61.1381F, 43.9631F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 47.667F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 55.0748F, -29.5142F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 58.7787F, -14.6986F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-46.3225F, 51.3709F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 43.9631F, -25.8103F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-50.0264F, 40.2592F, -22.1064F, 14.8156F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 40.2592F, -18.4025F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 43.9631F, -22.1064F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 47.667F, -25.8103F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.5069F, 40.2592F, -14.6986F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.5069F, 43.9631F, -18.4025F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.5069F, 47.667F, -22.1064F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.5069F, 51.3709F, -25.8103F, 7.4078F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-31.5069F, 66.1865F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-24.0992F, 81.0021F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.3952F, 88.4099F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-16.6914F, 95.8177F, -7.2908F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-27.8031F, 73.5943F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-35.2108F, 58.7787F, -25.8103F, 7.4078F, 33.3351F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-64.842F, 47.667F, -33.2181F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-72.2498F, 88.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 125.4489F, -44.3297F, 44.4468F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 114.3372F, -44.3297F, 14.8156F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-83.3615F, 155.08F, -33.2181F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 158.784F, -40.6258F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 162.4878F, -40.6258F, 22.2234F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 162.4878F, -36.922F, 25.9273F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 162.4878F, -33.2181F, 111.5195F, 11.1117F, 32.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-98.1771F, 166.1917F, -36.922F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-75.9537F, 162.4878F, -25.8103F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-79.6576F, 166.1917F, -25.8103F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-101.881F, 166.1917F, -33.2181F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-87.0654F, 151.3761F, -44.3297F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-83.3615F, 151.3761F, -44.3297F, 22.2234F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 166.1917F, -44.3297F, 18.5195F, 14.8156F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-94.4732F, 184.7112F, -33.2181F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 188.4151F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 177.3034F, -36.922F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-105.5849F, 177.3034F, -29.5142F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-20.3056F, -136.5242F, 0.6997F, 0.0F, -1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone246(PartDefinition bone65) {
      return bone65.addOrReplaceChild(
         "bone246",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(28.0244F, -58.81F, 104.241F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -62.5039F, 100.5471F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -51.4493F, 100.5471F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -62.5039F, 96.8532F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -73.6127F, 96.8532F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -73.6127F, 93.1593F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -62.531F, 93.1593F, 3.6939F, 77.5721F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 18.735F, 93.1593F, 44.3945F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 15.0411F, 117.1697F, 3.6939F, 34.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -1.5815F, 93.1593F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 3.9594F, 100.5471F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -10.8163F, 80.2306F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -14.5102F, 70.9958F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.014F, 26.1228F, 89.4654F, 16.6226F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-14.4556F, 33.5106F, 89.4654F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, 37.2046F, 93.1593F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.2208F, 37.2046F, 96.8532F, 18.4695F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 37.2046F, 100.5471F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 37.2316F, 104.241F, 40.7006F, 7.3608F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, 44.5924F, 104.241F, 3.6939F, 3.6939F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.888F, 51.9802F, 115.3227F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5819F, 51.9802F, 119.0166F, 7.3878F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, 55.6741F, 126.4044F, 7.3878F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, 55.6741F, 133.7923F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.4237F, 51.9802F, 133.7923F, 11.2846F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 44.6194F, 115.3633F, 29.6189F, 5.3608F, 22.0958F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, 51.9802F, 120.8636F, 7.3878F, 3.6939F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, 59.368F, 133.7923F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, 44.5924F, 104.241F, 3.6939F, 3.6939F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, 40.8985F, 100.5471F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 33.5106F, 96.8532F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -47.7283F, 96.8532F, 3.6939F, 81.2389F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -7.1223F, 89.4654F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -10.8163F, 89.4654F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -34.8267F, 59.9141F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -7.1223F, 104.241F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -36.6736F, 78.3836F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -51.4493F, 70.9958F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -88.3883F, 107.9349F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -69.9188F, 104.241F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -44.0614F, 63.608F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -47.7553F, 67.3019F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -47.7553F, 67.3019F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -40.3675F, 59.9141F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -55.1432F, 63.608F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -55.1432F, 67.3019F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -47.7553F, 56.2202F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -47.7553F, 59.9141F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -40.3675F, 56.2202F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -29.2858F, 59.9141F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -21.898F, 63.608F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -25.5919F, 63.608F, 7.3878F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -25.5919F, 59.9141F, 11.0817F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -14.5102F, 74.6897F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -10.8163F, 74.6897F, 7.3878F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -7.1223F, 70.9958F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -18.2041F, 63.608F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-7.0678F, -3.4284F, 63.608F, 5.5409F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, 7.6533F, 82.0775F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, 26.1228F, 85.7714F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -3.4284F, 85.7714F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, 0.2655F, 67.3019F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, 0.2655F, 70.9958F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, 3.9594F, 74.6897F, 3.6939F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -10.8162F, 78.3836F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -3.4284F, 78.3836F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -7.1223F, 78.3836F, 7.3878F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -3.4284F, 82.0775F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -7.1223F, 82.0775F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, 15.0411F, 78.3836F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 22.4289F, 82.0775F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 26.1228F, 85.7714F, 29.6189F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 22.4289F, 78.3836F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 18.735F, 74.6897F, 29.6189F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 18.735F, 70.9958F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 15.0411F, 67.3019F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 15.0411F, 85.7714F, 40.7006F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, 22.4289F, 89.4654F, 18.4695F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, 9.5002F, 89.4654F, 14.7756F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -7.1223F, 82.0775F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -10.8163F, 74.6897F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -3.4284F, 70.9958F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -7.1223F, 67.3019F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -18.2041F, 82.0775F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -25.5919F, 59.9141F, 11.0817F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -18.2041F, 63.608F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -10.8163F, 67.3019F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -29.2858F, 74.6897F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -32.9797F, 63.608F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -32.9797F, 59.9141F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -44.0614F, 82.0775F, 3.6939F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -51.4493F, 85.7714F, 3.6939F, 48.0208F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -58.8371F, 82.0775F, 3.6939F, 25.8574F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -55.1432F, 89.4654F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -73.6127F, 89.4654F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -77.3066F, 85.7714F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -73.6127F, 82.0775F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -73.6127F, 74.6897F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -69.9188F, 82.0775F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -73.6127F, 63.608F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -62.531F, 70.9958F, 3.6939F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -58.8371F, 63.608F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -58.8371F, 59.9141F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -68.0718F, 56.2202F, 29.6189F, 33.2452F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -49.5383F, 63.608F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -88.3883F, 63.608F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -22.8983F, 63.608F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-14.4556F, -42.2145F, 56.2202F, 12.9287F, 46.1739F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -34.8267F, 56.2202F, 16.6902F, 42.48F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -55.1432F, 74.6897F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -55.1432F, 78.3836F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(28.0244F, -62.5039F, 107.9349F, 3.6939F, 33.2452F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.7183F, -51.4222F, 111.6288F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(28.0244F, -62.531F, 107.9349F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(28.0244F, -56.9631F, 122.7105F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -66.2249F, 133.7923F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -69.9188F, 133.7923F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -69.9188F, 130.0984F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -69.9188F, 119.0166F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -73.6127F, 115.3227F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -73.6127F, 122.7105F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -77.3066F, 122.7105F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -81.0005F, 122.7105F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -84.6944F, 119.0166F, 7.3878F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -88.3883F, 111.6288F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -88.3883F, 115.3227F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -92.0823F, 104.241F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -88.3883F, 89.4654F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -84.6944F, 89.4654F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.2208F, -88.3883F, 78.3836F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.9147F, -92.0823F, 78.3836F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -84.6944F, 78.3836F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -69.9188F, 67.3019F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -66.2249F, 63.608F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -36.6736F, 41.4446F, 5.6085F, 14.7756F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -51.4493F, 52.5263F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-14.4556F, -40.3675F, 52.5263F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -25.5919F, 52.5263F, 16.6902F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -44.0614F, 48.8324F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-14.4556F, -32.9797F, 48.8324F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -25.5919F, 41.4446F, 3.6939F, 7.3878F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -14.5102F, 41.4446F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -32.9797F, 45.1385F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -21.898F, 45.1385F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-14.4556F, -18.2041F, 48.8324F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -7.1223F, 48.8324F, 16.6902F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 7.6533F, 63.608F, 22.2311F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, 7.6533F, 59.9141F, 9.3024F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-14.4556F, -3.4284F, 45.1385F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -7.1223F, 41.4446F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -3.4284F, 45.1385F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-18.1495F, -7.1224F, 37.7506F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -58.8371F, 52.5263F, 5.6085F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -51.4492F, 48.8324F, 5.6085F, 7.3878F, 9.2348F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -21.898F, 37.7506F, 5.6085F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -18.2041F, 34.0567F, 5.6085F, 7.3878F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -10.8163F, 45.1385F, 9.3024F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -44.0614F, 45.1385F, 5.6085F, 12.9287F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -62.531F, 56.2202F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -81.0005F, 78.3836F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -77.3066F, 67.3019F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -81.0005F, 63.608F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.2208F, -84.6944F, 63.608F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.9147F, -88.3883F, 67.3019F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.9147F, -84.6944F, 59.9141F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -81.0005F, 56.2202F, 18.5372F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -88.3883F, 56.2202F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -92.0822F, 74.6897F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -95.7762F, 82.0775F, 11.1493F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -95.7762F, 100.5471F, 14.8433F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -95.7762F, 119.0166F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -99.4701F, 119.0166F, 11.1493F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.2208F, -81.0005F, 59.9141F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.9147F, -77.3066F, 52.5263F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.9147F, -77.3066F, 52.5263F, 11.0817F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -77.3066F, 52.5263F, 14.8433F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-12.6086F, -77.3066F, 52.5263F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.2208F, -69.9188F, 52.5263F, 11.0817F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -66.2249F, 78.3836F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -84.6944F, 78.3836F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.2208F, -95.7762F, 104.241F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(2.167F, -88.3883F, 93.1593F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-1.5269F, -92.0823F, 96.8532F, 3.6939F, 3.6939F, 29.5513F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-8.9147F, -92.0823F, 89.4654F, 7.3878F, 3.6939F, 48.0208F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-23.758F, -92.0823F, 89.4654F, 18.5372F, 3.6939F, 33.2452F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -73.6127F, 107.9349F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -73.6127F, 104.241F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -69.9188F, 89.4654F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -77.3066F, 100.5471F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -77.3066F, 96.8532F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -77.3066F, 93.1593F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -81.0005F, 89.4654F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -66.2249F, 104.241F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -69.9188F, 100.5471F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -77.3066F, 107.9349F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -81.0005F, 104.241F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -81.0005F, 96.8532F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.8609F, -84.6944F, 96.8532F, 3.6939F, 14.7756F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(9.5548F, -84.6944F, 100.5471F, 3.6939F, 3.6939F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -81.0005F, 107.9349F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -81.0005F, 115.3227F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -77.3066F, 115.3227F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(13.2487F, -84.6944F, 107.9349F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -58.8371F, 130.0984F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -66.2249F, 115.3227F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -62.531F, 107.9349F, 3.6939F, 20.3165F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(28.0244F, -42.1874F, 122.7105F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(28.0244F, -45.8813F, 126.4044F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(28.0244F, -32.9527F, 104.241F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -32.9527F, 100.5471F, 3.6939F, 11.0817F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -44.0344F, 100.5471F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -44.0344F, 96.8532F, 3.6939F, 29.5242F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -36.6466F, 93.1593F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -62.531F, 104.241F, 3.6939F, 29.5513F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -21.8709F, 104.241F, 7.3878F, 7.3878F, 22.1364F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(28.0244F, -21.8709F, 111.6288F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -10.7892F, 111.6288F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, 0.2925F, 111.6288F, 3.6939F, 14.7486F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -16.3301F, 111.6288F, 3.6939F, 16.6226F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -18.2041F, 122.7105F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(28.0244F, -18.177F, 115.3227F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -14.4831F, 107.9349F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9697F, -14.4831F, 104.268F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9697F, -10.8163F, 107.9349F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, -10.8163F, 100.5471F, 3.6939F, 48.0208F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, 0.2925F, 115.3227F, 11.0817F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -25.5648F, 126.4044F, 3.6939F, 14.7486F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -21.898F, 133.7923F, 3.6939F, 33.1911F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -55.1432F, 130.0984F, 3.6939F, 29.5783F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -29.2588F, 126.4044F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -18.177F, 126.4044F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, 3.9864F, 130.0984F, 3.6939F, 31.3712F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.5769F, 37.2046F, 111.6288F, 15.0597F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, 40.8985F, 122.7105F, 3.6939F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, 44.5924F, 133.7923F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, 37.2046F, 107.9349F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 15.0682F, 107.962F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, 15.0682F, 115.3498F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 40.9255F, 119.0437F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 15.0682F, 111.6559F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 26.1499F, 115.3498F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 26.1499F, 119.0437F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 26.1499F, 122.7376F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 18.7621F, 126.4315F, 3.6939F, 36.9391F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, 44.5924F, 107.9349F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, -8.9423F, 130.1254F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 26.1499F, 130.1254F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 29.8167F, 133.7923F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.9426F, 15.0411F, 111.6288F, 3.6939F, 22.1635F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(20.6365F, 0.2925F, 122.7105F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(24.3304F, -18.177F, 130.0984F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-47.0F, -300.0F, 77.0F, 0.0F, 1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone247(PartDefinition param0) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      //
      // Bytecode:
      // 0000: aload 0
      // 0001: ldc_w "bone247"
      // 0004: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0007: bipush 44
      // 0009: bipush 29
      // 000b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 000e: ldc_w 136.4151
      // 0011: ldc_w 154.9683
      // 0014: ldc_w -41.3297
      // 0017: ldc_w 7.4078
      // 001a: ldc_w 44.4468
      // 001d: ldc_w 11.1117
      // 0020: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0023: dup
      // 0024: fconst_0
      // 0025: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0028: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 002b: bipush 44
      // 002d: bipush 29
      // 002f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0032: ldc_w 140.119
      // 0035: ldc_w 188.3034
      // 0038: ldc_w -37.6258
      // 003b: ldc_w 7.4078
      // 003e: ldc_w 11.1117
      // 0041: ldc_w 11.1117
      // 0044: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0047: dup
      // 0048: fconst_0
      // 0049: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 004c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 004f: bipush 44
      // 0051: bipush 29
      // 0053: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0056: ldc_w 143.8229
      // 0059: ldc_w 195.7112
      // 005c: ldc_w -37.6258
      // 005f: ldc_w 3.7039
      // 0062: ldc_w 3.7039
      // 0065: ldc_w 7.4078
      // 0068: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 006b: dup
      // 006c: fconst_0
      // 006d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0070: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0073: bipush 44
      // 0075: bipush 29
      // 0077: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 007a: ldc_w 154.9346
      // 007d: ldc_w 192.0073
      // 0080: ldc_w -30.2181
      // 0083: ldc_w 3.7039
      // 0086: ldc_w 3.7039
      // 0089: ldc_w 3.7039
      // 008c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 008f: dup
      // 0090: fconst_0
      // 0091: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0094: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0097: bipush 44
      // 0099: bipush 29
      // 009b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 009e: ldc_w 147.5268
      // 00a1: ldc_w 188.3034
      // 00a4: ldc_w -37.6258
      // 00a7: ldc_w 7.4078
      // 00aa: ldc_w 7.4078
      // 00ad: ldc_w 11.1117
      // 00b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 00b3: dup
      // 00b4: fconst_0
      // 00b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 00b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00bb: bipush 44
      // 00bd: bipush 29
      // 00bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00c2: ldc_w 154.9346
      // 00c5: ldc_w 188.3034
      // 00c8: ldc_w -37.6258
      // 00cb: ldc_w 7.4078
      // 00ce: ldc_w 3.7039
      // 00d1: ldc_w 11.1117
      // 00d4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 00d7: dup
      // 00d8: fconst_0
      // 00d9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 00dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00df: bipush 44
      // 00e1: bipush 29
      // 00e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00e6: ldc_w 136.4151
      // 00e9: ldc_w 192.0073
      // 00ec: ldc_w -41.3297
      // 00ef: ldc_w 14.8156
      // 00f2: ldc_w 3.7039
      // 00f5: ldc_w 11.1117
      // 00f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 00fb: dup
      // 00fc: fconst_0
      // 00fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0100: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0103: bipush 44
      // 0105: bipush 29
      // 0107: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 010a: ldc_w 136.4151
      // 010d: ldc_w 158.6723
      // 0110: ldc_w -45.0336
      // 0113: ldc_w 3.7039
      // 0116: ldc_w 37.039
      // 0119: ldc_w 3.7039
      // 011c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 011f: dup
      // 0120: fconst_0
      // 0121: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0124: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0127: bipush 44
      // 0129: bipush 29
      // 012b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 012e: ldc_w 136.4151
      // 0131: ldc_w 114.2255
      // 0134: ldc_w -45.0336
      // 0137: ldc_w 18.5195
      // 013a: ldc_w 29.6312
      // 013d: ldc_w 3.7039
      // 0140: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0143: dup
      // 0144: fconst_0
      // 0145: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0148: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 014b: bipush 44
      // 014d: bipush 29
      // 014f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0152: ldc_w 147.5268
      // 0155: ldc_w 140.1528
      // 0158: ldc_w -48.7375
      // 015b: ldc_w 7.4078
      // 015e: ldc_w 3.7039
      // 0161: ldc_w 3.7039
      // 0164: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0167: dup
      // 0168: fconst_0
      // 0169: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 016c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 016f: bipush 44
      // 0171: bipush 29
      // 0173: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0176: ldc_w 143.8229
      // 0179: ldc_w 125.3372
      // 017c: ldc_w -48.7375
      // 017f: ldc_w 14.8156
      // 0182: ldc_w 14.8156
      // 0185: ldc_w 3.7039
      // 0188: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 018b: dup
      // 018c: fconst_0
      // 018d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0190: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0193: bipush 44
      // 0195: bipush 29
      // 0197: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 019a: ldc_w 143.8229
      // 019d: ldc_w 114.2255
      // 01a0: ldc_w -48.7375
      // 01a3: ldc_w 14.8156
      // 01a6: ldc_w 7.4078
      // 01a9: ldc_w 3.7039
      // 01ac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01af: dup
      // 01b0: fconst_0
      // 01b1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 01b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01b7: bipush 44
      // 01b9: bipush 29
      // 01bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01be: ldc_w 151.2307
      // 01c1: ldc_w 132.745
      // 01c4: ldc_w -45.0336
      // 01c7: ldc_w 14.8156
      // 01ca: ldc_w 7.4078
      // 01cd: ldc_w 3.7039
      // 01d0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01d3: dup
      // 01d4: fconst_0
      // 01d5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 01d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01db: bipush 44
      // 01dd: bipush 29
      // 01df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01e2: ldc_w 147.5268
      // 01e5: ldc_w 129.0411
      // 01e8: ldc_w -45.0336
      // 01eb: ldc_w 14.8156
      // 01ee: ldc_w 7.4078
      // 01f1: ldc_w 3.7039
      // 01f4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01f7: dup
      // 01f8: fconst_0
      // 01f9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 01fc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01ff: bipush 44
      // 0201: bipush 29
      // 0203: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0206: ldc_w 162.3424
      // 0209: ldc_w 121.6333
      // 020c: ldc_w -45.0336
      // 020f: ldc_w 3.7039
      // 0212: ldc_w 7.4078
      // 0215: ldc_w 3.7039
      // 0218: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 021b: dup
      // 021c: fconst_0
      // 021d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0220: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0223: bipush 44
      // 0225: bipush 29
      // 0227: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 022a: ldc_w 166.0463
      // 022d: ldc_w 117.9294
      // 0230: ldc_w -45.0336
      // 0233: ldc_w 3.7039
      // 0236: ldc_w 14.8156
      // 0239: ldc_w 3.7039
      // 023c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 023f: dup
      // 0240: fconst_0
      // 0241: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0244: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0247: bipush 44
      // 0249: bipush 29
      // 024b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 024e: ldc_w 169.7502
      // 0251: ldc_w 95.706
      // 0254: ldc_w -45.0336
      // 0257: ldc_w 3.7039
      // 025a: ldc_w 29.6312
      // 025d: ldc_w 3.7039
      // 0260: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0263: dup
      // 0264: fconst_0
      // 0265: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0268: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 026b: bipush 44
      // 026d: bipush 29
      // 026f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0272: ldc_w 173.4541
      // 0275: ldc_w 103.1138
      // 0278: ldc_w -45.0336
      // 027b: ldc_w 3.7039
      // 027e: ldc_w 11.1117
      // 0281: ldc_w 3.7039
      // 0284: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0287: dup
      // 0288: fconst_0
      // 0289: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 028c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 028f: bipush 44
      // 0291: bipush 29
      // 0293: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0296: ldc_w 140.119
      // 0299: ldc_w 121.6333
      // 029c: ldc_w -48.7375
      // 029f: ldc_w 7.4078
      // 02a2: ldc_w 11.1117
      // 02a5: ldc_w 3.7039
      // 02a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02ab: dup
      // 02ac: fconst_0
      // 02ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02b3: bipush 44
      // 02b5: bipush 29
      // 02b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02ba: ldc_w 136.4151
      // 02bd: ldc_w 121.6333
      // 02c0: ldc_w -48.7375
      // 02c3: ldc_w 11.1117
      // 02c6: ldc_w 3.7039
      // 02c9: ldc_w 3.7039
      // 02cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02cf: dup
      // 02d0: fconst_0
      // 02d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02d7: bipush 44
      // 02d9: bipush 29
      // 02db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02de: ldc_w 151.2307
      // 02e1: ldc_w 121.6333
      // 02e4: ldc_w -48.7375
      // 02e7: ldc_w 11.1117
      // 02ea: ldc_w 3.7039
      // 02ed: ldc_w 3.7039
      // 02f0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02f3: dup
      // 02f4: fconst_0
      // 02f5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02fb: bipush 44
      // 02fd: bipush 29
      // 02ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0302: ldc_w 151.2307
      // 0305: ldc_w 121.6333
      // 0308: ldc_w -52.4414
      // 030b: ldc_w 3.7039
      // 030e: ldc_w 7.4078
      // 0311: ldc_w 3.7039
      // 0314: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0317: dup
      // 0318: fconst_0
      // 0319: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 031c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 031f: bipush 44
      // 0321: bipush 29
      // 0323: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0326: ldc_w 154.9346
      // 0329: ldc_w 117.9294
      // 032c: ldc_w -52.4414
      // 032f: ldc_w 3.7039
      // 0332: ldc_w 7.4078
      // 0335: ldc_w 3.7039
      // 0338: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 033b: dup
      // 033c: fconst_0
      // 033d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0340: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0343: bipush 44
      // 0345: bipush 29
      // 0347: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 034a: ldc_w 158.6385
      // 034d: ldc_w 110.5216
      // 0350: ldc_w -48.7375
      // 0353: ldc_w 3.7039
      // 0356: ldc_w 18.5195
      // 0359: ldc_w 3.7039
      // 035c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 035f: dup
      // 0360: fconst_0
      // 0361: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0364: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0367: bipush 44
      // 0369: bipush 29
      // 036b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 036e: ldc_w 136.4151
      // 0371: ldc_w 77.1865
      // 0374: ldc_w -48.7375
      // 0377: ldc_w 22.2234
      // 037a: ldc_w 37.039
      // 037d: ldc_w 3.7039
      // 0380: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0383: dup
      // 0384: fconst_0
      // 0385: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0388: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 038b: bipush 44
      // 038d: bipush 29
      // 038f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0392: ldc_w 136.4151
      // 0395: ldc_w 66.0748
      // 0398: ldc_w -45.0336
      // 039b: ldc_w 14.8156
      // 039e: ldc_w 37.039
      // 03a1: ldc_w 3.7039
      // 03a4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03a7: dup
      // 03a8: fconst_0
      // 03a9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03af: bipush 44
      // 03b1: bipush 29
      // 03b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03b6: ldc_w 136.4151
      // 03b9: ldc_w 62.3709
      // 03bc: ldc_w -41.3297
      // 03bf: ldc_w 14.8156
      // 03c2: ldc_w 37.039
      // 03c5: ldc_w 3.7039
      // 03c8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03cb: dup
      // 03cc: fconst_0
      // 03cd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03d0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03d3: bipush 44
      // 03d5: bipush 29
      // 03d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03da: ldc_w 136.4151
      // 03dd: ldc_w 58.667
      // 03e0: ldc_w -37.6258
      // 03e3: ldc_w 14.8156
      // 03e6: ldc_w 37.039
      // 03e9: ldc_w 3.7039
      // 03ec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03ef: dup
      // 03f0: fconst_0
      // 03f1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03f7: bipush 44
      // 03f9: bipush 29
      // 03fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03fe: ldc_w 147.5268
      // 0401: ldc_w 58.667
      // 0404: ldc_w -41.3297
      // 0407: ldc_w 14.8156
      // 040a: ldc_w 37.039
      // 040d: ldc_w 3.7039
      // 0410: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0413: dup
      // 0414: fconst_0
      // 0415: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0418: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 041b: bipush 44
      // 041d: bipush 29
      // 041f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0422: ldc_w 143.8229
      // 0425: ldc_w 54.9631
      // 0428: ldc_w -37.6258
      // 042b: ldc_w 14.8156
      // 042e: ldc_w 37.039
      // 0431: ldc_w 3.7039
      // 0434: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0437: dup
      // 0438: fconst_0
      // 0439: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 043c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 043f: bipush 44
      // 0441: bipush 29
      // 0443: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0446: ldc_w 147.5268
      // 0449: ldc_w 69.7787
      // 044c: ldc_w -45.0336
      // 044f: ldc_w 18.5195
      // 0452: ldc_w 3.7039
      // 0455: ldc_w 3.7039
      // 0458: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 045b: dup
      // 045c: fconst_0
      // 045d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0460: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0463: bipush 44
      // 0465: bipush 29
      // 0467: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 046a: ldc_w 154.9346
      // 046d: ldc_w 66.0748
      // 0470: ldc_w -45.0336
      // 0473: ldc_w 7.4078
      // 0476: ldc_w 22.2234
      // 0479: ldc_w 3.7039
      // 047c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 047f: dup
      // 0480: fconst_0
      // 0481: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0484: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0487: bipush 44
      // 0489: bipush 29
      // 048b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 048e: ldc_w 143.8229
      // 0491: ldc_w 62.3709
      // 0494: ldc_w -45.0336
      // 0497: ldc_w 22.2234
      // 049a: ldc_w 3.7039
      // 049d: ldc_w 3.7039
      // 04a0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04a3: dup
      // 04a4: fconst_0
      // 04a5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04a8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04ab: bipush 44
      // 04ad: bipush 29
      // 04af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04b2: ldc_w 154.9346
      // 04b5: ldc_w 62.3709
      // 04b8: ldc_w -48.7375
      // 04bb: ldc_w 7.4078
      // 04be: ldc_w 3.7039
      // 04c1: ldc_w 3.7039
      // 04c4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04c7: dup
      // 04c8: fconst_0
      // 04c9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04cf: bipush 44
      // 04d1: bipush 29
      // 04d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04d6: ldc_w 154.9346
      // 04d9: ldc_w 58.667
      // 04dc: ldc_w -45.0336
      // 04df: ldc_w 7.4078
      // 04e2: ldc_w 3.7039
      // 04e5: ldc_w 3.7039
      // 04e8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04eb: dup
      // 04ec: fconst_0
      // 04ed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04f3: bipush 44
      // 04f5: bipush 29
      // 04f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04fa: ldc_w 154.9346
      // 04fd: ldc_w 54.9631
      // 0500: ldc_w -41.3297
      // 0503: ldc_w 7.4078
      // 0506: ldc_w 3.7039
      // 0509: ldc_w 3.7039
      // 050c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 050f: dup
      // 0510: fconst_0
      // 0511: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0514: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0517: bipush 44
      // 0519: bipush 29
      // 051b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 051e: ldc_w 158.6385
      // 0521: ldc_w 43.8514
      // 0524: ldc_w -26.5142
      // 0527: ldc_w 7.4078
      // 052a: ldc_w 3.7039
      // 052d: ldc_w 3.7039
      // 0530: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0533: dup
      // 0534: fconst_0
      // 0535: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0538: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 053b: bipush 44
      // 053d: bipush 29
      // 053f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0542: ldc_w 154.9346
      // 0545: ldc_w 40.1475
      // 0548: ldc_w -19.1064
      // 054b: ldc_w 11.1117
      // 054e: ldc_w 3.7039
      // 0551: ldc_w 3.7039
      // 0554: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0557: dup
      // 0558: fconst_0
      // 0559: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 055c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 055f: bipush 44
      // 0561: bipush 29
      // 0563: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0566: ldc_w 154.9346
      // 0569: ldc_w 40.1475
      // 056c: ldc_w -11.6986
      // 056f: ldc_w 14.8156
      // 0572: ldc_w 3.7039
      // 0575: ldc_w 14.8156
      // 0578: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 057b: dup
      // 057c: fconst_0
      // 057d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0580: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0583: bipush 44
      // 0585: bipush 29
      // 0587: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 058a: ldc_w 136.4151
      // 058d: ldc_w 40.1475
      // 0590: ldc_w -11.6986
      // 0593: ldc_w 14.8156
      // 0596: ldc_w 3.7039
      // 0599: ldc_w 14.8156
      // 059c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 059f: dup
      // 05a0: fconst_0
      // 05a1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05a7: bipush 44
      // 05a9: bipush 29
      // 05ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05ae: ldc_w 140.119
      // 05b1: ldc_w 36.4436
      // 05b4: ldc_w -7.9947
      // 05b7: ldc_w 11.1117
      // 05ba: ldc_w 3.7039
      // 05bd: ldc_w 11.1117
      // 05c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 05c3: dup
      // 05c4: fconst_0
      // 05c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05cb: bipush 44
      // 05cd: bipush 29
      // 05cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05d2: ldc_w 154.9346
      // 05d5: ldc_w 36.4436
      // 05d8: ldc_w -11.6986
      // 05db: ldc_w 7.4078
      // 05de: ldc_w 3.7039
      // 05e1: ldc_w 14.8156
      // 05e4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 05e7: dup
      // 05e8: fconst_0
      // 05e9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05ef: bipush 44
      // 05f1: bipush 29
      // 05f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05f6: ldc_w 162.3424
      // 05f9: ldc_w 36.4436
      // 05fc: ldc_w -7.9947
      // 05ff: ldc_w 3.7039
      // 0602: ldc_w 3.7039
      // 0605: ldc_w 3.7039
      // 0608: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 060b: dup
      // 060c: fconst_0
      // 060d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0610: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0613: bipush 44
      // 0615: bipush 29
      // 0617: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 061a: ldc_w 166.0463
      // 061d: ldc_w 36.4436
      // 0620: ldc_w -4.2908
      // 0623: ldc_w 3.7039
      // 0626: ldc_w 3.7039
      // 0629: ldc_w 7.4078
      // 062c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 062f: dup
      // 0630: fconst_0
      // 0631: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0634: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0637: bipush 44
      // 0639: bipush 29
      // 063b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 063e: ldc_w 154.9346
      // 0641: ldc_w 51.2592
      // 0644: ldc_w -37.6258
      // 0647: ldc_w 7.4078
      // 064a: ldc_w 3.7039
      // 064d: ldc_w 14.8156
      // 0650: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0653: dup
      // 0654: fconst_0
      // 0655: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0658: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 065b: bipush 44
      // 065d: bipush 29
      // 065f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0662: ldc_w 143.8229
      // 0665: ldc_w 51.2592
      // 0668: ldc_w -33.922
      // 066b: ldc_w 11.1117
      // 066e: ldc_w 3.7039
      // 0671: ldc_w 7.4078
      // 0674: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0677: dup
      // 0678: fconst_0
      // 0679: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 067c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 067f: bipush 44
      // 0681: bipush 29
      // 0683: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0686: ldc_w 136.4151
      // 0689: ldc_w 54.9631
      // 068c: ldc_w -33.922
      // 068f: ldc_w 11.1117
      // 0692: ldc_w 3.7039
      // 0695: ldc_w 3.7039
      // 0698: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 069b: dup
      // 069c: fconst_0
      // 069d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 06a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06a3: bipush 44
      // 06a5: bipush 29
      // 06a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06aa: ldc_w 136.4151
      // 06ad: ldc_w 51.2592
      // 06b0: ldc_w -30.2181
      // 06b3: ldc_w 11.1117
      // 06b6: ldc_w 3.7039
      // 06b9: ldc_w 3.7039
      // 06bc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 06bf: dup
      // 06c0: fconst_0
      // 06c1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 06c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06c7: bipush 44
      // 06c9: bipush 29
      // 06cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06ce: ldc_w 136.4151
      // 06d1: ldc_w 47.5553
      // 06d4: ldc_w -26.5142
      // 06d7: ldc_w 22.2234
      // 06da: ldc_w 3.7039
      // 06dd: ldc_w 7.4078
      // 06e0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 06e3: dup
      // 06e4: fconst_0
      // 06e5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 06e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06eb: bipush 44
      // 06ed: bipush 29
      // 06ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06f2: ldc_w 143.8229
      // 06f5: ldc_w 43.8514
      // 06f8: ldc_w -22.8103
      // 06fb: ldc_w 29.6312
      // 06fe: ldc_w 7.4078
      // 0701: ldc_w 25.9273
      // 0704: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0707: dup
      // 0708: fconst_0
      // 0709: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 070c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 070f: bipush 44
      // 0711: bipush 29
      // 0713: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0716: ldc_w 136.4151
      // 0719: ldc_w 43.8514
      // 071c: ldc_w -19.1064
      // 071f: ldc_w 29.6312
      // 0722: ldc_w 3.7039
      // 0725: ldc_w 22.2234
      // 0728: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 072b: dup
      // 072c: fconst_0
      // 072d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0730: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0733: bipush 44
      // 0735: bipush 29
      // 0737: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 073a: ldc_w 162.3424
      // 073d: ldc_w 47.5553
      // 0740: ldc_w -26.5142
      // 0743: ldc_w 14.8156
      // 0746: ldc_w 3.7039
      // 0749: ldc_w 3.7039
      // 074c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 074f: dup
      // 0750: fconst_0
      // 0751: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0754: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0757: bipush 44
      // 0759: bipush 29
      // 075b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 075e: ldc_w 166.0463
      // 0761: ldc_w 66.0748
      // 0764: ldc_w -45.0336
      // 0767: ldc_w 3.7039
      // 076a: ldc_w 29.6312
      // 076d: ldc_w 3.7039
      // 0770: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0773: dup
      // 0774: fconst_0
      // 0775: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0778: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 077b: bipush 44
      // 077d: bipush 29
      // 077f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0782: ldc_w 169.7502
      // 0785: ldc_w 77.1865
      // 0788: ldc_w -45.0336
      // 078b: ldc_w 3.7039
      // 078e: ldc_w 14.8156
      // 0791: ldc_w 3.7039
      // 0794: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0797: dup
      // 0798: fconst_0
      // 0799: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 079c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 079f: bipush 44
      // 07a1: bipush 29
      // 07a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07a6: ldc_w 140.119
      // 07a9: ldc_w 73.4826
      // 07ac: ldc_w -48.7375
      // 07af: ldc_w 25.9273
      // 07b2: ldc_w 7.4078
      // 07b5: ldc_w 3.7039
      // 07b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 07bb: dup
      // 07bc: fconst_0
      // 07bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 07c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07c3: bipush 44
      // 07c5: bipush 29
      // 07c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07ca: ldc_w 143.8229
      // 07cd: ldc_w 77.1865
      // 07d0: ldc_w -48.7375
      // 07d3: ldc_w 25.9273
      // 07d6: ldc_w 11.1117
      // 07d9: ldc_w 3.7039
      // 07dc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 07df: dup
      // 07e0: fconst_0
      // 07e1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 07e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07e7: bipush 44
      // 07e9: bipush 29
      // 07eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07ee: ldc_w 143.8229
      // 07f1: ldc_w 110.5216
      // 07f4: ldc_w -52.4414
      // 07f7: ldc_w 3.7039
      // 07fa: ldc_w 3.7039
      // 07fd: ldc_w 3.7039
      // 0800: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0803: dup
      // 0804: fconst_0
      // 0805: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0808: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 080b: bipush 44
      // 080d: bipush 29
      // 080f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0812: ldc_w 154.9346
      // 0815: ldc_w 88.2982
      // 0818: ldc_w -56.1453
      // 081b: ldc_w 3.7039
      // 081e: ldc_w 22.2234
      // 0821: ldc_w 3.7039
      // 0824: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0827: dup
      // 0828: fconst_0
      // 0829: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 082c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 082f: bipush 44
      // 0831: bipush 29
      // 0833: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0836: ldc_w 140.119
      // 0839: ldc_w 95.706
      // 083c: ldc_w -52.4414
      // 083f: ldc_w 22.2234
      // 0842: ldc_w 14.8156
      // 0845: ldc_w 3.7039
      // 0848: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 084b: dup
      // 084c: fconst_0
      // 084d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0850: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0853: bipush 44
      // 0855: bipush 29
      // 0857: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 085a: ldc_w 143.8229
      // 085d: ldc_w 80.8904
      // 0860: ldc_w -52.4414
      // 0863: ldc_w 18.5195
      // 0866: ldc_w 11.1117
      // 0869: ldc_w 3.7039
      // 086c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 086f: dup
      // 0870: fconst_0
      // 0871: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0874: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0877: bipush 44
      // 0879: bipush 29
      // 087b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 087e: ldc_w 147.5268
      // 0881: ldc_w 77.1865
      // 0884: ldc_w -52.4414
      // 0887: ldc_w 14.8156
      // 088a: ldc_w 3.7039
      // 088d: ldc_w 3.7039
      // 0890: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0893: dup
      // 0894: fconst_0
      // 0895: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0898: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 089b: bipush 44
      // 089d: bipush 29
      // 089f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08a2: ldc_w 151.2307
      // 08a5: ldc_w 73.4826
      // 08a8: ldc_w -52.4414
      // 08ab: ldc_w 11.1117
      // 08ae: ldc_w 3.7039
      // 08b1: ldc_w 3.7039
      // 08b4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 08b7: dup
      // 08b8: fconst_0
      // 08b9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 08bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08bf: bipush 44
      // 08c1: bipush 29
      // 08c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08c6: ldc_w 147.5268
      // 08c9: ldc_w 92.0021
      // 08cc: ldc_w -52.4414
      // 08cf: ldc_w 14.8156
      // 08d2: ldc_w 3.7039
      // 08d5: ldc_w 3.7039
      // 08d8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 08db: dup
      // 08dc: fconst_0
      // 08dd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 08e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08e3: bipush 44
      // 08e5: bipush 29
      // 08e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08ea: ldc_w 162.3424
      // 08ed: ldc_w 106.8177
      // 08f0: ldc_w -52.4414
      // 08f3: ldc_w 3.7039
      // 08f6: ldc_w 3.7039
      // 08f9: ldc_w 3.7039
      // 08fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 08ff: dup
      // 0900: fconst_0
      // 0901: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0904: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0907: bipush 44
      // 0909: bipush 29
      // 090b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 090e: ldc_w 140.119
      // 0911: ldc_w 92.0021
      // 0914: ldc_w -52.4414
      // 0917: ldc_w 3.7039
      // 091a: ldc_w 3.7039
      // 091d: ldc_w 3.7039
      // 0920: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0923: dup
      // 0924: fconst_0
      // 0925: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0928: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 092b: bipush 44
      // 092d: bipush 29
      // 092f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0932: ldc_w 162.3424
      // 0935: ldc_w 110.5216
      // 0938: ldc_w -48.7375
      // 093b: ldc_w 3.7039
      // 093e: ldc_w 14.8156
      // 0941: ldc_w 3.7039
      // 0944: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0947: dup
      // 0948: fconst_0
      // 0949: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 094c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 094f: bipush 44
      // 0951: bipush 29
      // 0953: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0956: ldc_w 166.0463
      // 0959: ldc_w 95.706
      // 095c: ldc_w -48.7375
      // 095f: ldc_w 3.7039
      // 0962: ldc_w 22.2234
      // 0965: ldc_w 3.7039
      // 0968: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 096b: dup
      // 096c: fconst_0
      // 096d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0970: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0973: bipush 44
      // 0975: bipush 29
      // 0977: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 097a: ldc_w 162.3424
      // 097d: ldc_w 92.0021
      // 0980: ldc_w -48.7375
      // 0983: ldc_w 3.7039
      // 0986: ldc_w 11.1117
      // 0989: ldc_w 3.7039
      // 098c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 098f: dup
      // 0990: fconst_0
      // 0991: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0994: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0997: bipush 44
      // 0999: bipush 29
      // 099b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 099e: ldc_w 162.3424
      // 09a1: ldc_w 80.8904
      // 09a4: ldc_w -52.4414
      // 09a7: ldc_w 3.7039
      // 09aa: ldc_w 11.1117
      // 09ad: ldc_w 7.4078
      // 09b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 09b3: dup
      // 09b4: fconst_0
      // 09b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 09b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09bb: bipush 44
      // 09bd: bipush 29
      // 09bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09c2: ldc_w 140.119
      // 09c5: ldc_w 154.9684
      // 09c8: ldc_w -45.0336
      // 09cb: ldc_w 3.7039
      // 09ce: ldc_w 33.3351
      // 09d1: ldc_w 3.7039
      // 09d4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 09d7: dup
      // 09d8: fconst_0
      // 09d9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 09dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09df: bipush 44
      // 09e1: bipush 29
      // 09e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09e6: ldc_w 143.8229
      // 09e9: ldc_w 154.9684
      // 09ec: ldc_w -45.0336
      // 09ef: ldc_w 3.7039
      // 09f2: ldc_w 22.2234
      // 09f5: ldc_w 3.7039
      // 09f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 09fb: dup
      // 09fc: fconst_0
      // 09fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a00: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a03: bipush 44
      // 0a05: bipush 29
      // 0a07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a0a: ldc_w 147.5268
      // 0a0d: ldc_w 154.9684
      // 0a10: ldc_w -45.0336
      // 0a13: ldc_w 3.7039
      // 0a16: ldc_w 18.5195
      // 0a19: ldc_w 3.7039
      // 0a1c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a1f: dup
      // 0a20: fconst_0
      // 0a21: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a24: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a27: bipush 44
      // 0a29: bipush 29
      // 0a2b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a2e: ldc_w 151.2307
      // 0a31: ldc_w 154.9684
      // 0a34: ldc_w -45.0336
      // 0a37: ldc_w 3.7039
      // 0a3a: ldc_w 11.1117
      // 0a3d: ldc_w 3.7039
      // 0a40: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a43: dup
      // 0a44: fconst_0
      // 0a45: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a48: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a4b: bipush 44
      // 0a4d: bipush 29
      // 0a4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a52: ldc_w 147.5268
      // 0a55: ldc_w 154.9684
      // 0a58: ldc_w -48.7375
      // 0a5b: ldc_w 3.7039
      // 0a5e: ldc_w 3.7039
      // 0a61: ldc_w 3.7039
      // 0a64: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a67: dup
      // 0a68: fconst_0
      // 0a69: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a6c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a6f: bipush 44
      // 0a71: bipush 29
      // 0a73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a76: ldc_w 154.9346
      // 0a79: ldc_w 154.9684
      // 0a7c: ldc_w -45.0336
      // 0a7f: ldc_w 3.7039
      // 0a82: ldc_w 3.7039
      // 0a85: ldc_w 3.7039
      // 0a88: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a8b: dup
      // 0a8c: fconst_0
      // 0a8d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a93: bipush 44
      // 0a95: bipush 29
      // 0a97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a9a: ldc_w 143.8229
      // 0a9d: ldc_w 180.8956
      // 0aa0: ldc_w -45.0336
      // 0aa3: ldc_w 3.7039
      // 0aa6: ldc_w 3.7039
      // 0aa9: ldc_w 3.7039
      // 0aac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0aaf: dup
      // 0ab0: fconst_0
      // 0ab1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ab4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ab7: bipush 44
      // 0ab9: bipush 29
      // 0abb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0abe: ldc_w 151.2307
      // 0ac1: ldc_w 184.5995
      // 0ac4: ldc_w -41.3297
      // 0ac7: ldc_w 7.4078
      // 0aca: ldc_w 3.7039
      // 0acd: ldc_w 11.1117
      // 0ad0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ad3: dup
      // 0ad4: fconst_0
      // 0ad5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ad8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0adb: bipush 44
      // 0add: bipush 29
      // 0adf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ae2: ldc_w 151.2307
      // 0ae5: ldc_w 177.1917
      // 0ae8: ldc_w -41.3297
      // 0aeb: ldc_w 7.4078
      // 0aee: ldc_w 3.7039
      // 0af1: ldc_w 11.1117
      // 0af4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0af7: dup
      // 0af8: fconst_0
      // 0af9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0afc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0aff: bipush 44
      // 0b01: bipush 29
      // 0b03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b06: ldc_w 147.5268
      // 0b09: ldc_w 166.08
      // 0b0c: ldc_w -41.3297
      // 0b0f: ldc_w 22.2234
      // 0b12: ldc_w 11.1117
      // 0b15: ldc_w 3.7039
      // 0b18: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b1b: dup
      // 0b1c: fconst_0
      // 0b1d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b23: bipush 44
      // 0b25: bipush 29
      // 0b27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b2a: ldc_w 151.2307
      // 0b2d: ldc_w 166.08
      // 0b30: ldc_w -37.6258
      // 0b33: ldc_w 22.2234
      // 0b36: ldc_w 11.1117
      // 0b39: ldc_w 7.4078
      // 0b3c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b3f: dup
      // 0b40: fconst_0
      // 0b41: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b47: bipush 44
      // 0b49: bipush 29
      // 0b4b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b4e: ldc_w 154.9346
      // 0b51: ldc_w 169.784
      // 0b54: ldc_w -30.2181
      // 0b57: ldc_w 22.2234
      // 0b5a: ldc_w 11.1117
      // 0b5d: ldc_w 33.3351
      // 0b60: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b63: dup
      // 0b64: fconst_0
      // 0b65: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b68: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b6b: bipush 44
      // 0b6d: bipush 29
      // 0b6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b72: ldc_w 162.3424
      // 0b75: ldc_w 162.3761
      // 0b78: ldc_w -30.2181
      // 0b7b: ldc_w 22.2234
      // 0b7e: ldc_w 11.1117
      // 0b81: ldc_w 33.3351
      // 0b84: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b87: dup
      // 0b88: fconst_0
      // 0b89: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b8c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b8f: bipush 44
      // 0b91: bipush 29
      // 0b93: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b96: ldc_w 135.3051
      // 0b99: ldc_w 104.6148
      // 0b9c: ldc_w 1.2651
      // 0b9f: ldc_w 74.078
      // 0ba2: ldc_w 46.6496
      // 0ba5: ldc_w 1.852
      // 0ba8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0bab: dup
      // 0bac: fconst_0
      // 0bad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bb0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bb3: bipush 44
      // 0bb5: bipush 29
      // 0bb7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bba: ldc_w 135.3051
      // 0bbd: ldc_w 57.9948
      // 0bc0: ldc_w 1.2651
      // 0bc3: ldc_w 74.078
      // 0bc6: ldc_w 46.6496
      // 0bc9: ldc_w 1.852
      // 0bcc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0bcf: dup
      // 0bd0: fconst_0
      // 0bd1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bd4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bd7: bipush 44
      // 0bd9: bipush 29
      // 0bdb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bde: ldc_w 136.4151
      // 0be1: ldc_w 51.3348
      // 0be4: ldc_w 1.2651
      // 0be7: ldc_w 74.078
      // 0bea: ldc_w 46.6496
      // 0bed: ldc_w 1.852
      // 0bf0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0bf3: dup
      // 0bf4: fconst_0
      // 0bf5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bf8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bfb: bipush 44
      // 0bfd: bipush 29
      // 0bff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c02: ldc_w 135.3051
      // 0c05: ldc_w 151.2645
      // 0c08: ldc_w 1.2651
      // 0c0b: ldc_w 40.7429
      // 0c0e: ldc_w 27.7792
      // 0c11: ldc_w 1.852
      // 0c14: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c17: dup
      // 0c18: fconst_0
      // 0c19: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c1f: bipush 44
      // 0c21: bipush 29
      // 0c23: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c26: ldc_w 158.6385
      // 0c29: ldc_w 134.5969
      // 0c2c: ldc_w 1.2651
      // 0c2f: ldc_w 40.7429
      // 0c32: ldc_w 27.7792
      // 0c35: ldc_w 1.852
      // 0c38: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c3b: dup
      // 0c3c: fconst_0
      // 0c3d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c43: bipush 44
      // 0c45: bipush 29
      // 0c47: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c4a: ldc_w 197.5294
      // 0c4d: ldc_w 92.0021
      // 0c50: ldc_w 1.2651
      // 0c53: ldc_w 27.7792
      // 0c56: ldc_w 37.039
      // 0c59: ldc_w 1.852
      // 0c5c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c5f: dup
      // 0c60: fconst_0
      // 0c61: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c67: bipush 44
      // 0c69: bipush 29
      // 0c6b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c6e: ldc_w 169.7502
      // 0c71: ldc_w 151.2645
      // 0c74: ldc_w -37.6258
      // 0c77: ldc_w 11.1117
      // 0c7a: ldc_w 14.8156
      // 0c7d: ldc_w 7.4078
      // 0c80: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c83: dup
      // 0c84: fconst_0
      // 0c85: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c88: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c8b: bipush 44
      // 0c8d: bipush 29
      // 0c8f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c92: ldc_w 184.5658
      // 0c95: ldc_w 158.6722
      // 0c98: ldc_w -37.6258
      // 0c9b: ldc_w 3.7039
      // 0c9e: ldc_w 3.7039
      // 0ca1: ldc_w 14.8156
      // 0ca4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ca7: dup
      // 0ca8: fconst_0
      // 0ca9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0cac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0caf: bipush 44
      // 0cb1: bipush 29
      // 0cb3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cb6: ldc_w 184.5658
      // 0cb9: ldc_w 162.3761
      // 0cbc: ldc_w -26.5142
      // 0cbf: ldc_w 3.7039
      // 0cc2: ldc_w 3.7039
      // 0cc5: ldc_w 29.6312
      // 0cc8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ccb: dup
      // 0ccc: fconst_0
      // 0ccd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0cd0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cd3: bipush 44
      // 0cd5: bipush 29
      // 0cd7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cda: ldc_w 188.2697
      // 0cdd: ldc_w 162.3761
      // 0ce0: ldc_w -19.1064
      // 0ce3: ldc_w 3.7039
      // 0ce6: ldc_w 3.7039
      // 0ce9: ldc_w 22.2234
      // 0cec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0cef: dup
      // 0cf0: fconst_0
      // 0cf1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0cf4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cf7: bipush 44
      // 0cf9: bipush 29
      // 0cfb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cfe: ldc_w 180.8619
      // 0d01: ldc_w 173.4878
      // 0d04: ldc_w -26.5142
      // 0d07: ldc_w 3.7039
      // 0d0a: ldc_w 3.7039
      // 0d0d: ldc_w 7.4078
      // 0d10: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d13: dup
      // 0d14: fconst_0
      // 0d15: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d1b: bipush 44
      // 0d1d: bipush 29
      // 0d1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d22: ldc_w 180.8619
      // 0d25: ldc_w 162.3761
      // 0d28: ldc_w -37.6258
      // 0d2b: ldc_w 3.7039
      // 0d2e: ldc_w 3.7039
      // 0d31: ldc_w 7.4078
      // 0d34: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d37: dup
      // 0d38: fconst_0
      // 0d39: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d3f: bipush 44
      // 0d41: bipush 29
      // 0d43: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d46: ldc_w 180.8619
      // 0d49: ldc_w 158.6722
      // 0d4c: ldc_w -37.6258
      // 0d4f: ldc_w 3.7039
      // 0d52: ldc_w 3.7039
      // 0d55: ldc_w 7.4078
      // 0d58: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d5b: dup
      // 0d5c: fconst_0
      // 0d5d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d63: bipush 44
      // 0d65: bipush 29
      // 0d67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d6a: ldc_w 184.5658
      // 0d6d: ldc_w 154.9684
      // 0d70: ldc_w -37.6258
      // 0d73: ldc_w 7.4078
      // 0d76: ldc_w 3.7039
      // 0d79: ldc_w 14.8156
      // 0d7c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d7f: dup
      // 0d80: fconst_0
      // 0d81: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d87: bipush 44
      // 0d89: bipush 29
      // 0d8b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d8e: ldc_w 180.8619
      // 0d91: ldc_w 147.5606
      // 0d94: ldc_w -37.6258
      // 0d97: ldc_w 7.4078
      // 0d9a: ldc_w 7.4078
      // 0d9d: ldc_w 7.4078
      // 0da0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0da3: dup
      // 0da4: fconst_0
      // 0da5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0da8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dab: bipush 44
      // 0dad: bipush 29
      // 0daf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0db2: ldc_w 177.158
      // 0db5: ldc_w 140.1528
      // 0db8: ldc_w -37.6258
      // 0dbb: ldc_w 7.4078
      // 0dbe: ldc_w 7.4078
      // 0dc1: ldc_w 7.4078
      // 0dc4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0dc7: dup
      // 0dc8: fconst_0
      // 0dc9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0dcc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dcf: bipush 44
      // 0dd1: bipush 29
      // 0dd3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dd6: ldc_w 177.158
      // 0dd9: ldc_w 132.745
      // 0ddc: ldc_w -37.6258
      // 0ddf: ldc_w 7.4078
      // 0de2: ldc_w 7.4078
      // 0de5: ldc_w 7.4078
      // 0de8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0deb: dup
      // 0dec: fconst_0
      // 0ded: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0df0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0df3: bipush 44
      // 0df5: bipush 29
      // 0df7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dfa: ldc_w 177.158
      // 0dfd: ldc_w 153.1164
      // 0e00: ldc_w -33.922
      // 0e03: ldc_w 7.4078
      // 0e06: ldc_w 7.4078
      // 0e09: ldc_w 7.4078
      // 0e0c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e0f: dup
      // 0e10: fconst_0
      // 0e11: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e14: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e17: bipush 44
      // 0e19: bipush 29
      // 0e1b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e1e: ldc_w 162.3424
      // 0e21: ldc_w 103.1138
      // 0e24: ldc_w -45.0336
      // 0e27: ldc_w 7.4078
      // 0e2a: ldc_w 7.4078
      // 0e2d: ldc_w 7.4078
      // 0e30: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e33: dup
      // 0e34: fconst_0
      // 0e35: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e3b: bipush 44
      // 0e3d: bipush 29
      // 0e3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e42: ldc_w 154.9346
      // 0e45: ldc_w 103.1138
      // 0e48: ldc_w -48.7375
      // 0e4b: ldc_w 7.4078
      // 0e4e: ldc_w 7.4078
      // 0e51: ldc_w 7.4078
      // 0e54: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e57: dup
      // 0e58: fconst_0
      // 0e59: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e5c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e5f: bipush 44
      // 0e61: bipush 29
      // 0e63: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e66: ldc_w 184.5658
      // 0e69: ldc_w 140.1528
      // 0e6c: ldc_w -33.922
      // 0e6f: ldc_w 7.4078
      // 0e72: ldc_w 7.4078
      // 0e75: ldc_w 7.4078
      // 0e78: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e7b: dup
      // 0e7c: fconst_0
      // 0e7d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e80: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e83: bipush 44
      // 0e85: bipush 29
      // 0e87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e8a: ldc_w 188.2697
      // 0e8d: ldc_w 143.8567
      // 0e90: ldc_w -33.922
      // 0e93: ldc_w 7.4078
      // 0e96: ldc_w 7.4078
      // 0e99: ldc_w 11.1117
      // 0e9c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e9f: dup
      // 0ea0: fconst_0
      // 0ea1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ea4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ea7: bipush 44
      // 0ea9: bipush 29
      // 0eab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0eae: ldc_w 188.2697
      // 0eb1: ldc_w 147.5606
      // 0eb4: ldc_w -30.2181
      // 0eb7: ldc_w 7.4078
      // 0eba: ldc_w 7.4078
      // 0ebd: ldc_w 7.4078
      // 0ec0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ec3: dup
      // 0ec4: fconst_0
      // 0ec5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ec8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ecb: bipush 44
      // 0ecd: bipush 29
      // 0ecf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ed2: ldc_w 191.9736
      // 0ed5: ldc_w 151.2645
      // 0ed8: ldc_w -26.5142
      // 0edb: ldc_w 3.7039
      // 0ede: ldc_w 11.1117
      // 0ee1: ldc_w 29.6312
      // 0ee4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ee7: dup
      // 0ee8: fconst_0
      // 0ee9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0eec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0eef: bipush 44
      // 0ef1: bipush 29
      // 0ef3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ef6: ldc_w 199.3814
      // 0ef9: ldc_w 140.1528
      // 0efc: ldc_w -22.8103
      // 0eff: ldc_w 3.7039
      // 0f02: ldc_w 25.9273
      // 0f05: ldc_w 3.7039
      // 0f08: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f0b: dup
      // 0f0c: fconst_0
      // 0f0d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f10: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f13: bipush 44
      // 0f15: bipush 29
      // 0f17: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f1a: ldc_w 203.0853
      // 0f1d: ldc_w 121.6333
      // 0f20: ldc_w -22.8103
      // 0f23: ldc_w 3.7039
      // 0f26: ldc_w 25.9273
      // 0f29: ldc_w 3.7039
      // 0f2c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f2f: dup
      // 0f30: fconst_0
      // 0f31: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f34: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f37: bipush 44
      // 0f39: bipush 29
      // 0f3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f3e: ldc_w 199.3814
      // 0f41: ldc_w 140.1528
      // 0f44: ldc_w -19.1064
      // 0f47: ldc_w 7.4078
      // 0f4a: ldc_w 18.5195
      // 0f4d: ldc_w 3.7039
      // 0f50: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f53: dup
      // 0f54: fconst_0
      // 0f55: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f58: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f5b: bipush 44
      // 0f5d: bipush 29
      // 0f5f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f62: ldc_w 206.7892
      // 0f65: ldc_w 151.2645
      // 0f68: ldc_w -19.1064
      // 0f6b: ldc_w 3.7039
      // 0f6e: ldc_w 7.4078
      // 0f71: ldc_w 3.7039
      // 0f74: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f77: dup
      // 0f78: fconst_0
      // 0f79: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f7c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f7f: bipush 44
      // 0f81: bipush 29
      // 0f83: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f86: ldc_w 206.7892
      // 0f89: ldc_w 140.1528
      // 0f8c: ldc_w -15.4025
      // 0f8f: ldc_w 3.7039
      // 0f92: ldc_w 14.8156
      // 0f95: ldc_w 18.5195
      // 0f98: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f9b: dup
      // 0f9c: fconst_0
      // 0f9d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0fa0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fa3: bipush 44
      // 0fa5: bipush 29
      // 0fa7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0faa: ldc_w 203.0853
      // 0fad: ldc_w 140.1528
      // 0fb0: ldc_w -15.4025
      // 0fb3: ldc_w 3.7039
      // 0fb6: ldc_w 14.8156
      // 0fb9: ldc_w 18.5195
      // 0fbc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0fbf: dup
      // 0fc0: fconst_0
      // 0fc1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0fc4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fc7: bipush 44
      // 0fc9: bipush 29
      // 0fcb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fce: ldc_w 206.7892
      // 0fd1: ldc_w 140.1528
      // 0fd4: ldc_w -19.1064
      // 0fd7: ldc_w 3.7039
      // 0fda: ldc_w 7.4078
      // 0fdd: ldc_w 7.4078
      // 0fe0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0fe3: dup
      // 0fe4: fconst_0
      // 0fe5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0fe8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0feb: bipush 44
      // 0fed: bipush 29
      // 0fef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ff2: ldc_w 206.7892
      // 0ff5: ldc_w 117.9294
      // 0ff8: ldc_w -19.1064
      // 0ffb: ldc_w 7.4078
      // 0ffe: ldc_w 22.2234
      // 1001: ldc_w 7.4078
      // 1004: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1007: dup
      // 1008: fconst_0
      // 1009: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 100c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 100f: bipush 44
      // 1011: bipush 29
      // 1013: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1016: ldc_w 210.4931
      // 1019: ldc_w 140.1528
      // 101c: ldc_w -7.9947
      // 101f: ldc_w 3.7039
      // 1022: ldc_w 7.4078
      // 1025: ldc_w 11.1117
      // 1028: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 102b: dup
      // 102c: fconst_0
      // 102d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1030: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1033: bipush 44
      // 1035: bipush 29
      // 1037: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 103a: ldc_w 210.4931
      // 103d: ldc_w 129.0411
      // 1040: ldc_w -15.4025
      // 1043: ldc_w 11.1117
      // 1046: ldc_w 11.1117
      // 1049: ldc_w 18.5195
      // 104c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 104f: dup
      // 1050: fconst_0
      // 1051: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1054: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1057: bipush 44
      // 1059: bipush 29
      // 105b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 105e: ldc_w 214.197
      // 1061: ldc_w 114.2255
      // 1064: ldc_w -11.6986
      // 1067: ldc_w 11.1117
      // 106a: ldc_w 14.8156
      // 106d: ldc_w 14.8156
      // 1070: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1073: dup
      // 1074: fconst_0
      // 1075: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1078: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 107b: bipush 44
      // 107d: bipush 29
      // 107f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1082: ldc_w 225.3086
      // 1085: ldc_w 121.6333
      // 1088: ldc_w -7.9947
      // 108b: ldc_w 3.7039
      // 108e: ldc_w 3.7039
      // 1091: ldc_w 3.7039
      // 1094: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1097: dup
      // 1098: fconst_0
      // 1099: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 109c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 109f: bipush 44
      // 10a1: bipush 29
      // 10a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10a6: ldc_w 225.3086
      // 10a9: ldc_w 110.5216
      // 10ac: ldc_w -7.9947
      // 10af: ldc_w 3.7039
      // 10b2: ldc_w 7.4078
      // 10b5: ldc_w 3.7039
      // 10b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 10bb: dup
      // 10bc: fconst_0
      // 10bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 10c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10c3: bipush 44
      // 10c5: bipush 29
      // 10c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10ca: ldc_w 225.3086
      // 10cd: ldc_w 110.5216
      // 10d0: ldc_w -4.2908
      // 10d3: ldc_w 3.7039
      // 10d6: ldc_w 14.8156
      // 10d9: ldc_w 7.4078
      // 10dc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 10df: dup
      // 10e0: fconst_0
      // 10e1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 10e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10e7: bipush 44
      // 10e9: bipush 29
      // 10eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10ee: ldc_w 221.6047
      // 10f1: ldc_w 103.1138
      // 10f4: ldc_w -7.9947
      // 10f7: ldc_w 3.7039
      // 10fa: ldc_w 14.8156
      // 10fd: ldc_w 3.7039
      // 1100: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1103: dup
      // 1104: fconst_0
      // 1105: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1108: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 110b: bipush 44
      // 110d: bipush 29
      // 110f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1112: ldc_w 217.9008
      // 1115: ldc_w 92.0021
      // 1118: ldc_w -7.9947
      // 111b: ldc_w 3.7039
      // 111e: ldc_w 18.5195
      // 1121: ldc_w 3.7039
      // 1124: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1127: dup
      // 1128: fconst_0
      // 1129: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 112c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 112f: bipush 44
      // 1131: bipush 29
      // 1133: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1136: ldc_w 214.197
      // 1139: ldc_w 84.5943
      // 113c: ldc_w -7.9947
      // 113f: ldc_w 3.7039
      // 1142: ldc_w 22.2234
      // 1145: ldc_w 3.7039
      // 1148: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 114b: dup
      // 114c: fconst_0
      // 114d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1150: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1153: bipush 44
      // 1155: bipush 29
      // 1157: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 115a: ldc_w 214.197
      // 115d: ldc_w 103.1138
      // 1160: ldc_w -11.6986
      // 1163: ldc_w 7.4078
      // 1166: ldc_w 11.1117
      // 1169: ldc_w 3.7039
      // 116c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 116f: dup
      // 1170: fconst_0
      // 1171: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1174: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1177: bipush 44
      // 1179: bipush 29
      // 117b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 117e: ldc_w 221.6047
      // 1181: ldc_w 132.745
      // 1184: ldc_w -11.6986
      // 1187: ldc_w 3.7039
      // 118a: ldc_w 11.1117
      // 118d: ldc_w 3.7039
      // 1190: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1193: dup
      // 1194: fconst_0
      // 1195: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1198: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 119b: bipush 44
      // 119d: bipush 29
      // 119f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11a2: ldc_w 221.6047
      // 11a5: ldc_w 129.0411
      // 11a8: ldc_w -7.9947
      // 11ab: ldc_w 3.7039
      // 11ae: ldc_w 11.1117
      // 11b1: ldc_w 3.7039
      // 11b4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11b7: dup
      // 11b8: fconst_0
      // 11b9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 11bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11bf: bipush 44
      // 11c1: bipush 29
      // 11c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11c6: ldc_w 225.3086
      // 11c9: ldc_w 129.0411
      // 11cc: ldc_w -7.9947
      // 11cf: ldc_w 3.7039
      // 11d2: ldc_w 7.4078
      // 11d5: ldc_w 3.7039
      // 11d8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11db: dup
      // 11dc: fconst_0
      // 11dd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 11e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11e3: bipush 44
      // 11e5: bipush 29
      // 11e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11ea: ldc_w 229.0125
      // 11ed: ldc_w 129.0411
      // 11f0: ldc_w -7.9947
      // 11f3: ldc_w 3.7039
      // 11f6: ldc_w 3.7039
      // 11f9: ldc_w 3.7039
      // 11fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11ff: dup
      // 1200: fconst_0
      // 1201: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1204: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1207: bipush 44
      // 1209: bipush 29
      // 120b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 120e: ldc_w 214.197
      // 1211: ldc_w 110.5216
      // 1214: ldc_w -15.4025
      // 1217: ldc_w 3.7039
      // 121a: ldc_w 29.6312
      // 121d: ldc_w 7.4078
      // 1220: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1223: dup
      // 1224: fconst_0
      // 1225: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1228: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 122b: bipush 44
      // 122d: bipush 29
      // 122f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1232: ldc_w 195.6775
      // 1235: ldc_w 154.9684
      // 1238: ldc_w -15.4025
      // 123b: ldc_w 7.4078
      // 123e: ldc_w 7.4078
      // 1241: ldc_w 3.7039
      // 1244: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1247: dup
      // 1248: fconst_0
      // 1249: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 124c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 124f: bipush 44
      // 1251: bipush 29
      // 1253: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1256: ldc_w 199.3814
      // 1259: ldc_w 151.2645
      // 125c: ldc_w -15.4025
      // 125f: ldc_w 7.4078
      // 1262: ldc_w 7.4078
      // 1265: ldc_w 3.7039
      // 1268: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 126b: dup
      // 126c: fconst_0
      // 126d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1270: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1273: bipush 44
      // 1275: bipush 29
      // 1277: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 127a: ldc_w 195.6775
      // 127d: ldc_w 154.9684
      // 1280: ldc_w -11.6986
      // 1283: ldc_w 3.7039
      // 1286: ldc_w 7.4078
      // 1289: ldc_w 14.8156
      // 128c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 128f: dup
      // 1290: fconst_0
      // 1291: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1294: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1297: bipush 44
      // 1299: bipush 29
      // 129b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 129e: ldc_w 199.3814
      // 12a1: ldc_w 151.2645
      // 12a4: ldc_w -11.6986
      // 12a7: ldc_w 3.7039
      // 12aa: ldc_w 7.4078
      // 12ad: ldc_w 14.8156
      // 12b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12b3: dup
      // 12b4: fconst_0
      // 12b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 12b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12bb: bipush 44
      // 12bd: bipush 29
      // 12bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12c2: ldc_w 195.6775
      // 12c5: ldc_w 143.8567
      // 12c8: ldc_w -22.8103
      // 12cb: ldc_w 3.7039
      // 12ce: ldc_w 22.2234
      // 12d1: ldc_w 7.4078
      // 12d4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12d7: dup
      // 12d8: fconst_0
      // 12d9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 12dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12df: bipush 44
      // 12e1: bipush 29
      // 12e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12e6: ldc_w 188.2697
      // 12e9: ldc_w 151.2645
      // 12ec: ldc_w -22.8103
      // 12ef: ldc_w 3.7039
      // 12f2: ldc_w 11.1117
      // 12f5: ldc_w 3.7039
      // 12f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12fb: dup
      // 12fc: fconst_0
      // 12fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1300: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1303: bipush 44
      // 1305: bipush 29
      // 1307: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 130a: ldc_w 188.2697
      // 130d: ldc_w 136.4489
      // 1310: ldc_w -37.6258
      // 1313: ldc_w 11.1117
      // 1316: ldc_w 7.4078
      // 1319: ldc_w 14.8156
      // 131c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 131f: dup
      // 1320: fconst_0
      // 1321: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1324: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1327: bipush 44
      // 1329: bipush 29
      // 132b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 132e: ldc_w 191.9736
      // 1331: ldc_w 132.745
      // 1334: ldc_w -33.922
      // 1337: ldc_w 11.1117
      // 133a: ldc_w 7.4078
      // 133d: ldc_w 11.1117
      // 1340: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1343: dup
      // 1344: fconst_0
      // 1345: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1348: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 134b: bipush 44
      // 134d: bipush 29
      // 134f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1352: ldc_w 195.6775
      // 1355: ldc_w 129.0411
      // 1358: ldc_w -33.922
      // 135b: ldc_w 11.1117
      // 135e: ldc_w 3.7039
      // 1361: ldc_w 11.1117
      // 1364: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1367: dup
      // 1368: fconst_0
      // 1369: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 136c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 136f: bipush 44
      // 1371: bipush 29
      // 1373: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1376: ldc_w 199.3814
      // 1379: ldc_w 117.9294
      // 137c: ldc_w -37.6258
      // 137f: ldc_w 11.1117
      // 1382: ldc_w 3.7039
      // 1385: ldc_w 18.5195
      // 1388: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 138b: dup
      // 138c: fconst_0
      // 138d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1390: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1393: bipush 44
      // 1395: bipush 29
      // 1397: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 139a: ldc_w 199.3814
      // 139d: ldc_w 121.6333
      // 13a0: ldc_w -33.922
      // 13a3: ldc_w 11.1117
      // 13a6: ldc_w 3.7039
      // 13a9: ldc_w 18.5195
      // 13ac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 13af: dup
      // 13b0: fconst_0
      // 13b1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 13b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13b7: bipush 44
      // 13b9: bipush 29
      // 13bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13be: ldc_w 206.7892
      // 13c1: ldc_w 125.3372
      // 13c4: ldc_w -22.8103
      // 13c7: ldc_w 3.7039
      // 13ca: ldc_w 3.7039
      // 13cd: ldc_w 7.4078
      // 13d0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 13d3: dup
      // 13d4: fconst_0
      // 13d5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 13d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13db: bipush 44
      // 13dd: bipush 29
      // 13df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13e2: ldc_w 195.6775
      // 13e5: ldc_w 132.745
      // 13e8: ldc_w -26.5142
      // 13eb: ldc_w 11.1117
      // 13ee: ldc_w 3.7039
      // 13f1: ldc_w 11.1117
      // 13f4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 13f7: dup
      // 13f8: fconst_0
      // 13f9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 13fc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13ff: bipush 44
      // 1401: bipush 29
      // 1403: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1406: ldc_w 184.5658
      // 1409: ldc_w 129.0411
      // 140c: ldc_w -37.6258
      // 140f: ldc_w 11.1117
      // 1412: ldc_w 11.1117
      // 1415: ldc_w 14.8156
      // 1418: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 141b: dup
      // 141c: fconst_0
      // 141d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1420: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1423: bipush 44
      // 1425: bipush 29
      // 1427: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 142a: ldc_w 191.9736
      // 142d: ldc_w 117.9294
      // 1430: ldc_w -37.6258
      // 1433: ldc_w 14.8156
      // 1436: ldc_w 11.1117
      // 1439: ldc_w 14.8156
      // 143c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 143f: dup
      // 1440: fconst_0
      // 1441: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1444: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1447: bipush 44
      // 1449: bipush 29
      // 144b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 144e: ldc_w 195.6775
      // 1451: ldc_w 143.8567
      // 1454: ldc_w -26.5142
      // 1457: ldc_w 3.7039
      // 145a: ldc_w 3.7039
      // 145d: ldc_w 3.7039
      // 1460: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1463: dup
      // 1464: fconst_0
      // 1465: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1468: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 146b: bipush 44
      // 146d: bipush 29
      // 146f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1472: ldc_w 184.5658
      // 1475: ldc_w 147.5606
      // 1478: ldc_w -33.922
      // 147b: ldc_w 7.4078
      // 147e: ldc_w 7.4078
      // 1481: ldc_w 7.4078
      // 1484: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1487: dup
      // 1488: fconst_0
      // 1489: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 148c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 148f: bipush 44
      // 1491: bipush 29
      // 1493: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1496: ldc_w 154.9346
      // 1499: ldc_w 158.6722
      // 149c: ldc_w -41.3297
      // 149f: ldc_w 22.2234
      // 14a2: ldc_w 3.7039
      // 14a5: ldc_w 7.4078
      // 14a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14ab: dup
      // 14ac: fconst_0
      // 14ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 14b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14b3: bipush 44
      // 14b5: bipush 29
      // 14b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14ba: ldc_w 158.6385
      // 14bd: ldc_w 154.9684
      // 14c0: ldc_w -41.3297
      // 14c3: ldc_w 14.8156
      // 14c6: ldc_w 3.7039
      // 14c9: ldc_w 7.4078
      // 14cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14cf: dup
      // 14d0: fconst_0
      // 14d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 14d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14d7: bipush 44
      // 14d9: bipush 29
      // 14db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14de: ldc_w 136.4151
      // 14e1: ldc_w 151.2645
      // 14e4: ldc_w -41.3297
      // 14e7: ldc_w 40.7429
      // 14ea: ldc_w 3.7039
      // 14ed: ldc_w 7.4078
      // 14f0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14f3: dup
      // 14f4: fconst_0
      // 14f5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 14f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14fb: bipush 44
      // 14fd: bipush 29
      // 14ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1502: ldc_w 180.8619
      // 1505: ldc_w 132.745
      // 1508: ldc_w -41.3297
      // 150b: ldc_w 3.7039
      // 150e: ldc_w 7.4078
      // 1511: ldc_w 7.4078
      // 1514: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1517: dup
      // 1518: fconst_0
      // 1519: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 151c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 151f: bipush 44
      // 1521: bipush 29
      // 1523: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1526: ldc_w 177.158
      // 1529: ldc_w 125.3372
      // 152c: ldc_w -41.3297
      // 152f: ldc_w 11.1117
      // 1532: ldc_w 7.4078
      // 1535: ldc_w 7.4078
      // 1538: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 153b: dup
      // 153c: fconst_0
      // 153d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1540: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1543: bipush 44
      // 1545: bipush 29
      // 1547: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 154a: ldc_w 173.4541
      // 154d: ldc_w 117.9294
      // 1550: ldc_w -41.3297
      // 1553: ldc_w 18.5195
      // 1556: ldc_w 11.1117
      // 1559: ldc_w 7.4078
      // 155c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 155f: dup
      // 1560: fconst_0
      // 1561: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1564: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1567: bipush 44
      // 1569: bipush 29
      // 156b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 156e: ldc_w 191.9736
      // 1571: ldc_w 114.2255
      // 1574: ldc_w -41.3297
      // 1577: ldc_w 3.7039
      // 157a: ldc_w 3.7039
      // 157d: ldc_w 7.4078
      // 1580: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1583: dup
      // 1584: fconst_0
      // 1585: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1588: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 158b: bipush 44
      // 158d: bipush 29
      // 158f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1592: ldc_w 173.4541
      // 1595: ldc_w 106.8177
      // 1598: ldc_w -41.3297
      // 159b: ldc_w 18.5195
      // 159e: ldc_w 11.1117
      // 15a1: ldc_w 7.4078
      // 15a4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15a7: dup
      // 15a8: fconst_0
      // 15a9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15af: bipush 44
      // 15b1: bipush 29
      // 15b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15b6: ldc_w 169.7502
      // 15b9: ldc_w 99.4099
      // 15bc: ldc_w -41.3297
      // 15bf: ldc_w 18.5195
      // 15c2: ldc_w 11.1117
      // 15c5: ldc_w 7.4078
      // 15c8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15cb: dup
      // 15cc: fconst_0
      // 15cd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15d0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15d3: bipush 44
      // 15d5: bipush 29
      // 15d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15da: ldc_w 166.0463
      // 15dd: ldc_w 92.0021
      // 15e0: ldc_w -41.3297
      // 15e3: ldc_w 18.5195
      // 15e6: ldc_w 11.1117
      // 15e9: ldc_w 7.4078
      // 15ec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15ef: dup
      // 15f0: fconst_0
      // 15f1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15f7: bipush 44
      // 15f9: bipush 29
      // 15fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15fe: ldc_w 162.3424
      // 1601: ldc_w 88.2982
      // 1604: ldc_w -41.3297
      // 1607: ldc_w 18.5195
      // 160a: ldc_w 11.1117
      // 160d: ldc_w 7.4078
      // 1610: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1613: dup
      // 1614: fconst_0
      // 1615: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1618: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 161b: bipush 44
      // 161d: bipush 29
      // 161f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1622: ldc_w 177.158
      // 1625: ldc_w 80.8904
      // 1628: ldc_w -37.6258
      // 162b: ldc_w 11.1117
      // 162e: ldc_w 18.5195
      // 1631: ldc_w 7.4078
      // 1634: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1637: dup
      // 1638: fconst_0
      // 1639: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 163c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 163f: bipush 44
      // 1641: bipush 29
      // 1643: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1646: ldc_w 180.8619
      // 1649: ldc_w 84.5943
      // 164c: ldc_w -37.6258
      // 164f: ldc_w 11.1117
      // 1652: ldc_w 22.2234
      // 1655: ldc_w 7.4078
      // 1658: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 165b: dup
      // 165c: fconst_0
      // 165d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1660: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1663: bipush 44
      // 1665: bipush 29
      // 1667: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 166a: ldc_w 188.2697
      // 166d: ldc_w 88.2982
      // 1670: ldc_w -37.6258
      // 1673: ldc_w 11.1117
      // 1676: ldc_w 29.6312
      // 1679: ldc_w 7.4078
      // 167c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 167f: dup
      // 1680: fconst_0
      // 1681: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1684: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1687: bipush 44
      // 1689: bipush 29
      // 168b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 168e: ldc_w 188.2697
      // 1691: ldc_w 84.5943
      // 1694: ldc_w -37.6258
      // 1697: ldc_w 3.7039
      // 169a: ldc_w 33.3351
      // 169d: ldc_w 7.4078
      // 16a0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16a3: dup
      // 16a4: fconst_0
      // 16a5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16a8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16ab: bipush 44
      // 16ad: bipush 29
      // 16af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16b2: ldc_w 199.3814
      // 16b5: ldc_w 103.1138
      // 16b8: ldc_w -37.6258
      // 16bb: ldc_w 11.1117
      // 16be: ldc_w 7.4078
      // 16c1: ldc_w 7.4078
      // 16c4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16c7: dup
      // 16c8: fconst_0
      // 16c9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16cf: bipush 44
      // 16d1: bipush 29
      // 16d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16d6: ldc_w 203.0853
      // 16d9: ldc_w 106.8177
      // 16dc: ldc_w -33.922
      // 16df: ldc_w 11.1117
      // 16e2: ldc_w 3.7039
      // 16e5: ldc_w 22.2234
      // 16e8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16eb: dup
      // 16ec: fconst_0
      // 16ed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16f3: bipush 44
      // 16f5: bipush 29
      // 16f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16fa: ldc_w 203.0853
      // 16fd: ldc_w 99.4099
      // 1700: ldc_w -30.2181
      // 1703: ldc_w 11.1117
      // 1706: ldc_w 7.4078
      // 1709: ldc_w 18.5195
      // 170c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 170f: dup
      // 1710: fconst_0
      // 1711: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1714: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1717: bipush 44
      // 1719: bipush 29
      // 171b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 171e: ldc_w 203.0853
      // 1721: ldc_w 92.0021
      // 1724: ldc_w -22.8103
      // 1727: ldc_w 11.1117
      // 172a: ldc_w 11.1117
      // 172d: ldc_w 11.1117
      // 1730: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1733: dup
      // 1734: fconst_0
      // 1735: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1738: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 173b: bipush 44
      // 173d: bipush 29
      // 173f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1742: ldc_w 203.0853
      // 1745: ldc_w 80.8904
      // 1748: ldc_w -22.8103
      // 174b: ldc_w 11.1117
      // 174e: ldc_w 11.1117
      // 1751: ldc_w 11.1117
      // 1754: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1757: dup
      // 1758: fconst_0
      // 1759: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 175c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 175f: bipush 44
      // 1761: bipush 29
      // 1763: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1766: ldc_w 199.3814
      // 1769: ldc_w 80.8904
      // 176c: ldc_w -26.5142
      // 176f: ldc_w 11.1117
      // 1772: ldc_w 7.4078
      // 1775: ldc_w 3.7039
      // 1778: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 177b: dup
      // 177c: fconst_0
      // 177d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1780: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1783: bipush 44
      // 1785: bipush 29
      // 1787: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 178a: ldc_w 199.3814
      // 178d: ldc_w 77.1865
      // 1790: ldc_w -30.2181
      // 1793: ldc_w 11.1117
      // 1796: ldc_w 3.7039
      // 1799: ldc_w 3.7039
      // 179c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 179f: dup
      // 17a0: fconst_0
      // 17a1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 17a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17a7: bipush 44
      // 17a9: bipush 29
      // 17ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17ae: ldc_w 203.0853
      // 17b1: ldc_w 77.1865
      // 17b4: ldc_w -26.5142
      // 17b7: ldc_w 11.1117
      // 17ba: ldc_w 3.7039
      // 17bd: ldc_w 3.7039
      // 17c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 17c3: dup
      // 17c4: fconst_0
      // 17c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 17c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17cb: bipush 44
      // 17cd: bipush 29
      // 17cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17d2: ldc_w 188.2697
      // 17d5: ldc_w 77.1865
      // 17d8: ldc_w -33.922
      // 17db: ldc_w 18.5195
      // 17de: ldc_w 7.4078
      // 17e1: ldc_w 7.4078
      // 17e4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 17e7: dup
      // 17e8: fconst_0
      // 17e9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 17ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17ef: bipush 44
      // 17f1: bipush 29
      // 17f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17f6: ldc_w 191.9736
      // 17f9: ldc_w 73.4826
      // 17fc: ldc_w -33.922
      // 17ff: ldc_w 7.4078
      // 1802: ldc_w 3.7039
      // 1805: ldc_w 7.4078
      // 1808: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 180b: dup
      // 180c: fconst_0
      // 180d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1810: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1813: bipush 44
      // 1815: bipush 29
      // 1817: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 181a: ldc_w 199.3814
      // 181d: ldc_w 73.4826
      // 1820: ldc_w -30.2181
      // 1823: ldc_w 14.8156
      // 1826: ldc_w 3.7039
      // 1829: ldc_w 7.4078
      // 182c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 182f: dup
      // 1830: fconst_0
      // 1831: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1834: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1837: bipush 44
      // 1839: bipush 29
      // 183b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 183e: ldc_w 191.9736
      // 1841: ldc_w 69.7787
      // 1844: ldc_w -30.2181
      // 1847: ldc_w 14.8156
      // 184a: ldc_w 3.7039
      // 184d: ldc_w 7.4078
      // 1850: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1853: dup
      // 1854: fconst_0
      // 1855: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1858: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 185b: bipush 44
      // 185d: bipush 29
      // 185f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1862: ldc_w 188.2697
      // 1865: ldc_w 66.0748
      // 1868: ldc_w -30.2181
      // 186b: ldc_w 14.8156
      // 186e: ldc_w 3.7039
      // 1871: ldc_w 7.4078
      // 1874: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1877: dup
      // 1878: fconst_0
      // 1879: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 187c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 187f: bipush 44
      // 1881: bipush 29
      // 1883: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1886: ldc_w 191.9736
      // 1889: ldc_w 62.3709
      // 188c: ldc_w -30.2181
      // 188f: ldc_w 3.7039
      // 1892: ldc_w 3.7039
      // 1895: ldc_w 3.7039
      // 1898: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 189b: dup
      // 189c: fconst_0
      // 189d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18a3: bipush 44
      // 18a5: bipush 29
      // 18a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18aa: ldc_w 184.5658
      // 18ad: ldc_w 66.0748
      // 18b0: ldc_w -33.922
      // 18b3: ldc_w 3.7039
      // 18b6: ldc_w 3.7039
      // 18b9: ldc_w 3.7039
      // 18bc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 18bf: dup
      // 18c0: fconst_0
      // 18c1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18c7: bipush 44
      // 18c9: bipush 29
      // 18cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18ce: ldc_w 177.158
      // 18d1: ldc_w 69.7787
      // 18d4: ldc_w -37.6258
      // 18d7: ldc_w 3.7039
      // 18da: ldc_w 3.7039
      // 18dd: ldc_w 3.7039
      // 18e0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 18e3: dup
      // 18e4: fconst_0
      // 18e5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18eb: bipush 44
      // 18ed: bipush 29
      // 18ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18f2: ldc_w 184.5658
      // 18f5: ldc_w 62.3709
      // 18f8: ldc_w -30.2181
      // 18fb: ldc_w 3.7039
      // 18fe: ldc_w 3.7039
      // 1901: ldc_w 3.7039
      // 1904: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1907: dup
      // 1908: fconst_0
      // 1909: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 190c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 190f: bipush 44
      // 1911: bipush 29
      // 1913: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1916: ldc_w 177.158
      // 1919: ldc_w 69.7787
      // 191c: ldc_w -33.922
      // 191f: ldc_w 14.8156
      // 1922: ldc_w 11.1117
      // 1925: ldc_w 7.4078
      // 1928: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 192b: dup
      // 192c: fconst_0
      // 192d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1930: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1933: bipush 44
      // 1935: bipush 29
      // 1937: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 193a: ldc_w 191.9736
      // 193d: ldc_w 69.7787
      // 1940: ldc_w -30.2181
      // 1943: ldc_w 14.8156
      // 1946: ldc_w 3.7039
      // 1949: ldc_w 7.4078
      // 194c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 194f: dup
      // 1950: fconst_0
      // 1951: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1954: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1957: bipush 44
      // 1959: bipush 29
      // 195b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 195e: ldc_w 188.2697
      // 1961: ldc_w 80.8904
      // 1964: ldc_w -30.2181
      // 1967: ldc_w 11.1117
      // 196a: ldc_w 7.4078
      // 196d: ldc_w 3.7039
      // 1970: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1973: dup
      // 1974: fconst_0
      // 1975: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1978: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 197b: bipush 44
      // 197d: bipush 29
      // 197f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1982: ldc_w 184.5658
      // 1985: ldc_w 80.8904
      // 1988: ldc_w -33.922
      // 198b: ldc_w 11.1117
      // 198e: ldc_w 7.4078
      // 1991: ldc_w 3.7039
      // 1994: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1997: dup
      // 1998: fconst_0
      // 1999: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 199c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 199f: bipush 44
      // 19a1: bipush 29
      // 19a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19a6: ldc_w 203.0853
      // 19a9: ldc_w 95.706
      // 19ac: ldc_w -33.922
      // 19af: ldc_w 11.1117
      // 19b2: ldc_w 7.4078
      // 19b5: ldc_w 7.4078
      // 19b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 19bb: dup
      // 19bc: fconst_0
      // 19bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 19c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19c3: bipush 44
      // 19c5: bipush 29
      // 19c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19ca: ldc_w 203.0853
      // 19cd: ldc_w 92.0021
      // 19d0: ldc_w -30.2181
      // 19d3: ldc_w 11.1117
      // 19d6: ldc_w 3.7039
      // 19d9: ldc_w 7.4078
      // 19dc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 19df: dup
      // 19e0: fconst_0
      // 19e1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 19e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19e7: bipush 44
      // 19e9: bipush 29
      // 19eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19ee: ldc_w 206.7892
      // 19f1: ldc_w 88.2982
      // 19f4: ldc_w -33.922
      // 19f7: ldc_w 3.7039
      // 19fa: ldc_w 7.4078
      // 19fd: ldc_w 3.7039
      // 1a00: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a03: dup
      // 1a04: fconst_0
      // 1a05: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a08: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a0b: bipush 44
      // 1a0d: bipush 29
      // 1a0f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a12: ldc_w 199.3814
      // 1a15: ldc_w 88.2982
      // 1a18: ldc_w -30.2181
      // 1a1b: ldc_w 7.4078
      // 1a1e: ldc_w 7.4078
      // 1a21: ldc_w 3.7039
      // 1a24: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a27: dup
      // 1a28: fconst_0
      // 1a29: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a2c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a2f: bipush 44
      // 1a31: bipush 29
      // 1a33: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a36: ldc_w 199.3814
      // 1a39: ldc_w 88.2982
      // 1a3c: ldc_w -26.5142
      // 1a3f: ldc_w 3.7039
      // 1a42: ldc_w 7.4078
      // 1a45: ldc_w 3.7039
      // 1a48: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a4b: dup
      // 1a4c: fconst_0
      // 1a4d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a50: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a53: bipush 44
      // 1a55: bipush 29
      // 1a57: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a5a: ldc_w 203.0853
      // 1a5d: ldc_w 95.706
      // 1a60: ldc_w -33.922
      // 1a63: ldc_w 11.1117
      // 1a66: ldc_w 7.4078
      // 1a69: ldc_w 7.4078
      // 1a6c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a6f: dup
      // 1a70: fconst_0
      // 1a71: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a74: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a77: bipush 44
      // 1a79: bipush 29
      // 1a7b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a7e: ldc_w 195.6775
      // 1a81: ldc_w 99.4099
      // 1a84: ldc_w -37.6258
      // 1a87: ldc_w 11.1117
      // 1a8a: ldc_w 7.4078
      // 1a8d: ldc_w 7.4078
      // 1a90: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a93: dup
      // 1a94: fconst_0
      // 1a95: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a98: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a9b: bipush 44
      // 1a9d: bipush 29
      // 1a9f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1aa2: ldc_w 191.9736
      // 1aa5: ldc_w 95.706
      // 1aa8: ldc_w -37.6258
      // 1aab: ldc_w 11.1117
      // 1aae: ldc_w 7.4078
      // 1ab1: ldc_w 7.4078
      // 1ab4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ab7: dup
      // 1ab8: fconst_0
      // 1ab9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1abc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1abf: bipush 44
      // 1ac1: bipush 29
      // 1ac3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ac6: ldc_w 195.6775
      // 1ac9: ldc_w 88.2982
      // 1acc: ldc_w -33.922
      // 1acf: ldc_w 11.1117
      // 1ad2: ldc_w 29.6312
      // 1ad5: ldc_w 3.7039
      // 1ad8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1adb: dup
      // 1adc: fconst_0
      // 1add: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ae0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ae3: bipush 44
      // 1ae5: bipush 29
      // 1ae7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1aea: ldc_w 199.3814
      // 1aed: ldc_w 110.5216
      // 1af0: ldc_w -30.2181
      // 1af3: ldc_w 11.1117
      // 1af6: ldc_w 7.4078
      // 1af9: ldc_w 7.4078
      // 1afc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1aff: dup
      // 1b00: fconst_0
      // 1b01: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b07: bipush 44
      // 1b09: bipush 29
      // 1b0b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b0e: ldc_w 203.0853
      // 1b11: ldc_w 110.5216
      // 1b14: ldc_w -26.5142
      // 1b17: ldc_w 11.1117
      // 1b1a: ldc_w 7.4078
      // 1b1d: ldc_w 11.1117
      // 1b20: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b23: dup
      // 1b24: fconst_0
      // 1b25: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b2b: bipush 44
      // 1b2d: bipush 29
      // 1b2f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b32: ldc_w 169.7502
      // 1b35: ldc_w 73.4826
      // 1b38: ldc_w -41.3297
      // 1b3b: ldc_w 7.4078
      // 1b3e: ldc_w 14.8156
      // 1b41: ldc_w 7.4078
      // 1b44: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b47: dup
      // 1b48: fconst_0
      // 1b49: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b4c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b4f: bipush 44
      // 1b51: bipush 29
      // 1b53: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b56: ldc_w 166.0463
      // 1b59: ldc_w 62.3709
      // 1b5c: ldc_w -41.3297
      // 1b5f: ldc_w 7.4078
      // 1b62: ldc_w 14.8156
      // 1b65: ldc_w 7.4078
      // 1b68: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b6b: dup
      // 1b6c: fconst_0
      // 1b6d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b73: bipush 44
      // 1b75: bipush 29
      // 1b77: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b7a: ldc_w 169.7502
      // 1b7d: ldc_w 62.3709
      // 1b80: ldc_w -37.6258
      // 1b83: ldc_w 7.4078
      // 1b86: ldc_w 11.1117
      // 1b89: ldc_w 7.4078
      // 1b8c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b8f: dup
      // 1b90: fconst_0
      // 1b91: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b97: bipush 44
      // 1b99: bipush 29
      // 1b9b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b9e: ldc_w 166.0463
      // 1ba1: ldc_w 58.667
      // 1ba4: ldc_w -37.6258
      // 1ba7: ldc_w 7.4078
      // 1baa: ldc_w 11.1117
      // 1bad: ldc_w 7.4078
      // 1bb0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1bb3: dup
      // 1bb4: fconst_0
      // 1bb5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1bb8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bbb: bipush 44
      // 1bbd: bipush 29
      // 1bbf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bc2: ldc_w 162.3424
      // 1bc5: ldc_w 58.667
      // 1bc8: ldc_w -41.3297
      // 1bcb: ldc_w 7.4078
      // 1bce: ldc_w 11.1117
      // 1bd1: ldc_w 7.4078
      // 1bd4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1bd7: dup
      // 1bd8: fconst_0
      // 1bd9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1bdc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bdf: bipush 44
      // 1be1: bipush 29
      // 1be3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1be6: ldc_w 158.6385
      // 1be9: ldc_w 54.9631
      // 1bec: ldc_w -37.6258
      // 1bef: ldc_w 7.4078
      // 1bf2: ldc_w 11.1117
      // 1bf5: ldc_w 7.4078
      // 1bf8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1bfb: dup
      // 1bfc: fconst_0
      // 1bfd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c00: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c03: bipush 44
      // 1c05: bipush 29
      // 1c07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c0a: ldc_w 162.3424
      // 1c0d: ldc_w 51.2592
      // 1c10: ldc_w -33.922
      // 1c13: ldc_w 7.4078
      // 1c16: ldc_w 11.1117
      // 1c19: ldc_w 7.4078
      // 1c1c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c1f: dup
      // 1c20: fconst_0
      // 1c21: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c24: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c27: bipush 44
      // 1c29: bipush 29
      // 1c2b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c2e: ldc_w 166.0463
      // 1c31: ldc_w 54.9631
      // 1c34: ldc_w -33.922
      // 1c37: ldc_w 7.4078
      // 1c3a: ldc_w 11.1117
      // 1c3d: ldc_w 7.4078
      // 1c40: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c43: dup
      // 1c44: fconst_0
      // 1c45: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c48: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c4b: bipush 44
      // 1c4d: bipush 29
      // 1c4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c52: ldc_w 166.0463
      // 1c55: ldc_w 51.2592
      // 1c58: ldc_w -30.2181
      // 1c5b: ldc_w 7.4078
      // 1c5e: ldc_w 11.1117
      // 1c61: ldc_w 7.4078
      // 1c64: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c67: dup
      // 1c68: fconst_0
      // 1c69: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c6c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c6f: bipush 44
      // 1c71: bipush 29
      // 1c73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c76: ldc_w 173.4541
      // 1c79: ldc_w 58.667
      // 1c7c: ldc_w -33.922
      // 1c7f: ldc_w 7.4078
      // 1c82: ldc_w 11.1117
      // 1c85: ldc_w 7.4078
      // 1c88: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c8b: dup
      // 1c8c: fconst_0
      // 1c8d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c93: bipush 44
      // 1c95: bipush 29
      // 1c97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c9a: ldc_w 173.4541
      // 1c9d: ldc_w 54.9631
      // 1ca0: ldc_w -30.2181
      // 1ca3: ldc_w 7.4078
      // 1ca6: ldc_w 11.1117
      // 1ca9: ldc_w 7.4078
      // 1cac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1caf: dup
      // 1cb0: fconst_0
      // 1cb1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1cb4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cb7: bipush 44
      // 1cb9: bipush 29
      // 1cbb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cbe: ldc_w 173.4541
      // 1cc1: ldc_w 51.2592
      // 1cc4: ldc_w -26.5142
      // 1cc7: ldc_w 7.4078
      // 1cca: ldc_w 11.1117
      // 1ccd: ldc_w 7.4078
      // 1cd0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1cd3: dup
      // 1cd4: fconst_0
      // 1cd5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1cd8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cdb: bipush 44
      // 1cdd: bipush 29
      // 1cdf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ce2: ldc_w 173.4541
      // 1ce5: ldc_w 47.5553
      // 1ce8: ldc_w -22.8103
      // 1ceb: ldc_w 7.4078
      // 1cee: ldc_w 11.1117
      // 1cf1: ldc_w 25.9273
      // 1cf4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1cf7: dup
      // 1cf8: fconst_0
      // 1cf9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1cfc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cff: bipush 44
      // 1d01: bipush 29
      // 1d03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d06: ldc_w 180.8619
      // 1d09: ldc_w 51.2592
      // 1d0c: ldc_w -22.8103
      // 1d0f: ldc_w 11.1117
      // 1d12: ldc_w 7.4078
      // 1d15: ldc_w 7.4078
      // 1d18: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d1b: dup
      // 1d1c: fconst_0
      // 1d1d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d23: bipush 44
      // 1d25: bipush 29
      // 1d27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d2a: ldc_w 180.8619
      // 1d2d: ldc_w 47.5553
      // 1d30: ldc_w -19.1064
      // 1d33: ldc_w 11.1117
      // 1d36: ldc_w 7.4078
      // 1d39: ldc_w 22.2234
      // 1d3c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d3f: dup
      // 1d40: fconst_0
      // 1d41: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d47: bipush 44
      // 1d49: bipush 29
      // 1d4b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d4e: ldc_w 180.8619
      // 1d51: ldc_w 43.8514
      // 1d54: ldc_w -11.6986
      // 1d57: ldc_w 11.1117
      // 1d5a: ldc_w 7.4078
      // 1d5d: ldc_w 14.8156
      // 1d60: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d63: dup
      // 1d64: fconst_0
      // 1d65: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d68: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d6b: bipush 44
      // 1d6d: bipush 29
      // 1d6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d72: ldc_w 188.2697
      // 1d75: ldc_w 47.5553
      // 1d78: ldc_w -15.4025
      // 1d7b: ldc_w 11.1117
      // 1d7e: ldc_w 7.4078
      // 1d81: ldc_w 18.5195
      // 1d84: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d87: dup
      // 1d88: fconst_0
      // 1d89: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d8c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d8f: bipush 44
      // 1d91: bipush 29
      // 1d93: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d96: ldc_w 180.8619
      // 1d99: ldc_w 54.9631
      // 1d9c: ldc_w -26.5142
      // 1d9f: ldc_w 11.1117
      // 1da2: ldc_w 11.1117
      // 1da5: ldc_w 7.4078
      // 1da8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1dab: dup
      // 1dac: fconst_0
      // 1dad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1db0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1db3: bipush 44
      // 1db5: bipush 29
      // 1db7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dba: ldc_w 191.9736
      // 1dbd: ldc_w 58.667
      // 1dc0: ldc_w -26.5142
      // 1dc3: ldc_w 11.1117
      // 1dc6: ldc_w 11.1117
      // 1dc9: ldc_w 7.4078
      // 1dcc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1dcf: dup
      // 1dd0: fconst_0
      // 1dd1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1dd4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dd7: bipush 44
      // 1dd9: bipush 29
      // 1ddb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dde: ldc_w 206.7892
      // 1de1: ldc_w 66.0748
      // 1de4: ldc_w -26.5142
      // 1de7: ldc_w 11.1117
      // 1dea: ldc_w 7.4078
      // 1ded: ldc_w 11.1117
      // 1df0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1df3: dup
      // 1df4: fconst_0
      // 1df5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1df8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dfb: bipush 44
      // 1dfd: bipush 29
      // 1dff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e02: ldc_w 206.7892
      // 1e05: ldc_w 69.7787
      // 1e08: ldc_w -11.6986
      // 1e0b: ldc_w 11.1117
      // 1e0e: ldc_w 3.7039
      // 1e11: ldc_w 3.7039
      // 1e14: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e17: dup
      // 1e18: fconst_0
      // 1e19: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e1f: bipush 44
      // 1e21: bipush 29
      // 1e23: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e26: ldc_w 195.6775
      // 1e29: ldc_w 62.3709
      // 1e2c: ldc_w -26.5142
      // 1e2f: ldc_w 11.1117
      // 1e32: ldc_w 11.1117
      // 1e35: ldc_w 7.4078
      // 1e38: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e3b: dup
      // 1e3c: fconst_0
      // 1e3d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e43: bipush 44
      // 1e45: bipush 29
      // 1e47: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e4a: ldc_w 191.9736
      // 1e4d: ldc_w 54.9631
      // 1e50: ldc_w -22.8103
      // 1e53: ldc_w 14.8156
      // 1e56: ldc_w 11.1117
      // 1e59: ldc_w 7.4078
      // 1e5c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e5f: dup
      // 1e60: fconst_0
      // 1e61: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e67: bipush 44
      // 1e69: bipush 29
      // 1e6b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e6e: ldc_w 191.9736
      // 1e71: ldc_w 51.2592
      // 1e74: ldc_w -19.1064
      // 1e77: ldc_w 14.8156
      // 1e7a: ldc_w 11.1117
      // 1e7d: ldc_w 22.2234
      // 1e80: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e83: dup
      // 1e84: fconst_0
      // 1e85: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e88: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e8b: bipush 44
      // 1e8d: bipush 29
      // 1e8f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e92: ldc_w 206.7892
      // 1e95: ldc_w 51.2592
      // 1e98: ldc_w -15.4025
      // 1e9b: ldc_w 3.7039
      // 1e9e: ldc_w 11.1117
      // 1ea1: ldc_w 18.5195
      // 1ea4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ea7: dup
      // 1ea8: fconst_0
      // 1ea9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1eac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eaf: bipush 44
      // 1eb1: bipush 29
      // 1eb3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eb6: ldc_w 206.7892
      // 1eb9: ldc_w 54.9631
      // 1ebc: ldc_w -19.1064
      // 1ebf: ldc_w 3.7039
      // 1ec2: ldc_w 11.1117
      // 1ec5: ldc_w 18.5195
      // 1ec8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ecb: dup
      // 1ecc: fconst_0
      // 1ecd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ed0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ed3: bipush 44
      // 1ed5: bipush 29
      // 1ed7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eda: ldc_w 206.7892
      // 1edd: ldc_w 58.667
      // 1ee0: ldc_w -22.8103
      // 1ee3: ldc_w 3.7039
      // 1ee6: ldc_w 11.1117
      // 1ee9: ldc_w 18.5195
      // 1eec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1eef: dup
      // 1ef0: fconst_0
      // 1ef1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ef4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ef7: bipush 44
      // 1ef9: bipush 29
      // 1efb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1efe: ldc_w 210.4931
      // 1f01: ldc_w 51.2592
      // 1f04: ldc_w -11.6986
      // 1f07: ldc_w 3.7039
      // 1f0a: ldc_w 11.1117
      // 1f0d: ldc_w 14.8156
      // 1f10: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f13: dup
      // 1f14: fconst_0
      // 1f15: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f1b: bipush 44
      // 1f1d: bipush 29
      // 1f1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f22: ldc_w 210.4931
      // 1f25: ldc_w 54.9631
      // 1f28: ldc_w -15.4025
      // 1f2b: ldc_w 3.7039
      // 1f2e: ldc_w 11.1117
      // 1f31: ldc_w 14.8156
      // 1f34: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f37: dup
      // 1f38: fconst_0
      // 1f39: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f3f: bipush 44
      // 1f41: bipush 29
      // 1f43: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f46: ldc_w 210.4931
      // 1f49: ldc_w 58.667
      // 1f4c: ldc_w -19.1064
      // 1f4f: ldc_w 3.7039
      // 1f52: ldc_w 11.1117
      // 1f55: ldc_w 14.8156
      // 1f58: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f5b: dup
      // 1f5c: fconst_0
      // 1f5d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f63: bipush 44
      // 1f65: bipush 29
      // 1f67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f6a: ldc_w 210.4931
      // 1f6d: ldc_w 62.3709
      // 1f70: ldc_w -22.8103
      // 1f73: ldc_w 7.4078
      // 1f76: ldc_w 7.4078
      // 1f79: ldc_w 25.9273
      // 1f7c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f7f: dup
      // 1f80: fconst_0
      // 1f81: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f87: bipush 44
      // 1f89: bipush 29
      // 1f8b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f8e: ldc_w 210.4931
      // 1f91: ldc_w 77.1865
      // 1f94: ldc_w -4.2908
      // 1f97: ldc_w 7.4078
      // 1f9a: ldc_w 7.4078
      // 1f9d: ldc_w 7.4078
      // 1fa0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1fa3: dup
      // 1fa4: fconst_0
      // 1fa5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1fa8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fab: bipush 44
      // 1fad: bipush 29
      // 1faf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fb2: ldc_w 217.9008
      // 1fb5: ldc_w 92.0021
      // 1fb8: ldc_w -4.2908
      // 1fbb: ldc_w 7.4078
      // 1fbe: ldc_w 7.4078
      // 1fc1: ldc_w 7.4078
      // 1fc4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1fc7: dup
      // 1fc8: fconst_0
      // 1fc9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1fcc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fcf: bipush 44
      // 1fd1: bipush 29
      // 1fd3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fd6: ldc_w 221.6047
      // 1fd9: ldc_w 99.4099
      // 1fdc: ldc_w -4.2908
      // 1fdf: ldc_w 7.4078
      // 1fe2: ldc_w 7.4078
      // 1fe5: ldc_w 7.4078
      // 1fe8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1feb: dup
      // 1fec: fconst_0
      // 1fed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ff0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ff3: bipush 44
      // 1ff5: bipush 29
      // 1ff7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ffa: ldc_w 225.3086
      // 1ffd: ldc_w 106.8177
      // 2000: ldc_w -4.2908
      // 2003: ldc_w 7.4078
      // 2006: ldc_w 11.1117
      // 2009: ldc_w 7.4078
      // 200c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 200f: dup
      // 2010: fconst_0
      // 2011: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2014: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2017: bipush 44
      // 2019: bipush 29
      // 201b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 201e: ldc_w 214.197
      // 2021: ldc_w 84.5943
      // 2024: ldc_w -4.2908
      // 2027: ldc_w 7.4078
      // 202a: ldc_w 7.4078
      // 202d: ldc_w 7.4078
      // 2030: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2033: dup
      // 2034: fconst_0
      // 2035: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2038: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 203b: bipush 44
      // 203d: bipush 29
      // 203f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2042: ldc_w 206.7892
      // 2045: ldc_w 69.7787
      // 2048: ldc_w -22.8103
      // 204b: ldc_w 7.4078
      // 204e: ldc_w 33.3351
      // 2051: ldc_w 25.9273
      // 2054: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2057: dup
      // 2058: fconst_0
      // 2059: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 205c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 205f: bipush 44
      // 2061: bipush 29
      // 2063: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2066: ldc_w 177.158
      // 2069: ldc_w 58.667
      // 206c: ldc_w -30.2181
      // 206f: ldc_w 7.4078
      // 2072: ldc_w 11.1117
      // 2075: ldc_w 7.4078
      // 2078: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 207b: dup
      // 207c: fconst_0
      // 207d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2080: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2083: bipush 44
      // 2085: bipush 29
      // 2087: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 208a: ldc_w 169.7502
      // 208d: ldc_w 99.4099
      // 2090: ldc_w -41.3297
      // 2093: ldc_w 18.5195
      // 2096: ldc_w 11.1117
      // 2099: ldc_w 7.4078
      // 209c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 209f: dup
      // 20a0: fconst_0
      // 20a1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 20a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20a7: bipush 44
      // 20a9: bipush 29
      // 20ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20ae: ldc_w 136.4151
      // 20b1: ldc_w 136.4489
      // 20b4: ldc_w -41.3297
      // 20b7: ldc_w 44.4468
      // 20ba: ldc_w 14.8156
      // 20bd: ldc_w 7.4078
      // 20c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 20c3: dup
      // 20c4: fconst_0
      // 20c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 20c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20cb: bipush 44
      // 20cd: bipush 29
      // 20cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20d2: ldc_w 162.3424
      // 20d5: ldc_w 125.3372
      // 20d8: ldc_w -41.3297
      // 20db: ldc_w 14.8156
      // 20de: ldc_w 14.8156
      // 20e1: ldc_w 7.4078
      // 20e4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 20e7: dup
      // 20e8: fconst_0
      // 20e9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 20ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20ef: bipush 44
      // 20f1: bipush 29
      // 20f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20f6: ldc_w 158.6385
      // 20f9: ldc_w 166.08
      // 20fc: ldc_w -30.2181
      // 20ff: ldc_w 22.2234
      // 2102: ldc_w 11.1117
      // 2105: ldc_w 33.3351
      // 2108: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 210b: dup
      // 210c: fconst_0
      // 210d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2110: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2113: bipush 44
      // 2115: bipush 29
      // 2117: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 211a: ldc_w 147.5268
      // 211d: ldc_w 169.784
      // 2120: ldc_w -37.6258
      // 2123: ldc_w 22.2234
      // 2126: ldc_w 11.1117
      // 2129: ldc_w 11.1117
      // 212c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 212f: dup
      // 2130: fconst_0
      // 2131: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2134: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2137: bipush 44
      // 2139: bipush 29
      // 213b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 213e: ldc_w 143.8229
      // 2141: ldc_w 173.4878
      // 2144: ldc_w -37.6258
      // 2147: ldc_w 22.2234
      // 214a: ldc_w 11.1117
      // 214d: ldc_w 22.2234
      // 2150: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2153: dup
      // 2154: fconst_0
      // 2155: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2158: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 215b: bipush 44
      // 215d: bipush 29
      // 215f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2162: ldc_w 136.4151
      // 2165: ldc_w 173.4878
      // 2168: ldc_w -33.922
      // 216b: ldc_w 25.9273
      // 216e: ldc_w 11.1117
      // 2171: ldc_w 22.2234
      // 2174: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2177: dup
      // 2178: fconst_0
      // 2179: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 217c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 217f: bipush 44
      // 2181: bipush 29
      // 2183: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2186: ldc_w 136.4151
      // 2189: ldc_w 173.4878
      // 218c: ldc_w -30.2181
      // 218f: ldc_w 18.5195
      // 2192: ldc_w 11.1117
      // 2195: ldc_w 33.3351
      // 2198: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 219b: dup
      // 219c: fconst_0
      // 219d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 21a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21a3: bipush 44
      // 21a5: bipush 29
      // 21a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21aa: ldc_w 143.8229
      // 21ad: ldc_w 177.1917
      // 21b0: ldc_w -33.922
      // 21b3: ldc_w 22.2234
      // 21b6: ldc_w 11.1117
      // 21b9: ldc_w 3.7039
      // 21bc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 21bf: dup
      // 21c0: fconst_0
      // 21c1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 21c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21c7: bipush 44
      // 21c9: bipush 29
      // 21cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21ce: ldc_w 166.0463
      // 21d1: ldc_w 173.4878
      // 21d4: ldc_w -22.8103
      // 21d7: ldc_w 3.7039
      // 21da: ldc_w 11.1117
      // 21dd: ldc_w 3.7039
      // 21e0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 21e3: dup
      // 21e4: fconst_0
      // 21e5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 21e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21eb: bipush 44
      // 21ed: bipush 29
      // 21ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21f2: ldc_w 162.3424
      // 21f5: ldc_w 177.1917
      // 21f8: ldc_w -22.8103
      // 21fb: ldc_w 3.7039
      // 21fe: ldc_w 11.1117
      // 2201: ldc_w 3.7039
      // 2204: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2207: dup
      // 2208: fconst_0
      // 2209: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 220c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 220f: bipush 44
      // 2211: bipush 29
      // 2213: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2216: ldc_w 140.119
      // 2219: ldc_w 177.1917
      // 221c: ldc_w -30.2181
      // 221f: ldc_w 22.2234
      // 2222: ldc_w 11.1117
      // 2225: ldc_w 11.1117
      // 2228: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 222b: dup
      // 222c: fconst_0
      // 222d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2230: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2233: bipush 44
      // 2235: bipush 29
      // 2237: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 223a: ldc_w 154.9346
      // 223d: ldc_w 162.3761
      // 2240: ldc_w -41.3297
      // 2243: ldc_w 22.2234
      // 2246: ldc_w 11.1117
      // 2249: ldc_w 11.1117
      // 224c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 224f: dup
      // 2250: fconst_0
      // 2251: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2254: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2257: bipush 44
      // 2259: bipush 29
      // 225b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 225e: ldc_w 158.6385
      // 2261: ldc_w 162.3761
      // 2264: ldc_w -41.3297
      // 2267: ldc_w 22.2234
      // 226a: ldc_w 7.4078
      // 226d: ldc_w 11.1117
      // 2270: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2273: dup
      // 2274: fconst_0
      // 2275: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2278: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 227b: bipush 44
      // 227d: bipush 29
      // 227f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2282: ldc_w 136.4151
      // 2285: ldc_w 177.1917
      // 2288: ldc_w -41.3297
      // 228b: ldc_w 18.5195
      // 228e: ldc_w 14.8156
      // 2291: ldc_w 11.1117
      // 2294: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2297: dup
      // 2298: fconst_0
      // 2299: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 229c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 229f: bipush 44
      // 22a1: bipush 29
      // 22a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22a6: ldc_w 147.5268
      // 22a9: ldc_w 195.7112
      // 22ac: ldc_w -30.2181
      // 22af: ldc_w 3.7039
      // 22b2: ldc_w 3.7039
      // 22b5: ldc_w 3.7039
      // 22b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 22bb: dup
      // 22bc: fconst_0
      // 22bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 22c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22c3: bipush 44
      // 22c5: bipush 29
      // 22c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22ca: ldc_w 136.4151
      // 22cd: ldc_w 199.4151
      // 22d0: ldc_w -37.6258
      // 22d3: ldc_w 3.7039
      // 22d6: ldc_w 3.7039
      // 22d9: ldc_w 7.4078
      // 22dc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 22df: dup
      // 22e0: fconst_0
      // 22e1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 22e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22e7: bipush 44
      // 22e9: bipush 29
      // 22eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22ee: ldc_w 136.4151
      // 22f1: ldc_w 188.3034
      // 22f4: ldc_w -33.922
      // 22f7: ldc_w 3.7039
      // 22fa: ldc_w 11.1117
      // 22fd: ldc_w 7.4078
      // 2300: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2303: dup
      // 2304: fconst_0
      // 2305: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2308: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 230b: bipush 44
      // 230d: bipush 29
      // 230f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2312: ldc_w 136.4151
      // 2315: ldc_w 188.3034
      // 2318: ldc_w -26.5142
      // 231b: ldc_w 7.4078
      // 231e: ldc_w 3.7039
      // 2321: ldc_w 11.1117
      // 2324: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2327: dup
      // 2328: fconst_0
      // 2329: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 232c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 232f: ldc_w -20.3056
      // 2332: ldc_w -136.5242
      // 2335: ldc_w 0.6997
      // 2338: fconst_0
      // 2339: ldc_w -1.5708
      // 233c: fconst_0
      // 233d: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 2340: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 2343: areturn
   }

   private static PartDefinition make_bone145(PartDefinition bone65) {
      return bone65.addOrReplaceChild(
         "bone145",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(31.0244F, -124.81F, -31.759F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -128.5039F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -117.4493F, -35.4529F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -128.5039F, -39.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -139.6127F, -39.1468F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -139.6127F, -42.8407F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -128.531F, -42.8407F, 3.6939F, 77.5721F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -47.265F, -42.8407F, 44.3945F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -50.9589F, -18.8303F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -67.5815F, -42.8407F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -62.0406F, -35.4529F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -76.8163F, -55.7694F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -80.5102F, -65.0042F, 3.6939F, 36.9391F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.014F, -39.8772F, -46.5346F, 16.6226F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -32.4894F, -46.5346F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -28.7954F, -42.8407F, 16.6226F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -28.7954F, -39.1468F, 18.4695F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -28.7954F, -35.4529F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -28.7684F, -31.759F, 40.7006F, 7.3608F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -21.4076F, -31.759F, 3.6939F, 3.6939F, 11.0547F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.888F, -14.0198F, -20.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5819F, -14.0198F, -16.9834F, 7.3878F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -10.3259F, -9.5955F, 7.3878F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -10.3259F, -2.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.4237F, -14.0198F, -2.2077F, 11.2846F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -21.3806F, -20.6367F, 29.6189F, 7.3608F, 22.0958F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -14.0198F, -15.1364F, 7.3878F, 3.6939F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -6.632F, -2.2077F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -21.4076F, -31.759F, 3.6939F, 3.6939F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -25.1015F, -35.4529F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -32.4894F, -39.1468F, 40.7006F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -113.7283F, -39.1468F, 3.6939F, 81.2389F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -73.1223F, -46.5346F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -76.8163F, -46.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -100.8267F, -76.0859F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -73.1224F, -31.759F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -102.6736F, -57.6164F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -117.4493F, -65.0042F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -154.3883F, -28.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -135.9188F, -31.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -110.0614F, -72.392F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -113.7553F, -68.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -113.7553F, -68.6981F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -106.3675F, -76.0859F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -121.1432F, -72.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -121.1432F, -68.6981F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -113.7553F, -79.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -113.7553F, -76.0859F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -106.3675F, -79.7798F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -95.2858F, -76.0859F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -87.898F, -72.392F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -91.5919F, -72.392F, 7.3878F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -91.5919F, -76.0859F, 11.0817F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -80.5102F, -61.3103F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -76.8162F, -61.3103F, 7.3878F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -73.1224F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -84.2041F, -72.392F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-4.0678F, -69.4284F, -72.392F, 5.5409F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -58.3467F, -53.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -39.8772F, -50.2285F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -69.4284F, -50.2285F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -65.7345F, -68.6981F, 3.6939F, 25.8574F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -65.7345F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -62.0406F, -61.3103F, 3.6939F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -76.8162F, -57.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -69.4284F, -57.6164F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -73.1223F, -57.6164F, 7.3878F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -69.4284F, -53.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -73.1223F, -53.9225F, 3.6939F, 29.5513F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -50.9589F, -57.6164F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -43.5711F, -53.9225F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -39.8772F, -50.2285F, 29.6189F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -43.5711F, -57.6164F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -47.265F, -61.3103F, 29.6189F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -47.265F, -65.0042F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -50.9589F, -68.6981F, 25.925F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -50.9589F, -50.2285F, 40.7006F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -43.5711F, -46.5346F, 18.4695F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -56.4998F, -46.5346F, 14.7756F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -73.1223F, -53.9225F, 7.3878F, 25.8574F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -76.8162F, -61.3103F, 3.6939F, 22.1634F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -69.4284F, -65.0042F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -73.1224F, -68.6981F, 7.3878F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -84.2041F, -53.9225F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -91.5919F, -76.0859F, 11.0817F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -84.2041F, -72.392F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -76.8163F, -68.6981F, 7.3878F, 7.3878F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -95.2858F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -98.9797F, -72.392F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -98.9797F, -76.0859F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -110.0614F, -53.9225F, 3.6939F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -117.4493F, -50.2285F, 3.6939F, 48.0208F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -124.8371F, -53.9225F, 3.6939F, 25.8574F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -121.1431F, -46.5346F, 3.6939F, 44.3269F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -139.6127F, -46.5346F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -143.3066F, -50.2285F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -139.6127F, -53.9225F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -139.6127F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -135.9188F, -53.9225F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -139.6127F, -72.392F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -128.531F, -65.0042F, 3.6939F, 12.9287F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -124.8371F, -72.392F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -124.8371F, -76.0859F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -134.0718F, -79.7798F, 29.6189F, 33.2452F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -115.5383F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -154.3883F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -88.8983F, -72.392F, 0.0F, 37.9394F, 73.8782F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -108.2145F, -79.7798F, 12.9287F, 46.1739F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -100.8267F, -79.7798F, 16.6902F, 42.48F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -121.1432F, -61.3103F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -121.1432F, -57.6164F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -128.5039F, -28.0651F, 3.6939F, 33.2452F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(34.7183F, -117.4222F, -24.3712F, 3.6939F, 22.1635F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -128.531F, -28.0651F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -122.9631F, -13.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -132.2249F, -2.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -135.9188F, -2.2077F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -135.9188F, -5.9016F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -135.9188F, -16.9834F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -139.6127F, -20.6773F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -139.6127F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -143.3066F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -147.0005F, -13.2895F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -150.6944F, -16.9834F, 7.3878F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -154.3883F, -24.3712F, 3.6939F, 3.6939F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -154.3883F, -20.6773F, 3.6939F, 3.6939F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -158.0822F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -154.3883F, -46.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -150.6944F, -46.5346F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -154.3883F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -158.0822F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -150.6944F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -135.9188F, -68.6981F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -132.2249F, -72.392F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -102.6736F, -94.5555F, 5.6085F, 14.7756F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -117.4493F, -83.4737F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -106.3675F, -83.4737F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -91.5919F, -83.4737F, 16.6902F, 33.2452F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -110.0614F, -87.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -98.9797F, -87.1676F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -91.5919F, -94.5555F, 3.6939F, 7.3878F, 12.9287F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -80.5102F, -94.5555F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -98.9797F, -90.8615F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -87.898F, -90.8615F, 7.3878F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -84.2041F, -87.1676F, 3.6939F, 14.7756F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -73.1223F, -87.1676F, 16.6902F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -58.3467F, -72.392F, 22.2311F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -58.3467F, -76.0859F, 9.3024F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-11.4556F, -69.4284F, -90.8615F, 3.6939F, 11.0817F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -73.1224F, -94.5555F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -69.4284F, -90.8615F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-15.1495F, -73.1223F, -98.2494F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -124.8371F, -83.4737F, 5.6085F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -117.4493F, -87.1676F, 5.6085F, 7.3878F, 9.2348F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -87.898F, -98.2494F, 5.6085F, 3.6939F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -84.2041F, -101.9433F, 5.6085F, 7.3878F, 20.3165F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -76.8163F, -90.8615F, 9.3024F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -110.0614F, -90.8615F, 5.6085F, 12.9287F, 16.6226F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -128.531F, -79.7798F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -147.0005F, -57.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -143.3066F, -68.6981F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -147.0005F, -72.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -150.6944F, -72.392F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -154.3883F, -68.6981F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -150.6944F, -76.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -147.0005F, -79.7798F, 18.5372F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -154.3883F, -79.7798F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -158.0823F, -61.3103F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -161.7762F, -53.9225F, 11.1494F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -161.7762F, -35.4529F, 14.8433F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -161.7762F, -16.9834F, 14.8433F, 7.3878F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -165.4701F, -16.9834F, 11.1494F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -147.0005F, -76.0859F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -143.3066F, -83.4737F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -143.3066F, -83.4737F, 11.0817F, 7.3878F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -143.3066F, -83.4737F, 14.8433F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-9.6086F, -143.3066F, -83.4737F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -135.9188F, -83.4737F, 11.0817F, 7.3878F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -132.2249F, -57.6164F, 3.6939F, 11.0817F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -150.6944F, -57.6164F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-2.2208F, -161.7762F, -31.759F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(5.167F, -154.3883F, -42.8407F, 3.6939F, 3.6939F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(1.4731F, -158.0822F, -39.1468F, 3.6939F, 3.6939F, 29.5513F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-5.9147F, -158.0822F, -46.5346F, 7.3878F, 3.6939F, 48.0208F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-20.758F, -158.0822F, -46.5346F, 18.5372F, 3.6939F, 33.2452F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -139.6127F, -28.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -139.6127F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -135.9188F, -46.5346F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -143.3066F, -35.4529F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -143.3066F, -39.1468F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -143.3066F, -42.8407F, 3.6939F, 7.3878F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -147.0005F, -46.5346F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -132.2249F, -31.759F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -135.9188F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -143.3066F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -147.0005F, -31.759F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -147.0005F, -39.1468F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.8609F, -150.6944F, -39.1468F, 3.6939F, 14.7756F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(12.5548F, -150.6944F, -35.4529F, 3.6939F, 3.6939F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -147.0005F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -147.0005F, -20.6773F, 3.6939F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -143.3066F, -20.6773F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(16.2487F, -150.6944F, -28.0651F, 3.6939F, 14.7756F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -124.8371F, -5.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -132.2249F, -20.6773F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -128.531F, -28.0651F, 3.6939F, 20.3165F, 22.1635F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -108.1874F, -13.2895F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -111.8813F, -9.5955F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -98.9527F, -31.759F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -98.9527F, -35.4529F, 3.6939F, 11.0817F, 25.8574F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -110.0344F, -35.4529F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -110.0344F, -39.1468F, 3.6939F, 29.5242F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -102.6466F, -42.8407F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -128.531F, -31.759F, 3.6939F, 29.5513F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -87.8709F, -31.759F, 7.3878F, 7.3878F, 22.1364F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -87.8709F, -24.3712F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -76.7892F, -24.3712F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -65.7075F, -24.3712F, 3.6939F, 14.7486F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -82.3301F, -24.3712F, 3.6939F, 16.6226F, 22.1634F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -84.2041F, -13.2895F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(31.0244F, -84.177F, -20.6773F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -80.4831F, -28.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -80.4831F, -31.7319F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9697F, -76.8163F, -28.0651F, 7.3878F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -76.8162F, -35.4529F, 3.6939F, 48.0208F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -65.7075F, -20.6773F, 11.0817F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -91.5648F, -9.5955F, 3.6939F, 14.7486F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -87.898F, -2.2077F, 3.6939F, 33.1911F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -121.1432F, -5.9016F, 3.6939F, 29.5783F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -95.2587F, -9.5955F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -84.177F, -9.5955F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -62.0136F, -5.9016F, 3.6939F, 31.3712F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(8.5769F, -28.7954F, -24.3712F, 15.0597F, 14.7756F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -25.1015F, -13.2895F, 3.6939F, 18.4695F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -21.4076F, -2.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -28.7954F, -28.0651F, 3.6939F, 3.6939F, 7.3878F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -50.9318F, -28.038F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -50.9318F, -20.6502F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -25.0745F, -16.9563F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -50.9318F, -24.3441F, 3.6939F, 11.0817F, 14.7756F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -39.8501F, -20.6502F, 3.6939F, 7.3878F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -39.8501F, -16.9563F, 3.6939F, 11.0817F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -39.8501F, -13.2624F, 3.6939F, 22.1634F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -47.2379F, -9.5685F, 3.6939F, 36.9391F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -21.4076F, -28.0651F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -74.9423F, -5.8746F, 3.6939F, 16.6226F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -39.8501F, -5.8746F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -36.1833F, -2.2077F, 3.6939F, 18.4695F, 3.6939F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(19.9426F, -50.9589F, -24.3712F, 3.6939F, 22.1634F, 18.4695F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(23.6365F, -65.7075F, -13.2895F, 3.6939F, 3.6939F, 11.0817F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(27.3305F, -84.177F, -5.9016F, 3.6939F, 3.6939F, 3.6939F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-10.0F, -110.0F, 47.0F, 0.0F, 1.5708F, 0.0F)
      );
   }

   private static PartDefinition make_bone146(PartDefinition param0) {
      return param0.addOrReplaceChild(
         "bone146",
         CubeListBuilder.create()
            .texOffs(44, 29)
            .addBox(0.4151F, 88.9684F, -44.3297F, 7.4078F, 44.4468F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 122.3034F, -40.6258F, 7.4078F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 129.7112F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 126.0073F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 122.3034F, -40.6258F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 122.3034F, -40.6258F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 126.0073F, -44.3297F, 14.8156F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 92.6723F, -48.0336F, 3.7039F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 48.2255F, -48.0336F, 18.5195F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 74.1528F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 59.3372F, -51.7375F, 14.8156F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 48.2255F, -51.7375F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 66.745F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 63.0411F, -48.0336F, 14.8156F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 55.6333F, -48.0336F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 51.9294F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 29.706F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 37.1138F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 55.6333F, -51.7375F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 55.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 55.6333F, -51.7375F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 55.6333F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 51.9294F, -55.4414F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 44.5216F, -51.7375F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 11.1865F, -51.7375F, 22.2234F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 0.0748F, -48.0336F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, -3.6291F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, -7.333F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, -7.333F, -44.3297F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, -11.0369F, -40.6258F, 14.8156F, 37.039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 3.7787F, -48.0336F, 18.5195F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 0.0748F, -48.0336F, 7.4078F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, -3.6291F, -48.0336F, 22.2234F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, -3.6291F, -51.7375F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, -7.333F, -48.0336F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, -11.0369F, -44.3297F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, -22.1486F, -29.5142F, 7.4078F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, -25.8525F, -22.1064F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, -25.8525F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, -25.8525F, -14.6986F, 14.8156F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, -29.5564F, -10.9947F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, -29.5564F, -14.6986F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, -29.5564F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, -29.5564F, -7.2908F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, -14.7408F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, -14.7408F, -36.9219F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, -11.0369F, -36.9219F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, -14.7408F, -33.218F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, -18.4447F, -29.5142F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, -22.1486F, -25.8102F, 29.6312F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, -22.1486F, -22.1064F, 29.6312F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, -18.4447F, -29.5142F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 0.0748F, -48.0336F, 3.7039F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 11.1865F, -48.0336F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 7.4826F, -51.7375F, 25.9273F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 11.1865F, -51.7375F, 25.9273F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 44.5216F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 22.2982F, -59.1453F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 29.706F, -55.4414F, 22.2234F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 14.8904F, -55.4414F, 18.5195F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 11.1865F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 7.4826F, -55.4414F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 26.0021F, -55.4414F, 14.8156F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 40.8177F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 26.0021F, -55.4414F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 44.5216F, -51.7375F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 29.706F, -51.7375F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 26.0021F, -51.7375F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 14.8904F, -55.4414F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 88.9684F, -48.0336F, 3.7039F, 33.3351F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 88.9684F, -48.0336F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 88.9684F, -48.0336F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 88.9684F, -48.0336F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 88.9684F, -51.7375F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 88.9684F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 114.8956F, -48.0336F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 118.5995F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 111.1917F, -44.3297F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 100.0801F, -44.3297F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(15.2307F, 100.0801F, -40.6258F, 22.2234F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 103.784F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 96.3762F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 38.6148F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, -8.0052F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, -14.6652F, -1.7349F, 74.078F, 46.6496F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(-0.6949F, 85.2645F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 68.5969F, -1.7349F, 40.7429F, 27.7792F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(61.5294F, 26.0021F, -1.7349F, 27.7792F, 37.039F, 1.852F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 85.2645F, -40.6258F, 11.1117F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 92.6723F, -40.6258F, 3.7039F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 96.3762F, -29.5142F, 3.7039F, 3.7039F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 96.3762F, -22.1064F, 3.7039F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 107.4878F, -29.5142F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 96.3762F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 92.6723F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 88.9684F, -40.6258F, 7.4078F, 3.7039F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 81.5606F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 74.1528F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 66.745F, -40.6258F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 87.1164F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 37.1138F, -48.0336F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 37.1138F, -51.7375F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 74.1528F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 77.8567F, -36.9219F, 7.4078F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 81.5606F, -33.218F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 85.2645F, -29.5142F, 3.7039F, 11.1117F, 29.6312F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 74.1528F, -25.8102F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 55.6333F, -25.8102F, 3.7039F, 25.9273F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 74.1528F, -22.1064F, 7.4078F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 85.2645F, -22.1064F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 74.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 74.1528F, -18.4025F, 3.7039F, 14.8156F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 74.1528F, -22.1064F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 51.9294F, -22.1064F, 7.4078F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 74.1528F, -10.9947F, 3.7039F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.4931F, 63.0411F, -18.4025F, 11.1117F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 48.2255F, -14.6986F, 11.1117F, 14.8156F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 55.6333F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 44.5216F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 44.5216F, -7.2908F, 3.7039F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6048F, 37.1138F, -10.9947F, 3.7039F, 14.8156F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(81.9009F, 26.0021F, -10.9947F, 3.7039F, 18.5195F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 18.5943F, -10.9947F, 3.7039F, 22.2234F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 37.1138F, -14.6986F, 7.4078F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6048F, 66.745F, -14.6986F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6048F, 63.0411F, -10.9947F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 63.0411F, -10.9947F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(93.0125F, 63.0411F, -10.9947F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 44.5216F, -18.4025F, 3.7039F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 88.9684F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 85.2645F, -18.4025F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 88.9684F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 85.2645F, -14.6986F, 3.7039F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 77.8567F, -25.8102F, 3.7039F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 85.2645F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 70.4489F, -40.6258F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 66.745F, -36.9219F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 63.0411F, -36.9219F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 51.9294F, -40.6258F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 55.6333F, -36.9219F, 11.1117F, 3.7039F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 59.3372F, -25.8102F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 66.745F, -29.5142F, 11.1117F, 3.7039F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 63.0411F, -40.6258F, 11.1117F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 51.9294F, -40.6258F, 14.8156F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 77.8567F, -29.5142F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 81.5606F, -36.9219F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 92.6723F, -44.3297F, 22.2234F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 88.9684F, -44.3297F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 85.2644F, -44.3297F, 40.7429F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 66.745F, -44.3297F, 3.7039F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 59.3372F, -44.3297F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 51.9294F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 48.2255F, -44.3297F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, 40.8177F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 33.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 26.0021F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 22.2982F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 14.8904F, -40.6258F, 11.1117F, 18.5195F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, 18.5943F, -40.6258F, 11.1117F, 22.2234F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 22.2982F, -40.6258F, 11.1117F, 29.6312F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 18.5943F, -40.6258F, 3.7039F, 33.3351F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 37.1138F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 40.8177F, -36.9219F, 11.1117F, 3.7039F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 33.4099F, -33.218F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 26.0021F, -25.8102F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 14.8904F, -25.8102F, 11.1117F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 14.8904F, -29.5142F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 11.1865F, -33.218F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 11.1865F, -29.5142F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 11.1865F, -36.9219F, 18.5195F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 7.4826F, -36.9219F, 7.4078F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 7.4826F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 3.7787F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 0.0748F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, -3.6291F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 0.0748F, -36.9219F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 3.7787F, -40.6258F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, -3.6291F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, 3.7787F, -36.9219F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 3.7787F, -33.218F, 14.8156F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, 14.8904F, -33.218F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(48.5658F, 14.8904F, -36.9219F, 11.1117F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 29.706F, -36.9219F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 26.0021F, -33.218F, 11.1117F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 22.2982F, -36.9219F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 22.2982F, -33.218F, 7.4078F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 22.2982F, -29.5142F, 3.7039F, 7.4078F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 29.706F, -36.9219F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 33.4099F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, 29.706F, -40.6258F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, 22.2982F, -36.9219F, 11.1117F, 29.6312F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(63.3814F, 44.5216F, -33.218F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(67.0853F, 44.5216F, -29.5142F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 7.4826F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, -3.6291F, -44.3297F, 7.4078F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, -3.6291F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, -7.333F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, -7.333F, -44.3297F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, -11.0369F, -40.6258F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, -14.7408F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, -11.0369F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, -14.7408F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, -7.333F, -36.9219F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, -11.0369F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, -14.7408F, -29.5142F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(37.4541F, -18.4447F, -25.8102F, 7.4078F, 11.1117F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, -14.7408F, -25.8102F, 11.1117F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, -18.4447F, -22.1064F, 11.1117F, 7.4078F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, -22.1486F, -14.6986F, 11.1117F, 7.4078F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(52.2697F, -18.4447F, -18.4025F, 11.1117F, 7.4078F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(44.8619F, -11.0369F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, -7.333F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 0.0748F, -29.5142F, 11.1117F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 3.7787F, -14.6986F, 11.1117F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(59.6775F, -3.6291F, -29.5142F, 11.1117F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, -11.0369F, -25.8102F, 14.8156F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(55.9736F, -14.7408F, -22.1064F, 14.8156F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, -14.7408F, -18.4025F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, -11.0369F, -22.1064F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, -7.333F, -25.8102F, 3.7039F, 11.1117F, 18.5195F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, -14.7408F, -14.6986F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, -11.0369F, -18.4025F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, -7.333F, -22.1064F, 3.7039F, 11.1117F, 14.8156F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, -3.6291F, -25.8102F, 7.4078F, 7.4078F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(74.493F, 11.1865F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(81.9009F, 26.0021F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(85.6048F, 33.4099F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(89.3086F, 40.8177F, -7.2908F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(78.197F, 18.5943F, -7.2908F, 7.4078F, 7.4078F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(70.7892F, 3.7787F, -25.8102F, 7.4078F, 33.3351F, 25.9273F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(41.158F, -7.333F, -33.218F, 7.4078F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(33.7502F, 33.4099F, -44.3297F, 18.5195F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 70.4489F, -44.3297F, 44.4468F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 59.3372F, -44.3297F, 14.8156F, 14.8156F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 100.0801F, -33.218F, 22.2234F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 103.784F, -40.6258F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 107.4878F, -40.6258F, 22.2234F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 107.4878F, -36.9219F, 25.9273F, 11.1117F, 22.2234F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 107.4878F, -33.218F, 18.5195F, 11.1117F, 33.3351F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(7.8229F, 111.1917F, -36.9219F, 22.2234F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(30.0463F, 107.4878F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(26.3424F, 111.1917F, -25.8102F, 3.7039F, 11.1117F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(4.119F, 111.1917F, -33.218F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(18.9346F, 96.3762F, -44.3297F, 22.2234F, 11.1117F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(22.6385F, 96.3761F, -44.3297F, 22.2234F, 7.4078F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 111.1917F, -44.3297F, 18.5195F, 14.8156F, 11.1117F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(11.5268F, 129.7112F, -33.218F, 3.7039F, 3.7039F, 3.7039F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 133.4151F, -40.6258F, 3.7039F, 3.7039F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 122.3034F, -36.9219F, 3.7039F, 11.1117F, 7.4078F, new CubeDeformation(0.0F))
            .texOffs(44, 29)
            .addBox(0.4151F, 122.3034F, -29.5142F, 7.4078F, 3.7039F, 11.1117F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-20.3056F, -136.5242F, 0.6997F, 0.0F, -1.5708F, 0.0F)
      );
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.hugeassback.render(poseStack, buffer, packedLight, packedOverlay);
   }
}
