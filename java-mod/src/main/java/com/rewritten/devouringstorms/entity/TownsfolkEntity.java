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
 * ENDERTONIANS. The townsfolk who never left. They sweep the plaza, tend the banners,
 * and tell you quiet things if you stop beside them: where the rot came in, what the
 * Preacher won't say out loud, which house still has a chest that bites.
 */
public class TownsfolkEntity extends PathAwareEntity {

    private int nextChatAt = 400;

    public TownsfolkEntity(EntityType<? extends PathAwareEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("§7Endertonian"));
        this.setCustomNameVisible(false);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createTownsfolkAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.26)
            .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 5.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount < nextChatAt) return;
        nextChatAt = this.tickCount + 420 + this.getRandom().nextInt(480);
        var near = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8.0));
        if (near.isEmpty()) return;
        String line = ModTexts.TOWNSFOLK_LINES.get(this.getRandom().nextInt(ModTexts.TOWNSFOLK_LINES.size()));
        for (Player p : near) {
            p.sendSystemMessage(Component.literal(line));
        }
    }
}
