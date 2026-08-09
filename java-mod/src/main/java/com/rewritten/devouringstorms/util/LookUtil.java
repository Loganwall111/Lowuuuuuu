package com.rewritten.devouringstorms.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Shared "silent gaze" math. */
public final class LookUtil {

    private LookUtil() {
    }

    /**
     * True when the player is actually looking at the entity: narrow field of view,
     * unobstructed line of sight, within range.
     */
    public static boolean isLookingAt(Player player, Entity target, double maxDistance, double minDot) {
        if (player.distanceTo(target) > maxDistance) return false;
        if (!player.hasLineOfSight(target)) return false;
        Vec3 view = player.getViewVector(1.0f).normalize();
        Vec3 toTarget = target.getEyePosition().subtract(player.getEyePosition()).normalize();
        return view.dot(toTarget) >= minDot;
    }

    /** Standard gaze cone (~25 degrees). */
    public static boolean isGazing(Player player, Entity target, double maxDistance) {
        return isLookingAt(player, target, maxDistance, 0.9);
    }
}
