package net.mcsm.extras;

/**
 * BUILD #482 -- THE SPACETIME RIFTS: where the void is torn, and what is behind it.
 *
 * <p>THE BRIEF. "Program your procedural rift entities to spawn procedurally across
 * Layer 3 and Layer 4 ... the inner side of the spacetime rifts must execute a
 * screen-space refraction shader pass ... create moving, liquid-like ripples across
 * the portal face ... project a separate parallax layer displaying moving cosmic
 * particles, distant star arrays, and shifting silhouettes to simulate looking
 * straight into an alternative dimension", with "floating bubble strings and glowing
 * star-fracture dust clouds around the active rift boundaries."
 *
 * <p>WHAT THIS IS, AND WHY IT IS A FIELD RATHER THAN AN ENTITY. A rift that exists
 * only on the server would need a packet, an entity id and a spawn order before the
 * client could draw a single pixel of it -- and a fall crosses a tier in seconds, so
 * anything that has to be synchronised is something that can be late. A rift here is
 * a FUNCTION of position instead: the same lattice, the same hash, computed on both
 * sides, so the server could build what the client draws and the client draws what
 * the server built without a word passing between them. There is no entity to lose,
 * no packet to drop, and no drift: two machines, one answer.
 *
 * <p>WHERE THEY ARE. The lattice is {@link #SPACING} blocks across in x and z; a cell
 * has a rift when its own hash says so, and the rift's Y is drawn from the same hash
 * inside the two tiers the plan names ({@link McsmVoidTiers#TIER_FRACTURE} and
 * {@link McsmVoidTiers#TIER_GEL}). {@link #inside(double, double, double)} is true
 * when a fall is in the window of one of them -- that is the moment the frame turns
 * over into the window itself, and the moment the emitters wake up.
 */
public final class McsmVoidRifts {

    /** The rift lattice's own spacing, in blocks. */
    public static final int SPACING = 160;
    /** How wide a window is, and how tall: the rim a player falls through. */
    public static final double REACH = 34.0D;
    public static final double HALF_HEIGHT = 30.0D;
    /** How much of the lattice carries a rift, in percent. */
    public static final int DENSITY = 45;

    private McsmVoidRifts() {
    }

    /** Are rifts possible at this Y at all? Only the last two tiers tear. */
    public static boolean torn(double y) {
        return McsmVoidTiers.tierAt(y) >= McsmVoidTiers.TIER_FRACTURE;
    }

    /** The rift lattice cell a coordinate is in. */
    public static int cell(double v) {
        return Math.floorDiv((int) Math.floor(v), SPACING);
    }

    /** Where in its cell a rift sits, or null when the cell has none. */
    public static double[] centre(int cx, int cz, int tier) {
        // BUILD #483 -- the same salt, so a wrap rearranges the rifts too
        long h = mix(cx * 0x9E3779B97F4A7C15L ^ cz * 0xC2B2AE3D27D4EB4FL
                ^ McsmVoidLoop.salt() * 0x2545F4914F6CDD1DL);
        if (Math.floorMod(h, 100L) >= DENSITY) {
            return null;
        }
        double x = cx * (double) SPACING + Math.floorMod(h >> 8, (long) SPACING);
        double z = cz * (double) SPACING + Math.floorMod(h >> 20, (long) SPACING);
        int top = tier == McsmVoidTiers.TIER_GEL
                ? McsmVoidTiers.FRACTURE_FLOOR : McsmVoidTiers.ABYSS_FLOOR;
        int bottom = tier == McsmVoidTiers.TIER_GEL
                ? McsmVoidTiers.GEL_FLOOR : McsmVoidTiers.FRACTURE_FLOOR;
        double y = top - Math.floorMod(h >> 32, (long) Math.max(1, top - bottom));
        return new double[] { x, y, z, (Math.floorMod(h >> 44, 100L)) / 100.0D };
    }

    /** The rift whose window contains this point, or null. */
    public static double[] at(double x, double y, double z) {
        if (!torn(y)) {
            return null;
        }
        int tier = McsmVoidTiers.tierAt(y);
        int cx = cell(x);
        int cz = cell(z);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                double[] c = centre(cx + dx, cz + dz, tier);
                if (c == null) {
                    continue;
                }
                double dh = Math.hypot(x - c[0], z - c[2]);
                if (dh <= REACH && Math.abs(y - c[1]) <= HALF_HEIGHT) {
                    return c;
                }
            }
        }
        return null;
    }

    /** True inside a rift's window: the frame's cue to become the window. */
    public static boolean inside(double x, double y, double z) {
        return at(x, y, z) != null;
    }

    /** How deep into the window a point is: 0 at the rim, 1 at its heart. */
    public static float depth(double x, double y, double z) {
        double[] c = at(x, y, z);
        if (c == null) {
            return 0.0F;
        }
        double dh = Math.hypot(x - c[0], z - c[2]) / REACH;
        double dv = Math.abs(y - c[1]) / HALF_HEIGHT;
        return (float) Math.max(0.0D, Math.min(1.0D, 1.0D - Math.max(dh, dv)));
    }

    /** A rift's own phase, so two rifts do not ripple in step. */
    public static double phase(double x, double y, double z) {
        double[] c = at(x, y, z);
        return c == null ? 0.0D : c[3];
    }

    private static long mix(long v) {
        v ^= v >>> 33;
        v *= 0xFF51AFD7ED558CCDL;
        v ^= v >>> 33;
        v *= 0xC4CEB9FE1A85EC53L;
        return v ^ (v >>> 33);
    }
}
