package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.entity.ai.WatcherStalkGoal;
import com.rewritten.devouringstorms.registry.ModItems;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.registry.ModStatusEffects;
import com.rewritten.devouringstorms.util.LookUtil;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * THE WATCHER.
 * It does not run. It does not scream. It stands where you were not looking, and it watches.
 * Sustain eye contact too long and it leaves its mark on you — then it is gone.
 */
public class WatcherEntity extends Monster {

    private static final int GAZE_MARK_TICKS = 45;  // ~2.25 s of being observed
    private static final long BLINK_COOLDOWN = 12_000L; // world ticks between postcards home

    private Player focus;
    private int gazeTicks;
    private long blinkReadyAt;

    public WatcherEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createWatcherAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.MOVEMENT_SPEED, 0.24)
            .add(Attributes.FOLLOW_RANGE, 72.0)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WatcherStalkGoal(this));
    }

    public Player getFocus() {
        return this.focus;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
        // it is not quite... there
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        // acquire the nearest warm thing
        if (this.tickCount % 20 == 0 || (this.focus != null && !this.focus.isAlive())) {
            var players = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(64.0),
                p -> p.isAlive() && !p.isSpectator());
            this.focus = players.isEmpty() ? null : players.get(this.getRandom().nextInt(players.size()));
        }

        if (this.focus == null) return;

        // heartbeat for the very brave or the very close
        if (this.tickCount % 55 == 0 && this.distanceTo(this.focus) < 20.0f) {
            level.playSound(null, this, ModSounds.WATCHER_HEARTBEAT, SoundSource.HOSTILE, 0.9f, 0.85f);
        }

        // the gaze
        if (this.focus instanceof ServerPlayer sp && LookUtil.isGazing(this.focus, this, 80.0)) {
            if (++this.gazeTicks == GAZE_MARK_TICKS) {
                markAndVanish(level, sp);
            } else if (this.gazeTicks == 10 && this.level().getGameTime() >= this.blinkReadyAt) {
                sp.sendSystemMessage(Component.literal(ModTexts.WATCHER_GAZE));
                level.playSound(null, this, ModSounds.WATCHER_WHISPER, SoundSource.HOSTILE, 1.2f, 0.8f);
            }
        } else {
            this.gazeTicks = Math.max(0, this.gazeTicks - 2);
        }
    }

    /** Being stared at is intolerable. It marks the observer, leaves an eye behind, and leaves. */
    private void markAndVanish(ServerLevel level, ServerPlayer observer) {
        observer.addEffect(new MobEffectInstance(ModStatusEffects.GAZED, 240, 0));
        observer.sendSystemMessage(Component.literal("§8§oYou should not have watched it back."));
        vanish(level, true);
    }

    /** Relocates somewhere else in the dark — usually right after being hurt or gazed at. */
    public void vanish(ServerLevel level, boolean leaveEyeBehind) {
        level.sendParticles(ModParticles.GLITCH, this.getX(), this.getY() + 1.3, this.getZ(), 50, 0.3, 1.0, 0.3, 0.05);
        level.playSound(null, this, ModSounds.WATCHER_VANISH, SoundSource.HOSTILE, 1.5f, 1.0f);
        if (leaveEyeBehind && this.getRandom().nextFloat() < 0.5f && this.level().getGameTime() >= this.blinkReadyAt) {
            this.blinkReadyAt = this.level().getGameTime() + BLINK_COOLDOWN;
            this.spawnAtLocation(ModItems.WATCHER_EYE);
        }
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = this.getRandom().nextDouble() * Math.PI * 2.0;
            double dist = 30.0 + this.getRandom().nextDouble() * 30.0;
            double x = this.getX() + Math.cos(angle) * dist;
            double z = this.getZ() + Math.sin(angle) * dist;
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (int) Math.floor(x), (int) Math.floor(z));
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!level.getBlockState(pos).isAir()) continue;
            this.teleportTo(level, x, y, z, java.util.Set.of(), this.getYRot(), this.getXRot(), false);
            return;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // it cannot be cornered
        if (this.getRandom().nextFloat() < 0.85f) {
            vanish(level, this.getRandom().nextFloat() < 0.35f);
            return false;
        }
        return super.hurtServer(level, source, amount);
    }
}
