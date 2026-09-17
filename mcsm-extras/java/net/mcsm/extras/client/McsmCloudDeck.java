package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmAdams;
import net.mcsm.extras.McsmCreatorRealm;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmIdentity;
import net.mcsm.extras.McsmReality;
import net.mcsm.extras.McsmVoid;

/**
 * BUILD #470 -- THE REAL CLOUD DECK.
 *
 * <p>Part of the same standing report as the painted cube: "custom skyboxes", "the
 * sky is not fully the sky yet". The cube (#457) is the AIR -- four walls and a lid,
 * one painting per dimension. Air without weather is still half a sky: the vanilla
 * cloud plane is one flat sheet, one colour, at one height, identical in every world,
 * and it is the thing a player looks at when they look up.
 *
 * <p>So this is the deck. THREE layers, stacked above the eye, each made of a grid of
 * quads (never one big sheet, so the deck has a surface rather than a lid), each
 * drifting at its own speed and rippling as it goes, textured with the dimension's own
 * cloud painting ({@code ci/make_skybox_textures.py} -- the same seeds as its cube, so a
 * world's sky and its weather belong to the same painting) and lit by that dimension's
 * own glow colour.
 *
 * <p>The deck is part of the painted sky, so it answers to the same switch: with
 * "Painted skies" off, the vanilla sky and its vanilla clouds are untouched.
 *
 * <p>Drawn in the world out of ordinary geometry: it exists for a player with no
 * shader pack at all, which is the rule this whole sky system was built on. Draw-only,
 * bounded (3 x 4 x 4 quads), and wrapped -- a sky that throws is worse than a sky that
 * is vanilla.
 */
public final class McsmCloudDeck {

    /**
     * V2 EPIC - The decks, as a distance above the eye. 6 layers now for volumetric depth.
     * Lowest is weather player standing in, middle decks, top wisps + storm veil.
     */
    private static final double[] HEIGHT = {18.0D, 28.0D, 38.0D, 52.0D, 72.0D, 96.0D};

    /** How far each deck reaches (half-width) -- inside the cube's own 256. */
    private static final double SPAN = 320.0D;

    /** Quads per side of each deck. 8x8 now = 64 quads per layer for epic surface. */
    private static final int GRID = 8;

    /** Blocks per tile of the painting, so a cloud is a cloud's size, not a pixel's. */
    private static final double TILE = 48.0D;

    /** How far each layer's painting travels per tick - more varied. */
    private static final double[] DRIFT = {0.0018D, 0.0028D, 0.0035D, 0.0048D, 0.0062D, 0.0080D};

    /** How much of each painting's own alpha each layer keeps. */
    private static final int[] ALPHA = {200, 180, 150, 120, 85, 50};

    /** How hard each layer ripples (in blocks), so the deck is not a flat plane. */
    private static final double[] WAVE = {4.2D, 3.4D, 2.6D, 1.8D, 1.2D, 0.8D};

    private McsmCloudDeck() {
    }

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath("mcsm", "textures/sky/" + name + ".png");
    }

    private static final Identifier DECAYED = tex("clouds_decayed");
    private static final Identifier ADAMS = tex("clouds_adams");
    private static final Identifier VOID = tex("clouds_void");
    private static final Identifier CREATOR = tex("clouds_creator");

    /** Which painting a world's weather is made of. Null: the world keeps its own. */
    private static Identifier sheetFor(ClientLevel level) {
        try {
            if (McsmReality.inside(level)) {
                return DECAYED;
            }
            if (level.dimension().equals(McsmAdams.ADAMS)) {
                return ADAMS;
            }
            if (level.dimension().equals(McsmCreatorRealm.DIMENSION)) {
                return CREATOR;
            }
            if (level.dimension().equals(McsmVoid.DIMENSION)) {
                return VOID;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /** One dimension's own light, caught in its clouds. */
    private static String glowFor(ClientLevel level) {
        try {
            McsmIdentity.Skin s;
            if (McsmReality.inside(level)) {
                s = McsmIdentity.skin(McsmIdentity.DECAYED);
            } else if (level.dimension().equals(McsmAdams.ADAMS)) {
                s = McsmIdentity.skin(McsmIdentity.ADAMS);
            } else if (level.dimension().equals(McsmCreatorRealm.DIMENSION)) {
                s = McsmIdentity.skin(McsmIdentity.CREATOR);
            } else {
                s = McsmIdentity.skin(McsmIdentity.VOID);
            }
            return s == null ? null : s.glow();
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.paintedSky || ctx == null) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) {
                return;
            }
            final Identifier sheet = sheetFor(level);
            if (sheet == null) {
                return;
            }
            float[] glow = McsmIdentity.rgb(glowFor(level));
            if (glow == null) {
                glow = new float[]{0.55F, 0.36F, 1.0F};
            }
            final int cr = clamp255(glow[0] * 255.0F * 0.45F + 255.0F * 0.55F);
            final int cg = clamp255(glow[1] * 255.0F * 0.45F + 255.0F * 0.55F);
            final int cb = clamp255(glow[2] * 255.0F * 0.45F + 255.0F * 0.55F);

            final Vec3 cam = ctx.levelState().cameraRenderState.pos;
            final double clock = level.getGameTime() % 200000L;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            final double step = SPAN * 2.0D / GRID;

            for (int layer = 0; layer < HEIGHT.length; layer++) {
                final int li = layer;
                final double y = cam.y + HEIGHT[li];
                final double u0 = clock * DRIFT[li];
                final double v0 = clock * DRIFT[li] * 0.37D;
                final int alpha = ALPHA[li];
                final double wave = WAVE[li];
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(sheet),
                        (pose, consumer) -> {
                            for (int gx = 0; gx < GRID; gx++) {
                                for (int gz = 0; gz < GRID; gz++) {
                                    double x0 = cam.x - SPAN + gx * step;
                                    double x1 = x0 + step;
                                    double z0 = cam.z - SPAN + gz * step;
                                    double z1 = z0 + step;
                                    double y00 = y + ripple(x0, z0, clock, wave, li);
                                    double y10 = y + ripple(x1, z0, clock, wave, li);
                                    double y11 = y + ripple(x1, z1, clock, wave, li);
                                    double y01 = y + ripple(x0, z1, clock, wave, li);
                                    float uu0 = (float) (x0 / TILE + u0);
                                    float uu1 = (float) (x1 / TILE + u0);
                                    float vv0 = (float) (z0 / TILE + v0);
                                    float vv1 = (float) (z1 / TILE + v0);
                                    quad(pose, consumer,
                                            x0, y00, z0, uu0, vv0,
                                            x1, y10, z0, uu1, vv0,
                                            x1, y11, z1, uu1, vv1,
                                            x0, y01, z1, uu0, vv1,
                                            cr, cg, cb, alpha, 0.0F, 1.0F, 0.0F);
                                    // V2 VOLUMETRIC: add vertical sides for thickness - makes cloud a volume not a sheet
                                    if (li < HEIGHT.length - 1) {
                                        double nextY = cam.y + HEIGHT[li + 1];
                                        double y00n = nextY + ripple(x0, z0, clock, WAVE[li + 1], li + 1);
                                        double y10n = nextY + ripple(x1, z0, clock, WAVE[li + 1], li + 1);
                                        // side walls for volumetric thickness
                                        int sideAlpha = alpha / 3;
                                        // east wall
                                        quad(pose, consumer,
                                                x1, y10, z0, uu1, vv0,
                                                x1, y10n, z0, uu1, vv0,
                                                x1, y11, z1, uu1, vv1,
                                                x1, y11 - (y11 - y10), z1, uu1, vv1,
                                                cr, cg, cb, sideAlpha, 1.0F, 0.0F, 0.0F);
                                        // north wall
                                        quad(pose, consumer,
                                                x0, y00, z0, uu0, vv0,
                                                x0, y00 + (y00n - y00), z0, uu0, vv0,
                                                x1, y10n, z0, uu1, vv0,
                                                x1, y10, z0, uu1, vv0,
                                                cr, cg, cb, sideAlpha, 0.0F, 0.0F, -1.0F);
                                    }
                                }
                            }
                            // V2 - 3D DROPS: hanging fog tendrils dropping from clouds
                            for (int d = 0; d < 12; d++) {
                                double dx = cam.x + Math.sin(clock * 0.001 + d * 1.7) * SPAN * 0.7;
                                double dz = cam.z + Math.cos(clock * 0.0013 + d * 2.3) * SPAN * 0.7;
                                double dropTop = y + Math.sin(clock * 0.002 + d) * 2.0;
                                double dropBottom = dropTop - 18.0 - Math.abs(Math.sin(clock * 0.001 + d * 0.5)) * 12.0;
                                double r = 4.0 + (d % 3) * 2.0;
                                float u = (float)(dx / TILE + u0);
                                float v = (float)(dz / TILE + v0);
                                // billboard drop as vertical quad
                                quad(pose, consumer,
                                        dx - r, dropTop, dz, u, v,
                                        dx + r, dropTop, dz, u + 0.1F, v,
                                        dx + r, dropBottom, dz, u + 0.1F, v + 0.3F,
                                        dx - r, dropBottom, dz, u, v + 0.3F,
                                        cr, cg, cb, alpha / 4, 0.0F, 0.0F, 1.0F);
                            }
                        });
            }
            // V2 - Volumetric fog volumes epic - separate pass for fog
            submitFogVolumes(poseStack, collector, cam, clock, sheet, cr, cg, cb);
        } catch (Throwable ignored) {
        }
    }

    private static void submitFogVolumes(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cam, double clock, Identifier sheet, int cr, int cg, int cb) {
        // Epic fog volumes - 8 large volumetric boxes around player
        for (int f = 0; f < 8; f++) {
            final int fi = f;
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(sheet),
                    (pose, consumer) -> {
                        double angle = clock * 0.0005 + fi * Math.PI * 0.25;
                        double dist = 80.0 + fi * 20.0;
                        double cx = cam.x + Math.cos(angle) * dist;
                        double cz = cam.z + Math.sin(angle) * dist;
                        double cy = cam.y + 30.0 + Math.sin(clock * 0.001 + fi) * 10.0;
                        double size = 40.0 + fi * 8.0;
                        double height = 50.0 + Math.sin(clock * 0.002 + fi * 0.7) * 10.0;
                        int alpha = 18 + (fi % 3) * 8;
                        float u0 = (float)(clock * 0.0008 + fi * 0.2);
                        // 6 faces of fog volume cube
                        // top
                        quad(pose, consumer,
                                cx - size, cy + height, cz - size, u0, u0,
                                cx + size, cy + height, cz - size, u0 + 1, u0,
                                cx + size, cy + height, cz + size, u0 + 1, u0 + 1,
                                cx - size, cy + height, cz + size, u0, u0 + 1,
                                cr, cg, cb, alpha, 0, 1, 0);
                        // bottom
                        quad(pose, consumer,
                                cx - size, cy, cz - size, u0, u0,
                                cx - size, cy, cz + size, u0, u0 + 1,
                                cx + size, cy, cz + size, u0 + 1, u0 + 1,
                                cx + size, cy, cz - size, u0 + 1, u0,
                                cr, cg, cb, alpha / 2, 0, -1, 0);
                        // sides
                        quad(pose, consumer,
                                cx - size, cy, cz - size, u0, u0,
                                cx + size, cy, cz - size, u0 + 1, u0,
                                cx + size, cy + height, cz - size, u0 + 1, u0 + 1,
                                cx - size, cy + height, cz - size, u0, u0 + 1,
                                cr, cg, cb, alpha, 0, 0, -1);
                        quad(pose, consumer,
                                cx - size, cy, cz + size, u0, u0,
                                cx - size, cy + height, cz + size, u0, u0 + 1,
                                cx + size, cy + height, cz + size, u0 + 1, u0 + 1,
                                cx + size, cy, cz + size, u0 + 1, u0,
                                cr, cg, cb, alpha, 0, 0, 1);
                    });
        }
    }

    /** A slow, layer-shifted swell: three decks that never look like one sheet. */
    private static double ripple(double x, double z, double clock, double wave, int layer) {
        double a = (x + z * 0.7D) * 0.011D + clock * 0.0016D + layer * 1.7D;
        double b = (z - x * 0.4D) * 0.021D - clock * 0.0011D;
        return (Math.sin(a) * 0.7D + Math.sin(b) * 0.3D) * wave;
    }

    private static int clamp255(float v) {
        return (int) Math.max(0.0F, Math.min(255.0F, v));
    }

    /** Four corners, in order, each with its own UV. */
    private static void quad(Pose pose, VertexConsumer consumer,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int r, int g, int b, int a, float nx, float ny, float nz) {
        vertex(pose, consumer, x0, y0, z0, u0, v0, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x1, y1, z1, u1, v1, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x2, y2, z2, u2, v2, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x3, y3, z3, u3, v3, r, g, b, a, nx, ny, nz);
    }

    private static void vertex(Pose pose, VertexConsumer consumer,
            double x, double y, double z, float u, float v,
            int r, int g, int b, int a, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728810)
                .setNormal(pose, nx, ny, nz);
    }
}
