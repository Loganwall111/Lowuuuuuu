package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Build #389 -- EMERGENCY VISUAL FIX: the floating 3D "giant bulbs" are gone.
 *
 * The old per-storm stack of three camera-facing billboard shells (teal /
 * purple / bloom, radii up to ~500 blocks at 150 blocks offset) rendered as
 * hard-edged solid balls parked in the sky - the green, lavender and tan
 * spheres in the user's screenshots. Every atmospheric colour now lives in
 * the horizon-wrapped environmental sky (sky.fsh gradient decks + the
 * McsmSkybox equirect sphere), which stretches seamlessly across the whole
 * horizon instead of forming circular balls.
 *
 * What remains here is the ONE legitimate element the user asked to keep:
 * the backdrop. It is a single, very small, very far disc placed BEHIND the
 * single true storm origin (nearest tracked storm / real storm entity -
 * strictly one unified atmospheric overlay system, no per-clone duplicates).
 * It ignites with phase 5 exactly like the rest of the late-game sky; during
 * phase 4 and earlier nothing rides the sky.
 */
public final class McsmGlareBackdrop {

    private static final Identifier BLOOM = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/mcsm/glare/glare_3.png");

    /** The backdrop sits far BEHIND the storm, at sky distance. */
    private static final double FAR_OFFSET = 1200.0D;
    /** Very small on purpose - a distant disc, not a giant bulb. */
    private static final double BACKDROP_RADIUS = 70.0D;

    private McsmGlareBackdrop() {
    }

    public static void submit(LevelRenderContext ctx) {
        if (ctx == null || Minecraft.getInstance() == null
                || !McsmExtrasConfig.glareBackdrop) {
            return;
        }
        try {
            Vec3 camera = ctx.levelState().cameraRenderState.pos;

            // ONE true storm origin: nearest tracked storm, or a real storm
            // entity in the level (the packet manager misses a commanded
            // wither standing next to the player). No per-clone loops.
            double bestD = Double.MAX_VALUE;
            float bestPhase = -1.0F;
            Vec3 bestPos = null;
            for (ClientDistantStormManager.StormData storm : ClientDistantStormManager.all()) {
                double dx = storm.dispX - camera.x, dy = storm.dispY - camera.y, dz = storm.dispZ - camera.z;
                double dd = dx * dx + dy * dy + dz * dz;
                if (dd < bestD) {
                    bestD = dd;
                    bestPhase = storm.phase;
                    bestPos = new Vec3(storm.dispX, storm.dispY, storm.dispZ);
                }
            }
            try {
                for (net.minecraft.world.entity.Entity e : Minecraft.getInstance().level.entitiesForRendering()) {
                    if (e instanceof net.dabicco.witherstormmod.entity.WitherStormEntity ws) {
                        double dx = ws.getX() - camera.x, dy = ws.getY() - camera.y, dz = ws.getZ() - camera.z;
                        double dd = dx * dx + dy * dy + dz * dz;
                        if (dd < bestD) {
                            bestD = dd;
                            bestPhase = (float) ws.getPhase();
                            bestPos = new Vec3(ws.getX(), ws.getY(), ws.getZ());
                        }
                    }
                }
            } catch (Throwable ignored) {
                // entity scan is best-effort
            }

            // Phase 5+ only; nothing rides the sky in the calm early phases.
            if (bestPos == null || bestPhase < 4.95F) {
                return;
            }
            double distance = Math.sqrt(bestD);
            // No weather storm nearby = completely regular vanilla: the
            // backdrop (like the sky) exists only while the storm is near.
            if (distance < 1.0E-4D || distance > 1700.0D) {
                return;
            }
            Vec3 view = bestPos.subtract(camera).scale(1.0D / distance);

            // Small, far, faint - the backdrop behind the storm.
            double radius = BACKDROP_RADIUS * Mth.clamp(McsmExtrasConfig.glareBackdropSize, 0.5D, 1.5D);
            int alpha = (int) (255.0F * 0.28F
                    * Mth.clamp((bestPhase - 4.95F) / 0.5F, 0.0F, 1.0F)
                    * (float) McsmExtrasConfig.glareBackdropStrength);
            submitQuad(ctx.poseStack(), ctx.submitNodeCollector(),
                    camera.add(view.scale(FAR_OFFSET)), view, radius, BLOOM, alpha);
        } catch (Throwable ignored) {
            // a backdrop must never break the level render
        }
    }

    /** One camera-facing billboard, no spin, no shells, no balls. */
    private static void submitQuad(PoseStack poseStack, SubmitNodeCollector collector,
            Vec3 at, Vec3 view, double radius, Identifier texture, int alpha) {
        if (alpha <= 2) {
            return;
        }
        Vec3 upHint = Math.abs(view.y) > 0.98D
                ? new Vec3(1, 0, 0)
                : new Vec3(0, 1, 0);
        Vec3 right = view.cross(upHint).normalize();
        Vec3 up = right.cross(view).normalize();
        Vec3 rx = right.scale(radius * 1.15D);
        Vec3 uy = up.scale(radius);
        final int a = Math.min(alpha, 255);
        collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(texture),
                (pose, consumer) -> {
                    vertex(pose, consumer, at.subtract(rx).subtract(uy), 0.0F, 1.0F, a);
                    vertex(pose, consumer, at.add(rx).subtract(uy), 1.0F, 1.0F, a);
                    vertex(pose, consumer, at.add(rx).add(uy), 1.0F, 0.0F, a);
                    vertex(pose, consumer, at.subtract(rx).add(uy), 0.0F, 0.0F, a);
                });
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at,
            float u, float v, int alpha) {
        consumer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
