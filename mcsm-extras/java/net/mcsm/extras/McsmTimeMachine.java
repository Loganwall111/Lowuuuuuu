package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 NEXT-GEN Phase 4 - Time Machine: Industrial era, time travel mechanics
 * Time Machine block crafted with Redstone + time reality core (found in void dimension giant castle at very bottom).
 * Constructing time castle opens elevator you walk into, clicking knob activates turn, click date/time and go back.
 * Eras: beginning of Earth when God first created world, dinosaur ages, cyberpunk future with spaceships, Roman empires, kings, Knights medieval.
 * Physical active building that summons, you can walk in them, watch rest of world as time passes.
 * Realistic time warp effect: 3D colored lighting affect over camera blur blue/green/black shifting, seamless portal.
 * Time router at top shows time rewind in real time when looking at elevator window.
 */
public final class McsmTimeMachine {

    public static final int OPS_PER_TICK = 1800;
    public static final int MAX_PLAN_OPS = 400000;

    private static final Map<Long, Boolean> BUILT_CASTLES = new ConcurrentHashMap<>();
    private static final Set<Long> QUEUED = ConcurrentHashMap.newKeySet();
    private static final ArrayDeque<long[]> OPS = new ArrayDeque<>();
    private static long placed;

    // Time Machine states
    public static final String[] ERAS = {
        "BEGINNING_EARTH",      // When God first created world
        "DINOSAUR_AGE",         // Dinosaur ages
        "PREHISTORIC",          // Prehistoric
        "ANCIENT_EGYPT",        // Ancient Egypt
        "ROMAN_EMPIRE",         // Roman empires
        "MEDIEVAL_KINGS",       // Kings, Knights medieval
        "INDUSTRIAL_ERA",       // Industrial era
        "WORLD_WAR_ERA",        // World wars
        "PRESENT_DAY",          // Present
        "CYBERPUNK_FUTURE",     // Far future cyberpunk spaceships
        "SPACE_AGE"             // Orbiting Earth, rockets to other planets
    };

    private static volatile BlockState[] palette;

    private McsmTimeMachine() {}

    public static void boot() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmTimeMachine::tick);
        } catch (Throwable ignored) {}
    }

    private static void tick(ServerLevel level) {
        try {
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            // Void dimension - giant castle at very bottom with time reality core
            if (dim.contains("void") || dim.contains("mcsm:void")) {
                tickVoidCastle(level);
            }
            // Overworld - time castle elevator buildings
            if (dim.contains("overworld") || dim.equals("minecraft:overworld")) {
                tickTimeCastles(level);
            }
            drain(level);
        } catch (Throwable ignored) {}
    }

    private static void tickVoidCastle(ServerLevel level) {
        try {
            // Giant castle at very bottom of void dimension - contains time reality core
            // Only build once per void dimension
            long key = level.dimension().identifier().hashCode();
            if (BUILT_CASTLES.containsKey(key)) return;

            // Find bottom - void dimension min_y -64, but we want at very bottom
            int castleX = 0;
            int castleZ = 0;
            int castleY = -60; // Very bottom

            // Check if already built (look for core)
            BlockPos corePos = new BlockPos(castleX, castleY + 10, castleZ);
            if (!level.getBlockState(corePos).isAir()) {
                BUILT_CASTLES.put(key, true);
                return;
            }

            ArrayDeque<long[]> plan = new ArrayDeque<>();
            planVoidCastle(plan, castleX, castleY, castleZ, 12345);
            if (plan.size() > MAX_PLAN_OPS) return;

            OPS.addAll(plan);
            BUILT_CASTLES.put(key, true);

        } catch (Throwable ignored) {}
    }

    private static void tickTimeCastles(ServerLevel level) {
        try {
            // Time castles can be built by players using Time Machine block
            // For now, auto-spawn one near world spawn for demo, and handle elevator logic
            for (ServerPlayer player : level.players()) {
                if (player == null) continue;
                // Check if player near time castle elevator
                BlockPos ppos = player.blockPosition();
                // Look for time machine blocks nearby
                for (int dx = -8; dx <= 8; dx++) {
                    for (int dy = -4; dy <= 6; dy++) {
                        for (int dz = -8; dz <= 8; dz++) {
                            BlockPos check = ppos.offset(dx, dy, dz);
                            var state = level.getBlockState(check);
                            if (state.getBlock() == Blocks.BEACON || state.getBlock() == Blocks.LODESTONE) { // Placeholder for time machine
                                // Player in time castle elevator - show time router
                                if (player.isShiftKeyDown() && (level.getGameTime() % 40 == 0)) {
                                    showTimeRouter(player, check);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void showTimeRouter(ServerPlayer player, BlockPos machinePos) {
        try {
            long time = player.level().getGameTime() % 24000;
            String currentEra = getEraForTime(time);
            player.sendSystemMessage(Component.literal("§b§l[TIME ROUTER] §fCurrent: " + currentEra + " §7| §eTime: " + time + " §7| §aLooking at elevator window..."));
            player.sendSystemMessage(Component.literal("§8Time rewinding... §d" + (24000 - time) + " ticks to midnight §8| §bEras: BEGINNING_EARTH, DINOSAUR_AGE, ROMAN_EMPIRE, MEDIEVAL_KINGS, CYBERPUNK_FUTURE"));
            // Warp effect would be triggered client-side via packet
            if (Math.random() < 0.3) {
                player.sendSystemMessage(Component.literal("§5§lWarp effect: §9Blue §aGreen §0Black §fshifting... seamless portal opening... universes passing by..."));
            }
        } catch (Throwable ignored) {}
    }

    private static String getEraForTime(long time) {
        if (time < 2000) return "BEGINNING_EARTH";
        if (time < 4000) return "DINOSAUR_AGE";
        if (time < 6000) return "ROMAN_EMPIRE";
        if (time < 8000) return "MEDIEVAL_KINGS";
        if (time < 10000) return "INDUSTRIAL_ERA";
        if (time < 12000) return "PRESENT_DAY";
        if (time < 14000) return "CYBERPUNK_FUTURE";
        if (time < 16000) return "SPACE_AGE";
        return "PRESENT_DAY";
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
                if (level.getBlockState(pos).isAir() || palIdx == 0) {
                    level.setBlock(pos, state, 2);
                    placed++;
                }
                ops++;
            }
        } catch (Throwable ignored) {}
    }

    private static void planVoidCastle(ArrayDeque<long[]> plan, int cx, int cy, int cz, int hash) {
        // Giant castle at very bottom of void - contains time reality core
        int width = 32;
        int depth = 32;
        int height = 28;

        // Foundation - obsidian + crying obsidian
        for (int x = -width/2; x <= width/2; x++) {
            for (int z = -depth/2; z <= depth/2; z++) {
                int wx = cx + x;
                int wz = cz + z;
                // Floor
                plan.add(new long[]{wx, cy, wz, OBSIDIAN()});
                // Walls
                if (Math.abs(x) == width/2 || Math.abs(z) == depth/2) {
                    for (int y = 1; y < height; y++) {
                        int wy = cy + y;
                        if (y < height - 1) {
                            if ((x + y) % 3 == 0 || (z + y) % 3 == 0) {
                                plan.add(new long[]{wx, wy, wz, CRYING_OBSIDIAN()});
                            } else {
                                plan.add(new long[]{wx, wy, wz, OBSIDIAN()});
                            }
                        } else {
                            plan.add(new long[]{wx, wy, wz, OBSIDIAN()});
                        }
                    }
                }
            }
        }

        // Towers at corners - extremely tall like castle
        int[][] corners = {{-width/2, -depth/2}, {width/2, -depth/2}, {-width/2, depth/2}, {width/2, depth/2}};
        for (int[] corner : corners) {
            int tx = cx + corner[0];
            int tz = cz + corner[1];
            for (int y = 1; y < height + 12; y++) {
                int wy = cy + y;
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (Math.abs(x) == 2 || Math.abs(z) == 2 || y > height) {
                            int wx = tx + x;
                            int wz = tz + z;
                            plan.add(new long[]{wx, wy, wz, OBSIDIAN()});
                        }
                    }
                }
            }
        }

        // Central chamber with time reality core
        int coreX = cx;
        int coreY = cy + 10;
        int coreZ = cz;
        // Pedestal
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                int wx = coreX + x;
                int wz = coreZ + z;
                plan.add(new long[]{wx, coreY - 1, wz, GOLD_BLOCK()});
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    plan.add(new long[]{wx, coreY, wz, GOLD_BLOCK()});
                }
            }
        }
        // Time reality core - beacon as placeholder (will be custom block)
        plan.add(new long[]{coreX, coreY, coreZ, TIME_CORE()});
        plan.add(new long[]{coreX, coreY + 1, coreZ, BEACON()});

        // Elevator shaft - opens up
        for (int y = 1; y < 20; y++) {
            int wy = cy + y;
            // Shaft walls - glass
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) == 1 || Math.abs(z) == 1) {
                        int wx = cx + x + width/2 + 3;
                        int wz = cz + z;
                        if (x == 0 && z == 0) continue;
                        plan.add(new long[]{wx, wy, wz, GLASS()});
                    }
                }
            }
        }

        // Knob to activate - lever
        plan.add(new long[]{cx + width/2 + 4, cy + 2, cz, LEVER()});

        // Lore - books about time travel
        for (int x = -width/2 + 2; x < -width/2 + 6; x++) {
            int wx = cx + x;
            int wz = cz - depth/2 + 2;
            plan.add(new long[]{wx, cy + 1, wz, BOOKSHELF()});
            plan.add(new long[]{wx, cy + 2, wz, BOOKSHELF()});
        }
    }

    public static void planTimeCastleElevator(ArrayDeque<long[]> plan, int cx, int cy, int cz, int hash, String era) {
        // Physical active building you walk into - elevator that shows time rewinding
        int width = 7;
        int depth = 7;
        int height = 12;

        // Base
        for (int x = -width/2; x <= width/2; x++) {
            for (int z = -depth/2; z <= depth/2; z++) {
                int wx = cx + x;
                int wz = cz + z;
                plan.add(new long[]{wx, cy, wz, IRON_BLOCK()});
                if (Math.abs(x) == width/2 || Math.abs(z) == depth/2) {
                    for (int y = 1; y < height; y++) {
                        int wy = cy + y;
                        if (y < height - 1) {
                            if ((x == -width/2 && z == 0 && y == 1) || (x == width/2 && z == 0 && y == 1)) {
                                continue; // Door
                            }
                            plan.add(new long[]{wx, wy, wz, IRON_BLOCK()});
                        } else {
                            plan.add(new long[]{wx, wy, wz, GLASS()}); // Roof window to see time router
                        }
                    }
                }
            }
        }

        // Elevator interior - time router at top shows time rewind
        int routerY = cy + height - 1;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                int wx = cx + x;
                int wz = cz + z;
                plan.add(new long[]{wx, routerY, wz, REDSTONE_LAMP()});
            }
        }

        // Knob - button to activate turn, click date/time
        plan.add(new long[]{cx, cy + 2, cz + depth/2, BUTTON()});
        plan.add(new long[]{cx, cy + 3, cz, CLOCK()});

        // Era-specific decoration
        switch (era) {
            case "DINOSAUR_AGE" -> {
                plan.add(new long[]{cx + 1, cy + 1, cz, FOSSIL()});
            }
            case "ROMAN_EMPIRE", "MEDIEVAL_KINGS" -> {
                plan.add(new long[]{cx - 1, cy + 1, cz, ARMOR_STAND()});
            }
            case "CYBERPUNK_FUTURE" -> {
                plan.add(new long[]{cx, cy + 1, cz + 1, BEACON()});
            }
        }

        // Warp effect blocks - blue/green/black shifting colors
        plan.add(new long[]{cx, cy + 1, cz, WARP_BLOCK()});
    }

    private static BlockState[] palette() {
        if (palette != null) return palette;
        synchronized (McsmTimeMachine.class) {
            if (palette != null) return palette;
            BlockState[] p = new BlockState[32];
            p[0] = Blocks.AIR.defaultBlockState();
            p[1] = Blocks.OBSIDIAN.defaultBlockState();
            p[2] = Blocks.CRYING_OBSIDIAN.defaultBlockState();
            p[3] = Blocks.GOLD_BLOCK.defaultBlockState();
            p[4] = Blocks.BEACON.defaultBlockState();
            p[5] = Blocks.GLASS.defaultBlockState();
            p[6] = Blocks.LEVER.defaultBlockState();
            p[7] = Blocks.BOOKSHELF.defaultBlockState();
            p[8] = Blocks.IRON_BLOCK.defaultBlockState();
            p[9] = Blocks.REDSTONE_LAMP.defaultBlockState();
            p[10] = Blocks.STONE_BUTTON.defaultBlockState();
            p[11] = Blocks.REDSTONE_BLOCK.defaultBlockState();
            p[12] = Blocks.BONE_BLOCK.defaultBlockState();
            p[13] = Blocks.SCAFFOLDING.defaultBlockState();
            p[14] = Blocks.SEA_LANTERN.defaultBlockState();
            p[15] = Blocks.LODESTONE.defaultBlockState();
            p[16] = Blocks.RESPAWN_ANCHOR.defaultBlockState();
            p[17] = Blocks.CRYING_OBSIDIAN.defaultBlockState();
            p[18] = Blocks.GILDED_BLACKSTONE.defaultBlockState();
            p[19] = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            p[20] = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            palette = p;
            return p;
        }
    }

    private static int AIR() { return 0; }
    private static int OBSIDIAN() { return 1; }
    private static int CRYING_OBSIDIAN() { return 2; }
    private static int GOLD_BLOCK() { return 3; }
    private static int BEACON() { return 4; }
    private static int GLASS() { return 5; }
    private static int LEVER() { return 6; }
    private static int BOOKSHELF() { return 7; }
    private static int IRON_BLOCK() { return 8; }
    private static int REDSTONE_LAMP() { return 9; }
    private static int BUTTON() { return 10; }
    private static int CLOCK() { return 11; }
    private static int FOSSIL() { return 12; }
    private static int ARMOR_STAND() { return 13; }
    private static int WARP_BLOCK() { return 14; }
    private static int TIME_CORE() { return 15; }
    private static int LODESTONE() { return 15; }
}
