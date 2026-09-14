package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.world.phys.Vec3;

/**
 * Devouring Storms: The Point of No Return - the REAL rendered sky.
 *
 * A closed 360-degree rendered sky sphere around the camera - genuine 3D
 * geometry (a full UV sphere, not a backdrop card, not a half dome, and not
 * the seamed 6-face cube whose corner gaps showed a line). Textured with the
 * six exact Story Mode skies (1024x512 equirectangular, zero banding):
 *
 *   1 lavender day   2 midnight blue   3 sunset
 *   4 turquoise      5 purple          6 brown-purple witherstorm
 *
 *  - per STORM: the nearest tracked storm drives the sky, so a storm at
 *    phase 4 and one at phase 6 each show their own sky (nearest wins).
 *  - per PHASE: cross-fades between the phase skies, and fades back to the
 *    regular vanilla sky when the storm despawns or the toggle goes off.
 *  - the sky is attached to the storm: its palette follows the storm's
 *    phase and the Telltale black blur (McsmStormBlob) rides the storm
 *    itself, so everything moves as one element and never clips (the
 *    sphere is centred on the camera, so the camera is always 540 blocks
 *    from every point of it).
 *  - when the sphere is fully faded in, the vanilla sky pass is cancelled
 *    (McsmSkyPassGateMixin) so the base storm-darken/void tint cannot show
 *    through as a second layer - no giant void, no weird layer.
 *
 * Render plumbing: GlowRenderTypes.translucent - alpha-blended, textured,
 * no culling, NO FOG, depth-tested but no depth write, so terrain and the
 * real storm body always draw in front of the sky for free.
 */
public final class McsmSkybox {

    /** Sphere radius in blocks (the sky volume uses shells of 520-550). */
    private static final double HALF = 540.0D;

    private static final int LON = 48;
    private static final int LAT = 24;

    private McsmSkybox() {
    }

    private static Identifier tex(int set) {
        return Identifier.fromNamespaceAndPath("dabywitherstormmod",
                "textures/skybox/phase" + set + "_equirect.png");
    }

    /** Phase -> sky set: 1 day, 2 midnight, 3 sunset, 4 turquoise, 5 purple, 6 witherstorm. */
    private static int setForPhase(float phase) {
        if (phase < 2.0F) {
            return 1;
        }
        if (phase < 3.0F) {
            return 2;
        }
        if (phase < 4.0F) {
            return 3;
        }
        if (phase < 5.0F) {
            return 4;
        }
        if (phase < 6.0F) {
            return 5;
        }
        return 6;
    }

    // ---- cross-fade state --------------------------------------------------
    private static int setA = 0, setB = 0;
    private static float alphaA = 0.0F, alphaB = 0.0F;
    private static double lastTicks = -1.0D;

    /** True while the sphere fully covers the view - the sky pass gate uses
     *  this to cancel the vanilla sky (and the base storm-darken/void tint)
     *  so no second sky layer can show through or paint over the sphere. */
    private static boolean skyPassCancellable = false;

    public static boolean skyPassCancellable() {
        return skyPassCancellable && McsmExtrasConfig.skyPassCancel;
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            McsmExtrasConfig.load();
            Vec3 cam = ctx.levelState().cameraRenderState.pos;

            // nearest tracked storm drives the sky (per-storm, per-phase).
            // The position-packet manager only knows storms the server reports
            // (the distant ones); a commanded wither standing right in front of
            // the player is a real level entity and never appears in it, which
            // left the sky on the base storm-darken instead of the calm lavender
            // day sphere (Build #381: scan the level for real storm entities).
            double bestD = Double.MAX_VALUE;
            float bestPhase = -1.0F;
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                double dx = d.dispX - cam.x, dy = d.dispY - cam.y, dz = d.dispZ - cam.z;
                double dd = dx * dx + dy * dy + dz * dz;
                if (dd < bestD) {
                    bestD = dd;
                    bestPhase = d.phase;
                }
            }
            try {
                for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
                    if (e instanceof net.dabicco.witherstormmod.entity.WitherStormEntity ws) {
                        double dx = ws.getX() - cam.x, dy = ws.getY() - cam.y, dz = ws.getZ() - cam.z;
                        double dd = dx * dx + dy * dy + dz * dz;
                        if (dd < bestD) {
                            bestD = dd;
                            bestPhase = (float) ws.getPhase();
                        }
                    }
                }
            } catch (Throwable ignored) {
                // entity scan is best-effort; the packet manager still works
            }
            // Build #384 (user directive): early phases render the standard
            // vanilla day overworld STRICTLY by default - the sphere only
            // ignites from Phase 4 (teal haze shell) onward, with the
            // lavender/purple chaos skies locked to the later evolution.
            int target = (!McsmExtrasConfig.skyboxEnabled || bestPhase < 3.95F)
                    ? 0 : setForPhase(Mth.clamp(bestPhase, 4.0F, 9.0F));

            // start a new cross-fade when the target changes
            if (target != setB) {
                if (setB != 0) {
                    setA = setB;
                    alphaA = alphaB;
                } else if (setA == 0) {
                    alphaA = 0.0F;
                }
                setB = target;
                alphaB = 0.0F;
            }

            double ticks = mc.level.getGameTime();
            double dt = lastTicks < 0.0D ? 0.0D : Mth.clamp((ticks - lastTicks) / 20.0D, 0.0D, 0.25D);
            lastTicks = ticks;
            double rate = dt / Math.max(0.25D, McsmExtrasConfig.skyboxFadeSeconds);
            if (setB != 0) {
                alphaB = (float) Mth.clamp(alphaB + rate, 0.0D, 1.0D);
            }
            if (setA != 0 && setA != setB) {
                alphaA = (float) Math.max(0.0D, alphaA - rate);
                if (alphaA <= 0.004D) {
                    setA = 0;
                    alphaA = 0.0F;
                }
            } else if (setA == setB) {
                setA = 0;
                alphaA = 0.0F;
            }
            if (setB == 0 && alphaA <= 0.004F) {
                // fully back to the regular vanilla sky
                setA = 0;
                alphaA = 0.0F;
                setB = 0;
                alphaB = 0.0F;
                skyPassCancellable = false;
                return;
            }
            if (setA == setB && alphaB <= 0.004F && setB != 0) {
                skyPassCancellable = false;
                return; // nothing visible yet
            }

            // the sphere only fully hides the vanilla sky once its fade is done
            skyPassCancellable = setB != 0 && alphaB >= 0.995F;

            double half = HALF * Mth.clamp(McsmExtrasConfig.skyboxSize, 0.5D, 1.5D);
            PoseStack poseStack = ctx.poseStack();
            net.minecraft.client.renderer.SubmitNodeCollector collector = ctx.submitNodeCollector();

            if (setB != 0 && alphaB > 0.004F) {
                drawSphere(collector, poseStack, setB, alphaB, half, cam);
            }
            if (setA != 0 && setA != setB && alphaA > 0.004F) {
                drawSphere(collector, poseStack, setA, alphaA, half, cam);
            }
        } catch (Throwable ignored) {
            // a sky must never break a frame
        }
    }

    /** One closed UV sphere: 48x24 quads, poles converging to exact points,
     *  u = longitude, v = latitude (texture row 0 = zenith). The equirect
     *  texture is rotationally symmetric, so no yaw is needed and there is
     *  no seam anywhere on the sphere. */
    private static void drawSphere(net.minecraft.client.renderer.SubmitNodeCollector collector,
                                   PoseStack poseStack, int set, float alpha, double half, Vec3 cam) {
        int a = Mth.clamp((int) (alpha * 255.0F), 1, 255);
        final int fa = a;
        final double hx = half;
        final Vec3 camF = cam;
        final float invLon = 1.0F / LON;
        final float invLat = 1.0F / LAT;
        collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(tex(set)), (pose, consumer) -> {
            for (int iy = 0; iy < LAT; iy++) {
                double latTop = (0.5 - (iy + 0.0) * invLat) * Math.PI;
                double latBot = (0.5 - (iy + 1.0) * invLat) * Math.PI;
                float vTop = (iy + 0.0F) * invLat;
                float vBot = (iy + 1.0F) * invLat;
                for (int ix = 0; ix < LON; ix++) {
                    float uL = (ix + 0.0F) * invLon;
                    float uR = (ix + 1.0F) * invLon;
                    double lonL = (ix + 0.0) * invLon * (Math.PI * 2.0D);
                    double lonR = (ix + 1.0) * invLon * (Math.PI * 2.0D);
                    vertex(pose, consumer, camF, hx, latTop, lonL, uL, vTop, fa);
                    vertex(pose, consumer, camF, hx, latTop, lonR, uR, vTop, fa);
                    vertex(pose, consumer, camF, hx, latBot, lonR, uR, vBot, fa);
                    vertex(pose, consumer, camF, hx, latBot, lonL, uL, vBot, fa);
                }
            }
        });
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 cam,
                               double radius, double lat, double lon, float u, float v, int alpha) {
        double cl = Math.cos(lat);
        Vec3 p = cam.add(new Vec3(
                cl * Math.cos(lon) * radius,
                Math.sin(lat) * radius,
                cl * Math.sin(lon) * radius));
        consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

}
