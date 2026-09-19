package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.dabicco.witherstormmod.client.StormPresenceFX;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores the world-anchored presence effects that were temporarily disabled
 * while the attached oval was being repaired.
 *
 * The base pass supplies the sparks, pulse and original Catalyst halo. This
 * hook deliberately does not cancel it: it adds the phase-colored, enlarged
 * oval layers from the later 1.9.212/1.9.215 asset set. They are positioned at
 * the storm's real world coordinates rather than on a camera-wide sky card.
 */
@Mixin(StormPresenceFX.class)
public abstract class McsmPresenceFxPatch {
    private static final Identifier HALO_RING = id("textures/misc/halo_ring.png");
    private static final Identifier HALO_WHITE = id("textures/mcsm_atmosphere/halo.png");
    // BUILD #401 -- the accurate per-phase halo colours (sampled from the
    // reference frames and baked into the glare asset set): blue at 4,
    // sage-green at 5, purple 5.5-5.9, salmon at 6, ember at 7+. Same oval
    // geometry as before; only the colour layers change texture.
    private static final Identifier GLARE4 = id("textures/mcsm_atmosphere/glare/phase4.png");   // #5962D0
    private static final Identifier GLARE5 = id("textures/mcsm_atmosphere/glare/phase5.png");   // #6A9A78
    private static final Identifier GLARE54 = id("textures/mcsm_atmosphere/glare/phase54.png"); // #703887
    private static final Identifier GLARE55 = id("textures/mcsm_atmosphere/glare/phase55.png"); // #87529C
    private static final Identifier GLARE6 = id("textures/mcsm_atmosphere/glare/phase6.png");   // #D89874
    private static final Identifier GLARE89 = id("textures/mcsm_atmosphere/glare/phase89.png"); // #CE5A1F
    // BUILD #405 -- shader-less teeth/eye glow: additive radial billboards at
    // the mouth cluster so the glow reads even with NO shader pack installed
    // (emissive texture + additive blending + full-bright light, no bloom pass
    // required). Tinted per phase band like the teeth table.
    private static final Identifier GLOW_WHITE = id("textures/misc/teeth_glow_white.png");
    private static final Identifier GLOW_CYAN = id("textures/misc/teeth_glow_cyan.png");
    private static final Identifier GLOW_BLUE = id("textures/misc/teeth_glow_blue.png");

    @Inject(method = "submit", at = @At("HEAD"), remap = false, require = 0)
    private static void dabyws$restorePresenceFx(LevelRenderContext ctx, CallbackInfo ci) {
        try {
            // The user explicitly wants these layers back. Keep the old pass
            // enabled as well so its debris and pulse colors are not lost.
            // #409: the base pass's BLACK GLARE ring drew the giant dark disc
            // around the storm ("why is it in a circle") and the Cataclysm
            // halo pair drew the old purple ring artifact. Both stay OFF;
            // the phase-coloured glare layers below ARE the halo now.
            DabyWSClientConfig.cataclysmHalos = false;
            DabyWSClientConfig.blackGlare = false;
            // #410: the world-anchored STORM BACKDROP quad is the flat purple
            // sky card with the razor horizon edge and the dark core that
            // survived #409 -- it paints over the real sky. User directive:
            // regular sky, no bands. The phase glare layers supply the
            // atmosphere instead.
            DabyWSClientConfig.stormBackdropQuad = false;
            DabyWSClientConfig.stormBackdropBlack = false;
            // #412: two more base-mod layers that sat between the player and
            // the real picture. stormBackdrop (main flag) draws the flat
            // purple sky card whose bottom edge was the razor horizon line;
            // stormProximityVignette is a screen-space radial darkening
            // centred on the storm (it darkened terrain too) -- the "dark
            // disc". Both off; the reference stills have neither.
            DabyWSClientConfig.stormBackdrop = false;
            DabyWSClientConfig.stormProximityVignette = false;
            DabyWSClientConfig.atmospherePulse = true;
            DabyWSClientConfig.glareEjecta = true;
            submitExpandedHalos(ctx);
        } catch (Throwable ignored) {
            // A missing optional render surface must never break the client.
        }
    }

    private static void submitExpandedHalos(LevelRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || ctx == null) {
            return;
        }
        Vec3 camera = ctx.levelState().cameraRenderState.pos;
        PoseStack poseStack = ctx.poseStack();
        SubmitNodeCollector collector = ctx.submitNodeCollector();
        for (ClientDistantStormManager.StormData storm : ClientDistantStormManager.all()) {
            float phase = storm.phase;
            // BUILD #455 -- the eye/teeth glow starts as soon as there is a storm
            // to look at; only the big glare blobs still wait for the atmosphere
            // (they get their own fade below, so "below phase 4 nothing glows" is
            // no longer the reason a summoned storm has dark eyes).
            if (phase < 0.5F) {
                continue;
            }
            float auraFade = smoothstep(phase, 3.0F, 4.45F);
            Vec3 centre = new Vec3(storm.dispX, storm.dispY, storm.dispZ);
            Vec3 toStorm = centre.subtract(camera);
            double distance = toStorm.length();
            if (distance < 1.0D || distance > 2700.0D) {
                continue;
            }
            float distanceFade = 1.0F - smoothstep((float) distance, 2000.0F, 3200.0F);
            if (distanceFade <= 0.004F) {
                continue;
            }
            double bodyRadius = bodyRadius(phase);
            // Lift every phase's halo as a unit so its center sits over the
            // storm's crown instead of cutting across the middle of the body.
            // The lift grows with the phase because the later storm silhouette
            // is taller and wider.
            Vec3 haloCentre = centre.add(0.0D, haloLift(phase, bodyRadius), 0.0D);
            Vec3 view = haloCentre.subtract(camera).normalize();

            // BUILD #426 -- "the purple colour still renders on top of the
            // wither storm." It did, literally: every glare layer below is a
            // camera-facing card centred exactly ON the storm, so half of each
            // card is nearer to the player than the body and the purple is
            // painted over it. Each layer is 3.3 to 4.0 body radii across, so it
            // does not need to be in the body's plane to be seen: pushed a body
            // radius and a bit BEHIND the storm along the view direction, the
            // depth test hides whatever would cover the silhouette and the
            // oversized skin still rings it exactly as the reference stills do.
            Vec3 glareCentre = McsmExtrasConfig.glareBehindBody
                    ? haloCentre.subtract(view.scale(bodyRadius * 1.15D))
                    : haloCentre;

            // BUILD #401: accurate per-phase halo colours from the glare asset
            // set, cross-faded on the approved storm mapping (blue 4, green 5,
            // purple 5.5-5.9, salmon 6, ember 7+). Oval sizes/alphas keep the
            // established geometry; only the textures carry the phase colour.
            float w4 = 1.0F - smoothstep(phase, 4.95F, 5.15F);
            float w5 = smoothstep(phase, 4.95F, 5.15F)
                    * (1.0F - smoothstep(phase, 5.35F, 5.50F));
            float w54 = smoothstep(phase, 5.35F, 5.50F)
                    * (1.0F - smoothstep(phase, 5.50F, 5.62F));
            float w55 = smoothstep(phase, 5.50F, 5.62F)
                    * (1.0F - smoothstep(phase, 5.88F, 6.05F));
            float w6 = smoothstep(phase, 5.88F, 6.05F)
                    * (1.0F - smoothstep(phase, 6.90F, 7.10F));
            float w89 = smoothstep(phase, 6.90F, 7.10F);

            // BUILD #416 -- the giant glare circles are now drawn as CLOUD BLOBS.
            // Same six phase layers, same cross-fades, same textures -- but each one
            // is emitted as a small cluster of offset, rotated, anisotropically
            // scaled lobes that drift slowly instead of as one rigid oval card. The
            // textures themselves were re-baked as organic blobs by
            // ci/make_cloud_blobs.py (warped radius, no rim, bright core), so the
            // shape holds up whether the camera is 300 blocks away or 30.
            blobLayer(poseStack, collector, GLARE4, glareCentre, view,
                    bodyRadius * 3.35D, bodyRadius * 2.25D,
                    w4 * distanceFade * auraFade, 0);
            blobLayer(poseStack, collector, GLARE5, glareCentre, view,
                    bodyRadius * 3.55D, bodyRadius * 2.35D,
                    w5 * distanceFade * auraFade, 1);
            blobLayer(poseStack, collector, GLARE54, glareCentre, view,
                    bodyRadius * 3.70D, bodyRadius * 2.45D,
                    w54 * distanceFade * auraFade, 2);
            blobLayer(poseStack, collector, GLARE55, glareCentre, view,
                    bodyRadius * 3.85D, bodyRadius * 2.55D,
                    w55 * distanceFade * auraFade, 3);
            blobLayer(poseStack, collector, GLARE6, glareCentre, view,
                    bodyRadius * 4.05D, bodyRadius * 2.65D,
                    w6 * distanceFade * 0.95F * auraFade, 4);
            blobLayer(poseStack, collector, GLARE89, glareCentre, view,
                    bodyRadius * 4.05D, bodyRadius * 2.65D,
                    w89 * distanceFade * 0.95F * auraFade, 5);

            // The purple/pink oval ring is the older Catalyst Halo that was
            // present in the newer builds. Its width is intentionally larger
            // than the body's top silhouette, matching the supplied reference.
            float ring = smoothstep(phase, 5.18F, 5.48F);
            // Phase 5.5 is the broad rear-circle shot: enlarge the ring in
            // both axes until it clears and visually swallows the top of the
            // storm instead of reading as a small belt behind it.
            float phase55Circle = smoothstep(phase, 5.22F, 5.50F)
                    * (1.0F - smoothstep(phase, 5.70F, 5.96F));
            double ringWidth = bodyRadius * (4.35D + 3.05D * phase55Circle);
            double ringHeight = bodyRadius * (2.82D + 2.45D * phase55Circle);
            // #405: shader-less mouth glow (fake bloom via additive sprites)
            Vec3 mouth = haloCentre.add(0.0D, -bodyRadius * 0.95D, 0.0D);
            // BUILD #455 -- the mouth cluster is the TEETH, and the brief is white
            // teeth with the phase colour only in the hand-off bands. It is drawn
            // from phase 0.5 up now (weight 1 below the first cross-fade), with the
            // colour bands taking over as they arrive.
            float whiteW = 1.0F - smoothstep(phase, 4.95F, 5.15F);
            layer(poseStack, collector, GLOW_WHITE, mouth, view,
                    bodyRadius * 1.00D, bodyRadius * 0.75D,
                    whiteW * distanceFade * 0.60F);
            layer(poseStack, collector, GLOW_WHITE, mouth, view,
                    bodyRadius * 1.15D, bodyRadius * 0.85D,
                    (whiteW + w5) * distanceFade * 0.55F);
            layer(poseStack, collector, GLOW_CYAN, mouth, view,
                    bodyRadius * 1.15D, bodyRadius * 0.85D,
                    (w55 + w54 * 0.5F) * distanceFade * 0.55F);
            layer(poseStack, collector, GLOW_BLUE, mouth, view,
                    bodyRadius * 1.15D, bodyRadius * 0.85D,
                    (w6 + w89) * distanceFade * 0.55F);

            // #404: ring card retired -- it read as a concentric artifact
            // floating in the sky; the reference halos are pure soft glare.
            ringWidth = 0.0D; ringHeight = 0.0D; ring = 0.0F;
            // BUILD #416 -- the retained white under-halo went too. That oval was
            // the "flat bottom circle": a horizontal disc locked onto the lower
            // chassis layers, drawn only from 5.82 up, which read as a sticker
            // under the body and never followed the storm as it grew. The white
            // atmosphere is now an INDEPENDENT render path --
            // McsmHaloSkyRenderer's body-tethered conic light column, which is
            // tethered to the storm's own coordinates and whose radius, height and
            // spread angle are recomputed from what the body itself is (see the
            // growth weld in that class). Nothing white is drawn from this pass any
            // more, so there is exactly one white layer and it cannot double up.
        }
    }

    private static double haloLift(float phase, double bodyRadius) {
        float progression = Mth.clamp((phase - 4.45F) / 1.55F, 0.0F, 1.0F);
        return bodyRadius * (0.80D + 0.20D * progression);
    }

    private static double bodyRadius(float phase) {
        if (phase < 4.0F) {
            return 4.0D + phase * 1.5D;
        }
        if (phase < 5.0F) {
            return 10.0D + (phase - 4.0D) * 12.0D;
        }
        return 22.0D + Math.min(phase - 5.0F, 1.99F) * 9.0D;
    }

    /** Lobes per blob. Five offset lobes is enough to lose the oval silhouette. */
    private static final int BLOB_LOBES = 5;

    /**
     * BUILD #416 -- one glare layer as a soft, organic cloud blob.
     *
     * The oval assets are drawn through the translucent pipeline, so the shape the
     * player sees is texture x geometry. The textures no longer have a rim (see
     * ci/make_cloud_blobs.py), and this is the geometry half of that: the layer is
     * emitted as BLOB_LOBES lobes, each offset from the centre on its own angle,
     * each scaled differently and squashed anisotropically, all of it rotating
     * slowly with the world clock. A rigid circular card can no longer be read
     * anywhere in the result -- what is left is a drifting mass of soft lobes,
     * which is what a cloud around a storm should look like.
     *
     * The lobes are deliberately deterministic (no RNG): the same layer and lobe
     * always distort the same way, and only the shared slow roll moves, so the
     * atmosphere never boils or flickers.
     */
    private static void blobLayer(PoseStack poseStack, SubmitNodeCollector collector,
            Identifier texture, Vec3 centre, Vec3 view,
            double horizontalRadius, double verticalRadius, float alpha, int layerSeed) {
        if (alpha <= 0.004F) {
            return;
        }
        long ticks = 0L;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            ticks = mc.level.getGameTime();
        }
        // one shared slow roll for the whole blob, plus a per-layer phase offset
        double spin = ticks * 0.0045D + layerSeed * 1.7D;
        for (int lobe = 0; lobe < BLOB_LOBES; lobe++) {
            double angle = spin + lobe * (Math.PI * 2.0D / BLOB_LOBES) + layerSeed * 0.9D;
            // the centre lobe stays put and carries most of the light; the others
            // orbit it at a fraction of the radius with their own squash
            double orbit = lobe == 0 ? 0.0D
                    : 0.30D + 0.10D * (((lobe * 7 + layerSeed) % 5) / 4.0D);
            double sx = lobe == 0 ? 0.72D
                    : 0.52D - 0.07D * (((lobe * 3 + layerSeed) % 3));
            double sy = sx * (0.86D + 0.10D * (((lobe * 5 + layerSeed) % 4) / 3.0D));
            double breathe = 1.0D + 0.09D * Math.sin(ticks * 0.01D + lobe * 1.3D);
            float lobeAlpha = alpha * (lobe == 0 ? 0.52F : 0.22F);

            Vec3 upHint = Math.abs(view.y) > 0.98D
                    ? new Vec3(1.0D, 0.0D, 0.0D)
                    : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = view.cross(upHint).normalize();
            Vec3 up = right.cross(view).normalize();
            Vec3 offset = right.scale(Math.cos(angle) * orbit * horizontalRadius * 0.55D)
                    .add(up.scale(Math.sin(angle) * orbit * verticalRadius * 0.55D));

            quad(poseStack, collector, texture, centre.add(offset),
                    right.scale(horizontalRadius * sx * breathe),
                    up.scale(verticalRadius * sy * breathe), lobeAlpha);
        }
    }

    /** One textured, camera-facing quad with its basis already scaled. */
    private static void quad(PoseStack poseStack, SubmitNodeCollector collector,
            Identifier texture, Vec3 centre, Vec3 right, Vec3 up, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        if (a <= 2) {
            return;
        }
        RenderType type = texture.equals(HALO_WHITE)
                ? GlowRenderTypes.glow(texture)
                : GlowRenderTypes.translucent(texture);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            vertex(pose, consumer, centre.subtract(right).subtract(up), 0.0F, 0.0F, a);
            vertex(pose, consumer, centre.add(right).subtract(up), 1.0F, 0.0F, a);
            vertex(pose, consumer, centre.add(right).add(up), 1.0F, 1.0F, a);
            vertex(pose, consumer, centre.subtract(right).add(up), 0.0F, 1.0F, a);
        });
    }

    private static void layer(PoseStack poseStack, SubmitNodeCollector collector,
            Identifier texture, Vec3 centre, Vec3 view,
            double horizontalRadius, double verticalRadius, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        if (a <= 2) {
            return;
        }
        Vec3 upHint = Math.abs(view.y) > 0.98D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = view.cross(upHint).normalize().scale(horizontalRadius);
        Vec3 up = right.normalize().cross(view).normalize().scale(verticalRadius);
        // The backdrop PNGs carry the requested teal/purple/pink color, so
        // they use the normal translucent textured pipeline. The historical
        // halo assets use the emissive pipeline only for the white under-halo;
        // the purple ring stays textured so its violet/pink RGB is preserved.
        RenderType type = texture.equals(HALO_WHITE)
                ? GlowRenderTypes.glow(texture)
                : GlowRenderTypes.translucent(texture);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            vertex(pose, consumer, centre.subtract(right).subtract(up), 0.0F, 0.0F, a);
            vertex(pose, consumer, centre.add(right).subtract(up), 1.0F, 0.0F, a);
            vertex(pose, consumer, centre.add(right).add(up), 1.0F, 1.0F, a);
            vertex(pose, consumer, centre.subtract(right).add(up), 0.0F, 1.0F, a);
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

    private static float smoothstep(float value, float low, float high) {
        float t = Mth.clamp((value - low) / (high - low), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
    }
}
