package net.dabicco.devouringstorms.mixin;

import java.util.List;
import net.dabicco.devouringstorms.beacon.WitheredBeacon;
import net.dabicco.devouringstorms.beacon.WitheredBeacons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BeaconBlockEntity.class})
public abstract class BeaconWitherMixin implements WitheredBeacon {
   @Shadow
   private int levels;
   @Shadow
   private Holder<MobEffect> primaryPower;
   @Shadow
   private Holder<MobEffect> secondaryPower;
   @Unique
   private boolean dabyws$withered;
   @Unique
   private boolean dabyws$affected;

   @Override
   public boolean dabyws$isWithered() {
      return this.dabyws$withered;
   }

   @Override
   public boolean dabyws$isAffected() {
      return this.dabyws$affected;
   }

   @Override
   public void dabyws$setAffected(boolean value) {
      this.dabyws$affected = value;
   }

   @Override
   public void dabyws$setWithered(boolean value) {
      this.dabyws$withered = value;
      BeaconBlockEntity self = (BeaconBlockEntity)(Object)this;
      Level level = self.getLevel();
      if (level != null) {
         if (value) {
            WitheredBeacons.add(level, self.getBlockPos());
         } else {
            WitheredBeacons.remove(level, self.getBlockPos());
         }

         self.setChanged();
         level.sendBlockUpdated(self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
      }
   }

   @Inject(
      method = {"saveAdditional"},
      at = {@At("TAIL")}
   )
   private void dabyws$save(ValueOutput output, CallbackInfo ci) {
      if (this.dabyws$withered) {
         output.putBoolean("DabywsWithered", true);
      }
   }

   @Inject(
      method = {"loadAdditional"},
      at = {@At("TAIL")}
   )
   private void dabyws$load(ValueInput input, CallbackInfo ci) {
      this.dabyws$withered = input.getBooleanOr("DabywsWithered", false);
   }

   @Inject(
      method = {"getUpdateTag"},
      at = {@At("RETURN")}
   )
   private void dabyws$updateTag(Provider provider, CallbackInfoReturnable<CompoundTag> cir) {
      ((CompoundTag)cir.getReturnValue()).putBoolean("DabywsWithered", this.dabyws$withered);
      ((CompoundTag)cir.getReturnValue()).putBoolean("DabywsAffected", this.dabyws$affected);
   }

   @Inject(
      method = {"setRemoved"},
      at = {@At("TAIL")}
   )
   private void dabyws$removed(CallbackInfo ci) {
      BeaconBlockEntity self = (BeaconBlockEntity)(Object)this;
      if (self.getLevel() != null) {
         WitheredBeacons.remove(self.getLevel(), self.getBlockPos());
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private static void dabyws$tick(Level level, BlockPos pos, BlockState state, BeaconBlockEntity beacon, CallbackInfo ci) {
      if (!level.isClientSide()) {
         WitheredBeacon self = (WitheredBeacon)beacon;
         if (self.dabyws$isWithered()) {
            WitheredBeacons.add(level, pos);
         }

         if (level.getGameTime() % 80L == 0L) {
            boolean near = !self.dabyws$isWithered() && WitheredBeacons.anyNear(level, pos);
            if (near != self.dabyws$isAffected()) {
               self.dabyws$setAffected(near);
               level.sendBlockUpdated(pos, state, state, 3);
            }

            ((WitheredBeacon)beacon).dabyws$boost(level, pos);
         }
      }
   }

   @Unique
   public void dabyws$boost(Level level, BlockPos pos) {
      if (this.levels > 0) {
         BeaconBlockEntity self = (BeaconBlockEntity)(Object)this;
         WitheredBeacon flags = (WitheredBeacon)self;
         if (flags.dabyws$isWithered() || flags.dabyws$isAffected()) {
            double range = (double)(this.levels * 10 + 10);
            AABB box = new AABB(pos).inflate(range).expandTowards(0.0, (double)level.getHeight(), 0.0);
            List<Player> players = level.getEntitiesOfClass(Player.class, box);
            int duration = (9 + this.levels * 2) * 20;
            boolean doubled = this.levels >= 4 && this.primaryPower != null && this.primaryPower.equals(this.secondaryPower);
            if (this.primaryPower != null) {
               this.dabyws$give(players, this.primaryPower, duration, doubled ? 2 : 1);
            }

            if (this.levels >= 4 && this.secondaryPower != null && !doubled) {
               this.dabyws$give(players, this.secondaryPower, duration, 1);
            }
         }
      }
   }

   @Unique
   public void dabyws$give(List<Player> players, Holder<MobEffect> effect, int duration, int amplifier) {
      for (Player player : players) {
         player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
      }
   }
}
