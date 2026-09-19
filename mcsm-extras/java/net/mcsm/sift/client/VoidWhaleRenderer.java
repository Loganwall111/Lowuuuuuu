package net.mcsm.sift.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mcsm.sift.entity.VoidWhaleEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Ghost Whale Renderer - colossal multi-segmented, Pixar-VFX - Fabric version
 */
public class VoidWhaleRenderer extends MobRenderer<VoidWhaleEntity, VoidWhaleRenderer.WhaleModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath("mcsm", "void_whale"), "main");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("mcsm", "textures/entity/void_whale/void_whale.png");
    private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath("mcsm", "textures/entity/void_whale/void_whale_glow.png");

    public VoidWhaleRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new WhaleModel(ctx.bakeLayer(LAYER)), 3.0f);
    }

    @Override
    public Identifier getTextureLocation(VoidWhaleEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(VoidWhaleEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float glow = entity.getGlowIntensity();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.pushPose();
        poseStack.popPose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", 
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8, -4, -12, 16, 8, 24),
            net.minecraft.client.model.geom.PartPose.ZERO);

        for (int i = 0; i < 12; i++) {
            float size = 6 - (float)i * 0.4f;
            float yOffset = (i % 2 == 0) ? 0 : 0.5f;
            root.addOrReplaceChild("segment_" + i,
                CubeListBuilder.create()
                    .texOffs(0, 32 + i*8).addBox(-size, -size/2 + yOffset, i*8, size*2, size, 8),
                net.minecraft.client.model.geom.PartPose.ZERO);
        }

        root.addOrReplaceChild("left_fin",
            CubeListBuilder.create().texOffs(56, 0).addBox(6, -1, -2, 10, 2, 8),
            net.minecraft.client.model.geom.PartPose.offset(0, 0, 0));
        root.addOrReplaceChild("right_fin",
            CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-16, -1, -2, 10, 2, 8),
            net.minecraft.client.model.geom.PartPose.ZERO);

        root.addOrReplaceChild("tail",
            CubeListBuilder.create().texOffs(0, 56).addBox(-8, -2, 96, 16, 4, 12),
            net.minecraft.client.model.geom.PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    public static class WhaleModel extends EntityModel<VoidWhaleEntity> {
        private final ModelPart root;
        private final ModelPart[] segments = new ModelPart[12];

        public WhaleModel(ModelPart root) {
            this.root = root;
            for (int i = 0; i < 12; i++) {
                if (root.hasChild("segment_" + i)) {
                    segments[i] = root.getChild("segment_" + i);
                }
            }
        }

        @Override
        public void setupAnim(VoidWhaleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            float swim = entity.getSwimAnimation(0);
            for (int i = 0; i < segments.length; i++) {
                if (segments[i] != null) {
                    float wave = Mth.sin(swim + i * 0.5f) * 0.2f;
                    segments[i].yRot = wave;
                    segments[i].xRot = Mth.sin(swim * 0.7f + i * 0.3f) * 0.1f;
                }
            }
            ModelPart head = root.getChild("head");
            if (head != null) {
                head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.5f;
                head.xRot = headPitch * Mth.DEG_TO_RAD * 0.5f;
            }
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
            root.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
        }
    }
}
