package net.dabicco.witherstormmod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.dabicco.witherstormmod.beacon.WitherTheBeacon;
import net.dabicco.witherstormmod.block.entity.ModBlockEntities;
import net.dabicco.witherstormmod.bowels.BowelsActionKeys;
import net.dabicco.witherstormmod.bowels.BowelsBoss;
import net.dabicco.witherstormmod.bowels.ModBowelsEntities;
import net.dabicco.witherstormmod.command.DabyWSCommand;
import net.dabicco.witherstormmod.config.PendingWorldConfig;
import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.WitheredStarEntity;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.dabicco.witherstormmod.entity.withered.WitheredMobs;
import net.dabicco.witherstormmod.menu.ModMenus;
import net.dabicco.witherstormmod.nether.NetherScaleManager;
import net.dabicco.witherstormmod.network.CaveRumblePayload;
import net.dabicco.witherstormmod.network.CommandBlockPowerPayload;
import net.dabicco.witherstormmod.network.FormidibombFlashPayload;
import net.dabicco.witherstormmod.network.SpawnStructurePayload;
import net.dabicco.witherstormmod.network.StormRemovedPacket;
import net.dabicco.witherstormmod.network.WitherSicknessPayload;
import net.dabicco.witherstormmod.network.WitherStormPositionPacket;
import net.dabicco.witherstormmod.network.WitheredCastPayload;
import net.dabicco.witherstormmod.structures.McsmCommand;
import net.dabicco.witherstormmod.structures.McsmGuidebook;
import net.dabicco.witherstormmod.structures.McsmWorldgen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AfterDeath;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents.StartTracking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder.BuildCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DabyWitherStormMod implements ModInitializer {
   private static final Map<UUID, Boolean> insideTower = new HashMap<>();
   public static final String MOD_ID = "dabywitherstormmod";
   public static final Logger LOGGER = LoggerFactory.getLogger("dabywitherstormmod");
   private static final Logger BRAND = LoggerFactory.getLogger("DabiccosWitherStorm");

   public void onInitialize() {
      PayloadTypeRegistry.clientboundPlay().register(ClusterBlocksPayload.TYPE, ClusterBlocksPayload.CODEC);
      BowelsActionKeys.listen();
      WitherTheBeacon.listen();
      PayloadTypeRegistry.clientboundPlay().register(CaveRumblePayload.TYPE, CaveRumblePayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(CommandBlockPowerPayload.TYPE, CommandBlockPowerPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(WitherStormPositionPacket.TYPE, WitherStormPositionPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(SpawnStructurePayload.TYPE, SpawnStructurePayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(StormRemovedPacket.TYPE, StormRemovedPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(FormidibombFlashPayload.TYPE, FormidibombFlashPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(WitherSicknessPayload.TYPE, WitherSicknessPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(WitheredCastPayload.TYPE, WitheredCastPayload.CODEC);
      ModComponents.register();
      ModBlocks.initialize();
      ModBlockEntities.initialize();
      ModMenus.initialize();
      ModItems.initialize();
      ModItemGroups.initialize();
      ModSounds.initialize();
      ModEffects.initialize();
      ModPotions.initialize();
      registerBrewing();
      ModParticles.register();
      ModEntityTypes.registerModEntityTypes();
      ModEntityTypes.registerAttributes();
      ModBowelsEntities.register();
      String version = FabricLoader.getInstance()
         .getModContainer("dabywitherstormmod")
         .map(c -> c.getMetadata().getVersion().getFriendlyString())
         .orElse("unknown version");
      BRAND.info("Dabicco's Wither Storm Mod - {}", version);
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> DabyWSCommand.register(dispatcher));
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> McsmCommand.register(dispatcher));
      ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick)level -> McsmWorldgen.tick(level));
      McsmGuidebook.register();
      SigeonNetwork.register();
      StrippableBlockRegistry.register(ModBlocks.WITHERED_LOG, ModBlocks.STRIPPED_WITHERED_LOG);
      DabyWSCommand.registerTick();
      NetherScaleManager.registerTick();
      ServerTickEvents.END_LEVEL_TICK.register(WitheredMobs::serverTick);
      ServerTickEvents.END_LEVEL_TICK.register(BowelsGravity::tick);
      ServerTickEvents.END_LEVEL_TICK.register(WitheredStarEntity::tick);
      ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick)level -> {
         if (level.getGameTime() % 20L == 0L) {
            CaveRumble.tick(level);
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, server) -> CaveRumble.forget(handler.getPlayer().getUUID()));
      ServerLifecycleEvents.SERVER_STOPPED.register((ServerStopped)server -> WitheredMobs.clear());
      ServerLifecycleEvents.SERVER_STARTED.register(StormSpawnPlatform::onServerStarted);
      ServerLifecycleEvents.SERVER_STARTED.register(PendingWorldConfig::applyTo);
      ServerTickEvents.END_SERVER_TICK.register((EndTick)srv -> {
         StormSpawnPlatform.spawnTowerDust(srv);
         if (srv.getTickCount() % 20 == 0) {
            ServerLevel bowels = srv.getLevel(BowelsGravity.BOWELS);
            if (bowels != null) {
               BowelsBoss.tick(bowels);
            }
         }

         if (srv.getTickCount() % 10 == 0) {
            for (ServerPlayer p : srv.getPlayerList().getPlayers()) {
               boolean in = StormSpawnPlatform.insideTower(p.position());
               Boolean was = insideTower.get(p.getUUID());
               if (was == null || was != in) {
                  insideTower.put(p.getUUID(), in);
                  Vec3 heart = StormSpawnPlatform.towerHeart();
                  ServerPlayNetworking.send(p, new SpawnStructurePayload(in, heart.x, heart.y, heart.z));
               }
            }
         }
      });
      ServerLivingEntityEvents.AFTER_DEATH.register((AfterDeath)(entity, damageSource) -> WitheredMobs.onDeath(entity, damageSource));
      EntityTrackingEvents.START_TRACKING.register((StartTracking)(entity, player) -> {
         if (entity instanceof WitherStormClusterEntity cluster) {
            cluster.sendBlocksTo(player);
         }
      });
   }

   private static void registerBrewing() {
      FabricPotionBrewingBuilder.BUILD.register((BuildCallback)builder -> {
         Ingredient fragment = Ingredient.of(ModItems.WITHER_FRAGMENT);
         Ingredient redstone = Ingredient.of(Items.REDSTONE);
         ((net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder)(Object)builder).registerPotionRecipe(Potions.INVISIBILITY, fragment, ModPotions.HYPER_INVISIBILITY);
         ((net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder)(Object)builder).registerPotionRecipe(Potions.LONG_INVISIBILITY, fragment, ModPotions.HYPER_INVISIBILITY);
         ((net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder)(Object)builder).registerPotionRecipe(ModPotions.HYPER_INVISIBILITY, redstone, ModPotions.LONG_HYPER_INVISIBILITY);
         ((net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder)(Object)builder).registerPotionRecipe(ModPotions.LONG_HYPER_INVISIBILITY, redstone, ModPotions.EXTENDED_HYPER_INVISIBILITY);
      });
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
   }
}
