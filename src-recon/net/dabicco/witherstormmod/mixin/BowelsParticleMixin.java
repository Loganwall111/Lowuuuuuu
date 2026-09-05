package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.BowelsGravity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Particle.class})
public class BowelsParticleMixin {
   @Shadow
   @Final
   protected ClientLevel level;
   @Shadow
   protected float gravity;

   @Inject(
      method = {"tick()V"},
      at = {@At("HEAD")}
   )
   private void dabyws$noFallingDust(CallbackInfo ci) {
      if (this.gravity != 0.0F && BowelsGravity.isBowels(this.level)) {
         this.gravity = 0.0F;
      }
   }
}
