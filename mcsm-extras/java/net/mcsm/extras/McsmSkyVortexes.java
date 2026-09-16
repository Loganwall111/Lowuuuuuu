package net.mcsm.extras;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #433 -- THE SKY VORTEXES, AND WHAT COMES OUT OF THEM.
 *
 * <p>The mandate, in the artist's own list: "sky vortexes spawning monsters."
 * The storm has had sky scenery since the port -- a halo, an atmosphere wall, a
 * vortex mesh drawn around the boss -- and every one of those is a picture. None
 * of them has ever put anything on the ground. This is the half that does: a
 * mouth opens in the sky over whoever the storm is closest to, hangs there for
 * {@link #LIFE} ticks, and drops things out of it one at a time.
 *
 * <p>WHAT IT IS BUILT FROM. Only calls this repository has already proved: the
 * level-tick event the cities, the tornadoes and the black hole all register on;
 * {@code level.sendParticles} for the mouth (the same particles those systems
 * already throw); a registry lookup by id for each dropped creature, exactly the
 * way {@code McsmMassg} and {@code McsmCities} resolve theirs, so no field name
 * has to exist in this version's {@code EntityType} holder; and
 * {@code level.addFreshEntity}.
 *
 * <p>WHY THEY SURVIVE THE DROP. A creature that falls 60 blocks arrives dead, and
 * a corpse is not a threat. So the mouth hangs {@link #HEIGHT} blocks over the
 * ground and each one is let go at the rim, {@link #DROP_FALL} blocks under it:
 * about {@link #HEIGHT} - {@link #DROP_FALL} blocks of fall, which a zombie or a
 * skeleton walks away from with most of its health. Phantoms fly anyway -- they
 * are the ones that make the sky itself dangerous.
 *
 * <p>WHY IT CANNOT RUN AWAY. One mouth per level at a time, at least
 * {@link #INTERVAL} ticks between them, a life timer, a hard cap on how many it
 * can let go, and it only opens where the storm has actually reached
 * {@link #ONSET_PHASE} (or inside the decayed reality, where the weather is
 * already broken).
 */
public final class McsmSkyVortexes {

    /** The storm has to have gone this far before the sky is allowed to open. */
    private static final double ONSET_PHASE = 5.0D;
    /** How high the mouth hangs over the ground under it. */
    private static final double HEIGHT = 26.0D;
    /** The rim of the mouth, so it reads as an opening rather than a point. */
    private static final double RIM = 9.0D;
    /** How far above the ground a dropped creature is let go. */
    private static final double DROP_FALL = 12.0D;
    /** How long a mouth hangs there, in ticks (~13 seconds). */
    private static final int LIFE = 260;
    /** Ticks between two drops. */
    private static final int DROP = 45;
    /** The most a single mouth can let go. */
    private static final int MAX_DROPS = 6;
    /** Ticks between two mouths, per level, at the very least. */
    private static final int INTERVAL = 1500;
    /** How far the mouth is offset from the player it opens over. */
    private static final double OFFSET = 34.0D;
    /** What comes out: vanilla ids, so no field name has to exist for them. */
    private static final String[] DROPS = {"zombie", "skeleton", "phantom", "zombie", "phantom"};

    private static final class Mouth {
        double x;
        double y;
        double z;
        int age;
        int dropped;

        Mouth(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final Map<ResourceKey<Level>, Mouth> LIVE = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Long> LAST = new ConcurrentHashMap<>();

    private McsmSkyVortexes() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmSkyVortexes::tick);
            System.out.println("[ds] sky vortexes are armed (one per level, "
                    + (LIFE / 20) + "s open, up to " + MAX_DROPS + " things out of it)");
        } catch (Throwable t) {
            System.err.println("[ds] the sky vortexes could not hook the level tick: " + t);
        }
    }

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.skyVortexes || level.players().isEmpty()) {
                return;
            }
            Mouth live = LIVE.get(level.dimension());
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
                if (level.getRandom().nextInt(100) >= (decayed ? 16 : 22)) {
                    continue;
                }
                if (open(level, player)) {
                    LAST.put(level.dimension(), Long.valueOf(time));
                    return;
                }
            }
        } catch (Throwable ignored) {
            // a sky that throws is worse than a sky that stays shut
        }
    }

    // ---------------------------------------------------------------------
    // The mouth
    // ---------------------------------------------------------------------

    private static boolean open(ServerLevel level, ServerPlayer player) {
        try {
            RandomSource rng = level.getRandom();
            double angle = rng.nextDouble() * Math.PI * 2.0D;
            double distance = 18.0D + rng.nextDouble() * OFFSET;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            int ground = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(x), (int) Math.floor(z));
            if (ground <= level.getMinY() + 3) {
                return false;
            }
            double y = (double) ground + HEIGHT;
            LIVE.put(level.dimension(), new Mouth(x, y, z));
            level.playSound(null, x, y, z, McsmSounds.OBLIVION_WARP,
                    net.minecraft.sounds.SoundSource.WEATHER, 3.2F, 0.62F);
            level.playSound(null, x, y, z, McsmSounds.RADIO_STATIC,
                    net.minecraft.sounds.SoundSource.WEATHER, 2.4F, 0.40F);
            McsmCreatures.say(level, new Vec3(x, y, z), 280.0D,
                    "The sky opens over you, and something in it is choosing what to let go.",
                    ChatFormatting.LIGHT_PURPLE);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void advance(ServerLevel level, Mouth mouth) {
        try {
            mouth.age++;
            if (mouth.age > LIFE) {
                close(level, mouth);
                return;
            }
            double open = Math.sqrt(Math.min(1.0D, (double) mouth.age / 40.0D));
            double radius = RIM * open;

            // ---- the mouth itself -------------------------------------------
            // A ring of portal light at the rim, a spiral of ash turning inside
            // it, and a slow drift of dust falling out of the middle: enough to
            // read as a hole in the sky with a throat behind it.
            if (level.getGameTime() % 2L == 0L) {
                double spin = (double) mouth.age * 0.14D;
                for (int i = 0; i < 36; i++) {
                    double a = spin + (double) i / 36.0D * Math.PI * 2.0D;
                    double px = mouth.x + Math.cos(a) * radius;
                    double pz = mouth.z + Math.sin(a) * radius;
                    level.sendParticles(ParticleTypes.PORTAL, px, mouth.y, pz, 1,
                            0.25D, 0.25D, 0.25D, 0.02D);
                    level.sendParticles(ParticleTypes.ASH, px, mouth.y - 2.0D, pz, 1,
                            0.3D, 0.3D, 0.3D, 0.03D);
                }
                for (int i = 0; i < 14; i++) {
                    double u = (double) i / 13.0D;
                    double a = spin * 1.7D + u * Math.PI * 3.0D;
                    double r = radius * (1.0D - u * 0.85D);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            mouth.x + Math.cos(a) * r, mouth.y - u * 6.0D, mouth.z + Math.sin(a) * r,
                            1, 0.06D, 0.06D, 0.06D, 0.01D);
                }
            }

            // ---- and what comes out of it -----------------------------------
            if (mouth.age % DROP == 0 && mouth.dropped < MAX_DROPS) {
                drop(level, mouth, radius);
            }
        } catch (Throwable ignored) {
            LIVE.remove(level.dimension());
        }
    }

    private static void drop(ServerLevel level, Mouth mouth, double radius) {
        try {
            RandomSource rng = level.getRandom();
            String id = DROPS[rng.nextInt(DROPS.length)];
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("minecraft", id));
            if (type == null) {
                return;
            }
            Entity created = type.create(level, EntitySpawnReason.EVENT);
            if (created == null) {
                return;
            }
            double a = rng.nextDouble() * Math.PI * 2.0D;
            // Let go at the rim, DROP_FALL over the ground: a fall it survives.
            created.snapTo(mouth.x + Math.cos(a) * radius * 0.55D, mouth.y - DROP_FALL,
                    mouth.z + Math.sin(a) * radius * 0.55D, rng.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(created);
            mouth.dropped++;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, created.getX(), created.getY() + 1.0D,
                    created.getZ(), 24, 0.5D, 0.5D, 0.5D, 0.08D);
        } catch (Throwable ignored) {
            // a drop that fails is one fewer monster; the mouth keeps working
        }
    }

    private static void close(ServerLevel level, Mouth mouth) {
        LIVE.remove(level.dimension());
        try {
            level.playSound(null, mouth.x, mouth.y, mouth.z, McsmSounds.OBLIVION_GLITCH,
                    net.minecraft.sounds.SoundSource.WEATHER, 2.6F, 0.55F);
            for (int i = 0; i < 60; i++) {
                double a = (double) i / 60.0D * Math.PI * 2.0D;
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        mouth.x + Math.cos(a) * RIM, mouth.y, mouth.z + Math.sin(a) * RIM,
                        2, 0.4D, 0.4D, 0.4D, 0.06D);
            }
        } catch (Throwable ignored) {
            // the sky can go quiet badly
        }
    }

    /** The phase of the storm that owns this player, or 0 if there is none. */
    private static double phaseNear(ServerLevel level, ServerPlayer player) {
        try {
            AABB box = player.getBoundingBox().inflate(160.0D);
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
