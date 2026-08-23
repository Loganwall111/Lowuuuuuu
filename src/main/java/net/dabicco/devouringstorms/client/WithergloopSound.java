package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.BowelsGravity;
import net.dabicco.devouringstorms.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class WithergloopSound {
   private static final double FROM_PHASE = 6.1;
   private static final double FULL_AT = (double)60.0F;
   private static final double SILENT_AT = (double)220.0F;
   private static Loop loop;

   private WithergloopSound() {
   }

   public static void tick(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         if (BowelsGravity.isBowels(mc.level)) {
            stop();
         } else if (StormMusic.isPlaying()) {
            stop();
         } else {
            double best = Double.MAX_VALUE;

            for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
               if (!((double)d.phase < 6.1) && mc.level.getEntity(d.entityId) == null) {
                  best = Math.min(best, mc.player.position().distanceToSqr(d.x, d.y, d.z));
               }
            }

            if (best == Double.MAX_VALUE) {
               stop();
            } else {
               double d = Math.sqrt(best);
               float close = (float)Mth.clamp(((double)220.0F - d) / (double)160.0F, (double)0.0F, (double)1.0F);
               if (close <= 0.0F) {
                  stop();
               } else {
                  if (loop == null || loop.isStopped()) {
                     loop = new Loop();
                     mc.getSoundManager().play(loop);
                  }

                  loop.want = close * close;
               }
            }
         }
      } else {
         stop();
      }
   }

   private static void stop() {
      if (loop != null) {
         loop.fade = true;
         loop = null;
      }

   }

   private static final class Loop extends AbstractTickableSoundInstance {
      private float want;
      private float level;
      private boolean fade;
      private boolean done;

      Loop() {
         super(ModSounds.MUSIC_WITHERGLOOP, SoundSource.AMBIENT, RandomSource.create());
         this.looping = true;
         this.delay = 0;
         this.attenuation = Attenuation.NONE;
         this.relative = true;
         this.volume = 0.01F;
      }

      public void tick() {
         if (this.fade) {
            this.want = 0.0F;
         }

         float step = 0.025F;
         this.level = this.want > this.level ? Math.min(this.want, this.level + step) : Math.max(this.want, this.level - step);
         this.volume = Math.max(0.01F, this.level * StormMusic.gain());
         if (this.fade && this.level <= 0.0F) {
            this.done = true;
            this.stop();
         }

      }

      public boolean isStopped() {
         return this.done || super.isStopped();
      }
   }
}
