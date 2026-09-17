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
 * BUILD #416 (D.8, phase 3) -- THE CREATOR: seven arms through the sky.
 *
 * THE MANDATE. Phase 3 of "build the never-made concepts from scratch" is the
 * creatures and the bosses, and the picture the user gave for the top of that
 * ladder is exact: <em>the Creator is a giant octopus, and its arms come down
 * through rips in the sky</em>.
 *
 * WHY THIS IS A RENDER PASS AND NOT AN ENTITY. The base mod's own grab tentacle
 * ({@code GrabTentacleEntity}) is a position marker with an EMPTY renderer --
 * its geometry is drawn by the storm renderer, from the storm's single private
 * {@code grabTentacle} field, so a second tentacle entity anywhere in the world
 * would be invisible. Adding a new entity type would mean a new model, a new
 * layer registration and a new renderer in the frozen base jar (not compiled
 * from this repository -- see the class comment on {@code McsmWhiteGlow}).
 * Neither is needed: the arms are PURE GEOMETRY in world space, submitted by the
 * same {@code LevelRenderContext} pass the light column and the vortex already
 * use, so they cost one pass, need no registry, and can never desync from the
 * entity they belong to -- they are functions of the storm's own live phase,
 * radius, height and scale, read from {@code ClientDistantStormManager} exactly
 * like the column is.
 *
 * WHAT IS DRAWN. One rip per arm: a vertical slit of light in the sky, opened
 * above the storm's shoulders. Out of each rip comes a tapered limb, built from
 * overlapping camera-facing bands so it reads as a soft volume from any angle
 * and never as a chain of flat cards. Every limb spirals outward, hangs down,
 * sways on its own sine phase, tapers to a point, and ends in a VIOLET TIP --
 * the same violet as the storm's eye lenses, which is the user's own colour
 * order. The whole manifestation fades in between phase {@link #ONSET} and
 * {@link #FULL} and fades out with distance, so there is never a hard switch.
 *
 * There is no dome here, no skybox, no JSON model and no GPU buffer kept between
 * frames: stateless geometry, fail-soft on every frame, exactly like the rest of
 * the atmosphere passes.
 */
public final class McsmCreatorArms {

    /** The soft falloff material the storm's own smudge uses (in the base jar). */
    private static final Identifier BODY = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_smudge.png");

    /** How many arms the Creator reaches down with. */
    private static final int ARMS = 7;
    /** Bands per arm. Overlapped by {@link #BAND_OVERLAP} so the limb is continuous. */
    private static final int SEGMENTS = 26;
    private static final double BAND_OVERLAP = 1.55D;
    /** Slits of light per rip. */
    private static final int RIFT_SLIVERS = 3;

    private static final double MAX_DISTANCE = 2600.0D;
    private static final float BAND_ALPHA = 0.34F;
    /** The manifestation onset: the arms start to reach down here ... */
    private static final float ONSET = 6.55F;
    /** ... and are fully out here. */
    private static final float FULL = 7.35F;

    /** The Creator's lens violet, the same colour family as the storm's eyes. */
    private static final float[] VIOLET = new float[]{0.62F, 0.34F, 1.00F};
    /** The rip itself reads as cold white light with violet bleed. */
    private static final float[] RIFT = new float[]{0.78F, 0.64F, 1.00F};

    private McsmCreatorArms() {
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.creatorArms) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || ctx == null) {
                return;
            }

            Vec3 camera = ctx.levelState().cameraRenderState.pos;
            ClientDistantStormManager.StormData storm = nearestStorm(camera);
            if (storm == null) {
                return;
            }

            float phase = storm.phase;
            float manifest = ramp(phase, ONSET, FULL);
            if (manifest <= 0.01F) {
                return;
            }

            Vec3 centre = new Vec3(storm.dispX, storm.dispY, storm.dispZ);
            double distance = camera.distanceTo(centre);
            if (distance > MAX_DISTANCE) {
                return;
            }
            float distanceFade = 1.0F - Mth.clamp(
                    (float) ((distance - 1400.0D) / 1200.0D), 0.0F, 1.0F);
            float visibility = manifest * distanceFade;
            if (visibility <= 0.004F) {
                return;
            }

            // ---- the growth weld ------------------------------------------------
            // Nothing here is a constant that could drift away from the body: the
            // mouth of every rip, the length of every arm and the thickness of
            // every band come from the same phase numbers the storm's own radius
            // and height come from.
            double time = (double) (mc.level.getGameTime() % 24000L);
            double scale = McsmStormPhase.scaleMultiplier(phase, storm.activeHeads);
            double bodyRadius = McsmStormPhase.bodyRadius(phase);
            double bodyHeight = McsmStormPhase.bodyHeight(phase) * scale;
            double mouthRadius = Math.min(900.0D, bodyRadius * 1.05D);
            double mountY = bodyHeight * 0.86D;
            double hang = bodyHeight * 0.55D + Math.min(2600.0D, bodyRadius * 1.35D);
            double reach = Math.min(2600.0D, bodyRadius * 2.45D * (0.55D + 0.45D * (double) manifest));
            float spin = (float) (time * 0.0016D) + storm.dispYaw;

            // ---- the shared billboard basis -------------------------------------
            Vec3 toCamera = camera.subtract(centre);
            Vec3 view = toCamera.scale(1.0D / Math.max(1.0E-4D, distance));
            Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = new Vec3(view.z, 0.0D, -view.x);
            if (right.lengthSqr() < 1.0E-6D) {
                right = new Vec3(1.0D, 0.0D, 0.0D);
            }
            right = right.normalize();

            // Config is read on the client tick; never touch files in this pass.
            float armAlpha = BAND_ALPHA * Mth.clamp(
                    (float) McsmExtrasConfig.creatorArmScale, 0.5F, 2.0F);
            float[] tint = armColour(phase, time);

            RenderType material = GlowRenderTypes.glow(BODY);
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();

            final Vec3 origin = centre;
            final Vec3 r = right;
            final Vec3 u = up;
            final float[] colour = tint;
            final double mouth = mouthRadius;
            final double mount = mountY;
            final double drop = hang;
            final double armReach = reach;
            final float sway = spin;
            final float vis = visibility;
            final float alpha = armAlpha;
            final double t = time;

            collector.submitCustomGeometry(poseStack, material,
                    (pose, consumer) -> {
                        for (int arm = 0; arm < ARMS; arm++) {
                            emitArm(pose, consumer, origin, r, u, arm, sway, colour, vis, alpha,
                                    mouth, mount, drop, armReach, bodyRadius, t);
                        }
                    });
        } catch (Throwable ignored) {
            // A visual pass must vanish rather than crash the render thread.
        }
    }

    // ---------------------------------------------------------------------
    // One arm: a rip, then the limb hanging out of it
    // ---------------------------------------------------------------------

    private static void emitArm(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 right, Vec3 up,
            int arm, float spin, float[] colour, float visibility, float armAlpha,
            double mouthRadius, double mountY, double hang, double reach,
            double bodyRadius, double time) {

        double baseAngle = ((double) arm / (double) ARMS) * Math.PI * 2.0D + (double) spin;
        double mouthX = centre.x + Math.cos(baseAngle) * mouthRadius;
        double mouthZ = centre.z + Math.sin(baseAngle) * mouthRadius;
        double mouthY = centre.y + mountY;
        Vec3 mouth = new Vec3(mouthX, mouthY, mouthZ);

        // The rip: a slit of white-violet light in the sky the limb comes through.
        float ripPulse = 0.72F + 0.28F * (float) Math.sin(time * 0.07D + (double) arm * 1.9D);
        emitRift(pose, consumer, mouth, right, up,
                visibility * ripPulse, 0.85F + 0.55F * visibility,
                Math.max(6.0D, bodyRadius * 0.32D));

        double thick0 = Math.max(3.4D, bodyRadius * 0.20D);
        double step = hang / (double) SEGMENTS;
        double half = step * BAND_OVERLAP * 0.5D;
        Vec3 tip = mouth;

        for (int i = 0; i < SEGMENTS; i++) {
            double u = (double) i / (double) (SEGMENTS - 1);

            double angle = baseAngle + u * 2.35D
                    + Math.sin(time * 0.013D + (double) arm * 1.7D) * 0.18D * u;
            double radius = mouthRadius + reach * (0.18D + 0.92D * u)
                    * (0.92D + 0.08D * Math.sin(time * 0.021D + (double) i * 0.42D));
            double y = mouthY - hang * Math.pow(u, 1.35D)
                    + Math.sin(u * Math.PI * 3.0D + time * 0.03D + (double) arm)
                        * bodyRadius * 0.05D * u;
            double sway = Math.sin(time * 0.019D + u * 4.2D + (double) arm * 2.1D) * 0.34D;
            double px = centre.x + Math.cos(angle + sway) * radius;
            double pz = centre.z + Math.sin(angle + sway) * radius;

            Vec3 at = new Vec3(px, centre.y + y, pz);
            tip = at;

            // A limb is thin at the rip, thickest through the first third and a
            // point at the tip -- the octopus read, not a rope.
            double profile = Math.sin(Math.PI * (0.22D + 0.78D * (1.0D - u)));
            double thick = Math.max(0.55D, thick0 * (0.35D + 0.65D * profile) * (1.0D - 0.72D * u));
            float bandAlpha = armAlpha * visibility * (1.0F - 0.35F * (float) u);

            put(pose, consumer, at, right, up, thick * 2.05D, Math.max(half, thick * 1.20D),
                    colour, bandAlpha, (float) (0.10D + 0.55D * u));
        }

        // The violet tip: what the user actually sees when an arm reaches down.
        double tipScale = Math.max(2.4D, thick0 * 0.85D);
        float tipAlpha = armAlpha * visibility * 1.35F;
        put(pose, consumer, tip, right, up, tipScale * 2.6D, tipScale * 2.6D,
                VIOLET, tipAlpha, 0.05F);
        put(pose, consumer, tip, right, up, tipScale * 1.25D, tipScale * 1.25D,
                VIOLET, Math.min(1.0F, tipAlpha * 1.45F), 0.05F);
    }

    /**
     * The rip itself: vertical slits of light, longest through the middle, so the
     * sky looks torn open rather than stamped.
     */
    private static void emitRift(Pose pose, VertexConsumer consumer, Vec3 at, Vec3 right, Vec3 up,
            float visibility, float scale, double size) {
        for (int s = 0; s < RIFT_SLIVERS; s++) {
            double length = size * (2.6D - 0.55D * (double) s);
            double wide = (1.05D + 0.55D * (double) s) * (double) scale;
            double off = ((double) s - (double) (RIFT_SLIVERS - 1) * 0.5D) * (size * 0.36D);
            put(pose, consumer, at.add(right.scale(off)), right, up, wide, length,
                    RIFT, visibility * (0.68F - 0.16F * (float) s), 0.0F);
        }
    }

    // ---------------------------------------------------------------------
    // Geometry emitters -- the same camera-facing band idiom as the light column
    // ---------------------------------------------------------------------

    private static void put(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 right, Vec3 up,
            double halfWidth, double halfHeight, float[] rgb, float alpha, float vOffset) {
        if (alpha <= 0.004F || halfWidth <= 0.01D || halfHeight <= 0.01D) {
            return;
        }
        int r = Mth.clamp((int) (rgb[0] * 255.0F), 0, 255);
        int g = Mth.clamp((int) (rgb[1] * 255.0F), 0, 255);
        int b = Mth.clamp((int) (rgb[2] * 255.0F), 0, 255);
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        float v0 = Mth.clamp(0.04F + vOffset, 0.0F, 0.92F);
        float v1 = Mth.clamp(0.10F + vOffset, 0.0F, 0.97F);
        vertex(pose, consumer, centre, right, up, -halfWidth, -halfHeight, 0.06F, v0, r, g, b, a);
        vertex(pose, consumer, centre, right, up, halfWidth, -halfHeight, 0.94F, v0, r, g, b, a);
        vertex(pose, consumer, centre, right, up, halfWidth, halfHeight, 0.94F, v1, r, g, b, a);
        vertex(pose, consumer, centre, right, up, -halfWidth, halfHeight, 0.06F, v1, r, g, b, a);
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 right, Vec3 up,
            double dx, double dy, float u, float v, int r, int g, int b, int a) {
        Vec3 p = centre.add(right.scale(dx)).add(up.scale(dy));
        consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * The arm colour: the storm's own live column colour, pulled darker and
     * pushed toward the Creator's violet, so the limbs belong to the same
     * mythology as the body they grow out of.
     */
    private static float[] armColour(float phase, double time) {
        float[] live = null;
        try {
            live = McsmStormPhase.columnFor(phase, (float) (time * 0.5D));
        } catch (Throwable ignored) {
            live = null;
        }
        if (live == null || live.length < 3) {
            live = new float[]{0.34F, 0.20F, 0.62F};
        }
        return new float[]{
            Mth.clamp(live[0] * 0.70F + 0.09F, 0.0F, 1.0F),
            Mth.clamp(live[1] * 0.58F + 0.04F, 0.0F, 1.0F),
            Mth.clamp(live[2] * 0.86F + 0.26F, 0.0F, 1.0F)};
    }

    private static ClientDistantStormManager.StormData nearestStorm(Vec3 camera) {
        ClientDistantStormManager.StormData best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ClientDistantStormManager.StormData storm : ClientDistantStormManager.all()) {
            if (storm.phase < ONSET) {
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

    private static float ramp(float v, float lo, float hi) {
        if (hi <= lo) {
            return v >= hi ? 1.0F : 0.0F;
        }
        float t = Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
