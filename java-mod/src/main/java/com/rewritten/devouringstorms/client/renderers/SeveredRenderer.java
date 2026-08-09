package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.SeveredStormEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.TexturedModelData;
import net.minecraft.client.model.geom.builders.TextureDimensions;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * SEVERED STORM. A ragged knot of storm-matter with three broken spines — MASSG in miniature,
 * an infant god of bad weather.
 */
public class SeveredRenderer extends DsEntityRenderer<SeveredStormEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("severed"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/massg.png"); // storm flesh is storm flesh

    public SeveredRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.7f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    @Override
    protected void fillExtra(com.rewritten.devouringstorms.entity.SeveredStormEntity entity,
                             DsRenderState state, float partialTick) {
        state.variant = entity.getVariant().ordinal();
    }

    public static TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("knot",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5f, 0f, -5f, 10f, 10f, 10f), PartPose.ZERO);
        root.addOrReplaceChild("spine_0",
            CubeListBuilder.create().texOffs(0, 80).addBox(-1f, 0f, -1f, 2f, 8f, 2f), PartPose.offsetAndRotation(-4f, 8f, -4f, 0.4f, 0f, 0.4f));
        root.addOrReplaceChild("spine_1",
            CubeListBuilder.create().texOffs(0, 80).addBox(-1f, 0f, -1f, 2f, 9f, 2f), PartPose.offsetAndRotation(4f, 9f, -3f, -0.3f, 0f, -0.5f));
        root.addOrReplaceChild("spine_2",
            CubeListBuilder.create().texOffs(0, 80).addBox(-1f, 0f, -1f, 2f, 7f, 2f), PartPose.offsetAndRotation(0f, 10f, 4f, 0.5f, 0f, -0.2f));
        return TexturedModelData.createDefault(mesh, new TextureDimensions(128, 128));
    }

    @Override
    protected void animate(DsRenderState state) {
        // tumbling slowly, spines twitching on their own
        float age = state.ageTicks;
        withChild("knot", part -> part.yRot = age * 0.15f);
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            withChild("spine_" + i, part -> part.xRot += (float) Math.sin(age * 0.3f + idx * 2.1f) * 0.35f);
        }
    }

    @Override
    protected int tint(DsRenderState state) {
        int raw0 = tintRaw(state);
        var v = com.rewritten.devouringstorms.entity.MassgVariant.byIdTurn(state.variant);
        if (v == com.rewritten.devouringstorms.entity.MassgVariant.CLASSIC) return raw0;
        int a = (raw0 >>> 24) & 0xFF;
        int rr = (int) (((raw0 >> 16) & 0xFF) * v.r) & 0xFF;
        int gg = (int) (((raw0 >> 8) & 0xFF) * v.g) & 0xFF;
        int bb = (int) ((raw0 & 0xFF) * v.b) & 0xFF;
        return (a << 24) | (rr << 16) | (gg << 8) | bb;
    }

    protected int tintRaw(DsRenderState state) {
        return 0xFF << 24 | 0xE6 << 16 | 0xC0 << 8 | 0xFF; // bruised lilac
    }
}
