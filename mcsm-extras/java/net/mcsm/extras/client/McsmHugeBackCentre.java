package net.mcsm.extras.client;

/**
 * BUILD #423 -- WHERE THE PHASE-5.5 UPPER BACK ACTUALLY SITS.
 *
 * The user's report: "phase 5.5 has an upper back disattached from the main
 * body". The only thing that exists at 5.4-5.8 and nowhere else is the huge
 * back (WitherStormRenderer.submitGrowth5 draws HugeAssBackModel), and the
 * reason it reads as disattached is measurable:
 *
 *   the huge back's own geometry is authored far from the model origin
 *   (its bounds centre is about 11.7 blocks BELOW the origin), and the
 *   renderer enlarges it by scaling 1.72x about that origin. Scaling a body
 *   whose mass is 11.7 blocks from the pivot by 1.72 moves its centre by
 *   0.72 * 11.7 = 8.4 model blocks, which the storm's own -4 body scale turns
 *   into about 34 world blocks. So the huge back is flung up and back, off the
 *   body it belongs to -- exactly what the user photographed.
 *
 * This holder is the hand-off between the two halves of the fix:
 *
 *   McsmHugeBackAttachMixin (renderer)  publishes the model's measured centre
 *                                       for the duration of submitGrowth5, and
 *                                       takes it back down at RETURN;
 *   McsmHugeBackPoseMixin    (PoseStack) sees the 1.72x scale while that centre
 *                                       is published and adds the one translate
 *                                       that turns "scale about the origin"
 *                                       into "scale about the model's own
 *                                       centre", so the back grows where it is
 *                                       instead of flying off the body.
 *
 * The maths, so it can be checked rather than trusted: scaling about the origin
 * maps a point p to s*p, and scaling about the centre c is s*(p - c) + c =
 * s*p + (1 - s)*c. A translate applied AFTER the scale adds s*a, so a is
 * (1 - s)/s * c -- one translate, no second pivot, nothing to unwind.
 */
public final class McsmHugeBackCentre {

    private McsmHugeBackCentre() {
    }

    /** The huge back's measured centre in model blocks, or null outside the pass. */
    private static volatile float[] centre = null;
    /** How many nested submitGrowth5 calls are open (shadow capture nests none,
     *  but a counter costs nothing and cannot go stale). */
    private static volatile int depth = 0;

    /**
     * Publishes the centre of the model that is about to be enlarged. Called by
     * the renderer mixin at the head of submitGrowth5 with the bounds array
     * CubeReveal.bounds() returns: {minX, maxX, minY, maxY, minZ, maxZ}.
     */
    public static void inside(float[] bounds) {
        if (bounds == null || bounds.length < 6) {
            centre = null;
            return;
        }
        centre = new float[]{
                (bounds[0] + bounds[1]) * 0.5F,
                (bounds[2] + bounds[3]) * 0.5F,
                (bounds[4] + bounds[5]) * 0.5F
        };
        depth++;
    }

    /** Called at RETURN of submitGrowth5. */
    public static void leave() {
        if (depth > 0) {
            depth--;
        }
        if (depth <= 0) {
            centre = null;
        }
    }

    /** The centre, or null when no huge back is being submitted. */
    public static float[] centre() {
        return centre;
    }
}
