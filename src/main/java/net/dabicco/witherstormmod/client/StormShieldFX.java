package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.dabicco.witherstormmod.network.WitherStormPositionPacket;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormShieldFX — the glowing, emissive, alpha-blended 3D spherical protective
 * shield halos around the Wither Storm.
 *
 * Timeline (chronological stage triggers):
 *  - Phase 4.0+: the blue shield sphere activates around the storm centre and
 *    stays persistently active across ALL subsequent phases (4, 5, 6, 7 and
 *    beyond — there is deliberately no upper bound on the phase gate).
 *  - Phase 6.0+ (split heads): the same 3D shield is cleanly duplicated across
 *    all three split heads — the main devourer body plus both severed halves
 *    reported by the server.
 *
 * True transparency & depth: the shield is rendered as a real UV-sphere mesh
 * (not a billboard quad) through the depth-tested translucent pipeline, with a
 * second additive emissive pass on top so it reads as a glowing protective
 * shell that terrain can occlude and the player can look through.
 */
public final class StormShieldFX {
   private static final Identifier SHIELD = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/blue_shield.png");
   private static final Identifier HALO_RING = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/halo_ring.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final int RINGS = 18;
   private static final int SEGS = 36;
   /** Phase 7 purple flare: spike every 300 ticks (15s) with a slow breathing base. */
   private static final long FLARE_PERIOD_TICKS = 300L;
   private static final long FLARE_SPIKE_TICKS = 45L;
   private static BakedMesh.Mesh sphere;

   private StormShieldFX() {
   }

   private static BakedMesh.Mesh sphere() {
      BakedMesh.Mesh s = sphere;
      if (s == null) {
         s = buildSphere();
         sphere = s;
      }
      return s;
   }

   /** Procedural UV sphere (lat/long) so the shield never depends on a baked asset. */
   private static BakedMesh.Mesh buildSphere() {
      List<Float> tris = new ArrayList<>(RINGS * SEGS * 18);
      List<Float> uvs = new ArrayList<>(RINGS * SEGS * 12);
      List<Float> normals = new ArrayList<>(RINGS * SEGS * 6);
      for (int ring = 0; ring < RINGS; ring++) {
         float phi0 = (float)Math.PI * ring / RINGS;
         float phi1 = (float)Math.PI * (ring + 1) / RINGS;
         for (int seg = 0; seg < SEGS; seg++) {
            float th0 = (float)(Math.PI * 2.0) * seg / SEGS;
            float th1 = (float)(Math.PI * 2.0) * (seg + 1) / SEGS;
            // four sphere points for this cell
            float[][] p = new float[][]{
               point(phi0, th0), point(phi0, th1), point(phi1, th0), point(phi1, th1)
            };
            float[][] uv = new float[][]{
               {th0 / (float)(Math.PI * 2.0), 1.0F - phi0 / (float)Math.PI},
               {th1 / (float)(Math.PI * 2.0), 1.0F - phi0 / (float)Math.PI},
               {th0 / (float)(Math.PI * 2.0), 1.0F - phi1 / (float)Math.PI},
               {th1 / (float)(Math.PI * 2.0), 1.0F - phi1 / (float)Math.PI}
            };
            // two triangles: (p0,p2,p1) and (p1,p2,p3), normals = positions
            addTri(tris, p[0], p[2], p[1]);
            addTri(tris, p[1], p[2], p[3]);
            for (int q = 0; q < 2; q++) {
               int[] idx = q == 0 ? new int[]{0, 2, 1} : new int[]{1, 2, 3};
               for (int k : idx) {
                  uvs.add(uv[k][0]);
                  uvs.add(uv[k][1]);
               }
               for (int k : idx) {
                  normals.add(p[k][0]);
                  normals.add(p[k][1]);
                  normals.add(p[k][2]);
               }
            }
         }
      }
      float[] t = new float[tris.size()];
      for (int i = 0; i < t.length; i++) t[i] = tris.get(i);
      float[] u = new float[uvs.size()];
      for (int i = 0; i < u.length; i++) u[i] = uvs.get(i);
      float[] n = new float[normals.size()];
      for (int i = 0; i < n.length; i++) n[i] = normals.get(i);
      return new BakedMesh.Mesh(t, u, n);
   }

   private static float[] point(float phi, float th) {
      float sinPhi = (float)Math.sin(phi);
      return new float[]{sinPhi * (float)Math.cos(th), (float)Math.cos(phi), sinPhi * (float)Math.sin(th)};
   }

   private static void addTri(List<Float> tris, float[] a, float[] b, float[] c) {
      for (float[] v : new float[][]{a, b, c}) {
         tris.add(v[0]);
         tris.add(v[1]);
         tris.add(v[2]);
      }
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || ClientDistantStormManager.all().isEmpty()) {
         return;
      }
      float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float nowSec = gt * 0.05F;
      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();

      float[] col = new float[3];
      StormPalettes.haloShieldColor(col);

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         float phase = d.phase;
         if (phase < 4.0F) {
            continue; // shield timeline starts exactly at Phase 4
         }
         double bodyR = StormPresenceFX.bodyRadius(phase);
         double breathe = 0.75 + 0.25 * Math.sin(nowSec * 1.1 + d.entityId);
         float spin = nowSec * 6.0F + d.entityId;

         // Main shield: persistent from Phase 4 through every later phase.
         Vec3 centre = new Vec3(d.dispX, d.dispY, d.dispZ);
         shield(poseStack, collector, centre, bodyR * 1.45, col, breathe, spin);

         // Phase 6 split-heads: cleanly duplicate the 3D shield across all
         // three heads (main body above + both severed halves).
         if (phase >= 6.0F) {
            for (WitherStormPositionPacket.SeveredData s : d.severed) {
               Vec3 head = new Vec3(s.x(), s.y(), s.z());
               shield(poseStack, collector, head, bodyR * 0.85, col, breathe, spin + 90.0F);
            }
         }

         // Phase 7: maximize purple flares — periodic additive flare bursts
         // over the main body and every severed head, plus a cinematic
         // sky-spanning purple glare bloom high above the storm.
         if (phase >= 7.0F) {
            long gtTicks = mc.level.getGameTime();
            long inWindow = gtTicks % FLARE_PERIOD_TICKS;
            float spike = (float)Math.exp(-(double)inWindow / (double)FLARE_SPIKE_TICKS);
            float base = 0.22F + 0.10F * (float)Math.sin(nowSec * 0.9 + d.entityId);
            int a = (int)(Mth.clamp(base + 0.55F * spike, 0.0F, 1.0F) * 255.0F);
            if (a > 4) {
               Vec3 view = centre.subtract(cam).normalize();
               quad(poseStack, collector, GlowRenderTypes.glow(HALO_RING), cam, centre, view, bodyR * 2.4, 140, 51, 242, a);
               quad(poseStack, collector, GlowRenderTypes.glow(HALO_RING), cam, centre, view, bodyR * 3.0, 170, 80, 250, (int)(a * 0.55F));
               // Sky-spanning purple glare bloom high above the storm.
               Vec3 skyGlow = centre.add(0.0, bodyR * 4.2, 0.0);
               Vec3 skyView = skyGlow.subtract(cam).normalize();
               quad(poseStack, collector, GlowRenderTypes.glow(HALO_RING), cam, skyGlow, skyView, bodyR * 5.0, 120, 40, 235, (int)(a * 0.45F));
               quad(poseStack, collector, GlowRenderTypes.glow(HALO_RING), cam, skyGlow, skyView, bodyR * 7.0, 90, 28, 220, (int)(a * 0.28F));
               for (WitherStormPositionPacket.SeveredData s : d.severed) {
                  Vec3 head = new Vec3(s.x(), s.y(), s.z());
                  Vec3 hView = head.subtract(cam).normalize();
                  quad(poseStack, collector, GlowRenderTypes.glow(HALO_RING), cam, head, hView, bodyR * 1.3, 140, 51, 242, (int)(a * 0.7F));
               }
            }
         }
      }
   }

   private static void shield(PoseStack poseStack, SubmitNodeCollector collector, Vec3 at, double radius, float[] col, double breathe, float spin) {
      BakedMesh.Mesh m = sphere();
      if (m.tris().length == 0) {
         return;
      }
      int r = (int)(col[0] * 255.0F);
      int g = (int)(col[1] * 255.0F);
      int b = (int)(col[2] * 255.0F);
      int aShell = (int)(145.0 * breathe);
      int aGlow = (int)(72.0 * breathe);
      // Depth-tested translucent shell: true transparency, occluded by terrain.
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(SHIELD), (pose, consumer) -> emit(consumer, pose, m, at, radius, spin, r, g, b, aShell));
      // Additive emissive pass on top so the shield glows like a barrier.
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(SHIELD), (pose, consumer) -> emit(consumer, pose, m, at, radius, spin, r, g, b, aGlow));
   }

   /** camera-facing textured quad (phase-7 purple flares) */
   private static void quad(PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.rendertype.RenderType type, Vec3 cam, Vec3 centre, Vec3 view, double radius, int r, int g, int b, int alpha) {
      if (alpha <= 2) {
         return;
      }
      Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = view.cross(upHint).normalize();
      Vec3 up = right.cross(view).normalize();
      Vec3 rx = right.scale(radius);
      Vec3 uy = up.scale(radius);
      collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
         vertex(pose, consumer, centre.subtract(rx).subtract(uy), 0.0F, 0.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.add(rx).subtract(uy), 1.0F, 0.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.add(rx).add(uy), 1.0F, 1.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.subtract(rx).add(uy), 0.0F, 1.0F, r, g, b, alpha);
      });
   }

   private static void emit(VertexConsumer c, PoseStack.Pose p, BakedMesh.Mesh m, Vec3 pos, double radius, float spin, int r, int g, int b, int a) {      double y = Math.toRadians(spin);
      for (int i = 0; i < m.tris().length / 9; i++) {
         float nx = m.normals()[i * 3];
         float ny = m.normals()[i * 3 + 1];
         float nz = m.normals()[i * 3 + 2];
         for (int q = 0; q < 4; q++) {
            int j = i * 9 + Math.min(q, 2) * 3;
            int u = i * 6 + Math.min(q, 2) * 2;
            float px = m.tris()[j] * (float)radius;
            float py = m.tris()[j + 1] * (float)radius;
            float pz = m.tris()[j + 2] * (float)radius;
            float rx = (float)(px * Math.cos(y) - pz * Math.sin(y));
            float rz = (float)(px * Math.sin(y) + pz * Math.cos(y));
            c.addVertex(p, (float)pos.x + rx, (float)pos.y + py, (float)pos.z + rz)
               .setColor(r, g, b, a)
               .setUv(m.uvs()[u], m.uvs()[u + 1])
               .setOverlay(OverlayTexture.NO_OVERLAY)
               .setLight(FULL_BRIGHT)
               .setNormal(p, nx, ny, nz);
         }
      }
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
