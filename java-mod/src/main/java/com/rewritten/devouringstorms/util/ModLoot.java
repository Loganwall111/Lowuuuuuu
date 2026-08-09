package com.rewritten.devouringstorms.util;

import com.rewritten.devouringstorms.registry.ModItems;
import net.fabricmc.fabric.api.loot_table.v3.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Injects the Corrupted Blueprints into deep-world loot — "deep beneath the broken code".
 * Ancient Cities and Stronghold corridor/altar chests.
 */
public final class ModLoot {

    private static final ResourceLocation ANCIENT_CITY = ResourceLocation.withDefaultNamespace("chests/ancient_city");
    private static final ResourceLocation STRONGHOLD_CORRIDOR = ResourceLocation.withDefaultNamespace("chests/stronghold_corridor");
    private static final ResourceLocation STRONGHOLD_LIBRARY = ResourceLocation.withDefaultNamespace("chests/stronghold_library");

    private ModLoot() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) return;
            ResourceLocation id = key.identifier();
            float chance = id.equals(ANCIENT_CITY) ? 0.35f : 0.18f;
            if (id.equals(ANCIENT_CITY) || id.equals(STRONGHOLD_CORRIDOR) || id.equals(STRONGHOLD_LIBRARY)) {
                tableBuilder.pool(
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .conditionally(LootItemRandomChanceCondition.randomChance(chance).build())
                        .add(LootItem.lootTableItem(ModItems.CORRUPTED_BLUEPRINTS))
                        .build()
                );
            }
        });
    }
}
