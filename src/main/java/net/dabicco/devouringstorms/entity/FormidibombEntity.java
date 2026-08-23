package net.dabicco.devouringstorms.entity;

import java.util.HashSet;
import java.util.Set;
import net.dabicco.devouringstorms.ModSounds;
import net.dabicco.devouringstorms.config.WitherStormConfigs;
import net.dabicco.devouringstorms.network.FormidibombFlashPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FormidibombEntity extends Entity {
   public static final int MORPH_AT = 50;
   public static final int LIFETIME = 600;
   public static final float POWER = 22.0F;
   private static final EntityDataAccessor<Integer> DATA_TICKS;
   private static final EntityDataAccessor<Boolean> DATA_SUCKING;
   private static final int SUCK_START = 300;
   private static final int SUCK_LOCK_ON = 20;
   private static final int SUCK_ARRIVE = 580;
   private static final double SUCK_DIST = (double)10.0F;
   private final Set<ChunkPos> forcedByUs;
   private ChunkPos forcedOrigin;
   private WitherStormHeadEntity suckHead;
   private Vec3 suckStartPos;
   private int suckLockTick;
   private int suckStartTick;
   private Vec3 suckDir;
   public static final int BLAST_RADIUS = 30;

   public FormidibombEntity(EntityType<? extends FormidibombEntity> type, Level level) {
      super(type, level);
      this.forcedByUs = new HashSet();
      this.suckLockTick = -1;
      this.suckStartTick = -1;
      this.blocksBuilding = true;
   }

   public FormidibombEntity(Level level, double x, double y, double z) {
      this(ModEntityTypes.FORMIDIBOMB, level);
      this.setPos(x, y, z);
      this.xo = x;
      this.yo = y;
      this.zo = z;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(DATA_TICKS, 0);
      builder.define(DATA_SUCKING, false);
   }

   public int getTicks() {
      return (Integer)this.entityData.get(DATA_TICKS);
   }

   private void setTicks(int t) {
      this.entityData.set(DATA_TICKS, t);
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public boolean isAlwaysTicking() {
      return true;
   }

   protected double getDefaultGravity() {
      return 0.04;
   }

   public boolean isCurrentlyGlowing() {
      return false;
   }

   public void tick() {
      int t = this.getTicks() + 1;
      boolean sucking;
      if (this.level().isClientSide()) {
         sucking = (Boolean)this.entityData.get(DATA_SUCKING);
      } else {
         sucking = this.tickSuck(t);
         this.entityData.set(DATA_SUCKING, sucking);
      }

      if (!sucking) {
         this.applyGravity();
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
         if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, (double)-0.5F, 0.7));
         }
      }

      this.setTicks(t);
      if (!this.level().isClientSide()) {
         this.updateForcedChunks();
         if (t == 1) {
            this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), ModSounds.FORMIDIBOMB_CREATION, SoundSource.BLOCKS, 4.0F, 1.0F);
         }

         if (t == 50) {
            this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.4F, 0.6F);
         }

         if (t >= 600) {
            this.discard();
            this.explode();
         }

      } else {
         if (t < 50 && t % 2 == 0) {
            this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY() + (double)0.5F, this.getZ(), (this.random.nextDouble() - (double)0.5F) * 0.3, 0.1, (this.random.nextDouble() - (double)0.5F) * 0.3);
         } else if (t >= 50) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6, this.getZ(), (double)0.0F, 0.02, (double)0.0F);
         }

      }
   }

   private void updateForcedChunks() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         if (this.suckHead != null) {
            this.releaseForcedChunks();
         } else {
            ChunkPos here = this.chunkPosition();
            if (this.forcedOrigin != null && !this.forcedOrigin.equals(here)) {
               this.releaseForcedChunks();
            } else if (this.forcedOrigin == null) {
               this.forcedOrigin = here;

               for(int x = -2; x <= 2; ++x) {
                  for(int z = -2; z <= 2; ++z) {
                     ChunkPos pos = new ChunkPos(here.x() + x, here.z() + z);
                     if (this.forcedByUs.add(pos)) {
                        ChunkForceRegistry.acquire(server, this.getUUID(), pos);
                     }
                  }
               }

            }
         }
      }
   }

   private void releaseForcedChunks() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         ChunkForceRegistry.releaseAll(server, this.getUUID(), this.forcedByUs);
         this.forcedByUs.clear();
         this.forcedOrigin = null;
      }
   }

   public void remove(Entity.RemovalReason reason) {
      this.releaseForcedChunks();
      super.remove(reason);
   }

   private boolean tickSuck(int t) {
      Level var3 = this.level();
      if (!(var3 instanceof ServerLevel server)) {
         return false;
      } else {
         if (this.suckHead == null) {
            if (t < 300) {
               return false;
            }

            double range = WitherStormConfigs.get(server).headTargetRange;
            double bestSq = range * range;

            for(WitherStormEntity ws : server.getEntitiesOfClass(WitherStormEntity.class, this.getBoundingBox().inflate(range), Entity::isAlive)) {
               WitherStormHeadEntity mh = ws.getMiddleHead();
               if (mh != null) {
                  double dSq = mh.distanceToSqr(this);
                  if (dSq <= bestSq) {
                     bestSq = dSq;
                     this.suckHead = mh;
                  }
               }
            }

            if (this.suckHead == null) {
               return false;
            }

            this.suckLockTick = t;
         }

         if (this.suckHead.isAlive() && !this.suckHead.isRemoved()) {
            this.suckHead.lookAt(this.position());
            if (t < this.suckLockTick + 20) {
               return false;
            } else {
               if (this.suckStartTick < 0) {
                  this.suckStartTick = t;
                  this.suckStartPos = this.position();
                  Vec3 fromHead = this.position().subtract(this.suckHead.getEyePosition());
                  double d = fromHead.length();
                  this.suckDir = d > 1.0E-4 ? fromHead.scale((double)1.0F / d) : new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
               }

               Vec3 target = this.suckHead.getEyePosition().add(this.suckDir.scale((double)10.0F));
               float p = (float)Mth.clamp((double)(t - this.suckStartTick) / (double)Math.max(1, 580 - this.suckStartTick), (double)0.0F, (double)1.0F);
               float smooth = p * p * (3.0F - 2.0F * p);
               Vec3 desired = this.suckStartPos.lerp(target, (double)smooth);
               this.setDeltaMovement(desired.subtract(this.position()));
               this.setPos(desired.x, desired.y, desired.z);
               return true;
            }
         } else {
            this.suckHead = null;
            return false;
         }
      }
   }

   private void explode() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         boolean var30 = true;
         double var3 = (double)900.0F;
         double cx = this.getX();
         double cy = this.getY((double)0.0625F);
         double cz = this.getZ();
         server.explode(this, cx, cy, cz, 30.0F, true, ExplosionInteraction.NONE);
         BlockState air = Blocks.AIR.defaultBlockState();
         int bx = Mth.floor(cx);
         int by = Mth.floor(cy);
         int bz = Mth.floor(cz);
         int minY = server.getMinY();
         int maxY = server.getMaxY();
         BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();

         for(int dx = -30; dx <= 30; ++dx) {
            for(int dy = -30; dy <= 30; ++dy) {
               int wy = by + dy;
               if (wy >= minY && wy <= maxY) {
                  int dxy = dx * dx + dy * dy;

                  for(int dz = -30; dz <= 30; ++dz) {
                     if (!((double)(dxy + dz * dz) > (double)900.0F)) {
                        p.set(bx + dx, wy, bz + dz);
                        BlockState st = server.getBlockState(p);
                        if (!st.isAir() && !(st.getDestroySpeed(server, p) < 0.0F)) {
                           server.setBlock(p, air, 2);
                        }
                     }
                  }
               }
            }
         }

         for(int i = 0; i < 400; ++i) {
            int fx = bx + this.random.nextInt(61) - 30;
            int fz = bz + this.random.nextInt(61) - 30;

            for(int fy = by + 30; fy > by - 30; --fy) {
               p.set(fx, fy, fz);
               if (!server.getBlockState(p).isAir()) {
                  BlockPos ap = p.above();
                  double ex = (double)fx - cx;
                  double ey = (double)ap.getY() - cy;
                  double ez = (double)fz - cz;
                  if (server.getBlockState(ap).isAir() && ex * ex + ey * ey + ez * ez <= (double)900.0F) {
                     server.setBlock(ap, Blocks.FIRE.defaultBlockState(), 2);
                  }
                  break;
               }
            }
         }

         AABB blast = new AABB(cx - (double)30.0F, cy - (double)30.0F, cz - (double)30.0F, cx + (double)30.0F, cy + (double)30.0F, cz + (double)30.0F);

         for(LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, blast)) {
            if (!(e.distanceToSqr(cx, cy, cz) > (double)900.0F)) {
               if (e instanceof WitherStormEntity) {
                  WitherStormEntity ws = (WitherStormEntity)e;
                  if (!ws.formidibombed(server)) {
                     ws.kill(server);
                  }
               } else {
                  e.igniteForSeconds(8.0F);
               }
            }
         }

         double flashRangeSq = (double)4000000.0F;
         FormidibombFlashPayload flash = new FormidibombFlashPayload(cx, cy, cz);

         for(ServerPlayer viewer : server.players()) {
            if (viewer.distanceToSqr(cx, cy, cz) <= flashRangeSq) {
               ServerPlayNetworking.send(viewer, flash);
            }
         }

         server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, true, true, cx, cy, cz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);

         for(int ring = 1; ring <= 3; ++ring) {
            double rr = (double)30.0F * 0.35 * (double)ring;
            int count = 16 * ring;

            for(int i = 0; i < count; ++i) {
               double a = (double)i / (double)count * Math.PI * (double)2.0F;
               server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, true, true, cx + Math.cos(a) * rr, cy + (this.random.nextDouble() - 0.3) * (double)30.0F * (double)0.5F, cz + Math.sin(a) * rr, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
            }
         }

         for(int i = 0; i < 48; ++i) {
            double a = (double)i / (double)48.0F * Math.PI * (double)2.0F;
            double rr = (double)37.5F;
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, true, true, cx + Math.cos(a) * rr, cy + (double)1.0F, cz + Math.sin(a) * rr, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
         }

         server.sendParticles(ParticleTypes.FLAME, true, true, cx, cy, cz, 2000, (double)18.0F, (double)15.0F, (double)18.0F, 0.35);
         server.sendParticles(ParticleTypes.LAVA, true, true, cx, cy, cz, 600, (double)18.0F, (double)15.0F, (double)18.0F, (double)0.0F);
         server.sendParticles(ParticleTypes.LARGE_SMOKE, true, true, cx, cy + (double)15.0F, cz, 1200, (double)21.0F, (double)24.0F, (double)21.0F, 0.1);
         Holder<SoundEvent> nearSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.FORMIDIBOMB_EXPLOSION);
         Holder.Reference<SoundEvent> bangSound = SoundEvents.GENERIC_EXPLODE;
         double nearSq = (double)67600.0F;

         for(ServerPlayer listener : server.players()) {
            double dSq = listener.distanceToSqr(cx, cy, cz);
            if (dSq <= nearSq) {
               listener.connection.send(new ClientboundSoundPacket(nearSound, SoundSource.BLOCKS, cx, cy, cz, 16.0F, 1.0F, this.level().getRandom().nextLong()));
               listener.connection.send(new ClientboundSoundPacket(bangSound, SoundSource.BLOCKS, cx, cy, cz, 16.0F, 0.4F, this.level().getRandom().nextLong()));
            }
         }

      }
   }

   public boolean isMorphed() {
      return this.getTicks() >= 50;
   }

   public float getSpin(float partialTick) {
      float t = (float)this.getTicks() + partialTick;
      if (t < 50.0F) {
         return t * t * 0.3F;
      } else {
         float base = 750.0F;
         float dt = t - 50.0F;
         float momentum = 22.0F * (1.0F - (float)Math.exp((double)(-dt * 0.35F)));
         return base + momentum + dt * 1.5F;
      }
   }

   public float getWhiteout(float partialTick) {
      float t = (float)this.getTicks() + partialTick;
      if (t < 50.0F) {
         float w = t / 50.0F;
         return Mth.clamp(w * w, 0.0F, 1.0F);
      } else {
         return t >= 594.0F ? Mth.clamp((t - 594.0F) / 6.0F, 0.0F, 1.0F) : Mth.clamp(1.0F - (t - 50.0F) / 14.0F, 0.0F, 1.0F);
      }
   }

   public float getCrackGlow(float partialTick) {
      float t = (float)this.getTicks() + partialTick;
      return t < 50.0F ? 0.0F : Mth.clamp((t - 50.0F) / 550.0F, 0.0F, 1.0F);
   }

   public float getShake(float partialTick) {
      float t = (float)this.getTicks() + partialTick;
      return (t < 50.0F ? t / 50.0F : 0.15F) * 0.09F;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.setTicks(input.getIntOr("ticks", 0));
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putInt("ticks", this.getTicks());
   }

   static {
      DATA_TICKS = SynchedEntityData.defineId(FormidibombEntity.class, EntityDataSerializers.INT);
      DATA_SUCKING = SynchedEntityData.defineId(FormidibombEntity.class, EntityDataSerializers.BOOLEAN);
   }
}
