package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.TazoEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.TexturedModelData;
import net.minecraft.client.model.geom.builders.TextureDimensions;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * TAZO. A survivor in a storm-torn cloak. Walks like a person — because it is one.
 */
public class TazoRenderer extends DsEntityRenderer<TazoEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("tazo"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/tazo.png");

    public TazoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 1.0f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    @Override
    protected void fillExtra(com.rewritten.devouringstorms.entity.TazoEntity entity,
                             DsRenderState state, float partialTick) {
        state.opacity = 1.0f;
        state.variant = switch (entity.getVariantName()) {
            case "rose" -> 1;
            case "dusk" -> 2;
            case "ivory" -> 3;
            default -> 0;
        };
    }

    @Override
    protected int tint(DsRenderState state) {
        int raw = super.tint(state);
        float[] mult = switch (state.variant) {
            case 1 -> new float[] {1.0f, 0.5f, 0.8f};    // rose tazo of the pink variant storms
            case 2 -> new float[] {0.55f, 0.5f, 0.95f};  // dusk tazo — the one that sleeps twice
            case 3 -> new float[] {0.95f, 0.9f, 0.8f};   // ivory tazo. you cannot afford them.
            default -> new float[] {1f, 1f, 1f};
        };
        int a = (raw >>> 24) & 0xFF;
        int rr = (int) (((raw >> 16) & 0xFF) * mult[0]) & 0xFF;
        int gg = (int) (((raw >> 8) & 0xFF) * mult[1]) & 0xFF;
        int bb = (int) ((raw & 0xFF) * mult[2]) & 0xFF;
        return (a << 24) | (rr << 16) | (gg << 8) | bb;
    }

    public static TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -8f, -4f, 8f, 8f, 8f), PartPose.offset(0f, 6f, 0f));
        root.addOrReplaceChild("cloak_torso",
            CubeListBuilder.create().texOffs(0, 16).addBox(-4.5f, 8f, -2.5f, 9f, 14f, 5f), PartPose.ZERO);
        root.addOrReplaceChild("leg_left",
            CubeListBuilder.create().texOffs(32, 16).addBox(-1.75f, 0f, -1.75f, 3.5f, 12f, 3.5f), PartPose.offset(-2f, 22f, 0f));
        root.addOrReplaceChild("leg_right",
            CubeListBuilder.create().texOffs(32, 16).addBox(-1.75f, 0f, -1.75f, 3.5f, 12f, 3.5f), PartPose.offset(2f, 22f, 0f));
        root.addOrReplaceChild("arm_left",
            CubeListBuilder.create().texOffs(48, 16).addBox(-1.25f, 0f, -1.25f, 2.5f, 12f, 2.5f), PartPose.offset(-5.5f, 8f, 0f));
        root.addOrReplaceChild("arm_right",
            CubeListBuilder.create().texOffs(48, 16).addBox(-1.25f, 0f, -1.25f, 2.5f, 12f, 2.5f), PartPose.offset(5.5f, 8f, 0f));
        return TexturedModelData.createDefault(mesh, new TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        // plain human-ish walk
        float swing = (float) Math.sin(state.ageTicks * 0.2f) * 0.55f;
        withChild("leg_left", part -> part.xRot = swing);
        withChild("leg_right", part -> part.xRot = -swing);
        withChild("arm_left", part -> part.xRot = -swing * 0.7f);
        withChild("arm_right", part -> part.xRot = swing * 0.7f);
        withChild("head", part -> part.xRot = (float) Math.sin(state.ageTicks * 0.05f) * 0.05f);
    }
}
