package net.dabicco.witherstormmod.client.particle;

import net.dabicco.witherstormmod.client.TractorBeamRenderer;
import net.dabicco.witherstormmod.entity.WitherStormHeadEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle.Layer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class BeamMoteParticle extends SingleQuadParticle {
   public static WitherStormHeadEntity pendingHead;
   public static double pendingAngle;
   public static double pendingRadialFrac;
   public static double pendingAxisT;
   public static double pendingClimbPerTick;
   public static double pendingBaseRadius;
   public static float pendingBeamScale = 1.0F;
   private final WitherStormHeadEntity head;
   private final double angle;
   private final double radialFrac;
   private final double climbPerTick;
   private final double baseRadius;
   private final float beamScale;
   private double axisT;
   private final float baseSize;
   private int detachedTicks = -1;

   private BeamMoteParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
      super(level, x, y, z, sprite);
      this.head = pendingHead;
      this.angle = pendingAngle;
      this.radialFrac = pendingRadialFrac;
      this.axisT = pendingAxisT;
      this.climbPerTick = pendingClimbPerTick;
      this.baseRadius = pendingBaseRadius;
      this.beamScale = pendingBeamScale;
      pendingHead = null;
      pendingBeamScale = 1.0F;
      this.xd = (double)0.0F;
      this.yd = (double)0.0F;
      this.zd = (double)0.0F;
      this.friction = 1.0F;
      this.gravity = 0.0F;
      this.hasPhysics = false;
      this.lifetime = 400;
      this.baseSize = (0.055F + this.random.nextFloat() * 0.06F) * Math.max(this.beamScale, 0.28F);
      this.quadSize = this.baseSize;
      this.rCol = 0.62F;
      this.gCol = 0.28F;
      this.bCol = 0.95F;
      this.alpha = 0.0F;
   }

   public SingleQuadParticle.Layer getLayer() {
      return Layer.TRANSLUCENT;
   }

   public int getLightCoords(float partialTick) {
      return 15728880;
   }

   private boolean beamStillThere() {
      if (this.head != null && this.head.isAlive() && this.head.isBeamActive()) {
         return this.level.getEntity(this.head.getId()) == this.head;
      } else {
         return false;
      }
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         Vec3 ground = this.beamStillThere() ? (this.head.clientBeamEnd != null ? this.head.clientBeamEnd : this.head.getBeamEndExact()) : null;
         if (ground == null) {
            if (this.detachedTicks < 0) {
               this.detachedTicks = 0;
            }

            this.alpha = Math.max(0.0F, this.alpha - 0.15F);
            if (++this.detachedTicks > 8) {
               this.remove();
            }

         } else {
            this.detachedTicks = -1;
            this.axisT = Math.min(this.axisT + this.climbPerTick, (double)1.0F);
            if (this.axisT >= 0.92) {
               this.remove();
            } else {
               Vec3 published = TractorBeamRenderer.eyeWorld(this.head.getId());
               Vec3 tip = published != null ? published : this.head.position();
               Vec3 axis = tip.subtract(ground);
               if (!(axis.lengthSqr() < 1.0E-4)) {
                  Vec3 d = axis.normalize();
                  Vec3 up = Math.abs(d.y) > 0.98 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
                  Vec3 right = d.cross(up).normalize();
                  Vec3 upB = right.cross(d);
                  double radius = Mth.lerp(this.axisT, (double)TractorBeamRenderer.baseHalfWidth((float)this.baseRadius), (double)(0.3F * this.beamScale)) * this.radialFrac;
                  Vec3 p = ground.add(axis.scale(this.axisT)).add(right.scale(Math.cos(this.angle) * radius)).add(upB.scale(Math.sin(this.angle) * radius));
                  this.setPos(p.x, p.y, p.z);
                  float in = Math.min((float)this.age / 5.0F, 1.0F);
                  float out = this.axisT < 0.62 ? 1.0F : (float)((double)1.0F - (this.axisT - 0.62) / 0.3);
                  this.alpha = 0.9F * in * Math.max(out, 0.0F);
                  this.quadSize = this.baseSize * (1.0F - 0.3F * (float)this.axisT);
               }
            }
         }
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
         return new BeamMoteParticle(level, x, y, z, this.sprites.get(random));
      }
   }
}
