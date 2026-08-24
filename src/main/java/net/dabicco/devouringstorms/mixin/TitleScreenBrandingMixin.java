package net.dabicco.devouringstorms.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Screen.class})
public abstract class TitleScreenBrandingMixin {
   private static final Identifier LOGO = Identifier.fromNamespaceAndPath("devouringstorms", "textures/gui/title/devouring_logo.png");
   private static final Identifier ICON = Identifier.fromNamespaceAndPath("devouringstorms", "textures/gui/title/devouring_icon.png");

   @Inject(
      method = {"extractRenderState"},
      at = {@At("TAIL")}
   )
   private void dabyws$drawBranding(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      if (!((Object)this instanceof TitleScreen)) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();
      int width = mc.getWindow().getGuiScaledWidth();
      int logoW = Math.min(360, width - 48);
      int logoH = logoW * 640 / 2048;
      int logoX = (width - logoW) / 2;
      int logoY = 14;
      int icon = Math.max(28, logoH / 2);
      int iconX = logoX - icon + 10;
      int iconY = logoY + Math.max(0, (logoH - icon) / 2);
      g.blit(RenderPipelines.GUI_TEXTURED, LOGO, logoX, logoY, 0.0F, 0.0F, logoW, logoH, 2048, 640);
      g.blit(RenderPipelines.GUI_TEXTURED, ICON, iconX, iconY, 0.0F, 0.0F, icon, icon, 256, 256);
   }
}
