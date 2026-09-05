package net.dabicco.witherstormmod.config;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class PendingWorldConfig {
   private static net.dabicco.witherstormmod.config.WitherStormWorldConfig pending;

   private PendingWorldConfig() {
   }

   public static net.dabicco.witherstormmod.config.WitherStormWorldConfig getOrCreate() {
      if (pending == null) {
         pending = new net.dabicco.witherstormmod.config.WitherStormWorldConfig();
      }

      return pending;
   }

   public static boolean has() {
      return pending != null;
   }

   public static void set(net.dabicco.witherstormmod.config.WitherStormWorldConfig config) {
      pending = new net.dabicco.witherstormmod.config.WitherStormWorldConfig();
      pending.applyArray(config.toArray());
   }

   public static void clear() {
      pending = null;
   }

   public static void applyTo(MinecraftServer server) {
      if (pending != null) {
         ServerLevel overworld = server.overworld();
         if (overworld != null) {
            net.dabicco.witherstormmod.config.WitherStormWorldConfig live = net.dabicco.witherstormmod.config.WitherStormConfigs.get(overworld);
            live.applyArray(pending.toArray());
            live.setDirty();
            DabyWitherStormMod.LOGGER.info("[wither storm] applied the settings chosen at world creation");
            clear();
         }
      }
   }
}
