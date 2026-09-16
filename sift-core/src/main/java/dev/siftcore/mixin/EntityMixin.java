package dev.siftcore.mixin;

import dev.siftcore.transfer.SiftTransfer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The only common mixin: a server-side player falling through the Overworld void. */
@Mixin(net.minecraft.entity.Entity.class)
public abstract class EntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void sift$checkVoidHandshake(CallbackInfo callbackInfo) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            SiftTransfer.tryVoidHandshake(player);
        }
    }
}
