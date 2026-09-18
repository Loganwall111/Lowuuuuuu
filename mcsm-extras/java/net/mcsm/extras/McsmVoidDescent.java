package net.mcsm.extras;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BUILD #479 -- THE DESCENT: you do not go to the void, you FALL into it.
 *
 * <p>THE ASK, in the player's own words: "we made that void dimension a while ago,
 * however it's not really attached to the regular minecraft void ... the world is
 * simply digging down in the overworld and falling directly into the void downwards
 * ... there's no loading screen, you just fall into it ... to get here all you have
 * to do is dive directly into the regular minecraft void ... 3 to 5 ... 10 to 15
 * seconds ... as you drop in another 20 to 30 seconds ... instead of dying what
 * will end up happening is you fall and fall and fall and keep falling."
 *
 * <p>So the void is no longer somewhere you are sent: it is what happens when
 * somebody keeps going down. There is no item, no portal and no command in this
 * file. The trigger is the one thing every player already knows how to do.
 *
 * <p><b>THE FALL IS REAL, NOT A CUTSCENE.</b> Everything here is a position, never
 * a timer doing the falling: the player is put into the void's air and Minecraft's
 * own gravity does the rest, so the dive is steerable, the drop can be watched, and
 * nothing is being puppeteered. The four stops are read off Y as the fall passes
 * through them:
 *
 * <pre>
 *   THE RATE       entry .. 118   3-5 s   the world lets go; the plunge starts
 *   THE DARK       118 .. 78     10-15 s  the light above goes out, the air thins
 *   THE GEL        78 .. 56      3-5 s    you cross into the gel: bubbles, the glow
 *   THE DEEP       56 .. below   20-30 s  sinking, drifting, landing on an island
 * </pre>
 *
 * <p>Those blocks are the numbers the player gave, at Minecraft's own terminal
 * velocity (~3.1 blocks/s), which is why they are written as Y lines rather than as
 * "seconds": a fall cannot be a timer without lying about the physics.
 *
 * <p><b>AND IT IS NOT A DIMENSION CHANGE AS FAR AS THE PLAYER IS CONCERNED.</b> The
 * hand-over happens below {@code minY - 8}, where the world is already nothing but
 * air: the sky, the fog and the light are the descent's own from that instant, and
 * the frame the game spends moving between worlds is painted as the deep itself by
 * {@code McsmVoidDeep} -- no grey screen, no progress bar, no grey text. It is the
 * one place in this mod where a screen would be a real lie about what is happening.
 *
 * <p><b>THE FALL NEVER KILLS YOU.</b> That is the whole point of the report: falling
 * into Minecraft's void used to be the end of the dive. Now the descent is exempt
 * from the void's damage and from fall damage until the deep sets the player down on
 * the gel's own floor or on one of its islands -- see {@link #protectedFall} and the
 * scripts in {@code McsmVoidDeep}.
 */
public final class McsmVoidDescent {

    // ------------------------------------------------------------------
    // The four stops, as Y lines. See the class doc for the seconds.
    // ------------------------------------------------------------------

    /** Below the world by this much: the dive has started. Merged void = 200 blocks for epic deep fall. */
    public static final int DIVE_MARGIN = 8;
    public static final int DIVE_MARGIN_MERGED = 200;
    /** Where the fall is handed over: 12 blocks of air before the plunge below. Merged = directly to first layer. */
    public static final int ARRIVAL_Y = 130;
    public static final int ARRIVAL_Y_MERGED = 20;
    /** The plunge ends and the dark begins. */
    public static final int DARK_Y = 118;
    public static final int DARK_Y_MERGED = 10;
    /** The dark ends and the gel begins (also the low-void art's own ceiling). */
    public static final int GEL_Y = 78;
    public static final int GEL_Y_MERGED = -5;
    /** The gel's surface: below this, the deep is where the player is. */
    public static final int SURFACE_Y = 56;
    public static final int SURFACE_Y_MERGED = -12;

    public static int diveMargin() {
        return McsmExtrasConfig.voidMerged ? DIVE_MARGIN_MERGED : DIVE_MARGIN;
    }
    public static int arrivalY() {
        return McsmExtrasConfig.voidMerged ? ARRIVAL_Y_MERGED : ARRIVAL_Y;
    }
    /** How far above a surface the deep sets a player down instead of letting them hit it. */
    public static final int CATCH_ABOVE = 7;
    /** The fall must stop going down for this many ticks before it counts as caught. */
    private static final int STALL_TICKS = 24;

    private static final Map<UUID, Dive> DIVING = new ConcurrentHashMap<>();

    /** How far into the fall one player is, and what has already been said about it. */
    private static final class Dive {
        int stop = 0;          // 0 = rate, 1 = dark, 2 = gel, 3 = deep
        int tier = -1;         // BUILD #481 -- the last tier of the void announced
        int said = -1;         // the last line index announced for this stop
        double lastY = Double.MAX_VALUE;
        int stalled;
        int gelTicks;
        boolean landed;
    }

    private McsmVoidDescent() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmVoidDescent::tick);
            System.out.println("[ds] the descent is armed (dive below y=" + DIVE_MARGIN
                    + " under any world, fall " + ARRIVAL_Y + " -> " + SURFACE_Y
                    + ", no screen, no damage)");
        } catch (Throwable t) {
            System.err.println("[ds] the descent could not hook the level tick: " + t);
        }
    }

    public static void tick(ServerLevel level) {
        if (!McsmExtrasConfig.voidDescent) {
            return;
        }
        try {
            boolean deep = level.dimension().equals(McsmVoid.DIMENSION);
            boolean mergedDeep = McsmExtrasConfig.voidMerged && level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD);
            for (ServerPlayer player : level.players()) {
                if (deep) {
                    deep(player, level);
                } else if (mergedDeep && player.getY() < -50) {
                    // Merged void: overworld below -50 is treated as deep void with same logic
                    deep(player, level);
                } else {
                    dive(player, level);
                }
            }
        } catch (Throwable t) {
            // a fall system can never be the reason a tick dies
        }
    }

    // ------------------------------------------------------------------
    // Any world: the dive
    // ------------------------------------------------------------------

    private static void dive(ServerPlayer player, ServerLevel level) {
        double y = player.getY();
        Dive dive = DIVING.get(player.getUUID());
        int margin = diveMargin();
        // Original check preserved for phase uniformity: y > level.getMinY() - DIVE_MARGIN

        // above the line: not diving, and if they were, the fall is over
        // Merged void: hundreds blocks down = crazier feel, skybox slowly turns color
        if (y > level.getMinY() - margin) {
            // Merged void fog: slowly turn color of first area as you fall hundreds blocks
            if (McsmExtrasConfig.voidMerged && y < level.getMinY() + 20 && y > level.getMinY() - margin) {
                float progress = (float)((level.getMinY() + 20 - y) / (20 + margin));
                // Keep air full during long fall to prevent suffocation/drown
                if (McsmExtrasConfig.voidNoSuffocation) {
                    player.setAirSupply(player.getMaxAirSupply());
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 200, 0, true, false));
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 100, 0, true, false));
                }
            }
            if (dive != null) {
                DIVING.remove(player.getUUID());
                player.sendSystemMessage(Component.literal(
                        "\u00a78the fall let go of you \u00a77\u00b7 you are back on something solid"));
            }
            return;
        }
        if (dive == null) {
            // THE LAST THING THAT HAPPENS IN THE WORLD ABOVE: no damage from here on
            // (see protectedFall), and the fall is handed over to the void's own air.
            dive = new Dive();
            DIVING.put(player.getUUID(), dive);
            handOver(player, level);
            return;
        }
        // still in the world below the line: the hand-over either failed or is late
        dive.stalled++;
        if (dive.stalled > STALL_TICKS * 4) {
            DIVING.remove(player.getUUID());
        }
    }

    /**
     * THE HAND-OVER: the one moment this feature moves anybody, and it happens where
     * there is nothing to see anyway -- below the world, in the air, already falling.
     * No screen: the byte-level reason a screen appears at all is the dimension
     * change, and {@code McsmVoidDeep} paints that frame as the deep.
     */
    private static void handOver(ServerPlayer player, ServerLevel level) {
        try {
            double x = player.getX();
            double z = player.getZ();
            var motion = player.getDeltaMovement();
            // Merged void: place dimension directly underneath overworld hundreds blocks down
            // Instead of instant teleport to second dimension, keep falling in overworld with skybox merging
            if (McsmExtrasConfig.voidMerged && level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                // Keep in overworld, drop to -100 (hundreds blocks down) - real depth, no loading screen
                // Overworld now has min_y -2032 height 4064, so -100 is valid and has void tiers generated
                player.teleportTo(level, x, -100, z,
                        java.util.Set.of(), player.getYRot(), 35.0F, false);
                player.setDeltaMovement(motion.multiply(1, 1.2, 1));
                player.resetFallDistance();
                player.onUpdateAbilities();
                player.sendSystemMessage(Component.literal(
                        "\u00a75\u00a7lTHE DESCENT MERGED \u00a78\u00b7 you fell hundreds of blocks - void is directly underneath overworld"));
                player.sendSystemMessage(Component.literal(
                        "\u00a77skybox merging slowly to first layer color \u00b7 no loading screen, real depth, crazier"));
                // Also start cinematic
                if (McsmExtrasConfig.voidCinematic) {
                    try {
                        Class.forName("net.mcsm.extras.client.McsmVoidCinematic").getMethod("start", net.minecraft.server.level.ServerPlayer.class).invoke(null, player);
                    } catch (Throwable ignored) {}
                }
                return;
            }
            ServerLevel voidLevel = level.getServer().getLevel(McsmVoid.DIMENSION);
            if (voidLevel == null) {
                return;
            }
            int arrival = arrivalY();
            // Original preserved for phase uniformity: player.teleportTo(voidLevel, x, ARRIVAL_Y, z,
            player.teleportTo(voidLevel, x, arrival, z,
                    java.util.Set.of(), player.getYRot(), 35.0F, false);
            player.setDeltaMovement(motion);
            player.resetFallDistance();
            player.onUpdateAbilities();
            player.sendSystemMessage(Component.literal(
                    "\u00a75\u00a7lTHE DESCENT \u00a78\u00b7 the world let go of you"));
            player.sendSystemMessage(Component.literal(
                    "\u00a77keep falling. there is no floor to find, and nothing down here "
                    + "that wants you dead."));
            // Trigger cinematic if enabled
            if (McsmExtrasConfig.voidCinematic) {
                try {
                    Class.forName("net.mcsm.extras.client.McsmVoidCinematic").getMethod("start", net.minecraft.server.level.ServerPlayer.class).invoke(null, player);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            System.err.println("[ds] the descent could not hand a fall over: " + t);
        }
    }

    // ------------------------------------------------------------------
    // The void: the fall through the four stops
    // ------------------------------------------------------------------

    private static void deep(ServerPlayer player, ServerLevel level) {
        // Anti-suffocation fix for 7000.0.5-M: player reported barrier invisible and suffocating before gel
        // 7000.0.8-M: completely disable suffocation option - clear huge area
        try {
            boolean noSuffoc = McsmExtrasConfig.voidNoSuffocation;
            if (noSuffoc) {
                BlockPos center = player.blockPosition();
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dy = -2; dy <= 4; dy++) {
                        for (int dz = -3; dz <= 3; dz++) {
                            BlockPos p = center.offset(dx, dy, dz);
                            if (p.getY() <= McsmVoidTiers.FLOOR_Y) {
                                // Only clear barrier if has knife/rudder to cut open
                                boolean hasKnife = false;
                                try {
                                    String mainId = player.getMainHandItem().getItem().getDescriptionId();
                                    String offId = player.getOffhandItem().getItem().getDescriptionId();
                                    if (mainId.contains("reality_knife") || mainId.contains("void_rudder") ||
                                        offId.contains("reality_knife") || offId.contains("void_rudder")) hasKnife = true;
                                } catch (Throwable ignored) {}
                                if (!hasKnife) continue;
                            }
                            BlockState st = level.getBlockState(p);
                            if (!st.isAir() && st.isSolid()) {
                                level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            } else {
                BlockPos headPos = player.blockPosition().above();
                BlockState headState = level.getBlockState(headPos);
                if (!headState.isAir() && !headState.is(net.minecraft.world.level.block.Blocks.BARRIER)) {
                    level.setBlock(headPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                    level.setBlock(headPos.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                }
            }
            // Give water breathing + slow falling while in void descent to prevent drown/suffocate - expanded
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 200, 0, true, false));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 100, 0, true, false));
            player.setAirSupply(player.getMaxAirSupply());
            if (noSuffoc) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 200, 0, true, false));
                try {
                    // 26.2 renamed DAMAGE_RESISTANCE -> RESISTANCE, try both via reflection-safe
                    java.lang.reflect.Field f = net.minecraft.world.effect.MobEffects.class.getField("DAMAGE_RESISTANCE");
                    Object holder = f.get(null);
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance((net.minecraft.core.Holder)holder, 100, 1, true, false));
                } catch (Throwable t) {
                    try {
                        java.lang.reflect.Field f2 = net.minecraft.world.effect.MobEffects.class.getField("RESISTANCE");
                        Object holder2 = f2.get(null);
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance((net.minecraft.core.Holder)holder2, 100, 1, true, false));
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        // a player who reached the void by its own doorway is not on the descent and
        // is not tracked: they are visiting, not falling
        Dive dive = DIVING.get(player.getUUID());
        if (dive == null) {
            return;
        }
        double y = player.getY();

        // the fall is only a fall while it is going down
        if (y >= dive.lastY - 1.0e-4) {
            dive.stalled++;
        } else {
            dive.stalled = 0;
        }
        dive.lastY = y;
        if (dive.stalled > STALL_TICKS) {
            DIVING.remove(player.getUUID());
            player.sendSystemMessage(Component.literal(
                    "\u00a78the descent stops with you \u00a77\u00b7 you caught yourself"));
            return;
        }

        int stop = y > DARK_Y ? 0 : y > GEL_Y ? 1 : y > SURFACE_Y ? 2 : 3;
        if (stop != dive.stop) {
            dive.stop = stop;
            dive.said = -1;
        }

        // BUILD #481 -- FIVE TIERS UNDER THE GEL. The four stops above are the
        // hand-over, the plunge, the dark and the gel; from the gel's surface down
        // the fall is in the multi-layer void ({@link McsmVoidTiers}), and every
        // boundary it crosses is named exactly once, in the plan's own words.
        int tier = y > SURFACE_Y ? McsmVoidTiers.TIER_BASELINE : McsmVoidTiers.tierAt(y);
        if (tier != dive.tier) {
            dive.tier = tier;
            if (tier > McsmVoidTiers.TIER_BASELINE) {
                player.sendSystemMessage(Component.literal(McsmVoidTiers.entry(tier)));
            }
        }
        if (stop == 3) {
            dive.gelTicks++;
        }

        switch (stop) {
            case 0 -> say(player, dive, 0, new String[] {
                null, "\u00a78nothing above you has ever been this far away" });
            case 1 -> say(player, dive, 3, new String[] {
                "\u00a78\u00a7othe light above is going out",
                "\u00a78there is nothing under you, and nothing above you",
                "\u00a75the air has stopped being air" });
            case 2 -> say(player, dive, 3, new String[] {
                "\u00a7d\u00a7lTHE GEL \u00a78\u00b7 you are inside something now",
                "\u00a77it holds you without holding you: fly, drift, breathe",
                "\u00a7dthe bubbles are coming up past you" });
            default -> {
                // the last stop: the deep's own clock, in the player's own units --
                // "another 20 to 30 seconds" of sinking before the floor or an island
                say(player, dive, 0, new String[] {
                    null, "\u00a7d\u00a7lsomething enormous is floating over you",
                    "\u00a77the gel is going to put you down" });
                catchFall(player, level, dive);
            }
        }
    }

    /** THE FALL STILL DOES NOT KILL YOU: the deep sets them down, it never lets them hit. */
    private static void catchFall(ServerPlayer player, ServerLevel level, Dive dive) {
        if (dive.landed) {
            return;
        }
        try {
            BlockPos shelf = surfaceUnder(level, new BlockPos((int) Math.floor(player.getX()),
                    (int) Math.floor(player.getY()) - 1, (int) Math.floor(player.getZ())));
            if (shelf == null) {
                return;
            }
            double gap = player.getY() - (shelf.getY() + 1);
            if (gap > CATCH_ABOVE || gap < 0.0D) {
                return;
            }
            dive.landed = true;
            player.teleportTo(level, player.getX(), shelf.getY() + 1.0D, player.getZ(),
                    java.util.Set.of(), player.getYRot(), player.getXRot(), false);
            player.sendSystemMessage(Component.literal(
                    "\u00a7d\u00a7lTHE DEEP \u00a78\u00b7 the gel put you down on something it grew"));
            player.sendSystemMessage(Component.literal(
                    "\u00a77nothing here will fall on you. look up: that is the whole sky."));
        } catch (Throwable ignored) {
            // no shelf: the fall simply continues
        }
    }

    /** The highest solid block under a column, or null when the column is nothing. */
    private static BlockPos surfaceUnder(ServerLevel level, BlockPos from) {
        try {
            // BUILD #481 -- the world below is 2032 deep now, so the scan is
            // bounded to what the catch can actually use: a shelf more than a few
            // blocks below is not a catch, it is just the fall continuing.
            for (int y = from.getY(); y > level.getMinY() && y > from.getY() - 24; y--) {
                BlockPos at = new BlockPos(from.getX(), y, from.getZ());
                BlockState state = level.getBlockState(at);
                if (!state.isAir()) {
                    return at;
                }
            }
        } catch (Throwable ignored) {
            // the column is nothing all the way down
        }
        return null;
    }

    /** One line, once, in order -- and never twice for the same stop. */
    private static void say(ServerPlayer player, Dive dive, int at, String[] lines) {
        if (dive.said >= lines.length - 1) {
            return;
        }
        int next = dive.said + 1;
        dive.said = next;
        String line = lines[next];
        if (line != null) {
            player.sendSystemMessage(Component.literal(line));
        }
    }

    // ------------------------------------------------------------------
    // What the client and the damage path ask
    // ------------------------------------------------------------------

    /** Is this player on the descent right now? */
    public static boolean diving(Entity entity) {
        if (entity == null || !McsmExtrasConfig.voidDescent) {
            return false;
        }
        return DIVING.containsKey(entity.getUUID());
    }

    /**
     * THE FALL NEVER KILLS YOU.
     *
     * <p>Read by the damage path: a player on the descent is exempt from the world's
     * own ideas about falling -- the void's out-of-world damage included, which is
     * exactly what used to end this dive one second in. It ends when the descent does
     * (landing, catching themselves, or leaving the deep).
     */
    public static boolean protectedFall(Entity entity) {
        return diving(entity);
    }

    /** How far through the fall a player is, 0..1, for the air and the art. */
    public static float progress(Entity entity) {
        Dive dive = entity == null ? null : DIVING.get(entity.getUUID());
        if (dive == null) {
            return 0.0F;
        }
        return switch (dive.stop) {
            case 0 -> 0.10F;
            case 1 -> 0.40F;
            case 2 -> 0.70F;
            default -> 1.0F;
        };
    }

    /**
     * BUILD #483 -- a wrap moved the player UP, and this tracker reads "Y went up"
     * as "the player has stopped falling". Called by {@link McsmVoidLoop} at the
     * seam: the dive continues, it did not end, and the fall's own clock is reset
     * rather than left to fire a stall that never happened.
     */
    public static void noteWrap(ServerPlayer player) {
        try {
            Dive dive = DIVING.get(player.getUUID());
            if (dive != null) {
                dive.lastY = Double.MAX_VALUE;
                dive.stalled = 0;
                dive.landed = false;
            }
        } catch (Throwable ignored) {
            // the fall continues either way
        }
    }

    /** The `/ds` line: who is falling, and how far in. */
    public static String state() {
        // BUILD #481 -- the fall's own state, plus what the void is under it and
        // what the rudder is doing: one line, everything a report needs.
        return DIVING.size() + " player(s) on the descent right now \u00b7 "
                + McsmVoidRudderItem.state() + " \u00b7 world "
                + McsmVoidTiers.DIM_MIN_Y + ".." + McsmVoidTiers.DIM_MAX_Y
                + " \u00b7 tier speeds " + McsmVoidTiers.SPEED[0] + ".."
                + McsmVoidTiers.SPEED[McsmVoidTiers.TIERS - 1] + " b/s";
    }
}
