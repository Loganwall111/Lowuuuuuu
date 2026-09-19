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
   private static final SoundEvent[] VOCALS;
   private static final double NEAR_CUTOFF = (double)60.0F;
   private static final double MAX_RANGE = (double)500.0F;
   private static int nextIn;
   private static int echoIn;
   private static SoundEvent echoSound;
   private static double echoX;
   private static double echoY;
   private static double echoZ;
   private static float echoVol;
   private static float echoPitch;

   private StormDistantVocals() {
   }

   public static void tick(Minecraft mc) {
      if (mc.player != null && mc.level != null && DabyWSClientConfig.stormAmbience) {
         if (!mc.isPaused()) {
            if (mc.level.tickRateManager().runsNormally()) {
               RandomSource rng = mc.level.getRandom();
               if (echoIn == 0 && echoSound != null) {
                  playFromStorm(mc, echoSound, echoX, echoY, echoZ, echoVol * 0.5F, echoPitch * 0.9F);
                  echoSound = null;
               }

               if (echoIn >= 0) {
                  --echoIn;
               }

               if (nextIn > 0) {
                  --nextIn;
               } else {
                  Vec3 me = mc.player.position();
                  double bestSq = Double.MAX_VALUE;
                  double sx = (double)0.0F;
                  double sy = (double)0.0F;
                  double sz = (double)0.0F;
                  boolean found = false;

                  for(ClientDistantStormManager.StormData s : ClientDistantStormManager.all()) {
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
                     if (!(dist < (double)60.0F) && !(dist > (double)500.0F)) {
                        float t = (float)Mth.clamp((dist - (double)60.0F) / (double)440.0F, (double)0.0F, (double)1.0F);
                        float userVol = (float)Math.max((double)0.0F, DabyWSClientConfig.headSoundsVolume);
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
      }
   }

   private static void playFromStorm(Minecraft mc, SoundEvent sound, double x, double y, double z, float volume, float pitch) {
      if (!(volume <= 0.0F)) {
         mc.getSoundManager().play(new SimpleSoundInstance(sound.location(), SoundSource.HOSTILE, volume, pitch, RandomSource.create(), false, 0, Attenuation.NONE, x, y, z, false));
      }
   }

   static {
      VOCALS = new SoundEvent[]{ModSounds.HEAD_GROWL, ModSounds.HEAD_SHORT_GROWL, ModSounds.HEAD_SNARL, ModSounds.HEAD_ROAR};
      nextIn = 0;
      echoIn = -1;
   }
}
