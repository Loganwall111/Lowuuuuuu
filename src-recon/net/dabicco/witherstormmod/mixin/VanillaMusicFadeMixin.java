package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormMusic;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MusicManager.class})
public abstract class VanillaMusicFadeMixin {
   @Unique
   private static final float DABYWS_FADE = 0.0125F;

   @Shadow
   private boolean fadePlaying(float amount) {
      throw new AssertionError();
   }

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$yieldToStormMusic(CallbackInfo ci) {
      if (StormMusic.isPlaying()) {
         this.fadePlaying(0.0125F);
         ci.cancel();
      }
   }
}
