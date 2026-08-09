package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.MonstrosityEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * THE MONSTROSITY — moustache first, apology never.
 * A humanoid face plastered over a broadcast storm: jacket of channel-static, the famous
 * moustache band, and eyes the colour of an untuned channel.
 */
public class MonstrosityRenderer extends DsEntityRenderer<MonstrosityEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("monstrosity"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/monstrosity.png");

    public MonstrosityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 1.15f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("jacket",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5f, -2f, -3f, 10f, 12f, 6f),
            PartPose.offset(0f, 12f, 0f));
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 21).addBox(-4f, -4f, -4f, 8f, 8f, 8f),
            PartPose.offset(0f, 6f, 0f));
        // THE MOUSTACHE — glorious, spanning, non-negotiable
        root.addOrReplaceChild("moustache",
            CubeListBuilder.create().texOffs(32, 20).addBox(-4.5f, 1f, -5.5f, 9f, 2f, 2f),
            PartPose.offset(0f, 6f, 0f));
        // shoulder antennae — reception for the overtake
        root.addOrReplaceChild("antenna_l",
            CubeListBuilder.create().texOffs(48, 30).addBox(-0.5f, -8f, -0.5f, 1f, 8f, 1f),
            PartPose.offset(-4f, 0f, 0f));
        root.addOrReplaceChild("antenna_r",
            CubeListBuilder.create().texOffs(48, 30).addBox(-0.5f, -8f, -0.5f, 1f, 8f, 1f),
            PartPose.offset(4f, 0f, 0f));
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(40, 40).addBox(-1.5f, 0f, -1.5f, 3f, 8f, 3f),
            PartPose.offset(-6f, -1f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(40, 40).addBox(-1.5f, 0f, -1.5f, 3f, 8f, 3f),
            PartPose.offset(6f, -1f, 0f));
        root.addOrReplaceChild("leg_l",
            CubeListBuilder.create().texOffs(0, 40).addBox(-2f, 0f, -2f, 4f, 9f, 4f),
            PartPose.offset(-2.5f, 12f, 0f));
        root.addOrReplaceChild("leg_r",
            CubeListBuilder.create().texOffs(0, 40).addBox(-2f, 0f, -2f, 4f, 9f, 4f),
            PartPose.offset(2.5f, 12f, 0f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        float swing = (float) Math.sin(age * 0.18f) * 0.6f;
        withChild("leg_l", part -> part.xRot = -swing);
        withChild("leg_r", part -> part.xRot = swing);
        // the moustache preens independently
        withChild("moustache", part -> {
            part.yRot = (float) Math.sin(age * 0.07f) * 0.09f;
            part.xRot = 0.08f + (float) Math.sin(age * 0.11f) * 0.04f;
        });
        withChild("antenna_l", part -> part.xRot = (float) Math.sin(age * 0.21f) * 0.21f);
        withChild("antenna_r", part -> part.xRot = (float) Math.cos(age * 0.19f) * 0.21f);
        withChild("head", part -> part.yRot = (float) Math.sin(age * 0.04f) * 0.35f);
    }

    @Override
    protected String emissiveChild() {
        return "head"; // eyes = the untuned channel
    }
}
