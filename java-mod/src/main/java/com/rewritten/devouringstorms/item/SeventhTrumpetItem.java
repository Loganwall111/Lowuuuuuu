package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.storm.MassgPhase;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * THE SEVENTH TRUMPET. The dormant ritual line: "it awaits the seventh trumpet trigger."
 * Sound it against the storm and it answers — one phase, right now, on purpose.
 * The husk does not answer. Nothing answers the husk but the Storm Killer.
 */
public class SeventhTrumpetItem extends Item {

    public SeventhTrumpetItem(Properties props) {
        super(props);
    }

    // mappings fence: interactLivingEntity(ItemStack, Player, LivingEntity, InteractionHand)
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof MassgEntity storm)) {
            return super.interactLivingEntity(stack, player, target, hand);
        }
        if (storm.level().isClientSide()) return InteractionResult.SUCCESS;

        if (storm.getPhase() == MassgPhase.HUSK || storm.getDeadTicks() >= 0) {
            player.sendSystemMessage(Component.literal(
                "§8You sound the trumpet. The husk does not answer. §oOnly the knife does.§r"));
            return InteractionResult.CONSUME;
        }

        MassgPhase next = storm.getPhase() == MassgPhase.SLEEPING
            ? MassgPhase.SIGNAL
            : storm.getPhase().next();
        storm.setPhase(next);
        storm.level().playSound(null, storm, ModSounds.MASSG_AWAKENING, SoundSource.HOSTILE, 4.0f, 1.6f);
        for (Player p : storm.level().players()) {
            if (p.distanceTo(storm) < 320.0) {
                p.sendSystemMessage(Component.literal("§5§lTHE SEVENTH TRUMPET SOUNDS. §r§d§oit answers.§r"));
            }
        }
        if (!player.isCreative()) stack.shrink(1);
        return InteractionResult.CONSUME;
    }
}
