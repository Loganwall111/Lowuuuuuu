package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.WorldOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldOptionsScreen.class})
public class WorldOptionsScreenMixin {
   @Inject(
      method = {"init"},
      at = {@At("TAIL")}
   )
   private void addWitherStormButton(CallbackInfo ci) {
      WorldOptionsScreen screen = (WorldOptionsScreen)(Object)this;
      Button button = Button.builder(
            Component.literal("Dabicco's Wither Storm"), b -> Minecraft.getInstance().setScreen(new WitherStormConfigScreen(screen))
         )
         .width(200)
         .build();
      button.setPosition(screen.width / 2 - 100, 35);
      ((ScreenInvoker)screen).invokeAddRenderableWidget(button);
   }
}
