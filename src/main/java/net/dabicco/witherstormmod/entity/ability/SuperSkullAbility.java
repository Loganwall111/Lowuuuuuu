package net.dabicco.witherstormmod.entity.ability;

import net.dabicco.witherstormmod.entity.SuperSkullEntity;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Fires flaming super skulls from the storm's head toward the nearest target.
 *
 * Available from the tentacle phase onward (phase 3+). Pre-phase 4 the storm uses its
 * vanilla Wither skull attack; this ability takes over once the storm is a true boss.
 */
public class SuperSkullAbility implements StormAbility {
   private static final int COOLDOWN = 100;
   private int timer;

   @Override
   public double phaseThreshold() {
      return 3.0;
   }

   @Override
   public void tick(WitherStormEntity storm, ServerLevel level) {
      if (--this.timer > 0) {
         return;
      }
      this.timer = COOLDOWN;
      if (storm.isCollapsed()) {
         return;
      }
      Player target = level.getNearestPlayer(storm, 96.0);
      if (target == null) {
         return;
      }
      Vec3 mouth = storm.position().add(0.0, 3.0, 0.0);
      Vec3 dir = target.getEyePosition().subtract(mouth).normalize();
      SuperSkullEntity skull = new SuperSkullEntity(net.dabicco.witherstormmod.entity.ModEntityTypes.SUPER_SKULL, level);
      skull.setPos(mouth.x, mouth.y, mouth.z);
      skull.shoot(dir.scale(0.9));
      level.addFreshEntity(skull);
   }
}
