package com.rewritten.devouringstorms.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Shared render state for all Devouring Storms entities. */
public class DsRenderState extends EntityRenderState {
    public float ageTicks;      // tickCount + partial tick, for animation
    public int phase;           // MASSG only
    public float growth;        // MASSG only
    public boolean critical;    // MASSG only
    public float opacity = 1.0f;
    public int variant = 0;      // MassgVariant ordinal (MASSG/SEVERED) or tazo variant id

    public DsRenderState copy() {
        DsRenderState out = new DsRenderState();
        out.ageTicks = this.ageTicks;
        out.phase = this.phase;
        out.growth = this.growth;
        out.critical = this.critical;
        out.opacity = this.opacity;
        out.variant = this.variant;
        return out;
    }
}
