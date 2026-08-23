package net.dabicco.devouringstorms.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class Yeet {
   private Yeet() {
   }

   public static void fire(Level level, Player player, ItemStack retriever, ItemStack firework) {
      Vec3 dir = player.getLookAngle();
      GrappledTntEntity shot = new GrappledTntEntity(level, player, dir, firework);
      level.addFreshEntity(shot);
      level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 1.0F);
   }
}
