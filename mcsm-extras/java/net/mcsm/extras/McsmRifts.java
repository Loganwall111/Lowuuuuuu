package net.mcsm.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #466 -- THE RIFTS IN REALITY, AND THE FACT THAT THEY CLOSE.
 *
 * <p>THE MANDATE. "Black holes with the rifts that collapse (black hole in the
 * sky)" -- and, from the concept list behind it, "rips in reality". The black hole
 * event (#416, phase 4) opens a singularity, pulls, and collapses on a timer, but
 * the world it opens in stayed whole around it: one object, one event, no damage
 * to the fabric the object was supposed to be tearing. This is the fabric.
 *
 * <p>WHAT A RIFT IS HERE. A vertical seam in the air -- a tear, not an object: it
 * is tracked by this class and drawn with particles, so it cannot be killed, moved
 * or left behind by anything else in the world. It tears open over two seconds,
 * stands there pulling at whatever is near it, whispers, and then SNAPS SHUT,
 * throwing a shockwave out at whatever stood too close. Every rift this class
 * opens is on a timer ({@code riftSeconds}) and is sealed by the same code that
 * opened it: a rift nobody can close is not an event, it is a hole in someone's
 * world.
 *
 * <p>AND IT IS THE HOLE'S OWN PHENOMENON. While a black hole of ours is open,
 * rifts open AROUND IT -- the hole tears the sky it hangs in -- and when that hole
 * collapses, {@link #sealAround} snaps every rift near it shut in the same tick.
 * That is the mandate sentence taken literally: black holes with the rifts that
 * collapse.
 *
 * <p>WHY PARTICLES AND NOT A RENDERER. The base entity already has a renderer and
 * this overlay cannot add one to it (the class is compiled into the base jar). A
 * seam built from the particles this project already uses renders with no shader
 * pack installed, which is a standing requirement: it must work on regular
 * Minecraft.
 */
public final class McsmRifts {

    /** The storm has to have gone this far before reality can tear on its own. */
    private static final double EVENT_PHASE = 5.0D;
    /** Ticks between rifts, per level, at the very least. */
    private static final int INTERVAL = 1200;
    /** Never more than this many alive in one level: a torn sky is not a wallpaper. */
    private static final int MAX_ALIVE = 3;
    /** A rift this close to an existing one is the same tear, not a new one. */
    private static final double NEAR = 220.0D;
    /** Where a rift opens relative to the player it opens for. */
    private static final double MIN_DISTANCE = 26.0D;
    private static final double MAX_DISTANCE = 64.0D;
    /** The seam's full height, and how long it takes to tear open and shut. */
    private static final int SEAM_HEIGHT = 38;
    private static final int GROW_TICKS = 40;
    private static final int SEAL_TICKS = 24;
    /** What it pulls, and what it does to anything that stands in it. */
    private static final double PULL_RANGE = 20.0D;
    private static final double HURT_RANGE = 3.0D;
    private static final float HURT = 2.0F;

    /** One tear. Position is fixed for its whole life: rifts do not wander. */
    private static final class Rift {
        final double x;
        final double y;
        final double z;
        final long born;
        final long dies;

        Rift(double x, double y, double z, long born, long dies) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.born = born;
            this.dies = dies;
        }
    }

    /** Every rift we opened, with the tick it must be sealed on. */
    private static final Map<UUID, Rift> ALIVE = new ConcurrentHashMap<>();
    private static long lastOpen = Long.MIN_VALUE;

    private McsmRifts() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmRifts::tick);
            System.out.println("[ds] the rifts are armed (phase " + EVENT_PHASE
                    + "+, at most " + MAX_ALIVE + " at a time, each one sealed by its own timer)");
        } catch (Throwable t) {
            System.err.println("[ds] the rifts could not hook the level tick: " + t);
        }
    }

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.riftEvents) {
                return;
            }
            long time = level.getGameTime();
            for (Map.Entry<UUID, Rift> entry : new ArrayList<>(ALIVE.entrySet())) {
                Rift rift = entry.getValue();
                if (time >= rift.dies) {
                    ALIVE.remove(entry.getKey());
                    seal(level, rift, false);
                } else {
                    draw(level, rift, time);
                }
            }
            if (level.players().isEmpty() || time % 20L != 0L) {
                return;
            }
            if (ALIVE.size() >= MAX_ALIVE || time - lastOpen < (long) INTERVAL) {
                return;
            }
            for (ServerPlayer player : level.players()) {
                if (!broken(level, player)) {
                    continue;
                }
                if (level.getRandom().nextInt(100) >= 18) {
                    continue;
                }
                if (open(level, player)) {
                    lastOpen = time;
                    return;
                }
            }
        } catch (Throwable ignored) {
            // an event that throws is worse than one that does not fire
        }
    }

    // ---------------------------------------------------------------------
    // Opening, standing, sealing
    // ---------------------------------------------------------------------

    /** A rift only opens where reality is already broken: never in a whole world. */
    private static boolean broken(ServerLevel level, ServerPlayer player) {
        if (McsmReality.inside(level)) {
            return true;
        }
        return phaseNear(level, player) >= EVENT_PHASE;
    }

    /**
     * Opens one rift for this player. If a black hole of ours is open anywhere near
     * them, the rift is torn open around THAT -- the hole is what is doing the
     * tearing -- otherwise it opens in front of the player at a distance.
     */
    private static boolean open(ServerLevel level, ServerPlayer player) {
        try {
            RandomSource rng = level.getRandom();
            Vec3 anchor = null;
            double anchorDistance = 0.0D;
            Vec3 hole = nearestHole(level, player.position(), 320.0D);
            if (hole != null) {
                anchor = hole;
                anchorDistance = 30.0D + rng.nextDouble() * 40.0D;
            } else {
                anchor = player.position();
                anchorDistance = MIN_DISTANCE + rng.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            }
            double angle = rng.nextDouble() * Math.PI * 2.0D;
            double x = anchor.x + Math.cos(angle) * anchorDistance;
            double z = anchor.z + Math.sin(angle) * anchorDistance;
            int ground = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(x), (int) Math.floor(z));
            if (ground <= level.getMinY() + 2) {
                return false;
            }
            // the seam stands in the air, its foot just off the ground: a tear the
            // player walks up to, not a pit
            double y = (double) ground + 1.5D;
            if (riftNear(x, z, NEAR)) {
                return false;
            }

            double seconds = Math.max(10.0D, McsmExtrasConfig.riftSeconds);
            Rift rift = new Rift(x, y, z, level.getGameTime(),
                    level.getGameTime() + (long) (seconds * 20.0D));
            ALIVE.put(UUID.randomUUID(), rift);

            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + SEAM_HEIGHT / 2.0D, z, 60,
                    0.8D, SEAM_HEIGHT / 3.0D, 0.8D, 0.05D);
            level.sendParticles(ParticleTypes.PORTAL, x, y, z, 30, 0.8D, 0.8D, 0.8D, 0.18D);
            level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE, 2.6F, 0.55F);
            McsmCreatures.say(level, new Vec3(x, y, z), 140.0D,
                    hole != null
                            ? "The hole is tearing the air around it open."
                            : "Reality has torn open nearby. It will not stay open.",
                    ChatFormatting.DARK_PURPLE);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** The seam, growing and then standing. */
    private static void draw(ServerLevel level, Rift rift, long time) {
        try {
            long age = time - rift.born;
            long left = rift.dies - time;
            double grow = Math.min(1.0D, (double) age / (double) GROW_TICKS);
            double shut = Math.min(1.0D, (double) left / (double) SEAL_TICKS);
            double height = (double) SEAM_HEIGHT * grow * shut;
            if (height <= 0.5D) {
                return;
            }
            // the seam itself: bright at the edges, cold down the middle
            for (double t = 0.0D; t <= 1.0D; t += 0.05D) {
                double y = rift.y + t * height;
                double wobble = Math.sin((time * 0.22D) + t * 7.0D) * 1.25D;
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        rift.x + wobble, y, rift.z - wobble * 0.4D, 1, 0.12D, 0.12D, 0.12D, 0.005D);
                if (t > 0.35D && t < 0.65D) {
                    level.sendParticles(ParticleTypes.SOUL, rift.x, y, rift.z, 1,
                            0.18D, 0.18D, 0.18D, 0.004D);
                }
            }
            // the light on the ground where it stands, and the dust it lifts
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, rift.x, rift.y + 0.2D, rift.z,
                    2, 0.5D, 0.2D, 0.5D, 0.01D);
            if (time % 10L == 0L) {
                level.sendParticles(ParticleTypes.ASH, rift.x, rift.y + height * 0.5D, rift.z,
                        6, 1.6D, height * 0.35D, 1.6D, 0.02D);
            }
            // what it does to whatever is standing there
            AABB box = new AABB(rift.x - PULL_RANGE, rift.y - 8.0D, rift.z - PULL_RANGE,
                    rift.x + PULL_RANGE, rift.y + height + 8.0D, rift.z + PULL_RANGE);
            for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, box)) {
                double dx = rift.x - caught.getX();
                double dy = (rift.y + height * 0.5D) - caught.getY();
                double dz = rift.z - caught.getZ();
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d > PULL_RANGE || d < 1.0E-3D) {
                    continue;
                }
                double falloff = 1.0D - d / PULL_RANGE;
                double scale = 0.030D * falloff * grow;
                caught.push(dx / d * scale, dy / d * scale * 0.5D, dz / d * scale);
                if (d < HURT_RANGE && time % 20L == 0L) {
                    caught.hurtServer(level, level.damageSources().generic(), HURT);
                }
            }
            if (time % 90L == 0L) {
                level.playSound(null, rift.x, rift.y + height * 0.5D, rift.z,
                        McsmSounds.MASSG_WHISPER, SoundSource.AMBIENT, 1.3F, 0.7F);
            }
        } catch (Throwable ignored) {
            // a draw that throws must not stop the timer that closes it
        }
    }

    /**
     * Seals one rift: the seam snaps inward, and anything close is thrown back from
     * where it stood. {@code quiet} is for the mass sealing that follows a black
     * hole's collapse -- three shockwaves in one tick are one event, not three.
     */
    private static void seal(ServerLevel level, Rift rift, boolean quiet) {
        try {
            double mid = rift.y + SEAM_HEIGHT * 0.5D;
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, rift.x, mid, rift.z, 1,
                    0.0D, 0.0D, 0.0D, 0.0D);
            for (double t = 0.0D; t <= 1.0D; t += 0.05D) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, rift.x, rift.y + t * SEAM_HEIGHT,
                        rift.z, 2, 0.2D, 0.2D, 0.2D, 0.30D);
            }
            level.sendParticles(ParticleTypes.SQUID_INK, rift.x, mid, rift.z, 40,
                    0.6D, SEAM_HEIGHT * 0.4D, 0.6D, 0.10D);
            if (quiet) {
                return;
            }
            level.playSound(null, rift.x, mid, rift.z, SoundEvents.ELDER_GUARDIAN_CURSE,
                    SoundSource.HOSTILE, 2.4F, 1.35F);
            AABB box = new AABB(rift.x - PULL_RANGE, rift.y - 8.0D, rift.z - PULL_RANGE,
                    rift.x + PULL_RANGE, rift.y + SEAM_HEIGHT + 8.0D, rift.z + PULL_RANGE);
            for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, box)) {
                double dx = caught.getX() - rift.x;
                double dy = caught.getY() - mid;
                double dz = caught.getZ() - rift.z;
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d > PULL_RANGE || d < 1.0E-3D) {
                    continue;
                }
                double push = 1.15D * (1.0D - d / PULL_RANGE);
                caught.push(dx / d * push, Math.abs(dy / d) * 0.35D + 0.25D, dz / d * push);
            }
            McsmCreatures.say(level, new Vec3(rift.x, rift.y, rift.z), 140.0D,
                    "The tear has sealed.", ChatFormatting.LIGHT_PURPLE);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Seals every rift within {@code range} of a point -- the black hole's own way
     * of taking its tears with it when it goes. Called by {@link McsmBlackHole}.
     */
    public static void sealAround(ServerLevel level, double x, double y, double z, double range) {
        try {
            int sealed = 0;
            for (Map.Entry<UUID, Rift> entry : new ArrayList<>(ALIVE.entrySet())) {
                Rift rift = entry.getValue();
                double dx = rift.x - x;
                double dy = rift.y - y;
                double dz = rift.z - z;
                if (dx * dx + dy * dy + dz * dz > range * range) {
                    continue;
                }
                ALIVE.remove(entry.getKey());
                seal(level, rift, sealed > 0);
                sealed++;
            }
            if (sealed > 0) {
                McsmCreatures.say(level, new Vec3(x, y, z), 220.0D,
                        sealed == 1 ? "The singularity's tear snapped shut with it."
                                : "The singularity's tears snapped shut with it (" + sealed + ").",
                        ChatFormatting.LIGHT_PURPLE);
            }
        } catch (Throwable ignored) {
        }
    }

    /** Every rift we currently have open in this level, for the panel and the command. */
    public static int alive(ServerLevel level) {
        return ALIVE.size();
    }

    /** Seals everything open in this level now -- {@code /ds rift seal}. */
    public static int sealAll(ServerLevel level) {
        int n = 0;
        try {
            for (Map.Entry<UUID, Rift> entry : new ArrayList<>(ALIVE.entrySet())) {
                ALIVE.remove(entry.getKey());
                seal(level, entry.getValue(), n > 0);
                n++;
            }
        } catch (Throwable ignored) {
        }
        return n;
    }

    /** Opens one by hand, the way the ritual opens a hole -- {@code /ds rift}. */
    public static boolean openNow(ServerLevel level, ServerPlayer player) {
        try {
            if (!McsmExtrasConfig.riftEvents || ALIVE.size() >= MAX_ALIVE) {
                return false;
            }
            return open(level, player);
        } catch (Throwable t) {
            return false;
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static boolean riftNear(double x, double z, double range) {
        for (Rift rift : ALIVE.values()) {
            double dx = rift.x - x;
            double dz = rift.z - z;
            if (dx * dx + dz * dz <= range * range) {
                return true;
            }
        }
        return false;
    }

    private static Vec3 nearestHole(ServerLevel level, Vec3 at, double range) {
        try {
            List<Vec3> holes = McsmBlackHole.openHoles(level);
            Vec3 best = null;
            double bestDistance = range * range;
            for (Vec3 hole : holes) {
                double d = hole.distanceToSqr(at);
                if (d <= bestDistance) {
                    bestDistance = d;
                    best = hole;
                }
            }
            return best;
        } catch (Throwable ignored) {
            return null;
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
