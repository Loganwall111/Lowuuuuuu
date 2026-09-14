package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LevelRenderer;
import net.mcsm.extras.client.McsmSkybox;

/**
 * Build #375: the sky-layer fix. When the real rendered sky sphere is fully
 * faded in, cancel the vanilla sky pass for this frame - that removes the
 * giant void (the base storm-darken/black-core tint) and prevents any second
 * sky layer from painting over the sphere. While the sphere is fading in or
 * out the pass runs normally, so the cross-fade to the regular vanilla sky
 * stays smooth.
 */
@Mixin(LevelRenderer.class)
public class McsmSkyPassGateMixin {

    @Inject(method = "lambda$addSkyPass$0", at = @At("HEAD"), cancellable = true, require = 1)
    private void dabyws$mcsmSkyPassGate(CallbackInfo ci) {
        if (McsmSkybox.skyPassCancellable()) {
            ci.cancel();
        }
    }
}
