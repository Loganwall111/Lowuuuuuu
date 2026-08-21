package net.dabicco.witherstormmod.menu;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
   public static final MenuType<FurnaceFilterMenu> FURNACE_FILTER = (MenuType<FurnaceFilterMenu>)Registry.register(
      BuiltInRegistries.MENU, DabyWitherStormMod.id("furnace_filter"), new MenuType(FurnaceFilterMenu::new, FeatureFlags.VANILLA_SET)
   );

   public static void initialize() {
   }
}
