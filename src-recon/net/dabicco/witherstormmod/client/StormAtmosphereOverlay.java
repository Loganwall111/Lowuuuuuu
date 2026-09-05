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
         float stormFactor = net.dabicco.witherstormmod.client.StormSkyDarken.factor();
         if (DabyWSClientConfig.stormProximityVignette && stormFactor > 0.04F) {
            float pulse = (float)(Math.sin(now * 0.002) * 0.08 + 0.92);
            float intensity = Mth.clamp(stormFactor * pulse * (float)DabyWSClientConfig.vignetteIntensity, 0.0F, 1.0F);
            int alphaMax = (int)(intensity * 140.0F);
            if (alphaMax > 2) {
               int topColor = alphaMax << 24 | 1180448;
               int sideColor = alphaMax * 3 / 4 << 24 | 918042;
               int barHeight = Math.max(16, (int)(h * 0.22F * intensity));
               g.fillGradient(0, 0, w, barHeight, topColor, 1180448);
               g.fillGradient(0, h - barHeight, w, h, 1180448, topColor);
               int barWidth = Math.max(16, (int)(w * 0.16F * intensity));
               g.fillGradient(0, 0, barWidth, h, sideColor, 918042);
               g.fillGradient(w - barWidth, 0, w, h, 918042, sideColor);
            }
         }

         float infection = net.dabicco.witherstormmod.client.ClientSicknessManager.getInfection(player.getId());
         if (DabyWSClientConfig.sicknessVeinOverlay && infection > 0.02F) {
            float heartBeat = (float)(Math.sin(now * 0.0055) * 0.25 + 0.75);
            float veinAlpha = Mth.clamp(infection * heartBeat * (float)DabyWSClientConfig.sicknessVeinIntensity, 0.0F, 1.0F);
            int vAlpha = (int)(veinAlpha * 180.0F);
            if (vAlpha > 3) {
               int veinColor = vAlpha << 24 | 2754108;
               int cornerSize = Math.max(20, (int)(Math.min(w, h) * 0.35F * infection));
               g.fillGradient(0, 0, cornerSize, cornerSize / 2, veinColor, 2754108);
               g.fillGradient(0, h - cornerSize / 2, cornerSize, h, 2754108, veinColor);
               g.fillGradient(w - cornerSize, 0, w, cornerSize / 2, veinColor, 2754108);
               g.fillGradient(w - cornerSize, h - cornerSize / 2, w, h, 2754108, veinColor);
               int edgeThick = Math.max(1, (int)(4.0F * infection));
               g.fill(0, 0, w, edgeThick, vAlpha / 2 << 24 | 4720744);
               g.fill(0, h - edgeThick, w, h, vAlpha / 2 << 24 | 4720744);
               g.fill(0, 0, edgeThick, h, vAlpha / 2 << 24 | 4720744);
               g.fill(w - edgeThick, 0, w, h, vAlpha / 2 << 24 | 4720744);
            }
         }

         if (snatchShake > 0.01F) {
            snatchShake = Math.max(0.0F, snatchShake - 0.03F);
            int shakeAlpha = (int)(snatchShake * 95.0F);
            if (shakeAlpha > 2) {
               g.fill(0, 0, w, h, shakeAlpha << 24 | 3671360);
            }

            if (snatchShake > 0.25F) {
               float qtePulse = (float)(Math.sin(now * 0.01) * 0.5 + 0.5);
               int qteColor = qtePulse > 0.5F ? -171 : -43691;
               String qteText = "§e§l[ RAPIDLY ATTACK WITH WEAPON TO BREAK FREE! ]";
               g.centeredText(mc.font, qteText, w / 2, h / 2 + 35, qteColor);
               g.fill(w / 2 - 110, h / 2 + 48, w / 2 + 110, h / 2 + 52, -1440611021);
               int progW = (int)(216.0F * snatchShake);
               g.fill(w / 2 - 108, h / 2 + 49, w / 2 - 108 + progW, h / 2 + 51, -11141121);
            }
         }

         if (DabyWSClientConfig.storyModeBossbar && stormFactor > 0.75F) {
            float titlePulse = (float)(Math.sin(now * 0.003) * 0.15 + 0.85);
            int titleAlpha = (int)(titlePulse * 220.0F);
            if (titleAlpha > 20) {
               g.centeredText(mc.font, "§5§lMINECRAFT: STORY MODE §8— §d§lTHE DEVOURER", w / 2, 8, titleAlpha << 24 | 14527214);
            }
         }
      }
   }
}
