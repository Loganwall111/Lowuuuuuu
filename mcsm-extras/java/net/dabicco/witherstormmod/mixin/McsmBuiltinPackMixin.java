package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.mcsm.extras.McsmBuiltinPack;

/**
 * Registers the built-in Story Look resource pack during the mod's own
 * initialization (target and method name verified against the compiled
 * base mod), so the pack is known to the pack repository before its first
 * reload and comes up enabled by default.
 */
@Mixin(DabyWitherStormMod.class)
public abstract class McsmBuiltinPackMixin {

    @Inject(method = "onInitialize", at = @At("HEAD"), remap = false)
    private void dabyws$builtinPack(CallbackInfo ci) {
        McsmBuiltinPack.register();
    }
}
