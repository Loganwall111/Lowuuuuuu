package net.dabicco.witherstormmod.entity.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;

public final class BlackHoleSphere {
   private static final int SLICES = 32;
   private static final int STACKS = 20;

   private BlackHoleSphere() {
   }

   public static void emit(Pose pose, VertexConsumer buf, float radius, int light) {
      for (int i = 0; i < 20; i++) {
         float phi0 = (float)(Math.PI * i / 20.0);
         float phi1 = (float)(Math.PI * (i + 1) / 20.0);
         float y0 = (float)Math.cos(phi0) * radius;
         float y1 = (float)Math.cos(phi1) * radius;
         float r0 = (float)Math.sin(phi0) * radius;
         float r1 = (float)Math.sin(phi1) * radius;

         for (int j = 0; j < 32; j++) {
            float t0 = (float)((Math.PI * 2) * j / 32.0);
            float t1 = (float)((Math.PI * 2) * (j + 1) / 32.0);
            float c0 = (float)Math.cos(t0);
            float s0 = (float)Math.sin(t0);
            float c1 = (float)Math.cos(t1);
            float s1 = (float)Math.sin(t1);
            v(pose, buf, r0 * c0, y0, r0 * s0, light);
            v(pose, buf, r0 * c1, y0, r0 * s1, light);
            v(pose, buf, r1 * c1, y1, r1 * s1, light);
            v(pose, buf, r1 * c0, y1, r1 * s0, light);
         }
      }
   }

   private static void v(Pose pose, VertexConsumer buf, float x, float y, float z, int light) {
      Vector3f n = new Vector3f(x, y, z);
      if (n.lengthSquared() > 1.0E-8F) {
         n.normalize();
      } else {
         n.set(0.0F, 1.0F, 0.0F);
      }

      buf.addVertex(pose, x, y, z).setColor(-16777216).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, n.x, n.y, n.z);
   }
}
