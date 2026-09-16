package net.mcsm.extras;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #443 -- THE INFINITE DIMENSION OF ADAMS.
 *
 * <p>The wishlist line is three words long: "the infinite dimension of adams". It
 * is not something this mod ever had, so it is built here, and it has to earn the
 * word INFINITE rather than being one more room.
 *
 * <p>WHAT "INFINITE" MEANS, CONCRETELY. A datapack dimension is already endless in
 * EXTENT -- no border, no edge. What is not automatic is endless CONTENT: a flat
 * world can be walked for a million blocks and show the same nothing. So the
 * dimension ships with its own generator, which builds in it forever, on a
 * deterministic region grid, as the player walks:
 *
 * <ul>
 *   <li>a repeating PILLAR FIELD -- every 24 blocks a column of decayed brick with
 *       a tiled cap, one in six carrying a lit glitch lamp, one in five a memory
 *       crystal, with torn patches of the storm's own flesh in the ground;</li>
 *   <li>WALKWAYS of city tile joining the grid, so the place reads as something
 *       that was BUILT, endlessly, by something;</li>
 *   <li>one CHAMBER per region -- fifteen by fifteen, walls and a grate roof, a
 *       lit spine inside, a crate of salvage on the floor and, two times in three,
 *       something that was waiting for you in there.</li>
 * </ul>
 *
 * <p>Every region is planned from its own coordinates (splitmix64, the same
 * determinism {@link McsmCities} uses), so the same place is always the same
 * place and no two regions are the same shape. There is no last region and no
 * completion state: walking is the only limit.
 *
 * <p>HOW YOU GET THERE. The {@code adams} ritual -- a ring of city tiles around a
 * memory crystal with the offering in hand ({@link McsmRituals}) -- or
 * {@code /ds reality adams}. The way out is the gesture the decayed reality
 * already uses: hold the Rift Key and sneak. The arrival point is derived from the
 * player's own UUID, so two players who step in together are not inside each
 * other, and the arrival column is SCANNED rather than assumed.
 *
 * <p>Nothing here is client-side and nothing needs a shader: ordinary blocks,
 * ordinary sounds, and the level tick. The dimension itself is
 * {@code data/mcsm/dimension/adams_infinity.json} plus its own dimension type.
 */
public final class McsmAdams {

    /** The dimension: data/mcsm/dimension/adams_infinity.json. */
    public static final ResourceKey<Level> ADAMS = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("mcsm", "adams_infinity"));

    /** Content is planned on this grid; a region always holds the same build. */
    public static final int REGION = 192;
    /** Regions planned around each player: 1 = the 3x3 the player stands in. */
    private static final int VIEW = 1;
    /** Blocks written per level tick (a chamber is ~1800 blocks). */
    public static final int OPS_PER_TICK = 1100;
    /** Regions allowed to be queued at once. Keeps a long teleport from banking work. */
    private static final int MAX_PENDING = 6;
    /** Pillars stand on this spacing across the field. */
    private static final int PILLAR_SPACING = 24;
    /** Grid steps of pillars a region carries (6 = seven pillars each way). */
    private static final int PILLAR_STEPS = 6;
    /** Where the generator starts looking for ground. */
    private static final int SCAN_TOP = 200;
    private static final long COOLDOWN_TICKS = 80L;

    private static final ConcurrentLinkedQueue<Region> REGIONS = new ConcurrentLinkedQueue<>();
    private static final Set<Long> QUEUED = ConcurrentHashMap.newKeySet();
    private static final Set<Long> BUILT = ConcurrentHashMap.newKeySet();
    private static final java.util.Map<UUID, Long> COOLDOWN = new ConcurrentHashMap<>();

    private McsmAdams() {
    }

    // -------------------------------------------------------------------------
    // Wiring
    // -------------------------------------------------------------------------

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmAdams::tick);
            System.out.println("[ds] the infinite dimension of adams is armed (region "
                    + REGION + " blocks, endless generation, no end state)");
        } catch (Throwable t) {
            System.err.println("[ds] adams could not hook the level tick: " + t);
        }
    }

    // -------------------------------------------------------------------------
    // Going in and coming out
    // -------------------------------------------------------------------------

    /** Send a player into the infinite dimension. The ritual and /ds both land here. */
    public static boolean enter(ServerPlayer player) {
        try {
            if (!McsmExtrasConfig.adamsReality || player == null
                    || player.level().getServer() == null) {
                return false;
            }
            ServerLevel target = player.level().getServer().getLevel(ADAMS);
            if (target == null) {
                player.sendSystemMessage(Component.literal(
                        "\u00a75The gate opens onto nothing \u00a78(the infinite dimension is not loaded)"));
                return false;
            }
            return send(player, target);
        } catch (Throwable t) {
            return false;
        }
    }

    /** Where a given player lands: deterministic per player, never stacked. */
    public static int[] gateFor(UUID id) {
        long h = mix(id.getMostSignificantBits() ^ id.getLeastSignificantBits());
        int x = Math.floorDiv((int) Math.floorMod(h >> 21, 8192L) - 4096, REGION) * REGION
                + REGION / 2;
        int z = Math.floorDiv((int) Math.floorMod(h >> 42, 8192L) - 4096, REGION) * REGION
                + REGION / 2;
        return new int[]{x, z};
    }

    /**
     * The teleport. This mirrors {@link McsmReality}'s own arrival path exactly --
     * the same teleportTo overload, the same column scan, the same sound
     * vocabulary -- because that path is already proven against the release jar
     * and a second, invented one would only be a second thing to break.
     */
    private static boolean send(ServerPlayer player, ServerLevel target) {
        boolean intoAdams = target.dimension().equals(ADAMS);
        double x;
        double z;
        if (intoAdams) {
            int[] gate = gateFor(player.getUUID());
            x = gate[0] + 0.5D;
            z = gate[1] + 0.5D;
        } else {
            x = player.getX() + 0.5D;
            z = player.getZ() + 0.5D;
        }
        double y = intoAdams ? ground(target, (int) x, (int) z) + 2.0D
                : Math.max(player.getY(), target.getMinY() + 8.0D);
        player.teleportTo(target, x, y, z, Set.of(), player.getYRot(), 0.0F, false);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        target.playSound((Entity) null, x, y, z, McsmSounds.OBLIVION_WARP,
                SoundSource.PLAYERS, 0.85F, 1.0F);
        target.playSound((Entity) null, x, y, z, McsmSounds.MASSG_WHISPER,
                SoundSource.PLAYERS, 0.45F, 1.35F);
        player.sendSystemMessage(Component.literal(intoAdams
                ? "\u00a75\u00a7lTHE INFINITE DIMENSION \u00a78\u00b7 it does not end, and it is already built"
                : "\u00a75\u00a7lTHE GATE CLOSES \u00a78\u00b7 the endless field is behind you"));
        if (intoAdams) {
            player.sendSystemMessage(Component.literal("\u00a78\u00b7 a chamber stands in every "
                    + REGION + " blocks \u00b7 hold the \u00a7fRift Key\u00a78 and sneak to leave"));
        }
        return true;
    }

    /** First solid block under an arrival, by scanning -- never by assuming. */
    private static double ground(ServerLevel level, int x, int z) {
        try {
            for (int y = SCAN_TOP; y > level.getMinY(); y--) {
                if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                    return y + 1.0D;
                }
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return 66.0D;
    }

    // -------------------------------------------------------------------------
    // The tick: endless generation around every player
    // -------------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.adamsReality || !level.dimension().equals(ADAMS)) {
                return;
            }
            for (ServerPlayer player : level.players()) {
                gesture(player);
                planNear(level, player);
            }
            Region head = REGIONS.peek();
            if (head == null) {
                return;
            }
            int budget = OPS_PER_TICK;
            BlockState[] states = palette();
            while (budget-- > 0 && head.cursor < head.ops.length) {
                int i = head.cursor;
                head.cursor += 4;
                BlockState state = states[head.ops[i + 3]];
                if (state != null) {
                    level.setBlock(new BlockPos(head.ops[i], head.ops[i + 1], head.ops[i + 2]),
                            state, 2);
                }
            }
            if (head.cursor >= head.ops.length) {
                REGIONS.poll();
                BUILT.add(head.key);
                finish(level, head);
            }
        } catch (Throwable t) {
            System.err.println("[ds] adams tick failed: " + t);
        }
    }

    /** Once a region is written: its salvage, and whatever it was keeping. */
    private static void finish(ServerLevel level, Region region) {
        for (int i = 0; i + 3 < region.crates.length; i += 4) {
            fillCrate(level, region.crates[i], region.crates[i + 1], region.crates[i + 2],
                    region.crates[i + 3]);
        }
        for (int i = 0; i + 3 < region.waiting.length; i += 4) {
            release(level, region.waiting[i], region.waiting[i + 1], region.waiting[i + 2],
                    region.waiting[i + 3]);
        }
    }

    /** Sneak with the Rift Key inside adams: the way out. */
    private static void gesture(ServerPlayer player) {
        try {
            if (!player.isShiftKeyDown()) {
                return;
            }
            if (player.getMainHandItem().getItem() != McsmContent.RIFT_KEY
                    && player.getOffhandItem().getItem() != McsmContent.RIFT_KEY) {
                return;
            }
            long now = player.level().getGameTime();
            Long last = COOLDOWN.get(player.getUUID());
            if (last != null && now - last < COOLDOWN_TICKS) {
                return;
            }
            ServerLevel overworld = player.level().getServer() == null ? null
                    : player.level().getServer().overworld();
            if (overworld != null) {
                COOLDOWN.put(player.getUUID(), now);
                send(player, overworld);
            }
        } catch (Throwable ignored) {
            // a gesture that fails costs the player nothing
        }
    }

    private static void planNear(ServerLevel level, ServerPlayer player) {
        if (REGIONS.size() >= MAX_PENDING) {
            return;
        }
        int prx = Math.floorDiv((int) Math.floor(player.getX()), REGION);
        int prz = Math.floorDiv((int) Math.floor(player.getZ()), REGION);
        for (int dx = -VIEW; dx <= VIEW; dx++) {
            for (int dz = -VIEW; dz <= VIEW; dz++) {
                long key = regionKey(prx + dx, prz + dz);
                if (BUILT.contains(key) || QUEUED.contains(key)) {
                    continue;
                }
                QUEUED.add(key);
                try {
                    REGIONS.add(planRegion(level, prx + dx, prz + dz, key));
                } catch (Throwable t) {
                    System.err.println("[ds] adams region plan failed: " + t);
                }
                if (REGIONS.size() >= MAX_PENDING) {
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // The plan: what one region of the endless field holds
    // -------------------------------------------------------------------------

    private static final int AIR = 0;
    private static final int STONE = 1;
    private static final int BRICKS = 2;
    private static final int TILES = 3;
    private static final int WALL = 4;
    private static final int GRATE = 5;
    private static final int LAMP = 6;
    private static final int CRYSTAL = 7;
    private static final int FLESH = 8;
    private static final int CRATE = 9;

    private static Region planRegion(ServerLevel level, int rx, int rz, long key) {
        long seed = mix(key);
        int ox = rx * REGION;
        int oz = rz * REGION;
        int ground = (int) ground(level, ox + REGION / 2, oz + REGION / 2);
        Grow grow = new Grow(24000);

        // ---- the walkway grid -------------------------------------------
        int span = PILLAR_STEPS * PILLAR_SPACING + PILLAR_SPACING;
        for (int gx = 0; gx <= PILLAR_STEPS + 1; gx++) {
            int x = ox + gx * PILLAR_SPACING;
            for (int z = 0; z <= span; z += 2) {
                grow.put(x, ground, oz + z, TILES);
            }
        }
        for (int gz = 0; gz <= PILLAR_STEPS + 1; gz++) {
            int z = oz + gz * PILLAR_SPACING;
            for (int x = 0; x <= span; x += 2) {
                grow.put(ox + x, ground, z, TILES);
            }
        }

        // ---- the pillar field -------------------------------------------
        for (int gx = 0; gx <= PILLAR_STEPS; gx++) {
            for (int gz = 0; gz <= PILLAR_STEPS; gz++) {
                long h = mix(seed ^ ((long) gx << 32) ^ gz);
                int x = ox + gx * PILLAR_SPACING + 6 + (int) Math.floorMod(h >> 8, 5L) - 2;
                int z = oz + gz * PILLAR_SPACING + 6 + (int) Math.floorMod(h >> 16, 5L) - 2;
                int height = 4 + (int) Math.floorMod(h >> 24, 11L);
                int body = Math.floorMod(h >> 32, 4L) == 0 ? STONE : BRICKS;
                for (int y = ground + 1; y <= ground + height; y++) {
                    grow.put(x, y, z, body);
                }
                grow.put(x, ground + height + 1, z, TILES);
                long roll = Math.floorMod(h >> 40, 6L);
                if (roll == 0) {
                    grow.put(x, ground + height + 2, z, LAMP);
                } else if (roll == 1) {
                    grow.put(x, ground + height + 2, z, CRYSTAL);
                }
                if (Math.floorMod(h >> 52, 7L) == 0) {
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            if ((dx * dx + dz * dz) <= 5) {
                                grow.put(x + dx, ground, z + dz, FLESH);
                            }
                        }
                    }
                }
            }
        }

        // ---- the chamber -------------------------------------------------
        int cx = ox + REGION / 2 + (int) Math.floorMod(seed >> 4, 41L) - 20;
        int cz = oz + REGION / 2 + (int) Math.floorMod(seed >> 14, 41L) - 20;
        final int w = 15;
        final int h = 5;
        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= w; dz++) {
                boolean edge = dx <= 0 || dz <= 0 || dx >= w || dz >= w;
                if (!edge) {
                    grow.put(cx + dx, ground, cz + dz, TILES);
                    continue;
                }
                boolean door = (dz == -1 || dz == w) ? (dx > 5 && dx < 9) : (dz > 5 && dz < 9);
                for (int y = ground + 1; y <= ground + h; y++) {
                    grow.put(cx + dx, y, cz + dz,
                            door && y <= ground + 3 ? AIR
                                    : (y == ground + h ? GRATE : WALL));
                }
            }
        }
        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= w; dz++) {
                grow.put(cx + dx, ground + h, cz + dz, GRATE);
            }
        }
        int mid = w / 2;
        grow.put(cx + mid, ground, cz + 1, BRICKS);
        grow.put(cx + mid, ground + 1, cz + 1, CRYSTAL);
        for (int y = ground + 1; y <= ground + 2; y++) {
            grow.put(cx + 1, y, cz + 1, LAMP);
            grow.put(cx + w - 2, y, cz + w - 2, LAMP);
        }
        int crateX = cx + w - 3;
        int crateZ = cz + 2;
        grow.put(crateX, ground + 1, crateZ, CRATE);
        grow.crate(crateX, ground + 1, crateZ, (int) mix(seed ^ 0x5ADL));
        if (Math.floorMod(seed >> 30, 3L) != 0) {
            grow.waiting(cx + 3, ground + 1, cz + w - 3, (int) (seed >> 40));
        }
        return new Region(key, grow);
    }

    /** A region's plan, being filled in. */
    private static final class Grow {
        private final int[] ops;
        private int opsCursor;
        private int[] crates = new int[0];
        private int cratesCursor;
        private int[] waiting = new int[0];
        private int waitingCursor;

        Grow(int capacity) {
            this.ops = new int[capacity * 4];
        }

        void put(int x, int y, int z, int state) {
            if (opsCursor + 4 > ops.length) {
                return;
            }
            ops[opsCursor++] = x;
            ops[opsCursor++] = y;
            ops[opsCursor++] = z;
            ops[opsCursor++] = state;
        }

        void crate(int x, int y, int z, int seed) {
            if (cratesCursor + 4 > crates.length) {
                int[] next = new int[Math.max(8, crates.length * 2)];
                System.arraycopy(crates, 0, next, 0, cratesCursor);
                crates = next;
            }
            crates[cratesCursor++] = x;
            crates[cratesCursor++] = y;
            crates[cratesCursor++] = z;
            crates[cratesCursor++] = seed;
        }

        void waiting(int x, int y, int z, int seed) {
            if (waitingCursor + 4 > waiting.length) {
                int[] next = new int[Math.max(8, waiting.length * 2)];
                System.arraycopy(waiting, 0, next, 0, waitingCursor);
                waiting = next;
            }
            waiting[waitingCursor++] = x;
            waiting[waitingCursor++] = y;
            waiting[waitingCursor++] = z;
            waiting[waitingCursor++] = seed;
        }
    }

    /** One planned region: the blocks to write, then the things that live in it. */
    private static final class Region {
        final long key;
        final int[] ops;
        final int[] crates;
        final int[] waiting;
        int cursor;

        Region(long key, Grow grow) {
            this.key = key;
            this.ops = java.util.Arrays.copyOf(grow.ops, grow.opsCursor);
            this.crates = java.util.Arrays.copyOf(grow.crates, grow.cratesCursor);
            this.waiting = java.util.Arrays.copyOf(grow.waiting, grow.waitingCursor);
        }
    }

    // -------------------------------------------------------------------------
    // Loot and the thing in the room
    // -------------------------------------------------------------------------

    private static final String[][] LOOT = {
        {"mcsm:rift_shard", "mcsm:void_thread", "mcsm:decayed_bone", "mcsm:decayed_steel_ingot"},
        {"mcsm:glitch_echo", "mcsm:memory_fragment", "mcsm:hallucination_dust", "mcsm:city_keycard"},
        {"mcsm:glyph_cell", "mcsm:storm_heart_shard", "minecraft:iron_ingot", "minecraft:gold_ingot"},
        {"minecraft:bread", "minecraft:torch", "minecraft:arrow", "minecraft:string"},
    };

    /** Fills a chamber crate, straight into its container. */
    private static void fillCrate(ServerLevel level, int x, int y, int z, int seed) {
        try {
            Object be = level.getBlockEntity(new BlockPos(x, y, z));
            if (!(be instanceof Container container)) {
                return;
            }
            int slots = Math.max(1, container.getContainerSize());
            int rolls = 2 + (int) Math.floorMod(seed >> 7, 4L);
            for (int i = 0; i < rolls; i++) {
                long h = mix(seed ^ ((long) i * 0x9E3779B9L));
                String[] pool = LOOT[Math.floorMod((int) (h >> 3), LOOT.length)];
                Item item = item(pool[Math.floorMod((int) (h >> 17), pool.length)]);
                if (item == null) {
                    continue;
                }
                int count = 1 + (int) Math.floorMod(h >> 29, 4L);
                container.setItem(Math.floorMod((int) (h >> 41), slots), new ItemStack(item, count));
            }
        } catch (Throwable ignored) {
            // an empty crate is still a crate
        }
    }

    /** Releases what a chamber was keeping. */
    private static void release(ServerLevel level, int x, int y, int z, int seed) {
        try {
            McsmCreatures.release(level, new BlockPos(x, y, z),
                    2 + Math.floorMod(seed, 2), 6.0D);
        } catch (Throwable ignored) {
        }
    }

    private static Item item(String id) {
        try {
            int colon = id.indexOf(':');
            if (colon <= 0) {
                return null;
            }
            return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(
                    id.substring(0, colon), id.substring(colon + 1)));
        } catch (Throwable t) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // The palette (resolved once; a missing id just means a skipped block)
    // -------------------------------------------------------------------------

    private static volatile BlockState[] palette;

    private static BlockState[] palette() {
        BlockState[] local = palette;
        if (local != null) {
            return local;
        }
        BlockState[] next = new BlockState[10];
        next[AIR] = Blocks.AIR.defaultBlockState();
        next[STONE] = state("mcsm:decayed_stone", McsmContent.DECAYED_STONE);
        next[BRICKS] = state("mcsm:decayed_stone_bricks", McsmContent.DECAYED_STONE_BRICKS);
        next[TILES] = state("mcsm:city_tiles", McsmContent.CITY_TILES);
        next[WALL] = state("mcsm:hollow_wall", McsmContent.HOLLOW_WALL);
        next[GRATE] = state("mcsm:rebar_grate", McsmContent.REBAR_GRATE);
        next[LAMP] = state("mcsm:glitch_lamp", McsmContent.GLITCH_LAMP);
        next[CRYSTAL] = state("mcsm:memory_crystal", McsmContent.MEMORY_CRYSTAL);
        next[FLESH] = state("mcsm:withered_flesh_block", McsmContent.WITHERED_FLESH_BLOCK);
        next[CRATE] = state("mcsm:supply_crate", McsmContent.SUPPLY_CRATE);
        palette = next;
        return next;
    }

    /**
     * A block state by id with a compile-time constant as the fallback -- the same
     * {@code design(path, fallback)} discipline the district generator uses, so a
     * renamed content block degrades to a plain one instead of leaving a hole.
     */
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

    // -------------------------------------------------------------------------
    // Determinism + reporting
    // -------------------------------------------------------------------------

    private static long regionKey(int rx, int rz) {
        return ((long) rx << 32) ^ (rz & 0xFFFFFFFFL);
    }

    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** Regions planned but not yet written -- the panel and /ds reality report it. */
    public static int pendingRegions() {
        return REGIONS.size();
    }

    /** Regions this session has finished writing. */
    public static int builtRegions() {
        return BUILT.size();
    }

    /** The surface the dimension's own layer stack produces: min_y plus the layers. */
    public static final int SURFACE_Y = 63;
}
