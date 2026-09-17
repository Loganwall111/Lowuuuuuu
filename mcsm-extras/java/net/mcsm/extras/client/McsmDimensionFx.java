package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmIdentity;
import net.mcsm.extras.McsmSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * BUILD #461 -- EVERY WORLD'S OWN AIR.
 *
 * <p>THE ASK, the last line of the identity list: "own blocks, items, mobs,
 * locks, VFX; no re-use". The blocks, the items, the locks and the mobs are in.
 * This is the VFX -- and it is the piece that decides whether two dimensions
 * actually <i>feel</i> different when a player is standing in them doing nothing.
 *
 * <p>Until now the air of the mod's three worlds was the storm's own weather: the
 * same storm particles, the same storm sounds, in three places that are not the
 * storm. Now every dimension breathes its own:
 *
 * <pre>
 *   decayed reality   ASH falls, and now and then something pale rises. The air
 *                     is a low drone with nothing behind it.
 *   adams infinity    motes LIFT, far out and slow. Something enormous is
 *                     breathing a long way off.
 *   the void          ink drifts down, and occasionally things fall UPWARD --
 *                     the one direction nothing else in this mod moves.
 * </pre>
 *
 * <p>Each dimension has its own two particle types, its own rate and spread, and
 * its own ambience sound; no particle type and no sound is used by two of them.
 * The Overworld is untouched: {@code McsmIdentity.forLevel} is null outside the
 * mod's dimensions, and this returns without drawing anything. The storm keeps
 * its own weather in the Overworld, where it belongs.
 *
 * <p>Driven from the same per-frame hook the blast front uses
 * ({@code LevelRenderer.render}), and it steps at most once per game tick. Wrapped
 * end to end: a visual can never break a frame.
 */
public final class McsmDimensionFx {

    private McsmDimensionFx() {
    }

    /** The last game tick this ran for, so a frame loop cannot spawn 60x the air. */
    private static long lastTick = Long.MIN_VALUE;
    /** A per-client counter for the slow ambience rolls. */
    private static int clock;

    /**
     * One world's air: two particle types, how often it breathes, how far out it
     * reaches, and the sound it makes when nobody is talking.
     */
    private record Air(String id, ParticleOptions near, double nearChance,
                       ParticleOptions far, double farChance,
                       double spread, double lift, double fall,
                       SoundEvent ambience, int ambienceEvery) {
    }

    private static final Air DECAYED = new Air(
            McsmIdentity.DECAYED,
            ParticleTypes.ASH, 1.0D,
            ParticleTypes.SOUL, 0.12D,
            20.0D, 0.010D, -0.020D,
            McsmSounds.OBLIVION_DRONE, 1400);

    private static final Air ADAMS = new Air(
            McsmIdentity.ADAMS,
            ParticleTypes.DUST_PLUME, 0.85D,
            ParticleTypes.CLOUD, 0.05D,
            34.0D, 0.014D, -0.004D,
            McsmSounds.MASSG_BREATH, 1700);

    private static final Air VOID = new Air(
            McsmIdentity.VOID,
            ParticleTypes.SQUID_INK, 0.90D,
            ParticleTypes.REVERSE_PORTAL, 0.10D,
            40.0D, 0.008D, 0.012D,
            McsmSounds.OBLIVION_GLITCH, 1100);

    private static final Air[] AIRS = { DECAYED, ADAMS, VOID };

    /** The air of a world, or null for a world that is not one of ours. */
    public static Air airFor(McsmIdentity.Skin skin) {
        if (skin == null) {
            return null;
        }
        for (Air air : AIRS) {
            if (air.id().equals(skin.id())) {
                return air;
            }
        }
        return null;
    }

    /** Every world's air, for the boot log and the gate. */
    public static String summary() {
        StringBuilder out = new StringBuilder("dimension air:");
        for (Air air : AIRS) {
            out.append(' ').append(air.id()).append('=')
                    .append(air.near()).append('+').append(air.far());
        }
        return out.toString();
    }

    /**
     * Breathe for the dimension the camera is standing in. Called once a frame
     * from the client hook; does its work once per game tick.
     */
    public static void tick() {
        try {
            if (!McsmExtrasConfig.dimensionFx) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) {
                return;
            }
            long now = level.getGameTime();
            if (now == lastTick) {
                return;
            }
            lastTick = now;
            Air air = airFor(McsmIdentity.forLevel(level));
            if (air == null) {
                return;
            }
            if (mc.player == null) {
                return;
            }
            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();
            if (level.getRandom().nextDouble() < air.nearChance()) {
                breathe(level, air.near(), px, py, pz, air.spread(), air.lift(), air.fall(), 3);
            }
            if (level.getRandom().nextDouble() < air.farChance()) {
                breathe(level, air.far(), px, py, pz, air.spread() * 1.8D,
                        air.lift() * 1.6D, air.fall() * 0.4D, 1);
            }
            // the ambience: one long sound, on its own slow clock, quiet
            if (++clock >= air.ambienceEvery()) {
                clock = 0;
                level.playLocalSound(px, py, pz, air.ambience(), SoundSource.AMBIENT,
                        0.30F, 1.0F, false);
            }
        } catch (Throwable ignored) {
            // a visual can never break a frame
        }
    }

    /** A handful of one particle type, spread around the player. */
    private static void breathe(ClientLevel level, ParticleOptions type,
            double px, double py, double pz, double spread,
            double lift, double fall, int count) {
        for (int i = 0; i < count; i++) {
            double x = px + (level.getRandom().nextDouble() - 0.5D) * spread;
            double y = py + 1.0D + level.getRandom().nextDouble() * spread * 0.75D;
            double z = pz + (level.getRandom().nextDouble() - 0.5D) * spread;
            level.addParticle(type, x, y, z,
                    (level.getRandom().nextDouble() - 0.5D) * 0.010D,
                    lift + (level.getRandom().nextDouble() - 0.5D) * 0.006D + fall,
                    (level.getRandom().nextDouble() - 0.5D) * 0.010D);
        }
    }
}
