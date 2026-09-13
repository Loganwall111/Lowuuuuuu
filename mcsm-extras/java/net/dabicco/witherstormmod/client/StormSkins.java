package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Texture policy for every native storm model pass.
 *
 * Phase 1 through Phase 5 strictly use the clean, solid pitch-black texture map:
 * "witherstormmod:textures/entity/wither_storm/wither_storm.png".
 * Phase 6 transitions to the smooth 4-quadrant dark navy/indigo Phase 6 atlas:
 * "dabywitherstormmod:textures/entity/phase_4_assets_p6.png".
 * Native translucent gloss sheen overlay ("gloss_layer.png") provides moving light reflections
 * natively using standard translucent blend modes without external shader dependencies.
 */
public final class StormSkins {
    private static final Identifier CANONICAL_TEXTURE = Identifier.fromNamespaceAndPath(
            "witherstormmod", "textures/entity/wither_storm/wither_storm.png");
    private static final Identifier CANONICAL_ALT = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/entity/wither_storm/wither_storm.png");

    private static final Identifier LEGACY_CLASSIC = id("textures/entity/wither_storm.png");
    private static final Identifier LEGACY_OG = id("textures/entity/wither_storm_og.png");
    private static final Identifier PHASE6_BODY = id("textures/entity/phase_4_assets_p6.png");
    private static final Identifier PHASE6_EMISSIVE = id("textures/entity/phase_4_assets_e.png");
    private static final Identifier PHASE6_DEVOURER = id("textures/entity/devourer_assets_p6.png");
    private static final Identifier GLOSS_LAYER_TEXTURE = id("textures/entity/wither_storm/gloss_layer.png");

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

    /** Phase 0-5 use clean solid black; Phase 6+ uses the Phase 6 quadrant atlas. */
    public static Identifier legacy() {
        return phaseHint >= 6.0D ? PHASE6_BODY : CANONICAL_TEXTURE;
    }

    /** Select clean solid black for Phase 1-5; transition to quadrant atlas for Phase 6+. */
    public static Identifier body(double phase) {
        setPhaseHint(phase);
        return phase >= 6.0D ? PHASE6_BODY : CANONICAL_TEXTURE;
    }

    /** Native translucent gloss layer texture for animated specular sheen reflections. */
    public static Identifier glossTexture() {
        return GLOSS_LAYER_TEXTURE;
    }

    /** Animated sliding U-offset calculated natively from system tick time. */
    public static float glossUOffset() {
        long time = System.currentTimeMillis();
        return (float) ((time % 4000L) / 4000.0D);
    }

    /** Animated sliding V-offset calculated natively from system tick time. */
    public static float glossVOffset() {
        long time = System.currentTimeMillis();
        return (float) (((time * 2) % 6000L) / 6000.0D);
    }

    /** Standard translucent RenderType for native moving gloss sheen overlay pass. */
    public static RenderType glossRenderType() {
        return GlowRenderTypes.translucent(GLOSS_LAYER_TEXTURE);
    }

    /** Dedicated Phase 6 quadrant body atlas. */
    public static Identifier phase6Body() {
        return PHASE6_BODY;
    }

    /** Dedicated native-model eye/teeth emissive atlas. */
    public static Identifier phase6Emissive() {
        return PHASE6_EMISSIVE;
    }

    /** Phase 4/5 callers receive clean solid black. */
    public static Identifier phase4() {
        return phaseHint >= 6.0D ? PHASE6_BODY : CANONICAL_TEXTURE;
    }

    /** Detached/devourer pieces use Phase 6 devourer sheet for Phase 6+, clean black otherwise. */
    public static Identifier devourer() {
        return phaseHint >= 6.0D ? PHASE6_DEVOURER : CANONICAL_TEXTURE;
    }

    /** Isolated emissive teeth and eye facial glow maps. */
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
