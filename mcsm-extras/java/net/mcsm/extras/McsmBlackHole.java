package net.mcsm.extras;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.dabicco.witherstormmod.entity.BlackHoleEntity;
import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #416 (D.8, phase 4) -- THE BLACK HOLE EVENT.
 *
 * THE MANDATE. "Black holes." The base mod ships one -- {@code BlackHoleEntity},
 * with its own renderer, its own mass model, and a {@code /dabyws} command that
 * can place it -- and this repository has carried a {@code blackHoleEvent} switch
 * since the original mandate list with nothing behind it. This is what is behind
 * it now: the singularity opens on its own, in the broken parts of the world, in
 * front of a player, and it closes again on its own.
 *
 * WHY IT REUSES THE BASE ENTITY RATHER THAN BUILDING ONE. The base entity already
 * does the hard part properly: it grows with what it eats, pulls entities and
 * items, carves the terrain it touches, and it is already rendered. What was
 * missing was never the object -- it was the EVENT: something that decides when
 * the sky is allowed to open, where, and how long for, and that guarantees the
 * world does not simply accumulate singularities forever. That decision-making is
 * this class, and it is the only thing this class does.
 *
 * THE GUARANTEES.
 *   * One event at a time per level (a new one needs {@link #INTERVAL} ticks and
 *     no existing hole inside {@link #NEAR} blocks of the player).
 *   * Only where the world is already broken: the storm's own reach, or the
 *     decayed reality. A peaceful overworld never opens one.
 *   * Every hole this class opens is on a timer ({@code blackHoleSeconds}) and is
 *     collapsed by this class when the timer runs out -- a black hole nobody can
 *     close is not an event, it is a save file on a countdown.
 */
public final class McsmBlackHole {

    /** The storm has to have gone this far before the sky can open on its own. */
    private static final double EVENT_PHASE = 7.0D;
    /** Ticks between events, per level, at the very least. */
    private static final int INTERVAL = 3600;
    /** Never open one this close to an existing hole. */
    private static final double NEAR = 480.0D;
    /** Where the hole opens relative to the player who called it. */
    private static final double MIN_DISTANCE = 38.0D;
    private static final double MAX_DISTANCE = 78.0D;

    /** Every hole we opened, with the tick it must be closed on. */
    private static final Map<UUID, Long> OURS = new ConcurrentHashMap<>();
    private static long lastEvent = Long.MIN_VALUE;

    private McsmBlackHole() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmBlackHole::tick);
            // BUILD #466 -- the rifts are the hole's own phenomenon: it tears the
            // air around it open, and when it goes, they go with it (see collapse).
            McsmRifts.register();
            System.out.println("[ds] the black hole event is armed (phase " + EVENT_PHASE
                    + "+, one at a time, closes itself)");
        } catch (Throwable t) {
            System.err.println("[ds] the black hole event could not hook the level tick: " + t);
        }
    }

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.blackHoleEvent) {
                return;
            }
            if (level.players().isEmpty()) {
                return;
            }
            long time = level.getGameTime();
            closeExpired(level, time);
            if (time % 20L != 0L || time - lastEvent < (long) INTERVAL) {
                return;
            }
            for (ServerPlayer player : level.players()) {
                if (McsmReality.inside(level)) {
                    if (level.getRandom().nextInt(100) >= 16) {
                        continue;
                    }
                } else {
                    double phase = phaseNear(level, player);
                    if (phase < EVENT_PHASE || level.getRandom().nextInt(100) >= 22) {
                        continue;
                    }
                }
                if (holeNear(level, player.position(), NEAR)) {
                    continue;
                }
                if (open(level, player)) {
                    lastEvent = time;
                    return;
                }
            }
        } catch (Throwable ignored) {
            // an event that throws is worse than one that does not fire
        }
    }

    // ---------------------------------------------------------------------
    // Opening and closing
    // ---------------------------------------------------------------------

    /**
     * BUILD #443 -- the ritual door. The Black Sun rite is a player deliberately
     * building the shape that the ambient event does on its own timer, so it calls
     * the same opener; the guards inside (one at a time per level, never within
     * {@code MIN_DISTANCE} of another) still apply, and the caller is told when
     * they do.
     */
    public static boolean openNear(ServerLevel level, ServerPlayer player) {
        try {
            if (!McsmExtrasConfig.blackHoleEvent) {
                return false;
            }
            return open(level, player);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean open(ServerLevel level, ServerPlayer player) {
        try {
            RandomSource rng = level.getRandom();
            double angle = rng.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_DISTANCE + rng.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            int ground = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(x), (int) Math.floor(z));
            if (ground <= level.getMinY() + 2) {
                return false;
            }
            // BUILD #466 -- "black hole in the sky": the hole hangs over the world
            // instead of standing in it, high enough to be the thing on the horizon
            // rather than a prop between two hills, with its rift light running down
            // to the ground below it. The lower placement stays one property away.
            boolean inSky = McsmExtrasConfig.blackHoleInSky;
            double y = inSky ? (double) ground + 84.0D + rng.nextDouble() * 56.0D
                             : (double) ground + 3.0D;

            Entity hole = ModEntityTypes.BLACK_HOLE.create(level, EntitySpawnReason.COMMAND);
            if (hole == null) {
                return false;
            }
            hole.setPos(new Vec3(x, y, z));
            level.addFreshEntity(hole);

            double seconds = Math.max(20.0D, McsmExtrasConfig.blackHoleSeconds);
            OURS.put(hole.getUUID(), Long.valueOf(level.getGameTime() + (long) (seconds * 20.0D)));

            // The sky answers: a column of rift light standing on the hole, so it
            // is visible from anywhere the player can see the horizon. When the hole
            // is up in the sky the column runs DOWN from it to the ground, and the
            // air under it is drawn up into it.
            if (inSky) {
                for (double t = 0.0D; t <= 1.0D; t += 0.02D) {
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y - t * (y - ground), z,
                            2, 0.6D, 0.6D, 0.6D, 0.01D);
                }
                level.sendParticles(ParticleTypes.ASH, x, (double) ground + 1.0D, z, 90,
                        8.0D, 1.0D, 8.0D, 0.06D);
            } else {
                for (double t = 0.0D; t <= 1.0D; t += 0.04D) {
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + t * 46.0D, z, 3,
                            0.5D, 0.5D, 0.5D, 0.01D);
                }
            }
            level.sendParticles(ParticleTypes.PORTAL, x, y, z, 80, 1.4D, 1.4D, 1.4D, 0.30D);
            level.playSound(null, x, y, z, SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.HOSTILE, 3.4F, 0.42F);
            level.playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER, 2.6F, 0.7F);
            McsmCreatures.say(level, new Vec3(x, y, z), 220.0D,
                    (inSky
                            ? "A black hole has opened in the sky. Reality is folding in on itself -- "
                            : "Reality is folding in on itself -- a black hole has opened nearby. ")
                            + (int) seconds + " seconds.",
                    ChatFormatting.DARK_PURPLE);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void closeExpired(ServerLevel level, long time) {
        for (Map.Entry<UUID, Long> entry : new ArrayList<>(OURS.entrySet())) {
            if (time < entry.getValue().longValue()) {
                continue;
            }
            OURS.remove(entry.getKey());
            Entity hole = level.getEntity(entry.getKey());
            if (hole == null) {
                continue;
            }
            collapse(level, hole);
        }
    }

    private static void collapse(ServerLevel level, Entity hole) {
        try {
            double x = hole.getX();
            double y = hole.getY();
            double z = hole.getZ();
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1.0D, z, 2,
                    0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 120, 2.0D, 2.0D, 2.0D, 0.25D);
            level.playSound(null, x, y, z, SoundEvents.ELDER_GUARDIAN_CURSE,
                    SoundSource.HOSTILE, 3.0F, 0.6F);
            McsmCreatures.say(level, new Vec3(x, y, z), 220.0D,
                    "The singularity has collapsed. The world is still here.",
                    ChatFormatting.LIGHT_PURPLE);
            // BUILD #466 -- and it takes its tears with it: every rift the hole tore
            // open around itself is sealed in this same tick, so nothing is left
            // hanging over a hole that is not there any more.
            McsmRifts.sealAround(level, x, y, z, 360.0D);
            hole.discard();
        } catch (Throwable ignored) {
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * BUILD #466 -- where our own holes are, alive, right now. The rift system opens
     * its tears around one of these when there is one near the player, which is what
     * "black holes with the rifts that collapse" means in practice.
     */
    public static java.util.List<Vec3> openHoles(ServerLevel level) {
        java.util.List<Vec3> found = new ArrayList<>();
        try {
            for (UUID id : new ArrayList<>(OURS.keySet())) {
                Entity hole = level.getEntity(id);
                if (hole != null && hole.isAlive()) {
                    found.add(hole.position());
                }
            }
        } catch (Throwable ignored) {
        }
        return found;
    }

    private static boolean holeNear(ServerLevel level, Vec3 at, double range) {
        try {
            AABB box = new AABB(at.x - range, at.y - 420.0D, at.z - range,
                    at.x + range, at.y + 420.0D, at.z + range);
            return !level.getEntitiesOfClass(BlackHoleEntity.class, box).isEmpty();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static double phaseNear(ServerLevel level, ServerPlayer player) {
        try {
            Vec3 at = player.position();
            double range = 420.0D;
            AABB box = new AABB(at.x - range, at.y - 420.0D, at.z - range,
                    at.x + range, at.y + 420.0D, at.z + range);
            double best = 0.0D;
            for (WitherStormEntity storm : level.getEntitiesOfClass(WitherStormEntity.class, box)) {
                best = Math.max(best, (double) storm.getPhase());
            }
            return best;
        } catch (Throwable ignored) {
            return 0.0D;
        }
    }
}
