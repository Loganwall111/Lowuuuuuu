package net.mcsm.extras;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.mcsm.extras.entity.McsmEntities;

/**
 * BUILD #451 -- THE VOID, AND THE WAYS INTO EVERY DIMENSION.
 *
 * <p>Two things in one build, because they only make sense together: a new
 * dimension that is mostly NOTHING, and a real portal for each dimension instead of
 * a command.
 *
 * <p><b>THE VOID.</b> The report, in the player's own words: "this dimension is
 * filled with basically nothing ... you fall infinitely, you don't hit the
 * dimension ... it's very very you can walk through nothing ... there's houses and
 * rivers and weird stuff happens, glitchy walls ... it's a strange place". So:
 *
 * <ul>
 *   <li><b>It is genuinely empty.</b> The dimension's terrain layers are one layer of
 *       BARRIER at the bottom: nothing you can see, everything you can stand on. Above
 *       that there is no floor, no ceiling and no terrain, so you fall the whole way
 *       down and land on nothing at all.</li>
 *   <li><b>The fall does not kill you, it keeps you.</b> Past {@link #CATCH_Y} the
 *       void catches you and sets you down on the next shelf of itself. Falling for
 *       ever would be a death sentence, not a dimension.</li>
 *   <li><b>The nothing has things in it.</b> Floating shelves of decayed stone and
 *       city tile, houses with windows, rivers of the dimension's own water that run
 *       off the edge, glitch walls that stand in the air on their own, and vortex
 *       ribs around the bigger shelves. Everything is scattered in three dimensions
 *       so that walking through nothing is how you find it.</li>
 *   <li><b>It is purple, and it is loud about it.</b> The dimension ships its own
 *       biome: purple water, purple water fog, violet sky, its own fog distances.</li>
 * </ul>
 *
 * <p><b>THE PORTALS.</b> One per dimension, and each one is built out of that
 * dimension's own material, so a player can tell them apart from across a room: the
 * rift's arch is rift anchors around reality glass, adams' is memory crystal around
 * city tile, and the void's is void core around black-hole glass. Walk into the
 * middle and you go. No command, no item, no menu: a doorway. The doors themselves
 * live in {@link McsmPortals}.</p>
 *
 * <p>Both halves are switched and both are in the panel. {@code /ds portal build
 * &lt;id&gt;} raises a frame in front of you so there is nothing to guess about the
 * shape, and {@code /ds reality void} goes in directly when you would rather skip
 * the walk.
 */
public final class McsmVoid {

    /** Blocks per region, and how far an island cluster reaches. */
    public static final int REGION = 192;
    /** Built when a player is this close. */
    public static final int ACTIVATE = 320;
    /** Blocks written per level tick. */
    public static final int OPS_PER_TICK = 1500;
    /**
     * BUILD #452 -- THE INVISIBLE FLOOR.
     *
     * <p>"could you make it an invisible floor at the very bottom". It is the
     * dimension's own flat layer now: one layer of {@code minecraft:barrier} at
     * {@code min_y}, which is solid, invisible, everywhere at once, and costs
     * nothing to run. You land on it, you stand on it, and there is nothing to see
     * holding you up -- which is the point.
     */
    public static final int FLOOR_Y = McsmVoidTiers.FLOOR_Y;
    /**
     * BUILD #481 -- AND THE FLOOR MOVED. The void's dimension is now 4064 blocks
     * tall (min_y -2032, the engine's own maximum), so the flat layer that IS the
     * floor sits at -2032 and the five tiers of {@link McsmVoidTiers} hang between
     * the gel's surface and it. Everything that used to be "the deep" is tier 0 of
     * something much bigger; the floor is still invisible and still everywhere.
     */
    /** The safety net is BELOW the floor: only a hole in the world can reach it. */
    public static final int CATCH_Y = McsmVoidTiers.FLOOR_Y - 6;
    /** Where it sets them down when even that fails. */
    public static final int SHELF_Y = 210;
    /**
     * The void's own door frame, and what stands in it. BUILD #458 -- named
     * through the identity table rather than typed out, so the arch a player
     * walks through cannot be made of another world's block by a typo.
     */
    public static final String FRAME_BLOCK = McsmIdentity.material(McsmIdentity.VOID, "anchor");
    public static final String DOOR_BLOCK = McsmIdentity.material(McsmIdentity.VOID, "glass");
    private static final int MAX_PENDING = 3;
    private static final int SHELVES_PER_REGION = 7;

    private static final McsmBuildQueue.Queue QUEUE = new McsmBuildQueue.Queue(MAX_PENDING);
    private static final Set<Long> QUEUED = ConcurrentHashMap.newKeySet();
    private static final Set<Long> BUILT = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> LAST_CATCH = new ConcurrentHashMap<>();

    // palette indices
    private static final int AIR = 0;
    private static final int STONE = 1;
    private static final int TILES = 2;
    private static final int GLASS = 3;
    private static final int LAMP = 4;
    private static final int WATER = 5;
    private static final int CORE = 6;
    private static final int ANCHOR = 7;
    private static final int PLANK = 8;
    private static final int BONE = 9;
    /** BUILD #459 -- the void's own seal (on the way into a house) and its cache. */
    private static final int LOCK = 10;
    private static final int CACHE = 11;

    private McsmVoid() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmVoid::tick);
            System.out.println("[ds] the void is listening (" + FRAME_BLOCK + " / " + DOOR_BLOCK
                    + " doors, shelves every " + REGION + " blocks)");
        } catch (Throwable t) {
            System.err.println("[ds] the void could not hook the level tick: " + t);
        }
    }

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.voidReality || !level.dimension().equals(DIMENSION)) {
                return;
            }
            if (!QUEUE.full()) {
                for (ServerPlayer player : level.players()) {
                    planNear(level, player);
                }
            }
            QUEUE.pump(level, OPS_PER_TICK, palette(), plan -> markBuilt(plan.key));
            for (ServerPlayer player : level.players()) {
                catchFall(level, player);
                // BUILD #483 -- the seam: the bottom of the world is a door, and the
                // fall continues 1781 blocks higher with its velocity intact.
                McsmVoidLoop.tick(level, player);
                // BUILD #482 -- and the sponge, grown around the fall as it passes
                // through the second tier: the maze is where the player is, and it
                // stays behind them.
                McsmVoidSponge.tick(level, player);
            }
            // BUILD #456 -- the void walkers, as the real thing now. They used to be
            // re-kitted vanilla bodies rolled out of the shared bestiary; they are
            // mcsm:voidwalker and mcsm:void_lurker, with their own models and their
            // own behaviour, and they live here, in the nothing, between the shelves.
            if (level.getGameTime() % 600L == 0L) {
                for (ServerPlayer player : level.players()) {
                    BlockPos at = shelfUnder(level, player.blockPosition());
                    spawnDwellers(level, at == null ? player.blockPosition() : at, player);
                }
            }
            // BUILD #483 -- THE GHOST WHALES. The plan's colossal passive fauna,
            // drifting through the luminous cavern with no gravity of their own: they
            // are the mod's own whale beasts (mcsm:whale_monster, the class whose
            // whale already swims rather than walks), kinded, named, and set loose in
            // the tier that has the room for them -- and their voice is already their
            // own, the vast drone the beast plays when it is a whale.
            if (level.getGameTime() % 1200L == 0L) {
                for (ServerPlayer player : level.players()) {
                    spawnVoidWhale(level, player);
                }
            }
        } catch (Throwable t) {
            System.err.println("[ds] void tick failed: " + t);
        }
    }

    /**
     * BUILD #456 -- WHO LIVES IN THE NOTHING.
     *
     * <p>One to three walkers, placed off to the side and out of arm's reach so
     * nothing ever appears in a player's face, and -- on one roll in six for the
     * last of them -- the mini-boss instead. Rare enough that meeting the lurker is
     * an event; common enough that a player who spends a session down here will.
     */
    private static void spawnDwellers(ServerLevel level, BlockPos near, ServerPlayer player) {
        try {
            if (McsmEntities.VOIDWALKER == null) {
                return;
            }
            net.minecraft.util.RandomSource rng = level.getRandom();
            int count = 1 + rng.nextInt(3);
            double angle = rng.nextDouble() * Math.PI * 2.0D;
            for (int i = 0; i < count; i++) {
                double a = angle + i * 1.15D;
                int x = near.getX() + (int) Math.round(Math.cos(a) * (12.0D + rng.nextInt(12)));
                int z = near.getZ() + (int) Math.round(Math.sin(a) * (12.0D + rng.nextInt(12)));
                net.minecraft.world.entity.EntityType<?> type =
                        (i == count - 1 && rng.nextInt(6) == 0)
                                ? McsmEntities.VOID_LURKER : McsmEntities.VOIDWALKER;
                // BUILD #483 -- and, one time in four, the one that talks instead.
                if (McsmEntities.VOID_DWELLER != null && rng.nextInt(4) == 0) {
                    type = McsmEntities.VOID_DWELLER;
                }
                if (type == null) {
                    continue;
                }
                net.minecraft.world.entity.Entity spawned = type.create(level,
                        net.minecraft.world.entity.EntitySpawnReason.EVENT);
                if (!(spawned instanceof net.minecraft.world.entity.Mob mob)) {
                    continue;
                }
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(near),
                        net.minecraft.world.entity.EntitySpawnReason.EVENT, (net.minecraft.world.entity.SpawnGroupData) null);
                mob.setCustomName(net.minecraft.network.chat.Component.literal(
                        spawned.getType() == McsmEntities.VOID_LURKER ? "The Lurker"
                                : spawned.getType() == McsmEntities.VOID_DWELLER
                                        ? "Void Dweller" : "Voidwalker"));
                mob.setCustomNameVisible(false);
                mob.snapTo(x + 0.5D, near.getY(), z + 0.5D, rng.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(mob);
            }
        } catch (Throwable ignored) {
            // a dweller that cannot be placed is not an error worth a stack trace
        }
    }

    /**
     * One ghost whale, above a player in the cavern -- and only in the cavern, and
     * only ever a couple of them, because a whale is not scenery.
     */
    private static void spawnVoidWhale(ServerLevel level, ServerPlayer player) {
        try {
            if (McsmEntities.WHALE_MONSTER == null && McsmEntities.VOID_WHALE == null) {
                return;
            }
            double y = player.getY();
            if (y > McsmVoidTiers.BASELINE_FLOOR || y < McsmVoidTiers.LUMINOUS_FLOOR) {
                return;
            }
            net.minecraft.util.RandomSource rng = level.getRandom();
            if (rng.nextInt(3) != 0) {
                return;
            }
            // BUILD #483b -- run 609's one error: this file imports what it needs one
            // name at a time, so the type has to be spelled out in full here.
            net.minecraft.world.phys.AABB box = player.getBoundingBox().inflate(220.0D);
            if (level.getEntitiesOfClass(net.mcsm.extras.entity.McsmBeast.class, box).size() >= 2) {
                return;
            }
            // BUILD #484 -- the plan's own id when it is there, the whale monster
            // otherwise: either way what drifts out of the dark is a whale.
            net.minecraft.world.entity.EntityType<net.mcsm.extras.entity.McsmBeast> type =
                    McsmEntities.VOID_WHALE != null ? McsmEntities.VOID_WHALE : McsmEntities.WHALE_MONSTER;
            if (type == null) {
                return;
            }
            net.minecraft.world.entity.Entity spawned = type.create(level,
                    net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (!(spawned instanceof net.mcsm.extras.entity.McsmBeast whale)) {
                return;
            }
            whale.setKind(net.mcsm.extras.entity.McsmBeast.WHALE);
            whale.finalizeSpawn(level, level.getCurrentDifficultyAt(player.blockPosition()),
                    net.minecraft.world.entity.EntitySpawnReason.EVENT,
                    (net.minecraft.world.entity.SpawnGroupData) null);
            whale.setCustomName(net.minecraft.network.chat.Component.literal("Void Whale"));
            whale.setCustomNameVisible(false);
            double a = rng.nextDouble() * Math.PI * 2.0D;
            whale.snapTo(player.getX() + Math.cos(a) * (70.0D + rng.nextInt(90)),
                    Math.min(y + 24.0D + rng.nextInt(40), McsmVoidTiers.BASELINE_FLOOR - 8),
                    player.getZ() + Math.sin(a) * (70.0D + rng.nextInt(90)),
                    rng.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(whale);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "\u00a78something enormous just went by above you \u00b7 it is not hunting"));
        } catch (Throwable ignored) {
            // a whale that cannot be placed is not worth a stack trace
        }
    }

    /** The void's own dimension key. */
    public static final net.minecraft.resources.ResourceKey<Level> DIMENSION =
            net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("mcsm", "void_reality"));

    // ---------------------------------------------------------------------
    // The fall
    // ---------------------------------------------------------------------

    /**
     * THE VOID KEEPS YOU.
     *
     * <p>The floor catches everybody: {@link #FLOOR_Y} is a solid barrier layer, so
     * an ordinary fall ends standing on nothing. {@link #CATCH_Y} is below it and
     * exists only for the impossible case -- a hole in the world, somebody mining the
     * barrier -- and sets them back on the floor rather than letting the void damage
     * under every world finish the job. A cooldown keeps it from becoming a
     * trampoline.
     */
    private static void catchFall(ServerLevel level, ServerPlayer player) {
        if (player.getY() > CATCH_Y) {
            return;
        }
        long now = level.getGameTime();
        Long last = LAST_CATCH.get(player.getUUID());
        if (last != null && now - last < 40L) {
            return;
        }
        LAST_CATCH.put(player.getUUID(), now);
        BlockPos shelf = shelfUnder(level, new BlockPos((int) Math.floor(player.getX()), SHELF_Y,
                (int) Math.floor(player.getZ())));
        BlockPos target = shelf == null
                ? new BlockPos((int) Math.floor(player.getX()), SHELF_Y, (int) Math.floor(player.getZ()))
                : shelf.above(2);
        player.teleportTo(level, target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D,
                java.util.Set.of(), player.getYRot(), 0.0F, false);
        player.sendSystemMessage(Component.literal(
                "\u00a75\u00a7lTHE VOID \u00a78\u00b7 you fell past everything, and it caught you"));
        player.sendSystemMessage(Component.literal(
                "\u00a78there is no floor here. that is the dimension, not a bug"));
    }

    /** The highest solid block under a column, or null when the column is nothing. */
    private static BlockPos shelfUnder(ServerLevel level, BlockPos from) {
        try {
            for (int y = from.getY(); y > level.getMinY() + 1; y--) {
                BlockPos at = new BlockPos(from.getX(), y, from.getZ());
                BlockState state = level.getBlockState(at);
                if (!state.isAir()) {
                    return at;
                }
            }
        } catch (Throwable ignored) {
            // no shelf: the void keeps them at the catch line
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // Entry
    // ---------------------------------------------------------------------

    /** The door the portals and the command both use. */
    public static boolean enter(ServerPlayer player) {
        try {
            if (!McsmExtrasConfig.voidReality || !(player.level() instanceof ServerLevel level)) {
                return false;
            }
            ServerLevel target = level.getServer().getLevel(DIMENSION);
            if (target == null) {
                return false;
            }
            BlockPos landing = landing(target, player);
            player.teleportTo(target, landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D,
                    java.util.Set.of(), player.getYRot(), 0.0F, false);
            player.sendSystemMessage(Component.literal(
                    "\u00a75\u00a7lTHE VOID \u00a78\u00b7 there is nothing under you now. that is "
                    + "the point."));
            // a shelf to land on, so the arrival is a place rather than a plunge
            if (!BUILT.contains(regionKey(Math.floorDiv(landing.getX(), REGION),
                    Math.floorDiv(landing.getZ(), REGION)))) {
                planHere(target, landing);
            }
            return true;
        } catch (Throwable t) {
            System.err.println("[ds] could not open the void: " + t);
            return false;
        }
    }

    private static BlockPos landing(ServerLevel level, ServerPlayer player) {
        try {
            if (level.dimension().equals(DIMENSION)) {
                return new BlockPos((int) Math.floor(player.getX()), SHELF_Y,
                        (int) Math.floor(player.getZ()));
            }
        } catch (Throwable ignored) {
            // fall through to the standard landing
        }
        return new BlockPos(0, SHELF_Y, 0);
    }

    // ---------------------------------------------------------------------
    // What little there is
    // ---------------------------------------------------------------------

    private static void planNear(ServerLevel level, ServerPlayer player) {
        int prx = Math.floorDiv((int) Math.floor(player.getX()), REGION);
        int prz = Math.floorDiv((int) Math.floor(player.getZ()), REGION);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int rx = prx + dx;
                int rz = prz + dz;
                long key = regionKey(rx, rz);
                if (BUILT.contains(key) || QUEUED.contains(key)) {
                    continue;
                }
                int ox = rx * REGION + REGION / 2;
                int oz = rz * REGION + REGION / 2;
                if (Math.hypot(player.getX() - ox, player.getZ() - oz) > ACTIVATE) {
                    continue;
                }
                QUEUED.add(key);
                try {
                    QUEUE.add(plan(level, key, ox, oz));
                } catch (Throwable t) {
                    System.err.println("[ds] void plan failed at " + ox + "," + oz + ": " + t);
                }
                if (QUEUE.full()) {
                    return;
                }
            }
        }
    }

    /** Force a shelf at the arrival point, so coming in is not the same as falling. */
    private static void planHere(ServerLevel level, BlockPos at) {
        try {
            long key = regionKey(Math.floorDiv(at.getX(), REGION), Math.floorDiv(at.getZ(), REGION));
            if (BUILT.contains(key) || QUEUED.contains(key)) {
                return;
            }
            QUEUED.add(key);
            QUEUE.add(shelf(level, key, at.getX(), at.getY() - 1, at.getZ(), true));
        } catch (Throwable ignored) {
            // the void keeps them regardless
        }
    }

    private static McsmBuildQueue.Plan plan(ServerLevel level, long key, int ox, int oz) {
        // a region is SEVEN shelves in one plan, and one shelf can ask for ten thousand
        // blocks of its own: 26000 would have silently dropped the last four shelves
        McsmBuildQueue.Planner planner = new McsmBuildQueue.Planner(120000);
        for (int i = 0; i < SHELVES_PER_REGION; i++) {
            long seed = mix(key * 0x9E3779B97F4A7C15L + i * 0x5DEECE66DL);
            int x = ox + (int) (Math.floorMod(seed, 2L * REGION) - REGION);
            long seed2 = mix(seed ^ 0x51ED270BL);
            int z = oz + (int) (Math.floorMod(seed2, 2L * REGION) - REGION);
            long seed3 = mix(seed2 * 0x2545F491L);
            int y = 24 + (int) Math.floorMod(seed3, 220L);
            shelfInto(planner, key, x, y, z, false);
        }
        return planner.plan(key);
    }

    /**
     * One shelf of the void: the ground, something standing on it, and something
     * wrong with it. Rivers run off the edge; glitch walls stand on their own.
     */
    private static McsmBuildQueue.Plan shelf(ServerLevel level, long key, int cx, int cy, int cz,
                                             boolean landing) {
        McsmBuildQueue.Planner planner = new McsmBuildQueue.Planner(32000);
        shelfInto(planner, key, cx, cy, cz, landing);
        return planner.plan(key ^ 0x5F0L);
    }

    /**
     * One shelf, carved straight into somebody else's planner -- because a region is
     * ONE plan made of many shelves, and a landing is one plan made of one, and the
     * two have to go through the same builder. (Run 534 was the first compile of this
     * file: the region pass still called the plan-returning shelf() and handed a
     * Planner to a parameter asking for a ServerLevel.)
     */
    private static void shelfInto(McsmBuildQueue.Planner planner, long key, int cx, int cy, int cz,
                                  boolean landing) {
        long seed = mix(key ^ ((long) cx * 31L) ^ ((long) cz * 17L));
        int w = 14 + (int) Math.floorMod(seed, 12L);
        int d = 14 + (int) Math.floorMod(mix(seed), 12L);
        int kind = (int) Math.floorMod(mix(seed * 0x2545F4914F6CDD1DL), 5L);
        for (int x = -w; x <= w; x++) {
            for (int z = -d; z <= d; z++) {
                double edge = Math.hypot((double) x / w, (double) z / d);
                if (edge > 1.0D) {
                    continue;
                }
                int surface = (int) Math.round(cy + 3.0D
                        - Math.max(0.0D, edge - 0.55D) * 6.0D);
                planner.put(cx + x, surface, cz + z, STONE);
                planner.put(cx + x, surface + 1, cz + z, AIR);
                planner.put(cx + x, surface + 2, cz + z, AIR);
                planner.put(cx + x, surface + 3, cz + z, AIR);
                if (Math.floorMod(seed + x * 7L + z * 13L, 11L) == 0L) {
                    planner.put(cx + x, surface - 1, cz + z, TILES);
                }
                if (edge > 0.86D && Math.floorMod(seed + x * 3L + z * 5L, 5L) == 0L) {
                    planner.put(cx + x, surface, cz + z, ANCHOR);
                }
            }
        }
        // a river of the dimension's own water, running off the edge
        for (int x = -w; x <= w; x++) {
            for (int z = -2; z <= 2; z++) {
                double edge = Math.hypot((double) x / w, (double) z / d);
                if (edge > 1.0D) {
                    continue;
                }
                int surface = (int) Math.round(cy + 3.0D - Math.max(0.0D, edge - 0.55D) * 6.0D);
                planner.put(cx + x, surface, cz + z, WATER);
                planner.put(cx + x, surface - 1, cz + z, STONE);
                planner.put(cx + x, surface + 1, cz + z, AIR);
            }
        }
        // what stands on the shelf
        switch (kind) {
            case 0 -> house(planner, cx, cy + 4, cz, seed);
            case 1 -> glitchWall(planner, cx, cy + 4, cz, seed);
            case 2 -> vortexRibs(planner, cx, cy + 4, cz, w, d, seed);
            case 3 -> brokenPillars(planner, cx, cy + 4, cz, seed);
            default -> {
                if (landing) {
                    house(planner, cx, cy + 4, cz, seed);
                } else {
                    planner.put(cx, cy + 4, cz, CORE);
                }
            }
        }
    }

    /** A house in the nothing: walls, glass windows, planks, a roof with a hole. */
    private static void house(McsmBuildQueue.Planner planner, int cx, int y, int cz, long seed) {
        int w = 5 + (int) Math.floorMod(seed, 4L);
        int h = 4 + (int) Math.floorMod(mix(seed), 3L);
        planner.box(cx - w, y, cz - w, cx + w, y, cz + w, PLANK);
        for (int dy = 1; dy < h; dy++) {
            planner.box(cx - w, y + dy, cz - w, cx + w, y + dy, cz + w, AIR);
            planner.box(cx - w, y + dy, cz - w, cx + w, y + dy, cz - w, PLANK);
            planner.box(cx - w, y + dy, cz + w, cx + w, y + dy, cz + w, PLANK);
            planner.box(cx - w, y + dy, cz - w, cx - w, y + dy, cz + w, PLANK);
            planner.box(cx + w, y + dy, cz - w, cx + w, y + dy, cz + w, PLANK);
            if (dy % 2 == 0) {
                planner.put(cx - w, y + dy, cz, GLASS);
                planner.put(cx + w, y + dy, cz, GLASS);
                planner.put(cx, y + dy, cz - w, GLASS);
                planner.put(cx, y + dy, cz + w, GLASS);
            }
        }
        planner.box(cx - w, y + h, cz - w, cx + w, y + h, cz + w, PLANK);
        // a hole in the roof, because nothing here is finished
        planner.box(cx, y + h, cz, cx + 1, y + h, cz + 1, AIR);
        planner.put(cx, y + 1, cz, LAMP);
        // BUILD #459 -- and a way in that is SHUT. The doorway carries the void's
        // own lock (the key is in the void's own cache, a few steps away), so a
        // house in the nothing is a room you have to open rather than walk into.
        planner.put(cx, y + 1, cz + w, LOCK);
        planner.put(cx, y + 2, cz + w, AIR);
        planner.put(cx - 1, y + 1, cz + w - 1, CACHE);
    }

    /** A wall of glitch that stands in the air on its own. */
    private static void glitchWall(McsmBuildQueue.Planner planner, int cx, int y, int cz, long seed) {
        int len = 9 + (int) Math.floorMod(seed, 9L);
        int tall = 6 + (int) Math.floorMod(mix(seed), 9L);
        for (int i = 0; i < len; i++) {
            int x = cx - len / 2 + i + (int) Math.floorMod(seed + i, 2L);
            for (int dy = 0; dy < tall; dy++) {
                int state = Math.floorMod(seed + i * 3L + dy * 5L, 6L) == 0L ? LAMP : GLASS;
                planner.put(x, y + dy, cz, state);
            }
        }
    }

    /** The ram's horns: ribbed rings around a shelf, so the air has a shape. */
    private static void vortexRibs(McsmBuildQueue.Planner planner, int cx, int y, int cz,
                                   int w, int d, long seed) {
        for (int ring = 0; ring < 3; ring++) {
            int ry = y + 3 + ring * 5;
            int radius = Math.min(w, d) - ring * 2;
            for (int a = 0; a < 24; a++) {
                double ang = (a / 24.0D) * Math.PI * 2.0D + ring * 0.35D;
                int x = cx + (int) Math.round(Math.cos(ang) * radius);
                int z = cz + (int) Math.round(Math.sin(ang) * radius);
                planner.put(x, ry, z, ANCHOR);
                if (a % 3 == 0) {
                    planner.put(x, ry + 1, z, CORE);
                }
            }
        }
    }

    private static void brokenPillars(McsmBuildQueue.Planner planner, int cx, int y, int cz,
                                      long seed) {
        for (int i = 0; i < 5; i++) {
            long s = mix(seed + i * 0x9E3779B9L);
            int x = cx + (int) (Math.floorMod(s, 11L) - 5L);
            int z = cz + (int) (Math.floorMod(mix(s), 11L) - 5L);
            int h = 3 + (int) Math.floorMod(mix(s * 3L), 8L);
            for (int dy = 0; dy < h; dy++) {
                planner.put(x, y + dy, z, dy == h - 1 ? BONE : STONE);
            }
        }
    }

    // ---------------------------------------------------------------------
    // Blocks
    // ---------------------------------------------------------------------

    private static volatile BlockState[] palette;

    private static BlockState[] palette() {
        BlockState[] local = palette;
        if (local != null) {
            return local;
        }
        BlockState[] next = new BlockState[12];
        next[AIR] = Blocks.AIR.defaultBlockState();
        // BUILD #458 -- THE VOID'S OWN MATERIAL. These four were decayed_stone,
        // city_tiles, decayed_planks and decayed_stone_bricks: the decayed
        // reality's building set, floating in another dimension. The void is made
        // of the void now (McsmIdentity.VOID), and nothing in this palette is
        // borrowed from another world.
        next[STONE] = state("mcsm:void_stone", McsmContent.VOID_STONE);
        next[TILES] = state("mcsm:void_tiles", McsmContent.VOID_TILES);
        next[GLASS] = state("mcsm:void_glass", McsmContent.VOID_GLASS);
        next[LAMP] = state("mcsm:void_lamp", McsmContent.VOID_LAMP);
        // the dimension's own water: plain water, coloured by the void's biome, so
        // it is purple without a new fluid and behaves like water because it is
        next[WATER] = Blocks.WATER.defaultBlockState();
        next[CORE] = state("mcsm:void_core", McsmContent.VOID_CORE);
        next[ANCHOR] = state("mcsm:void_anchor", McsmContent.VOID_ANCHOR);
        next[PLANK] = state("mcsm:void_planks", McsmContent.VOID_PLANKS);
        // "decayed_bone" is an ITEM in the content pack, not a block (and asking
        // the block registry for a name that is not a block hands back AIR, not
        // null). Broken pillars are capped with the void's own bone, which is a
        // block and reads as the material it is named for.
        next[BONE] = state("mcsm:void_bone", McsmContent.VOID_BONE);
        next[LOCK] = state("mcsm:void_lock", McsmContent.VOID_LOCK);
        next[CACHE] = state("mcsm:void_cache", McsmContent.VOID_CACHE);
        palette = next;
        return next;
    }

    private static BlockState state(String id, Block fallback) {
        try {
            int colon = id.indexOf(':');
            if (colon > 0) {
                Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                id.substring(0, colon), id.substring(colon + 1)));
                // the block registry's default IS air, so a missing id comes back as
                // air rather than null: air is never an acceptable answer here
                if (block != null && block != Blocks.AIR) {
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

    /** For /ds reality and /ds portal. */
    public static String stats() {
        return BUILT.size() + " shelves built this session, "
                + (QUEUED.size() - BUILT.size()) + " queued, catch line y=" + CATCH_Y
                + " \u00b7 " + McsmVoidSponge.state() + " \u00b7 " + McsmVoidLoop.state();
    }

    /** Called by the queue when a plan lands. */
    public static void markBuilt(long key) {
        BUILT.add(key);
    }

    /** The doorway the portal builder raises for this dimension is the void's own. */
    public static String doorwayFrame() {
        return FRAME_BLOCK;
    }

    public static String doorwayDoor() {
        return DOOR_BLOCK;
    }
}
