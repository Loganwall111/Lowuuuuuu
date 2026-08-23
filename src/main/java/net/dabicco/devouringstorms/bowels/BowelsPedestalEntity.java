package net.dabicco.devouringstorms.bowels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.dabicco.devouringstorms.BowelsEndRoom;
import net.dabicco.devouringstorms.client.ClusterMesh;
import net.dabicco.devouringstorms.network.CaveRumblePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BowelsPedestalEntity extends Entity {
   private static final int RISE_TICKS = 110;
   private static final int SETTLE_TICKS = 15;
   public static final int SAND_RISE = 4;
   private static final double KEEPOUT = (double)9.0F;
   private static final double KEEPOUT_PUSH = 0.85;
   private static final EntityDataAccessor<Integer> RISEN;
   private static final EntityDataAccessor<Long> SEED;
   private ClusterMesh pedestalMesh;
   private ClusterMesh sandMesh;
   private boolean picked;
   private boolean landed;

   public BowelsPedestalEntity(EntityType<? extends BowelsPedestalEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(RISEN, 0);
      builder.define(SEED, 0L);
   }

   public void setSeed(long seed) {
      this.entityData.set(SEED, seed);
   }

   public long getSeed() {
      return (Long)this.entityData.get(SEED);
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isAttackable() {
      return false;
   }

   public boolean canBeCollidedWith(Entity by) {
      return false;
   }

   public boolean hurtServer(ServerLevel level, DamageSource src, float amount) {
      return false;
   }

   private float progress(float partialTick) {
      float ticks = (float)(Integer)this.entityData.get(RISEN) + partialTick;
      return BowelsTentacleShape.smoothstep(Mth.clamp((ticks - 15.0F) / 110.0F, 0.0F, 1.0F));
   }

   public float pedestalLift(float partialTick) {
      return this.progress(partialTick) * 4.0F;
   }

   public float sandLift(float partialTick) {
      return (this.progress(partialTick) - 1.0F) * 4.0F;
   }

   public void tick() {
      super.tick();
      double r = (double)12.5F;
      this.setBoundingBox(new AABB(this.getX() - r, this.getY() - (double)4.0F - (double)2.0F, this.getZ() - r, this.getX() + r, this.getY() + (double)3.0F + (double)4.0F + (double)2.0F, this.getZ() + r));
      Level var4 = this.level();
      if (var4 instanceof ServerLevel server) {
         long var10 = this.getSeed();
         if (!this.picked) {
            this.picked = true;
            BowelsEndRoom.clearPedestal(server, var10, 0);
            this.shoveClear(server);
         }

         int ticks = (Integer)this.entityData.get(RISEN);
         this.entityData.set(RISEN, ticks + 1);
         float lift = this.pedestalLift(0.0F);
         this.carryPassengers(server, (double)lift);
         this.pushBack(server);
         if (ticks > 15 && ticks % 16 == 0) {
            server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PISTON_EXTEND, SoundSource.HOSTILE, 2.6F, 0.32F);
            server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + (double)1.0F, this.getZ(), 40, (double)4.5F, 0.6, (double)4.5F, (double)0.0F);
         }

         if (ticks >= 125 && !this.landed) {
            this.landed = true;
            BowelsEndRoom.writeSand(server, var10);
            BowelsEndRoom.writePedestal(server, var10, 4);
            this.carryPassengers(server, (double)4.0F);
            server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 3.0F, 0.4F);

            for(ServerPlayer player : server.players()) {
               ServerPlayNetworking.send(player, new CaveRumblePayload(30, 1.0F));
            }

            this.discard();
         }
      }
   }

   private void pushBack(ServerLevel server) {
      for(ServerPlayer player : server.players()) {
         double dx = player.getX() - this.getX();
         double dz = player.getZ() - this.getZ();
         double d2 = dx * dx + dz * dz;
         if (!(d2 > (double)81.0F) && !player.isSpectator() && !player.isCreative()) {
            Vec3 away = d2 < 1.0E-4 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : (new Vec3(dx, (double)0.0F, dz)).normalize();
            double force = 0.85 * ((double)1.0F - Math.sqrt(d2) / (double)9.0F);
            player.push(away.x * force, 0.32 * force + 0.06, away.z * force);
            player.hurtMarked = true;
         }
      }

   }

   private void shoveClear(ServerLevel server) {
      for(ServerPlayer player : server.players()) {
         if (BowelsEndRoom.onPedestal(player.getX(), player.getY(), player.getZ())) {
            Vec3 middle = BowelsEndRoom.daisTop();
            Vec3 away = player.position().subtract(middle);
            away = away.horizontalDistanceSqr() < 1.0E-4 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : (new Vec3(away.x, (double)0.0F, away.z)).normalize();
            player.push(away.x * 1.4, 0.55, away.z * 1.4);
            player.hurtMarked = true;
         }
      }

   }

   private void carryPassengers(ServerLevel server, double lift) {
      Vec3 top = BowelsEndRoom.daisTop();

      for(BowelsHeartEntity heart : server.getEntitiesOfClass(BowelsHeartEntity.class, this.getBoundingBox())) {
         heart.setPos(top.x, top.y + lift, top.z);
         heart.setDeltaMovement(Vec3.ZERO);
      }

      for(BowelsTentacleEntity limb : server.getEntitiesOfClass(BowelsTentacleEntity.class, this.getBoundingBox(), BowelsTentacleEntity::isGuard)) {
         limb.setPos(limb.getX(), BowelsEndRoom.guardMountY() + lift, limb.getZ());
      }

   }

   public ClusterMesh pedestalMesh() {
      if (this.pedestalMesh == null) {
         this.pedestalMesh = this.bake(true);
      }

      return this.pedestalMesh;
   }

   public ClusterMesh sandMesh() {
      if (this.sandMesh == null) {
         this.sandMesh = this.bake(false);
      }

      return this.sandMesh;
   }

   private ClusterMesh bake(boolean pedestal) {
      Level var3 = this.level();
      if (var3 instanceof ClientLevel clientLevel) {
         ArrayList<BlockPos> var5 = new ArrayList<>();
         ArrayList<BlockState> states = new ArrayList<>();
         if (pedestal) {
            BowelsEndRoom.collectPedestal(this.getSeed(), var5, states);
         } else {
            BowelsEndRoom.collectSand(this.getSeed(), var5, states);
         }

         return var5.isEmpty() ? null : ClusterMesh.bake(states, var5, faceVisibility(var5), clientLevel, this.blockPosition());
      } else {
         return null;
      }
   }

   private static List<boolean[]> faceVisibility(List<BlockPos> offsets) {
      Set<Long> filled = new HashSet();

      for(BlockPos off : offsets) {
         filled.add(off.asLong());
      }

      List<boolean[]> out = new ArrayList(offsets.size());

      for(BlockPos off : offsets) {
         boolean[] visible = new boolean[6];

         for(Direction dir : Direction.values()) {
            visible[dir.ordinal()] = !filled.contains(off.relative(dir).asLong());
         }

         out.add(visible);
      }

      return out;
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.entityData.set(RISEN, input.getIntOr("Risen", 0));
      this.entityData.set(SEED, input.getLongOr("Seed", 0L));
      this.picked = input.getBooleanOr("Picked", false);
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putInt("Risen", (Integer)this.entityData.get(RISEN));
      output.putLong("Seed", this.getSeed());
      output.putBoolean("Picked", this.picked);
   }

   static {
      RISEN = SynchedEntityData.defineId(BowelsPedestalEntity.class, EntityDataSerializers.INT);
      SEED = SynchedEntityData.defineId(BowelsPedestalEntity.class, EntityDataSerializers.LONG);
   }
}
