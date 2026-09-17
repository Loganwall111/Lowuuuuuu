package net.mcsm.sift.item;

import net.mcsm.sift.McsmSiftMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Spawn eggs with cool design - every single response involved in game
 * Custom colors matching concept images - vibrant, rainbow, cosmic
 */
public final class SiftSpawnEggs {

    public static final DeferredRegister<Item> SPAWN_EGGS = DeferredRegister.create(ForgeRegistries.ITEMS, "mcsm_sift");

    // Void Whale - colossal ghost whale - teal and purple with glowing
    public static final RegistryObject<Item> VOID_WHALE_SPAWN_EGG = SPAWN_EGGS.register("void_whale_spawn_egg",
        () -> new ForgeSpawnEggItem(
            McsmSiftMod.VOID_WHALE,
            0x33FFCC, // primary - teal iridescent
            0xAA55FF, // secondary - cosmic purple glow
            new Item.Properties()
        ));

    // Void Dweller - fish-like silhouette - each type different but egg uses angler colors
    public static final RegistryObject<Item> VOID_DWELLER_SPAWN_EGG = SPAWN_EGGS.register("void_dweller_spawn_egg",
        () -> new ForgeSpawnEggItem(
            McsmSiftMod.VOID_DWELLER,
            0xFFAA55, // primary - warm orange like concept art characters
            0x55FFAA, // secondary - lumen green
            new Item.Properties()
        ));

    // Town Guard - from McsmNpcs - blue and gold like hero from screenshots
    public static final RegistryObject<Item> TOWN_GUARD_SPAWN_EGG = SPAWN_EGGS.register("town_guard_spawn_egg",
        () -> new ForgeSpawnEggItem(
            net.mcsm.extras.McsmNpcs.TOWN_GUARD,
            0x55AAFF, // primary - hero blue
            0xFFCC55, // secondary - gold hammer
            new Item.Properties()
        ));

    // Town Merchant - red and white like second hero
    public static final RegistryObject<Item> TOWN_MERCHANT_SPAWN_EGG = SPAWN_EGGS.register("town_merchant_spawn_egg",
        () -> new ForgeSpawnEggItem(
            net.mcsm.extras.McsmNpcs.TOWN_MERCHANT,
            0xFF5555, // primary - red
            0xFFFFFF, // secondary - white
            new Item.Properties()
        ));

    // Town Sage - purple and orange like third hero
    public static final RegistryObject<Item> TOWN_SAGE_SPAWN_EGG = SPAWN_EGGS.register("town_sage_spawn_egg",
        () -> new ForgeSpawnEggItem(
            net.mcsm.extras.McsmNpcs.TOWN_SAGE,
            0xFFAA33, // primary - orange/brown
            0xAA55FF, // secondary - purple staff
            new Item.Properties()
        ));

    // Sift Rift - reality rift entity - neon purple and hot magenta for rim
    public static final RegistryObject<Item> RIFT_SPAWN_EGG = SPAWN_EGGS.register("rift_spawn_egg",
        () -> new ForgeSpawnEggItem(
            McsmSiftMod.SIFT_RIFT,
            0xAA33FF, // primary - neon purple
            0xFF3399, // secondary - hot magenta
            new Item.Properties()
        ));

    // Colossal Octopus - rainbow colored water with sparkles
    public static final RegistryObject<Item> OCTOPUS_SPAWN_EGG = SPAWN_EGGS.register("colossal_octopus_spawn_egg",
        () -> new ForgeSpawnEggItem(
            McsmSiftMod.COLOSSAL_OCTOPUS,
            0xFF6699, // primary - rainbow pink
            0x66FFCC, // secondary - rainbow teal
            new Item.Properties()
        ));

    // Jokest Creature - small funny creatures - blue bun, pink puff, fluor sprite, red rockling
    public static final RegistryObject<Item> JOKEST_SPAWN_EGG = SPAWN_EGGS.register("jokest_creature_spawn_egg",
        () -> new ForgeSpawnEggItem(
            McsmSiftMod.JOKEST_CREATURE,
            0x55AAFF, // primary - blue bun
            0xFF55AA, // secondary - pink puff
            new Item.Properties()
        ));

    private SiftSpawnEggs() {}
}
