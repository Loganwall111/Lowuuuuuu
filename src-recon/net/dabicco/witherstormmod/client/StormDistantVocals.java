package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class StormDistantVocals {
   private static final SoundEvent[] VOCALS = new SoundEvent[]{ModSounds.HEAD_GROWL, ModSounds.HEAD_SHORT_GROWL, ModSounds.HEAD_SNARL, ModSounds.HEAD_ROAR};
   private static final double NEAR_CUTOFF = 60.0;
   private static final double MAX_RANGE = 500.0;
   private static int nextIn = 0;
   private static int echoIn = -1;
   private static SoundEvent echoSound;
   private static double echoX;
   private static double echoY;
   private static double echoZ;
   private static float echoVol;
   private static float echoPitch;

   private StormDistantVocals() {
   }

   public static void tick(Minecraft mc) {
      if (mc.player != null && mc.level != null && DabyWSClientConfig.stormAmbience && !mc.isPaused() && mc.level.tickRateManager().runsNormally()) {
         RandomSource rng = mc.level.getRandom();
         if (echoIn == 0 && echoSound != null) {
            playFromStorm(mc, echoSound, echoX, echoY, echoZ, echoVol * 0.5F, echoPitch * 0.9F);
            echoSound = null;
         }

         if (echoIn >= 0) {
            echoIn--;
         }

         if (nextIn > 0) {
            nextIn--;
         } else {
            Vec3 me = mc.player.position();
            double bestSq = Double.MAX_VALUE;
            double sx = 0.0;
            double sy = 0.0;
            double sz = 0.0;
            boolean found = false;

            for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData s : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
               if (!s.collapsed) {
                  double dx = s.x - me.x;
                  double dy = s.y - me.y;
                  double dz = s.z - me.z;
                  double dSq = dx * dx + dy * dy + dz * dz;
                  if (dSq < bestSq) {
                     bestSq = dSq;
                     sx = s.x;
                     sy = s.y;
                     sz = s.z;
                     found = true;
                  }
               }
            }

            if (!found) {
               nextIn = 40;
            } else {
               double dist = Math.sqrt(bestSq);
               if (!(dist < 60.0) && !(dist > 500.0)) {
                  float t = (float)Mth.clamp((dist - 60.0) / 440.0, 0.0, 1.0);
                  float userVol = (float)Math.max(0.0, DabyWSClientConfig.headSoundsVolume);
                  float vol = (0.65F - 0.18F * t) * userVol;
                  float pitch = 0.72F - 0.14F * t;
                  SoundEvent snd = VOCALS[rng.nextInt(VOCALS.length)];
                  playFromStorm(mc, snd, sx, sy, sz, vol, pitch);
                  echoSound = snd;
                  echoX = sx;
                  echoY = sy;
                  echoZ = sz;
                  echoVol = vol;
                  echoPitch = pitch;
                  echoIn = 5 + rng.nextInt(4);
                  nextIn = 200 + rng.nextInt(280);
               } else {
                  nextIn = 40;
               }
            }
         }
      }
   }

   private static void playFromStorm(Minecraft mc, SoundEvent sound, double x, double y, double z, float volume, float pitch) {
      if (!(volume <= 0.0F)) {
         mc.getSoundManager()
            .play(
               new SimpleSoundInstance(sound.location(), SoundSource.HOSTILE, volume, pitch, RandomSource.create(), false, 0, Attenuation.NONE, x, y, z, false)
            );
      }
   }
}
