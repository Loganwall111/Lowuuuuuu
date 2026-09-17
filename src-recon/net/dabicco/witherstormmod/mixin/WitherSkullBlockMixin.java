package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.WitherStormSummon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WitherSkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WitherSkullBlock.class})
public class WitherSkullBlockMixin {
   @Inject(
      method = {"checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void witherstormmod$stormSpawn(Level level, BlockPos pos, CallbackInfo ci) {
      if (WitherStormSummon.trySpawn(level, pos)) {
         ci.cancel();
      }
   }
}
