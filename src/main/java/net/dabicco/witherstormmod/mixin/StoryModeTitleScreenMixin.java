package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreen.class})
public abstract class StoryModeTitleScreenMixin extends Screen {
   protected StoryModeTitleScreenMixin(Component title) {
      super(title);
   }

   @Inject(
      method = {"init"},
      at = {@At("TAIL")}
   )
   private void dabyws$addStoryModeButtons(CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      TitleScreen self = (TitleScreen)(Object)this;

      Button settingsBtn = Button.builder(
         Component.literal("§5§l⚡ Storm Config"),
         b -> mc.setScreen(new WitherStormConfigScreen(self))
      ).bounds(self.width - 142, 6, 136, 18).build();

      Button previewBtn = Button.builder(
         Component.literal("§d§l👁 3D Storm Preview"),
         b -> mc.setScreen(WitherStormConfigScreen.createGiganticPreview(self))
      ).bounds(self.width - 142, 27, 136, 18).build();

      ((ScreenInvoker)self).invokeAddRenderableWidget(settingsBtn);
      ((ScreenInvoker)self).invokeAddRenderableWidget(previewBtn);
   }

   @Inject(
      method = {"extractRenderState"},
      at = {@At("TAIL")}
   )
   private void dabyws$renderStoryModeBanner(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      TitleScreen self = (TitleScreen)(Object)this;
      
      // Story Mode Top & Bottom Cinematic Atmospheric Bars
      g.fillGradient(0, 0, self.width, 50, 0xC0120220, 0x00120220);
      g.fillGradient(0, self.height - 38, self.width, self.height, 0x00120220, 0xC0120220);

      // Story Mode Title Badge
      long ms = System.currentTimeMillis();
      float pulse = (float)(Math.sin((double)ms * 0.0035) * 0.5 + 0.5);
      int pulseAlpha = 0xFF000000 | ((int)(180 + pulse * 75) << 16) | ((int)(60 + pulse * 80) << 8) | 255;
      
      g.text(self.getMinecraft().font, "§5§lMINECRAFT: STORY MODE §8| §d§lWITHER STORM ULTIMATE", 10, 8, pulseAlpha, true);
      g.text(self.getMinecraft().font, "§8Trailer Accurate Boss Overhaul §7— All Phases + Devourer", 10, 20, -6710887, true);
   }
}
