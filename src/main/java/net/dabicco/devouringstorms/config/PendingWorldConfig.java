package net.dabicco.devouringstorms.config;

import net.dabicco.devouringstorms.DevouringStormsMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class PendingWorldConfig {
   private static WitherStormWorldConfig pending;

   private PendingWorldConfig() {
   }

   public static WitherStormWorldConfig getOrCreate() {
      if (pending == null) {
         pending = new WitherStormWorldConfig();
      }

      return pending;
   }

   public static boolean has() {
      return pending != null;
   }

   public static void set(WitherStormWorldConfig config) {
      pending = new WitherStormWorldConfig();
      pending.applyArray(config.toArray());
   }

   public static void clear() {
      pending = null;
   }

   public static void applyTo(MinecraftServer server) {
      if (pending != null) {
         ServerLevel overworld = server.overworld();
         if (overworld != null) {
            WitherStormWorldConfig live = WitherStormConfigs.get(overworld);
            live.applyArray(pending.toArray());
            live.setDirty();
            DevouringStormsMod.LOGGER.info("[wither storm] applied the settings chosen at world creation");
            clear();
         }
      }
   }
}
