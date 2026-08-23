package net.dabicco.devouringstorms.menu;

import net.dabicco.devouringstorms.DevouringStormsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
   public static final MenuType<FurnaceFilterMenu> FURNACE_FILTER = (MenuType<FurnaceFilterMenu>)Registry.register(
      BuiltInRegistries.MENU, DevouringStormsMod.id("furnace_filter"), new MenuType(FurnaceFilterMenu::new, FeatureFlags.VANILLA_SET)
   );

   public static void initialize() {
   }
}
