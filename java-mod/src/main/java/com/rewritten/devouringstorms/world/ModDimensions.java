package com.rewritten.devouringstorms.world;

import com.rewritten.devouringstorms.DevouringStorms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** The Decayed Reality — a quarantined world trapped in an endless cycle. */
public final class ModDimensions {

    public static final ResourceKey<Level> DECAYED_LEVEL_KEY = ResourceKey.create(
        Registries.DIMENSION, DevouringStorms.id("decayed_reality"));
    /** The Fray — where the quarantine's stitched edges come loose. Multiverse ring, node 2. */
    public static final ResourceKey<Level> FRAY_LEVEL_KEY = ResourceKey.create(
        Registries.DIMENSION, DevouringStorms.id("the_fray"));
    /** Echo Fields — the quietest place left anywhere. Multiverse ring, node 3. */
    public static final ResourceKey<Level> ECHO_LEVEL_KEY = ResourceKey.create(
        Registries.DIMENSION, DevouringStorms.id("echo_fields"));
    /** The Belly — physically inside the storm. Fly into the open bowels to get here. */
    public static final ResourceKey<Level> BELLY_LEVEL_KEY = ResourceKey.create(
        Registries.DIMENSION, DevouringStorms.id("storm_belly"));
    /** The Cosmic Abyss — the empty blackness BHS shelved across. Broken records summon there. */
    public static final ResourceKey<Level> ABYSS_LEVEL_KEY = ResourceKey.create(
        Registries.DIMENSION, DevouringStorms.id("cosmic_abyss"));
    /** PLANET AURTH — the Stone Age had a morning. */
    public static final ResourceKey<Level> AURTH_LEVEL_KEY = ResourceKey.create(
        Registries.DIMENSION, DevouringStorms.id("planet_aurth"));
    /** PLANET VOLMAR — iron and fire. */
    public static final ResourceKey<Level> VOLMAR_LEVEL_KEY = ResourceKey.create(
        Registries.DIMENSION, DevouringStorms.id("planet_volmar"));
    /** NEXUS — the Multiverse Age. The broadcast plays to itself. */
    public static final ResourceKey<Level> NEXUS_LEVEL_KEY = ResourceKey.create(
        Registries.DIMENSION, DevouringStorms.id("planet_nexus"));

    private ModDimensions() {
    }

    public static boolean isDecayed(Level level) {
        return level.dimension() == DECAYED_LEVEL_KEY;
    }
}
