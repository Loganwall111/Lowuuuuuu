package net.mcsm.sift.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen-Space Refraction Waves with Cosmic Interior Windows
 * Procedural rift entities - advanced GLSL displacement wave inside final.fsh
 * Outer border razor-sharp jagged, inner face ripples liquid-like wave animation
 * Cosmic Window Effect: independent parallax layer inside rippling boundary
 * Emissive Rim: full-bright glowing neon-purple and hot-magenta border
 */
public final class SiftRiftRenderer {

    public static final SiftRiftRenderer INSTANCE = new SiftRiftRenderer();

    private final List<Rift> activeRifts = new ArrayList<>();
    private final RandomSource random = RandomSource.create();

    public static class Rift {
        public Vec3 position;
        public float width;
        public float height;
        public float rotation;
        public float wavePhase;
        public float cosmicOffset;
        public float[] shapePoints; // jagged outline
        public long seed;
        public float lifeTime;
        public float maxLife = 600f; // ticks
        public boolean isOpening = true;

        public Rift(Vec3 pos, float w, float h, long seed) {
            this.position = pos;
            this.width = w;
            this.height = h;
            this.seed = seed;
            this.rotation = (seed % 360);
            this.wavePhase = (seed % 1000) * 0.01f;
            this.cosmicOffset = 0f;
            this.lifeTime = 0f;
            generateJaggedShape();
        }

        private void generateJaggedShape() {
            // Razor-sharp jagged outer border - random polygon with 8-14 points
            RandomSource r = RandomSource.create(seed);
            int points = 10 + r.nextInt(6);
            shapePoints = new float[points * 2];
            for (int i = 0; i < points; i++) {
                float angle = (float)i / points * Mth.TWO_PI;
                float radiusJitter = 0.7f + r.nextFloat() * 0.6f;
                // Make it look like torn reality
                if (i % 3 == 0) radiusJitter *= 1.3f;
                float x = Mth.cos(angle) * width * 0.5f * radiusJitter;
                float y = Mth.sin(angle) * height * 0.5f * radiusJitter;
                shapePoints[i*2] = x;
                shapePoints[i*2+1] = y;
            }
        }

        public void tick(float partial) {
            lifeTime += partial;
            wavePhase += 0.02f;
            cosmicOffset += 0.005f;

            // Opening animation - from tiny slit to full rift
            if (isOpening && lifeTime < 60f) {
                float openProgress = lifeTime / 60f;
                openProgress = 1f - (1f - openProgress) * (1f - openProgress); // ease out
                // Animate width/height
            }
        }

        public float getOpenProgress() {
            return Mth.clamp(lifeTime / 60f, 0f, 1f);
        }

        public boolean shouldRemove() {
            return lifeTime > maxLife;
        }
    }

    private SiftRiftRenderer() {}

    public void spawnRift(Vec3 pos, float size, long seed) {
        float w = size * (0.8f + random.nextFloat() * 0.6f);
        float h = size * (1.2f + random.nextFloat() * 0.8f);
        activeRifts.add(new Rift(pos, w, h, seed));
    }

    public void tick() {
        activeRifts.removeIf(Rift::shouldRemove);
        for (Rift rift : activeRifts) {
            rift.tick(1f);
        }

        // Spawn new rifts in Tier 3 and 4
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            int py = (int) mc.player.getY();
            if (McsmVoidTiers.Tier.TIER_3_RIFT_FIELD.contains(py) ||
                McsmVoidTiers.Tier.TIER_4_DISPLACEMENT.contains(py)) {
                if (random.nextFloat() < 0.02f) {
                    Vec3 spawnPos = new Vec3(
                        mc.player.getX() + (random.nextFloat() - 0.5f) * 80,
                        py + (random.nextFloat() - 0.5f) * 30,
                        mc.player.getZ() + (random.nextFloat() - 0.5f) * 80
                    );
                    spawnRift(spawnPos, 8 + random.nextFloat() * 12, random.nextLong());
                }
            }
        }
    }

    /**
     * Screen-Space Refraction Waves - called from final.fsh uniform injection
     * Returns shader uniforms for rift rendering
     */
    public float[] getRiftShaderUniforms(Rift rift, float worldTime) {
        // Outer border razor-sharp jagged, inner face ripple liquid-like wave animation loop
        float wave = Mth.sin(worldTime * 0.02f + rift.wavePhase) * 0.5f + 0.5f;
        float wave2 = Mth.sin(worldTime * 0.015f + rift.wavePhase * 1.3f) * 0.5f + 0.5f;

        // Cosmic window parallax - moving star arrays, dust fragments, dark silhouettes
        float cosmicX = Mth.sin(rift.cosmicOffset) * 0.1f;
        float cosmicY = Mth.cos(rift.cosmicOffset * 0.7f) * 0.1f;

        // Emissive rim - full-bright neon-purple and hot-magenta
        float rimPulse = 0.8f + 0.2f * Mth.sin(worldTime * 0.05f + rift.wavePhase);

        return new float[]{
            wave, wave2, // displacement wave
            cosmicX, cosmicY, // parallax offset
            rimPulse,
            rift.getOpenProgress(),
            rift.width, rift.height
        };
    }

    /**
     * Renders rift as screen-space quad with custom shader
     * This is called from world renderer mixin
     */
    public void renderRifts(PoseStack poseStack, Matrix4f projectionMatrix, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || activeRifts.isEmpty()) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        for (Rift rift : activeRifts) {
            renderSingleRift(poseStack, rift, partialTicks);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void renderSingleRift(PoseStack poseStack, Rift rift, float partialTicks) {
        // Project world pos to screen - simplified
        // In real implementation, use WorldRenderer frustum culling and transform
        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 relative = rift.position.subtract(camPos);

        // Distance fade
        double dist = relative.length();
        if (dist > 128) return;
        float alpha = (float) (1.0 - dist / 128.0);

        poseStack.pushPose();
        poseStack.translate(relative.x, relative.y, relative.z);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rift.rotation));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rift.wavePhase * 10));

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.getBuilder();

        // Render cosmic window interior first
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

        float open = rift.getOpenProgress();
        float w = rift.width * open;
        float h = rift.height * open;

        // Inner cosmic window - iridescent starfield
        float time = (mc.level != null ? mc.level.getGameTime() : 0) + partialTicks;

        // Create triangulated jagged shape for interior
        for (int i = 0; i < rift.shapePoints.length / 2 - 1; i++) {
            float x1 = rift.shapePoints[i*2] * open;
            float y1 = rift.shapePoints[i*2+1] * open;
            float x2 = rift.shapePoints[(i+1)*2] * open;
            float y2 = rift.shapePoints[(i+1)*2+1] * open;

            // Center point
            buffer.vertex(matrix, 0, 0, 0).uv(0.5f, 0.5f).color(0.3f, 0.1f, 0.8f, alpha * 0.9f).endVertex();
            buffer.vertex(matrix, x1, y1, 0).uv(0, 0).color(0.6f, 0.2f, 1f, alpha * 0.7f).endVertex();
            buffer.vertex(matrix, x2, y2, 0).uv(1, 1).color(0.2f, 0.5f, 1f, alpha * 0.7f).endVertex();
        }

        tess.end();

        // Render emissive rim - glowing neon-purple and hot-magenta border frame
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < rift.shapePoints.length / 2; i++) {
            float x = rift.shapePoints[i*2] * open;
            float y = rift.shapePoints[i*2+1] * open;
            float pulse = 0.8f + 0.2f * Mth.sin(time * 0.1f + i);
            // Neon-purple (0.8, 0.2, 1.0) to hot-magenta (1.0, 0.2, 0.6)
            float r = 0.8f + 0.2f * pulse;
            float g = 0.2f;
            float b = 0.6f + 0.4f * pulse;
            buffer.vertex(matrix, x, y, 0.01f).color(r, g, b, alpha).endVertex();
        }
        // Close loop
        if (rift.shapePoints.length >= 2) {
            float x = rift.shapePoints[0] * open;
            float y = rift.shapePoints[1] * open;
            buffer.vertex(matrix, x, y, 0.01f).color(0.8f, 0.2f, 1f, alpha).endVertex();
        }
        tess.end();

        poseStack.popPose();
    }

    public List<Rift> getActiveRifts() {
        return activeRifts;
    }

    public void clear() {
        activeRifts.clear();
    }
}
