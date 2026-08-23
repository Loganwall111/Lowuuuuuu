package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.ModSounds;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class StormAmbienceSound extends AbstractTickableSoundInstance {
   private static final double HEAR_RANGE = (double)1000.0F;
   private int silentTicks = 0;
   private static final float AMBIENCE_FROM = 1.5F;
   private static final float AMBIENCE_HALF_AT = 3.8F;

   public static boolean anyStormInHearRange(Minecraft mc) {
      if (mc.player == null) {
         return false;
      } else {
         Vec3 player = mc.player.position();

         for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
            if (!d.collapsed && player.distanceTo(new Vec3(d.x, d.y, d.z)) < (double)950.0F) {
               return true;
            }
         }

         return false;
      }
   }

   private static float phaseLevel(float phase) {
      if (phase >= 4.0F) {
         return 1.0F;
      } else if (phase <= 1.5F) {
         return 0.0F;
      } else {
         float t = (phase - 1.5F) / 2.3F;
         return Mth.clamp(t, 0.0F, 1.0F) * 0.5F;
      }
   }

   public StormAmbienceSound() {
      super(ModSounds.AMBIENCE_LOOP, SoundSource.AMBIENT, RandomSource.create());
      this.looping = true;
      this.delay = 0;
      this.volume = 0.05F;
      this.attenuation = Attenuation.NONE;
      this.relative = false;
      this.placeAtNearestStorm();
   }

   private void placeAtNearestStorm() {
      Minecraft mc = Minecraft.getInstance();
      Vec3 from = mc.player != null ? mc.player.position() : Vec3.ZERO;
      double best = Double.MAX_VALUE;

      for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         Vec3 pos = new Vec3(d.x, d.y, d.z);
         double dist = from.distanceTo(pos);
         if (dist < best) {
            best = dist;
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
         }
      }

   }

   public void tick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && DevouringStormsClientConfig.stormAmbience) {
         Vec3 player = mc.player.position();
         double best = Double.MAX_VALUE;
         float bestPhase = 0.0F;

         for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
            if (!d.collapsed) {
               Vec3 pos = new Vec3(d.x, d.y, d.z);
               double dist = player.distanceTo(pos);
               if (dist < best) {
                  best = dist;
                  bestPhase = d.phase;
                  this.x = pos.x;
                  this.y = pos.y;
                  this.z = pos.z;
               }
            }
         }

         if (best > (double)1000.0F) {
            ++this.silentTicks;
            if (this.silentTicks > 100) {
               this.stop();
               return;
            }
         } else {
            this.silentTicks = 0;
         }

         float closeness = (float)Mth.clamp((double)1.0F - best / (double)1000.0F, (double)0.0F, (double)1.0F);
         float targetVolume = closeness + (best < (double)1000.0F ? 0.02F : 0.0F);
         targetVolume *= phaseLevel(bestPhase);
         targetVolume *= (float)DevouringStormsClientConfig.ambienceVolume;
         this.volume += (targetVolume - this.volume) * 0.05F;
         this.pitch = 1.0F - (1.0F - closeness) * 0.25F;
      } else {
         this.stop();
      }
   }
}
