package net.dabicco.witherstormmod.block;

import net.dabicco.witherstormmod.entity.SuperTntEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class SuperTntBlock extends TntBlock {
   public SuperTntBlock(Properties properties) {
      super(properties);
   }

   private static void primeSuper(Level level, BlockPos pos, LivingEntity igniter) {
      if (!level.isClientSide()) {
         SuperTntEntity tnt = new SuperTntEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
         level.addFreshEntity(tnt);
         level.playSound((Entity)null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
         level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
      }
   }

   protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
      if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
         primeSuper(level, pos, (LivingEntity)null);
         level.removeBlock(pos, false);
      }
   }

   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean moved) {
      if (level.hasNeighborSignal(pos)) {
         primeSuper(level, pos, (LivingEntity)null);
         level.removeBlock(pos, false);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && !player.isCreative() && (Boolean)state.getValue(UNSTABLE)) {
         primeSuper(level, pos, (LivingEntity)null);
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
         return super.useItemOn(stack, state, level, pos, player, hand, hit);
      } else {
         primeSuper(level, pos, player);
         level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
         if (!player.isCreative()) {
            if (stack.is(Items.FLINT_AND_STEEL)) {
               stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            } else {
               stack.consume(1, player);
            }
         }

         return InteractionResult.SUCCESS;
      }
   }

   protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
      if (!level.isClientSide() && projectile.isOnFire() && projectile.mayInteract((ServerLevel)level, hit.getBlockPos())) {
         LivingEntity var10000;
         if (projectile.getOwner() instanceof LivingEntity le) {
            var10000 = le;
         } else {
            var10000 = null;
         }

         primeSuper(level, hit.getBlockPos(), var10000);
         level.removeBlock(hit.getBlockPos(), false);
      }
   }

   public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
      SuperTntEntity tnt = new SuperTntEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, explosion.getIndirectSourceEntity());
      int base = tnt.getFuse();
      tnt.setFuse((short)(level.getRandom().nextInt(base / 4) + base / 8));
      level.addFreshEntity(tnt);
   }
}
