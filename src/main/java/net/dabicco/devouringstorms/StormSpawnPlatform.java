package net.dabicco.devouringstorms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class StormSpawnPlatform {
   private static final Identifier STRUCTURE = DevouringStormsMod.id("spawn_tower");
   private static final String MARKER = "dabywsmod_spawn_platform.placed";
   private static final int Y_OFFSET = -4;
   private static final int SEARCH_RADIUS = 2000;
   private static final int SEARCH_STEP = 32;
   private static final int SAMPLE_BUDGET = 220000;
   private static int samplesUsed;
   private static final int INLAND_RADIUS = 72;
   private static AABB towerBox;
   private static final int DUST_PER_TICK = 10;
   private static final double DUST_RANGE = (double)40.0F;
   private static final double DUST_SPREAD_XZ = (double)6.0F;
   private static final double DUST_SPREAD_Y = (double)4.0F;
   private static final int FOOTPRINT = 12;
   private static final int MAX_RELIEF = 3;

   private StormSpawnPlatform() {
   }

   public static boolean insideTower(Vec3 at) {
      return towerBox != null && towerBox.contains(at);
   }

   public static Vec3 towerHeart() {
      return towerBox == null ? Vec3.ZERO : new Vec3((towerBox.minX + towerBox.maxX) * (double)0.5F, towerBox.minY + (double)4.0F, (towerBox.minZ + towerBox.maxZ) * (double)0.5F);
   }

   public static AABB towerBox() {
      return towerBox;
   }

   public static BlockPos towerPos() {
      return towerBox == null ? null : BlockPos.containing((towerBox.minX + towerBox.maxX) * (double)0.5F, towerBox.minY, (towerBox.minZ + towerBox.maxZ) * (double)0.5F);
   }

   public static void markTowerAt(MinecraftServer server, BlockPos standingOn, Vec3i size) {
      BlockPos origin = new BlockPos(standingOn.getX() - size.getX() / 2, standingOn.getY() + -4, standingOn.getZ() - size.getZ() / 2);
      recordAndWrite(server, origin, size);
   }

   public static boolean placeTowerAt(MinecraftServer server, BlockPos standingOn) {
      ServerLevel overworld = server.overworld();
      if (overworld == null) {
         return false;
      } else {
         StructureTemplate template = overworld.getStructureManager().get(STRUCTURE).orElse(null);
         if (template == null) {
            return false;
         } else {
            Vec3i size = template.getSize();
            BlockPos origin = new BlockPos(standingOn.getX() - size.getX() / 2, standingOn.getY() + -4, standingOn.getZ() - size.getZ() / 2);
            template.placeInWorld(overworld, origin, origin, new StructurePlaceSettings(), overworld.getRandom(), 2);
            adaptToSand(overworld, origin, size);
            recordAndWrite(server, origin, size);
            return true;
         }
      }
   }

   public static boolean resurfaceTower(MinecraftServer server) {
      ServerLevel overworld = server.overworld();
      if (overworld != null && towerBox != null) {
         BlockPos origin = BlockPos.containing(towerBox.minX, towerBox.minY, towerBox.minZ);
         adaptToSand(overworld, origin, new Vec3i((int)towerBox.getXsize(), (int)towerBox.getYsize(), (int)towerBox.getZsize()));
         return true;
      } else {
         return false;
      }
   }

   public static Vec3i templateSize(MinecraftServer server) {
      ServerLevel overworld = server.overworld();
      return overworld == null ? Vec3i.ZERO : (Vec3i)overworld.getStructureManager().get(STRUCTURE).map(StructureTemplate::getSize).orElse(Vec3i.ZERO);
   }

   private static void recordAndWrite(MinecraftServer server, BlockPos origin, Vec3i size) {
      towerBox = new AABB((double)origin.getX(), (double)origin.getY(), (double)origin.getZ(), (double)(origin.getX() + size.getX()), (double)(origin.getY() + size.getY()), (double)(origin.getZ() + size.getZ()));
      Path marker = server.getWorldPath(LevelResource.ROOT).resolve("dabywsmod_spawn_platform.placed");

      try {
         int var10001 = origin.getX();
         Files.writeString(marker, var10001 + " " + origin.getY() + " " + origin.getZ() + " " + size.getX() + " " + size.getY() + " " + size.getZ());
      } catch (IOException e) {
         DevouringStormsMod.LOGGER.warn("[storm platform] couldn't write marker file", e);
      }

   }

   public static void spawnTowerDust(MinecraftServer server) {
      if (towerBox != null) {
         ServerLevel overworld = server.overworld();
         if (overworld != null) {
            for(ServerPlayer player : overworld.players()) {
               Vec3 eye = player.getEyePosition();
               if (towerBox.inflate((double)40.0F).contains(eye)) {
                  double x = Mth.clamp(eye.x, towerBox.minX + (double)1.5F, towerBox.maxX - (double)1.5F);
                  double y = Mth.clamp(eye.y, towerBox.minY + (double)5.0F, towerBox.maxY - (double)1.5F);
                  double z = Mth.clamp(eye.z, towerBox.minZ + (double)1.5F, towerBox.maxZ - (double)1.5F);
                  overworld.sendParticles(ParticleTypes.WHITE_ASH, x, y, z, 10, (double)6.0F, (double)4.0F, (double)6.0F, (double)0.0F);
               }
            }

         }
      }
   }

   private static void adaptToSand(ServerLevel level, BlockPos origin, Vec3i size) {
      int sandy = 0;
      int sampled = 0;

      for(int dx = 0; dx < size.getX(); dx += 5) {
         for(int dz = 0; dz < size.getZ(); dz += 5) {
            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;
            int top = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockState state = level.getBlockState(new BlockPos(x, top - 1, z));
            ++sampled;
            if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.SANDSTONE)) {
               ++sandy;
            }
         }
      }

      if (sampled != 0 && sandy * 2 >= sampled) {
         for(int dx = 0; dx < size.getX(); ++dx) {
            for(int dz = 0; dz < size.getZ(); ++dz) {
               for(int dy = 0; dy < size.getY(); ++dy) {
                  BlockPos at = origin.offset(dx, dy, dz);
                  BlockState state = level.getBlockState(at);
                  if (dy < 2) {
                     if (!state.isAir()) {
                        level.setBlock(at, Blocks.SANDSTONE.defaultBlockState(), 2);
                     }
                  } else if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
                     level.setBlock(at, Blocks.SAND.defaultBlockState(), 2);
                  }
               }
            }
         }

         DevouringStormsMod.LOGGER.info("[storm platform] desert: earth swapped for sand");
      }
   }

   private static void loadTowerBox(Path marker) {
      try {
         String[] parts = Files.readString(marker).trim().split("\\s+");
         if (parts.length < 6) {
            DevouringStormsMod.LOGGER.warn("[storm platform] this world's marker has no coordinates in it (written by an older build). The tower is out there but the mod cannot see it, so its dust, gloom and music are all off. Stand in the tower and run /dabyws tower mark, or /dabyws tower place to build a fresh one where you stand.");
            return;
         }

         double x = Double.parseDouble(parts[0]);
         double y = Double.parseDouble(parts[1]);
         double z = Double.parseDouble(parts[2]);
         double sx = Double.parseDouble(parts[3]);
         double sy = Double.parseDouble(parts[4]);
         double sz = Double.parseDouble(parts[5]);
         towerBox = new AABB(x, y, z, x + sx, y + sy, z + sz);
      } catch (Exception var14) {
      }

   }

   public static void onServerStarted(MinecraftServer server) {
      ServerLevel overworld = server.overworld();
      if (overworld != null) {
         Path marker = server.getWorldPath(LevelResource.ROOT).resolve("dabywsmod_spawn_platform.placed");
         if (Files.exists(marker, new LinkOption[0])) {
            loadTowerBox(marker);
         } else {
            StructureTemplate template = overworld.getStructureManager().get(STRUCTURE).orElse(null);
            if (template == null) {
               DevouringStormsMod.LOGGER.warn("[storm platform] structure {} not found; not placing", STRUCTURE);
            } else {
               long searchBegan = System.currentTimeMillis();
               BlockPos land = findNearestLand(overworld);
               DevouringStormsMod.LOGGER.info("[storm platform] land search finished in {} ms", System.currentTimeMillis() - searchBegan);
               if (land == null) {
                  DevouringStormsMod.LOGGER.warn("[storm platform] no land found within {} blocks of world spawn; not placing", 2000);
               } else {
                  Vec3i size = template.getSize();
                  BlockPos origin = new BlockPos(land.getX() - size.getX() / 2, land.getY() + -4, land.getZ() - size.getZ() / 2);
                  StructurePlaceSettings settings = new StructurePlaceSettings();
                  template.placeInWorld(overworld, origin, origin, settings, overworld.getRandom(), 2);
                  DevouringStormsMod.LOGGER.info("[storm platform] placed {} at {} (land surface {})", new Object[]{STRUCTURE, origin, land});
                  adaptToSand(overworld, origin, size);
                  recordAndWrite(server, origin, size);
               }
            }
         }
      }
   }

   private static BlockPos searchCentre(ServerLevel level) {
      LevelData.RespawnData respawn = level.getRespawnData();
      return respawn == null ? BlockPos.ZERO : respawn.pos();
   }

   private static BlockPos findNearestLand(ServerLevel level) {
      ChunkGenerator gen = level.getChunkSource().getGenerator();
      RandomState random = level.getChunkSource().randomState();
      int sea = level.getSeaLevel();
      samplesUsed = 0;
      long began = System.currentTimeMillis();
      BlockPos centre = searchCentre(level);

      for(int pass = 0; pass < 2; ++pass) {
         boolean strict = pass == 0;

         for(int r = 0; r <= 2000; r += 32) {
            BlockPos hit = scanRing(level, gen, random, sea, r, strict, centre);
            if (hit != null) {
               return hit;
            }
         }

         if (strict) {
            DevouringStormsMod.LOGGER.info("[storm platform] nothing flat and inland within {} blocks after {} samples; taking any land", 2000, samplesUsed);
         }
      }

      DevouringStormsMod.LOGGER.info("[storm platform] search took {} samples in {} ms", samplesUsed, System.currentTimeMillis() - began);
      return null;
   }

   private static BlockPos scanRing(ServerLevel level, ChunkGenerator gen, RandomState random, int sea, int r, boolean strict, BlockPos centre) {
      int cx = centre.getX();
      int cz = centre.getZ();
      if (r == 0) {
         return candidate(level, gen, random, sea, cx, cz, strict);
      } else {
         for(int x = -r; x <= r; x += 32) {
            BlockPos a = candidate(level, gen, random, sea, cx + x, cz - r, strict);
            if (a != null) {
               return a;
            }

            BlockPos b = candidate(level, gen, random, sea, cx + x, cz + r, strict);
            if (b != null) {
               return b;
            }
         }

         for(int z = -r + 32; z <= r - 32; z += 32) {
            BlockPos a = candidate(level, gen, random, sea, cx - r, cz + z, strict);
            if (a != null) {
               return a;
            }

            BlockPos b = candidate(level, gen, random, sea, cx + r, cz + z, strict);
            if (b != null) {
               return b;
            }
         }

         return null;
      }
   }

   private static int noiseHeight(ChunkGenerator gen, RandomState random, ServerLevel level, int x, int z) {
      ++samplesUsed;
      return gen.getBaseHeight(x, z, Types.WORLD_SURFACE_WG, level, random);
   }

   private static boolean outOfBudget() {
      return samplesUsed > 220000;
   }

   private static BlockPos candidate(ServerLevel level, ChunkGenerator gen, RandomState random, int sea, int x, int z, boolean strict) {
      int h = noiseHeight(gen, random, level, x, z);
      if (h <= sea + 2) {
         return null;
      } else {
         if (strict && !outOfBudget()) {
            int lowest = h;
            int highest = h;

            for(int dx = -12; dx <= 12; dx += 12) {
               for(int dz = -12; dz <= 12; dz += 12) {
                  int ph = noiseHeight(gen, random, level, x + dx, z + dz);
                  if (ph <= sea) {
                     return null;
                  }

                  lowest = Math.min(lowest, ph);
                  highest = Math.max(highest, ph);
                  if (highest - lowest > 3) {
                     return null;
                  }
               }
            }

            for(int i = 0; i < 8; ++i) {
               double a = (double)i * Math.PI / (double)4.0F;
               int px = x + (int)Math.round(Math.cos(a) * (double)72.0F);
               int pz = z + (int)Math.round(Math.sin(a) * (double)72.0F);
               if (noiseHeight(gen, random, level, px, pz) <= sea) {
                  return null;
               }
            }
         }

         return verify(level, x, z);
      }
   }

   private static BlockPos verify(ServerLevel level, int x, int z) {
      for(int dx = -12; dx <= 12; dx += 6) {
         for(int dz = -12; dz <= 12; dz += 6) {
            int px = x + dx;
            int pz = z + dz;
            level.getChunk(px >> 4, pz >> 4);
            int solid = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
            if (solid <= level.getMinY() + 1) {
               return null;
            }

            BlockPos top = new BlockPos(px, solid - 1, pz);
            if (!level.getFluidState(top).isEmpty()) {
               return null;
            }

            BlockState state = level.getBlockState(top);
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
               return null;
            }
         }
      }

      return new BlockPos(x, level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
   }
}
