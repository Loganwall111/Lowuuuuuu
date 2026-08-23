package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.BowelsFrame;
import net.dabicco.devouringstorms.BowelsGravity;
import net.dabicco.devouringstorms.BowelsTrace;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BowelsHud {
   private BowelsHud() {
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      Minecraft client = Minecraft.getInstance();
      LocalPlayer player = client.player;
      if (DevouringStormsClientConfig.bowelsFrameHud) {
         if (player != null && client.level != null) {
            if (BowelsGravity.isBowels(client.level)) {
               Direction of = BowelsFrame.of(player);
               Direction box = BowelsFrame.boxAxis(player);
               Direction step = BowelsFrame.active(player);
               Vec3 eye = player.getEyePosition(1.0F);
               Vec3 cam = client.gameRenderer.mainCamera().position();
               Vec3 vel = player.getDeltaMovement();
               String[] var10000 = new String[8];
               String var10003 = String.valueOf(of);
               var10000[0] = "BOWELS  of=" + var10003 + "  box=" + String.valueOf(box) + "  step=" + String.valueOf(step);
               boolean var22 = player.isNoGravity();
               var10000[1] = "noGrav=" + var22 + "  onGround=" + player.onGround();
               String var23 = String.valueOf(player.getPose());
               var10000[2] = "pose=" + var23 + "  eyeH=" + String.format("%.3f", player.getEyeHeight());
               var10000[3] = String.format("pos %.2f %.2f %.2f", player.getX(), player.getY(), player.getZ());
               var10000[4] = String.format("vel %.4f %.4f %.4f", vel.x, vel.y, vel.z);
               var10000[5] = String.format("eye %.2f %.2f %.2f", eye.x, eye.y, eye.z);
               var10000[6] = String.format("cam %.2f %.2f %.2f   apart %.2f", cam.x, cam.y, cam.z, eye.distanceTo(cam));
               var10000[7] = "aabb " + fmt(player.getBoundingBox());
               String[] lines = var10000;
               int y = 6;

               for(String line : lines) {
                  g.text(client.font, line, 6, y, -171, true);
                  y += 10;
               }

               Vec3 previous = BowelsTrace.previousTick();

               for(int i = 0; i < BowelsTrace.NAMES.length; ++i) {
                  Vec3 sample = BowelsTrace.sample(i);
                  if (sample != null) {
                     String line = String.format("%-8s y=%+.4f  z=%+.4f  dy=%+.4f", BowelsTrace.NAMES[i], sample.y, sample.z, previous == null ? (double)0.0F : sample.y - previous.y);
                     int colour = previous != null && Math.abs(sample.y - previous.y) > 1.0E-5 ? -43691 : -5592406;
                     g.text(client.font, line, 6, y, colour, true);
                     y += 10;
                     previous = sample;
                  }
               }

               Vec3 in = BowelsTrace.input();
               g.text(client.font, String.format("input %+.3f %+.3f %+.3f  speed %.4f", in.x, in.y, in.z, BowelsTrace.speed()), 6, y, -11141121, true);
            }
         }
      }
   }

   private static String fmt(AABB b) {
      return String.format("%.1f..%.1f  %.1f..%.1f  %.1f..%.1f", b.minX, b.maxX, b.minY, b.maxY, b.minZ, b.maxZ);
   }
}
