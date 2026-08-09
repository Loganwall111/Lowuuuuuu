package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.TownsfolkEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** ENDERTONIAN — plain townsfolk: worn cloak, tired eyes, still standing. */
public class TownsfolkRenderer extends DsEntityRenderer<TownsfolkEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("townsfolk"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/townsfolk.png");

    public TownsfolkRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.95f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -2f, -2.5f, 8f, 12f, 5f),
            PartPose.offset(0f, 10f, 0f));
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 17).addBox(-3.5f, -3.5f, -3.5f, 7f, 7f, 7f),
            PartPose.offset(0f, 6.5f, 0f));
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(28, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(-5.5f, 0f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(28, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(5.5f, 0f, 0f));
        // the broom that never finishes the plaza
        root.addOrReplaceChild("broom",
            CubeListBuilder.create().texOffs(44, 30).addBox(-1f, 0f, -1f, 2f, 12f, 2f),
            PartPose.offset(7.5f, 4f, -2f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        // sweeping, forever — the plaza is the job now
        float sweep = (float) Math.sin(age * 0.07f) * 0.35f;
        withChild("arm_r", part -> part.zRot = sweep - 0.45f);
        withChild("arm_l", part -> part.xRot = (float) Math.sin(age * 0.05f) * 0.1f);
        withChild("broom", part -> part.zRot = sweep - 0.2f);
        withChild("head", part -> part.yRot = (float) Math.sin(age * 0.03f) * 0.3f);
    }
}
