package dev.siftcore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.siftcore.SiftDimensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class SiftSkyRenderer implements DimensionRenderingRegistry.SkyRenderer {
    @Override
    public void render(WorldRenderContext context) {
        if (SiftShaders.SKY == null || !SiftDimensions.isSift(context.world())) {
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
}
