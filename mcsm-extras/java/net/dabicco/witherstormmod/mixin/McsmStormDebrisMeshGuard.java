package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormDebris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cancels custom StormDebris geometry; native block particles own the bonus pass. */
@Mixin(StormDebris.class)
public abstract class McsmStormDebrisMeshGuard {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mcsm$cancelSubmit(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "submitEarly", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mcsm$cancelEarly(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "submitSeveredCloud", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mcsm$cancelSevered(CallbackInfo ci) {
        ci.cancel();
    }
}
