package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.WitherStormHeadEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class BeamGroundLoopSound extends AbstractTickableSoundInstance {
   public static final double AUDIBLE_RANGE = 42.0;
   private final WitherStormHeadEntity head;
   private int silentTicks = 0;

   public BeamGroundLoopSound(WitherStormHeadEntity head) {
      super(ModSounds.TRACTOR_BEAM_GROUND_LOOP, SoundSource.HOSTILE, RandomSource.create());
      this.head = head;
      this.looping = true;
      this.delay = 0;
      this.volume = 0.0F;
      Vec3 end = head.getBeamEndExact();
      this.x = end.x;
      this.y = end.y;
      this.z = end.z;
   }

   public WitherStormHeadEntity head() {
      return this.head;
   }

   public void tick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && this.head != null && this.head.isAlive() && !this.head.isRemoved() && this.head.isBeamActive()) {
         Vec3 end = this.head.getBeamEndExact();
         this.x = end.x;
         this.y = end.y;
         this.z = end.z;
         double dist = mc.player.position().distanceTo(end);
         float target = 0.0F;
         if (dist < 42.0) {
            float t = 1.0F - (float)Mth.clamp(dist / 42.0, 0.0, 1.0);
            target = 0.9F * t * t * (float)Math.max(0.0, DabyWSClientConfig.beamSoundsVolume);
         }

         if (target <= 0.001F) {
            if (++this.silentTicks > 60) {
               this.stop();
               return;
            }
         } else {
            this.silentTicks = 0;
         }

         this.volume = this.volume + (target - this.volume) * 0.15F;
      } else {
         this.stop();
      }
   }
}
