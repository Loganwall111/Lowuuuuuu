package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.ModItems;
import net.dabicco.witherstormmod.entity.FormidibombEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ResultSlot.class})
public class ResultSlotMixin {
   @Inject(
      method = {"onTake"},
      at = {@At("TAIL")}
   )
   private void dabyws$formidibombMorph(Player player, ItemStack stack, CallbackInfo ci) {
      if (stack.is(ModItems.FORMIDIBOMB) && !player.level().isClientSide()) {
         if (player.containerMenu instanceof CraftingMenu menu) {
            ((net.dabicco.witherstormmod.mixin.CraftingMenuAccessor)menu).dabyws$getAccess().execute((level, pos) -> {
               level.removeBlock(pos, false);
               FormidibombEntity bomb = new FormidibombEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
               level.addFreshEntity(bomb);
            });
            player.containerMenu.setCarried(ItemStack.EMPTY);
         }

         stack.setCount(0);
         if (player instanceof ServerPlayer sp) {
            sp.closeContainer();
         }
      }
   }
}
