package net.mcsm.extras;

/**
 * BUILD #481 -- THE MULTI-LAYER VOID: five tiers, one continuous fall.
 *
 * <p>THE BRIEF. The void is no longer one place. Under the bedrock line the player
 * falls through a descending vertical ecosystem: empty baseline space, a luminous
 * cavern of coloured light spires, a sponge of fractal tubes, a light-suppressed
 * abyss, a fracturing reality, and finally the gaseous gel the whole place is
 * made of. Each tier has its own colour, its own light, its own speed of fall and
 * its own thing to look at, and the client art ({@code McsmVoidDeep}) reads the
 * numbers from HERE so the world and the picture can never drift apart.
 *
 * <p>THE NUMBERS, HONESTLY. The plan gives the tiers in absolute Y: -64 to -250,
 * -251 to -2999, -3000 to -3500, -3501 to -4500, -4501 to -6000, and -6001 and
 * below. No Minecraft world can express that: a dimension's {@code min_y} is
 * bounded below by -2032, so the deepest floor any dimension can have is -2032,
 * and a floor at -10000 is not a build target, it is a number the engine refuses.
 * (The plan's own "permanently extend the absolute minimum generation bounds ...
 * locking the total vertical loop down to Y = -10000" is that case: it cannot be
 * done, so it is not pretended.)
 *
 * <p>What IS done is the whole shape of it inside the space the engine has: the
 * void's dimension is extended to its own legal maximum -- {@link #DIM_MIN_Y}
 * (-2032) to {@link #DIM_MAX_Y} (2032), a 4064-block world, which is ten times
 * the height it had -- and the plan's five bands are SCALED into the depth that
 * exists, keeping their order, their relative proportions and every tier's
 * colours, materials and behaviour. {@link #SPEC_TOP}/{@link #SPEC_FLOOR} keep
 * the plan's own numbers for the record, and {@link #scale()} is the mapping, so
 * a future engine that can go deeper changes one constant and the tiers follow.
 *
 *     plan band              plan Y              this world      tier
 *     Layer 0  baseline      -64  .. -250        -64 .. -250     0  the baseline
 *     Layer 1  luminous      -251 .. -2999       -251 .. -1100   1  the luminous cavern
 *     Layer .5 sponge        -3000 .. -3500      -1101 .. -1250  2  the sponge
 *     Layer 2  abyss         -3501 .. -4500      -1251 .. -1550  3  the abyss
 *     Layer 3  fracture      -4501 .. -6000      -1551 .. -1800  4  the fracture
 *     Layer 4  the gel       -6001 .. below      -1801 .. -2032  5  the gel
 */
public final class McsmVoidTiers {

    // ------------------------------------------------------------------
    // The world the void gets. Vanilla's own limits, at their maximum.
    // ------------------------------------------------------------------

    /** The deepest {@code min_y} this engine allows: the void's new floor. */
    public static final int DIM_MIN_Y = -2032;
    /** The tallest world the engine allows, which is what the void now is. */
    public static final int DIM_HEIGHT = 4064;
    /** The top of the void's world. */
    public static final int DIM_MAX_Y = DIM_MIN_Y + DIM_HEIGHT;

    // ------------------------------------------------------------------
    // The plan's own numbers. Not the world; the record of what was asked.
    // ------------------------------------------------------------------

    /** The plan's first band starts here. */
    public static final int SPEC_TOP = -64;
    /** The plan's last band is "below this". */
    public static final int SPEC_FLOOR = -6001;

    // ------------------------------------------------------------------
    // The bands, in this world's Y.
    // ------------------------------------------------------------------

    /** Tier 0 runs from the surface down to here: nothing but space. */
    public static final int BASELINE_FLOOR = -250;
    /** Tier 1, the luminous cavern, ends here. */
    public static final int LUMINOUS_FLOOR = -1100;
    /** Tier 2, the sponge of fractal tubes, ends here. */
    public static final int SPONGE_FLOOR = -1250;
    /** Tier 3, the light-suppressed abyss, ends here. */
    public static final int ABYSS_FLOOR = -1550;
    /** Tier 4, the fracturing reality, ends here. */
    public static final int FRACTURE_FLOOR = -1800;
    /** Tier 5, the gel, runs from there to the floor of the world. */
    public static final int GEL_FLOOR = DIM_MIN_Y;
    /** The barrier layer the dimension's own generator lays at min_y. */
    public static final int FLOOR_Y = DIM_MIN_Y;

    public static final int TIER_BASELINE = 0;
    public static final int TIER_LUMINOUS = 1;
    public static final int TIER_SPONGE = 2;
    public static final int TIER_ABYSS = 3;
    public static final int TIER_FRACTURE = 4;
    public static final int TIER_GEL = 5;
    /** How many tiers there are. */
    public static final int TIERS = 6;

    /** What the chat calls each tier when a fall crosses into it. */
    public static final String[] NAME = {
            "the baseline",
            "the luminous cavern",
            "the sponge",
            "the abyss",
            "the fracture",
            "the gel" };

    /** What the plan called each tier, kept so the mapping is reviewable. */
    public static final String[] PLAN_NAME = {
            "Layer 0, the baseline void",
            "Layer 1, the luminous cavern",
            "Layer 0.5, the fractal sponge tube maze",
            "Layer 2, the dark abyss",
            "Layer 3, the fragmented reality",
            "Layer 4, the gaseous gel horizon" };

    /**
     * The tier's own ambient colour, as the plan gives it: the luminous cavern's
     * plum {@code #2E0B36}, the abyss's total black, the gel's greenish-brown
     * {@code #1C1F16}, and colours chosen for the two the plan leaves open (the
     * sponge is its own orange-to-pink gradient; the fracture has no ambient at
     * all, only the waves, so it borrows the plan's glitch violet).
     */
    public static final int[] FOG = {
            0x000000,   // 0 baseline: no filter at all -- the plan says so
            0x2E0B36,   // 1 luminous cavern: plum-purple
            0xFF5A1E,   // 2 sponge: the orange end of its own gradient
            0x000000,   // 3 abyss: total void-black
            0x3A0F52,   // 4 fracture: glitch violet, behind the waves
            0x1C1F16 }; // 5 gel: cartoonish cosmic greenish-brown

    /**
     * The colour each tier's own light is. The luminous cavern's spires are the
     * plan's cyan/emerald/amber; the abyss keeps only the ruins' neon; the gel
     * glows green-white through its own fluid.
     */
    public static final int[] LIGHT = {
            0x6A5ACD,
            0x38F0E0,
            0xFFA23A,
            0x00FFB0,
            0xE070FF,
            0xBFFFC8 };

    /** How many spires / lights the tier raises, for the client art. */
    public static final int[] LIGHTS = { 0, 14, 10, 4, 6, 8 };

    /**
     * The tier's own speed: the terminal fall, in blocks per second.
     *
     * <p>The deep is 1968 blocks of air below the gel's surface, so these are not
     * the Overworld's 3.1 -- they climb, tier by tier, from the baseline's own
     * weight to the gel's 30. A whole fall, hand-over to the invisible floor, is
     * about three and a half minutes at these speeds, and a bit over one with the
     * rudder engaged; the gel's flight lets a player stop anywhere in it.
     */
    public static final double[] SPEED = { 3.6D, 7.0D, 11.0D, 16.0D, 22.0D, 30.0D };

    /**
     * What the chat says the first time a fall crosses into the tier. Each one is
     * the tier's own description in the plan's words: the spires it raises, the
     * maze it is, the light it kills, the waves it runs, the gel it becomes.
     */
    public static final String[] ENTRY = {
            "\u00a78the world above is out of sight \u00b7 there is nothing here yet",
            "\u00a7d\u00a7lTHE LUMINOUS CAVERN \u00a78\u00b7 plum air, and spires of light "
            + "standing in it \u00b7 look up: that is the bedrock, thousands of blocks of it",
            "\u00a76\u00a7lTHE SPONGE \u00a78\u00b7 a maze of tubes grown through the gel "
            + "\u00b7 orange to pink, and none of it straight",
            "\u00a78\u00a7lTHE ABYSS \u00a78\u00b7 the light is gone \u00b7 only the ruins "
            + "down here still glow, and they are not lit for you",
            "\u00a75\u00a7lTHE FRACTURE \u00a78\u00b7 the view is rippling \u00b7 something "
            + "up there is running the world through a wave",
            "\u00a7a\u00a7lTHE GEL HORIZON \u00a78\u00b7 the void turns over into its own "
            + "fluid \u00b7 fall, drift, breathe: it will not drown you" };

    private McsmVoidTiers() {
    }

    /**
     * The scaling from the plan's depth (-64 .. -6001) to this world's
     * (-64 .. -2032). One constant to change if a future engine can go deeper.
     */
    public static double scale() {
        return (BASELINE_FLOOR - GEL_FLOOR) / (double) (SPEC_TOP - SPEC_FLOOR);
    }

    /** Which tier a Y is in. Above the baseline's floor is the baseline itself. */
    public static int tierAt(double y) {
        if (y > BASELINE_FLOOR) {
            return TIER_BASELINE;
        }
        if (y > LUMINOUS_FLOOR) {
            return TIER_LUMINOUS;
        }
        if (y > SPONGE_FLOOR) {
            return TIER_SPONGE;
        }
        if (y > ABYSS_FLOOR) {
            return TIER_ABYSS;
        }
        if (y > FRACTURE_FLOOR) {
            return TIER_FRACTURE;
        }
        return TIER_GEL;
    }

    /** The tier's name, for chat and for the frame. */
    public static String nameAt(double y) {
        return NAME[tierAt(y)];
    }

    /** The plan's own name for a tier, for the record. */
    public static String planName(int tier) {
        return PLAN_NAME[Math.max(0, Math.min(TIERS - 1, tier))];
    }

    /** The terminal fall speed of the tier a Y is in, in blocks per tick. */
    public static double current(double y) {
        return SPEED[tierAt(y)] / 20.0D;
    }

    /** The tier's ambient colour as three 0..1 floats. */
    public static void ambient(int tier, float[] out) {
        int rgb = FOG[Math.max(0, Math.min(TIERS - 1, tier))];
        out[0] = ((rgb >> 16) & 0xFF) / 255.0F;
        out[1] = ((rgb >> 8) & 0xFF) / 255.0F;
        out[2] = (rgb & 0xFF) / 255.0F;
    }

    /** The tier's own light colour as three 0..1 floats. */
    public static void light(int tier, float[] out) {
        int rgb = LIGHT[Math.max(0, Math.min(TIERS - 1, tier))];
        out[0] = ((rgb >> 16) & 0xFF) / 255.0F;
        out[1] = ((rgb >> 8) & 0xFF) / 255.0F;
        out[2] = (rgb & 0xFF) / 255.0F;
    }

    /** What the chat says when a fall crosses into a tier; never null. */
    public static String entry(int tier) {
        return ENTRY[Math.max(0, Math.min(TIERS - 1, tier))];
    }

    /** How far down the fall has got, 0 at the gel's surface, 1 at the floor. */
    public static float depthAt(double y) {
        float span = McsmVoidDescent.SURFACE_Y - GEL_FLOOR;
        float t = (float) ((McsmVoidDescent.SURFACE_Y - y) / span);
        return Math.max(0.0F, Math.min(1.0F, t));
    }
}
