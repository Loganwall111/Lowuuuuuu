package net.dabicco.devouringstorms;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.dabicco.devouringstorms.beacon.WitherTheBeacon;
import net.dabicco.devouringstorms.block.entity.ModBlockEntities;
import net.dabicco.devouringstorms.bowels.BowelsActionKeys;
import net.dabicco.devouringstorms.bowels.BowelsBoss;
import net.dabicco.devouringstorms.bowels.ModBowelsEntities;
import net.dabicco.devouringstorms.command.DevouringStormsCommand;
import net.dabicco.devouringstorms.config.PendingWorldConfig;
import net.dabicco.devouringstorms.entity.ModEntityTypes;
import net.dabicco.devouringstorms.entity.WitheredStarEntity;
import net.dabicco.devouringstorms.entity.cluster.WitherStormClusterEntity;
import net.dabicco.devouringstorms.entity.withered.WitheredMobs;
import net.dabicco.devouringstorms.menu.ModMenus;
import net.dabicco.devouringstorms.nether.NetherScaleManager;
import net.dabicco.devouringstorms.network.CaveRumblePayload;
import net.dabicco.devouringstorms.network.CommandBlockPowerPayload;
import net.dabicco.devouringstorms.network.FormidibombFlashPayload;
import net.dabicco.devouringstorms.network.SpawnStructurePayload;
import net.dabicco.devouringstorms.network.StormPulsePayload;
import net.dabicco.devouringstorms.network.StormRemovedPacket;
import net.dabicco.devouringstorms.network.WitherSicknessPayload;
import net.dabicco.devouringstorms.network.WitherStormPositionPacket;
import net.dabicco.devouringstorms.network.WitheredCastPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
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

public class DevouringStormsMod implements ModInitializer {
   private static final Map<UUID, Boolean> insideTower = new HashMap();
   public static final String MOD_ID = "devouringstorms";
   public static final Logger LOGGER = LoggerFactory.getLogger("devouringstorms");
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
      PayloadTypeRegistry.clientboundPlay().register(StormPulsePayload.TYPE, StormPulsePayload.CODEC);
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
      String version = (String)FabricLoader.getInstance().getModContainer("devouringstorms").map((c) -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown version");
      BRAND.info("Dabicco's Wither Storm Mod - {}", version);
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> DevouringStormsCommand.register(dispatcher));
      SigeonNetwork.register();
      StrippableBlockRegistry.register(ModBlocks.WITHERED_LOG, ModBlocks.STRIPPED_WITHERED_LOG);
      DevouringStormsCommand.registerTick();
      NetherScaleManager.registerTick();
      ServerTickEvents.END_LEVEL_TICK.register(WitheredMobs::serverTick);
      ServerTickEvents.END_LEVEL_TICK.register(BowelsGravity::tick);
      ServerTickEvents.END_LEVEL_TICK.register(WitheredStarEntity::tick);
      ServerTickEvents.END_LEVEL_TICK.register((ServerTickEvents.EndLevelTick)(level) -> {
         if (level.getGameTime() % 20L == 0L) {
            CaveRumble.tick(level);
         }

      });
      ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayConnectionEvents.Disconnect)(handler, server) -> CaveRumble.forget(handler.getPlayer().getUUID()));
      ServerLifecycleEvents.SERVER_STOPPED.register((ServerLifecycleEvents.ServerStopped)(server) -> WitheredMobs.clear());
      ServerLifecycleEvents.SERVER_STARTED.register(StormSpawnPlatform::onServerStarted);
      ServerLifecycleEvents.SERVER_STARTED.register(PendingWorldConfig::applyTo);
      ServerTickEvents.END_SERVER_TICK.register((ServerTickEvents.EndTick)(srv) -> {
         StormSpawnPlatform.spawnTowerDust(srv);
         if (srv.getTickCount() % 20 == 0) {
            ServerLevel bowels = srv.getLevel(BowelsGravity.BOWELS);
            if (bowels != null) {
               BowelsBoss.tick(bowels);
            }
         }

         if (srv.getTickCount() % 10 == 0) {
            for(ServerPlayer p : srv.getPlayerList().getPlayers()) {
               boolean in = StormSpawnPlatform.insideTower(p.position());
               Boolean was = (Boolean)insideTower.get(p.getUUID());
               if (was == null || was != in) {
                  insideTower.put(p.getUUID(), in);
                  Vec3 heart = StormSpawnPlatform.towerHeart();
                  ServerPlayNetworking.send(p, new SpawnStructurePayload(in, heart.x, heart.y, heart.z));
               }
            }

         }
      });
      ServerLivingEntityEvents.AFTER_DEATH.register((ServerLivingEntityEvents.AfterDeath)(entity, damageSource) -> WitheredMobs.onDeath(entity, damageSource));
      EntityTrackingEvents.START_TRACKING.register((EntityTrackingEvents.StartTracking)(entity, player) -> {
         if (entity instanceof WitherStormClusterEntity cluster) {
            cluster.sendBlocksTo(player);
         }

      });
   }

   private static void registerBrewing() {
      FabricPotionBrewingBuilder.BUILD.register((FabricPotionBrewingBuilder.BuildCallback)(builder) -> {
         Ingredient fragment = Ingredient.of(ModItems.WITHER_FRAGMENT);
         Ingredient redstone = Ingredient.of(Items.REDSTONE);
         builder.registerPotionRecipe(Potions.INVISIBILITY, fragment, ModPotions.HYPER_INVISIBILITY);
         builder.registerPotionRecipe(Potions.LONG_INVISIBILITY, fragment, ModPotions.HYPER_INVISIBILITY);
         builder.registerPotionRecipe(ModPotions.HYPER_INVISIBILITY, redstone, ModPotions.LONG_HYPER_INVISIBILITY);
         builder.registerPotionRecipe(ModPotions.LONG_HYPER_INVISIBILITY, redstone, ModPotions.EXTENDED_HYPER_INVISIBILITY);
      });
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("devouringstorms", path);
   }
}
