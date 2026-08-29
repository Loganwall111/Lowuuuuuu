package net.dabicco.witherstormmod.world;

import net.minecraft.resources.Identifier;

/**
 * FloatingIslandStructure — Devouring Storms sky structure.
 * Massive floating islands suspended in the sky.
 */
public class FloatingIslandStructure {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(
        "dabywitherstormmod", "floating_island"
    );

    private final int islandSize;
    private final int heightAboveGround;

    public FloatingIslandStructure(int size, int height) {
        this.islandSize = size;
        this.heightAboveGround = height;
    }

    public int getIslandSize() {
        return islandSize;
    }

    public int getHeightAboveGround() {
        return heightAboveGround;
    }
}
