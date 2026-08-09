package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.TheTakenEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** THE TAKEN — a villager-shaped hate. Arms too long, eyes too sure. */
public class TheTakenRenderer extends DsEntityRenderer<TheTakenEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("the_taken"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/the_taken.png");

    public TheTakenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.95f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // hunched torso, bowed forward
        root.addOrReplaceChild("torso",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -2f, -2f, 8f, 10f, 4f),
            PartPose.offset(0f, 10f, 1.5f));
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 15).addBox(-3.5f, -4f, -3.5f, 7f, 7f, 7f),
            PartPose.offset(0f, 6.5f, -2.5f));
        // arms too long. They remember picking up children, holding doors.
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(28, 30).addBox(-1.5f, 0f, -1.5f, 3f, 14f, 3f),
            PartPose.offset(-5.5f, 1f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(28, 30).addBox(-1.5f, 0f, -1.5f, 3f, 14f, 3f),
            PartPose.offset(5.5f, 1f, 0f));
        // clawed fingers
        root.addOrReplaceChild("claws_l",
            CubeListBuilder.create().texOffs(42, 46).addBox(-1.5f, 14f, -1.5f, 3f, 2f, 3f),
            PartPose.offset(-5.5f, 0f, 0f));
        root.addOrReplaceChild("claws_r",
            CubeListBuilder.create().texOffs(42, 46).addBox(-1.5f, 14f, -1.5f, 3f, 2f, 3f),
            PartPose.offset(5.5f, 0f, 0f));
        root.addOrReplaceChild("leg_l",
            CubeListBuilder.create().texOffs(0, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(-2.2f, 15f, 0f));
        root.addOrReplaceChild("leg_r",
            CubeListBuilder.create().texOffs(0, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(2.2f, 15f, 0f));
        // the long goat nose it grew to taste the wind
        root.addOrReplaceChild("nose",
            CubeListBuilder.create().texOffs(20, 22).addBox(-1f, -1f, -6.5f, 2f, 4f, 3f),
            PartPose.offset(0f, 7f, -2.5f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float a = state.ageTicks;
        float swing = (float) Math.sin(a * 0.18f) * 0.75f;
        withChild("arm_l", part -> part.xRot = swing - 0.35f);
        withChild("arm_r", part -> part.xRot = -swing - 0.35f);
        withChild("leg_l", part -> part.xRot = -swing * 0.6f);
        withChild("leg_r", part -> part.xRot = swing * 0.6f);
        // it sniffs constantly. It's looking for who it used to be.
        withChild("nose", part -> part.xRot = (float) Math.sin(state.ageTicks * 0.11f) * 0.15f - 0.1f);
        withChild("head", part -> {
            part.yRot = (float) Math.sin(state.ageTicks * 0.04f) * 0.4f;
            part.xRot = 0.25f; // bowed, forever
        });
    }

    @Override
    protected String emissiveChild() {
        return "head"; // the eyes remember the plague glow
    }
}
