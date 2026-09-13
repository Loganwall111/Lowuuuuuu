package net.mcsm.extras.client;

import java.util.HashMap;
import java.util.Map;

import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;

/**
 * Native-block replacement for the retired custom StormDebris cube mesh.
 * Every chunk here is emitted through Minecraft's normal block particle
 * provider, so the renderer owns lighting, atlas selection, and particle
 * lifetime rather than an overlay vertex factory.
 */
public final class McsmNativeStormDebris {
    private static final BlockParticleOption OBSIDIAN = new BlockParticleOption(
            ParticleTypes.BLOCK, Blocks.OBSIDIAN.defaultBlockState());
    private static final BlockParticleOption CRYING_OBSIDIAN = new BlockParticleOption(
            ParticleTypes.BLOCK, Blocks.CRYING_OBSIDIAN.defaultBlockState());

    // Existing ambient dust was oversized. 35% of the old 1.9/2.4 sizes keeps
    // the same fast orbit while making the purple energy a tight mist.
    private static final DustParticleOptions PURPLE_MIST = new DustParticleOptions(
            0xFFB14DFF, 0.665F);
    private static final DustParticleOptions VIOLET_MIST = new DustParticleOptions(
            0xFF6E32D0, 0.840F);
    private static final Map<Integer, Long> LAST_TICK = new HashMap<>();

    private McsmNativeStormDebris() {
    }

    /** Spawn once per game tick for the rendered storm, never per draw pass. */
    public static void submit(WitherStormRenderState state) {
        if (state == null || state.preview != null || state.phase < 1.0D) {
            return;
        }
        try {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft == null ? null : minecraft.level;
            if (level == null) {
                return;
            }
            long tick = level.getGameTime();
            Long previous = LAST_TICK.put(state.stormId, tick);
            if (previous != null && previous == tick) {
                return;
            }
            if (LAST_TICK.size() > 64) {
                LAST_TICK.clear();
            }

            float phase = (float) state.phase;
            boolean finale = phase >= 6.80F;
            int count = finale ? 160 : phase >= 6.0F ? 70 : phase >= 5.0F ? 42
                    : phase >= 4.0F ? 26 : phase >= 3.0F ? 16 : 8;
            float radius = finale ? 54.0F + Math.min(40.0F, (phase - 6.80F) * 120.0F)
                    : phase >= 6.0F ? 34.0F : phase >= 5.0F ? 23.0F
                    : phase >= 4.0F ? 14.0F : 7.0F + phase * 2.0F;

            for (int i = 0; i < count; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2.0D;
                float scale = 1.5F + level.random.nextFloat() * 2.5F;
                double radial = radius * (0.48D + level.random.nextDouble() * 0.70D) * scale / 2.75D;
                double height = (level.random.nextDouble() - 0.42D) * radius * 0.85D * scale / 2.75D;
                double x = state.worldX + Math.cos(angle) * radial;
                double y = state.worldY + 9.0D + height;
                double z = state.worldZ + Math.sin(angle) * radial;
                double tangent = (0.16D + level.random.nextDouble() * 0.18D) * scale;
                double inward = 0.018D * scale;
                double vx = -Math.sin(angle) * tangent - Math.cos(angle) * inward;
                double vz = Math.cos(angle) * tangent - Math.sin(angle) * inward;
                double vy = (level.random.nextDouble() - 0.38D) * 0.07D * scale;
                BlockParticleOption block = level.random.nextFloat() < 0.68F
                        ? OBSIDIAN : CRYING_OBSIDIAN;

                // Native block particles have a provider-owned sprite scale,
                // so a 1.5F..4.0F chunk scale is represented by a tight native
                // cluster. This produces large landscape fragments without a
                // replacement cube mesh or a custom texture atlas.
                int cluster = Math.max(1, Math.min(3, Math.round(scale * 0.72F)));
                for (int j = 0; j < cluster; j++) {
                    double jitter = j == 0 ? 0.0D : 0.22D * scale;
                    level.addParticle(block,
                            x + (level.random.nextDouble() - 0.5D) * jitter,
                            y + (level.random.nextDouble() - 0.5D) * jitter,
                            z + (level.random.nextDouble() - 0.5D) * jitter,
                            vx, vy, vz);
                }
            }

            int mistCount = finale ? 44 : phase >= 6.0F ? 28 : phase >= 4.0F ? 18 : 8;
            for (int i = 0; i < mistCount; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2.0D;
                double radial = radius * (0.72D + level.random.nextDouble() * 0.36D);
                double x = state.worldX + Math.cos(angle) * radial;
                double y = state.worldY + 7.0D
                        + (level.random.nextDouble() - 0.5D) * radius * 0.62D;
                double z = state.worldZ + Math.sin(angle) * radial;
                double tangent = 0.42D + level.random.nextDouble() * 0.30D;
                level.addParticle(level.random.nextBoolean() ? PURPLE_MIST : VIOLET_MIST,
                        x, y, z,
                        -Math.sin(angle) * tangent,
                        (level.random.nextDouble() - 0.5D) * 0.035D,
                        Math.cos(angle) * tangent);
            }
        } catch (Throwable ignored) {
            // A bonus visual must never break entity rendering.
        }
    }
}
