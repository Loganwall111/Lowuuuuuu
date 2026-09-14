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
import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.world.phys.Vec3;

/**
 * Devouring Storms: mega-phase 2+5 - the storm blob, corrected and welded.
 *
 *  - phases 5.5-5.9 get the PINKISH-VIOLET blob; the reddish cast is gone;
 *  - the dark storm heart sits dead-centre in every phase from 4 up;
 *  - the centre direction is temporally smoothed (25% per frame) so blob
 *    and storm glide as one sky element;
 *  - the GLARE is the original game's construction, exposed by the reference
 *    frames: one soft gradient billboard hung BEHIND the silhouette (wide
 *    purple aura 5.5+, blue at 4-5), terrain occluding it for free - the old
 *    hard ring glare is deleted;
 *  - the MOUTHS are flat emissive squares over the body: cyan-white inner
 *    mouth, a zigzagged U-arc of tiny white dashed teeth, one magenta cube
 *    per emitter - exactly what the close-up frames show;
 *  - new for 5.5+: a PURPLE OVERLAY over the storm's face - an additive
 *    fringe hugging the silhouette plus a faint violet wash across the
 *    whole face, like a second silhouette layered on the creature.
 *
 * Every call is copied verbatim from the base mod's own compiled
 * StormBackdrop (verified 26.2 surface).
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
    private static final Identifier STORM_FACE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_face.png");
    // mega-phase 5c: the reference frames exposed how the original game
    // builds the glare - a plain soft gradient quad BEHIND the silhouette,
    // plus flat emissive squares for the mouth details. The old hard ring
    // glare is gone.
    private static final Identifier GLARE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_glare.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_white.png");

    /** The three beam mouths, in billboard units of baseR (x right, y up). */
    /**
     * The backdrop is a prop of the storm, not a camera shell.  It sits well
     * behind the entity along the entity-to-camera opposite direction and is
     * intentionally tiny in world space.
     */
    private static final double BACKDROP_DISTANCE = 1200.0D;
    private static final double DOME_RADIUS = 24.0D;

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
            submitSkyVolume(ctx);
        } catch (Throwable ignored) {
            // an unexpected base-jar surface degrades to no blob, never a crash
        }
    }

    /**
     * MCSM-style storm backdrop: a curved sky-volume/wash, not a flat card.
     * The reference frames read like a storm-bearing skybox layer: the colour
     * is locked to the direction of the storm, blacks out the horizon behind
     * it, and forms a broad foggy lobe above/behind the body.
     */
    private static void submitSkyVolume(LevelRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camera = ctx.levelState().cameraRenderState.pos;
        // One authoritative state/origin selection for the entire atmosphere.
        // The adapter calls state.getStormOrigin(); it never derives a second
        // centre from the camera, a head, or a billboard.
        ClientDistantStormManager.StormData state = McsmStormOrigin.nearest(camera);
        if (state == null || state.phase < 5.0F) return;

        Vec3 origin = McsmStormOrigin.getStormOrigin(state);
        Vec3 entityToCamera = camera.subtract(origin);
        double distance = entityToCamera.length();
        if (distance < 1.0D || distance > 2800.0D) return;
        Vec3 awayFromCamera = entityToCamera.scale(-1.0D / distance);
        float gt = (float) (mc.level.getGameTime() % 240000L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 backdrop = origin.add(awayFromCamera.scale(BACKDROP_DISTANCE))
                .add(sway(state.phase, gt * 0.05F, DOME_RADIUS));
        Vec3 facingCamera = camera.subtract(backdrop).normalize();
        if (facingCamera.lengthSqr() < 1.0E-4D) return;

        // The global colour tint is supplied by StormSkyDome. This single,
        // small geometric prop only supplies the cinematic distant silhouette.
        float phase = state.phase;
        float wPurple = ramp(phase, 5.0F, 5.35F);
        float wLate = ramp(phase, 5.5F, 5.95F);
        float wSix = ramp(phase, 5.95F, 6.20F);
        float r = 0.20F + 0.30F * wPurple + 0.12F * wLate;
        float g = 0.12F + 0.05F * (1.0F - wSix);
        float b = 0.30F + 0.34F * wPurple + 0.12F * wLate;
        float alpha = Mth.clamp(ramp(phase, 5.0F, 5.12F) * 0.82F + wLate * 0.12F, 0.0F, 0.94F);
        if (alpha <= 0.01F) return;

        final Vec3 centre = backdrop;
        final Vec3 normal = facingCamera;
        final float fr = r, fg = g, fb = b, fa = alpha;
        ctx.submitNodeCollector().submitCustomGeometry(ctx.poseStack(),
                RenderTypes.entityTranslucentEmissive(PURPLE),
                (pose, consumer) -> emitBackdropDome(pose, consumer, centre, normal,
                        DOME_RADIUS, fr, fg, fb, fa));
    }

    private static Vec3 sway(float phase, float timeSec, double bodyR) {
        if (phase < 4.0F || bodyR <= 0.0D) return Vec3.ZERO;
        float amp = (float)(bodyR * (0.025D + 0.020D * Mth.clamp((phase - 4.0F) / 3.0F, 0.0F, 1.0F)));
        return new Vec3(Mth.sin(timeSec * 0.20F) * amp,
                Mth.sin(timeSec * 0.11F) * amp * 0.16F,
                Mth.sin(timeSec * 0.16F + 1.3F) * amp * 0.45F);
    }

    private static void emitBackdropDome(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 normal,
            double radius, float r, float g, float b, float alpha) {
        Vec3 upHint = Math.abs(normal.y) > 0.96D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = normal.cross(upHint).normalize();
        Vec3 up = right.cross(normal).normalize();
        int sx = 8, sy = 6;
        for (int iy = 0; iy < sy; iy++) {
            for (int ix = 0; ix < sx; ix++) {
                backdropQuad(pose, consumer, centre, normal, right, up, radius,
                        ix / (float) sx, iy / (float) sy,
                        (ix + 1) / (float) sx, (iy + 1) / (float) sy,
                        r, g, b, alpha);
            }
        }
    }

    private static void backdropQuad(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 normal,
            Vec3 right, Vec3 up, double radius, float u0, float v0, float u1, float v1,
            float r, float g, float b, float alpha) {
        backdropVertex(pose, consumer, centre, normal, right, up, radius, u0, v1, r, g, b, alpha);
        backdropVertex(pose, consumer, centre, normal, right, up, radius, u1, v1, r, g, b, alpha);
        backdropVertex(pose, consumer, centre, normal, right, up, radius, u1, v0, r, g, b, alpha);
        backdropVertex(pose, consumer, centre, normal, right, up, radius, u0, v0, r, g, b, alpha);
    }

    private static void backdropVertex(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 normal,
            Vec3 right, Vec3 up, double radius, float u, float v,
            float r, float g, float b, float alpha) {
        double x = (u * 2.0D - 1.0D) * radius;
        double y = (v * 2.0D - 1.0D) * radius * 0.74D;
        double edge = Math.max(0.0D, 1.0D - (x * x + y * y) / (radius * radius));
        double bulge = Math.sqrt(edge) * radius * 0.18D;
        Vec3 point = centre.add(right.scale(x)).add(up.scale(y)).add(normal.scale(bulge));
        int a = Mth.clamp((int) (alpha * 255.0F * edge * edge), 0, 255);
        vertex(pose, consumer, point, u, v,
                Mth.clamp((int) (r * 255.0F), 0, 255),
                Mth.clamp((int) (g * 255.0F), 0, 255),
                Mth.clamp((int) (b * 255.0F), 0, 255), a);
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
