package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.BowelsEntry;
import net.dabicco.witherstormmod.BowelsFrame;
import net.dabicco.witherstormmod.BowelsGravity;
import net.dabicco.witherstormmod.BowelsTrace;
import net.dabicco.witherstormmod.client.BowelsView;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public abstract class BowelsLocalPlayerMixin {
   @Unique
   private boolean dabyws$held;

   @Inject(
      method = {"moveTowardsClosestSpace(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$unstickInFrame(double x, double z, CallbackInfo ci) {
      LocalPlayer self = (LocalPlayer)(Object)this;
      Direction gravity = BowelsFrame.active(self);
      if (gravity != null) {
         ci.cancel();
         BowelsFrame.unstick(self, gravity, x, z);
      }
   }

   private void dabyws$holdForTurnover(LocalPlayer self) {
      if (BowelsView.holding()) {
         this.dabyws$held = true;
         self.setNoGravity(true);
         self.setDeltaMovement(Vec3.ZERO);
         self.xxa = 0.0F;
         self.yya = 0.0F;
         self.zza = 0.0F;
      } else if (this.dabyws$held) {
         this.dabyws$held = false;
         self.setNoGravity(false);
         Direction wall = BowelsView.leaving();
         if (wall != null) {
            Vec3 off = BowelsFrame.down(wall).scale(-1.35).add(BowelsFrame.down(BowelsFrame.boxAxis(self)).scale(-0.52));
            self.setDeltaMovement(off);
            self.setOnGround(false);
            self.hurtMarked = true;
         }
      }
   }

   private void dabyws$centreInDrop(LocalPlayer self) {
      if (BowelsGravity.isBowels(self.level()) && BowelsEntry.holds(self.getX(), self.getY(), self.getZ()) && !self.onGround() && !self.getAbilities().flying) {
         double dx = 28.5 - self.getX();
         double dy = 64.5 - self.getY();
         double off = Math.sqrt(dx * dx + dy * dy);
         if (!(off < 0.35)) {
            double pull = Math.min(off * 0.01, 0.03);
            Vec3 v = self.getDeltaMovement();
            self.setDeltaMovement(v.x + dx / off * pull, v.y + dy / off * pull, v.z);
         }
      }
   }

   @Inject(
      method = {"aiStep()V"},
      at = {@At("HEAD")}
   )
   private void dabyws$intoFrame(CallbackInfo ci) {
      LocalPlayer self = (LocalPlayer)(Object)this;
      this.dabyws$holdForTurnover(self);
      this.dabyws$centreInDrop(self);
      boolean outer = BowelsFrame.active(self) == null;
      if (outer) {
         BowelsTrace.record(self, 0, self.getDeltaMovement(), false);
      }

      BowelsFrame.stepIn(self);
      if (outer) {
         BowelsTrace.record(self, 1, self.getDeltaMovement(), true);
      }
   }

   @Inject(
      method = {"aiStep()V"},
      at = {@At("RETURN")}
   )
   private void dabyws$outOfFrame(CallbackInfo ci) {
      LocalPlayer self = (LocalPlayer)(Object)this;
      BowelsTrace.record(self, 5, self.getDeltaMovement(), true);
      BowelsFrame.stepOut(self);
      if (BowelsFrame.active(self) == null) {
         BowelsTrace.record(self, 6, self.getDeltaMovement(), false);
      }
   }
}
