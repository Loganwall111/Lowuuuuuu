package net.dabicco.witherstormmod.entity.ability;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.dabicco.witherstormmod.entity.WitherStormHeadEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tractor beam ability (phase 4+, the signature Story Mode attack).
 *
 * When a head's beam is active it pulls blocks, items and mobs at its ground target up
 * toward the storm. This implementation delegates the visual beam to the head entity
 * (which already renders it) and adds the gameplay pull: entities caught at the beam's
 * impact point are dragged toward the storm's body and converted into growth.
 */
public class TractorBeamAbility implements StormAbility {
   private int pulse;

   @Override
   public double phaseThreshold() {
      return 4.0;
   }

   @Override
   public void tick(WitherStormEntity storm, ServerLevel level) {
      if ((++this.pulse & 7) != 0) {
         return;
      }
      for (int i = 0; i < 3; ++i) {
         WitherStormHeadEntity head = storm.hostHead(level, i);
         if (head == null || !head.isBeamActive()) {
            continue;
         }
         Vec3 end = head.getBeamEndExact();
         double r = Math.max(1.0, 3.0);
         AABB box = new AABB(end.x - r, end.y - 6.0, end.z - r, end.x + r, end.y + 14.0, end.z + r);
         for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == storm || e instanceof Player p && (p.isSpectator() || p.isCreative())) {
               continue;
            }
            Vec3 pull = storm.position().subtract(e.position()).normalize().scale(0.5);
            e.setDeltaMovement(pull);
            e.hurtMarked = true;
            if (e.distanceToSqr(storm) < 4.0 * 4.0) {
               storm.addSubGrowth(2);
               e.discard();
            }
         }
      }
   }
}
