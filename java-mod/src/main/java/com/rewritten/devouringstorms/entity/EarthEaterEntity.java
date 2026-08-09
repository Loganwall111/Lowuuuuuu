package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * THE EARTH EATER. A god that regards planets as a tasting menu.
 * It doesn't hate you — you're not on the menu; the ground is. Hung in the sky of the
 * Multiverse Age, it lowers a slow jaw over the world and takes a whole slice out of it.
 * Somewhere in its shadow, another planet quietly decides not to be a home.
 */
public class EarthEaterEntity extends Monster {

    private int nextBiteAt = 400;

    public EarthEaterEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setNoAi(true);
    }

    public static AttributeSupplier.Builder createEaterAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 1500.0)
            .add(Attributes.MOVEMENT_SPEED, 0.03)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 256.0)
            .add(Attributes.SCALE, 6.0);
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        var random = this.getRandom();

        // the rumble that precedes the appetite
        if (this.tickCount % 47 == 0) {
            level.playSound(null, this, ModSounds.AMBIENT_RIFT_HUM, SoundSource.HOSTILE, 4.0f, 0.2f);
        }
        if (this.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.END_ROD,
                this.getX() + random.nextGaussian() * 5.0, this.getY() - 1.0, this.getZ() + random.nextGaussian() * 5.0,
                1, 0.2, 0.3, 0.2, 0.01);
        }

        // ---- THE BITE: a whole mouthful of the world, chewed slowly ----
        if (this.tickCount >= nextBiteAt) {
            nextBiteAt = this.tickCount + 900 + random.nextInt(700);
            BlockPos base = this.blockPosition().below(4);
            int r = 12;
            for (BlockPos pos : BlockPos.betweenClosed(base.offset(-r, -6, -r), base.offset(r, 4, r))) {
                double dx = pos.getX() - base.getX(), dz = pos.getZ() - base.getZ();
                if (dx * dx + dz * dz > r * r * (0.6 + random.nextDouble() * 0.4)) continue;
                var state = level.getBlockState(pos);
                if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.OBSIDIAN)) continue;
                level.removeBlock(pos, false);
                if (random.nextInt(6) == 0) {
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.03);
                }
            }
            level.playSound(null, this, ModSounds.MASSG_DEVOUR, SoundSource.HOSTILE, 4.0f, 0.3f);
            level.sendParticles(ModParticles.GLITCH, this.getX(), this.getY() - 2.0, this.getZ(),
                60, 6.0, 2.0, 6.0, 0.05);
            for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(96.0))) {
                p.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§8§oThe jaw closes over your sky. The planet beneath it is... smaller."));
            }
        }

        // slow drift, like weather with opinions
        if (this.tickCount % 60 == 0) {
            this.setDeltaMovement(random.nextGaussian() * 0.04, 0.02 + random.nextGaussian() * 0.015,
                random.nextGaussian() * 0.04);
        }
    }
}
