package net.dabicco.witherstormmod.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.dabicco.witherstormmod.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class WitheredStarEntity {
   private static final double FROM_ABOVE = (double)26.0F;
   private static final int FALL_TICKS = 140;
   private static final Map<UUID, UUID> FALLING = new HashMap();

   private WitheredStarEntity() {
   }

   public static void sendTo(ServerPlayer winner) {
      ServerLevel level = winner.level();
      Vec3 from = winner.position().add((double)0.0F, (double)26.0F, (double)0.0F);
      ItemStack star = new ItemStack(ModItems.WITHERED_NETHER_STAR);
      ItemEntity drop = new ItemEntity(level, from.x, from.y, from.z, star);
      drop.setNoGravity(true);
      drop.setUnlimitedLifetime();
      drop.setPickUpDelay(160);
      drop.setGlowingTag(true);
      drop.setTarget(winner.getUUID());
      level.addFreshEntity(drop);
      FALLING.put(drop.getUUID(), winner.getUUID());
      level.playSound((Entity)null, winner.getX(), winner.getY(), winner.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.MASTER, 2.0F, 1.4F);
   }

   public static void tick(ServerLevel level) {
      if (!FALLING.isEmpty()) {
         MinecraftServer server = level.getServer();
         if (server != null) {
            FALLING.entrySet().removeIf((entry) -> {
               Entity drop = level.getEntity((UUID)entry.getKey());
               if (drop instanceof ItemEntity star) {
                  if (star.isRemoved()) {
                     return true;
                  } else {
                     ServerPlayer winner = server.getPlayerList().getPlayer((UUID)entry.getValue());
                     if (winner == null) {
                        star.setNoGravity(false);
                        star.setPickUpDelay(0);
                        return true;
                     } else {
                        Vec3 to = winner.position().add((double)0.0F, (double)1.0F, (double)0.0F);
                        Vec3 run = to.subtract(star.position());
                        double d = run.length();
                        if (d < 1.2) {
                           star.setPickUpDelay(0);
                           star.setNoGravity(false);
                           level.playSound((Entity)null, winner.getX(), winner.getY(), winner.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.4F, 1.2F);
                           return true;
                        } else {
                           double speed = Math.min(0.55, 0.06 + d * 0.02);
                           star.setDeltaMovement(run.scale(speed / d));
                           star.hurtMarked = true;
                           level.sendParticles(ParticleTypes.END_ROD, star.getX(), star.getY(), star.getZ(), 3, 0.12, 0.12, 0.12, 0.01);
                           level.sendParticles(ParticleTypes.GLOW, star.getX(), star.getY(), star.getZ(), 1, 0.2, 0.2, 0.2, (double)0.0F);
                           return false;
                        }
                     }
                  }
               } else {
                  return false;
               }
            });
         }
      }
   }
}
