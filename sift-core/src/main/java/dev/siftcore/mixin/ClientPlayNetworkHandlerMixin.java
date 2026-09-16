package dev.siftcore.mixin;

import dev.siftcore.SiftDimensions;
import dev.siftcore.client.SiftTransitionClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onPlayerRespawn", at = @At("HEAD"))
    private void sift$armSilentRespawn(PlayerRespawnS2CPacket packet, CallbackInfo callbackInfo) {
        if (SiftDimensions.THE_SIFT.equals(packet.getDimension())) {
            SiftTransitionClient.arm();
        }
    }
}
