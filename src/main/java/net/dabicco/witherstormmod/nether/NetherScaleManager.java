package net.dabicco.witherstormmod.nether;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.config.WitherStormWorldConfig;
import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.NetherScaleEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class NetherScaleManager {
   private static final Map<UUID, Long> NEXT_ELIGIBLE = new HashMap<>();
   private static final double ROOF_Y = 127.0;
   private static final float SCALE = 6.0F;
   private static final double ANCHOR_Y = 162.0;
   private static final double OVERSHOOT = 14.0;
   private static final double MIN_PLUNGE = 30.0;
   private static final double MAX_PLUNGE = 110.0;

   private NetherScaleManager() {
   }

   public static void registerTick() {
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         if (server.getTickCount() % 20 == 0) {
            tick(server);
         }
      });
   }

   private static void tick(MinecraftServer server) {
      ServerLevel nether = server.getLevel(Level.NETHER);
      if (nether != null) {
         WitherStormWorldConfig cfg = WitherStormConfigs.get(nether);
         long now = (long)server.getTickCount();
         NEXT_ELIGIBLE.keySet().removeIf(uuid -> {
            ServerPlayer p = server.getPlayerList().getPlayer(uuid);
            return p == null || p.level() != nether;
         });

         for (ServerPlayer player : nether.players()) {
            if (player.isAlive() && !player.isSpectator()) {
               Long next = NEXT_ELIGIBLE.get(player.getUUID());
               if (next == null) {
                  NEXT_ELIGIBLE.put(player.getUUID(), now + intervalTicks(cfg));
               } else if (now >= next) {
                  NEXT_ELIGIBLE.put(player.getUUID(), now + intervalTicks(cfg));
                  if (cfg.netherScale > 0 && !(player.getY() >= 127.0) && anyStormAtPhase(server, 5.1)) {
                     trigger(nether, player.position());
                  }
               }
            }
         }
      }
   }

   private static long intervalTicks(WitherStormWorldConfig cfg) {
      int base = Math.max(1, cfg.netherScaleInterval);
      int jitter = Math.max(0, cfg.netherScaleRandom);
      int extra = jitter > 0 ? (int)(Math.random() * (double)jitter) : 0;
      return (long)(base + extra) * 20L;
   }

   private static boolean anyStormAtPhase(MinecraftServer server, double minPhase) {
      for (ServerLevel level : server.getAllLevels()) {
         if (!level.getEntities(ModEntityTypes.WITHER_STORM, ws -> ws.getPhase() >= minPhase).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public static boolean trigger(ServerLevel level, Vec3 near) {
      double plunge = Mth.clamp(162.0 - near.y + 14.0, 30.0, 110.0);
      NetherScaleEntity e = new NetherScaleEntity(ModEntityTypes.NETHER_SCALE, level);
      e.setPos(near.x, 162.0, near.z);
      e.setup(6.0F, plunge, level.getRandom().nextFloat() * 360.0F);
      return level.addFreshEntity(e);
   }
}
