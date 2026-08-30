package net.dabicco.witherstormmod.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.dabicco.witherstormmod.client.gui.DevouringStormsConfigScreen;

public class ModMenuIntegration implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return DevouringStormsConfigScreen::new;
   }
}
