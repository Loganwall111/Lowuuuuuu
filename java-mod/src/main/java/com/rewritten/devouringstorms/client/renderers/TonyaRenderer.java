package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.TonyaEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** TONYA? — the echo remaining. No legs; the fields finish her sentences. */
public class TonyaRenderer extends DsEntityRenderer<TonyaEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("tonya"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/tonya.png");

    public TonyaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.9f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    @Override
    public void extractRenderState(TonyaEntity entity, DsRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.opacity = 0.55f + (float) Math.sin(entity.tickCount * 0.05f) * 0.12f;
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("dress",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -2f, -2f, 7f, 12f, 4f),
            PartPose.offset(0f, 10f, 0f));
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 17).addBox(-3f, -3.5f, -3f, 6f, 6f, 6f),
            PartPose.offset(0f, 6.5f, 0f));
        // hair into wind that isn't there
        root.addOrReplaceChild("hair",
            CubeListBuilder.create().texOffs(25, 17).addBox(-3.5f, -4.5f, -3.5f, 7f, 2f, 7f),
            PartPose.offset(0f, 6.5f, 0f));
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(40, 28).addBox(-1.5f, 0f, -1.5f, 3f, 8f, 3f),
            PartPose.offset(-5f, 0f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(40, 28).addBox(-1.5f, 0f, -1.5f, 3f, 8f, 3f),
            PartPose.offset(5f, 0f, 0f));
        // echo shimmer: two faint wisps trailing where legs would be
        root.addOrReplaceChild("wisp_l",
            CubeListBuilder.create().texOffs(0, 30).addBox(-1f, 0f, -1f, 2f, 6f, 2f),
            PartPose.offset(-2f, 14f, 0f));
        root.addOrReplaceChild("wisp_r",
            CubeListBuilder.create().texOffs(0, 30).addBox(-1f, 0f, -1f, 2f, 6f, 2f),
            PartPose.offset(2f, 14f, 0f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        // she hovers a half-beat behind her own shadow
        float bob = (float) Math.sin(state.ageTicks * 0.06f) * 0.12f;
        withChild("dress", part -> part.y = 10f + bob);
        withChild("head", part -> {
            part.y = 6.5f + bob;
            part.yRot = (float) Math.sin(state.ageTicks * 0.018f) * 0.5f;
        });
        withChild("hair", part -> part.yRot = (float) Math.sin(state.ageTicks * 0.018f) * 0.5f);
        float drift = (float) Math.sin(state.ageTicks * 0.04f) * 0.2f;
        withChild("arm_l", part -> part.xRot = -0.15f + drift);
        withChild("arm_r", part -> part.xRot = -0.15f - drift);
        withChild("wisp_l", part -> {
            part.y = 14f + (float) Math.sin(state.ageTicks * 0.09f) * 1.5f;
            part.xRot = drift * 1.5f;
        });
        withChild("wisp_r", part -> {
            part.y = 14f + (float) Math.cos(state.ageTicks * 0.083f) * 1.5f;
            part.xRot = -drift * 1.5f;
        });
    }

    @Override
    protected String emissiveChild() {
        return "head";
    }
}
