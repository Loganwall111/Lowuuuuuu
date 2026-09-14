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

    @Inject(method = "submit", at = @At("HEAD"), remap = false, require = 0)
    private static void dabyws$restorePresenceFx(LevelRenderContext ctx, CallbackInfo ci) {
        try {
            // The user explicitly wants these layers back. Keep the old pass
            // enabled as well so its debris and pulse colors are not lost.
            DabyWSClientConfig.cataclysmHalos = true;
            DabyWSClientConfig.blackGlare = true;
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
            if (phase < 4.45F) {
                continue;
            }
            Vec3 centre = new Vec3(storm.dispX, storm.dispY, storm.dispZ);
            Vec3 toStorm = centre.subtract(camera);
            double distance = toStorm.length();
            if (distance < 1.0D || distance > 2700.0D) {
                continue;
            }
            float distanceFade = 1.0F - smoothstep((float) distance, 1700.0F, 2700.0F);
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

            layer(poseStack, collector, GLARE4, haloCentre, view,
                    bodyRadius * 3.35D, bodyRadius * 2.25D,
                    w4 * distanceFade * 1.0F);
            layer(poseStack, collector, GLARE5, haloCentre, view,
                    bodyRadius * 3.55D, bodyRadius * 2.35D,
                    w5 * distanceFade * 1.0F);
            layer(poseStack, collector, GLARE54, haloCentre, view,
                    bodyRadius * 3.70D, bodyRadius * 2.45D,
                    w54 * distanceFade * 1.0F);
            layer(poseStack, collector, GLARE55, haloCentre, view,
                    bodyRadius * 3.85D, bodyRadius * 2.55D,
                    w55 * distanceFade * 1.0F);
            layer(poseStack, collector, GLARE6, haloCentre, view,
                    bodyRadius * 4.05D, bodyRadius * 2.65D,
                    w6 * distanceFade * 0.95F);
            layer(poseStack, collector, GLARE89, haloCentre, view,
                    bodyRadius * 4.05D, bodyRadius * 2.65D,
                    w89 * distanceFade * 0.95F);

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
            layer(poseStack, collector, HALO_RING, haloCentre, view,
                    ringWidth, ringHeight,
                    ring * distanceFade * 0.95F);
            if (phase >= 5.82F) {
                layer(poseStack, collector, HALO_RING, haloCentre, view,
                        bodyRadius * 3.05D, bodyRadius * 1.98D,
                        smoothstep(phase, 5.82F, 6.12F) * distanceFade * 0.55F);
                // This is the retained white under-halo from the newer asset
                // set. The texture is black outside its luminous shape, so it
                // is submitted through the additive glow pipeline rather than
                // as a translucent dark card.
                Vec3 under = haloCentre.add(0.0D, -bodyRadius * 0.38D, 0.0D);
                layer(poseStack, collector, HALO_WHITE, under, view,
                        bodyRadius * 3.25D, bodyRadius * 2.80D,
                        smoothstep(phase, 5.82F, 6.18F) * distanceFade * 0.45F);
            }
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
