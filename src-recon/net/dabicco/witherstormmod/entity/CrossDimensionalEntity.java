package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
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
   private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.CrossDimensionalEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Float> EXTEND = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.CrossDimensionalEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> TIP_X = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.CrossDimensionalEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> TIP_Y = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.CrossDimensionalEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> TIP_Z = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.CrossDimensionalEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Integer> VICTIM_ID = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.CrossDimensionalEntity.class, EntityDataSerializers.INT
   );
   public static final ResourceKey<DamageType> PULLED_THROUGH = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("dabywitherstormmod", "pulled_through")
   );
   private static final int SHOOT_TICKS = 10;
   private static final int SEARCH_TICKS = 320;
   private static final int DESTROY_TICKS = 70;
   private static final int RETRACT_TICKS = 12;
   private static final double REACH = 7.0;
   private static final double GRAB_RANGE = 2.6;
   private int stateTicks;
   private UUID stormUUID;
   private BlockPos portalAnchor;
   private Vec3 clientTip = Vec3.ZERO;
   private Vec3 clientTipPrev = Vec3.ZERO;
   private boolean clientTipInit;

   public CrossDimensionalEntity(EntityType<? extends net.dabicco.witherstormmod.entity.CrossDimensionalEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(STATE, 0);
      builder.define(EXTEND, 0.0F);
      builder.define(TIP_X, 0.0F);
      builder.define(TIP_Y, 0.0F);
      builder.define(TIP_Z, 0.0F);
      builder.define(VICTIM_ID, -1);
   }

   public int getProbeState() {
      return (Integer)(Object)(Object)this.entityData.get(STATE);
   }

   public float getExtend() {
      return (Float)(Object)(Object)this.entityData.get(EXTEND);
   }

   public Vec3 getTipOffset() {
      return new Vec3(
         ((Float)(Object)(Object)this.entityData.get(TIP_X)).floatValue(), ((Float)(Object)(Object)this.entityData.get(TIP_Y)).floatValue(), ((Float)(Object)(Object)this.entityData.get(TIP_Z)).floatValue()
      );
   }

   public Vec3 getInterpolatedTip(float partialTick) {
      return this.clientTipPrev.lerp(this.clientTip, partialTick);
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
      } else if (this.level() instanceof ServerLevel server) {
         this.stateTicks++;
         switch (this.getProbeState()) {
            case 0:
               this.tickShoot(server);
               break;
            case 1:
               this.tickSearch(server);
               break;
            case 2:
               this.tickGrab(server);
               break;
            case 3:
               this.tickDestroy(server);
               break;
            case 4:
               this.tickRetract();
               break;
            default:
               this.discard();
         }
      }
   }

   private void tickShoot(ServerLevel server) {
      float p = Math.min(1.0F, this.stateTicks / 10.0F);
      this.entityData.set(EXTEND, p);
      Vec3 forward = this.facing();
      this.setTip(forward.scale(7.0 * p));
      this.hitAnythingAtTip(server, true);
      if (p >= 1.0F) {
         this.setState(1);
      }
   }

   private void tickSearch(ServerLevel server) {
      this.entityData.set(EXTEND, 1.0F);
      double t = this.stateTicks * 0.09;
      Vec3 forward = this.facing();
      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      Vec3 tip = forward.scale(7.0 * (0.75 + 0.25 * Math.sin(t * 0.7))).add(side.scale(Math.sin(t) * 3.2)).add(0.0, Math.sin(t * 1.3) * 2.0, 0.0);
      this.setTip(tip);
      this.hitAnythingAtTip(server, false);
      Player prey = this.nearestPreyToTip(server);
      if (prey != null) {
         this.entityData.set(VICTIM_ID, prey.getId());
         this.setState(2);
      } else if (this.stateTicks >= 320) {
         this.setState(3);
      }
   }

   private void tickGrab(ServerLevel server) {
      if (server.getEntity((Integer)(Object)(Object)this.entityData.get(VICTIM_ID)) instanceof ServerPlayer player && player.isAlive()) {
         Vec3 mouth = this.position();
         Vec3 toMouth = mouth.subtract(player.position());
         this.setTip(player.position().subtract(this.position()));
         if (toMouth.length() > 1.2 && this.stateTicks < 30) {
            Vec3 step = toMouth.normalize().scale(Math.min(0.9, toMouth.length()));
            player.setDeltaMovement(step);
            player.hurtMarked = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
         } else {
            this.deliverToStorm(server, player);
            this.setState(4);
         }
      } else {
         this.setState(3);
      }
   }

   private void deliverToStorm(ServerLevel server, ServerPlayer player) {
      ServerLevel overworld = server.getServer().getLevel(Level.OVERWORLD);
      if (overworld != null) {
         net.dabicco.witherstormmod.entity.WitherStormEntity var10000;
         if (this.stormUUID != null && overworld.getEntity(this.stormUUID) instanceof net.dabicco.witherstormmod.entity.WitherStormEntity ws) {
            var10000 = ws;
         } else {
            var10000 = null;
         }

         net.dabicco.witherstormmod.entity.WitherStormHeadEntity head = var10000 != null ? var10000.getAnyHead() : null;
         Vec3 dest;
         if (head != null) {
            dest = head.position().add(0.0, -2.0, 0.0);
         } else if (var10000 != null) {
            dest = var10000.getBoundingBox().getCenter().add(0.0, 6.0, 0.0);
         } else {
            dest = surfaceAt(overworld, this.getX() * 8.0, this.getZ() * 8.0);
         }

         player.teleportTo(overworld, dest.x, dest.y, dest.z, Set.of(), player.getYRot(), player.getXRot(), false);
         unstick(overworld, player);
         if (head != null) {
            head.chompVictim(player, PULLED_THROUGH);
         } else {
            Reference<DamageType> type = overworld.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(PULLED_THROUGH);
            player.hurtServer(overworld, new DamageSource(type, var10000), Float.MAX_VALUE);
         }
      }
   }

   private static Vec3 surfaceAt(ServerLevel level, double x, double z) {
      BlockPos column = BlockPos.containing(x, 0.0, z);
      level.getChunk(column);
      int y = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, column).getY();
      return new Vec3(x, y + 1.0, z);
   }

   private static void unstick(ServerLevel level, ServerPlayer player) {
      for (int i = 0; i < 48; i++) {
         if (level.noCollision(player, player.getBoundingBox())) {
            return;
         }

         player.teleportTo(player.getX(), player.getY() + 1.0, player.getZ());
      }

      Vec3 surface = surfaceAt(level, player.getX(), player.getZ());
      player.teleportTo(surface.x, surface.y, surface.z);

      for (int i = 0; i < 16 && !level.noCollision(player, player.getBoundingBox()); i++) {
         player.teleportTo(player.getX(), player.getY() + 1.0, player.getZ());
      }
   }

   private void tickDestroy(ServerLevel server) {
      this.entityData.set(EXTEND, 1.0F);
      float p = Mth.clamp(this.stateTicks / 70.0F, 0.0F, 1.0F);
      Vec3 forward = this.facing();
      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      double coil = p * Math.PI * 3.0;
      double wrapR = 2.1 - 0.8 * p;
      double tremble = 0.05 + 0.13 * p;
      double t = this.stateTicks * 0.32;
      Vec3 tip = forward.scale(1.2)
         .add(side.scale(Math.cos(coil) * wrapR + Math.sin(t) * tremble))
         .add(0.0, 1.6 + Math.sin(coil) * wrapR + Math.cos(t * 1.13) * tremble, 0.0);
      this.setTip(tip);
      if (this.stateTicks % 4 == 0) {
         for (BlockPos pos : this.portalBlocks(server)) {
            server.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.5, 0.3, 0.4);
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
      List<BlockPos> frame = new ArrayList<>();

      for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-12, -14, -12), centre.offset(12, 14, 12))) {
         BlockState state = server.getBlockState(pos);
         if (state.is(Blocks.OBSIDIAN)) {
            frame.add(pos.immutable());
         }
      }

      for (BlockPos posx : this.portalBlocks(server)) {
         server.removeBlock(posx, false);
      }

      Collections.shuffle(frame, new Random(server.getGameTime()));
      int half = frame.size() / 2;
      int quarter = frame.size() / 4;

      for (int i = 0; i < frame.size(); i++) {
         BlockPos posx = frame.get(i);
         if (i < half) {
            BlockState state = server.getBlockState(posx);
            server.removeBlock(posx, false);
            WitherStormClusterEntity cluster = new WitherStormClusterEntity(net.dabicco.witherstormmod.entity.ModEntityTypes.WITHER_STORM_CLUSTER, server);
            cluster.setOrigin(posx);
            cluster.setRadius(0);
            cluster.setPos(posx.getX() + 0.5, posx.getY() + 0.5, posx.getZ() + 0.5);
            cluster.setClientBlockData(List.of(state), List.of(BlockPos.ZERO));
            server.addFreshEntity(cluster);
            Vec3 away = new Vec3(posx.getX() + 0.5 - this.getX(), 0.0, posx.getZ() + 0.5 - this.getZ());
            if (away.lengthSqr() < 1.0E-4) {
               away = new Vec3(1.0, 0.0, 0.0);
            }

            cluster.launchAsDebris(away.normalize().scale(0.55).add(0.0, 0.45, 0.0));
            WitherStormClusterEntity.syncBlocksToTracking(cluster);
         } else if (i < half + quarter) {
            server.removeBlock(posx, false);
         }
      }
   }

   private void tickRetract() {
      float p = 1.0F - Math.min(1.0F, this.stateTicks / 12.0F);
      this.entityData.set(EXTEND, p);
      this.setTip(this.getTipOffset().scale(0.8));
      if (p <= 0.0F) {
         this.discard();
      }
   }

   private List<BlockPos> portalBlocks(ServerLevel server) {
      List<BlockPos> out = new ArrayList<>();
      BlockPos centre = this.portalAnchor != null ? this.portalAnchor : this.blockPosition();

      for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-3, -2, -3), centre.offset(3, 4, 3))) {
         if (server.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
            out.add(pos.immutable());
         }
      }

      return out;
   }

   private Vec3 facing() {
      float yaw = this.getYRot();
      double rad = Math.toRadians(yaw);
      return new Vec3(-Math.sin(rad), 0.0, Math.cos(rad));
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

      for (Player p : server.getEntitiesOfClass(Player.class, new AABB(tip, tip).inflate(1.6), pl -> pl.isAlive() && !pl.isCreative() && !pl.isSpectator())) {
         if (!p.hurtMarked) {
            Vec3 push = p.position().subtract(tip);
            if (push.lengthSqr() < 1.0E-4) {
               push = this.facing();
            }

            double power = hard ? 0.95 : 0.5;
            p.setDeltaMovement(push.normalize().scale(power).add(0.0, 0.35, 0.0));
            p.hurtMarked = true;
            if (p instanceof ServerPlayer sp) {
               sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }

            Reference<DamageType> type = server.registryAccess()
               .lookupOrThrow(Registries.DAMAGE_TYPE)
               .getOrThrow(net.dabicco.witherstormmod.entity.WitherStormHeadEntity.CHOMP_DAMAGE);
            p.hurtServer(server, new DamageSource(type, this), hard ? 6.0F : 3.0F);
         }
      }
   }

   private Player nearestPreyToTip(ServerLevel server) {
      Vec3 tip = this.tipWorld();
      Player best = null;
      double bestSqr = 6.760000000000001;

      for (Player p : server.players()) {
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
}
