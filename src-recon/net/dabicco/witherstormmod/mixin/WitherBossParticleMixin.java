package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({WitherBoss.class})
public abstract class WitherBossParticleMixin {
   @Redirect(
      method = {"aiStep"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
      )
   )
   private void dabyws$spreadStormParticles(Level level, ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {
      if (((Object)this) instanceof WitherStormEntity storm && storm.isPhase4()) {
         AABB box = storm.getBoundingBox();
         RandomSource random = level.getRandom();
         x = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
         y = Mth.lerp(random.nextDouble(), box.minY, box.maxY);
         z = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);
      }

      level.addParticle(options, x, y, z, vx, vy, vz);
   }
}
