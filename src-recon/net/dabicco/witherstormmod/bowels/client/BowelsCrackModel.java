package net.dabicco.witherstormmod.bowels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public final class BowelsCrackModel {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "bowels_crack"), "main");
   private static final int STAGES = 10;
   private final ModelPart[] faces = new ModelPart[6];

   public BowelsCrackModel(ModelPart root) {
      for (int i = 0; i < 6; i++) {
         this.faces[i] = root.getChild("face" + i);
      }
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      float out = 8.06F;
      float[][] pose = new float[][]{
         {0.0F, 0.0F, -out, 0.0F, 0.0F, 0.0F},
         {0.0F, 0.0F, out, 0.0F, (float) Math.PI, 0.0F},
         {out, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F},
         {-out, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 2), 0.0F},
         {0.0F, -out, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F},
         {0.0F, out, 0.0F, (float) (-Math.PI / 2), 0.0F, 0.0F}
      };

      for (int i = 0; i < 6; i++) {
         float[] p = pose[i];
         root.addOrReplaceChild(
            "face" + i,
            CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(p[0], p[1], p[2], p[3], p[4], p[5])
         );
      }

      return LayerDefinition.create(mesh, 16, 16);
   }

   public void submit(int cracks, int hits, PoseStack poseStack, SubmitNodeCollector collector, int light) {
      if (cracks > 0) {
         int stage = Math.min(9, cracks * 10 / Math.max(1, hits));
         Identifier texture = Identifier.withDefaultNamespace("textures/block/destroy_stage_" + stage + ".png");

         for (ModelPart face : this.faces) {
            collector.submitModelPart(face, poseStack, RenderTypes.crumbling(texture), light, OverlayTexture.NO_OVERLAY, (TextureAtlasSprite)null);
         }
      }
   }
}
