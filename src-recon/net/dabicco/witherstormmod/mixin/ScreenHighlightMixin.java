package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.gui.DiscoveryHighlight;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.WorldOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Screen.class})
public class ScreenHighlightMixin {
   @Inject(
      method = {"extractRenderState"},
      at = {@At("TAIL")}
   )
   private void dabyws$discoveryHighlight(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      if (DiscoveryHighlight.shouldShow()) {
         Screen screen = (Screen)(Object)(Object)this;
         String wanted;
         if (screen instanceof OptionsScreen) {
            wanted = Component.translatable("options.worldOptions.button").getString();
         } else {
            if (!(screen instanceof WorldOptionsScreen)) {
               return;
            }

            wanted = "Dabicco's Wither Storm";
         }

         for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget && widget.getMessage() != null && wanted.equals(widget.getMessage().getString())) {
               DiscoveryHighlight.draw(g, widget);
               return;
            }
         }
      }
   }
}
