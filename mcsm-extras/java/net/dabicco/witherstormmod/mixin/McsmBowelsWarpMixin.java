package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.BowelsPortal;
import net.minecraft.server.level.ServerPlayer;
import net.mcsm.extras.McsmWarp;

/**
 * Mega-phase 6b: replaces the instant bowels teleport with the warp entry
 * sequence. Common mixin (singleplayer integrated server shares the client
 * JVM); require = 0 so a jar without the portal class simply keeps the
 * original instant teleport.
 */
@Mixin(BowelsPortal.class)
public abstract class McsmBowelsWarpMixin {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dabyws$warpEntry(ServerPlayer player, CallbackInfo ci) {
        if (McsmWarp.begin(player)) {
            ci.cancel();
        }
    }
}
