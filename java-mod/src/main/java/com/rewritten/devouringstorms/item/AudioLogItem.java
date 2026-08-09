package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * E.P.A. AUDIO LOGS. Field recordings from the quarantine's last official expedition —
 * three tapes that fill in the bell, the plague, and the thing at the edge of camp.
 * Use to play it back. The tape hisses like it's still recording.
 */
public class AudioLogItem extends Item {

    private final int index;   // 0..2 into ModTexts.AUDIO_LOGS

    public AudioLogItem(Properties props, int index) {
        super(props);
        this.index = index;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            for (String line : ModTexts.AUDIO_LOGS.get(index)) {
                player.sendSystemMessage(Component.literal(line));
            }
            level.playSound(null, player, ModSounds.TERMINAL_BOOT, SoundSource.PLAYERS, 0.5f, 0.9f + index * 0.1f);
        }
        return InteractionResult.CONSUME;
    }
}
