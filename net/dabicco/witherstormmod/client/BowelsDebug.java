package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.BowelsFrame;
import net.dabicco.witherstormmod.BowelsGravity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class BowelsDebug {
   private static final boolean ENABLED = Boolean.getBoolean("dabyws.bowels");

   private BowelsDebug() {
   }

   public static void tick(Minecraft client) {
      if (ENABLED) {
         LocalPlayer player = client.player;
         if (player != null && client.level != null) {
            if (BowelsGravity.isBowels(client.level)) {
               if (client.level.getGameTime() % 20L == 0L) {
                  Direction of = BowelsFrame.of(player);
                  Direction box = BowelsFrame.boxAxis(player);
                  Direction step = BowelsFrame.active(player);
                  Vec3 eye = player.getEyePosition(1.0F);
                  Vec3 cam = client.gameRenderer.mainCamera().position();
                  Vec3 vel = player.getDeltaMovement();
                  System.out.printf("[bowels] of=%s box=%s step=%s noGrav=%s ground=%s%n         pos=(%.2f %.2f %.2f)%n         aabb=%s%n         eye=(%.2f %.2f %.2f) cam=(%.2f %.2f %.2f) apart=%.2f%n         vel=(%.4f %.4f %.4f)%n", of, box, step, player.isNoGravity(), player.onGround(), player.getX(), player.getY(), player.getZ(), player.getBoundingBox(), eye.x, eye.y, eye.z, cam.x, cam.y, cam.z, eye.distanceTo(cam), vel.x, vel.y, vel.z);
               }
            }
         }
      }
   }
}
