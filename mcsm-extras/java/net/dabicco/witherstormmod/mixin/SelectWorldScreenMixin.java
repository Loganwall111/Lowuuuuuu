package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.config.PendingWorldConfig;
import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Devouring Storms: Reskins SelectWorldScreen with Image 3 silver pixel border frame.
 */
@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

   protected SelectWorldScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void dabyws$forgetPendingWorldConfig(CallbackInfo ci) {
      PendingWorldConfig.clear();
   }

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void dabyws$worldSelectionBorder(GuiGraphicsExtractor g, int mouseX, int mouseY,
         float partialTick, CallbackInfo ci) {
      int w = this.width;
      int h = this.height;

      // Image 3 Silver Pixel Border Frame
      if (McsmExtrasConfig.uiBorderLines) {
         int borderCol = 0xFF8A8A9E; // Silver-gray
         int innerCol  = 0xFF2A2A38;
         g.fill(0, 0, w, 2, borderCol);
         g.fill(0, h - 2, w, h, borderCol);
         g.fill(0, 0, 2, h, borderCol);
         g.fill(w - 2, 0, w, h, borderCol);

         g.fill(4, 4, w - 4, 5, innerCol);
         g.fill(4, h - 5, w - 4, h - 4, innerCol);
         g.fill(4, 4, 5, h - 4, innerCol);
         g.fill(w - 5, 4, w - 4, h - 4, innerCol);

         // L-shape Corner Accents
         g.fill(2, 2, 10, 4, borderCol);
         g.fill(2, 2, 4, 10, borderCol);
         g.fill(w - 10, 2, w - 2, 4, borderCol);
         g.fill(w - 4, 2, w - 2, 10, borderCol);
         g.fill(2, h - 4, 10, h - 2, borderCol);
         g.fill(2, h - 10, 4, h - 2, borderCol);
         g.fill(w - 10, h - 4, w - 2, h - 2, borderCol);
         g.fill(w - 4, h - 10, w - 2, h - 2, borderCol);
      }
   }
}
