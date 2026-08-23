package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.ModSounds;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.WitherStormHeadEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class BeamHumSound extends AbstractTickableSoundInstance {
   private int outsideTicks = 0;

   public BeamHumSound() {
      super(ModSounds.HEAD_BEAM_LOOP, SoundSource.AMBIENT, RandomSource.create());
      this.looping = true;
      this.delay = 0;
      this.volume = 0.25F;
      this.attenuation = Attenuation.NONE;
      this.relative = false;
   }

   public void tick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && DevouringStormsClientConfig.beamHum) {
         float nearness = beamNearness(mc);
         if (nearness > 0.0F) {
            this.outsideTicks = 0;
         } else if (++this.outsideTicks > 30) {
            this.stop();
            return;
         }

         Vec3 src = nearestBeamEnd(mc);
         if (src != null) {
            this.x = src.x;
            this.y = src.y;
            this.z = src.z;
         }

         float target = 0.45F * nearness;
         this.volume += (target - this.volume) * 0.12F;
      } else {
         this.stop();
      }
   }

   private static Vec3 nearestBeamEnd(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         Vec3 p = mc.player.position();
         Vec3 best = null;
         double bestDist = Double.MAX_VALUE;

         for(Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof WitherStormHeadEntity) {
               WitherStormHeadEntity head = (WitherStormHeadEntity)entity;
               if (head.isBeamActive()) {
                  Vec3 end = head.getBeamEndExact();
                  if (end != null) {
                     double dd = p.distanceToSqr(end);
                     if (dd < bestDist) {
                        bestDist = dd;
                        best = end;
                     }
                  }
               }
            }
         }

         for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
            for(int i = 0; i < 3; ++i) {
               if (d.beamActive[i] && d.beamEnd[i] != null) {
                  double dd = p.distanceToSqr(d.beamEnd[i]);
                  if (dd < bestDist) {
                     bestDist = dd;
                     best = d.beamEnd[i];
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static float beamNearness(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         if (isPlayerInAnyBeam(mc)) {
            return 1.0F;
         } else {
            double range = DevouringStormsClientConfig.beamHumRange;
            if (range <= (double)0.0F) {
               return 0.0F;
            } else {
               Vec3 p = mc.player.position();
               double best = Double.MAX_VALUE;

               for(Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof WitherStormHeadEntity) {
                     WitherStormHeadEntity head = (WitherStormHeadEntity)entity;
                     if (head.isBeamActive()) {
                        Vec3 end = head.getBeamEndExact();
                        if (end != null) {
                           best = Math.min(best, p.distanceTo(end));
                        }
                     }
                  }
               }

               for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                  for(int i = 0; i < 3; ++i) {
                     if (d.beamActive[i] && d.beamEnd[i] != null) {
                        best = Math.min(best, p.distanceTo(d.beamEnd[i]));
                     }
                  }
               }

               if (best != Double.MAX_VALUE && !(best > range)) {
                  float t = (float)((double)1.0F - best / range);
                  return t * t * (3.0F - 2.0F * t);
               } else {
                  return 0.0F;
               }
            }
         }
      } else {
         return 0.0F;
      }
   }

   public static boolean isPlayerInAnyBeam(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         Vec3 p = mc.player.position();
         int radius = ClientConfigCache.cfg.beamGroundRadius + 2;

         for(Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof WitherStormHeadEntity) {
               WitherStormHeadEntity head = (WitherStormHeadEntity)entity;
               if (head.isBeamActive() && nearBeamSegment(p, head.position(), head.getBeamEndExact(), (double)radius)) {
                  return true;
               }
            }
         }

         for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
            for(int i = 0; i < 3; ++i) {
               if (d.beamActive[i] && d.beamEnd[i] != null && p.distanceTo(d.beamEnd[i]) < (double)(radius + 3)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean nearBeamSegment(Vec3 p, Vec3 head, Vec3 ground, double radius) {
      Vec3 axis = head.subtract(ground);
      double len = axis.length();
      if (len < (double)1.0F) {
         return false;
      } else {
         Vec3 axisN = axis.scale((double)1.0F / len);
         Vec3 rel = p.subtract(ground);
         double along = rel.dot(axisN);
         if (!(along < (double)-2.0F) && !(along > len)) {
            double offAxis = rel.subtract(axisN.scale(along)).length();
            return offAxis < radius + (double)1.0F;
         } else {
            return false;
         }
      }
   }
}
