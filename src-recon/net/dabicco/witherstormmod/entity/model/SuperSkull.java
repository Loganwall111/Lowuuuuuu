package net.dabicco.witherstormmod.entity.model;

import net.dabicco.witherstormmod.entity.state.SuperSkullRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class SuperSkull extends EntityModel<SuperSkullRenderState> {
   private final ModelPart wsSkull;
   private final ModelPart fire;

   public SuperSkull(ModelPart root) {
      super(root);
      this.wsSkull = root.getChild("ws_skull");
      this.fire = this.wsSkull.getChild("fire");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ws_skull = partdefinition.addOrReplaceChild(
         "ws_skull",
         CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 16.0F, 0.0F)
      );
      PartDefinition fire = ws_skull.addOrReplaceChild("fire", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      fire.addOrReplaceChild(
         "fire_r1",
         CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -16.0F, 8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.0F, 0.0F, -3.1416F)
      );
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void setupAnim(SuperSkullRenderState state) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      this.fire.visible = !state.extinguished;
      this.fire.zRot = Mth.sin(state.ageInTicks * 0.9F) * 0.12F;
   }
}
