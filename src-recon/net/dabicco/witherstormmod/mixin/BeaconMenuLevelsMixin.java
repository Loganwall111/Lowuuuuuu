package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.beacon.WitheredBeacon;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconPowerButton"}
)
public abstract class BeaconMenuLevelsMixin {
   @Shadow(
      remap = false
   )
   private boolean isPrimary;
   @Unique
   private static final int DABYWS_NEAR = 10;

   @Inject(
      method = {"createEffectDescription"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$bump(Holder<MobEffect> effect, CallbackInfoReturnable<MutableComponent> cir) {
      if (dabyws$nearWithered()) {
         int potency = !this.isPrimary && !effect.is(MobEffects.REGENERATION) ? 2 : 1;
         cir.setReturnValue(
            Component.translatable(((MobEffect)effect.value()).getDescriptionId()).append(" ").append(Component.translatable("potion.potency." + potency))
         );
      }
   }

   @Unique
   private static boolean dabyws$nearWithered() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos at = mc.player.blockPosition();

         for (BlockPos p : BlockPos.betweenClosed(at.offset(-10, -10, -10), at.offset(10, 10, 10))) {
            if (mc.level.getBlockEntity(p) instanceof BeaconBlockEntity beacon
               && (((WitheredBeacon)beacon).dabyws$isWithered() || ((WitheredBeacon)beacon).dabyws$isAffected())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
