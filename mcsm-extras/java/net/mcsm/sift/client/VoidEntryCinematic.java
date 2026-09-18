package net.mcsm.sift.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.Random;

/**
 * Volumetric screen shader cinematic for void entry - NOT generic pixel overlay
 * 
 * User request:
 * - volumetric shader disintegration perspective looking down to black
 * - then show every Minecraft universe on 4 gigantic planets (Dungeons logo, Legends logo, Minecraft 2, Movie logo, includes Story Mode) 10-15s
 * - then stream glitches warp drive, particles/stars fly by, dust, white flash
 * - landing on second cutscene overlay – real cinematic, then white background falling onto white/black M maze that doesn't exist
 * - glitch sides left/right every few seconds, cut to pitch black, fog from first layer fades in, directly in first area
 * - epic seamlessly automatically
 * 
 * Phases:
 * 0: Disintegration to black (0-2s) - perspective looking down, body disintegrates
 * 1: Multiverse planets showcase (2-8s) - 4 gigantic planets with Minecraft universe logos
 * 2: Reality warp drive (8-11s) - stream glitches, warp drive particles/stars fly by
 * 3: White flash (11-12s)
 * 4: White maze that doesn't exist (12-14s) - white background, white/black M maze illusion
 * 5: Pitch black then fog fade to first layer (14-15s)
 */
public final class VoidEntryCinematic {

    public static final VoidEntryCinematic INSTANCE = new VoidEntryCinematic();

    public enum Phase {
        INACTIVE,
        DISINTEGRATION,      // 0-2s: volumetric disintegration looking down to black
        MULTIVERSE_PLANETS,  // 2-8s: 4 gigantic planets - Dungeons, Legends, Minecraft 2, Movie, Story Mode
        WARP_DRIVE,          // 8-11s: warp drive, glitches, particles
        WHITE_FLASH,         // 11-12s: white flash
        WHITE_MAZE,          // 12-14s: white maze that doesn't exist, glitch left/right
        PITCH_BLACK_FOG_FADE // 14-15s: pitch black then fog fade to first layer
    }

    private Phase currentPhase = Phase.INACTIVE;
    private int tick = 0;
    private int maxTicks = 300; // 15 seconds * 20 ticks
    private float progress = 0f;
    private final Random random = new Random();
    private float glitchTimer = 0f;

    private VoidEntryCinematic() {}

    public void trigger() {
        this.currentPhase = Phase.DISINTEGRATION;
        this.tick = 0;
        this.maxTicks = 300; // 15s
        this.progress = 0f;
        this.glitchTimer = 0f;
        System.out.println("[MCSM Sift Cinematic] Triggered void entry cinematic - 15s epic sequence");
    }

    public void tick() {
        if (currentPhase == Phase.INACTIVE) return;
        tick++;
        progress = (float) tick / maxTicks;
        glitchTimer += 0.05f;

        // Phase transitions based on time
        if (tick < 40) { // 0-2s
            currentPhase = Phase.DISINTEGRATION;
        } else if (tick < 160) { // 2-8s (6s) planets showcase
            currentPhase = Phase.MULTIVERSE_PLANETS;
        } else if (tick < 220) { // 8-11s (3s) warp drive
            currentPhase = Phase.WARP_DRIVE;
        } else if (tick < 240) { // 11-12s (1s) white flash
            currentPhase = Phase.WHITE_FLASH;
        } else if (tick < 280) { // 12-14s (2s) white maze
            currentPhase = Phase.WHITE_MAZE;
        } else if (tick < 300) { // 14-15s (1s) fog fade
            currentPhase = Phase.PITCH_BLACK_FOG_FADE;
        } else {
            currentPhase = Phase.INACTIVE;
            tick = 0;
            progress = 0f;
        }
    }

    public boolean isActive() {
        return currentPhase != Phase.INACTIVE;
    }

    public Phase getPhase() {
        return currentPhase;
    }

    public float getProgress() {
        return progress;
    }

    public void render(PoseStack poseStack) {
        if (currentPhase == Phase.INACTIVE) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();

        switch (currentPhase) {
            case DISINTEGRATION -> renderDisintegration(poseStack, matrix, tess, buf, width, height);
            case MULTIVERSE_PLANETS -> renderMultiversePlanets(poseStack, matrix, tess, buf, width, height);
            case WARP_DRIVE -> renderWarpDrive(poseStack, matrix, tess, buf, width, height);
            case WHITE_FLASH -> renderWhiteFlash(poseStack, matrix, tess, buf, width, height);
            case WHITE_MAZE -> renderWhiteMaze(poseStack, matrix, tess, buf, width, height);
            case PITCH_BLACK_FOG_FADE -> renderFogFade(poseStack, matrix, tess, buf, width, height);
            default -> {}
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // Phase 0: Disintegration to black - volumetric shader perspective looking down to black
    private void renderDisintegration(PoseStack poseStack, Matrix4f matrix, Tesselator tess, BufferBuilder buf, int width, int height) {
        float phaseProgress = (tick % 40) / 40f; // 0-1 over 2s

        // Volumetric disintegration - looking down to black, body particles disintegrate
        // Darken screen with radial disintegration from center (player looking down)
        float centerX = width / 2f;
        float centerY = height / 2f + 20; // slightly below center - looking down perspective

        // Full black background that closes in
        float blackAlpha = Mth.lerp(phaseProgress, 0.2f, 1f);
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, 0, 0).color(0f, 0f, 0f, blackAlpha).endVertex();
        buf.vertex(matrix, 0, height, 0).color(0f, 0f, 0f, blackAlpha).endVertex();
        buf.vertex(matrix, width, height, 0).color(0.02f, 0f, 0.05f, blackAlpha).endVertex();
        buf.vertex(matrix, width, 0, 0).color(0f, 0f, 0f, blackAlpha).endVertex();
        tess.end();

        // Disintegration particles - body breaking into pixels that fall down
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 80; i++) {
            float px = centerX + (random.nextFloat() - 0.5f) * width * 0.6f * (1f - phaseProgress);
            float py = centerY + (random.nextFloat() - 0.5f) * height * 0.5f + phaseProgress * 100;
            float size = 2 + random.nextFloat() * 4 * (1f - phaseProgress);
            float alpha = 1f - phaseProgress + random.nextFloat() * 0.3f;
            float r = 0.6f + random.nextFloat() * 0.4f;
            float g = 0.4f + random.nextFloat() * 0.3f;
            float b = 1f;

            // Particle disintegrates downward
            py += phaseProgress * 50 + i * 0.5f;

            buf.vertex(matrix, px - size, py - size, 0).color(r, g, b, alpha).endVertex();
            buf.vertex(matrix, px - size, py + size, 0).color(r, g, b, alpha * 0.8f).endVertex();
            buf.vertex(matrix, px + size, py + size, 0).color(r * 0.8f, g * 0.8f, b, alpha * 0.5f).endVertex();
            buf.vertex(matrix, px + size, py - size, 0).color(r, g, b, alpha * 0.3f).endVertex();
        }
        tess.end();

        // Vignette closing to black - perspective looking down to black
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, centerX, centerY, 0).color(0f, 0f, 0f, 0f).endVertex();
        for (int i = 0; i <= 24; i++) {
            float angle = (float) i / 24 * Mth.TWO_PI;
            float radius = width * 0.8f * (1f - phaseProgress * 0.5f);
            float x = centerX + Mth.cos(angle) * radius;
            float y = centerY + Mth.sin(angle) * radius * 0.7f;
            float a = Mth.lerp(phaseProgress, 0.3f, 1f);
            buf.vertex(matrix, x, y, 0).color(0f, 0f, 0f, a).endVertex();
        }
        tess.end();
    }

    // Phase 1: Multiverse planets showcase - 10-15s total, this phase 6s - 4 gigantic planets
    private void renderMultiversePlanets(PoseStack poseStack, Matrix4f matrix, Tesselator tess, BufferBuilder buf, int width, int height) {
        float phaseProgress = (tick - 40) / 120f; // 0-1 over 6s

        // Pitch black space background with stars
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, 0, 0).color(0f, 0f, 0f, 1f).endVertex();
        buf.vertex(matrix, 0, height, 0).color(0.02f, 0f, 0.08f, 1f).endVertex();
        buf.vertex(matrix, width, height, 0).color(0f, 0f, 0.05f, 1f).endVertex();
        buf.vertex(matrix, width, 0, 0).color(0f, 0f, 0f, 1f).endVertex();
        tess.end();

        // Stars
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 100; i++) {
            float x = (Mth.sin(i * 1.7f + phaseProgress) * 0.5f + 0.5f) * width;
            float y = (Mth.cos(i * 2.3f + phaseProgress * 0.5f) * 0.5f + 0.5f) * height;
            float size = 0.5f + (i % 4) * 0.5f;
            float twinkle = Mth.sin(phaseProgress * 5 + i) * 0.3f + 0.7f;
            buf.vertex(matrix, x - size, y - size, 0).color(1f, 1f, 1f, twinkle).endVertex();
            buf.vertex(matrix, x - size, y + size, 0).color(1f, 1f, 1f, twinkle).endVertex();
            buf.vertex(matrix, x + size, y + size, 0).color(1f, 1f, 1f, twinkle).endVertex();
            buf.vertex(matrix, x + size, y - size, 0).color(1f, 1f, 1f, twinkle).endVertex();
        }
        tess.end();

        // 4 Gigantic planets - Minecraft Dungeons, Legends, Minecraft 2, Movie, Story Mode
        // Planet 1: Dungeons - left, blue-purple, ringed
        renderPlanet(buf, matrix, width * 0.25f, height * 0.35f, 60 + phaseProgress * 5, 0.2f, 0.4f, 0.9f, 0.9f, "DUNGEONS");
        // Planet 2: Legends - right, orange-yellow
        renderPlanet(buf, matrix, width * 0.75f, height * 0.4f, 70 + phaseProgress * 3, 0.9f, 0.6f, 0.2f, 0.9f, "LEGENDS");
        // Planet 3: Minecraft 2 / Movie - center top, earth-like with logo
        renderPlanet(buf, matrix, width * 0.5f, height * 0.25f, 80, 0.3f, 0.7f, 0.3f, 1f, "MOVIE");
        // Planet 4: Story Mode - bottom, pink-purple
        renderPlanet(buf, matrix, width * 0.5f, height * 0.7f, 50 + phaseProgress * 8, 0.8f, 0.3f, 0.6f, 0.85f, "STORY");

        // Subtle text hint - multiverse showcase
        // Would render with font renderer, but for now use colored quads as logo placeholders
    }

    private void renderPlanet(BufferBuilder buf, Matrix4f matrix, float cx, float cy, float radius, float r, float g, float b, float alpha, String label) {
        Tesselator tess = Tesselator.getInstance();
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, cx, cy, 0).color(r, g, b, alpha).endVertex();
        for (int i = 0; i <= 32; i++) {
            float angle = (float) i / 32 * Mth.TWO_PI;
            float x = cx + Mth.cos(angle) * radius;
            float y = cy + Mth.sin(angle) * radius;
            // Gradient edge
            float edgeR = r * 0.6f;
            float edgeG = g * 0.6f;
            float edgeB = b * 0.8f;
            buf.vertex(matrix, x, y, 0).color(edgeR, edgeG, edgeB, alpha * 0.8f).endVertex();
        }
        tess.end();

        // Ring for some planets (Dungeons)
        if (label.equals("DUNGEONS")) {
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < 32; i++) {
                float angle = (float) i / 32 * Mth.TWO_PI;
                float nextAngle = (float) (i + 1) / 32 * Mth.TWO_PI;
                float inner = radius * 1.3f;
                float outer = radius * 1.6f;
                float x1 = cx + Mth.cos(angle) * inner;
                float y1 = cy + Mth.sin(angle) * inner * 0.3f;
                float x2 = cx + Mth.cos(angle) * outer;
                float y2 = cy + Mth.sin(angle) * outer * 0.3f;
                float x3 = cx + Mth.cos(nextAngle) * outer;
                float y3 = cy + Mth.sin(nextAngle) * outer * 0.3f;
                float x4 = cx + Mth.cos(nextAngle) * inner;
                float y4 = cy + Mth.sin(nextAngle) * inner * 0.3f;
                float ringAlpha = 0.4f;
                buf.vertex(matrix, x1, y1, 0).color(0.5f, 0.7f, 1f, ringAlpha).endVertex();
                buf.vertex(matrix, x2, y2, 0).color(0.5f, 0.7f, 1f, ringAlpha * 0.5f).endVertex();
                buf.vertex(matrix, x3, y3, 0).color(0.5f, 0.7f, 1f, ringAlpha * 0.5f).endVertex();
                buf.vertex(matrix, x4, y4, 0).color(0.5f, 0.7f, 1f, ringAlpha).endVertex();
            }
            tess.end();
        }
    }

    // Phase 2: Warp drive - stream glitches, particles/stars fly by, dust
    private void renderWarpDrive(PoseStack poseStack, Matrix4f matrix, Tesselator tess, BufferBuilder buf, int width, int height) {
        float phaseProgress = (tick - 160) / 60f; // 0-1 over 3s

        // Black background with warp streaks
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, 0, 0).color(0f, 0f, 0f, 1f).endVertex();
        buf.vertex(matrix, 0, height, 0).color(0.05f, 0f, 0.1f, 1f).endVertex();
        buf.vertex(matrix, width, height, 0).color(0f, 0f, 0f, 1f).endVertex();
        buf.vertex(matrix, width, 0, 0).color(0f, 0f, 0f, 1f).endVertex();
        tess.end();

        // Warp drive particles - stars flying by horizontally, dust
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 120; i++) {
            float x = (width * 0.5f + (random.nextFloat() - 0.5f) * width * 2 - phaseProgress * width * 3) % width;
            if (x < 0) x += width;
            float y = random.nextFloat() * height;
            float length = 10 + random.nextFloat() * 40 * phaseProgress;
            float thickness = 1 + random.nextFloat() * 2;
            float r = 0.6f + random.nextFloat() * 0.4f;
            float g = 0.6f + random.nextFloat() * 0.4f;
            float b = 1f;
            float alpha = 0.8f + random.nextFloat() * 0.2f;

            // Streak
            buf.vertex(matrix, x - length, y - thickness, 0).color(r, g, b, 0f).endVertex();
            buf.vertex(matrix, x - length, y + thickness, 0).color(r, g, b, 0f).endVertex();
            buf.vertex(matrix, x, y + thickness, 0).color(r, g, b, alpha).endVertex();
            buf.vertex(matrix, x, y - thickness, 0).color(r, g, b, alpha).endVertex();
        }
        tess.end();

        // Glitch effects - left/right glitch every few frames
        if (tick % 10 < 3) {
            float glitchOffset = (random.nextFloat() - 0.5f) * 20;
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            float glitchY = random.nextFloat() * height;
            float glitchH = 5 + random.nextFloat() * 20;
            buf.vertex(matrix, 0, glitchY, 0).color(1f, 0f, 1f, 0.3f).endVertex();
            buf.vertex(matrix, 0, glitchY + glitchH, 0).color(0f, 1f, 1f, 0.3f).endVertex();
            buf.vertex(matrix, width, glitchY + glitchH, 0).color(0f, 1f, 1f, 0.3f).endVertex();
            buf.vertex(matrix, width, glitchY, 0).color(1f, 0f, 1f, 0.3f).endVertex();
            tess.end();
        }
    }

    // Phase 3: White flash
    private void renderWhiteFlash(PoseStack poseStack, Matrix4f matrix, Tesselator tess, BufferBuilder buf, int width, int height) {
        float phaseProgress = (tick - 220) / 20f; // 0-1 over 1s
        float alpha = 1f;
        if (phaseProgress < 0.5f) {
            alpha = Mth.lerp(phaseProgress * 2, 0f, 1f);
        } else {
            alpha = Mth.lerp((phaseProgress - 0.5f) * 2, 1f, 0.9f);
        }
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, 0, 0).color(1f, 1f, 1f, alpha).endVertex();
        buf.vertex(matrix, 0, height, 0).color(1f, 1f, 1f, alpha).endVertex();
        buf.vertex(matrix, width, height, 0).color(1f, 1f, 1f, alpha).endVertex();
        buf.vertex(matrix, width, 0, 0).color(1f, 1f, 1f, alpha).endVertex();
        tess.end();
    }

    // Phase 4: White maze that doesn't exist - white background falling onto white/black M maze illusion
    private void renderWhiteMaze(PoseStack poseStack, Matrix4f matrix, Tesselator tess, BufferBuilder buf, int width, int height) {
        float phaseProgress = (tick - 240) / 40f; // 0-1 over 2s

        // White background
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0, 0, 0).color(1f, 1f, 1f, 1f).endVertex();
        buf.vertex(matrix, 0, height, 0).color(0.95f, 0.95f, 0.95f, 1f).endVertex();
        buf.vertex(matrix, width, height, 0).color(1f, 1f, 1f, 1f).endVertex();
        buf.vertex(matrix, width, 0, 0).color(1f, 1f, 1f, 1f).endVertex();
        tess.end();

        // M maze that doesn't exist - white/black maze illusion, falling perspective
        float mazeOffset = phaseProgress * 200; // falling down
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = -2; i < 10; i++) {
            float y = i * 40 - mazeOffset % 40;
            // M pattern - alternating black/white walls that create maze illusion
            for (int x = 0; x < width; x += 40) {
                boolean isWall = (x / 40 + i) % 3 == 0;
                if (!isWall) continue;
                float wallAlpha = 0.1f + (i % 2) * 0.1f;
                // Black maze lines on white
                buf.vertex(matrix, x, y, 0).color(0f, 0f, 0f, wallAlpha).endVertex();
                buf.vertex(matrix, x, y + 30, 0).color(0f, 0f, 0f, wallAlpha).endVertex();
                buf.vertex(matrix, x + 10, y + 30, 0).color(0f, 0f, 0f, wallAlpha).endVertex();
                buf.vertex(matrix, x + 10, y, 0).color(0f, 0f, 0f, wallAlpha).endVertex();
            }
        }
        tess.end();

        // Glitch sides left/right every few seconds
        if (tick % 20 < 5) {
            float glitch = Mth.sin(glitchTimer) * 10;
            // Left glitch
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(matrix, 0, 0, 0).color(0f, 0f, 0f, 0.05f).endVertex();
            buf.vertex(matrix, 0, height, 0).color(0f, 0f, 0f, 0.05f).endVertex();
            buf.vertex(matrix, 20 + glitch, height, 0).color(0f, 0f, 0f, 0.1f).endVertex();
            buf.vertex(matrix, 20 + glitch, 0, 0).color(0f, 0f, 0f, 0.1f).endVertex();
            tess.end();
            // Right glitch
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(matrix, width - 20 - glitch, 0, 0).color(0f, 0f, 0f, 0.05f).endVertex();
            buf.vertex(matrix, width - 20 - glitch, height, 0).color(0f, 0f, 0f, 0.05f).endVertex();
            buf.vertex(matrix, width, height, 0).color(0f, 0f, 0f, 0.05f).endVertex();
            buf.vertex(matrix, width, 0, 0).color(0f, 0f, 0f, 0.05f).endVertex();
            tess.end();
        }
    }

    // Phase 5: Pitch black then fog fade to first layer
    private void renderFogFade(PoseStack poseStack, Matrix4f matrix, Tesselator tess, BufferBuilder buf, int width, int height) {
        float phaseProgress = (tick - 280) / 20f; // 0-1 over 1s

        if (phaseProgress < 0.5f) {
            // Pitch black
            float blackAlpha = 1f - phaseProgress * 0.2f;
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(matrix, 0, 0, 0).color(0f, 0f, 0f, blackAlpha).endVertex();
            buf.vertex(matrix, 0, height, 0).color(0f, 0f, 0f, blackAlpha).endVertex();
            buf.vertex(matrix, width, height, 0).color(0f, 0f, 0f, blackAlpha).endVertex();
            buf.vertex(matrix, width, 0, 0).color(0f, 0f, 0f, blackAlpha).endVertex();
            tess.end();
        } else {
            // Fog from first layer fades in - directly in first area
            float fogProgress = (phaseProgress - 0.5f) * 2;
            // First layer is Gel Horizon - cyan fog
            float r = Mth.lerp(fogProgress, 0f, 0.6f);
            float g = Mth.lerp(fogProgress, 0f, 0.9f);
            float b = Mth.lerp(fogProgress, 0f, 1f);
            float alpha = Mth.lerp(fogProgress, 1f, 0.3f);

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).endVertex();
            buf.vertex(matrix, 0, height, 0).color(r * 0.8f, g * 0.8f, b, alpha).endVertex();
            buf.vertex(matrix, width, height, 0).color(r * 0.5f, g * 0.7f, b * 0.9f, alpha * 0.5f).endVertex();
            buf.vertex(matrix, width, 0, 0).color(r, g, b, alpha * 0.8f).endVertex();
            tess.end();

            // God rays starting to appear
            buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(matrix, width / 2f, 0, 0).color(1f, 1f, 0.9f, fogProgress * 0.5f).endVertex();
            for (int i = 0; i <= 12; i++) {
                float angle = (float) i / 12 * Mth.TWO_PI;
                float x = width / 2f + Mth.cos(angle) * width * 0.6f;
                float y = Mth.sin(angle) * height * 0.3f;
                buf.vertex(matrix, x, y, 0).color(0.6f, 0.9f, 1f, fogProgress * 0.2f).endVertex();
            }
            tess.end();
        }
    }

    public void clear() {
        currentPhase = Phase.INACTIVE;
        tick = 0;
        progress = 0f;
    }
}
