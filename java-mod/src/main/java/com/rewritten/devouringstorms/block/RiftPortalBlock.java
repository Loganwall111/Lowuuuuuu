package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.storm.StormDirector;
import com.rewritten.devouringstorms.util.RiftTravel;
import com.rewritten.devouringstorms.world.ModDimensions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * THE RIFT. A plane of not-quite-reality. Stand in it for a heartbeat and it takes you
 * to the other side — Overworld ⟷ Decayed Reality.
 * Includes per-entity dwell + cooldown so arrival doesn't bounce you straight back.
 */
public class RiftPortalBlock extends Block {

    private static final int DWELL_TICKS = 15;
    private static final long COOLDOWN_TICKS = 100L;

    private static final Map<UUID, Integer> DWELL = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN_UNTIL = new HashMap<>();

    public RiftPortalBlock(Properties props) {
        super(props);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier applier) {
        if (!(level instanceof ServerLevel server)) return;
        UUID id = entity.getUUID();
        long now = server.getGameTime();

        Long until = COOLDOWN_UNTIL.get(id);
        if (until != null && now < until) return;

        int dwell = DWELL.merge(id, 1, Integer::sum);
        if (dwell < DWELL_TICKS) return;

        DWELL.remove(id);
        COOLDOWN_UNTIL.put(id, now + COOLDOWN_TICKS);

        var destinationKey = server.dimension() == ModDimensions.DECAYED_LEVEL_KEY
            ? Level.OVERWORLD
            : ModDimensions.DECAYED_LEVEL_KEY;
        ServerLevel destination = server.getServer().getLevel(destinationKey);
        if (destination == null) return;

        // First visit ever: the quarantine zone assembles itself.
        StormDirector.ensureSpawnPlatform(destination);

        server.playSound(null, pos, ModSounds.RIFT_OPEN, SoundSource.BLOCKS, 1.5f, 1.2f);
        RiftTravel.travel(entity, destinationKey);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // venting the void
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL,
                pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() * 1.5, pos.getZ() + random.nextDouble(),
                0, 0.06, 0);
        }
        if (random.nextInt(24) == 0) {
            level.addParticle(com.rewritten.devouringstorms.registry.ModParticles.GLITCH,
                pos.getX() + random.nextDouble(), pos.getY() + 0.5, pos.getZ() + random.nextDouble(),
                0, 0.02, 0);
        }
        if (random.nextInt(120) == 0) {
            level.playLocalSound(pos, ModSounds.AMBIENT_RIFT_HUM, SoundSource.BLOCKS, 0.4f, 1.0f, false);
        }
    }
}
