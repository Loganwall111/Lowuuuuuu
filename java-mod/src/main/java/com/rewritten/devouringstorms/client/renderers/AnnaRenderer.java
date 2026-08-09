package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.AnnaApparitionEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.TexturedModelData;
import net.minecraft.client.model.geom.builders.TextureDimensions;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * ANNA. Pale, still, and never quite fully rendered — her opacity stutters like a bad signal.
 * "Anna isn't real."
 */
public class AnnaRenderer extends DsEntityRenderer<AnnaApparitionEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("anna"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/anna.png");

    public AnnaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.95f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -7f, -3.5f, 7f, 7f, 7f), PartPose.offset(0f, 7f, 0f));
        root.addOrReplaceChild("dress",
            CubeListBuilder.create().texOffs(0, 14).addBox(-4f, 10f, -2f, 8f, 16f, 4f), PartPose.ZERO);
        root.addOrReplaceChild("arm_left",
            CubeListBuilder.create().texOffs(28, 14).addBox(-1f, 0f, -1f, 2f, 12f, 2f), PartPose.offset(-4.5f, 10f, 0f));
        root.addOrReplaceChild("arm_right",
            CubeListBuilder.create().texOffs(28, 14).addBox(-1f, 0f, -1f, 2f, 12f, 2f), PartPose.offset(4.5f, 10f, 0f));
        return TexturedModelData.createDefault(mesh, new TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        // glitch flicker: stuttering transparency, occasionally hard-cutting
        float base = 0.55f + 0.35f * (float) Math.max(0.0, Math.sin(state.ageTicks * 0.31f));
        float cut = ((state.ageTicks * 7) % 23) < 3 ? 0.25f : 1.0f;
        state.opacity = base * cut;
        withChild("head", part -> part.yRot = (float) Math.sin(state.ageTicks * 0.11f) * 0.2f);
    }

    @Override
    protected int tint(DsRenderState state) {
        int alpha = (int) (state.opacity * 255.0f) & 0xFF;
        // desaturated pale lilac
        return (alpha << 24) | 0xD8 << 16 | 0xD0 << 8 | 0xE8;
    }
}
