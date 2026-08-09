package com.rewritten.devouringstorms.client;

import com.rewritten.devouringstorms.storm.MassgPhase;

/** Client-side mirror of the storm state, fed by StormSyncPayload packets. */
public final class StormClientState {

    public static int phase = -1;                  // -1 = no storm anywhere
    public static float growth = 0.0f;
    public static boolean critical = false;
    public static boolean stormActive = false;
    public static float intensity = 0.0f;          // smoothed 0..1 driving all presentation

    private StormClientState() {
    }

    public static MassgPhase currentPhase() {
        return stormActive && phase >= 0 ? MassgPhase.byId(phase) : null;
    }

    public static void update(int phase, float growth, boolean critical, boolean stormActive, float intensity) {
        StormClientState.phase = phase;
        StormClientState.growth = growth;
        StormClientState.critical = critical;
        StormClientState.stormActive = stormActive;
        StormClientState.intensity = intensity;
    }
}
