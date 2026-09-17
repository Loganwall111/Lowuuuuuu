package net.mcsm.sift.entity;

import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Talking creature entities (mcsm:void_dweller)
 * Highly stylized, cartoonish fish-like silhouettes that glide smoothly through Gel Horizon
 * Right-clicking opens overlay screen with show-inspired lore texts and glowing amethyst-purple chat logs
 * Using animated fade-in typewriting effect
 */
public class VoidDwellerEntity extends FlyingMob {

    private static final EntityDataAccessor<Integer> DWELLER_TYPE = SynchedEntityData.defineId(VoidDwellerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_TALKING = SynchedEntityData.defineId(VoidDwellerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TALK_ANIM = SynchedEntityData.defineId(VoidDwellerEntity.class, EntityDataSerializers.INT);

    private int talkCooldown = 0;
    private int idleBob = 0;
    private float prevBob = 0;

    public enum DwellerType {
        ANGLER("angler", "The Angler", 0xFFAA55FF),
        LUMEN("lumen", "Lumen Keeper", 0xFF55FFAA),
        ECHO("echo", "Echo Child", 0xFF55AAFF),
        SIFT_SAGE("sage", "Sift Sage", 0xFFFF55AA);

        public final String id;
        public final String displayName;
        public final int glowColor;

        DwellerType(String id, String name, int color) {
            this.id = id;
            this.displayName = name;
            this.glowColor = color;
        }
    }

    // Lore texts - show-inspired
    private static final List<String> LORE_LINES = List.of(
        "You fell... but you didn't land. The Sift remembers every fall.",
        "Below bedrock, the world forgets what it was supposed to be. We remember for it.",
        "Those rifts? They're not tears. They're windows. Something's looking back.",
        "The water here... it doesn't want to be water. It wants to be sky. Listen to it hum.",
        "I was like you once. Walked on grass. Now I float, and the void hums lullabies.",
        "Don't chase the ghost whales. Let them circle you. They know you're lost.",
        "Tier 2 maze shifts when you're not looking. The sponge breathes. Orange to pink... like a heartbeat.",
        "The Sift was never meant to be found. But you opened it. Brave the unknown, huh?",
        "Every layer has a sky. Every sky has a lie. The real sky is below us.",
        "Hear that echo? That's not your voice. That's the Sift learning your name."
    );

    public VoidDwellerEntity(EntityType<? extends FlyingMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 15, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 40.0D)
            .add(Attributes.FLYING_SPEED, 0.8D)
            .add(Attributes.MOVEMENT_SPEED, 0.4D)
            .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DWELLER_TYPE, 0);
        this.entityData.define(IS_TALKING, false);
        this.entityData.define(TALK_ANIM, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 12f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(4, new GlideInGelHorizonGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    public void tick() {
        super.tick();
        prevBob = idleBob;
        idleBob++;

        if (talkCooldown > 0) {
            talkCooldown--;
            if (talkCooldown == 0) {
                this.entityData.set(IS_TALKING, false);
            }
        }

        // Gentle bobbing float - not walking, floating
        double bob = Math.sin(idleBob * 0.05) * 0.02;
        this.setDeltaMovement(this.getDeltaMovement().add(0, bob, 0));

        // Keep in void dimension bounds
        int y = (int) this.getY();
        if (!McsmVoidTiers.isInVoidDimension(y)) {
            double targetY = (McsmVoidTiers.TIER_1_GEL_HORIZON_TOP + McsmVoidTiers.TIER_5_GEL_VOID_BOTTOM) / 2.0;
            this.setDeltaMovement(this.getDeltaMovement().add(0, (targetY - y) * 0.005, 0));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            // Open dialogue overlay with typewriting effect
            openDialogueScreen(player);
            return InteractionResult.SUCCESS;
        } else {
            // Server side - trigger talking state and play sound
            this.entityData.set(IS_TALKING, true);
            this.entityData.set(TALK_ANIM, this.random.nextInt(3));
            talkCooldown = 100;
            this.playSound(SoundEvents.VILLAGER_AMBIENT, 1.0f, 0.8f + random.nextFloat() * 0.4f);

            // Send lore message with glowing amethyst-purple chat logs
            String lore = getRandomLore();
            Component msg = Component.literal("[" + getDwellerType().displayName + "] ")
                .withStyle(s -> s.withColor(getDwellerType().glowColor))
                .append(Component.literal(lore).withStyle(s -> s.withColor(0xFFCC88FF)));
            player.sendSystemMessage(msg);

            return InteractionResult.CONSUME;
        }
    }

    private void openDialogueScreen(Player player) {
        // Client hook - would open custom screen
        // For now, trigger particle and sound
        if (MinecraftAccessor.hasInstance()) {
            // Trigger overlay via McsmSiftClient
            net.mcsm.sift.client.McsmSiftClient.openDwellerDialogue(this);
        }
    }

    // Workaround for client check without direct Minecraft import in common
    private static class MinecraftAccessor {
        static boolean hasInstance() {
            try {
                Class.forName("net.minecraft.client.Minecraft");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    public DwellerType getDwellerType() {
        int idx = this.entityData.get(DWELLER_TYPE);
        DwellerType[] vals = DwellerType.values();
        return vals[Mth.clamp(idx, 0, vals.length - 1)];
    }

    public boolean isTalking() {
        return this.entityData.get(IS_TALKING);
    }

    public float getBobAnimation(float partial) {
        return Mth.lerp(partial, prevBob, idleBob) * 0.05f;
    }

    public float getTalkAnimation(float partial) {
        if (!isTalking()) return 0f;
        return Mth.sin((idleBob + partial) * 0.5f) * 0.3f;
    }

    private String getRandomLore() {
        return LORE_LINES.get(random.nextInt(LORE_LINES.size()));
    }

    public List<String> getAllLoreForScreen() {
        return LORE_LINES;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DwellerType", this.entityData.get(DWELLER_TYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DwellerType")) {
            this.entityData.set(DWELLER_TYPE, tag.getInt("DwellerType"));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        this.entityData.set(DWELLER_TYPE, level.getRandom().nextInt(DwellerType.values().length));
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    static class GlideInGelHorizonGoal extends Goal {
        private final VoidDwellerEntity dweller;

        GlideInGelHorizonGoal(VoidDwellerEntity dweller) {
            this.dweller = dweller;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            // Smooth gliding, not flying - float like in concept images
            if (dweller.random.nextFloat() < 0.05f) {
                double dx = (dweller.random.nextFloat() - 0.5) * 0.2;
                double dy = (dweller.random.nextFloat() - 0.5) * 0.05;
                double dz = (dweller.random.nextFloat() - 0.5) * 0.2;
                dweller.setDeltaMovement(dweller.getDeltaMovement().lerp(new net.minecraft.world.phys.Vec3(dx, dy, dz), 0.1));
            }
        }
    }
}
