package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * MEMORY FRAGMENT. A splinter of something Anna left behind so you'd know she never existed.
 * Use it to remember. Give it to Tazo to prove she was never a dream you were having.
 */
public class MemoryFragmentItem extends Item {

    public MemoryFragmentItem(Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            player.sendSystemMessage(Component.literal(
                ModTexts.ANNA_LINES.get(server.getRandom().nextInt(ModTexts.ANNA_LINES.size()))));
            server.playSound(null, player.blockPosition(), ModSounds.GLITCH, SoundSource.PLAYERS, 0.8f, 1.2f);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
