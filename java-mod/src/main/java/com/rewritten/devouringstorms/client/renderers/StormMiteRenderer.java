package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.StormMiteEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** FRAYSPAWN — storm mite: skittering globule of shed storm-tissue with feelers. */
public class StormMiteRenderer extends DsEntityRenderer<StormMiteEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("storm_mite"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/storm_mite.png");

    public StormMiteRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.5f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, -5f, -3f, 5f, 5f, 6f),
            PartPose.offset(0f, 22f, 0f));
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 11).addBox(-1.5f, -4f, -5.5f, 3f, 3f, 3f),
            PartPose.offset(0f, 22f, 0f));
        // feelers: opinionated
        root.addOrReplaceChild("feel_l",
            CubeListBuilder.create().texOffs(20, 20).addBox(-0.5f, -6.5f, -5.5f, 1f, 3f, 1f),
            PartPose.offset(-0.7f, 22f, 0f));
        root.addOrReplaceChild("feel_r",
            CubeListBuilder.create().texOffs(20, 20).addBox(-0.5f, -6.5f, -5.5f, 1f, 3f, 1f),
            PartPose.offset(0.7f, 22f, 0f));
        for (int i = 0; i < 3; i++) {
            root.addOrReplaceChild("leg_l" + i,
                CubeListBuilder.create().texOffs(0, 20).addBox(-2f, 0f, -0.5f, 2f, 4f, 1f),
                PartPose.offset(-2.5f, 20f, -2f + i * 2f));
            root.addOrReplaceChild("leg_r" + i,
                CubeListBuilder.create().texOffs(0, 20).addBox(0f, 0f, -0.5f, 2f, 4f, 1f),
                PartPose.offset(2.5f, 20f, -2f + i * 2f));
        }
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float a = state.ageTicks;
        for (int i = 0; i < 3; i++) {
            float ph = a * 0.9f + i * 1.2f;
            float sw = (float) Math.sin(ph) * 0.55f;
            int j = i;
            withChild("leg_l" + j, part -> part.xRot = sw);
            withChild("leg_r" + j, part -> part.xRot = -sw);
        }
        float wiggle = (float) Math.sin(state.ageTicks * 0.35f) * 0.3f;
        withChild("feel_l", part -> part.xRot = wiggle);
        withChild("feel_r", part -> part.xRot = -wiggle);
        withChild("head", part -> part.xRot = (float) Math.sin(state.ageTicks * 0.1f) * 0.1f);
    }
}
