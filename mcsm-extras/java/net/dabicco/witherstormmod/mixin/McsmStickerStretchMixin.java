package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcsm.extras.McsmExtrasConfig;

/**
 * BUILD #422 (Phase 1) -- THE 2D BACKDROP STICKER'S BOTTOM IS STRETCHED PAST
 * BEDROCK.
 *
 * THE REPORT. "There's a top layer but there isn't a bottom layer." The storm's
 * backdrop sheets are drawn as flat, camera-axis-locked stickers (see
 * {@code StormBackdrop}, the 2D background sky layer -- never a dome, never
 * world-space geometry). A sticker is a square of texture on the sky: its TOP
 * half is what a player looks at, and its bottom edge lands exactly on the
 * horizon. Below that edge there was nothing at all, so the lower sky fell back
 * to whatever the regular sky pass was painting -- the seam the user kept
 * finding under the storm.
 *
 * WHAT THIS DOES. The sticker is no longer one square. Its bottom half is STRETCHED
 * downward by {@code backdropBottomStretch} (default 6x, i.e. deep past bedrock
 * level and out the bottom of the frustum) with the texture coordinates of that
 * half left exactly as they were:
 *
 *     top half     at + uy  ..  at        v = 0.00 .. 0.50   (unchanged)
 *     bottom half  at       ..  at - uy*N v = 0.50 .. 1.00   (stretched N times)
 *
 * so the bottom rows of the sheet are smeared into a solid wall of the sheet's
 * own horizon colour. No new colour, no second gradient, no edge: the backdrop
 * simply keeps going down as far as the world does. The pass is cancelled and
 * re-drawn here because the geometry is the whole point of it.
 *
 * require = 0: if the base build's sticker signature ever changes, this quietly
 * stops applying and the backdrop renders exactly as it did before.
 */
@Mixin(net.dabicco.witherstormmod.client.StormBackdrop.class)
public abstract class McsmStickerStretchMixin {

    @Inject(method = "sticker", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mcsm$stretchedSticker(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            RenderType type,
            Vec3 cam,
            Vec3 view,
            Vec3 right,
            Vec3 up,
            double dist,
            double half,
            float alpha,
            CallbackInfo ci) {
        try {
            double stretchSetting = McsmExtrasConfig.backdropBottomStretch;
            if (stretchSetting <= 1.0D) {
                return;      // 1x or less: the base sticker draws exactly as before
            }
            int a = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 235.0F);
            if (a <= 2) {
                ci.cancel();  // the base would draw nothing either
                return;
            }
            double stretch = Math.min(24.0D, Math.max(2.0D, stretchSetting));
            Vec3 at = cam.add(view.scale(dist));
            Vec3 rx = right.scale(half * 1.15D);
            Vec3 uy = up.scale(half);
            Vec3 low = up.scale(-half * stretch);
            Vec3 mid = Vec3.ZERO;
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                // the upper half: exactly the sheet's top, unmoved
                vertex(pose, consumer, at.subtract(rx).add(uy), 0.0F, 0.0F, a);
                vertex(pose, consumer, at.add(rx).add(uy), 1.0F, 0.0F, a);
                vertex(pose, consumer, at.add(rx).add(mid), 1.0F, 0.5F, a);
                vertex(pose, consumer, at.subtract(rx).add(mid), 0.0F, 0.5F, a);
                // the stretched half: same rows, many times the height
                vertex(pose, consumer, at.subtract(rx).add(mid), 0.0F, 0.5F, a);
                vertex(pose, consumer, at.add(rx).add(mid), 1.0F, 0.5F, a);
                vertex(pose, consumer, at.add(rx).add(low), 1.0F, 1.0F, a);
                vertex(pose, consumer, at.subtract(rx).add(low), 0.0F, 1.0F, a);
            });
            ci.cancel();
        } catch (Throwable ignored) {
            // if anything about this is not available, the base sticker draws
        }
    }

    /** The base pass's own vertex shape, so the sheet lands identically. */
    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int a) {
        consumer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(255, 255, 255, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
