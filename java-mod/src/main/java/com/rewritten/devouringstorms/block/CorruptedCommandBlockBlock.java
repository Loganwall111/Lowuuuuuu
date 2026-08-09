package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.registry.ModItems;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * THE CORRUPTED COMMAND BLOCK. "The Wither Storm blueprints are corrupted."
 *
 * Use the Corrupted Blueprints on a placed block: the shell sacrifices itself into a decay
 * bloom, the sky answers with lightning — and MASSG appears, dormant. Sleeping.
 * Waking when observed.
 */
public class CorruptedCommandBlockBlock extends Block {

    public CorruptedCommandBlockBlock(Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.CORRUPTED_BLUEPRINTS)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }

        // consume the blueprints; the anchor burns out into the first decay bloom
        if (!player.getAbilities().instabuild) stack.shrink(1);
        server.setBlock(pos, ModBlocks.DECAY_BLOCK.defaultBlockState(), 3);

        // dramatic arrival
        server.playSound(null, pos, ModSounds.MASSG_AWAKENING, SoundSource.HOSTILE, 4.0f, 0.8f);
        server.sendParticles(ModParticlesRef.GLITCH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
            60, 1.0, 2.0, 1.0, 0.1);
        for (int i = 0; i < 3; i++) {
            var bolt = EntityType.LIGHTNING_BOLT.create(server);
            if (bolt != null) {
                bolt.setVisualOnly(true);
                bolt.moveTo(pos.getX() + (server.random.nextDouble() - 0.5) * 16.0,
                    pos.getY(), pos.getZ() + (server.random.nextDouble() - 0.5) * 16.0, 0.0f, 0.0f);
                server.addFreshEntity(bolt);
            }
        }

        // the dormant storm hangs overhead
        MassgEntity massg = ModEntities.MASSG.create(server);
        if (massg != null) {
            massg.moveTo(pos.getX() + 0.5, pos.getY() + 14.0, pos.getZ() + 0.5, 0.0f, 0.0f);
            server.addFreshEntity(massg);
        }

        for (ServerPlayer p : server.players()) {
            if (p.blockPosition().distSqr(pos) < 64.0 * 64.0) {
                p.sendSystemMessage(Component.literal(ModTexts.MASSG_WAKE));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Tiny indirection so this class doesn't import the particles registry directly. */
    private static final class ModParticlesRef {
        private static final net.minecraft.core.particles.SimpleParticleType GLITCH =
            com.rewritten.devouringstorms.registry.ModParticles.GLITCH;
    }
}
