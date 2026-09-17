package net.mcsm.sift.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Animated skyboxes for each layer like with a rainbow one with rainbow water
 * God rays and colored lights coming from above
 * Everything designed to look extremely cool - Pixar-VFX triple-A
 */
public final class McsmSiftSkyRenderer {

    public static final McsmSiftSkyRenderer INSTANCE = new McsmSiftSkyRenderer();

    private float time = 0f;
    private float prevTime = 0f;

    private McsmSiftSkyRenderer() {}

    public void tick() {
        prevTime = time;
        time += 0.01f;
    }

    public void renderSky(PoseStack poseStack, Matrix4f projectionMatrix, float partialTicks, Camera camera, boolean isFoggy, Runnable skyFogSetup) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        int y = (int) mc.player.getY();
        if (!McsmVoidTiers.isInVoidDimension(y)) return;

        McsmVoidTiers.Tier tier = McsmVoidTiers.getTierForY(y);
        float depthFactor = (float)(McsmVoidTiers.TOP_BOUNDARY - y) / McsmVoidTiers.TOTAL_HEIGHT;

        // Cancel vanilla sky
        RenderSystem.setShaderColor(1, 1, 1, 1);

        poseStack.pushPose();

        // Render tier-specific skybox
        switch (tier) {
            case TIER_1_GEL_HORIZON -> renderTier1Sky(poseStack, partialTicks, depthFactor);
            case TIER_2_MENGER_SPONGE -> renderTier2Sky(poseStack, partialTicks, depthFactor);
            case TIER_3_RIFT_FIELD -> renderTier3Sky(poseStack, partialTicks, depthFactor);
            case TIER_4_DISPLACEMENT -> renderTier4Sky(poseStack, partialTicks, depthFactor);
            case TIER_5_IRIDESCENT_GEL -> renderTier5Sky(poseStack, partialTicks, depthFactor);
            default -> {}
        }

        poseStack.popPose();
    }

    private void renderTier1Sky(PoseStack poseStack, float partialTicks, float depth) {
        // God rays and lights coming from above - light shafts
        float t = Mth.lerp(partialTicks, prevTime, time);
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        // Sky gradient - cyan to soft pink with god rays
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        // Center top - bright god ray source
        buf.vertex(matrix, 0, 100, 0).color(1.0f, 1.0f, 0.9f, 0.8f).endVertex();

        int rays = 12;
        for (int i = 0; i <= rays; i++) {
            float angle = (float)i / rays * Mth.TWO_PI;
            float x = Mth.cos(angle) * 150;
            float z = Mth.sin(angle) * 150;
            // Alternate bright and dim for god ray effect
            float bright = (i % 2 == 0) ? 0.9f : 0.5f;
            float r = 0.6f + bright * 0.4f;
            float g = 0.9f + bright * 0.1f;
            float b = 1.0f;
            float a = 0.3f + bright * 0.3f;
            // Add colored lights variation
            if (i % 3 == 0) {
                r = 0.8f; g = 0.6f; b = 1.0f; // purple ray
            }
            buf.vertex(matrix, x, 80, z).color(r, g, b, a).endVertex();
        }
        tess.end();

        // Floating dust motes - like in concept images
        renderFloatingParticles(poseStack, t, depth);
    }

    private void renderTier2Sky(PoseStack poseStack, float partialTicks, float depth) {
        // Orange-to-pink emissive maze sky - tight, claustrophobic with glowing walls
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, 60, 0).color(1.0f, 0.5f, 0.2f, 0.6f).endVertex();
        for (int i = 0; i <= 8; i++) {
            float angle = (float)i / 8 * Mth.TWO_PI;
            float x = Mth.cos(angle) * 80;
            float z = Mth.sin(angle) * 80;
            float wave = Mth.sin(time + angle) * 0.1f;
            buf.vertex(matrix, x, 40 + wave * 10, z).color(1.0f, 0.3f + wave, 0.6f, 0.4f).endVertex();
        }
        tess.end();
    }

    private void renderTier3Sky(PoseStack poseStack, float partialTicks, float depth) {
        // Rift field - dark with neon-purple rifts and cosmic windows
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Dark purple base
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, 100, 0).color(0.1f, 0.05f, 0.2f, 0.9f).endVertex();
        for (int i = 0; i <= 16; i++) {
            float angle = (float)i / 16 * Mth.TWO_PI;
            float x = Mth.cos(angle) * 200;
            float z = Mth.sin(angle) * 200;
            float flicker = Mth.sin(time * 2 + angle * 2) * 0.1f;
            buf.vertex(matrix, x, 60, z).color(0.3f + flicker, 0.1f, 0.5f + flicker, 0.7f).endVertex();
        }
        tess.end();

        // Render active rifts via SiftRiftRenderer
        SiftRiftRenderer.INSTANCE.renderRifts(poseStack, poseStack.last().pose(), partialTicks);
    }

    private void renderTier4Sky(PoseStack poseStack, float partialTicks, float depth) {
        // Displacement bands - wavy spacetime with rainbow bands
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        for (int band = 0; band < 5; band++) {
            float bandY = 80 - band * 15;
            float hue = (time * 0.05f + band * 0.2f) % 1f;
            float[] rgb = hsvToRgb(hue, 0.8f, 0.9f);

            buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= 20; i++) {
                float x = (i / 20f - 0.5f) * 300;
                float wave = Mth.sin(time + x * 0.02f + band) * 5f;
                float z1 = -100 + wave;
                float z2 = 100 + wave;
                float a = 0.3f - band * 0.05f;
                buf.vertex(matrix, x, bandY, z1).color(rgb[0], rgb[1], rgb[2], a).endVertex();
                buf.vertex(matrix, x, bandY, z2).color(rgb[0], rgb[1], rgb[2], a * 0.5f).endVertex();
            }
            tess.end();
        }
    }

    private void renderTier5Sky(PoseStack poseStack, float partialTicks, float depth) {
        // Iridescent Cosmic Fluid - rainbow water sky, most beautiful layer
        // Like concept images: colorful water, god rays from below, floating
        float t = Mth.lerp(partialTicks, prevTime, time);
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();

        // Base iridescent gradient - teal, amethyst, magenta shifting with view angle
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        // Center bottom - glowing water source
        float centerPulse = Mth.sin(t * 0.5f) * 0.1f + 0.9f;
        buf.vertex(matrix, 0, -20, 0).color(0.2f * centerPulse, 1.0f * centerPulse, 0.9f * centerPulse, 0.9f).endVertex();

        int segs = 24;
        for (int i = 0; i <= segs; i++) {
            float angle = (float)i / segs * Mth.TWO_PI;
            float x = Mth.cos(angle) * 250;
            float z = Mth.sin(angle) * 250;
            float hue = (angle / Mth.TWO_PI + t * 0.02f) % 1f;
            float[] rgb = hsvToRgb(hue, 0.7f, 1.0f);
            // Iridescent color shim based on viewing angle
            float shim = Mth.sin(angle * 3 + t) * 0.2f;
            buf.vertex(matrix, x, 20 + shim * 10, z).color(rgb[0], rgb[1], rgb[2], 0.6f).endVertex();
        }
        tess.end();

        // God rays from below - inverted
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, -40, 0).color(1.0f, 1.0f, 1.0f, 0.5f).endVertex();
        for (int i = 0; i <= 12; i++) {
            float angle = (float)i / 12 * Mth.TWO_PI;
            float x = Mth.cos(angle) * 120;
            float z = Mth.sin(angle) * 120;
            buf.vertex(matrix, x, 0, z).color(0.5f, 0.8f, 1.0f, 0.2f).endVertex();
        }
        tess.end();

        renderFloatingParticles(poseStack, t, depth);
    }

    private void renderFloatingParticles(PoseStack poseStack, float t, float depth) {
        // Legal particles - floating, not walking, echoing
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 30; i++) {
            float px = Mth.sin(t * 0.1f + i * 1.3f) * 50;
            float py = 20 + Mth.cos(t * 0.07f + i) * 30 + i * 2;
            float pz = Mth.cos(t * 0.11f + i * 0.7f) * 50;
            float size = 0.3f + Mth.sin(t + i) * 0.2f;
            float hue = (i * 0.1f + t * 0.01f) % 1f;
            float[] rgb = hsvToRgb(hue, 0.6f, 1.0f);
            float a = 0.6f + Mth.sin(t + i) * 0.3f;

            buf.vertex(matrix, px - size, py - size, pz).color(rgb[0], rgb[1], rgb[2], a).endVertex();
            buf.vertex(matrix, px + size, py - size, pz).color(rgb[0], rgb[1], rgb[2], a).endVertex();
            buf.vertex(matrix, px + size, py + size, pz).color(rgb[0], rgb[1], rgb[2], a).endVertex();
            buf.vertex(matrix, px - size, py + size, pz).color(rgb[0], rgb[1], rgb[2], a).endVertex();
        }
        tess.end();
    }

    private float[] hsvToRgb(float h, float s, float v) {
        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        return switch (i % 6) {
            case 0 -> new float[]{v, t, p};
            case 1 -> new float[]{q, v, p};
            case 2 -> new float[]{p, v, t};
            case 3 -> new float[]{p, q, v};
            case 4 -> new float[]{t, p, v};
            default -> new float[]{v, p, q};
        };
    }

    public Vec3 getFogColor(int y, float partialTicks) {
        McsmVoidTiers.Tier tier = McsmVoidTiers.getTierForY(y);
        float t = Mth.lerp(partialTicks, prevTime, time);
        return switch (tier) {
            case TIER_1_GEL_HORIZON -> new Vec3(0.6 + Math.sin(t)*0.05, 0.85, 1.0);
            case TIER_2_MENGER_SPONGE -> new Vec3(1.0, 0.5 + Math.sin(t*0.7)*0.1, 0.4);
            case TIER_3_RIFT_FIELD -> new Vec3(0.3, 0.15, 0.5);
            case TIER_4_DISPLACEMENT -> {
                float hue = (t * 0.02f) % 1f;
                float[] rgb = hsvToRgb(hue, 0.5f, 0.8f);
                yield new Vec3(rgb[0], rgb[1], rgb[2]);
            }
            case TIER_5_IRIDESCENT_GEL -> {
                float hue = (t * 0.03f) % 1f;
                float[] rgb = hsvToRgb(hue, 0.6f, 0.9f);
                yield new Vec3(rgb[0]*0.5+0.3, rgb[1]*0.5+0.3, rgb[2]*0.5+0.4);
            }
            default -> new Vec3(0.5, 0.7, 1.0);
        };
    }

    public float getFogDensity(int y) {
        McsmVoidTiers.Tier tier = McsmVoidTiers.getTierForY(y);
        return switch (tier) {
            case TIER_1_GEL_HORIZON -> 0.02f;
            case TIER_2_MENGER_SPONGE -> 0.08f; // tighter maze
            case TIER_3_RIFT_FIELD -> 0.05f;
            case TIER_4_DISPLACEMENT -> 0.04f;
            case TIER_5_IRIDESCENT_GEL -> 0.03f; // open gel void
            default -> 0.01f;
        };
    }
}
