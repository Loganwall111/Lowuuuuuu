package net.dabicco.witherstormmod.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Lightweight title-screen branding drawn through the title-screen mixin. */
public final class DevouringMainMenuOverlay {
   private DevouringMainMenuOverlay() { }

   public static void render(GuiGraphicsExtractor graphics, int width, int height) {
      graphics.fill(0, height - 18, width, height, 0xA0000000);
      graphics.centeredText(net.minecraft.client.Minecraft.getInstance().font,
         "Devouring Storms: The Point of No Return", width / 2, height - 14, 0xFFD38CFF);
   }
}
