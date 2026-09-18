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
            // 7000.0.8-M: completely disable suffocation - clear around player every tick near floor
            if (McsmExtrasConfig.voidNoSuffocation && level.dimension().equals(McsmVoid.DIMENSION)) {
                try {
                    if (player.getY() < TRIGGER_Y + 10) {
                        player.setAirSupply(player.getMaxAirSupply());
                        // Clear huge area near floor to prevent suffocation when holding rudder and breaking
                        for (int dx = -3; dx <= 3; dx++) {
                            for (int dy = -1; dy <= 5; dy++) {
                                for (int dz = -3; dz <= 3; dz++) {
                                    net.minecraft.core.BlockPos p = player.blockPosition().offset(dx, dy, dz);
                                    if (p.getY() <= McsmVoidTiers.FLOOR_Y && !hasRealityKnife(player)) continue;
                                    var st = level.getBlockState(p);
                                    if (!st.isAir() && st.isSolid()) {
                                        level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
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
            // If player has Reality Knife, cut open the fabric instead of wrapping - go to Sift
            if (hasRealityKnife(player)) {
                cutOpen(level, player, motion);
                return;
            }
            wrap(level, player, motion);
        } catch (Throwable ignored) {
            // a seam that throws is worse than a fall that ends
        }
    }

    private static boolean hasRealityKnife(ServerPlayer player) {
        try {
            String mainId = player.getMainHandItem().getItem().getDescriptionId();
            String offId = player.getOffhandItem().getItem().getDescriptionId();
            if (mainId.contains("reality_knife") || offId.contains("reality_knife")) return true;
            if (mainId.contains("void_rudder") || offId.contains("void_rudder")) return true;
            // Check inventory via contains predicate - safe for 26.2 (Inventory.items is private)
            return player.getInventory().contains(stack -> {
                try {
                    String id = stack.getItem().getDescriptionId();
                    return id.contains("reality_knife") || id.contains("void_rudder");
                } catch (Throwable ignored) { return false; }
            });
        } catch (Throwable ignored) {}
        return false;
    }

    /** Cut open the fabric - Reality Knife bypasses the seam and enters Sift */
    private static void cutOpen(ServerLevel level, ServerPlayer player, Vec3 motion) {
        try {
            // Break barrier at floor
            net.minecraft.core.BlockPos floorPos = new net.minecraft.core.BlockPos((int)player.getX(), McsmVoidTiers.FLOOR_Y, (int)player.getZ());
            if (level.getBlockState(floorPos).is(net.minecraft.world.level.block.Blocks.BARRIER)) {
                level.setBlock(floorPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
            }
            // Try to enter sift dimension
            try {
                Class<?> siftDim = Class.forName("net.mcsm.sift.world.McsmSiftDimension");
                java.lang.reflect.Field field = siftDim.getField("SIFT_DIMENSION");
                net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> siftKey = (net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>) field.get(null);
                ServerLevel siftLevel = level.getServer().getLevel(siftKey);
                if (siftLevel != null) {
                    player.teleportTo(siftLevel, player.getX(), 80, player.getZ(), java.util.Set.of(), player.getYRot(), player.getXRot(), false);
                    player.setDeltaMovement(motion);
                    player.resetFallDistance();
                    player.sendSystemMessage(Component.literal("\u00a7d\u00a7lFABRIC CUT \u00a78\u00b7 reality knife cut open the barrier"));
                    player.sendSystemMessage(Component.literal("\u00a77you fell through into Sift - the unknown place below all layers"));
                    return;
                }
            } catch (Throwable ignored) {
                // Sift not present, just allow staying at bottom
            }
            // If no sift, just place at bottom and allow digging
            player.teleportTo(level, player.getX(), McsmVoidTiers.FLOOR_Y + 1, player.getZ(), java.util.Set.of(), player.getYRot(), player.getXRot(), false);
            player.setDeltaMovement(motion.multiply(1, 0.5, 1));
            player.resetFallDistance();
            player.sendSystemMessage(Component.literal("\u00a7a\u00a7lBARRIER CUT \u00a78\u00b7 you cut the invisible floor with reality knife"));
            player.sendSystemMessage(Component.literal("\u00a77dig down now - the dimension no longer catches you"));
        } catch (Throwable ignored) {}
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
