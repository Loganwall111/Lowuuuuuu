package com.rewritten.devouringstorms.entity;

import java.util.Locale;

/**
 * THE DENS — the storm's colour-denominations.
 * "We missed three variants of the storm, command labs. Rose, abyssal, ivory." Same
 * body, same hunger, different light — and the light is the warning label:
 *   CLASSIC  — the violet everyone photographs last.
 *   ROSE     — the pink variant. Feeds faster than the camera can blink.
 *   ABYSSAL  — drowned-cobalt. Sees further than maps.
 *   IVORY    — bleached ash. Forgives nothing.
 */
public enum MassgVariant {

    CLASSIC("classic", 1.00f, 1.00f, 1.00f),
    ROSE("rose", 1.00f, 0.45f, 0.85f),
    ABYSSAL("abyssal", 0.45f, 0.62f, 1.00f),
    IVORY("ivory", 0.92f, 0.88f, 0.78f);

    public final String name;
    /** Tint multipliers (renderer-side grade over the classic atlas). */
    public final float r, g, b;

    MassgVariant(String name, float r, float g, float b) {
        this.name = name;
        this.r = r; this.g = g; this.b = b;
    }

    public static MassgVariant byName(String name) {
        if (name == null) return CLASSIC;
        String n = name.toLowerCase(Locale.ROOT);
        for (MassgVariant v : values()) if (v.name.equals(n)) return v;
        return CLASSIC;
    }

    public static MassgVariant byIdTurn(int id) {
        var vs = values();
        return vs[Math.floorMod(id, vs.length)];
    }
}
