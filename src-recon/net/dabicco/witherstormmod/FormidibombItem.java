package net.dabicco.witherstormmod.item;

import net.dabicco.witherstormmod.entity.FormidibombEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class FormidibombItem extends Item {
   public FormidibombItem(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
      if (!level.isClientSide()) {
         FormidibombEntity bomb = new FormidibombEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
         level.addFreshEntity(bomb);
         if (ctx.getPlayer() == null || !ctx.getPlayer().hasInfiniteMaterials()) {
            ctx.getItemInHand().shrink(1);
         }
      }

      return InteractionResult.SUCCESS;
   }
}
