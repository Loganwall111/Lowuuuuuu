package net.mcsm.sift.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * World right back level overlay - you can't see it for a few seconds
 * Blackout effect during Inner Space -> Overworld transition
 * Also during Fabric -> Emptiness and Bottom Fabric -> Unknown
 */
public final class WorldReentryOverlay {

    public static final WorldReentryOverlay INSTANCE = new WorldReentryOverlay();

    private float blackoutProgress = 0f; // 0 = visible, 1 = full black
    private float targetBlackout = 0f;
    private int blackoutTicks = 0;
    private int maxBlackoutTicks = 0;

    private WorldReentryOverlay() {}

    public void triggerBlackout(int durationTicks) {
        this.targetBlackout = 1f;
        this.blackoutTicks = 0;
        this.maxBlackoutTicks = durationTicks;
    }

    public void tick() {
        if (blackoutTicks < maxBlackoutTicks) {
            blackoutTicks++;
            // Fade in quickly, hold, fade out slowly
            float progress = (float) blackoutTicks / maxBlackoutTicks;
            if (progress < 0.2f) {
                // Fade in - can't see
                targetBlackout = Mth.lerp(progress / 0.2f, 0f, 1f);
            } else if (progress < 0.7f) {
                // Hold black
                targetBlackout = 1f;
            } else {
                // Fade out - slowly see again
                targetBlackout = Mth.lerp((progress - 0.7f) / 0.3f, 1f, 0f);
            }
        } else {
            targetBlackout = 0f;
        }
        blackoutProgress = Mth.lerp(0.1f, blackoutProgress, targetBlackout);
    }

    public float getBlackout() {
        return blackoutProgress;
    }

    public boolean isInBlackout() {
        return blackoutProgress > 0.01f;
    }

    public void render(PoseStack poseStack) {
        if (blackoutProgress < 0.01f) return;

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();

        // Full screen black with slight purple tint - you can't see it for a few seconds
        float alpha = blackoutProgress;
        // Add starfield during blackout - pitch black void of stars you see faint stars
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, 0, 0).color(0f, 0f, 0f, alpha).endVertex();
        buf.vertex(matrix, 0, height, 0).color(0f, 0f, 0f, alpha).endVertex();
        buf.vertex(matrix, width, height, 0).color(0.05f, 0f, 0.1f, alpha).endVertex();
        buf.vertex(matrix, width, 0, 0).color(0f, 0f, 0f, alpha).endVertex();
        tess.end();

        // Add subtle stars during blackout - so you can't see world but see void stars
        if (alpha > 0.5f) {
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < 20; i++) {
                float x = (Mth.sin(i * 1.5f) * 0.5f + 0.5f) * width;
                float y = (Mth.cos(i * 2.3f) * 0.5f + 0.5f) * height;
                float size = 1 + (i % 3);
                float starAlpha = alpha * (0.5f + 0.5f * Mth.sin((blackoutTicks + i * 10) * 0.1f));
                buf.vertex(matrix, x - size, y - size, 0).color(1f, 1f, 1f, starAlpha).endVertex();
                buf.vertex(matrix, x - size, y + size, 0).color(1f, 1f, 1f, starAlpha).endVertex();
                buf.vertex(matrix, x + size, y + size, 0).color(1f, 1f, 1f, starAlpha).endVertex();
                buf.vertex(matrix, x + size, y - size, 0).color(1f, 1f, 1f, starAlpha).endVertex();
            }
            tess.end();
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public void clear() {
        blackoutProgress = 0f;
        targetBlackout = 0f;
        blackoutTicks = 0;
        maxBlackoutTicks = 0;
    }
}
