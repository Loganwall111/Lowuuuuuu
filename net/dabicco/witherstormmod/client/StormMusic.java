package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.BowelsGravity;
import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.FormidibombEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public final class StormMusic {
   private static Cue cue;
   private static Track current;
   private static float duck;
   private static Track fading;
   private static final int FADE_TICKS = 45;
   private static final int CUT_TICKS = 1;
   private static final int RISE_CUT_TICKS = 12;
   private static final int DOWN_TICKS = 1000;
   private static boolean risen;
   private static final RandomSource RANDOM;
   private static double lastPhase;
   private static boolean lastCollapsed;
   private static boolean closedIn;
   private static final int START_GRACE = 60;
   private static SoundEvent lastEarly;
   private static boolean insideSpawnTower;
   private static final double DISTANT_MIN = (double)170.0F;
   private static final double DISTANT_LOOK = 0.55;
   private static final int DISTANT_GAP_TICKS = 1500;
   private static final float DISTANT_CHANCE = 0.5F;
   private static int distantCooldown;
   private static double lastDistance;
   private static final double APPROACH_SPEED = (double)3.0F;

   private StormMusic() {
   }

   public static void setDuck(float value) {
      duck = Mth.clamp(value, 0.0F, 1.0F);
   }

   public static void tick(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         if (!DabyWSClientConfig.stormMusic) {
            stopAll(mc);
         } else if (BowelsGravity.isBowels(mc.level)) {
            setCue(mc, StormMusic.Cue.BOWELS);
            pump(mc);
         } else if (bombNearby(mc)) {
            setCue(mc, StormMusic.Cue.BOMB);
            pump(mc);
         } else {
            ClientDistantStormManager.StormData storm = nearest(mc);
            boolean stormHere = storm != null && audible(mc, storm);
            if (insideSpawnTower && !stormHere) {
               setCue(mc, StormMusic.Cue.IVOR);
               pump(mc);
            } else if (!stormHere) {
               setCue(mc, StormMusic.Cue.SILENT);
               pump(mc);
            } else {
               if (distantCooldown > 0) {
                  --distantCooldown;
               }

               if (cue == StormMusic.Cue.DISTANT) {
                  pump(mc);
               } else {
                  if (distantCooldown <= 0 && watchingFromAfar(mc, storm)) {
                     distantCooldown = 1500;
                     if (RANDOM.nextFloat() < 0.5F) {
                        setCue(mc, StormMusic.Cue.DISTANT);
                        pump(mc);
                        return;
                     }
                  }

                  setCue(mc, decide(mc, storm));
                  lastPhase = (double)storm.phase;
                  lastCollapsed = storm.collapsed;
                  pump(mc);
               }
            }
         }
      } else {
         stopAll(mc);
      }
   }

   private static Cue decide(Minecraft mc, ClientDistantStormManager.StormData storm) {
      double phase = (double)storm.phase;
      if (storm.collapsed) {
         risen = false;
         return storm.collapseTicks < 1000 ? StormMusic.Cue.DOWN : StormMusic.Cue.WAKING;
      } else {
         if (cue == StormMusic.Cue.WAKING || cue == StormMusic.Cue.RISEN || risen) {
            if (storm.siegeStage < 1 || storm.siegeStage > 3) {
               risen = true;
               return StormMusic.Cue.RISEN;
            }

            risen = false;
         }

         if (phase >= (double)6.0F) {
            Cue var10000;
            switch (storm.siegeStage) {
               case 1 -> var10000 = StormMusic.Cue.REBIRTH;
               case 2 -> var10000 = StormMusic.Cue.REBIRTH_TAIL;
               case 3 -> var10000 = StormMusic.Cue.SIEGE;
               default -> var10000 = StormMusic.Cue.DEVOURER;
            }

            return var10000;
         } else if (phase >= 5.4) {
            if (cue != StormMusic.Cue.CLOSING_NEAR && !closedIn) {
               if (cue == StormMusic.Cue.CLOSING && approaching(mc, storm)) {
                  return StormMusic.Cue.CLOSING_NEAR;
               } else {
                  return StormMusic.Cue.CLOSING;
               }
            } else {
               return StormMusic.Cue.CLOSING_NEAR;
            }
         } else if (phase >= (double)4.0F) {
            if (cue != StormMusic.Cue.ARRIVAL && cue != StormMusic.Cue.ARRIVAL_TAIL) {
               if (lastPhase < (double)4.0F) {
                  return StormMusic.Cue.ARRIVAL;
               } else {
                  return StormMusic.Cue.HUNT;
               }
            } else {
               return cue;
            }
         } else {
            return StormMusic.Cue.EARLY;
         }
      }
   }

   private static void setCue(Minecraft mc, Cue want) {
      if (want != cue) {
         cue = want;
         closedIn = want == StormMusic.Cue.CLOSING_NEAR;
         switch (want.ordinal()) {
            case 0 -> crossTo(mc, (SoundEvent)null, false, 45);
            case 1 -> crossTo(mc, ModSounds.MUSIC_MINI_A, false, 45);
            case 2 -> crossTo(mc, ModSounds.MUSIC_MAIN_A, false, 45);
            case 3 -> crossTo(mc, ModSounds.MUSIC_MAIN_B, false, 45);
            case 4 -> crossTo(mc, huntPick(), false, 45);
            case 5 -> crossTo(mc, ModSounds.MUSIC_MAIN_B, false, 45);
            case 6 -> crossTo(mc, ModSounds.MUSIC_MAIN_C, false, 45);
            case 7 -> crossTo(mc, ModSounds.MUSIC_MINI_C, true, 45);
            case 8 -> crossTo(mc, (SoundEvent)null, false, 45);
            case 9 -> crossTo(mc, ModSounds.MUSIC_MINI_A, false, 45);
            case 10 -> crossTo(mc, ModSounds.MUSIC_MINI_B, true, 12);
            case 11 -> crossTo(mc, ModSounds.MUSIC_MAIN_A, false, 45);
            case 12 -> crossTo(mc, ModSounds.MUSIC_AFTR_C, false, 45);
            case 13 -> crossTo(mc, ModSounds.MUSIC_AFTR_B, true, 45);
            case 14 -> crossTo(mc, devourerPick(), false, 45);
            case 15 -> crossTo(mc, ModSounds.MUSIC_BOWELS, true, 45);
            case 16 -> crossTo(mc, ModSounds.MUSIC_IVOR, true, 45);
            case 17 -> crossTo(mc, distantPick(), false, 45);
         }

      }
   }

   private static void pump(Minecraft mc) {
      if (fading != null && fading.isStopped()) {
         fading = null;
      }

      if (current == null) {
         onTrackFinished(mc);
      } else {
         boolean active = mc.getSoundManager().isActive(current);
         if (!active) {
            if (!current.everStarted && current.agedTicks < 60) {
               ++current.agedTicks;
            } else {
               current = null;
               onTrackFinished(mc);
            }
         }
      }
   }

   private static void onTrackFinished(Minecraft mc) {
      switch (cue.ordinal()) {
         case 0:
         case 8:
         case 11:
         case 12:
         default:
            break;
         case 1:
            SoundEvent next = current == null && lastEarly == ModSounds.MUSIC_MINI_A ? ModSounds.MUSIC_MINI_B : (lastEarly == ModSounds.MUSIC_MINI_B ? ModSounds.MUSIC_MINI_C : ModSounds.MUSIC_MINI_B);
            crossTo(mc, next, false, 1);
            break;
         case 2:
            setCue(mc, StormMusic.Cue.ARRIVAL_TAIL);
            break;
         case 3:
            setCue(mc, StormMusic.Cue.HUNT);
            break;
         case 4:
            crossTo(mc, huntPick(), false, 45);
            break;
         case 5:
            setCue(mc, StormMusic.Cue.CLOSING_NEAR);
            break;
         case 6:
            crossTo(mc, ModSounds.MUSIC_MAIN_C, false, 45);
            break;
         case 7:
            crossTo(mc, ModSounds.MUSIC_MINI_C, true, 45);
            break;
         case 9:
            crossTo(mc, ModSounds.MUSIC_MINI_A, false, 45);
            break;
         case 10:
            crossTo(mc, ModSounds.MUSIC_MINI_B, true, 12);
            break;
         case 13:
            crossTo(mc, ModSounds.MUSIC_AFTR_B, true, 45);
            break;
         case 14:
            crossTo(mc, devourerPick(), false, 45);
            break;
         case 15:
            crossTo(mc, ModSounds.MUSIC_BOWELS, true, 45);
            break;
         case 16:
            crossTo(mc, ModSounds.MUSIC_IVOR, true, 45);
            break;
         case 17:
            cue = StormMusic.Cue.SILENT;
            distantCooldown = 1500;
      }

   }

   private static SoundEvent huntPick() {
      return RANDOM.nextBoolean() ? ModSounds.MUSIC_MAIN_A : ModSounds.MUSIC_MINI_C;
   }

   private static SoundEvent devourerPick() {
      SoundEvent var10000;
      switch (RANDOM.nextInt(3)) {
         case 0 -> var10000 = ModSounds.MUSIC_AFTR_A;
         case 1 -> var10000 = ModSounds.MUSIC_AFTR_B;
         default -> var10000 = ModSounds.MUSIC_AFTR_C;
      }

      return var10000;
   }

   private static void crossTo(Minecraft mc, SoundEvent event, boolean loop, int ticks) {
      if (current != null) {
         if (fading != null) {
            mc.getSoundManager().stop(fading);
         }

         fading = current;
         fading.fadeTo(0.0F, ticks);
      }

      current = null;
      if (event != null) {
         lastEarly = event;
         Track track = new Track(event, loop);
         track.fadeTo(1.0F, ticks);
         mc.getSoundManager().play(track);
         current = track;
      }
   }

   private static void stopAll(Minecraft mc) {
      if (current != null) {
         mc.getSoundManager().stop(current);
         current = null;
      }

      if (fading != null) {
         mc.getSoundManager().stop(fading);
         fading = null;
      }

      cue = StormMusic.Cue.SILENT;
   }

   public static void setInsideSpawnTower(boolean inside) {
      insideSpawnTower = inside;
   }

   private static SoundEvent distantPick() {
      return RANDOM.nextBoolean() ? ModSounds.MUSIC_DISTANT_A : ModSounds.MUSIC_DISTANT_B;
   }

   private static boolean watchingFromAfar(Minecraft mc, ClientDistantStormManager.StormData s) {
      Vec3 to = new Vec3(s.x - mc.player.getX(), (double)0.0F, s.z - mc.player.getZ());
      if (to.length() < (double)170.0F) {
         return false;
      } else if (!mc.level.canSeeSky(mc.player.blockPosition())) {
         return false;
      } else {
         Vec3 look = mc.player.getLookAngle();
         return (new Vec3(look.x, (double)0.0F, look.z)).normalize().dot(to.normalize()) >= 0.55;
      }
   }

   public static boolean isPlaying() {
      return current != null;
   }

   private static boolean approaching(Minecraft mc, ClientDistantStormManager.StormData storm) {
      Vec3 to = new Vec3(storm.x - mc.player.getX(), (double)0.0F, storm.z - mc.player.getZ());
      if (to.lengthSqr() < (double)1.0F) {
         return true;
      } else {
         Vec3 move = mc.player.getDeltaMovement();
         double closing = (new Vec3(move.x, (double)0.0F, move.z)).dot(to.normalize()) * (double)20.0F;
         return closing >= (double)3.0F;
      }
   }

   private static ClientDistantStormManager.StormData nearest(Minecraft mc) {
      ClientDistantStormManager.StormData best = null;
      double bestDist = Double.MAX_VALUE;
      Vec3 at = mc.player.position();

      for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         double dist = at.distanceToSqr(new Vec3(d.x, d.y, d.z));
         if (dist < bestDist) {
            bestDist = dist;
            best = d;
         }
      }

      return best;
   }

   private static boolean audible(Minecraft mc, ClientDistantStormManager.StormData storm) {
      double range = DabyWSClientConfig.stormMusicRange;
      if (mc.player.position().distanceTo(new Vec3(storm.x, storm.y, storm.z)) > range) {
         return false;
      } else {
         int cutoff = (int)DabyWSClientConfig.stormMusicCaveCutoff;
         if (cutoff <= 0) {
            return true;
         } else {
            return mc.level.getBrightness(LightLayer.SKY, mc.player.blockPosition()) >= cutoff;
         }
      }
   }

   static float gain() {
      return (float)Mth.clamp(DabyWSClientConfig.stormMusicVolume, (double)0.0F, (double)2.0F);
   }

   private static boolean bombNearby(Minecraft mc) {
      double range = DabyWSClientConfig.stormMusicRange;

      for(Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof FormidibombEntity && entity.position().closerThan(mc.player.position(), range)) {
            return true;
         }
      }

      return false;
   }

   static {
      cue = StormMusic.Cue.SILENT;
      duck = 1.0F;
      risen = false;
      RANDOM = RandomSource.create();
      lastPhase = (double)-1.0F;
      lastCollapsed = false;
      closedIn = false;
      lastEarly = ModSounds.MUSIC_MINI_A;
      insideSpawnTower = false;
      distantCooldown = 400;
      lastDistance = (double)-1.0F;
   }

   private static enum Cue {
      SILENT,
      EARLY,
      ARRIVAL,
      ARRIVAL_TAIL,
      HUNT,
      CLOSING,
      CLOSING_NEAR,
      BOMB,
      DOWN,
      WAKING,
      RISEN,
      REBIRTH,
      REBIRTH_TAIL,
      SIEGE,
      DEVOURER,
      BOWELS,
      IVOR,
      DISTANT;

      // $FF: synthetic method
      private static Cue[] $values() {
         return new Cue[]{SILENT, EARLY, ARRIVAL, ARRIVAL_TAIL, HUNT, CLOSING, CLOSING_NEAR, BOMB, DOWN, WAKING, RISEN, REBIRTH, REBIRTH_TAIL, SIEGE, DEVOURER, BOWELS, IVOR, DISTANT};
      }
   }

   private static final class Track extends AbstractTickableSoundInstance {
      private float target = 1.0F;
      private float level = 0.0F;
      private int fadeTicks = 45;
      private boolean done;
      private int agedTicks;
      private boolean everStarted;

      Track(SoundEvent event, boolean loop) {
         super(event, SoundSource.AMBIENT, StormMusic.RANDOM);
         this.looping = loop;
         this.delay = 0;
         this.attenuation = Attenuation.NONE;
         this.relative = true;
         this.volume = 0.01F;
      }

      void fadeTo(float value, int ticks) {
         this.target = value;
         this.fadeTicks = Math.max(1, ticks);
      }

      public void tick() {
         ++this.agedTicks;
         this.everStarted = true;
         float step = 1.0F / (float)this.fadeTicks;
         this.level = this.target > this.level ? Math.min(this.target, this.level + step) : Math.max(this.target, this.level - step);
         this.volume = Math.max(0.01F, this.level * StormMusic.gain() * StormMusic.duck);
         if (this.level <= 0.0F && this.target <= 0.0F) {
            this.done = true;
            this.stop();
         }

      }

      public boolean isStopped() {
         return this.done || super.isStopped();
      }
   }
}
