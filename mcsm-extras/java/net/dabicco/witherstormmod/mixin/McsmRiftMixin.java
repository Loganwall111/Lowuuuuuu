package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.mcsm.extras.McsmReality;

/**
 * Devouring Storms: the rift gesture (mandate D.8, phase 1).
 *
 * Hold the Rift Key and sneak and the world rips open; do it again inside and
 * the rift spits you back. The check lives on the SERVER player's tick, so it
 * behaves identically in singleplayer and on a dedicated server without a
 * single network payload of our own -- and it needs no key binding, so it
 * cannot collide with the base mod's own controls.
 *
 * require = 0 on purpose: if a future client renames or moves
 * ServerPlayer#tick, this degrades to "the rift never opens" instead of
 * refusing to launch the game. The handler takes only CallbackInfo, so it
 * carries no signature dependency on the target's parameters at all.
 */
@Mixin(ServerPlayer.class)
public abstract class McsmRiftMixin {

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void mcsm$riftGesture(CallbackInfo ci) {
        try {
            McsmReality.tickServer((ServerPlayer) (Object) this);
        } catch (Throwable ignored) {
            // the rift must never take down a player tick
        }
    }
}
