package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.dabicco.witherstormmod.ClusterBlocksPayload;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlackHoleEntity extends Entity {
   private static final float MASS_PER_ITEM = 0.05F;
   private static final float MASS_PER_CLUSTER_BLOCK = 0.35F;
   private static final float SUPERMASSIVE_THRESHOLD = 150.0F;
   private static final float GROWTH_RATE = 0.6F;
   private static final float CARVE_THRESHOLD = 8.0F;
   private static final double CARVE_MARGIN = (double)2.5F;
   private static final float CONTACT_CARVE_MASS_MULT = 0.04F;
   private static final double RADIUS_EXP = 0.4;
   private static final double RADIUS_SCALE = (double)0.5F;
   private static final double MAX_SCAN_RADIUS = (double)4096.0F;
   private static final float HORIZON_DAMAGE = 3.0F;
   private static final float SINGULARITY_FRACTION = 0.35F;
   private static final EntityDataAccessor<Float> MASS;
   private float mass = 1.0F;
   private final Set<Entity> pullTargets = new HashSet();
   private int rescanTimer = 0;
   private int clusterClaimTimer = 0;
   private int clusterSpawnCooldown = 0;
   private int contactCarveTimer = 0;
   private int ambientSoundTimer = 0;

   public BlackHoleEntity(EntityType<? extends BlackHoleEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(MASS, 1.0F);
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return true;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      return false;
   }

   protected AABB makeBoundingBox(Vec3 pos) {
      float r = Math.max(this.getRadius(), 0.25F);
      return new AABB(pos.x - (double)r, pos.y - (double)r, pos.z - (double)r, pos.x + (double)r, pos.y + (double)r, pos.z + (double)r);
   }

   public Vec3 getCenter() {
      return this.position();
   }

   public float getMass() {
      return (Float)this.entityData.get(MASS);
   }

   public float getRadius() {
      return (float)(Math.pow((double)this.getMass(), 0.4) * (double)0.5F);
   }

   public double getCarveRadius() {
      return (double)this.getRadius() + (double)2.5F;
   }

   public float getPullRadius() {
      return Float.MAX_VALUE;
   }

   public float getClusterClaimRadius() {
      return this.getRadius() * 3.5F + 6.0F;
   }

   public float getIntensity() {
      return Mth.clamp(this.getMass() / 150.0F, 0.0F, 1.0F);
   }

   public boolean isSupermassive() {
      return this.getMass() >= 150.0F;
   }

   private void addMass(float amount) {
      this.mass += amount * 0.6F;
      this.entityData.set(MASS, this.mass);
      this.setBoundingBox(this.makeBoundingBox(this.position()));
   }

   public void setMass(float value) {
      this.mass = Math.max(0.1F, value);
      this.entityData.set(MASS, this.mass);
      this.setBoundingBox(this.makeBoundingBox(this.position()));
   }

   private int maxClusterRadiusForMass() {
      float m = this.getMass();
      if (m < 15.0F) {
         return 0;
      } else if (m < 60.0F) {
         return 1;
      } else if (m < 180.0F) {
         return 2;
      } else {
         return m < 450.0F ? 3 : 4;
      }
   }

   private int clusterRadiusAt(double distance) {
      int max = this.maxClusterRadiusForMass();
      if (max <= 0) {
         return 0;
      } else {
         double inner = this.getCarveRadius();
         double outer = (double)this.getClusterClaimRadius();
         if (outer <= inner) {
            return max;
         } else {
            double t = (double)1.0F - Mth.clamp((distance - inner) / (outer - inner), (double)0.0F, (double)1.0F);
            double closeness = t * t;
            int result = 0;

            for(int i = 1; i <= max && this.random.nextDouble() < closeness; result = i++) {
            }

            return result;
         }
      }
   }

   private int clusterBudget() {
      return 2 + (int)(Math.sqrt((double)this.getMass()) * 0.9);
   }

   private int clusterClaimInterval() {
      return Math.max(1, (int)((double)30.0F / ((double)1.0F + (double)this.getMass() * 0.02)));
   }

   private int clusterSpawnInterval() {
      return Math.max(2, (int)((double)40.0F / ((double)1.0F + (double)this.getMass() * 0.015)));
   }

   private boolean isImmune(Entity e) {
      if (!(e instanceof Player p)) {
         return false;
      } else {
         return p.isCreative() || p.isSpectator();
      }
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         this.mass = (Float)this.entityData.get(MASS);
         this.spawnAccretionParticles();
      } else {
         double radius = (double)this.getRadius();
         double pull = (double)this.getPullRadius();
         Vec3 center = this.getCenter();
         if (--this.contactCarveTimer <= 0) {
            this.contactCarveTimer = 5;
            this.carveContactSphere(center, this.getCarveRadius());
         }

         if (this.getMass() >= 8.0F && --this.clusterSpawnCooldown <= 0) {
            this.clusterSpawnCooldown = this.clusterSpawnInterval();
            this.spawnBlockCluster();
         }

         if (--this.clusterClaimTimer <= 0) {
            this.clusterClaimTimer = this.clusterClaimInterval();
            this.claimClusters(center, (double)this.getClusterClaimRadius());
         }

         if (--this.rescanTimer <= 0) {
            this.rescanTimer = 2;
            this.rescanTargets(center, pull);
         }

         if (--this.ambientSoundTimer <= 0) {
            this.ambientSoundTimer = 60 + this.random.nextInt(40);
            this.playAmbient();
         }

         Iterator<Entity> it = this.pullTargets.iterator();

         while(it.hasNext()) {
            Entity e = (Entity)it.next();
            if (e.isAlive() && e.level() == this.level() && !this.isImmune(e)) {
               double d = this.distanceToCenter(e, center);
               double leash = e instanceof WitherStormClusterEntity ? (double)this.getClusterClaimRadius() * (double)1.5F : Double.MAX_VALUE;
               if (d > leash) {
                  this.release(e);
                  it.remove();
               } else if (this.applyDamage(e, d, radius)) {
                  it.remove();
               } else if (this.tryConsume(e, d, radius)) {
                  it.remove();
               } else {
                  this.applyPull(e, center, d, radius, pull, this.strengthFor(e));
               }
            } else {
               this.release(e);
               it.remove();
            }
         }

      }
   }

   private double distanceToCenter(Entity e, Vec3 center) {
      return e.getBoundingBox().getCenter().distanceTo(center);
   }

   private AABB scanBox(Vec3 center, double r) {
      double capped = Math.min(r, (double)4096.0F);
      return (new AABB(center, center)).inflate(capped);
   }

   private void carveContactSphere(Vec3 c, double r) {
      Level var5 = this.level();
      if (var5 instanceof ServerLevel server) {
         int var25 = Mth.floor(c.x - r);
         int maxX = Mth.floor(c.x + r);
         int minY = Mth.floor(c.y - r);
         int maxY = Mth.floor(c.y + r);
         int minZ = Mth.floor(c.z - r);
         int maxZ = Mth.floor(c.z + r);
         double rSq = r * r;
         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
         ArrayList found = new ArrayList();

         for(int x = var25; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
               for(int z = minZ; z <= maxZ; ++z) {
                  double dx = (double)x + (double)0.5F - c.x;
                  double dy = (double)y + (double)0.5F - c.y;
                  double dz = (double)z + (double)0.5F - c.z;
                  if (!(dx * dx + dy * dy + dz * dz > rSq)) {
                     pos.set(x, y, z);
                     BlockState state = this.level().getBlockState(pos);
                     if (!state.isAir() && state.getFluidState().isEmpty() && !(state.getDestroySpeed(this.level(), pos) < 0.0F)) {
                        found.add(pos.immutable());
                     }
                  }
               }
            }
         }

         if (!found.isEmpty()) {
            int budget = 64 + (int)(rSq * (double)4.0F);
            if (found.size() > budget) {
               Collections.shuffle(found, new Random(this.random.nextLong()));
            }

            int eaten = Math.min(found.size(), budget);

            for(int i = 0; i < eaten; ++i) {
               this.level().removeBlock((BlockPos)found.get(i), false);
               this.addMass(0.0139999995F);
            }

            server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 0.6F + Math.min((float)eaten / 40.0F, 0.8F), 0.4F);
         }
      }
   }

   private void spawnBlockCluster() {
      BlockPos target = this.findSurfaceBlock();
      if (target != null) {
         WitherStormClusterEntity cluster = new WitherStormClusterEntity(ModEntityTypes.WITHER_STORM_CLUSTER, this.level());
         cluster.setOrigin(target);
         double dist = Math.sqrt(target.distToCenterSqr(this.getCenter()));
         int radius = this.clusterRadiusAt(dist);
         cluster.setRadius(radius);
         BlockPos spawnPos = WitherStormClusterEntity.adjustSpawnOrigin(target, radius);
         cluster.setPos((double)spawnPos.getX() + (double)0.5F, (double)spawnPos.getY() + (double)0.5F, (double)spawnPos.getZ() + (double)0.5F);
         cluster.absorbBlocks(target);
         cluster.setTargetStorm((WitherStormEntity)null);
         cluster.setTargetHole(this);
         this.level().addFreshEntity(cluster);
         this.pullTargets.add(cluster);
         if (!cluster.getBlocks().isEmpty()) {
            List<Integer> stateIds = new ArrayList();

            for(BlockState state : cluster.getBlocks()) {
               stateIds.add(Block.getId(state));
            }

            ClusterBlocksPayload payload = new ClusterBlocksPayload(cluster.getId(), stateIds, cluster.getBlockOffsets());

            for(ServerPlayer player : PlayerLookup.tracking(cluster)) {
               ServerPlayNetworking.send(player, payload);
            }
         }

      }
   }

   private BlockPos findSurfaceBlock() {
      BlockPos holePos = this.blockPosition();
      double reach = Math.max((double)1.5F, Math.min((double)this.getClusterClaimRadius(), (double)4096.0F));
      double reachSq = reach * reach;
      double innerSq = this.getCarveRadius() * this.getCarveRadius();
      Vec3 c = this.getCenter();

      for(int i = 0; i < 8; ++i) {
         double angle = this.random.nextDouble() * Math.PI * (double)2.0F;
         double dist = Math.pow(this.random.nextDouble(), (double)1.5F) * reach;
         int x = holePos.getX() + (int)Math.round(Math.cos(angle) * dist);
         int z = holePos.getZ() + (int)Math.round(Math.sin(angle) * dist);
         BlockPos surfacePos = this.level().getHeightmapPos(Types.MOTION_BLOCKING, new BlockPos(x, 0, z)).below();
         double dSq = surfacePos.distToCenterSqr(c);
         if (!(dSq > reachSq) && !(dSq < innerSq)) {
            BlockState state = this.level().getBlockState(surfacePos);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
               return surfacePos;
            }
         }
      }

      return null;
   }

   private void rescanTargets(Vec3 center, double pull) {
      AABB box = this.scanBox(center, pull);

      for(Entity e : this.level().getEntities(this, box)) {
         if (e.isAlive() && !this.isImmune(e) && !this.pullTargets.contains(e) && !(e instanceof BlackHoleEntity) && !(e instanceof WitherStormClusterEntity) && !(e instanceof WitherStormEntity)) {
            if (e instanceof ItemEntity) {
               ItemEntity item = (ItemEntity)e;
               item.setNoGravity(true);
               this.pullTargets.add(item);
            } else if (e instanceof LivingEntity) {
               this.pullTargets.add(e);
            }
         }
      }

   }

   private void claimClusters(Vec3 center, double claimRadius) {
      int budget = this.clusterBudget();
      int maxSize = this.maxClusterRadiusForMass() + 1;

      for(WitherStormClusterEntity cluster : this.level().getEntitiesOfClass(WitherStormClusterEntity.class, this.scanBox(center, claimRadius))) {
         if (budget <= 0) {
            break;
         }

         if (cluster.isAlive() && !this.pullTargets.contains(cluster) && cluster.getClusterState() != WitherStormClusterEntity.ClusterState.SHAKING && cluster.getRadius() <= maxSize && !(this.distanceToCenter(cluster, center) > claimRadius)) {
            --budget;
            cluster.setTargetStorm((WitherStormEntity)null);
            cluster.setTargetHole(this);
            cluster.setDeltaMovement(Vec3.ZERO);
            this.pullTargets.add(cluster);
         }
      }

   }

   private void release(Entity e) {
      if (e instanceof ItemEntity item) {
         item.setNoGravity(false);
      }

      if (e instanceof WitherStormClusterEntity cluster) {
         cluster.setTargetHole((Entity)null);
      }

   }

   private boolean applyDamage(Entity e, double distance, double radius) {
      if (e instanceof LivingEntity living) {
         Level var8 = this.level();
         if (var8 instanceof ServerLevel server) {
            if (distance < radius * (double)0.35F) {
               this.devourAndKill(server, living, Float.MAX_VALUE);
               return !living.isAlive();
            } else if (distance < radius) {
               float depth = 1.0F - (float)(distance / radius);
               living.invulnerableTime = 0;
               this.devourAndKill(server, living, 3.0F * (0.5F + depth));
               return !living.isAlive();
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void devourAndKill(ServerLevel server, LivingEntity living, float damage) {
      living.hurtServer(server, this.damageSources().fellOutOfWorld(), damage);
      if (!living.isAlive()) {
         Vec3 c = this.getCenter();
         if (living instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)living;
            sp.connection.teleport(c.x, c.y, c.z, sp.getYRot(), sp.getXRot());
         } else {
            living.teleportTo(c.x, c.y, c.z);
         }

         living.setDeltaMovement(Vec3.ZERO);
         this.addMass(1.0F + living.getMaxHealth() * 0.1F);
         this.onDevour(living);
      }

   }

   private void onDevour(LivingEntity victim) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel server) {
         server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.7F, 0.5F + this.random.nextFloat() * 0.2F);
      }
   }

   private boolean tryConsume(Entity e, double distance, double radius) {
      if (e instanceof ItemEntity item) {
         if (distance < radius * (double)0.5F + (double)1.0F) {
            this.addMass(0.05F * (float)item.getItem().getCount());
            item.discard();
            return true;
         } else {
            return false;
         }
      } else if (e instanceof WitherStormClusterEntity cluster) {
         if (distance < radius * 0.15 + 0.4) {
            this.addMass(0.35F * (float)cluster.getBlockCount());
            this.onSwallowCluster(cluster);
            cluster.discard();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void onSwallowCluster(WitherStormClusterEntity cluster) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel server) {
         int blocks = cluster.getBlockCount();
         if (blocks > 0) {
            server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, Math.min(0.3F + (float)blocks * 0.02F, 1.2F), 1.6F - Math.min((float)blocks * 0.01F, 0.8F));
         }
      }
   }

   private void playAmbient() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.5F + this.getIntensity() * 1.5F, 0.9F - this.getIntensity() * 0.5F);
      }
   }

   private void spawnAccretionParticles() {
      float r = this.getRadius();
      float pull = this.getPullRadius();
      int count = 1 + (int)(this.getIntensity() * 4.0F);
      double inner = (double)r * 1.6;
      double band = Math.max((double)pull - inner, (double)2.0F);

      for(int i = 0; i < count; ++i) {
         double theta = this.random.nextDouble() * Math.PI * (double)2.0F;
         double phi = Math.acos((double)2.0F * this.random.nextDouble() - (double)1.0F);
         double d = inner + this.random.nextDouble() * band;
         double px = this.getX() + Math.sin(phi) * Math.cos(theta) * d;
         double py = this.getY() + Math.cos(phi) * d;
         double pz = this.getZ() + Math.sin(phi) * Math.sin(theta) * d;
         Vec3 inward = (new Vec3(this.getX() - px, this.getY() - py, this.getZ() - pz)).normalize();
         Vec3 tangent = inward.cross(new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
         if (tangent.lengthSqr() < 1.0E-4) {
            tangent = new Vec3((double)1.0F, (double)0.0F, (double)0.0F);
         }

         tangent = tangent.normalize();
         Vec3 vel = inward.scale(0.08).add(tangent.scale(0.12));
         this.level().addParticle(this.random.nextInt(4) == 0 ? ParticleTypes.PORTAL : ParticleTypes.SMOKE, px, py, pz, vel.x, vel.y, vel.z);
      }

   }

   private double strengthFor(Entity e) {
      if (e instanceof ItemEntity) {
         return (double)3.0F;
      } else {
         return e instanceof WitherStormClusterEntity ? 0.35 + (double)this.getIntensity() * 0.6 : 0.6 * (double)(0.5F + this.getIntensity());
      }
   }

   private void applyPull(Entity target, Vec3 center, double distance, double radius, double pullRadius, double strength) {
      Vec3 toCenter = center.subtract(target.getBoundingBox().getCenter());
      if (!(toCenter.lengthSqr() < 1.0E-6)) {
         Vec3 dir = toCenter.normalize();
         double softenedDistSq = distance * distance + radius * radius;
         double accel = strength * (double)this.getMass() * 0.1 / (distance * distance + radius * radius);
         Vec3 pullVec = dir.scale(accel);
         Vec3 velocity = target.getDeltaMovement().add(pullVec);
         double maxSpeed = (double)8.0F;
         if (velocity.length() > maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
         }

         target.setDeltaMovement(velocity);
         target.hurtMarked = true;
      }
   }

   public void remove(Entity.RemovalReason reason) {
      for(Entity e : this.pullTargets) {
         this.release(e);
      }

      this.pullTargets.clear();
      super.remove(reason);
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putFloat("Mass", this.mass);
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.mass = input.getFloatOr("Mass", 1.0F);
      this.entityData.set(MASS, this.mass);
      this.setBoundingBox(this.makeBoundingBox(this.position()));
   }

   static {
      MASS = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);
   }
}
