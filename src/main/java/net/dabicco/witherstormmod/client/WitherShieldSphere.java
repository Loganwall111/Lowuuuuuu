package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/**
 * WitherShieldSphere — Generates a 3D spherical shell matrix wrapped entirely
 * around the bounding box of the Phase 4 Wither Storm.
 *
 * Implements a true 3D hollow sphere mesh with Fresnel edge falloff,
 * depth testing (glEnable(GL_DEPTH_TEST)), and automated radius scaling.
 */
public final class WitherShieldSphere {
   private static final int RINGS = 24;
   private static final int SECTORS = 32;
   private static final float[][] UNIT_VERTS;
   private static final float[][] UNIT_UVS;
   private static final int[][] INDICES;

   static {
      int totalVerts = (RINGS + 1) * (SECTORS + 1);
      UNIT_VERTS = new float[totalVerts][3];
      UNIT_UVS = new float[totalVerts][2];

      int vIdx = 0;
      for (int r = 0; r <= RINGS; r++) {
         float lat = (float)Math.PI * (-0.5F + (float)r / (float)RINGS);
         float cosLat = (float)Math.cos(lat);
         float sinLat = (float)Math.sin(lat);
         float v = (float)r / (float)RINGS;

         for (int s = 0; s <= SECTORS; s++) {
            float lon = 2.0F * (float)Math.PI * (float)s / (float)SECTORS;
            float cosLon = (float)Math.cos(lon);
            float sinLon = (float)Math.sin(lon);
            float u = (float)s / (float)SECTORS;

            UNIT_VERTS[vIdx][0] = cosLat * cosLon;
            UNIT_VERTS[vIdx][1] = sinLat;
            UNIT_VERTS[vIdx][2] = cosLat * sinLon;

            UNIT_UVS[vIdx][0] = u;
            UNIT_UVS[vIdx][1] = v;
            vIdx++;
         }
      }

      int quadCount = RINGS * SECTORS;
      INDICES = new int[quadCount][4];
      int qIdx = 0;
      for (int r = 0; r < RINGS; r++) {
         for (int s = 0; s < SECTORS; s++) {
            int p00 = r * (SECTORS + 1) + s;
            int p10 = (r + 1) * (SECTORS + 1) + s;
            int p11 = (r + 1) * (SECTORS + 1) + (s + 1);
            int p01 = r * (SECTORS + 1) + (s + 1);

            INDICES[qIdx][0] = p00;
            INDICES[qIdx][1] = p10;
            INDICES[qIdx][2] = p11;
            INDICES[qIdx][3] = p01;
            qIdx++;
         }
      }
   }

   private WitherShieldSphere() {
   }

   /**
    * Emits the 3D spherical shell geometry into the VertexConsumer.
    *
    * @param poseStack PoseStack for matrix transformations
    * @param consumer  VertexConsumer targeting the wither_shield shader pipeline
    * @param center    World-space anchor (middle/center of Wither Storm body)
    * @param radius    Calculated shell radius wrapping around the boss bounding box
    * @param r         Red tint (0-255)
    * @param g         Green tint (0-255)
    * @param b         Blue tint (0-255)
    * @param alpha     Alpha opacity (0-255)
    */
   public static void emit(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, float radius, int r, int g, int b, int alpha) {
      for (int[] quad : INDICES) {
         for (int i = 0; i < 4; i++) {
            int v = quad[i];
            float nx = UNIT_VERTS[v][0];
            float ny = UNIT_VERTS[v][1];
            float nz = UNIT_VERTS[v][2];
            float u = UNIT_UVS[v][0];
            float vCoord = UNIT_UVS[v][1];

            float px = (float)center.x + nx * radius;
            float py = (float)center.y + ny * radius;
            float pz = (float)center.z + nz * radius;

            consumer.addVertex(pose, px, py, pz)
               .setColor(r, g, b, alpha)
               .setUv(u, vCoord)
               .setOverlay(OverlayTexture.NO_OVERLAY)
               .setLight(15728880)
               .setNormal(pose, nx, ny, nz);
         }
      }
   }
}
