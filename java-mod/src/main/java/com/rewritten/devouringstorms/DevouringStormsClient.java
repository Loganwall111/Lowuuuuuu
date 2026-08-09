package com.rewritten.devouringstorms;

import com.rewritten.devouringstorms.client.GlitchParticle;
import com.rewritten.devouringstorms.client.StormClientState;
import com.rewritten.devouringstorms.client.StormMusicDirector;
import com.rewritten.devouringstorms.client.StormVisuals;
import com.rewritten.devouringstorms.client.renderers.AnnaRenderer;
import com.rewritten.devouringstorms.client.renderers.MassgRenderer;
import com.rewritten.devouringstorms.client.renderers.PreacherRenderer;
import com.rewritten.devouringstorms.client.renderers.TownsfolkRenderer;
import com.rewritten.devouringstorms.client.renderers.SeveredRenderer;
import com.rewritten.devouringstorms.client.renderers.StormMiteRenderer;
import com.rewritten.devouringstorms.client.renderers.SymbiontRenderer;
import com.rewritten.devouringstorms.client.renderers.TheTakenRenderer;
import com.rewritten.devouringstorms.client.renderers.TonyaRenderer;
import com.rewritten.devouringstorms.client.renderers.TravisRenderer;
import com.rewritten.devouringstorms.client.renderers.VoidMawRenderer;
import com.rewritten.devouringstorms.client.renderers.CreatorRenderer;
import com.rewritten.devouringstorms.client.renderers.CreatorHandRenderer;
import com.rewritten.devouringstorms.client.renderers.MonstrosityRenderer;
import com.rewritten.devouringstorms.client.renderers.ForgerRenderer;
import com.rewritten.devouringstorms.client.renderers.SkyTentacleRenderer;
import com.rewritten.devouringstorms.client.renderers.CartShopperRenderer;
import com.rewritten.devouringstorms.client.renderers.ResearcherRenderer;
import com.rewritten.devouringstorms.client.renderers.EarthEaterRenderer;
import com.rewritten.devouringstorms.client.renderers.TazoRenderer;
import com.rewritten.devouringstorms.client.renderers.WatcherRenderer;
import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.StormSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/**
 * Client bootstrap: entity renderers, the glitch particle, storm-state packets,
 * the horror overlay, and the storm music director.
 */
public final class DevouringStormsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // ---- renderers ----
        EntityModelLayerRegistry.registerModelLayer(MassgRenderer.LAYER, MassgRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(WatcherRenderer.LAYER, WatcherRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(TazoRenderer.LAYER, TazoRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(AnnaRenderer.LAYER, AnnaRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(SeveredRenderer.LAYER, SeveredRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(SymbiontRenderer.LAYER, SymbiontRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(PreacherRenderer.LAYER, PreacherRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(TownsfolkRenderer.LAYER, TownsfolkRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(StormMiteRenderer.LAYER, StormMiteRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(TheTakenRenderer.LAYER, TheTakenRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(TravisRenderer.LAYER, TravisRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(TonyaRenderer.LAYER, TonyaRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(VoidMawRenderer.LAYER, VoidMawRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(CreatorRenderer.LAYER, CreatorRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(CreatorHandRenderer.LAYER, CreatorHandRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(MonstrosityRenderer.LAYER, MonstrosityRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(ForgerRenderer.LAYER, ForgerRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(SkyTentacleRenderer.LAYER, SkyTentacleRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(CartShopperRenderer.LAYER, CartShopperRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(ResearcherRenderer.LAYER, ResearcherRenderer::createModelData);
        EntityModelLayerRegistry.registerModelLayer(EarthEaterRenderer.LAYER, EarthEaterRenderer::createModelData);

        EntityRendererRegistry.register(ModEntities.MASSG, MassgRenderer::new);
        EntityRendererRegistry.register(ModEntities.WATCHER, WatcherRenderer::new);
        EntityRendererRegistry.register(ModEntities.TAZO, TazoRenderer::new);
        EntityRendererRegistry.register(ModEntities.ANNA_APPARITION, AnnaRenderer::new);
        EntityRendererRegistry.register(ModEntities.SEVERED_STORM, SeveredRenderer::new);
        EntityRendererRegistry.register(ModEntities.WITHERED_SYMBIONT, SymbiontRenderer::new);
        EntityRendererRegistry.register(ModEntities.PREACHER, PreacherRenderer::new);
        EntityRendererRegistry.register(ModEntities.TOWNSFOLK, TownsfolkRenderer::new);
        EntityRendererRegistry.register(ModEntities.STORM_MITE, StormMiteRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_TAKEN, TheTakenRenderer::new);
        EntityRendererRegistry.register(ModEntities.TRAVIS, TravisRenderer::new);
        EntityRendererRegistry.register(ModEntities.TONYA, TonyaRenderer::new);
        EntityRendererRegistry.register(ModEntities.VOID_MAW, VoidMawRenderer::new);
        EntityRendererRegistry.register(ModEntities.CREATOR, CreatorRenderer::new);
        EntityRendererRegistry.register(ModEntities.CREATOR_HAND, CreatorHandRenderer::new);
        EntityRendererRegistry.register(ModEntities.MONSTROSITY, MonstrosityRenderer::new);
        EntityRendererRegistry.register(ModEntities.FORGER, ForgerRenderer::new);
        EntityRendererRegistry.register(ModEntities.SKY_TENTACLE, SkyTentacleRenderer::new);
        EntityRendererRegistry.register(ModEntities.CART_SHOPPER, CartShopperRenderer::new);
        EntityRendererRegistry.register(ModEntities.RESEARCHER, ResearcherRenderer::new);
        EntityRendererRegistry.register(ModEntities.EARTH_EATER, EarthEaterRenderer::new);
        EntityRendererRegistry.register(ModEntities.FORMIDI_BOMB, ThrownItemRenderer::new);

        // ---- particles ----
        ParticleFactoryRegistry.getInstance().register(ModParticles.GLITCH, GlitchParticle.Factory::new);

        // ---- storm state from the server ----
        ClientPlayNetworking.registerGlobalReceiver(StormSyncPayload.TYPE,
            (payload, context) -> StormClientState.update(
                payload.phase(), payload.growth(), payload.critical(), payload.stormActive(), payload.intensity()));

        // ---- translucent rift portal glass ----
        // (API note: older Fabric versions spell this BlockRenderLayerMap / RenderLayer.)
        net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap.put(
            net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT,
            com.rewritten.devouringstorms.registry.ModBlocks.RIFT_PORTAL);

        // ---- horror overlay ----
        HudElementRegistry.addLast(DevouringStorms.id("storm_overlay"), StormVisuals::render);

        // ---- storm music ----
        ClientTickEvents.END_CLIENT_TICK.register(StormMusicDirector::tick);

        DevouringStorms.LOGGER.info("[DevouringStorms] Client systems listening. The system is watching.");
    }
}
