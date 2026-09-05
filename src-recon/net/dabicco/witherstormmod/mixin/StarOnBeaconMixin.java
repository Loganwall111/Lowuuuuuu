package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.ModItems;
import net.dabicco.witherstormmod.beacon.WitherTheBeacon;
import net.dabicco.witherstormmod.beacon.WitheredBeacon;
import net.dabicco.witherstormmod.beacon.WitheredBeacons;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntity.class})
public abstract class StarOnBeaconMixin {
   @Shadow
   public abstract ItemStack getItem();

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void dabyws$offerToBeacon(CallbackInfo ci) {
      ItemEntity self = (ItemEntity)(Object)this;
      if (!self.level().isClientSide() && !self.isRemoved() && self.getItem().is(ModItems.WITHERED_NETHER_STAR) && self.onGround()) {
         BlockPos below = BlockPos.containing(self.getX(), self.getY() - 0.2, self.getZ());
         if (self.level().getBlockEntity(below) instanceof BeaconBlockEntity beacon
            && !((WitheredBeacon)beacon).dabyws$isWithered()
            && !WitheredBeacons.cooling(self.level(), below)) {
            WitherTheBeacon.wither(self.level(), beacon, below);
            self.getItem().shrink(1);
            if (self.getItem().isEmpty()) {
               self.discard();
            }
         }
      }
   }
}
