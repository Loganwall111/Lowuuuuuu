package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$GameTab"}
)
public abstract class CreateWorldGameTabMixin extends GridLayoutTab {
   public CreateWorldGameTabMixin(Component title) {
      super(title);
   }

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void dabyws$addServerConfigButton(CreateWorldScreen parent, CallbackInfo ci) {
      if (this.getLayout() instanceof GridLayout grid) {
         int[] var6 = new int[]{0};
         grid.visitChildren(child -> var6[0]++);
         Button button = Button.builder(
               Component.literal("Dabicco's Wither Storm Server"), b -> Minecraft.getInstance().gui.setScreen(new WitherStormConfigScreen(parent, true))
            )
            .width(210)
            .build();
         grid.addChild(button, var6[0], 0);
      }
   }
}
