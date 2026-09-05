package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({OptionsScreen.class})
public class OptionsScreenMixin {
   @Inject(
      method = {"init"},
      at = {@At("TAIL")}
   )
   private void dabyws$addWitherStormSettingsButton(CallbackInfo ci) {
      OptionsScreen screen = (OptionsScreen)(Object)(Object)this;
      Button button = Button.builder(
            Component.literal("§5Wither Storm Settings"), b -> Minecraft.getInstance().gui.setScreen(new WitherStormConfigScreen(screen))
         )
         .width(150)
         .build();
      button.setPosition(screen.width / 2 - 75, screen.height - 26);
      ((net.dabicco.witherstormmod.mixin.ScreenInvoker)screen).invokeAddRenderableWidget(button);
   }
}
