package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.ClientSicknessManager;
import net.dabicco.witherstormmod.client.InfectionRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void dabyws$extractInfection(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
      ((InfectionRenderState)state).dabyws$setInfection(ClientSicknessManager.getInfection(entity.getId()));
      ((InfectionRenderState)state).dabyws$setWithered(ClientSicknessManager.isWithered(entity.getId()));
   }

   @Inject(
      method = {"getModelTint(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)I"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$witheredTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
      if (((InfectionRenderState)state).dabyws$isWithered()) {
         int argb = (Integer)cir.getReturnValue();
         int a = argb >>> 24 & 0xFF;
         int r = (int)((float)(argb >> 16 & 0xFF) * 0.2F);
         int g = (int)((float)(argb >> 8 & 0xFF) * 0.14F);
         int b = (int)((float)(argb & 0xFF) * 0.3F);
         cir.setReturnValue(a << 24 | r << 16 | g << 8 | b);
      }
   }
}
