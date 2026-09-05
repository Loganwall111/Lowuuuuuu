package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.InfectionRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({LivingEntityRenderState.class})
public class LivingEntityRenderStateMixin implements InfectionRenderState {
   @Unique
   private float dabyws$infection;
   @Unique
   private boolean dabyws$withered;

   public float dabyws$getInfection() {
      return this.dabyws$infection;
   }

   public void dabyws$setInfection(float infection) {
      this.dabyws$infection = infection;
   }

   public boolean dabyws$isWithered() {
      return this.dabyws$withered;
   }

   public void dabyws$setWithered(boolean withered) {
      this.dabyws$withered = withered;
   }
}
