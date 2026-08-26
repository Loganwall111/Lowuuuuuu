package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Storm proximity fog. As the player gets closer to a Wither Storm (whether a real
 * entity nearby or a distant storm drawn by the client), the fog closes in and goes
 * thick, giving that Story-Mode "the storm is bearing down on me" feel. Returns a
 * fog multiplier in (0, 1]; 1 = no effect, smaller = denser.
 */
public final class StormFog {
   private static final double CLOSE_RADIUS = 160.0;

   private StormFog() {
   }

   public static float fogScale() {
      if (!DabyWSClientConfig.stormFog || DabyWSClientConfig.stormFogStrength <= 0.0) {
         return 1.0F;
      }

      float t = closeness();
      if (t <= 0.0F) {
         return 1.0F;
      }

      float scale = (float)(1.0 - (double)t * DabyWSClientConfig.stormFogStrength);
      return Math.max(scale, 0.05F);
   }

   /** Proximity (0..1) to the nearest storm: 0 = far, 1 = right on top of it. */
   public static float closeness() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null) {
         return 0.0F;
      }

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

      for (ClientDistantStormManager.StormData s : ClientDistantStormManager.all()) {
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
      }

      double dist = Math.sqrt(best);
      return (float)Math.max(0.0, 1.0 - dist / CLOSE_RADIUS);
   }
}
