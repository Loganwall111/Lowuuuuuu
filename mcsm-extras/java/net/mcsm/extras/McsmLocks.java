package net.mcsm.extras;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * BUILD #459 -- THE LOCKS. Every world locks its own doors, and only its key fits.
 *
 * <p>THE ASK, in the middle of the identity list: "own blocks, items, mobs,
 * LOCKS, VFX". Until now the mod had exactly one key in it -- the city keycard the
 * server room hands out -- and nothing to put it in: the crate it opens is a
 * container, not a door. So a lock is a block now, and there is one per world:
 *
 * <ul>
 *   <li><b>mcsm:decayed_lock</b> -- the abandoned cities' vault seal. Wants the
 *       {@code mcsm:city_keycard} the server rooms already hand out.</li>
 *   <li><b>mcsm:adams_lock</b> -- the seal on the chambers the infinite dimension
 *       builds forever. Wants {@code mcsm:adams_sigil}, which is only ever found
 *       in that dimension's own crates.</li>
 *   <li><b>mcsm:void_lock</b> -- the seal on the void's floating houses. Wants
 *       {@code mcsm:void_sigil}, which is only ever found in the void's own
 *       cache.</li>
 * </ul>
 *
 * <p>WHAT A LOCK DOES. Right-click it: with the world's own key in either hand it
 * opens -- it is consumed, the way a physical seal is, and the world says so in
 * its own voice. Without the key it stays exactly where it is and tells you what
 * it wants, which is the whole point of a lock being a block rather than a
 * command: the player learns the world's key by meeting its doors.
 *
 * <p>NOTHING IS CLIENT-SIDE AND NOTHING IS A GUESS. The block's interaction hook
 * is the same {@code useItemOn} override the mod's own storm beacon block already
 * uses (proven by the compiler in an earlier run), the key comparison is the
 * {@code getMainHandItem().getItem() == key} idiom {@link McsmReality} already
 * teleports players with, and the block change is {@code setBlock(pos, air, 3)},
 * the same call the server rooms' own power failure uses. The world a lock belongs
 * to comes from {@link McsmIdentity}, so a lock cannot ask for another world's key.
 */
public final class McsmLocks {

    /** Per-lock cooldown, so a held mouse button cannot spam the "locked" line. */
    private static final Map<Long, Long> LAST_REFUSED = new ConcurrentHashMap<>();
    private static final long REFUSAL_TICKS = 40L;

    private McsmLocks() {
    }

    /** The key a world's locks take, or null when the world has no key. */
    public static Item keyFor(String dimId) {
        if (McsmIdentity.DECAYED.equals(dimId)) {
            return McsmContent.CITY_KEYCARD;
        }
        if (McsmIdentity.ADAMS.equals(dimId)) {
            return McsmContent.ADAMS_SIGIL;
        }
        if (McsmIdentity.VOID.equals(dimId)) {
            return McsmContent.VOID_SIGIL;
        }
        return null;
    }

    /** What the refusal line calls the key. */
    public static String keyNameFor(String dimId) {
        if (McsmIdentity.DECAYED.equals(dimId)) {
            return "the city keycard";
        }
        if (McsmIdentity.ADAMS.equals(dimId)) {
            return "an Adams sigil";
        }
        if (McsmIdentity.VOID.equals(dimId)) {
            return "a void sigil";
        }
        return "a key this world does not make";
    }

    /** What the refusal line calls the world. */
    private static String worldName(String dimId) {
        McsmIdentity.Skin skin = McsmIdentity.skin(dimId);
        return skin == null ? "this place" : skin.label();
    }

    /** The block id a world's lock is, for the gates and for /ds lock. */
    public static String blockIdFor(String dimId) {
        return McsmIdentity.skin(dimId) == null ? null : "mcsm:" + dimId + "_lock";
    }

    /**
     * The interaction: called by {@link McsmContent.DimensionLock#useItemOn}. The
     * result is what the game expects from a block hook -- SUCCESS on the client so
     * the arm swings, CONSUME on the server because the lock is done talking.
     */
    public static InteractionResult use(String dimId, Level level, BlockPos pos, Player player) {
        try {
            if (level == null || pos == null || dimId == null) {
                return InteractionResult.PASS;
            }
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            Item key = keyFor(dimId);
            if (key == null) {
                return InteractionResult.PASS;
            }
            boolean holds;
            try {
                holds = (player.getMainHandItem().getItem() == key)
                        || (player.getOffhandItem().getItem() == key);
            } catch (Throwable t) {
                holds = false;
            }
            if (!holds) {
                refuse(level, pos, player, dimId);
                return InteractionResult.CONSUME;
            }
            // THE KEY FITS. The seal is spent -- a lock is a physical thing, so it
            // is gone once it has been opened, and what it was holding is yours.
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    McsmSounds.OBLIVION_WARP, SoundSource.BLOCKS, 0.9F, 1.0F);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.literal("\u00a7dTHE SEAL OPENS \u00a78\u00b7 "
                        + worldName(dimId) + " lets you through"));
            }
            return InteractionResult.CONSUME;
        } catch (Throwable t) {
            return InteractionResult.PASS;
        }
    }

    /** The refusal: the lock stays, says what it wants, and stays quiet for a bit. */
    private static void refuse(Level level, BlockPos pos, Player player, String dimId) {
        try {
            long now = level.getGameTime();
            Long last = LAST_REFUSED.get(pos.asLong());
            if (last != null && now - last < REFUSAL_TICKS) {
                return;
            }
            LAST_REFUSED.put(pos.asLong(), now);
            if (LAST_REFUSED.size() > 4096) {
                LAST_REFUSED.clear();
            }
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    McsmSounds.TERMINAL_DENY, SoundSource.BLOCKS, 0.7F, 0.8F);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.literal("\u00a78\u00b7 sealed \u00a77-- \u00a7f"
                        + keyNameFor(dimId) + " \u00a77opens this. \u00a78("
                        + worldName(dimId) + ")"));
            }
        } catch (Throwable ignored) {
            // a lock that cannot speak is still a lock
        }
    }

    /** Boot line, so the log names every lock and what it takes. */
    public static String summary() {
        StringBuilder sb = new StringBuilder("[ds] dimension locks:");
        for (McsmIdentity.Skin skin : McsmIdentity.all()) {
            Item key = keyFor(skin.id());
            sb.append(' ').append(skin.id()).append("--").append(keyNameFor(skin.id()));
            if (key == null) {
                sb.append("(no key registered)");
            }
        }
        return sb.toString();
    }
}
