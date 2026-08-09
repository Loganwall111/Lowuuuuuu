package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.registry.ModStatusEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.level.block.Blocks;

/**
 * THE MONSTROSITY. Moustache first, apology never.
 * Where it walks, reality loses the argument: the ground turns to colour-static, the sky
 * re-colours itself like a broken broadcast — and everyone watching gets the broadcast.
 */
public class MonstrosityEntity extends Monster {

    private int nextSpreadAt = 60;
    private int nextBroadcastAt = 40;

    public MonstrosityEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createMonstrosityAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 120.0)
            .add(Attributes.MOVEMENT_SPEED, 0.32)
            .add(Attributes.ATTACK_DAMAGE, 9.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
            .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        var random = this.getRandom();

        // ---- the colourful overtake: ground and sky both lose their plot ----
        if (this.tickCount >= nextSpreadAt) {
            nextSpreadAt = this.tickCount + 50 + random.nextInt(50);
            int r = 7 + random.nextInt(4);
            for (int i = 0; i < 9; i++) {
                BlockPos pos = this.blockPosition().offset(
                    random.nextInt(r * 2 + 1) - r,
                    random.nextInt(6) - 3,
                    random.nextInt(r * 2 + 1) - r);
                var state = level.getBlockState(pos);
                if (state.isAir() || state.is(ModBlocks.GLITCH_BLOCK) || state.is(Blocks.OBSIDIAN)
                    || state.is(Blocks.BEDROCK) || state.is(ModBlocks.CORRUPTED_COMMAND_BLOCK)) continue;
                level.setBlock(pos, ModBlocks.GLITCH_BLOCK.defaultBlockState(), 3);
                level.sendParticles(ModParticles.GLITCH, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                    6, 0.35, 0.35, 0.35, 0.03);
            }
            level.playSound(null, this, ModSounds.GLITCH, SoundSource.HOSTILE, 1.8f,
                0.5f + random.nextFloat() * 1.0f);
        }

        // ---- the sky follows: everyone's retinas get the channel ----
        if (this.tickCount >= nextBroadcastAt) {
            nextBroadcastAt = this.tickCount + 24;
            for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(36.0))) {
                p.addEffect(new MobEffectInstance(ModStatusEffects.OVERTAKEN, 60, 0, false, false, true));
            }
        }

        // identifier sparkle — the moustache remembers being broadcast
        if (this.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 1.6, this.getZ(),
                1, 0.35, 0.35, 0.35, 0.02);
        }
    }
}
