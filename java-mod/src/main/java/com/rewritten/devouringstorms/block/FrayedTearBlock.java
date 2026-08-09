package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.RiftTravel;
import com.rewritten.devouringstorms.world.ModDimensions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A FRAYED TEAR. Not a portal someone built — a place where the quarantine's stitching
 * came loose on its own. Step through and you ride the ring: Decayed Reality → The Fray →
 * Echo Fields → home again. "A poorly tear in the fabric of reality."
 * Dwell briefly inside to cross; a cooldown stops the ring from whiplashing you back.
 */
public class FrayedTearBlock extends Block {

    private static final int DWELL_TICKS = 15;
    private static final long COOLDOWN_TICKS = 100L;

    private static final Map<UUID, Integer> DWELL = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN_UNTIL = new HashMap<>();

    public FrayedTearBlock(Properties props) {
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

        ResourceKey<Level> destinationKey = nextOnRing(server.dimension());
        ServerLevel destination = server.getServer().getLevel(destinationKey);
        if (destination == null) return;

        server.playSound(null, pos, ModSounds.RIFT_OPEN, SoundSource.BLOCKS, 1.4f, 1.45f);
        RiftTravel.travel(entity, destinationKey);
        // first crossing into a ring stop assembles its waystation (Travis waits in the Fray,
        // Tonya in the Echo Fields)
        if (entity instanceof net.minecraft.world.entity.player.Player) {
            com.rewritten.devouringstorms.util.Multiverse.ensurePocket(destination);
        }
    }

    /** The ring route: decayed → fray → echo → decayed. Anything else rides home first. */
    private static ResourceKey<Level> nextOnRing(ResourceKey<Level> from) {
        if (from == ModDimensions.DECAYED_LEVEL_KEY) return ModDimensions.FRAY_LEVEL_KEY;
        if (from == ModDimensions.FRAY_LEVEL_KEY) return ModDimensions.ECHO_LEVEL_KEY;
        return ModDimensions.DECAYED_LEVEL_KEY;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL,
                pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() * 1.4, pos.getZ() + random.nextDouble(),
                0, 0.05, 0);
        }
        if (random.nextInt(30) == 0) {
            level.addParticle(com.rewritten.devouringstorms.registry.ModParticles.GLITCH,
                pos.getX() + random.nextDouble(), pos.getY() + 0.5, pos.getZ() + random.nextDouble(),
                0, 0.02, 0);
        }
    }
}
