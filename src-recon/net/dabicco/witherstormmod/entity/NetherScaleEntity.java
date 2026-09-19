package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NetherScaleEntity extends Entity {
   private static final EntityDataAccessor<Float> PROGRESS = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.NetherScaleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.NetherScaleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> TIP_X = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.NetherScaleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> TIP_Y = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.NetherScaleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> TIP_Z = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.NetherScaleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final int LIFE_TICKS = 160;
   private static final double CARVE_RADIUS = 1.6;
   private static final int ROOF_TOP_Y = 128;
   private static final double REACH_PER_SCALE = 3.2;
   private static final double SWEEP_LENGTH = 120.0;
   private static final double LEAN = 0.0;
   private static final int MAX_BREAKS_PER_TICK = 700;
   private static final double RAGGED_FROM = 0.8;
   private static final double THICKEN_PER_BLOCK = 0.022;
   private static final float CLUSTER_CHANCE_BURIED = 0.05F;
   private static final float CLUSTER_CHANCE_EXPOSED = 0.5F;
   private static final int MAX_CLUSTERS_PER_TICK = 6;
   private static final float DROP_CHANCE = 0.01F;
   private static final double WHACK_SPEED = 2.6;
   private static final float WHACK_DAMAGE = 8.0F;
   private static final double CURVE_AMPLITUDE = 22.0;
   private static final double DEPTH_WOBBLE = 9.0;
   private int lifeTicks;
   private double centreX;
   private double centreY;
   private double centreZ;
   private double plungeDepth = 60.0;
   private double dirX = 1.0;
   private double dirZ = 0.0;
   private double curDirX = 1.0;
   private double curDirZ = 0.0;
   private float curvePhase1;
   private float curvePhase2;
   private float curvePhase3;
   private Vec3 lastTip;

   public NetherScaleEntity(EntityType<? extends net.dabicco.witherstormmod.entity.NetherScaleEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(PROGRESS, 0.0F);
      builder.define(SCALE, 6.0F);
      builder.define(TIP_X, 0.0F);
      builder.define(TIP_Y, -1.0F);
      builder.define(TIP_Z, 0.0F);
   }

   public float getProgress() {
      return (Float)(Object)(Object)this.entityData.get(PROGRESS);
   }

   public float getScale() {
      return (Float)(Object)(Object)this.entityData.get(SCALE);
   }

   public Vec3 getTipOffset() {
      return new Vec3(
         ((Float)(Object)(Object)this.entityData.get(TIP_X)).floatValue(), ((Float)(Object)(Object)this.entityData.get(TIP_Y)).floatValue(), ((Float)(Object)(Object)this.entityData.get(TIP_Z)).floatValue()
      );
   }

   public void setup(float scale, double plungeDepth, float sweepYawDeg) {
      this.entityData.set(SCALE, scale);
      this.plungeDepth = plungeDepth;
      this.centreX = this.getX();
      this.centreY = this.getY();
      this.centreZ = this.getZ();
      double rad = Math.toRadians(sweepYawDeg);
      this.dirX = -Math.sin(rad);
      this.dirZ = Math.cos(rad);
      this.curDirX = this.dirX;
      this.curDirZ = this.dirZ;
      this.curvePhase1 = this.random.nextFloat() * (float) (Math.PI * 2);
      this.curvePhase2 = this.random.nextFloat() * (float) (Math.PI * 2);
      this.curvePhase3 = this.random.nextFloat() * (float) (Math.PI * 2);
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel server) {
         if (this.lifeTicks == 0) {
            if (this.centreY == 0.0) {
               this.centreX = this.getX();
               this.centreY = this.getY();
               this.centreZ = this.getZ();
            }

            server.playSound((Entity)null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 8.0F, 0.35F);
         }

         this.lifeTicks++;
         float p = Mth.clamp(this.lifeTicks / 160.0F, 0.0F, 1.0F);
         this.entityData.set(PROGRESS, p);
         double along = (p - 0.5) * 120.0;
         double down = Math.sin(p * Math.PI) * this.plungeDepth + Math.sin(p * 5.3 + this.curvePhase3) * 9.0 * Math.sin(p * Math.PI);
         double lat = (Math.sin(p * 3.7 + this.curvePhase1) * 0.65 + Math.sin(p * 8.1 + this.curvePhase2) * 0.35) * 22.0 * Math.sin(p * Math.PI);
         double perpX = -this.dirZ;
         double perpZ = this.dirX;
         double prevX = this.getX();
         double prevZ = this.getZ();
         this.setPos(this.centreX + this.dirX * along + perpX * lat, this.centreY - down, this.centreZ + this.dirZ * along + perpZ * lat);
         double mvX = this.getX() - prevX;
         double mvZ = this.getZ() - prevZ;
         double mvLen = Math.sqrt(mvX * mvX + mvZ * mvZ);
         if (mvLen > 1.0E-4) {
            this.curDirX = mvX / mvLen;
            this.curDirZ = mvZ / mvLen;
         }

         double reach = this.getScale() * 3.2;
         Vec3 aim = new Vec3(this.dirX * 0.0, -1.0, this.dirZ * 0.0).normalize().scale(reach);
         this.entityData.set(TIP_X, (float)aim.x);
         this.entityData.set(TIP_Y, (float)aim.y);
         this.entityData.set(TIP_Z, (float)aim.z);
         this.carveLimb(server, this.position(), this.position().add(aim));
         if (this.lifeTicks >= 160) {
            this.discard();
         }
      }
   }

   private void carveLimb(ServerLevel server, Vec3 anchor, Vec3 tip) {
      Set<Long> done = new HashSet<>();
      int broken = 0;
      MutableBlockPos pos = new MutableBlockPos();
      List<Vec3> columns = new ArrayList<>();
      columns.add(tip);
      if (this.lastTip != null) {
         int gap = Mth.ceil(this.lastTip.distanceTo(tip));

         for (int i = 1; i < gap; i++) {
            columns.add(this.lastTip.lerp(tip, (double)i / gap));
         }
      }

      this.lastTip = tip;
      int topY = Math.min(128, server.getMaxY());
      int clusters = 0;

      for (Vec3 c : columns) {
         if (broken >= 700) {
            break;
         }

         int cx = Mth.floor(c.x);
         int cz = Mth.floor(c.z);
         int botY = Math.max(Mth.floor(c.y), server.getMinY());

         for (int y = topY; y >= botY && broken < 700; y--) {
            double radius = 1.6 + Math.max(0, 128 - y) * 0.022;
            double rSqr = radius * radius;
            int r = Mth.ceil(radius);

            for (int dx = -r; dx <= r && broken < 700; dx++) {
               for (int dz = -r; dz <= r && broken < 700; dz++) {
                  double dSqr = dx * dx + dz * dz;
                  if (!(dSqr > rSqr)) {
                     double frac = Math.sqrt(dSqr) / radius;
                     if (frac > 0.8) {
                        double survive = (frac - 0.8) / 0.19999999999999996;
                        if (this.random.nextDouble() < survive) {
                           continue;
                        }
                     }

                     pos.set(cx + dx, y, cz + dz);
                     if (done.add(pos.asLong())) {
                        int[] clusterBudget = new int[]{clusters};
                        if (this.takeBlock(server, pos, clusterBudget)) {
                           broken++;
                        }

                        clusters = clusterBudget[0];
                     }
                  }
               }
            }
         }
      }

      this.whackEntities(server, columns, topY, tip.y);
      server.sendParticles(ParticleTypes.EXPLOSION, tip.x, tip.y, tip.z, 3, 1.2, 1.2, 1.2, 0.0);
      server.sendParticles(ParticleTypes.LARGE_SMOKE, tip.x, tip.y, tip.z, 14, 1.5, 2.0, 1.5, 0.02);
   }

   private boolean takeBlock(ServerLevel server, BlockPos pos, int[] clusterBudget) {
      BlockState state = server.getBlockState(pos);
      if (state.isAir()) {
         return false;
      } else if (state.is(Blocks.BEDROCK)) {
         return false;
      } else if (!state.is(Blocks.NETHER_PORTAL) && !state.is(Blocks.END_PORTAL) && !state.is(Blocks.END_PORTAL_FRAME)) {
         float clusterChance = this.touchingAir(server, pos) ? 0.5F : 0.05F;
         if (clusterBudget[0] < 6 && this.random.nextFloat() < clusterChance && this.flingCluster(server, pos)) {
            clusterBudget[0]++;
            return true;
         } else {
            server.destroyBlock(pos, this.random.nextFloat() < 0.01F);
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean touchingAir(ServerLevel server, BlockPos pos) {
      for (Direction d : Direction.values()) {
         if (server.getBlockState(pos.relative(d)).isAir()) {
            return true;
         }
      }

      return false;
   }

   private boolean flingCluster(ServerLevel server, BlockPos pos) {
      WitherStormClusterEntity cluster = new WitherStormClusterEntity(net.dabicco.witherstormmod.entity.ModEntityTypes.WITHER_STORM_CLUSTER, server);
      cluster.setRadius(0);
      cluster.setOrigin(pos);
      cluster.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      server.addFreshEntity(cluster);
      cluster.absorbBlocks(pos);
      cluster.launchAsDebris(
         new Vec3(
            this.curDirX * (0.8 + this.random.nextDouble() * 0.7),
            0.35 + this.random.nextDouble() * 0.45,
            this.curDirZ * (0.8 + this.random.nextDouble() * 0.7)
         )
      );
      WitherStormClusterEntity.syncBlocksToTracking(cluster);
      return true;
   }

   private void whackEntities(ServerLevel server, List<Vec3> columns, int topY, double tipY) {
      if (!columns.isEmpty()) {
         double pad = 4.1;
         double minX = Double.MAX_VALUE;
         double maxX = -Double.MAX_VALUE;
         double minZ = Double.MAX_VALUE;
         double maxZ = -Double.MAX_VALUE;

         for (Vec3 c : columns) {
            minX = Math.min(minX, c.x);
            maxX = Math.max(maxX, c.x);
            minZ = Math.min(minZ, c.z);
            maxZ = Math.max(maxZ, c.z);
         }

         AABB box = new AABB(minX - pad, tipY - pad, minZ - pad, maxX + pad, topY + pad, maxZ + pad);
         double padSqr = pad * pad;

         for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && !e.isSpectator())) {
            boolean caught = false;

            for (Vec3 c : columns) {
               double dx = victim.getX() - c.x;
               double dz = victim.getZ() - c.z;
               if (dx * dx + dz * dz <= padSqr) {
                  caught = true;
                  break;
               }
            }

            if (caught) {
               victim.hurtServer(server, server.damageSources().fallingBlock(this), 8.0F);
               victim.setDeltaMovement(this.curDirX * 2.6, 0.9, this.curDirZ * 2.6);
               victim.hurtMarked = true;
            }
         }
      }
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.lifeTicks = input.getIntOr("LifeTicks", 0);
      this.centreX = input.getDoubleOr("CentreX", this.getX());
      this.centreY = input.getDoubleOr("CentreY", this.getY());
      this.centreZ = input.getDoubleOr("CentreZ", this.getZ());
      this.plungeDepth = input.getDoubleOr("PlungeDepth", 60.0);
      this.dirX = input.getDoubleOr("DirX", 1.0);
      this.dirZ = input.getDoubleOr("DirZ", 0.0);
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putInt("LifeTicks", this.lifeTicks);
      output.putDouble("CentreX", this.centreX);
      output.putDouble("CentreY", this.centreY);
      output.putDouble("CentreZ", this.centreZ);
      output.putDouble("PlungeDepth", this.plungeDepth);
      output.putDouble("DirX", this.dirX);
      output.putDouble("DirZ", this.dirZ);
   }
}
