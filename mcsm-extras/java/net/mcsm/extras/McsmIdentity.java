package net.mcsm.extras;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * BUILD #458 -- THE PER-DIMENSION IDENTITY PASS: every world is its own place.
 *
 * <p>THE ASK, in the player's own words: "each dimension and infinite
 * subdimension completely unique: own blocks, items, mobs, locks, VFX, biomes,
 * fog, sky, horizon; no re-use, not the decay set in flat worlds."
 *
 * <p>They were right, and the report was exact. Every world this mod generates
 * was built out of the SAME blocks: the void's floating shelves were decayed
 * stone and city tile, the infinite dimension's pillars were decayed stone
 * bricks, both wore glitch lamps and memory crystals, and both inherited the
 * decayed reality's horizon. Three dimensions, one palette -- which is exactly
 * what "the decay set in flat worlds" means.
 *
 * <p>So this class is the single table. One {@link Skin} per dimension, and a
 * skin carries everything a place has to be distinguishable by without any Java
 * knowing where it is being read from:
 *
 * <ul>
 *   <li><b>materialPrefix</b> -- the block family the world is MADE of
 *       ({@code mcsm:&lt;prefix&gt;_*}), so "the ground here is not the ground
 *       there" is a fact of the code, not a colour choice;</li>
 *   <li><b>fog / sky / skyLight / water</b> -- the exact hexes its biome and
 *       dimension type carry, in the mod's {@code RRGGBB} spelling;</li>
 *   <li><b>horizon</b> -- the colour the sky wears where it meets the ground,
 *       which is what {@link net.mcsm.extras.client.McsmSkyFloorBand} paints
 *       downward and what {@code ci/make_skybox_textures.py} paints the bottom
 *       row of each panorama in;</li>
 *   <li><b>glow</b> -- the accent only that world has (the rift's violet, the
 *       city's amber, the void's cold green);</li>
 *   <li><b>tintR/G/B + spin</b> -- how the painted cube is graded and how fast
 *       its panorama turns.</li>
 * </ul>
 *
 * <p>Nothing here is client-only and nothing is reflection: it is a table of
 * strings and floats plus one dimension comparison each, so the server (for
 * biomes and structures) and the client (for the sky and the horizon band) read
 * the same numbers. THE OVERWORLD DELIBERATELY HAS NO SKIN: it keeps the vanilla
 * sky, the vanilla ground and the storm's own backdrop, which is the one sky
 * this mod does not own -- {@link #forLevel} says so by returning {@code null}.
 *
 * <p>The hexes are not decoration and they are not free: {@code
 * ci/check_phase_uniform.py} reads this file and the three biome JSONs and fails
 * the build if a dimension's fog, sky, horizon or water stops matching what is
 * written here, and cross-checks the horizon hexes against the sky painter.
 */
public final class McsmIdentity {

    /** Dimension ids, as the rest of the mod spells them. */
    public static final String DECAYED = "decayed";
    public static final String ADAMS = "adams";
    public static final String VOID = "void";

    /**
     * One dimension's identity. Immutable, and every field is something a player
     * can see: the blocks under their feet, the air, the sky, the horizon, the
     * accent light, the water.
     */
    public record Skin(String id, String label, String materialPrefix,
                       String fog, String sky, String skyLight, String horizon,
                       String glow, String water,
                       float tintR, float tintG, float tintB, float spin) {

        /** The material family this world is built from, e.g. {@code mcsm:void_stone}. */
        public String material(String suffix) {
            return "mcsm:" + materialPrefix + "_" + suffix;
        }
    }

    /**
     * The three, in the order the portals, the panel and the sky painter list
     * them. The hexes are the canonical ones: fog and water come from the biome,
     * skyLight from the dimension type, horizon from the bottom row of that
     * dimension's own painted panorama.
     */
    private static final Skin[] SKINS = {
        new Skin(DECAYED, "The Decayed Reality", "decayed",
                "140A1E", "1B1026", "5A2A7A", "6E3E2A", "8A5CFF", "7A2AD6",
                1.02F, 0.94F, 1.10F, 0.0022F),
        new Skin(ADAMS, "The Infinite Dimension of Adams", "adams",
                "2A2340", "6E5AC8", "6E5AC8", "5C2E86", "FFC26E", "3E2E7A",
                1.06F, 0.96F, 1.04F, 0.0030F),
        new Skin(VOID, "The Void", "void",
                "1E1642", "4A2E8A", "4A2E8A", "1E1642", "7CFFB0", "8C24FF",
                0.96F, 0.92F, 1.08F, 0.0040F),
    };

    private McsmIdentity() {
    }

    /** Every skin, in the canonical order. */
    public static Skin[] all() {
        return SKINS.clone();
    }

    /**
     * The material family of a dimension, as block ids -- {@code mcsm:void_stone},
     * {@code mcsm:adams_bricks} and so on. The doorways are built by name through
     * here, so an arch cannot be made of a block from another world.
     */
    public static String material(String dimId, String suffix) {
        Skin s = skin(dimId);
        return s == null ? null : s.material(suffix);
    }

    /** One skin by id ({@link #DECAYED} / {@link #ADAMS} / {@link #VOID}), or null. */
    public static Skin skin(String id) {
        if (id == null) {
            return null;
        }
        for (Skin s : SKINS) {
            if (s.id().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * The identity of the world the given level is, or {@code null} for anything
     * that is not ours -- the Overworld, the Nether, the End, and every dimension
     * another mod ships all keep exactly what they had.
     */
    public static Skin forLevel(Level level) {
        try {
            if (level == null) {
                return null;
            }
            ResourceKey<Level> key = level.dimension();
            if (key == null) {
                return null;
            }
            if (key.equals(McsmReality.DECAYED_REALITY)) {
                return skin(DECAYED);
            }
            if (key.equals(McsmAdams.ADAMS)) {
                return skin(ADAMS);
            }
            if (key.equals(McsmVoid.DIMENSION)) {
                return skin(VOID);
            }
        } catch (Throwable ignored) {
            // a dimension we cannot name is a dimension we do not own
        }
        return null;
    }

    /** {@code "RRGGBB"} -> three floats in 0..1, or null when it is not a colour. */
    public static float[] rgb(String hex) {
        try {
            if (hex == null || hex.length() != 6) {
                return null;
            }
            int v = Integer.parseInt(hex, 16);
            return new float[]{
                ((v >> 16) & 0xFF) / 255.0F,
                ((v >> 8) & 0xFF) / 255.0F,
                (v & 0xFF) / 255.0F,
            };
        } catch (Throwable t) {
            return null;
        }
    }

    /** A skin's horizon colour as floats, or null. */
    public static float[] horizon(Skin skin) {
        return skin == null ? null : rgb(skin.horizon());
    }

    /** Log line for the boot, so a build's identity is checkable from the log. */
    public static String summary() {
        StringBuilder sb = new StringBuilder("[ds] dimension identities:");
        for (Skin s : SKINS) {
            sb.append(' ').append(s.id()).append('=').append(s.materialPrefix())
              .append("(fog #").append(s.fog()).append(", horizon #").append(s.horizon())
              .append(')');
        }
        return sb.toString();
    }
}
