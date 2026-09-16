package net.mcsm.extras;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Devouring Storms: the abandoned cities (mandate D.8, phase 2).
 *
 * "The abandoned city generation actually working ... fully into the game."
 * This is that generator. It builds ruined city districts inside the decayed
 * reality -- real blocks, placed in the real world, at the real coordinates a
 * player walks through -- and it is deterministic: the same region of the world
 * always holds the same district, so a city is a place, not a random event.
 *
 * HOW IT WORKS
 * ------------
 *   * The world is divided into {@link #REGION} blocks square regions. A pure
 *     hash of the region coordinates decides whether that region holds a city
 *     (62% of them do) and everything inside it.
 *   * A region that holds one gets a plaza with a rift monument, nine plots in
 *     a 3x3 grid around it separated by streets, each plot carrying a building
 *     archetype (tower shell, warehouse, half-collapsed house, hospital, radio
 *     mast, or a crater), street furniture, and a ruined highway running out of
 *     the plaza.
 *   * Building is TIME-SLICED, exactly like the base mod's own town builder:
 *     planning a district produces a list of block placements, and only
 *     {@link #OPS_PER_TICK} of them are written per server tick. A district
 *     therefore rises over a couple of seconds of play without ever stalling a
 *     frame, and the player sees it happen.
 *   * Work only starts when a player is within {@link #ACTIVATE} blocks, so an
 *     unreachable corner of an infinite dimension costs nothing.
 *   * A district is recorded as standing by the rift monument at its centre:
 *     the marker block is part of the world, so a reloaded server does not
 *     rebuild what is already there (and a district whose monument was mined
 *     out is rebuilt rather than lost).
 *
 * WHY EVERY CALL HERE IS SAFE AGAINST THE FROZEN BASE JAR
 * ------------------------------------------------------
 *   * {@code level.setBlock(pos, state, 2)}, {@code level.getBlockState(pos)}
 *     and {@code Blocks.AIR} are the base mod's own world-building idiom
 *     (BowelsEndRoom, BowelsBackHall -- flag 2 = client update only, which is
 *     the cheap non-neighbour-updating flag worldgen uses).
 *   * {@code ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) level -> ...)}
 *     is literally how DabyWitherStormMod registers McsmWorldgen.tick, so the
 *     event, its SAM name and its single-ServerLevel lambda are all proven by
 *     the shipped jar's own bytecode -- no mixin, no key binding, no guess.
 *   * Nothing here touches a block entity, a structure template or an entity
 *     type, so no new model/registry surface is needed to ship it.
 */
public final class McsmCities {

    /** Region size in blocks. One region holds at most one district. */
    public static final int REGION = 256;
    /** Share of regions that hold a district, in percent. */
    public static final int CITY_PERCENT = 62;
    /** Player distance that starts a district's construction. */
    public static final int ACTIVATE = 352;
    /** Block placements written per server tick while a district is going up. */
    public static final int OPS_PER_TICK = 1200;
    /** Hard cap on one district's plan, so a pathological seed cannot run away. */
    public static final int MAX_PLAN_OPS = 90000;

    private static final int CELL = 64;          // plot spacing inside a region
    private static final int HALF_PLOT = 25;     // plot half-width
    private static final int FALLBACK_GROUND = -15; // top of the flat generator

    private static final Map<Long, Boolean> BUILT = new ConcurrentHashMap<>();
    private static final Set<Long> QUEUED = ConcurrentHashMap.newKeySet();
    private static final ArrayDeque<long[]> OPS = new ArrayDeque<>();
    private static long placed;
    private static long districts;

    // palette indices (the state array is built on first use, see palette())
    private static final int AIR = 0;
    private static final int STONE = 1;
    private static final int COBBLE = 2;
    private static final int BRICKS = 3;
    private static final int TILES = 4;
    private static final int HOLLOW = 5;
    private static final int PLATE = 6;
    private static final int GRATE = 7;
    private static final int ROAD = 8;
    private static final int GLASS = 9;
    private static final int LAMP = 10;
    private static final int PLANKS = 11;
    private static final int LOG = 12;
    private static final int RIB = 13;
    private static final int TENDON = 14;
    private static final int FLESH = 15;
    private static final int CRYSTAL = 16;
    private static final int VOIDCORE = 17;
    private static final int ANCHOR = 18;
    private static final int CRATE = 19;
    private static final int SUPPLY = 20;
    private static final int VAULT = 21;
    private static final int SAND = 22;
    private static final int LEAVES = 23;
    private static final int PALETTE_SIZE = 24;

    private static volatile BlockState[] palette;

    private McsmCities() {
    }

    // ---------------------------------------------------------------------
    // Wiring
    // ---------------------------------------------------------------------

    /** Called from the mod's own initialiser, next to the content pack. */
    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmCities::tick);
            System.out.println("[ds] decayed-reality districts armed (1 district per "
                    + REGION + " blocks, " + CITY_PERCENT + "% of regions)");
        } catch (Throwable t) {
            System.err.println("[ds] district generation could not hook the level tick: " + t);
        }
    }

    // ---------------------------------------------------------------------
    // The per-level tick: queue what is near a player, write a slice of it
    // ---------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.decayedReality || !McsmExtrasConfig.abandonedCities) {
                return;
            }
            if (!level.dimension().equals(McsmReality.DECAYED_REALITY)) {
                return;
            }
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) {
                return;
            }
            if (OPS.isEmpty()) {
                for (ServerPlayer player : players) {
                    enqueueNear(level, player);
                }
                if (OPS.isEmpty()) {
                    return;
                }
            }
            int budget = OPS_PER_TICK;
            BlockState[] states = palette();
            while (budget-- > 0) {
                long[] op = OPS.poll();
                if (op == null) {
                    break;
                }
                BlockState state = states[(int) op[3]];
                if (state == null) {
                    continue;
                }
                level.setBlock(new BlockPos((int) op[0], (int) op[1], (int) op[2]), state, 2);
                placed++;
            }
        } catch (Throwable t) {
            System.err.println("[ds] district tick failed: " + t);
        }
    }

    private static void enqueueNear(ServerLevel level, ServerPlayer player) {
        if (player == null) {
            return;
        }
        int px = (int) Math.floor(player.getX());
        int pz = (int) Math.floor(player.getZ());
        int prx = Math.floorDiv(px, REGION);
        int prz = Math.floorDiv(pz, REGION);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int rx = prx + dx;
                int rz = prz + dz;
                long key = regionKey(rx, rz);
                if (!hasCity(rx, rz) || BUILT.containsKey(key) || QUEUED.contains(key)) {
                    continue;
                }
                int ox = rx * REGION + REGION / 2;
                int oz = rz * REGION + REGION / 2;
                double far = Math.hypot(player.getX() - ox, player.getZ() - oz);
                if (far > ACTIVATE) {
                    continue;
                }
                QUEUED.add(key);
                try {
                    planCity(level, rx, rz, ox, oz);
                } catch (Throwable t) {
                    System.err.println("[ds] district plan failed at " + ox + "," + oz + ": " + t);
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // Determinism: the same region always holds the same district
    // ---------------------------------------------------------------------

    private static long regionKey(int rx, int rz) {
        return ((long) rx << 32) ^ (rz & 0xFFFFFFFFL);
    }

    /** splitmix64, so neighbouring regions do not produce neighbouring cities. */
    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public static boolean hasCity(int rx, int rz) {
        return Math.floorMod(mix(regionKey(rx, rz) * 0x2545F4914F6CDD1DL), 100L) < CITY_PERCENT;
    }

    /**
     * The nearest district centre, as {dx, dz, distanceSquared}, or null when
     * none is loaded. Pure arithmetic -- the client can call it for the HUD and
     * the server for the arrival message without touching world state.
     */
    public static int[] nearestCity(int x, int z) {
        int prx = Math.floorDiv(x, REGION);
        int prz = Math.floorDiv(z, REGION);
        int[] best = null;
        long bestD = Long.MAX_VALUE;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int rx = prx + dx;
                int rz = prz + dz;
                if (!hasCity(rx, rz)) {
                    continue;
                }
                int ox = rx * REGION + REGION / 2;
                int oz = rz * REGION + REGION / 2;
                long ddx = ox - (long) x;
                long ddz = oz - (long) z;
                long d = ddx * ddx + ddz * ddz;
                if (d < bestD) {
                    bestD = d;
                    best = new int[]{(int) ddx, (int) ddz, (int) Math.sqrt((double) d)};
                }
            }
        }
        return best;
    }

    /** One line of guidance for chat / the console, e.g. "240 blocks north-east". */
    public static String guidance(int x, int z) {
        int[] n = nearestCity(x, z);
        if (n == null) {
            return "no ruins within reach";
        }
        return n[2] + " blocks " + compass(n[0], n[1]);
    }

    private static String compass(int dx, int dz) {
        String ew = dx > 0 ? "east" : (dx < 0 ? "west" : "");
        String ns = dz > 0 ? "south" : (dz < 0 ? "north" : "");
        if (ew.isEmpty()) {
            return ns.isEmpty() ? "right here" : ns;
        }
        if (ns.isEmpty()) {
            return ew;
        }
        return ns + "-" + ew;
    }

    /** Diagnostics for the console: how much of the world we have built. */
    public static String stats() {
        return districts + " districts / " + placed + " blocks placed / "
                + (OPS.isEmpty() ? "queue idle" : OPS.size() + " placements queued");
    }

    // ---------------------------------------------------------------------
    // Planning a district
    // ---------------------------------------------------------------------

    private static void planCity(ServerLevel level, int rx, int rz, int ox, int oz) {
        long key = regionKey(rx, rz);
        int ground = groundAt(level, ox, oz);
        if (level.getBlockState(new BlockPos(ox, ground + 4, oz)).getBlock() == McsmContent.RIFT_ANCHOR) {
            BUILT.put(key, Boolean.TRUE);   // already standing: nothing to do
            return;
        }
        Rng rng = new Rng(mix(key * 0x9E3779B97F4A7C15L));
        Plan plan = new Plan();

        planPlaza(plan, rng, ox, oz, ground);
        planHighway(plan, rng, ox, oz, ground);

        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                if (cx == 0 && cz == 0) {
                    continue; // the plaza owns the centre
                }
                int bx = ox + cx * CELL;
                int bz = oz + cz * CELL;
                int kind = rng.range(100);
                if (kind < 26) {
                    planTower(plan, rng, bx, bz, ground, 18 + rng.range(6), 18 + rng.range(6),
                            22 + rng.range(30));
                } else if (kind < 46) {
                    planWarehouse(plan, rng, bx, bz, ground, 32 + rng.range(6), 22 + rng.range(6),
                            8 + rng.range(5));
                } else if (kind < 66) {
                    planHouse(plan, rng, bx, bz, ground);
                } else if (kind < 80) {
                    planHospital(plan, rng, bx, bz, ground, 24 + rng.range(4), 18 + rng.range(8));
                } else if (kind < 90) {
                    planRadio(plan, rng, bx, bz, ground, 30 + rng.range(16));
                } else {
                    planCrater(plan, rng, bx, bz, ground);
                }
                planStreet(plan, rng, bx, bz, ground);
            }
        }

        OPS.addAll(plan.ops);
        districts++;
        System.out.println("[ds] raising a district at " + ox + "," + oz + " ("
                + plan.ops.size() + " placements)");
    }

    /** Highest solid block in a column, so a district always sits on the ground. */
    private static int groundAt(ServerLevel level, int x, int z) {
        try {
            for (int y = 200; y > level.getMinY(); y--) {
                if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                    return y;
                }
            }
        } catch (Throwable ignored) {
            // fall through to the flat generator's known top
        }
        return FALLBACK_GROUND;
    }

    // ---------------------------------------------------------------------
    // The pieces
    // ---------------------------------------------------------------------

    /** Plaza: cracked road, lamp posts, a rift monument (and the district marker). */
    private static void planPlaza(Plan p, Rng r, int ox, int oz, int ground) {
        int half = 16;
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                if (Math.abs(x) + Math.abs(z) > half + 6) {
                    continue;
                }
                if (r.chance(12)) {
                    continue; // the road is broken up
                }
                p.put(ox + x, ground, oz + z, r.chance(10) ? PLATE : ROAD);
            }
        }
        for (int i = 0; i < 4; i++) {
            int px = ox + (i % 2 == 0 ? -12 : 12);
            int pz = oz + (i < 2 ? -12 : 12);
            p.put(px, ground + 1, pz, PLATE);
            p.put(px, ground + 2, pz, PLATE);
            p.put(px, ground + 3, pz, PLATE);
            p.put(px, ground + 4, pz, LAMP);
        }
        // monument: the marker block sits on top of it, see planCity()
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                p.put(ox + x, ground + 1, oz + z, PLATE);
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                p.put(ox + x, ground + 2, oz + z, ANCHOR);
                p.put(ox + x, ground + 3, oz + z, VOIDCORE);
            }
        }
        p.put(ox, ground + 4, oz, ANCHOR);            // <- the marker
        p.put(ox + 3, ground + 1, oz + 3, VAULT);
        p.put(ox - 3, ground + 1, oz - 2, SUPPLY);
        p.put(ox - 3, ground + 1, oz + 2, CRATE);
        p.put(ox + 3, ground + 1, oz - 3, CRATE);
        for (int i = 0; i < 26; i++) {
            int x = ox + r.range(half * 2 + 1) - half;
            int z = oz + r.range(half * 2 + 1) - half;
            p.put(x, ground + 1, z, r.chance(40) ? STONE : (r.chance(50) ? COBBLE : RIB));
        }
    }

    /** A ruined highway leaving the plaza, with gaps and wreckage. */
    private static void planHighway(Plan p, Rng r, int ox, int oz, int ground) {
        int length = 124;
        int half = 3;
        for (int d = 18; d < length; d++) {
            if (r.chance(13)) {
                continue; // collapsed span
            }
            for (int w = -half; w <= half; w++) {
                p.put(ox + d, ground, oz + w, r.chance(8) ? PLATE : ROAD);
            }
            if (r.chance(6)) {
                p.put(ox + d, ground + 1, oz - half - 1, PLATE);
                p.put(ox + d, ground + 1, oz + half + 1, PLATE);
            }
            if (r.chance(3)) {
                p.put(ox + d, ground + 1, oz + r.range(5) - 2, r.chance(50) ? PLATE : GRATE);
            }
        }
    }

    /** Street furniture along a plot's edge. */
    private static void planStreet(Plan p, Rng r, int cx, int cz, int ground) {
        int edge = HALF_PLOT + 7;
        for (int i = -1; i <= 1; i++) {
            if (r.chance(45)) {
                continue;
            }
            int x = cx + i * 12;
            int z = cz + edge;
            p.put(x, ground + 1, z, PLATE);
            p.put(x, ground + 2, z, PLATE);
            p.put(x, ground + 3, z, r.chance(70) ? LAMP : GRATE);
        }
        for (int i = 0; i < 14; i++) {
            int x = cx + r.range(2 * edge) - edge;
            int z = cz + r.range(2 * edge) - edge;
            if (r.chance(50)) {
                p.put(x, ground + 1, z, r.chance(50) ? COBBLE : HOLLOW);
            }
        }
    }

    /** A tower shell: window bands, floors every five levels, broken roof. */
    private static void planTower(Plan p, Rng r, int cx, int cz, int ground, int w, int d, int h) {
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        for (int y = 0; y < h; y++) {
            boolean band = (y % 5) == 4;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    if (x != x0 && x != x1 && z != z0 && z != z1) {
                        continue;
                    }
                    if (band) {
                        if (r.chance(55)) {
                            p.put(x, ground + y, z, GLASS);
                        }
                        continue;
                    }
                    int top = h - 1 - y;
                    if (r.chance(top <= 2 ? 30 : 8)) {
                        continue; // missing wall block
                    }
                    int m = r.range(10);
                    p.put(x, ground + y, z, m < 6 ? BRICKS : (m < 9 ? TILES : HOLLOW));
                }
            }
            if (y > 0 && y % 5 == 0) {
                for (int x = x0 + 1; x < x1; x++) {
                    for (int z = z0 + 1; z < z1; z++) {
                        p.put(x, ground + y, z, r.chance(12) ? PLATE : TILES);
                    }
                }
                int sx = x0 + 2 + r.range(Math.max(1, w - 5));
                int sz = z0 + 2 + r.range(Math.max(1, d - 5));
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        p.put(sx + i, ground + y, sz + j, AIR);   // stairwell
                    }
                }
                if (r.chance(70)) {
                    p.put(cx, ground + y + 1, cz, r.chance(30) ? SUPPLY : CRATE);
                }
                for (int i = 0; i < 3; i++) {
                    p.put(cx + r.range(w - 3) - w / 2 + 1, ground + y + 1,
                            cz + r.range(d - 3) - d / 2 + 1,
                            r.chance(50) ? STONE : (r.chance(50) ? COBBLE : FLESH));
                }
            }
        }
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!r.chance(38)) {
                    p.put(x, ground + h, z, r.chance(70) ? TILES : PLATE);
                }
            }
        }
        p.put(cx + r.range(3) - 1, ground + h + 1, cz + r.range(3) - 1, CRYSTAL);
    }

    /** A warehouse: a big broken box, a doorway, roofs with holes, crate rows. */
    private static void planWarehouse(Plan p, Rng r, int cx, int cz, int ground, int w, int d, int h) {
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        for (int y = 0; y < h; y++) {
            for (int x = x0; x <= x1; x++) {
                boolean doorway = y < 4 && Math.abs(x - cx) <= 3;
                for (int z = z0; z <= z1; z++) {
                    if (x != x0 && x != x1 && z != z0 && z != z1) {
                        continue;
                    }
                    if ((z == z0 || z == z1) && doorway) {
                        continue; // the way in
                    }
                    if (r.chance(y >= h - 2 ? 22 : 7)) {
                        continue;
                    }
                    int m = r.range(10);
                    p.put(x, ground + y, z, m < 5 ? PLATE : (m < 8 ? TILES : HOLLOW));
                }
            }
        }
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!r.chance(16)) {
                    p.put(x, ground + h, z, r.chance(60) ? PLATE : HOLLOW);
                }
                if (r.chance(85)) {
                    p.put(x, ground, z, r.chance(70) ? TILES : ROAD);
                }
            }
        }
        for (int row = 0; row < 4; row++) {
            int x = x0 + 3 + r.range(Math.max(1, w - 6));
            int z = z0 + 3 + r.range(Math.max(1, d - 6));
            p.put(x, ground + 1, z, r.chance(25) ? VAULT : (r.chance(45) ? SUPPLY : CRATE));
            p.put(x, ground + 1, z + 1, r.chance(60) ? CRATE : PLATE);
        }
        for (int i = 0; i < 18; i++) {
            p.put(x0 + r.range(w), ground + 1, z0 + r.range(d), r.chance(50) ? STONE : COBBLE);
        }
    }

    /** A half-collapsed house. */
    private static void planHouse(Plan p, Rng r, int cx, int cz, int ground) {
        int w = 14;
        int d = 12;
        int h = 6;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (edge) {
                    for (int y = 0; y < h; y++) {
                        if (r.chance(y >= h - 2 ? 25 : 9)) {
                            continue;
                        }
                        p.put(x, ground + y, z, r.chance(55) ? PLANKS : BRICKS);
                    }
                    if (r.chance(50)) {
                        p.put(x, ground + 1, z, r.chance(50) ? GLASS : PLANKS);
                    }
                } else if (!r.chance(30)) {
                    p.put(x, ground, z, PLANKS);
                }
                if (r.chance(45)) {
                    p.put(x, ground + h, z, r.chance(60) ? PLANKS : HOLLOW);
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            int lx = (i % 2 == 0 ? x0 : x1);
            int lz = (i < 2 ? z0 : z1);
            for (int y = 0; y < h + 1; y++) {
                if (r.chance(12)) {
                    continue;
                }
                p.put(lx, ground + y, lz, LOG);
            }
        }
        p.put(cx, ground + 1, cz, r.chance(35) ? SUPPLY : CRATE);
        for (int i = 0; i < 10; i++) {
            p.put(cx + r.range(w) - w / 2, ground + 1, cz + r.range(d) - d / 2,
                    r.chance(50) ? STONE : PLANKS);
        }
        for (int i = 0; i < 6; i++) {
            p.put(cx + r.range(w) - w / 2, ground + h + 1, cz + r.range(d) - d / 2, LEAVES);
        }
    }

    /** A hospital: window bands, floors, lamps, and the good crates. */
    private static void planHospital(Plan p, Rng r, int cx, int cz, int ground, int w, int h) {
        int d = w;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        for (int y = 0; y < h; y++) {
            boolean band = y % 4 == 3;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    if (x != x0 && x != x1 && z != z0 && z != z1) {
                        continue;
                    }
                    if (band && r.chance(70)) {
                        p.put(x, ground + y, z, GLASS);
                        continue;
                    }
                    if (r.chance(6)) {
                        continue;
                    }
                    p.put(x, ground + y, z, r.chance(75) ? TILES : BRICKS);
                }
            }
            if (y > 0 && y % 5 == 0) {
                for (int x = x0 + 1; x < x1; x++) {
                    for (int z = z0 + 1; z < z1; z++) {
                        p.put(x, ground + y, z, r.chance(10) ? PLATE : TILES);
                    }
                }
                p.put(cx - 4, ground + y + 1, cz, LAMP);
                p.put(cx + 4, ground + y + 1, cz, LAMP);
                if (r.chance(75)) {
                    p.put(cx + r.range(3) - 1, ground + y + 1, cz + r.range(3) - 1,
                            r.chance(30) ? VAULT : SUPPLY);
                }
            }
        }
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!r.chance(30)) {
                    p.put(x, ground + h, z, TILES);
                }
            }
        }
        p.put(cx, ground + h + 1, cz, GLASS);
    }

    /** A radio station with a mast: the tallest thing in the district. */
    private static void planRadio(Plan p, Rng r, int cx, int cz, int ground, int mast) {
        int w = 16;
        int h = 7;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - w / 2;
        int z1 = z0 + w - 1;
        for (int y = 0; y < h; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                    if (edge) {
                        if (r.chance(10)) {
                            continue;
                        }
                        p.put(x, ground + y, z, r.chance(60) ? TILES : HOLLOW);
                    } else if (y == 0) {
                        p.put(x, ground, z, PLATE);
                    }
                }
            }
        }
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!r.chance(25)) {
                    p.put(x, ground + h, z, PLATE);
                }
            }
        }
        p.put(cx, ground + 1, cz, SUPPLY);
        p.put(cx + 2, ground + 1, cz - 2, CRATE);
        for (int y = h; y < h + mast; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (r.chance(12)) {
                        continue;
                    }
                    p.put(cx + x, ground + y, cz + z, r.chance(70) ? PLATE : GRATE);
                }
            }
        }
        p.put(cx, ground + h + mast, cz, LAMP);
        for (int i = 0; i < 5; i++) {
            p.put(cx + r.range(5) - 2, ground + h + mast - 2, cz + r.range(5) - 2, GRATE);
        }
    }

    /** A crater where a building used to be: rim, debris, decay, one crate. */
    private static void planCrater(Plan p, Rng r, int cx, int cz, int ground) {
        int radius = 22;
        for (int a = 0; a < 360; a += 4) {
            double rad = Math.toRadians(a);
            int x = cx + (int) Math.round(Math.cos(rad) * radius);
            int z = cz + (int) Math.round(Math.sin(rad) * radius);
            int lift = r.range(3);
            for (int y = 0; y <= lift; y++) {
                if (r.chance(35)) {
                    continue;
                }
                p.put(x, ground + y, z, r.chance(50) ? COBBLE : HOLLOW);
            }
        }
        for (int i = 0; i < 40; i++) {
            double rad = r.range(360) * Math.PI / 180.0;
            double dist = r.range(radius);
            int x = cx + (int) Math.round(Math.cos(rad) * dist);
            int z = cz + (int) Math.round(Math.sin(rad) * dist);
            int m = r.range(10);
            p.put(x, ground, z, m < 3 ? RIB : (m < 6 ? TENDON : (m < 8 ? FLESH : STONE)));
        }
        p.put(cx, ground + 1, cz, r.chance(50) ? VAULT : SUPPLY);
        p.put(cx + 2, ground + 1, cz + 1, CRYSTAL);
        p.put(cx - 2, ground + 1, cz - 1, r.chance(50) ? FLESH : TENDON);
    }

    // ---------------------------------------------------------------------
    // Plan buffer, palette, RNG
    // ---------------------------------------------------------------------

    /** A district's block placements, in the order they must be applied. */
    private static final class Plan {
        final ArrayDeque<long[]> ops = new ArrayDeque<>();

        void put(int x, int y, int z, int index) {
            if (ops.size() < MAX_PLAN_OPS) {
                ops.add(new long[]{x, y, z, index});
            }
        }
    }

    private static BlockState[] palette() {
        BlockState[] p = palette;
        if (p != null) {
            return p;
        }
        BlockState[] q = new BlockState[PALETTE_SIZE];
        q[AIR] = Blocks.AIR.defaultBlockState();
        q[STONE] = stateOf(McsmContent.DECAYED_STONE);
        q[COBBLE] = stateOf(McsmContent.DECAYED_COBBLESTONE);
        q[BRICKS] = stateOf(McsmContent.CITY_BRICKS);
        q[TILES] = stateOf(McsmContent.CITY_TILES);
        q[HOLLOW] = stateOf(McsmContent.HOLLOW_WALL);
        q[PLATE] = stateOf(McsmContent.RUSTED_PLATE);
        q[GRATE] = stateOf(McsmContent.REBAR_GRATE);
        q[ROAD] = stateOf(McsmContent.CRACKED_ROAD);
        q[GLASS] = stateOf(McsmContent.REALITY_GLASS);
        q[LAMP] = stateOf(McsmContent.GLITCH_LAMP);
        q[PLANKS] = stateOf(McsmContent.DECAYED_PLANKS);
        q[LOG] = stateOf(McsmContent.DECAYED_LOG);
        q[RIB] = stateOf(McsmContent.STORM_RIB);
        q[TENDON] = stateOf(McsmContent.TENDON_BLOCK);
        q[FLESH] = stateOf(McsmContent.WITHERED_FLESH_BLOCK);
        q[CRYSTAL] = stateOf(McsmContent.MEMORY_CRYSTAL);
        q[VOIDCORE] = stateOf(McsmContent.VOID_CORE);
        q[ANCHOR] = stateOf(McsmContent.RIFT_ANCHOR);
        q[CRATE] = stateOf(McsmContent.CITY_CRATE);
        q[SUPPLY] = stateOf(McsmContent.SUPPLY_CRATE);
        q[VAULT] = stateOf(McsmContent.VAULT_CRATE);
        q[SAND] = stateOf(McsmContent.DECAYED_SAND);
        q[LEAVES] = stateOf(McsmContent.DECAYED_LEAVES);
        palette = q;
        return q;
    }

    private static BlockState stateOf(Block block) {
        return block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
    }

    /** xorshift64: tiny, fast, and identical on every machine for a given seed. */
    private static final class Rng {
        private long state;

        Rng(long seed) {
            this.state = seed | 1L;
        }

        long next() {
            long s = state;
            s ^= s << 13;
            s ^= s >>> 7;
            s ^= s << 17;
            state = s;
            return s;
        }

        int range(int n) {
            if (n <= 0) {
                return 0;
            }
            return (int) Math.floorMod(next(), (long) n);
        }

        boolean chance(int percent) {
            return range(100) < percent;
        }
    }
}
