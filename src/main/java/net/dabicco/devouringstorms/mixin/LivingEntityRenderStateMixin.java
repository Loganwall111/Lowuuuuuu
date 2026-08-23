package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.InfectionRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({LivingEntityRenderState.class})
public class LivingEntityRenderStateMixin implements InfectionRenderState {
   @Unique
   private float dabyws$infection;
   @Unique
   private boolean dabyws$withered;

   @Override
   public float dabyws$getInfection() {
      return this.dabyws$infection;
   }

   @Override
   public void dabyws$setInfection(float infection) {
      this.dabyws$infection = infection;
   }

   @Override
   public boolean dabyws$isWithered() {
      return this.dabyws$withered;
   }

   @Override
   public void dabyws$setWithered(boolean withered) {
      this.dabyws$withered = withered;
   }
}
