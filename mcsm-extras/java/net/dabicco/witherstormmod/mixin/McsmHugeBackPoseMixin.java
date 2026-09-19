package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.mcsm.extras.client.McsmHugeBackCentre;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BUILD #423 -- SCALING THE PHASE-5.5 UPPER BACK ABOUT ITSELF, NOT THE ORIGIN.
 *
 * The other half of McsmHugeBackAttachMixin (read that class first -- it carries
 * the measurement and the reason). This one watches the pose stack the
 * net.minecraft.client rendering pipeline uses and, while the renderer has
 * published a huge-back centre, converts the one scale that matters:
 *
 *     scale(1.72, 1.72, 1.72) about the model origin
 *         ->  scale(1.72, 1.72, 1.72) about the model's own centre
 *
 * which is exactly one extra translate: scaling about the origin sends p to
 * s*p, scaling about the centre c sends it to s*(p - c) + c = s*p + (1 - s)*c,
 * and a translate applied after the scale adds s*a -- so a = (1 - s)/s * c.
 * With s = 1.72 that is a = -0.4186 * c, applied in the model's own frame, so
 * the huge back grows where it stands instead of being thrown ~34 blocks up and
 * back off the body.
 *
 * WHY 1.72 IS THE TEST. That literal appears in the renderer exactly twice, both
 * of them the huge back (the shadow capture and the draw), and both inside
 * submitGrowth5 -- which is exactly when the centre is published. The body's own
 * -4 scale, the mirror flips (1, 1, -1) and (-1, 1, 1), and submitScaled's
 * 0..1 growth scale all pass through untouched.
 *
 * The method is selected by NAME ONLY ("scale"), so this class does not have to
 * guess the return type of PoseStack.scale on this game version -- and with
 * require = 0 a build whose scale() does not look like this simply skips the
 * correction instead of failing to launch.
 */
@Mixin(PoseStack.class)
public abstract class McsmHugeBackPoseMixin {

    /** The renderer's own enlargement factor for the huge back. */
    private static final float MCSM_HUGE_BACK_SCALE = 1.72F;

    @Inject(method = "scale", at = @At("RETURN"), remap = false, require = 0)
    private void mcsm$scaleHugeBackAboutItself(float x, float y, float z, CallbackInfo ci) {
        try {
            if (x != MCSM_HUGE_BACK_SCALE || y != MCSM_HUGE_BACK_SCALE || z != MCSM_HUGE_BACK_SCALE) {
                return;      // every other scale in the game is none of our business
            }
            float[] c = McsmHugeBackCentre.centre();
            if (c == null || c.length < 3) {
                return;      // no huge back is being submitted right now
            }
            double k = (1.0D - MCSM_HUGE_BACK_SCALE) / MCSM_HUGE_BACK_SCALE;
            ((PoseStack) (Object) this).translate(
                    k * c[0], k * c[1], k * c[2]);
        } catch (Throwable ignored) {
            // A presentation correction must never break a frame.
        }
    }
}
