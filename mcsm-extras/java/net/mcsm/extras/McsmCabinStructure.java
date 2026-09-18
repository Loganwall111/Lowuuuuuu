package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 NEXT-GEN Phase 3 - The Cabin
 * New structure that spawns essentially in middle of night in any forest.
 * Locked with special door, inside books, more stories of devouring storms,
 * Story of devouring storms, procedural interiors.
 * Spawns at midnight, locked cabin door, requires special key.
 */
public final class McsmCabinStructure {

    public static final int REGION = 512; // Forest region size
    public static final int CABIN_PERCENT = 18; // 18% of forest regions have cabin
    public static final int ACTIVATE = 280; // Player distance to trigger build
    public static final int OPS_PER_TICK = 1200;
    public static final int MAX_PLAN_OPS = 80000;

    private static final Map<Long, Boolean> BUILT = new ConcurrentHashMap<>();
    private static final Set<Long> QUEUED = ConcurrentHashMap.newKeySet();
    private static final ArrayDeque<long[]> OPS = new ArrayDeque<>();
    private static long placed;
    private static long cabins;

    private static volatile BlockState[] palette;

    private McsmCabinStructure() {}

    public static void boot() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmCabinStructure::tick);
        } catch (Throwable ignored) {}
    }

    private static void tick(ServerLevel level) {
        try {
            if (level == null) return;
            if (!level.dimension().identifier().toString().contains("overworld") && !level.dimension().identifier().toString().equals("minecraft:overworld")) return;
            // Only at midnight - time 18000
            long time = level.getDayTime() % 24000;
            boolean isMidnight = time >= 17800 && time <= 18200;
            if (!isMidnight) {
                // Still process queued ops, but don't start new cabins
                drain(level);
                return;
            }

            // Check players
            for (ServerPlayer player : level.players()) {
                if (player == null) continue;
                BlockPos ppos = player.blockPosition();
                // Check if in forest biome
                if (!isForestBiome(level, ppos)) continue;

                long rx = Math.floorDiv(ppos.getX(), REGION);
                long rz = Math.floorDiv(ppos.getZ(), REGION);
                long key = (rx << 32) | (rz & 0xffffffffL);
                if (BUILT.containsKey(key) || QUEUED.contains(key)) continue;

                // Hash to decide if this region has cabin
                int hash = hashRegion(rx, rz);
                if ((hash % 100) >= CABIN_PERCENT) {
                    BUILT.put(key, false);
                    continue;
                }

                // Plan cabin
                int cx = (int)(rx * REGION + REGION / 2 + (hash % 80) - 40);
                int cz = (int)(rz * REGION + REGION / 2 + ((hash >> 8) % 80) - 40);
                int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
                if (groundY < 60 || groundY > 120) {
                    BUILT.put(key, false);
                    continue;
                }

                // Ensure forest still
                BlockPos checkPos = new BlockPos(cx, groundY, cz);
                if (!isForestBiome(level, checkPos)) {
                    BUILT.put(key, false);
                    continue;
                }

                // Check if already has cabin nearby (avoid too close)
                if (hasNearbyCabin(level, cx, cz)) {
                    BUILT.put(key, false);
                    continue;
                }

                // Plan cabin ops
                ArrayDeque<long[]> plan = new ArrayDeque<>();
                planCabin(plan, cx, groundY, cz, hash);
                if (plan.size() > MAX_PLAN_OPS) {
                    BUILT.put(key, false);
                    continue;
                }

                // Queue
                QUEUED.add(key);
                OPS.addAll(plan);
                cabins++;
                // Notify player - creepy
                if (Math.random() < 0.4) {
                    player.sendSystemMessage(Component.literal("§8§oYou feel watched... something stirs in the forest at midnight..."));
                }
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
                // Don't overwrite if already built and not air
                if (level.getBlockState(pos).isAir() || palIdx == 0 || isReplaceable(level, pos)) {
                    level.setBlock(pos, state, 2);
                    placed++;
                }
                ops++;
            }
            // If queue empty, clear built markers for retry? No, keep built
        } catch (Throwable ignored) {}
    }

    private static void planCabin(ArrayDeque<long[]> plan, int cx, int groundY, int cz, int hash) {
        // Cabin dimensions - cozy but creepy, 9x9x6
        int width = 9 + (hash % 3);
        int depth = 9 + ((hash >> 4) % 3);
        int height = 6;
        int baseY = groundY;

        // Foundation - logs
        for (int x = -width/2; x <= width/2; x++) {
            for (int z = -depth/2; z <= depth/2; z++) {
                int wx = cx + x;
                int wz = cz + z;
                // Floor - planks with some moss
                int floorY = baseY;
                if (x == -width/2 || x == width/2 || z == -depth/2 || z == depth/2) {
                    // Walls - logs at corners, planks elsewhere
                    for (int y = 0; y < height; y++) {
                        int wy = baseY + y;
                        if (y == 0) {
                            // Foundation logs
                            plan.add(new long[]{wx, wy, wz, LOG()});
                        } else if ((x == -width/2 && z == -depth/2) || (x == width/2 && z == -depth/2) || (x == -width/2 && z == depth/2) || (x == width/2 && z == depth/2)) {
                            // Corner logs
                            plan.add(new long[]{wx, wy, wz, LOG()});
                        } else if (y < height - 1 && (x == -width/2 || x == width/2 || z == -depth/2 || z == depth/2)) {
                            // Wall planks
                            if (y == 2 && x == 0 && z == depth/2) {
                                // Door position - will be locked door
                                continue; // Leave air for door
                            }
                            if (y == 2 && (Math.abs(x) == width/2 - 1 || Math.abs(z) == depth/2 - 1) && hash % 3 == 0) {
                                // Window - glass
                                plan.add(new long[]{wx, wy, wz, GLASS()});
                            } else {
                                plan.add(new long[]{wx, wy, wz, PLANKS()});
                            }
                        } else if (y == height - 1 && (x == -width/2 || x == width/2 || z == -depth/2 || z == depth/2)) {
                            // Top logs
                            plan.add(new long[]{wx, wy, wz, LOG()});
                        }
                    }
                }
                // Floor inside
                if (Math.abs(x) < width/2 && Math.abs(z) < depth/2) {
                    plan.add(new long[]{wx, baseY, wz, PLANKS()});
                }
            }
        }

        // Roof - slanted
        for (int x = -width/2 - 1; x <= width/2 + 1; x++) {
            for (int z = -depth/2 - 1; z <= depth/2 + 1; z++) {
                int wx = cx + x;
                int wz = cz + z;
                int roofY = baseY + height + Math.abs(x) / 2;
                if (Math.abs(x) <= width/2 + 1 && Math.abs(z) <= depth/2 + 1) {
                    // Roof planks with stairs for slope
                    if (x == -width/2 - 1 || x == width/2 + 1 || z == -depth/2 - 1 || z == depth/2 + 1) {
                        // Eaves
                        plan.add(new long[]{wx, roofY, wz, PLANKS()});
                    } else if (Math.abs(x) % 2 == 0) {
                        plan.add(new long[]{wx, roofY, wz, PLANKS()});
                    }
                }
            }
        }

        // Locked cabin door - special block
        int doorX = cx;
        int doorZ = cz + depth/2;
        int doorY = baseY + 1;
        plan.add(new long[]{doorX, doorY, doorZ, LOCKED_DOOR()});
        plan.add(new long[]{doorX, doorY + 1, doorZ, LOCKED_DOOR_TOP()});

        // Interiors - books, stories of devouring storms
        // Bookshelf wall
        for (int x = -width/2 + 1; x <= width/2 - 1; x++) {
            if (x == 0) continue; // Door area
            int wx = cx + x;
            int wz = cz - depth/2 + 1;
            int wy = baseY + 1;
            if (hash % 2 == 0) {
                plan.add(new long[]{wx, wy, wz, BOOKSHELF()});
                if (x % 2 == 0) {
                    plan.add(new long[]{wx, wy + 1, wz, BOOKSHELF()});
                }
            }
        }

        // Table with book - story of devouring storms
        int tableX = cx;
        int tableZ = cz;
        int tableY = baseY + 1;
        plan.add(new long[]{tableX, tableY, tableZ, TABLE()});
        plan.add(new long[]{tableX, tableY + 1, tableZ, STORY_BOOK()});

        // Chairs
        plan.add(new long[]{tableX + 1, tableY, tableZ, CHAIR()});
        plan.add(new long[]{tableX - 1, tableY, tableZ, CHAIR()});

        // Fireplace
        int fpX = cx + width/2 - 1;
        int fpZ = cz;
        int fpY = baseY + 1;
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0 && y > 0) continue; // Inside fireplace air
                    int wx = fpX + x;
                    int wz = fpZ + z;
                    int wy = fpY + y;
                    if (y == 0 || Math.abs(x) == 1 || Math.abs(z) == 1) {
                        plan.add(new long[]{wx, wy, wz, COBBLE()});
                    }
                }
            }
        }
        // Fire
        plan.add(new long[]{fpX, fpY + 1, fpZ, FIRE()});

        // Lanterns - dim light
        plan.add(new long[]{cx, baseY + 3, cz, LANTERN()});

        // Hidden chest with lore
        int chestX = cx - width/2 + 2;
        int chestZ = cz + depth/2 - 2;
        plan.add(new long[]{chestX, baseY + 1, chestZ, CHEST()});

        // Creepy details - cobwebs, etc
        if (hash % 3 == 0) {
            plan.add(new long[]{cx + 1, baseY + 3, cz + 1, COBWEB()});
        }

        // Mark as built after planning
        long key = ((long)Math.floorDiv(cx, REGION) << 32) | (Math.floorDiv(cz, REGION) & 0xffffffffL);
        BUILT.put(key, true);
    }

    private static boolean isForestBiome(ServerLevel level, BlockPos pos) {
        try {
            var biome = level.getBiome(pos).value();
            String biomeId = biome.toString().toLowerCase();
            return biomeId.contains("forest") || biomeId.contains("taiga") || biomeId.contains("birch") || biomeId.contains("dark");
        } catch (Throwable t) {
            return true; // Assume forest for simplicity
        }
    }

    private static boolean hasNearbyCabin(ServerLevel level, int x, int z) {
        // Check nearby regions for existing cabin
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
            return state.isAir() || state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.TALL_GRASS || state.getBlock() == Blocks.FERN || state.getBlock() == Blocks.DEAD_BUSH || state.getBlock() == Blocks.VINE;
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
        synchronized (McsmCabinStructure.class) {
            if (palette != null) return palette;
            BlockState[] p = new BlockState[32];
            p[0] = Blocks.AIR.defaultBlockState();
            p[1] = Blocks.COBBLESTONE.defaultBlockState();
            p[2] = Blocks.OAK_PLANKS.defaultBlockState();
            p[3] = Blocks.OAK_LOG.defaultBlockState();
            p[4] = Blocks.GLASS.defaultBlockState();
            p[5] = Blocks.BOOKSHELF.defaultBlockState();
            p[6] = Blocks.CRAFTING_TABLE.defaultBlockState();
            p[7] = Blocks.OAK_STAIRS.defaultBlockState();
            p[8] = Blocks.CHEST.defaultBlockState();
            p[9] = Blocks.FIRE.defaultBlockState();
            p[10] = Blocks.LANTERN.defaultBlockState();
            p[11] = Blocks.COBWEB.defaultBlockState();
            p[12] = Blocks.OAK_DOOR.defaultBlockState();
            p[13] = Blocks.IRON_DOOR.defaultBlockState(); // Locked door placeholder
            p[14] = Blocks.OAK_FENCE.defaultBlockState();
            p[15] = Blocks.OAK_SLAB.defaultBlockState();
            // Extra for story book - use lectern with book
            p[16] = Blocks.LECTERN.defaultBlockState();
            p[17] = Blocks.BARREL.defaultBlockState();
            p[18] = Blocks.CAMPFIRE.defaultBlockState();
            p[19] = Blocks.SOUL_LANTERN.defaultBlockState();
            p[20] = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            palette = p;
            return p;
        }
    }

    private static int AIR() { return 0; }
    private static int COBBLE() { return 1; }
    private static int PLANKS() { return 2; }
    private static int LOG() { return 3; }
    private static int GLASS() { return 4; }
    private static int BOOKSHELF() { return 5; }
    private static int TABLE() { return 6; }
    private static int CHAIR() { return 7; }
    private static int CHEST() { return 8; }
    private static int FIRE() { return 9; }
    private static int LANTERN() { return 10; }
    private static int COBWEB() { return 11; }
    private static int LOCKED_DOOR() { return 13; }
    private static int LOCKED_DOOR_TOP() { return 13; }
    private static int STORY_BOOK() { return 16; }
}
