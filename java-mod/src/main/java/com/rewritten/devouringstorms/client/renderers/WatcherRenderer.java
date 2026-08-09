package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.WatcherEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.TexturedModelData;
import net.minecraft.client.model.geom.builders.TextureDimensions;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * THE WATCHER. Almost three blocks of wrongness wearing the shape of a person.
 * Its eyes are the only part of it that shines — kept on a separate fullbright pass.
 */
public class WatcherRenderer extends DsEntityRenderer<WatcherEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("watcher"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/watcher.png");

    public WatcherRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 1.35f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        // impossibly long thing (all y+ is down, model space)
        root.addOrReplaceChild("torso",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5f, 8f, -2.5f, 10f, 20f, 5f), PartPose.ZERO);
        root.addOrReplaceChild("arm_left",
            CubeListBuilder.create().texOffs(30, 0).addBox(-1f, 0f, -1f, 2f, 24f, 2f), PartPose.offset(-6f, 8f, 0f));
        root.addOrReplaceChild("arm_right",
            CubeListBuilder.create().texOffs(30, 0).addBox(-1f, 0f, -1f, 2f, 24f, 2f), PartPose.offset(6f, 8f, 0f));
        root.addOrReplaceChild("leg_left",
            CubeListBuilder.create().texOffs(40, 0).addBox(-1.5f, 0f, -1.5f, 3f, 16f, 3f), PartPose.offset(-2.5f, 28f, 0f));
        root.addOrReplaceChild("leg_right",
            CubeListBuilder.create().texOffs(40, 0).addBox(-1.5f, 0f, -1.5f, 3f, 16f, 3f), PartPose.offset(2.5f, 28f, 0f));
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 30).addBox(-4f, -8f, -4f, 8f, 8f, 8f), PartPose.offset(0f, 8f, 0f));
        // the gaze itself — emissive pass
        root.addOrReplaceChild("gaze",
            CubeListBuilder.create().texOffs(0, 46).addBox(-3f, -2f, -4.5f, 6f, 2f, 1f), PartPose.offset(0f, 8f, 0f));
        return TexturedModelData.createDefault(mesh, new TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        // It never swings its arms while walking. That's the point.
        withChild("head", part -> part.xRot = (float) Math.sin(state.ageTicks * 0.05f) * 0.08f);
        withChild("torso", part -> part.xRot = 0.08f + (float) Math.sin(state.ageTicks * 0.03f) * 0.03f);
    }

    @Override
    protected String emissiveChild() {
        return "gaze";
    }

    @Override
    protected int tint(DsRenderState state) {
        // near-black body; the eyes carry the whole face
        return (state.opacity == 1.0f ? 0xFF : (int) (state.opacity * 255) << 24)
            | 0x40 << 16 | 0x3A << 8 | 0x50;
    }
}
