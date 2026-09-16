package dev.siftcore;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class SiftDimensions {
    public static final RegistryKey<World> THE_SIFT = RegistryKey.of(
            RegistryKeys.WORLD,
            new Identifier(SiftCore.NAMESPACE, "the_sift")
    );

    /** The client-side DimensionEffects option referenced by the dimension JSON. */
    public static final Identifier EFFECTS = SiftCore.id("sift");

    private SiftDimensions() {
    }

    public static boolean isSift(World world) {
        return world.getRegistryKey().equals(THE_SIFT);
    }
}
