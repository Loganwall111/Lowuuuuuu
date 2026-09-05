package net.dabicco.witherstormmod.menu;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
   public static final MenuType<net.dabicco.witherstormmod.menu.FurnaceFilterMenu> FURNACE_FILTER = (MenuType<net.dabicco.witherstormmod.menu.FurnaceFilterMenu>)Registry.register(
      BuiltInRegistries.MENU,
      DabyWitherStormMod.id("furnace_filter"),
      new MenuType(net.dabicco.witherstormmod.menu.FurnaceFilterMenu::new, FeatureFlags.VANILLA_SET)
   );

   public static void initialize() {
   }
}
