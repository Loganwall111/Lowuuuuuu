package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.bowels.BowelsPlacedBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BlockItem.class})
public abstract class BowelsPlacementMixin {
   @Inject(
      method = {"placeBlock"},
      at = {@At("RETURN")}
   )
   private void dabyws$rememberWhatWasBuilt(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
      if (cir.getReturnValueZ()) {
         BowelsPlacedBlocks.remember(context.getLevel(), context.getClickedPos());
      }
   }
}
