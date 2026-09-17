package net.dabicco.witherstormmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class SpawnTowerGloom {
   private static Vec3 heart;
   private static final double REACH = 5.0;
   private static final float MAX_DARKEN = 0.22F;
   private static final float MAX_FOG_PULL = 0.45F;

   private SpawnTowerGloom() {
   }

   public static void set(boolean inside, double x, double floorY, double z) {
      heart = inside ? new Vec3(x, floorY, z) : null;
   }

   public static float factor() {
      if (heart == null) {
         return 0.0F;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null) {
            return 0.0F;
         } else {
            double dist = mc.player.position().distanceTo(heart);
            if (dist >= 5.0) {
               return 0.0F;
            } else {
               float t = (float)(1.0 - dist / 5.0);
               return t * t;
            }
         }
      }
   }

   public static float darken() {
      return factor() * 0.22F;
   }

   public static float fogScale() {
      return 1.0F - factor() * 0.45F;
   }
}
