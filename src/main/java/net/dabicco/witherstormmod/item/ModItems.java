package net.dabicco.witherstormmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Rarity;

/**
 * ModItems — Devouring Storms item definitions.
 * Items registered via DevouringItemRegistry.
 */
public class ModItems {

    public static final Item WITHER_BLADE = new SwordItem(
        Tiers.NETHERITE,
        new Item.Properties()
            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 8, -2.0f))
            .rarity(Rarity.EPIC)
            .fireResistant()
    );

    public static final Item STORM_SLICER = new SwordItem(
        Tiers.DIAMOND,
        new Item.Properties()
            .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 6, -1.8f))
            .rarity(Rarity.RARE)
    );

    public static final Item CORRUPTION_STAFF = new Item(
        new Item.Properties().rarity(Rarity.EPIC).stacksTo(1).fireResistant()
    );

    public static final Item REALITY_SHARD = new Item(
        new Item.Properties().rarity(Rarity.RARE).stacksTo(16)
    );

    public static final Item CORRUPTED_HEART = new Item(
        new Item.Properties().rarity(Rarity.EPIC).stacksTo(1).fireResistant()
    );

    public static final Item STORM_ESSENCE = new Item(
        new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(64)
    );

    public static final Item VOID_CRYSTAL = new Item(
        new Item.Properties().rarity(Rarity.RARE).stacksTo(8)
    );

    public static final Item WITHER_PICKAXE = new Item(
        new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1).durability(2500)
    );

    public static final Item STORM_COMPASS = new Item(
        new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)
    );

    public static final Item CORRUPTION_POTION = new Item(
        new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(8)
    );

    public static final Item STORM_BREW = new Item(
        new Item.Properties().rarity(Rarity.RARE).stacksTo(4)
    );

    public static final Item CORRUPTION_SPAWN_EGG = new Item(
        new Item.Properties().stacksTo(64)
    );

    public static final Item RIFT_SPAWN_EGG = new Item(
        new Item.Properties().stacksTo(64)
    );
}
