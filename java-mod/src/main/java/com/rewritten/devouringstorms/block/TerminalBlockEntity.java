package com.rewritten.devouringstorms.block;

import com.rewritten.devouringstorms.registry.ModBlockEntities;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Plays the classified transmission after the breach: one line at a time, audible static,
 * spacing like a signal pushed through a wall of corrupted code.
 */
public class TerminalBlockEntity extends BlockEntity {

    private static final int LINE_INTERVAL = 50; // ticks between transmission lines (2.5 s)

    private boolean transmitting;
    private int lineIndex;
    private long nextLineAt;

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TERMINAL, pos, state);
    }

    public void startTransmission(long gameTime) {
        this.transmitting = true;
        this.lineIndex = 0;
        this.nextLineAt = gameTime + 30;
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TerminalBlockEntity terminal) {
        if (!terminal.transmitting || !(level instanceof ServerLevel server)) return;
        if (server.getGameTime() < terminal.nextLineAt) return;

        if (terminal.lineIndex >= ModTexts.MAINFRAME_TRANSMISSION.size()) {
            terminal.transmitting = false;
            terminal.setChanged();
            return;
        }

        String line = ModTexts.MAINFRAME_TRANSMISSION.get(terminal.lineIndex++);
        terminal.nextLineAt = server.getGameTime() + LINE_INTERVAL;

        Vec3 centre = Vec3.atCenterOf(pos);
        server.playSound(null, pos, ModSounds.TERMINAL_TRANSMISSION, SoundSource.BLOCKS, 1.2f, 1.0f);
        for (Player player : server.players()) {
            if (player.distanceToSqr(centre) < 48.0 * 48.0) {
                player.sendSystemMessage(Component.literal(line));
            }
        }
        terminal.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Transmitting", this.transmitting);
        output.putInt("LineIndex", this.lineIndex);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.transmitting = input.getBooleanOr("Transmitting", false);
        this.lineIndex = input.getIntOr("LineIndex", 0);
        this.nextLineAt = 0; // resume promptly after reload
    }
}
