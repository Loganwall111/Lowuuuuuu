package net.dabicco.witherstormmod.world;

import net.minecraft.resources.ResourceLocation;

/**
 * AbandonedCityStructure — Devouring Storms world structure.
 * Abandoned cities scattered across the world, ravaged by the storm.
 * Structure generation registered via data-driven worldgen.
 */
public class AbandonedCityStructure {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        "dabywitherstormmod", "abandoned_city"
    );

    private final int size;

    public AbandonedCityStructure(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
