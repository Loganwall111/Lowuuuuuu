package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * WorldShadows — natural sun shadows for the ordinary world, storm or no storm.
 *
 * This is the "built-in shader" ground pass: real directional shadows cast by
 * the terrain itself (hills, trees, buildings — anything the heightmap knows
 * about) and by nearby creatures, falling on whatever the camera can see —
 * grass, water, walls, snow. It reuses the storm shadow machinery:
 *
 *  1. the height surface around the player is sampled (throttled, cached) and
 *     emitted as casters into the sun-facing depth map, along with simple
 *     boxes for nearby living entities;
 *  2. the same overhead ground lid is built, so pixels inside caves stay lit
 *     logic-wise and the grid's edge fades instead of cutting;
 *  3. the shared screen-space pass (StormShadow.drawShadowPass) unprojects
 *     the finished frame and shades every pixel the sun cannot reach, tinted
 *     cool blue-grey — what remains in a real shadow is skylight.
 *
 * It switches itself off the moment a phase-4+ storm is loaded nearby: from
 * that point the storm's own (much larger) shadow owns the frame.
 */
public final class WorldShadows {
   /** Half-extent of the shadowed area around the player, in blocks. */
   private static final float EXTENT = 152.0F;
   /** Caster grid step: 2 blocks reads as chunky-Minecraft-correct, not blurry. */
   private static final int CASTER_STEP = 2;
   private static final int MAX_CELLS = 176;
   /** How far away an entity still casts, and how thick its caster box is. */
   private static final double ENTITY_REACH = 44.0;
   private static final float ENTITY_PAD = 0.12F;
   private static final long REBUILD_MS = 400L;

   private static float[] casterQuads;
   private static float[] casterNormals;
   private static int casterFaces;
   private static double builtAtX = Double.NaN;
   private static double builtAtZ = Double.NaN;
   private static long builtAtMs = Long.MIN_VALUE;
   private static String lastStatus = "";

   private WorldShadows() {
   }

   private static void status(String reason) {
      if (!reason.equals(lastStatus)) {
         lastStatus = reason;
         StormShadowMap.status(reason);
      }
   }

   /** True when a local phase-4+ storm owns the shadow pass this frame. */
   private static boolean stormOwnsShadows(Minecraft mc, Vec3 eye) {
      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof WitherStormEntity && ((WitherStormEntity)entity).getPhase() >= (double)4.0F) {
            return true;
         }
      }

      return false;
   }

   /** (Re)build the cached heightmap caster grid if the player has moved or time has passed. */
   private static void buildCasterCache(Level level, Vec3 eye) {
      long now = System.currentTimeMillis();
      boolean moved = Double.isNaN(builtAtX) || Math.abs(eye.x - builtAtX) > (double)6.0F || Math.abs(eye.z - builtAtZ) > (double)6.0F;
      if (!moved && now - builtAtMs < REBUILD_MS && casterQuads != null) {
         return;
      }
      builtAtX = eye.x;
      builtAtZ = eye.z;
      builtAtMs = now;

      int cells = Math.min(MAX_CELLS, (int)(EXTENT * 2.0F / (float)CASTER_STEP) + 1);
      int originX = Mth.floor(eye.x) - cells * CASTER_STEP / 2;
      int originZ = Mth.floor(eye.z) - cells * CASTER_STEP / 2;

      // heights first (min-of-neighbourhood, same trick as the lid: a coarse
      // grid must not let spikes punch holes in the shadow field)
      float[] heights = new float[cells * cells];
      for (int ix = 0; ix < cells; ix++) {
         for (int iz = 0; iz < cells; iz++) {
            int wx = originX + ix * CASTER_STEP;
            int wz = originZ + iz * CASTER_STEP;
            float lowest = Float.MAX_VALUE;
            for (int ox = -1; ox <= 1; ox++) {
               for (int oz = -1; oz <= 1; oz++) {
                  lowest = Math.min(lowest, (float)level.getHeight(Heightmap.Types.MOTION_BLOCKING, wx + ox * CASTER_STEP, wz + oz * CASTER_STEP));
               }
            }
            heights[ix * cells + iz] = lowest;
         }
      }

      int faces = (cells - 1) * (cells - 1);
      if (casterQuads == null || casterQuads.length < faces * 12) {
         casterQuads = new float[faces * 12];
         casterNormals = new float[faces * 3];
      }

      casterFaces = 0;
      for (int ix = 0; ix < cells - 1; ix++) {
         for (int iz = 0; iz < cells - 1; iz++) {
            float h0 = heights[ix * cells + iz];
            float h1 = heights[(ix + 1) * cells + iz];
            float h2 = heights[ix * cells + iz + 1];
            float h3 = heights[(ix + 1) * cells + iz + 1];
            float x0 = (float)(originX + ix * CASTER_STEP - eye.x);
            float z0 = (float)(originZ + iz * CASTER_STEP - eye.z);
            float x1 = x0 + (float)CASTER_STEP;
            float z1 = z0 + (float)CASTER_STEP;
            float y = Math.min(Math.min(h0, h1), Math.min(h2, h3)) - (float)eye.y;
            int q = casterFaces * 12;
            casterQuads[q] = x0;
            casterQuads[q + 1] = y;
            casterQuads[q + 2] = z0;
            casterQuads[q + 3] = x1;
            casterQuads[q + 4] = y;
            casterQuads[q + 5] = z0;
            casterQuads[q + 6] = x1;
            casterQuads[q + 7] = y;
            casterQuads[q + 8] = z1;
            casterQuads[q + 9] = x0;
            casterQuads[q + 10] = y;
            casterQuads[q + 11] = z1;
            casterNormals[casterFaces * 3] = 0.0F;
            casterNormals[casterFaces * 3 + 1] = 1.0F;
            casterNormals[casterFaces * 3 + 2] = 0.0F;
            casterFaces++;
         }
      }
   }

   /** Emit a world-space AABB as six caster quads in camera-relative space. */
   private static void emitBox(java.util.List<Float> quads, java.util.List<Float> normals, AABB box, Vec3 eye) {
      float x0 = (float)(box.minX - eye.x);
      float y0 = (float)(box.minY - eye.y);
      float z0 = (float)(box.minZ - eye.z);
      float x1 = (float)(box.maxX - eye.x);
      float y1 = (float)(box.maxY - eye.y);
      float z1 = (float)(box.maxZ - eye.z);
      emitFace(quads, normals, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0.0F, 1.0F, 0.0F);
      emitFace(quads, normals, x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0, 0.0F, -1.0F, 0.0F);
      emitFace(quads, normals, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0.0F, 0.0F, -1.0F);
      emitFace(quads, normals, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, 1.0F, 0.0F, 0.0F);
      emitFace(quads, normals, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, -1.0F, 0.0F, 0.0F);
      emitFace(quads, normals, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0.0F, 0.0F, 1.0F);
   }

   private static void emitFace(java.util.List<Float> quads, java.util.List<Float> normals, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, float nx, float ny, float nz) {
      quads.add(ax);
      quads.add(ay);
      quads.add(az);
      quads.add(bx);
      quads.add(by);
      quads.add(bz);
      quads.add(cx);
      quads.add(cy);
      quads.add(cz);
      quads.add(dx);
      quads.add(dy);
      quads.add(dz);
      normals.add(nx);
      normals.add(ny);
      normals.add(nz);
   }

   private static float[] toFloatArray(java.util.List<Float> list) {
      float[] out = new float[list.size()];
      for (int i = 0; i < out.length; i++) {
         out[i] = list.get(i);
      }
      return out;
   }

   public static void render(CameraRenderState camera) {
      if (camera == null) {
         return;
      }
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null) {
         return;
      }
      if (!DevouringStormsClientConfig.worldShadows || ShaderPackCompat.active()) {
         status("world shadows off (disabled in Effects, or a shader pack is active)");
         return;
      }
      if (stormOwnsShadows(mc, camera.pos)) {
         status("world shadows paused: a phase-4+ storm owns the shadow pass");
         return;
      }
      Vec3 sun = StormShadow.sunDirection(mc);
      if (sun == null) {
         status("world shadows: the sun is down or this dimension has no sky");
         return;
      }
      float altitude = Mth.clamp(((float)sun.y - 0.06F) / 0.24F, 0.0F, 1.0F);
      if (altitude <= 0.0F) {
         status("world shadows: the sun is too near the horizon");
         return;
      } else {
         Vec3 eye = camera.pos;
         buildCasterCache(mc.level, eye);
         if (casterFaces <= 0) {
            return;
         }

         StormShadowMap.worldActive(true);

         try {
            // the overhead lid (classification + cave gate + edge fade)
            StormShadowMap.captureTerrain(mc.level, eye, eye, EXTENT);
            // casters: the terrain surface itself (cache array is exact-length),
            // then nearby creatures
            PoseStack identity = new PoseStack();
            StormShadowMap.captureCustomQuads(identity, casterQuads, casterNormals, 1.0F);

            java.util.List<Float> boxQuads = new java.util.ArrayList<>();
            java.util.List<Float> boxNormals = new java.util.ArrayList<>();
            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof LivingEntity && entity.isAlive() && !(entity instanceof WitherStormEntity)) {
                  double distSq = entity.distanceToSqr(eye.x, eye.y, eye.z);
                  if (distSq < ENTITY_REACH * ENTITY_REACH) {
                     AABB box = entity.getBoundingBox().inflate(ENTITY_PAD, 0.0, ENTITY_PAD);
                     emitBox(boxQuads, boxNormals, box, eye);
                  }
               }
            }

            if (!boxQuads.isEmpty()) {
               StormShadowMap.captureCustomQuads(identity, toFloatArray(boxQuads), toFloatArray(boxNormals), 1.0F);
            }

            if (StormShadowMap.build(new Vector3f((float)sun.x, (float)sun.y, (float)sun.z), new Vector3f(0.0F, 0.0F, 0.0F), EXTENT)) {
               float strength = (float)DevouringStormsClientConfig.worldShadowStrength * altitude;
               // cool blue-grey: a real shadow keeps only skylight
               StormShadow.drawShadowPass(camera, sun, strength, 0.48F, 0.53F, 0.63F, false, true, 0.0F, DevouringStormsClientConfig.stormShadowSoftEdge, "world");
               status("world shadows drawing (" + casterFaces + " terrain cells)");
            }
         } finally {
            StormShadowMap.worldActive(false);
         }

      }
   }
}
