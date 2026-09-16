package net.mcsm.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #444 -- SERVER ROOMS.
 *
 * <p>The wishlist's "server rooms", built as the thing the words actually mean:
 * three sealed halls under the world, walled in rusted plate, floored in grate,
 * filled with racks that BLINK, cabled overhead, and one mainframe standing on a
 * dais at the far end that talks back when you stand in front of it.
 *
 * <p>THE LIGHTS ARE THE POINT. A server room that is lit is a basement; a server
 * room that blinks is a machine. So the racks are lamps, the generator remembers
 * where every lamp it placed is, and the level tick runs them:
 *
 * <ul>
 *   <li><b>IDLE CHATTER</b> -- a handful of lamps change state every second, in a
 *       rotating slice, so the room never looks frozen;</li>
 *   <li><b>A POWER FAILURE</b> -- every {@link #FAILURE_PERIOD} ticks the room
 *       drops to emergency lighting for {@link #FAILURE_TICKS}: the lamps go
 *       dark, the drone starts, the mainframe announces it in chat, and then the
 *       lights come back. It is server-side, it is visible from anywhere in the
 *       hall, and it costs two block writes per lamp.</li>
 * </ul>
 *
 * <p>THE MAINFRAME. Rift anchors and a void core on a dais. Standing close to it
 * reads out a line of the room's own log, and the first time a player does it the
 * room gives them a city keycard -- the thing the abandoned cities want, handed out
 * by the machines that ran them. Nothing here is a GUI: the room speaks in chat and
 * in sound, like every other system in this build.
 *
 * <p>Where: a deterministic region grid (one room per {@link #REGION} blocks, in
 * {@link #ROOM_PERCENT} percent of regions), in the overworld and in the decayed
 * reality, built when a player comes within {@link #ACTIVATE} blocks. Blocks come
 * from the content pack; there is no new registry entry and no new asset.
 */
public final class McsmServerRooms {

    /** One room per this many blocks. */
    public static final int REGION = 176;
    /** Share of regions that hold a room. */
    public static final int ROOM_PERCENT = 38;
    /** Built when a player is this close. */
    public static final int ACTIVATE = 256;
    /** Blocks written per level tick. */
    public static final int OPS_PER_TICK = 1400;
    /** Floor height. Five blocks of headroom above it. */
    public static final int FLOOR_Y = 24;
    /** Halls per room: an entrance annex, the rack hall, and the mainframe vault. */
    private static final int HALL = 19;
    /** Ticks between power failures. */
    private static final long FAILURE_PERIOD = 3600L;
    /** How long the lights stay down. */
    private static final long FAILURE_TICKS = 90L;
    /** Lamps changed per chatter pass. */
    private static final int CHATTER = 5;
    /** Ticks between chatter passes. */
    private static final long CHATTER_PERIOD = 20L;
    private static final int MAX_PENDING = 3;
    /** How close a player has to stand to the mainframe for it to answer. */
    private static final double CONSOLE_RANGE = 7.0D;

    private static final McsmBuildQueue.Queue QUEUE = new McsmBuildQueue.Queue(MAX_PENDING);
    private static final Set<Long> QUEUED = ConcurrentHashMap.newKeySet();
    private static final Set<Long> BUILT = ConcurrentHashMap.newKeySet();
    /** Every lamp this session has placed, per level, so the room can blink. */
    private static final Map<Object, List<int[]>> LAMPS = new ConcurrentHashMap<>();
    /** Where each room's mainframe is, per level. */
    private static final Map<Object, List<int[]>> MAINFRAMES = new ConcurrentHashMap<>();
    private static final Set<String> GREETED = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> SPOKEN = new ConcurrentHashMap<>();

    private McsmServerRooms() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmServerRooms::tick);
            System.out.println("[ds] server rooms armed (" + ROOM_PERCENT + "% of "
                    + REGION + "-block regions, blinking racks at y=" + FLOOR_Y + ")");
        } catch (Throwable t) {
            System.err.println("[ds] server rooms could not hook the level tick: " + t);
        }
    }

    // -------------------------------------------------------------------------
    // The tick
    // -------------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.serverRooms || !supported(level)) {
                return;
            }
            if (!QUEUE.full()) {
                for (ServerPlayer player : level.players()) {
                    planNear(level, player);
                }
            }
            QUEUE.pump(level, OPS_PER_TICK, palette(), plan -> finish(level, plan));
            lights(level);
            console(level);
        } catch (Throwable t) {
            System.err.println("[ds] server room tick failed: " + t);
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
                if (BUILT.contains(key) || QUEUED.contains(key) || !hasRoom(rx, rz)) {
                    continue;
                }
                int ox = rx * REGION + REGION / 2;
                int oz = rz * REGION + REGION / 2;
                if (Math.hypot(player.getX() - ox, player.getZ() - oz) > ACTIVATE) {
                    continue;
                }
                try {
                    if (level.dimension().equals(Level.OVERWORLD)) {
                        BlockPos spawn = McsmCities.spawnPos(level);
                        if (Math.hypot(ox - spawn.getX(), oz - spawn.getZ()) < 320.0D) {
                            continue;
                        }
                    }
                } catch (Throwable ignored) {
                    // no spawn probe: the region is allowed
                }
                QUEUED.add(key);
                try {
                    QUEUE.add(plan(level, key, ox, oz));
                } catch (Throwable t) {
                    System.err.println("[ds] server room plan failed at " + ox + "," + oz + ": " + t);
                }
                if (QUEUE.full()) {
                    return;
                }
            }
        }
    }

    public static boolean hasRoom(int rx, int rz) {
        return Math.floorMod(mix(regionKey(rx, rz) * 0x9E3779B97F4A7C15L), 100L) < ROOM_PERCENT;
    }

    /** The nearest room centre, as {x, z, distanceSquared}, or null. */
    public static int[] nearestRoom(int x, int z) {
        int rx = Math.floorDiv(x, REGION);
        int rz = Math.floorDiv(z, REGION);
        int[] best = null;
        long bestD = Long.MAX_VALUE;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (!hasRoom(rx + dx, rz + dz)) {
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
    // The plan: three halls, a corridor and a mainframe
    // -------------------------------------------------------------------------

    private static final int AIR = 0;
    private static final int PLATE = 1;
    private static final int GRATE = 2;
    private static final int TILES = 3;
    private static final int LAMP = 4;
    private static final int GLASS = 5;
    private static final int CORE = 6;
    private static final int ANCHOR = 7;
    private static final int CRYSTAL = 8;
    private static final int BARS = 9;
    private static final int FLOOR = 10;
    /** Lamp tags, so the runtime knows which lamps are racks and which are exits. */
    private static final int TAG_RACK = 1;
    private static final int TAG_EXIT = 2;
    private static final int TAG_MAINFRAME = 3;

    private static McsmBuildQueue.Plan plan(ServerLevel level, long key, int ox, int oz) {
        long seed = mix(key ^ 0x5E12L);
        McsmBuildQueue.Planner planner = new McsmBuildQueue.Planner(30000);

        // Three halls on a line, joined by 3-wide corridors.
        int gap = 6;
        int total = HALL * 3 + gap * 2;
        int x0 = ox - total / 2;
        int z0 = oz - HALL / 2;

        for (int h = 0; h < 3; h++) {
            int hx = x0 + h * (HALL + gap);
            shell(planner, hx, z0, HALL, h == 0);
        }
        // corridors between them
        for (int h = 0; h < 2; h++) {
            int cx = x0 + h * (HALL + gap) + HALL;
            int cz = z0 + HALL / 2 - 1;
            planner.box(cx, FLOOR_Y, cz, cx + gap - 1, FLOOR_Y + 4, cz + 2, AIR);
            planner.box(cx, FLOOR_Y - 1, cz, cx + gap - 1, FLOOR_Y - 1, cz + 2, FLOOR);
            planner.box(cx, FLOOR_Y + 5, cz, cx + gap - 1, FLOOR_Y + 5, cz + 2, TILES);
            planner.box(cx, FLOOR_Y, cz - 1, cx + gap - 1, FLOOR_Y + 4, cz - 1, PLATE);
            planner.box(cx, FLOOR_Y, cz + 3, cx + gap - 1, FLOOR_Y + 4, cz + 3, PLATE);
        }

        // ---- hall 2: the racks ---------------------------------------
        int rackHall = x0 + HALL + gap;
        for (int ax = 2; ax <= HALL - 3; ax += 3) {
            for (int az = 2; az <= HALL - 3; az++) {
                planner.box(rackHall + ax, FLOOR_Y, z0 + az, rackHall + ax, FLOOR_Y + 2, z0 + az,
                        PLATE);
                boolean lamp = Math.floorMod(ax + az, 2) == 0;
                planner.put(rackHall + ax, FLOOR_Y + 1, z0 + az, lamp ? LAMP : GLASS);
                if (lamp) {
                    planner.spot(rackHall + ax, FLOOR_Y + 1, z0 + az, TAG_RACK);
                }
            }
        }
        // cable trays overhead
        for (int ax = 0; ax <= HALL - 1; ax++) {
            planner.put(rackHall + ax, FLOOR_Y + 4, z0 + HALL / 2, BARS);
        }

        // ---- hall 3: the vault and the mainframe ---------------------
        int vault = x0 + 2 * (HALL + gap);
        int vx = vault + HALL / 2;
        int vz = z0 + HALL / 2;
        planner.box(vx - 3, FLOOR_Y - 1, vz - 3, vx + 3, FLOOR_Y - 1, vz + 3, CORE);
        planner.box(vx - 2, FLOOR_Y, vz - 2, vx + 2, FLOOR_Y, vz + 2, PLATE);
        planner.put(vx, FLOOR_Y + 1, vz, ANCHOR);
        planner.put(vx - 1, FLOOR_Y + 1, vz, CRYSTAL);
        planner.put(vx + 1, FLOOR_Y + 1, vz, CRYSTAL);
        planner.put(vx, FLOOR_Y + 1, vz - 1, CRYSTAL);
        planner.put(vx, FLOOR_Y + 1, vz + 1, CRYSTAL);
        planner.spot(vx, FLOOR_Y + 1, vz, TAG_MAINFRAME);
        for (int ax = 1; ax <= HALL - 2; ax += 4) {
            planner.put(vault + ax, FLOOR_Y + 4, z0 + 2, BARS);
            planner.put(vault + ax, FLOOR_Y + 4, z0 + HALL - 3, BARS);
        }

        // ---- the way in: a shaft and a hatch -------------------------
        int sx = x0 + HALL / 2;
        int sz = z0 + HALL / 2;
        for (int y = FLOOR_Y + 5; y <= FLOOR_Y + 5; y++) {
            planner.box(sx, y, sz, sx + 1, y, sz + 1, AIR);
        }
        int surface = FLOOR_Y + 6;
        try {
            surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);
        } catch (Throwable ignored) {
            // the constant keeps the shaft a shaft
        }
        for (int y = FLOOR_Y + 5; y <= surface; y++) {
            planner.box(sx, y, sz, sx + 1, y, sz + 1, AIR);
        }
        planner.box(sx - 1, surface + 1, sz - 1, sx + 2, surface + 1, sz + 2, PLATE);
        planner.put(sx, surface + 2, sz, LAMP);
        planner.spot(sx, surface + 2, sz, TAG_EXIT);
        // the stairwell's own lights, so the way back up is findable in the dark
        for (int y = FLOOR_Y + 2; y < surface; y += 3) {
            planner.put(sx - 1, y, sz, LAMP);
            planner.spot(sx - 1, y, sz, TAG_EXIT);
        }
        return planner.plan(key);
    }

    /** One hall: floor, ceiling, walls, with the door gaps the corridors need. */
    private static void shell(McsmBuildQueue.Planner planner, int hx, int hz, int size,
                              boolean entrance) {
        planner.box(hx - 1, FLOOR_Y - 2, hz - 1, hx + size, FLOOR_Y + 5, hz + size, PLATE);
        planner.box(hx, FLOOR_Y - 1, hz, hx + size - 1, FLOOR_Y - 1, hz + size - 1, FLOOR);
        planner.box(hx, FLOOR_Y, hz, hx + size - 1, FLOOR_Y + 4, hz + size - 1, AIR);
        planner.box(hx, FLOOR_Y + 4, hz, hx + size - 1, FLOOR_Y + 4, hz + size - 1, TILES);
        // exit gaps east and west, plus the entrance shaft in the first hall
        if (entrance) {
            planner.box(hx + size / 2, FLOOR_Y, hz, hx + size / 2 + 1, FLOOR_Y + 4, hz, AIR);
        }
    }

    // -------------------------------------------------------------------------
    // Finishing, blinking, and the console
    // -------------------------------------------------------------------------

    private static void finish(ServerLevel level, McsmBuildQueue.Plan plan) {
        Object id = level.dimension();
        List<int[]> lamps = LAMPS.computeIfAbsent(id, k -> new ArrayList<>());
        List<int[]> frames = MAINFRAMES.computeIfAbsent(id, k -> new ArrayList<>());
        for (int i = 0; i + 3 < plan.spots.length; i += 4) {
            int tag = plan.spots[i + 3];
            int[] at = {plan.spots[i], plan.spots[i + 1], plan.spots[i + 2]};
            if (tag == TAG_RACK) {
                lamps.add(at);
            } else if (tag == TAG_EXIT) {
                lamps.add(at);
            } else if (tag == TAG_MAINFRAME) {
                frames.add(at);
            }
        }
    }

    /**
     * The room's own life: a rotating slice of lamps changes state every second,
     * and every {@link #FAILURE_PERIOD} ticks the whole room drops to emergency
     * lighting and comes back.
     */
    private static void lights(ServerLevel level) {
        List<int[]> lamps = LAMPS.get(level.dimension());
        if (lamps == null || lamps.isEmpty()) {
            return;
        }
        BlockState lit = palette()[LAMP];
        BlockState dark = palette()[GLASS];
        if (lit == null || dark == null) {
            return;
        }
        long now = level.getGameTime();
        long cycle = Math.floorMod(now, FAILURE_PERIOD);
        boolean failing = cycle < FAILURE_TICKS;
        if (failing && cycle == 0L) {
            level.playSound((Entity) null, lamps.get(0)[0], lamps.get(0)[1], lamps.get(0)[2],
                    McsmSounds.OBLIVION_GLITCH, SoundSource.BLOCKS, 1.2F, 0.6F);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(lamps.get(0)[0], lamps.get(0)[1], lamps.get(0)[2])
                        < 96.0D * 96.0D) {
                    player.sendSystemMessage(Component.literal(
                            "\u00a78\u00b7 the racks drop to emergency lighting \u00b7 \u00a7fsomething "
                            + "is coming back up"));
                }
            }
        }
        if (failing) {
            if (cycle == 0L || cycle % 10L == 0L) {
                for (int[] at : lamps) {
                    level.setBlock(new BlockPos(at[0], at[1], at[2]), dark, 2);
                }
            }
            return;
        }
        if (now % CHATTER_PERIOD != 0L) {
            return;
        }
        int start = (int) Math.floorMod(now / CHATTER_PERIOD, lamps.size());
        for (int i = 0; i < CHATTER; i++) {
            int[] at = lamps.get((start + i * 7) % lamps.size());
            boolean on = Math.floorMod(now / CHATTER_PERIOD + (long) i * 13L, 3L) != 0;
            level.setBlock(new BlockPos(at[0], at[1], at[2]), on ? lit : dark, 2);
        }
        if (now % 100L == 0L) {
            // one bright scatter, so the hall reads as busy rather than blinking
            for (int[] at : lamps) {
                if (Math.floorMod(at[0] * 31L + at[2] * 17L + now / 100L, 7L) == 0L) {
                    level.setBlock(new BlockPos(at[0], at[1], at[2]), lit, 2);
                }
            }
        }
    }

    /** The mainframe: reads the room's log, and hands out the keycard once. */
    private static void console(ServerLevel level) {
        List<int[]> frames = MAINFRAMES.get(level.dimension());
        if (frames == null || frames.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            for (int[] at : frames) {
                double d = player.distanceToSqr(at[0] + 0.5D, at[1] + 0.5D, at[2] + 0.5D);
                if (d > CONSOLE_RANGE * CONSOLE_RANGE) {
                    continue;
                }
                String key = player.getUUID() + "@" + at[0] + "," + at[2];
                if (GREETED.add(key)) {
                    player.sendSystemMessage(Component.literal(
                            "\u00a75\u00a7lMAINFRAME \u00a78\u00b7 a machine that still has power"));
                    player.sendSystemMessage(Component.literal(
                            "\u00a78" + logLine(at[0], at[2])));
                    Item keycard = item("mcsm:city_keycard");
                    if (keycard != null && player.getInventory().add(new ItemStack(keycard))) {
                        player.sendSystemMessage(Component.literal(
                                "\u00a7f\u00b7 a city keycard drops out of the tray"));
                    }
                    level.playSound((Entity) null, at[0], at[1], at[2], McsmSounds.TERMINAL_OPEN,
                            SoundSource.BLOCKS, 0.8F, 1.0F);
                    continue;
                }
                Long last = SPOKEN.get(player.getUUID());
                if (last != null && now - last < 600L) {
                    continue;
                }
                SPOKEN.put(player.getUUID(), now);
                player.sendSystemMessage(Component.literal("\u00a78" + logLine(at[0] + (int) now,
                        at[2])));
            }
        }
    }

    private static final String[] LOG = {
        "last entry: the racks were moved below ground the day the sky changed",
        "coolant nominal \u00b7 4 of 12 halls responding \u00b7 the rest are not there any more",
        "the generator runs itself now \u00b7 nobody has signed in for a very long time",
        "warning: an unauthorised user is standing in the vault",
        "the storm cannot be reached from here \u00b7 the storm does not agree",
        "backup complete \u00b7 restored from a copy that has not been made yet",
    };

    private static String logLine(int x, int z) {
        return LOG[Math.floorMod(x * 31 + z * 17, LOG.length)];
    }

    // -------------------------------------------------------------------------
    // Blocks
    // -------------------------------------------------------------------------

    private static volatile BlockState[] palette;

    private static BlockState[] palette() {
        BlockState[] local = palette;
        if (local != null) {
            return local;
        }
        BlockState[] next = new BlockState[11];
        next[AIR] = Blocks.AIR.defaultBlockState();
        next[PLATE] = state("mcsm:rusted_plate", McsmContent.RUSTED_PLATE);
        next[GRATE] = state("mcsm:rebar_grate", McsmContent.REBAR_GRATE);
        next[TILES] = state("mcsm:city_tiles", McsmContent.CITY_TILES);
        next[LAMP] = state("mcsm:glitch_lamp", McsmContent.GLITCH_LAMP);
        next[GLASS] = state("mcsm:reality_glass", McsmContent.REALITY_GLASS);
        next[CORE] = state("mcsm:void_core", McsmContent.VOID_CORE);
        next[ANCHOR] = state("mcsm:rift_anchor", McsmContent.RIFT_ANCHOR);
        next[CRYSTAL] = state("mcsm:memory_crystal", McsmContent.MEMORY_CRYSTAL);
        next[BARS] = state("minecraft:iron_bars", null);
        next[FLOOR] = state("mcsm:cracked_road", McsmContent.CRACKED_ROAD);
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

    private static Item item(String id) {
        try {
            int colon = id.indexOf(':');
            return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(
                    id.substring(0, colon), id.substring(colon + 1)));
        } catch (Throwable t) {
            return null;
        }
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

    /** For /ds server: where the nearest room is, in words. */
    public static String guidance(int x, int z) {
        int[] near = nearestRoom(x, z);
        if (near == null) {
            return "no server room within " + (REGION * 4) + " blocks";
        }
        return "the nearest server room is " + (int) Math.sqrt(near[2]) + " blocks away at "
                + near[0] + ", " + near[1] + " (the hatch is on the surface, the racks are at y="
                + FLOOR_Y + ")";
    }

    /** How many racks this level is running, for /ds and the panel. */
    public static int lamps(Level level) {
        List<int[]> list = LAMPS.get(level.dimension());
        return list == null ? 0 : list.size();
    }

    /** The distance to the nearest mainframe, or -1 when this level has none. */
    public static double nearestConsole(Level level, Vec3 at) {
        List<int[]> frames = MAINFRAMES.get(level.dimension());
        if (frames == null || frames.isEmpty()) {
            return -1.0D;
        }
        double best = Double.MAX_VALUE;
        for (int[] f : frames) {
            best = Math.min(best, at.distanceTo(new Vec3(f[0] + 0.5D, f[1] + 0.5D, f[2] + 0.5D)));
        }
        return best;
    }
}
