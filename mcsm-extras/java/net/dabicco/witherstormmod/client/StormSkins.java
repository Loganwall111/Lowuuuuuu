package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.resources.Identifier;

/**
 * Storm body + teeth emissive atlas selection.
 * Hard-locked Wither Storm HEADS, jawbones, and neck segments (Phases 2 through 8+)
 * exclusively onto the canonical dark charcoal-void texture map:
 * "witherstormmod:textures/entity/wither_storm/wither_storm.png".
 */
public final class StormSkins {
    private static final Identifier CANONICAL_TEXTURE = Identifier.fromNamespaceAndPath(
            "witherstormmod", "textures/entity/wither_storm/wither_storm.png");
    private static final Identifier CANONICAL_ALT = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/entity/wither_storm/wither_storm.png");

    private static volatile double phaseHint = 0.0D;

    private StormSkins() {
    }

    public static void setPhaseHint(double phase) {
        phaseHint = phase;
    }

    public static boolean og() {
        return Math.round(DabyWSClientConfig.stormSkin) >= 1L;
    }

    public static Identifier canonical() {
        return CANONICAL_TEXTURE;
    }

    // #413: mirror the BASE mod's own identifiers exactly. Returning the
    // wither-storm map for the devourer / phase-4 models was the brown-UV
    // potato: wrong atlas per model group.
    public static Identifier legacy() {
        return Identifier.fromNamespaceAndPath(
                "dabywitherstormmod", "textures/entity/wither_storm_og.png");
    }

    public static Identifier phase4() {
        return Identifier.fromNamespaceAndPath(
                "dabywitherstormmod", "textures/entity/phase_4_assets.png");
    }

    public static Identifier devourer() {
        return Identifier.fromNamespaceAndPath(
                "dabywitherstormmod", "textures/entity/devourer_assets.png");
    }

    public static Identifier teethGlow(double phase) {
        setPhaseHint(phase);
        boolean ogSkin = DabyWSClientConfig.stormSkin >= 0.5;
        String path;
        if (phase >= 7.0) {
            path = ogSkin ? "textures/entity/wither_storm_og_p7_e.png" : "textures/entity/wither_storm_p7_e.png";
        } else if (phase >= 6.0) {
            path = ogSkin ? "textures/entity/wither_storm_og_p6_e.png" : "textures/entity/wither_storm_p6_e.png";
        } else if (phase >= 5.5) {
            path = ogSkin ? "textures/entity/wither_storm_og_p55_e.png" : "textures/entity/wither_storm_p55_e.png";
        } else if (phase >= 5.1) {
            path = ogSkin ? "textures/entity/wither_storm_og_p51_e.png" : "textures/entity/wither_storm_p51_e.png";
        } else if (phase >= 5.0) {
            path = ogSkin ? "textures/entity/wither_storm_og_p5_e.png" : "textures/entity/wither_storm_p5_e.png";
        } else if (phase >= 4.0) {
            path = ogSkin ? "textures/entity/wither_storm_og_e.png" : "textures/entity/wither_storm_e.png";
        } else {
            path = "textures/entity/wither_storm_no_teeth_glow_e.png";
        }
        return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
    }
}
