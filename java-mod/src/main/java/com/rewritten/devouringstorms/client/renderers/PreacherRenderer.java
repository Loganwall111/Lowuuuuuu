package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.PreacherEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** THE PREACHER — hooded violet robes, hands always folded toward the spire. */
public class PreacherRenderer extends DsEntityRenderer<PreacherEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("preacher"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/preacher.png");

    public PreacherRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 1.0f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("robe",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5f, -2f, -3f, 10f, 14f, 6f),
            PartPose.offset(0f, 10f, 0f));
        root.addOrReplaceChild("hood",
            CubeListBuilder.create().texOffs(0, 20).addBox(-4f, -4f, -4f, 8f, 8f, 8f),
            PartPose.offset(0f, 7f, 0f));
        root.addOrReplaceChild("hem",
            CubeListBuilder.create().texOffs(32, 32).addBox(-6f, -2f, -4f, 12f, 4f, 8f),
            PartPose.offset(0f, 12f, 0f));
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(48, 0).addBox(-1.5f, 0f, -1.5f, 3f, 10f, 3f),
            PartPose.offset(-6.5f, -1f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(48, 0).addBox(-1.5f, 0f, -1.5f, 3f, 10f, 3f),
            PartPose.offset(6.5f, -1f, 0f));
        // folded hands toward the spire
        root.addOrReplaceChild("hands",
            CubeListBuilder.create().texOffs(48, 16).addBox(-3f, 0f, -2f, 6f, 3f, 4f),
            PartPose.offset(0f, 8f, -4f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        withChild("arm_l", part -> part.xRot = (float) Math.sin(age * 0.03f) * 0.08f - 0.5f);
        withChild("arm_r", part -> part.xRot = (float) Math.cos(age * 0.027f) * 0.08f - 0.5f);
        withChild("hood", part -> part.xRot = (float) Math.sin(age * 0.05f) * 0.06f);
        withChild("robe", part -> part.yRot = (float) Math.sin(age * 0.02f) * 0.05f);
    }
}
