package net.mcsm.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BUILD #443 -- RITUALS, AS A SYSTEM RATHER THAN AS SCENERY.
 *
 * <p>The wishlist asks for "rituals". A ritual is not a block and not a texture:
 * it is a thing the player DOES, over several steps, that the world then answers.
 * So this is a small engine with six rituals written into it, and it works like
 * this:
 *
 * <ol>
 *   <li><b>BUILD</b> a ring of the right blocks around a core block -- the ring
 *       can have gaps, because a player who is one block short should be able to
 *       see that they are close, not be told nothing at all;</li>
 *   <li><b>STAND</b> inside it, <b>HOLDING</b> the offering. Nothing is consumed
 *       until the ritual actually fires, so an attempt that fails costs nothing
 *       but the walk back;</li>
 *   <li>the world <b>ANSWERS</b>: each ritual has its own effect, its own sound,
 *       its own chat line, and its own line in the ritual catalogue.</li>
 * </ol>
 *
 * <p>Every site is remembered by position, so a ritual cannot be farmed on one
 * spot: build the ring somewhere else and it fires again. The scan runs on the
 * level tick for players within {@link #SCAN_RANGE} and checks a bounded number of
 * sites per player per pass, so a hundred ritual rings in one chunk cost the same
 * as one.
 *
 * <p>THE SIX, and what each one actually does:
 *
 * <table>
 *   <tr><td>{@code the_rift}</td><td>ring of rift anchors, core of reality glass,
 *       offering a rift shard -- sends the player through to the decayed reality
 *       (the arrival path is {@link McsmReality}'s own).</td></tr>
 *   <tr><td>{@code the_waking}</td><td>ring of glitch lamps around a void core,
 *       offering a glyph cell, storm phase 6.0 or later -- releases the MASSG,
 *       i.e. exactly what sneak-using the antenna does, but as something the
 *       player earned.</td></tr>
 *   <tr><td>{@code the_swarm}</td><td>ring of cracked road around a storm rib,
 *       offering hallucination dust -- calls the bestiary out of the ground
 *       around the site.</td></tr>
 *   <tr><td>{@code the_corruption}</td><td>ring of withered flesh around decayed
 *       stone, offering a decayed bone -- spreads the decayed palette through the
 *       ground in a radius, block by block, with a shockwave.</td></tr>
 *   <tr><td>{@code the_black_sun}</td><td>ring of rusted plate around a black hole
 *       core, offering an abyss orb, phase 7.0 or later -- opens a black hole
 *       overhead.</td></tr>
 *   <tr><td>{@code the_adams_gate}</td><td>ring of city tiles around a memory
 *       crystal, offering a memory fragment -- opens the infinite dimension of
 *       adams ({@link McsmAdams}).</td></tr>
 * </table>
 *
 * <p>Nothing here needs a shader, an entity registration or a packet: rings are
 * found by reading blocks, the effects are the mod's own server-side systems, and
 * the only client involvement is the chat line and the sound.
 */
public final class McsmRituals {

    /** Rings are only looked for around players this close. */
    public static final int SCAN_RANGE = 40;
    /** Ticks between two full scans for one player. */
    public static final int SCAN_PERIOD = 30;
    /** Points sampled on a ring. 12 lets a ring be up to two blocks short. */
    private static final int RING_SAMPLES = 12;
    /** How many of those sample points must be right for the ring to count. */
    private static final int RING_NEEDED = 10;
    /** How close to the core the player has to stand. */
    private static final double STAND_RANGE = 8.0D;
    /** Two firings of the same ritual cannot be closer together than this. */
    private static final long COOLDOWN_TICKS = 200L;

    private McsmRituals() {
    }

    // -------------------------------------------------------------------------
    // The rituals themselves
    // -------------------------------------------------------------------------

    /**
     * One ritual: the shape it is built in, the price it asks, the condition it
     * needs and the effect it has. {@code phase} is the storm phase a ritual
     * needs before it will answer (0 = none).
     */
    public record Ritual(String id, String title, String ring, String core, String offering,
                         int radius, double phase, String hint, int effect) {
    }

    public static final int EFFECT_RIFT = 0;
    public static final int EFFECT_WAKING = 1;
    public static final int EFFECT_SWARM = 2;
    public static final int EFFECT_CORRUPTION = 3;
    public static final int EFFECT_BLACK_SUN = 4;
    public static final int EFFECT_ADAMS = 5;

    public static final List<Ritual> RITUALS = List.of(
        new Ritual("the_rift", "The Rift", "mcsm:rift_anchor", "mcsm:reality_glass",
                "mcsm:rift_shard", 5, 0.0D,
                "a ring of rift anchors around reality glass, holding a rift shard",
                EFFECT_RIFT),
        new Ritual("the_waking", "The Waking", "mcsm:glitch_lamp", "mcsm:void_core",
                "mcsm:glyph_cell", 6, 6.0D,
                "a ring of glitch lamps around a void core, holding a glyph cell, at phase 6",
                EFFECT_WAKING),
        new Ritual("the_swarm", "The Swarm", "mcsm:cracked_road", "mcsm:storm_rib",
                "mcsm:hallucination_dust", 7, 0.0D,
                "a ring of cracked road around a storm rib, holding hallucination dust",
                EFFECT_SWARM),
        new Ritual("the_corruption", "The Corruption", "mcsm:withered_flesh_block",
                "mcsm:decayed_stone", "mcsm:decayed_bone", 5, 0.0D,
                "a ring of withered flesh around decayed stone, holding a decayed bone",
                EFFECT_CORRUPTION),
        new Ritual("the_black_sun", "The Black Sun", "mcsm:rusted_plate", "mcsm:black_hole_core",
                "mcsm:abyss_orb", 8, 7.0D,
                "a ring of rusted plate around a black hole core, holding an abyss orb, at phase 7",
                EFFECT_BLACK_SUN),
        new Ritual("the_adams_gate", "The Adams Gate", "mcsm:city_tiles", "mcsm:memory_crystal",
                "mcsm:memory_fragment", 4, 0.0D,
                "a ring of city tiles around a memory crystal, holding a memory fragment",
                EFFECT_ADAMS));

    /** Sites already fired: ritual index and core position folded into one key. */
    private static final Set<Long> SPENT = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> LAST = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> CURSOR = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> COOLDOWN = new ConcurrentHashMap<>();

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmRituals::tick);
            System.out.println("[ds] rituals armed: " + RITUALS.size()
                    + " rites, ring + offering + condition -> the world answers");
        } catch (Throwable t) {
            System.err.println("[ds] rituals could not hook the level tick: " + t);
        }
    }

    // -------------------------------------------------------------------------
    // The scan
    // -------------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.rituals) {
                return;
            }
            long now = level.getGameTime();
            for (ServerPlayer player : level.players()) {
                Long last = LAST.get(player.getUUID());
                if (last != null && now - last < SCAN_PERIOD) {
                    continue;
                }
                LAST.put(player.getUUID(), now);
                scanFor(level, player, now);
            }
        } catch (Throwable t) {
            System.err.println("[ds] ritual tick failed: " + t);
        }
    }

    /**
     * Look for one ritual around this player, round-robin over the six, so the
     * cost per pass is one ring-scan no matter how many rites are registered.
     */
    private static void scanFor(ServerLevel level, ServerPlayer player, long now) {
        int index = Math.floorMod(CURSOR.merge(player.getUUID(), 1, Integer::sum),
                RITUALS.size());
        Ritual ritual = RITUALS.get(index);
        BlockPos core = findCore(level, player, ritual);
        if (core == null) {
            return;
        }
        if (ringCount(level, core, ritual) < RING_NEEDED) {
            return;
        }
        if (!holding(player, ritual.offering())) {
            return;
        }
        double phase = phaseNear(level, player);
        if (phase < ritual.phase()) {
            // The ring is right, the offering is right, the world is not ready --
            // and saying so is the whole difference between a system and a wall.
            if (noSpam(player, now)) {
                player.sendSystemMessage(Component.literal("\u00a75the ring is ready \u00a78\u00b7 "
                        + ritual.title() + " waits for the storm to reach \u00a7f"
                        + ritual.phase() + "\u00a78 (now \u00a7f"
                        + String.format(java.util.Locale.ROOT, "%.2f", phase) + "\u00a78)"));
            }
            return;
        }
        long spent = spentKey(index, core);
        if (!SPENT.add(spent)) {
            return;
        }
        Long cooled = COOLDOWN.get(player.getUUID() + ":" + (long) index);
        if (cooled != null && now - cooled < COOLDOWN_TICKS) {
            return;
        }
        fire(level, player, ritual, core);
    }

    /** The core block of this ritual, near the player, or null. */
    private static BlockPos findCore(ServerLevel level, ServerPlayer player, Ritual ritual) {
        int px = (int) Math.floor(player.getX());
        int py = (int) Math.floor(player.getY());
        int pz = (int) Math.floor(player.getZ());
        BlockState want = state(ritual.core());
        if (want == null) {
            return null;
        }
        for (int dy = -3; dy <= 3; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos pos = new BlockPos(px + dx, py + dy, pz + dz);
                    try {
                        if (level.getBlockState(pos).equals(want)
                                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D,
                                        pos.getZ() + 0.5D) <= STAND_RANGE * STAND_RANGE) {
                            return pos;
                        }
                    } catch (Throwable ignored) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /** How many of the ring's sample points hold the right block. */
    private static int ringCount(ServerLevel level, BlockPos core, Ritual ritual) {
        BlockState want = state(ritual.ring());
        if (want == null) {
            return 0;
        }
        int found = 0;
        for (int i = 0; i < RING_SAMPLES; i++) {
            double a = 2.0D * Math.PI * i / RING_SAMPLES;
            int x = core.getX() + (int) Math.round(Math.cos(a) * ritual.radius());
            int z = core.getZ() + (int) Math.round(Math.sin(a) * ritual.radius());
            for (int dy = -1; dy <= 1; dy++) {
                try {
                    if (level.getBlockState(new BlockPos(x, core.getY() + dy, z)).equals(want)) {
                        found++;
                        break;
                    }
                } catch (Throwable ignored) {
                    break;
                }
            }
        }
        return found;
    }

    private static boolean holding(ServerPlayer player, String offering) {
        try {
            Item item = item(offering);
            if (item == null) {
                return false;
            }
            return player.getMainHandItem().getItem() == item
                    || player.getOffhandItem().getItem() == item;
        } catch (Throwable t) {
            return false;
        }
    }

    /** The storm's own phase near this player, 0 when there is no storm. */
    private static double phaseNear(ServerLevel level, ServerPlayer player) {
        try {
            return McsmCreatures.phaseNearPublic(level, player);
        } catch (Throwable t) {
            return 0.0D;
        }
    }

    private static boolean noSpam(ServerPlayer player, long now) {
        Long last = COOLDOWN.get(player.getUUID() + ":note");
        if (last != null && now - last < 200L) {
            return false;
        }
        COOLDOWN.put(player.getUUID() + ":note", now);
        return true;
    }

    private static long spentKey(int index, BlockPos core) {
        return ((long) index << 52) ^ ((long) (core.getX() & 0xFFFFF) << 32)
                ^ ((long) (core.getY() & 0xFF) << 24) ^ (core.getZ() & 0xFFFFFFL);
    }

    // -------------------------------------------------------------------------
    // The answer
    // -------------------------------------------------------------------------

    private static void fire(ServerLevel level, ServerPlayer player, Ritual ritual, BlockPos core) {
        try {
            double x = core.getX() + 0.5D;
            double y = core.getY() + 1.0D;
            double z = core.getZ() + 0.5D;
            consume(player, ritual.offering());
            COOLDOWN.put(player.getUUID() + ":" + RITUALS.indexOf(ritual), level.getGameTime());

            level.playSound((Entity) null, x, y, z, McsmSounds.OBLIVION_DRONE,
                    SoundSource.PLAYERS, 1.0F, 0.85F);
            level.playSound((Entity) null, x, y, z, McsmSounds.MASSG_HEART,
                    SoundSource.PLAYERS, 0.8F, 1.0F);
            player.sendSystemMessage(Component.literal("\u00a75\u00a7lRITUAL COMPLETE \u00a78\u00b7 "
                    + ritual.title().toUpperCase(java.util.Locale.ROOT) + " \u00a7fanswers"));

            switch (ritual.effect()) {
                case EFFECT_RIFT -> {
                    McsmReality.enter(player);
                    player.sendSystemMessage(Component.literal(
                            "\u00a78\u00b7 the ring swallows you whole"));
                }
                case EFFECT_WAKING -> {
                    if (!McsmMassg.summon(level, player)) {
                        player.sendSystemMessage(Component.literal(
                                "\u00a78\u00b7 the set does not answer (it is already here, or this "
                                + "world has it switched off)"));
                    }
                }
                case EFFECT_SWARM -> {
                    int n = McsmCreatures.release(level, core.above(2), 4, Math.max(4.0D, phaseNear(level, player)));
                    player.sendSystemMessage(Component.literal("\u00a78\u00b7 "
                            + n + " of them come up out of the ground around the ring"));
                    McsmCreatures.say(level, net.minecraft.world.phys.Vec3.atCenterOf(core), 48.0D,
                            "\"the ground is not a floor\"", net.minecraft.ChatFormatting.DARK_PURPLE);
                }
                case EFFECT_CORRUPTION -> {
                    int changed = corrupt(level, core, 22);
                    player.sendSystemMessage(Component.literal("\u00a78\u00b7 the decay spreads through "
                            + changed + " blocks"));
                }
                case EFFECT_BLACK_SUN -> {
                    boolean opened = McsmBlackHole.openNear(level, player);
                    player.sendSystemMessage(Component.literal(opened
                            ? "\u00a78\u00b7 something opens above the ring and starts eating the light"
                            : "\u00a78\u00b7 the sky refuses (one is already open, or it is too close to "
                              + "another)"));
                }
                case EFFECT_ADAMS -> {
                    if (!McsmAdams.enter(player)) {
                        player.sendSystemMessage(Component.literal(
                                "\u00a78\u00b7 the gate does not open (it is switched off, or the "
                                + "dimension is not loaded)"));
                    }
                }
                default -> {
                    // no effect registered: the ritual still fired and was paid for
                }
            }
        } catch (Throwable t) {
            System.err.println("[ds] ritual " + ritual.id() + " failed to fire: " + t);
        }
    }

    private static void consume(ServerPlayer player, String offering) {
        try {
            Item item = item(offering);
            if (item == null) {
                return;
            }
            ItemStack main = player.getMainHandItem();
            if (main.getItem() == item) {
                main.shrink(1);
                return;
            }
            ItemStack off = player.getOffhandItem();
            if (off.getItem() == item) {
                off.shrink(1);
            }
        } catch (Throwable ignored) {
            // an offering that cannot be consumed is a gift, not a bug
        }
    }

    /** Spread the decayed palette through the ground around a rite. */
    private static int corrupt(ServerLevel level, BlockPos core, int radius) {
        BlockState surface = state("mcsm:decayed_surface");
        BlockState stone = state("mcsm:decayed_stone");
        BlockState flesh = state("mcsm:withered_flesh_block");
        int changed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if ((dx * dx + dz * dz) > radius * radius) {
                    continue;
                }
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos pos = core.offset(dx, dy, dz);
                    try {
                        BlockState here = level.getBlockState(pos);
                        if (here.isAir()) {
                            continue;
                        }
                        boolean skin = level.getBlockState(pos.above()).isAir();
                        BlockState next = skin ? surface : (dy == -3 ? flesh : stone);
                        if (next != null && !here.equals(next)) {
                            level.setBlock(pos, next, 2);
                            changed++;
                        }
                    } catch (Throwable ignored) {
                        return changed;
                    }
                }
            }
        }
        return changed;
    }

    // -------------------------------------------------------------------------
    // Ids
    // -------------------------------------------------------------------------

    private static final Map<String, BlockState> BLOCK_CACHE = new HashMap<>();
    private static final Map<String, Item> ITEM_CACHE = new HashMap<>();

    private static BlockState state(String id) {
        BlockState cached = BLOCK_CACHE.get(id);
        if (cached != null) {
            return cached;
        }
        try {
            int colon = id.indexOf(':');
            Block block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(
                    id.substring(0, colon), id.substring(colon + 1)));
            if (block != null) {
                BlockState state = block.defaultBlockState();
                BLOCK_CACHE.put(id, state);
                return state;
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return null;
    }

    private static Item item(String id) {
        Item cached = ITEM_CACHE.get(id);
        if (cached != null) {
            return cached;
        }
        try {
            int colon = id.indexOf(':');
            Item found = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(
                    id.substring(0, colon), id.substring(colon + 1)));
            if (found != null) {
                ITEM_CACHE.put(id, found);
                return found;
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Reporting: the catalogue, for /ds ritual and the panel
    // -------------------------------------------------------------------------

    /** One line per ritual: what to build, what to hold, what it does. */
    public static List<String> catalogue() {
        List<String> out = new ArrayList<>();
        for (Ritual r : RITUALS) {
            out.add(r.title() + " (" + r.id() + ") -- " + r.hint());
        }
        return out;
    }

    /** How close a player is to the nearest ring, for /ds ritual check. */
    public static String nearest(ServerLevel level, ServerPlayer player) {
        for (int i = 0; i < RITUALS.size(); i++) {
            Ritual r = RITUALS.get(i);
            BlockPos core = findCore(level, player, r);
            if (core == null) {
                continue;
            }
            int ring = ringCount(level, core, r);
            boolean held = holding(player, r.offering());
            BlockState want = state(r.core());
            return r.title() + ": core " + (want != null ? "found" : "MISSING")
                    + " at " + core.getX() + " " + core.getY() + " " + core.getZ()
                    + ", ring " + ring + "/" + RING_SAMPLES
                    + " (needs " + RING_NEEDED + "), offering "
                    + (held ? "in hand" : "not held");
        }
        return "no ritual core within " + STAND_RANGE + " blocks (the core block is the one the "
                + "ring is built around: /ds ritual list)";
    }

    /** Where the player would be sent, for the guide page and /ds reality. */
    public static String where(Level level) {
        return level != null && level.dimension().equals(McsmAdams.ADAMS)
                ? "the infinite dimension of adams"
                : "the world";
    }
}
