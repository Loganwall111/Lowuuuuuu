package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.config.PendingWorldConfig;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SelectWorldScreen.class})
public class SelectWorldScreenMixin {
   @Inject(
      method = {"init"},
      at = {@At("TAIL")}
   )
   private void dabyws$forgetPendingWorldConfig(CallbackInfo ci) {
      PendingWorldConfig.clear();
   }
}
