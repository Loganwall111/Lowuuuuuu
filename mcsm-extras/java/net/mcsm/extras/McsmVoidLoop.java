package net.mcsm.extras;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #483 -- THE INFINITE FALL: what happens when the bottom has a door in it.
 *
 * <p>THE BRIEF. "Implement a positional wrapping calculation that monitors player
 * depth. If a player drops past the absolute hardcoded floor of the engine
 * (Y = -2032), inject an automatic modulo function to smoothly reset their
 * relative tracking matrix back up to the top boundary of Tier 1 (Y = -251) ...
 * ensure that during every loop reset cycle, your player's downward velocity
 * vector, momentum flags, pitch, yaw, and head rotation constants remain 100%
 * untouched ... force the structural spawner arrays to mathematically shuffle
 * their coordinate generation seeds on every single wrap loop, ensuring the
 * hanging spires, portals, and entity paths dynamically rearrange themselves so
 * the infinite drop never looks identical."
 *
 * <p>WHAT THIS DOES, EXACTLY.
 *
 *   * TRIGGER. The void's floor is a barrier layer at {@link McsmVoidTiers#FLOOR_Y}
 *     (-2032), and it is SOLID: a falling player lands on it and stops there,
 *     four blocks short of "past" it. So the wrap fires in the pocket just above
 *     it ({@link #TRIGGER_Y}, -2029) and only while the player is actually falling.
 *     There is nothing to pass through and nothing to break; the loop is where the
 *     fall would otherwise have ended.
 *   * WHERE IT PUTS YOU. {@link #WRAP_TOP} is -251 -- the plan's own number, and
 *     also this world's own boundary: it is exactly the top of Tier 1, the
 *     luminous cavern. (The two agree because the tier table was scaled from the
 *     plan's bands; the numbers either side of the seam are the same numbers.)
 *   * WHAT IS KEPT. Velocity is read before the move and written back after it,
 *     rotation is passed through the teleport itself (yaw and pitch as they were),
 *     the fall distance is cleared so no landing debt is carried across the seam,
 *     and {@code hurtMarked} is set so the client takes the server's own motion as
 *     the truth. No screen, no reload, no rubber-band: the player is falling at
 *     the same speed in the same direction, 47 blocks further up.
 *   * THE SEED SHUFFLE. Every wrap increments a salt that the void's own generators
 *     read: {@link #salt()} goes into {@link McsmVoidSponge}'s twist and
 *     {@link McsmVoidRifts}' lattice, so the maze is not the same maze and the
 *     rifts are not in the same places on the second pass. The fall cannot look
 *     identical twice, which is the whole point of an infinite one.
 *   * THE DESCENT IS TOLD. A wrap is a teleport UP, and the fall tracker reads
 *     "Y went up" as "the player stopped falling". {@link McsmVoidDescent#noteWrap}
 *     clears that, so the loop does not end the dive.
 */
public final class McsmVoidLoop {

    /**
     * The pocket above the floor where the wrap fires. The floor is solid, so this
     * is the only place a falling player can be when the bottom of the world is
     * reached -- four blocks of air, and then the seam.
     */
    public static final int TRIGGER_Y = McsmVoidTiers.FLOOR_Y + 3;
    /** Where a wrap puts the fall back: the top of Tier 1, the plan's own -251. */
    public static final int WRAP_TOP = McsmVoidTiers.BASELINE_FLOOR - 1;

    /** How many times each player has been carried round. */
    private static final Map<UUID, Integer> WRAPS = new ConcurrentHashMap<>();
    /** The salt the generators read. One bump per wrap, by anybody. */
    private static volatile int salt;

    private McsmVoidLoop() {
    }

    /** Called from the void's own tick, once per player, in the void dimension. */
    public static void tick(ServerLevel level, ServerPlayer player) {
        try {
            if (!McsmExtrasConfig.voidLoop) {
                return;
            }
            if (!level.dimension().equals(McsmVoid.DIMENSION)) {
                return;
            }
            Vec3 motion = player.getDeltaMovement();
            if (player.getY() > TRIGGER_Y || motion.y >= 0.0D) {
                return;
            }
            wrap(level, player, motion);
        } catch (Throwable ignored) {
            // a seam that throws is worse than a fall that ends
        }
    }

    /** The wrap itself: same velocity, same rotation, 1781 blocks higher. */
    private static void wrap(ServerLevel level, ServerPlayer player, Vec3 motion) {
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        int n = WRAPS.merge(player.getUUID(), 1, Integer::sum);
        salt++;
        player.teleportTo(level, player.getX(), WRAP_TOP, player.getZ(),
                java.util.Set.of(), yRot, xRot, false);
        // the momentum flags: the same vector it had at the floor, and the client
        // told to believe the server rather than its own old extrapolation
        player.setDeltaMovement(motion);
        player.resetFallDistance();
        player.hurtMarked = true;
        McsmVoidDescent.noteWrap(player);
        if (n == 1) {
            player.sendSystemMessage(Component.literal(
                    "\u00a7d\u00a7lTHE SEAM \u00a78\u00b7 the void has no floor, it has a door"));
            player.sendSystemMessage(Component.literal(
                    "\u00a77you are falling again, at the same speed you arrived \u00b7 look up"));
        } else if (n % 5 == 0) {
            player.sendSystemMessage(Component.literal(
                    "\u00a78round " + n + " \u00b7 the void has rearranged itself around you"));
        }
    }

    /** The generators' salt: every wrap makes the next pass a different place. */
    public static int salt() {
        return salt;
    }

    /** How many times this player has been carried round. */
    public static int wraps(net.minecraft.world.entity.Entity entity) {
        try {
            Integer n = WRAPS.get(entity.getUUID());
            return n == null ? 0 : n;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** The `/ds` line: how deep the loop has been taken, and how many times. */
    public static String state() {
        int total = 0;
        for (Integer n : WRAPS.values()) {
            total += n;
        }
        return "loop: seam at y=" + TRIGGER_Y + " -> y=" + WRAP_TOP + ", " + total
                + " wrap(s) by " + WRAPS.size() + " player(s), salt " + salt;
    }
}
