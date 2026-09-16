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
 * Two of them, one class: THE ANTENNA opens the restricted operator's console,
 * and THE FIELD GUIDE opens the guide pages. Both are real {@link Item}s with a
 * real {@code use(...)} override -- the same override this repository already
 * ships on its spawn-egg and summoner items.
 *
 * BOTH SIDES DO THEIR OWN JOB, AND THAT IS WHY THERE IS NO PACKET.
 *
 *   * On the CLIENT -- where the player is, and where the click happened -- using
 *     the item opens the screen. A story screen is interface: it belongs on the
 *     client, and this build's overlay compiles against the client jar and the
 *     Fabric rendering modules only, so a custom channel is not even available
 *     to it.
 *   * On the SERVER -- the authority -- using the antenna while sneaking is the
 *     release for the MASSG, and using it otherwise answers in chat. Those are
 *     the things that change the world, and they are decided here, not on the
 *     client.
 *
 * The two halves never contradict each other: the client shows, the server acts.
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
            if (level.isClientSide()) {
                McsmClientDispatch.openTerminal(mode);
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                if ("terminal".equals(mode) && player.isShiftKeyDown()) {
                    // THE RELEASE. Sneaking with the set is how the MASSG is
                    // brought through: the console's own summon row tells the
                    // player to do exactly this.
                    if (!McsmMassg.summon(serverPlayer.level(), serverPlayer)) {
                        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "\u00a77The set will not release it (it is already here, or this world "
                                + "has it switched off).")
                                .withStyle(net.minecraft.ChatFormatting.GRAY));
                    }
                    return InteractionResult.SUCCESS;
                }
                if ("terminal".equals(mode)) {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "\u00a7bOPERATOR'S SET\u00a77 :: the console is on your screen. "
                            + "Sneak-use to release the MASSG.")
                            .withStyle(net.minecraft.ChatFormatting.AQUA));
                }
            }
        } catch (Throwable ignored) {
            // an item that cannot open its screen must not take the click down
        }
        return InteractionResult.SUCCESS;
    }
}
