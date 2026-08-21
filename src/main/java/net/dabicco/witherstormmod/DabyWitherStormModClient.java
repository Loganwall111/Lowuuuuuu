package net.dabicco.witherstormmod;

import net.dabicco.witherstormmod.client.FormidibombFlash;
import net.dabicco.witherstormmod.client.StormDeathCinematic;
import net.dabicco.witherstormmod.client.renderer.BlackHoleRenderer;
import net.dabicco.witherstormmod.client.renderer.WitherStormClusterRenderer;
import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.model.ModEntityModelLayers;
import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.dabicco.witherstormmod.network.StormDeathPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudElementRegistry;

/**
 * Clean client entrypoint for the rewritten mod.
 *
 * Registers entity renderers, model layers, networking receivers and HUD overlays.
 * Only the renderers for entities whose classes actually exist are registered.
 */
public class DabyWitherStormModClient implements ClientModInitializer {
   @Override
   public void onInitializeClient() {
      // Wither Storm renderer (fresh; renders once a Blockbench model is registered).
      EntityRendererRegistry.register(ModEntityTypes.WITHER_STORM, WitherStormRenderer::new);

      // Working support renderers.
      EntityRendererRegistry.register(ModEntityTypes.BLACK_HOLE, BlackHoleRenderer::new);
      EntityRendererRegistry.register(ModEntityTypes.WITHER_STORM_CLUSTER, WitherStormClusterRenderer::new);

      // Model layers (Blockbench models are added by the user; see ModEntityModelLayers).
      ModEntityModelLayers.registerModelLayers();

      // Networking receivers.
      ClientPlayNetworking.registerGlobalReceiver(StormDeathPayload.TYPE, StormDeathPayload::handleClient);

      // HUD overlays (white-flash cinematic + formidibomb flash).
      HudElementRegistry.addLast(DabyWitherStormMod.id("storm_death"), StormDeathCinematic::render);
      HudElementRegistry.addLast(DabyWitherStormMod.id("formidibomb_flash"), FormidibombFlash::render);
   }
}
