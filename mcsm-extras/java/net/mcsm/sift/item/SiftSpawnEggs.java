package net.mcsm.sift.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawn eggs with cool design - Fabric version using SpawnEggItem
 */
public final class SiftSpawnEggs {

    public static final List<Item> ALL_EGGS = new ArrayList<>();

    // These will be initialized after entities are registered
    private static Item egg(String name, EntityType<?> type, int primary, int secondary) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("mcsm_sift", name));
        Item it = new SpawnEggItem(type, new Item.Properties().setId(key));
        try {
            Registry.register(BuiltInRegistries.ITEM, key, it);
            ALL_EGGS.add(it);
        } catch (Throwable t) {
            System.err.println("[sift] spawn egg mcsm_sift:" + name + " not registered: " + t);
        }
        return it;
    }

    public static Item VOID_WHALE_SPAWN_EGG;
    public static Item VOID_DWELLER_SPAWN_EGG;
    public static Item TOWN_GUARD_SPAWN_EGG;
    public static Item TOWN_MERCHANT_SPAWN_EGG;
    public static Item TOWN_SAGE_SPAWN_EGG;
    public static Item RIFT_SPAWN_EGG;
    public static Item OCTOPUS_SPAWN_EGG;
    public static Item JOKEST_SPAWN_EGG;

    // V2 new eggs
    public static Item VOID_JELLY_SPAWN_EGG;
    public static Item PRISMATIC_WISP_SPAWN_EGG;
    public static Item CRYSTAL_MANTA_SPAWN_EGG;
    public static Item NEBULA_RAY_SPAWN_EGG;
    public static Item VOID_SNAIL_SPAWN_EGG;

    private SiftSpawnEggs() {}

    public static void register() {
        // Called after entity types are registered
        try {
            // Use reflection to avoid hard dependency if entities not yet loaded
            // We'll register eggs in McsmSiftMod.register() after entities
        } catch (Throwable t) {
            System.err.println("[sift] spawn eggs init failed: " + t);
        }
    }

    public static void registerEggs() {
        try {
            Class<?> modClass = Class.forName("net.mcsm.sift.McsmSiftMod");
            // Void whale
            EntityType<?> whale = (EntityType<?>) modClass.getField("VOID_WHALE").get(null);
            VOID_WHALE_SPAWN_EGG = egg("void_whale_spawn_egg", whale, 0x33FFCC, 0xAA55FF);
            EntityType<?> dweller = (EntityType<?>) modClass.getField("VOID_DWELLER").get(null);
            VOID_DWELLER_SPAWN_EGG = egg("void_dweller_spawn_egg", dweller, 0xFFAA55, 0x55FFAA);
            EntityType<?> octo = (EntityType<?>) modClass.getField("COLOSSAL_OCTOPUS").get(null);
            OCTOPUS_SPAWN_EGG = egg("colossal_octopus_spawn_egg", octo, 0xFF6699, 0x66FFCC);
            EntityType<?> jokest = (EntityType<?>) modClass.getField("JOKEST_CREATURE").get(null);
            JOKEST_SPAWN_EGG = egg("jokest_creature_spawn_egg", jokest, 0x55AAFF, 0xFF55AA);
            EntityType<?> rift = (EntityType<?>) modClass.getField("SIFT_RIFT").get(null);
            RIFT_SPAWN_EGG = egg("rift_spawn_egg", rift, 0xAA33FF, 0xFF3399);

            // V2 new entities if present
            try {
                EntityType<?> jelly = (EntityType<?>) modClass.getField("VOID_JELLY").get(null);
                VOID_JELLY_SPAWN_EGG = egg("void_jelly_spawn_egg", jelly, 0x88FFFF, 0xFF88FF);
            } catch (Throwable ignored) {}
            try {
                EntityType<?> wisp = (EntityType<?>) modClass.getField("PRISMATIC_WISP").get(null);
                PRISMATIC_WISP_SPAWN_EGG = egg("prismatic_wisp_spawn_egg", wisp, 0xFFFFFF, 0xFFAAFF);
            } catch (Throwable ignored) {}
            try {
                EntityType<?> manta = (EntityType<?>) modClass.getField("CRYSTAL_MANTA").get(null);
                CRYSTAL_MANTA_SPAWN_EGG = egg("crystal_manta_spawn_egg", manta, 0x66CCFF, 0xCC66FF);
            } catch (Throwable ignored) {}
            try {
                EntityType<?> ray = (EntityType<?>) modClass.getField("NEBULA_RAY").get(null);
                NEBULA_RAY_SPAWN_EGG = egg("nebula_ray_spawn_egg", ray, 0xFF66AA, 0x66FFAA);
            } catch (Throwable ignored) {}
            try {
                EntityType<?> snail = (EntityType<?>) modClass.getField("VOID_SNAIL").get(null);
                VOID_SNAIL_SPAWN_EGG = egg("void_snail_spawn_egg", snail, 0xAAFF66, 0x66AAFF);
            } catch (Throwable ignored) {}

            // Town NPCs from McsmNpcs if present
            try {
                Class<?> npcs = Class.forName("net.mcsm.extras.McsmNpcs");
                EntityType<?> guard = (EntityType<?>) npcs.getField("TOWN_GUARD").get(null);
                TOWN_GUARD_SPAWN_EGG = egg("town_guard_spawn_egg", guard, 0x55AAFF, 0xFFCC55);
                EntityType<?> merchant = (EntityType<?>) npcs.getField("TOWN_MERCHANT").get(null);
                TOWN_MERCHANT_SPAWN_EGG = egg("town_merchant_spawn_egg", merchant, 0xFF5555, 0xFFFFFF);
                EntityType<?> sage = (EntityType<?>) npcs.getField("TOWN_SAGE").get(null);
                TOWN_SAGE_SPAWN_EGG = egg("town_sage_spawn_egg", sage, 0xFFAA33, 0xAA55FF);
            } catch (Throwable ignored) {}
        } catch (Throwable t) {
            System.err.println("[sift] spawn egg registration failed: " + t);
        }
    }
}
