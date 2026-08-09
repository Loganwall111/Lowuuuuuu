package com.rewritten.devouringstorms;

import com.rewritten.devouringstorms.registry.ModBlockEntities;
import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModDamageTypes;
import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.registry.ModItemGroup;
import com.rewritten.devouringstorms.registry.ModItems;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModPackets;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.registry.ModStatusEffects;
import com.rewritten.devouringstorms.storm.StormDirector;
import com.rewritten.devouringstorms.util.EcosystemTicker;
import com.rewritten.devouringstorms.util.InfectionTicker;
import com.rewritten.devouringstorms.util.ModLoot;
import com.rewritten.devouringstorms.world.ModWorldEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DEVOURING STORMS — WITHERING REWRITE: AWAKENING
 *
 * The system was never stable. The Wither Storm blueprints are corrupted.
 * MASSG is waking up.
 */
public final class DevouringStorms implements ModInitializer {
    public static final String MOD_ID = "devouring_storms";
    public static final String MOD_NAME = "Devouring Storms";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Order matters: blocks before items (BlockItems), entities before spawn eggs.
        ModStatusEffects.register();
        ModParticles.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModEntities.register();
        ModItemGroup.register();
        ModSounds.register();
        ModDamageTypes.register();
        ModPackets.register();
        ModLoot.register();
        ModWorldEvents.register();
        StormDirector.register();
        InfectionTicker.register();
        EcosystemTicker.register();

        LOGGER.info("[DevouringStorms] The system was never stable.");
        LOGGER.info("[DevouringStorms] The mainframe has been breached.");
        LOGGER.info("[DevouringStorms] MASSG is waking up...");
    }
}
