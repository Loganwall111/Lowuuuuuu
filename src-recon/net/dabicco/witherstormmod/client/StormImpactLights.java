package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.WitherStormHeadEntity;
import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class StormImpactLights {
   private static final int MAX_LIGHTS = 8;
   private static final float RADIUS_PER_LEVEL = 2.35F;
   private static final float BRIGHTNESS_AT_FULL = 0.75F;
   private static final float DESATURATE = 0.45F;
   private static final float CORE_DISTANCE = 6.0F;
   private static final double HEIGHT_ABOVE_IMPACT = 1.15;
   private static RenderPipeline pipeline;
   private static ByteBuffer staging;
   private static boolean failed;
   private static final float[] POS = new float[32];
   private static final float[] COLOR = new float[32];
   private static final float[] SHAPE = new float[32];
   private static final double[] DIST = new double[8];
   private static int count;
   private static int far;
   private static float tintR = 1.0F;
   private static float tintG = 1.0F;
   private static float tintB = 1.0F;
   private static float tintA = 1.0F;
   private static int lastOverflow;

   private StormImpactLights() {
   }

   private static void offer(Vec3 end, float beamScale, float baseRadius, float coreDistance, Vec3 eye, double rangeSq) {
      double distSq = eye.distanceToSqr(end);
      if (!(distSq > rangeSq)) {
         far++;
         float scale = Math.max(0.25F, beamScale);
         float lightRadius = baseRadius * (0.55F + 0.45F * scale);
         int slot = count;
         if (count < 8) {
            count++;
         } else {
            int worst = 0;

            for (int i = 1; i < 8; i++) {
               if (DIST[i] > DIST[worst]) {
                  worst = i;
               }
            }

            if (DIST[worst] <= distSq) {
               return;
            }

            slot = worst;
         }

         int ix = slot * 4;
         POS[ix] = (float)(end.x - eye.x);
         POS[ix + 1] = (float)(end.y + 1.15 - eye.y);
         POS[ix + 2] = (float)(end.z - eye.z);
         POS[ix + 3] = lightRadius;
         COLOR[ix] = tintR;
         COLOR[ix + 1] = tintG;
         COLOR[ix + 2] = tintB;
         COLOR[ix + 3] = tintA;
         SHAPE[ix] = coreDistance * (0.55F + 0.45F * scale);
         DIST[slot] = distSq;
      }
   }

   private static void noteOverflow(int total) {
      if (total != lastOverflow) {
         lastOverflow = total;
         System.out.println("[dabywitherstormmod] " + total + " beam impact lights in range, showing the nearest 8");
      }
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         pipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()})
            .withLocation(id("pipeline/storm_impact_light"))
            .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
            .withFragmentShader(id("post/storm_impact_light"))
            .withBindGroupLayout(BindGroupLayout.builder().withSampler("DepthSampler").withUniform("LightConfig", UniformType.UNIFORM_BUFFER).build())
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .build();
      }

      return pipeline;
   }

   public static void render(CameraRenderState camera) {
      if (!failed && camera != null && !net.dabicco.witherstormmod.client.ShaderPackCompat.active() && DabyWSClientConfig.impactLight) {
         int level = net.dabicco.witherstormmod.client.ClientConfigCache.cfg.beamImpactLight;
         if (level > 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
               RenderTarget scene = net.dabicco.witherstormmod.client.StormBloom.sceneTarget(mc);
               if (scene != null && scene.useDepth && scene.getDepthTextureView() != null) {
                  Vec3 eye = camera.pos;
                  float radius = (float)(level * 2.35F * Math.max(0.05, DabyWSClientConfig.impactLightSize));
                  float brightness = (float)(0.75F * (level / 15.0F) * Math.max(0.0, DabyWSClientConfig.impactLightBrightness));
                  float core = (float)(6.0 * Math.max(0.05, DabyWSClientConfig.impactLightSize));
                  if (!(brightness <= 0.0F) && !(radius <= 0.0F)) {
                     if (DabyWSClientConfig.impactLightUseBeamColor) {
                        tintR = (float)Mth.lerp(0.45F, Mth.clamp(DabyWSClientConfig.beamColorR, 0.0, 1.0), 1.0);
                        tintG = (float)Mth.lerp(0.45F, Mth.clamp(DabyWSClientConfig.beamColorG, 0.0, 1.0), 1.0);
                        tintB = (float)Mth.lerp(0.45F, Mth.clamp(DabyWSClientConfig.beamColorB, 0.0, 1.0), 1.0);
                     } else {
                        tintB = 1.0F;
                        tintG = 1.0F;
                        tintR = 1.0F;
                     }

                     tintA = brightness;
                     count = 0;
                     far = 0;
                     double maxRange = DabyWSClientConfig.impactLightRange;
                     double rangeSq = maxRange * maxRange;

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof WitherStormHeadEntity head && head.isBeamActive()) {
                           Vec3 end = head.clientBeamEnd != null ? head.clientBeamEnd : head.getBeamEndExact();
                           if (end != null) {
                              offer(end, head.beamScale(), radius, core, eye, rangeSq);
                           }
                        }
                     }

                     for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData storm : DabyWSClientConfig.distantStorms
                        ? net.dabicco.witherstormmod.client.ClientDistantStormManager.all()
                        : List.<net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData>of()) {
                        if (mc.level.getEntity(storm.entityId) == null) {
                           for (int i = 0; i < storm.beamActive.length; i++) {
                              if (storm.beamActive[i]) {
                                 Vec3 end = storm.dispBeamEnd[i] != null ? storm.dispBeamEnd[i] : storm.beamEnd[i];
                                 if (end != null) {
                                    offer(end, 1.0F, radius, core, eye, rangeSq);
                                 }
                              }
                           }
                        }
                     }

                     if (count != 0) {
                        if (far > count) {
                           noteOverflow(far);
                        }

                        for (int ix = count * 4; ix < POS.length; ix++) {
                           POS[ix] = 0.0F;
                           COLOR[ix] = 0.0F;
                           SHAPE[ix] = 1.0F;
                        }

                        Matrix4f invViewProj = new Matrix4f(camera.projectionMatrix).mul(camera.viewRotationMatrix).invert();

                        try {
                           int size = new Std140SizeCalculator().putMat4f().putVec4().align(16).get() + 384;
                           ByteBuffer data = staging(size);
                           Std140Builder builder = Std140Builder.intoBuffer(data);
                           builder.putMat4f(invViewProj);
                           builder.putVec4(count, 0.0F, 0.0F, 0.0F);

                           for (int ix = 0; ix < 8; ix++) {
                              builder.putVec4(POS[ix * 4], POS[ix * 4 + 1], POS[ix * 4 + 2], POS[ix * 4 + 3]);
                           }

                           for (int ix = 0; ix < 8; ix++) {
                              builder.putVec4(COLOR[ix * 4], COLOR[ix * 4 + 1], COLOR[ix * 4 + 2], COLOR[ix * 4 + 3]);
                           }

                           for (int ix = 0; ix < 8; ix++) {
                              builder.putVec4(SHAPE[ix * 4], 0.0F, 0.0F, 0.0F);
                           }

                           data.rewind();
                           GpuBuffer ubo = net.dabicco.witherstormmod.client.GpuBufferPool.write("dabyws impact light cfg", 128, data);
                           RenderPass pass = RenderSystem.getDevice()
                              .createCommandEncoder()
                              .createRenderPass(() -> "dabyws impact light", scene.getColorTextureView(), Optional.empty());

                           try {
                              pass.setPipeline(pipeline());
                              pass.setUniform("LightConfig", ubo);
                              pass.bindTexture("DepthSampler", scene.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                              pass.draw(3, 1, 0, 0);
                           } catch (Throwable var21) {
                              if (pass != null) {
                                 try {
                                    pass.close();
                                 } catch (Throwable var20) {
                                    var21.addSuppressed(var20);
                                 }
                              }

                              throw var21;
                           }

                           if (pass != null) {
                              pass.close();
                           }
                        } catch (Exception var22) {
                           failed = true;
                           System.out.println("[dabywitherstormmod] beam impact light DISABLED after an error: " + var22);
                           var22.printStackTrace();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static ByteBuffer staging(int bytes) {
      if (staging == null || staging.capacity() < bytes) {
         staging = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
      }

      staging.clear();
      return staging;
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
   }
}
