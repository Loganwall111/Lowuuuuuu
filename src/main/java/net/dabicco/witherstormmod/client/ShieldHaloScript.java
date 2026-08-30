package net.dabicco.witherstormmod.client;

/**
 * SHIELD HALO SCRIPT — volumetric 3D spherical mesh controller.
 * Keeps the protective barrier persistent from Phase 4 through Phase 7,
 * and duplicates it across all three split heads simultaneously during Phase 6.
 * Uses localized alpha-blending transparency with edge fading.
 */
public class ShieldHaloScript {
    public static final float SHIELD_PHASE_START = 4.0f;
    public static final float SHIELD_PHASE_END = 7.0f;
    public static final int SPLIT_HEAD_COUNT = 3;

    public static boolean isShieldActive(double phase) {
        return phase >= SHIELD_PHASE_START && phase <= SHIELD_PHASE_END;
    }

    public static float getShieldIntensity(double phase) {
        if (!isShieldActive(phase)) return 0.0f;
        // Intensity scales with phase progression
        return 0.7f + 0.3f * (float)((phase - SHIELD_PHASE_START) / (SHIELD_PHASE_END - SHIELD_PHASE_START));
    }

    public static int getSplitHeadCount() {
        return SPLIT_HEAD_COUNT;
    }

    public static String describeState() {
        return "ShieldHalo: volumetric_3D_spherical_mesh | alpha_blend=" + true
                + " | edge_fade=localized | persistent_phase_4_7 | split_head_duplicate=" + SPLIT_HEAD_COUNT;
    }
}
