package net.mcsm.extras;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * BUILD #416 (D.8, phase 5) -- the items that open the terminal.
 *
 * Two of them, one class: THE ANTENNA opens the restricted operator's console
 * (login screen -> console), and THE FIELD GUIDE opens the guide pages. Both are
 * real {@link Item}s with a real {@code use(...)} override -- the same override
 * this repository already ships on its spawn-egg and summoner items -- so using
 * one is a normal right click, it works from either hand, and the SERVER is what
 * decides what the player is allowed to see.
 *
 * The item does not open a screen itself: it asks the server, and the server's
 * answer comes back on the terminal channel, which the client opens on its next
 * tick. That is what keeps the password, the console contents and the guide
 * pages on the server side where a client cannot invent them.
 */
public class McsmTerminalItem extends Item {

    /** "terminal" = the restricted console, "guide" = the field guide. */
    private final String mode;

    public McsmTerminalItem(Properties properties, String mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        try {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                McsmTerminal.openFor(serverPlayer, mode);
            }
        } catch (Throwable ignored) {
            // an item that cannot open its screen must not take the click down
        }
        return InteractionResult.SUCCESS;
    }
}
