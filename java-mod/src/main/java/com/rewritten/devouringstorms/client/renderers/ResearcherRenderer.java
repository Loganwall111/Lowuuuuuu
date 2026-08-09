package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.ResearcherEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** E.P.A. RESEARCHER — lab coat, safety glasses, of-course-you're-welcome face. */
public class ResearcherRenderer extends DsEntityRenderer<ResearcherEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("researcher"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/researcher.png");

    public ResearcherRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.95f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("coat",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -2f, -2.5f, 8f, 12f, 5f),
            PartPose.offset(0f, 10f, 0f));
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -3.5f, -3.5f, 7f, 7f, 7f),
            PartPose.offset(0f, 6.5f, 0f));
        root.addOrReplaceChild("glasses",
            CubeListBuilder.create().texOffs(32, 18).addBox(-3.5f, -0.5f, -0.5f, 7f, 1f, 1f),
            PartPose.offset(0f, 6f, -4f));
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(40, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(-5.5f, 0f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(40, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(5.5f, 0f, 0f));
        root.addOrReplaceChild("clipboard",
            CubeListBuilder.create().texOffs(52, 30).addBox(-1.5f, -1f, -0.5f, 3f, 4f, 1f),
            PartPose.offset(5.5f, 6f, -3f));
        root.addOrReplaceChild("leg_l",
            CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(-2f, 12f, 0f));
        root.addOrReplaceChild("leg_r",
            CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(2f, 12f, 0f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        float swing = (float) Math.sin(age * 0.16f) * 0.5f;
        withChild("leg_l", part -> part.xRot = -swing * 0.6f);
        withChild("leg_r", part -> part.xRot = swing * 0.6f);
        withChild("arm_l", part -> part.xRot = (float) Math.sin(age * 0.05f) * 0.12f);
        withChild("arm_r", part -> part.xRot = -0.85f); // clipboard up, perpetually
        withChild("clipboard", part -> part.xRot = -0.85f);
        withChild("head", part -> part.yRot = (float) Math.sin(age * 0.028f) * 0.45f);
        withChild("glasses", part -> part.yRot = (float) Math.sin(age * 0.028f) * 0.45f);
    }

    @Override
    protected String emissiveChild() {
        return "head"; // memo-light
    }
}
