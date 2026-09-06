package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.FoglessRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MCSM 1.9.71 -- storm body visibility.
 *
 * Upstream selects the render path with:
 *     useCustom = fogless() || reverseShading()
 *     useCustom ? FoglessRenderTypes.bodyCutout(tex)   <-- renders nothing
 *               : MobRenderer.getRenderType(...)       <-- correct
 *
 * bodyCutout() is broken on 26.2, so with shaders OFF the storm is invisible.
 * With Iris ON, ShaderPackCompat.active() forced both helpers false, which is
 * why the storm only appeared with shaders on.
 *
 * We force BOTH gates false so the vanilla path is always taken. We must not
 * simply neutralise ShaderPackCompat.active(): fogless() is
 *     active && !legacyDistantRenderer && !ShaderPackCompat.active()
 * so making active() return false would turn fogless() TRUE and reselect the
 * broken path -- the exact opposite of the fix.
 */
@Mixin(value = FoglessRenderTypes.class, remap = false)
public abstract class McsmStormVisibilityPatch {

    @Inject(method = "fogless", at = @At("HEAD"), cancellable = true)
    private static void mcsm$forceVanillaBodyPath(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Boolean.FALSE);
    }

    @Inject(method = "reverseShading", at = @At("HEAD"), cancellable = true)
    private static void mcsm$forceVanillaShading(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Boolean.FALSE);
    }
}
