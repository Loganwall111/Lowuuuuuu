package net.dabicco.witherstormmod.menu;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.fabricmc.fabric.api.registry.MenuTypeRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.FabricMenuTypeBuilder;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
   public static final MenuType<FurnaceFilterMenu> FURNACE_FILTER;

   public static void initialize() {
      MenuTypeRegistry.register(DabyWitherStormMod.id("furnace_filter"), FURNACE_FILTER);
   }

   static {
      FURNACE_FILTER = (MenuType)FabricMenuTypeBuilder.create(FurnaceFilterMenu::new).build();
   }
}
