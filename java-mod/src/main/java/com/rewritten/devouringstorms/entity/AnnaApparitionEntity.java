package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModItems;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.LookUtil;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathAwareEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * ANNA. She appears where you are about to look, smiles like a memory of someone who was
 * never there, and dissolves if you try to prove it.
 *
 * "Anna isn't real. This world is an illusion."
 */
public class AnnaApparitionEntity extends PathAwareEntity {

    private static final int DISSOLVE_GAZE = 24; // ticks of sustained eye contact before she breaks

    private int gazeTicks;
    private int giggleAt = 200;

    public AnnaApparitionEntity(EntityType<? extends PathAwareEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAnnaAttributes() {
        return PathAwareEntity.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 32.0f));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true; // you cannot kill something that isn't real
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        var players = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(26.0),
            p -> p.isAlive() && !p.isSpectator());
        ServerPlayer nearest = null;
        double best = Double.MAX_VALUE;
        for (Player p : players) {
            if (p instanceof ServerPlayer sp && p.distanceToSqr(this) < best) {
                nearest = sp;
                best = p.distanceToSqr(this);
            }
        }
        if (nearest == null) {
            this.gazeTicks = 0;
            return;
        }

        // soft static, from far away
        if (--this.giggleAt <= 0) {
            this.giggleAt = 160 + this.getRandom().nextInt(240);
            level.playSound(null, this, ModSounds.ANNA_GIGGLE, SoundSource.NEUTRAL, 0.6f, 1.1f);
            level.sendParticles(ModParticles.GLITCH, this.getX(), this.getY() + 1.0, this.getZ(), 4, 0.3, 0.5, 0.3, 0.02);
        }

        boolean gazing = LookUtil.isGazing(nearest, this, 40.0);
        if (gazing || Math.sqrt(best) < 3.5) {
            if (++this.gazeTicks >= DISSOLVE_GAZE) {
                dissolve(level, nearest);
            }
        } else {
            this.gazeTicks = Math.max(0, this.gazeTicks - 1);
        }
    }

    /** She never dissolves without leaving something behind. Proof that she was never there. */
    private void dissolve(ServerLevel level, ServerPlayer observer) {
        observer.sendSystemMessage(Component.literal(
            ModTexts.ANNA_LINES.get(this.getRandom().nextInt(ModTexts.ANNA_LINES.size()))));
        level.playSound(null, this, ModSounds.GLITCH, SoundSource.NEUTRAL, 2.0f, 1.0f);
        level.sendParticles(ModParticles.GLITCH, this.getX(), this.getY() + 1.0, this.getZ(), 60, 0.4, 0.8, 0.4, 0.08);
        this.spawnAtLocation(ModItems.MEMORY_FRAGMENT);
        if (this.getRandom().nextFloat() < 0.5f) {
            // Schedule IV — The Apparition. She wants the vault found.
            this.spawnAtLocation(ModItems.SCHEDULE_4);
        }
        this.discard();
    }
}
