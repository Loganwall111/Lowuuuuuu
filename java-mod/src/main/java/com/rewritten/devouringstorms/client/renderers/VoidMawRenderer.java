package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.VoidMawEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * THE VOID MAW — a black sphere of wrong and its ring of borrowed light.
 * Gravitational lensing is approximated the honest way: a warped starlight band the
 * renderer spins around the singularity while the Iris shader bends the sky behind it.
 */
public class VoidMawRenderer extends DsEntityRenderer<VoidMawEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("void_maw"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/void_maw.png");

    public VoidMawRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.8f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    @Override
    public void extractRenderState(VoidMawEntity entity, DsRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        float sc = 1.0f;
        var attr = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
        if (attr != null) sc = (float) attr.getValue();
        state.ageTicks = entity.tickCount + partialTick;
        state.growth = sc; // repurposed: maw fedness drives the classrender scale pulse
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // singularity: the place light goes to be quiet
        root.addOrReplaceChild("shadow",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5f, -5f, -5f, 10f, 10f, 10f),
            PartPose.offset(0f, 12f, 0f));
        // warped halo facets — the lensed sky, sampling the bright end of the texture
        root.addOrReplaceChild("halo_top",
            CubeListBuilder.create().texOffs(0, 30).addBox(-8f, -0.5f, -1.5f, 16f, 1f, 3f),
            PartPose.offset(0f, 7.5f, 0f));
        root.addOrReplaceChild("halo_bot",
            CubeListBuilder.create().texOffs(0, 34).addBox(-8f, -0.5f, -1.5f, 16f, 1f, 3f),
            PartPose.offset(0f, 16.5f, 0f));
        root.addOrReplaceChild("halo_l",
            CubeListBuilder.create().texOffs(0, 38).addBox(-0.5f, -8f, -1.5f, 1f, 16f, 3f),
            PartPose.offset(-7.5f, 12f, 0f));
        root.addOrReplaceChild("halo_r",
            CubeListBuilder.create().texOffs(0, 38).addBox(-0.5f, -8f, -1.5f, 1f, 16f, 3f),
            PartPose.offset(7.5f, 12f, 0f));
        // core glow: nothing should glow inside a black hole, and yet
        root.addOrReplaceChild("core",
            CubeListBuilder.create().texOffs(40, 0).addBox(-1.5f, -1.5f, -5.5f, 3f, 3f, 1f),
            PartPose.offset(0f, 12f, 0f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float t = state.ageTicks;
        // the lensed band precesses. Physics would wince; cinematographers nod.
        float tilt = 0.35f + (float) Math.sin(t * 0.021f) * 0.18f;
        float spin = t * 0.03f;
        withChild("halo_top", part -> { part.xRot = tilt; part.yRot = spin; });
        withChild("halo_bot", part -> { part.xRot = -tilt; part.yRot = spin; });
        withChild("halo_l", part -> { part.zRot = tilt; part.yRot = -spin; });
        withChild("halo_r", part -> { part.zRot = -tilt; part.yRot = -spin; });
        // the heartbeat of having eaten
        float pulse = 1.0f + (float) Math.sin(t * 0.11f) * 0.06f;
        withChild("shadow", part -> {
            part.xScale = pulse;
            part.yScale = pulse;
            part.zScale = pulse;
        });
        withChild("core", part -> part.zRot = t * 0.07f);
    }

    @Override
    protected String emissiveChild() {
        return "core";
    }
}
