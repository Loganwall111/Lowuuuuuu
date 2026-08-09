package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.CreatorHandEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * THE HAND — five fingers of falling verdict, palm outwards like weather.
 * Rendered the same scale the sky uses for cloud-banks: enormous.
 */
public class CreatorHandRenderer extends DsEntityRenderer<CreatorHandEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("creator_hand"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/creator_hand.png");

    public CreatorHandRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 1.1f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // the palm
        root.addOrReplaceChild("palm",
            CubeListBuilder.create().texOffs(0, 0).addBox(-6f, -8f, -5f, 12f, 4f, 10f),
            PartPose.offset(0f, 10f, 0f));
        // five fingers, underneath — the pointing doom
        for (int i = 0; i < 5; i++) {
            float fx = -4f + i * 2f;
            root.addOrReplaceChild("finger_" + i,
                CubeListBuilder.create().texOffs(40, i == 2 ? 16 : 0 + i * 6)
                    .addBox(-0.8f, 0f, -0.8f, 1.6f, 5.5f + (2 - Math.abs(i - 2)) * 1.2f, 1.6f),
                PartPose.offset(fx, 6.5f, 0f));
        }
        // wrist-tail: up into the sky's sleeve
        root.addOrReplaceChild("wrist",
            CubeListBuilder.create().texOffs(0, 30).addBox(-4f, -14f, -4f, 8f, 6f, 8f),
            PartPose.offset(0f, 10f, 0f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        // it flexes while it descends
        for (int i = 0; i < 5; i++) {
            int k = i;
            withChild("finger_" + k, part -> {
                part.xRot = 0.25f + (float) Math.sin(age * 0.2f + k * 0.7f) * 0.25f;
                part.zRot = (k - 2) * 0.03f + (float) Math.cos(age * 0.15f + k) * 0.04f;
            });
        }
    }
}
