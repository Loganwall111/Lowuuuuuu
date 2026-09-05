package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.BowelsGravity;
import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.bowels.BowelsHeartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public final class BowelsMusic {
   private static final double FULL_AT = 12.0;
   private static final double SILENT_AT = 90.0;
   private static final float DUCK_FLOOR = 0.18F;
   private static final float FINAL_DUCK = 0.1F;
   private static net.dabicco.witherstormmod.client.BowelsMusic.Layer overlay;
   private static net.dabicco.witherstormmod.client.BowelsMusic.Layer finalTheme;
   private static boolean overlaySpent;

   private BowelsMusic() {
   }

   public static void tick(Minecraft mc) {
      if (mc.level != null && mc.player != null && BowelsGravity.isBowels(mc.level)) {
         BowelsHeartEntity heart = nearestHeart(mc);
         boolean fighting = heart != null && heart.isFighting();
         if (heart == null) {
            overlaySpent = false;
         }

         if (fighting) {
            overlaySpent = true;
            if (overlay != null) {
               overlay.fade = true;
               overlay = null;
            }

            net.dabicco.witherstormmod.client.StormMusic.setDuck(0.1F);
            if (finalTheme == null || finalTheme.isStopped()) {
               finalTheme = new net.dabicco.witherstormmod.client.BowelsMusic.Layer(ModSounds.MUSIC_FINAL);
               finalTheme.want = 1.0F;
               mc.getSoundManager().play(finalTheme);
            }
         } else {
            if (finalTheme != null) {
               finalTheme.fade = true;
               finalTheme = null;
            }

            if (overlaySpent) {
               net.dabicco.witherstormmod.client.StormMusic.setDuck(1.0F);
            } else {
               if (overlay == null || overlay.isStopped()) {
                  overlay = new net.dabicco.witherstormmod.client.BowelsMusic.Layer(ModSounds.MUSIC_BOWELS_OVERLAY);
                  mc.getSoundManager().play(overlay);
               }

               double d = heart == null ? 90.0 : Math.sqrt(mc.player.distanceToSqr(heart.getX(), heart.getY(), heart.getZ()));
               float close = (float)Mth.clamp((90.0 - d) / 78.0, 0.0, 1.0);
               close = close * close * (3.0F - 2.0F * close);
               overlay.want = close;
               net.dabicco.witherstormmod.client.StormMusic.setDuck(1.0F - 0.82F * close);
            }
         }
      } else {
         stopAll();
      }
   }

   private static BowelsHeartEntity nearestHeart(Minecraft mc) {
      BowelsHeartEntity best = null;
      double bestDist = Double.MAX_VALUE;

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof BowelsHeartEntity h) {
            double d = mc.player.distanceToSqr(h);
            if (d < bestDist) {
               bestDist = d;
               best = h;
            }
         }
      }

      return best;
   }

   private static void stopAll() {
      if (overlay != null) {
         overlay.fade = true;
         overlay = null;
      }

      if (finalTheme != null) {
         finalTheme.fade = true;
         finalTheme = null;
      }

      overlaySpent = false;
      net.dabicco.witherstormmod.client.StormMusic.setDuck(1.0F);
   }

   private static final class Layer extends AbstractTickableSoundInstance {
      private float want;
      private float level;
      private boolean fade;
      private boolean done;

      Layer(SoundEvent event) {
         super(event, SoundSource.AMBIENT, RandomSource.create());
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

         float step = 0.033333335F;
         this.level = this.want > this.level ? Math.min(this.want, this.level + step) : Math.max(this.want, this.level - step);
         this.volume = Math.max(0.01F, this.level * net.dabicco.witherstormmod.client.StormMusic.gain());
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
