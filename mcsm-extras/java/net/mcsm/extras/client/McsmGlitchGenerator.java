package net.mcsm.extras.client;

import net.minecraft.util.Mth;

/**
 * BUILD #493 – Procedural Void-Glitch Generator & Reality Mutation Engine
 *
 * Primary Development Base Line: anchor on stable 264/264 gate-checked V2 framework
 * Branch: arena/01a0b039-lowuuuuuu
 *
 * Core Mandate: specialized procedural generation algorithm within McsmNinthLayerGeometry
 * to force 3D meshes, needle spires, landscape structures to warp, glitch, randomly mutate
 * vertex math vectors deeper player plummets.
 *
 * Phase 1:
 * 1. Dynamic Mesh Displacements: low-level vertex modulation loop inside 3D Scene-Graph pipeline
 *    Feed GameTime and live FallDistance into high-frequency sine/cosine noise math matrix.
 * 2. Procedural Spire & Mountain Mutation: push, pull, warp 3D spire vertices on the fly.
 *    As sink into deep negative coords, neat geometric shapes glitch out – tearing into
 *    impossible organic fractal pillars, overlapping wireframes, reality-shattered cuts that morph continuously.
 * 3. Weld Reality-Glitched Chunk Loader: background thread to dynamically shuffle structure generation seeds
 *    based on player's depth tier, ensuring no two infinite falls ever generate same visual layout.
 *
 * This class provides low-level noise and displacement math used by NinthLayerGeometry,
 * CreatorSkybox, VoidVortexSkybox.
 */
public final class McsmGlitchGenerator {

    private McsmGlitchGenerator() {}

    // ---- Depth tiers for mutation intensity ---------------------------------
    public static final double GEL_TIER = -60.0;
    public static final double ABYSSAL_TIER = -180.0;
    public static final double NIGHTMARE_TIER = -500.0;
    public static final double UNINPOSSIBLE_TIER = -1200.0;
    public static final double REALITY_BREAK_TIER = -2032.0;

    // ---- Seed shuffling for never-ending possibility ------------------------
    private static long globalSeed = 0x5EEDBEEF12345678L;
    private static long lastDepthTier = 0;
    private static float chunkShufflePhase = 0.0F;

    /**
     * Feed GameTime and live FallDistance into high-frequency sine/cosine noise matrix.
     * EXTREME – NOT MILD: glitch factor boosted, more spikes, faster.
     * Returns glitch factor 0..1 based on depth and time.
     */
    public static float computeGlitchFactor(double fallDistance, double playerY, double gameTime) {
        // Depth factor: deeper = more glitch – EXTREME, starts earlier, stronger
        float depthFactor = 0.0F;
        if (playerY <= REALITY_BREAK_TIER) depthFactor = 1.0F;
        else if (playerY <= UNINPOSSIBLE_TIER) depthFactor = 0.85F + 0.15F * (float)((UNINPOSSIBLE_TIER - playerY) / (UNINPOSSIBLE_TIER - REALITY_BREAK_TIER));
        else if (playerY <= NIGHTMARE_TIER) depthFactor = 0.65F + 0.35F * (float)((NIGHTMARE_TIER - playerY) / (NIGHTMARE_TIER - UNINPOSSIBLE_TIER)); // was 0.55+0.30
        else if (playerY <= ABYSSAL_TIER) depthFactor = 0.35F + 0.35F * (float)((ABYSSAL_TIER - playerY) / (ABYSSAL_TIER - NIGHTMARE_TIER)); // was 0.25+0.30
        else if (playerY <= GEL_TIER) depthFactor = 0.15F + 0.30F * (float)((GEL_TIER - playerY) / (GEL_TIER - ABYSSAL_TIER)); // was 0.05+0.20
        else depthFactor = 0.05F; // EXTREME even above gel slight glitch, was 0.0

        // Fall distance amplifies glitch – faster fall = more tear – EXTREME boost
        float fallFactor = Mth.clamp((float)(fallDistance / 60.0), 0.0F, 1.0F); // was /100

        // High-frequency sine/cosine noise matrix driven by GameTime – EXTREME more octaves
        double t = gameTime * 0.08; // was 0.05 – faster
        float noise = 0.0F;
        noise += Mth.sin((float)(t * 1.0)) * 0.4F;
        noise += Mth.sin((float)(t * 2.3 + fallDistance * 0.02)) * 0.25F; // was 0.01
        noise += Mth.sin((float)(t * 4.7 + playerY * 0.01)) * 0.15F; // was 0.005 and 0.125
        noise += Mth.sin((float)(t * 9.1 + fallDistance * 0.04)) * 0.1F; // was 0.02 and 0.0625
        noise += Mth.sin((float)(t * 17.3 + playerY * 0.02)) * 0.05F; // NEW octave
        noise += Mth.sin((float)(t * 31.1 + fallDistance * 0.08)) * 0.05F; // NEW octave
        noise = (noise + 1.0F) * 0.5F; // 0..1

        // Combine: depth * fall * noise with spikes – EXTREME stronger
        float glitch = depthFactor * (0.7F + 0.5F * fallFactor) * (0.6F + 0.6F * noise); // was 0.6+0.4 and 0.5+0.5

        // Major spikes every few seconds – when mesh undergoes major warp – EXTREME more frequent
        float spike = 0.0F;
        double spikePhase = gameTime * 0.008; // was 0.003 – more frequent
        if (Mth.sin((float)spikePhase) > 0.65F) { // was 0.85 – more frequent
            spike = (Mth.sin((float)spikePhase) - 0.65F) / 0.35F; // 0..1 spike
        }

        return Mth.clamp(glitch + spike * depthFactor * 1.2F, 0.0F, 1.0F); // was 0.8
    }

    /**
     * Dynamic mesh displacement: push, pull, warp vertex on the fly.
     * EXTREME – NOT MILD: displacement x3, more tearing, more warping.
     * Returns displaced position.
     */
    public static double[] displaceVertex(double x, double y, double z, double gameTime, double fallDistance, float glitchFactor, int vertexIndex) {
        double t = gameTime * 0.01;
        double depthWarp = glitchFactor * 8.0; // EXTREME was 3.0

        // High-frequency noise matrix – EXTREME more octaves
        double nx = Math.sin(x * 0.15 + t * 1.8 + vertexIndex * 0.9) * Math.cos(y * 0.12 + t * 1.4) * depthWarp;
        double ny = Math.cos(y * 0.18 + t * 1.6 + vertexIndex * 1.3) * Math.sin(z * 0.14 + t * 2.0) * depthWarp;
        double nz = Math.sin(z * 0.16 + t * 2.2 + vertexIndex * 0.7) * Math.cos(x * 0.13 + t * 1.2) * depthWarp;

        nx += Math.sin(x * 0.35 + t * 3.5) * Math.cos(y * 0.25) * depthWarp * 0.5;
        ny += Math.cos(y * 0.32 + t * 3.1) * Math.sin(z * 0.28) * depthWarp * 0.5;
        nz += Math.sin(z * 0.30 + t * 3.8) * Math.cos(x * 0.22) * depthWarp * 0.5;

        // Fractal tearing – as deeper, vertices tear into impossible organic fractal pillars – EXTREME
        if (glitchFactor > 0.3F) { // EXTREME lower threshold (was 0.5)
            double fractal = fractalNoise(x * 0.08, y * 0.08 + t, z * 0.08);
            nx += fractal * glitchFactor * 20.0; // EXTREME was 8.0
            ny += fractal * glitchFactor * 30.0; // EXTREME was 12.0
            nz += fractal * glitchFactor * 20.0;

            // Overlapping wireframes – duplicate vertices with offset – EXTREME more frequent
            if (glitchFactor > 0.5F && (vertexIndex % 2 == 0)) { // was 0.75 and %3
                nx += Math.sin(t * 8.0 + vertexIndex) * glitchFactor * 10.0; // EXTREME was 4.0
                nz += Math.cos(t * 8.0 + vertexIndex) * glitchFactor * 10.0;
            }

            // Reality-shattered cuts – sudden jumps – EXTREME more frequent and larger
            if (glitchFactor > 0.6F && Mth.sin((float)(t * 15.0 + vertexIndex)) > 0.7F) { // was 0.85 and 0.9
                nx += (Math.random() - 0.5) * 50.0 * glitchFactor; // EXTREME was 20
                ny += (Math.random() - 0.5) * 80.0 * glitchFactor; // EXTREME was 30
                nz += (Math.random() - 0.5) * 50.0 * glitchFactor;
            }
        }

        // Fall distance stretches vertices vertically – like falling through ripping fabric – EXTREME
        double fallStretch = fallDistance * 0.06 * glitchFactor; // was 0.02
        ny -= fallStretch * Math.sin(t + x * 0.02);

        return new double[]{x + nx, y + ny, z + nz};
    }

    /**
     * Procedural spire & mountain mutation: push, pull, warp spire vertices.
     * Returns mutated height and radius.
     */
    public static double[] mutateSpire(double baseHeight, double baseRadius, double gameTime, double playerY, int spireIndex) {
        float depthFactor = computeGlitchFactor(0, playerY, gameTime);
        double t = gameTime * 0.002;

        double heightMut = baseHeight;
        double radiusMut = baseRadius;

        // As deeper, spires glitch into fractal pillars
        if (depthFactor > 0.3F) {
            heightMut += Math.sin(t * 1.5 + spireIndex * 1.7) * depthFactor * 30.0;
            heightMut += fractalNoise(spireIndex * 2.0, t * 3.0) * depthFactor * 50.0;
            radiusMut += Math.cos(t * 2.1 + spireIndex * 0.9) * depthFactor * 8.0;
            radiusMut += fractalNoise(spireIndex * 1.3, t * 2.5) * depthFactor * 12.0;

            // Organic fractal pillars – irregular, not neat geometric
            if (depthFactor > 0.6F) {
                heightMut *= 1.0 + fractalNoise(spireIndex * 0.7, t * 1.2) * depthFactor * 0.8;
                radiusMut *= 1.0 + Math.sin(t * 3.0 + spireIndex) * depthFactor * 0.5;
            }

            // Reality-shattered cuts – spire tears open
            if (depthFactor > 0.8F && Math.sin(t * 5.0 + spireIndex * 2.0) > 0.7) {
                heightMut += (Math.random() * 100.0 - 50.0) * depthFactor;
            }
        }

        return new double[]{heightMut, radiusMut};
    }

    /**
     * Weld reality-glitched chunk loader: shuffle seeds based on depth tier.
     * Ensures no two infinite falls generate same visual layout.
     */
    public static long shuffleSeedForDepth(double playerY, long baseSeed) {
        long tier = 0;
        if (playerY <= REALITY_BREAK_TIER) tier = 4;
        else if (playerY <= UNINPOSSIBLE_TIER) tier = 3;
        else if (playerY <= NIGHTMARE_TIER) tier = 2;
        else if (playerY <= ABYSSAL_TIER) tier = 1;
        else tier = 0;

        if (tier != lastDepthTier) {
            // Depth tier changed – shuffle global seed
            globalSeed ^= (baseSeed + tier * 0x9E3779B97F4A7C15L) * 0xBF58476D1CE4E5B9L;
            globalSeed = Long.rotateLeft(globalSeed, (int)(tier * 7 + 3));
            lastDepthTier = tier;
            chunkShufflePhase += 0.5F;
        }

        // Continuous shuffle based on time
        long timeSeed = (long)(System.currentTimeMillis() * 0.001) & 0xFFFF;
        return globalSeed ^ baseSeed ^ timeSeed ^ (tier * 0x123456789ABCDEFL);
    }

    public static float getChunkShufflePhase() {
        return chunkShufflePhase;
    }

    // ---- Noise helpers ----------------------------------------------------

    public static double fractalNoise(double x, double y) {
        return fractalNoise(x, y, 0.0);
    }

    public static double fractalNoise(double x, double y, double z) {
        double n = 0.0;
        n += Math.sin(x * 1.0 + y * 0.7 + z * 0.3) * 0.5;
        n += Math.sin(x * 2.3 - y * 1.1 + z * 0.8) * 0.25;
        n += Math.sin(x * 4.7 + y * 2.3 - z * 1.2) * 0.125;
        n += Math.sin(x * 9.1 - y * 3.7 + z * 2.1) * 0.0625;
        n += Math.sin(x * 17.3 + y * 7.1 - z * 4.3) * 0.03125;
        return n;
    }

    public static float computeEyeBloom(float baseBloom, float glitchFactor) {
        // Blinding Gaze Warp: when geometry mutates violently, eyes flare to 6.0x bloom
        if (glitchFactor > 0.7F) {
            float spike = (glitchFactor - 0.7F) / 0.3F; // 0..1
            return baseBloom + spike * (6.0F - baseBloom) + Mth.sin((float)(System.currentTimeMillis() * 0.01)) * spike * 0.5F;
        }
        return baseBloom;
    }

    public static boolean isMajorWarp(float glitchFactor) {
        return glitchFactor > 0.75F;
    }
}
