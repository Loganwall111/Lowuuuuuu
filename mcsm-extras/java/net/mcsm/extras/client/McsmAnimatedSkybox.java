package net.mcsm.extras.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * V2 - Animated Skyboxes Majestic Insane VFX
 * Full sky not bands - entire sky is shader-driven VFX world
 * 
 * Enhances existing side boxes in Devouring Storms (currently only bands not entire sky)
 * Now full 360 panorama cube with animated textures
 */
public final class McsmAnimatedSkybox {

    private static float time = 0f;

    // Skybox textures - 6 sides for full 360
    private static final Identifier[] SIFT_PANORAMA = new Identifier[]{
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/sift_panorama_0.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/sift_panorama_1.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/sift_panorama_2.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/sift_panorama_3.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/sift_panorama_4.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/sift_panorama_5.png")
    };

    private static final Identifier[] VOID_TIER_SKIES = new Identifier[]{
        Identifier.fromNamespaceAndPath("mcsm", "textures/sky/void_baseline_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm", "textures/sky/void_luminous_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm", "textures/sky/void_sponge_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm", "textures/sky/void_abyss_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm", "textures/sky/void_fracture_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm", "textures/sky/void_gel_sky.png")
    };

    private static final Identifier[] SIFT_TIER_SKIES = new Identifier[]{
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/fabric_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/emptiness_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/gel_horizon_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/menger_maze_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/rift_field_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/displacement_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/iridescent_gel_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/bottom_fabric_sky.png"),
        Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/unknown_sky.png")
    };

    private McsmAnimatedSkybox() {}

    public static void tick() {
        time += 0.015f;
    }

    public static void renderFullSky(PoseStack poseStack, Matrix4f projection, float partialTicks, Camera camera, int tier) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return;

        float t = Mth.lerp(partialTicks, time - 0.015f, time);

        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        // Full sky gradient - not bands, entire sky
        // God rays from above, animated
        float depthFactor = Mth.clamp(tier / 6f, 0f, 1f);

        // Sky dome - 360 coverage
        Identifier skyTex = getSkyForTier(tier, t);
        RenderSystem.setShaderTexture(0, skyTex);

        // Render sky dome with 3 layers for depth
        for (int layer = 0; layer < 3; layer++) {
            float layerDepth = 100f + layer * 20f;
            float alpha = 0.9f - layer * 0.2f;
            float hueShift = t * 0.005f + layer * 0.1f;

            buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX_COLOR);
            
            // Center top
            float[] topRgb = hsvToRgb((hueShift) % 1f, 0.6f, 1f);
            buf.vertex(matrix, 0, layerDepth, 0).uv(0.5f, 0.5f).color(topRgb[0], topRgb[1], topRgb[2], alpha).endVertex();

            int segs = 32;
            for (int i = 0; i <= segs; i++) {
                float angle = (float)i / segs * Mth.TWO_PI;
                float x = Mth.cos(angle) * layerDepth;
                float z = Mth.sin(angle) * layerDepth;
                float wave = Mth.sin(angle * 2 + t * 0.5f + layer) * 5f;
                float y = layerDepth * 0.5f + wave;

                float u = 0.5f + Mth.cos(angle) * 0.5f;
                float v = 0.5f + Mth.sin(angle) * 0.5f;

                // Animated color shift - majestic insane VFX
                float hue = (angle / Mth.TWO_PI + hueShift) % 1f;
                float[] rgb = hsvToRgb(hue, 0.7f + Mth.sin(t + angle) * 0.2f, 0.9f + Mth.sin(t * 0.3f + angle) * 0.1f);

                // God rays - bright shafts
                float godRay = (i % 4 == 0) ? 1.2f : 0.8f;
                rgb[0] *= godRay;
                rgb[1] *= godRay;
                rgb[2] *= godRay;

                buf.vertex(matrix, x, y, z).uv(u, v).color(rgb[0], rgb[1], rgb[2], alpha * 0.7f).endVertex();
            }
            tess.end();
        }

        // Side boxes - full 360 panorama cube
        // Render each of 6 sides with animated textures
        float cubeSize = 80f;
        for (int side = 0; side < 6; side++) {
            Identifier sideTex = SIFT_PANORAMA[side % SIFT_PANORAMA.length];
            RenderSystem.setShaderTexture(0, sideTex);

            float offset = Mth.sin(t * 0.2f + side) * 2f;
            float sideAlpha = 0.6f + Mth.sin(t * 0.3f + side * 0.5f) * 0.2f;

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            // Each side is a quad
            float s = cubeSize;
            switch (side) {
                case 0 -> { // top
                    buf.vertex(matrix, -s, s + offset, -s).uv(0,0).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s, s + offset, -s).uv(1,0).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s, s + offset, s).uv(1,1).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, -s, s + offset, s).uv(0,1).color(1f,1f,1f,sideAlpha).endVertex();
                }
                case 1 -> { // bottom
                    buf.vertex(matrix, -s, -s + offset, -s).uv(0,0).color(0.5f,0.5f,0.8f,sideAlpha).endVertex();
                    buf.vertex(matrix, -s, -s + offset, s).uv(0,1).color(0.5f,0.5f,0.8f,sideAlpha).endVertex();
                    buf.vertex(matrix, s, -s + offset, s).uv(1,1).color(0.5f,0.5f,0.8f,sideAlpha).endVertex();
                    buf.vertex(matrix, s, -s + offset, -s).uv(1,0).color(0.5f,0.5f,0.8f,sideAlpha).endVertex();
                }
                case 2 -> { // north
                    buf.vertex(matrix, -s, -s, -s + offset).uv(0,1).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s, -s, -s + offset).uv(1,1).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s, s, -s + offset).uv(1,0).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, -s, s, -s + offset).uv(0,0).color(1f,1f,1f,sideAlpha).endVertex();
                }
                case 3 -> { // south
                    buf.vertex(matrix, -s, -s, s + offset).uv(0,1).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, -s, s, s + offset).uv(0,0).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s, s, s + offset).uv(1,0).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s, -s, s + offset).uv(1,1).color(1f,1f,1f,sideAlpha).endVertex();
                }
                case 4 -> { // west
                    buf.vertex(matrix, -s + offset, -s, -s).uv(0,1).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, -s + offset, s, -s).uv(0,0).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, -s + offset, s, s).uv(1,0).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, -s + offset, -s, s).uv(1,1).color(1f,1f,1f,sideAlpha).endVertex();
                }
                case 5 -> { // east
                    buf.vertex(matrix, s + offset, -s, -s).uv(0,1).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s + offset, -s, s).uv(1,1).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s + offset, s, s).uv(1,0).color(1f,1f,1f,sideAlpha).endVertex();
                    buf.vertex(matrix, s + offset, s, -s).uv(0,0).color(1f,1f,1f,sideAlpha).endVertex();
                }
            }
            tess.end();
        }

        // God rays - volumetric light shafts from above
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f mat = poseStack.last().pose();
        buf.vertex(mat, 0, 120, 0).color(1f, 1f, 0.9f, 0.4f).endVertex();
        for (int i = 0; i <= 16; i++) {
            float angle = (float)i / 16 * Mth.TWO_PI;
            float x = Mth.cos(angle) * 100;
            float z = Mth.sin(angle) * 100;
            float bright = (i % 2 == 0) ? 1f : 0.5f;
            float r = 0.6f + bright * 0.4f;
            float g = 0.9f;
            float b = 1f;
            if (i % 3 == 0) { r = 0.8f; g = 0.6f; b = 1f; }
            buf.vertex(mat, x, 60, z).color(r, g, b, 0.15f * bright).endVertex();
        }
        tess.end();

        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    private static Identifier getSkyForTier(int tier, float time) {
        int idx = Mth.clamp(tier, 0, SIFT_TIER_SKIES.length - 1);
        // Animate between tiers for smooth transition
        float lerp = (time * 0.1f) % 1f;
        if (lerp < 0.5f) {
            return SIFT_TIER_SKIES[idx];
        } else {
            return SIFT_PANORAMA[idx % SIFT_PANORAMA.length];
        }
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        float r, g, b;
        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            case 5 -> { r = v; g = p; b = q; }
            default -> { r = 0; g = 0; b = 0; }
        }
        return new float[]{r, g, b};
    }

    public static float getTime() {
        return time;
    }
}
