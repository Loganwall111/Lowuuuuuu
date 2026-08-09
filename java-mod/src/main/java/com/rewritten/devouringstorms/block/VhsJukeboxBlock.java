package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.registry.ModItems;
import com.rewritten.devouringstorms.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * THE VHS JUKEBOX. It plays the tapes and it plays the truth.
 * Insert a Devouring Storms tape (or any vanilla disc — it's polite about history) and it
 * plays the music while the PLAYING lamp shows; and while it plays, everyone close enough
 * sees the world the way the Creator left it: tracked as dirty, streaking, letterboxed tape.
 */
public class VhsJukeboxBlock extends Block {

    public static final BooleanProperty PLAYING = BooleanProperty.create("playing");

    /** Tape ≈ 90 seconds. One tape per playthrough, then it spits it back out. */
    private static final int TAPE_LENGTH_TICKS = 90 * 20;

    public VhsJukeboxBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(PLAYING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLAYING);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(PLAYING)) return InteractionResult.CONSUME;
        if (!isPlayable(stack)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            level.setBlock(pos, state.setValue(PLAYING, true), 3);
            if (stack.getItem() == ModItems.MUSIC_DISC_EAOIN) {
                level.playSound(null, pos, ModSounds.SONG_EAOIN, SoundSource.JUKEBOX, 1.0f, 1.0f);
            } else if (stack.getItem() == ModItems.MUSIC_DISC_SIGNAL_TAPE) {
                level.playSound(null, pos, ModSounds.SONG_SIGNAL_TAPE, SoundSource.JUKEBOX, 1.0f, 1.0f);
            } else if (stack.getItem() == ModItems.MUSIC_DISC_COUNTDOWN) {
                level.playSound(null, pos, ModSounds.SONG_COUNTDOWN, SoundSource.JUKEBOX, 1.0f, 1.0f);
            } else if (stack.getItem() == ModItems.MUSIC_DISC_QUARANTINE) {
                level.playSound(null, pos, ModSounds.SONG_QUARANTINE, SoundSource.JUKEBOX, 1.0f, 1.0f);
            } else if (stack.getItem() == ModItems.MUSIC_DISC_CHANGED) {
                level.playSound(null, pos, ModSounds.SONG_WE_HAVE_BEEN_CHANGED, SoundSource.JUKEBOX, 1.0f, 1.0f);
            } else if (stack.getItem() == ModItems.MUSIC_DISC_SHIPS) {
                level.playSound(null, pos, ModSounds.SONG_SHIPS_TO_CARRY_US_HOME, SoundSource.JUKEBOX, 1.0f, 1.0f);
            } else {
                // vanilla discs: no jukebox-playable lookup ceremony — the vhs just hums along
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.MUSIC_DISC_CREATOR, SoundSource.JUKEBOX, 1.0f, 1.0f);
            }
            stack.shrinkUnless(1, player);
            server.scheduleTick(pos, this, TAPE_LENGTH_TICKS);
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§f§oPLAY ▶ §r§7— the tape starts turning."), true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (state.getValue(PLAYING)) {
            level.setBlock(pos, state.setValue(PLAYING, false), 3);
            if (!level.isClientSide()) {
                level.playSound(null, pos, ModSounds.GLITCH, SoundSource.BLOCKS, 0.5f, 0.6f);
                Item disc = ModItems.MUSIC_DISC_SIGNAL_TAPE;
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    new ItemStack(disc)));
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§f§oSTOP ■ §r§7— the tape seizes."), true);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!state.getValue(PLAYING)) return;
        level.setBlock(pos, state.setValue(PLAYING, false), 3);
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
            new ItemStack(ModItems.MUSIC_DISC_SIGNAL_TAPE)));
    }

    private static boolean isPlayable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(net.minecraft.tags.ItemTags.MUSIC_DISCS)
            || stack.is(ModItems.MUSIC_DISC_SIGNAL_TAPE)
            || stack.is(ModItems.MUSIC_DISC_EAOIN)
            || stack.is(ModItems.MUSIC_DISC_COUNTDOWN)
            || stack.is(ModItems.MUSIC_DISC_QUARANTINE)
            || stack.is(ModItems.MUSIC_DISC_CHANGED)
            || stack.is(ModItems.MUSIC_DISC_SHIPS);
    }
}
