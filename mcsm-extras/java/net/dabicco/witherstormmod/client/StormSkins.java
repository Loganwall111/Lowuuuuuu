package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.resources.Identifier;

/**
 * Storm body + teeth emissive atlas selection.
 * Textures must stay UV-safe: AI/image-generated art is useful as reference,
 * but the live atlases are constrained rewrites so the model's teeth, eyes,
 * command block, and body islands do not slide out of place.
 */
public final class StormSkins {
    private static final Identifier LEGACY_CLASSIC = id("textures/entity/wither_storm.png");
    private static final Identifier LEGACY_OG = id("textures/entity/wither_storm_og.png");
    private static final Identifier PHASE4_CLASSIC = id("textures/entity/phase_4_assets.png");
    private static final Identifier PHASE4_OG = id("textures/entity/phase_4_assets_og.png");
    private static final Identifier DEVOURER_CLASSIC = id("textures/entity/devourer_assets.png");
    private static final Identifier DEVOURER_OG = id("textures/entity/devourer_assets_og.png");

    private StormSkins() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
    }

    /**
     * Retained for binary/source compatibility with the head render-state
     * hook. Body UVs are intentionally phase-independent now; atmospheric
     * colour and teeth emissives carry the phase transition instead.
     */
    public static void setPhaseHint(double phase) {
        // No-op: never select an optional phase atlas at runtime.
    }

    public static boolean og() {
        return Math.round(DabyWSClientConfig.stormSkin) >= 1L;
    }

    public static Identifier legacy() {
        return og() ? LEGACY_OG : LEGACY_CLASSIC;
    }

    /** Dark chassis atlas used by split Stage B shell builders. */
    public static Identifier darkVanillaBlack() {
        return phase4();
    }

    public static Identifier phase4() {
        // The phase-specific P55/P6/P7 atlas files are optional presentation
        // variants. The model itself must never select one as its required
        // body sheet: an absent optional overlay becomes Minecraft's magenta
        // missing-texture checkerboard. The verified dark vanilla-black atlas
        // is the single stable body source for every phase.
        return og() ? PHASE4_OG : PHASE4_CLASSIC;
    }

    public static Identifier devourer() {
        // Keep the large devourer chassis on its verified base atlas as well;
        // phase-specific colour belongs to the sky/emissive pass, not geometry.
        return og() ? DEVOURER_OG : DEVOURER_CLASSIC;
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
