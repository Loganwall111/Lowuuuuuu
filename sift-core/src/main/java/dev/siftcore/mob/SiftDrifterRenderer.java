package dev.siftcore.mob;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.siftcore.SiftCore;
import dev.siftcore.client.SiftShaders;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Procedural billboard renderer for the first Sift atmospheric mob. */
@Environment(EnvType.CLIENT)
public final class SiftDrifterRenderer extends EntityRenderer<SiftDrifterEntity> {
    public SiftDrifterRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            SiftDrifterEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
            int light
    ) {
        if (SiftShaders.DRIFTER == null) {
            return;
        }

        SiftShaders.set(SiftShaders.DRIFTER, "GameTime", (entity.getWorld().getTime() + tickDelta) / 20.0F);
        SiftShaders.set(SiftShaders.DRIFTER, "Pulse", entity.getPulse(tickDelta));
        SiftShaders.set(SiftShaders.DRIFTER, "Seed", entity.getDrifterSeed());
        SiftShaders.setScreenSize(SiftShaders.DRIFTER);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> SiftShaders.DRIFTER);

        matrices.push();
        matrices.multiply(this.dispatcher.getRotation());
        float scale = entity.getDrifterScale();
        matrices.scale(scale, scale, scale);
        MatrixStack.Entry entry = matrices.peek();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(entry.getPositionMatrix(), -1.0F, -1.0F, 0.0F).texture(0.0F, 1.0F).color(255, 255, 255, 255).next();
        buffer.vertex(entry.getPositionMatrix(), 1.0F, -1.0F, 0.0F).texture(1.0F, 1.0F).color(255, 255, 255, 255).next();
        buffer.vertex(entry.getPositionMatrix(), 1.0F, 1.0F, 0.0F).texture(1.0F, 0.0F).color(255, 255, 255, 255).next();
        buffer.vertex(entry.getPositionMatrix(), -1.0F, 1.0F, 0.0F).texture(0.0F, 0.0F).color(255, 255, 255, 255).next();
        Tessellator.getInstance().draw();
        matrices.pop();

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    @Override
    public Identifier getTexture(SiftDrifterEntity entity) {
        return SiftCore.id("textures/misc/white.png");
    }
}
