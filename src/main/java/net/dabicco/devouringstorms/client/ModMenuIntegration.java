package net.dabicco.devouringstorms.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.dabicco.devouringstorms.client.gui.WitherStormConfigScreen;

public class ModMenuIntegration implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return WitherStormConfigScreen::new;
   }
}
