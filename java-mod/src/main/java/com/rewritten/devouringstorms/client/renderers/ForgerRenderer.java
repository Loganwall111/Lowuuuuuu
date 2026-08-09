package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.ForgerEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * THE FORGER — forge-cauldron head hanging from a vent of arms, tentacles below.
 * It floats over its own shadow, pouring yesterday's sky into tomorrow's.
 */
public class ForgerRenderer extends DsEntityRenderer<ForgerEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("forger"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/forger.png");

    public ForgerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 1.2f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // forge-bell crown body
        root.addOrReplaceChild("bell",
            CubeListBuilder.create().texOffs(0, 0).addBox(-6f, -4f, -6f, 12f, 10f, 12f),
            PartPose.offset(0f, 8f, 0f));
        // face slit (emissive heart-flame)
        root.addOrReplaceChild("flame",
            CubeListBuilder.create().texOffs(48, 20).addBox(-2f, 1f, -6.5f, 4f, 3f, 1f),
            PartPose.offset(0f, 8f, 0f));
        // tentacle bundle below: eight pour-spouts
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            float x = (float) Math.cos(a) * 4f;
            float z = (float) Math.sin(a) * 4f;
            root.addOrReplaceChild("spout_" + i,
                CubeListBuilder.create().texOffs(i % 2 == 0 ? 40 : 44, 40).addBox(-1.2f, 0f, -1.2f, 2.4f, 9f, 2.4f),
                PartPose.offset(x, 6f, z));
        }
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        withChild("bell", part -> part.yRot = age * 0.01f);
        withChild("flame", part -> {
            float gl = 1.0f + (float) Math.sin(age * 0.4f) * 0.1f;
            part.xScale = gl;
        });
        for (int i = 0; i < 8; i++) {
            float a = i * (float) Math.PI / 4.0f;
            int k = i;
            withChild("spout_" + k, part -> {
                part.xRot = (float) Math.sin(age * 0.11f + k * 0.8f) * 0.45f + 0.1f;
                part.zRot = (float) Math.cos(age * 0.09f + k * 0.5f) * 0.45f;
            });
        }
    }

    @Override
    protected String emissiveChild() {
        return "flame";
    }
}
