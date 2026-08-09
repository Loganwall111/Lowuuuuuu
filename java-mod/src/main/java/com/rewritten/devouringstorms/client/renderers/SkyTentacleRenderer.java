package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.SkyTentacleEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** SKY TENTACLE — a falling streak of sinew. */
public class SkyTentacleRenderer extends DsEntityRenderer<SkyTentacleEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("sky_tentacle"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/sky_tentacle.png");

    public SkyTentacleRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.8f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("stalk",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.2f, 0f, -1.2f, 2.4f, 14f, 2.4f),
            PartPose.offset(0f, 22f, 0f));
        root.addOrReplaceChild("tip",
            CubeListBuilder.create().texOffs(0, 20).addBox(-2f, -2f, -2f, 4f, 4f, 4f),
            PartPose.offset(0f, 36f, 0f));
        // suction cups down the stalk
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("cup_" + i,
                CubeListBuilder.create().texOffs(20, i * 4).addBox(-1.5f, -0.5f, -1.5f, 3f, 1f, 3f),
                PartPose.offset(0f, 26f + i * 3f, 0f));
        }
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(48, 48));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        withChild("stalk", part -> {
            part.xRot = (float) Math.sin(age * 0.3f) * 0.18f;
            part.zRot = (float) Math.cos(age * 0.25f) * 0.18f;
        });
        withChild("tip", part -> {
            float pulse = 1.0f + (float) Math.sin(age * 0.5f) * 0.1f;
            part.xScale = pulse;
            part.yScale = pulse;
            part.zScale = pulse;
        });
    }
}
