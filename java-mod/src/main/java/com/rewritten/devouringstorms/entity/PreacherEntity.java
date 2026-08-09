package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathAwareEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * THE PREACHER. "The storm is a mouth. The town is a prayer."
 * Last clergy of Endertown. It wanders the banner plaza and preaches to anyone who stops
 * to listen — and those who stand through a whole sermon leave a little less changed
 * (a small blessing: brief regeneration). Endertown keeps exactly one, at its heart.
 */
public class PreacherEntity extends PathAwareEntity {

    private int nextSermonAt = 300;

    public PreacherEntity(EntityType<? extends PathAwareEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("§5The Preacher"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createPreacherAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.22)
            .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.55));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 7.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount < nextSermonAt) return;
        nextSermonAt = this.tickCount + 360 + this.getRandom().nextInt(320);

        var near = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0));
        if (near.isEmpty()) {
            nextSermonAt = this.tickCount + 120;   // nobody to save today — try again soon
            return;
        }
        String line = ModTexts.PREACHER_LINES.get(this.getRandom().nextInt(ModTexts.PREACHER_LINES.size()));
        for (Player p : near) {
            p.sendSystemMessage(Component.literal(line));
            if (this.distanceTo(p) < 10.0) {
                // the blessing of listening out a sermon in the quarantine
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0));
            }
        }
    }
}
