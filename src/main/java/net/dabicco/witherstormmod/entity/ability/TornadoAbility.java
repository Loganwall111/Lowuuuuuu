package net.dabicco.witherstormmod.entity.ability;

import net.dabicco.witherstormmod.ModParticles;
import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tornado / vortex (phase 4+, the signature swirling debris column from the video).
 *
 * Periodically spawns a wind vortex under the storm: it throws debris particles into a
 * rotating spiral and throws/damages living entities caught inside it. This sells the
 * "the storm is tearing the world apart around it" feel between cluster absorptions.
 */
public class TornadoAbility implements StormAbility {
   private static final int ACTIVE_TICKS = 60;
   private static final int COOLDOWN = 200;
   private static final double VORTEX_RADIUS = 6.0;
   private static final double VORTEX_HEIGHT = 40.0;
   private int state;
   private int timer;
   private float phase;

   @Override
   public double phaseThreshold() {
      return 4.0;
   }

   @Override
   public void tick(WitherStormEntity storm, ServerLevel level) {
      if (storm.isCollapsed()) {
         this.state = 0;
         return;
      }
      ++this.timer;
      if (this.state == 0) {
         if (this.timer >= COOLDOWN) {
            this.state = 1;
            this.timer = 0;
            this.phase = level.getRandom().nextFloat() * 6.2832F;
         }
         return;
      }

      if (this.state == 1 && this.timer >= ACTIVE_TICKS) {
         this.state = 0;
         this.timer = 0;
         return;
      }

      // Spin debris particles in a vortex column under the storm.
      if (this.timer % 2 == 0) {
         this.phase += 0.35F;
         double rad = this.phase;
         Vec3 base = storm.position().add(0.0, -3.0, 0.0);
         int motes = 3;
         for (int i = 0; i < motes; ++i) {
            double h = level.getRandom().nextDouble() * VORTEX_HEIGHT;
            double r = 1.5 + level.getRandom().nextDouble() * (VORTEX_RADIUS - 1.5);
            double a = rad + (i * Math.PI * 2.0 / motes);
            double px = base.x + Math.cos(a) * r;
            double pz = base.z + Math.sin(a) * r;
            double py = base.y + h;
            level.sendParticles(ParticleTypes.SWIRL, px, py, pz, 1, 0.0, 0.4, 0.0, 0.0);
         }
         level.sendParticles(ModParticles.BEAM_MOTE, storm.getX(), storm.getY() - 3.0, storm.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
      }

      if (this.timer == 1) {
         level.playSound(null, storm.blockPosition(), ModSounds.STORM_TORNADO_LOOP, SoundSource.HOSTILE, 8.0F, 1.0F);
      }

      // Throw + damage entities inside the vortex.
      if (this.timer % 5 == 0) {
         AABB box = new AABB(storm.getX() - VORTEX_RADIUS, storm.getY() - 8.0, storm.getZ() - VORTEX_RADIUS, storm.getX() + VORTEX_RADIUS, storm.getY() + VORTEX_HEIGHT, storm.getZ() + VORTEX_RADIUS);
         for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == storm || e instanceof Player p && (p.isSpectator() || p.isCreative())) {
               continue;
            }
            double dx = e.getX() - storm.getX();
            double dz = e.getZ() - storm.getZ();
            double ang = Math.atan2(dz, dx) + 0.4;
            double spin = 0.6;
            Vec3 vel = new Vec3(Math.cos(ang) * spin - dx * 0.03, 0.6, Math.sin(ang) * spin - dz * 0.03);
            e.setDeltaMovement(vel);
            e.fallDistance = 0.0F;
            e.hurtMarked = true;
         }
      }
   }
}
