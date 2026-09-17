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
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmIdentity;
import net.mcsm.extras.McsmReality;
import net.mcsm.extras.McsmVoid;

/**
 * BUILD #457 -- THE PAINTED SKY. Every dimension wears its own cube.
 *
 * <p>THE REPORT, standing for a long time: "custom skyboxes", "the sky is not
 * fully the sky yet". Every shader-side answer had the same hole -- a player
 * without the shader pack sees none of it, and a shader pack can tint the vanilla
 * sky but cannot replace it.
 *
 * <p>So the mod draws its own sky, in the world, out of ordinary geometry: a
 * camera-anchored CUBE -- four walls and a lid, no dome anywhere, the dome ban
 * stands -- textured with the painted panoramas
 * {@code ci/make_skybox_textures.py} writes into the pack. Because it is a cube
 * with six flat faces and the texture is a painting of a sky, it costs one draw
 * call per dimension and carries no shader dependency at all.
 *
 * <p><b>Three dimensions, three skies, and no two of them alike:</b>
 * <ul>
 *   <li><b>The decayed reality</b> -- ash-teal air over an ochre band, painted
 *       cloud bars, violet tears in it, a violet zenith.</li>
 *   <li><b>The infinite dimension</b> -- amethyst air, the city's warm glow along
 *       the horizon, constellations above it.</li>
 *   <li><b>The void</b> -- near-black violet, magenta rifts crossing the whole
 *       sky, a cold green haze low down, the densest starfield of the three.</li>
 * </ul>
 *
 * <p>The Overworld is deliberately NOT in this list: it keeps the vanilla sky and
 * the storm's own backdrop, which is the one sky the mod does not own.
 *
 * <p><b>Why it cannot fight the terrain.</b> The cube is submitted through the same
 * fogless, depth-tested, alpha-blended path the atmosphere and the sky floor band
 * already use: the depth test is LESS with no depth write, so anything nearer than
 * the cube -- every block, mob and the player -- stays in front of it, and the cube
 * only paints where the vanilla sky would otherwise show. The walls run
 * {@link #SKIRT} blocks under the horizon and stop; below that the sky floor band
 * owns the under-world, so the two never argue over a pixel.
 */
public final class McsmPaintedSky {

    /** How far out the cube stands: inside every far plane, outside ordinary terrain. */
    private static final double RADIUS = 256.0D;
    /** How far the walls run below the horizon before the floor band takes over. */
    private static final double SKIRT = 8.0D;
    /** The lid's height above the eye, as a fraction of the radius. */
    private static final double LID = 0.62D;

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath("mcsm", "textures/sky/" + name + ".png");
    }

    private static final Identifier DECAYED_SIDES = tex("decayed_sides");
    private static final Identifier DECAYED_TOP = tex("decayed_top");
    private static final Identifier ADAMS_SIDES = tex("adams_sides");
    private static final Identifier ADAMS_TOP = tex("adams_top");
    private static final Identifier VOID_SIDES = tex("void_sides");
    private static final Identifier VOID_TOP = tex("void_top");

    /** One sky: its two paintings, its tint, and how fast the panorama turns. */
    private record Sky(Identifier sides, Identifier lid, float r, float g, float b, float spin) {
    }

    private McsmPaintedSky() {
    }

    /**
     * The sky for where the camera is, or null for "not ours" -- the Overworld and
     * any other dimension keep whatever sky they already have.
     */
    private static Sky skyFor(ClientLevel level) {
        try {
            if (McsmReality.inside(level)) {
                // the decayed reality: the violet is the dimension's own, so the sky
                // carries it rather than inventing a second colour. BUILD #458 -- the
                // grade and the turn are read from the dimension's identity, so the
                // sky, the fog and the horizon band cannot drift apart.
                McsmIdentity.Skin s = McsmIdentity.skin(McsmIdentity.DECAYED);
                return new Sky(DECAYED_SIDES, DECAYED_TOP, s.tintR(), s.tintG(), s.tintB(), s.spin());
            }
            if (level.dimension().equals(McsmAdams.ADAMS)) {
                McsmIdentity.Skin s = McsmIdentity.skin(McsmIdentity.ADAMS);
                return new Sky(ADAMS_SIDES, ADAMS_TOP, s.tintR(), s.tintG(), s.tintB(), s.spin());
            }
            if (level.dimension().equals(McsmVoid.DIMENSION)) {
                // the void is the only one that turns: a slow revolution over the
                // whole fall, so falling down it does not feel like standing still
                McsmIdentity.Skin s = McsmIdentity.skin(McsmIdentity.VOID);
                return new Sky(VOID_SIDES, VOID_TOP, s.tintR(), s.tintG(), s.tintB(), s.spin());
            }
        } catch (Throwable ignored) {
        }
        return null;
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
            Sky sky = skyFor(level);
            if (sky == null) {
                return;
            }
            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            final double r = RADIUS;
            final double top = cam.y + r * LID;
            final double bottom = cam.y - SKIRT;
            final float spin = (level.getGameTime() % 240000L) * sky.spin();
            final int cr = clamp255(sky.r() * 255.0F);
            final int cg = clamp255(sky.g() * 255.0F);
            final int cb = clamp255(sky.b() * 255.0F);

            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();

            final double cx = cam.x;
            final double cz = cam.z;
            // ---- the four walls: one quarter of the panorama each -----------
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(sky.sides()),
                    (pose, consumer) -> {
                        for (int i = 0; i < 4; i++) {
                            // 0: -z, 1: +x, 2: +z, 3: -x -- the four sides of the cube
                            double x0;
                            double z0;
                            double x1;
                            double z1;
                            switch (i) {
                                case 0 -> {
                                    x0 = cx - r;
                                    z0 = cz - r;
                                    x1 = cx + r;
                                    z1 = cz - r;
                                }
                                case 1 -> {
                                    x0 = cx + r;
                                    z0 = cz - r;
                                    x1 = cx + r;
                                    z1 = cz + r;
                                }
                                case 2 -> {
                                    x0 = cx + r;
                                    z0 = cz + r;
                                    x1 = cx - r;
                                    z1 = cz + r;
                                }
                                default -> {
                                    x0 = cx - r;
                                    z0 = cz + r;
                                    x1 = cx - r;
                                    z1 = cz - r;
                                }
                            }
                            // v = 0 is the horizon (the top row of the painting) and
                            // v = 1 is the zenith, which is how the painter draws them
                            float u0 = i * 0.25F + spin;
                            float u1 = (i + 1) * 0.25F + spin;
                            float nx = (float) ((x0 + x1) * 0.5D - cx);
                            float nz = (float) ((z0 + z1) * 0.5D - cz);
                            quad(pose, consumer,
                                    x0, bottom, z0, u0, 0.0F,
                                    x1, bottom, z1, u1, 0.0F,
                                    x1, top, z1, u1, 1.0F,
                                    x0, top, z0, u0, 1.0F,
                                    cr, cg, cb, 255, nx, 0.0F, nz);
                        }
                    });

            // ---- the lid: the zenith, turning with the walls ------------------
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(sky.lid()),
                    (pose, consumer) -> {
                        float du = -spin * 2.0F;
                        float dv = spin * 2.0F;
                        quad(pose, consumer,
                                cx - r, top, cz - r, du, dv,
                                cx + r, top, cz - r, 1.0F + du, dv,
                                cx + r, top, cz + r, 1.0F + du, 1.0F + dv,
                                cx - r, top, cz + r, du, 1.0F + dv,
                                cr, cg, cb, 255, 0.0F, -1.0F, 0.0F);
                    });
        } catch (Throwable ignored) {
            // a sky that throws is worse than a sky that is vanilla
        }
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
