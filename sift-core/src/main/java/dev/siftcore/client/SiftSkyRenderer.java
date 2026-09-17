package dev.siftcore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.siftcore.SiftDimensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public final class SiftSkyRenderer implements DimensionRenderingRegistry.SkyRenderer {
    @Override
    public void render(WorldRenderContext context) {
        if (!SiftDimensions.isSift(context.world())) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        float depth = 0.0f;
        float fallSpeed = 0.0f;
        if (player != null) {
            depth = MathHelper.clamp((float) ((-player.getY() - 64.0D) / 512.0D), 0.0f, 1.0f);
            fallSpeed = MathHelper.clamp((float) (-player.getVelocity().y * 2.5D), 0.0f, 1.0f);
        }

        // Never leave a SkyType.NONE dimension black if a resource reload omitted our shader.
        if (SiftShaders.SKY == null) {
            renderFallback(context, depth);
            return;
        }

        SiftShaders.set(SiftShaders.SKY, "GameTime", (context.world().getTime() + context.tickDelta()) / 20.0f);
        SiftShaders.set(SiftShaders.SKY, "Depth", depth);
        SiftShaders.set(SiftShaders.SKY, "FallSpeed", fallSpeed);
        SiftShaders.setScreenSize(SiftShaders.SKY);

        client.getProfiler().push("sift_sky");
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> SiftShaders.SKY);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(-1.0D, -1.0D, 0.0D).next();
        buffer.vertex(1.0D, -1.0D, 0.0D).next();
        buffer.vertex(1.0D, 1.0D, 0.0D).next();
        buffer.vertex(-1.0D, 1.0D, 0.0D).next();
        Tessellator.getInstance().draw();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        client.getProfiler().pop();
    }

    private static void renderFallback(WorldRenderContext context, float depth) {
        float green = 0.04f + depth * 0.08f;
        float red = 0.01f + depth * 0.025f;
        float blue = 0.015f + depth * 0.02f;
        int r = (int) (red * 255.0f);
        int g = (int) (green * 255.0f);
        int b = (int) (blue * 255.0f);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        float radius = 256.0f;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        addFace(buffer, matrix, -radius, -radius, -radius, radius, -radius, -radius, radius, radius, -radius, -radius, radius, -radius, r, g, b);
        addFace(buffer, matrix, radius, -radius, radius, -radius, -radius, radius, -radius, radius, radius, radius, radius, radius, r, g, b);
        addFace(buffer, matrix, -radius, radius, -radius, radius, radius, -radius, radius, radius, radius, -radius, radius, radius, r, g, b);
        addFace(buffer, matrix, -radius, -radius, radius, radius, -radius, radius, radius, -radius, -radius, -radius, -radius, -radius, r, g, b);
        addFace(buffer, matrix, -radius, -radius, radius, -radius, radius, radius, -radius, radius, -radius, -radius, -radius, -radius, r, g, b);
        addFace(buffer, matrix, radius, -radius, -radius, radius, radius, -radius, radius, radius, radius, radius, -radius, radius, r, g, b);
        Tessellator.getInstance().draw();
        matrices.pop();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void addFace(
            BufferBuilder buffer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            int red, int green, int blue
    ) {
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, 255).next();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, 255).next();
        buffer.vertex(matrix, x3, y3, z3).color(red, green, blue, 255).next();
        buffer.vertex(matrix, x4, y4, z4).color(red, green, blue, 255).next();
    }
}
