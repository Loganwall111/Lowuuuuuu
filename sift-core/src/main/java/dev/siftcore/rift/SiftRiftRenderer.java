package dev.siftcore.rift;

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

@Environment(EnvType.CLIENT)
public final class SiftRiftRenderer extends EntityRenderer<SiftRiftEntity> {
    public SiftRiftRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            SiftRiftEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
            int light
    ) {
        if (SiftShaders.RIFT == null) {
            return;
        }

        float scale = entity.getRiftScale();
        SiftShaders.set(SiftShaders.RIFT, "GameTime", (entity.getWorld().getTime() + tickDelta) / 20.0F);
        SiftShaders.set(SiftShaders.RIFT, "Pulse", entity.getPulse(tickDelta));
        SiftShaders.set(SiftShaders.RIFT, "Seed", entity.getRiftSeed());
        SiftShaders.set(SiftShaders.RIFT, "Choreography", entity.getChoreography());
        SiftShaders.setScreenSize(SiftShaders.RIFT);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> SiftShaders.RIFT);

        // Draw the pressure haze first, then the brighter filament veil, then
        // the projected cosmic aperture. Each layer is camera-facing but uses
        // a different shader pass so the atmosphere extends beyond the rim.
        SiftShaders.set(SiftShaders.RIFT, "Pass", 1.0F);
        drawQuad(matrices, scale * 1.55F, 76);
        SiftShaders.set(SiftShaders.RIFT, "Pass", 2.0F);
        drawQuad(matrices, scale * 1.30F, 124);
        SiftShaders.set(SiftShaders.RIFT, "Pass", 0.0F);
        drawQuad(matrices, scale, 255);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private void drawQuad(MatrixStack matrices, float scale, int alpha) {
        matrices.push();
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(scale, scale, scale);

        MatrixStack.Entry entry = matrices.peek();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(entry.getPositionMatrix(), -1.0F, -1.0F, 0.0F).texture(0.0F, 1.0F).color(255, 255, 255, alpha).next();
        buffer.vertex(entry.getPositionMatrix(), 1.0F, -1.0F, 0.0F).texture(1.0F, 1.0F).color(255, 255, 255, alpha).next();
        buffer.vertex(entry.getPositionMatrix(), 1.0F, 1.0F, 0.0F).texture(1.0F, 0.0F).color(255, 255, 255, alpha).next();
        buffer.vertex(entry.getPositionMatrix(), -1.0F, 1.0F, 0.0F).texture(0.0F, 0.0F).color(255, 255, 255, alpha).next();
        Tessellator.getInstance().draw();
        matrices.pop();
    }

    @Override
    public Identifier getTexture(SiftRiftEntity entity) {
        // The procedural pass does not sample a texture, but EntityRenderer still requires an id.
        return SiftCore.id("textures/misc/white.png");
    }
}
