package net.dabicco.witherstormmod.menu;

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
   private static final int CONTAINER_SIZE = 2;
   private final Container container;
   private final ContainerData data;

   // Client-side reconstruction: the server sends the menu data, so a placeholder
   // container is fine here; the real contents come from the block entity on the server.
   public FurnaceFilterMenu(int syncId, Inventory inv) {
      this(syncId, inv, new SimpleContainer(2), new SimpleContainerData(1));
   }

   public FurnaceFilterMenu(int syncId, Inventory inv, Container container, ContainerData data) {
      super(ModMenus.FURNACE_FILTER, syncId);
      checkContainerSize(container, 2);
      this.container = container;
      this.data = data;
      this.addSlot(new Slot(container, 0, 56, 34));
      this.addSlot(new Slot(container, 1, 116, 34));

      for(int row = 0; row < 3; ++row) {
         for(int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

      for(int col = 0; col < 9; ++col) {
         this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
      }

      this.addDataSlots(data);
   }

   public int getProgress() {
      return this.data.get(0);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack slotStack = slot.getItem();
         itemstack = slotStack.copy();
         if (index < 2) {
            if (!this.moveItemStackTo(slotStack, 2, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(slotStack, 0, 2, false)) {
            return ItemStack.EMPTY;
         }

         if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return itemstack;
   }

   public boolean stillValid(Player player) {
      return this.container.stillValid(player);
   }
}
