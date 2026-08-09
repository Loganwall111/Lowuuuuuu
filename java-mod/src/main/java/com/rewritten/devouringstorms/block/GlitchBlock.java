package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * GLITCH BLOCK — what the Monstrosity leaves behind.
 * Not a block, an opinion about a block: it refuses to hold still, drops nothing,
 * glows like an untuned channel, and whispers the static at any audience.
 */
public class GlitchBlock extends Block {

    public GlitchBlock(Properties props) {
        super(props);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ModParticles.GLITCH,
                pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                pos.getZ() + random.nextDouble(),
                (random.nextDouble() - 0.5) * 0.3, 0.15 + random.nextDouble() * 0.2,
                (random.nextDouble() - 0.5) * 0.3);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(30) == 0) {
            level.playSound(null, pos, ModSounds.GLITCH, SoundSource.BLOCKS, 0.4f, 0.7f + random.nextFloat() * 0.8f);
        }
    }
}
