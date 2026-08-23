package net.dabicco.devouringstorms.entity.withered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.dabicco.devouringstorms.BowelsGravity;
import net.dabicco.devouringstorms.ModAdvancements;
import net.dabicco.devouringstorms.ModItems;
import net.dabicco.devouringstorms.ModSounds;
import net.dabicco.devouringstorms.config.WitherStormConfigs;
import net.dabicco.devouringstorms.config.WitherStormWorldConfig;
import net.dabicco.devouringstorms.entity.ModEntityTypes;
import net.dabicco.devouringstorms.entity.WitherSickness;
import net.dabicco.devouringstorms.network.WitheredCastPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WitheredMobs {
   public static final int ABILITY_NONE = 0;
   public static final int ABILITY_TELEKINESIS = 1;
   public static final int ABILITY_ORB = 2;
   public static final int ABILITY_ARROWS = 3;
   public static final int ABILITY_SKULLS = 4;
   public static final int ABILITY_SLAM = 5;
   public static final int ABILITY_SPIKES = 6;
   public static final int ABILITY_CAGE = 7;
   public static final int ABILITY_SIPHON = 8;
   private static final int MISSING_EVICT_TICKS = 2400;
   private static final double CAST_RANGE = 30.0;
   private static final int COMMAND_REFRESH = 20;
   private static final int TELEKINESIS_WINDUP = 46;
   private static final int TELEKINESIS_THROW_GAP = 9;
   private static final int ORB_LENGTH = 110;
   private static final int SKULLS_LENGTH = 85;
   private static final int SLAM_CHARGE = 22;
   private static final int SLAM_LENGTH = 70;
   private static final int SPIKES_LENGTH = 74;
   private static final int SPIKE_GAP = 4;
   private static final int SPIKE_COUNT = 12;
   private static final int CAGE_LENGTH = 90;
   private static final int CAGE_BUILD_AT = 12;
   private static final int CAGE_RISE_TICKS = 6;
   private static final double CAGE_RADIUS = 2.0;
   private static final int SIPHON_LENGTH = 100;
   private static final Identifier WITHERED_HEALTH = Identifier.fromNamespaceAndPath("devouringstorms", "withered_health");
   private static final Identifier WITHERED_ARMOR = Identifier.fromNamespaceAndPath("devouringstorms", "withered_armor");
   private static final Identifier WITHERED_KNOCKBACK = Identifier.fromNamespaceAndPath("devouringstorms", "withered_knockback");
   private static final Identifier WITHERED_DAMAGE = Identifier.fromNamespaceAndPath("devouringstorms", "withered_damage");
   private static final Identifier WITHERED_FOLLOW = Identifier.fromNamespaceAndPath("devouringstorms", "withered_follow");
   private static final int RANGED_VOLLEY_GAP = 26;
   private static final Map<UUID, WitheredMobs.Withered> STATES = new HashMap<>();
   public static final int ORB_RISE_TICKS = 18;
   public static final double ORB_REACH = 16.0;
   private static final int SWERVE_TICKS = 14;
   private static final int RETURN_TICKS = 55;
   private static final int ARROW_SCAN_EVERY = 3;
   private static final double ARROW_ARCHER_RANGE = 52.0;
   private static final String[] SPIKE_BLOCKS = new String[]{"deepslate", "blackstone", "sculk", "tuff", "obsidian", "basalt"};

   private WitheredMobs() {
   }

   private static int castCooldown(RandomSource random) {
      int base = 45 + random.nextInt(45);
      return Math.min(base + 4 * Math.max(0, STATES.size() - 1), 150);
   }

   public static boolean isWithered(LivingEntity mob) {
      return STATES.containsKey(mob.getUUID());
   }

   public static boolean canTurn(LivingEntity mob) {
      return mob instanceof Monster;
   }

   public static boolean hasRoom(ServerLevel level, LivingEntity mob) {
      if (BowelsGravity.isBowels(level)) {
         return true;
      } else {
         WitherStormWorldConfig cfg = WitherStormConfigs.get(level);
         if (cfg.witheredMobs == 0) {
            return false;
         } else if (STATES.size() >= cfg.witheredMax) {
            return false;
         } else {
            return !level.canSeeSky(mob.blockPosition()) ? caveCount() < cfg.witheredMaxCaves : true;
         }
      }
   }

   private static int caveCount() {
      int n = 0;

      for (WitheredMobs.Withered w : STATES.values()) {
         if (w.fromCave) {
            n++;
         }
      }

      return n;
   }

   public static boolean turn(ServerLevel level, LivingEntity mob) {
      if (STATES.containsKey(mob.getUUID())) {
         return true;
      } else if (!canTurn(mob)) {
         return false;
      } else if (!hasRoom(level, mob)) {
         return false;
      } else {
         WitheredMobs.Withered fresh = new WitheredMobs.Withered();
         fresh.fromCave = !level.canSeeSky(mob.blockPosition());
         STATES.put(mob.getUUID(), fresh);
         mob.removeEffect(MobEffects.WITHER);
         boolean archer = mob instanceof RangedAttackMob;
         modify(mob, Attributes.MAX_HEALTH, WITHERED_HEALTH, archer ? -8.0 : 14.0);
         modify(mob, Attributes.ARMOR, WITHERED_ARMOR, archer ? 0.0 : 2.0);
         modify(mob, Attributes.KNOCKBACK_RESISTANCE, WITHERED_KNOCKBACK, 1.0);
         modify(mob, Attributes.ATTACK_DAMAGE, WITHERED_DAMAGE, 7.0);
         modify(mob, Attributes.FOLLOW_RANGE, WITHERED_FOLLOW, 24.0);
         mob.setHealth(Math.max(1.0F, mob.getMaxHealth()));
         level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.INFECTED_MOB, SoundSource.HOSTILE, 0.7F, 1.6F);
         level.sendParticles(ParticleTypes.SCULK_SOUL, mob.getX(), mob.getY() + (double)mob.getBbHeight() * 0.6, mob.getZ(), 30, 0.5, 0.6, 0.5, 0.02);
         return true;
      }
   }

   private static boolean holdsProjectileWeapon(Mob mob) {
      return mob.getMainHandItem().getItem() instanceof ProjectileWeaponItem || mob.getOffhandItem().getItem() instanceof ProjectileWeaponItem;
   }

   private static void modify(LivingEntity mob, Holder<Attribute> attribute, Identifier id, double amount) {
      AttributeInstance instance = mob.getAttribute(attribute);
      if (instance != null) {
         instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, Operation.ADD_VALUE));
      }
   }

   public static void serverTick(ServerLevel level) {
      if (!STATES.isEmpty()) {
         boolean enabled = WitherStormConfigs.get(level).witheredMobs != 0;
         Iterator<Entry<UUID, WitheredMobs.Withered>> it = STATES.entrySet().iterator();

         while (it.hasNext()) {
            Entry<UUID, WitheredMobs.Withered> entry = it.next();
            WitheredMobs.Withered w = entry.getValue();
            Entity e = level.getEntity(entry.getKey());
            if (e instanceof LivingEntity) {
               LivingEntity mob = (LivingEntity)e;
               w.missing = 0;
               if (mob.isAlive() && !mob.isRemoved() && enabled) {
                  tickOne(level, mob, w);
               } else {
                  endAbility(level, mob, w);
                  it.remove();
               }
            } else if (w.creeperArmed) {
               witheredCreeperBlast(level, w.lastX, w.lastY, w.lastZ);
               it.remove();
            } else if (++w.missing > 2400) {
               it.remove();
            }
         }
      }
   }

   public static void clear() {
      STATES.clear();
   }

   public static void onDeath(LivingEntity mob, DamageSource cause) {
      WitheredMobs.Withered w = STATES.get(mob.getUUID());
      if (w != null) {
         if (mob.level() instanceof ServerLevel level) {
            if (killedByPlayer(cause)) {
               dropFragments(level, w, mob.getRandom(), mob.getX(), mob.getY() + (double)mob.getBbHeight() * 0.5, mob.getZ());
            } else {
               w.dropped = true;
            }

            if (mob instanceof Creeper) {
               witheredCreeperBlast(level, mob.getX(), mob.getY(), mob.getZ());
            }
         }
      }
   }

   private static boolean killedByPlayer(DamageSource cause) {
      return cause == null ? false : cause.getEntity() instanceof Player || cause.getDirectEntity() instanceof Player;
   }

   private static void dropFragments(ServerLevel level, WitheredMobs.Withered w, RandomSource random, double x, double y, double z) {
      if (!w.dropped) {
         w.dropped = true;
         int count = random.nextInt(3);
         if (count > 0) {
            for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, new AABB(x - 24.0, y - 24.0, z - 24.0, x + 24.0, y + 24.0, z + 24.0))) {
               ModAdvancements.grant(p, "reap_what_it_sows");
            }
         }

         for (int i = 0; i < count; i++) {
            ItemEntity drop = new ItemEntity(level, x, y, z, new ItemStack(ModItems.WITHER_FRAGMENT));
            drop.setDeltaMovement(random.nextGaussian() * 0.06, 0.18 + random.nextDouble() * 0.08, random.nextGaussian() * 0.06);
            level.addFreshEntity(drop);
         }

         level.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 16, 0.3, 0.4, 0.3, 0.02);
      }
   }

   private static void witheredCreeperBlast(ServerLevel level, double x, double y, double z) {
      level.explode(null, x, y + 0.4, z, 6.5F, true, ExplosionInteraction.MOB);
      AreaEffectCloud cloud = new AreaEffectCloud(level, x, y, z);
      cloud.setRadius(5.0F);
      cloud.setDuration(400);
      cloud.setRadiusOnUse(-0.4F);
      cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
      cloud.setPotionDurationScale(1.0F);
      cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 240, 1));
      level.addFreshEntity(cloud);
      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.5, z, 90, 2.5, 1.2, 2.5, 0.1);
      level.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 0.5, z, 40, 2.0, 1.0, 2.0, 0.05);
   }

   private static void tickOne(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      w.lastX = mob.getX();
      w.lastY = mob.getY();
      w.lastZ = mob.getZ();
      if (mob instanceof Creeper creeper && creeper.getSwellDir() > 0) {
         w.creeperArmed = true;
      }

      if (mob.tickCount % 20 == 0) {
         WitherSickness.keepWithered(mob);
         mob.removeEffect(MobEffects.WITHER);
      }

      if (mob.tickCount % 4 == 0) {
         level.sendParticles(
            ParticleTypes.SCULK_SOUL,
            mob.getX(),
            mob.getY() + (double)mob.getBbHeight() * 0.55,
            mob.getZ(),
            2,
            (double)mob.getBbWidth() * 0.5,
            (double)mob.getBbHeight() * 0.4,
            (double)mob.getBbWidth() * 0.5,
            0.005
         );
      }

      if (mob instanceof RangedAttackMob ranged && mob.tickCount % 26 == 0 && mob instanceof Mob asMob && holdsProjectileWeapon(asMob)) {
         LivingEntity victim = asMob.getTarget();
         if (victim != null && victim.isAlive() && mob.hasLineOfSight(victim) && mob.distanceToSqr(victim) < 900.0) {
            ranged.performRangedAttack(victim, 1.6F);
         }
      }

      tickArrows(level, mob, w);
      if (w.ability != 0) {
         w.abilityTicks++;
         if (w.abilityTicks % 20 == 0) {
            broadcast(mob, w, w.ability, Math.max(20, w.abilityLength - w.abilityTicks), commandFor(level, mob, w, w.ability));
         }
         boolean done = switch (w.ability) {
            case 1 -> tickTelekinesis(level, mob, w);
            case 2 -> tickOrb(level, mob, w);
            default -> true;
            case 4 -> tickSkulls(level, mob, w);
            case 5 -> tickSlam(level, mob, w);
            case 6 -> tickSpikes(level, mob, w);
            case 7 -> tickCage(level, mob, w);
            case 8 -> tickSiphon(level, mob, w);
         };
         if (done) {
            endAbility(level, mob, w);
            w.cooldown = castCooldown(mob.getRandom());
         }
      } else if (--w.cooldown <= 0) {
         w.cooldown = 40;
         Player target = nearestTarget(level, mob);
         if (target != null) {
            w.targetId = target.getUUID();
            w.targetEntityId = target.getId();
            int[] pool = BowelsGravity.isBowels(level) ? new int[]{4, 7} : new int[]{1, 2, 4, 5, 6, 7, 8};
            int idx = mob.getRandom().nextInt(pool.length);
            if (pool[idx] == w.lastAbility) {
               idx = (idx + 1) % pool.length;
            }

            int pick = pool[idx];
            w.ability = pick;
            w.lastAbility = pick;
            w.abilityTicks = 0;
            w.spinPhase = 0.0;
            w.spikesPlaced = 0;
            w.slamHit.clear();
            switch (pick) {
               case 1:
                  beginTelekinesis(level, mob, w);
                  break;
               case 2:
                  beginOrb(level, mob, w);
               case 3:
               default:
                  break;
               case 4:
                  beginSkulls(level, mob, w);
                  break;
               case 5:
                  beginSlam(level, mob, w);
                  break;
               case 6:
                  beginSpikes(level, mob, w, target);
                  break;
               case 7:
                  beginCage(level, mob, w);
                  break;
               case 8:
                  beginSiphon(level, mob, w);
            }
         }
      }
   }

   private static void endAbility(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      for (WitheredBlockEntity block : w.held) {
         if (block.isAlive() && !block.isFlung()) {
            block.fling(new Vec3(0.0, -0.2, 0.0));
         }
      }

      w.held.clear();
      w.cageStartY.clear();
      w.cageTargetY.clear();
      w.ability = 0;
      w.abilityTicks = 0;
      broadcast(mob, w, 0, 0, "");
   }

   private static Player nearestTarget(ServerLevel level, LivingEntity mob) {
      Player best = null;
      double bestSqr = 900.0;

      for (Player p : level.players()) {
         if (!p.isCreative() && !p.isSpectator() && p.isAlive()) {
            double dSqr = p.distanceToSqr(mob);
            if (dSqr < bestSqr && mob.hasLineOfSight(p)) {
               bestSqr = dSqr;
               best = p;
            }
         }
      }

      return best;
   }

   private static Player target(ServerLevel level, WitheredMobs.Withered w) {
      return w.targetId == null ? null : level.getPlayerByUUID(w.targetId);
   }

   private static void beginTelekinesis(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      List<BlockPos> picks = surfaceBlocksAround(level, mob, 6);
      if (picks.isEmpty()) {
         w.ability = 0;
         w.cooldown = 60;
      } else {
         for (BlockPos pos : picks) {
            BlockState state = level.getBlockState(pos);
            w.liftedBlock = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            WitheredBlockEntity block = new WitheredBlockEntity(ModEntityTypes.WITHERED_BLOCK, level);
            block.setBlockState(state);
            block.setOwnerId(mob.getId());
            block.setPos((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
            level.addFreshEntity(block);
            w.held.add(block);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.sendParticles(
               new BlockParticleOption(ParticleTypes.BLOCK, state),
               (double)pos.getX() + 0.5,
               (double)pos.getY() + 0.5,
               (double)pos.getZ() + 0.5,
               18,
               0.3,
               0.2,
               0.3,
               0.08
            );
         }

         w.nextThrowAt = 46;
         w.abilityLength = 46 + w.held.size() * 9 + 25;
         level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 0.6F, 1.5F);
         broadcast(mob, w, 1, w.abilityLength, commandFor(level, mob, w, 1));
      }
   }

   private static List<BlockPos> surfaceBlocksAround(ServerLevel level, LivingEntity mob, int want) {
      List<BlockPos> picks = new ArrayList<>();
      RandomSource random = mob.getRandom();
      BlockPos origin = mob.blockPosition();

      for (int attempt = 0; attempt < 60 && picks.size() < want; attempt++) {
         int dx = random.nextInt(9) - 4;
         int dz = random.nextInt(9) - 4;
         BlockPos floor = groundUnder(level, origin.getX() + dx, origin.getY(), origin.getZ() + dz);
         if (floor != null && !picks.contains(floor)) {
            picks.add(floor);
         }
      }

      return picks;
   }

   private static BlockPos groundUnder(ServerLevel level, int x, int startY, int z) {
      MutableBlockPos cursor = new MutableBlockPos();

      for (int dy = 1; dy >= -4; dy--) {
         cursor.set(x, startY + dy, z);
         if (isLiftable(level, cursor)) {
            return cursor.immutable();
         }
      }

      return null;
   }

   private static boolean isLiftable(ServerLevel level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (state.isAir()) {
         return false;
      } else if (!state.getFluidState().isEmpty()) {
         return false;
      } else if (state.hasBlockEntity()) {
         return false;
      } else {
         float hardness = state.getDestroySpeed(level, pos);
         return hardness < 0.0F ? false : state.isSolidRender();
      }
   }

   private static boolean tickTelekinesis(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      int t = w.abilityTicks;
      Vec3 centre = mob.position().add(0.0, (double)mob.getBbHeight() + 1.1, 0.0);
      double windUp = Math.min(1.0, (double)t / 46.0);
      w.spinPhase += 0.09 + 0.42 * windUp * windUp;
      double gather = Mth.clamp((double)t / 22.0, 0.0, 1.0);
      int n = Math.max(1, w.held.size());

      for (int i = 0; i < w.held.size(); i++) {
         WitheredBlockEntity block = w.held.get(i);
         if (block.isAlive()) {
            double slot = (Math.PI * 2) * (double)i / (double)n;
            double angle = w.spinPhase + slot;
            double radius = Mth.lerp(gather, 3.4, 1.9);
            double bob = Math.sin(w.spinPhase * 0.7 + slot) * 0.55;
            Vec3 orbit = centre.add(Math.cos(angle) * radius, bob, Math.sin(angle) * radius);
            Vec3 next = block.position().add(orbit.subtract(block.position()).scale(0.28));
            block.moveToHeld(next);
         }
      }

      if (t % 6 == 0) {
         level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, centre.x, centre.y, centre.z, 4, 1.4, 0.5, 1.4, 0.0);
      }

      if (t >= w.nextThrowAt && !w.held.isEmpty()) {
         w.nextThrowAt = t + 9;
         throwOne(level, mob, w);
      }

      return w.held.isEmpty() && t >= 46;
   }

   private static void throwOne(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      WitheredBlockEntity block = w.held.remove(0);
      if (block.isAlive()) {
         Player p = target(level, w);
         Vec3 aim;
         if (p != null) {
            Vec3 lead = p.getEyePosition().add(p.getDeltaMovement().scale(7.0));
            aim = lead.subtract(block.position()).normalize();
         } else {
            aim = new Vec3(mob.getRandom().nextGaussian(), 0.2, mob.getRandom().nextGaussian()).normalize();
         }

         double spread = 0.06;
         aim = aim.add(mob.getRandom().nextGaussian() * spread, mob.getRandom().nextGaussian() * spread, mob.getRandom().nextGaussian() * spread).normalize();
         block.fling(aim.scale(1.2));
         level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.55F, 1.5F);
         broadcast(mob, w, 1, Math.max(20, w.abilityLength - w.abilityTicks), commandFor(level, mob, w, 1));
      }
   }

   private static void beginOrb(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      w.abilityLength = 110;
      level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_BEACON_ACTIVATE, SoundSource.HOSTILE, 0.5F, 1.8F);
      broadcast(mob, w, 2, 110, commandFor(level, mob, w, 2));
   }

   private static boolean tickOrb(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      int t = w.abilityTicks;
      Vec3 orb = orbPos(mob, (float)t);
      if (t < 18) {
         if (t % 2 == 0) {
            level.sendParticles(ParticleTypes.SCULK_SOUL, orb.x, orb.y, orb.z, 3, 0.15, 0.15, 0.15, 0.01);
         }

         return false;
      } else {
         Player p = target(level, w);
         if (p != null && p.isAlive() && !p.isCreative() && !p.isSpectator() && mob.distanceToSqr(p) < 256.0) {
            Vec3 toMob = mob.position().subtract(p.position());
            double horiz = Math.sqrt(toMob.x * toMob.x + toMob.z * toMob.z);
            if (horiz > 1.6) {
               Vec3 v = p.getDeltaMovement();
               double pull = 0.13;
               p.setDeltaMovement(v.x * 0.82 + toMob.x / horiz * pull, v.y, v.z * 0.82 + toMob.z / horiz * pull);
               p.hurtMarked = true;
               if (p instanceof ServerPlayer sp) {
                  sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
               }
            }

            Vec3 to = p.position().add(0.0, (double)p.getBbHeight() * 0.5, 0.0);
            Vec3 span = to.subtract(orb);
            int steps = Mth.clamp((int)(span.length() * 1.5), 3, 26);
            double slide = (double)(t % 10) / 10.0;

            for (int i = 0; i < steps; i++) {
               Vec3 at = orb.add(span.scale(((double)i + slide) / (double)steps));
               level.sendParticles(ParticleTypes.SCULK_SOUL, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
         }

         if (t % 3 == 0) {
            double a = (double)t * 0.9;
            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, orb.x + Math.cos(a) * 0.8, orb.y, orb.z + Math.sin(a) * 0.8, 1, 0.0, 0.0, 0.0, 0.0);
         }

         return t >= 110;
      }
   }

   public static Vec3 orbPos(LivingEntity mob, float ticks) {
      float rise = Mth.clamp(ticks / 18.0F, 0.0F, 1.0F);
      double eased = 1.0 - Math.pow(1.0 - (double)rise, 3.0);
      double from = (double)mob.getBbHeight() * 0.55;
      double to = (double)mob.getBbHeight() + 2.3;
      return mob.position().add(0.0, Mth.lerp(eased, from, to), 0.0);
   }

   private static void beginSkulls(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      w.abilityLength = 85;
      level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_WITHER_AMBIENT, SoundSource.HOSTILE, 0.6F, 1.7F);
      broadcast(mob, w, 4, 85, commandFor(level, mob, w, 4));
   }

   private static boolean tickSkulls(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      int t = w.abilityTicks;
      Vec3 above = mob.position().add(0.0, (double)mob.getBbHeight() + 2.1, 0.0);
      if (t % 25 < 12 && t % 3 == 0) {
         level.sendParticles(ParticleTypes.SMOKE, above.x, above.y, above.z, 6, 0.4, 0.3, 0.4, 0.01);
      }

      if (t % 25 == 14) {
         Player p = target(level, w);
         if (p != null) {
            Vec3 spawn = above.add((mob.getRandom().nextDouble() - 0.5) * 1.4, 0.0, (mob.getRandom().nextDouble() - 0.5) * 1.4);
            Vec3 dir = p.getEyePosition().subtract(spawn).normalize();
            WitherSkull skull = new WitherSkull(level, mob, dir);
            skull.setOwner(mob);
            skull.setPos(spawn.x, spawn.y, spawn.z);
            level.addFreshEntity(skull);
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_WITHER_SHOOT, SoundSource.HOSTILE, 0.8F, 1.5F);
         }
      }

      return t >= 85;
   }

   private static void beginSlam(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      w.abilityLength = 70;
      level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 0.9F, 0.7F);
      broadcast(mob, w, 5, 70, commandFor(level, mob, w, 5));
   }

   private static boolean tickSlam(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      int t = w.abilityTicks;
      if (t < 22) {
         if (t % 2 == 0) {
            double a = (double)t * 0.8;
            level.sendParticles(
               ParticleTypes.SCULK_CHARGE_POP, mob.getX() + Math.cos(a) * 1.8, mob.getY() + 0.2, mob.getZ() + Math.sin(a) * 1.8, 2, 0.1, 0.4, 0.1, 0.02
            );
         }

         return false;
      } else {
         if (t == 22) {
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.2F, 0.6F);
         }

         double radius = (double)(t - 22) * 0.85;
         double maxRadius = 14.0;
         if (radius > maxRadius) {
            return t >= 70;
         } else {
            BlockState floor = level.getBlockState(mob.blockPosition().below());
            if (!floor.isAir()) {
               for (int i = 0; i < 26; i++) {
                  double a = (Math.PI * 2) * (double)i / 26.0;
                  level.sendParticles(
                     new BlockParticleOption(ParticleTypes.BLOCK, floor),
                     mob.getX() + Math.cos(a) * radius,
                     mob.getY() + 0.3,
                     mob.getZ() + Math.sin(a) * radius,
                     2,
                     0.1,
                     0.15,
                     0.1,
                     0.06
                  );
               }
            }

            for (Player p : level.players()) {
               if (!p.isCreative() && !p.isSpectator() && p.isAlive() && !w.slamHit.contains(p.getUUID())) {
                  double dx = p.getX() - mob.getX();
                  double dz = p.getZ() - mob.getZ();
                  double horiz = Math.sqrt(dx * dx + dz * dz);
                  if (!(Math.abs(horiz - radius) > 1.6) && !(Math.abs(p.getY() - mob.getY()) > 4.0)) {
                     w.slamHit.add(p.getUUID());
                     p.hurtServer(level, level.damageSources().mobAttack(mob), 5.0F);
                     Vec3 out = horiz < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(dx / horiz, 0.0, dz / horiz);
                     p.setDeltaMovement(p.getDeltaMovement().add(out.scale(0.75)).add(0.0, 0.55, 0.0));
                     p.fallDistance = 0.0;
                     p.hurtMarked = true;
                     if (p instanceof ServerPlayer sp) {
                        sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
                     }
                  }
               }
            }

            return t >= 70;
         }
      }
   }

   private static void beginSpikes(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w, Player target) {
      Vec3 dir = target.position().subtract(mob.position());
      dir = new Vec3(dir.x, 0.0, dir.z);
      w.spikeDir = dir.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : dir.normalize();
      w.abilityLength = 74;
      level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 0.7F, 1.1F);
      broadcast(mob, w, 6, 74, commandFor(level, mob, w, 6));
   }

   private static boolean tickSpikes(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      int t = w.abilityTicks;
      if (t % 4 == 0 && w.spikesPlaced < 12) {
         double dist = 1.8 + (double)w.spikesPlaced * 1.9;
         w.spikesPlaced++;
         Vec3 at = mob.position().add(w.spikeDir.scale(dist));
         BlockPos surface = groundUnder(level, Mth.floor(at.x), mob.blockPosition().getY(), Mth.floor(at.z));
         if (surface == null) {
            return t >= 74;
         } else {
            BlockState state = level.getBlockState(surface);
            WitheredBlockEntity shard = new WitheredBlockEntity(ModEntityTypes.WITHERED_BLOCK, level);
            shard.setBlockState(state);
            shard.setOwnerId(mob.getId());
            shard.setPos((double)surface.getX() + 0.5, (double)surface.getY() + 1.05, (double)surface.getZ() + 0.5);
            level.addFreshEntity(shard);
            shard.fling(new Vec3(w.spikeDir.x * 0.06, 0.72 + mob.getRandom().nextDouble() * 0.14, w.spikeDir.z * 0.06));
            level.sendParticles(
               new BlockParticleOption(ParticleTypes.BLOCK, state),
               (double)surface.getX() + 0.5,
               (double)surface.getY() + 1.0,
               (double)surface.getZ() + 0.5,
               22,
               0.35,
               0.15,
               0.35,
               0.16
            );
            level.playSound(
               null, (double)surface.getX(), (double)surface.getY(), (double)surface.getZ(), ModSounds.W_STONE_BREAK, SoundSource.HOSTILE, 1.1F, 0.55F
            );
            return t >= 74;
         }
      } else {
         return t >= 74;
      }
   }

   private static void beginCage(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      w.abilityLength = 90;
      level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 0.8F, 0.9F);
      broadcast(mob, w, 7, 90, commandFor(level, mob, w, 7));
   }

   private static boolean tickCage(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      int t = w.abilityTicks;
      Player p = target(level, w);
      if (p == null) {
         return true;
      } else {
         if (t == 12) {
            int bars = Math.max(8, (int)Math.ceil(Math.PI * 5));

            for (int i = 0; i < bars; i++) {
               double a = (Math.PI * 2) * (double)i / (double)bars;
               int bx = Mth.floor(p.getX() + Math.cos(a) * 2.0);
               int bz = Mth.floor(p.getZ() + Math.sin(a) * 2.0);
               BlockPos floor = groundUnder(level, bx, p.blockPosition().getY(), bz);
               if (floor != null) {
                  BlockState state = level.getBlockState(floor);

                  for (int h = 0; h < 2; h++) {
                     WitheredBlockEntity bar = new WitheredBlockEntity(ModEntityTypes.WITHERED_BLOCK, level);
                     bar.setBlockState(state);
                     bar.setOwnerId(mob.getId());
                     bar.setSolid(true);
                     bar.setPos((double)floor.getX() + 0.5, (double)floor.getY() + 0.05, (double)floor.getZ() + 0.5);
                     level.addFreshEntity(bar);
                     w.held.add(bar);
                     w.cageStartY.add((double)floor.getY() + 0.05);
                     w.cageTargetY.add((double)floor.getY() + 1.05 + (double)h);
                  }

                  level.sendParticles(
                     new BlockParticleOption(ParticleTypes.BLOCK, state),
                     (double)floor.getX() + 0.5,
                     (double)floor.getY() + 1.0,
                     (double)floor.getZ() + 0.5,
                     14,
                     0.3,
                     0.1,
                     0.3,
                     0.12
                  );
               }
            }

            level.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.W_STONE_BREAK, SoundSource.HOSTILE, 1.4F, 0.5F);
         }

         int since = t - 12;
         if (since > 0 && since <= 6) {
            double k = (double)since / 6.0;
            double eased = 1.0 - Math.pow(1.0 - k, 3.0);

            for (int ix = 0; ix < w.held.size() && ix < w.cageTargetY.size(); ix++) {
               WitheredBlockEntity bar = w.held.get(ix);
               if (bar.isAlive()) {
                  double targetY = w.cageTargetY.get(ix);
                  double startY = w.cageStartY.get(ix);
                  bar.moveToHeld(new Vec3(bar.getX(), Mth.lerp(eased, startY, targetY), bar.getZ()));
               }
            }

            if (since % 2 == 0) {
               for (WitheredBlockEntity bar : w.held) {
                  if (bar.isAlive()) {
                     level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, bar.getX(), bar.getY(), bar.getZ(), 1, 0.15, 0.05, 0.15, 0.0);
                  }
               }
            }
         }

         if (t > 18 && t % 8 == 0) {
            for (WitheredBlockEntity barx : w.held) {
               if (barx.isAlive()) {
                  level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, barx.getX(), barx.getY() + 0.5, barx.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
               }
            }
         }

         if (t >= 90) {
            for (WitheredBlockEntity barxx : w.held) {
               if (barxx.isAlive() && !barxx.isFlung()) {
                  Vec3 out = barxx.position().subtract(p.position());
                  out = out.lengthSqr() < 1.0E-4 ? new Vec3(0.0, -0.2, 0.0) : out.normalize().scale(0.35);
                  barxx.fling(out);
               }
            }

            w.held.clear();
            return true;
         } else {
            return false;
         }
      }
   }

   private static void beginSiphon(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      w.abilityLength = 100;
      level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_BEACON_ACTIVATE, SoundSource.HOSTILE, 0.7F, 0.6F);
      broadcast(mob, w, 8, 100, commandFor(level, mob, w, 8));
   }

   private static boolean tickSiphon(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      int t = w.abilityTicks;
      Player p = target(level, w);
      if (p != null && p.isAlive() && mob.hasLineOfSight(p) && !(mob.distanceToSqr(p) > 676.0)) {
         Vec3 from = mob.position().add(0.0, (double)mob.getBbHeight() * 0.7, 0.0);
         Vec3 to = p.position().add(0.0, (double)p.getBbHeight() * 0.5, 0.0);
         Vec3 span = to.subtract(from);
         int steps = Mth.clamp((int)(span.length() * 2.0), 4, 40);

         for (int i = 0; i <= steps; i++) {
            Vec3 at = from.add(span.scale((double)i / (double)steps));
            level.sendParticles(ParticleTypes.SCULK_SOUL, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.0);
         }

         if (t % 20 == 0 && t > 0) {
            p.hurtServer(level, level.damageSources().mobAttack(mob), 3.0F);
            mob.heal(4.0F);
            level.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.W_AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.7F, 0.5F);
         }

         return t >= 100;
      } else {
         return true;
      }
   }

   private static void tickArrows(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w) {
      if (w.arrowAnnounceCooldown > 0) {
         w.arrowAnnounceCooldown--;
      }

      Vec3 centre = mob.position().add(0.0, (double)mob.getBbHeight() * 0.5, 0.0);
      boolean archerNearby = false;

      for (Player p : level.players()) {
         if (!p.isSpectator() && p.distanceToSqr(mob) < 2704.0) {
            archerNearby = true;
            break;
         }
      }

      if (archerNearby && mob.tickCount % 3 == 0) {
         AABB scan = mob.getBoundingBox().inflate(13.0);

         for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, scan)) {
            if (!w.caught.containsKey(arrow) && arrow.isAlive() && arrow.getOwner() != mob) {
               Vec3 vel = arrow.getDeltaMovement();
               if (!(vel.lengthSqr() < 0.04)) {
                  Vec3 toMob = centre.subtract(arrow.position());
                  if (!(toMob.lengthSqr() < 1.0E-4) && !(vel.normalize().dot(toMob.normalize()) < 0.8)) {
                     w.caught.put(arrow, new int[]{0});
                     arrow.setNoPhysics(true);
                     arrow.setCritArrow(false);
                     if (w.arrowAnnounceCooldown == 0 && w.ability == 0) {
                        w.arrowAnnounceCooldown = 60;
                        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.W_AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0F, 0.6F);
                        broadcast(mob, w, 3, 70, commandFor(level, mob, w, 3));
                     }
                  }
               }
            }
         }
      }

      if (!w.caught.isEmpty()) {
         if (w.ability == 0 && mob.tickCount % 20 == 0) {
            broadcast(mob, w, 3, 70, commandFor(level, mob, w, 3));
         }

         Iterator<Entry<AbstractArrow, int[]>> it = w.caught.entrySet().iterator();

         while (it.hasNext()) {
            Entry<AbstractArrow, int[]> e = it.next();
            AbstractArrow arrowx = e.getKey();
            int phase = e.getValue()[0]++;
            if (arrowx.isAlive() && phase <= 69) {
               Vec3 vel = arrowx.getDeltaMovement();
               double speed = Math.max(vel.length(), 0.6);
               if (phase < 14) {
                  Vec3 away = arrowx.position().subtract(centre);
                  if (away.lengthSqr() < 1.0E-4) {
                     away = new Vec3(0.0, 1.0, 0.0);
                  }

                  Vec3 steer = vel.normalize().add(away.normalize().scale(0.38)).add(0.0, 0.03, 0.0);
                  arrowx.setDeltaMovement(steer.normalize().scale(speed));
               } else {
                  Entity shooter = arrowx.getOwner();
                  if (shooter == null || !shooter.isAlive()) {
                     arrowx.setNoPhysics(false);
                     it.remove();
                     continue;
                  }

                  if (phase == 14) {
                     arrowx.setNoPhysics(false);
                     arrowx.setOwner(mob);
                     arrowx.setCritArrow(true);
                     level.playSound(null, arrowx.getX(), arrowx.getY(), arrowx.getZ(), ModSounds.W_ARROW_SHOOT, SoundSource.HOSTILE, 0.8F, 0.6F);
                  }

                  Vec3 home = shooter.getEyePosition().subtract(arrowx.position());
                  if (home.lengthSqr() > 1.0E-4) {
                     Vec3 steer = vel.normalize().scale(0.82).add(home.normalize().scale(0.18));
                     arrowx.setDeltaMovement(steer.normalize().scale(Math.max(speed, 1.1)));
                  }
               }

               arrowx.hurtMarked = true;
               level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, arrowx.getX(), arrowx.getY(), arrowx.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            } else {
               if (arrowx.isAlive()) {
                  arrowx.setNoPhysics(false);
               }

               it.remove();
            }
         }
      }
   }

   private static String commandFor(ServerLevel level, LivingEntity mob, WitheredMobs.Withered w, int ability) {
      RandomSource r = mob.getRandom();

      return switch (ability) {
         case 1 -> w.held.isEmpty()
         ? String.format(
            Locale.ROOT,
            "summon falling_block ~%.1f ~%.1f ~%.1f {Motion:[%.2f,%.2f,%.2f]}",
            gauss(r, 3.0),
            1.0 + r.nextDouble() * 2.0,
            gauss(r, 3.0),
            gauss(r, 1.2),
            0.2 + r.nextDouble() * 0.6,
            gauss(r, 1.2)
         )
         : String.format(
            Locale.ROOT,
            "fill ~-%d ~-%d ~-%d ~%d ~%d ~%d air replace %s",
            2 + r.nextInt(4),
            1 + r.nextInt(3),
            2 + r.nextInt(4),
            2 + r.nextInt(4),
            1 + r.nextInt(2),
            2 + r.nextInt(4),
            w.liftedBlock
         );
         case 2 -> String.format(Locale.ROOT, "effect give @a[distance=..%d] levitation %d %d true", 5 + r.nextInt(4), 2 + r.nextInt(6), r.nextInt(2));
         case 3 -> String.format(
         Locale.ROOT,
         "execute as @e[type=arrow,distance=..%d] run tp @s ^%.1f ^%.1f ^-%.1f",
         9 + r.nextInt(6),
         gauss(r, 1.5),
         gauss(r, 1.0),
         1.0 + r.nextDouble() * 3.0
      );
         case 4 -> String.format(
         Locale.ROOT,
         "summon wither_skull ~%.1f ~%.1f ~%.1f {direction:[%.2f,%.2f,%.2f]}",
         gauss(r, 1.5),
         2.0 + r.nextDouble() * 2.0,
         gauss(r, 1.5),
         gauss(r, 1.0),
         gauss(r, 0.5),
         gauss(r, 1.0)
      );
         case 5 -> String.format(Locale.ROOT, "execute at @s run damage @a[distance=..%d] %d minecraft:explosion", 6 + r.nextInt(10), 4 + r.nextInt(6));
         case 6 -> String.format(
         Locale.ROOT, "setblock ~%d ~%d ~%d %s destroy", r.nextInt(15) - 7, r.nextInt(3), r.nextInt(15) - 7, SPIKE_BLOCKS[r.nextInt(SPIKE_BLOCKS.length)]
      );
         case 7 -> String.format(
         Locale.ROOT,
         "fill ~-%d ~ ~-%d ~%d ~%d ~%d %s hollow",
         2 + r.nextInt(2),
         2 + r.nextInt(2),
         2 + r.nextInt(2),
         1 + r.nextInt(3),
         2 + r.nextInt(2),
         SPIKE_BLOCKS[r.nextInt(SPIKE_BLOCKS.length)]
      );
         case 8 -> String.format(Locale.ROOT, "execute at @s run damage @p[distance=..%d] %d minecraft:magic by @s", 12 + r.nextInt(14), 2 + r.nextInt(4));
         default -> "";
      };
   }

   private static double gauss(RandomSource r, double scale) {
      return Mth.clamp(r.nextGaussian(), -2.5, 2.5) * scale;
   }

   private static void broadcast(LivingEntity mob, WitheredMobs.Withered w, int ability, int duration, String command) {
      WitheredCastPayload payload = new WitheredCastPayload(mob.getId(), ability, duration, command, w.targetEntityId);

      for (ServerPlayer player : PlayerLookup.tracking(mob)) {
         ServerPlayNetworking.send(player, payload);
      }
   }

   private static final class Withered {
      int cooldown = 60 + (int)(Math.random() * 80.0);
      int ability = 0;
      int abilityTicks;
      int abilityLength;
      int lastAbility = -1;
      UUID targetId;
      int targetEntityId = -1;
      final List<WitheredBlockEntity> held = new ArrayList<>();
      double spinPhase;
      int nextThrowAt;
      String liftedBlock = "minecraft:stone";
      final List<Double> cageStartY = new ArrayList<>();
      final List<Double> cageTargetY = new ArrayList<>();
      Vec3 spikeDir = Vec3.ZERO;
      int spikesPlaced;
      final Set<UUID> slamHit = new HashSet<>();
      final Map<AbstractArrow, int[]> caught = new IdentityHashMap<>();
      int arrowAnnounceCooldown;
      int missing;
      boolean dropped;
      boolean fromCave;
      double lastX;
      double lastY;
      double lastZ;
      boolean creeperArmed;
   }
}
