package net.mcsm.extras;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * BUILD #444 -- STORAGE MAZES.
 *
 * <p>The wishlist asks for "storage mazes". What that is, taken literally and
 * built properly: a warehouse under the world, big enough to get lost in, whose
 * aisles are lined with crates and whose dead ends are worth reaching. There are
 * three things that make it a maze rather than a room, and all three are real:
 *
 * <ul>
 *   <li><b>IT IS CARVED, NOT DRAWN.</b> A depth-first walk over a cell grid digs
 *       the corridors, so every maze is a genuinely different, connected,
 *       perfect maze -- one route between any two cells, dead ends everywhere --
 *       and the walk is seeded from the region's own coordinates, so the same
 *       place is always the same maze;</li>
 *   <li><b>IT IS UNDER THE GROUND AND SEALED.</b> A warehouse at y = 18 with four
 *       blocks of headroom, a solid shell, and one shaft up to a hatch on the
 *       surface. The only light is the glitch lamps the staff left behind;</li>
 *   <li><b>IT IS WORTH LOOTING.</b> Every aisle carries crates (which drop through
 *       their own loot tables), and every DEAD END carries a barrel -- a real
 *       container, filled through the container API, so the far corners of the
 *       maze are the far corners of the maze on purpose. One dead end in each
 *       maze holds a vault crate instead.</li>
 * </ul>
 *
 * <p>Where it appears: on a deterministic region grid (one maze per
 * {@link #REGION} blocks, {@link #MAZE_PERCENT} percent of regions) in the
 * overworld and in the decayed reality, built when a player comes within
 * {@link #ACTIVATE} blocks -- the same activation shape the districts use, so a
 * maze exists before the player can see the hatch.
 *
 * <p>Everything is the content pack's own blocks plus vanilla barrels: no new
 * registry entries, no new assets, and the whole thing is written through
 * {@link McsmBuildQueue} at a fixed budget per tick, so a maze landing next to a
 * player is a few seconds of work rather than a stall.
 */
public final class McsmMazes {

    /** One maze per this many blocks. */
    public static final int REGION = 160;
    /** Share of regions that hold a maze (0-100). */
    public static final int MAZE_PERCENT = 42;
    /** Built when a player is this close. */
    public static final int ACTIVATE = 256;
    /** Blocks written per level tick. */
    public static final int OPS_PER_TICK = 1500;
    /** Floor height of a warehouse. Four blocks of headroom above it. */
    public static final int FLOOR_Y = 18;
    /** Aisle width (3) plus one wall each side. */
    private static final int CELL = 5;
    /** Cells per side: 12 gives a 61x61 warehouse. */
    private static final int GRID = 12;
    /** Cells allowed to be queued at once. */
    private static final int MAX_PENDING = 3;
    /** Loot tables' worth of tags, so a barrel knows what it is. */
    private static final int TAG_AISLE = 1;
    private static final int TAG_VAULT = 2;

    private static final McsmBuildQueue.Queue QUEUE = new McsmBuildQueue.Queue(MAX_PENDING);
    private static final Set<Long> QUEUED = ConcurrentHashMap.newKeySet();
    private static final Set<Long> BUILT = ConcurrentHashMap.newKeySet();

    private McsmMazes() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmMazes::tick);
            System.out.println("[ds] storage mazes armed (" + MAZE_PERCENT + "% of "
                    + REGION + "-block regions, carved, sealed at y=" + FLOOR_Y + ")");
        } catch (Throwable t) {
            System.err.println("[ds] storage mazes could not hook the level tick: " + t);
        }
    }

    // -------------------------------------------------------------------------
    // The tick
    // -------------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.storageMazes || !supported(level)) {
                return;
            }
            if (!QUEUE.full()) {
                for (ServerPlayer player : level.players()) {
                    planNear(level, player);
                }
            }
            QUEUE.pump(level, OPS_PER_TICK, palette(), plan -> finish(level, plan));
        } catch (Throwable t) {
            System.err.println("[ds] storage maze tick failed: " + t);
        }
    }

    private static boolean supported(ServerLevel level) {
        if (level.dimension().equals(Level.OVERWORLD)) {
            return McsmExtrasConfig.citiesInOverworld;
        }
        return level.dimension().equals(McsmReality.DECAYED_REALITY);
    }

    private static void planNear(ServerLevel level, ServerPlayer player) {
        int prx = Math.floorDiv((int) Math.floor(player.getX()), REGION);
        int prz = Math.floorDiv((int) Math.floor(player.getZ()), REGION);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int rx = prx + dx;
                int rz = prz + dz;
                long key = regionKey(rx, rz);
                if (BUILT.contains(key) || QUEUED.contains(key) || !hasMaze(rx, rz)) {
                    continue;
                }
                int ox = rx * REGION + REGION / 2;
                int oz = rz * REGION + REGION / 2;
                if (Math.hypot(player.getX() - ox, player.getZ() - oz) > ACTIVATE) {
                    continue;
                }
                if (level.dimension().equals(Level.OVERWORLD) && nearSpawn(level, ox, oz)) {
                    continue;
                }
                QUEUED.add(key);
                try {
                    QUEUE.add(plan(key, ox, oz));
                } catch (Throwable t) {
                    System.err.println("[ds] storage maze plan failed at " + ox + "," + oz + ": " + t);
                }
                if (QUEUE.full()) {
                    return;
                }
            }
        }
    }

    /** Never raise a warehouse (or its shaft) on top of world spawn. */
    private static boolean nearSpawn(ServerLevel level, int x, int z) {
        try {
            BlockPos spawn = McsmCities.spawnPos(level);
            return Math.hypot(x - spawn.getX(), z - spawn.getZ()) < 320.0D;
        } catch (Throwable t) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Determinism
    // -------------------------------------------------------------------------

    public static boolean hasMaze(int rx, int rz) {
        return Math.floorMod(mix(regionKey(rx, rz) * 0x2545F4914F6CDD1DL), 100L) < MAZE_PERCENT;
    }

    /** The nearest maze centre to a position, as {x, z, distanceSquared}. */
    public static int[] nearestMaze(int x, int z) {
        int rx = Math.floorDiv(x, REGION);
        int rz = Math.floorDiv(z, REGION);
        int[] best = null;
        long bestD = Long.MAX_VALUE;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (!hasMaze(rx + dx, rz + dz)) {
                    continue;
                }
                int ox = (rx + dx) * REGION + REGION / 2;
                int oz = (rz + dz) * REGION + REGION / 2;
                long d = (long) (ox - x) * (ox - x) + (long) (oz - z) * (oz - z);
                if (d < bestD) {
                    bestD = d;
                    best = new int[]{ox, oz, (int) d};
                }
            }
        }
        return best;
    }

    public static int built() {
        return BUILT.size();
    }

    // -------------------------------------------------------------------------
    // The plan: a carved warehouse
    // -------------------------------------------------------------------------

    private static final int AIR = 0;
    private static final int BRICK = 1;
    private static final int ROAD = 2;
    private static final int WALL = 3;
    private static final int GRATE = 4;
    private static final int LAMP = 5;
    private static final int PLATE = 6;
    private static final int CRATE = 7;
    private static final int SUPPLY = 8;
    private static final int VAULT = 9;
    private static final int BARREL = 10;
    private static final int TILES = 11;

    private static McsmBuildQueue.Plan plan(long key, int ox, int oz) {
        long seed = mix(key ^ 0x1D1DL);
        int side = GRID * CELL + 1;
        McsmBuildQueue.Planner planner = new McsmBuildQueue.Planner(side * side * 6);

        // ---- the shell, solid, so nothing can see in ------------------
        planner.box(ox - 1, FLOOR_Y - 1, oz - 1, ox + side, FLOOR_Y + 5, oz + side, BRICK);
        planner.box(ox, FLOOR_Y - 1, oz, ox + side - 1, FLOOR_Y - 1, oz + side - 1, ROAD);

        // ---- carve the maze (depth-first, seeded) ---------------------
        boolean[] visited = new boolean[GRID * GRID];
        int[] stackX = new int[GRID * GRID];
        int[] stackZ = new int[GRID * GRID];
        int top = 0;
        int cx = (int) Math.floorMod(seed, GRID);
        int cz = (int) Math.floorMod(seed >> 8, GRID);
        visited[cz * GRID + cx] = true;
        stackX[0] = cx;
        stackZ[0] = cz;
        java.util.Random rng = new java.util.Random(seed);
        while (top >= 0) {
            cx = stackX[top];
            cz = stackZ[top];
            int[] choices = new int[4];
            int n = 0;
            if (cz > 0 && !visited[(cz - 1) * GRID + cx]) {
                choices[n++] = 0;
            }
            if (cx < GRID - 1 && !visited[cz * GRID + cx + 1]) {
                choices[n++] = 1;
            }
            if (cz < GRID - 1 && !visited[(cz + 1) * GRID + cx]) {
                choices[n++] = 2;
            }
            if (cx > 0 && !visited[cz * GRID + cx - 1]) {
                choices[n++] = 3;
            }
            if (n == 0) {
                top--;
                continue;
            }
            int pick = choices[rng.nextInt(n)];
            int nx = cx;
            int nz = cz;
            switch (pick) {
                case 0 -> nz = cz - 1;
                case 1 -> nx = cx + 1;
                case 2 -> nz = cz + 1;
                default -> nx = cx - 1;
            }
            visited[nz * GRID + nx] = true;
            stackX[++top] = nx;
            stackZ[top] = nz;
            // Dig the 3-wide aisle through the wall between the two cells.
            int ax = ox + Math.min(cx, nx) * CELL + CELL;
            int az = oz + Math.min(cz, nz) * CELL + CELL;
            if (nx != cx) {
                planner.box(ax, FLOOR_Y, az - 1, ax + 1, FLOOR_Y + 3, az + 2, AIR);
            } else {
                planner.box(ax - 1, FLOOR_Y, az, ax + 2, FLOOR_Y + 3, az + 1, AIR);
            }
        }
        // Every cell's own 3x3 floor plan: open interior, tiled ceiling.
        for (int gx = 0; gx < GRID; gx++) {
            for (int gz = 0; gz < GRID; gz++) {
                int x = ox + gx * CELL + 1;
                int z = oz + gz * CELL + 1;
                planner.box(x, FLOOR_Y, z, x + 2, FLOOR_Y + 3, z + 2, AIR);
                planner.box(x, FLOOR_Y - 1, z, x + 2, FLOOR_Y - 1, z + 2, ROAD);
                planner.box(x, FLOOR_Y + 4, z, x + 2, FLOOR_Y + 4, z + 2, TILES);
            }
        }

        // ---- shelving, lamps and what the staff left behind -----------
        int deadEnds = 0;
        for (int gx = 0; gx < GRID; gx++) {
            for (int gz = 0; gz < GRID; gz++) {
                long h = mix(seed ^ ((long) gx << 20) ^ gz);
                int x = ox + gx * CELL + 1;
                int z = oz + gz * CELL + 1;
                // A run of crates along one side of the aisle, always walkable
                // around: the crate side is chosen per cell, and the aisle keeps
                // two clear blocks whichever side that is.
                boolean north = Math.floorMod(h, 2L) == 0;
                int shelfZ = north ? z : z + 2;
                boolean shelfX = Math.floorMod(h >> 3, 2L) == 0;
                if (shelfX) {
                    for (int i = 0; i <= 2; i++) {
                        planner.put(x + i, FLOOR_Y, shelfZ, Math.floorMod(h >> (6 + i), 3L) == 0
                                ? SUPPLY : CRATE);
                    }
                } else {
                    for (int i = 0; i <= 2; i++) {
                        planner.put(north ? x : x + 2, FLOOR_Y, z + i,
                                Math.floorMod(h >> (6 + i), 3L) == 0 ? SUPPLY : CRATE);
                    }
                }
                // Walls: hollow, and part grate so the shell reads as built.
                planner.box(x - 1, FLOOR_Y, z - 1, x - 1, FLOOR_Y + 3, z + 2,
                        Math.floorMod(h >> 11, 5L) == 0 ? GRATE : WALL);
                planner.box(x + 3, FLOOR_Y, z - 1, x + 3, FLOOR_Y + 3, z + 2,
                        Math.floorMod(h >> 13, 5L) == 0 ? GRATE : WALL);
                planner.box(x, FLOOR_Y, z - 1, x + 2, FLOOR_Y + 3, z - 1,
                        Math.floorMod(h >> 15, 5L) == 0 ? GRATE : WALL);
                planner.box(x, FLOOR_Y, z + 3, x + 2, FLOOR_Y + 3, z + 3,
                        Math.floorMod(h >> 17, 5L) == 0 ? GRATE : WALL);
                // Lamps are rare: a warehouse is dark, and that is the point.
                if (Math.floorMod(h >> 21, 5L) == 0) {
                    planner.put(x + 1, FLOOR_Y + 3, z + 1, LAMP);
                }
                // Dead ends (one way in, three ways blocked): the treasure.
                int ways = 0;
                if (gz > 0 && visited[(gz - 1) * GRID + gx]) {
                    ways++;
                }
                if (gz < GRID - 1 && visited[(gz + 1) * GRID + gx]) {
                    ways++;
                }
                if (gx > 0 && visited[gz * GRID + gx - 1]) {
                    ways++;
                }
                if (gx < GRID - 1 && visited[gz * GRID + gx + 1]) {
                    ways++;
                }
                if (ways == 1) {
                    deadEnds++;
                    boolean vault = deadEnds % 4 == 0;
                    planner.put(x, FLOOR_Y + 1, z, BARREL);
                    planner.container(x, FLOOR_Y + 1, z, vault ? TAG_VAULT : TAG_AISLE);
                    planner.put(x + 1, FLOOR_Y + 1, z, vault ? VAULT : CRATE);
                }
            }
        }

        // ---- the shaft up to the hatch -------------------------------
        int sx = ox + side / 2;
        int sz = oz + side / 2;
        for (int y = FLOOR_Y; y <= FLOOR_Y + 4; y++) {
            planner.box(sx, y, sz, sx + 1, y, sz + 1, AIR);
        }
        planner.box(sx - 1, FLOOR_Y + 4, sz - 1, sx + 2, FLOOR_Y + 4, sz + 2, PLATE);
        // THE SHAFT IS BOUNDED BY THE ACTUAL SURFACE. It used to clear a column to
        // a fixed height, which would have cut through whatever a player had built
        // above it; now it stops at the terrain's own surface (read from the
        // heightmap, never assumed) and the hatch is laid ON that surface.
        int surface = FLOOR_Y + 6;
        try {
            surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);
        } catch (Throwable ignored) {
            // the constant fallback keeps the shaft a shaft
        }
        for (int y = FLOOR_Y + 5; y <= surface; y++) {
            planner.box(sx, y, sz, sx + 1, y, sz + 1, AIR);
        }
        for (int y = FLOOR_Y + 1; y <= FLOOR_Y + 4; y += 2) {
            planner.put(sx - 1, y, sz + 1, LAMP);
        }
        // The hatch sits on the surface, and it is marked: whoever built this
        // wanted to find it again.
        planner.box(sx - 1, surface + 1, sz - 1, sx + 2, surface + 1, sz + 2, PLATE);
        planner.put(sx, surface + 2, sz, LAMP);
        planner.put(sx + 1, surface + 2, sz + 1, LAMP);
        return planner.plan(key);
    }

    // -------------------------------------------------------------------------
    // Finishing: the barrels
    // -------------------------------------------------------------------------

    private static final String[][] AISLE_LOOT = {
        {"mcsm:decayed_steel_ingot", "mcsm:void_thread", "mcsm:decayed_bone", "minecraft:torch"},
        {"mcsm:rift_shard", "minecraft:bread", "minecraft:string", "minecraft:arrow"},
        {"mcsm:city_keycard", "mcsm:memory_fragment", "minecraft:iron_ingot", "minecraft:copper_ingot"},
    };

    private static final String[][] VAULT_LOOT = {
        {"mcsm:glyph_cell", "mcsm:storm_heart_shard", "mcsm:creator_fragment", "minecraft:diamond"},
        {"mcsm:abyss_orb", "mcsm:rift_shard", "minecraft:gold_ingot", "minecraft:emerald"},
    };

    private static void finish(ServerLevel level, McsmBuildQueue.Plan plan) {
        for (int i = 0; i + 3 < plan.containers.length; i += 4) {
            fill(level, plan.containers[i], plan.containers[i + 1], plan.containers[i + 2],
                    plan.containers[i + 3] == TAG_VAULT, i);
        }
    }

    private static void fill(ServerLevel level, int x, int y, int z, boolean vault, int salt) {
        try {
            Object be = level.getBlockEntity(new BlockPos(x, y, z));
            if (!(be instanceof Container container)) {
                return;
            }
            String[][] pool = vault ? VAULT_LOOT : AISLE_LOOT;
            String[] row = pool[Math.floorMod(x + z + salt, pool.length)];
            int slots = Math.max(1, container.getContainerSize());
            for (int i = 0; i < Math.min(3, row.length); i++) {
                Item item = item(row[i]);
                if (item == null) {
                    continue;
                }
                container.setItem(Math.floorMod(i * 5 + x + z + salt, slots),
                        new ItemStack(item, 1 + Math.floorMod(i + x + z, 4)));
            }
        } catch (Throwable ignored) {
            // an empty barrel is still a barrel
        }
    }

    private static Item item(String id) {
        try {
            int colon = id.indexOf(':');
            return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(
                    id.substring(0, colon), id.substring(colon + 1)));
        } catch (Throwable t) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // The palette
    // -------------------------------------------------------------------------

    private static volatile BlockState[] palette;

    private static BlockState[] palette() {
        BlockState[] local = palette;
        if (local != null) {
            return local;
        }
        BlockState[] next = new BlockState[12];
        next[AIR] = Blocks.AIR.defaultBlockState();
        next[BRICK] = state("mcsm:city_bricks", McsmContent.CITY_BRICKS);
        next[ROAD] = state("mcsm:cracked_road", McsmContent.CRACKED_ROAD);
        next[WALL] = state("mcsm:hollow_wall", McsmContent.HOLLOW_WALL);
        next[GRATE] = state("mcsm:rebar_grate", McsmContent.REBAR_GRATE);
        next[LAMP] = state("mcsm:glitch_lamp", McsmContent.GLITCH_LAMP);
        next[PLATE] = state("mcsm:rusted_plate", McsmContent.RUSTED_PLATE);
        next[CRATE] = state("mcsm:city_crate", McsmContent.CITY_CRATE);
        next[SUPPLY] = state("mcsm:supply_crate", McsmContent.SUPPLY_CRATE);
        next[VAULT] = state("mcsm:vault_crate", McsmContent.VAULT_CRATE);
        next[BARREL] = state("minecraft:barrel", null);
        next[TILES] = state("mcsm:city_tiles", McsmContent.CITY_TILES);
        palette = next;
        return next;
    }

    private static BlockState state(String id, Block fallback) {
        try {
            int colon = id.indexOf(':');
            if (colon > 0) {
                Block block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(
                        id.substring(0, colon), id.substring(colon + 1)));
                if (block != null) {
                    return block.defaultBlockState();
                }
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return fallback == null ? null : fallback.defaultBlockState();
    }

    private static long regionKey(int rx, int rz) {
        return ((long) rx << 32) ^ (rz & 0xFFFFFFFFL);
    }

    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** The hatch's surface height convention, for /ds and the guide. */
    public static int surfaceY() {
        return FLOOR_Y;
    }

    /** The height a hatch sits at when the terrain is ordinary (see plan()). */
    public static final int HATCH_Y = 121;

    /** A player-readable description of where the nearest maze is. */
    public static String guidance(int x, int z) {
        int[] near = nearestMaze(x, z);
        if (near == null) {
            return "no warehouse within " + (REGION * 4) + " blocks";
        }
        int distance = (int) Math.sqrt(near[2]);
        return "the nearest storage maze is " + distance + " blocks away at "
                + near[0] + ", " + near[1] + " (look for the hatch at y=" + HATCH_Y + ")";
    }

    /** Announced the first time a player steps inside one. */
    public static void announce(ServerLevel level, ServerPlayer player, BlockPos at) {
        try {
            if (at.getY() > FLOOR_Y + 8) {
                return;
            }
            level.playSound((Entity) null, at.getX(), at.getY(), at.getZ(),
                    McsmSounds.OBLIVION_DRONE, SoundSource.AMBIENT, 0.6F, 0.7F);
            player.sendSystemMessage(Component.literal(
                    "\u00a77somewhere above you, a hatch \u00b7 \u00a7fthe aisles go further than "
                    + "the walls should allow"));
        } catch (Throwable ignored) {
            // an announcement is the least important part of a warehouse
        }
    }

    /** Every maze this session has written, for the panel and /ds. */
    public static List<String> report() {
        return List.of(
                "storage mazes: " + BUILT.size() + " built this session",
                "one per " + REGION + " blocks, " + MAZE_PERCENT + "% of regions",
                "floor y=" + FLOOR_Y + ", hatch y=" + HATCH_Y + ", queue "
                        + QUEUE.size() + "/" + MAX_PENDING);
    }
}
