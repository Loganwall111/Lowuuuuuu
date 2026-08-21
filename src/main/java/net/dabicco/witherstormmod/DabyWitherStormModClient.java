package net.dabicco.witherstormmod;

import net.dabicco.witherstormmod.client.renderer.BlackHoleRenderer;
import net.dabicco.witherstormmod.client.renderer.WitherStormClusterRenderer;
import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.model.ModEntityModelLayers;
import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Clean client entrypoint for the rewritten mod.
 *
 * Registers entity renderers and model layers. Only the renderers for entities whose
 * classes actually exist are registered. As the user adds Blockbench models, the
 * corresponding layer registration goes into {@link ModEntityModelLayers}.
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
   }
}
