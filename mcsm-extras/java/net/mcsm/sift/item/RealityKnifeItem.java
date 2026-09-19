package net.mcsm.sift.item;

import net.mcsm.sift.block.BrokenFabricOfRealityBlock;
import net.mcsm.sift.block.FabricOfRealityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Reality Knife - like material you have to use to cut through fabric of reality
 * In survival only way to get through fabric is obtain Reality Knife
 * In creative you could just breakthrough causing whole thing to break open
 * 
 * Knife-like material you have to use to cut through it
 */
public class RealityKnifeItem extends Item {

    public RealityKnifeItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        if (state.getBlock() instanceof FabricOfRealityBlock || state.getBlock() instanceof BrokenFabricOfRealityBlock) {
            if (!level.isClientSide) {
                // Cut through fabric - reality knife cuts
                if (state.getBlock() instanceof FabricOfRealityBlock) {
                    // Turn solid fabric into broken fabric (portal) - cut open
                    level.setBlock(pos, 
                        net.mcsm.sift.McsmSiftMod.BROKEN_FABRIC.get().defaultBlockState(), 
                        3);
                    
                    // Particles - reality being cut
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CRIT,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            20, 0.3, 0.3, 0.3, 0.5);
                        sl.sendParticles(ParticleTypes.PORTAL,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            30, 0.5, 0.5, 0.5, 0.3);
                    }
                    
                    level.playSound(null, pos, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.BLOCKS, 1f, 0.8f);
                    level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8f, 1.5f);
                    
                    // Damage knife
                    if (player != null && !player.isCreative()) {
                        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
                    }
                    
                    return InteractionResult.SUCCESS;
                } else if (state.getBlock() instanceof BrokenFabricOfRealityBlock) {
                    // Already broken - widen the hole
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos p = pos.offset(x, 0, z);
                            BlockState s = level.getBlockState(p);
                            if (s.getBlock() instanceof FabricOfRealityBlock) {
                                level.setBlock(p, 
                                    net.mcsm.sift.McsmSiftMod.BROKEN_FABRIC.get().defaultBlockState(), 
                                    3);
                            }
                        }
                    }
                    level.playSound(null, pos, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.BLOCKS, 1f, 1.2f);
                    return InteractionResult.SUCCESS;
                }
            } else {
                // Client particles
                level.addParticle(ParticleTypes.SWEEP_ATTACK,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    0, 0, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // enchanted glint - reality cutting material
    }

    @Override
    public int getEnchantmentValue() {
        return 22; // high enchantability - reality material
    }
}
