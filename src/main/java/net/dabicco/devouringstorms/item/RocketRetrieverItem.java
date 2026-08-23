package net.dabicco.devouringstorms.item;

import java.util.Optional;
import java.util.function.Consumer;
import net.dabicco.devouringstorms.ModAdvancements;
import net.dabicco.devouringstorms.ModComponents;
import net.dabicco.devouringstorms.entity.GrappledTntEntity;
import net.dabicco.devouringstorms.entity.Yeet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class RocketRetrieverItem extends Item {
   public RocketRetrieverItem(Properties properties) {
      super(properties);
   }

   public static RetrieverContents contents(ItemStack stack) {
      return (RetrieverContents)stack.getOrDefault(ModComponents.RETRIEVER_CONTENTS, RetrieverContents.EMPTY);
   }

   private static void setContents(ItemStack stack, RetrieverContents c) {
      if (c.isEmpty()) {
         stack.remove(ModComponents.RETRIEVER_CONTENTS);
      } else {
         stack.set(ModComponents.RETRIEVER_CONTENTS, c);
      }
   }

   public boolean overrideOtherStackedOnMe(ItemStack retriever, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
      if (action == ClickAction.SECONDARY && !other.isEmpty()) {
         int loaded = load(retriever, other);
         if (loaded > 0) {
            other.shrink(loaded);
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.9F);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean overrideStackedOnOther(ItemStack retriever, Slot slot, ClickAction action, Player player) {
      if (action != ClickAction.SECONDARY) {
         return false;
      } else {
         ItemStack target = slot.getItem();
         if (!target.isEmpty() && slot.mayPickup(player)) {
            int loaded = load(retriever, target);
            if (loaded > 0) {
               target.shrink(loaded);
               player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.9F);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private static int load(ItemStack retriever, ItemStack src) {
      RetrieverContents c = contents(retriever);
      int take = 0;
      if (src.is(Items.TNT)) {
         take = Math.min(src.getCount(), 4 - c.tnt());
         if (take > 0) {
            setContents(retriever, c.withTnt(c.tnt() + take));
         }
      } else if (src.is(Items.FIREWORK_ROCKET)) {
         take = Math.min(src.getCount(), 4 - c.rocketCount());
         RetrieverContents next = c;

         for (int k = 0; k < take; k++) {
            next = next.addRocket(src);
         }

         if (take > 0) {
            setContents(retriever, next);
         }
      }

      return take;
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (player instanceof ServerPlayer sp) {
         ModAdvancements.grant(sp, "boomtown");
      }

      ItemStack stack = player.getItemInHand(hand);
      if (!contents(stack).canShoot()) {
         return InteractionResult.FAIL;
      } else {
         player.startUsingItem(hand);
         return InteractionResult.CONSUME;
      }
   }

   public ItemUseAnimation getUseAnimation(ItemStack stack) {
      return ItemUseAnimation.NONE;
   }

   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
   }

   public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (entity instanceof Player player) {
         if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) {
            return false;
         } else {
            RetrieverContents c = contents(stack);
            if (!c.canShoot()) {
               return false;
            } else if (GrappledTntEntity.hasActiveShot(level, player)) {
               return false;
            } else {
               if (!level.isClientSide()) {
                  ItemStack firework = c.lastRocket();
                  Yeet.fire(level, player, stack, firework);
                  setContents(stack, c.consumeOne());
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
      RetrieverContents c = contents(stack);
      adder.accept(Component.literal("TNT: " + c.tnt() + "/4").withStyle(ChatFormatting.RED));
      adder.accept(Component.literal("Rockets: " + c.rocketCount() + "/4").withStyle(ChatFormatting.GOLD));

      for (int i = c.rockets().size() - 1; i >= 0; i--) {
         ItemStack fw = c.rockets().get(i);
         Fireworks comp = (Fireworks)fw.get(DataComponents.FIREWORKS);
         int flight = comp != null ? comp.flightDuration() : 1;
         adder.accept(Component.literal(" • Flight Duration " + flight + " Firework").withStyle(ChatFormatting.GRAY));
         if (comp != null) {
            for (FireworkExplosion exp : comp.explosions()) {
               Component shape = Component.translatable("item.minecraft.firework_star.shape." + exp.shape().getSerializedName());
               adder.accept(Component.literal("    ⤷ ").withStyle(ChatFormatting.DARK_GRAY).append(shape.copy().withStyle(ChatFormatting.DARK_GRAY)));
            }
         }
      }
   }

   public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
      RetrieverContents c = contents(stack);
      return c.isEmpty() ? Optional.empty() : Optional.of(new RetrieverTooltip(c.tnt(), c.rocketCount()));
   }
}
