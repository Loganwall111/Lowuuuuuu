package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModStatusEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * DECAY BLOCK. "Then the plague came."
 * A creeping bloom of corrupted matter: it spreads through soil, stone and wood,
 * and it is not kind to things that stand in it. The Amulet of Decay keeps it out of your blood.
 */
public class DecayBlock extends Block {

    private static final int CONVERSION_ATTEMPTS = 4;

    public DecayBlock(Properties props) {
        super(props);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // creep into neighbouring terrain
        for (int i = 0; i < CONVERSION_ATTEMPTS; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            BlockState toReplace = level.getBlockState(target);
            BlockState replacement = convert(toReplace);
            if (replacement != null) {
                level.setBlock(target, replacement, 3);
            }
        }
        // sometimes the bloom advances visibly
        if (random.nextInt(4) == 0) {
            BlockPos front = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            BlockState there = level.getBlockState(front);
            if (!there.isAir() && there.getDestroySpeed(level, front) >= 0 && convert(there) == null
                && !there.is(ModBlocks.DECAY_BLOCK) && random.nextInt(3) == 0
                && level.getBlockState(front.above()).isAir() && front.distSqr(pos) <= 4.0) {
                level.setBlock(front, ModBlocks.DECAY_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    private BlockState convert(BlockState state) {
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.PODZOL)
            || state.is(Blocks.MYCELIUM) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
            return ModBlocks.DECAYED_SOIL.defaultBlockState();
        }
        if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.COBBLESTONE)
            || state.is(Blocks.MOSSY_COBBLESTONE) || state.is(BlockTags.BASE_STONE_NETHER)) {
            return ModBlocks.DECAYED_STONE.defaultBlockState();
        }
        if (state.is(BlockTags.LOGS)) {
            return ModBlocks.ROT_LOG.defaultBlockState();
        }
        if (state.is(Blocks.GRAVEL) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            return ModBlocks.DECAYED_SOIL.defaultBlockState();
        }
        return null;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier) {
        if (!(level instanceof ServerLevel) || !(entity instanceof LivingEntity living)) return;
        living.addEffect(new MobEffectInstance(ModStatusEffects.DECAY, 100, 0));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.MYCELIUM,
                pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(),
                0, 0.01, 0);
        }
    }
}
