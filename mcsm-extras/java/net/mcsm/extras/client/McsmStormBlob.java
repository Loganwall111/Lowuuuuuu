package net.mcsm.extras.client;

import java.util.HashMap;
import java.util.Map;

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

/**
 * Devouring Storms: mega-phase 2 - the storm blob, corrected.
 *
 * Re-submits the base mod's skybox billboard (sun/moon style: a quad pinned
 * at a fixed 220-block sky distance along the storm direction) with the
 * player's corrections:
 *
 *  - phases 5.5-5.9 get the PINKISH-VIOLET blob (purple_pink texture in a
 *    5.2-6.35 window) instead of the reddish/turquoise one;
 *  - the dark storm core (black quad) sits at the EXACT centre of the blob
 *    in every phase from 4 up, so the Wither Storm always looks welded to
 *    the middle of its blob instead of wandering inside it;
 *  - the centre direction is temporally smoothed, so blob and storm glide
 *    together like one skybox element (the Minecraft-sunset behaviour);
 *  - the red ember wash now starts later (6.5+) and weaker, killing the
 *    reddish cast the player flagged.
 *
 * Every call is copied verbatim from the base mod's own compiled
 * StormBackdrop (verified 26.2 surface): submitCustomGeometry, addVertex /
 * setColor / setUv / setOverlay / setLight / setNormal, Identifier,
 * Mth, Vec3.
 */
public final class McsmStormBlob {

    private static final Identifier BLUE4 = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_phase4_blue.png");
    private static final Identifier BLACK = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_black.png");
    private static final Identifier TURQUOISE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_turquoise.png");
    private static final Identifier PURPLE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_purple.png");
    private static final Identifier PURPLE_PINK = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_purple_pink.png");
    private static final Identifier EMBER = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_ember.png");

    private static final Map<Integer, Vec3> SMOOTH = new HashMap<>();

    private McsmStormBlob() {
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
            return 4.0F + 1.5F * phase;
        } else if (phase < 5.0F) {
            return 10.0F + 8.0F * (phase - 4.0F);
        } else {
            return phase < 6.0F ? 18.0F + 22.0F * (phase - 5.0F) : 40.0F + 30.0F * (phase - 6.0F);
        }
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            submitInner(ctx);
        } catch (Throwable ignored) {
            // an unexpected base-jar surface degrades to no blob, never a crash
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
        Vec3 cam = ctx.levelState().cameraRenderState.pos;
        PoseStack poseStack = ctx.poseStack();
        SubmitNodeCollector collector = ctx.submitNodeCollector();
        float master = 1.0F; // config-free: the corrected blob always runs
        int idx = 0;
        for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
            int key = idx++;
            float phase = d.phase;
            if (phase < 3.9F) {
                continue;
            }
            Vec3 centre = new Vec3(d.dispX, d.dispY, d.dispZ);
            Vec3 prev = SMOOTH.get(key);
            if (prev != null) {
                centre = prev.add(centre.subtract(prev).scale(0.12D));
            }
            SMOOTH.put(key, centre);
            Vec3 toStorm = centre.subtract(cam);
            double dist = toStorm.length();
            if (dist < 1.0E-4) {
                continue;
            }
            Vec3 view = toStorm.scale(1.0D / dist);
            float distFade = 1.0F - Mth.clamp((float) ((dist - 1200.0) / 900.0), 0.0F, 1.0F);
            if (distFade <= 0.004F) {
                continue;
            }
            double skyDist = 220.0D;
            Vec3 at = cam.add(view.scale(skyDist));
            double angular = Mth.clamp(bodyRadius(phase) / Math.max(dist, 1.0), 0.012, 0.85);
            double baseR = skyDist * angular * 1.5;
            if (phase > 5.5F) {
                baseR *= 1.0F + (phase - 5.5F) * 0.26F;
            }
            float breathe = 1.0F + 0.03F * Mth.sin(nowSec * 0.045F);
            baseR *= breathe;
            float a = master * distFade;

            // phase windows, corrected per the reference frames
            float wBlue = ramp(phase, 3.95F, 4.2F) * (1.0F - ramp(phase, 4.6F, 5.0F));
            float wTurq = ramp(phase, 4.45F, 4.9F) * (1.0F - ramp(phase, 5.2F, 5.5F));
            float wViolet = ramp(phase, 5.2F, 5.5F) * (1.0F - ramp(phase, 6.0F, 6.35F));
            float wPurp = ramp(phase, 6.0F, 6.35F);
            float wPink = ramp(phase, 6.3F, 7.0F);
            float wCore = ramp(phase, 4.0F, 4.3F); // dark heart, every phase

            if (wPink > 0.004F) {
                quad(poseStack, collector, GlowRenderTypes.translucent(PURPLE_PINK), at, view,
                        baseR * 1.55, 255, 205, 225, (int) (a * wPink * 245.0F));
            }
            if (wPurp > 0.004F) {
                quad(poseStack, collector, GlowRenderTypes.translucent(PURPLE), at, view,
                        baseR * 1.18, 236, 200, 255, (int) (a * wPurp * 250.0F));
            }
            if (wViolet > 0.004F) {
                quad(poseStack, collector, GlowRenderTypes.translucent(PURPLE_PINK), at, view,
                        baseR * 1.35, 255, 214, 236, (int) (a * wViolet * 240.0F));
            }
            if (wTurq > 0.004F) {
                quad(poseStack, collector, GlowRenderTypes.translucent(TURQUOISE), at, view,
                        baseR * 1.1, 255, 255, 255, (int) (a * wTurq * 250.0F));
            }
            // red ember: later and weaker - kills the reddish cast
            if (phase >= 6.5F) {
                quad(poseStack, collector, GlowRenderTypes.translucent(EMBER), at, view,
                        baseR * 1.34, 255, 255, 255,
                        (int) (a * 60.0F));
            }
            // the dark storm heart, dead centre of the blob, always
            if (wCore > 0.004F) {
                quad(poseStack, collector, GlowRenderTypes.translucent(BLACK), at, view,
                        baseR * 0.85, 255, 255, 255,
                        (int) (a * wCore * 235.0F));
            }
            if (wBlue > 0.004F) {
                quad(poseStack, collector, GlowRenderTypes.glow(BLUE4), at, view,
                        baseR * 0.95, 190, 215, 255,
                        (int) (a * wBlue * 235.0F));
            }
        }
    }

    private static void quad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
            Vec3 at, Vec3 view, double radius, int r, int g, int b, int alpha) {
        if (alpha <= 2) {
            return;
        }
        Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = view.cross(upHint).normalize();
        Vec3 up = right.cross(view).normalize();
        Vec3 rx = right.scale(radius * 1.15);
        Vec3 uy = up.scale(radius);
        int fa = Math.min(alpha, 255);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            vertex(pose, consumer, at.subtract(rx).subtract(uy), 0.0F, 1.0F, r, g, b, fa);
            vertex(pose, consumer, at.add(rx).subtract(uy), 1.0F, 1.0F, r, g, b, fa);
            vertex(pose, consumer, at.add(rx).add(uy), 1.0F, 0.0F, r, g, b, fa);
            vertex(pose, consumer, at.subtract(rx).add(uy), 0.0F, 0.0F, r, g, b, fa);
        });
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at,
            float u, float v, int r, int g, int b, int a) {
        consumer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
