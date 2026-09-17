package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class StormAmbienceSound extends AbstractTickableSoundInstance {
   private static final double HEAR_RANGE = 1000.0;
   private int silentTicks = 0;
   private static final float AMBIENCE_FROM = 1.5F;
   private static final float AMBIENCE_HALF_AT = 3.8F;

   public static boolean anyStormInHearRange(Minecraft mc) {
      if (mc.player == null) {
         return false;
      } else {
         Vec3 player = mc.player.position();

         for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
            if (!d.collapsed && player.distanceTo(new Vec3(d.x, d.y, d.z)) < 950.0) {
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

      for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
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
      if (mc.level != null && mc.player != null && DabyWSClientConfig.stormAmbience) {
         Vec3 player = mc.player.position();
         double best = Double.MAX_VALUE;
         float bestPhase = 0.0F;

         for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
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

         if (best > 1000.0) {
            this.silentTicks++;
            if (this.silentTicks > 100) {
               this.stop();
               return;
            }
         } else {
            this.silentTicks = 0;
         }

         float closeness = (float)Mth.clamp(1.0 - best / 1000.0, 0.0, 1.0);
         float targetVolume = closeness + (best < 1000.0 ? 0.02F : 0.0F);
         targetVolume *= phaseLevel(bestPhase);
         targetVolume *= (float)DabyWSClientConfig.ambienceVolume;
         this.volume = this.volume + (targetVolume - this.volume) * 0.05F;
         this.pitch = 1.0F - (1.0F - closeness) * 0.25F;
      } else {
         this.stop();
      }
   }
}
