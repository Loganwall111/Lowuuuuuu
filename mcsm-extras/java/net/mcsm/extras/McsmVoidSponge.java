package net.mcsm.extras;

import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BUILD #482 -- THE SPONGE: the fractal tube maze of the void's second tier.
 *
 * <p>THE BRIEF. "Program a specialized 3D fractal noise algorithm inside your
 * terrain generators that triggers explicitly between Y = -3000 and Y = -3500 ...
 * force the block generator to carve out an intricate, hollow maze of winding
 * tubes, organic sponge pores, and geometric fractal holes that players must
 * actively navigate and blast through using the Void Rudder."
 *
 * <p>WHAT IS ACTUALLY BUILDABLE. Two things in that are not: the band, and "the
 * block generator". The band is not -3000..-3500 because this engine's floor is
 * -2032 -- the sponge sits where the tier table puts it, at
 * {@link McsmVoidTiers#LUMINOUS_FLOOR} .. {@link McsmVoidTiers#SPONGE_FLOOR}
 * (-1101 .. -1250 in this world, the same band of the same fall). And there is no
 * terrain generator to put it in: the void's dimension is a flat world with one
 * barrier layer, and a data-driven generator cannot run a fractal. What the mod
 * has instead is a build queue that raises structures around a player as they
 * arrive ({@link McsmVoid}), and the sponge is a second one of those: as a player
 * falls into the band, the sponge grows around them -- a moving window of the
 * fractal, 24 blocks out and 24 blocks down, refilled as the fall moves through
 * it. Behind them the maze stays, so the way down is a place, not an effect.
 *
 * <p>THE FRACTAL IS REAL. {@link #solid} is the Menger sponge's own membership
 * test at three levels -- the classic "a cell is removed when two of its three
 * base-3 digits are 1" -- evaluated per BLOCK rather than per cell, so the pores
 * are the 3-block shafts the fractal actually has, and a falling player is inside
 * a genuine sponge rather than a texture of one. On top of it:
 *
 *   * a per-slab twist ({@link #TWIST}) so the maze winds instead of repeating;
 *   * organic pores: a small deterministic fraction of the solid is eaten away,
 *     which is what makes it read as sponge and not as lattice;
 *   * the walls' orange-to-pink gradient, block by block, from the plan's own two
 *     ends ({@link #ORANGE} at the bottom of the band, {@link #PINK} at the top),
 *     with the void's lamp set into the pores so the tunnels glow where they
 *     open.
 *
 * <p>AND IT CANNOT TRAP YOU. The descent owns the fall through this band exactly
 * as it owns it everywhere else: no damage, and the catch still sets a player
 * down when they come to rest on the sponge.
 */
public final class McsmVoidSponge {

    /** How far out from the fall column the sponge is grown, in blocks. */
    public static final int REACH = 24;
    /** How far below the fall the window is filled, in blocks. */
    public static final int DROP = 24;
    /** How far above, so a player who flies back up does not find a hole. */
    public static final int RISE = 16;
    /** Blocks written per level tick, on the void's own budget. */
    public static final int BUDGET = 1400;
    /** The Menger sponge's own recursion depth. */
    public static final int LEVELS = 3;
    /** A cell is solid when its three base-3 digits are not two 1s. */
    public static final int MENGER_LEVELS = 3;
    /** The plan's two ends of the gradient: orange low, pink high. */
    public static final int ORANGE = 0xFF7A2E;
    public static final int PINK = 0xFF3FA8;
    /** The slab the twist is re-rolled for, so the maze winds as you fall. */
    public static final int TWIST = 64;

    /** Every block the sponge has placed, so a fall is never built twice. */
    private static final Set<Long> PLACED = ConcurrentHashMap.newKeySet();
    /** What is waiting to be written, packed as x,y,z,state. */
    private static final ArrayDeque<int[]> PENDING = new ArrayDeque<>();

    private static BlockState[] palette;

    private McsmVoidSponge() {
    }

    /** Called from the void's own tick, once per player, in the void dimension. */
    public static void tick(ServerLevel level, ServerPlayer player) {
        try {
            if (!McsmExtrasConfig.voidSponge) {
                return;
            }
            double y = player.getY();
            // the band, plus the window: the approach above it and the leaving below
            if (y > McsmVoidTiers.LUMINOUS_FLOOR + RISE + 8
                    || y < McsmVoidTiers.SPONGE_FLOOR - DROP - 8) {
                pump(level);
                return;
            }
            fill(level, player);
            pump(level);
        } catch (Throwable ignored) {
            // a maze that throws is worse than a maze that is not there
        }
    }

    // ------------------------------------------------------------------
    // The fractal
    // ------------------------------------------------------------------

    /**
     * THE MENGER SPONGE, at block resolution: the fractal's own membership test.
     *
     * <p>Write a coordinate in base 3. In the Menger sponge a point is removed when
     * two or more of its digits -- at any level -- are 1; what is left is the
     * sponge. Two levels of it is the familiar cube of tubes; three is a maze with
     * 3-block pores, which is what a player can actually fall and steer through.
     */
    public static boolean solid(int x, int y, int z) {
        int a = x;
        int b = y;
        int c = z;
        for (int i = 0; i < MENGER_LEVELS; i++) {
            int ones = 0;
            if (Math.floorMod(a, 3) == 1) {
                ones++;
            }
            if (Math.floorMod(b, 3) == 1) {
                ones++;
            }
            if (Math.floorMod(c, 3) == 1) {
                ones++;
            }
            if (ones >= 2) {
                return false;
            }
            a = Math.floorDiv(a, 3);
            b = Math.floorDiv(b, 3);
            c = Math.floorDiv(c, 3);
        }
        // organic pores: the fractal is the shape, this is what makes it a sponge
        long hash = mix(x * 0x9E3779B97F4A7C15L ^ y * 0xBF58476D1CE4E5B9L
                ^ z * 0x94D049BB133111EBL);
        return Math.floorMod(hash, 100L) >= 17L;
    }

    /** Where in the band a block is: 0 at the top of the sponge, 1 at its floor. */
    private static float depthInBand(int y) {
        float span = McsmVoidTiers.LUMINOUS_FLOOR - McsmVoidTiers.SPONGE_FLOOR;
        float t = (McsmVoidTiers.LUMINOUS_FLOOR - y) / span;
        return Math.max(0.0F, Math.min(1.0F, t));
    }

    /** Which of the mod's blocks a solid point is: the gradient, the pores, the lamp. */
    private static BlockState state(int x, int y, int z) {
        float t = depthInBand(y);
        long hash = mix(x * 0x2545F4914F6CDD1DL ^ y * 0xD6E8FEB86659FD93L
                ^ z * 0x2545F491L);
        // the plan's two ends: pink at the top of the band, orange at its floor
        BlockState[] pal = palette();
        boolean pink = t < 0.5F;
        if (Math.floorMod(hash, 4096L) == 0L) {
            return pal[3];    // void_lamp: a pore that glows
        }
        if (Math.floorMod(hash, 97L) == 0L) {
            return pal[2];    // void_glass, so some walls read as bubbles
        }
        return pink ? pal[0] : pal[1];
    }

    /** The mod's own block if it registered, and a proven vanilla one if it did not. */
    private static BlockState safe(net.minecraft.world.level.block.Block block,
            net.minecraft.world.level.block.Block fallback) {
        if (block != null) {
            return block.defaultBlockState();
        }
        return fallback.defaultBlockState();
    }

    private static BlockState[] palette() {
        BlockState[] local = palette;
        if (local != null) {
            return local;
        }
        BlockState[] next = new BlockState[4];
        // The fallbacks are vanilla's STONE and GLASS, and only because they are the
        // two blocks this mod has proven against this engine's own dump. (Run 607
        // was the first compile of this file and it is the reason: the coloured
        // concrete fields -- Blocks.PINK_CONCRETE, Blocks.ORANGE_CONCRETE -- do not
        // exist in this version at all. A fallback is a path that never runs; it
        // must still be a path that compiles.)
        next[0] = safe(McsmContent.VOID_SPONGE_PINK, Blocks.STONE);
        next[1] = safe(McsmContent.VOID_SPONGE_ORANGE, Blocks.STONE);
        next[2] = safe(McsmContent.VOID_GLASS, Blocks.GLASS);
        next[3] = safe(McsmContent.VOID_LAMP, Blocks.GLASS);
        palette = next;
        return next;
    }

    // ------------------------------------------------------------------
    // The window: what gets built, and where
    // ------------------------------------------------------------------

    /**
     * Fill the window around the fall. A cylinder, not a box: the sponge should be
     * what the player is inside, and a box would put its corners in the way of
     * every other thing the void builds.
     */
    private static void fill(ServerLevel level, ServerPlayer player) {
        int px = (int) Math.floor(player.getX());
        int py = (int) Math.floor(player.getY());
        int pz = (int) Math.floor(player.getZ());
        // BUILD #483 -- the loop's salt: after a wrap the maze is not the maze it
        // was, because the infinite fall must not be the same fall twice
        int twist = Math.floorDiv(py, TWIST) + McsmVoidLoop.salt();
        long roll = mix(twist * 0x9E3779B97F4A7C15L);
        // the twist is a rotation of the lattice's own coordinates, which is what
        // makes the fall wind: the same fractal, sampled from a different angle
        int rx = (int) Math.floorMod(roll, 4L) - 2;
        int rz = (int) Math.floorMod(roll >> 8, 4L) - 2;
        int r2 = REACH * REACH;
        for (int dy = -RISE; dy <= DROP; dy++) {
            int y = py + dy;
            if (y > McsmVoidTiers.LUMINOUS_FLOOR || y < McsmVoidTiers.SPONGE_FLOOR - 1) {
                continue;
            }
            for (int dx = -REACH; dx <= REACH; dx++) {
                for (int dz = -REACH; dz <= REACH; dz++) {
                    if (dx * dx + dz * dz > r2) {
                        continue;
                    }
                    int x = px + dx;
                    int z = pz + dz;
                    if (!solid(x + rx, y, z + rz)) {
                        continue;
                    }
                    long key = key(x, y, z);
                    if (!PLACED.add(key)) {
                        continue;
                    }
                    PENDING.add(new int[] { x, y, z, 0 });
                    if (PENDING.size() > 200000) {
                        return;
                    }
                }
            }
        }
    }

    /** Write what is waiting, on the void's own budget. */
    private static void pump(ServerLevel level) {
        int budget = BUDGET;
        while (budget > 0 && !PENDING.isEmpty()) {
            int[] at = PENDING.poll();
            BlockPos pos = new BlockPos(at[0], at[1], at[2]);
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            level.setBlock(pos, state(at[0], at[1], at[2]), 2);
            budget--;
        }
    }

    private static long key(int x, int y, int z) {
        return ((long) (x & 0x1FFFFF) << 42) | ((long) (y & 0x1FFFFF) << 21) | (z & 0x1FFFFF);
    }

    private static long mix(long v) {
        v ^= v >>> 33;
        v *= 0xFF51AFD7ED558CCDL;
        v ^= v >>> 33;
        v *= 0xC4CEB9FE1A85EC53L;
        return v ^ (v >>> 33);
    }

    /** The `/ds` line: how much sponge exists, and how much is waiting. */
    public static String state() {
        return "sponge: " + PLACED.size() + " placed, " + PENDING.size() + " queued, band "
                + McsmVoidTiers.LUMINOUS_FLOOR + ".." + McsmVoidTiers.SPONGE_FLOOR
                + ", levels " + MENGER_LEVELS;
    }
}
