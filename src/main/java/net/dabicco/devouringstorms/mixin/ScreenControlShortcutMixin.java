package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.gui.WitherStormConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
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
   private void devouringstorms$openConfigOnCtrlO(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
      Screen screen = (Screen)(Object)this;
      if ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && event.key() == GLFW.GLFW_KEY_O && !(screen instanceof WitherStormConfigScreen)) {
         Minecraft.getInstance().gui.setScreen(new WitherStormConfigScreen(screen));
         cir.setReturnValue(true);
      }
   }
}
