package net.dabicco.witherstormmod.entity.ability;

import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tentacle slam (phase 4+, like the show).
 *
 * Body tentacles periodically slam down through the terrain around the storm, carving
 * blocks, kicking up debris, and slapping players across the face with heavy knockback.
 * This is the "tentacles slam through blocks / slap you in the face" behaviour from the
 * video. Requires {@code tentacleSlam} in the config.
 */
public class TentacleSlamAbility implements StormAbility {
   private int cooldown;

   @Override
   public double phaseThreshold() {
      return 4.0;
   }

   @Override
   public void tick(WitherStormEntity storm, ServerLevel level) {
      if (WitherStormConfigs.get(level).tentacleSlam == 0 || storm.isCollapsed()) {
         return;
      }
      if (--this.cooldown > 0) {
         return;
      }
      this.cooldown = 80 + level.getRandom().nextInt(40);
      int radius = WitherStormConfigs.get(level).tentacleSlamRadius;
      double ang = level.getRandom().nextDouble() * Math.PI * 2.0;
      double dist = 6.0 + level.getRandom().nextDouble() * 10.0;
      double sx = storm.getX() + Math.cos(ang) * dist;
      double sz = storm.getZ() + Math.sin(ang) * dist;
      int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(sx), (int) Math.floor(sz));
      Vec3 impact = new Vec3(sx, y, sz);

      // Carve blocks in a radius (tentacle rips through the terrain).
      for (int dx = -radius; dx <= radius; ++dx) {
         for (int dz = -radius; dz <= radius; ++dz) {
            if (dx * dx + dz * dz > radius * radius) {
               continue;
            }
            BlockPos pos = new BlockPos((int) Math.floor(sx) + dx, y - 1, (int) Math.floor(sz) + dz);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.getBlock() != Blocks.BEDROCK && state.getFluidState().isEmpty() && state.getDestroySpeed(level, pos) >= 0.0F) {
               level.destroyBlock(pos, false);
            }
         }
      }

      level.playSound(null, BlockPos.containing(impact), ModSounds.STORM_THUMP_LARGE, SoundSource.HOSTILE, 6.0F, 0.9F);
      level.sendParticles(ParticleTypes.BLOCK, impact.x, impact.y, impact.z, 40, 1.0, 0.5, 1.0, 0.2);

      // Slap players within reach across the face with heavy knockback.
      AABB box = new AABB(sx - 4.0, y - 3.0, sz - 4.0, sx + 4.0, y + 5.0, sz + 4.0);
      for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
         if (e == storm || e instanceof Player p && (p.isSpectator() || p.isCreative())) {
            continue;
         }
         Vec3 away = e.position().subtract(impact);
         away = away.horizontalDistanceSqr() < 0.01 ? new Vec3(level.getRandom().nextDouble() - 0.5, 0, level.getRandom().nextDouble() - 0.5) : away.normalize();
         e.setDeltaMovement(away.x * 2.4, 1.4, away.z * 2.4);
         e.fallDistance = 0.0F;
         e.hurtMarked = true;
         e.hurtServer(level, level.damageSources().mobAttack(storm), 6.0F);
      }
   }
}
