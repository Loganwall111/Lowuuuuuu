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
 * E.P.A. RESEARCHER. Top floor of the biggest building in the world's most haunted field office.
 * The lab coat fits badly — they bought these in bulk on a Tuesday and nobody's been measured
 * since the quarantine. They talk in memos. Occasionally the memos talk back.
 */
public class ResearcherEntity extends PathAwareEntity {

    private int nextMemoAt = 320;

    public ResearcherEntity(EntityType<? extends PathAwareEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("§fE.P.A. Researcher"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createResearcherAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.MOVEMENT_SPEED, 0.27)
            .add(Attributes.FOLLOW_RANGE, 22.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.65));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount < nextMemoAt) return;
        nextMemoAt = this.tickCount + 420 + this.getRandom().nextInt(420);
        var near = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0));
        if (near.isEmpty()) return;
        String line = ModTexts.EPA_RESEARCH_LINES.get(this.getRandom().nextInt(ModTexts.EPA_RESEARCH_LINES.size()));
        for (Player p : near) p.sendSystemMessage(Component.literal(line));
    }
}
