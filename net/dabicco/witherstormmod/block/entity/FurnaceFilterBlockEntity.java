package net.dabicco.witherstormmod.block.entity;

import net.dabicco.witherstormmod.ModItems;
import net.dabicco.witherstormmod.menu.FurnaceFilterMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FurnaceFilterBlockEntity extends BlockEntity implements Container, MenuProvider {
   public static final int FRAGMENTS_PER_ESSENCE = 8;
   private static final int SLOT_PREVIEW = 0;
   private static final int SLOT_OUTPUT = 1;
   private static final int RESULT_SLOT = 2;
   private final NonNullList<ItemStack> items;
   private int progress;
   private int lastOutputGunpowder;
   private final ContainerData data;

   public FurnaceFilterBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.FURNACE_FILTER, pos, state);
      this.items = NonNullList.withSize(2, ItemStack.EMPTY);
      this.data = new ContainerData() {
         public int get(int i) {
            return i == 0 ? FurnaceFilterBlockEntity.this.progress : 0;
         }

         public void set(int i, int v) {
            if (i == 0) {
               FurnaceFilterBlockEntity.this.progress = v;
            }

         }

         public int getCount() {
            return 1;
         }
      };
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, FurnaceFilterBlockEntity be) {
      if (level instanceof ServerLevel server) {
         boolean lit;
         BlockState below;
         label46: {
            lit = false;
            below = server.getBlockState(pos.below());
            if (below.getBlock() instanceof AbstractFurnaceBlock) {
               BlockEntity var8 = server.getBlockEntity(pos.below());
               if (var8 instanceof AbstractFurnaceBlockEntity) {
                  AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity)var8;
                  lit = below.hasProperty(AbstractFurnaceBlock.LIT) && (Boolean)below.getValue(AbstractFurnaceBlock.LIT);
                  ItemStack result = furnace.getItem(2);
                  int cur = result.is(Items.GUNPOWDER) ? result.getCount() : 0;
                  if (cur > be.lastOutputGunpowder) {
                     be.addProgress(cur - be.lastOutputGunpowder);
                  }

                  be.lastOutputGunpowder = cur;
                  break label46;
               }
            }

            be.lastOutputGunpowder = 0;
         }

         if (below.hasProperty(AbstractFurnaceBlock.LIT) && (Boolean)state.getValue(BlockStateProperties.LIT) != lit) {
            server.setBlock(pos, (BlockState)state.setValue(BlockStateProperties.LIT, lit), 3);
         }

         if (lit && server.getGameTime() % 6L == 0L) {
            server.sendParticles(ParticleTypes.SMOKE, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.75F, (double)pos.getZ() + (double)0.5F, 1, 0.12, 0.02, 0.12, 0.01);
         }

      }
   }

   private void addProgress(int n) {
      this.progress += n;

      while(this.progress >= 8) {
         this.progress -= 8;
         ItemStack out = (ItemStack)this.items.get(1);
         if (out.isEmpty()) {
            this.items.set(1, new ItemStack(ModItems.COMMAND_ESSENCE));
         } else {
            if (!out.is(ModItems.COMMAND_ESSENCE) || out.getCount() >= out.getMaxStackSize()) {
               this.progress = 7;
               break;
            }

            out.grow(1);
         }
      }

      this.setChanged();
   }

   public ItemStack previewStack() {
      if (this.progress <= 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack s = new ItemStack(ModItems.COMMAND_ESSENCE);
         s.set(DataComponents.MAX_DAMAGE, 8);
         s.set(DataComponents.DAMAGE, 8 - this.progress);
         s.set(DataComponents.CUSTOM_NAME, Component.translatable("item.dabywitherstormmod.command_essence").withStyle(ChatFormatting.OBFUSCATED));
         return s;
      }
   }

   public ItemStack getOutput() {
      return (ItemStack)this.items.get(1);
   }

   public int getProgress() {
      return this.progress;
   }

   public ContainerData getData() {
      return this.data;
   }

   public int getContainerSize() {
      return 2;
   }

   public boolean isEmpty() {
      return ((ItemStack)this.items.get(1)).isEmpty();
   }

   public ItemStack getItem(int slot) {
      return slot == 0 ? this.previewStack() : (ItemStack)this.items.get(slot);
   }

   public ItemStack removeItem(int slot, int amount) {
      if (slot == 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack r = ContainerHelper.removeItem(this.items, slot, amount);
         if (!r.isEmpty()) {
            this.setChanged();
         }

         return r;
      }
   }

   public ItemStack removeItemNoUpdate(int slot) {
      if (slot == 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack r = (ItemStack)this.items.get(slot);
         this.items.set(slot, ItemStack.EMPTY);
         return r;
      }
   }

   public void setItem(int slot, ItemStack stack) {
      if (slot != 0) {
         this.items.set(slot, stack);
         this.setChanged();
      }
   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public void clearContent() {
      this.items.set(1, ItemStack.EMPTY);
   }

   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.items.set(1, ItemStack.EMPTY);
      NonNullList<ItemStack> tmp = NonNullList.withSize(1, ItemStack.EMPTY);
      ContainerHelper.loadAllItems(input, tmp);
      this.items.set(1, (ItemStack)tmp.get(0));
      this.progress = input.getIntOr("Progress", 0);
      this.lastOutputGunpowder = input.getIntOr("LastGunpowder", 0);
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      NonNullList<ItemStack> tmp = NonNullList.withSize(1, (ItemStack)this.items.get(1));
      ContainerHelper.saveAllItems(output, tmp);
      output.putInt("Progress", this.progress);
      output.putInt("LastGunpowder", this.lastOutputGunpowder);
   }

   public Component getDisplayName() {
      return Component.translatable("block.dabywitherstormmod.furnace_filter");
   }

   public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
      return new FurnaceFilterMenu(id, inv, this, this.data);
   }
}
