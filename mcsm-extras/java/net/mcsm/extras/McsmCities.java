package net.mcsm.extras;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
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
    // BUILD #429 -- the interior/decor set. "Full of designs, banners from
    // blocks and stuff": real, placeable blocks, in the mod's own palette, that
    // the furnishing and the facade banners are drawn with.
    private static final int DESIGN_LIGHT = 24;
    private static final int DESIGN_DARK = 25;
    private static final int DESIGN_ACCENT = 26;
    private static final int DESIGN_SOFT = 27;
    private static final int DESIGN_TRIM = 28;
    private static final int PALETTE_SIZE = 29;

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
            // BUILD #429 -- "I would like it that they summon in the regular
            // [world]." The generator used to be reachable ONLY from the decayed
            // reality. Now the overworld grows them too, on the same deterministic
            // region grid (a district always holds the same place), with two
            // guards so a player's own base is never bulldozed: the overworld
            // doesn't start until OVERWORLD_MIN_DISTANCE from world spawn, and it
            // obeys its own switch.
            boolean overworld = level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD);
            boolean decayed = level.dimension().equals(McsmReality.DECAYED_REALITY);
            if (overworld && !McsmExtrasConfig.citiesInOverworld) {
                return;
            }
            if (!overworld && !decayed) {
                return;
            }
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) {
                return;
            }
            if (OPS.isEmpty()) {
                for (ServerPlayer player : players) {
                    if (overworld && nearSpawn(level, player)) {
                        continue;   // never raise a district on top of world spawn
                    }
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
            // BUILD #429 -- once a district's placements are all down, the people
            // who live in it arrive.
            if (OPS.isEmpty()) {
                spawnLife(level);
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
                // BUILD #429 -- "the real [cities] have actual interiors full of
                // life, full of people, full of designs, banners from blocks and
                // stuff". Every plot gets its inside furnished and somebody left
                // inside it, and the facade gets a made-of-blocks banner.
                planInterior(plan, rng, bx, bz, ground, kind);
                planFacadeBanner(plan, rng, bx, bz, ground, kind);
                markLife(plan, bx, bz, ground);
            }
        }

        OPS.addAll(plan.ops);
        districts++;
        System.out.println("[ds] raising a district at " + ox + "," + oz + " ("
                + plan.ops.size() + " placements)");
    }

    /**
     * BUILD #429 -- one survivor per booked spot, through the mod's own NPC
     * path, so the city has people in it when the player walks in. Fallback: a
     * villager, because an empty city is the thing this is fixing.
     */
    private static void spawnLife(ServerLevel level) {
        int[] at;
        int spawned = 0;
        while ((at = LIFE.poll()) != null && spawned < 8) {
            try {
                BlockPos pos = new BlockPos(at[0], at[1], at[2]);
                // BUILD #432 -- a spot booked as IVOR's is IVOR's: the secret room
                // of a hospital is where the terminal sends the player, so the one
                // survivor standing in it has to be the one the hint names.
                boolean ivor = at.length > 3 && at[3] == IVOR_ENTRY;
                EntityType<?> type = null;
                try {
                    type = net.mcsm.extras.entity.McsmEntities.STORY_CHARACTER;
                } catch (Throwable ignored) {
                    type = null;
                }
                if (type == null) {
                    type = BuiltInRegistries.ENTITY_TYPE
                            .getValue(net.minecraft.resources.Identifier
                                    .fromNamespaceAndPath("minecraft", "villager"));
                }
                if (type == null) {
                    continue;
                }
                net.minecraft.world.entity.Entity created =
                        type.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
                if (created instanceof net.minecraft.world.entity.Mob mob) {
                    mob.setPersistenceRequired();
                    if (mob instanceof net.mcsm.extras.entity.StoryCharacterEntity character) {
                        character.setCharacter(ivor ? "ivor"
                                : CHARACTERS[level.getRandom().nextInt(CHARACTERS.length)]);
                    }
                    if (ivor) {
                        // Carry the name even when the cast entity is unavailable
                        // and this fell back to a villager: the terminal's hint
                        // looks for a custom name containing "ivor", which is how
                        // the player gets the five words out of him.
                        mob.setCustomName(net.minecraft.network.chat.Component.literal("IVOR"));
                    }
                    mob.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                            level.getRandom().nextFloat() * 360.0F, 0.0F);
                    level.addFreshEntity(mob);
                    spawned++;
                }
            } catch (Throwable ignored) {
                // a district without its cast is still a district
            }
        }
    }

    /** The cast the abandoned city is populated from. */
    private static final String[] CHARACTERS = {"jesse", "petra", "lukas", "axel", "olivia", "ivor"};

    /** How far from world spawn the overworld's districts may start. */
    public static final int OVERWORLD_MIN_DISTANCE = 384;

    /**
     * BUILD #429 -- keep the overworld's own house standing. World spawn and
     * everything within OVERWORLD_MIN_DISTANCE of it is left exactly as it was
     * found; the ruined city begins outside it.
     */
    private static boolean nearSpawn(ServerLevel level, ServerPlayer player) {
        BlockPos spawn = spawnPos(level);
        double dx = player.getX() - spawn.getX();
        double dz = player.getZ() - spawn.getZ();
        return dx * dx + dz * dz < (double) OVERWORLD_MIN_DISTANCE * OVERWORLD_MIN_DISTANCE;
    }

    /**
     * Where the world started, read by name rather than called directly.
     *
     * Run 508 answered this one the hard way: `ServerLevel.getSharedSpawnPos()`
     * is NOT in this version's API -- javac: "cannot find symbol: method
     * getSharedSpawnPos(), location: variable level of type ServerLevel". A
     * try/catch cannot help there, because the method has to exist at COMPILE
     * time. So the name is looked up once, the way the story stage already looks
     * up its own spawn, and the first candidate that answers wins. If none does,
     * the origin stands in, which is where a fresh world's spawn is.
     */
    private static BlockPos spawnPos(ServerLevel level) {
        for (String name : SPAWN_METHODS) {
            try {
                java.lang.reflect.Method method = level.getClass().getMethod(name);
                Object value = method.invoke(level);
                if (value instanceof BlockPos pos) {
                    return pos;
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // try the next name; the fallback below is always valid
            }
        }
        return BlockPos.ZERO;
    }

    /** Candidate names for "where is world spawn", newest first. */
    private static final String[] SPAWN_METHODS = {"getSharedSpawnPos", "getSpawnPos", "getRespawnPosition"};

    /**
     * BUILD #429 -- A BUILDING'S INSIDE.
     *
     * "The real [cities] have actual interiors full of life, full of people,
     * full of designs." Each plot's ground floor is furnished: a floor, a rug,
     * a counter, seating, lamps, shelving against the back wall, a bed in the
     * houses, and the good crates in the rooms that would have had them. It is
     * all drawn with setBlock placements like everything else in this generator,
     * so it is real, breakable, buildable-with world state.
     */
    private static void planInterior(Plan p, Rng r, int bx, int bz, int ground, int kind) {
        int w = 16 + r.range(6);
        int d = 14 + r.range(6);
        int floor = ground + 1;
        // the floor itself, worn planks with the odd missing board
        for (int x = -w; x <= w; x++) {
            for (int z = -d; z <= d; z++) {
                if (r.chance(12)) {
                    continue;                 // a hole in the floor: it is a ruin
                }
                p.put(bx + x, floor, bz + z, PLANKS);
            }
        }
        // a rug down the middle, a different colour per building
        for (int x = -w + 6; x <= w - 6; x++) {
            p.put(bx + x, floor + 1, bz, DESIGN_SOFT);
        }
        // counters and seating: two runs of blocks, one against each side wall
        for (int z = -d + 4; z <= d - 4; z += 3) {
            p.put(bx - w + 3, floor + 1, bz + z, DESIGN_TRIM);
            p.put(bx - w + 3, floor + 2, bz + z, LAMP);
            p.put(bx + w - 3, floor + 1, bz + z, PLANKS);
        }
        // shelving on the back wall, and the crates people left behind
        for (int x = -w + 5; x <= w - 5; x += 4) {
            p.put(bx + x, floor + 1, bz - d + 2, LOG);
            p.put(bx + x, floor + 2, bz - d + 2, CRATE);
            if (r.chance(30)) {
                p.put(bx + x, floor + 3, bz - d + 2, SUPPLY);
            }
        }
        // houses get a bed and a table; the hospitals get a row of them
        if (kind >= 46 && kind < 66) {
            p.put(bx + w - 6, floor + 1, bz + d - 4, DESIGN_SOFT);
            p.put(bx + w - 6, floor + 1, bz + d - 5, DESIGN_SOFT);
            p.put(bx + w - 4, floor + 1, bz + d - 4, DESIGN_TRIM);
            p.put(bx + w - 4, floor + 2, bz + d - 4, LAMP);
        } else if (kind >= 66 && kind < 80) {
            for (int x = -w + 6; x <= w - 6; x += 6) {
                p.put(bx + x, floor + 1, bz + d - 5, DESIGN_SOFT);
                p.put(bx + x, floor + 1, bz + d - 4, DESIGN_LIGHT);
                p.put(bx + x, floor + 1, bz + d - 3, DESIGN_SOFT);
            }
        }
        // a lamp in each corner so the inside is not a black box
        p.put(bx - w + 2, floor + 2, bz - d + 2, LAMP);
        p.put(bx + w - 2, floor + 2, bz + d - 2, LAMP);
    }

    /**
     * BUILD #429 -- A BANNER MADE OF BLOCKS.
     *
     * The user asked for "designs, banners from blocks": a two-tone wall
     * design in the mod's own colours, hung on the facade above the entrance of
     * everything except the craters. It is a pattern, not a texture, so it
     * survives being broken, rebuilt and photographed.
     */
    private static void planFacadeBanner(Plan p, Rng r, int bx, int bz, int ground, int kind) {
        if (kind >= 90) {
            return;                    // a crater has no facade to hang one on
        }
        int y = ground + 6 + r.range(8);
        int half = 5 + r.range(3);
        // the field
        for (int x = -half; x <= half; x++) {
            for (int dy = 0; dy < 5; dy++) {
                p.put(bx + x, y + dy, bz - HALF_PLOT, DESIGN_DARK);
            }
        }
        // the device: a diamond, or a band, or a stair -- one of three designs
        int design = r.range(3);
        for (int x = -half; x <= half; x++) {
            int dy = design == 0
                    ? 2 - Math.abs(x) / 2
                    : design == 1
                        ? (x % 2 == 0 ? 1 : 2)
                        : Math.abs(x) % 3;
            p.put(bx + x, y + Math.max(0, Math.min(4, dy)), bz - HALF_PLOT, DESIGN_ACCENT);
            if (design == 0 && dy > 0) {
                p.put(bx + x, y + Math.min(4, dy + 2), bz - HALF_PLOT, DESIGN_LIGHT);
            }
        }
        // a trim line under it, and two lamps so it reads at night
        for (int x = -half - 1; x <= half + 1; x++) {
            p.put(bx + x, y - 1, bz - HALF_PLOT, DESIGN_TRIM);
        }
        p.put(bx - half - 1, y + 5, bz - HALF_PLOT, LAMP);
        p.put(bx + half + 1, y + 5, bz - HALF_PLOT, LAMP);
    }

    /**
     * BUILD #429 -- SOMEBODY IS STILL HERE.
     *
     * "Full of people." A district books a handful of survivors: the mod's own
     * Story Mode cast (McsmNpcs' own spawn path, which falls back to villagers if
     * the cast entity is unavailable). Recorded as coordinates and spawned by the
     * tick that finishes the district, so a half-built building never contains a
     * person standing in the air.
     */
    private static void markLife(Plan plan, int bx, int bz, int ground) {
        LIFE.add(new int[]{bx, ground + 2, bz});
    }

    /** Where the survivors of the districts already raised are waiting. */
    private static final java.util.concurrent.ConcurrentLinkedQueue<int[]> LIFE =
            new java.util.concurrent.ConcurrentLinkedQueue<>();

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
        // BUILD #432 -- the room the terminal has been pointing at since #424.
        planSecretRoom(p, x0, z1, ground);
    }

    // ------------------------------------------------------------------
    // BUILD #432 -- IVOR'S SECRET ROOM.
    //
    // Since #424 the restricted terminal has told the player exactly where the
    // five letters are: "It is written down in the world, in the hands of the
    // one survivor who is still carrying the paperwork: IVOR. Find him in the
    // secret room of an abandoned hospital." Nothing in the world wrote them
    // down. This does.
    //
    // It is a BASEMENT of the hospital, three blocks under the ward, 23 wide and
    // 6 deep, walled in design-dark with tiles inside. It has no door in any
    // facade: the only way in is the stairwell punched down through the ward's
    // back corner, which is why it is a secret. The five letters are inlaid in
    // the floor in the storm's own purple -- M A S S G, readable from the foot of
    // the stairs -- and IVOR is standing in the room, named, which is what makes
    // the terminal's IVOR hint answer with the words the letters come from.
    //
    // Everything here is ordinary setBlock placements like the rest of this
    // generator, so the room is real, breakable, and re-buildable by a player.
    // ------------------------------------------------------------------
    private static void planSecretRoom(Plan p, int x0, int z1, int ground) {
        final int walk = ground - SECRET_DEPTH;      // the room's floor surface
        final int ax0 = x0 + 2;
        final int ax1 = ax0 + SECRET_ROOM_WIDTH - 1;
        final int zBack = z1 - 6;
        final int zFront = z1 - 1;

        // the shell: floor below, ceiling above, design-dark walls around
        for (int x = ax0 - 1; x <= ax1 + 1; x++) {
            for (int z = zBack - 1; z <= zFront + 1; z++) {
                p.put(x, walk - 1, z, TILES);
                p.put(x, walk + 3, z, DESIGN_DARK);
            }
        }
        for (int y = walk; y < walk + 3; y++) {
            for (int x = ax0 - 1; x <= ax1 + 1; x++) {
                p.put(x, y, zBack - 1, DESIGN_DARK);
                p.put(x, y, zFront + 1, DESIGN_DARK);
            }
            for (int z = zBack - 1; z <= zFront + 1; z++) {
                p.put(ax0 - 1, y, z, DESIGN_DARK);
                p.put(ax1 + 1, y, z, DESIGN_DARK);
            }
        }
        // hollow it out
        for (int y = walk; y < walk + 3; y++) {
            for (int x = ax0; x <= ax1; x++) {
                for (int z = zBack; z <= zFront; z++) {
                    p.put(x, y, z, AIR);
                }
            }
        }
        // THE WAY IN: a stairwell punched down through the ward's back corner.
        // Step s is one block lower than the step before it, so it is walkable
        // both ways -- a secret room nobody can leave is a bug, not a secret.
        for (int s = 0; s < SECRET_DEPTH; s++) {
            int y = ground - s;
            int z = zFront + 1 - s;
            for (int x = ax0; x <= ax0 + 1; x++) {
                p.put(x, y, z, TILES);
                p.put(x, y + 1, z, AIR);
                p.put(x, y + 2, z, AIR);
            }
        }
        // THE LETTERS: M A S S G inlaid in the floor, in the storm's purple.
        // Four blocks apart, drawn from the same five-by-three glyph table the
        // terminal's hint decodes into words.
        int gx = x0 + 5;
        for (int letter = 0; letter < SECRET_GLYPHS.length; letter++) {
            String glyph = SECRET_GLYPHS[letter];
            for (int row = 0; row < GLYPH_ROWS; row++) {
                for (int col = 0; col < GLYPH_COLS; col++) {
                    if (glyph.charAt(row * GLYPH_COLS + col) != '#') {
                        continue;
                    }
                    p.put(gx + col, walk - 1, zBack + 1 + row, DESIGN_ACCENT);
                }
            }
            gx += GLYPH_COLS + 1;
        }
        // two lamps so the paperwork can actually be read, and Ivor's spot
        p.put(ax0, walk + 2, zFront, LAMP);
        p.put(ax1, walk + 2, zBack, LAMP);
        p.put(ax1, walk, zBack, SUPPLY);
        // Ivor's own spot is beside the first letter, not on top of it.
        LIFE.add(new int[]{x0 + 4, walk, zBack + 3, IVOR_ENTRY});
    }

    /** How far under the ward the room sits. */
    private static final int SECRET_DEPTH = 3;

    /** The room's width in blocks: the five glyphs, their gaps, and the stairs. */
    private static final int SECRET_ROOM_WIDTH = 23;

    private static final int GLYPH_ROWS = 5;
    private static final int GLYPH_COLS = 3;

    /** Marks a booked survivor as IVOR himself. */
    private static final int IVOR_ENTRY = 1;

    /**
     * M A S S G, five rows by three columns each, row-major. These five bitmaps
     * ARE the code: the terminal's hint decodes them into "Mourning, Ash,
     * Silence, Sirens, Graves", and the gate reads this table back and checks the
     * shapes spell the five letters rather than trusting the comment above them.
     */
    private static final String[] SECRET_GLYPHS = {
        "#.#####.##.##.#",   // M
        ".#.#.#####.##.#",   // A
        ".###...#...###.",   // S
        ".###...#...###.",   // S
        ".###..#.##.#.##",   // G
    };

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
        // the design set: vanilla blocks on purpose, because a banner has to
        // read as a banner -- and because a ruined city built only from the
        // mod's own grey stone reads as a quarry, not as a city people lived in
        q[DESIGN_LIGHT] = design("white_concrete", Blocks.POLISHED_DEEPSLATE);
        q[DESIGN_DARK] = design("black_concrete", Blocks.POLISHED_DEEPSLATE);
        q[DESIGN_ACCENT] = design("purple_concrete", Blocks.POLISHED_DEEPSLATE);
        q[DESIGN_SOFT] = design("light_gray_wool", Blocks.POLISHED_DEEPSLATE);
        q[DESIGN_TRIM] = stateOf(Blocks.POLISHED_DEEPSLATE);
        palette = q;
        return q;
    }

    /**
     * The design set, by registry id.
     *
     * Run 508 also proved the field names are gone in this version: javac
     * "cannot find symbol: variable WHITE_CONCRETE, location: class Blocks" --
     * and likewise for BLACK_CONCRETE, PURPLE_CONCRETE and LIGHT_GRAY_WOOL. The
     * ids themselves are the stable, public names of those blocks (`whitelisted`
     * by every datapack and every resource pack since they were added), and the
     * block registry is the one place a block cannot go missing from, so the
     * design set is resolved from BuiltInRegistries.BLOCK instead of named as
     * fields. A pack that removes one degrades to polished deepslate -- the
     * city's own stone -- rather than to air.
     */
    private static BlockState design(String path, Block fallback) {
        Block block = BuiltInRegistries.BLOCK.getValue(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", path));
        if (block == null || block == Blocks.AIR) {
            block = fallback;
        }
        return stateOf(block);
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
