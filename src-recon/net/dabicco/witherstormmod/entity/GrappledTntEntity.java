package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.dabicco.witherstormmod.ModAdvancements;
import net.dabicco.witherstormmod.ModItems;
import net.dabicco.witherstormmod.item.RocketRetrieverItem;
import net.dabicco.witherstormmod.mixin.FireworkRocketEntityAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class GrappledTntEntity extends Entity {
   public static final int STATE_OUTBOUND = 0;
   public static final int STATE_REELING = 1;
   private static final float EXPLOSION_POWER = 4.0F;
   private static final double SPEED = 1.4;
   private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.GrappledTntEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.GrappledTntEntity.class, EntityDataSerializers.INT
   );
   private UUID ownerUUID;
   private Vec3 shootDir = new Vec3(0.0, 0.0, 1.0);
   private double traveled;
   private double maxDistance = 24.0;
   private ItemStack firework = ItemStack.EMPTY;
   private final List<Integer> hooked = new ArrayList<>();

   public GrappledTntEntity(EntityType<? extends net.dabicco.witherstormmod.entity.GrappledTntEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public GrappledTntEntity(Level level, Player owner, Vec3 dir, ItemStack firework) {
      this(net.dabicco.witherstormmod.entity.ModEntityTypes.GRAPPLED_TNT, level);
      this.ownerUUID = owner.getUUID();
      this.entityData.set(DATA_OWNER, owner.getId());
      this.shootDir = dir.normalize();
      this.firework = firework.copy();
      int flight = 1;
      Fireworks fw = (Fireworks)firework.get(DataComponents.FIREWORKS);
      if (fw != null) {
         flight = Math.max(1, fw.flightDuration());
      }

      this.maxDistance = flight * 3 * 8.0;
      Vec3 muzzle = owner.getEyePosition().add(dir.scale(1.2)).add(0.0, -0.2, 0.0);
      this.setPos(muzzle.x, muzzle.y, muzzle.z);
      this.setOldPosAndRot();
      this.setDeltaMovement(this.shootDir.scale(1.4));
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(DATA_STATE, 0);
      builder.define(DATA_OWNER, -1);
   }

   public int getProbeState() {
      return (Integer)this.entityData.get(DATA_STATE);
   }

   public int getOwnerId() {
      return (Integer)this.entityData.get(DATA_OWNER);
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean isCurrentlyGlowing() {
      return false;
   }

   public void tick() {
      this.xOld = this.getX();
      this.yOld = this.getY();
      this.zOld = this.getZ();
      super.tick();
      if (this.level().isClientSide()) {
         if (this.getProbeState() == 0) {
            double cy = this.getY() + 0.45;
            Vec3 back = this.getDeltaMovement().normalize().scale(-0.4);
            this.level().addParticle(ParticleTypes.FIREWORK, this.getX() + back.x, cy + back.y, this.getZ() + back.z, back.x * 0.1, back.y * 0.1, back.z * 0.1);
            this.level().addParticle(ParticleTypes.SMOKE, this.getX() + back.x, cy + back.y, this.getZ() + back.z, 0.0, 0.0, 0.0);
         }
      } else {
         Player owner = this.ownerUUID != null ? this.level().getPlayerByUUID(this.ownerUUID) : null;
         if (this.getProbeState() == 0) {
            Vec3 from = this.position();
            Vec3 to = from.add(this.getDeltaMovement());
            net.dabicco.witherstormmod.entity.WitherStormEntity struck = this.findStormModelHit(to);
            if (struck != null) {
               this.setPos(to.x, to.y, to.z);
               struck.onGrappleHit(to);
               this.detonate((ServerLevel)this.level());
               this.grantFragments((ServerLevel)this.level(), struck.getPhase());
               return;
            }

            BlockHitResult hit = this.level().clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, this));
            boolean hitBlock = hit.getType() != Type.MISS;
            boolean hitEntity = !this.level()
               .getEntitiesOfClass(LivingEntity.class, new AABB(from, to).inflate(0.4), e -> e != owner && e.isAlive() && !e.isSpectator())
               .isEmpty();
            this.traveled = this.traveled + this.getDeltaMovement().length();
            if (!hitBlock && !hitEntity && !(this.traveled > this.maxDistance)) {
               this.setPos(to.x, to.y, to.z);
            } else {
               Vec3 at = hitBlock ? hit.getLocation() : to;
               this.setPos(at.x, at.y, at.z);
               this.detonate((ServerLevel)this.level());
            }
         } else {
            if (owner == null) {
               this.dropHookedAt(this.position());
               this.discard();
               return;
            }

            Vec3 target = owner.getEyePosition().add(0.0, -0.3, 0.0);
            Vec3 toOwner = target.subtract(this.position());
            double dist = toOwner.length();
            if (dist < 1.5) {
               this.dropHookedAt(owner.position());
               this.applyRetrieverCooldown(owner);
               this.discard();
               return;
            }

            Vec3 step = toOwner.normalize().scale(Math.min(1.0, dist));
            this.setPos(this.getX() + step.x, this.getY() + step.y, this.getZ() + step.z);
            this.dragHookedItems();
         }
      }
   }

   private void dragHookedItems() {
      Vec3 hook = this.position();

      for (int id : this.hooked) {
         if (this.level().getEntity(id) instanceof ItemEntity item && item.isAlive()) {
            item.setNoGravity(true);
            item.setPickUpDelay(20);
            Vec3 toHook = hook.subtract(item.position());
            if (toHook.length() > 2.5) {
               item.setPos(hook.x, hook.y, hook.z);
               item.setDeltaMovement(Vec3.ZERO);
            } else {
               item.setDeltaMovement(toHook.scale(0.6));
            }

            item.hurtMarked = true;
         }
      }
   }

   private net.dabicco.witherstormmod.entity.WitherStormEntity findStormModelHit(Vec3 point) {
      AABB search = new AABB(point, point).inflate(48.0);
      net.dabicco.witherstormmod.entity.WitherStormEntity best = null;
      double bestD = Double.MAX_VALUE;

      for (net.dabicco.witherstormmod.entity.WitherStormEntity ws : this.level()
         .getEntitiesOfClass(net.dabicco.witherstormmod.entity.WitherStormEntity.class, search, Entity::isAlive)) {
         if (ws.isGrappleHittable(point)) {
            double d = ws.modelCenter().distanceTo(point);
            if (d < bestD) {
               best = ws;
               bestD = d;
            }
         }
      }

      return best;
   }

   private void applyRetrieverCooldown(Player owner) {
      if (owner != null) {
         for (ItemStack s : new ItemStack[]{owner.getMainHandItem(), owner.getOffhandItem()}) {
            if (s.getItem() instanceof RocketRetrieverItem) {
               owner.getCooldowns().addCooldown(s, 40);
            }
         }
      }
   }

   public static boolean hasActiveShot(Level level, Player player) {
      return !level.getEntitiesOfClass(
            net.dabicco.witherstormmod.entity.GrappledTntEntity.class,
            player.getBoundingBox().inflate(256.0),
            e -> e.isAlive() && e.getOwnerId() == player.getId()
         )
         .isEmpty();
   }

   private void grantFragments(ServerLevel server, double phase) {
      int count;
      if (phase < 4.0) {
         count = 0;
      } else {
         double nothingChance = phase >= 5.0 ? 0.5 : 0.75;
         if (this.random.nextFloat() < nothingChance) {
            count = 0;
         } else {
            int maxRoll = phase >= 5.8 ? 3 : 1;
            count = this.random.nextInt(maxRoll + 1);
         }
      }

      if (count > 0 && server.getEntity(this.getOwnerId()) instanceof ServerPlayer sp) {
         ModAdvancements.grant(sp, "reeled_it_in");
      }

      for (int i = 0; i < count; i++) {
         ItemEntity frag = new ItemEntity(server, this.getX(), this.getY(), this.getZ(), new ItemStack(ModItems.WITHER_FRAGMENT));
         frag.setNoGravity(true);
         frag.setDeltaMovement(Vec3.ZERO);
         frag.setPickUpDelay(Integer.MAX_VALUE);
         server.addFreshEntity(frag);
         this.hooked.add(frag.getId());
      }
   }

   private void detonate(ServerLevel server) {
      if (!this.firework.isEmpty()) {
         FireworkRocketEntity fw = new FireworkRocketEntity(server, this.getX(), this.getY() + 0.45, this.getZ(), this.firework);
         server.addFreshEntity(fw);
         ((FireworkRocketEntityAccessor)fw).setLifetime(0);
      }

      server.explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, ExplosionInteraction.TNT);
      server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, true, true, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
      server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 12.0F, 1.0F);

      for (ItemEntity item : server.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(8.0), e -> e.isAlive() && e.tickCount <= 1)) {
         this.hooked.add(item.getId());
         item.setNoGravity(true);
         item.setPickUpDelay(Integer.MAX_VALUE);
      }

      this.entityData.set(DATA_STATE, 1);
   }

   private void dropHookedAt(Vec3 pos) {
      for (int id : this.hooked) {
         if (this.level().getEntity(id) instanceof ItemEntity item && item.isAlive()) {
            item.setPos(pos.x, pos.y + 0.2, pos.z);
            item.setNoGravity(false);
            item.setDeltaMovement(0.0, 0.1, 0.0);
            item.setPickUpDelay(0);
         }
      }

      this.hooked.clear();
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   protected void readAdditionalSaveData(ValueInput input) {
      String s = input.getStringOr("Owner", "");
      this.ownerUUID = s.isEmpty() ? null : UUID.fromString(s);
      this.maxDistance = input.getDoubleOr("MaxDistance", 24.0);
      this.traveled = input.getDoubleOr("Traveled", 0.0);
      this.firework = input.read("Firework", ItemStack.CODEC).orElse(ItemStack.EMPTY);
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      if (this.ownerUUID != null) {
         output.putString("Owner", this.ownerUUID.toString());
      }

      output.putDouble("MaxDistance", this.maxDistance);
      output.putDouble("Traveled", this.traveled);
      if (!this.firework.isEmpty()) {
         output.store("Firework", ItemStack.CODEC, this.firework);
      }
   }
}
