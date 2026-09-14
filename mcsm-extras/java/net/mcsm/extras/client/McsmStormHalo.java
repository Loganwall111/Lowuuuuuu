package net.mcsm.extras.client;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.Pose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.dabicco.witherstormmod.client.StormPalettes;
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
 * Build #374: the restored working blue halo.
 *
 * The base mod's own halo pass (StormPresenceFX "cataclysm halos") was the old
 * working construction: the halo_ring.png billboard tinted by the blue
 * StormPalettes.haloRingColor / haloUnderColor colours. It ran from phase 5.8
 * as a far detached card, and a previous session cancelled the whole presence
 * pass over it. This pass restores the same blue halo files, welded to the
 * storm chassis: the ring is centred on the storm's (temporally smoothed)
 * body, sized to the body's own silhouette radius, camera-facing, and it
 * ramps in dynamically at phase 4.0 and holds through every later stage.
 *
 * All render calls are copied from the proven 26.2 surface of
 * McsmStormBlob / the base StormPresenceFX.
 */
public final class McsmStormHalo {

    /** The base mod's own halo texture (shipped inside the base jar). */
    private static final Identifier HALO = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/halo_ring.png");

    /** Temporal smoothing of the body centre, per storm. */
    private static final Map<Integer, Vec3> SMOOTH = new HashMap<>();

    private McsmStormHalo() {
    }

    private static float ramp(float v, float lo, float hi) {
        if (hi <= lo) {
            return v >= hi ? 1.0F : 0.0F;
        }
        float t = Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    /** Copied from McsmStormBlob — the body's world-space silhouette radius. */
    private static double bodyRadius(float phase) {
        if (phase < 4.0F) {
            return 4.0F + 1.5F * phase;
        } else if (phase < 5.0F) {
            return 10.0F + 8.0F * (phase - 4.0F);
        } else {
            return phase < 6.0F ? 18.0F + 22.0F * (phase - 5.0F) : 40.0F + 30.0F * (phase - 6.0F);
        }
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.stormHaloEnabled) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || ClientDistantStormManager.all().isEmpty()) {
                return;
            }
            float gt = (float) (mc.level.getGameTime() % 2400000L)
                    + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float nowSec = gt * 0.05F;
            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();

            int idx = 0;
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                int key = idx++;
                float phase = d.phase;
                // Ramps in dynamically at phase 4.0 and HOLDS through every
                // later stage — there is deliberately no phase-out.
                float rampIn = ramp(phase, 4.0F, 4.3F);
                if (rampIn <= 0.004F) {
                    continue;
                }

                Vec3 centre = new Vec3(d.dispX, d.dispY, d.dispZ);
                Vec3 prev = SMOOTH.get(key);
                if (prev != null) {
                    centre = prev.add(centre.subtract(prev).scale(0.25D));
                }
                SMOOTH.put(key, centre);

                Vec3 toStorm = centre.subtract(cam);
                double dist = toStorm.length();
                if (dist < 1.0E-4D) {
                    continue;
                }
                float distFade = 1.0F - Mth.clamp((float) ((dist - 1200.0D) / 900.0D), 0.0F, 1.0F);
                if (distFade <= 0.004F) {
                    continue;
                }

                double bodyR = bodyRadius(phase);
                // A gentle dynamic pulse so the halo breathes with the storm.
                float breathe = 1.0F + 0.03F * Mth.sin(nowSec * 0.045F + key);
                double ringR = bodyR * 1.18D * breathe;

                Vec3 dir = toStorm.scale(1.0D / dist);
                Vec3 upHint = Math.abs(dir.y) > 0.96D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
                Vec3 right = dir.cross(upHint).normalize();
                Vec3 up = right.cross(dir).normalize();

                float[] col = new float[3];
                StormPalettes.haloRingColor(col);
                final int r = Mth.clamp((int) (col[0] * 255.0F), 0, 255);
                final int g = Mth.clamp((int) (col[1] * 255.0F), 0, 255);
                final int b = Mth.clamp((int) (col[2] * 255.0F), 0, 255);
                float[] under = new float[3];
                StormPalettes.haloUnderColor(under);
                final int ur = Mth.clamp((int) (under[0] * 255.0F), 0, 255);
                final int ug = Mth.clamp((int) (under[1] * 255.0F), 0, 255);
                final int ub = Mth.clamp((int) (under[2] * 255.0F), 0, 255);

                final float master = rampIn * distFade;
                final Vec3 c = centre;
                final Vec3 rightV = right;
                final Vec3 upV = up;
                final double radius = ringR;
                final RenderType type = GlowRenderTypes.translucent(HALO);
                final double underR = ringR * 0.85D;
                final Vec3 underCentre = centre.subtract(up.scale(ringR * 0.45D));

                collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                    // The blue ring hugging the chassis silhouette.
                    disk(pose, consumer, c, rightV, upV, radius, r, g, b,
                            (int) (master * 165.0F), 0.72F, 0.17F);
                    // The pale under-glow beneath the body.
                    disk(pose, consumer, underCentre, rightV, upV, underR, ur, ug, ub,
                            (int) (master * 70.0F), 0.30F, 0.34F);
                });
            }
        } catch (Throwable ignored) {
            // Cosmetic pass only — never let the halo take down the level.
        }
    }

    /**
     * A camera-facing disk centred at {@code centre} in world space, with a
     * Gaussian ring falloff peaking at normalised radius {@code peak}
     * (sigma {@code sigma}) so the alpha reads as a hugging ring, not a card.
     */
    private static void disk(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 right, Vec3 up,
                             double radius, int r, int g, int b, int alpha, float peak, float sigma) {
        int n = 12;
        double hx = radius, hy = radius;
        for (int iy = 0; iy < n; iy++) {
            for (int ix = 0; ix < n; ix++) {
                quad(pose, consumer, centre, right, up, hx, hy,
                        ix / (float) n, iy / (float) n, (ix + 1) / (float) n, (iy + 1) / (float) n,
                        r, g, b, alpha, peak, sigma);
            }
        }
    }

    private static void quad(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 right, Vec3 up,
                             double hx, double hy, float u0, float v0, float u1, float v1,
                             int r, int g, int b, int alpha, float peak, float sigma) {
        vtx(pose, consumer, centre, right, up, hx, hy, u0, v1, r, g, b, alpha, peak, sigma);
        vtx(pose, consumer, centre, right, up, hx, hy, u1, v1, r, g, b, alpha, peak, sigma);
        vtx(pose, consumer, centre, right, up, hx, hy, u1, v0, r, g, b, alpha, peak, sigma);
        vtx(pose, consumer, centre, right, up, hx, hy, u0, v0, r, g, b, alpha, peak, sigma);
    }

    private static void vtx(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 right, Vec3 up,
                            double hx, double hy, float u, float v, int r, int g, int b, int alpha,
                            float peak, float sigma) {
        double rx = (u * 2.0D - 1.0D);
        double ry = (v * 2.0D - 1.0D);
        double q = Math.sqrt(rx * rx + ry * ry) / 1.4142135623730951D;
        double d = (q - peak) / sigma;
        double fall = Math.exp(-d * d);
        int a = Mth.clamp((int) (alpha * fall), 0, 255);
        Vec3 p = centre.add(right.scale(rx * hx)).add(up.scale(ry * hy));
        consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
