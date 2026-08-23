package net.dabicco.devouringstorms;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class ModEnchantments {
   public static final ResourceKey<Enchantment> GRAVITIC_DRAG;
   private static final EquipmentSlot[] ARMOUR;
   private static final float PER_PIECE_AT_MAX = 0.125F;
   private static final float HEAVY_BONUS_AT_MAX = 0.0375F;
   private static final float MAX_RESISTANCE = 0.7F;

   private ModEnchantments() {
   }

   public static float dragResistance(LivingEntity entity) {
      float total = 0.0F;

      for(EquipmentSlot slot : ARMOUR) {
         ItemStack stack = entity.getItemBySlot(slot);
         if (!stack.isEmpty()) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(GRAVITIC_DRAG), stack);
            if (level > 0) {
               float scale = (float)Math.min(level, 3) / 3.0F;
               total += 0.125F * scale;
               if (isHeavy(stack)) {
                  total += 0.0375F * scale;
               }
            }
         }
      }

      return Math.min(total, 0.7F);
   }

   private static boolean isHeavy(ItemStack stack) {
      return stack.is(ItemTags.DIAMOND_TOOL_MATERIALS) || stack.is(ItemTags.NETHERITE_TOOL_MATERIALS) || isDiamondOrNetheriteArmour(stack);
   }

   private static boolean isDiamondOrNetheriteArmour(ItemStack stack) {
      return stack.is(Items.DIAMOND_HELMET) || stack.is(Items.DIAMOND_CHESTPLATE) || stack.is(Items.DIAMOND_LEGGINGS) || stack.is(Items.DIAMOND_BOOTS) || stack.is(Items.NETHERITE_HELMET) || stack.is(Items.NETHERITE_CHESTPLATE) || stack.is(Items.NETHERITE_LEGGINGS) || stack.is(Items.NETHERITE_BOOTS);
   }

   public static boolean isWearing(LivingEntity entity) {
      return dragResistance(entity) > 0.0F;
   }

   static {
      GRAVITIC_DRAG = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("devouringstorms", "gravitic_drag"));
      ARMOUR = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   }
}
