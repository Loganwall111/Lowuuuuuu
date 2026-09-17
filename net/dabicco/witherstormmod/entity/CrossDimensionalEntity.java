package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CrossDimensionalEntity extends Entity {
   public static final int STATE_SHOOT = 0;
   public static final int STATE_SEARCH = 1;
   public static final int STATE_GRAB = 2;
   public static final int STATE_DESTROY = 3;
   public static final int STATE_RETRACT = 4;
   private static final EntityDataAccessor<Integer> STATE;
   private static final EntityDataAccessor<Float> EXTEND;
   private static final EntityDataAccessor<Float> TIP_X;
   private static final EntityDataAccessor<Float> TIP_Y;
   private static final EntityDataAccessor<Float> TIP_Z;
   private static final EntityDataAccessor<Integer> VICTIM_ID;
   public static final ResourceKey<DamageType> PULLED_THROUGH;
   private static final int SHOOT_TICKS = 10;
   private static final int SEARCH_TICKS = 320;
   private static final int DESTROY_TICKS = 70;
   private static final int RETRACT_TICKS = 12;
   private static final double REACH = (double)7.0F;
   private static final double GRAB_RANGE = 2.6;
   private int stateTicks;
   private UUID stormUUID;
   private BlockPos portalAnchor;
   private Vec3 clientTip;
   private Vec3 clientTipPrev;
   private boolean clientTipInit;

   public CrossDimensionalEntity(EntityType<? extends CrossDimensionalEntity> type, Level level) {
      super(type, level);
      this.clientTip = Vec3.ZERO;
      this.clientTipPrev = Vec3.ZERO;
      this.noPhysics = true;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(STATE, 0);
      builder.define(EXTEND, 0.0F);
      builder.define(TIP_X, 0.0F);
      builder.define(TIP_Y, 0.0F);
      builder.define(TIP_Z, 0.0F);
      builder.define(VICTIM_ID, -1);
   }

   public int getProbeState() {
      return (Integer)this.entityData.get(STATE);
   }

   public float getExtend() {
      return (Float)this.entityData.get(EXTEND);
   }

   public Vec3 getTipOffset() {
      return new Vec3((double)(Float)this.entityData.get(TIP_X), (double)(Float)this.entityData.get(TIP_Y), (double)(Float)this.entityData.get(TIP_Z));
   }

   public Vec3 getInterpolatedTip(float partialTick) {
      return this.clientTipPrev.lerp(this.clientTip, (double)partialTick);
   }

   public void setStormUUID(UUID id) {
      this.stormUUID = id;
   }

   public void setPortalAnchor(BlockPos pos) {
      this.portalAnchor = pos;
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

   private void setState(int s) {
      this.entityData.set(STATE, s);
      this.stateTicks = 0;
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         Vec3 synced = this.getTipOffset();
         if (!this.clientTipInit) {
            this.clientTip = this.clientTipPrev = synced;
            this.clientTipInit = true;
         } else {
            this.clientTipPrev = this.clientTip;
            this.clientTip = synced;
         }

      } else {
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)var2;
            ++this.stateTicks;
            switch (this.getProbeState()) {
               case 0 -> this.tickShoot(server);
               case 1 -> this.tickSearch(server);
               case 2 -> this.tickGrab(server);
               case 3 -> this.tickDestroy(server);
               case 4 -> this.tickRetract();
               default -> this.discard();
            }

         }
      }
   }

   private void tickShoot(ServerLevel server) {
      float p = Math.min(1.0F, (float)this.stateTicks / 10.0F);
      this.entityData.set(EXTEND, p);
      Vec3 forward = this.facing();
      this.setTip(forward.scale((double)7.0F * (double)p));
      this.hitAnythingAtTip(server, true);
      if (p >= 1.0F) {
         this.setState(1);
      }

   }

   private void tickSearch(ServerLevel server) {
      this.entityData.set(EXTEND, 1.0F);
      double t = (double)this.stateTicks * 0.09;
      Vec3 forward = this.facing();
      Vec3 side = new Vec3(-forward.z, (double)0.0F, forward.x);
      Vec3 tip = forward.scale((double)7.0F * ((double)0.75F + (double)0.25F * Math.sin(t * 0.7))).add(side.scale(Math.sin(t) * 3.2)).add((double)0.0F, Math.sin(t * 1.3) * (double)2.0F, (double)0.0F);
      this.setTip(tip);
      this.hitAnythingAtTip(server, false);
      Player prey = this.nearestPreyToTip(server);
      if (prey != null) {
         this.entityData.set(VICTIM_ID, prey.getId());
         this.setState(2);
      } else {
         if (this.stateTicks >= 320) {
            this.setState(3);
         }

      }
   }

   private void tickGrab(ServerLevel server) {
      Entity victim = server.getEntity((Integer)this.entityData.get(VICTIM_ID));
      if (victim instanceof ServerPlayer player) {
         if (player.isAlive()) {
            Vec3 mouth = this.position();
            Vec3 toMouth = mouth.subtract(player.position());
            this.setTip(player.position().subtract(this.position()));
            if (toMouth.length() > 1.2 && this.stateTicks < 30) {
               Vec3 step = toMouth.normalize().scale(Math.min(0.9, toMouth.length()));
               player.setDeltaMovement(step);
               player.hurtMarked = true;
               player.connection.send(new ClientboundSetEntityMotionPacket(player));
               return;
            }

            this.deliverToStorm(server, player);
            this.setState(4);
            return;
         }
      }

      this.setState(3);
   }

   private void deliverToStorm(ServerLevel server, ServerPlayer player) {
      ServerLevel overworld = server.getServer().getLevel(Level.OVERWORLD);
      if (overworld != null) {
         WitherStormEntity var10000;
         label33: {
            if (this.stormUUID != null) {
               Entity var6 = overworld.getEntity(this.stormUUID);
               if (var6 instanceof WitherStormEntity) {
                  WitherStormEntity ws = (WitherStormEntity)var6;
                  var10000 = ws;
                  break label33;
               }
            }

            var10000 = null;
         }

         WitherStormEntity storm = var10000;
         WitherStormHeadEntity head = storm != null ? storm.getAnyHead() : null;
         Vec3 dest;
         if (head != null) {
            dest = head.position().add((double)0.0F, (double)-2.0F, (double)0.0F);
         } else if (storm != null) {
            dest = storm.getBoundingBox().getCenter().add((double)0.0F, (double)6.0F, (double)0.0F);
         } else {
            dest = surfaceAt(overworld, this.getX() * (double)8.0F, this.getZ() * (double)8.0F);
         }

         player.teleportTo(overworld, dest.x, dest.y, dest.z, Set.of(), player.getYRot(), player.getXRot(), false);
         unstick(overworld, player);
         if (head != null) {
            head.chompVictim(player, PULLED_THROUGH);
         } else {
            Holder.Reference<DamageType> type = overworld.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(PULLED_THROUGH);
            player.hurtServer(overworld, new DamageSource(type, storm), Float.MAX_VALUE);
         }

      }
   }

   private static Vec3 surfaceAt(ServerLevel level, double x, double z) {
      BlockPos column = BlockPos.containing(x, (double)0.0F, z);
      level.getChunk(column);
      int y = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, column).getY();
      return new Vec3(x, (double)y + (double)1.0F, z);
   }

   private static void unstick(ServerLevel level, ServerPlayer player) {
      for(int i = 0; i < 48; ++i) {
         if (level.noCollision(player, player.getBoundingBox())) {
            return;
         }

         player.teleportTo(player.getX(), player.getY() + (double)1.0F, player.getZ());
      }

      Vec3 surface = surfaceAt(level, player.getX(), player.getZ());
      player.teleportTo(surface.x, surface.y, surface.z);

      for(int i = 0; i < 16 && !level.noCollision(player, player.getBoundingBox()); ++i) {
         player.teleportTo(player.getX(), player.getY() + (double)1.0F, player.getZ());
      }

   }

   private void tickDestroy(ServerLevel server) {
      this.entityData.set(EXTEND, 1.0F);
      float p = Mth.clamp((float)this.stateTicks / 70.0F, 0.0F, 1.0F);
      Vec3 forward = this.facing();
      Vec3 side = new Vec3(-forward.z, (double)0.0F, forward.x);
      double coil = (double)p * Math.PI * (double)3.0F;
      double wrapR = 2.1 - 0.8 * (double)p;
      double tremble = 0.05 + 0.13 * (double)p;
      double t = (double)this.stateTicks * 0.32;
      Vec3 tip = forward.scale(1.2).add(side.scale(Math.cos(coil) * wrapR + Math.sin(t) * tremble)).add((double)0.0F, 1.6 + Math.sin(coil) * wrapR + Math.cos(t * 1.13) * tremble, (double)0.0F);
      this.setTip(tip);
      if (this.stateTicks % 4 == 0) {
         for(BlockPos pos : this.portalBlocks(server)) {
            server.sendParticles(ParticleTypes.PORTAL, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 6, 0.3, (double)0.5F, 0.3, 0.4);
         }
      }

      if (this.stateTicks == 35) {
         this.level().playSound((Entity)null, this.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 3.0F, 0.4F);
      }

      if (this.stateTicks >= 70) {
         this.tearPortalApart(server);
         this.setState(4);
      }

   }

   private void tearPortalApart(ServerLevel server) {
      BlockPos centre = this.portalAnchor != null ? this.portalAnchor : this.blockPosition();
      List<BlockPos> frame = new ArrayList();

      for(BlockPos pos : BlockPos.betweenClosed(centre.offset(-12, -14, -12), centre.offset(12, 14, 12))) {
         BlockState state = server.getBlockState(pos);
         if (state.is(Blocks.OBSIDIAN)) {
            frame.add(pos.immutable());
         }
      }

      for(BlockPos pos : this.portalBlocks(server)) {
         server.removeBlock(pos, false);
      }

      Collections.shuffle(frame, new Random(server.getGameTime()));
      int half = frame.size() / 2;
      int quarter = frame.size() / 4;

      for(int i = 0; i < frame.size(); ++i) {
         BlockPos pos = (BlockPos)frame.get(i);
         if (i < half) {
            BlockState state = server.getBlockState(pos);
            server.removeBlock(pos, false);
            WitherStormClusterEntity cluster = new WitherStormClusterEntity(ModEntityTypes.WITHER_STORM_CLUSTER, server);
            cluster.setOrigin(pos);
            cluster.setRadius(0);
            cluster.setPos((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F);
            cluster.setClientBlockData(List.of(state), List.of(BlockPos.ZERO));
            server.addFreshEntity(cluster);
            Vec3 away = new Vec3((double)pos.getX() + (double)0.5F - this.getX(), (double)0.0F, (double)pos.getZ() + (double)0.5F - this.getZ());
            if (away.lengthSqr() < 1.0E-4) {
               away = new Vec3((double)1.0F, (double)0.0F, (double)0.0F);
            }

            cluster.launchAsDebris(away.normalize().scale(0.55).add((double)0.0F, 0.45, (double)0.0F));
            WitherStormClusterEntity.syncBlocksToTracking(cluster);
         } else if (i < half + quarter) {
            server.removeBlock(pos, false);
         }
      }

   }

   private void tickRetract() {
      float p = 1.0F - Math.min(1.0F, (float)this.stateTicks / 12.0F);
      this.entityData.set(EXTEND, p);
      this.setTip(this.getTipOffset().scale(0.8));
      if (p <= 0.0F) {
         this.discard();
      }

   }

   private List<BlockPos> portalBlocks(ServerLevel server) {
      List<BlockPos> out = new ArrayList();
      BlockPos centre = this.portalAnchor != null ? this.portalAnchor : this.blockPosition();

      for(BlockPos pos : BlockPos.betweenClosed(centre.offset(-3, -2, -3), centre.offset(3, 4, 3))) {
         if (server.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
            out.add(pos.immutable());
         }
      }

      return out;
   }

   private Vec3 facing() {
      float yaw = this.getYRot();
      double rad = Math.toRadians((double)yaw);
      return new Vec3(-Math.sin(rad), (double)0.0F, Math.cos(rad));
   }

   private void setTip(Vec3 offset) {
      this.entityData.set(TIP_X, (float)offset.x);
      this.entityData.set(TIP_Y, (float)offset.y);
      this.entityData.set(TIP_Z, (float)offset.z);
   }

   private Vec3 tipWorld() {
      return this.position().add(this.getTipOffset());
   }

   private void hitAnythingAtTip(ServerLevel server, boolean hard) {
      Vec3 tip = this.tipWorld();

      for(Player p : server.getEntitiesOfClass(Player.class, (new AABB(tip, tip)).inflate(1.6), (pl) -> pl.isAlive() && !pl.isCreative() && !pl.isSpectator())) {
         if (!p.hurtMarked) {
            Vec3 push = p.position().subtract(tip);
            if (push.lengthSqr() < 1.0E-4) {
               push = this.facing();
            }

            double power = hard ? 0.95 : (double)0.5F;
            p.setDeltaMovement(push.normalize().scale(power).add((double)0.0F, 0.35, (double)0.0F));
            p.hurtMarked = true;
            if (p instanceof ServerPlayer) {
               ServerPlayer sp = (ServerPlayer)p;
               sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }

            Holder.Reference<DamageType> type = server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(WitherStormHeadEntity.CHOMP_DAMAGE);
            p.hurtServer(server, new DamageSource(type, this), hard ? 6.0F : 3.0F);
         }
      }

   }

   private Player nearestPreyToTip(ServerLevel server) {
      Vec3 tip = this.tipWorld();
      Player best = null;
      double bestSqr = 6.760000000000001;

      for(Player p : server.players()) {
         if (p.isAlive() && !p.isCreative() && !p.isSpectator()) {
            double d = p.position().distanceToSqr(tip);
            if (d < bestSqr) {
               bestSqr = d;
               best = p;
            }
         }
      }

      return best;
   }

   protected void readAdditionalSaveData(ValueInput input) {
      String s = input.getStringOr("StormUUID", "");
      this.stormUUID = s.isEmpty() ? null : UUID.fromString(s);
      this.stateTicks = input.getIntOr("StateTicks", 0);
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      if (this.stormUUID != null) {
         output.putString("StormUUID", this.stormUUID.toString());
      }

      output.putInt("StateTicks", this.stateTicks);
   }

   static {
      STATE = SynchedEntityData.defineId(CrossDimensionalEntity.class, EntityDataSerializers.INT);
      EXTEND = SynchedEntityData.defineId(CrossDimensionalEntity.class, EntityDataSerializers.FLOAT);
      TIP_X = SynchedEntityData.defineId(CrossDimensionalEntity.class, EntityDataSerializers.FLOAT);
      TIP_Y = SynchedEntityData.defineId(CrossDimensionalEntity.class, EntityDataSerializers.FLOAT);
      TIP_Z = SynchedEntityData.defineId(CrossDimensionalEntity.class, EntityDataSerializers.FLOAT);
      VICTIM_ID = SynchedEntityData.defineId(CrossDimensionalEntity.class, EntityDataSerializers.INT);
      PULLED_THROUGH = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("dabywitherstormmod", "pulled_through"));
   }
}
