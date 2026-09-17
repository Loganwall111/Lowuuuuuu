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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BUILD #451 -- ONE PORTAL PER DIMENSION, AND NONE OF THEM THE SAME.
 *
 * <p>The report: "a unique portal for each dimensions". Until now the ways in were
 * a sneak gesture with a key, a command, or a rite that fired on a ring of blocks --
 * all of which work and none of which is a DOORWAY. This is the doorway.
 *
 * <p>Each dimension has its own frame and its own glass, out of that dimension's own
 * material, so they are told apart at a glance (and from across a room):
 *
 * <pre>
 *   the rift (decayed reality)  rift anchors   around  reality glass
 *   adams (the infinite)        memory crystal around  city tile
 *   the void                    void core      around  an abyss orb
 *   the Creator's reach         gilded marble  around  the reach's own glass
 * </pre>
 *
 * <p>WALK IN AND YOU GO. No item, no command, no menu, no activation step: the door
 * is checked where the player is standing, every level tick, once per dimension and
 * with a cooldown so a doorway cannot become a loop. The shape is the plainest one
 * there is -- two tall, one wide, with that dimension's frame on both sides, above
 * and below -- and {@code /ds portal build <id>} raises exactly that shape in front
 * of whoever asks, so nobody has to guess it.
 */
public final class McsmPortals {

    /** One dimension's doorway. */
    public record Door(String id, String dimensionName, String frameBlock, String doorBlock,
                       String label) {
    }

    private static final Door[] DOORS = {
        new Door("decayed", "mcsm:decayed_reality", "mcsm:rift_anchor", "mcsm:reality_glass",
                "THE RIFT"),
        // BUILD #458 -- every doorway is built from the material of the world
        // behind it, and from nothing else. Adams was memory crystal around city
        // tile, i.e. two blocks belonging to other places; it is its own crystal
        // around its own brick now, and the void's arch is its own anchor around
        // its own glass rather than a shared rift anchor and a black-hole core.
        new Door("adams", "mcsm:adams_infinity", "mcsm:adams_crystal", "mcsm:adams_bricks",
                "THE INFINITE DIMENSION"),
        new Door("void", "mcsm:void_reality", "mcsm:void_anchor", "mcsm:void_glass",
                "THE VOID"),
        // BUILD #462 -- and the fourth: the Creator's reach. Gilded marble for the
        // frame and the dimension's own glass for the opening, so the one doorway
        // that was built rather than torn reads that way from across a room.
        new Door("creator", "mcsm:creators_realm", "mcsm:creator_gold", "mcsm:creator_glass",
                "THE CREATOR'S REACH"),
    };

    /** Ticks a player must wait between doorways. */
    private static final long COOLDOWN = 60L;
    private static final Map<UUID, Long> LAST = new ConcurrentHashMap<>();

    private McsmPortals() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmPortals::tick);
            // BUILD #462 -- the list prints itself now: a fourth dimension must not
            // need this line edited to be announced.
            StringBuilder ids = new StringBuilder();
            for (Door door : DOORS) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(door.id());
            }
            System.out.println("[ds] " + DOORS.length + " doorways armed (" + ids + ")");
        } catch (Throwable t) {
            System.err.println("[ds] the doorways could not hook the level tick: " + t);
        }
    }

    public static Door[] doors() {
        return DOORS.clone();
    }

    public static Door door(String id) {
        if (id == null) {
            return null;
        }
        for (Door door : DOORS) {
            if (door.id().equalsIgnoreCase(id)) {
                return door;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // Walking through
    // ---------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.portals || level.players().isEmpty()) {
                return;
            }
            BlockState[] frames = new BlockState[DOORS.length];
            BlockState[] glass = new BlockState[DOORS.length];
            for (int i = 0; i < DOORS.length; i++) {
                frames[i] = state(DOORS[i].frameBlock());
                glass[i] = state(DOORS[i].doorBlock());
            }
            for (ServerPlayer player : level.players()) {
                step(level, player, frames, glass);
            }
        } catch (Throwable t) {
            System.err.println("[ds] doorway tick failed: " + t);
        }
    }

    private static void step(ServerLevel level, ServerPlayer player, BlockState[] frames,
                             BlockState[] glass) {
        BlockPos feet = player.blockPosition();
        for (int i = 0; i < DOORS.length; i++) {
            if (frames[i] == null || glass[i] == null) {
                continue;
            }
            if (!is(level, feet, glass[i]) || !is(level, feet.above(), glass[i])) {
                continue;
            }
            if (!isFrame(level, feet, frames[i])) {
                continue;
            }
            enter(level, player, DOORS[i]);
            return;
        }
    }

    /** A 2-tall, 1-wide doorway: frame below, above, and on both sides. */
    private static boolean isFrame(ServerLevel level, BlockPos feet, BlockState frame) {
        if (!is(level, feet.below(), frame) || !is(level, feet.above(2), frame)) {
            return false;
        }
        boolean alongX = is(level, feet.east(), frame) && is(level, feet.west(), frame)
                && is(level, feet.above().east(), frame) && is(level, feet.above().west(), frame);
        boolean alongZ = is(level, feet.north(), frame) && is(level, feet.south(), frame)
                && is(level, feet.above().north(), frame) && is(level, feet.above().south(), frame);
        return alongX || alongZ;
    }

    private static boolean is(ServerLevel level, BlockPos at, BlockState state) {
        try {
            return level.getBlockState(at) == state
                    || level.getBlockState(at).getBlock() == state.getBlock();
        } catch (Throwable t) {
            return false;
        }
    }

    private static void enter(ServerLevel level, ServerPlayer player, Door door) {
        long now = level.getGameTime();
        Long last = LAST.get(player.getUUID());
        if (last != null && now - last < COOLDOWN) {
            return;
        }
        LAST.put(player.getUUID(), now);
        boolean moved;
        switch (door.id()) {
            case "decayed":
                moved = McsmReality.enter(player);
                break;
            case "adams":
                moved = McsmAdams.enter(player);
                break;
            case "void":
                moved = McsmVoid.enter(player);
                break;
            case "creator":
                moved = McsmCreatorRealm.enter(player);
                break;
            default:
                moved = false;
        }
        if (!moved) {
            player.sendSystemMessage(Component.literal(
                    "\u00a78\u00b7 the doorway is switched off in the config"));
        }
    }

    // ---------------------------------------------------------------------
    // Building one
    // ---------------------------------------------------------------------

    /**
     * Raises a doorway in front of the player, facing them: two tall, one wide,
     * framed out of the dimension's own blocks. Returns false when the dimension is
     * unknown or the world refused the writes.
     */
    public static boolean build(ServerPlayer player, String id) {
        Door door = door(id);
        if (door == null) {
            return false;
        }
        BlockState frame = state(door.frameBlock());
        BlockState glassState = state(door.doorBlock());
        if (frame == null || glassState == null) {
            return false;
        }
        try {
            ServerLevel level = player.level() instanceof ServerLevel server ? server : null;
            if (level == null) {
                return false;
            }
            BlockPos base = player.blockPosition().relative(facing(player));
            // the plane runs along whichever axis the player is NOT facing
            boolean alongX = facing(player).getStepX() == 0;
            BlockPos left = alongX ? base.east() : base.north();
            BlockPos right = alongX ? base.west() : base.south();
            BlockPos floor = base.below();
            BlockPos top = base.above(2);
            level.setBlock(floor, frame, 2);
            level.setBlock(top, frame, 2);
            level.setBlock(left, frame, 2);
            level.setBlock(right, frame, 2);
            level.setBlock(left.above(), frame, 2);
            level.setBlock(right.above(), frame, 2);
            level.setBlock(base, glassState, 2);
            level.setBlock(base.above(), glassState, 2);
            player.sendSystemMessage(Component.literal("\u00a75" + door.label()
                    + " \u00a78\u00b7 the doorway is up. walk into the middle."));
            return true;
        } catch (Throwable t) {
            System.err.println("[ds] could not raise a doorway: " + t);
            return false;
        }
    }

    private static net.minecraft.core.Direction facing(ServerPlayer player) {
        try {
            return player.getDirection();
        } catch (Throwable t) {
            return net.minecraft.core.Direction.NORTH;
        }
    }

    private static BlockState state(String id) {
        try {
            int colon = id.indexOf(':');
            if (colon > 0) {
                Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                id.substring(0, colon), id.substring(colon + 1)));
                // a block registry answers a missing id with AIR, not null: an air
                // "door" would be a hole in the wall that teleports nobody
                if (block != null && block != Blocks.AIR) {
                    return block.defaultBlockState();
                }
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return null;
    }

    /** "decayed=rift_anchor/reality_glass, ..." -- for the console and the gate. */
    public static String summary() {
        StringBuilder out = new StringBuilder();
        for (Door door : DOORS) {
            if (out.length() > 0) {
                out.append(" \u00b7 ");
            }
            out.append(door.id()).append('=').append(door.frameBlock())
               .append('/').append(door.doorBlock());
        }
        return out.toString();
    }
}
