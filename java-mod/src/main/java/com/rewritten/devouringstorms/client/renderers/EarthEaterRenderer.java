package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.EarthEaterEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * THE EARTH EATER — a jaw.
 * Rendered as a monumental ring of night-flesh and slow relic light: the creature is
 * functionally a mouth with opinions arranged around it.
 */
public class EarthEaterRenderer extends DsEntityRenderer<EarthEaterEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("earth_eater"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/earth_eater.png");

    public EarthEaterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.9f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ring of night-flesh: the maw perimeter
        root.addOrReplaceChild("jaw_top",
            CubeListBuilder.create().texOffs(0, 0).addBox(-14f, -4f, -14f, 28f, 4f, 28f),
            PartPose.offset(0f, 8f, 0f));
        root.addOrReplaceChild("jaw_bottom",
            CubeListBuilder.create().texOffs(0, 36).addBox(-13f, 0f, -13f, 26f, 4f, 26f),
            PartPose.offset(0f, 8f, 0f));
        // throat glow — a slow cyan at the bend (maw cavity)
        root.addOrReplaceChild("throat",
            CubeListBuilder.create().texOffs(48, 36).addBox(-6f, -1f, -6f, 12f, 2f, 12f),
            PartPose.offset(0f, 12f, 0f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(96, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        // chew
        float chew = (float) (0.5f + 0.5f * Math.sin(age * 0.06f));
        withChild("jaw_top", part -> part.y = 8f - chew * 2.0f);
        withChild("jaw_bottom", part -> part.y = 8f + chew * 2.0f);
        withChild("throat", part -> {
            float gl = 0.8f + chew * 0.6f;
            part.xScale = gl;
            part.zScale = gl;
        });
    }

    @Override
    protected String emissiveChild() {
        return "throat";
    }
}
