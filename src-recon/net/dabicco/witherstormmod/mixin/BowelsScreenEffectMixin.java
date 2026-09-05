package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.BowelsFrame;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ScreenEffectRenderer.class})
public class BowelsScreenEffectMixin {
   @Inject(
      method = {"getViewBlockingState(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void dabyws$viewBlockInFrame(Player player, CallbackInfoReturnable<BlockState> cir) {
      Direction gravity = BowelsFrame.boxAxis(player);
      if (gravity != Direction.DOWN) {
         cir.setReturnValue(BowelsFrame.viewBlocker(player, gravity));
      }
   }
}
