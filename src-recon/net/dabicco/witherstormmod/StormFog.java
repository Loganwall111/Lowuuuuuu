package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class StormFog {
   private static final double CLOSE_RADIUS = 160.0;

   private StormFog() {
   }

   public static float fogScale() {
      if (DabyWSClientConfig.stormFog && !(DabyWSClientConfig.stormFogStrength <= 0.0)) {
         float t = closeness();
         if (t <= 0.0F) {
            return 1.0F;
         } else {
            float scale = (float)(1.0 - t * DabyWSClientConfig.stormFogStrength);
            return Math.max(scale, 0.05F);
         }
      } else {
         return 1.0F;
      }
   }

   public static float closeness() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         Vec3 cam = mc.player.position();
         double best = Double.MAX_VALUE;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof WitherStormEntity) {
               double d = entity.distanceToSqr(cam.x, cam.y, cam.z);
               if (d < best) {
                  best = d;
               }
            }
         }

         for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData s : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
            double dx = s.x - cam.x;
            double dy = s.y - cam.y;
            double dz = s.z - cam.z;
            double d = dx * dx + dy * dy + dz * dz;
            if (d < best) {
               best = d;
            }
         }

         if (best >= Double.MAX_VALUE) {
            return 0.0F;
         } else {
            double dist = Math.sqrt(best);
            return (float)Math.max(0.0, 1.0 - dist / 160.0);
         }
      } else {
         return 0.0F;
      }
   }
}
