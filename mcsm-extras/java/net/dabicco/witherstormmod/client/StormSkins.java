package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.resources.Identifier;

/**
 * Texture policy for every native storm model pass.
 *
 * All storm body, head, jaw, skull, neck, and tentacle model passes use the
 * smooth, solid pitch-black canonical dark texture map:
 * "witherstormmod:textures/entity/wither_storm/wither_storm.png"
 * eliminating all visible pixel grain and static noise artifacts.
 */
public final class StormSkins {
    private static final Identifier CANONICAL_TEXTURE = Identifier.fromNamespaceAndPath(
            "witherstormmod", "textures/entity/wither_storm/wither_storm.png");
    private static final Identifier CANONICAL_ALT = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/entity/wither_storm/wither_storm.png");

    private static final Identifier PHASE6_EMISSIVE = id("textures/entity/phase_4_assets_e.png");

    private static volatile double phaseHint;

    private StormSkins() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
    }

    public static void setPhaseHint(double phase) {
        phaseHint = phase;
    }

    public static double phaseHint() {
        return phaseHint;
    }

    public static boolean og() {
        return Math.round(DabyWSClientConfig.stormSkin) >= 1L;
    }

    public static Identifier canonical() {
        return CANONICAL_TEXTURE;
    }

    public static Identifier legacy() {
        return CANONICAL_TEXTURE;
    }

    public static Identifier body(double phase) {
        setPhaseHint(phase);
        return CANONICAL_TEXTURE;
    }

    public static Identifier phase6Body() {
        return CANONICAL_TEXTURE;
    }

    public static Identifier phase6Emissive() {
        return PHASE6_EMISSIVE;
    }

    public static Identifier phase4() {
        return CANONICAL_TEXTURE;
    }

    public static Identifier devourer() {
        return CANONICAL_TEXTURE;
    }

    public static Identifier teethGlow(double phase) {
        setPhaseHint(phase);
        boolean ogSkin = DabyWSClientConfig.stormSkin >= 0.5;
        String path;
        if (phase >= 7.0D) {
            path = ogSkin ? "textures/entity/wither_storm_og_p7_e.png" : "textures/entity/wither_storm_p7_e.png";
        } else if (phase >= 6.0D) {
            path = ogSkin ? "textures/entity/wither_storm_og_p6_e.png" : "textures/entity/wither_storm_p6_e.png";
        } else if (phase >= 5.5D) {
            path = ogSkin ? "textures/entity/wither_storm_og_p55_e.png" : "textures/entity/wither_storm_p55_e.png";
        } else if (phase >= 5.1D) {
            path = ogSkin ? "textures/entity/wither_storm_og_p51_e.png" : "textures/entity/wither_storm_p51_e.png";
        } else if (phase >= 5.0D) {
            path = ogSkin ? "textures/entity/wither_storm_og_p5_e.png" : "textures/entity/wither_storm_p5_e.png";
        } else if (phase >= 4.0D) {
            path = ogSkin ? "textures/entity/wither_storm_og_e.png" : "textures/entity/wither_storm_e.png";
        } else {
            path = "textures/entity/wither_storm_no_teeth_glow_e.png";
        }
        return id(path);
    }
}
