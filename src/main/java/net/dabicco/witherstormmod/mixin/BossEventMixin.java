package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BossEvent.class})
public abstract class BossEventMixin {
   @Shadow
   public abstract Component getName();

   @Inject(
      method = {"getName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$overrideName(CallbackInfoReturnable<Component> cir) {
      if ((Object)this instanceof LerpingBossEvent) {
         Component original = (Component)cir.getReturnValue();
         if (original != null) {
            String name = original.getString();
            if (!name.contains("The Wither Storm") && !name.equals("Wither Storm")) {
               if (name.contains("Commanded Wither") || name.startsWith("Wither (")) {
                  String want = DabyWSClientConfig.earlyName();
                  if (!name.equals(want)) {
                     cir.setReturnValue(Component.literal(want));
                  }
               }
            } else {
               String want = DabyWSClientConfig.stormName();
               if (!name.equals(want)) {
                  cir.setReturnValue(Component.literal(want));
               }
            }
         }
      }
   }

   @Inject(
      method = {"getColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$overrideColor(CallbackInfoReturnable<BossBarColor> cir) {
      if (this.dabyws$isStormBarOnClient()) {
         int idx = (int)DabyWSClientConfig.bossbarColor;
         BossBarColor[] colors = BossBarColor.values();
         cir.setReturnValue(colors[Math.max(0, Math.min(idx, colors.length - 1))]);
      }
   }

   @Inject(
      method = {"getOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$overrideOverlay(CallbackInfoReturnable<BossBarOverlay> cir) {
      if (this.dabyws$isStormBarOnClient()) {
         cir.setReturnValue(DabyWSClientConfig.bossbarNotched ? BossBarOverlay.NOTCHED_10 : BossBarOverlay.PROGRESS);
      }
   }

   private boolean dabyws$isStormBarOnClient() {
      if (!((Object)this instanceof LerpingBossEvent)) {
         return false;
      } else {
         String name = this.getName().getString();
         return name.contains("Wither Storm") || name.contains("Commanded Wither");
      }
   }
}
