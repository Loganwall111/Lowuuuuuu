package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class StormAtmosphereOverlay {
   private static float snatchShake = 0.0F;

   private StormAtmosphereOverlay() {
   }

   public static void triggerShake(float amount) {
      snatchShake = Math.min(1.0F, snatchShake + amount);
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null && mc.level != null) {
         int w = g.guiWidth();
         int h = g.guiHeight();
         long now = System.currentTimeMillis();

         // 1. Storm Proximity Vignette & Atmospheric Border Darkness
         float stormFactor = StormSkyDarken.factor();
         if (stormFactor > 0.04F) {
            float pulse = (float)(Math.sin((double)now * 0.002) * 0.08 + 0.92);
            float intensity = Mth.clamp(stormFactor * pulse, 0.0F, 1.0F);
            int alphaMax = (int)(intensity * 140.0F);
            if (alphaMax > 2) {
               int topColor = (alphaMax << 24) | 0x120320;
               int sideColor = ((alphaMax * 3 / 4) << 24) | 0x0E021A;
               
               // Top & Bottom gradients
               int barHeight = Math.max(16, (int)((float)h * 0.22F * intensity));
               g.fillGradient(0, 0, w, barHeight, topColor, 0x00120320);
               g.fillGradient(0, h - barHeight, w, h, 0x00120320, topColor);

               // Left & Right gradients
               int barWidth = Math.max(16, (int)((float)w * 0.16F * intensity));
               g.fillGradient(0, 0, barWidth, h, sideColor, 0x000E021A);
               g.fillGradient(w - barWidth, 0, w, h, 0x000E021A, sideColor);
            }
         }

         // 2. Wither Sickness Necrotic Infection Creeping Veins Overlay
         float infection = ClientSicknessManager.getInfection(player.getId());
         if (infection > 0.02F) {
            float heartBeat = (float)(Math.sin((double)now * 0.0055) * 0.25 + 0.75);
            float veinAlpha = Mth.clamp(infection * heartBeat, 0.0F, 1.0F);
            int vAlpha = (int)(veinAlpha * 180.0F);
            if (vAlpha > 3) {
               int veinColor = (vAlpha << 24) | 0x2A063C;
               int cornerSize = Math.max(20, (int)((float)Math.min(w, h) * 0.35F * infection));

               // Draw creeping corner accents
               g.fillGradient(0, 0, cornerSize, cornerSize / 2, veinColor, 0x002A063C);
               g.fillGradient(0, h - cornerSize / 2, cornerSize, h, 0x002A063C, veinColor);
               g.fillGradient(w - cornerSize, 0, w, cornerSize / 2, veinColor, 0x002A063C);
               g.fillGradient(w - cornerSize, h - cornerSize / 2, w, h, 0x002A063C, veinColor);

               // Necrotic border lines
               int edgeThick = Math.max(1, (int)(4.0F * infection));
               g.fill(0, 0, w, edgeThick, (vAlpha / 2) << 24 | 0x480868);
               g.fill(0, h - edgeThick, w, h, (vAlpha / 2) << 24 | 0x480868);
               g.fill(0, 0, edgeThick, h, (vAlpha / 2) << 24 | 0x480868);
               g.fill(w - edgeThick, 0, w, h, (vAlpha / 2) << 24 | 0x480868);
            }
         }

         // 3. Shake & Snatch Tension Overlay
         if (snatchShake > 0.01F) {
            snatchShake = Math.max(0.0F, snatchShake - 0.04F);
            int shakeAlpha = (int)(snatchShake * 90.0F);
            if (shakeAlpha > 2) {
               g.fill(0, 0, w, h, (shakeAlpha << 24) | 0x380540);
            }
         }
      }
   }
}
