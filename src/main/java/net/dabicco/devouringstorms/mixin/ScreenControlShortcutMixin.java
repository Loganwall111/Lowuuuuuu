package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.gui.WitherStormConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Screen.class})
public class ScreenControlShortcutMixin {
   @Inject(
      method = {"keyPressed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void devouringstorms$openConfigOnCtrlO(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
      Screen screen = (Screen)(Object)this;
      if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && keyCode == GLFW.GLFW_KEY_O && !(screen instanceof WitherStormConfigScreen)) {
         Minecraft.getInstance().gui.setScreen(new WitherStormConfigScreen(screen));
         cir.setReturnValue(true);
      }
   }
}
