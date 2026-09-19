package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.ModParticles;
import net.dabicco.witherstormmod.client.particle.BeamMoteParticle;
import net.dabicco.witherstormmod.entity.WitherStormHeadEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class BeamMoteSpawner {
   private static final float SPAWN_CHANCE = 0.65F;
   private static final int SPAWNS_PER_TICK = 5;

   private BeamMoteSpawner() {
   }

   public static void tick(Minecraft mc) {
      if (mc.level != null && !mc.isPaused() && mc.level.tickRateManager().runsNormally()) {
         RandomSource random = mc.level.getRandom();

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof WitherStormHeadEntity head && head.isBeamActive()) {
               float beamScale = head.beamScale();
               int radius = Math.max(Math.round(net.dabicco.witherstormmod.client.ClientConfigCache.cfg.beamGroundRadius * beamScale), 1);
               Vec3 ground = head.clientBeamEnd != null ? head.clientBeamEnd : head.getBeamEndExact();

               for (int n = 0; n < 5; n++) {
                  if (!(random.nextFloat() > 0.65F)) {
                     double angle = random.nextDouble() * Math.PI * 2.0;
                     double radialFrac = Math.sqrt(random.nextDouble());
                     double axisT = 0.005 + random.nextDouble() * 0.03;
                     double climb = 0.0075 + random.nextDouble() * 0.009;
                     BeamMoteParticle.pendingHead = head;
                     BeamMoteParticle.pendingAngle = angle;
                     BeamMoteParticle.pendingRadialFrac = radialFrac;
                     BeamMoteParticle.pendingAxisT = axisT;
                     BeamMoteParticle.pendingClimbPerTick = climb;
                     BeamMoteParticle.pendingBaseRadius = radius;
                     BeamMoteParticle.pendingBeamScale = beamScale;
                     double r = net.dabicco.witherstormmod.client.TractorBeamRenderer.baseHalfWidth(radius) * radialFrac;
                     Vec3 pos = ground.add(Math.cos(angle) * r, 0.3, Math.sin(angle) * r);
                     mc.level.addParticle(ModParticles.BEAM_MOTE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
                     BeamMoteParticle.pendingHead = null;
                  }
               }
            }
         }
      }
   }
}
