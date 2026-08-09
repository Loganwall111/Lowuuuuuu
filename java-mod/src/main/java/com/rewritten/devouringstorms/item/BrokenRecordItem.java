package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.registry.ModSounds;
import com.rewritten.devouringstorms.util.RiftTravel;
import com.rewritten.devouringstorms.world.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * THE BROKEN RECORD. "play it backwards and you're already there."
 * A warped disc of splintered shellac — the end-loops-into-the-start kind of broken.
 * Playing it rips the listener through to the COSMIC ABYSS. The BHS aisles don't have an
 * exit printed anywhere, so the way back is on you — find the tear.
 */
public class BrokenRecordItem extends Item {

    public BrokenRecordItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;
        if (server.dimension().equals(ModDimensions.ABYSS_LEVEL_KEY)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§8§oThe needle finds the groove... it only plays arrival."));
            return InteractionResult.CONSUME;
        }
        ServerLevel abyss = server.getServer().getLevel(ModDimensions.ABYSS_LEVEL_KEY);
        if (abyss == null) return InteractionResult.PASS;
        server.playSound(null, player.blockPosition(), ModSounds.GLITCH, SoundSource.PLAYERS, 1.0f, 0.4f);
        RiftTravel.travelTo(player, abyss, new Vec3(0.5, 70.0, 0.5));
        stack.shrinkUnless(1, player);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§8§o...you are now leaving the store. §r§7§othe doors were never doors.§r"));
        return InteractionResult.CONSUME;
    }
}
