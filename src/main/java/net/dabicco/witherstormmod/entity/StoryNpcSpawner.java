package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class StoryNpcSpawner {
    private static final String[] NAMES = new String[]{
        "Jesse", "Petra", "Axel", "Olivia", "Lukas", "Radar", "Ivor", "Gabriel",
        "Ellegaard", "Magnus", "Soren", "Harper", "Jack", "Nurm", "Lluna", "Stacy",
        "Stampy", "DanTDM", "Lizzie", "Winslow", "Boren", "Porkchop", "Nell", "Isa", "Milo"
    };

    private StoryNpcSpawner() {
    }

    public static int spawn(ServerLevel server, BlockPos pos, int amount) {
        if (amount <= 0) {
            return 0;
        }
        
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse("witherstormmod:storymode"));
        if (type == null) {
            return 0;
        }

        RandomSource random = server.getRandom();
        List<String> names = new ArrayList<>(List.of(NAMES));
        Collections.shuffle(names, new java.util.Random(random.nextLong()));
        int spawned = 0;

        for (int i = 0; i < amount && spawned < names.size(); ++i) {
            double x = pos.getX() + random.nextInt(16) - 8;
            double z = pos.getZ() + random.nextInt(16) - 8;
            int y = server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)x, (int)z);
            BlockPos spawnPos = new BlockPos((int)x, y, (int)z);

            if (SpawnPlacements.checkSpawnRules(type, server, MobSpawnType.STRUCTURE, spawnPos, server.getRandom())) {
                // Logic can go here if needed
            }

            SpawnGroupData groupData = null;
            // The type.spawn method requires explicit double casting to clear up the method overload match error
            groupData = type.spawn(server, null, groupData, spawnPos, MobSpawnType.STRUCTURE, true, false);
            
            if (groupData != null) {
                spawned++;
            }
        }
        return spawned;
    }
}

