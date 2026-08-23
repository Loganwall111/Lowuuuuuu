package net.dabicco.devouringstorms.mixin;

import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.options.WorldOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({WorldOptionsScreen.class})
public interface WorldOptionsScreenAccessor {
   @Accessor("layout")
   HeaderAndFooterLayout getLayout();
}
