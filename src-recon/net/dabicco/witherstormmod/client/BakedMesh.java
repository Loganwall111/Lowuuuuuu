package net.dabicco.witherstormmod.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.Vec3;

public final class BakedMesh {
   private static final Map<String, net.dabicco.witherstormmod.client.BakedMesh.Mesh> CACHE = new HashMap<>();

   public static net.dabicco.witherstormmod.client.BakedMesh.Mesh mesh(String name) {
      return CACHE.computeIfAbsent(name, net.dabicco.witherstormmod.client.BakedMesh::load);
   }

   private static net.dabicco.witherstormmod.client.BakedMesh.Mesh load(String n) {
      try {
         Optional<Resource> r = Minecraft.getInstance()
            .getResourceManager()
            .getResource(Identifier.fromNamespaceAndPath("dabywitherstormmod", "meshes/" + n + ".json"));
         if (r.isEmpty()) {
            return new net.dabicco.witherstormmod.client.BakedMesh.Mesh(new float[0], new float[0], new float[0]);
         } else {
            JsonObject o = JsonParser.parseReader(new InputStreamReader(r.get().open())).getAsJsonObject();
            return new net.dabicco.witherstormmod.client.BakedMesh.Mesh(
               values(o.getAsJsonArray("tris")), values(o.getAsJsonArray("uvs")), values(o.getAsJsonArray("normals"))
            );
         }
      } catch (Exception var3) {
         return new net.dabicco.witherstormmod.client.BakedMesh.Mesh(new float[0], new float[0], new float[0]);
      }
   }

   private static float[] values(JsonArray a) {
      float[] x = new float[a.size()];

      for (int i = 0; i < x.length; i++) {
         x[i] = a.get(i).getAsFloat();
      }

      return x;
   }

   public static void emit(
      VertexConsumer c,
      Pose p,
      net.dabicco.witherstormmod.client.BakedMesh.Mesh m,
      Vec3 pos,
      float yaw,
      float tumble,
      float scale,
      int r,
      int g,
      int b,
      int a,
      int light
   ) {
      double y = Math.toRadians(yaw);
      double x = Math.toRadians(tumble);

      for (int i = 0; i < m.tris.length / 9; i++) {
         float nx = m.normals[i * 3];
         float ny = m.normals[i * 3 + 1];
         float nz = m.normals[i * 3 + 2];
         float lam = (float)(0.66 + 0.34 * Math.max(0.0, Math.min(1.0, nx * 0.25 + ny * 0.88 + nz * 0.38)));

         for (int q = 0; q < 4; q++) {
            int j = i * 9 + Math.min(q, 2) * 3;
            int u = i * 6 + Math.min(q, 2) * 2;
            float px = m.tris[j] * scale;
            float pz = m.tris[j + 2] * scale;
            float py = m.tris[j + 1] * scale;
            float rx = (float)(px * Math.cos(y) - pz * Math.sin(y));
            float rz = (float)(px * Math.sin(y) + pz * Math.cos(y));
            float ry = (float)(py * Math.cos(x) - rz * Math.sin(x));
            rz = (float)(py * Math.sin(x) + rz * Math.cos(x));
            c.addVertex(p, (float)pos.x + rx, (float)pos.y + ry, (float)pos.z + rz)
               .setColor((int)(r * lam), (int)(g * lam), (int)(b * lam), a)
               .setUv(m.uvs[u], m.uvs[u + 1])
               .setOverlay(OverlayTexture.NO_OVERLAY)
               .setLight(light)
               .setNormal(p, nx, ny, nz);
         }
      }
   }

   public record Mesh(float[] tris, float[] uvs, float[] normals) {
   }
}
