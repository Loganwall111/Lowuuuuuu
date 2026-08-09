package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.registry.ModBlockEntities;
import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModItems;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * THE TERMINAL. Centre of the Mainframe.
 *
 * Stand it in a 5×5 foundation of Mainframe Frames and offer a Rift Key: the terminal boots,
 * the classified transmission plays — and the portal to the Decayed Reality opens.
 * Try to use it without the key and the mainframe tells you exactly what it thinks of that.
 */
public class TerminalBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public TerminalBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TerminalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
            ? null
            : createTickerHelper(type, ModBlockEntities.TERMINAL, TerminalBlockEntity::serverTick);
    }

    /** Empty-handed use: the mainframe tells you what it needs. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        // EAOIN, sometimes, answers first. "the next generation is listening."
        if (player.getRandom().nextInt(9) == 0) {
            var lines = ModTexts.EAOIN_LINES;
            player.sendSystemMessage(Component.literal(lines.get(player.getRandom().nextInt(lines.size()))));
            level.playSound(null, pos, ModSounds.TERMINAL_BOOT, SoundSource.BLOCKS, 0.35f, 1.4f);
            return InteractionResult.CONSUME;
        }
        if (!state.getValue(ACTIVE)) {
            player.sendSystemMessage(Component.literal(
                "§8[§dMAINFRAME§8] §7ANCHOR UNSTABLE. §cRIFT KEY REQUIRED."));
        } else {
            player.sendSystemMessage(Component.literal(
                "§8[§dMAINFRAME§8] §7The portal is open. Something on the other side is listening."));
        }
        return InteractionResult.SUCCESS;
    }

    /** Rift Key on Terminal → foundation check → breach. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.RIFT_KEY) || state.getValue(ACTIVE)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        if (!foundationComplete(server, pos)) {
            player.sendSystemMessage(Component.literal(
                "§8[§dMAINFRAME§8] §7FOUNDATION INCOMPLETE. §oThe mainframe must stand upon a 5×5 frame.§r"));
            return InteractionResult.SUCCESS;
        }

        // THE MAINFRAME HAS BEEN BREACHED.
        if (!player.getAbilities().instabuild) stack.shrink(1);
        server.setBlock(pos, state.setValue(ACTIVE, true), 3);
        server.playSound(null, pos, ModSounds.TERMINAL_BOOT, SoundSource.BLOCKS, 2.0f, 1.0f);
        server.playSound(null, pos, ModSounds.RIFT_OPEN, SoundSource.BLOCKS, 2.0f, 0.9f);

        // open the portal: a 3×3 rift pad three blocks north of the terminal
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 2; dz <= 4; dz++) {
                BlockPos portalPos = pos.offset(dx, 0, -dz);
                if (server.getBlockState(portalPos).isAir()) {
                    server.setBlock(portalPos, ModBlocks.RIFT_PORTAL.defaultBlockState(), 3);
                }
            }
        }

        if (level.getBlockEntity(pos) instanceof TerminalBlockEntity terminal) {
            terminal.startTransmission(server.getGameTime());
        }
        return InteractionResult.SUCCESS;
    }

    /** 5×5 of Mainframe Frames on the same Y level, terminal in the centre slot. */
    private boolean foundationComplete(ServerLevel level, BlockPos terminalPos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (!level.getBlockState(terminalPos.offset(dx, -1, dz)).is(ModBlocks.MAINFRAME_FRAME)) {
                    return false;
                }
            }
        }
        return true;
    }
}
