package net.dabicco.witherstormmod.client;

import java.util.Random;

/**
 * DevouringStormVFX — Immersive visual effects for Devouring Storms.
 * Pink smoke, corruption particles, reality warping effects.
 * Rendering handled by StormPresenceFX integration.
 */
public final class DevouringStormVFX {

    private static final int MAX_PARTICLES = 256;
    private static final float[] PX = new float[MAX_PARTICLES];
    private static final float[] PY = new float[MAX_PARTICLES];
    private static final float[] PZ = new float[MAX_PARTICLES];
    private static final float[] PVX = new float[MAX_PARTICLES];
    private static final float[] PVY = new float[MAX_PARTICLES];
    private static final float[] PVZ = new float[MAX_PARTICLES];
    private static final int[] PLIFE = new int[MAX_PARTICLES];
    private static final int[] PMAX = new int[MAX_PARTICLES];
    private static final float[] PSIZE = new float[MAX_PARTICLES];
    private static final float[] PCOLOR_R = new float[MAX_PARTICLES];
    private static final float[] PCOLOR_G = new float[MAX_PARTICLES];
    private static final float[] PCOLOR_B = new float[MAX_PARTICLES];
    private static final Random RANDOM = new Random(42L);
    private static int nextParticle = 0;

    private static final float[][] PINK_SMOKE_COLORS = {
        {0.95f, 0.3f, 0.7f},
        {0.85f, 0.2f, 0.6f},
        {0.75f, 0.15f, 0.5f},
        {0.65f, 0.1f, 0.45f},
        {0.55f, 0.08f, 0.35f},
    };

    private DevouringStormVFX() {
    }

    public static void emitPinkSmoke(double x, double y, double z, double radius) {
        for (int i = 0; i < 3; i++) {
            int idx = nextParticle;
            nextParticle = (nextParticle + 1) % MAX_PARTICLES;

            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = RANDOM.nextDouble() * radius;
            double height = RANDOM.nextDouble() * radius * 0.5;

            PX[idx] = (float)(x + Math.cos(angle) * dist);
            PY[idx] = (float)(y + height);
            PZ[idx] = (float)(z + Math.sin(angle) * dist);

            PVX[idx] = (float)(RANDOM.nextGaussian() * 0.02);
            PVY[idx] = (float)(RANDOM.nextDouble() * 0.05 + 0.02);
            PVZ[idx] = (float)(RANDOM.nextGaussian() * 0.02);

            PLIFE[idx] = 0;
            PMAX[idx] = 60 + RANDOM.nextInt(40);
            PSIZE[idx] = 0.5f + RANDOM.nextFloat() * 1.5f;

            float[] color = PINK_SMOKE_COLORS[RANDOM.nextInt(PINK_SMOKE_COLORS.length)];
            PCOLOR_R[idx] = color[0];
            PCOLOR_G[idx] = color[1];
            PCOLOR_B[idx] = color[2];
        }
    }

    public static void tick() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (PLIFE[i] >= PMAX[i]) continue;

            PLIFE[i]++;
            PX[i] += PVX[i];
            PY[i] += PVY[i];
            PZ[i] += PVZ[i];

            PVX[i] *= 0.98f;
            PVY[i] *= 0.99f;
            PVZ[i] *= 0.98f;
            PSIZE[i] *= 1.01f;
        }
    }

    public static int getActiveCount() {
        int count = 0;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (PLIFE[i] < PMAX[i]) count++;
        }
        return count;
    }
}
