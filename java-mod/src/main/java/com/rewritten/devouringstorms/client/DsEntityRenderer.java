package com.rewritten.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Shared renderer for all Devouring Storms entities: code-built ModelPart models,
 * classic flipped-y entity transform (cubes authored in Blockbench texel space, +y down),
 * per-state tint hooks, and an optional emissive pass for anything that should glow through
 * the darkness it was born in.
 *
 * @param <E> entity type
 * @param <S> render state type
 */
public abstract class DsEntityRenderer<E extends LivingEntity, S extends DsRenderState> extends EntityRenderer<E, S> {

    private final ModelPart root;
    private final ResourceLocation texture;
    private final float baseScale;

    protected DsEntityRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer,
                               ResourceLocation texture, float baseScale) {
        super(ctx);
        this.root = ctx.bakeLayer(layer);
        this.texture = texture;
        this.baseScale = baseScale;
        this.shadowRadius = 0.6f * baseScale;
    }

    protected abstract S newRenderState();

    /** Filled every frame: subclass animation on the baked model parts. */
    protected abstract void animate(S state);

    /** ARGB tint for the main pass; alpha honoured (translucent layer). */
    protected int tint(S state) {
        int alpha = (int) (state.opacity * 255.0f) & 0xFF;
        return (alpha << 24) | 0xFFFFFF;
    }

    /** Child name that should render emissive (fullbright, small bloom) — or null. */
    protected String emissiveChild() {
        return null;
    }

    @Override
    public S createRenderState() {
        return newRenderState();
    }

    @Override
    public void extractRenderState(E entity, S state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageTicks = entity.tickCount + partialTick;
        fillExtra(entity, state, partialTick);
    }

    /** Subclass state extraction extension point. */
    protected void fillExtra(E entity, S state, float partialTick) {
    }

    @Override
    public void render(S state, PoseStack pose, MultiBufferSource buffers, int packedLight) {
        animate(state);

        pose.pushPose();
        pose.scale(this.baseScale, this.baseScale, this.baseScale);
        pose.scale(-1.0f, -1.0f, 1.0f);
        pose.translate(0.0f, -1.501f, 0.0f);

        var consumer = buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(this.texture));
        this.root.render(pose, consumer, packedLight, OverlayTexture.NO_OVERLAY, tint(state));

        String emissive = emissiveChild();
        if (emissive != null) {
            var part = this.root.getChild(emissive);
            if (part != null && part.visible) {
                part.render(pose, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY, tint(state));
            }
        }
        pose.popPose();
        super.render(state, pose, buffers, packedLight);
    }

    /** Vertical bob in blocks, applied before the flip — positive is up. */
    protected static void bob(S state, PoseStack pose, float amplitude, float speed) {
        pose.translate(0.0, Math.sin(state.ageTicks * speed) * amplitude, 0.0);
    }

    /** Access a named child of the baked root for animation hooks. Null-safe. */
    protected final ModelPart child(String name) {
        try {
            return this.root.getChild(name);
        } catch (RuntimeException missing) {
            return null;
        }
    }

    /** Run an animation hook on a named child if it exists. */
    protected final void withChild(String name, java.util.function.Consumer<ModelPart> action) {
        ModelPart part = child(name);
        if (part != null) action.accept(part);
    }
}
