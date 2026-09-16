package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.McsmWhiteGlow;
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
 * BUILD #416 -- the body-tethered WHITE CONIC AMBIENT LIGHT COLUMN.
 *
 * WHAT THIS REPLACED, AND WHY. Two shapes have been retired here:
 *
 *   1. the six-ring curved oval cap (a world-attached "sky with a top on it"),
 *      and
 *   2. the flat white halo disc that was pinned onto the lower chassis layers --
 *      a horizontal circle sitting under the body, which read as a sticker and
 *      did not move with the storm as it grew.
 *
 * Neither is coming back. What draws here now is a soft, TALL CONE OF LIGHT that
 * wraps the storm from its lowest core blocks up past the top tentacle
 * attachments, and nothing else: no cap, no disc, no ring, no geometry of its
 * own beyond a stack of camera-facing light bands.
 *
 * HOW IT IS BUILT. The column is a stack of additive glow quads sharing the
 * teeth/eye glow shader, `core/storm_glow`, through the dedicated white pipeline
 * ({@link McsmWhiteGlow#glowWhite}). That matters for two reasons: the falloff
 * is computed PER PIXEL (so the column has no polygonal edge anywhere and its
 * sides fade into the sky instead of ending at a line), and the pipeline pins the
 * pool to WHITE, so the atmosphere the storm throws is white even in the phases
 * whose aura around the teeth is blue, green or magenta.
 *
 * Each band is billboarded around the column's own axis: it keeps world-up and
 * faces the camera, so the cone reads as a volume from every angle without ever
 * being a flat cut-out. Bands overlap by 30%, which welds them into one column.
 *
 * THE GROWTH WELD (the point of the whole exercise). Radius, height AND the
 * cone's spread angle all come from the phase and from the entity's own scale
 * multiplier -- see {@code McsmStormPhase.growth/bodyRadius/bodyHeight/
 * scaleMultiplier/columnHalfAngleDeg}. Nothing here is a constant that could get
 * out of step with the body: every time the chassis grows, the radius scale and
 * the vertical spread angle are recomputed from the same numbers the body uses,
 * so the light expands in sync around the growing block clusters. A severed head
 * pulls the column in with it.
 *
 * Tethering is exact: the column's base is the storm's own tracked position, the
 * same tether the backdrop sheets and the phase feed use.
 *
 * There is deliberately no FabricSkyboxes API, no JSON skybox, no vanilla sky
 * texture override, no block, no entity, no persistent GPU buffer and no physical
 * dome. It is a render-only pass that travels with the storm.
 */
public final class McsmHaloSkyRenderer {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_white.png");

    /** Bands in the stack. Overlapped 1.30x, so the column reads as continuous. */
    private static final int BANDS = 26;
    private static final double BAND_OVERLAP = 1.30D;
    private static final double MAX_DISTANCE = 2800.0D;
    /** Peak alpha of ONE band. The stack sums to the final column. */
    private static final float BAND_ALPHA = 0.30F;
    /** Widest the column may ever get in blocks, so a close pass cannot flood the screen. */
    private static final double MAX_RADIUS = 620.0D;

    private McsmHaloSkyRenderer() {
    }

    /** Submit one nearest phase-5+ light column, fail-soft on every frame. */
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

            // Phase 4.45 is the onset: the column fades in with the atmosphere so
            // there is never a hard band switch.
            float phase = storm.phase;
            float phaseFade = Mth.clamp((phase - 4.45F) / 0.22F, 0.0F, 1.0F);
            float distanceFade = 1.0F - Mth.clamp(
                    (float) ((distance - 1500.0D) / 1300.0D), 0.0F, 1.0F);
            float visibility = phaseFade * distanceFade;
            if (visibility <= 0.004F) {
                return;
            }

            // ---- the growth weld -------------------------------------------------
            float scale = McsmStormPhase.scaleMultiplier(phase, storm.activeHeads);
            double bodyRadius = McsmStormPhase.bodyRadius(phase);
            double height = McsmStormPhase.bodyHeight(phase) * scale;
            // the column starts inside the lowest core blocks and reaches past the
            // top tentacle attachments, so it wraps the whole silhouette
            double baseLift = -bodyRadius * 0.30D * scale;
            double baseRadius = Math.min(MAX_RADIUS, bodyRadius * 0.68D * scale);
            double spreadRad = Math.toRadians(McsmStormPhase.columnHalfAngleDeg(phase));
            double topSpread = Math.min(MAX_RADIUS, baseRadius + Math.tan(spreadRad) * height);
            // a closer camera pulls the column in with the body instead of filling the view
            double clamp = Math.max(distance * 0.55D, bodyRadius * 1.2D);
            topSpread = Math.min(topSpread, clamp);
            baseRadius = Math.min(baseRadius, clamp);

            // ---- the billboard basis --------------------------------------------
            Vec3 view = toCamera.scale(1.0D / distance);
            Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = new Vec3(view.z, 0.0D, -view.x);
            if (right.lengthSqr() < 1.0E-6D) {
                right = new Vec3(1.0D, 0.0D, 0.0D);
            }
            right = right.normalize();

            // Config is loaded on the client tick; never touch config or files in this pass.
            float size = Mth.clamp((float) McsmExtrasConfig.glareSize, 0.35F, 3.05F);
            double sizeScale = 0.85D + 0.15D * size;

            RenderType material = McsmWhiteGlow.glowWhite(WHITE);
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            final Vec3 base = stormPos.add(0.0D, baseLift, 0.0D);
            final Vec3 r = right;
            final Vec3 u = up;
            final float alpha = visibility * BAND_ALPHA;
            final double columnHeight = height;
            final double r0 = baseRadius * sizeScale;
            final double r1 = topSpread * sizeScale;

            collector.submitCustomGeometry(poseStack, material,
                    (pose, consumer) -> emitColumn(pose, consumer, base, r, u,
                            columnHeight, r0, r1, alpha));
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
     * One camera-facing band per step up the cone. Each band is as wide as the
     * cone is at that height, so the stack IS the cone; the shader's per-pixel
     * falloff turns every band into a soft pool of light rather than a rectangle.
     */
    private static void emitColumn(Pose pose, VertexConsumer consumer, Vec3 base,
            Vec3 right, Vec3 up, double height, double r0, double r1, float maxAlpha) {
        double step = height / (BANDS - 1.0D);
        double halfHeight = step * BAND_OVERLAP * 0.5D;
        for (int band = 0; band < BANDS; band++) {
            double u = band / (BANDS - 1.0D);
            double radius = r0 + (r1 - r0) * u;
            double halfWidth = Math.max(radius, 0.35D);

            // Vertical light profile. It starts at ZERO, which is what keeps the
            // base open -- no flat disc under the storm -- and softens toward the
            // top so the column dissolves into the sky instead of stopping.
            double fadeIn = smoothstep(u / 0.20D);
            double fadeOut = 1.0D - smoothstep((u - 0.45D) / 0.55D);
            double profile = fadeIn * fadeOut * (0.55D + 0.45D * (1.0D - u));
            float alpha = (float) (maxAlpha * profile);
            if (alpha <= 0.004F) {
                continue;
            }

            Vec3 centre = base.add(up.scale(u * height));
            put(pose, consumer, centre, right, up, halfWidth, halfHeight, alpha);
        }
    }

    /** One camera-facing band, white. UV0 spans 0..1: storm_glow.fsh reads it as the disc coordinate. */
    private static void put(Pose pose, VertexConsumer consumer, Vec3 centre,
            Vec3 right, Vec3 up, double halfWidth, double halfHeight, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        vertex(pose, consumer, centre, right, up, -halfWidth, -halfHeight, 0.0F, 0.0F, a);
        vertex(pose, consumer, centre, right, up, halfWidth, -halfHeight, 1.0F, 0.0F, a);
        vertex(pose, consumer, centre, right, up, halfWidth, halfHeight, 1.0F, 1.0F, a);
        vertex(pose, consumer, centre, right, up, -halfWidth, halfHeight, 0.0F, 1.0F, a);
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 centre,
            Vec3 right, Vec3 up, double dx, double dy, float u, float v, int a) {
        Vec3 p = centre.add(right.scale(dx)).add(up.scale(dy));
        consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(255, 255, 255, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static double smoothstep(double t) {
        double c = Mth.clamp(t, 0.0D, 1.0D);
        return c * c * (3.0D - 2.0D * c);
    }
}
