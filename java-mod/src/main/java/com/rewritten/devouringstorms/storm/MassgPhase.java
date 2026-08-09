package com.rewritten.devouringstorms.storm;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.BossEvent;

/**
 * The MASSG phase ladder — "MASSIVE ABOMINATION SUNDERING STORM GENESIS".
 *
 * SLEEPING  — dormant shell after the ritual. "MASSG IS WAKING UP."
 * SIGNAL    — the blueprints boot up; it notices you.
 * HUNGER    — begins devouring: tractor pull, block absorption.
 * DEVOURER  — full feeding cycle; growth accelerates.
 * SUNDERER  — tears living fragments off itself (Severed Storms).
 * BOWELS    — "phase 5.5": the storm splits open; the bowels glow burning violet.
 * GENESIS   — final form. Debris vortex, storm lightning, world-eater. If left alive,
 *             it keeps growing. It does not stop.
 * HUSK      — genesis struck down without the right tools: the storm falls out of the
 *             sky as a grounded zombie-form. The command block inside keeps what is
 *             left of it intact — only the Storm Killer can rend it.
 */
public enum MassgPhase implements StringRepresentable {
    SLEEPING(0, 0.6f, BossEvent.BossBarColor.BLUE),
    SIGNAL(1, 1.25f, BossEvent.BossBarColor.PURPLE),
    HUNGER(2, 1.6f, BossEvent.BossBarColor.PURPLE),
    DEVOURER(3, 2.0f, BossEvent.BossBarColor.PINK),
    SUNDERER(4, 2.6f, BossEvent.BossBarColor.RED),
    BOWELS(5, 3.0f, BossEvent.BossBarColor.RED),
    GENESIS(6, 3.5f, BossEvent.BossBarColor.YELLOW),
    HUSK(7, 2.2f, BossEvent.BossBarColor.WHITE);

    private final int id;
    private final float scale;
    private final BossEvent.BossBarColor color;

    MassgPhase(int id, float scale, BossEvent.BossBarColor color) {
        this.id = id;
        this.scale = scale;
        this.color = color;
    }

    public int id() {
        return id;
    }

    /** Entity SCALE attribute multiplier for this phase — the storm physically grows. */
    public float scale() {
        return scale;
    }

    public BossEvent.BossBarColor color() {
        return color;
    }

    public MassgPhase next() {
        return values()[Math.min(ordinal() + 1, values().length - 1)];
    }

    public boolean atLeast(MassgPhase other) {
        return ordinal() >= other.ordinal();
    }

    public static MassgPhase byId(int id) {
        for (MassgPhase p : values()) {
            if (p.id == id) return p;
        }
        return SLEEPING;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Display name shown on the boss bar per phase. */
    public String bossName() {
        return switch (this) {
            case SLEEPING -> "M A S S G — dormant";
            case SIGNAL -> "M A S S G — THE SIGNAL";
            case HUNGER -> "M A S S G — HUNGER";
            case DEVOURER -> "M A S S G — THE DEVOURER";
            case SUNDERER -> "M A S S G — THE SUNDERER";
            case BOWELS -> "M A S S G — THE BOWELS";
            case GENESIS -> "M A S S G — GENESIS";
            case HUSK -> "M A S S G — HUSK OF THE STORM";
        };
    }

    /** Phase 5.5 up through GENESIS: the bowels are exposed — the storm glows from within. */
    public boolean glows() {
        return atLeast(BOWELS) && this != HUSK;
    }

    /** Genesis struck down: the fell-to-earth zombie form. The command block holds it together. */
    public boolean alive() {
        return this != HUSK;
    }
}
