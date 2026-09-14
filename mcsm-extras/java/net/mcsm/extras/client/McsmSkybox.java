package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.world.phys.Vec3;

/**
 * Build #374 -- the REAL Story Mode skybox: a textured CUBE around the
 * camera (explicitly NOT a dome). Six faces per phase, converted offline
 * from equirectangular Story Mode sky panoramas into seam-free cube faces
 * (jar-overrides/assets/dabywitherstormmod/textures/skybox/phaseN_*.png;
 * 12/12 face adjacencies verified to share identical physical edges).
 *
 *  - per STORM: the nearest tracked storm drives the sky, so a storm at
 *    phase 4 and one at phase 6 each show their own sky (nearest wins, the
 *    same rule as the sky volume).
 *  - per PHASE: 1 lavender day / 2 midnight blue / 3 sunset / 4 turquoise /
 *    5 purple / 6 brown-purple witherstorm. The storm eye sits at the
 *    front-centre of the sky: the cube yaws so its front face points at the
 *    storm, pitch-locked so the horizon stays level.
 *  - fade: cross-fades between phase skies, and fades back to the regular
 *    vanilla sky when the storm despawns or the toggle goes off.
 *  - vanilla variant: "MCSM Skybox" toggle OFF in the config console.
 *
 * Render plumbing is the exact proven pattern of McsmStormBlob.
 * submitSkyVolume (RenderTypes.entityTranslucentEmissive +
 * submitCustomGeometry + the ~540-block shell already inside the far clip).
 */
public final class McsmSkybox {

    /** Cube half-size in blocks (the sky volume uses shells of 520-550). */
    private static final double HALF = 540.0D;

    private static final String[] FACE_NAMES = {"nz", "px", "pz", "nx", "py", "ny"};

    private McsmSkybox() {
    }

    private static Identifier tex(int set, int face) {
        return Identifier.fromNamespaceAndPath("dabywitherstormmod",
                "textures/skybox/phase" + set + "_" + FACE_NAMES[face] + ".png");
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

    /** Face direction for corner (u, v in 0..1), cube-local.
     * Tangents derived so the outside panorama is continuous:
     * nz/px/pz/nx: v -> -Y; py: u -> +X, v -> +Z; ny: u -> +X, v -> -Z. */
    private static Vec3 faceDir(int face, float u, float v) {
        double ux = 2.0D * u - 1.0D, vy = 2.0D * v - 1.0D;
        switch (face) {
            case 0: return new Vec3(-ux, -vy, -1.0D).normalize();
            case 1: return new Vec3(1.0D, -vy, -ux).normalize();
            case 2: return new Vec3(ux, -vy, 1.0D).normalize();
            case 3: return new Vec3(-1.0D, -vy, ux).normalize();
            case 4: return new Vec3(ux, 1.0D, vy).normalize();
            default: return new Vec3(ux, -1.0D, -vy).normalize();
        }
    }

    // ---- cross-fade state --------------------------------------------------
    private static int setA = 0, setB = 0;
    private static float alphaA = 0.0F, alphaB = 0.0F;
    private static double lastTicks = -1.0D;

    public static void submit(LevelRenderContext ctx) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            McsmExtrasConfig.load();
            Vec3 cam = ctx.levelState().cameraRenderState.pos;

            // nearest tracked storm drives the sky (per-storm, per-phase)
            ClientDistantStormManager.StormData best = null;
            double bestD = Double.MAX_VALUE;
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                double dx = d.dispX - cam.x, dy = d.dispY - cam.y, dz = d.dispZ - cam.z;
                double dd = dx * dx + dy * dy + dz * dz;
                if (dd < bestD) {
                    bestD = dd;
                    best = d;
                }
            }
            int target = (!McsmExtrasConfig.skyboxEnabled || best == null)
                    ? 0 : setForPhase(Mth.clamp(best.phase, 1.0F, 9.0F));

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
                return;
            }
            if (setA == setB && alphaB <= 0.004F && setB != 0) {
                return; // nothing visible yet
            }

            // yaw the cube so its front face (the storm eye) points at the storm
            double yaw = 0.0D;
            if (best != null) {
                yaw = Math.atan2(-(best.dispX - cam.x), -(best.dispZ - cam.z));
            }
            double half = HALF * Mth.clamp(McsmExtrasConfig.skyboxSize, 0.5D, 1.5D);
            PoseStack poseStack = ctx.poseStack();

            if (setB != 0 && alphaB > 0.004F) {
                drawCube(ctx, poseStack, setB, alphaB, yaw, half, cam);
            }
            if (setA != 0 && setA != setB && alphaA > 0.004F) {
                drawCube(ctx, poseStack, setA, alphaA, yaw, half, cam);
            }
        } catch (Throwable ignored) {
            // a skybox must never break a frame
        }
    }

    private static void drawCube(LevelRenderContext ctx, PoseStack poseStack, int set, float alpha,
                                 double yaw, double half, Vec3 cam) {
        int a = Mth.clamp((int) (alpha * 255.0F), 1, 255);
        float cos = (float) Math.cos(yaw);
        float sin = (float) Math.sin(yaw);
        final int fa = a;
        final float cc = cos, cs = sin;
        final double hx = half;
        final Vec3 camF = cam;
        for (int f = 0; f < 6; f++) {
            final int ff = f;
            ctx.submitNodeCollector().submitCustomGeometry(poseStack,
                    RenderTypes.entityTranslucentEmissive(tex(set, ff)),
                    (pose, consumer) -> emitFace(pose, consumer, camF, cc, cs, hx, ff, fa));
        }
    }

    /** Emits the four corners of face f in the proven quad order:
     * (u,v) = (0,0) top-left, (1,0) top-right, (1,1) bottom-right, (0,1) bottom-left. */
    private static void emitFace(Pose pose, VertexConsumer consumer, Vec3 cam,
                                 float cos, float sin, double half, int face, int alpha) {
        float[][] corners = {{0.0F, 0.0F}, {1.0F, 0.0F}, {1.0F, 1.0F}, {0.0F, 1.0F}};
        float[][] uvs = {{0.0F, 0.0F}, {1.0F, 0.0F}, {1.0F, 1.0F}, {0.0F, 1.0F}};
        for (int i = 0; i < 4; i++) {
            Vec3 d = faceDir(face, corners[i][0], corners[i][1]);
            // yaw rotation about Y: x' = x cos + z sin ; z' = -x sin + z cos
            float x = (float) (d.x * cos + d.z * sin);
            float z = (float) (-d.x * sin + d.z * cos);
            Vec3 p = cam.add(new Vec3(x * half, d.y * half, z * half));
            consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                    .setColor(255, 255, 255, alpha)
                    .setUv(uvs[i][0], uvs[i][1])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880)
                    .setNormal(pose, 0.0F, 1.0F, 0.0F);
        }
    }
}
