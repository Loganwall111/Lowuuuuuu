package net.dabicco.witherstormmod.menu;

import java.util.Objects;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FurnaceFilterMenu extends AbstractContainerMenu {
   private final Container container;
   private final ContainerData data;

   public FurnaceFilterMenu(int id, Inventory playerInv) {
      this(id, playerInv, new SimpleContainer(2), new SimpleContainerData(1));
   }

   public FurnaceFilterMenu(int id, Inventory playerInv, Container container, ContainerData data) {
      super(net.dabicco.witherstormmod.menu.ModMenus.FURNACE_FILTER, id);
      checkContainerSize(container, 2);
      this.container = container;
      this.data = data;
      container.startOpen(playerInv.player);
      this.addSlot(new Slot(container, 0, 56, 35) {
         {
            Objects.requireNonNull(FurnaceFilterMenu.this);
         }

         public boolean mayPickup(Player p) {
            return false;
         }

         public boolean mayPlace(ItemStack s) {
            return false;
         }
      });
      this.addSlot(new Slot(container, 1, 116, 35) {
         {
            Objects.requireNonNull(FurnaceFilterMenu.this);
         }

         public boolean mayPlace(ItemStack s) {
            return false;
         }
      });

      for (int r = 0; r < 3; r++) {
         for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(playerInv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
         }
      }

      for (int c = 0; c < 9; c++) {
         this.addSlot(new Slot(playerInv, c, 8 + c * 18, 142));
      }

      this.addDataSlots(data);
   }

   public int getProgress() {
      return this.data.get(0);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = (Slot)(Object)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         result = stack.copy();
         if (index >= 2) {
            return ItemStack.EMPTY;
         }

         if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return result;
   }

   public boolean stillValid(Player player) {
      return this.container.stillValid(player);
   }

   public void removed(Player player) {
      super.removed(player);
      this.container.stopOpen(player);
   }
}
