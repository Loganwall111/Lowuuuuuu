package net.dabicco.devouringstorms.config;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;

public class WitherStormConfigs {
   public static final SavedDataType<WitherStormWorldConfig> TYPE;

   public static WitherStormWorldConfig get(Level level) {
      if (level instanceof ServerLevel server) {
         return (WitherStormWorldConfig)server.getDataStorage().computeIfAbsent(TYPE);
      } else {
         throw new IllegalStateException("Cannot access Wither Storm config client-side");
      }
   }

   static {
      TYPE = new SavedDataType(Identifier.fromNamespaceAndPath("devouringstorms", "devouring_storms_world"), WitherStormWorldConfig::new, WitherStormWorldConfig.CODEC, DataFixTypes.LEVEL);
   }
}
