package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.CartShopperEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * THE CART SHOPPER — a human pain silhouette pushing the store's only empty cart,
 * forever, aisle four, forever.
 */
public class CartShopperRenderer extends DsEntityRenderer<CartShopperEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("cart_shopper"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/cart_shopper.png");

    public CartShopperRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 0.95f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // the shopper: bowed apron figure
        root.addOrReplaceChild("torso",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -2f, -2f, 7f, 10f, 4f),
            PartPose.offset(0f, 10f, 0f));
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 15).addBox(-3f, -3f, -3f, 6f, 6f, 6f),
            PartPose.offset(0f, 6.5f, -1.5f));
        root.addOrReplaceChild("leg_l",
            CubeListBuilder.create().texOffs(0, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(-2f, 10f, 0f));
        root.addOrReplaceChild("leg_r",
            CubeListBuilder.create().texOffs(0, 30).addBox(-1.5f, 0f, -1.5f, 3f, 9f, 3f),
            PartPose.offset(2f, 10f, 0f));
        // THE CART — basket, wheels, handle. It rattles. It minds.
        root.addOrReplaceChild("cart",
            CubeListBuilder.create().texOffs(20, 30).addBox(-2.5f, -2f, -4.5f, 5f, 3f, 3f),
            PartPose.offset(0f, 8f, -4f));
        root.addOrReplaceChild("cart_handle",
            CubeListBuilder.create().texOffs(28, 38).addBox(-2.5f, 0f, -2.5f, 5f, 1f, 1f),
            PartPose.offset(0f, 8f, -3f));
        for (int i = 0; i < 2; i++) {
            root.addOrReplaceChild("wheel_" + i,
                CubeListBuilder.create().texOffs(36, 38).addBox(-0.5f, -0.5f, -0.5f, 1f, 1f, 1f),
                PartPose.offset(i == 0 ? -1.8f : 1.8f, 5.5f, -3f));
        }
        root.addOrReplaceChild("arm_l",
            CubeListBuilder.create().texOffs(40, 30).addBox(-1.5f, 0f, -1.5f, 3f, 8f, 3f),
            PartPose.offset(-4.5f, 1f, 0f));
        root.addOrReplaceChild("arm_r",
            CubeListBuilder.create().texOffs(40, 30).addBox(-1.5f, 0f, -1.5f, 3f, 8f, 3f),
            PartPose.offset(4.5f, 1f, 0f));
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(64, 64));
    }

    @Override
    protected void animate(DsRenderState state) {
        float age = state.ageTicks;
        float swing = (float) Math.sin(age * 0.2f) * 0.55f;
        withChild("leg_l", part -> part.xRot = -swing);
        withChild("leg_r", part -> part.xRot = swing);
        // arms locked on the handle, trembling with the load
        withChild("arm_l", part -> part.xRot = -1.05f + (float) Math.sin(age * 0.3f) * 0.04f);
        withChild("arm_r", part -> part.xRot = -1.05f - (float) Math.sin(age * 0.3f) * 0.04f);
        withChild("cart", part -> part.zRot = (float) Math.sin(age * 0.44f) * 0.1f);
        withChild("head", part -> part.xRot = 0.35f); // always bowed
    }
}
