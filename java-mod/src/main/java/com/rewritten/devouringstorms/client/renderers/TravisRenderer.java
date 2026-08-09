package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.TravisEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** TRAVIS — the one who stayed behind at the tear. Navy field coat, worn recorder. */
public class TravisRenderer extends DsEntityRenderer<TravisEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("travis"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/travis.png");

    public TravisRenderer(EntityRendererProvider.Context ctx) {
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
        root.addOrReplaceChild("hair",
            CubeListBuilder.create().texOffs(28, 18).addBox(-3.5f, -4.5f, -3.5f, 7f, 1.5f, 7f),
            PartPose.offset(0f, 6.5f, 0f));
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(40, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(-5.5f, 0f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(40, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(5.5f, 0f, 0f));
        root.addOrReplaceChild("leg_l",
            CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(-2f, 15f, 0f));
        root.addOrReplaceChild("leg_r",
            CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(2f, 15f, 0f));
        // the field recorder he never lets go of
        root.addOrReplaceChild("recorder",
            CubeListBuilder.create().texOffs(52, 30).addBox(-1f, -0.5f, -1f, 2f, 2f, 2f),
            PartPose.offset(-5.5f, 7.5f, -2.5f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float a = state.ageTicks;
        float swing = (float) Math.sin(a * 0.16f) * 0.5f;
        withChild("leg_l", part -> part.xRot = -swing);
        withChild("leg_r", part -> part.xRot = swing);
        withChild("arm_r", part -> part.xRot = -swing * 0.6f);
        // left arm holds the recorder up, ready to capture proof
        withChild("arm_l", part -> part.xRot = -0.9f + (float) Math.sin(state.ageTicks * 0.03f) * 0.05f);
        withChild("recorder", part -> part.xRot = -0.9f);
        // nervous scan of the horizon. The wind lies here.
        withChild("head", part -> part.yRot = (float) Math.sin(state.ageTicks * 0.021f) * 0.65f);
        withChild("hair", part -> part.yRot = (float) Math.sin(state.ageTicks * 0.021f) * 0.65f);
    }
}
