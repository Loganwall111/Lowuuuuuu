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
   private static net.dabicco.witherstormmod.client.StormMusic.Cue cue = net.dabicco.witherstormmod.client.StormMusic.Cue.SILENT;
   private static net.dabicco.witherstormmod.client.StormMusic.Track current;
   private static float duck = 1.0F;
   private static net.dabicco.witherstormmod.client.StormMusic.Track fading;
   private static final int FADE_TICKS = 45;
   private static final int CUT_TICKS = 1;
   private static final int RISE_CUT_TICKS = 12;
   private static final int DOWN_TICKS = 1000;
   private static boolean risen = false;
   private static final RandomSource RANDOM = RandomSource.create();
   private static double lastPhase = -1.0;
   private static boolean lastCollapsed = false;
   private static boolean closedIn = false;
   private static final int START_GRACE = 60;
   private static SoundEvent lastEarly = ModSounds.MUSIC_MINI_A;
   private static boolean insideSpawnTower = false;
   private static final double DISTANT_MIN = 170.0;
   private static final double DISTANT_LOOK = 0.55;
   private static final int DISTANT_GAP_TICKS = 1500;
   private static final float DISTANT_CHANCE = 0.5F;
   private static int distantCooldown = 400;
   private static double lastDistance = -1.0;
   private static final double APPROACH_SPEED = 3.0;

   private StormMusic() {
   }

   public static void setDuck(float value) {
      duck = Mth.clamp(value, 0.0F, 1.0F);
   }

   public static void tick(Minecraft mc) {
      if (mc.level == null || mc.player == null) {
         stopAll(mc);
      } else if (!DabyWSClientConfig.stormMusic) {
         stopAll(mc);
      } else if (BowelsGravity.isBowels(mc.level)) {
         setCue(mc, net.dabicco.witherstormmod.client.StormMusic.Cue.BOWELS);
         pump(mc);
      } else if (bombNearby(mc)) {
         setCue(mc, net.dabicco.witherstormmod.client.StormMusic.Cue.BOMB);
         pump(mc);
      } else {
         net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData storm = nearest(mc);
         boolean stormHere = storm != null && audible(mc, storm);
         if (insideSpawnTower && !stormHere) {
            setCue(mc, net.dabicco.witherstormmod.client.StormMusic.Cue.IVOR);
            pump(mc);
         } else if (!stormHere) {
            setCue(mc, net.dabicco.witherstormmod.client.StormMusic.Cue.SILENT);
            pump(mc);
         } else {
            if (distantCooldown > 0) {
               distantCooldown--;
            }

            if (cue == net.dabicco.witherstormmod.client.StormMusic.Cue.DISTANT) {
               pump(mc);
            } else {
               if (distantCooldown <= 0 && watchingFromAfar(mc, storm)) {
                  distantCooldown = 1500;
                  if (RANDOM.nextFloat() < 0.5F) {
                     setCue(mc, net.dabicco.witherstormmod.client.StormMusic.Cue.DISTANT);
                     pump(mc);
                     return;
                  }
               }

               setCue(mc, decide(mc, storm));
               lastPhase = storm.phase;
               lastCollapsed = storm.collapsed;
               pump(mc);
            }
         }
      }
   }

   private static net.dabicco.witherstormmod.client.StormMusic.Cue decide(
      Minecraft mc, net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData storm
   ) {
      double phase = storm.phase;
      if (storm.collapsed) {
         risen = false;
         return storm.collapseTicks < 1000 ? net.dabicco.witherstormmod.client.StormMusic.Cue.DOWN : net.dabicco.witherstormmod.client.StormMusic.Cue.WAKING;
      } else {
         if (cue == net.dabicco.witherstormmod.client.StormMusic.Cue.WAKING || cue == net.dabicco.witherstormmod.client.StormMusic.Cue.RISEN || risen) {
            if (storm.siegeStage < 1 || storm.siegeStage > 3) {
               risen = true;
               return net.dabicco.witherstormmod.client.StormMusic.Cue.RISEN;
            }

            risen = false;
         }

         if (phase >= 6.0) {
            return switch (storm.siegeStage) {
               case 1 -> net.dabicco.witherstormmod.client.StormMusic.Cue.REBIRTH;
               case 2 -> net.dabicco.witherstormmod.client.StormMusic.Cue.REBIRTH_TAIL;
               case 3 -> net.dabicco.witherstormmod.client.StormMusic.Cue.SIEGE;
               default -> net.dabicco.witherstormmod.client.StormMusic.Cue.DEVOURER;
            };
         } else if (phase >= 5.4) {
            if (cue == net.dabicco.witherstormmod.client.StormMusic.Cue.CLOSING_NEAR || closedIn) {
               return net.dabicco.witherstormmod.client.StormMusic.Cue.CLOSING_NEAR;
            } else {
               return cue == net.dabicco.witherstormmod.client.StormMusic.Cue.CLOSING && approaching(mc, storm)
                  ? net.dabicco.witherstormmod.client.StormMusic.Cue.CLOSING_NEAR
                  : net.dabicco.witherstormmod.client.StormMusic.Cue.CLOSING;
            }
         } else if (!(phase >= 4.0)) {
            return net.dabicco.witherstormmod.client.StormMusic.Cue.EARLY;
         } else if (cue == net.dabicco.witherstormmod.client.StormMusic.Cue.ARRIVAL || cue == net.dabicco.witherstormmod.client.StormMusic.Cue.ARRIVAL_TAIL) {
            return cue;
         } else {
            return lastPhase < 4.0 ? net.dabicco.witherstormmod.client.StormMusic.Cue.ARRIVAL : net.dabicco.witherstormmod.client.StormMusic.Cue.HUNT;
         }
      }
   }

   private static void setCue(Minecraft mc, net.dabicco.witherstormmod.client.StormMusic.Cue want) {
      if (want != cue) {
         cue = want;
         closedIn = want == net.dabicco.witherstormmod.client.StormMusic.Cue.CLOSING_NEAR;
         switch (want) {
            case SILENT:
               crossTo(mc, (SoundEvent)null, false, 45);
               break;
            case EARLY:
               crossTo(mc, ModSounds.MUSIC_MINI_A, false, 45);
               break;
            case ARRIVAL:
               crossTo(mc, ModSounds.MUSIC_MAIN_A, false, 45);
               break;
            case ARRIVAL_TAIL:
               crossTo(mc, ModSounds.MUSIC_MAIN_B, false, 45);
               break;
            case HUNT:
               crossTo(mc, huntPick(), false, 45);
               break;
            case CLOSING:
               crossTo(mc, ModSounds.MUSIC_MAIN_B, false, 45);
               break;
            case CLOSING_NEAR:
               crossTo(mc, ModSounds.MUSIC_MAIN_C, false, 45);
               break;
            case BOMB:
               crossTo(mc, ModSounds.MUSIC_MINI_C, true, 45);
               break;
            case DOWN:
               crossTo(mc, (SoundEvent)null, false, 45);
               break;
            case WAKING:
               crossTo(mc, ModSounds.MUSIC_MINI_A, false, 45);
               break;
            case RISEN:
               crossTo(mc, ModSounds.MUSIC_MINI_B, true, 12);
               break;
            case REBIRTH:
               crossTo(mc, ModSounds.MUSIC_MAIN_A, false, 45);
               break;
            case REBIRTH_TAIL:
               crossTo(mc, ModSounds.MUSIC_AFTR_C, false, 45);
               break;
            case SIEGE:
               crossTo(mc, ModSounds.MUSIC_AFTR_B, true, 45);
               break;
            case DEVOURER:
               crossTo(mc, devourerPick(), false, 45);
               break;
            case BOWELS:
               crossTo(mc, ModSounds.MUSIC_BOWELS, true, 45);
               break;
            case IVOR:
               crossTo(mc, ModSounds.MUSIC_IVOR, true, 45);
               break;
            case DISTANT:
               crossTo(mc, distantPick(), false, 45);
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
               current.agedTicks++;
            } else {
               current = null;
               onTrackFinished(mc);
            }
         }
      }
   }

   private static void onTrackFinished(Minecraft mc) {
      switch (cue) {
         case SILENT:
         case DOWN:
         case REBIRTH:
         case REBIRTH_TAIL:
         default:
            break;
         case EARLY:
            SoundEvent next = current == null && lastEarly == ModSounds.MUSIC_MINI_A
               ? ModSounds.MUSIC_MINI_B
               : (lastEarly == ModSounds.MUSIC_MINI_B ? ModSounds.MUSIC_MINI_C : ModSounds.MUSIC_MINI_B);
            crossTo(mc, next, false, 1);
            break;
         case ARRIVAL:
            setCue(mc, net.dabicco.witherstormmod.client.StormMusic.Cue.ARRIVAL_TAIL);
            break;
         case ARRIVAL_TAIL:
            setCue(mc, net.dabicco.witherstormmod.client.StormMusic.Cue.HUNT);
            break;
         case HUNT:
            crossTo(mc, huntPick(), false, 45);
            break;
         case CLOSING:
            setCue(mc, net.dabicco.witherstormmod.client.StormMusic.Cue.CLOSING_NEAR);
            break;
         case CLOSING_NEAR:
            crossTo(mc, ModSounds.MUSIC_MAIN_C, false, 45);
            break;
         case BOMB:
            crossTo(mc, ModSounds.MUSIC_MINI_C, true, 45);
            break;
         case WAKING:
            crossTo(mc, ModSounds.MUSIC_MINI_A, false, 45);
            break;
         case RISEN:
            crossTo(mc, ModSounds.MUSIC_MINI_B, true, 12);
            break;
         case SIEGE:
            crossTo(mc, ModSounds.MUSIC_AFTR_B, true, 45);
            break;
         case DEVOURER:
            crossTo(mc, devourerPick(), false, 45);
            break;
         case BOWELS:
            crossTo(mc, ModSounds.MUSIC_BOWELS, true, 45);
            break;
         case IVOR:
            crossTo(mc, ModSounds.MUSIC_IVOR, true, 45);
            break;
         case DISTANT:
            cue = net.dabicco.witherstormmod.client.StormMusic.Cue.SILENT;
            distantCooldown = 1500;
      }
   }

   private static SoundEvent huntPick() {
      return RANDOM.nextBoolean() ? ModSounds.MUSIC_MAIN_A : ModSounds.MUSIC_MINI_C;
   }

   private static SoundEvent devourerPick() {
      return switch (RANDOM.nextInt(3)) {
         case 0 -> ModSounds.MUSIC_AFTR_A;
         case 1 -> ModSounds.MUSIC_AFTR_B;
         default -> ModSounds.MUSIC_AFTR_C;
      };
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
         net.dabicco.witherstormmod.client.StormMusic.Track track = new net.dabicco.witherstormmod.client.StormMusic.Track(event, loop);
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

      cue = net.dabicco.witherstormmod.client.StormMusic.Cue.SILENT;
   }

   public static void setInsideSpawnTower(boolean inside) {
      insideSpawnTower = inside;
   }

   private static SoundEvent distantPick() {
      return RANDOM.nextBoolean() ? ModSounds.MUSIC_DISTANT_A : ModSounds.MUSIC_DISTANT_B;
   }

   private static boolean watchingFromAfar(Minecraft mc, net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData s) {
      Vec3 to = new Vec3(s.x - mc.player.getX(), 0.0, s.z - mc.player.getZ());
      if (to.length() < 170.0) {
         return false;
      } else if (!mc.level.canSeeSky(mc.player.blockPosition())) {
         return false;
      } else {
         Vec3 look = mc.player.getLookAngle();
         return new Vec3(look.x, 0.0, look.z).normalize().dot(to.normalize()) >= 0.55;
      }
   }

   public static boolean isPlaying() {
      return current != null;
   }

   private static boolean approaching(Minecraft mc, net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData storm) {
      Vec3 to = new Vec3(storm.x - mc.player.getX(), 0.0, storm.z - mc.player.getZ());
      if (to.lengthSqr() < 1.0) {
         return true;
      } else {
         Vec3 move = mc.player.getDeltaMovement();
         double closing = new Vec3(move.x, 0.0, move.z).dot(to.normalize()) * 20.0;
         return closing >= 3.0;
      }
   }

   private static net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData nearest(Minecraft mc) {
      net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData best = null;
      double bestDist = Double.MAX_VALUE;
      Vec3 at = mc.player.position();

      for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
         double dist = at.distanceToSqr(new Vec3(d.x, d.y, d.z));
         if (dist < bestDist) {
            bestDist = dist;
            best = d;
         }
      }

      return best;
   }

   private static boolean audible(Minecraft mc, net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData storm) {
      double range = DabyWSClientConfig.stormMusicRange;
      if (mc.player.position().distanceTo(new Vec3(storm.x, storm.y, storm.z)) > range) {
         return false;
      } else {
         int cutoff = (int)DabyWSClientConfig.stormMusicCaveCutoff;
         return cutoff <= 0 ? true : mc.level.getBrightness(LightLayer.SKY, mc.player.blockPosition()) >= cutoff;
      }
   }

   static float gain() {
      return (float)Mth.clamp(DabyWSClientConfig.stormMusicVolume, 0.0, 2.0);
   }

   private static boolean bombNearby(Minecraft mc) {
      double range = DabyWSClientConfig.stormMusicRange;

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof FormidibombEntity && entity.position().closerThan(mc.player.position(), range)) {
            return true;
         }
      }

      return false;
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

      private static net.dabicco.witherstormmod.client.StormMusic.Cue[] $values() {
         return new net.dabicco.witherstormmod.client.StormMusic.Cue[]{
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
            DISTANT
         };
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
         super(event, SoundSource.AMBIENT, net.dabicco.witherstormmod.client.StormMusic.RANDOM);
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
         this.agedTicks++;
         this.everStarted = true;
         float step = 1.0F / this.fadeTicks;
         this.level = this.target > this.level ? Math.min(this.target, this.level + step) : Math.max(this.target, this.level - step);
         this.volume = Math.max(0.01F, this.level * net.dabicco.witherstormmod.client.StormMusic.gain() * net.dabicco.witherstormmod.client.StormMusic.duck);
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
