package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 NEXT-GEN Phase 3 - Mega Cities: Toronto/NY tall, massive, extremely high into air
 * Extends abandoned cities to be massive, buildings extend extremely high, infant spawn places, expanded lakes.
 * Buildings can be 40-120 blocks tall, like real skyscrapers, with interiors, rooms, full of life.
 */
public final class McsmMegaCities {

    public static final int REGION = 384; // Larger region for mega cities
    public static final int MEGA_PERCENT = 28; // 28% of regions have mega city
    public static final int ACTIVATE = 420;
    public static final int OPS_PER_TICK = 2200;
    public static final int MAX_PLAN_OPS = 500000;

    private static final Map<Long, Boolean> BUILT = new ConcurrentHashMap<>();
    private static final Set<Long> QUEUED = ConcurrentHashMap.newKeySet();
    private static final ArrayDeque<long[]> OPS = new ArrayDeque<>();
    private static long placed;
    private static long megaCities;

    private static volatile BlockState[] palette;

    private McsmMegaCities() {}

    public static void boot() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmMegaCities::tick);
        } catch (Throwable ignored) {}
    }

    private static void tick(ServerLevel level) {
        try {
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (!dim.contains("overworld") && !dim.equals("minecraft:overworld")) return;

            for (ServerPlayer player : level.players()) {
                if (player == null) continue;
                BlockPos ppos = player.blockPosition();
                long rx = Math.floorDiv(ppos.getX(), REGION);
                long rz = Math.floorDiv(ppos.getZ(), REGION);
                long key = (rx << 32) | (rz & 0xffffffffL);
                if (BUILT.containsKey(key) || QUEUED.contains(key)) continue;

                int hash = hashRegion(rx, rz);
                if ((hash % 100) >= MEGA_PERCENT) {
                    BUILT.put(key, false);
                    continue;
                }

                // Check if near existing mega city
                if (hasNearbyMega(level, ppos.getX(), ppos.getZ())) {
                    BUILT.put(key, false);
                    continue;
                }

                int cx = (int)(rx * REGION + REGION / 2);
                int cz = (int)(rz * REGION + REGION / 2);
                int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
                if (groundY < 60 || groundY > 100) {
                    BUILT.put(key, false);
                    continue;
                }

                ArrayDeque<long[]> plan = new ArrayDeque<>();
                planMegaCity(plan, cx, groundY, cz, hash);
                if (plan.size() > MAX_PLAN_OPS) {
                    BUILT.put(key, false);
                    continue;
                }

                QUEUED.add(key);
                OPS.addAll(plan);
                megaCities++;
            }

            drain(level);

        } catch (Throwable ignored) {}
    }

    private static void drain(ServerLevel level) {
        try {
            int ops = 0;
            while (ops < OPS_PER_TICK && !OPS.isEmpty()) {
                long[] op = OPS.poll();
                if (op == null) break;
                int x = (int)op[0];
                int y = (int)op[1];
                int z = (int)op[2];
                int palIdx = (int)op[3];
                BlockState state = palette()[palIdx];
                if (state == null) continue;
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).isAir() || palIdx == 0 || isReplaceable(level, pos)) {
                    level.setBlock(pos, state, 2);
                    placed++;
                }
                ops++;
            }
        } catch (Throwable ignored) {}
    }

    private static void planMegaCity(ArrayDeque<long[]> plan, int cx, int groundY, int cz, int hash) {
        // Mega city - 5x5 to 7x7 plots, each building extremely tall like Toronto/NY
        int gridSize = 5 + (hash % 3); // 5-7
        int plotSpacing = 28 + (hash % 8); // 28-35
        int baseY = groundY;

        // Central plaza
        int plazaSize = 12;
        for (int x = -plazaSize/2; x <= plazaSize/2; x++) {
            for (int z = -plazaSize/2; z <= plazaSize/2; z++) {
                int wx = cx + x;
                int wz = cz + z;
                plan.add(new long[]{wx, baseY, wz, ROAD()});
                if (Math.abs(x) == plazaSize/2 || Math.abs(z) == plazaSize/2) {
                    plan.add(new long[]{wx, baseY + 1, wz, LAMP()});
                }
            }
        }

        // Buildings - skyscrapers
        for (int gx = -gridSize/2; gx <= gridSize/2; gx++) {
            for (int gz = -gridSize/2; gz <= gridSize/2; gz++) {
                if (gx == 0 && gz == 0) continue; // Plaza
                int bx = cx + gx * plotSpacing;
                int bz = cz + gz * plotSpacing;
                int buildingHash = hashRegion(bx, bz);

                // Skip some plots for roads/parks
                if ((buildingHash % 100) < 12) {
                    // Park
                    for (int x = -6; x <= 6; x++) {
                        for (int z = -6; z <= 6; z++) {
                            int wx = bx + x;
                            int wz = bz + z;
                            if (Math.abs(x) <= 5 && Math.abs(z) <= 5) {
                                plan.add(new long[]{wx, baseY, wz, GRASS()});
                                if ((x + z) % 3 == 0) {
                                    plan.add(new long[]{wx, baseY + 1, wz, LEAVES()});
                                    plan.add(new long[]{wx, baseY + 2, wz, LEAVES()});
                                }
                            }
                        }
                    }
                    continue;
                }

                // Building dimensions - extremely high like Toronto/NY
                int width = 8 + (buildingHash % 6); // 8-13
                int depth = 8 + ((buildingHash >> 4) % 6);
                int height = 40 + (buildingHash % 80); // 40-120 blocks tall! Gigantic
                if (buildingHash % 7 == 0) height = 90 + (buildingHash % 60); // Super tall 90-150

                // Foundation
                for (int x = -width/2; x <= width/2; x++) {
                    for (int z = -depth/2; z <= depth/2; z++) {
                        int wx = bx + x;
                        int wz = bz + z;
                        // Floor
                        plan.add(new long[]{wx, baseY, wz, CONCRETE()});
                        // Walls up to height
                        if (x == -width/2 || x == width/2 || z == -depth/2 || z == depth/2) {
                            for (int y = 1; y < height; y++) {
                                int wy = baseY + y;
                                // Windows every 3 blocks
                                if (y % 4 == 0 && y > 2 && y < height - 2) {
                                    if (x == -width/2 || x == width/2 || z == -depth/2 || z == depth/2) {
                                        if ((x + y) % 2 == 0 || (z + y) % 2 == 0) {
                                            plan.add(new long[]{wx, wy, wz, GLASS()});
                                        } else {
                                            plan.add(new long[]{wx, wy, wz, CONCRETE()});
                                        }
                                    }
                                } else if (y < height - 1) {
                                    plan.add(new long[]{wx, wy, wz, CONCRETE()});
                                }
                                // Floors inside every 5 blocks - interiors, rooms
                                if (y % 5 == 0 && y < height - 5) {
                                    // Floor slab
                                    for (int ix = -width/2 + 1; ix < width/2; ix++) {
                                        for (int iz = -depth/2 + 1; iz < depth/2; iz++) {
                                            int iwx = bx + ix;
                                            int iwz = bz + iz;
                                            if (ix == -width/2 + 1 || ix == width/2 - 1 || iz == -depth/2 + 1 || iz == depth/2 - 1) {
                                                // Interior walls - rooms
                                                if ((ix + iz + y) % 7 == 0) {
                                                    plan.add(new long[]{iwx, wy, iwz, PLANKS()});
                                                }
                                            } else {
                                                // Floor
                                                if (ix == 0 && iz == 0) {
                                                    // Stairs for vertical movement
                                                    plan.add(new long[]{iwx, wy, iwz, STAIRS()});
                                                } else {
                                                    plan.add(new long[]{iwx, wy, iwz, PLANKS()});
                                                }
                                            }
                                        }
                                    }
                                    // Furniture in rooms - full of life
                                    if (buildingHash % 3 == 0) {
                                        int fx = bx + (buildingHash % (width - 2)) - width/2 + 1;
                                        int fz = bz + ((buildingHash >> 8) % (depth - 2)) - depth/2 + 1;
                                        plan.add(new long[]{fx, wy + 1, fz, TABLE()});
                                        plan.add(new long[]{fx + 1, wy + 1, fz, CHAIR()});
                                        if (y % 10 == 0) {
                                            plan.add(new long[]{fx, wy + 1, fz + 1, CHEST()});
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Roof - helipad or antenna for tall buildings
                int roofY = baseY + height;
                for (int x = -width/2; x <= width/2; x++) {
                    for (int z = -depth/2; z <= depth/2; z++) {
                        int wx = bx + x;
                        int wz = bz + z;
                        if (height > 80) {
                            // Helipad - flat roof with H
                            if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                                plan.add(new long[]{wx, roofY, wz, HELIPAD()});
                            } else {
                                plan.add(new long[]{wx, roofY, wz, CONCRETE()});
                            }
                        } else {
                            plan.add(new long[]{wx, roofY, wz, CONCRETE()});
                        }
                    }
                }

                // Antenna for super tall
                if (height > 90) {
                    for (int y = 1; y < 15; y++) {
                        plan.add(new long[]{bx, roofY + y, bz, FENCE()});
                    }
                    plan.add(new long[]{bx, roofY + 15, bz, LAMP()});
                }

                // Entrance - door
                int doorX = bx;
                int doorZ = bz + depth/2;
                plan.add(new long[]{doorX, baseY + 1, doorZ, DOOR()});
                plan.add(new long[]{doorX, baseY + 2, doorZ, DOOR_TOP()});
            }
        }

        // Roads connecting buildings
        for (int gx = -gridSize/2; gx <= gridSize/2; gx++) {
            int roadZ = cz + gx * plotSpacing;
            for (int x = cx - gridSize/2 * plotSpacing; x <= cx + gridSize/2 * plotSpacing; x++) {
                plan.add(new long[]{x, baseY, roadZ, ROAD()});
                if (x % 12 == 0) {
                    plan.add(new long[]{x, baseY + 1, roadZ, LAMP()});
                }
            }
        }
        for (int gz = -gridSize/2; gz <= gridSize/2; gz++) {
            int roadX = cx + gz * plotSpacing;
            for (int z = cz - gridSize/2 * plotSpacing; z <= cz + gridSize/2 * plotSpacing; z++) {
                plan.add(new long[]{roadX, baseY, z, ROAD()});
            }
        }

        // Expanded lakes - bigger lakes near mega cities
        if (hash % 4 == 0) {
            int lakeX = cx + (hash % 100) - 50;
            int lakeZ = cz + ((hash >> 8) % 100) - 50;
            int lakeRadius = 20 + (hash % 25); // 20-45 radius, much bigger
            for (int x = -lakeRadius; x <= lakeRadius; x++) {
                for (int z = -lakeRadius; z <= lakeRadius; z++) {
                    if (x*x + z*z <= lakeRadius*lakeRadius) {
                        int wx = lakeX + x;
                        int wz = lakeZ + z;
                        int wy = baseY - 1;
                        plan.add(new long[]{wx, wy, wz, WATER()});
                        plan.add(new long[]{wx, wy - 1, wz, SAND()});
                        // Edge - sand
                        if (x*x + z*z > (lakeRadius-2)*(lakeRadius-2)) {
                            plan.add(new long[]{wx, baseY, wz, SAND()});
                        }
                    }
                }
            }
        }

        long key = ((long)Math.floorDiv(cx, REGION) << 32) | (Math.floorDiv(cz, REGION) & 0xffffffffL);
        BUILT.put(key, true);
    }

    private static boolean hasNearbyMega(ServerLevel level, int x, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long rx = Math.floorDiv(x + dx * REGION, REGION);
                long rz = Math.floorDiv(z + dz * REGION, REGION);
                long key = (rx << 32) | (rz & 0xffffffffL);
                if (BUILT.containsKey(key) && BUILT.get(key)) return true;
            }
        }
        return false;
    }

    private static boolean isReplaceable(ServerLevel level, BlockPos pos) {
        try {
            var state = level.getBlockState(pos);
            return state.isAir() || state.getBlock() == Blocks.SHORT_GRASS || state.getBlock() == Blocks.TALL_GRASS || state.getBlock() == Blocks.FERN;
        } catch (Throwable t) {
            return true;
        }
    }

    private static int hashRegion(long rx, long rz) {
        long h = rx * 0x9E3779B97F4A7C15L ^ rz * 0xC6A4A7935BD1E995L;
        h ^= h >> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >> 33;
        return (int)(h & 0x7fffffff);
    }

    private static BlockState[] palette() {
        if (palette != null) return palette;
        synchronized (McsmMegaCities.class) {
            if (palette != null) return palette;
            BlockState[] p = new BlockState[32];
            p[0] = Blocks.AIR.defaultBlockState();
            p[1] = Blocks.COBBLESTONE.defaultBlockState();
            p[2] = Blocks.GLASS.defaultBlockState();
            p[3] = Blocks.OAK_PLANKS.defaultBlockState();
            p[4] = Blocks.OAK_STAIRS.defaultBlockState();
            p[5] = Blocks.CRAFTING_TABLE.defaultBlockState();
            p[6] = Blocks.OAK_FENCE.defaultBlockState();
            p[7] = Blocks.LANTERN.defaultBlockState();
            p[8] = Blocks.COBBLESTONE.defaultBlockState();
            p[9] = Blocks.GRASS_BLOCK.defaultBlockState();
            p[10] = Blocks.OAK_LEAVES.defaultBlockState();
            p[11] = Blocks.WATER.defaultBlockState();
            p[12] = Blocks.SAND.defaultBlockState();
            p[13] = Blocks.OAK_DOOR.defaultBlockState();
            p[14] = Blocks.OAK_PLANKS.defaultBlockState();
            p[15] = Blocks.CHEST.defaultBlockState();
            p[16] = Blocks.IRON_BARS.defaultBlockState();
            palette = p;
            return p;
        }
    }

    private static int AIR() { return 0; }
    private static int CONCRETE() { return 1; }
    private static int GLASS() { return 2; }
    private static int PLANKS() { return 3; }
    private static int STAIRS() { return 4; }
    private static int TABLE() { return 5; }
    private static int FENCE() { return 6; }
    private static int LAMP() { return 7; }
    private static int ROAD() { return 8; }
    private static int GRASS() { return 9; }
    private static int LEAVES() { return 10; }
    private static int WATER() { return 11; }
    private static int SAND() { return 12; }
    private static int DOOR() { return 13; }
    private static int DOOR_TOP() { return 13; }
    private static int HELIPAD() { return 14; }
    private static int CHEST() { return 15; }
    private static int CHAIR() { return 6; }
}
