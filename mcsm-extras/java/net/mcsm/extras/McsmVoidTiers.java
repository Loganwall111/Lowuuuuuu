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

    /** Safe bounds for 26.2 - fixed from -2032/4064 which caused Safe Mode loop */
    public static final int DIM_MIN_Y = -64;
    /** Safe height for 26.2 */
    public static final int DIM_HEIGHT = 384;
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
    // The bands, in this world's Y - scaled into safe -64/384 to fix suffocation glitch
    // Descent: ARRIVAL 130 -> DARK 118 -> GEL 78 -> SURFACE 56 -> tiers down to -64
    // ------------------------------------------------------------------

    /** Tier 0 runs from the surface down to here: nothing but space. */
    public static final int BASELINE_FLOOR = 20;
    /** Tier 1, the luminous cavern, ends here. */
    public static final int LUMINOUS_FLOOR = -5;
    /** Tier 2, the sponge of fractal tubes, ends here - faster start per user request. */
    public static final int SPONGE_FLOOR = -12;
    /** 15 more tethered sponge layers underneath - per user request */
    public static final int SPONGE_01_FLOOR = -14;
    public static final int SPONGE_02_FLOOR = -16;
    public static final int SPONGE_03_FLOOR = -18;
    public static final int SPONGE_04_FLOOR = -20;
    public static final int SPONGE_05_FLOOR = -22;
    public static final int SPONGE_06_FLOOR = -24;
    public static final int SPONGE_07_FLOOR = -26;
    public static final int SPONGE_08_FLOOR = -28;
    public static final int SPONGE_09_FLOOR = -30;
    public static final int SPONGE_10_FLOOR = -32;
    public static final int SPONGE_11_FLOOR = -34;
    public static final int SPONGE_12_FLOOR = -36;
    public static final int SPONGE_13_FLOOR = -38;
    public static final int SPONGE_14_FLOOR = -40;
    public static final int SPONGE_15_FLOOR = -42;
    /** Tier 3, the light-suppressed abyss, ends here. */
    public static final int ABYSS_FLOOR = -50;
    /** Tier 4, the fracturing reality, ends here. */
    public static final int FRACTURE_FLOOR = -58;
    /** Tier 5, the gel, runs from there to the floor of the world - now leaves room for 9th layer. */
    public static final int GEL_FLOOR = -60;
    /** Tier 6, the abyssal nightmare dimension (Layer 8 from infographic - final realm of terror). */
    public static final int ABYSSAL_NIGHTMARE_FLOOR = -62;
    /** Tier 7/9, the reality-glitch nightmare / uninpossible layer (Layer 9 - beyond impossible). */
    public static final int REALITY_GLITCH_NIGHTMARE_FLOOR = DIM_MIN_Y;
    /** Alias for the uninpossible layer - made-up word for beyond-impossible where reality breaks. */
    public static final int UNINPOSSIBLE_FLOOR = DIM_MIN_Y;
    /** The barrier layer the dimension's own generator lays at min_y. */
    public static final int FLOOR_Y = DIM_MIN_Y;

    // Deep sub-bedrock coordinate handshake: extended Overworld rendering loops handle ninth layer
    // when depth variables scale. Translation matrix so player plummets directly out of Tier 8 into Tier 9.
    public static final int REALITY_GLITCH_HANDSHAKE_Y = -1801; // plan-space Y where Tier 8 -> Tier 9 transition
    public static final int UNINPOSSIBLE_HANDSHAKE_Y = -2032; // absolute deepest

    public static final int TIER_BASELINE = 0;
    public static final int TIER_LUMINOUS = 1;
    public static final int TIER_SPONGE = 2;
    public static final int TIER_SPONGE_01 = 3;
    public static final int TIER_SPONGE_02 = 4;
    public static final int TIER_SPONGE_03 = 5;
    public static final int TIER_SPONGE_04 = 6;
    public static final int TIER_SPONGE_05 = 7;
    public static final int TIER_SPONGE_06 = 8;
    public static final int TIER_SPONGE_07 = 9;
    public static final int TIER_SPONGE_08 = 10;
    public static final int TIER_SPONGE_09 = 11;
    public static final int TIER_SPONGE_10 = 12;
    public static final int TIER_SPONGE_11 = 13;
    public static final int TIER_SPONGE_12 = 14;
    public static final int TIER_SPONGE_13 = 15;
    public static final int TIER_SPONGE_14 = 16;
    public static final int TIER_SPONGE_15 = 17;
    public static final int TIER_ABYSS = 18;
    public static final int TIER_FRACTURE = 19;
    public static final int TIER_GEL = 20;
    public static final int TIER_ABYSSAL_NIGHTMARE = 21;
    public static final int TIER_REALITY_GLITCH_NIGHTMARE = 22;
    public static final int TIER_UNINPOSSIBLE = 22;
    /** How many tiers there are - 6 + 15 sponge layers + 2 nightmare layers = 23. */
    public static final int TIERS = 23;

    /** What the chat calls each tier when a fall crosses into it. */
    public static final String[] NAME = {
            "the baseline",
            "the luminous cavern",
            "the sponge",
            "the sponge - depth 1",
            "the sponge - depth 2",
            "the sponge - depth 3",
            "the sponge - depth 4",
            "the sponge - depth 5",
            "the sponge - depth 6",
            "the sponge - depth 7",
            "the sponge - depth 8",
            "the sponge - depth 9",
            "the sponge - depth 10",
            "the sponge - depth 11",
            "the sponge - depth 12",
            "the sponge - depth 13",
            "the sponge - depth 14",
            "the sponge - depth 15",
            "the abyss",
            "the fracture",
            "the gel",
            "the abyssal nightmare dimension",
            "the reality-glitch nightmare / uninpossible layer" };

    /** What the plan called each tier, kept so the mapping is reviewable. */
    public static final String[] PLAN_NAME = {
            "Layer 0, the baseline void",
            "Layer 1, the luminous cavern",
            "Layer 0.5, the fractal sponge tube maze",
            "Layer 0.5.1, sponge tether 1 - faster",
            "Layer 0.5.2, sponge tether 2",
            "Layer 0.5.3, sponge tether 3",
            "Layer 0.5.4, sponge tether 4",
            "Layer 0.5.5, sponge tether 5",
            "Layer 0.5.6, sponge tether 6",
            "Layer 0.5.7, sponge tether 7",
            "Layer 0.5.8, sponge tether 8",
            "Layer 0.5.9, sponge tether 9",
            "Layer 0.5.10, sponge tether 10",
            "Layer 0.5.11, sponge tether 11",
            "Layer 0.5.12, sponge tether 12",
            "Layer 0.5.13, sponge tether 13",
            "Layer 0.5.14, sponge tether 14",
            "Layer 0.5.15, sponge tether 15",
            "Layer 2, the dark abyss",
            "Layer 3, the fragmented reality",
            "Layer 4, the gaseous gel horizon",
            "Layer 8, the abyssal nightmare dimension - final realm, terror chaos ancient evil reality screams",
            "Layer 9, the reality-glitch nightmare / decayed reality / uninpossible layer - beyond impossible where Minecraft reality completely breaks down" };

    /**
     * The tier's own ambient colour, as the plan gives it: the luminous cavern's
     * plum {@code #2E0B36}, the abyss's total black, the gel's greenish-brown
     * {@code #1C1F16}, and colours chosen for the two the plan leaves open (the
     * sponge is its own orange-to-pink gradient; the fracture has no ambient at
     * all, only the waves, so it borrows the plan's glitch violet).
     * 15 sponge layers gradient from orange to pink.
     * BUILD #485 -- Tier 9: matte-black & radiant shading filters - deep navy-black #0A0E14 and void-black #000000
     */
    public static final int[] FOG = {
            0x000000,   // 0 baseline: no filter
            0x2E0B36,   // 1 luminous cavern: plum-purple
            0xFF5A1E,   // 2 sponge: orange start
            0xFF6A2E,   // 3 sponge 1
            0xFF7A3E,   // 4 sponge 2
            0xFF8A4E,   // 5 sponge 3
            0xFF9A5E,   // 6 sponge 4
            0xFFAA6E,   // 7 sponge 5
            0xFFBA7E,   // 8 sponge 6
            0xFFCA8E,   // 9 sponge 7
            0xFFDA9E,   // 10 sponge 8
            0xFF8A9E,   // 11 sponge 9 - shifting to pink
            0xFF7AA8,   // 12 sponge 10
            0xFF6AB2,   // 13 sponge 11
            0xFF5ABC,   // 14 sponge 12
            0xFF4AC6,   // 15 sponge 13
            0xFF3FD0,   // 16 sponge 14
            0xFF3FA8,   // 17 sponge 15: pink end
            0x000000,   // 18 abyss: total void-black
            0x3A0F52,   // 19 fracture: glitch violet
            0x1C1F16,   // 20 gel: greenish-brown
            0x0A0E14,   // 21 abyssal nightmare: deep navy-black matte #0A0E14
            0x000000 }; // 22 reality-glitch nightmare / uninpossible: void-black #000000 with radiant aura

    /**
     * The colour each tier's own light is. The luminous cavern's spires are the
     * plan's cyan/emerald/amber; the abyss keeps only the ruins' neon; the gel
     * glows green-white through its own fluid.
     * BUILD #485 -- Tier 9: glowing purple lenses #8A2BE2 full-bright emissive RenderTypes.eyes, radiant aura
     */
    public static final int[] LIGHT = {
            0x6A5ACD, // 0 baseline
            0x38F0E0, // 1 luminous
            0xFFA23A, // 2 sponge
            0xFF8A3A, // 3
            0xFF7A4A, // 4
            0xFF6A5A, // 5
            0xFF5A6A, // 6
            0xFF4A7A, // 7
            0xFF3A8A, // 8
            0xFF2A9A, // 9
            0xFF1AAA, // 10
            0xFF0ABA, // 11
            0xFF00CA, // 12
            0xFF00B2, // 13
            0xFF00A0, // 14
            0xFF0090, // 15
            0xFF0080, // 16
            0xFF0070, // 17
            0x00FFB0, // 18 abyss
            0xE070FF, // 19 fracture
            0xBFFFC8, // 20 gel
            0x8A2BE2, // 21 abyssal nightmare: glowing purple lenses
            0x9D00FF }; // 22 reality-glitch nightmare: radiant purple emission aura + phase-shifting skirt 4.5x bloom

    /** How many spires / lights the tier raises, for the client art. */
    public static final int[] LIGHTS = { 0, 14, 10, 8,8,8,8,8,8,8,8,8,8,8,8,8,8,8, 4, 6, 8, 12, 24 };

    /**
     * The tier's own speed: the terminal fall, in blocks per second.
     * Sponge now faster at start per user request: 18.0 instead of 11.0, accelerating through 15 tethered layers.
     * BUILD #485 -- Tier 9: physics-defying 3D landscape, faster reality tear 45.0 and 50.0
     */
    public static final double[] SPEED = { 3.6D, 7.0D, 18.0D, 19.0D,20.0D,21.0D,22.0D,23.0D,24.0D,25.0D,26.0D,27.0D,28.0D,29.0D,30.0D,31.0D,32.0D,33.0D, 35.0D,38.0D,42.0D, 45.0D, 50.0D };

    /**
     * What the chat says the first time a fall crosses into the tier. Each one is
     * the tier's own description in the plan's words: the spires it raises, the
     * maze it is, the light it kills, the waves it runs, the gel it becomes.
     * BUILD #485 -- Tier 9: reality-glitch nightmare / uninpossible layer entries
     */
    public static final String[] ENTRY = {
            "\u00a78the world above is out of sight \u00b7 there is nothing here yet",
            "\u00a7d\u00a7lTHE LUMINOUS CAVERN \u00a78\u00b7 plum air, and spires of light "
            + "standing in it \u00b7 look up: that is the bedrock, thousands of blocks of it",
            "\u00a76\u00a7lTHE SPONGE \u00a78\u00b7 a maze of tubes grown through the gel "
            + "\u00b7 orange to pink, and none of it straight - now faster",
            "\u00a76THE SPONGE DEPTH 1 \u00a78\u00b7 tethered layer 1 \u00b7 tighter, faster, orange still",
            "\u00a76THE SPONGE DEPTH 2 \u00a78\u00b7 tethered layer 2 \u00b7 tubes narrowing",
            "\u00a76THE SPONGE DEPTH 3 \u00a78\u00b7 tethered layer 3 \u00b7 pink bleeding in",
            "\u00a76THE SPONGE DEPTH 4 \u00a78\u00b7 tethered layer 4 \u00b7 the maze winds harder",
            "\u00a76THE SPONGE DEPTH 5 \u00a78\u00b7 tethered layer 5 \u00b7 halfway through the tether",
            "\u00a76THE SPONGE DEPTH 6 \u00a78\u00b7 tethered layer 6 \u00b7 orange to pink shifting",
            "\u00a76THE SPONGE DEPTH 7 \u00a78\u00b7 tethered layer 7 \u00b7 you are deep in the sponge now",
            "\u00a76THE SPONGE DEPTH 8 \u00a78\u00b7 tethered layer 8 \u00b7 the tubes are almost solid",
            "\u00a76THE SPONGE DEPTH 9 \u00a78\u00b7 tethered layer 9 \u00b7 pink taking over",
            "\u00a76THE SPONGE DEPTH 10 \u00a78\u00b7 tethered layer 10 \u00b7 faster, tighter",
            "\u00a76THE SPONGE DEPTH 11 \u00a78\u00b7 tethered layer 11 \u00b7 almost through",
            "\u00a76THE SPONGE DEPTH 12 \u00a78\u00b7 tethered layer 12 \u00b7 pink maze",
            "\u00a76THE SPONGE DEPTH 13 \u00a78\u00b7 tethered layer 13 \u00b7 the sponge thins",
            "\u00a76THE SPONGE DEPTH 14 \u00a78\u00b7 tethered layer 14 \u00b7 light ahead",
            "\u00a76THE SPONGE DEPTH 15 \u00a78\u00b7 tethered layer 15 \u00b7 last sponge layer before abyss",
            "\u00a78\u00a7lTHE ABYSS \u00a78\u00b7 the light is gone \u00b7 only the ruins "
            + "down here still glow, and they are not lit for you",
            "\u00a75\u00a7lTHE FRACTURE \u00a78\u00b7 the view is rippling \u00b7 something "
            + "up there is running the world through a wave",
            "\u00a7a\u00a7lTHE GEL HORIZON \u00a78\u00b7 the void turns over into its own "
            + "fluid \u00b7 fall, drift, breathe: it will not drown you",
            "\u00a75\u00a7lTHE ABYSSAL NIGHTMARE DIMENSION \u00a78\u00b7 final realm \u00b7 terror chaos ancient evil reality screams \u00b7 "
            + "gothic towers hanging in matte-black void, jagged ridges on absolute horizon \u00b7 you should not be here",
            "\u00a7d\u00a7lTHE REALITY-GLITCH NIGHTMARE / UNINPOSSIBLE LAYER \u00a78\u00b7 beyond impossible where Minecraft reality completely breaks down \u00b7 "
            + "photorealistic 3D spires, hanging castles, colossal Creator visage tearing reality \u00b7 glowing purple lenses watching you \u00b7 4.5x bloom \u00b7 matte-black #0A0E14 #000000 \u00b7 the watcher stirs" };

    private McsmVoidTiers() {
    }

    /**
     * The scaling from the plan's depth (-64 .. -6001) to this world's
     * (-64 .. -2032). One constant to change if a future engine can go deeper.
     */
    public static double scale() {
        return (BASELINE_FLOOR - GEL_FLOOR) / (double) (SPEC_TOP - SPEC_FLOOR);
    }

    /** Deep sub-bedrock coordinate handshake: translation matrix for Tier 9 */
    public static double realityGlitchTranslation(double y) {
        // Maps plan-space -1801..-2032 into safe -60..-64 with seamless plummet from Tier 8
        if (y > GEL_FLOOR) return y;
        double planT = (y - REALITY_GLITCH_HANDSHAKE_Y) / (double)(UNINPOSSIBLE_HANDSHAKE_Y - REALITY_GLITCH_HANDSHAKE_Y);
        planT = Math.max(0.0D, Math.min(1.0D, planT));
        return GEL_FLOOR - planT * (GEL_FLOOR - REALITY_GLITCH_NIGHTMARE_FLOOR);
    }

    /** Whether Y is in the reality-glitch nightmare / uninpossible layer */
    public static boolean isRealityGlitchNightmare(double y) {
        return y <= ABYSSAL_NIGHTMARE_FLOOR;
    }

    /** Whether Y is in the abyssal nightmare dimension */
    public static boolean isAbyssalNightmare(double y) {
        return y <= GEL_FLOOR && y > ABYSSAL_NIGHTMARE_FLOOR;
    }

    /** Which tier a Y is in. Above the baseline's floor is the baseline itself. 23 tiers now with nightmare layers. */
    public static int tierAt(double y) {
        if (y > BASELINE_FLOOR) return TIER_BASELINE;
        if (y > LUMINOUS_FLOOR) return TIER_LUMINOUS;
        if (y > SPONGE_FLOOR) return TIER_SPONGE;
        if (y > SPONGE_01_FLOOR) return TIER_SPONGE_01;
        if (y > SPONGE_02_FLOOR) return TIER_SPONGE_02;
        if (y > SPONGE_03_FLOOR) return TIER_SPONGE_03;
        if (y > SPONGE_04_FLOOR) return TIER_SPONGE_04;
        if (y > SPONGE_05_FLOOR) return TIER_SPONGE_05;
        if (y > SPONGE_06_FLOOR) return TIER_SPONGE_06;
        if (y > SPONGE_07_FLOOR) return TIER_SPONGE_07;
        if (y > SPONGE_08_FLOOR) return TIER_SPONGE_08;
        if (y > SPONGE_09_FLOOR) return TIER_SPONGE_09;
        if (y > SPONGE_10_FLOOR) return TIER_SPONGE_10;
        if (y > SPONGE_11_FLOOR) return TIER_SPONGE_11;
        if (y > SPONGE_12_FLOOR) return TIER_SPONGE_12;
        if (y > SPONGE_13_FLOOR) return TIER_SPONGE_13;
        if (y > SPONGE_14_FLOOR) return TIER_SPONGE_14;
        if (y > SPONGE_15_FLOOR) return TIER_SPONGE_15;
        if (y > ABYSS_FLOOR) return TIER_ABYSS;
        if (y > FRACTURE_FLOOR) return TIER_FRACTURE;
        if (y > GEL_FLOOR) return TIER_GEL;
        if (y > ABYSSAL_NIGHTMARE_FLOOR) return TIER_ABYSSAL_NIGHTMARE;
        return TIER_REALITY_GLITCH_NIGHTMARE;
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
