package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.item.RetrieverTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RetrieverClientTooltip implements ClientTooltipComponent {
   private static final int SLOT = 20;
   private final ItemStack tnt;
   private final ItemStack rockets;

   public RetrieverClientTooltip(RetrieverTooltip data) {
      this.tnt = new ItemStack(Items.TNT, Math.max(1, data.tnt()));
      this.rockets = new ItemStack(Items.FIREWORK_ROCKET, Math.max(1, data.rockets()));
   }

   public int getWidth(Font font) {
      return 42;
   }

   public int getHeight(Font font) {
      return 20;
   }

   public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
      graphics.item(this.tnt, x + 2, y + 2);
      graphics.itemDecorations(font, this.tnt, x + 2, y + 2);
      graphics.item(this.rockets, x + 20 + 2, y + 2);
      graphics.itemDecorations(font, this.rockets, x + 20 + 2, y + 2);
   }
}
