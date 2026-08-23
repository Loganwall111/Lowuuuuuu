package net.dabicco.devouringstorms.client.gui;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public final class DiscoveryHighlight {
   private DiscoveryHighlight() {
   }

   public static boolean shouldShow() {
      return !DevouringStormsClientConfig.configOpened;
   }

   public static void draw(GuiGraphicsExtractor g, AbstractWidget widget) {
      if (shouldShow() && widget != null && widget.visible) {
         float t = (float)(Util.getMillis() % 1600L) / 1600.0F;
         float pulse = 0.5F + 0.5F * Mth.sin((double)(t * ((float)Math.PI * 2F)));
         int grow = 1 + Math.round(pulse * 2.0F);
         int alpha = 150 + Math.round(pulse * 105.0F);
         int color = alpha << 24 | 16767036;
         int x0 = widget.getX() - grow;
         int y0 = widget.getY() - grow;
         int x1 = widget.getX() + widget.getWidth() + grow;
         int y1 = widget.getY() + widget.getHeight() + grow;
         g.fill(x0, y0, x1, y0 + 1, color);
         g.fill(x0, y1 - 1, x1, y1, color);
         g.fill(x0, y0, x0 + 1, y1, color);
         g.fill(x1 - 1, y0, x1, y1, color);
      }
   }
}
