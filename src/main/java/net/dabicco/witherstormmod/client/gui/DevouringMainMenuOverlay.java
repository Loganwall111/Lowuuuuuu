package net.dabicco.witherstormmod.client.gui;

/**
 * DevouringMainMenuOverlay — Custom main menu overlay for Devouring Storms.
 * Adds atmospheric effects to the title screen.
 * Rendering integration handled via mixin hooks.
 */
public class DevouringMainMenuOverlay {

    private static float animationTime = 0;
    private static float pulseIntensity = 0;

    public static void tick() {
        animationTime += 1.0f;
        pulseIntensity = (float)(Math.sin(animationTime * 0.05) * 0.5 + 0.5);
    }

    public static float getPulseIntensity() {
        return pulseIntensity;
    }

    public static float getAnimationTime() {
        return animationTime;
    }
}
