package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * World-attached Wither Storm overcast.
 *
 * This is deliberately an entity render component, not a sky pass. The
 * renderer calls it while the Wither Storm's own pose stack is still active,
 * so the backdrop inherits the entity translation, rotation, interpolation,
 * and scale exactly once. No camera position, level render event, SkyRenderer,
 * skybox resource, or screen-facing billboard is involved.
 *
 * The mesh is the original curved, low-poly 3D wall behind the body. Its
 * footprint is phase-sized: close to the storm at Phase 5.5, then larger at
 * Phases 6 and 7. It is a render-only translucent object with no collision, so
 * the camera and clouds can pass through it instead of revealing a distant
 * back face. The outer margin reaches zero alpha, allowing the ordinary world
 * and clouds to show through its soft edge.
 */
public final class McsmAtmosphericMeshComponent {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_white.png");

    private static final int COLUMNS = 28;
    private static final int ROWS = 12;
    /** Close attached oval; it grows with the storm rather than becoming a far sky card. */
    private static final float BEHIND_OFFSET = 36.0F;
    private static final float MAX_ALPHA = 0.72F;
    private static final double START_PHASE = 4.45D;

    private McsmAtmosphericMeshComponent() {
    }

    /**
     * Called from WitherStormRenderer.submit at HEAD, before the chassis,
     * heads, tentacles, and ordinary local debris are submitted.
     */
    public static void submit(WitherStormRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector) {
        if (state == null || poseStack == null || collector == null
                || state.preview != null || state.phase < START_PHASE) {
            return;
        }

        // Keep the phase input explicit: this is the Java-side u_StormPhase
        // carrier for the dynamic vertex palette, not a camera or level query.
        float u_StormPhase = (float) state.phase;
        float phaseFade = smoothstep(u_StormPhase, 4.45F, 4.68F);
        float phase = u_StormPhase;
        if (phaseFade <= 0.004F) {
            return;
        }

        // Match the renderer's body orientation. The pose stack already owns
        // the entity's world-space translation; this local rotation keeps the
        // atmosphere parented to the same yaw as the boss rather than to the
        // camera bearing.
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
        if (state.bodyRoll != 0.0F) {
            poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));
        }

        final float finalPhase = phase;
        final float finalFade = phaseFade;
        collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(WHITE),
                (pose, consumer) -> emitBackdrop(pose, consumer, finalPhase, finalFade));
        poseStack.popPose();
    }

    private static void emitBackdrop(Pose pose, VertexConsumer consumer,
            float phase, float phaseFade) {
        // Keep the original curved-wall construction, but size it from the
        // actual phase. At 5.5 the top is about 10 blocks above the body;
        // Phases 6 and 7 deliberately grow the oval instead of moving it away.
        double radius = bodyRadius(phase);
        // The reference halo is a broad horizontal oval, not a small round
        // smudge. Keep it attached to the body but make its width exceed the
        // top silhouette from phase 5.5 onward.
        double widthFactor = phase < 5.0F ? 2.15D
                : (phase < 5.5F ? 2.65D
                : (phase < 6.0F ? 3.15D
                : (phase < 7.0F ? 3.55D : 3.80D)));
        double halfWidth = radius * widthFactor;
        double extraTop = phase < 5.5F ? 8.0D : 8.0D + Math.max(0.0D, phase - 5.5D) * 14.0D;
        double halfHeight = radius * 0.86D + extraTop;
        double depth = Math.min(58.0D, halfWidth * 0.30D);
        double behind = Math.min(BEHIND_OFFSET, Math.max(18.0D, radius * 0.85D));

        for (int row = 0; row < ROWS; row++) {
            double y0 = -1.0D + 2.0D * row / ROWS;
            double y1 = -1.0D + 2.0D * (row + 1) / ROWS;
            // Keep only the lower atmospheric deck. The upper half was the
            // unwanted second sky/card band visible above the storm; the
            // native Minecraft sky remains responsible for the regular upper
            // sky path.
            if (y0 >= 0.0D) {
                continue;
            }
            for (int column = 0; column < COLUMNS; column++) {
                double x0 = -1.0D + 2.0D * column / COLUMNS;
                double x1 = -1.0D + 2.0D * (column + 1) / COLUMNS;
                Vec3 a = point(x0, y0, halfWidth, halfHeight, behind, depth);
                Vec3 b = point(x1, y0, halfWidth, halfHeight, behind, depth);
                Vec3 c = point(x1, y1, halfWidth, halfHeight, behind, depth);
                Vec3 d = point(x0, y1, halfWidth, halfHeight, behind, depth);

                putQuad(pose, consumer, phase, phaseFade, a, b, c, d, x0, x1, y0, y1);
            }
        }
    }

    /** A convex-in-depth, horizontally stretched atmospheric wall. */
    private static Vec3 point(double x, double y, double halfWidth, double halfHeight,
            double behind, double depth) {
        double horizontalCurve = 1.0D - x * x;
        double verticalCurve = 0.78D + 0.22D * (1.0D - y * y);
        double z = behind + depth * horizontalCurve * verticalCurve;
        return new Vec3(x * halfWidth, y * halfHeight, z);
    }

    private static void putQuad(Pose pose, VertexConsumer consumer, float phase,
            float phaseFade, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
            double x0, double x1, double y0, double y1) {
        int ca = colour(phase, x0, y0, phaseFade);
        int cb = colour(phase, x1, y0, phaseFade);
        int cc = colour(phase, x1, y1, phaseFade);
        int cd = colour(phase, x0, y1, phaseFade);
        Vec3 normal = normal(c.subtract(a).cross(b.subtract(a)));

        vertex(pose, consumer, a, 0.0F, 1.0F, ca, normal);
        vertex(pose, consumer, b, 1.0F, 1.0F, cb, normal);
        vertex(pose, consumer, c, 1.0F, 0.0F, cc, normal);
        vertex(pose, consumer, d, 0.0F, 0.0F, cd, normal);
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at,
            float u, float v, int colour, Vec3 normal) {
        consumer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor((colour >> 16) & 0xFF, (colour >> 8) & 0xFF,
                        colour & 0xFF, (colour >>> 24) & 0xFF)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    /** The deck of the band owning this quad, at this quad's height on the wall. */
    private static int colour(float phase, double x, double y, float phaseFade) {
        float radius = Mth.clamp((float) Math.sqrt(x * x + y * y), 0.0F, 1.0F);
        float vertical = Mth.clamp((float) ((y + 1.0D) * 0.5D), 0.0F, 1.0F);
        // BUILD #430 -- the wall wears the SUPPLIED SHEETS' columns, read out of
        // the one generated palette the story stage shell shares. This file used
        // to carry its own hand-typed horizon/zenith pair per phase and could
        // drift from the sky without any gate noticing. The palette's t runs
        // zenith (0) -> horizon (1); this wall's vertical runs the other way, so
        // the horizon row lands on the wall's bottom edge where it belongs, and
        // the band routing is sky.fsh's own (5.10-5.50, 5.75-6.05, 7.00-8.05).
        int rgb = McsmBackdropPalette.column(phase, 1.0F - vertical);

        float outerFade = 1.0F - smoothstep(radius, 0.68F, 1.0F);
        float corePass = phase >= 5.0F
                ? 0.22F + 0.78F * smoothstep(radius, 0.0F, 0.42F)
                : 1.0F;
        float bottomSilhouette = 1.0F - smoothstep(vertical, 0.0F, 0.32F);
        rgb = mix(rgb, McsmBackdropPalette.VOID_BLACK, bottomSilhouette * 0.82F);
        float alpha = MAX_ALPHA * phaseFade * outerFade * corePass;
        return (Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24) | (rgb & 0x00FFFFFF);
    }

    private static int mix(int a, int b, float amount) {
        float t = Mth.clamp(amount, 0.0F, 1.0F);
        int r = Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bBlue = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return rgb(r, g, bBlue);
    }

    private static int rgb(int r, int g, int b) {
        return (Mth.clamp(r, 0, 255) << 16)
                | (Mth.clamp(g, 0, 255) << 8)
                | Mth.clamp(b, 0, 255);
    }

    private static Vec3 normal(Vec3 value) {
        return value.lengthSqr() < 1.0E-8D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : value.normalize();
    }
}
