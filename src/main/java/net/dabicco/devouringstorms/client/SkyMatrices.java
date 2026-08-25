package net.dabicco.devouringstorms.client;

import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

/**
 * SkyMatrices — the projection matrix for the storm sky pass.
 *
 * Vanilla uploads the projection as an opaque GPU-side buffer, so the sky
 * pass composes its own view-projection UBO. When no captured matrix is
 * available it falls back to a live 70-degree perspective with the real
 * window aspect (gui-scaled dimensions keep the exact screen ratio), which
 * keeps the dome's angular scale honest; SkyRendererMixin's sky hook only
 * needs the projection for direction, never for depth ordering.
 */
public final class SkyMatrices {
   private static Matrix4f projection;

   private SkyMatrices() {
   }

   /** Called with a captured projection matrix, if one is ever provided. */
   public static void setProjection(Matrix4f matrix) {
      projection = matrix == null ? null : new Matrix4f(matrix);
   }

   /** The captured projection, or a live 70-degree fallback with the true aspect. */
   public static Matrix4f projection() {
      if (projection != null) {
         return projection;
      } else {
         Minecraft mc = Minecraft.getInstance();
         int w = mc.getWindow().getGuiScaledWidth();
         int h = mc.getWindow().getGuiScaledHeight();
         float aspect = h <= 0 ? 1.7777778F : (float)w / (float)h;
         return new Matrix4f().perspective((float)Math.toRadians(70.0), aspect, 0.05F, 200.0F);
      }
   }
}
