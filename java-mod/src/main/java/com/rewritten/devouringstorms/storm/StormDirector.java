package com.rewritten.devouringstorms.storm;

import com.mojang.serialization.Codec;
import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.entity.AnnaApparitionEntity;
import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.entity.TazoEntity;
import com.rewritten.devouringstorms.entity.WatcherEntity;
import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.registry.StormSyncPayload;
import com.rewritten.devouringstorms.world.ModDimensions;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The brains of the operation.
 *
 *  - Tracks every MASSG and broadcasts {@link StormSyncPayload} so clients can shift sky,
 *    music and screen corruption.
 *  - Ambient horror spawner: The Watcher appears in darkness; Anna flickers into the Decayed
 *    Reality.
 *  - Builds the starting platform (and a waiting Tazo) the first time the Decayed Reality
 *    is loaded.
 */
public final class StormDirector {

    /** Client presentation intensity 0..1, per level. */
    public static final AttachmentType<Float> STORM_INTENSITY = AttachmentRegistry.create(
        DevouringStorms.id("storm_intensity"),
        builder -> builder.persistent(Codec.FLOAT).initializer(() -> 0.0f)
    );

    /** Whether the Decayed Reality starting platform has been generated. */
    public static final AttachmentType<Boolean> PLATFORM_BUILT = AttachmentRegistry.create(
        DevouringStorms.id("platform_built"),
        builder -> builder.persistent(Codec.BOOL).initializer(() -> false)
    );

    private static final AABB WORLD_SCAN = new AABB(-30_000_000, -64, -30_000_000, 30_000_000, 2048, 30_000_000);

    private StormDirector() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(StormDirector::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            var storms = level.getEntitiesOfClass(MassgEntity.class, WORLD_SCAN, e -> e.isAlive());

            float intensity = storms.isEmpty()
                ? Math.max(0.0f, level.getAttachedOrCreate(STORM_INTENSITY) - 0.002f)
                : Math.min(1.0f, level.getAttachedOrCreate(STORM_INTENSITY) + 0.005f);
            level.setAttached(STORM_INTENSITY, intensity);

            // ---- client sync (every 40 ticks, matches vanilla scoreboard cadence) ----
            if (level.getGameTime() % 40L == 0L) {
                int phase = storms.isEmpty() ? -1 : storms.get(0).getPhase().id();
                float growth = storms.isEmpty() ? 0.0f : storms.get(0).getGrowth();
                boolean critical = !storms.isEmpty() && storms.get(0).isCritical();
                var payload = new StormSyncPayload(phase, growth, critical, !storms.isEmpty(), intensity);
                for (ServerPlayer player : level.players()) {
                    ServerPlayNetworking.send(player, payload);
                }
            }

            // ---- ambient horror spawns ----
            if (level.getGameTime() % 1200L == 0L) {
                maybeSpawnWatcher(level, storms.isEmpty());
            }
            if (level.getGameTime() % 2400L == 0L
                && level.dimension() == ModDimensions.DECAYED_LEVEL_KEY
                && level.getRandom().nextFloat() < 0.25f) {
                maybeSpawnAnna(level);
            }
        }
    }

    /** The Watcher prefers darkness: Decayed Reality always; Overworld only at night or under a storm. */
    private static void maybeSpawnWatcher(ServerLevel level, boolean noLocalStorm) {
        boolean decayed = level.dimension() == ModDimensions.DECAYED_LEVEL_KEY;
        boolean night = level.isNight();
        if (!(decayed || night)) return;
        if (level.getRandom().nextFloat() > (decayed ? 0.45f : (noLocalStorm ? 0.30f : 0.55f))) return;
        if (level.players().isEmpty()) return;

        ServerPlayer target = level.players().get(level.getRandom().nextInt(level.players().size()));
        var existing = level.getEntitiesOfClass(WatcherEntity.class, target.getBoundingBox().inflate(96.0));
        if (!existing.isEmpty()) return; // one gaze at a time

        // appear behind the player — "the silent gaze"
        Vec3 view = target.getViewVector(1.0f);
        Vec3 behind = target.position().subtract(view.scale(1.0)).add(
            -view.x * (14 + level.getRandom().nextInt(14)) + (level.getRandom().nextDouble() - 0.5) * 10.0,
            0.0,
            -view.z * (14 + level.getRandom().nextInt(14)) + (level.getRandom().nextDouble() - 0.5) * 10.0
        );

        WatcherEntity watcher = ModEntities.WATCHER.create(level);
        if (watcher == null) return;
        watcher.moveTo(behind.x, target.getY(), behind.z, target.getYRot() + 180.0f, 0.0f);
        level.addFreshEntity(watcher);
    }

    /** Anna flickers in, never quite close, waiting to be observed. */
    private static void maybeSpawnAnna(ServerLevel level) {
        if (level.players().isEmpty()) return;
        ServerPlayer target = level.players().get(level.getRandom().nextInt(level.players().size()));
        var existing = level.getEntitiesOfClass(AnnaApparitionEntity.class, target.getBoundingBox().inflate(120.0));
        if (!existing.isEmpty()) return;

        Vec3 view = target.getViewVector(1.0f);
        Vec3 ahead = target.position().add(view.scale(18.0 + level.getRandom().nextInt(12)));

        AnnaApparitionEntity anna = ModEntities.ANNA_APPARITION.create(level);
        if (anna == null) return;
        anna.moveTo(ahead.x, target.getY() + 0.5, ahead.z, target.getYRot() + 180.0f, 0.0f);
        level.addFreshEntity(anna);
    }

    /**
     * First time the Decayed Reality is loaded: raise the arrival platform, plant the return rift,
     * and leave Tazo waiting for company.
     */
    public static void ensureSpawnPlatform(ServerLevel level) {
        if (level.dimension() != ModDimensions.DECAYED_LEVEL_KEY) return;
        if (level.getAttachedOrCreate(PLATFORM_BUILT)) return;
        level.setAttached(PLATFORM_BUILT, true);

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        if (y < level.getMinY()) y = 64;
        BlockPos origin = new BlockPos(0, y, 0);

        // 11x11 decayed-stone platform with rot-log pillars
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                level.setBlock(origin.offset(dx, -1, dz), ModBlocks.DECAYED_STONE.defaultBlockState(), 3);
            }
        }
        for (int c : new int[]{-4, 4}) {
            for (int h = 0; h < 3; h++) {
                level.setBlock(origin.offset(c, h, c), ModBlocks.ROT_LOG.defaultBlockState(), 3);
                level.setBlock(origin.offset(-c, h, c), ModBlocks.ROT_LOG.defaultBlockState(), 3);
            }
        }
        // a quiet terminal on the platform — the breached relay home
        level.setBlock(origin.offset(3, 0, 3), ModBlocks.TERMINAL.defaultBlockState(), 3);
        // the return rift (3x3, at the north edge)
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(origin.offset(dx, 0, -4), ModBlocks.RIFT_PORTAL.defaultBlockState(), 3);
        }

        // Tazo was already there. Tazo has always been there.
        TazoEntity tazo = ModEntities.TAZO.create(level);
        if (tazo != null) {
            tazo.moveTo(origin.getX() - 2.5, origin.getY() + 1.0, origin.getZ() + 0.5, 90.0f, 0.0f);
            tazo.setPersistenceRequired();
            level.addFreshEntity(tazo);
        }
    }
}
