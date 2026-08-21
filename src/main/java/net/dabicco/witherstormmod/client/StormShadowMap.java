package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.mixin.CubePolygonsAccessor;
import net.dabicco.witherstormmod.mixin.ModelPartAccessor;
import net.dabicco.witherstormmod.mixin.ModelPartCubesAccessor;
import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public final class StormShadowMap {
   private static int builtSize = 0;
   private static final int GROUND_SIZE = 1024;
   private static final float GROUND_SPAN = 512.0F;
   private static final double LIGHT_DISTANCE = (double)260.0F;
   private static final float RECEIVER_REACH = 512.0F;
   private static RenderTarget target;
   private static RenderTarget groundTarget;
   private static final Matrix4f groundViewProj = new Matrix4f();
   private static boolean haveGround;
   private static long groundDrawnAt = Long.MIN_VALUE;
   private static Vec3 groundDrawnEye;
   private static RenderPipeline pipeline;
   private static ByteBuffer uniformStaging;
   private static final Matrix4f lightViewProj = new Matrix4f();
   private static boolean haveGeometry;
   private static boolean failed;
   private static float[] verts = new float['쀀'];
   private static int vertCount;
   private static float minX;
   private static float minY;
   private static float minZ;
   private static float maxX;
   private static float maxY;
   private static float maxZ;
   private static float[] terrainVerts = new float[98304];
   private static int terrainVertCount;
   private static float terrainMinX;
   private static float terrainMaxX;
   private static float terrainMinZ;
   private static float terrainMaxZ;
   private static double terrainAtX = Double.NaN;
   private static double terrainAtZ = Double.NaN;
   private static long terrainBuiltAt = Long.MIN_VALUE;
   private static final int TERRAIN_MAX_CELLS = 192;
   private static final double TERRAIN_MOVE = (double)6.0F;
   private static final long TERRAIN_INTERVAL_MS = 500L;
   private static float cullSunX;
   private static float cullSunY;
   private static float cullSunZ;
   private static boolean cullAway;
   private static float[] terrainCache;
   private static int terrainCacheCells;
   private static int terrainOriginX;
   private static int terrainOriginZ;
   private static int terrainCacheStep = 2;
   private static String lastStatus = "";
   private static final Map<ModelPart, PartGeom> GEOM = new IdentityHashMap();
   private static final int MAX_PART_GEOM = 2048;
   private static ByteBuffer vertexStaging;
   private static FloatBuffer vertexStagingF;
   private static GpuBuffer stormVbo;
   private static GpuBuffer groundVbo;
   private static int wantedSize;
   private static long wantedSince;
   private static final long REBUILD_SETTLE_MS = 400L;

   private StormShadowMap() {
   }

   private static int size() {
      return (int)DabyWSClientConfig.shadowMapResolution;
   }

   private static int terrainStep() {
      return DabyWSClientConfig.stormShadowTerrain ? 2 : 8;
   }

   public static void faceCulling(Vector3fc sun) {
      cullAway = sun != null && DabyWSClientConfig.shadowCullBackFaces;
      if (cullAway) {
         cullSunX = sun.x();
         cullSunY = sun.y();
         cullSunZ = sun.z();
      }

   }

   public static void beginFrame() {
      faceCulling(StormShadow.sunDirectionF());
      vertCount = 0;
      haveGeometry = false;
   }

   public static void captureTerrain(ClientLevel level, Vec3 centre, Vec3 eye, float extent) {
      terrainVertCount = 0;
      int TERRAIN_STEP = terrainStep();
      long now = System.currentTimeMillis();
      boolean moved = Double.isNaN(terrainAtX) || Math.abs(centre.x - terrainAtX) > (double)6.0F || Math.abs(centre.z - terrainAtZ) > (double)6.0F;
      if (!moved && now - terrainBuiltAt < 500L && terrainCache != null) {
         emitTerrainCache(eye);
      } else {
         terrainAtX = centre.x;
         terrainAtZ = centre.z;
         terrainBuiltAt = now;
         int reach = Math.max(16, Mth.ceil(extent)) + TERRAIN_STEP;

         int step;
         for(step = TERRAIN_STEP; reach * 2 / step + 1 > 192; step *= 2) {
         }

         int cells = reach * 2 / step + 1;
         if (terrainCache == null || terrainCacheCells != cells || terrainCacheStep != step) {
            terrainCache = new float[cells * cells];
            terrainCacheCells = cells;
         }

         terrainCacheStep = step;
         terrainOriginX = Mth.floor(centre.x) - reach;
         terrainOriginZ = Mth.floor(centre.z) - reach;

         for(int ix = 0; ix < cells; ++ix) {
            for(int iz = 0; iz < cells; ++iz) {
               int wx = terrainOriginX + ix * step;
               int wz = terrainOriginZ + iz * step;
               float lowest = Float.MAX_VALUE;

               for(int ox = -1; ox <= 1; ++ox) {
                  for(int oz = -1; oz <= 1; ++oz) {
                     lowest = Math.min(lowest, (float)level.getHeight(Types.MOTION_BLOCKING, wx + ox * TERRAIN_STEP, wz + oz * TERRAIN_STEP));
                  }
               }

               terrainCache[ix * cells + iz] = lowest;
            }
         }

         emitTerrainCache(eye);
      }
   }

   private static void emitTerrainCache(Vec3 eye) {
      if (terrainCache != null) {
         int TERRAIN_STEP = terrainCacheStep;
         int cells = terrainCacheCells;
         int need = (cells - 1) * (cells - 1) * 4 * 3;
         if (terrainVerts.length < need) {
            terrainVerts = new float[need];
         }

         terrainVertCount = 0;
         terrainMinX = (float)((double)terrainOriginX - eye.x);
         terrainMinZ = (float)((double)terrainOriginZ - eye.z);
         terrainMaxX = terrainMinX + (float)((cells - 1) * TERRAIN_STEP);
         terrainMaxZ = terrainMinZ + (float)((cells - 1) * TERRAIN_STEP);

         for(int ix = 0; ix < cells - 1; ++ix) {
            for(int iz = 0; iz < cells - 1; ++iz) {
               float x0 = (float)((double)(terrainOriginX + ix * TERRAIN_STEP) - eye.x);
               float z0 = (float)((double)(terrainOriginZ + iz * TERRAIN_STEP) - eye.z);
               float x1 = x0 + (float)TERRAIN_STEP;
               float z1 = z0 + (float)TERRAIN_STEP;
               float h = Math.min(Math.min(terrainCache[ix * cells + iz], terrainCache[(ix + 1) * cells + iz]), Math.min(terrainCache[ix * cells + iz + 1], terrainCache[(ix + 1) * cells + iz + 1]));
               float y = (float)((double)h - eye.y);
               pushTerrain(x0, y, z0);
               pushTerrain(x1, y, z0);
               pushTerrain(x1, y, z1);
               pushTerrain(x0, y, z1);
            }
         }

      }
   }

   private static void pushTerrain(float x, float y, float z) {
      int i = terrainVertCount * 3;
      terrainVerts[i] = x;
      terrainVerts[i + 1] = y;
      terrainVerts[i + 2] = z;
      ++terrainVertCount;
   }

   public static void status(String reason) {
      if (!reason.equals(lastStatus)) {
         lastStatus = reason;
         System.out.println("[dabywitherstormmod][shadow] " + reason);
      }
   }

   public static int capturedVertices() {
      return vertCount;
   }

   public static boolean wanted() {
      return !failed && (DabyWSClientConfig.stormShadow || DabyWSClientConfig.stormSelfShadow) && DabyWSClientConfig.stormShadowStrength > (double)0.0F && !ShaderPackCompat.active();
   }

   public static void capture(PoseStack pose, Model model) {
      capture(pose, model.root());
   }

   public static void capture(PoseStack pose, ModelPart part) {
      if (wanted()) {
         try {
            walk(part, pose);
            haveGeometry = vertCount > 0;
         } catch (Exception e) {
            failed = true;
            System.out.println("[dabywitherstormmod] storm shadow capture FAILED, shadows off: " + String.valueOf(e));
         }

      }
   }

   private static PartGeom geomFor(ModelPart part) {
      PartGeom hit = (PartGeom)GEOM.get(part);
      if (hit != null) {
         return hit;
      } else {
         if (GEOM.size() > 2048) {
            GEOM.clear();
         }

         List<ModelPart.Cube> cubes = ((ModelPartCubesAccessor)(Object)part).dabyws$getCubes();
         int quads = 0;

         for(ModelPart.Cube cube : cubes) {
            quads += ((CubePolygonsAccessor)cube).dabyws$getPolygons().length;
         }

         PartGeom g = new PartGeom();
         g.quads = quads;
         g.pos = new float[quads * 12];
         g.nrm = new float[quads * 3];
         int p = 0;
         int nq = 0;

         for(ModelPart.Cube cube : cubes) {
            for(ModelPart.Polygon poly : ((CubePolygonsAccessor)cube).dabyws$getPolygons()) {
               Vector3fc n = poly.normal();
               g.nrm[nq * 3] = n.x();
               g.nrm[nq * 3 + 1] = n.y();
               g.nrm[nq * 3 + 2] = n.z();
               ++nq;

               for(ModelPart.Vertex vert : poly.vertices()) {
                  g.pos[p++] = vert.worldX();
                  g.pos[p++] = vert.worldY();
                  g.pos[p++] = vert.worldZ();
               }
            }
         }

         GEOM.put(part, g);
         return g;
      }
   }

   private static void walk(ModelPart part, PoseStack pose) {
      if (part.visible) {
         pose.pushPose();
         part.translateAndRotate(pose);
         Matrix4f m = pose.last().pose();
         Matrix3f nm = pose.last().normal();
         PartGeom g = geomFor(part);
         if (g.quads > 0) {
            float a00 = m.m00();
            float a01 = m.m01();
            float a02 = m.m02();
            float a10 = m.m10();
            float a11 = m.m11();
            float a12 = m.m12();
            float a20 = m.m20();
            float a21 = m.m21();
            float a22 = m.m22();
            float a30 = m.m30();
            float a31 = m.m31();
            float a32 = m.m32();
            float b00 = nm.m00();
            float b01 = nm.m01();
            float b02 = nm.m02();
            float b10 = nm.m10();
            float b11 = nm.m11();
            float b12 = nm.m12();
            float b20 = nm.m20();
            float b21 = nm.m21();
            float b22 = nm.m22();
            float[] gp = g.pos;
            float[] gn = g.nrm;

            for(int q = 0; q < g.quads; ++q) {
               if (cullAway) {
                  float nx = gn[q * 3];
                  float ny = gn[q * 3 + 1];
                  float nz = gn[q * 3 + 2];
                  float tx = b00 * nx + b10 * ny + b20 * nz;
                  float ty = b01 * nx + b11 * ny + b21 * nz;
                  float tz = b02 * nx + b12 * ny + b22 * nz;
                  if (tx * cullSunX + ty * cullSunY + tz * cullSunZ <= 0.0F) {
                     continue;
                  }
               }

               if (vertCount * 3 + 12 > verts.length) {
                  verts = Arrays.copyOf(verts, verts.length * 2);
               }

               int base = q * 12;

               for(int c = 0; c < 4; ++c) {
                  float x = gp[base + c * 3];
                  float y = gp[base + c * 3 + 1];
                  float z = gp[base + c * 3 + 2];
                  float wx = a00 * x + a10 * y + a20 * z + a30;
                  float wy = a01 * x + a11 * y + a21 * z + a31;
                  float wz = a02 * x + a12 * y + a22 * z + a32;
                  verts[vertCount * 3] = wx;
                  verts[vertCount * 3 + 1] = wy;
                  verts[vertCount * 3 + 2] = wz;
                  if (vertCount == 0) {
                     maxX = wx;
                     minX = wx;
                     maxY = wy;
                     minY = wy;
                     maxZ = wz;
                     minZ = wz;
                  } else {
                     if (wx < minX) {
                        minX = wx;
                     } else if (wx > maxX) {
                        maxX = wx;
                     }

                     if (wy < minY) {
                        minY = wy;
                     } else if (wy > maxY) {
                        maxY = wy;
                     }

                     if (wz < minZ) {
                        minZ = wz;
                     } else if (wz > maxZ) {
                        maxZ = wz;
                     }
                  }

                  ++vertCount;
               }
            }
         }

         for(ModelPart child : ((ModelPartAccessor)(Object)part).getChildren().values()) {
            walk(child, pose);
         }

         pose.popPose();
      }
   }

   public static boolean build(Vector3fc sun, Vector3f centre, float radius) {
      if (failed) {
         status("off: a previous frame errored");
         return false;
      } else if (!haveGeometry) {
         status("NO GEOMETRY captured this frame -- the renderer's capture hook never ran (is the storm phase 4+ and actually on screen?)");
         return false;
      } else {
         try {
            ensureTarget();
            if (vertCount > 0) {
               centre = new Vector3f((minX + maxX) * 0.5F, (minY + maxY) * 0.5F, (minZ + maxZ) * 0.5F);
            }

            Vector3f eye = (new Vector3f(sun)).mul(260.0F).add(centre);
            Vector3f up = Math.abs(sun.y()) > 0.99F ? new Vector3f(0.0F, 0.0F, 1.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
            Matrix4f view = (new Matrix4f()).lookAt(eye, centre, up);
            float l = Float.MAX_VALUE;
            float r = -Float.MAX_VALUE;
            float bo = Float.MAX_VALUE;
            float t = -Float.MAX_VALUE;
            float nearest = Float.MAX_VALUE;
            float furthest = -Float.MAX_VALUE;
            if (vertCount > 0) {
               for(int i = 0; i < 8; ++i) {
                  Vector3f corner = new Vector3f((i & 1) == 0 ? minX : maxX, (i & 2) == 0 ? minY : maxY, (i & 4) == 0 ? minZ : maxZ);
                  view.transformPosition(corner);
                  l = Math.min(l, corner.x);
                  r = Math.max(r, corner.x);
                  bo = Math.min(bo, corner.y);
                  t = Math.max(t, corner.y);
                  nearest = Math.min(nearest, -corner.z);
                  furthest = Math.max(furthest, -corner.z);
               }
            } else {
               l = bo = -radius;
               t = radius;
               r = radius;
               nearest = 260.0F - radius;
               furthest = 260.0F + radius;
            }

            float pad = Math.max(1.0F, (r - l) * 0.02F);
            l -= pad;
            r += pad;
            bo -= pad;
            t += pad;
            lightViewProj.setOrtho(l, r, bo, t, Math.max(0.5F, nearest - 8.0F), furthest + 512.0F, true).mul(view);
            haveGround = false;
            if (terrainVertCount > 0) {
               boolean groundStale = groundDrawnAt != terrainBuiltAt || groundDrawnEye == null || groundDrawnEye.distanceToSqr((double)centre.x, (double)centre.y, (double)centre.z) > (double)1.0F;
               ensureGroundTarget();
               float gcx = (terrainMinX + terrainMaxX) * 0.5F;
               float gcz = (terrainMinZ + terrainMaxZ) * 0.5F;
               float groundHalf = Math.max(4.0F, 0.5F * Math.max(terrainMaxX - terrainMinX, terrainMaxZ - terrainMinZ));
               Vector3f groundAt = new Vector3f(gcx, centre.y, gcz);
               Vector3f above = new Vector3f(gcx, centre.y + 512.0F, gcz);
               groundViewProj.setOrtho(-groundHalf, groundHalf, -groundHalf, groundHalf, 0.0F, 1024.0F, true).lookAt(above, groundAt, new Vector3f(0.0F, 0.0F, -1.0F));
               if (groundStale) {
                  drawGround();
                  groundDrawnAt = terrainBuiltAt;
                  groundDrawnEye = new Vec3((double)centre.x, (double)centre.y, (double)centre.z);
               }

               haveGround = true;
            }

            uploadAndDraw();
            status("drawing, " + vertCount + " storm vertices, " + terrainVertCount + " ground vertices");
            return true;
         } catch (Exception e) {
            failed = true;
            System.out.println("[dabywitherstormmod] storm shadow map FAILED, shadows off: " + String.valueOf(e));
            e.printStackTrace();
            return false;
         }
      }
   }

   private static void uploadAndDraw() {
      RenderSystem.AutoStorageIndexBuffer indexer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
      int stormIndices = vertCount / 4 * 6;
      GpuBuffer indices = indexer.getBuffer(stormIndices);
      stormVbo = writeVerts(stormVbo, "dabyws shadow verts", verts, vertCount);
      if (stormVbo != null) {
         GpuBuffer stormUbo = uploadMatrix("dabyws shadow storm", lightViewProj, 1.0F);
         RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "dabyws storm shadow map", target.getColorTextureView(), Optional.of(new Vector4f(0.0F, 0.0F, 0.0F, 1.0F)), target.getDepthTextureView(), OptionalDouble.of((double)1.0F));

         try {
            pass.setPipeline(pipeline());
            pass.setIndexBuffer(indices, indexer.type());
            pass.setUniform("ShadowCasterConfig", stormUbo);
            pass.setVertexBuffer(0, stormVbo.slice(0L, (long)(vertCount * 3 * 4)));
            pass.drawIndexed(stormIndices, 1, 0, 0, 0);
         } catch (Throwable var8) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (pass != null) {
            pass.close();
         }

      }
   }

   private static void drawGround() {
      RenderSystem.AutoStorageIndexBuffer indexer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
      int indexCount = terrainVertCount / 4 * 6;
      GpuBuffer indices = indexer.getBuffer(indexCount);
      groundVbo = writeVerts(groundVbo, "dabyws ground verts", terrainVerts, terrainVertCount);
      if (groundVbo != null) {
         GpuBuffer ubo = uploadMatrix("dabyws ground matrix", groundViewProj, 0.0F);
         RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "dabyws ground height map", groundTarget.getColorTextureView(), Optional.of(new Vector4f(0.0F, 0.0F, 0.0F, 1.0F)), groundTarget.getDepthTextureView(), OptionalDouble.of((double)1.0F));

         try {
            pass.setPipeline(pipeline());
            pass.setUniform("ShadowCasterConfig", ubo);
            pass.setVertexBuffer(0, groundVbo.slice(0L, (long)(terrainVertCount * 3 * 4)));
            pass.setIndexBuffer(indices, indexer.type());
            pass.drawIndexed(indexCount, 1, 0, 0, 0);
         } catch (Throwable var8) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (pass != null) {
            pass.close();
         }

      }
   }

   private static void ensureGroundTarget() {
      if (groundTarget == null) {
         groundTarget = new TextureTarget("dabyws_ground_map", 1024, 1024, true, GpuFormat.RGBA8_UNORM);
      }

   }

   public static GpuTextureView groundView() {
      return groundTarget == null ? null : groundTarget.getDepthTextureView();
   }

   public static Matrix4f groundViewProj() {
      return groundViewProj;
   }

   public static boolean hasGround() {
      return haveGround;
   }

   private static GpuBuffer writeVerts(GpuBuffer existing, String name, float[] src, int count) {
      int floats = count * 3;
      int bytes = floats * 4;
      if (bytes == 0) {
         return existing;
      } else {
         stageVerts(src, floats, bytes);
         GpuBuffer buffer = existing;
         if (existing == null || existing.size() < (long)bytes) {
            if (existing != null) {
               existing.close();
            }

            buffer = RenderSystem.getDevice().createBuffer(() -> name, 40, (long)bytes * 2L);
         }

         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(0L, (long)bytes), vertexStaging);
         return buffer;
      }
   }

   private static void stageVerts(float[] src, int floats, int bytes) {
      if (vertexStaging == null || vertexStaging.capacity() < bytes) {
         vertexStaging = ByteBuffer.allocateDirect(Math.max(bytes, 1) * 2).order(ByteOrder.nativeOrder());
         vertexStagingF = vertexStaging.asFloatBuffer();
      }

      vertexStagingF.clear();
      vertexStagingF.put(src, 0, floats);
      vertexStaging.limit(bytes).position(0);
   }

   private static GpuBuffer uploadMatrix(String name, Matrix4f matrix, float kind) {
      ByteBuffer data = uniformStaging((new Std140SizeCalculator()).putMat4f().putVec4().get());
      Std140Builder.intoBuffer(data).putMat4f(matrix).putVec4(kind, 0.0F, 0.0F, 0.0F);
      data.rewind();
      return GpuBufferPool.write(name, 128, data);
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         pipeline = RenderPipeline.builder(new RenderPipeline.Snippet[]{RenderPipelinesAccessor.dabyws$globalsSnippet()}).withLocation(id("pipeline/storm_shadow_depth")).withVertexShader(id("core/storm_shadow_depth")).withFragmentShader(id("core/storm_shadow_depth")).withVertexBinding(0, DefaultVertexFormat.POSITION).withPrimitiveTopology(PrimitiveTopology.QUADS).withBindGroupLayout(BindGroupLayout.builder().withUniform("ShadowCasterConfig", UniformType.UNIFORM_BUFFER).build()).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, true)).build();
      }

      return pipeline;
   }

   private static void ensureTarget() {
      int want = size();
      if (want != builtSize) {
         long now = Util.getMillis();
         if (want != wantedSize) {
            wantedSize = want;
            wantedSince = now;
         } else if (now - wantedSince >= 400L && target != null) {
            target.destroyBuffers();
            target = null;
         }
      }

      if (target == null) {
         int build = builtSize == 0 ? want : wantedSize;
         target = new TextureTarget("dabyws_shadow_map", build, build, true, GpuFormat.RGBA8_UNORM);
         builtSize = build;
      }

   }

   public static float resolution() {
      return builtSize > 0 ? (float)builtSize : (float)size();
   }

   public static GpuTextureView kindView() {
      return target == null ? null : target.getColorTextureView();
   }

   public static GpuTextureView depthView() {
      return target == null ? null : target.getDepthTextureView();
   }

   public static Matrix4f lightViewProj() {
      return lightViewProj;
   }

   private static ByteBuffer uniformStaging(int bytes) {
      if (uniformStaging == null || uniformStaging.capacity() < bytes) {
         uniformStaging = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
      }

      uniformStaging.clear();
      return uniformStaging;
   }

   public static void close() {
      if (target != null) {
         target.destroyBuffers();
         target = null;
      }

      if (groundTarget != null) {
         groundTarget.destroyBuffers();
         groundTarget = null;
      }

      GpuBufferPool.close();
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
   }

   private static final class PartGeom {
      float[] pos;
      float[] nrm;
      int quads;
   }
}
