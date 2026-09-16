package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Native render-only HALO pass -- BUILD #416, rebuilt around the storm's own glow shader.
 *
 * WHAT CHANGED AND WHY. The previous halo was a six-ring curved oval mesh: a world-attached
 * cap with vertex-colour bands, i.e. exactly the "sky with a top on it" that this build
 * exists to delete. It is gone. What replaces it is a ring of the SAME additive glow quads
 * the teeth and eyes use -- <code>dabywitherstormmod:core/storm_glow</code>, the one shader
 * in this mod whose falloff is computed per PIXEL -- laid out around the storm's head:
 *
 *   * one camera-facing quad per ring segment, flat (no curvature, no cap, no dome),
 *   * each quad is the radial light pool storm_glow.fsh draws: gaussian body + hot core,
 *     clamped to the disc by a discard, so the ring has no polygonal corner anywhere,
 *   * the quads overlap, so the band reads as one continuous ring of thrown light that
 *     fades smoothly in every direction instead of as 40 flat decals,
 *   * the inner hole is deliberately wider than the head, which is what makes this a halo
 *     around the teeth rather than a lid over them: the mouth stays fully visible, lit by
 *     the same shader, at the same gain, so halo and teeth match by construction.
 *
 * Everything is tethered to the storm's own tracked position, so the halo travels with it --
 * the same tether the backdrop sheets use. Colour comes from McsmStormPhase.columnFor, i.e.
 * the traced reference sheets ("phase 5 turquoise sky" / "phase5sky0purple sky" /
 * "phase6sky 6 witherstorm"), which keeps the halo inside the same palette as the sky it
 * hangs in.
 *
 * There is deliberately no FabricSkyBoxes API, no JSON skybox, no vanilla sky texture
 * override, no block, no entity, no persistent GPU buffer and no physical dome.
 */
public final class McsmHaloSkyRenderer {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_white.png");

    /** Quads around the ring. One draw call's worth of overlap; 40 reads as continuous. */
    private static final int SEGMENTS = 40;
    private static final double MAX_DISTANCE = 2800.0D;
    /** Ring band: the light sits between these two radii of the head-relative extent. */
    private static final double RING_INNER = 0.62D;
    private static final double RING_OUTER = 1.30D;
    /** Peak alpha of ONE quad. The segments overlap, so the band sums to the final glow. */
    private static final float QUAD_ALPHA = 0.42F;

    private McsmHaloSkyRenderer() {
    }

    /** Submit one nearest phase-5+ halo ring, fail-soft on every frame. */
    public static void submit(LevelRenderContext ctx) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || ctx == null) {
                return;
            }

            Vec3 camera = ctx.levelState().cameraRenderState.pos;
            ClientDistantStormManager.StormData storm = nearestStorm(camera);
            if (storm == null) {
                return;
            }

            Vec3 stormPos = new Vec3(storm.dispX, storm.dispY, storm.dispZ);
            Vec3 toCamera = camera.subtract(stormPos);
            double distance = toCamera.length();
            if (distance < 1.0D || distance > MAX_DISTANCE) {
                return;
            }

            // Phase 4 is vanilla: the ring fades in with the atmosphere at 4.45 so
            // the onset is smooth rather than a hard band switch.
            float phase = storm.phase;
            float phaseFade = Mth.clamp((phase - 4.45F) / 0.22F, 0.0F, 1.0F);
            float distanceFade = 1.0F - Mth.clamp(
                    (float) ((distance - 1500.0D) / 1300.0D), 0.0F, 1.0F);
            float visibility = phaseFade * distanceFade;
            if (visibility <= 0.004F) {
                return;
            }

            // The ring plane: perpendicular to the line of sight to the storm, so it is
            // always a flat halo around the head and never a shape seen edge-on.
            Vec3 bearing = toCamera.scale(1.0D / distance);
            Vec3 upHint = Math.abs(bearing.y) > 0.985D
                    ? new Vec3(1.0D, 0.0D, 0.0D)
                    : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = bearing.cross(upHint).normalize();
            Vec3 up = right.cross(bearing).normalize();
            if (right.lengthSqr() < 1.0E-5D || up.lengthSqr() < 1.0E-5D) {
                return;
            }

            // World-space radius, scaled from the storm's body and the viewing distance so a
            // close approach pulls the halo in with the head instead of flooding the screen.
            double radius = Math.max(bodyRadius(phase) * 1.9D, distance * 0.085D);
            radius = Math.min(radius, 640.0D);

            // Config is loaded on the client tick; never touch config or files in this path.
            float size = Mth.clamp((float) McsmExtrasConfig.glareSize, 0.35F, 3.05F);
            double outer = radius * RING_OUTER * (0.80D + 0.20D * size);
            double inner = radius * RING_INNER;
            double quad = Math.max((outer - inner) * 1.15D, outer * 0.18D);

            RenderType material = GlowRenderTypes.glow(WHITE);
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            final Vec3 centre = stormPos;      // exact storm tether; the halo follows it
            final Vec3 r = right;
            final Vec3 u = up;
            final float alpha = visibility * QUAD_ALPHA;
            final double mid = (inner + outer) * 0.5D;

            collector.submitCustomGeometry(poseStack, material,
                    (pose, consumer) -> emitRing(pose, consumer, centre, r, u, mid, outer - inner,
                            quad, phase, alpha));
        } catch (Throwable ignored) {
            // A visual pass must disappear rather than crash the render thread.
        }
    }

    private static ClientDistantStormManager.StormData nearestStorm(Vec3 camera) {
        ClientDistantStormManager.StormData best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ClientDistantStormManager.StormData storm : ClientDistantStormManager.all()) {
            if (storm.phase < 4.45F) {
                continue;
            }
            double dx = storm.dispX - camera.x;
            double dy = storm.dispY - camera.y;
            double dz = storm.dispZ - camera.z;
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = storm;
            }
        }
        return best;
    }

    /**
     * One additive glow quad per segment, centred on the ring's midline. The quads are
     * spaced closer than their own width, which is what turns the row into a continuous
     * band; each one still gets storm_glow.fsh's own per-pixel falloff.
     */
    private static void emitRing(Pose pose, VertexConsumer consumer, Vec3 centre,
            Vec3 right, Vec3 up, double mid, double band, double quad,
            float phase, float maxAlpha) {
        double half = quad * 0.5D;
        for (int segment = 0; segment < SEGMENTS; segment++) {
            double angle = Math.PI * 2.0D * segment / SEGMENTS;
            // Ellipse: taller than wide, matching the storm's own silhouette.
            double cx = Math.cos(angle) * mid;
            double cy = Math.sin(angle) * mid * 1.12D;
            Vec3 centreOfQuad = centre.add(right.scale(cx)).add(up.scale(cy));

            // Colour out of the traced reference sheets at this point of the ring:
            // the top of the halo reads the zenith rows, the bottom the horizon rows.
            float t = Mth.clamp((float) (Math.sin(angle) * 0.5D + 0.5D), 0.0F, 1.0F);
            int color = McsmStormPhase.argb(McsmStormPhase.columnFor(phase, t));
            // The band's own thickness is a light curve, not a hard edge: the quads at the
            // widest part of the ring carry slightly more of the pool than the tight ones.
            float bandFade = Mth.clamp((float) (band / Math.max(quad, 1.0E-4D)), 0.0F, 1.0F);

            put(pose, consumer, centreOfQuad, right, up, half, color, maxAlpha * (0.72F + 0.28F * t) * bandFade);
        }
    }

    /** One camera-facing quad. UV0 spans 0..1: storm_glow.fsh reads it as the disc coordinate. */
    private static void put(Pose pose, VertexConsumer consumer, Vec3 centre,
            Vec3 right, Vec3 up, double half, int color, float alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        vertex(pose, consumer, centre, right, up, -half, -half, 0.0F, 0.0F, r, g, b, a);
        vertex(pose, consumer, centre, right, up, half, -half, 1.0F, 0.0F, r, g, b, a);
        vertex(pose, consumer, centre, right, up, half, half, 1.0F, 1.0F, r, g, b, a);
        vertex(pose, consumer, centre, right, up, -half, half, 0.0F, 1.0F, r, g, b, a);
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 centre,
            Vec3 right, Vec3 up, double dx, double dy, float u, float v,
            int r, int g, int b, int a) {
        Vec3 p = centre.add(right.scale(dx)).add(up.scale(dy));
        consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static double bodyRadius(float phase) {
        if (phase < 5.0F) {
            return 12.0D;
        }
        return phase < 6.0F
                ? 18.0D + 22.0D * (phase - 5.0F)
                : Math.min(340.0D, 40.0D + 30.0D * (phase - 6.0F));
    }
}
