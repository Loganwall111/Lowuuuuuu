package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
 * TONYA? — the echo remaining. "the fields remember all four of us."
 * She stands in the Echo Fields answering slightly out of phase with everything that happens.
 * Harmless. Probably. During the quiet she circles the platform and says things backwards
 * into the wind.
 */
public class TonyaEntity extends PathAwareEntity {

    private int nextChatAt = 300;

    public TonyaEntity(EntityType<? extends PathAwareEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("§b§oTonya?"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createTonyaAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.2)
            .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.45));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 5.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount < nextChatAt) return;
        nextChatAt = this.tickCount + 380 + this.getRandom().nextInt(420);
        var near = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0));
        if (near.isEmpty()) return;
        String line = ModTexts.TONYA_LINES.get(this.getRandom().nextInt(ModTexts.TONYA_LINES.size()));
        for (Player p : near) p.sendSystemMessage(Component.literal(line));
    }
}
