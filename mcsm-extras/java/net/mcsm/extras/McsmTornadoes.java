package net.mcsm.extras;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #416 (D.8, phase 4) -- GIGANTIC TORNADOES.
 *
 * THE MANDATE. "Gigantic tornadoes." The mod has had a {@code megaTornadoes}
 * switch since the original mandate list and a tornado SOUND since long before
 * that; what it has never had is a tornado. This is one.
 *
 * WHAT A TORNADO IS HERE. A funnel that exists in the real world, at real
 * coordinates, for {@link #LIFE} ticks: it is announced when it touches down, it
 * walks across the terrain toward whoever is closest, it throws everything alive
 * that comes within {@link #PULL_RANGE} of its axis, it lifts what it catches,
 * it scours a track into the surface it passes over, and it dissipates on its
 * own -- loudly -- where it ends up.
 *
 * HOW IT IS BUILT. Only three things, all of them proven calls in this
 * repository: particles for the funnel itself (the same helix the storm's own
 * atmosphere passes use), {@code Entity.push} for the throw, and
 * {@code level.setBlock(pos, state, 2)} -- the same call the abandoned-city
 * generator writes every district with -- for the track, one to two blocks deep
 * so a tornado ruins the surface instead of hollowing the world out.
 *
 * WHY IT CANNOT RUN AWAY. One funnel per level; at least {@link #INTERVAL} ticks
 * between touch-downs; it needs the storm to have reached {@link #ONSET_PHASE}
 * (or the decayed reality, where the weather is already broken); it only scours
 * blocks it can see the sky over; and it always ends, on a timer, even if the
 * player it was chasing logs out.
 */
public final class McsmTornadoes {

    /** The storm has to have gone this far before the weather turns. */
    private static final double ONSET_PHASE = 5.5D;
    /** Ticks between touch-downs, per level, at the very least. */
    private static final int INTERVAL = 2400;
    /** How long a funnel lives, in ticks. */
    private static final int LIFE = 900;
    /** Maturity ramp, in ticks. */
    private static final double MATURE_TICKS = 120.0D;
    /** Everything alive inside this distance of the axis is thrown. */
    private static final double PULL_RANGE = 48.0D;
    /** Contact radius: what is inside this is hit. */
    private static final double CONTACT = 4.5D;
    /** The funnel is this tall when fully grown. */
    private static final double MAX_HEIGHT = 78.0D;
    /** Top radius when fully grown. */
    private static final double MAX_RADIUS = 22.0D;
    /** How often the track is scoured, in ticks. */
    private static final int SCOUR_INTERVAL = 6;

    private static final class Spin {
        double x;
        double y;
        double z;
        double driftX;
        double driftZ;
        int age;
        int scour;

        Spin(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final Map<ResourceKey<Level>, Spin> LIVE = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Long> LAST = new ConcurrentHashMap<>();

    private McsmTornadoes() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmTornadoes::tick);
            System.out.println("[ds] mega-tornadoes are armed (one per level, "
                    + (LIFE / 20) + "s lifetime, scours a track)");
        } catch (Throwable t) {
            System.err.println("[ds] the tornadoes could not hook the level tick: " + t);
        }
    }

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.megaTornadoes || level.players().isEmpty()) {
                return;
            }
            Spin live = LIVE.get(level.dimension());
            if (live != null) {
                advance(level, live);
                return;
            }
            long time = level.getGameTime();
            if (time % 20L != 0L) {
                return;
            }
            Long last = LAST.get(level.dimension());
            if (last != null && time - last.longValue() < (long) INTERVAL) {
                return;
            }
            for (ServerPlayer player : level.players()) {
                boolean decayed = McsmReality.inside(level);
                if (!decayed && phaseNear(level, player) < ONSET_PHASE) {
                    continue;
                }
                if (level.getRandom().nextInt(100) >= (decayed ? 18 : 26)) {
                    continue;
                }
                if (touchDown(level, player)) {
                    LAST.put(level.dimension(), Long.valueOf(time));
                    return;
                }
            }
        } catch (Throwable ignored) {
            // weather that throws is worse than weather that never comes
        }
    }

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    private static boolean touchDown(ServerLevel level, ServerPlayer player) {
        try {
            RandomSource rng = level.getRandom();
            double angle = rng.nextDouble() * Math.PI * 2.0D;
            double distance = 60.0D + rng.nextDouble() * 40.0D;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            int ground = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(x), (int) Math.floor(z));
            if (ground <= level.getMinY() + 3) {
                return false;
            }
            Spin spin = new Spin(x, (double) ground, z);
            double toward = rng.nextDouble() * Math.PI * 2.0D;
            spin.driftX = Math.cos(toward) * 0.055D;
            spin.driftZ = Math.sin(toward) * 0.055D;
            LIVE.put(level.dimension(), spin);
            level.playSound(null, x, (double) ground, z, SoundEvents.ELDER_GUARDIAN_CURSE,
                    SoundSource.WEATHER, 3.4F, 0.45F);
            level.playSound(null, x, (double) ground, z, SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER, 3.0F, 0.72F);
            McsmCreatures.say(level, new Vec3(x, (double) ground, z), 260.0D,
                    "A tornado has touched down " + (int) distance + " blocks away. Run.",
                    ChatFormatting.AQUA);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void advance(ServerLevel level, Spin spin) {
        try {
            spin.age++;
            if (spin.age > LIFE) {
                dissipate(level, spin);
                return;
            }
            double mature = Math.min(1.0D, (double) spin.age / MATURE_TICKS);
            double height = 30.0D + (MAX_HEIGHT - 30.0D) * mature;
            double radius = 4.0D + (MAX_RADIUS - 4.0D) * Math.sqrt(mature);

            // ---- the funnel itself -------------------------------------------
            // One helix of overlapping dust, every other tick: cheap enough to
            // run for 45 seconds and solid enough to read as a wall of debris.
            if (level.getGameTime() % 2L == 0L) {
                double phase = (double) spin.age * 0.09D;
                for (int i = 0; i < 44; i++) {
                    double u = (double) i / 43.0D;
                    double angle = phase + u * Math.PI * 4.4D;
                    double r = radius * (0.22D + 0.95D * u);
                    double y = spin.y + u * height;
                    double px = spin.x + Math.cos(angle) * r;
                    double pz = spin.z + Math.sin(angle) * r;
                    level.sendParticles(ParticleTypes.ASH, px, y, pz, 1,
                            0.45D, 0.45D, 0.45D, 0.05D);
                    if (i % 4 == 0) {
                        level.sendParticles(ParticleTypes.CLOUD, px, y, pz, 1,
                                0.6D, 0.5D, 0.6D, 0.03D);
                    }
                }
                // the ground scar: a ring of dust where the funnel meets the world
                for (int i = 0; i < 14; i++) {
                    double angle = phase * 1.7D + (double) i * 0.45D;
                    level.sendParticles(ParticleTypes.ASH,
                            spin.x + Math.cos(angle) * radius * 0.5D,
                            spin.y + 0.4D,
                            spin.z + Math.sin(angle) * radius * 0.5D,
                            2, 1.2D, 0.2D, 1.2D, 0.10D);
                }
            }

            // ---- what it throws ----------------------------------------------
            AABB box = new AABB(spin.x - PULL_RANGE, spin.y - 12.0D, spin.z - PULL_RANGE,
                    spin.x + PULL_RANGE, spin.y + height, spin.z + PULL_RANGE);
            for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, box)) {
                double dx = spin.x - caught.getX();
                double dz = spin.z - caught.getZ();
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > PULL_RANGE) {
                    continue;
                }
                double falloff = 1.0D - d / PULL_RANGE;
                double scale = 0.085D * falloff * (0.6D + 0.4D * mature);
                double nx = dx / (d + 1.0E-4D);
                double nz = dz / (d + 1.0E-4D);
                caught.push(nx * scale, 0.10D + 0.24D * falloff, nz * scale);
                if (d < CONTACT && level.getGameTime() % 10L == 0L) {
                    caught.hurtServer(level, level.damageSources().generic(), 3.0F);
                }
            }

            // ---- the drift ----------------------------------------------------
            ServerPlayer chase = nearestPlayer(level, spin, 120.0D);
            if (chase != null && level.getGameTime() % 40L == 0L) {
                double dx = chase.getX() - spin.x;
                double dz = chase.getZ() - spin.z;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 26.0D) {
                    spin.driftX = dx / (d + 1.0E-4D) * 0.055D;
                    spin.driftZ = dz / (d + 1.0E-4D) * 0.055D;
                }
            }
            spin.x += spin.driftX;
            spin.z += spin.driftZ;
            int ground = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(spin.x), (int) Math.floor(spin.z));
            spin.y = (double) ground;

            // ---- the track ----------------------------------------------------
            spin.scour--;
            if (spin.scour <= 0) {
                spin.scour = SCOUR_INTERVAL;
                scour(level, spin, radius);
            }

            // ---- the weather voice --------------------------------------------
            if (level.getGameTime() % 60L == 0L) {
                level.playSound(null, spin.x, spin.y + 6.0D, spin.z,
                        SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.WEATHER,
                        2.2F, 0.42F + 0.2F * (float) mature);
            }
        } catch (Throwable ignored) {
            // a funnel that stops advancing still dissipates on its own timer
        }
    }

    /**
     * The track: the surface the funnel has passed over is torn up one to two
     * blocks deep, and only where the sky is open -- so a tornado ruins a field
     * or a street and never digs into a cave under a mountain.
     */
    private static void scour(ServerLevel level, Spin spin, double radius) {
        try {
            RandomSource rng = level.getRandom();
            int reach = Math.max(2, (int) Math.round(radius * 0.55D));
            for (int i = 0; i < 6; i++) {
                int x = (int) Math.floor(spin.x) + rng.nextInt(reach * 2 + 1) - reach;
                int z = (int) Math.floor(spin.z) + rng.nextInt(reach * 2 + 1) - reach;
                int top = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos at = new BlockPos(x, top - 1, z);
                if (!level.isLoaded(at) || !level.canSeeSky(at.above())) {
                    continue;
                }
                BlockState state = level.getBlockState(at);
                if (state.isAir()) {
                    continue;
                }
                int depth = 1 + rng.nextInt(2);
                for (int k = 0; k < depth; k++) {
                    BlockPos cut = at.below(k);
                    if (!level.isLoaded(cut) || level.getBlockState(cut).isAir()) {
                        continue;
                    }
                    level.setBlock(cut, McsmContent.DECAYED_SURFACE.defaultBlockState(), 2);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void dissipate(ServerLevel level, Spin spin) {
        try {
            LIVE.remove(level.dimension());
            level.sendParticles(ParticleTypes.CLOUD, spin.x, spin.y + 8.0D, spin.z, 120,
                    10.0D, 14.0D, 10.0D, 0.42D);
            level.sendParticles(ParticleTypes.ASH, spin.x, spin.y + 4.0D, spin.z, 160,
                    14.0D, 10.0D, 14.0D, 0.30D);
            level.playSound(null, spin.x, spin.y, spin.z, SoundEvents.WITHER_SPAWN,
                    SoundSource.WEATHER, 3.0F, 0.5F);
            McsmCreatures.say(level, new Vec3(spin.x, spin.y, spin.z), 260.0D,
                    "The tornado has come apart.",
                    ChatFormatting.AQUA);
        } catch (Throwable ignored) {
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static ServerPlayer nearestPlayer(ServerLevel level, Spin spin, double range) {
        ServerPlayer best = null;
        double bestDistance = range * range;
        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - spin.x;
            double dz = player.getZ() - spin.z;
            double d = dx * dx + dz * dz;
            if (d < bestDistance) {
                bestDistance = d;
                best = player;
            }
        }
        return best;
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
