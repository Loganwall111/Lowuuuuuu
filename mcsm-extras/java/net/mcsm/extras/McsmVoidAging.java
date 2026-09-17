package net.mcsm.extras;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.mcsm.extras.client.McsmVoidAgingPayload;

/**
 * BUILD #463 -- VOID AGING. nextOrder (d), and the fourth list's own words:
 * "the void effect = aging ... the player model slowly becomes the corrupted
 * void (happy-then-corrupted lore)".
 *
 * <p>Staying in the void costs something, and it is not health. A player who
 * keeps going back is changed by it in five visible stages: clean, touched,
 * marked, claimed, taken. The change is the model itself -- every client tints
 * that player's own body toward the void's own palette as the value rises (see
 * {@code McsmVoidAgingRendererMixin}, which reads the vanilla render state the
 * same way the base mod's wither-sickness layer does) -- and it is the voice:
 * the lines start out kind, because the place is kind at first, and they stop
 * being kind.
 *
 * <pre>
 *   touched   3 minutes in the void    the air is warm, and it is glad you came
 *   marked    8 minutes                it has noticed you, and it is keeping count
 *   claimed   15 minutes               you are staying, and you are kept
 *   taken     25 minutes               you have always been here
 * </pre>
 *
 * <p>Aging is per player, it is SAVED (see {@link McsmExtrasConfig#voidAgingLedger}),
 * it grows in the void fastest, in the decayed reality slowly, in the infinite
 * dimension barely at all, and it comes back down while a player is anywhere
 * else -- slowly, because the mod is about consequences rather than chores.
 * Everything here is server-side and guarded: a client that never receives the
 * payload simply sees an untinted world.
 */
public final class McsmVoidAging {

    private McsmVoidAging() {
    }

    /** The five stages, in order. */
    public static final String[] STAGES = {"clean", "touched", "marked", "claimed", "taken"};

    /**
     * Ticks spent in the void at which each stage begins: 3, 8, 15 and 25 minutes.
     * The last one is the whole of it, and there is nothing after "taken".
     */
    public static final long[] THRESHOLDS = {0L, 3600L, 9600L, 18000L, 30000L};

    /** The value at which aging is complete. */
    public static final long MAX = THRESHOLDS[THRESHOLDS.length - 1];

    /** How the lines are coloured: kinder at the start, wrong at the end. */
    private static final ChatFormatting[] COLOURS = {ChatFormatting.GRAY, ChatFormatting.GREEN,
            ChatFormatting.YELLOW, ChatFormatting.GOLD, ChatFormatting.DARK_PURPLE};

    /**
     * THE LORE, stage by stage, happy at the top and corrupted at the bottom.
     * This is the whole point of an ageing effect: a player should notice the
     * change in the place's manners before they notice it in their own body.
     */
    private static final String[][] LORE = {
            {},
            {
                "\u00a77the air in here is warm",
                "\u00a77you could rest. nobody would know",
                "\u00a77it is quiet, and the quiet likes you",
                "\u00a77stay a little longer. it does not mind",
            },
            {
                "\u00a7ethe quiet is not quiet any more",
                "\u00a7eit has noticed you. it keeps a count",
                "\u00a7eit knows how many times you have come back",
                "\u00a7eyou are welcome here. that is the problem",
            },
            {
                "\u00a76\u00a7lit is not keeping you. you are kept",
                "\u00a76\u00a7lthe way back is still there. it is harder to want",
                "\u00a76\u00a7lyou are staying",
                "\u00a76\u00a7lyour name has been written down somewhere dark",
            },
            {
                "\u00a75\u00a7lyou have always been here",
                "\u00a75\u00a7lthere was never a way back. there was a way in",
                "\u00a75\u00a7lsay the word, and it will be your name",
                "\u00a75\u00a7lwe are not the void. we are what it keeps",
            },
    };

    /** Per player, in ticks. The save file is the ledger below. */
    private static final Map<UUID, Long> AGES = new ConcurrentHashMap<>();
    /** Per player, the last time the place said anything to them. */
    private static final Map<UUID, Long> LAST = new ConcurrentHashMap<>();

    private static boolean ledgerDirty = false;
    private static boolean done = false;

    /** Booted from the base mod's onInitialize, with everything else. */
    public static void register() {
        if (done) {
            return;
        }
        done = true;
        try {
            McsmExtrasConfig.load();
            loadLedger();
            PayloadTypeRegistry.clientboundPlay().register(McsmVoidAgingPayload.TYPE,
                    McsmVoidAgingPayload.CODEC);
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmVoidAging::tick);
            System.out.println("[ds] void aging is armed (" + summary() + ")");
        } catch (Throwable t) {
            System.err.println("[ds] void aging could not be armed: " + t);
        }
    }

    // -------------------------------------------------------------------------
    // What the value is
    // -------------------------------------------------------------------------

    public static long ageOf(ServerPlayer player) {
        Long v = AGES.get(player.getUUID());
        return v == null ? 0L : v;
    }

    /** 0..1, how far into the void this player is. What the client tints by. */
    public static float fractionOf(ServerPlayer player) {
        return Math.max(0.0F, Math.min(1.0F, ageOf(player) / (float) MAX));
    }

    /** The stage a tick count sits in, 0..4. */
    public static int stageOf(long ticks) {
        int stage = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (ticks >= THRESHOLDS[i]) {
                stage = i;
            }
        }
        return stage;
    }

    public static String stageName(int stage) {
        return stage < 0 || stage >= STAGES.length ? STAGES[0] : STAGES[stage];
    }

    public static void setAge(ServerPlayer player, long ticks) {
        long clamped = Math.max(0L, Math.min(MAX, ticks));
        AGES.put(player.getUUID(), clamped);
        ledgerDirty = true;
        announce(player, stageOf(clamped));
        send(player);
    }

    public static void clear(ServerPlayer player) {
        AGES.remove(player.getUUID());
        LAST.remove(player.getUUID());
        ledgerDirty = true;
        send(player);
    }

    public static String summary() {
        return STAGES.length + " stages, " + (MAX / 1200L)
                + " minutes to the last one, " + AGES.size() + " player(s) on the ledger";
    }

    // -------------------------------------------------------------------------
    // The tick: it grows, it says something, and it comes back down
    // -------------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.voidAging || level.players().isEmpty()) {
                return;
            }
            long now = level.getGameTime();
            boolean inVoid = level.dimension().equals(McsmVoid.DIMENSION);
            boolean inDecayed = level.dimension().equals(McsmReality.DECAYED_REALITY);
            boolean inAdams = level.dimension().equals(McsmAdams.ADAMS);
            boolean inReach = level.dimension().equals(McsmCreatorRealm.DIMENSION);
            boolean ours = McsmIdentity.forLevel(level) != null;
            for (ServerPlayer player : level.players()) {
                long age = ageOf(player);
                if (inVoid) {
                    age = grow(player, age + 1L);
                } else if (inDecayed && now % 4L == 0L) {
                    // the decayed reality is the void's shadow: it ages you in
                    // minutes, not in hours, but it does age you
                    age = grow(player, age + 1L);
                } else if (inAdams && now % 8L == 0L) {
                    age = grow(player, age + 1L);
                } else if (!ours && !inReach && now % 2L == 0L && age > 0L) {
                    // the Overworld, the Nether and the End give it back -- slowly,
                    // because a mark that rubs off in a minute is not a mark
                    age = Math.max(0L, age - 1L);
                    AGES.put(player.getUUID(), age);
                    if (age == 0L) {
                        ledgerDirty = true;
                        send(player);
                    }
                }
                breathe(level, player, now, age);
                if (now % 20L == 0L && age > 0L) {
                    send(player);
                }
            }
            if (ledgerDirty && now % 600L == 0L) {
                flush();
            }
        } catch (Throwable t) {
            System.err.println("[ds] void aging tick failed: " + t);
        }
    }

    /** Adds to a player's age, announces a stage crossing, and marks the ledger. */
    private static long grow(ServerPlayer player, long age) {
        long next = Math.max(0L, Math.min(MAX, age));
        long previous = ageOf(player);
        if (next == previous) {
            return next;
        }
        AGES.put(player.getUUID(), next);
        ledgerDirty = true;
        int before = stageOf(previous);
        int after = stageOf(next);
        if (after != before) {
            announce(player, after);
            send(player);
            // the body answers the same way the voice does: a stage crossing is
            // felt, not only read
            level_sound(player, after);
        }
        return next;
    }

    private static void level_sound(ServerPlayer player, int stage) {
        try {
            if (stage <= 0) {
                return;
            }
            player.level().playSound((net.minecraft.world.entity.Entity) null,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    stage >= 4 ? McsmSounds.MASSG_GIGGLE : McsmSounds.MASSG_HEART,
                    SoundSource.PLAYERS, 0.5F + 0.08F * stage, 1.05F - 0.07F * stage);
        } catch (Throwable ignored) {
            // a stage without its sound is still a stage
        }
    }

    /**
     * What ageing looks and sounds like, by stage. Nothing here touches health:
     * the void does not hurt you, it keeps you.
     */
    private static void breathe(ServerLevel level, ServerPlayer player, long now, long age) {
        int stage = stageOf(age);
        if (stage <= 0) {
            return;
        }
        Long last = LAST.get(player.getUUID());
        long every = Math.max(80L, 220L - 30L * stage);
        if (last != null && now - last < every) {
            return;
        }
        LAST.put(player.getUUID(), now);
        String[] lines = LORE[stage];
        if (lines.length > 0) {
            int index = (int) ((age / 300L) % (long) lines.length);
            player.sendSystemMessage(Component.literal(lines[index])
                    .withStyle(COLOURS[stage]), false);
        }
        double x = player.getX();
        double y = player.getY() + 1.0D;
        double z = player.getZ();
        level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 2 + stage,
                0.5D, 1.0D, 0.5D, 0.01D);
        if (stage >= 2) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 1.0D, z, stage,
                    0.6D, 0.9D, 0.6D, 0.02D);
        }
        SoundEvent sound = stage >= 4 ? McsmSounds.MASSG_GIGGLE
                : (stage >= 3 ? McsmSounds.MASSG_HEART : McsmSounds.MASSG_WHISPER);
        level.playSound((net.minecraft.world.entity.Entity) null, x, y, z, sound,
                SoundSource.PLAYERS, 0.35F + 0.1F * stage, 1.0F - 0.06F * stage);
        if (stage >= 2) {
            // short, so it reads as the place answering rather than as a debuff
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40 + stage * 20,
                    0, false, false));
        }
        if (stage >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30 + stage * 10,
                    0, false, false));
        }
    }

    private static void announce(ServerPlayer player, int stage) {
        if (stage <= 0) {
            player.sendSystemMessage(Component.literal("\u00a77\u00b7 the void lets go of you")
                    .withStyle(ChatFormatting.GRAY), false);
            return;
        }
        player.sendSystemMessage(Component.literal("\u00a78\u00b7 you are "
                + stageName(stage).toUpperCase(java.util.Locale.ROOT) + " now")
                .withStyle(COLOURS[stage]), false);
    }

    // -------------------------------------------------------------------------
    // Telling the client, so the body changes too
    // -------------------------------------------------------------------------

    /** Pushes this player's ageing to their own client. Harmless if it never lands. */
    public static void send(ServerPlayer player) {
        try {
            ServerPlayNetworking.send(player, new McsmVoidAgingPayload(player.getId(),
                    fractionOf(player)));
        } catch (Throwable ignored) {
            // a client that does not know this payload keeps its own skin
        }
    }

    // -------------------------------------------------------------------------
    // The ledger: ageing survives a logout
    // -------------------------------------------------------------------------

    private static void loadLedger() {
        AGES.clear();
        String raw = McsmExtrasConfig.voidAgingLedger;
        if (raw == null || raw.isEmpty()) {
            return;
        }
        for (String part : raw.split(";")) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            try {
                UUID id = UUID.fromString(part.substring(0, eq).trim());
                long ticks = Long.parseLong(part.substring(eq + 1).trim());
                if (ticks > 0L) {
                    AGES.put(id, Math.min(MAX, ticks));
                }
            } catch (Throwable ignored) {
                // one unreadable row is one player who starts clean
            }
        }
    }

    private static void flush() {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<UUID, Long> entry : AGES.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0L) {
                continue;
            }
            if (out.length() > 0) {
                out.append(';');
            }
            out.append(entry.getKey()).append('=').append(entry.getValue());
        }
        try {
            McsmExtrasConfig.voidAgingLedger = out.toString();
            McsmExtrasConfig.save();
            ledgerDirty = false;
        } catch (Throwable t) {
            System.err.println("[ds] the void ageing ledger could not be written: " + t);
        }
    }

    /** The Level a player is in, as a ServerLevel, or null. */
    static ServerLevel levelOf(ServerPlayer player) {
        Level level = player.level();
        return level instanceof ServerLevel server ? server : null;
    }
}
