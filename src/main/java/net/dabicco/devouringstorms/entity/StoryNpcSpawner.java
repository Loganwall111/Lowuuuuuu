package net.dabicco.devouringstorms.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.dabicco.devouringstorms.config.WitherStormWorldConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.levelgen.Heightmap.Types;

/**
 * Spawns named Story Mode town life around a built town centre.
 */
public final class StoryNpcSpawner {
   private static final String[] VILLAGER_NAMES = new String[]{
      "Jesse", "Petra", "Axel", "Olivia", "Lukas", "Radar", "Ivor", "Gabriel",
      "Ellegaard", "Magnus", "Soren", "Harper", "Jack", "Nurm", "Stacy", "Stampy",
      "Dan", "Sparklez", "Binta", "Wink", "Fangirl", "Nell", "Em", "Otto"
   };
   private static final String[] GOLEM_NAMES = new String[]{
      "Town Guard", "Gatekeeper", "Sentinel", "Bulwark", "Champion", "Praetorian"
   };
   private static final String[] CAT_NAMES = new String[]{
      "Winslow", "Pebble", "Mittens", "Shadow", "Luna", "Clover", "Mochi", "Whiskers"
   };
   private static final String[] PIG_NAMES = new String[]{
      "Reuben", "Porkchop", "Snuffles", "Truffle"
   };

   private StoryNpcSpawner() {
   }

   public static int populate(ServerLevel server, BlockPos centre, WitherStormWorldConfig cfg) {
      if (cfg == null) {
         return 0;
      }

      int spawned = 0;
      spawned += spawnNamedMobs(server, centre, "villager", VILLAGER_NAMES, cfg.townNpcPopulation, 6.0, 18.0, true);
      spawned += spawnNamedMobs(server, centre, "iron_golem", GOLEM_NAMES, cfg.townGuardPopulation, 10.0, 22.0, true);
      spawned += spawnNamedMobs(server, centre, "cat", CAT_NAMES, cfg.townCatPopulation, 5.0, 16.0, false);
      spawned += spawnNamedMobs(server, centre, "pig", PIG_NAMES, cfg.townPigPopulation, 4.0, 14.0, true);
      return spawned;
   }

   public static int populate(ServerLevel server, BlockPos centre, int count) {
      WitherStormWorldConfig cfg = new WitherStormWorldConfig();
      cfg.townNpcPopulation = count;
      cfg.townGuardPopulation = 0;
      cfg.townCatPopulation = 0;
      cfg.townPigPopulation = 0;
      return populate(server, centre, cfg);
   }

   private static int spawnNamedMobs(ServerLevel server, BlockPos centre, String entityPath, String[] names, int count, double minRing, double maxRing, boolean forceVisibleName) {
      if (count <= 0 || names.length == 0) {
         return 0;
      }

      EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", entityPath));
      if (type == null) {
         return 0;
      }

      RandomSource random = server.getRandom();
      List<String> shuffled = new ArrayList<String>(List.of(names));
      Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));
      int spawned = 0;
      int limit = Math.min(count, shuffled.size());

      for(int i = 0; i < limit; ++i) {
         double ang = random.nextDouble() * Math.PI * 2.0;
         double ring = minRing + random.nextDouble() * Math.max(0.0, maxRing - minRing);
         int x = centre.getX() + (int)Math.round(Math.cos(ang) * ring);
         int z = centre.getZ() + (int)Math.round(Math.sin(ang) * ring);
         int y = server.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
         BlockPos at = new BlockPos(x, y, z);
         Mob mob = (Mob)type.create(server, EntitySpawnReason.STRUCTURE);
         if (mob == null) {
            continue;
         }

         mob.finalizeSpawn(server, server.getCurrentDifficultyAt(at), EntitySpawnReason.STRUCTURE, (SpawnGroupData)null);
         mob.setCustomName(Component.literal(shuffled.get(i)));
         mob.setCustomNameVisible(forceVisibleName);
         mob.setPersistenceRequired();
         mob.snapTo((double)at.getX() + 0.5, (double)at.getY(), (double)at.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
         if (server.addFreshEntity(mob)) {
            ++spawned;
         }
      }

      return spawned;
   }
}
