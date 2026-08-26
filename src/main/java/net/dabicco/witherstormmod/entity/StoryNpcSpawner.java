package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * Spawns named Story Mode villagers around a town centre. Every API call in
 * this file uses a pattern proven to compile in this codebase.
 */
public final class StoryNpcSpawner {
   private static final String[] NAMES = new String[]{
      "Jesse", "Petra", "Axel", "Olivia", "Lukas", "Radar", "Ivor", "Gabriel",
      "Ellegaard", "Magnus", "Soren", "Harper", "Jack", "Nurm", "Stacy", "Stampy",
      "Dan", "Sparklez", "Binta", "Wink", "Fangirl", "Nell", "Em", "Otto"
   };

   private StoryNpcSpawner() {
   }

   public static int populate(ServerLevel server, BlockPos centre, int count) {
      if (count <= 0) {
         return 0;
      }
      EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", "villager"));
      if (type == null) {
         return 0;
      }

      RandomSource random = server.getRandom();
      List<String> names = new ArrayList<String>(List.of(NAMES));
      Collections.shuffle(names, new java.util.Random(random.nextLong()));
      int spawned = 0;

      for (int i = 0; i < names.size() && spawned < count; i++) {
         double ang = random.nextDouble() * Math.PI * 2.0;
         double ring = 6.0 + random.nextDouble() * 14.0;
         int x = centre.getX() + (int)Math.round(Math.cos(ang) * ring);
         int z = centre.getZ() + (int)Math.round(Math.sin(ang) * ring);
         int y = server.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
         BlockPos at = new BlockPos(x, y, z);
         Mob mob = (Mob)type.create(server, EntitySpawnReason.STRUCTURE);
         if (mob == null) {
            continue;
         }
         mob.finalizeSpawn(server, server.getCurrentDifficultyAt(at), EntitySpawnReason.STRUCTURE, (SpawnGroupData)null);
         mob.setCustomName(Component.literal(names.get(i)));
         mob.setCustomNameVisible(true);
         mob.setPersistenceRequired();
         mob.snapTo((double)at.getX() + 0.5D, (double)at.getY(), (double)at.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
         if (server.addFreshEntity(mob)) {
            spawned++;
         }
      }

      return spawned;
   }
}
