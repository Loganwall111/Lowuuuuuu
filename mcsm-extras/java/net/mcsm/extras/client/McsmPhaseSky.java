package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.world.phys.Vec3;

/**
 * MCSM storm glare — body-glued SOFT volume from mcsm_atmosphere/glare/*.
 *
 * Smooth multi-color gradient radial discs attached to the storm.
 * Smooth animation pulses and non-euclidean world-anchoring support.
 */
public final class McsmPhaseSky {

    private static final Identifier G4 = id("textures/mcsm_atmosphere/glare/phase4.png");
    private static final Identifier G5 = id("textures/mcsm_atmosphere/glare/phase5.png");
    private static final Identifier G54 = id("textures/mcsm_atmosphere/glare/phase54.png");
    private static final Identifier G55 = id("textures/mcsm_atmosphere/glare/phase55.png");
    private static final Identifier G6 = id("textures/mcsm_atmosphere/glare/phase6.png");
    private static final Identifier BLACK = id("textures/misc/backdrop_black.png");

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
    }

    private McsmPhaseSky() {
    }

    private static float ramp(float v, float lo, float hi) {
        if (hi <= lo) {
            return v >= hi ? 1.0F : 0.0F;
        }
        float t = Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static double bodyRadius(float phase) {
        if (phase < 4.0F) {
            return 4.0 + 1.5 * phase;
        } else if (phase < 5.0F) {
            return 10.0 + 8.0 * (phase - 4.0F);
        } else {
            return phase < 6.0F ? 18.0 + 22.0 * (phase - 5.0F) : 40.0 + 30.0 * (phase - 6.0F);
        }
    }

    public static Vec3 swayOffset(float phase, float timeSec, double bodyR) {
        if (phase < 4.0F || bodyR <= 0.0) {
            return Vec3.ZERO;
        }
        float amp = (float) (bodyR * (0.04 + 0.03 * Mth.clamp((phase - 4.0F) / 3.0F, 0.0F, 1.0F)));
        return new Vec3(
                Mth.sin(timeSec * 0.22F) * amp,
                Mth.sin(timeSec * 0.13F) * amp * 0.18F,
                Mth.sin(timeSec * 0.17F + 1.3F) * amp * 0.55F);
    }

    /** Pick glare texture for phase — matches user atmosphere pack. */
    private static Identifier glareTex(float phase) {
        if (phase >= 6.0F) {
            return G6;
        }
        if (phase >= 5.48F) {
            return G55; // 5.5-5.9 pink-lavender soft mass
        }
        if (phase >= 5.25F) {
            return G54; // 5.4 purple
        }
        if (phase >= 4.9F) {
            return G5; // 5.0 teal
        }
        return G4; // 4.x icy blue
    }

    public static void submit(LevelRenderContext ctx) {
        // BUILD #390 addenda -- the big purple ring is BACK. The 1.9.197
        // shutdown was right about the rectangular card artifacts but wrong
        // to kill the ring: the glare disc is centred on the storm and the
        // glow pipeline depth-tests (GREATER_THAN_OR_EQUAL, no depth write),
        // so the storm's own body silhouette occludes the disc centre and
        // what survives is exactly the halo ring hugging the silhouette --
        // the DS 7000.0.0 look from the reference shots.
        // The fix the user asked for instead of the shutdown: the giant
        // CIRCLES become the wide soft blobs of the last reference images --
        // every quad here is an ellipse (roughly 2:1), the old lateral circle
        // stack and the circular skirt are gone, and the additive-black core
        // plate (a no-op under additive blending, and an artifact source) is
        // dropped. try/ignored like every other overlay render hook.
        try {
            submitInner(ctx);
        } catch (Throwable ignored) {
            // overlay render pass must never take the frame down
        }
    }

    private static void submitInner(LevelRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || ClientDistantStormManager.all().isEmpty()) {
            return;
        }
        float gt = (float) (mc.level.getGameTime() % 240000L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float nowSec = gt * 0.05F;

        McsmExtrasConfig.load();
        double glareMul = Mth.clamp(McsmExtrasConfig.glareSize, 0.25, 3.05);
        double smudge = Mth.clamp(McsmExtrasConfig.smudgeScale, 0.15, 2.5);
        double glareAnimPhase = McsmExtrasConfig.glareAnimPhase;
        double bodyAnimPulse = McsmExtrasConfig.bodyAnimPulse;
        double glareAnimIntensity = McsmExtrasConfig.glareAnimIntensity;
        boolean glareNonEuclidean = McsmExtrasConfig.glareNonEuclidean;

        Vec3 cam = ctx.levelState().cameraRenderState.pos;
        PoseStack poseStack = ctx.poseStack();
        SubmitNodeCollector collector = ctx.submitNodeCollector();

        for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
            float phase = d.phase;
            if (phase < 3.95F) {
                continue;
            }
            Vec3 stormPos = new Vec3(d.dispX, d.dispY, d.dispZ);
            double bodyR = bodyRadius(phase);

            Vec3 centre = stormPos.add(swayOffset(phase, nowSec + (float) glareAnimPhase * 2.0F, bodyR));

            Vec3 toStorm = centre.subtract(cam);
            double dist = toStorm.length();
            if (dist < 1.0E-3) {
                continue;
            }

            Vec3 view = toStorm.scale(1.0 / dist);
            if (glareNonEuclidean) {
                // Non-euclidean: anchored in storm's fixed space
                view = new Vec3(0.0, 0.2, -1.0).normalize();
            }

            float distFade = 1.0F - Mth.clamp((float) ((dist - 1800.0) / 1200.0), 0.0F, 1.0F);
            if (distFade <= 0.01F) {
                continue;
            }

            float presence = ramp(phase, 3.95F, 4.25F);
            if (presence <= 0.01F) {
                continue;
            }

            // Smooth pulsation
            float pulse = 1.0F + 0.05F * (float) Mth.sin(nowSec * 2.4F) * (float) (1.0 + glareAnimIntensity);
            float bodyThrob = 1.0F + 0.08F * (float) Mth.sin(nowSec * 4.0F) * (float) Math.max(0.0, bodyAnimPulse);

            double baseR = bodyR * (1.55 + 1.05 * glareMul) * smudge * pulse * bodyThrob;
            if (phase > 5.3F) {
                baseR *= 1.0 + (phase - 5.3F) * 0.16;
            }
            if (phase >= 5.48F && phase < 6.0F) {
                baseR *= 1.35;
            }

            float amp = presence * distFade;
            float intenMult = (float) (1.0 + 0.4 * glareAnimIntensity);
            int aa = Mth.clamp((int) (amp * 210.0F * intenMult), 0, 255);
            if (aa <= 4) {
                continue;
            }

            Identifier tex = glareTex(phase);

            // BUILD #390 addenda -- blob-shaped glare stack (no circles):
            //  1. wide soft backdrop wash, the blurry 2:1 blob of the
            //     reference sheets, low alpha so the sky sheet reads through;
            //  2. the main glare ellipse -- the storm body occludes its
            //     centre, leaving the big phase-coloured ring;
            //  3. a wider, flatter skirt ellipse for the atmospheric bleed.
            quad(poseStack, collector, GlowRenderTypes.glow(tex),
                    centre, view, baseR * 2.15, baseR * 0.90, 255, 255, 255,
                    Mth.clamp((int) (aa * 0.50F), 0, 255));

            quad(poseStack, collector, GlowRenderTypes.glow(tex),
                    centre, view, baseR * 1.05, baseR * 0.98, 255, 255, 255, aa);

            int aa2 = Mth.clamp((int) (aa * 0.32F), 0, 255);
            if (aa2 > 4) {
                quad(poseStack, collector, GlowRenderTypes.glow(tex),
                        centre.add(view.scale(-bodyR * 0.08)), view,
                        baseR * 1.45, baseR * 0.70, 255, 255, 255, aa2);
            }
        }
    }

    private static void quad(PoseStack poseStack, SubmitNodeCollector collector,
            net.minecraft.client.renderer.rendertype.RenderType type,
            Vec3 at, Vec3 view, double radiusX, double radiusY, int r, int g, int b, int alpha) {
        if (alpha <= 2 || radiusX < 0.5 || radiusY < 0.5) {
            return;
        }
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
            Vec3 right = view.cross(upHint).normalize();
            Vec3 up = right.cross(view).normalize();
            Vec3 rx = right.scale(radiusX);
            Vec3 uy = up.scale(radiusY);
            int fa = Math.min(Math.max(alpha, 0), 255);
            vtx(pose, consumer, at.subtract(rx).subtract(uy), 0.0F, 1.0F, r, g, b, fa);
            vtx(pose, consumer, at.add(rx).subtract(uy), 1.0F, 1.0F, r, g, b, fa);
            vtx(pose, consumer, at.add(rx).add(uy), 1.0F, 0.0F, r, g, b, fa);
            vtx(pose, consumer, at.subtract(rx).add(uy), 0.0F, 0.0F, r, g, b, fa);
        });
    }

    private static void vtx(Pose pose, VertexConsumer consumer, Vec3 at,
            float u, float v, int r, int g, int b, int a) {
        consumer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
