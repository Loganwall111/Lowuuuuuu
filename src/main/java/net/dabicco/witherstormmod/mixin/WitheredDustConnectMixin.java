package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.ModBlocks;
import net.dabicco.witherstormmod.block.WitheredDustBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({RedStoneWireBlock.class})
public abstract class WitheredDustConnectMixin {
   private static final ThreadLocal<Block> DABYWS$ASKING = new ThreadLocal<>();

   @Inject(
      method = {"getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;"},
      at = {@At("HEAD")}
   )
   private void dabyws$rememberWhoIsAsking(BlockGetter level, BlockPos pos, Direction direction, boolean canSurvive, CallbackInfoReturnable<RedstoneSide> cir) {
      DABYWS$ASKING.set((Block)this);
   }

   @Inject(
      method = {"getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;"},
      at = {@At("RETURN")}
   )
   private void dabyws$doneAsking(BlockGetter level, BlockPos pos, Direction direction, boolean canSurvive, CallbackInfoReturnable<RedstoneSide> cir) {
      DABYWS$ASKING.remove();
   }

   @Inject(
      method = {"shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void dabyws$connectUpDown(BlockState neighbour, CallbackInfoReturnable<Boolean> cir) {
      Boolean forced = dabyws$decide(neighbour);
      if (forced != null) {
         cir.setReturnValue(forced);
      }
   }

   @Inject(
      method = {"shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void dabyws$connectSide(BlockState neighbour, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      Boolean forced = dabyws$decide(neighbour);
      if (forced != null) {
         cir.setReturnValue(forced);
      } else {
         if (direction != null && DABYWS$ASKING.get() instanceof WitheredDustBlock && neighbour.getBlock() instanceof PistonBaseBlock) {
            cir.setReturnValue(true);
         }
      }
   }

   private static Boolean dabyws$decide(BlockState neighbour) {
      boolean witheredAsking = DABYWS$ASKING.get() instanceof WitheredDustBlock;
      if (neighbour.is(ModBlocks.WITHERED_DUST)) {
         return witheredAsking;
      } else {
         return neighbour.is(Blocks.REDSTONE_WIRE) ? !witheredAsking : null;
      }
   }
}
