package dev.siftcore.mixin;

import dev.siftcore.client.SiftTransitionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void sift$skipDimensionLoadingScreen(@Nullable Screen screen, CallbackInfo callbackInfo) {
        if (SiftTransitionClient.consumeLoadingScreen(screen)) {
            callbackInfo.cancel();
        }
    }
}
