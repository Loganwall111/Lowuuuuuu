package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.DabyWitherStormModClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BUILD #456 -- the bodies get their bones.
 *
 * <p>An entity type with no renderer is not drawn: that is how the beasts could be
 * real, spawnable, walking mobs and still invisible in game. This hooks the same
 * client init the base mod registers its own renderers from -- and the same method
 * McsmStoryRendererMixin already uses for the Story Mode cast -- and registers the
 * four mob meshes and the five renderers around them.
 *
 * <p>Layers first, because a renderer bakes its layer when it is built, and both
 * halves are wrapped: a client that cannot take the whole set should still run.
 */
@Mixin(DabyWitherStormModClient.class)
public abstract class McsmMobRendererMixin {

    @Inject(method = "onInitializeClient", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$mobBodies(CallbackInfo ci) {
        try {
            net.mcsm.extras.client.McsmMobRenderers.registerLayers();
            net.mcsm.extras.client.McsmMobRenderers.registerRenderers();
            System.out.println("[ds] the mobs have bodies: massg, creator, whale, voidwalker, lurker");
        } catch (Throwable ignored) {
            // the mod keeps running with vanilla-shaped monsters rather than not at all
        }
    }
}
