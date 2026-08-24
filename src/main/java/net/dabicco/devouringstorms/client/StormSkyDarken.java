package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormSkyDarken {
   private static final double RANGE = (double)420.0F;
   private static final double CORE_FRAC = 0.6;
   private static final double DARKEN_START_PHASE = (double)5.0F;
   private static final double DARKEN_FULL_PHASE = 5.8;
   private static final float MAX_DARKEN = 0.94F;
   private static float displayed;
   /** smoothed phase of the dominant storm in range, drives the palette stage */
   private static float palettePhase;
   /** proximity/ownership of the current sky palette, 0 = no override, 1 = full storm takeover */
   private static float paletteBlend;
   /**
    * Phase 6's rise: when the dominant palette crosses into the split storm
    * the whole sky dips black for a few seconds while the storm rises, then
    * settles back into the pinkish-purple phase-6 look.
    */
   private static float riseBlack;
   private static float lastPalettePhase;

   public static float floorR() {
      return (float)DevouringStormsClientConfig.skyDarkenR;
   }

   public static float floorG() {
      return (float)DevouringStormsClientConfig.skyDarkenG;
   }

   public static float floorB() {
      return (float)DevouringStormsClientConfig.skyDarkenB;
   }

   private static float latePurpleShift() {
      float t = StormPalettes.phaseAmount(palettePhase, 5.15F, 5.38F);
      return t * Math.max(displayed, paletteBlend);
   }

   public static float skyBaseR() {
      return Mth.lerp(latePurpleShift(), 0.17F, floorR());
   }

   public static float skyBaseG() {
      return Mth.lerp(latePurpleShift(), 0.18F, floorG());
   }

   public static float skyBaseB() {
      return Mth.lerp(latePurpleShift(), 0.21F, floorB());
   }

   public static float cloudBaseR() {
      return Mth.lerp(latePurpleShift(), 0.24F, (float)DevouringStormsClientConfig.cloudColorR);
   }

   public static float cloudBaseG() {
      return Mth.lerp(latePurpleShift(), 0.26F, (float)DevouringStormsClientConfig.cloudColorG);
   }

   public static float cloudBaseB() {
      return Mth.lerp(latePurpleShift(), 0.30F, (float)DevouringStormsClientConfig.cloudColorB);
   }

   /** Smoothed phase driving this frame's atmosphere palette (0 when no storm is in range). */
   public static float palettePhase() {
      return palettePhase;
   }

   public static boolean globalVisualsActive() {
      return DevouringStormsClientConfig.globalMcsmVisuals && DevouringStormsClientConfig.globalMcsmStrength > 0.01;
   }

   public static boolean globalCloudDeckActive() {
      return globalVisualsActive() && DevouringStormsClientConfig.globalMcsmCloudDeck && Math.round((float)DevouringStormsClientConfig.stormCloudDeck) > 0;
   }

   public static float globalPhase() {
      return Mth.clamp((float)DevouringStormsClientConfig.globalMcsmPhase, 4.5F, 6.15F);
   }

   public static float globalBlend() {
      return globalVisualsActive() ? Mth.clamp((float)DevouringStormsClientConfig.globalMcsmStrength, 0.0F, 1.0F) : 0.0F;
   }

   /** How strongly the phase palette should override the user's manual colours right now. */
   public static float paletteBlend() {
      return paletteBlend;
   }

   /** The phase-6 black-sky dip while the split storm rises (1 = fully black, decays over a few seconds). */
   public static float riseBlack() {
      return riseBlack * Math.min(1.0F, paletteBlend * 1.6F);
   }

   private static float applyRiseBlack(float value) {
      float black = riseBlack();
      return black <= 0.001F ? value : Mth.lerp(black, value, 0.012F);
   }

   public static float fogR() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return applyRiseBlack(Mth.lerp(StormPalettes.strength() * paletteBlend, DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorR : floorR(), c[0]));
      } else {
         return DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorR : floorR();
      }
   }

   public static float fogG() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return applyRiseBlack(Mth.lerp(StormPalettes.strength() * paletteBlend, DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorG : floorG(), c[1]));
      } else {
         return DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorG : floorG();
      }
   }

   public static float fogB() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return applyRiseBlack(Mth.lerp(StormPalettes.strength() * paletteBlend, DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorB : floorB(), c[2]));
      } else {
         return DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorB : floorB();
      }
   }

   public static float skyR() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.skyColor(palettePhase, new float[3]);
         return applyRiseBlack(Mth.lerp(StormPalettes.strength() * paletteBlend, skyBaseR(), c[0]));
      }
      return skyBaseR();
   }

   public static float skyG() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.skyColor(palettePhase, new float[3]);
         return applyRiseBlack(Mth.lerp(StormPalettes.strength() * paletteBlend, skyBaseG(), c[1]));
      }
      return skyBaseG();
   }

   public static float skyB() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.skyColor(palettePhase, new float[3]);
         return applyRiseBlack(Mth.lerp(StormPalettes.strength() * paletteBlend, skyBaseB(), c[2]));
      }
      return skyBaseB();
   }

   private StormSkyDarken() {
   }

   public static float factor() {
      return Math.min(1.0F, displayed + SpawnTowerGloom.darken());
   }

   public static void update(Vec3 cameraPos, float partialTick) {
      var storms = ClientDistantStormManager.all();
      float globalPhase = globalPhase();
      float globalBlend = globalBlend();
      float globalStage = Mth.clamp((globalPhase - 4.35F) / 1.35F, 0.0F, 1.0F);
      globalStage = 0.12F + 0.88F * globalStage * globalStage * (3.0F - 2.0F * globalStage);
      float globalTarget = globalBlend * globalStage * (float)DevouringStormsClientConfig.skyDarkenIntensity;
      if (storms.isEmpty() && globalTarget <= 0.0F) {
         displayed += (0.0F - displayed) * 0.22F;
         if (displayed < 0.002F) {
            displayed = 0.0F;
         }

         palettePhase = 0.0F;
         paletteBlend = 0.0F;
         if (riseBlack > 0.0F) {
            riseBlack = Math.max(0.0F, riseBlack - 0.05F);
         }
         return;
      }

      float target = globalTarget;

      for(ClientDistantStormManager.StormData d : storms) {
         if (!((double)d.phase < (double)5.0F)) {
            float growthScale = (float)WitherStormEntity.clientGrowthScaleForPhase(Math.max(d.phase, d.expansionPhase));
            double dx = d.dispX - cameraPos.x;
            double dy = d.dispY - cameraPos.y;
            double dz = d.dispZ - cameraPos.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double reach = 420.0 + Math.max(0.0F, growthScale - 1.0F) * 180.0;
            if (!(dist > reach)) {
               float phaseRamp = (float)Mth.clamp(((double)d.phase - (double)5.0F) / 0.7999999999999998, (double)0.0F, (double)1.0F);
               double frac = dist / reach;
               float proximity;
               if (frac <= 0.6) {
                  proximity = 1.0F;
               } else {
                  float edge = (float)((double)1.0F - (frac - 0.6) / 0.4);
                  proximity = edge * edge * (3.0F - 2.0F * edge);
               }

               float intensity = (float)DevouringStormsClientConfig.skyDarkenIntensity;
               target = Math.max(target, proximity * phaseRamp * 0.94F * intensity);
            }
         }
      }

      displayed += (target - displayed) * 0.06F;
      if (displayed < 0.002F) {
         displayed = 0.0F;
      }

      // palette phase: pick the dominant nearby storm by proximity/ownership,
      // but preserve its real phase so the handoff follows the screenshot notes:
      // phase 4 stays normal, phase 4.5 turns green, phase 5 goes turquoise,
      // then only later does the sky drift pink/purple.
      float phaseTarget = globalBlend > 0.0F ? globalPhase : 0.0F;
      float blendTarget = globalBlend;
      float bestScore = globalBlend * globalStage;

      for(ClientDistantStormManager.StormData d : storms) {
         double dx = d.dispX - cameraPos.x;
         double dy = d.dispY - cameraPos.y;
         double dz = d.dispZ - cameraPos.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist <= 620.0F && d.phase >= 4.5F) {
            float proximity = 1.0F;
            if (dist > 240.0F) {
               proximity = (float)(1.0F - (dist - 240.0F) / 380.0F);
            }

            proximity = Mth.clamp(proximity, 0.0F, 1.0F);
            if (proximity > 0.05F) {
               float stage = Mth.clamp((d.phase - 4.35F) / 1.35F, 0.0F, 1.0F);
               stage = 0.12F + 0.88F * stage * stage * (3.0F - 2.0F * stage);
               float score = proximity * stage;
               if (score > bestScore) {
                  bestScore = score;
                  blendTarget = proximity;
                  phaseTarget = d.phase;
               }
            }
         }
      }

      palettePhase += (phaseTarget - palettePhase) * (phaseTarget > palettePhase ? 0.060F : 0.040F);
      paletteBlend += (blendTarget - paletteBlend) * 0.055F;
      // Phase-6 rise: crossing 5.9 blacks the sky out for a few seconds, then
      // the black decays and the pinkish-purple phase-6 palette returns.
      if (palettePhase >= 5.9F && lastPalettePhase < 5.9F) {
         riseBlack = 1.0F;
      }
      lastPalettePhase = palettePhase;
      if (riseBlack > 0.0F) {
         riseBlack = Math.max(0.0F, riseBlack - 0.0125F);
      }
      if (palettePhase < 0.01F) {
         palettePhase = 0.0F;
      }
      if (paletteBlend < 0.01F) {
         paletteBlend = 0.0F;
      }

   }

   public static void clear() {
      displayed = 0.0F;
      palettePhase = 0.0F;
      paletteBlend = 0.0F;
      riseBlack = 0.0F;
      lastPalettePhase = 0.0F;
   }
}
