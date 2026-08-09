package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.entity.ai.MassgChaseGoal;
import com.rewritten.devouringstorms.entity.ai.DevourPullGoal;
import com.rewritten.devouringstorms.entity.ai.MassgMoveControl;
import com.rewritten.devouringstorms.registry.ModDamageTypes;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.registry.ModStatusEffects;
import com.rewritten.devouringstorms.storm.MassgPhase;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * MASSG — Massive Abomination Sundering Storm Genesis.
 *
 * A living storm: it hangs in the sky, drifts towards prey, devours anything it can reach
 * (entities AND blocks), grows, sunder off living fragments — and when "killed" it only plays
 * dead. The blueprints are corrupted: unless finished with a Formidibomb, it rises again as
 * GENESIS, and the music turns critical.
 */
public class MassgEntity extends Monster {

    private static final EntityDataAccessor<Integer> PHASE =
        SynchedEntityData.defineId(MassgEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> GROWTH =
        SynchedEntityData.defineId(MassgEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> CRITICAL =
        SynchedEntityData.defineId(MassgEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DEAD_TICKS =
        SynchedEntityData.defineId(MassgEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> VARIANT =
        SynchedEntityData.defineId(MassgEntity.class, EntityDataSerializers.STRING);

    /** Health fractions at which the storm devolves (music turns critical, spawns symbionts). */
    private static final double[] DEVOLVE_THRESHOLDS = {0.75, 0.5, 0.25};
    private int nextDevolveIndex = 0;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        Component.literal(MassgPhase.SLEEPING.bossName()),
        BossEvent.BossBarColor.BLUE,
        BossEvent.BossBarOverlay.PROGRESS
    );

    public MassgEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new MassgMoveControl(this);
        this.setNoGravity(true);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createMassgAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 700.0)
            .add(Attributes.ARMOR, 10.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 256.0)
            .add(Attributes.FLYING_SPEED, 0.6)
            .add(Attributes.SCALE, MassgPhase.SLEEPING.scale());
    }

    // ------------------------------------------------------------------------------ data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, 0).define(GROWTH, 0.0f).define(CRITICAL, false).define(DEAD_TICKS, -1)
            .define(VARIANT, "classic");
    }

    /** The storm's colour-denom: classic, rose, abyssal, ivory. The variants feed differently. */
    public com.rewritten.devouringstorms.entity.MassgVariant getVariant() {
        return com.rewritten.devouringstorms.entity.MassgVariant.byName(this.entityData.get(VARIANT));
    }

    public void setVariant(com.rewritten.devouringstorms.entity.MassgVariant variant) {
        this.entityData.set(VARIANT, variant.name);
    }

    public MassgPhase getPhase() {
        return MassgPhase.byId(this.entityData.get(PHASE));
    }

    public float getGrowth() {
        return this.entityData.get(GROWTH);
    }

    public boolean isCritical() {
        return this.entityData.get(CRITICAL);
    }

    /** >= 0 while the storm is playing dead. */
    public int getDeadTicks() {
        return this.entityData.get(DEAD_TICKS);
    }

    public void setPhase(MassgPhase phase) {
        MassgPhase old = getPhase();
        if (old == phase) return;
        this.entityData.set(PHASE, phase.id());
        var scaleAttr = this.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(phase.scale());
        this.refreshDimensions();

        this.bossEvent.setColor(phase.color());
        this.bossEvent.setName(Component.literal(phase.bossName()));

        if (this.level() instanceof ServerLevel level) {
            level.playSound(null, this, ModSounds.MASSG_ROAR, SoundSource.HOSTILE, 4.0f, 0.7f + phase.ordinal() * 0.08f);
            broadcastToNearbyPlayers(level, Component.literal("§5§l" + phase.bossName()), 320.0);
            if (phase == MassgPhase.SIGNAL) {
                level.playSound(null, this, ModSounds.MASSG_AWAKENING, SoundSource.HOSTILE, 4.0f, 1.0f);
                broadcastToNearbyPlayers(level, Component.literal(ModTexts.MASSG_WAKE), 320.0);
            }
            if (phase == MassgPhase.BOWELS) {
                // phase 5.5 — the storm splits open and the bowels glow
                level.playSound(null, this, ModSounds.MASSG_DEVOLVE_STING, SoundSource.HOSTILE, 4.0f, 0.55f);
                level.playSound(null, this, ModSounds.MASSG_REBIRTH, SoundSource.HOSTILE, 3.5f, 0.65f);
                broadcastToNearbyPlayers(level, Component.literal(ModTexts.MASSG_BOWELS), 320.0);
                this.ruptureTicks = 100;   // the full rupture cinematic: split, pour, rise, shockwave
            }
        }
    }

    public void addGrowth(float amount) {
        if (getDeadTicks() >= 0) return;
        // config: infinite_growth — because the server owner asked the storm to stop. It won't.
        if (getPhase() == MassgPhase.GENESIS
            && !com.rewritten.devouringstorms.util.DevouringConfig.getBool("infinite_growth", true)) return;
        float g = this.entityData.get(GROWTH) + amount;
        if (g >= 1.0f) {
            this.entityData.set(GROWTH, 0.0f);
            setPhase(getPhase().next());
        } else {
            this.entityData.set(GROWTH, g);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("MassgPhase", getPhase().id());
        output.putFloat("MassgGrowth", getGrowth());
        output.putInt("DeadTicks", getDeadTicks());
        output.putInt("DevolveIndex", nextDevolveIndex);
        output.putInt("CoreHits", coreHits);
        output.putString("MassgVariant", this.entityData.get(VARIANT));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(PHASE, input.getIntOr("MassgPhase", 0));
        this.entityData.set(GROWTH, input.getFloatOr("MassgGrowth", 0.0f));
        this.entityData.set(DEAD_TICKS, input.getIntOr("DeadTicks", -1));
        this.nextDevolveIndex = input.getIntOr("DevolveIndex", 0);
        this.coreHits = input.getIntOr("CoreHits", 0);
        this.entityData.set(VARIANT, input.getStringOr("MassgVariant", "classic"));
        applyPhasePresentation();
    }

    private void applyPhasePresentation() {
        MassgPhase phase = getPhase();
        var scaleAttr = this.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(phase.scale());
        this.bossEvent.setColor(phase.color());
        this.bossEvent.setName(Component.literal(phase.bossName()));
    }

    // ------------------------------------------------------------------------------ behaviour

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DevourPullGoal(this));
        this.goalSelector.addGoal(2, new MassgChaseGoal(this));
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 128.0f));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // the storm does not despawn — it must be ENDED
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    /** Devour a captured entity: mobs are eaten whole; players are hurt and hurled away. */
    public void devour(LivingEntity victim) {
        if (!(this.level() instanceof ServerLevel level)) return;
        Vec3 mouth = mouthPosition();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, mouth.x, mouth.y, mouth.z, 30, 0.4, 0.4, 0.4, 0.05);
        level.sendParticles(ModParticles.GLITCH, mouth.x, mouth.y, mouth.z, 12, 0.3, 0.3, 0.3, 0.1);
        level.playSound(null, this, ModSounds.MASSG_DEVOUR, SoundSource.HOSTILE, 3.0f, 0.9f);

        if (victim instanceof ServerPlayer player) {
            player.hurtServer(level, this.damageSources().mobAttack(this), 12.0f);
            player.addEffect(new MobEffectInstance(ModStatusEffects.DECAY, 120, 0));
            Vec3 away = player.position().subtract(mouthPosition()).normalize().scale(2.2);
            player.setDeltaMovement(away.x, 0.6, away.z);
            player.hurtMarked = true;
        } else {
            victim.discard();
            addGrowth(0.035f); // feeding drives evolution
        }
    }

    /** The pull point DevourPullGoal drags victims to. */
    public Vec3 mouthPosition() {
        double s = getPhase().scale();
        return this.position().add(0.0, this.getBbHeight() * 0.35 + s, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        updateBossBar();
        if (this.level().isClientSide()) {
            spawnAmbientParticles();
            return;
        }
        serverStormTick((ServerLevel) this.level());
    }

    private void updateBossBar() {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.level() instanceof ServerLevel level && this.tickCount % 20 == 0) {
            this.bossEvent.setPlayers(
                level.players().stream()
                    .filter(p -> p.distanceToSqr(this) < 192.0 * 192.0)
                    .toList()
            );
        }
    }

    private void spawnAmbientParticles() {
        var random = this.getRandom();
        double s = getPhase().scale();
        for (int i = 0; i < (int) (2 + s); i++) {
            double ox = (random.nextDouble() - 0.5) * getBbWidth();
            double oy = (random.nextDouble() - 0.5) * getBbHeight();
            double oz = (random.nextDouble() - 0.5) * getBbWidth();
            this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                this.getX() + ox, this.getY() + oy + 1.0, this.getZ() + oz,
                -ox * 0.02, 0.02, -oz * 0.02);
        }
        if (isCritical() && random.nextInt(4) == 0) {
            this.level().addParticle(ModParticles.GLITCH,
                this.getX() + (random.nextDouble() - 0.5) * getBbWidth(),
                this.getY() + random.nextDouble() * getBbHeight(),
                this.getZ() + (random.nextDouble() - 0.5) * getBbWidth(),
                0, 0.05, 0);
        }
    }

    private void serverStormTick(ServerLevel level) {
        // ---- playing dead ----
        int deadTicks = getDeadTicks();
        if (deadTicks >= 0) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9).add(0, -0.004, 0));
            if (deadTicks == 0) {
                rebirth(level);
            } else {
                this.entityData.set(DEAD_TICKS, deadTicks - 1);
            }
            return;
        }

        // ---- passive growth: the signal strengthens ----
        MassgPhase phase = getPhase();
        if (phase == MassgPhase.SLEEPING) {
            // it wakes when observed — "MASSG IS WAKING UP."
            if (this.tickCount % 20 == 0
                && !level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(96.0)).isEmpty()) {
                addGrowth(0.02f);
            }
        } else {
            addGrowth(0.00006f * phase.ordinal());
        }

        // ---- devouring the terrain ----
        if (phase.atLeast(MassgPhase.HUNGER) && this.tickCount % 20 == 0 && !this.isSilent()) {
            absorbBlocks(level);
        }

        // ---- sundering: tearing fragments off (never from the husk) ----
        if (phase.atLeast(MassgPhase.SUNDERER) && phase.alive() && this.tickCount % 600 == 0) {
            spawnSevered(level);
        }

        // ---- tractor beams: three heads, three beams, up you come ----
        if (phase.atLeast(MassgPhase.DEVOURER) && phase.alive()) {
            tractorBeamTick(level, phase);
        }

        // ---- GENESIS: debris vortex + storm lightning (never from the husk) ----
        if (phase == MassgPhase.GENESIS) {
            genesisVortex(level);
            if (this.tickCount % 140 == 0) strikeStormLightning(level);
        }

        // ---- phase 5.5+: the exposed bowels bleed violet light ----
        if (phase.glows() && this.tickCount % 5 == 0) {
            bowelsGlow(level);
        }

        // ---- phase 5.5 rupture cinematic: split, pour, rise, shockwave ----
        if (ruptureTicks > 0) {
            ruptureTick(level);
        }

        // ---- the rend finale (Storm Killer ×3 into the husk's command block) ----
        if (rendTicks > 0) {
            rendTick(level);
            if (rendTicks <= 0) {
                this.rendKilling = true;
                this.spawnAtLocation(com.rewritten.devouringstorms.registry.ModItems.STORM_HEART);
                this.setHealth(0.0f);
                this.die(this.damageSources().generic());
            }
        }

        // ---- if you let it live, it grows. It does not stop. ----
        if (phase == MassgPhase.GENESIS && this.tickCount % 600 == 0) {
            var attr = this.getAttribute(Attributes.SCALE);
            if (attr != null && attr.getBaseValue() < 6.5) {
                attr.setBaseValue(attr.getBaseValue() + 0.08);
                this.refreshDimensions();
                if (this.getRandom().nextFloat() < 0.3f) {
                    broadcastToNearbyPlayers(level, Component.literal("§5§oTHE STORM STILL GROWS."), 320.0);
                }
            }
        }

        // ---- debris rings: the halo of the eaten world grows with it ----
        if (phase.atLeast(MassgPhase.DEVOURER) && phase.alive() && this.tickCount % 4 == 0) {
            double a = this.tickCount * 0.14;
            float radius = 4.0f + phase.ordinal() * 1.8f;
            double rx = this.getX() + Math.cos(a) * radius;
            double rz = this.getZ() + Math.sin(a) * radius;
            double ry = this.getY() + 1.0 + Math.sin(this.tickCount * 0.05) * 0.6;
            if (com.rewritten.devouringstorms.util.DevouringConfig.getBool("debris_rings", true)) {
                level.sendParticles(ParticleTypes.ASH, rx, ry, rz, 2, 0.4, 0.3, 0.4, 0.01);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, rx, ry, rz, 1, 0.2, 0.2, 0.2, 0.015);
            }
        }

        // ---- earthquakes: the world buckles under the bigger phases ----
        if (phase.atLeast(MassgPhase.SUNDERER) && phase.alive()
            && com.rewritten.devouringstorms.util.DevouringConfig.getBool("earthquakes", true)
            && this.tickCount >= nextQuakeAt) {
            nextQuakeAt = this.tickCount + 700 + this.getRandom().nextInt(900);
            earthquake(level, phase);
        }

        // ---- the belly takes the unwary: fly into the open bowels and you're inside ----
        if (phase.atLeast(MassgPhase.BOWELS) && phase.alive() && this.tickCount % 15 == 0) {
            Vec3 mouth = this.mouthPosition();
            for (var player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6.0))) {
                if (player.distanceToSqr(mouth) < 12.0 && player.getAbilities().mayfly) continue;
                if (player.distanceToSqr(mouth) < 9.0) {
                    com.rewritten.devouringstorms.util.StomachChamber.enterStomach(player, this);
                }
            }
        }

        // ---- critical state ----
        float frac = this.getHealth() / this.getMaxHealth();
        if (frac < 0.30f && !isCritical()) setCritical(level, true);
        if (frac > 0.60f && isCritical()) setCritical(level, false);

        // ---- devolution thresholds ----
        if (nextDevolveIndex < DEVOLVE_THRESHOLDS.length && frac <= DEVOLVE_THRESHOLDS[nextDevolveIndex]) {
            devolveShock(level);
            nextDevolveIndex++;
        }
    }

    /** Replace nearby terrain with nothing — it feeds. */
    private void absorbBlocks(ServerLevel level) {
        // Mappings note: the gamerule constant may be RULE_MOB_GRIEFING in newer mappings.
        if (!level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.MOB_GRIEFING)) return;
        var random = this.getRandom();
        int r = 6;
        for (int i = 0; i < 14; i++) {
            int dx = Mth.nextInt(random, -r, r);
            int dy = Mth.nextInt(random, -2, r);
            int dz = Mth.nextInt(random, -r, r);
            BlockPos pos = this.blockPosition().offset(dx, dy, dz);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(Blocks.BEDROCK) || state.getDestroySpeed(level, pos) < 0) continue;
            if (random.nextFloat() < 0.2f) {
                level.destroyBlock(pos, false);
                addGrowth(0.004f);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
            }
        }
    }

    /** The phase-5.5 rupture, staged: the belly tears, the pour, the rise, then the ring. */
    private void ruptureTick(ServerLevel level) {
        int t = 100 - ruptureTicks;
        var random = this.getRandom();
        double cx = this.getX(), cy = this.getY(), cz = this.getZ();

        if (t < 35) {
            // SPLIT — the belly tears open, debris and tissue shredding outward
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                cx + (random.nextDouble() - 0.5) * 4, cy - 2.4, cz + (random.nextDouble() - 0.5) * 4,
                5, 1.2, 0.3, 1.2, 0.05);
            level.sendParticles(ParticleTypes.WITCH, cx, cy - 2.2, cz, 6, 1.6, 0.4, 1.6, 0.08);
            if (t % 9 == 0) {
                // shed segments rain out of the storm (fence: FallingBlockEntity.fall API name)
                trySpawnSegment(level, random);
            }
        } else if (t < 65) {
            // POUR — purple liquid streams out of the split
            for (int i = 0; i < 7; i++) {
                double ox = (random.nextDouble() - 0.5) * 2.4;
                double oz = (random.nextDouble() - 0.5) * 2.4;
                level.sendParticles(ParticleTypes.WITCH, cx + ox, cy - 1.8, cz + oz, 1, 0.05, -2.2, 0.05, 0.55);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, cx + ox, cy - 1.6, cz + oz, 1, 0.02, -1.6, 0.02, 0.4);
            }
        } else if (t < 88) {
            // RISE — and then it pulls it all back in; the pour climbs the storm instead
            for (int i = 0; i < 6; i++) {
                double a = random.nextDouble() * Math.PI * 2;
                level.sendParticles(ParticleTypes.WITCH,
                    cx + Math.cos(a) * 1.4, cy - 3.0, cz + Math.sin(a) * 1.4, 1, 0.0, 2.6, 0.0, 0.5);
            }
        } else if (ruptureTicks > 2) {
            // SHOCKWAVE — one vast ring of everything it is made of
            int n = 40;
            double r = (100 - ruptureTicks) * 1.15;
            for (int i = 0; i < n; i++) {
                double a = i * (Math.PI * 2 / n);
                level.sendParticles(ParticleTypes.END_ROD,
                    cx + Math.cos(a) * r, cy - 1.0, cz + Math.sin(a) * r, 1, 0, 0, 0, 0.0);
                if (i % 2 == 0) {
                    level.sendParticles(ParticleTypes.WITCH,
                        cx + Math.cos(a) * (r * 0.8), cy - 1.4, cz + Math.sin(a) * (r * 0.8), 1, 0, 0, 0, 0.0);
                }
            }
            if (ruptureTicks == 10) {
                level.playSound(null, this, ModSounds.MASSG_ROAR, SoundSource.HOSTILE, 4.0f, 0.45f);
                broadcastToNearbyPlayers(level, Component.literal("§d§lTHE BOWELS HAVE FORMED. §oAnd the purple pours on.§r"), 480.0);
            }
        }
        ruptureTicks--;
    }

    /** Falling debris segment helper — decay blocks shed like scabs, gone on landing. */
    private void trySpawnSegment(ServerLevel level, net.minecraft.util.RandomSource random) {
        double a = random.nextDouble() * Math.PI * 2;
        double r = 2.0 + random.nextDouble() * 2.0;
        net.minecraft.core.BlockPos spawn = this.blockPosition()
            .offset((int) (Math.cos(a) * r), -2, (int) (Math.sin(a) * r));
        if (!level.getBlockState(spawn).isAir()) return;
        net.minecraft.world.entity.item.FallingBlockEntity segment = net.minecraft.world.entity.item.FallingBlockEntity.fall(
            level, spawn, com.rewritten.devouringstorms.registry.ModBlocks.DECAY_BLOCK.defaultBlockState());
        segment.disableDrop();
        segment.time = 560;   // fizzle out shortly after landing instead of littering forever
    }

    /** The rend finale: ripping rings of white and violet fire until it has nothing left. */
    private void rendTick(ServerLevel level) {
        rendTicks--;
        double cx = this.getX(), cy = this.getY() + 1.4, cz = this.getZ();
        if (rendTicks % 7 == 0) {
            int n = 26;
            double r = Math.max(1.0, (110 - rendTicks) * 0.32);
            boolean white = rendTicks % 14 == 0;
            for (int i = 0; i < n; i++) {
                double a = i * (Math.PI * 2 / n) + rendTicks * 0.11;
                level.sendParticles(white ? ParticleTypes.END_ROD : ParticleTypes.WITCH,
                    cx, cy, cz, 1, Math.cos(a) * r, 0.1, Math.sin(a) * r, 0.30);
            }
            level.playSound(null, this, ModSounds.MASSG_PULL_LOOP, SoundSource.HOSTILE, 1.8f,
                0.45f + (110 - rendTicks) * 0.008f);
        }
        if (rendTicks % 17 == 0) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** The exposed bowels: violet tissue-light bleeding out of the storm's open centre. */
    private void bowelsGlow(ServerLevel level) {
        float s = getPhase().scale();
        double r = 2.2 * s;
        var random = this.getRandom();
        double x = this.getX() + (random.nextDouble() - 0.5) * r;
        double y = this.getY() - 1.2 * s + random.nextDouble() * 1.6;
        double z = this.getZ() + (random.nextDouble() - 0.5) * r;
        level.sendParticles(ParticleTypes.WITCH, x, y, z, 2, 0.25 * s, 0.12, 0.25 * s, 0.02);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 1, 0.1, 0.1, 0.1, 0.01);
        if (this.tickCount % 120 == 0) {
            level.playSound(null, this, ModSounds.MASSG_PULL_LOOP, SoundSource.HOSTILE, 1.6f, 0.5f);
        }
    }

    private void spawnSevered(ServerLevel level) {
        var existing = level.getEntitiesOfClass(SeveredStormEntity.class, this.getBoundingBox().inflate(96.0));
        if (existing.size() >= 6) return;
        SeveredStormEntity severed = com.rewritten.devouringstorms.registry.ModEntities.SEVERED_STORM.create(level);
        if (severed == null) return;
        var random = this.getRandom();
        severed.moveTo(
            this.getX() + (random.nextDouble() - 0.5) * 12.0,
            this.getY() + 2.0,
            this.getZ() + (random.nextDouble() - 0.5) * 12.0,
            random.nextFloat() * 360.0f, 0.0f);
        level.addFreshEntity(severed);
        level.playSound(null, this, ModSounds.MASSG_DEVOUR, SoundSource.HOSTILE, 2.0f, 0.6f);
    }

    private void genesisVortex(ServerLevel level) {
        if (this.tickCount % 10 != 0) return;
        double s = getPhase().scale();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(24.0 + 6.0 * s), e -> e != this)) {
            Vec3 pull = this.position().add(0, getBbHeight() * 0.5, 0).subtract(entity.position());
            double dist = pull.length();
            if (dist < 8.0) {
                entity.hurtServer(level, ModDamageTypes.decay(level, this), 4.0f);
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(pull.normalize().scale(0.06)));
            entity.hurtMarked = true;
        }
    }

    private void strikeStormLightning(ServerLevel level) {
        var players = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(64.0));
        if (players.isEmpty()) return;
        Player target = players.get(this.getRandom().nextInt(players.size()));
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(target.getX() + (this.getRandom().nextDouble() - 0.5) * 8.0,
            target.getY(), target.getZ() + (this.getRandom().nextDouble() - 0.5) * 8.0,
            0.0f, 0.0f);
        level.addFreshEntity(bolt);
    }

    /** The storm loses a threshold of itself — and answers. */
    private void devolveShock(ServerLevel level) {
        setCritical(level, true);
        level.playSound(null, this, ModSounds.MASSG_DEVOLVE_STING, SoundSource.HOSTILE, 4.0f, 0.8f);
        level.sendParticles(ModParticles.GLITCH, this.getX(), this.getY() + 2.0, this.getZ(), 80,
            getBbWidth() * 0.5, getBbHeight() * 0.5, getBbWidth() * 0.5, 0.2);
        broadcastToNearbyPlayers(level, Component.literal("§c§l" + getPhase().bossName() + " §4IS DEVOLVING..."), 320.0);
        // spawn symbionts as living shrapnel
        for (int i = 0; i < 2; i++) {
            var symbiont = com.rewritten.devouringstorms.registry.ModEntities.WITHERED_SYMBIONT.create(level);
            if (symbiont != null) {
                symbiont.moveTo(this.getX() + (this.getRandom().nextDouble() - 0.5) * 6.0,
                    this.getY(), this.getZ() + (this.getRandom().nextDouble() - 0.5) * 6.0, 0.0f, 0.0f);
                level.addFreshEntity(symbiont);
            }
        }
        this.heal(this.getMaxHealth() * 0.08f); // it adapts — "the system was never stable"
    }

    private void setCritical(ServerLevel level, boolean critical) {
        if (isCritical() == critical) return;
        this.entityData.set(CRITICAL, critical);
        if (critical) {
            broadcastToNearbyPlayers(level, Component.literal("§4§oThe air itself is screaming."), 320.0);
        }
    }

    private void rebirth(ServerLevel level) {
        this.entityData.set(DEAD_TICKS, -1);
        this.entityData.set(PHASE, MassgPhase.GENESIS.id());
        var scaleAttr = this.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(MassgPhase.GENESIS.scale());
        this.refreshDimensions();
        this.bossEvent.setColor(MassgPhase.GENESIS.color());
        this.bossEvent.setName(Component.literal(MassgPhase.GENESIS.bossName()));
        this.setHealth(this.getMaxHealth() * 0.6f);
        level.playSound(null, this, ModSounds.MASSG_REBIRTH, SoundSource.HOSTILE, 4.0f, 0.7f);
        level.explode(this, null, null, this.getX(), this.getY(), this.getZ(), 3.0f, false, Level.ExplosionInteraction.NONE);
        broadcastToNearbyPlayers(level,
            Component.literal("§5§lTHE BLUEPRINTS WERE CORRUPTED. §r§5MASSG RISES AS GENESIS."), 320.0);
    }

    // ------------------------------------------------------------------------------ v1.3: husk & rend
    /** Storm Killer strikes on the exposed command block (husk state). Three rend it. */
    private int coreHits = 0;
    /** > 0 while the rend finale (rip-apart white energy) plays out. */
    private int rendTicks = 0;
    /** Internal: the rend sequence itself is allowed to truly kill the husk. */
    private boolean rendKilling = false;
    /** > 0 while the phase-5.5 rupture cinematic (split / liquid / shockwave) plays. */
    private int ruptureTicks = 0;

    /** The stomach-hole opens in pulses: only then can the anchored command block be struck. */
    public boolean holeOpen() {
        return getPhase() == MassgPhase.HUSK && (this.tickCount % 260) < 120;
    }

    /** The Storm Killer: the only thing that can rend the husk's anchored command block. */
    @Override
    protected net.minecraft.world.InteractionResult mobInteract(
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (!(this.level() instanceof ServerLevel level)) return net.minecraft.world.InteractionResult.PASS;
        if (getPhase() == MassgPhase.HUSK
            && stack.is(com.rewritten.devouringstorms.registry.ModItems.STORM_KILLER)) {
            if (rendTicks > 0) return net.minecraft.world.InteractionResult.CONSUME;
            if (!holeOpen()) {
                player.sendSystemMessage(Component.literal(
                    "§8The stomach-hole is sealed shut. It opens in pulses — watch for the glow."));
                return net.minecraft.world.InteractionResult.CONSUME;
            }
            applyStormKillerHit(player, level);
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /** The exposed bowels: violet tissue-light bleeding out of the storm's open centre. */

    /** Ground-shaker under SUNDERER+: rumble, dust, and a nudge that says "it's still feeding." */
    /**
     * MCSM-made-flesh: three filament beams pour out of the three heads and comb the
     * ground for prey. Anything warm caught in a beam is lifted and fed toward the maw,
     * one meal per head per sweep. The renderer paints the beams; this is the hunger physics.
     */
    private void tractorBeamTick(ServerLevel level, MassgPhase phase) {
        if (this.tickCount % 4 != 0) return;
        double range = 34.0 + phase.ordinal() * 4.0;
        float g = getGrowth();
        // the three heads: sockets ring the crown; growth widens the sweep
        float spread = (10.0f + g * 6.0f);
        Vec3[] sockets = new Vec3[] {
            this.position().add(0, 6.0, -spread),
            this.position().add(-spread * 0.87, 6.0, spread * 0.5),
            this.position().add(spread * 0.87, 6.0, spread * 0.5),
        };
        var prey = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
            this.getBoundingBox().inflate(range), e -> e != this && e.isAlive() && !(e instanceof MassgEntity));
        int head = 0;
        for (var victim : prey) {
            // each head takes the nearest payload; a victim in a beam stops belonging to the ground
            if (head >= sockets.length) break;
            Vec3 headPos = sockets[head++];
            double d = victim.position().distanceTo(headPos);
            if (d > range || d < 4.0) continue;
            Vec3 lift = headPos.subtract(victim.position()).normalize()
                .add(0, 0.55, 0).normalize().scale(0.11 + phase.ordinal() * 0.01);
            victim.setDeltaMovement(victim.getDeltaMovement().scale(0.82).add(lift));
            victim.fallDistance = 0.0f;
            victim.hurtMarked = true;
            // beam filaments: an upward rain of glitch sparks along the capture line
            if (this.tickCount % 8 == 0) {
                Vec3 mid = headPos.add(victim.position()).scale(0.5);
                level.sendParticles(ModParticles.GLITCH, mid.x, mid.y, mid.z,
                    6, d * 0.12, d * 0.12, d * 0.12, 0.02);
                level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getEyeY(), victim.getZ(),
                    2, 0.2, 0.2, 0.2, 0.01);
            }
            if (victim instanceof Player && this.tickCount % 60 == 0) {
                victim.sendSystemMessage(Component.literal("§5§oThe beam has you. The heads do not blink.§r"));
            }
        }
        if (this.tickCount % 100 == 0) {
            level.playSound(null, this, ModSounds.AMBIENT_RIFT_HUM, SoundSource.HOSTILE, 2.2f, 1.6f);
        }
    }

    private void earthquake(ServerLevel level, MassgPhase phase) {
        level.playSound(null, this, ModSounds.MASSG_ROAR, SoundSource.HOSTILE, 3.0f, 0.32f);
        var random = this.getRandom();
        for (var player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(320.0))) {
            player.push((random.nextDouble() - 0.5) * 0.2, 0.18 + random.nextDouble() * 0.08,
                (random.nextDouble() - 0.5) * 0.2);
            player.hurtMarked = true;
        }
        double cx = this.getX(), cz = this.getZ();
        for (int ring = 1; ring <= 3; ring++) {
            double r = ring * (6.0 + phase.ordinal());
            for (int i = 0; i < 28; i++) {
                double a = (i / 28.0) * Math.PI * 2 + ring;
                level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    cx + Math.cos(a) * r, this.getY() - 2.5, cz + Math.sin(a) * r,
                    3, 0.8, 0.6, 0.8, 0.04);
            }
        }
        level.sendParticles(ParticleTypes.ASH, cx, this.getY() - 1.0, cz, 200, 12, 1.5, 12, 0.05);
        broadcastToNearbyPlayers(level, Component.literal("§8§oThe ground shudders — it is still feeding."), 340.0);
    }

    /** Shared Storm Killer hit API (used by stomach-core strikes AND direct husk clicks). */
    public void applyStormKillerHit(Player player, ServerLevel level) {
        coreHits++;
        level.playSound(null, this, ModSounds.MASSG_DEVOLVE_STING, SoundSource.HOSTILE, 3.0f, 1.3f);
        level.sendParticles(ParticleTypes.END_ROD,
            this.getX(), this.getY() + 1.0, this.getZ(), 26, 0.8, 0.5, 0.8, 0.12);
        level.sendParticles(ParticleTypes.WITCH,
            this.getX(), this.getY() + 1.2, this.getZ(), 18, 0.6, 0.4, 0.6, 0.10);
        broadcastToNearbyPlayers(level, Component.literal(
            "§f§l" + coreHits + " §r§7— the Storm Killer bites the command block. " +
            (coreHits >= 3 ? "" : "§ostrike " + (3 - coreHits) + " more time" + (coreHits == 2 ? "" : "s") + "§r")), 200.0);
        if (coreHits >= 3) {
            this.rendTicks = 110;
            broadcastToNearbyPlayers(level, Component.literal(
                "§f§lTHE COMMAND BLOCK IS BREACHED. §r§d§oWhite fire rips out of the bowels.§r"), 480.0);
            level.playSound(null, this, ModSounds.MASSG_REBIRTH, SoundSource.HOSTILE, 4.0f, 1.5f);
        }
    }

    private int nextQuakeAt = 500;

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (getPhase() == MassgPhase.HUSK && !rendKilling) {
            // the command block inside keeps it intact — nothing else gets through
            return true;
        }
        if (getDeadTicks() >= 0) {
            // Playing dead: ONLY a formidibomb pierces the corrupted hide now.
            return !(source.getDirectEntity() instanceof FormidiBombEntity);
        }
        return super.isInvulnerableTo(source)
            || source.is(DamageTypes.IN_WALL)
            || source.is(DamageTypes.FALL)
            || source.is(DamageTypes.DROWN);
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel level
            && getDeadTicks() < 0
            && !rendKilling
            && !(source.getDirectEntity() instanceof FormidiBombEntity)) {
            if (getPhase() == MassgPhase.GENESIS) {
                // GENESIS falls wrong: out of the sky, into the dirt, and NOT dead.
                enterHusk(level);
            } else {
                // It does not die. It NEVER dies. It only plays dead.
                enterPlayDead(level);
            }
            this.setHealth(Math.max(this.getHealth(), 60.0f));
            return;
        }
        // True death — finished by a formidibomb, or rend by the Storm Killer.
        if (this.level() instanceof ServerLevel level) {
            level.playSound(null, this, ModSounds.MASSG_TRUE_DEATH, SoundSource.HOSTILE, 4.0f, 0.6f);
            broadcastToNearbyPlayers(level,
                Component.literal(rendKilling
                    ? "§f§lTHE STORM TEARS APART. §r§7White fire pours out of the bowels — it is over."
                    : "§a§lTHE STORM IS ENDED. §r§7...somewhere, a countdown keeps running."), 480.0);
        }
        super.die(source);
    }

    /** GENESIS, minus the sky. The corpse walks too slowly to matter, but it does not stop watching. */
    private void enterHusk(ServerLevel level) {
        setPhase(MassgPhase.HUSK);
        this.coreHits = 0;
        this.setNoAi(true);   // grounded for good — it waits to be rent, it does not hunt
        this.goalSelector.getRunningGoals().forEach(net.minecraft.world.entity.ai.goal.WrappedGoal::stop);
        level.playSound(null, this, ModSounds.MASSG_PLAY_DEAD, SoundSource.HOSTILE, 4.0f, 0.55f);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
            this.getX(), this.getY() + 1.0, this.getZ(), 20, getBbWidth() * 0.5, 0.6, getBbWidth() * 0.5, 0.1);
        broadcastToNearbyPlayers(level, Component.literal(
            "§8§lTHE STORM FALLS OUT OF THE SKY.§r §5§oIt drags itself across the dirt — the command block still holds it together.§r"), 480.0);
        broadcastToNearbyPlayers(level, Component.literal(
            "§8Only the Storm Killer can rend the heart now. The Watcher hoards them.§8"), 480.0);
    }

    private void enterPlayDead(ServerLevel level) {
        this.entityData.set(DEAD_TICKS, 600);
        this.entityData.set(CRITICAL, true);
        this.goalSelector.getRunningGoals().forEach(net.minecraft.world.entity.ai.goal.WrappedGoal::stop);
        level.playSound(null, this, ModSounds.MASSG_PLAY_DEAD, SoundSource.HOSTILE, 4.0f, 0.7f);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.0, this.getZ(), 8,
            getBbWidth() * 0.5, 1.0, getBbWidth() * 0.5, 0.1);
        broadcastToNearbyPlayers(level, Component.literal("§8§oThe storm's light fades... §r§7§oit is only sleeping. §r§4§oUse a Formidibomb. END IT."), 480.0);
    }

    private void broadcastToNearbyPlayers(ServerLevel level, Component message, double radius) {
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < radiusSqr) {
                player.sendSystemMessage(message);
            }
        }
    }
}
