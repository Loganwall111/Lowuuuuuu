package net.dabicco.witherstormmod.beacon;

import net.dabicco.witherstormmod.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class WitherTheBeacon {
   private WitherTheBeacon() {
   }

   public static void listen() {
      UseBlockCallback.EVENT.register((UseBlockCallback)(player, level, hand, hit) -> {
         if (level.isClientSide()) {
            return InteractionResult.PASS;
         } else {
            BlockPos pos = hit.getBlockPos();
            BlockEntity patt0$temp = level.getBlockEntity(pos);
            if (patt0$temp instanceof BeaconBlockEntity) {
               BeaconBlockEntity beacon = (BeaconBlockEntity)patt0$temp;
               WitheredBeacon flags = (WitheredBeacon)beacon;
               ItemStack held = player.getItemInHand(hand);
               if (!flags.dabyws$isWithered()) {
                  if (!held.is(ModItems.WITHERED_NETHER_STAR)) {
                     return InteractionResult.PASS;
                  } else {
                     wither(level, beacon, pos);
                     if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                     }

                     if (player instanceof ServerPlayer) {
                        ServerPlayer sp = (ServerPlayer)player;
                        WitheredBeacons.award(sp, "withered_beacon");
                     }

                     return InteractionResult.SUCCESS;
                  }
               } else if (!player.isShiftKeyDown()) {
                  return InteractionResult.PASS;
               } else {
                  pop(level, beacon, pos, player);
                  return InteractionResult.SUCCESS;
               }
            } else {
               return InteractionResult.PASS;
            }
         }
      });
      PlayerBlockBreakEvents.BEFORE.register((PlayerBlockBreakEvents.Before)(world, player, pos, state, blockEntity) -> {
         if (!world.isClientSide() && blockEntity instanceof BeaconBlockEntity beacon) {
            if (((WitheredBeacon)beacon).dabyws$isWithered()) {
               pop(world, beacon, pos, player);
            }
         }

         return true;
      });
   }

   public static void pop(Level level, BeaconBlockEntity beacon, BlockPos pos, Player toward) {
      ((WitheredBeacon)beacon).dabyws$setWithered(false);
      WitheredBeacons.justPopped(level, pos);
      level.playSound((Entity)null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.7F, 1.4F);
      ItemEntity drop = new ItemEntity(level, (double)pos.getX() + (double)0.5F, (double)pos.getY() + 1.1, (double)pos.getZ() + (double)0.5F, new ItemStack(ModItems.WITHERED_NETHER_STAR));
      Vec3 away = toward == null ? new Vec3((double)0.0F, (double)0.0F, (double)1.0F) : toward.position().subtract((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F);
      if (away.horizontalDistanceSqr() < 1.0E-4) {
         away = new Vec3((double)0.0F, (double)0.0F, (double)1.0F);
      }

      away = away.normalize();
      drop.setDeltaMovement(away.x * 0.28, 0.24, away.z * 0.28);
      drop.setDefaultPickUpDelay();
      level.addFreshEntity(drop);
   }

   public static void wither(Level level, BeaconBlockEntity beacon, BlockPos pos) {
      ((WitheredBeacon)beacon).dabyws$setWithered(true);
      level.playSound((Entity)null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
   }
}
