package net.dabicco.witherstormmod.config;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;

public class WitherStormConfigs {
   public static final SavedDataType<net.dabicco.witherstormmod.config.WitherStormWorldConfig> TYPE = new SavedDataType(
      Identifier.fromNamespaceAndPath("witherstormmod", "dabiccos_wither_storm"),
      net.dabicco.witherstormmod.config.WitherStormWorldConfig::new,
      net.dabicco.witherstormmod.config.WitherStormWorldConfig.CODEC,
      DataFixTypes.LEVEL
   );

   public static net.dabicco.witherstormmod.config.WitherStormWorldConfig get(Level level) {
      if (level instanceof ServerLevel server) {
         return (net.dabicco.witherstormmod.config.WitherStormWorldConfig)server.getDataStorage().computeIfAbsent(TYPE);
      } else {
         throw new IllegalStateException("Cannot access Wither Storm config client-side");
      }
   }
}
