package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.dabicco.devouringstorms.entity.WitherStormHeadEntity;
import net.dabicco.devouringstorms.mixin.RenderPipelinesAccessor;
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

   private static void offer(Vec3 end, double yOffset, float beamScale, float baseRadius, float coreDistance, float r, float g, float b, float a, Vec3 eye, double rangeSq) {
      Vec3 lit = end.add(0.0, yOffset, 0.0);
      double distSq = eye.distanceToSqr(lit);
      if (!(distSq > rangeSq)) {
         ++far;
         float scale = Math.max(0.25F, beamScale);
         float lightRadius = baseRadius * (0.55F + 0.45F * scale);
         int slot = count;
         if (count >= 8) {
            int worst = 0;

            for(int i = 1; i < 8; ++i) {
               if (DIST[i] > DIST[worst]) {
                  worst = i;
               }
            }

            if (DIST[worst] <= distSq) {
               return;
            }

            slot = worst;
         } else {
            ++count;
         }

         int i = slot * 4;
         POS[i] = (float)(lit.x - eye.x);
         POS[i + 1] = (float)(lit.y - eye.y);
         POS[i + 2] = (float)(lit.z - eye.z);
         POS[i + 3] = lightRadius;
         COLOR[i] = r;
         COLOR[i + 1] = g;
         COLOR[i + 2] = b;
         COLOR[i + 3] = a;
         SHAPE[i] = coreDistance * (0.55F + 0.45F * scale);
         DIST[slot] = distSq;
      }
   }

   private static float shadedBodyAmount(double phase, double expansionPhase) {
      if (!StormSkins.shaded()) {
         return 0.0F;
      } else {
         float phaseRamp = Mth.clamp((float)((Math.max(phase, expansionPhase) - 4.15) / 1.45), 0.0F, 1.0F);
         float growth = Mth.clamp((float)WitherStormEntity.clientGrowthScaleForPhase(Math.max(phase, expansionPhase)) - 0.9F, 0.0F, 1.0F);
         return Math.max(phaseRamp, growth * 0.7F);
      }
   }

   private static void offerStormBody(Vec3 centre, double phase, double expansionPhase, Vec3 eye, double rangeSq, float baseRadius, float brightness) {
      float shaded = shadedBodyAmount(phase, expansionPhase);
      if (!(shaded <= 0.01F)) {
         float growth = (float)WitherStormEntity.clientGrowthScaleForPhase(Math.max(phase, expansionPhase));
         float[] pulse = StormPalettes.pulseColor(phase, new float[3]);
         float[] halo = StormPalettes.haloUnderColor(new float[3]);
         float[] cloud = StormPalettes.cloudColor(phase, new float[3]);
         float r = Mth.clamp(Mth.lerp(0.36F, halo[0], pulse[0]) * (1.0F - 0.10F * DESATURATE), 0.0F, 1.0F);
         float g = Mth.clamp(Mth.lerp(0.30F, halo[1], cloud[1]) * (1.0F - 0.20F * DESATURATE), 0.0F, 1.0F);
         float b = Mth.clamp(Mth.lerp(0.24F, pulse[2], cloud[2]) + 0.08F, 0.0F, 1.0F);
         float alpha = brightness * (0.34F + 0.28F * shaded);
         float radius = baseRadius * (1.15F + shaded * 0.95F) * (0.85F + 0.25F * growth);
         float core = CORE_DISTANCE * (1.35F + shaded * 0.55F);
         double yOffset = 8.0 + Math.min(Math.max(phase, 4.0), 6.5) * 2.6 * (0.82 + 0.16 * growth);
         offer(centre, yOffset, 1.0F + growth * 0.24F, radius, core, r, g, b, alpha, eye, rangeSq);
      }
   }

   private static void noteOverflow(int total) {
      if (total != lastOverflow) {
         lastOverflow = total;
         System.out.println("[devouringstorms] " + total + " storm lights in range, showing the nearest 8");
      }
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         pipeline = RenderPipeline.builder(new RenderPipeline.Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()}).withLocation(id("pipeline/storm_impact_light")).withVertexShader(Identifier.withDefaultNamespace("core/screenquad")).withFragmentShader(id("post/storm_impact_light")).withBindGroupLayout(BindGroupLayout.builder().withSampler("DepthSampler").withUniform("LightConfig", UniformType.UNIFORM_BUFFER).build()).withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE)).build();
      }

      return pipeline;
   }

   public static void render(CameraRenderState camera) {
      if (!failed && camera != null) {
         if (!ShaderPackCompat.active()) {
            if (DevouringStormsClientConfig.impactLight) {
               int level = ClientConfigCache.cfg.beamImpactLight;
               if (level > 0) {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc.level != null) {
                     RenderTarget scene = StormBloom.sceneTarget(mc);
                     if (scene != null && scene.useDepth && scene.getDepthTextureView() != null) {
                        Vec3 eye = camera.pos;
                        float radius = (float)((double)((float)level * 2.35F) * Math.max(0.05, DevouringStormsClientConfig.impactLightSize));
                        float brightness = (float)((double)(0.75F * ((float)level / 15.0F)) * Math.max((double)0.0F, DevouringStormsClientConfig.impactLightBrightness));
                        float core = (float)((double)6.0F * Math.max(0.05, DevouringStormsClientConfig.impactLightSize));
                        if (!(brightness <= 0.0F) && !(radius <= 0.0F)) {
                           if (DevouringStormsClientConfig.impactLightUseBeamColor) {
                              tintR = (float)Mth.lerp((double)0.45F, Mth.clamp(DevouringStormsClientConfig.beamColorR, (double)0.0F, (double)1.0F), (double)1.0F);
                              tintG = (float)Mth.lerp((double)0.45F, Mth.clamp(DevouringStormsClientConfig.beamColorG, (double)0.0F, (double)1.0F), (double)1.0F);
                              tintB = (float)Mth.lerp((double)0.45F, Mth.clamp(DevouringStormsClientConfig.beamColorB, (double)0.0F, (double)1.0F), (double)1.0F);
                           } else {
                              tintB = 1.0F;
                              tintG = 1.0F;
                              tintR = 1.0F;
                           }

                           tintA = brightness;
                           count = 0;
                           far = 0;
                           double maxRange = DevouringStormsClientConfig.impactLightRange;
                           double rangeSq = maxRange * maxRange;

                           for(Entity entity : mc.level.entitiesForRendering()) {
                              if (entity instanceof WitherStormHeadEntity) {
                                 WitherStormHeadEntity head = (WitherStormHeadEntity)entity;
                                 if (head.isBeamActive()) {
                                    Vec3 end = head.clientBeamEnd != null ? head.clientBeamEnd : head.getBeamEndExact();
                                    if (end != null) {
                                       offer(end, HEIGHT_ABOVE_IMPACT, head.beamScale(), radius, core, tintR, tintG, tintB, tintA, eye, rangeSq);
                                    }
                                 }
                              } else if (entity instanceof WitherStormEntity) {
                                 WitherStormEntity storm = (WitherStormEntity)entity;
                                 if (storm.getPhase() >= 4.0F) {
                                    offerStormBody(storm.position(), storm.getPhase(), storm.getExpansionPhase(), eye, rangeSq, radius, brightness);
                                 }
                              }
                           }

                           for(ClientDistantStormManager.StormData storm : DevouringStormsClientConfig.distantStorms ? ClientDistantStormManager.all() : List.<ClientDistantStormManager.StormData>of()) {
                              if (mc.level.getEntity(storm.entityId) == null) {
                                 for(int i = 0; i < storm.beamActive.length; ++i) {
                                    if (storm.beamActive[i]) {
                                       Vec3 end = storm.dispBeamEnd[i] != null ? storm.dispBeamEnd[i] : storm.beamEnd[i];
                                       if (end != null) {
                                          offer(end, HEIGHT_ABOVE_IMPACT, 1.0F, radius, core, tintR, tintG, tintB, tintA, eye, rangeSq);
                                       }
                                    }
                                 }

                                 if (storm.phase >= 4.0F) {
                                    offerStormBody(new Vec3(storm.dispX, storm.dispY, storm.dispZ), storm.phase, storm.expansionPhase, eye, rangeSq, radius, brightness);
                                 }
                              }
                           }

                           if (count != 0) {
                              if (far > count) {
                                 noteOverflow(far);
                              }

                              for(int i = count * 4; i < POS.length; ++i) {
                                 POS[i] = 0.0F;
                                 COLOR[i] = 0.0F;
                                 SHAPE[i] = 1.0F;
                              }

                              Matrix4f invViewProj = (new Matrix4f(camera.projectionMatrix)).mul(camera.viewRotationMatrix).invert();

                              try {
                                 int size = (new Std140SizeCalculator()).putMat4f().putVec4().align(16).get() + 384;
                                 ByteBuffer data = staging(size);
                                 Std140Builder builder = Std140Builder.intoBuffer(data);
                                 builder.putMat4f(invViewProj);
                                 builder.putVec4((float)count, 0.0F, 0.0F, 0.0F);

                                 for(int i = 0; i < 8; ++i) {
                                    builder.putVec4(POS[i * 4], POS[i * 4 + 1], POS[i * 4 + 2], POS[i * 4 + 3]);
                                 }

                                 for(int i = 0; i < 8; ++i) {
                                    builder.putVec4(COLOR[i * 4], COLOR[i * 4 + 1], COLOR[i * 4 + 2], COLOR[i * 4 + 3]);
                                 }

                                 for(int i = 0; i < 8; ++i) {
                                    builder.putVec4(SHAPE[i * 4], 0.0F, 0.0F, 0.0F);
                                 }

                                 data.rewind();
                                 GpuBuffer ubo = GpuBufferPool.write("dabyws impact light cfg", 128, data);
                                 RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "dabyws impact light", scene.getColorTextureView(), Optional.empty());

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
                              } catch (Exception e) {
                                 failed = true;
                                 System.out.println("[devouringstorms] storm impact/body light DISABLED after an error: " + String.valueOf(e));
                                 e.printStackTrace();
                              }

                           }
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
      return Identifier.fromNamespaceAndPath("devouringstorms", path);
   }
}
