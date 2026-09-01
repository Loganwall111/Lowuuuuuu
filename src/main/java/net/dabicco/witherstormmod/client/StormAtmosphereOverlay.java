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
         if (DabyWSClientConfig.stormProximityVignette && stormFactor > 0.04F) {
            float pulse = (float)(Math.sin((double)now * 0.002) * 0.08 + 0.92);
            float intensity = Mth.clamp(stormFactor * pulse * (float)DabyWSClientConfig.vignetteIntensity, 0.0F, 1.0F);
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
         if (DabyWSClientConfig.sicknessVeinOverlay && infection > 0.02F) {
            float heartBeat = (float)(Math.sin((double)now * 0.0055) * 0.25 + 0.75);
            float veinAlpha = Mth.clamp(infection * heartBeat * (float)DabyWSClientConfig.sicknessVeinIntensity, 0.0F, 1.0F);
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

         // 3. Shake & Snatch Tension Overlay & Story Mode Escape QTE Prompt
         if (snatchShake > 0.01F) {
            snatchShake = Math.max(0.0F, snatchShake - 0.03F);
            int shakeAlpha = (int)(snatchShake * 95.0F);
            if (shakeAlpha > 2) {
               g.fill(0, 0, w, h, (shakeAlpha << 24) | 0x380540);
            }

            // Story Mode Quick-Time Event Escape Prompt
            if (snatchShake > 0.25F) {
               float qtePulse = (float)(Math.sin((double)now * 0.01) * 0.5 + 0.5);
               int qteColor = qtePulse > 0.5F ? -171 : -43691;
               String qteText = "§e§l[ RAPIDLY ATTACK WITH WEAPON TO BREAK FREE! ]";
               g.centeredText(mc.font, qteText, w / 2, h / 2 + 35, qteColor);
               g.fill(w / 2 - 110, h / 2 + 48, w / 2 + 110, h / 2 + 52, 0xAA220533);
               int progW = (int)(216.0F * snatchShake);
               g.fill(w / 2 - 108, h / 2 + 49, w / 2 - 108 + progW, h / 2 + 51, -11141121);
            }
         }

         // 4. Story Mode Episodic Chapter Card (Fades in near colossal phases)
         if (DabyWSClientConfig.storyModeBossbar && stormFactor > 0.75F) {
            float titlePulse = (float)(Math.sin((double)now * 0.003) * 0.15 + 0.85);
            int titleAlpha = (int)(titlePulse * 220.0F);
            if (titleAlpha > 20) {
               g.centeredText(mc.font, "§5§lMINECRAFT: STORY MODE §8— §d§lTHE DEVOURER", w / 2, 8, (titleAlpha << 24) | 0xDDAAEE);
            }
         }
      }
   }
}
