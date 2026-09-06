package net.mcsm.extras;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Devouring Storms: coloured dynamic lights, phase 1 - dropped items glow
 * in their own colour. Each luminous item maps to a packed ARGB tint used
 * for the dust particles the ItemEntity mixin breathes around the drop
 * (flint &amp; steel sparks steel-blue, torches burn orange, soul fire
 * cyan, glowstone gold, amethyst violet...).
 *
 * Item constants and the packed-int DustParticleOptions(int, float)
 * constructor are the verified 26.2 forms (copied from the base mod's own
 * compiled particle code).
 */
public final class McsmDynamicLights {

    private static final Map<Item, Integer> GLOW = new HashMap<>();

    private static void put(Item item, int rgb) {
        GLOW.put(item, 0xFF000000 | rgb);
    }

    static {
        // fire & steel
        put(Items.FLINT_AND_STEEL, 0x9FB8FF); // struck-steel blue spark
        put(Items.FIRE_CHARGE, 0xFF7733);
        put(Items.LAVA_BUCKET, 0xFF5522);
        put(Items.TORCH, 0xFF9933);
        put(Items.REDSTONE_TORCH, 0xFF3333);
        put(Items.MAGMA_CREAM, 0xFF6622);
        put(Items.BLAZE_ROD, 0xFFBB33);
        put(Items.BLAZE_POWDER, 0xFFCC44);
        put(Items.GLOW_BERRIES, 0xFFAA44);
        // soul fire
        put(Items.SOUL_TORCH, 0x33FFE6);
        put(Items.SOUL_LANTERN, 0x33FFE6);
        // golden light
        put(Items.GLOWSTONE, 0xFFE666);
        put(Items.GLOWSTONE_DUST, 0xFFE666);
        put(Items.SHROOMLIGHT, 0xFFAA55);
        put(Items.LANTERN, 0xFFC466);
        put(Items.COPPER_TORCH, 0xFFB070);
        // cold & arcane
        put(Items.SEA_LANTERN, 0x99FFE6);
        put(Items.END_ROD, 0xFFFFFF);
        put(Items.NETHER_STAR, 0xFFFFEE);
        put(Items.BEACON, 0x99FFFF);
        put(Items.AMETHYST_SHARD, 0xCC66FF);
        put(Items.EMERALD, 0x33FF77);
        put(Items.DIAMOND, 0x66FFFF);
        put(Items.GLOW_INK_SAC, 0x33FFCC);
        put(Items.REDSTONE, 0xFF2222);
        put(Items.PRISMARINE_CRYSTALS, 0x66FFCC);
    }

    private McsmDynamicLights() {
    }

    /** Packed ARGB glow colour for this stack, or 0 when it does not glow. */
    public static int glowColor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        Integer c = GLOW.get(stack.getItem());
        return c == null ? 0 : c.intValue();
    }
}
