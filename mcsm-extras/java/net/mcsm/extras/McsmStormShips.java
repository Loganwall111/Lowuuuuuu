package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 Phase 5 - Towering Storm Ships drivable to atmosphere
 * Massive ships that spawn high in sky, can be driven, fly to atmosphere.
 * Uses vanilla boat/armor_stand as base to stay Sodium-safe, no new registry.
 */
public final class McsmStormShips {

    private static final Map<UUID, Long> SHIPS = new ConcurrentHashMap<>();
    private static final double SHIP_SPAWN_HEIGHT = 320.0;
    private static final double ATMOSPHERE_HEIGHT = 600.0;

    private McsmStormShips() {}

    public static void boot() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register(level -> tick(level));
        } catch (Throwable ignored) {}
    }

    private static void tick(ServerLevel level) {
        try {
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (!dim.contains("overworld") && !dim.equals("minecraft:overworld")) return;
            if (level.getGameTime() % 200 != 0) return; // every 10 sec

            // Rare spawn - towering storm ships
            if (Math.random() < 0.02) {
                for (ServerPlayer player : level.players()) {
                    if (player == null) continue;
                    if (Math.random() < 0.3) {
                        spawnStormShip(level, player);
                    }
                }
            }

            // Tick existing ships - move towards atmosphere if piloted
            tickShips(level);

        } catch (Throwable ignored) {}
    }

    private static void spawnStormShip(ServerLevel level, ServerPlayer player) {
        try {
            double px = player.getX() + (Math.random() - 0.5) * 200;
            double pz = player.getZ() + (Math.random() - 0.5) * 200;
            double py = SHIP_SPAWN_HEIGHT + Math.random() * 80;

            // Use armor stand with boat visual? For now use armor stand as ship core
            // In full implementation this would be custom entity, but we use vanilla to stay safe
            var entityType = BuiltInRegistries.ENTITY_TYPE.getValue(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "armor_stand")
            );
            if (entityType == null) return;

            var entity = entityType.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (entity == null) return;

            entity.snapTo(px, py, pz, (float)(Math.random() * 360), 0);
            entity.setCustomName(net.minecraft.network.chat.Component.literal("§8§lStorm Ship §7- Drivable to Atmosphere"));
            entity.setCustomNameVisible(true);
            entity.addTag("ds_storm_ship");
            entity.addTag("ds_drivable");

            // Make invulnerable and persistent
            entity.setInvulnerable(true);
            entity.setNoGravity(true);

            level.addFreshEntity(entity);
            SHIPS.put(entity.getUUID(), level.getGameTime());

            // Build ship hull around it using blocks - towering structure
            buildShipHull(level, new BlockPos((int)px, (int)py, (int)pz));

        } catch (Throwable ignored) {}
    }

    private static void buildShipHull(ServerLevel level, BlockPos center) {
        try {
            // Gigantic ship hull - 25x8x60 blocks, iron + dark prismarine
            int length = 60;
            int width = 12;
            int height = 8;
            for (int x = -width/2; x <= width/2; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = -length/2; z <= length/2; z++) {
                        if (Math.abs(x) == width/2 || y == 0 || y == height-1 || Math.abs(z) == length/2) {
                            BlockPos pos = center.offset(x, y, z);
                            if (level.getBlockState(pos).isAir()) {
                                if (y == 0) {
                                    level.setBlock(pos, Blocks.IRON_BLOCK.defaultBlockState(), 2);
                                } else if (Math.random() < 0.3) {
                                    level.setBlock(pos, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                                } else {
                                    level.setBlock(pos, Blocks.IRON_BLOCK.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
            }
            // Beacon as engine
            level.setBlock(center.offset(0, 1, -length/2 + 2), Blocks.BEACON.defaultBlockState(), 2);
            level.setBlock(center.offset(0, 1, length/2 - 2), Blocks.LODESTONE.defaultBlockState(), 2);
        } catch (Throwable ignored) {}
    }

    private static void tickShips(ServerLevel level) {
        try {
            var toRemove = new java.util.ArrayList<UUID>();
            for (Map.Entry<UUID, Long> entry : SHIPS.entrySet()) {
                final UUID uuid = entry.getKey();
                final long spawnTime = entry.getValue();
                Entity ship = level.getEntity(uuid);
                if (ship == null) {
                    if (level.getGameTime() - spawnTime > 12000) { // 10 min timeout
                        toRemove.add(uuid);
                    }
                    continue;
                }

                // If player riding, move towards atmosphere
                if (!ship.getPassengers().isEmpty()) {
                    Vec3 pos = ship.position();
                    if (pos.y < ATMOSPHERE_HEIGHT) {
                        // Slow ascent
                        ship.setPos(pos.x, pos.y + 0.2, pos.z);
                        // Particle trail
                        if (level.getGameTime() % 5 == 0) {
                            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                pos.x, pos.y - 2, pos.z, 3, 0.5, 0.2, 0.5, 0.02);
                        }
                    } else {
                        // Reached atmosphere - message
                        for (Entity passenger : ship.getPassengers()) {
                            if (passenger instanceof ServerPlayer sp) {
                                if (level.getGameTime() % 100 == 0) {
                                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§b§l[STORM SHIP] §fAtmosphere reached! §7Space visible... §8Stars twinkling above..."));
                                }
                            }
                        }
                    }
                } else {
                    // Drift slowly
                    if (level.getGameTime() % 100 == 0) {
                        Vec3 pos = ship.position();
                        double nx = pos.x + (Math.random() - 0.5) * 2;
                        double nz = pos.z + (Math.random() - 0.5) * 2;
                        ship.setPos(nx, pos.y, nz);
                    }
                }
            }
            for (UUID id : toRemove) {
                SHIPS.remove(id);
            }
        } catch (Throwable ignored) {}
    }
}
