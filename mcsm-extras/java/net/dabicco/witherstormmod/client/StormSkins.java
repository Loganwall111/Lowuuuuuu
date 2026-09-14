package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.resources.Identifier;

/**
 * Texture policy for every native storm model pass.
 *
 * Build #371 (default): phases 4-5.5 use the authentic OG traced-shading body
 * sheet ("dabywitherstormmod:textures/entity/wither_storm/wither_storm_traced.png"
 * — the original StageB mottle, bilinear-smoothed), ON by default. Phases 1-3.9
 * keep the clean solid pitch-black map; turning the traced option OFF restores
 * the regular plain-black body for 4-5.5.
 *
 * Phase 1 through Phase 5.5 strictly use the clean, solid pitch-black texture map:
 * "witherstormmod:textures/entity/wither_storm/wither_storm.png".
 * Phase 6 transitions to the smooth 4-quadrant dark navy/indigo Phase 6 atlas:
 * "dabywitherstormmod:textures/entity/phase_4_assets_p6.png" (HARD-LOCKED).
 * Native scrolling gloss sheen ("storm_gloss.png") provides Tattletale-style moving
 * shine via UV offsets only - no external shader dependencies.
 *
 * Build #365: the experimental native energy-swirl RenderType API (glossSwirlRenderType)
 * was removed; the swirl pass is rendered with the proven translucent pipeline only.
 */
public final class StormSkins {
    private static final Identifier CANONICAL_TEXTURE = Identifier.fromNamespaceAndPath(
            "witherstormmod", "textures/entity/wither_storm/wither_storm.png");
    private static final Identifier CANONICAL_ALT = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/entity/wither_storm/wither_storm.png");

    private static final Identifier LEGACY_CLASSIC = id("textures/entity/wither_storm.png");
    private static final Identifier LEGACY_OG = id("textures/entity/wither_storm_og.png");
    /** Build #371 — authentic OG traced-shading body sheet (16x16 StageB mottle). */
    private static final Identifier TRACED_P4_LEGACY = id("textures/entity/wither_storm/wither_storm_traced.png");
    /**
     * Build #374 — the "tray shaded" MCSM Blockbench blueprint, injected: the
     * blueprint's traced StageB body mottle tiled across the full 160x160 body
     * UV space (exactly how the blueprint's 16px repeat texture renders), with
     * the witherBloodA traced tile's luminance baked in as pre-baked shading
     * depth. This is the DEFAULT body look for phases 4-5.9.
     */
    private static final Identifier TRAY_SHADED_P4 = id("textures/entity/wither_storm/wither_storm_trayshaded.png");
    private static final Identifier TRACED_P4 = TRAY_SHADED_P4;
    private static final Identifier PHASE6_BODY = id("textures/entity/phase_4_assets_p6.png");
    private static final Identifier PHASE6_EMISSIVE = id("textures/entity/phase_4_assets_e.png");
    private static final Identifier PHASE6_DEVOURER = id("textures/entity/devourer_assets_p6.png");
    private static final Identifier NATIVE_GLOSS_TEXTURE = id("textures/misc/storm_gloss.png");

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

    /**
     * Build #371 traced-shading resolution for phases 4-5.9: the authentic
     * OG traced sheet when ON (default); the regular plain black when OFF.
     * Phases below 4.0 always stay the canonical deep-black sheet.
     */
    private static Identifier tracedOrCanonical(double phase) {
        if (phase >= 4.0D && McsmExtrasConfig.tracedShadingBody) {
            return TRACED_P4;
        }
        return CANONICAL_TEXTURE;
    }

    /** Phase 0-5.9 use traced (default) or plain black; Phase 6+ uses the quadrant atlas. */
    public static Identifier legacy() {
        return phaseHint >= 6.0D ? PHASE6_BODY : tracedOrCanonical(phaseHint);
    }

    /** Build #371: Phase 1-3.9 clean black, Phase 4-5.9 OG traced shading (default ON), Phase 6+ quadrant atlas. */
    public static Identifier body(double phase) {
        setPhaseHint(phase);
        return phase >= 6.0D ? PHASE6_BODY : tracedOrCanonical(phase);
    }

    /** Native scrolling gloss sheen texture for Tattletale-style live specular reflections. */
    public static Identifier glossTexture() {
        return NATIVE_GLOSS_TEXTURE;
    }

    /** Dynamic scrolling U-offset calculation for the gloss sheen. */
    public static float glossUOffset(long gameTime) {
        return (float) ((gameTime % 200L) / 200.0D);
    }

    /** Dynamic scrolling V-offset calculation for the gloss sheen. */
    public static float glossVOffset(long gameTime) {
        return (float) (((gameTime * 2) % 300L) / 300.0D);
    }

    /** Dedicated Phase 6 quadrant body atlas. */
    public static Identifier phase6Body() {
        return PHASE6_BODY;
    }

    /** Dedicated native-model eye/teeth emissive atlas. */
    public static Identifier phase6Emissive() {
        return PHASE6_EMISSIVE;
    }

    /** Build #371: Phase 4/5 callers receive the OG traced sheet (default ON) or plain black. */
    public static Identifier phase4() {
        return phaseHint >= 6.0D ? PHASE6_BODY : tracedOrCanonical(phaseHint);
    }

    /** Detached/devourer pieces use Phase 6 devourer sheet for Phase 6+, clean black otherwise. */
    public static Identifier devourer() {
        return phaseHint >= 6.0D ? PHASE6_DEVOURER : CANONICAL_TEXTURE;
    }

    /**
     * Phase-aware devourer variant: resolves from the entity's ACTUAL phase
     * instead of the shared hint (which other storms or off-frame ticks can
     * pollute). Used by the renderer's texture hook so the Phase 6 devourer
     * body can never render on a stale skin.
     */
    public static Identifier devourer(double phase) {
        setPhaseHint(phase);
        return phase >= 6.0D ? PHASE6_DEVOURER : CANONICAL_TEXTURE;
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
            // Build #374: the phase 4.0-4.9 face needs a BODY-space (160x160)
            // glow map — the old branch pointed at the 64x96 head-space map,
            // so the face region sampled wrong atlas quarters and rendered
            // flat/dark. The p4 map is the isolated bright face for that stage.
            path = ogSkin ? "textures/entity/wither_storm_og_p4_e.png" : "textures/entity/wither_storm_p4_e.png";
        } else {
            // 1.9.116: the early-phase face (eyes + teeth, UV 20,65 - 49,79 of
            // the canonical sheet) got a real isolated emissive map instead of
            // the blank 160x160 "no teeth glow" placeholder, so the Phase 1-3.9
            // face actively glows in the dark on its own full-bright channel.
            path = "textures/entity/wither_storm_legacy_e.png";
        }
        return id(path);
    }
}
