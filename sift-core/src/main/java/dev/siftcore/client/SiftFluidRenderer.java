package dev.siftcore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.siftcore.SiftDimensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

/** Visual-only fluid sheets; there is deliberately no fluid or block collision. */
@Environment(EnvType.CLIENT)
public final class SiftFluidRenderer {
    private static final double LAYER_SPACING = 48.0D;
    private static final float SHEET_RADIUS = 112.0F;

    private SiftFluidRenderer() {
    }

    public static void render(WorldRenderContext context) {
        if (SiftShaders.FINAL == null || !SiftDimensions.isSift(context.world())) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = context.world();
        Vec3d camera = context.camera().getPos();
        double anchor = Math.floor(camera.y / LAYER_SPACING) * LAYER_SPACING;
        double playerY = client.player == null ? camera.y : client.player.getY();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> SiftShaders.FINAL);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        for (int layer = -2; layer <= 2; layer++) {
            double fluidY = anchor + layer * LAYER_SPACING + 16.0D;
            double delta = Math.abs(playerY - fluidY) / LAYER_SPACING;

            SiftShaders.set(SiftShaders.FINAL, "GameTime", (world.getTime() + context.tickDelta()) / 20.0f);
            SiftShaders.set(SiftShaders.FINAL, "Layer", (float) layer);
            SiftShaders.set(SiftShaders.FINAL, "PlayerDelta", (float) delta);
            SiftShaders.setScreenSize(SiftShaders.FINAL);

            context.matrixStack().push();
            context.matrixStack().translate(0.0D, fluidY - camera.y, 0.0D);
            var matrix = context.matrixStack().peek().getPositionMatrix();
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(matrix, -SHEET_RADIUS, 0.0F, -SHEET_RADIUS).texture(0.0F, 0.0F).color(255, 255, 255, 255).next();
            buffer.vertex(matrix, SHEET_RADIUS, 0.0F, -SHEET_RADIUS).texture(1.0F, 0.0F).color(255, 255, 255, 255).next();
            buffer.vertex(matrix, SHEET_RADIUS, 0.0F, SHEET_RADIUS).texture(1.0F, 1.0F).color(255, 255, 255, 255).next();
            buffer.vertex(matrix, -SHEET_RADIUS, 0.0F, SHEET_RADIUS).texture(0.0F, 1.0F).color(255, 255, 255, 255).next();
            Tessellator.getInstance().draw();
            context.matrixStack().pop();
        }

        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
