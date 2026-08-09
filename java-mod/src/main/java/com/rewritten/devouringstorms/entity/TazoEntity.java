package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.entity.ai.TazoFollowGoal;
import com.rewritten.devouringstorms.util.ModTexts;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.PathAwareEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;

/**
 * TAZO. "Alongside Tazo, you will enter a world where nothing is what it seems."
 * A survivor of the Decayed Reality. Show it a Memory Fragment — proof that Anna was not
 * a dream you are having — and it will walk beside you to the end of the storm.
 */
public class TazoEntity extends PathAwareEntity implements OwnableEntity {

    private static final EntityDataAccessor<String> VARIANT =
        SynchedEntityData.defineId(TazoEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
        SynchedEntityData.defineId(TazoEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private int nextChatAt = 240;

    public TazoEntity(EntityType<? extends PathAwareEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("§bTazo"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createTazoAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.MOVEMENT_SPEED, 0.34)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(VARIANT, "teal");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TazoFollowGoal(this, 1.1, 6.0f, 3.0f));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    // ------------------------------------------------------------------------- ownership

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        UUID uuid = getOwnerUUID();
        if (uuid == null || !(this.level() instanceof ServerLevel level)) return null;
        var entity = level.getEntity(uuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    public boolean isBonded() {
        return getOwnerUUID() != null;
    }

    // ------------------------------------------------------------------------- interaction

    /** Transient: whether this Tazo already gave up Schedule V. One gift per companion. */
    private boolean scheduleGifted = false;

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(this.level() instanceof ServerLevel level)) return InteractionResult.PASS;

        if (!isBonded() && stack.is(com.rewritten.devouringstorms.registry.ModItems.MEMORY_FRAGMENT)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.entityData.set(OWNER_UUID, Optional.of(player.getUUID()));
            this.setTarget(null);
            level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.2, this.getZ(), 8, 0.4, 0.4, 0.4, 0.02);
            level.playSound(null, this, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0f, 1.2f);
            player.sendSystemMessage(Component.literal("§b<Tazo>§r You... you REMEMBER? Then I'm not alone. §oLead on.§r"));
            if (!scheduleGifted) {
                // Schedule V — The Companion. Tazo kept one schedule safe the whole time.
                scheduleGifted = true;
                this.spawnAtLocation(com.rewritten.devouringstorms.registry.ModItems.SCHEDULE_5);
                player.sendSystemMessage(Component.literal(
                    "§b<Tazo>§r Hold onto this. §oA schedule. I kept it safe since the banners fell.§r"));
            }
            return InteractionResult.SUCCESS;
        }
        if (isBonded() && player.getUUID().equals(getOwnerUUID()) && stack.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                ModTexts.TAZO_LINES.get(this.getRandom().nextInt(ModTexts.TAZO_LINES.size()))));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // ------------------------------------------------------------------------- behaviour

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);

        // fight what threatens its person
        LivingEntity owner = getOwner();
        if (owner != null && this.tickCount % 20 == 0) {
            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker != null && attacker.isAlive() && attacker != this && this.distanceTo(attacker) < 24.0) {
                this.setTarget(attacker);
            } else if (this.getTarget() == null) {
                var hostiles = level.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(16.0),
                    e -> e.isAlive() && !(e instanceof MassgEntity) && e != this);
                if (!hostiles.isEmpty()) {
                    this.setTarget(hostiles.get(0));
                }
            }
        }

        // ambient dialogue
        if (owner instanceof ServerPlayer sp && --this.nextChatAt <= 0) {
            this.nextChatAt = 1400 + this.getRandom().nextInt(900);
            sp.sendSystemMessage(Component.literal(
                ModTexts.TAZO_LINES.get(this.getRandom().nextInt(ModTexts.TAZO_LINES.size()))));
        }
    }

    @Override
    public void tick() {
        super.tick();
        // teleport to owner if left impossibly behind
        if (this.level() instanceof ServerLevel level) {
            LivingEntity owner = getOwner();
            if (owner != null && this.distanceTo(owner) > 40.0 && !owner.isSpectator()) {
                this.teleportTo(level, owner.getX(), owner.getY(), owner.getZ(),
                    java.util.Set.of(), this.getYRot(), this.getXRot(), false);
            }
        }
    }

    @Override
    protected void finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                 net.minecraft.world.entity.EntitySpawnReason spawnReason,
                                 SpawnGroupData spawnGroupData) {
        super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        if (!this.entityData.get(VARIANT).equals("teal") || this.random.nextInt(4) == 0) return;
        // the storm bred tazos out of many lights: mostly teal, sometimes rose/dusk/ivory
        this.entityData.set(VARIANT, switch (this.random.nextInt(7)) {
            case 0 -> "rose";
            case 1 -> "dusk";
            default -> "teal";
        });
    }

    public String getVariantName() {
        return this.entityData.get(VARIANT);
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("TazoVariant", this.entityData.get(VARIANT));
        UUID uuid = getOwnerUUID();
        if (uuid != null) output.putString("OwnerUUID", uuid.toString());
        output.putInt("NextChatAt", this.nextChatAt);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(VARIANT, input.getStringOr("TazoVariant", "teal"));
        input.getString("OwnerUUID").ifPresent(s -> {
            try {
                this.entityData.set(OWNER_UUID, Optional.of(UUID.fromString(s)));
            } catch (IllegalArgumentException ignored) {
                // malformed save data — leave unbonded
            }
        });
        this.nextChatAt = input.getIntOr("NextChatAt", 240);
    }
}
