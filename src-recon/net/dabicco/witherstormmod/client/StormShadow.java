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
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class StormShadow {
   private static final double MIN_PHASE = 4.0;
   private static final float SUN_FADE_IN = 0.06F;
   private static final float SUN_FULL = 0.3F;
   private static final double SHADOW_REACH_MAX = 420.0;
   private static final float EXTENT = 46.0F;
   private static final float EXTENT_PER_PHASE = 22.0F;
   private static final float EXTENT_SEVERED = 52.0F;
   private static final float CENTRE_Y = 12.0F;
   private static final float CENTRE_Y_PER_PHASE = 3.5F;
   private static final float DEPTH_BIAS = 0.0015F;
   private static float cameraShadow;
   private static float cameraShadowSmooth;
   private static RenderPipeline pipeline;
   private static ByteBuffer staging;
   private static boolean failed;

   private StormShadow() {
   }

   public static float cameraShadowAmount() {
      return cameraShadowSmooth;
   }

   private static void updateCameraShadow(Vec3 eye, Vec3 sun, Vec3 middle, float extent, float strength) {
      Vec3 rel = eye.subtract(middle);
      double along = -rel.dot(sun);
      if (along <= 0.0) {
         cameraShadow = 0.0F;
      } else {
         double miss = rel.add(sun.scale(along)).length();
         float inside = (float)Mth.clamp(1.0 - (miss - extent * 0.55) / (extent * 0.45), 0.0, 1.0);
         cameraShadow = inside * strength;
      }
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         pipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()})
            .withLocation(id("pipeline/storm_shadow"))
            .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
            .withFragmentShader(id("post/storm_shadow"))
            .withBindGroupLayout(
               BindGroupLayout.builder()
                  .withSampler("DepthSampler")
                  .withSampler("ShadowSampler")
                  .withSampler("GroundSampler")
                  .withUniform("ShadowConfig", UniformType.UNIFORM_BUFFER)
                  .build()
            )
            .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ZERO, BlendFactor.SRC_COLOR, BlendFactor.ZERO, BlendFactor.ONE)))
            .build();
      }

      return pipeline;
   }

   public static Vec3 sunDirection(Minecraft mc) {
      if (mc.level != null && mc.level.dimensionType().hasSkyLight()) {
         float f = (float)(mc.level.getDefaultClockTime() % 24000L) / 24000.0F - 0.25F;
         if (f < 0.0F) {
            f++;
         }

         float eased = 1.0F - (float)((Math.cos(f * Math.PI) + 1.0) / 2.0);
         float a = (f + (eased - f) / 3.0F) * (float) (Math.PI * 2);
         Vec3 dir = new Vec3(-Math.sin(a), Math.cos(a), 0.0);
         return dir.y <= 0.0 ? null : dir;
      } else {
         return null;
      }
   }

   private static double highestGroundNear(Minecraft mc, Vec3 at) {
      return mc.level == null ? at.y : mc.level.getHeight(Types.MOTION_BLOCKING, Mth.floor(at.x), Mth.floor(at.z));
   }

   public static Vector3f sunDirectionF() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return null;
      } else {
         Vec3 sun = sunDirection(mc);
         return sun == null ? null : new Vector3f((float)sun.x, (float)sun.y, (float)sun.z);
      }
   }

   public static void render(CameraRenderState camera) {
      if (!failed && camera != null) {
         if (!net.dabicco.witherstormmod.client.StormShadowMap.wanted()) {
            net.dabicco.witherstormmod.client.StormShadowMap.status(
               "off: disabled in Effects, strength 0, a shader pack is active, or an earlier error switched it off"
            );
         } else {
            float strength = (float)DabyWSClientConfig.stormShadowStrength;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
               Vec3 sun = sunDirection(mc);
               if (sun == null) {
                  cameraShadow = 0.0F;
                  net.dabicco.witherstormmod.client.StormShadowMap.status("no shadow: the sun is below the horizon (or this dimension has no sky)");
               } else {
                  float altitude = Mth.clamp(((float)sun.y - 0.06F) / 0.24000001F, 0.0F, 1.0F);
                  if (altitude <= 0.0F) {
                     net.dabicco.witherstormmod.client.StormShadowMap.status("no shadow: the sun is too near the horizon");
                  } else {
                     cameraShadowSmooth = cameraShadowSmooth + (cameraShadow - cameraShadowSmooth) * 0.12F;
                     if (cameraShadowSmooth < 0.004F) {
                        cameraShadowSmooth = 0.0F;
                     }

                     RenderTarget scene = net.dabicco.witherstormmod.client.StormBloom.sceneTarget(mc);
                     if (scene != null && scene.useDepth && scene.getDepthTextureView() != null) {
                        Vec3 eye = camera.pos;
                        WitherStormEntity nearest = null;
                        double bestSq = Double.MAX_VALUE;

                        for (Entity entity : mc.level.entitiesForRendering()) {
                           if (entity instanceof WitherStormEntity storm && !(storm.getPhase() < 4.0)) {
                              double d = storm.position().distanceToSqr(eye);
                              if (d < bestSq) {
                                 bestSq = d;
                                 nearest = storm;
                              }
                           }
                        }

                        if (nearest == null) {
                           cameraShadow = 0.0F;
                           net.dabicco.witherstormmod.client.StormShadowMap.status("no shadow: no phase-4+ storm is loaded on this client");
                        } else {
                           float over = (float)Math.max(0.0, nearest.getPhase() - 4.0);
                           float extent = 46.0F + 22.0F * over;
                           if (nearest.getPhase() >= 6.0) {
                              extent += 52.0F;
                           }

                           Vec3 middle = nearest.position().add(0.0, 12.0F + 3.5F * over, 0.0);
                           Vector3f centre = new Vector3f((float)(middle.x - eye.x), (float)(middle.y - eye.y), (float)(middle.z - eye.z));
                           updateCameraShadow(eye, sun, middle, extent, strength * altitude);
                           double drop = Math.max(0.0, middle.y - highestGroundNear(mc, middle));
                           double slide = Mth.clamp(sun.y > 0.08 ? drop / sun.y : 420.0, 0.0, 420.0);
                           Vec3 landing = middle.subtract(sun.x * slide, 0.0, sun.z * slide);
                           Vec3 gridMiddle = new Vec3((middle.x + landing.x) * 0.5, middle.y, (middle.z + landing.z) * 0.5);
                           float gridExtent = extent + (float)(middle.subtract(landing).horizontalDistance() * 0.5) + 48.0F;
                           net.dabicco.witherstormmod.client.StormShadowMap.captureTerrain(mc.level, gridMiddle, eye, gridExtent);
                           if (net.dabicco.witherstormmod.client.StormShadowMap.build(new Vector3f((float)sun.x, (float)sun.y, (float)sun.z), centre, extent)) {
                              GpuTextureView shadowDepth = net.dabicco.witherstormmod.client.StormShadowMap.depthView();
                              GpuTextureView groundDepth = net.dabicco.witherstormmod.client.StormShadowMap.groundView();
                              if (groundDepth == null) {
                                 groundDepth = shadowDepth;
                              }

                              if (shadowDepth == null) {
                                 net.dabicco.witherstormmod.client.StormShadowMap.status("no shadow: the shadow map has no depth texture");
                              } else {
                                 Matrix4f viewProj = new Matrix4f(camera.projectionMatrix).mul(camera.viewRotationMatrix);
                                 Matrix4f invViewProj = new Matrix4f(viewProj).invert();

                                 try {
                                    int size = new Std140SizeCalculator()
                                       .putMat4f()
                                       .putMat4f()
                                       .putVec4()
                                       .putMat4f()
                                       .putVec4()
                                       .putVec4()
                                       .putMat4f()
                                       .putVec4()
                                       .get();
                                    ByteBuffer data = staging(size);
                                    Std140Builder.intoBuffer(data)
                                       .putMat4f(invViewProj)
                                       .putMat4f(net.dabicco.witherstormmod.client.StormShadowMap.lightViewProj())
                                       .putVec4(
                                          strength * altitude,
                                          0.0015F,
                                          1.0F / net.dabicco.witherstormmod.client.StormShadowMap.resolution(),
                                          net.dabicco.witherstormmod.client.StormShadowMap.hasGround() ? 1.0F : 0.0F
                                       )
                                       .putMat4f(viewProj)
                                       .putVec4((float)sun.x, (float)sun.y, (float)sun.z, DabyWSClientConfig.stormSelfShadow ? 1.0F : 0.0F)
                                       .putVec4(
                                          (float)DabyWSClientConfig.stormShadowR,
                                          (float)DabyWSClientConfig.stormShadowG,
                                          (float)DabyWSClientConfig.stormShadowB,
                                          DabyWSClientConfig.stormShadow ? 1.0F : 0.0F
                                       )
                                       .putMat4f(net.dabicco.witherstormmod.client.StormShadowMap.groundViewProj())
                                       .putVec4(
                                          (float)DabyWSClientConfig.stormShadingContrast, DabyWSClientConfig.stormShadowSoftEdge ? 1.0F : 0.0F, 0.0F, 0.0F
                                       );
                                    data.rewind();
                                    GpuBuffer ubo = net.dabicco.witherstormmod.client.GpuBufferPool.write("dabyws shadow cfg", 128, data);
                                    RenderPass pass = RenderSystem.getDevice()
                                       .createCommandEncoder()
                                       .createRenderPass(() -> "dabyws storm shadow", scene.getColorTextureView(), Optional.empty());

                                    try {
                                       pass.setPipeline(pipeline());
                                       pass.setUniform("ShadowConfig", ubo);
                                       GpuSampler point = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
                                       pass.bindTexture("DepthSampler", scene.getDepthTextureView(), point);
                                       pass.bindTexture("ShadowSampler", shadowDepth, point);
                                       pass.bindTexture("GroundSampler", groundDepth, point);
                                       pass.draw(3, 1, 0, 0);
                                    } catch (Throwable var321) {
                                       if (pass != null) {
                                          try {
                                             pass.close();
                                          } catch (Throwable var31) {
                                             var321.addSuppressed(var31);
                                          }
                                       }

                                       throw var321;
                                    }

                                    if (pass != null) {
                                       pass.close();
                                    }
                                 } catch (Exception var33) {
                                    failed = true;
                                    System.out.println("[dabywitherstormmod] storm shadow DISABLED after an error: " + var33);
                                    var33.printStackTrace();
                                 }
                              }
                           }
                        }
                     } else {
                        net.dabicco.witherstormmod.client.StormShadowMap.status(
                           "no shadow: no scene depth buffer to read (scene target is " + (scene == null ? "null" : "present but has no depth") + ")"
                        );
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
