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
import java.util.Optional;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class StormSunGlow {
   private static final float CORE_TIGHTNESS = 120.0F;
   private static RenderPipeline pipeline;
   private static ByteBuffer staging;
   private static boolean failed;

   private StormSunGlow() {
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         pipeline = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.dabyws$postProcessingSnippet()})
            .withLocation(id("pipeline/storm_sun_glow"))
            .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
            .withFragmentShader(id("post/storm_sun_glow"))
            .withBindGroupLayout(BindGroupLayout.builder().withSampler("DepthSampler").withUniform("SunGlowConfig", UniformType.UNIFORM_BUFFER).build())
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .build();
      }

      return pipeline;
   }

   public static void render(CameraRenderState camera) {
      if (!failed && camera != null && DabyWSClientConfig.sunGlow && !net.dabicco.witherstormmod.client.ShaderPackCompat.active()) {
         float gloom = net.dabicco.witherstormmod.client.StormSkyDarken.factor();
         float strength = gloom * (float)DabyWSClientConfig.sunGlowStrength;
         if (!(strength <= 0.002F)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
               Vec3 sun = net.dabicco.witherstormmod.client.StormShadow.sunDirection(mc);
               if (sun != null) {
                  float rise = Mth.clamp((float)sun.y / 0.22F, 0.0F, 1.0F);
                  strength *= rise * rise * (3.0F - 2.0F * rise);
                  if (!(strength <= 0.002F)) {
                     RenderTarget scene = net.dabicco.witherstormmod.client.StormBloom.sceneTarget(mc);
                     if (scene != null && scene.useDepth && scene.getDepthTextureView() != null) {
                        Matrix4f invViewProj = new Matrix4f(camera.projectionMatrix).mul(camera.viewRotationMatrix).invert();

                        try {
                           int size = new Std140SizeCalculator().putMat4f().putVec4().putVec4().get();
                           ByteBuffer data = staging(size);
                           Std140Builder.intoBuffer(data)
                              .putMat4f(invViewProj)
                              .putVec4((float)sun.x, (float)sun.y, (float)sun.z, strength)
                              .putVec4((float)DabyWSClientConfig.sunGlowR, (float)DabyWSClientConfig.sunGlowG, (float)DabyWSClientConfig.sunGlowB, 120.0F);
                           data.rewind();
                           GpuBuffer ubo = net.dabicco.witherstormmod.client.GpuBufferPool.write("dabyws sun glow cfg", 128, data);
                           RenderPass pass = RenderSystem.getDevice()
                              .createCommandEncoder()
                              .createRenderPass(() -> "dabyws sun glow", scene.getColorTextureView(), Optional.empty());

                           try {
                              pass.setPipeline(pipeline());
                              pass.setUniform("SunGlowConfig", ubo);
                              pass.bindTexture("DepthSampler", scene.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                              pass.draw(3, 1, 0, 0);
                           } catch (Throwable var15) {
                              if (pass != null) {
                                 try {
                                    pass.close();
                                 } catch (Throwable var14) {
                                    var15.addSuppressed(var14);
                                 }
                              }

                              throw var15;
                           }

                           if (pass != null) {
                              pass.close();
                           }
                        } catch (Exception var16) {
                           failed = true;
                           System.out.println("[dabywitherstormmod] sun glow DISABLED after an error: " + var16);
                           var16.printStackTrace();
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
