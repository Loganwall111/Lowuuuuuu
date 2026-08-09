package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.CreatorEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * THE CREATOR — world-scale robe of starfield, two red eyes that do not ever close.
 * The texture does most of the talking (constellations, fracture seams), the scale attribute
 * does the remainder; a 26.4-x-sized head rises through the cloud cover.
 */
public class CreatorRenderer extends DsEntityRenderer<CreatorEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("creator"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/creator.png");

    public CreatorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.55f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // the robe of the cosmos: wider at the crown, tapering into event-Horizon skirt
        root.addOrReplaceChild("robe",
            CubeListBuilder.create().texOffs(0, 0).addBox(-12f, -6f, -10f, 24f, 26f, 20f),
            PartPose.offset(0f, 6f, 0f));
        // the mask: the skull-front and the eyes the lore keeps quoting
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 46).addBox(-7f, -14f, -8f, 14f, 12f, 14f),
            PartPose.offset(0f, 6f, 0f));
        root.addOrReplaceChild("eyes",
            CubeListBuilder.create().texOffs(48, 56).addBox(-5f, -9f, -9.5f, 10f, 3f, 2f),
            PartPose.offset(0f, 6f, 0f));
        // crown ridges — the constellation bands
        root.addOrReplaceChild("crown",
            CubeListBuilder.create().texOffs(48, 40).addBox(-8f, -16f, -7f, 16f, 2f, 12f),
            PartPose.offset(0f, 6f, 0f));
        // arm sleeves — out of which THE HAND issues
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(60, 0).addBox(-3f, 0f, -3f, 6f, 18f, 6f),
            PartPose.offset(-15f, -2f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(60, 0).addBox(-3f, 0f, -3f, 6f, 18f, 6f),
            PartPose.offset(15f, -2f, 0f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(96, 96));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        // breathing, at planet scale
        withChild("robe", part -> {
            part.yScale = 1.0f + (float) Math.sin(age * 0.015f) * 0.015f;
            part.yRot = (float) Math.sin(age * 0.006f) * 0.03f;
        });
        withChild("head", part -> {
            part.yRot = (float) Math.sin(age * 0.01f) * 0.18f;
            part.xRot = 0.25f + (float) Math.sin(age * 0.008f) * 0.06f;
        });
        withChild("eyes", part -> {
            // the eyes follow — and glow pulses with nothing else in the model
            float glow = 1.0f + (float) (Math.sin(age * 0.3f) * 0.06f);
            part.xScale = glow;
        });
        withChild("crown", part -> part.yRot = (float) Math.sin(age * 0.01f) * 0.18f);
        // the arms sway like weather decides things
        withChild("arm_l", part -> part.xRot = (float) Math.sin(age * 0.02f) * 0.12f);
        withChild("arm_r", part -> part.xRot = (float) Math.cos(age * 0.017f) * 0.12f);
    }

    @Override
    protected String emissiveChild() {
        return "eyes";
    }
}
