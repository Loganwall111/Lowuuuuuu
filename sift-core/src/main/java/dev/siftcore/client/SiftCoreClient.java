package dev.siftcore.client;

import dev.siftcore.SiftCore;
import dev.siftcore.SiftDimensions;
import dev.siftcore.rift.SiftRiftRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class SiftCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CoreShaderRegistrationCallback.EVENT.register(SiftShaders::register);
        EntityRendererRegistry.register(SiftCore.SIFT_RIFT, SiftRiftRenderer::new);

        DimensionRenderingRegistry.registerDimensionEffects(
                SiftDimensions.EFFECTS,
                new SiftDimensionEffects()
        );
        DimensionRenderingRegistry.registerSkyRenderer(
                SiftDimensions.THE_SIFT,
                new SiftSkyRenderer()
        );
        WorldRenderEvents.AFTER_ENTITIES.register(SiftFluidRenderer::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                SiftTransitionClient.clear();
            } else {
                SiftTransitionClient.tick();
            }
        });
    }
}
