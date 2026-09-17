package dev.siftcore;

import dev.siftcore.block.SiftBlocks;
import dev.siftcore.command.SiftCommands;
import dev.siftcore.encounter.SiftEncounterController;
import dev.siftcore.mob.SiftDrifterEntity;
import dev.siftcore.mob.SiftDrifterSpawner;
import dev.siftcore.physics.SiftCurrentPhysics;
import dev.siftcore.rift.SiftRiftEntity;
import dev.siftcore.terrain.SiftTerrainSpawner;
import dev.siftcore.rift.SiftRiftSpawner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entry point for the standalone Sift sandbox.
 *
 * <p>The resource namespace is deliberately {@code mcsm}; the mod id remains
 * {@code sift_core} so this project can be merged into the future Dimensions
 * Forged workspace without rewriting the dimension data.</p>
 */
public final class SiftCore implements ModInitializer {
    public static final String MOD_ID = "sift_core";
    public static final String NAMESPACE = "mcsm";
    public static final String VERSION = "0.4.1-SIFT";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final EntityType<SiftRiftEntity> SIFT_RIFT = Registry.register(
            Registries.ENTITY_TYPE,
            id("sift_rift"),
            EntityType.Builder.create(SiftRiftEntity::new, SpawnGroup.MISC)
                    .setDimensions(4.0f, 4.0f)
                    .maxTrackingRange(128)
                    .trackingTickInterval(10)
                    .disableSaving()
                    .disableSummon()
                    .build("sift_rift")
    );

    public static final EntityType<SiftDrifterEntity> SIFT_DRIFTER = Registry.register(
            Registries.ENTITY_TYPE,
            id("sift_drifter"),
            EntityType.Builder.create(SiftDrifterEntity::new, SpawnGroup.AMBIENT)
                    .setDimensions(1.2f, 1.2f)
                    .maxTrackingRange(112)
                    .trackingTickInterval(3)
                    .disableSaving()
                    .build("sift_drifter")
    );

    public static Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }

    @Override
    public void onInitialize() {
        SiftBlocks.register();
        FabricDefaultAttributeRegistry.register(SIFT_DRIFTER, SiftDrifterEntity.createAttributes());
        SiftCommands.register();
        ServerTickEvents.END_WORLD_TICK.register(SiftRiftSpawner::tick);
        ServerTickEvents.END_WORLD_TICK.register(SiftDrifterSpawner::tick);
        ServerTickEvents.END_WORLD_TICK.register(SiftTerrainSpawner::tick);
        ServerTickEvents.END_WORLD_TICK.register(SiftEncounterController::tick);
        ServerTickEvents.END_WORLD_TICK.register(SiftCurrentPhysics::tick);
        LOGGER.info("Sift-Core {} initialized; The Sift handshake is armed", VERSION);
    }
}
