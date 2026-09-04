package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class CommandBlockPowerSound extends AbstractTickableSoundInstance {
   private static final int HOLD_TICKS = 22;
   private static final int FADE_TICKS = 34;
   private int age;

   public CommandBlockPowerSound(double x, double y, double z) {
      super(ModSounds.CB_POWER, SoundSource.HOSTILE, RandomSource.create());
      this.x = x;
      this.y = y;
      this.z = z;
      this.volume = 1.0F;
      this.looping = false;
      this.delay = 0;
      this.attenuation = Attenuation.LINEAR;
      this.relative = false;
   }

   public void tick() {
      if (Minecraft.getInstance().level == null) {
         this.stop();
      } else {
         ++this.age;
         if (this.age > 22) {
            float gone = (float)(this.age - 22) / 34.0F;
            if (gone >= 1.0F) {
               this.volume = 0.0F;
               this.stop();
            } else {
               float left = 1.0F - gone;
               this.volume = left * left;
            }
         }
      }
   }
}
