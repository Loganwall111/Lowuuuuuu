package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.entity.FormidiBombEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * FORMIDIBOMB. The F-bomb. Thrown like a snowball with infinitely worse manners.
 * The only thing in the world that can finish a playing-dead MASSG.
 */
public class FormidiBombItem extends Item {

    public FormidiBombItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            var bomb = new FormidiBombEntity(level, player, stack);
            bomb.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.1f, 1.0f);
            level.addFreshEntity(bomb);
            level.playSound(null, player.blockPosition(), SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 1.0f, 0.8f);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
