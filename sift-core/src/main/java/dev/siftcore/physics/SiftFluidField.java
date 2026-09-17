package dev.siftcore.physics;

/**
 * Shared geometry for the visual fluid sheets and their server-side current.
 *
 * <p>The sheets are not blocks or fluids. They are mathematical layers so the
 * client and server can agree on where a current is felt without introducing a
 * collision shape into the air-only dimension.</p>
 */
public final class SiftFluidField {
    public static final double LAYER_SPACING = 48.0D;
    public static final double LAYER_OFFSET = 16.0D;
    public static final double CURRENT_BAND = 7.0D;

    private SiftFluidField() {
    }

    /** Returns the nearest fluid sheet height for a world-space Y coordinate. */
    public static double nearestLayer(double y) {
        return layerHeight(nearestLayerIndex(y));
    }

    /** Returns the stable integer index of the nearest sheet. */
    public static int nearestLayerIndex(double y) {
        return (int) Math.rint((y - LAYER_OFFSET) / LAYER_SPACING);
    }

    public static double layerHeight(int index) {
        return LAYER_OFFSET + index * LAYER_SPACING;
    }

    /** A linear current falloff, reaching zero at the edge of the sheet band. */
    public static double currentFalloff(double y) {
        double distance = Math.abs(y - nearestLayer(y));
        return distance >= CURRENT_BAND ? 0.0D : 1.0D - distance / CURRENT_BAND;
    }

    /** Shared horizontal flow phase used by both the visual sheet and player current. */
    public static double currentPhase(double time, double x, double z) {
        return time * 0.035D + x * 0.012D + z * 0.009D;
    }

    public static double currentX(double time, double x, double z) {
        return Math.cos(currentPhase(time, x, z));
    }

    public static double currentZ(double time, double x, double z) {
        return Math.sin(currentPhase(time, x, z) * 1.31D);
    }
}
