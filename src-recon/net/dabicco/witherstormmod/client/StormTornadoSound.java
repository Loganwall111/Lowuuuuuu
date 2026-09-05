package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class StormTornadoSound extends AbstractTickableSoundInstance {
   public static final double RANGE = 260.0;
   private final int stormId;
   private int silentTicks = 0;

   public StormTornadoSound(int stormId, double x, double y, double z) {
      super(ModSounds.STORM_TORNADO_LOOP, SoundSource.HOSTILE, RandomSource.create());
      this.stormId = stormId;
      this.looping = true;
      this.delay = 0;
      this.volume = 0.0F;
      this.x = x;
      this.y = y;
      this.z = z;
      this.attenuation = Attenuation.NONE;
      this.relative = false;
   }

   public int stormId() {
      return this.stormId;
   }

   public void tick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData found = null;

         for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
            if (d.entityId == this.stormId) {
               found = d;
               break;
            }
         }

         if (found != null && !found.collapsed && !(found.phase < 4.0F) && !(found.phase >= 5.8F)) {
            this.x = found.dispX;
            this.y = found.dispY;
            this.z = found.dispZ;
            double dist = Math.sqrt(
               (found.dispX - mc.player.getX()) * (found.dispX - mc.player.getX()) + (found.dispZ - mc.player.getZ()) * (found.dispZ - mc.player.getZ())
            );
            float target = 0.0F;
            if (dist < 260.0) {
               float t = 1.0F - (float)Mth.clamp(dist / 260.0, 0.0, 1.0);
               target = 1.0F * t * t * (float)Math.max(0.0, DabyWSClientConfig.ambienceVolume);
            }

            if (target <= 0.001F) {
               if (++this.silentTicks > 80) {
                  this.stop();
                  return;
               }
            } else {
               this.silentTicks = 0;
            }

            this.volume = this.volume + (target - this.volume) * 0.08F;
         } else {
            this.stop();
         }
      } else {
         this.stop();
      }
   }
}
