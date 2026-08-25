package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.dabicco.devouringstorms.mixin.RenderPipelinesAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * StormSkyBox — the native Telltale-style sky pass.
 *
 * Drawn INSIDE vanilla's own sky frame pass (hooked from SkyRendererMixin at
 * renderSunMoonAndStars), so it inherits everything that makes the vanilla
 * sky sit at true infinite depth:
 *
 *  - the geometry is CAMERA-LOCKED (built around 0,0,0 of the sky pass'
 *    model-view; it never translates with the world, only rotates with the
 *    view, exactly like the sun/moon/stars);
 *  - the pipeline carries no depth-stencil state, so no depth TEST and no
 *    depth WRITE (the glDepthMask(false) / glDisable(GL_DEPTH_TEST) of the
 *    original technique) — mountains can never clip it and it can never
 *    produce square bounding-box artifacts against terrain;
 *  - blending is ADDITIVE, so the plates glow over the base sky disc and
 *    pure-black regions of a texture simply contribute nothing.
 *
 * Layers (all phase-blended by SkyAtmosphereController):
 *  1. the blue/cyan energy plate (phase 4+, with warm yellow horizon accents);
 *  2. the deep purple / black / orange anomaly plate (5.5-6.0 crossfade),
 *     tinted toward mutated red / magenta / orange through 6-8;
 *  3. two churning storm cloud bands built from the mod's cloud texture
 *     (the wired-in clouds), snapping to a chunky blocky rhythm;
 *  4. the localized purple mutation flash bloom (phase 6+), rendered here in
 *     the sky layer around the storm's bearing — never a full-screen overlay.
 *
 * The whole mass is concentrated in an angular cone around the normalized
 * camera-to-storm vector (the cone widens as the storm mutates) and the UVs
 * rotate slowly around that same axis so the backdrop churns around the storm.
 */
public final class StormSkyBox {
   private static final Identifier ENERGY_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/sky/phase4_energy.png");
   private static final Identifier ANOMALY_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/sky/phase59_anomaly.png");
   private static final Identifier CLOUD_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/mcsm_cloud.png");
   /** Sky geometry radius — the same "infinite" band vanilla's sun/moon live in. */
   private static final float RADIUS = 320.0F;
   /** Elevation rings, degrees above the horizon plane of the dome. */
   private static final float[] ELEVATIONS = new float[]{0.0F, 7.0F, 15.0F, 26.0F, 40.0F, 58.0F, 78.0F, 90.0F};
   private static final float[] RING_WEIGHTS = new float[]{1.0F, 0.95F, 0.85F, 0.66F, 0.45F, 0.26F, 0.12F, 0.04F};
   private static final int SEGMENTS = 28;
   /** Cloud band elevations (degrees) — two chunky strata. */
   private static final float[] CLOUD_ELEV_A = new float[]{12.0F, 24.0F, 36.0F};
   private static final float[] CLOUD_ELEV_B = new float[]{6.0F, 14.0F, 26.0F};

   private static RenderPipeline pipeline;

   /** Functional emitter: writes quads into the builder, returns the quad count. */
   @FunctionalInterface
   public interface LayerEmit {
      int emit(BufferBuilder bufferBuilder);
   }

   private StormSkyBox() {
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         // An additive clone of vanilla's END_SKY pipeline: same projection
         // snippet + position_tex_color shaders, but ONE,ONE blending so the
         // storm plates glow over the sky disc without ever boxing out.
         pipeline = RenderPipeline.builder(new RenderPipeline.Snippet[]{RenderPipelinesAccessor.dabyws$matricesProjectionSnippet()})
            .withLocation(Identifier.fromNamespaceAndPath("devouringstorms", "pipeline/storm_sky"))
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withSampler("Sampler0")
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .build();
      }

      return pipeline;
   }

   /**
    * Entry point from the SkyRenderer mixin. Refreshes the controller, then
    * draws every active layer. Returns true when the storm sky owned the
    * frame (the mixin then cancels the vanilla sun/moon/star pass).
    */
   public static boolean renderSkyLayers() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return false;
      }
      SkyAtmosphereController.update(mc.gameRenderer.mainCamera().position(), mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), mc.level.getGameTime());
      if (!SkyAtmosphereController.active()) {
         return false;
      }

      float intensity = SkyAtmosphereController.intensity();
      float energy = SkyAtmosphereController.energyWeight() * intensity;
      float anomaly = SkyAtmosphereController.anomalyWeight() * intensity;
      float clouds = SkyAtmosphereController.cloudWeight() * intensity;
      float phase = SkyAtmosphereController.phase();

      // storm bearing in the sky pass' local (view-rotated) space
      Vector3f target = stormBearingLocal();

      if (energy > 0.02F) {
         // phase 4: black/purple void with blue/cyan energy highlights and a
         // yellow horizon accent on the lowest ring
         float[] tint = new float[]{0.62F, 0.86F, 1.0F};
         drawLayer(ENERGY_TEXTURE, bb -> emitDome(bb, target, tint, energy, 1.0F, 0.0F, true));
      }

      if (anomaly > 0.02F) {
         // 5.9 anomaly plate, drifting into the mutated red/orange/magenta
         float mutation = Mth.clamp((phase - 6.0F) / 2.0F, 0.0F, 1.0F);
         float[] m = new float[3];
         SkyAtmosphereController.mutationTint(m);
         float[] tint = new float[]{Mth.lerp(mutation, 0.9F, m[0]), Mth.lerp(mutation, 0.66F, m[1]), Mth.lerp(mutation, 0.92F, m[2])};
         drawLayer(ANOMALY_TEXTURE, bb -> emitDome(bb, target, tint, anomaly, 1.12F, 0.35F, false));
      }

      if (clouds > 0.02F) {
         // the wired-in storm cloud bands: chunky, churning, indigo -> dark purple
         float late = StormCloudDeck.smooth(phase, 5.3F, 6.2F);
         float[] tintA = new float[]{Mth.lerp(late, 0.34F, 0.26F), Mth.lerp(late, 0.38F, 0.17F), Mth.lerp(late, 0.56F, 0.36F)};
         float[] tintB = new float[]{Mth.lerp(late, 0.28F, 0.20F), Mth.lerp(late, 0.31F, 0.13F), Mth.lerp(late, 0.50F, 0.30F)};
         drawLayer(CLOUD_TEXTURE, bb -> emitCloudBand(bb, target, CLOUD_ELEV_A, tintA, clouds * 0.8F, 1.6F, 18));
         drawLayer(CLOUD_TEXTURE, bb -> emitCloudBand(bb, target, CLOUD_ELEV_B, tintB, clouds * 0.62F, -1.1F, 14));
      }

      // phase 6+ mutation flash bloom — same sky layer, localized around the
      // storm bearing, never a full-screen overlay
      StormMutationFlash.renderSkyBloom(target);
      return true;
   }

   /** Storm bearing transformed into the sky pass' local view space. */
   private static Vector3f stormBearingLocal() {
      Vec3 dir = SkyAtmosphereController.stormDir();
      Vector3f v = new Vector3f((float)dir.x, (float)dir.y, (float)dir.z);
      Matrix4f mv = RenderSystem.getModelViewMatrixCopy();
      if (mv != null) {
         mv.transformDirection(v);
      }

      if (v.lengthSquared() < 1.0E-6F) {
         v.set(0.0F, 0.35F, 0.94F);
      }

      return v.normalize();
   }

   /**
    * Build the textured dome rings with the cone weighting. UVs are mapped
    * VERTICALLY by elevation and horizontally by azimuth around the storm
    * axis (plus the slow churn offset), staying inside the plate's safe band
    * so nothing ever samples past a texture edge.
    */
   private static int emitDome(BufferBuilder bb, Vector3f target, float[] tint, float alpha, float coneBoost, float extraChurn, boolean yellowHorizon) {
      float churn = SkyAtmosphereController.churn() + extraChurn;
      float coneCos = (float)Math.cos((double)Mth.clamp(SkyAtmosphereController.coneRadians() * coneBoost, 0.35F, 2.6F));
      int tr = (int)(Mth.clamp(tint[0], 0.0F, 1.0F) * 255.0F);
      int tg = (int)(Mth.clamp(tint[1], 0.0F, 1.0F) * 255.0F);
      int tb = (int)(Mth.clamp(tint[2], 0.0F, 1.0F) * 255.0F);
      int quads = 0;

      for (int i = 0; i < ELEVATIONS.length - 1; i++) {
         float lo = ELEVATIONS[i];
         float hi = ELEVATIONS[i + 1];
         float loRad = (float)Math.toRadians((double)lo);
         float hiRad = (float)Math.toRadians((double)hi);
         float yLo = Mth.sin(loRad) * RADIUS;
         float rLo = Mth.cos(loRad) * RADIUS;
         float yHi = Mth.sin(hiRad) * RADIUS;
         float rHi = Mth.cos(hiRad) * RADIUS;
         float wLo = RING_WEIGHTS[i];
         float wHi = RING_WEIGHTS[i + 1];
         // the yellow horizon accent lives only on the very first band
         int hr = yellowHorizon && i == 0 ? Math.min(255, tr + 96) : tr;
         int hg = yellowHorizon && i == 0 ? Math.min(255, tg + 62) : tg;
         int hb = yellowHorizon && i == 0 ? tb : tb;

         for (int s = 0; s < SEGMENTS; s++) {
            float az0 = (float)(Math.PI * 2.0 * (double)s / (double)SEGMENTS);
            float az1 = (float)(Math.PI * 2.0 * (double)(s + 1) / (double)SEGMENTS);
            // corner directions for the cone weight
            float c00 = coneWeight(dir(az0, yLo, rLo), target, coneCos);
            float c01 = coneWeight(dir(az1, yLo, rLo), target, coneCos);
            float c11 = coneWeight(dir(az1, yHi, rHi), target, coneCos);
            float c10 = coneWeight(dir(az0, yHi, rHi), target, coneCos);
            int a00 = alpha(alpha, c00 * wLo);
            int a01 = alpha(alpha, c01 * wLo);
            int a11 = alpha(alpha, c11 * wHi);
            int a10 = alpha(alpha, c10 * wHi);
            if (a00 + a01 + a11 + a10 <= 8) {
               continue;
            }

            float u0 = 0.5F + 0.35F * Mth.sin(az0 + churn);
            float u1 = 0.5F + 0.35F * Mth.sin(az1 + churn);
            float vLo = Mth.clamp(0.55F - lo * 0.0058F, 0.06F, 0.55F);
            float vHi = Mth.clamp(0.55F - hi * 0.0058F, 0.06F, 0.55F);
            vertex(bb, Mth.cos(az0) * rLo, yLo, Mth.sin(az0) * rLo, u0, vLo, hr, hg, hb, a00);
            vertex(bb, Mth.cos(az1) * rLo, yLo, Mth.sin(az1) * rLo, u1, vLo, hr, hg, hb, a01);
            vertex(bb, Mth.cos(az1) * rHi, yHi, Mth.sin(az1) * rHi, u1, vHi, hr, hg, hb, a11);
            vertex(bb, Mth.cos(az0) * rHi, yHi, Mth.sin(az0) * rHi, u0, vHi, hr, hg, hb, a10);
            quads++;
         }
      }

      return quads;
   }

   /** Chunky churning cloud strata between the given elevations. */
   private static int emitCloudBand(BufferBuilder bb, Vector3f target, float[] elevations, float[] tint, float alpha, float churnDir, int segments) {
      float churn = SkyAtmosphereController.churn() * churnDir;
      float coneCos = (float)Math.cos((double)Mth.clamp(SkyAtmosphereController.coneRadians() * 1.35F, 0.5F, 2.8F));
      int tr = (int)(Mth.clamp(tint[0], 0.0F, 1.0F) * 255.0F);
      int tg = (int)(Mth.clamp(tint[1], 0.0F, 1.0F) * 255.0F);
      int tb = (int)(Mth.clamp(tint[2], 0.0F, 1.0F) * 255.0F);
      int quads = 0;

      for (int i = 0; i < elevations.length - 1; i++) {
         float lo = (float)Math.toRadians((double)elevations[i]);
         float hi = (float)Math.toRadians((double)elevations[i + 1]);
         float yLo = Mth.sin(lo) * RADIUS;
         float rLo = Mth.cos(lo) * RADIUS;
         float yHi = Mth.sin(hi) * RADIUS;
         float rHi = Mth.cos(hi) * RADIUS;
         float bandLo = 1.0F - (float)i * 0.22F;
         float bandHi = 1.0F - (float)(i + 1) * 0.22F;

         for (int s = 0; s < segments; s++) {
            float az0 = (float)(Math.PI * 2.0 * (double)s / (double)segments);
            float az1 = (float)(Math.PI * 2.0 * (double)(s + 1) / (double)segments);
            float c00 = coneWeight(dir(az0, yLo, rLo), target, coneCos);
            float c01 = coneWeight(dir(az1, yLo, rLo), target, coneCos);
            float c11 = coneWeight(dir(az1, yHi, rHi), target, coneCos);
            float c10 = coneWeight(dir(az0, yHi, rHi), target, coneCos);
            int a00 = alpha(alpha, c00 * bandLo);
            int a01 = alpha(alpha, c01 * bandLo);
            int a11 = alpha(alpha, c11 * bandHi);
            int a10 = alpha(alpha, c10 * bandHi);
            if (a00 + a01 + a11 + a10 <= 8) {
               continue;
            }

            float u0 = 0.5F + 0.4F * Mth.sin(az0 + churn);
            float u1 = 0.5F + 0.4F * Mth.sin(az1 + churn);
            float vLo = 0.68F;
            float vHi = 0.95F;
            vertex(bb, Mth.cos(az0) * rLo, yLo, Mth.sin(az0) * rLo, u0, vLo, tr, tg, tb, a00);
            vertex(bb, Mth.cos(az1) * rLo, yLo, Mth.sin(az1) * rLo, u1, vLo, tr, tg, tb, a01);
            vertex(bb, Mth.cos(az1) * rHi, yHi, Mth.sin(az1) * rHi, u1, vHi, tr, tg, tb, a11);
            vertex(bb, Mth.cos(az0) * rHi, yHi, Mth.sin(az0) * rHi, u0, vHi, tr, tg, tb, a10);
            quads++;
         }
      }

      return quads;
   }

   private static Vector3f dir(float azimuth, float y, float r) {
      Vector3f v = new Vector3f(Mth.cos(azimuth) * r, y, Mth.sin(azimuth) * r);
      return v.normalize();
   }

   /** Cone weight: 1 at the storm bearing, falling to 0 past the cone edge. */
   private static float coneWeight(Vector3f dir, Vector3f target, float coneCos) {
      float d = dir.dot(target);
      if (d <= coneCos) {
         return 0.0F;
      }
      float t = (d - coneCos) / Math.max(1.0F - coneCos, 1.0E-4F);
      return t * t * (3.0F - 2.0F * t);
   }

   private static int alpha(float base, float weight) {
      return (int)(Mth.clamp(base * Mth.clamp(weight, 0.0F, 1.0F), 0.0F, 1.0F) * 235.0F);
   }

   private static void vertex(BufferBuilder bb, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
      bb.addVertex(x, y, z).setUv(u, v).setColor(r, g, b, a);
   }

   /**
    * Build + draw one additive textured layer in the sky pass, mirroring
    * vanilla's own SkyRenderer render passes exactly (fresh vertex buffer per
    * frame, own RenderPass on the main target, default uniforms + transforms).
    */
   public static void drawLayer(Identifier texture, LayerEmit emitter) {
      Minecraft mc = Minecraft.getInstance();
      AbstractTexture tex = mc.getTextureManager().getTexture(texture);
      int maxQuads = SEGMENTS * (ELEVATIONS.length + CLOUD_ELEV_A.length + CLOUD_ELEV_B.length) + 16;

      try (ByteBufferBuilder builder = ByteBufferBuilder.exactlySized((long)maxQuads * 4L * (long)DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
         BufferBuilder bufferBuilder = new BufferBuilder(builder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         int quads = emitter.emit(bufferBuilder);
         if (quads <= 0) {
            return;
         }

         try (MeshData meshData = bufferBuilder.buildOrThrow()) {
            RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            GpuBuffer indexBuffer = autoIndices.getBuffer(quads * 6);

            try (GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Devouring Storms sky layer", 32, meshData.vertexBuffer())) {
               GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
               GpuTextureView color = mc.getMainRenderTarget().getColorTextureView();
               GpuTextureView depth = mc.getMainRenderTarget().getDepthTextureView();

               try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Devouring Storms sky", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
                  renderPass.setPipeline(pipeline());
                  RenderSystem.bindDefaultUniforms(renderPass);
                  renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                  renderPass.bindTexture("Sampler0", tex.getTextureView(), tex.getSampler());
                  renderPass.setVertexBuffer(0, vertexBuffer);
                  renderPass.setIndexBuffer(indexBuffer, autoIndices.type());
                  renderPass.drawIndexed(0, 0, quads * 6, 1);
               }
            }
         }
      } catch (Exception e) {
         // Never let a sky-layer hiccup kill the frame
      }
   }
}
