package net.mcsm.extras;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * BUILD #481 -- THE VOID RUDDER: the tool you steer a fall with.
 *
 * <p>THE BRIEF. "Register the Void Rudder Item ... a custom tool class ... in your
 * item registries ... with a custom glowing neon-purple texture map", and give it
 * "deep-void propulsion physics": held while falling under the world, it multiplies
 * the player's downward velocity so they can cross the tiers at "extreme,
 * controllable cinematic speeds with sleek trailing particles".
 *
 * <p>WHAT IT IS. A one-slot tool that only bites in the void's air -- under the
 * bedrock line of any world, or anywhere below the gel's surface inside the void
 * itself. Held, it multiplies the tier's own current ({@link McsmVoidTiers#SPEED})
 * by {@link #HELD}, and right-clicked it engages and multiplies by {@link #ENGAGED}
 * instead; sneaking is the brake. The physics run on the player's own side of the
 * fence (see {@code client/McsmVoidRudder}), because a player's motion in this game
 * is theirs to make -- a server that shoves a falling player around is a server the
 * client argues with -- while the tool itself, its texture, its recipe and its chat
 * lines live here, where both sides can see them.
 *
 * <p>IT CANNOT HURT YOU. Nothing here touches damage, and the descent's own
 * protection ({@link McsmVoidDescent#protectedFall}) holds for the whole fall, so a
 * rudder dive is fast rather than fatal. That is the point of the tier speeds:
 * the deeper you are, the more the gel lets go.
 */
public class McsmVoidRudderItem extends Item {

    /** The gel lets go by this much while the rudder is merely held. */
    public static final double HELD = 1.6D;
    /** ... and by this much when it is engaged. */
    public static final double ENGAGED = 2.6D;
    /** Sneak is the brake, not a stop: the gel still pulls. */
    public static final double BRAKE = 0.35D;

    /** Who has the rudder engaged. Set on both sides by {@link #use}. */
    private static final Map<UUID, Boolean> STEERED = new ConcurrentHashMap<>();

    public McsmVoidRudderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        try {
            if (!voidAir(level, player)) {
                if (!level.isClientSide()) {
                    player.sendSystemMessage(Component.literal(
                            "\u00a78the rudder finds nothing to bite \u00a77\u00b7 take it "
                            + "under the world, into the gel"));
                }
                return InteractionResult.SUCCESS;
            }
            boolean now = !engaged(player);
            STEERED.put(player.getUUID(), now);
            if (!level.isClientSide()) {
                if (now) {
                    player.sendSystemMessage(Component.literal(
                            "\u00a7d\u00a7lTHE RUDDER \u00a78\u00b7 engaged"
                            + " \u00a77\u00b7 the gel is letting go of you"));
                    player.sendSystemMessage(Component.literal(
                            "\u00a7dsteer with your look \u00a78\u00b7 hold sneak to brake"
                            + " \u00a78\u00b7 right-click to let it go"));
                } else {
                    player.sendSystemMessage(Component.literal(
                            "\u00a78the rudder is loose \u00a77\u00b7 the fall is a fall again"));
                }
            }
        } catch (Throwable ignored) {
            // a tool that throws is worse than a tool that does nothing
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Is this player in the void's air -- the only place the rudder means anything?
     * Under the bedrock line of any world (on the way down to the hand-over), or
     * below the gel's surface inside the void's own dimension.
     */
    public static boolean voidAir(Level level, Player player) {
        try {
            if (level == null || player == null) {
                return false;
            }
            if (level.dimension().equals(McsmVoid.DIMENSION)) {
                return player.getY() < McsmVoidDescent.SURFACE_Y;
            }
            return player.getY() < level.getMinY() - McsmVoidDescent.DIVE_MARGIN;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Is the rudder in either hand? */
    public static boolean held(Player player) {
        try {
            return player.getMainHandItem().is(McsmContent.VOID_RUDDER)
                    || player.getOffhandItem().is(McsmContent.VOID_RUDDER);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Has this player engaged it? */
    public static boolean engaged(Player player) {
        try {
            return Boolean.TRUE.equals(STEERED.get(player.getUUID()));
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** The multiplier the rudder applies right now: 1 when it is not in hand. */
    public static double factor(Player player) {
        if (!held(player)) {
            return 1.0D;
        }
        return engaged(player) ? ENGAGED : HELD;
    }

    /** Let go (a player who leaves the void, or dies, should not stay engaged). */
    public static void release(Player player) {
        try {
            STEERED.remove(player.getUUID());
        } catch (Throwable ignored) {
            // nothing to release
        }
    }

    /** The `/ds` line: who is steering, and with what. */
    public static String state() {
        int engaged = 0;
        for (Boolean b : STEERED.values()) {
            if (Boolean.TRUE.equals(b)) {
                engaged++;
            }
        }
        return "rudder: " + engaged + " engaged, held x" + HELD + ", engaged x" + ENGAGED
                + ", tiers " + McsmVoidTiers.TIERS;
    }
}
