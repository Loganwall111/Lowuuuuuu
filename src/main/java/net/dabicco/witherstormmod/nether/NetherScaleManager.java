package net.dabicco.witherstormmod.nether;

import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.NetherScaleEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class NetherScaleManager {
   private NetherScaleManager() {
   }

   public static void registerTick() {
      ServerTickEvents.END_SERVER_TICK.register((srv) -> {
         if (srv.getTickCount() % 20 == 0) {
            // Ambient nether scaling can be driven here later; see trigger() for the explicit version.
         }
      });
   }

   public static boolean trigger(ServerLevel level, Vec3 position) {
      if (level.dimension() != Level.NETHER) {
         return false;
      }

      NetherScaleEntity scale = (NetherScaleEntity)ModEntityTypes.NETHER_SCALE.create(level, EntitySpawnReason.EVENT);
      if (scale == null) {
         return false;
      }

      int roofY = level.getHeight();
      scale.setPos(position.x, (double)Math.min(roofY - 1, (int)position.y + 20), position.z);
      scale.setYRot(level.getRandom().nextFloat() * 360.0F);
      scale.setup(6.0F, 60.0D, scale.getYRot());
      level.addFreshEntity(scale);
      return true;
   }
}
