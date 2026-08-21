package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreen.class})
public abstract class TitleScreenConfigNoticeMixin {
   @Inject(
      method = {"init"},
      at = {@At("TAIL")}
   )
   private void dabyws$configResetNotice(CallbackInfo ci) {
      if (DabyWSClientConfig.consumeRestructureNotice()) {
         Minecraft mc = Minecraft.getInstance();
         Screen title = (TitleScreen)(Object)this;
         mc.setScreenAndShow(
            new ConfirmScreen(
               accepted -> mc.setScreenAndShow(title),
               Component.literal("Wither Storm settings were reset"),
               Component.literal(
                  "Your configuration file was reset as of Beta 1.9.33.\n\nThe config was reorganised, so your saved settings no longer matched it. Please recheck the Client and Experimental tabs."
               ),
               CommonComponents.GUI_OK,
               CommonComponents.GUI_OK
            )
         );
      }
   }
}
