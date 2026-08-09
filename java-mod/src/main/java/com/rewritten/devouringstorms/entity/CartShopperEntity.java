package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * THE CART SHOPPER. Aisle four, forever.
 * The BHS aisles hired quietly, in the dark, and then never stopped hiring. It still pushes
 * the cart the way it pushes the aisles open: with its face. It rams. It apologises. It rams.
 */
public class CartShopperEntity extends Monster {

    private int nextApologyAt = 200;

    public CartShopperEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createShopperAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 26.0)
            .add(Attributes.MOVEMENT_SPEED, 0.44)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.ATTACK_KNOCKBACK, 1.6)
            .add(Attributes.FOLLOW_RANGE, 26.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.35, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % 4 == 0) {
            level.sendParticles(ModParticles.GLITCH, this.getX(), this.getY() + 0.4, this.getZ(),
                1, 0.3, 0.2, 0.3, 0.01);
        }
        if (this.tickCount >= nextApologyAt) {
            nextApologyAt = this.tickCount + 360 + this.getRandom().nextInt(380);
            var near = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(9.0));
            if (near.isEmpty()) return;
            String line = ModTexts.BHS_LINES.get(this.getRandom().nextInt(ModTexts.BHS_LINES.size()));
            for (Player p : near) p.sendSystemMessage(Component.literal(line));
        }
    }
}
