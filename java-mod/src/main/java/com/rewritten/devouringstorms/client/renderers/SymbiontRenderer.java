package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.WitheredSymbiontEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.TexturedModelData;
import net.minecraft.client.model.geom.builders.TextureDimensions;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * WITHERED SYMBIONT. A body the storm gave back, wrong-armed, wrong-walking.
 * Its veins are the one part of it that still carries light.
 */
public class SymbiontRenderer extends DsEntityRenderer<WitheredSymbiontEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("symbiont"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/symbiont.png");

    public SymbiontRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 1.05f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -8f, -4f, 8f, 8f, 8f), PartPose.offset(0f, 6f, 0f));
        root.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 16).addBox(-4f, 8f, -2f, 8f, 12f, 4f), PartPose.ZERO);
        root.addOrReplaceChild("arm_left",
            CubeListBuilder.create().texOffs(32, 16).addBox(-1.5f, 0f, -1.5f, 3f, 13f, 3f),
            PartPose.offsetAndRotation(-5.5f, 8f, 0f, -1.1f, 0f, -0.15f));
        root.addOrReplaceChild("arm_right",
            CubeListBuilder.create().texOffs(32, 16).addBox(-1.5f, 0f, -1.5f, 3f, 13f, 3f),
            PartPose.offsetAndRotation(5.5f, 8f, 0f, -1.0f, 0f, 0.2f));
        root.addOrReplaceChild("leg_left",
            CubeListBuilder.create().texOffs(48, 16).addBox(-1.75f, 0f, -1.75f, 3.5f, 14f, 3.5f), PartPose.offset(-2f, 20f, 0f));
        root.addOrReplaceChild("leg_right",
            CubeListBuilder.create().texOffs(48, 16).addBox(-1.75f, 0f, -1.75f, 3.5f, 14f, 3.5f), PartPose.offset(2f, 20f, 0f));
        return TexturedModelData.createDefault(mesh, new TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float swing = (float) Math.sin(state.ageTicks * 0.24f) * 0.5f;
        withChild("leg_left", part -> part.xRot = swing);
        withChild("leg_right", part -> part.xRot = -swing);
        withChild("head", part -> part.zRot = (float) Math.sin(state.ageTicks * 0.09f) * 0.12f);
    }

    @Override
    protected int tint(DsRenderState state) {
        return 0xFF << 24 | 0xC8 << 16 | 0xC8 << 8 | 0xD8;
    }
}
