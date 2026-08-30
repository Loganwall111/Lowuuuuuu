package net.dabicco.witherstormmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Living anchor for a rift into Decayed Reality. */
public class RealityRiftEntity extends LivingEntity {
   public RealityRiftEntity(EntityType<? extends LivingEntity> type, Level level) { super(type, level); }
   @Override public ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }
   @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) { }
   @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
}
