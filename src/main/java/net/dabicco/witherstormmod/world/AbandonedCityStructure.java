package net.dabicco.witherstormmod.world;

import net.minecraft.resources.Identifier;

/**
 * AbandonedCityStructure — Devouring Storms world structure.
 * Abandoned cities scattered across the world, ravaged by the storm.
 */
public class AbandonedCityStructure {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(
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
