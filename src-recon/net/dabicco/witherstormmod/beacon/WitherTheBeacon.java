package net.dabicco.witherstormmod.beacon;

import net.dabicco.witherstormmod.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.Before;
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
            if (level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon) {
               net.dabicco.witherstormmod.beacon.WitheredBeacon flags = (net.dabicco.witherstormmod.beacon.WitheredBeacon)beacon;
               ItemStack held = player.getItemInHand(hand);
               if (!flags.dabyws$isWithered()) {
                  if (!held.is(ModItems.WITHERED_NETHER_STAR)) {
                     return InteractionResult.PASS;
                  } else {
                     wither(level, beacon, pos);
                     if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                     }

                     if (player instanceof ServerPlayer sp) {
                        net.dabicco.witherstormmod.beacon.WitheredBeacons.award(sp, "withered_beacon");
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
      PlayerBlockBreakEvents.BEFORE
         .register(
            (Before)(world, player, pos, state, blockEntity) -> {
               if (!world.isClientSide()
                  && blockEntity instanceof BeaconBlockEntity beacon
                  && ((net.dabicco.witherstormmod.beacon.WitheredBeacon)beacon).dabyws$isWithered()) {
                  pop(world, beacon, pos, player);
               }

               return true;
            }
         );
   }

   public static void pop(Level level, BeaconBlockEntity beacon, BlockPos pos, Player toward) {
      ((net.dabicco.witherstormmod.beacon.WitheredBeacon)beacon).dabyws$setWithered(false);
      net.dabicco.witherstormmod.beacon.WitheredBeacons.justPopped(level, pos);
      level.playSound((Entity)null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.7F, 1.4F);
      ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, new ItemStack(ModItems.WITHERED_NETHER_STAR));
      Vec3 away = toward == null ? new Vec3(0.0, 0.0, 1.0) : toward.position().subtract(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      if (away.horizontalDistanceSqr() < 1.0E-4) {
         away = new Vec3(0.0, 0.0, 1.0);
      }

      away = away.normalize();
      drop.setDeltaMovement(away.x * 0.28, 0.24, away.z * 0.28);
      drop.setDefaultPickUpDelay();
      level.addFreshEntity(drop);
   }

   public static void wither(Level level, BeaconBlockEntity beacon, BlockPos pos) {
      ((net.dabicco.witherstormmod.beacon.WitheredBeacon)beacon).dabyws$setWithered(true);
      level.playSound((Entity)null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
   }
}
