package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.DabyWitherStormModClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.mcsm.extras.client.McsmVoidDeep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BUILD #479 -- the deep's own clock.
 *
 * <p>One client tick hook, registered in the same client init the mob bodies, the
 * terminal and void aging already use ({@code DabyWitherStormModClient.onInitialize}).
 * {@code require = 0}: a client that cannot take the hook still runs the mod, it
 * simply has no gel.
 */
@Mixin(DabyWitherStormModClient.class)
public abstract class McsmVoidDeepClientMixin {

    @Inject(method = "onInitializeClient", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$voidDeepTick(CallbackInfo ci) {
        try {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                try {
                    McsmVoidDeep.tick();
                    // BUILD #481 -- and the rudder: the tier's own current, the
                    // tool's multiplier, and the dive's trail. Reflective so jar compiles without V2 extras
                    try {
                        Class.forName("net.mcsm.extras.client.McsmVoidRudder").getMethod("tick").invoke(null);
                    } catch (Throwable ignore) {}
                    // V2 - Sift infinite cosmos + black hole backdrop + animated skyboxes
                    try {
                        Class.forName("net.mcsm.sift.client.McsmSiftClient").getMethod("tickClient").invoke(null);
                    } catch (Throwable ignore) {}
                } catch (Throwable ignored) {
                    // a frame is never lost to the gel
                }
            });
            System.out.println("[ds] the deep is awake (gel, bubbles, flight, "
                    + "bioluminescence; no shader required) + Sift V2 (black hole lensing, animated skyboxes)");
        } catch (Throwable t) {
            System.err.println("[ds] the deep could not hook the client tick: " + t);
        }
    }
}
