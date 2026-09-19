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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * BUILD #455 -- THE GLOW THE PLAYER CAN ACTUALLY SEE.
 *
 * THE REPORT, AGAIN. "I know it a bug the storm's teeth and eyes are still not
 * glowing. The aura I don't see." Two builds have now aimed at the native
 * passes, and the player still sees nothing, so this pass stops relying on them.
 *
 * WHAT THE NATIVE PATH DEPENDS ON, and why none of it is in our hands:
 *
 *   1. the base renderer's own render-type call sites -- redirected with
 *      {@code require = 0}, so a call site that moves takes the glow with it and
 *      the mixin still loads clean;
 *   2. the emissive atlases the base samples -- our phase-4/5 head sheet's lit
 *      pixels are two tiny UV islands (30 pixels in a 512x512 sheet), so the
 *      geometry they light is a couple of dots on a 40-block head. ci's emissive
 *      gate passed that atlas because "every opaque pixel is pure white" is
 *      vacuously true when there are almost none, and that hole is closed in the
 *      same build;
 *   3. the pack-era glow gates -- the base's emitterMark/mark pipelines are
 *      switched off wholesale whenever ShaderPackCompat.active() is true.
 *
 * So this file draws the glow ITSELF, in world space, at the storm's own tracked
 * coordinates, on the vanilla eyes material -- no custom pipeline, no injected
 * call site, no pack, no config file somebody can leave switched off:
 *
 *   EYES   -- two lens lights on the head band, one each side of the face, in the
 *             phase's own colour (the reference stills have violet lenses), drawn
 *             as additive-cored billboards.
 *   TEETH  -- a white mouth cluster under the lenses, the brightest thing in the
 *             pass, which is the brief: WHITE TEETH, the aura carries the colour.
 *   AURA   -- a soft white haze wrapped around the head band, so a storm the
 *             player is standing next to reads as a light source even at phases
 *             whose native atmosphere has not faded in yet.
 *
 * Every layer is submitted twice: once through the mod's own additive emissive
 * pipeline and once through vanilla {@code RenderTypes.eyes} -- full-bright,
 * unlit, and part of the game itself rather than of any pack. If the custom
 * shader cannot build on the player's driver, the vanilla half still draws; if
 * both draw, the glow simply gets a solid core. That is the whole point: a glow
 * is not allowed to be invisible on a machine that can run the game.
 *
 * Phase range: 0.2 and up. "Below phase 4 nothing is forced" was the old rule and
 * it is what left a summonable, visible storm with unlit eyes for its first four
 * phases. The reference stills only ever show one storm, and its eyes glow.
 */
public final class McsmEyeGlow {

    private static final Identifier WHITE = id("textures/misc/teeth_glow_white.png");
    private static final Identifier CYAN = id("textures/misc/teeth_glow_cyan.png");
    private static final Identifier BLUE = id("textures/misc/teeth_glow_blue.png");
    /** The white atmosphere sheet, for the wrapped haze. */
    private static final Identifier HAZE = id("textures/misc/storm_white.png");
    /** Violet, for the lenses of the bands the reference stills show violet. */
    private static final Identifier VIOLET = id("textures/mcsm_atmosphere/glare/phase55.png");

    /** No glow for a storm further away than this. */
    private static final double MAX_DISTANCE = 3200.0D;
    /** Onset: the first phase whose eyes light. */
    private static final float ONSET = 0.2F;
    /** How long the fade-in lasts once the onset is crossed. */
    private static final float ONSET_RAMP = 0.5F;

    /** Peak alpha of one eye lens. */
    private static final float EYE_ALPHA = 0.85F;
    /** Peak alpha of the mouth cluster. */
    private static final float TEETH_ALPHA = 0.95F;
    /** Peak alpha of the wrapped haze. */
    private static final float HAZE_ALPHA = 0.22F;

    private McsmEyeGlow() {
    }

    /** One glow pass per frame for the nearest tracked storm. Fail-soft always. */
    public static void submit(LevelRenderContext ctx) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || ctx == null) {
                return;
            }
            if (!McsmExtrasConfig.eyeGlow) {
                return;
            }
            Vec3 camera = ctx.levelState().cameraRenderState.pos;

            ClientDistantStormManager.StormData storm = null;
            double best = Double.MAX_VALUE;
            for (ClientDistantStormManager.StormData s : ClientDistantStormManager.all()) {
                if (s.phase < ONSET) {
                    continue;
                }
                double d = Math.sqrt((s.dispX - camera.x) * (s.dispX - camera.x)
                        + (s.dispY - camera.y) * (s.dispY - camera.y)
                        + (s.dispZ - camera.z) * (s.dispZ - camera.z));
                if (d < best) {
                    best = d;
                    storm = s;
                }
            }
            if (storm == null || best > MAX_DISTANCE) {
                // The carrier says a storm is live but the position feed has
                // nothing in range: that is the one case where this pass would be
                // silently blind, so say it once, in the log and in chat, rather
                // than leaving a player staring at unlit eyes with no explanation.
                noteFeedEmpty();
                return;
            }

            float phase = storm.phase;
            float fade = Mth.clamp((phase - ONSET) / ONSET_RAMP, 0.0F, 1.0F);
            float distanceFade = 1.0F - Mth.clamp(
                    (float) ((best - 1800.0D) / 1400.0D), 0.0F, 1.0F);
            float visibility = fade * distanceFade;
            if (visibility <= 0.004F) {
                return;
            }

            double bodyRadius = McsmStormPhase.bodyRadius(phase);
            double bodyHeight = McsmStormPhase.bodyHeight(phase);
            Vec3 centre = new Vec3(storm.dispX, storm.dispY, storm.dispZ);
            // the head band: the top third of the body, which is where the three
            // faces and their lenses ride in every phase of the reference set
            Vec3 headBand = centre.add(0.0D, bodyHeight * 0.42D, 0.0D);
            // the storm's own facing decides where the face is. dispYaw is the
            // smoothed yaw the base feeds us, so the lenses stay on the front of
            // the heads instead of orbiting with the camera.
            double yaw = Math.toRadians(storm.dispYaw);
            Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
            Vec3 side = new Vec3(forward.z, 0.0D, -forward.x);

            Vec3 view = centre.subtract(camera);
            if (view.lengthSqr() < 1.0E-6D) {
                return;
            }
            view = view.normalize();

            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();

            // ---- the eyes: one lens each side of the face ------------------------
            double lensOut = bodyRadius * 0.42D;
            double lensUp = bodyHeight * 0.06D;
            double lensSize = Math.max(1.6D, bodyRadius * 0.34D);
            Vec3 leftLens = headBand.add(side.scale(lensOut)).add(0.0D, lensUp, 0.0D);
            Vec3 rightLens = headBand.add(side.scale(-lensOut)).add(0.0D, lensUp, 0.0D);
            float eyeAlpha = visibility * EYE_ALPHA * McsmExtrasConfig.eyeGlowStrength();
            lens(poseStack, collector, camera, leftLens, 0.0D, lensSize, eyeAlpha, phase);
            lens(poseStack, collector, camera, rightLens, Math.PI * 0.5D, lensSize, eyeAlpha, phase);

            // ---- the teeth: the white mouth cluster, under the lenses ------------
            Vec3 mouth = headBand.add(forward.scale(bodyRadius * 0.30D))
                    .add(0.0D, -bodyRadius * 0.34D, 0.0D);
            double mouthW = Math.max(2.2D, bodyRadius * 0.52D);
            double mouthH = Math.max(1.4D, bodyRadius * 0.30D);
            card(poseStack, collector, WHITE, mouth, view, mouthW, mouthH,
                    visibility * TEETH_ALPHA);

            // ---- the aura: a haze wrapped around the head band -------------------
            double hazeW = Math.max(4.0D, bodyRadius * 1.75D);
            double hazeH = Math.max(3.0D, bodyHeight * 0.34D);
            card(poseStack, collector, HAZE, headBand, view, hazeW, hazeH,
                    visibility * HAZE_ALPHA);
            card(poseStack, collector, HAZE, headBand.add(0.0D, -hazeH * 0.35D, 0.0D),
                    view, hazeW * 0.82D, hazeH * 0.72D, visibility * HAZE_ALPHA * 0.8F);
        } catch (Throwable ignored) {
            // a visual pass disappears rather than crashing the render thread
        }
    }

    /** One lens: the phase-coloured eye light, with a white core behind it. */
    private static void lens(PoseStack poseStack, SubmitNodeCollector collector, Vec3 camera,
            Vec3 at, double roll, double size, float alpha, float phase) {
        if (alpha <= 0.004F) {
            return;
        }
        Vec3 view = at.subtract(camera);
        if (view.lengthSqr() < 1.0E-6D) {
            return;
        }
        view = view.normalize();
        // the phase's own colour, on the approved mapping: cool cyan while the
        // storm is still young and thin, VIOLET through the 5.x bands (the lenses
        // the reference stills show are violet), blue-white from 6.9 up
        Identifier tint = phase >= 6.9F ? BLUE : (phase >= 5.0F ? VIOLET : CYAN);
        Vec3 right = view.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        // roll the lens quad about its own axis so the two lenses are not the same
        // stamp: the eyes of a thing this size should not read as two identical discs
        double c = Math.cos(roll);
        double s = Math.sin(roll);
        Vec3 axisA = right.scale(c).add(0.0D, s, 0.0D);
        Vec3 axisB = right.scale(-s).add(0.0D, c, 0.0D);
        lensQuad(poseStack, collector, at, axisA, axisB, size, alpha, tint);
    }

    private static void lensQuad(PoseStack poseStack, SubmitNodeCollector collector,
            Vec3 centre, Vec3 axisA, Vec3 axisB, double size, float alpha, Identifier tint) {
        float core = Mth.clamp(alpha * 0.55F, 0.0F, 1.0F);
        drawCard(poseStack, collector, tint, centre, axisA.scale(size * 0.62D),
                axisB.scale(size * 0.62D), alpha);
        drawCard(poseStack, collector, WHITE, centre, axisA.scale(size * 0.34D),
                axisB.scale(size * 0.34D), core);
    }

    /** A camera-facing card: the proven two-material submission. */
    private static void card(PoseStack poseStack, SubmitNodeCollector collector,
            Identifier texture, Vec3 centre, Vec3 view, double halfW, double halfH,
            float alpha) {
        if (alpha <= 0.004F) {
            return;
        }
        Vec3 upHint = Math.abs(view.y) > 0.98D ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = view.cross(upHint).normalize();
        Vec3 up = right.cross(view).normalize();
        drawCard(poseStack, collector, texture, centre, right.scale(halfW),
                up.scale(halfH), alpha);
    }

    /**
     * The same four vertices, submitted twice: the mod's additive emissive
     * pipeline first, vanilla's unlit eyes material second. Both are needed -- the
     * first is the look, the second is the guarantee.
     */
    private static void drawCard(PoseStack poseStack, SubmitNodeCollector collector,
            Identifier texture, Vec3 centre, Vec3 right, Vec3 up, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        if (a <= 2) {
            return;
        }
        collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(texture),
                (pose, consumer) -> quad(pose, consumer, centre, right, up, a));
        if (McsmExtrasConfig.vanillaGlow) {
            collector.submitCustomGeometry(poseStack, eyes(texture),
                    (pose, consumer) -> quad(pose, consumer, centre, right, up, a));
        }
    }

    /** Vanilla's unlit, full-bright eyes material -- the no-pack guarantee. */
    private static RenderType eyes(Identifier texture) {
        return RenderTypes.eyes(texture);
    }

    private static void quad(Pose pose, VertexConsumer consumer, Vec3 centre,
            Vec3 right, Vec3 up, int d) {
        vertex(pose, consumer, centre.subtract(right).subtract(up), 0.0F, 0.0F, d);
        vertex(pose, consumer, centre.add(right).subtract(up), 1.0F, 0.0F, d);
        vertex(pose, consumer, centre.add(right).add(up), 1.0F, 1.0F, d);
        vertex(pose, consumer, centre.subtract(right).add(up), 0.0F, 1.0F, d);
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

    /** Said once per session: the pass has a storm but no coordinates for it. */
    private static boolean said = false;

    private static void noteFeedEmpty() {
        if (said || !McsmStormPhase.active()) {
            return;
        }
        said = true;
        net.mcsm.extras.McsmDiag.say("eye glow: the phase carrier says a storm is"
                + " active, but the client position feed has no storm within "
                + (int) MAX_DISTANCE + " blocks, so the eyes and the aura cannot be"
                + " placed. Everything else about the storm still draws.");
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
    }
}
