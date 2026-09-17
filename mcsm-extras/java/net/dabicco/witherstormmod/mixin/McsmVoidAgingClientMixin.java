package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.DabyWitherStormModClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.mcsm.extras.client.McsmVoidAgingPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BUILD #463 -- the client end of void aging.
 *
 * <p>One receiver, registered in the same client init the mob bodies use
 * ({@code DabyWitherStormModClient.onInitializeClient}, the same hook the base mod
 * registers its own payloads from). {@code require = 0}: a client that cannot take
 * the hook still runs, it simply never sees anybody's body change.
 */
@Mixin(DabyWitherStormModClient.class)
public abstract class McsmVoidAgingClientMixin {

    @Inject(method = "onInitializeClient", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$voidAgingReceiver(CallbackInfo ci) {
        try {
            ClientPlayNetworking.registerGlobalReceiver(McsmVoidAgingPayload.TYPE,
                    McsmVoidAgingPayload::handleClient);
            // this hook is the client's own half of net.minecraft.client's game
            Minecraft client = Minecraft.getInstance();
            System.out.println("[ds] void aging: the body knows what the void is doing"
                    + (client == null ? " (no client)" : ""));
        } catch (Throwable t) {
            System.err.println("[ds] void aging could not reach the client: " + t);
        }
    }
}
