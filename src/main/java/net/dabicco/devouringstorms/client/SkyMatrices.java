package net.dabicco.devouringstorms.client;

import org.joml.Matrix4f;

/**
 * SkyMatrices — holds the frame's projection matrix for the storm sky pass.
 *
 * Vanilla uploads the projection as an opaque GPU-side buffer, so the mod
 * captures the raw Matrix4f at the single point vanilla itself has it in
 * hand (GameRenderer.renderLevel feeding ProjectionMatrixBuffer) via
 * GameRendererSkyMixin, and stores a private copy here. The sky pass then
 * composes projection x live view rotation for its own UBO.
 */
public final class SkyMatrices {
   private static final Matrix4f FALLBACK = new Matrix4f().perspective((float)Math.toRadians(70.0), 1.7777778F, 0.05F, 100.0F);
   private static Matrix4f projection;

   private SkyMatrices() {
   }

   /** Called by GameRendererSkyMixin with the frame's projection matrix. */
   public static void setProjection(Matrix4f matrix) {
      projection = matrix == null ? null : new Matrix4f(matrix);
   }

   /** The last captured projection, or a sane 70-degree fallback. */
   public static Matrix4f projection() {
      return projection == null ? FALLBACK : projection;
   }
}
