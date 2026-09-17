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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * A visual abyss floor at the bottom of the configured dimension. It is drawn
 * as shader geometry only: the dimension remains blockless and the player can
 * fall through this shelf without collision or a landing transition.
 */
@Environment(EnvType.CLIENT)
public final class SiftGroundRenderer {
    private static final double GROUND_Y = -896.0D;
    private static final float GROUND_RADIUS = 512.0F;

    private SiftGroundRenderer() {
    }

    public static void render(WorldRenderContext context) {
        if (SiftShaders.GROUND == null || !SiftDimensions.isSift(context.world())) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = context.world();
        Vec3d camera = context.camera().getPos();
        if (camera.y <= GROUND_Y - 8.0D) {
            return;
        }

        float depth = MathHelper.clamp((float) ((-camera.y - 64.0D) / 512.0D), 0.0F, 1.0F);
        float fallSpeed = 0.0F;
        if (client.player != null) {
            fallSpeed = MathHelper.clamp((float) (-client.player.getVelocity().y * 2.5D), 0.0F, 1.0F);
        }
        SiftShaders.set(SiftShaders.GROUND, "GameTime", (world.getTime() + context.tickDelta()) / 20.0F);
        SiftShaders.set(SiftShaders.GROUND, "Depth", depth);
        SiftShaders.set(SiftShaders.GROUND, "FallSpeed", fallSpeed);
        SiftShaders.setScreenSize(SiftShaders.GROUND);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> SiftShaders.GROUND);

        context.matrixStack().push();
        context.matrixStack().translate(0.0D, GROUND_Y - camera.y, 0.0D);
        var matrix = context.matrixStack().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, -GROUND_RADIUS, 0.0F, -GROUND_RADIUS).texture(0.0F, 0.0F).color(255, 255, 255, 255).next();
        buffer.vertex(matrix, GROUND_RADIUS, 0.0F, -GROUND_RADIUS).texture(1.0F, 0.0F).color(255, 255, 255, 255).next();
        buffer.vertex(matrix, GROUND_RADIUS, 0.0F, GROUND_RADIUS).texture(1.0F, 1.0F).color(255, 255, 255, 255).next();
        buffer.vertex(matrix, -GROUND_RADIUS, 0.0F, GROUND_RADIUS).texture(0.0F, 1.0F).color(255, 255, 255, 255).next();
        Tessellator.getInstance().draw();
        context.matrixStack().pop();

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
