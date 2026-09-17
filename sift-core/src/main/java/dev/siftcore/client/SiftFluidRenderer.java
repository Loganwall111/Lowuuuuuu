package dev.siftcore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.siftcore.SiftDimensions;
import dev.siftcore.physics.SiftFluidField;
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
    private static final int VISIBLE_LAYERS = 2;
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
        int centerLayer = SiftFluidField.nearestLayerIndex(camera.y);
        double playerY = client.player == null ? camera.y : client.player.getY();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> SiftShaders.FINAL);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        for (int layer = -VISIBLE_LAYERS; layer <= VISIBLE_LAYERS; layer++) {
            double fluidY = SiftFluidField.layerHeight(centerLayer + layer);
            double delta = Math.abs(playerY - fluidY) / SiftFluidField.LAYER_SPACING;

            SiftShaders.set(SiftShaders.FINAL, "GameTime", (world.getTime() + context.tickDelta()) / 20.0f);
            SiftShaders.set(SiftShaders.FINAL, "Layer", (float) (centerLayer + layer));
            SiftShaders.set(SiftShaders.FINAL, "PlayerDelta", (float) delta);
            SiftShaders.set(SiftShaders.FINAL, "WorldOrigin", (float) camera.x, (float) camera.z);
            SiftShaders.set(SiftShaders.FINAL, "SheetRadius", SHEET_RADIUS);
            double flowTime = world.getTime() + context.tickDelta();
            SiftShaders.set(
                    SiftShaders.FINAL,
                    "FlowDirection",
                    (float) SiftFluidField.currentX(flowTime, camera.x, camera.z),
                    (float) SiftFluidField.currentZ(flowTime, camera.x, camera.z)
            );
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

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
