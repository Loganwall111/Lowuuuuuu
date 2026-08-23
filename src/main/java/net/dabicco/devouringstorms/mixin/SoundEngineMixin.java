package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SoundEngine.class})
public abstract class SoundEngineMixin {
   @Inject(
      method = {"calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$scaleModSounds(SoundInstance instance, CallbackInfoReturnable<Float> cir) {
      Identifier id = instance.getIdentifier();
      if (id != null && "devouringstorms".equals(id.getNamespace())) {
         float mul = DevouringStormsClientConfig.soundMultiplier(id.getPath());
         if (mul != 1.0F) {
            cir.setReturnValue(Mth.clamp(cir.getReturnValueF() * mul, 0.0F, 1.0F));
         }
      }
   }
}
