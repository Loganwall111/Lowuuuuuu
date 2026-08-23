package net.dabicco.devouringstorms.client.gui;

import net.dabicco.devouringstorms.menu.FurnaceFilterMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FurnaceFilterScreen extends AbstractContainerScreen<FurnaceFilterMenu> {
   private static final Identifier BG = Identifier.fromNamespaceAndPath("devouringstorms", "textures/gui/furnace_filter.png");

   public FurnaceFilterScreen(FurnaceFilterMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
   }

   public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(g, mouseX, mouseY, partialTick);
      int x = this.leftPos;
      int y = this.topPos;
      g.blit(RenderPipelines.GUI_TEXTURED, BG, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      int prog = ((FurnaceFilterMenu)this.menu).getProgress();
      int filled = prog * 22 / 8;
      if (filled > 0) {
         g.blit(RenderPipelines.GUI_TEXTURED, BG, x + 79, y + 34, 176.0F, 14.0F, filled, 16, 256, 256);
      }

   }
}
