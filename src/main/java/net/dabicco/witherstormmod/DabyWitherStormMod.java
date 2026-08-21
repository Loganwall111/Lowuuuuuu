package net.dabicco.witherstormmod;

import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.network.StormDeathPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clean entrypoint for the rewritten Wither Storm mod.
 *
 * Only wires up registries that exist and are self-contained. Everything that was
 * broken (old models, mixins, renderers) is intentionally not referenced here — the
 * rewrite keeps the working registries (config, sounds, blocks, items, effects,
 * potions, particles) and rebuilds the entity + render layer fresh.
 */
public class DabyWitherStormMod implements ModInitializer {
   public static final String MOD_ID = "dabywitherstormmod";
   public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

   @Override
   public void onInitialize() {
      LOGGER.info("Initializing Daby's Wither Storm (clean rewrite)");

      // Items, blocks, sounds, particles, effects, potions.
      // (Enchantments are data-driven; ModEnchantments only exposes a ResourceKey.)
      ModItems.initialize();
      ModBlocks.initialize();
      ModSounds.initialize();
      ModParticles.register();
      ModEffects.initialize();
      ModPotions.initialize();
      ModItemGroups.initialize();

      // Entities + attributes.
      ModEntityTypes.registerModEntityTypes();
      ModEntityTypes.registerAttributes();

      // Networking payloads.
      PayloadTypeRegistry.clientboundPlay().register(StormDeathPayload.TYPE, StormDeathPayload.CODEC);

      LOGGER.info("Daby's Wither Storm initialized");
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(MOD_ID, path);
   }
}
