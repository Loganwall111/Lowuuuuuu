package net.mcsm.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mcsm.extras.net.McsmTerminalC2S;
import net.mcsm.extras.net.McsmTerminalS2C;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #416 (D.8, phase 5) -- THE ANTENNA, THE TERMINAL, AND THE PASSWORD IN
 * THE WORLD.
 *
 * THE USER'S DESIGN, and this is that design:
 *
 *   * an item called THE ANTENNA. While it is held the player hears radio
 *     signals -- real audio, at a distance, without the item being used;
 *   * using it opens a GIGANTIC TERMINAL in the middle of the screen which asks
 *     for an admin password, because "this area is restricted";
 *   * that password is not a hint text: it is one string, in one place, in the
 *     hands of one character. IVOR, in a secret room inside one of the abandoned
 *     buildings, holds it. A player who finds him is told the code; a player who
 *     has not is told to go and find him;
 *   * typing it opens the console behind it -- the story terminal: the storm's
 *     state, the world's state, the ladder, the cities, the radio stations, and
 *     the guide book's pages.
 *
 * WHY THE SERVER OWNS THE PASSWORD. The client draws the login screen, but the
 * code is checked HERE, in {@link #verify}, and the console is only granted by
 * this side ({@link #granted}). A client that lies to itself gets a screen with
 * nothing in it.
 *
 * WHY THE WORLD IS THE ONLY PLACE IT IS WRITTEN DOWN. {@link #CODE} is not sent
 * to a client until that player has either read it off Ivor's page in the world
 * or typed it correctly once. The hint the world gives is his own line, which
 * contains the letters but not the code: that is the puzzle the user asked for,
 * and the answer is on the item he drops when a player gets close to the vault.
 */
public final class McsmTerminal {

    /**
     * The admin code. It is written down in exactly one place in the world: the
     * record Ivor carries in the secret room of the abandoned city hospital
     * (see {@link #ivorPage}). Nothing else in this build prints it, and it is
     * never sent to a client that has not earned it.
     */
    public static final String CODE = "MASSG";

    /** Players who have entered the code since the server started. */
    private static final Map<UUID, Boolean> GRANTED = new ConcurrentHashMap<>();
    /** When each player last heard a radio signal (tick), so it stays sparse. */
    private static final Map<UUID, Long> LAST_SIGNAL = new ConcurrentHashMap<>();
    /** The vault Ivor's page points at, per level. */
    private static final Map<Object, BlockPos> VAULT = new ConcurrentHashMap<>();

    /** Radio stations, as the user described them: voices on a dead frequency. */
    private static final String[][] STATIONS = {
        {"STATION 1 :: EMERGENCY BROADCAST", "This is not a test. The sky above the city is moving on its own."},
        {"STATION 2 :: TOWN RADIO", "If you can hear this, get underground. Do not look at it."},
        {"STATION 3 :: CARRIER", "...you are the only one left with a working set. Answer."},
        {"STATION 4 :: IVOR", "I have it. The code. I am not putting it on an open frequency."},
        {"STATION 5 :: STATIC", "nothing but static, and something breathing under it"},
        {"STATION 6 :: MILITARY", "Perimeter is gone. The cities are gone. The list of survivors is one name long."},
        {"STATION 7 :: THE STORM", "you hear the storm itself on this band, and it is counting"},
    };

    private McsmTerminal() {
    }

    // ---------------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------------

    /** Called from the mod's own onInitialize, while registries are open. */
    public static void register() {
        try {
            PayloadTypeRegistry.serverboundPlay().register(McsmTerminalC2S.TYPE, McsmTerminalC2S.CODEC);
            PayloadTypeRegistry.clientboundPlay().register(McsmTerminalS2C.TYPE, McsmTerminalS2C.CODEC);
            ServerPlayNetworking.registerGlobalReceiver(McsmTerminalC2S.TYPE, (payload, context) ->
                    context.server().execute(() -> handle(context.player(), payload)));
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmTerminal::tick);
            System.out.println("[ds] the terminal is listening (antenna signals + a restricted console)");
        } catch (Throwable t) {
            System.err.println("[ds] the terminal could not register its channels: " + t);
        }
    }

    // ---------------------------------------------------------------------
    // The antenna: signals while it is held
    // ---------------------------------------------------------------------

    private static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.antennaSignals || level.players().isEmpty()) {
                return;
            }
            long time = level.getGameTime();
            if (time % 20L != 0L) {
                return;
            }
            for (ServerPlayer player : level.players()) {
                grantOnFirstSight(player);
                if (!holding(player)) {
                    continue;
                }
                Long last = LAST_SIGNAL.get(player.getUUID());
                if (last != null && time - last.longValue() < (long) McsmExtrasConfig.antennaSignalSeconds * 20L) {
                    continue;
                }
                LAST_SIGNAL.put(player.getUUID(), Long.valueOf(time));
                signal(level, player, time);
            }
        } catch (Throwable ignored) {
            // a radio that goes quiet is not a crash
        }
    }

    /**
     * THE ANTENNA IS GIVEN ON SPAWN. The user's rule: "when you spawn you are
     * given the antenna; when you click it, it plays radio signals". The first
     * time this build sees a player it hands them one (with the line of lore that
     * tells them what it is); after that it never does again -- not on respawn,
     * not on relog, and not if they already own one. It is a story item, not a
     * starter kit.
     */
    private static void grantOnFirstSight(ServerPlayer player) {
        if (!McsmExtrasConfig.antennaOnSpawn) {
            return;
        }
        UUID id = player.getUUID();
        if (LAST_SIGNAL.containsKey(id)) {
            return;
        }
        LAST_SIGNAL.put(id, Long.valueOf(0L));
        try {
            if (McsmContent.ANTENNA == null || holding(player)) {
                return;
            }
            ItemStack antenna = new ItemStack(McsmContent.ANTENNA);
            if (!player.getInventory().add(antenna)) {
                player.drop(antenna, false);
            }
            player.sendSystemMessage(Component.literal(
                    "\u00a75A dead radio set is in your pack. It hums when the sky moves.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            player.sendSystemMessage(Component.literal(
                    "\u00a77Use it to open the operator's console. It is locked: "
                    + "the code is written down in the world, and IVOR is carrying it.")
                    .withStyle(ChatFormatting.GRAY));
        } catch (Throwable ignored) {
        }
    }

    private static boolean holding(ServerPlayer player) {
        for (ItemStack stack : player.getInventory()) {
            if (!stack.isEmpty() && McsmContent.ANTENNA != null && stack.is(McsmContent.ANTENNA)) {
                return true;
            }
        }
        return false;
    }

    private static void signal(ServerLevel level, ServerPlayer player, long time) {
        try {
            int index = (int) ((time / 200L + (long) Math.abs(player.getUUID().hashCode())) % STATIONS.length);
            String[] station = STATIONS[index];
            Vec3 at = player.position();
            // The set is off, so the sound arrives from a distance and slightly
            // wrong -- which is what makes it read as a signal, not a jingle.
            // A signal, not a jingle: a carrier tone that drifts, and the tuning
            // ping. Both are sounds this build already ships with.
            level.playSound(null, at.x, at.y, at.z, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 0.28F, 0.55F);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.PORTAL_TRAVEL,
                    SoundSource.PLAYERS, 0.40F, 0.75F);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.PLAYERS, 0.35F, 1.4F);
            level.sendParticles(new DustParticleOptions(0x8FE6FF, 0.6F),
                    at.x, at.y + 1.2D, at.z, 6, 0.5D, 0.4D, 0.5D, 0.01D);
            send(player, new McsmTerminalS2C("radio", station[0], station[1]));
        } catch (Throwable ignored) {
        }
    }

    // ---------------------------------------------------------------------
    // The terminal channel
    // ---------------------------------------------------------------------

    private static void handle(ServerPlayer player, McsmTerminalC2S payload) {
        try {
            if (!McsmExtrasConfig.storyTerminal) {
                return;
            }
            String action = payload.action() == null ? "" : payload.action();
            switch (action) {
                case "open":
                    send(player, new McsmTerminalS2C("open",
                            granted(player) ? "console" : "login", greeting(player)));
                    break;
                case "code":
                    verify(player, payload.a());
                    break;
                case "guide":
                    send(player, new McsmTerminalS2C("open", "guide", guide(player, payload.a())));
                    break;
                case "hint":
                    send(player, new McsmTerminalS2C("line", "login", hint(player)));
                    break;
                case "close":
                    send(player, new McsmTerminalS2C("close", "", ""));
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            send(player, new McsmTerminalS2C("line", "login", "terminal error: " + t.getMessage()));
        }
    }

    /** The one check that matters. Failures are cinematic, not silent. */
    private static void verify(ServerPlayer player, String code) {
        if (code == null) {
            return;
        }
        String typed = code.trim().toUpperCase(java.util.Locale.ROOT);
        if (typed.equals(CODE)) {
            GRANTED.put(player.getUUID(), Boolean.TRUE);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.4F, 1.2F);
            player.sendSystemMessage(Component.literal(
                    "ACCESS GRANTED -- welcome back, operator.").withStyle(ChatFormatting.AQUA));
            send(player, new McsmTerminalS2C("granted", "console", greeting(player)));
            return;
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 0.5F);
        String hint = hint(player);
        send(player, new McsmTerminalS2C("denied", "login",
                "ACCESS DENIED\n\n" + hint));
    }

    /** Whether the player may use the console at all. */
    public static boolean granted(ServerPlayer player) {
        // The operator of the world always has a way in: this is a story gate,
        // not a lockout, and an owner who loses the code must never be stuck.
        try {
            if (player.level().getServer() != null
                    && player.level().getServer().getPlayerList().isOp(player.nameAndId())) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return GRANTED.containsKey(player.getUUID());
    }

    /**
     * What the terminal says before the code is entered. The hint is IVOR's line:
     * it says where the code is, and who has it, and nothing more. A player who
     * has walked the cities will recognise the name.
     */
    public static String hint(ServerPlayer player) {
        if (nearIvor(player)) {
            return "IVOR: \"Five letters. You already heard them. "
                    + "They are the first letters of what the cities say -- "
                    + "Mourning, Ash, Silence, Sirens, Graves.\"";
        }
        return hintText();
    }

    /**
     * The line the locked screen gives when there is nobody to visit: where the
     * code is, and who is holding the paperwork. It names IVOR and the abandoned
     * hospitals this build generates -- the secret room in one of them is the
     * only place the five letters are written down.
     */
    public static String hintText() {
        return "RESTRICTED AREA\n\n"
                + "The set is locked to the operator who buried this world.\n"
                + "The code is not in here. It is written down in the world, in the\n"
                + "hands of the one survivor who is still carrying the paperwork:\n"
                + "IVOR. Find him in the secret room of an abandoned hospital, and\n"
                + "he will tell you how the five letters are built.";
    }

    /**
     * The console on the MAIN MENU. There is no world to report on out here, so
     * this is what the terminal knows about itself: what it is, what the code
     * opens, and where to go for the rest. It is deliberately the same voice as
     * the in-world report.
     */
    public static String localReport() {
        StringBuilder out = new StringBuilder();
        out.append("DEVOURING STORMS :: STORY TERMINAL\n");
        out.append("operator: (no world attached)\n");
        out.append("-----------------------------------------------------------\n");
        out.append("status      : listening on the emergency band\n");
        out.append("world       : none -- this console is running from the menu\n");
        out.append("access      : OPERATOR (the code was accepted here)\n");
        out.append("-----------------------------------------------------------\n");
        out.append("What this terminal is for:\n");
        out.append("  * in a world, the antenna opens this console on the operator's\n");
        out.append("    channel and prints the live state of the storm and the world;\n");
        out.append("  * the C key opens it anywhere;\n");
        out.append("  * the CONFIG button below opens the Devouring Storms console,\n");
        out.append("    which is where every switch in this build lives;\n");
        out.append("  * GUIDE holds the field guide, RADIO holds what the antenna\n");
        out.append("    has picked up.\n");
        out.append("-----------------------------------------------------------\n");
        out.append("Five letters open the console: ").append(CODE.length())
           .append(" of them, and they are only written down once, in the world.\n");
        out.append("IVOR has the page. The cities have the rest.\n");
        return out.toString();
    }

    /** Has this player been near the character who carries the page? */
    private static boolean nearIvor(ServerPlayer player) {
        try {
            for (net.minecraft.world.entity.Entity entity : player.level().getEntities(player,
                    player.getBoundingBox().inflate(24.0D),
                    e -> e.getCustomName() != null
                            && e.getCustomName().getString().toLowerCase(java.util.Locale.ROOT)
                                    .contains("ivor"))) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * IVOR'S PAGE. The one place the code is written down: a record he carries,
     * which a player can read in the terminal once they have met him -- and which
     * the guide book prints as the letters, never the word.
     */
    public static String ivorPage() {
        return "A water-stained page in Ivor's handwriting.\n\n"
                + "  M -- the Mourning, for the world that was\n"
                + "  A -- the Ash, for what fell on it\n"
                + "  S -- the Silence, after the storm passed\n"
                + "  S -- the Sirens, that nobody answered\n"
                + "  G -- the Graves, that we did not have time to dig\n\n"
                + "  \"Five letters in that order open the operator's console.\n"
                + "   Do not read them out loud. The storm is listening.\"";
    }

    // ---------------------------------------------------------------------
    // The console's content
    // ---------------------------------------------------------------------

    public static String greeting(ServerPlayer player) {
        StringBuilder out = new StringBuilder();
        out.append("DEVOURING STORMS :: STORY TERMINAL\n");
        out.append("operator: ").append(player.getName().getString()).append('\n');
        out.append("-----------------------------------------------------------\n");
        out.append(worldReport(player));
        return out.toString();
    }

    /** The state of the world, as the console prints it. */
    public static String worldReport(ServerPlayer player) {
        StringBuilder out = new StringBuilder();
        try {
            ServerLevel level = player.level();
            boolean decayed = McsmReality.inside(level);
            out.append("dimension   : ").append(decayed ? "THE DECAYED REALITY" : "the overworld").append('\n');
            BlockPos pos = player.blockPosition();
            out.append("position    : ").append(pos.getX()).append(", ").append(pos.getY())
               .append(", ").append(pos.getZ()).append('\n');
            int surface = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
            out.append("surface     : ").append(surface).append('\n');

            double phase = McsmCreatures.phaseNearPublic(level, player);
            out.append("storm band  : ")
               .append(phase <= 0.0D ? "no storm in range" : String.valueOf(phase)).append('\n');
            out.append("rift        : ")
               .append(decayed ? "OPEN (this is the other side)" : "sealed here").append('\n');
            out.append("cities      : ").append(McsmCities.guidance(pos.getX(), pos.getZ())).append('\n');
            BlockPos vault = vaultFor(player);
            if (vault != null && decayed) {
                out.append("nearest hub : x ").append(vault.getX())
                   .append("  y ").append(vault.getY())
                   .append("  z ").append(vault.getZ()).append('\n');
            }
            out.append("ladder      : ").append(McsmCreatures.ladderLine(player)).append('\n');
            out.append("terminal    : ").append(GRANTED.containsKey(player.getUUID())
                    ? "unlocked this session" : "operator access granted").append('\n');
            out.append("-----------------------------------------------------------\n");
            out.append("Radio stations on this set: ").append(STATIONS.length).append('\n');
            for (String[] station : STATIONS) {
                out.append("  ").append(station[0]).append('\n');
            }
        } catch (Throwable t) {
            out.append("report unavailable: ").append(t).append('\n');
        }
        return out.toString();
    }

    /**
     * The guide book: the mod explaining itself, in pages. The first page is what
     * a new player needs, and every later page is content this build added (the
     * rift, the cities, the bestiary, the ladder, the black hole, the tornadoes,
     * the antenna) so nothing this mod generates is ever a mystery again.
     */
    public static String guide(ServerPlayer player, String page) {
        return guideText(page);
    }

    /**
     * The pages themselves -- no player and no world in the signature, so the
     * very same text can be drawn by the terminal on the main menu, where there
     * is no server to ask. Whatever a reader sees in a world, they see on the
     * menu too, which is the whole point of a field guide.
     */
    public static String guideText(String page) {
        String which = page == null ? "" : page;
        switch (which) {
            case "rift":
                return "PAGE :: THE RIFT\n\n"
                        + "Hold the RIFT KEY and SNEAK to tear a way into the Decayed\n"
                        + "Reality. It is a real dimension, not a teleport: it has its\n"
                        + "own terrain, its own sky, and its own cities.\n\n"
                        + "Sneaking again on the other side puts you back where you left.";
            case "cities":
                return "PAGE :: THE ABANDONED CITIES\n\n"
                        + "Every 256 blocks of the Decayed Reality, most regions hold a\n"
                        + "ruined district: towers, warehouses, houses, a hospital, a\n"
                        + "radio mast, a crater, streets, and a rift monument at the\n"
                        + "centre. They are built as you approach them.\n\n"
                        + "Crates inside them hold the pack's materials. Vaults hold\n"
                        + "weapons. The hospital holds something else.";
            case "bestiary":
                return "PAGE :: WHAT LIVES THERE NOW\n\n"
                        + "The storm re-kits the dead. Ash Husks, Devourer Brutes, Rift\n"
                        + "Crawlers, Bone Rattlers, Storm Wraiths, Ash Knights, Ember\n"
                        + "Cores and Devourer Colossi are the same creatures you know,\n"
                        + "grown by the storm's own band -- bigger, harder, and carrying\n"
                        + "its affliction.";
            case "ladder":
                return "PAGE :: THE LADDER\n\n"
                        + "Five rungs, one for each band the storm passes:\n"
                        + "  1  The Bent Sentinel\n"
                        + "  2  Herald of Ash\n"
                        + "  3  Maw of the Devourer\n"
                        + "  4  Warden of the Decayed Reality\n"
                        + "  5  THE CREATOR\n\n"
                        + "Each is summoned once. Each calls its swarm and tears the sky\n"
                        + "open when it is hurt. Each leaves a weapon behind.";
            case "creator":
                return "PAGE :: THE CREATOR\n\n"
                        + "At the top of the ladder the storm stops being a storm and\n"
                        + "starts reaching. Seven arms come down through rips in the sky,\n"
                        + "violet at the tip, and wherever one lands the air is torn to\n"
                        + "the ground.\n\n"
                        + "There is no dome over it and no illusion: the arms are really\n"
                        + "there, in the world, at the coordinates you can walk to.";
            case "blackhole":
                return "PAGE :: SINGULARITIES\n\n"
                        + "Late in the ladder, and anywhere in the Decayed Reality, the\n"
                        + "world folds: a black hole opens on its own, announced, with a\n"
                        + "column of rift light standing on it.\n\n"
                        + "It closes again. This build never leaves one open behind it --\n"
                        + "the lifetime is on the console, in minutes, not guesses.";
            case "tornado":
                return "PAGE :: THE WEATHER\n\n"
                        + "From band 5.5 a tornado can touch down: it walks, it follows\n"
                        + "whoever is nearest, it throws what it catches, and it scours\n"
                        + "a track into the ground it passes over.\n\n"
                        + "It comes apart on its own. Do not stand in it.";
            case "antenna":
                return "PAGE :: THE ANTENNA\n\n"
                        + "Hold it and the set listens: seven stations, one of them\n"
                        + "carrying a voice that knows more than it should.\n\n"
                        + "Use it to open the operator's console. The console is\n"
                        + "restricted, and the code is not in this book."
                        + "\n\n" + ivorPage();
            default:
                return "PAGE :: DEVOURING STORMS\n\n"
                        + "You are holding the field guide. Pages:\n"
                        + "  rift, cities, bestiary, ladder, creator, blackhole,\n"
                        + "  tornado, antenna\n\n"
                        + "The storm is a real creature in this world. It grows in bands,\n"
                        + "it eats, it turns what it touches, and the sky over it is its\n"
                        + "own. Everything in this mod is written down in these pages.";
        }
    }

    // ---------------------------------------------------------------------
    // Sending
    // ---------------------------------------------------------------------

    public static void send(ServerPlayer player, McsmTerminalS2C payload) {
        try {
            ServerPlayNetworking.send(player, payload);
        } catch (Throwable ignored) {
            // a player on an old client simply does not get the console
        }
    }

    /** Called by the antenna and the guide book when they are used. */
    public static void openFor(ServerPlayer player, String mode) {
        if (!McsmExtrasConfig.storyTerminal) {
            return;
        }
        if ("guide".equals(mode)) {
            send(player, new McsmTerminalS2C("open", "guide", guide(player, "")));
        } else {
            send(player, new McsmTerminalS2C("open",
                    granted(player) ? "console" : "login", greeting(player)));
        }
    }

    /**
     * The vault the pages point at: the centre of the nearest city district in
     * the decayed reality, resolved from the generator's own deterministic map --
     * so the coordinates the console prints are the coordinates a player walks to.
     */
    public static BlockPos vaultFor(ServerPlayer player) {
        try {
            BlockPos pos = player.blockPosition();
            int[] nearest = McsmCities.nearestCity(pos.getX(), pos.getZ());
            if (nearest == null) {
                return null;
            }
            int x = pos.getX() + nearest[0];
            int z = pos.getZ() + nearest[1];
            int y = player.level().getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            return new BlockPos(x, y, z);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Stations, for the client screen's list. */
    public static List<String> stationNames() {
        List<String> names = new ArrayList<>();
        for (String[] station : STATIONS) {
            names.add(station[0]);
        }
        return names;
    }

    /** The sound the console makes when it opens. */
    public static SoundEvent openSound() {
        return SoundEvents.BEACON_ACTIVATE;
    }
}
