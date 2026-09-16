package net.dabicco.witherstormmod.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.dabicco.witherstormmod.structures.McsmCommand;
import net.dabicco.witherstormmod.structures.McsmSchematic;
import net.dabicco.witherstormmod.structures.McsmWorldgen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmTemplateSummoner;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Devouring Storms 1.9.114 -- the town queue, made findable.
 *
 * Deep-scan of the base jar (ci/api/scan/) settled the "towns never build"
 * report. Nothing in the builder is broken:
 *
 *   - McsmWorldgen holds a static QUEUE; McsmCommand.build() loads each
 *     site's .schematic from the jar assets and enqueues it;
 *   - DabyWitherStormMod registers McsmWorldgen.tick(level) on
 *     ServerTickEvents.END_LEVEL_TICK, and tick() places up to budget
 *     (24000) blocks PER TICK -- a whole town appears in seconds, not
 *     "over the next few minutes" (that message is flavour);
 *   - every one of the 33 referenced schematics is present in the jar;
 *   - sites build at ABSOLUTE coordinates: ANCHOR_X=-640, ANCHOR_Z=256
 *     plus per-site offsets out to ~1400 blocks, y=64..296 (Sky City
 *     floats at y=296).
 *
 * So the queue DID run. The player simply was not standing at (-640, 64,
 * 256)-ish, and the discovery path is broken UX: "/mcsm tp all" is not a
 * thing ("No location called 'all'"), site keys are label-derived slugs
 * (Beacon Town -> beacon_town) that nothing ever shows, and the queued
 * message never prints coordinates.
 *
 * This adds /ds towns as the usable surface over THEIR machinery (no
 * reimplementation, we call their public methods):
 *
 *   /ds towns              list every site with absolute coordinates
 *   /ds towns build [site] queue one site or all of them, printing coords
 *   /ds towns tp <site>    teleport onto the site (tab-completes keys)
 *   /ds towns status       how many jobs are still pending
 *
 * Registered at the TAIL of their own McsmCommand.register(dispatcher), so
 * it lives and dies with their command surface and cannot register before
 * the dispatcher exists.
 */
@Mixin(value = McsmCommand.class, remap = false)
public abstract class McsmTownCommandPatch {

    private static final SuggestionProvider<CommandSourceStack> DS$SITES =
            (ctx, builder) -> {
                for (McsmWorldgen.Site s : McsmWorldgen.layout()) {
                    builder.suggest(McsmCommand.key(s));
                }
                return CompletableFuture.completedFuture(builder.build());
            };

    private static final SuggestionProvider<CommandSourceStack> DS$BLUEPRINTS =
            (ctx, builder) -> {
                for (String key : McsmTemplateSummoner.keys()) {
                    builder.suggest(key);
                }
                return CompletableFuture.completedFuture(builder.build());
            };

    @Inject(method = "register(Lcom/mojang/brigadier/CommandDispatcher;)V", at = @At("TAIL"))
    private static void ds$towns(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
        try {
            LiteralArgumentBuilder<CommandSourceStack> towns = Commands.literal("towns");
            towns.executes(ctx -> ds$list(ctx.getSource()));
            towns.then(Commands.literal("build")
                    .executes(ctx -> ds$build(ctx.getSource(), "all"))
                    .then(Commands.argument("site", StringArgumentType.word())
                            .suggests(DS$SITES)
                            .executes(ctx -> ds$build(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "site")))));
            towns.then(Commands.literal("tp")
                    .then(Commands.argument("site", StringArgumentType.word())
                            .suggests(DS$SITES)
                            .executes(ctx -> ds$tp(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "site")))));
            towns.then(Commands.literal("status")
                    .executes(ctx -> ds$status(ctx.getSource())));
            towns.then(Commands.literal("summon")
                    .executes(ctx -> ds$summon(ctx.getSource(), "world"))
                    .then(Commands.argument("blueprint", StringArgumentType.word())
                            .suggests(DS$BLUEPRINTS)
                            .executes(ctx -> ds$summon(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "blueprint")))));
            towns.then(Commands.literal("start")
                    .executes(ctx -> ds$start(ctx.getSource())));

            LiteralArgumentBuilder<CommandSourceStack> storm = Commands.literal("storm");
            storm.then(Commands.literal("backgrowth")
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                            .executes(ctx -> ds$backGrowth(ctx.getSource(),
                                    BoolArgumentType.getBool(ctx, "enabled")))));
            storm.then(Commands.literal("backgrowth_speed")
                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.01D, 12.0D))
                            .executes(ctx -> ds$backGrowthSpeed(ctx.getSource(),
                                    DoubleArgumentType.getDouble(ctx, "speed")))));
            // BUILD #443 -- rituals and the two dimensions behind them.
            LiteralArgumentBuilder<CommandSourceStack> ritual = Commands.literal("ritual");
            ritual.executes(ctx -> ds$ritualList(ctx.getSource()));
            ritual.then(Commands.literal("list").executes(ctx -> ds$ritualList(ctx.getSource())));
            ritual.then(Commands.literal("check").executes(ctx -> ds$ritualCheck(ctx.getSource())));

            LiteralArgumentBuilder<CommandSourceStack> reality = Commands.literal("reality");
            reality.executes(ctx -> ds$realityStatus(ctx.getSource()));
            reality.then(Commands.literal("status").executes(ctx -> ds$realityStatus(ctx.getSource())));
            reality.then(Commands.literal("adams").executes(ctx -> ds$adams(ctx.getSource())));
            reality.then(Commands.literal("decayed").executes(ctx -> ds$decayed(ctx.getSource())));

            // BUILD #444 -- the underground structures, findable.
            LiteralArgumentBuilder<CommandSourceStack> maze = Commands.literal("maze");
            maze.executes(ctx -> ds$maze(ctx.getSource()));
            maze.then(Commands.literal("where").executes(ctx -> ds$maze(ctx.getSource())));
            maze.then(Commands.literal("build").executes(ctx -> ds$mazeBuild(ctx.getSource())));

            // BUILD #445 -- the future-book, for anyone who wants the countdown in chat
            // rather than on a page.
            LiteralArgumentBuilder<CommandSourceStack> book = Commands.literal("book");
            book.executes(ctx -> ds$book(ctx.getSource()));

            LiteralArgumentBuilder<CommandSourceStack> server = Commands.literal("server");
            server.executes(ctx -> ds$server(ctx.getSource()));
            server.then(Commands.literal("where").executes(ctx -> ds$server(ctx.getSource())));
            server.then(Commands.literal("shell").executes(ctx -> ds$serverShell(ctx.getSource())));

            dispatcher.register(Commands.literal("ds").then(towns).then(storm)
                    .then(ritual).then(reality).then(maze).then(server).then(book));
        } catch (Throwable ignored) {
            // our extension failing must never take down their /mcsm command
        }
    }

    private static int ds$list(CommandSourceStack src) {
        List<McsmWorldgen.Site> sites = McsmWorldgen.layout();
        src.sendSuccess(() -> Component.literal("[ds] " + sites.size()
                + " Story Mode sites. They build at these ABSOLUTE coordinates,"
                + " not near you:"), false);
        for (McsmWorldgen.Site s : sites) {
            src.sendSuccess(() -> Component.literal("  - " + s.label() + " ("
                    + s.x() + ", " + s.y() + ", " + s.z() + ")  ->  /ds towns tp "
                    + McsmCommand.key(s)), false);
        }
        return sites.size();
    }

    private static int ds$build(CommandSourceStack src, String name) {
        boolean all = name.equalsIgnoreCase("all");
        int queued = 0;
        int failed = 0;
        for (McsmWorldgen.Site s : McsmWorldgen.layout()) {
            if (!all && !McsmCommand.key(s).equalsIgnoreCase(name)
                    && !s.label().equalsIgnoreCase(name)) {
                continue;
            }
            try {
                McsmSchematic sch = McsmSchematic.load(
                        src.getServer().getResourceManager(), s.path());
                McsmWorldgen.enqueue(sch, new BlockPos(s.x(), s.y(), s.z()), s.label());
                queued++;
                src.sendSuccess(() -> Component.literal("[ds] queued " + s.label()
                        + " at (" + s.x() + ", " + s.y() + ", " + s.z()
                        + ") -- it builds within seconds; get there with /ds towns tp "
                        + McsmCommand.key(s)), false);
            } catch (Throwable t) {
                failed++;
                src.sendFailure(Component.literal("[ds] " + s.label()
                        + ": could not load schematic (" + t + ")"));
            }
        }
        if (queued == 0 && failed == 0) {
            src.sendFailure(Component.literal("[ds] no site called '" + name
                    + "'. /ds towns lists every site."));
        }
        return queued;
    }

    private static int ds$tp(CommandSourceStack src, String name) {
        ServerPlayer p = src.getPlayer();
        if (p == null) {
            src.sendFailure(Component.literal("/ds towns tp must be run by a player."));
            return 0;
        }
        for (McsmWorldgen.Site s : McsmWorldgen.layout()) {
            if (!McsmCommand.key(s).equalsIgnoreCase(name)
                    && !s.label().equalsIgnoreCase(name)) {
                continue;
            }
            ServerLevel level = src.getLevel();
            p.teleportTo(level, s.x() + 0.5, s.y() + 2.0, s.z() + 0.5,
                    Collections.emptySet(), p.getYRot(), p.getXRot(), false);
            src.sendSuccess(() -> Component.literal("[ds] teleported to " + s.label()
                    + " (" + s.x() + ", " + s.y() + ", " + s.z()
                    + "). If it is empty ground, run /ds towns build "
                    + McsmCommand.key(s) + " and watch it rise."), false);
        return 1;
        }
        src.sendFailure(Component.literal("[ds] no site called '" + name
                + "'. /ds towns lists every site."));
        return 0;
    }

    /**
     * Devouring Storms 1.9.115 -- the story starts where the story starts:
     * the treehouse in the wilderness outside EnderCon (Episode 1 opening).
     * Builds the three-site opening cluster and stands the player at the
     * treehouse. Site labels come from the recovered 1.9.100 source
     * (src-recon/.../McsmWorldgen.java layout()).
     */
    private static final String[] DS$EPISODE_ONE = {
            "Wilderness Treehouse", "The Wilderness", "EnderCon Town Fair",
    };

    private static int ds$start(CommandSourceStack src) {
        ServerPlayer p = src.getPlayer();
        McsmWorldgen.Site treehouse = null;
        int queued = 0;
        for (String want : DS$EPISODE_ONE) {
            for (McsmWorldgen.Site s : McsmWorldgen.layout()) {
                if (!s.label().equalsIgnoreCase(want)) {
                    continue;
                }
                try {
                    McsmSchematic sch = McsmSchematic.load(
                            src.getServer().getResourceManager(), s.path());
                    McsmWorldgen.enqueue(sch, new BlockPos(s.x(), s.y(), s.z()), s.label());
                    queued++;
                    src.sendSuccess(() -> Component.literal("[ds] building " + s.label()
                            + " at (" + s.x() + ", " + s.y() + ", " + s.z() + ")"), false);
                } catch (Throwable t) {
                    src.sendFailure(Component.literal("[ds] " + s.label()
                            + ": could not load schematic (" + t + ")"));
                }
                if (s.label().equalsIgnoreCase("Wilderness Treehouse")) {
                    treehouse = s;
                }
                break;
            }
        }
        if (treehouse == null) {
            src.sendFailure(Component.literal("[ds] the Wilderness Treehouse site is missing"
                    + " from this jar's layout -- /ds towns still lists what exists."));
            return 0;
        }
        final int n = queued;   // loop counter is not effectively final
        if (p != null) {
            McsmWorldgen.Site t = treehouse;
            p.teleportTo(src.getLevel(), t.x() + 0.5, t.y() + 2.0, t.z() + 0.5,
                    Collections.emptySet(), p.getYRot(), p.getXRot(), false);
            src.sendSuccess(() -> Component.literal("[ds] the story starts here: the treehouse"
                    + " in the wilderness outside EnderCon (" + t.x() + ", " + t.y() + ", " + t.z()
                    + "). " + n + " site(s) queued -- they rise within seconds."), false);
        } else {
            McsmWorldgen.Site t2 = treehouse;   // effectively final for the lambda
            src.sendSuccess(() -> Component.literal("[ds] " + n
                    + " Episode 1 site(s) queued around the Wilderness Treehouse ("
                    + t2.x() + ", " + t2.y() + ", " + t2.z() + ")."), false);
        }
        return queued;
    }

    private static int ds$backGrowth(CommandSourceStack src, boolean enabled) {
        McsmExtrasConfig.load();
        McsmExtrasConfig.infiniteBackGrowth = enabled;
        McsmExtrasConfig.save();
        src.sendSuccess(() -> Component.literal("[ds] infinite visual back growth is now "
                + (enabled ? "ON" : "OFF") + ". It is intentionally off by default."), false);
        return enabled ? 1 : 0;
    }

    private static int ds$backGrowthSpeed(CommandSourceStack src, double speed) {
        McsmExtrasConfig.load();
        McsmExtrasConfig.infiniteBackGrowthSpeed = speed;
        McsmExtrasConfig.save();
        src.sendSuccess(() -> Component.literal("[ds] infinite visual back growth speed set to " + speed
                + ". Use /ds storm backgrowth true to enable it."), false);
        return (int)Math.round(speed * 100.0D);
    }

    private static int ds$summon(CommandSourceStack src, String key) {
        ServerPlayer p = src.getPlayer();
        if (p == null) {
            src.sendFailure(Component.literal("/ds towns summon must be run by a player."));
            return 0;
        }
        int placed = McsmTemplateSummoner.summon(p, key);
        if (placed <= 0) {
            src.sendFailure(Component.literal("[ds] no converted NBT blueprint loaded for '" + key
                    + "'. Run ci/convert_story_worlds.py after adding world_data_temp/MC105 and MC201."));
            return 0;
        }
        src.sendSuccess(() -> Component.literal("[ds] summoned " + placed
                + " converted Story Mode blueprint(s) at your spawn/current position."), false);
        return placed;
    }

    private static int ds$status(CommandSourceStack src) {
        int pending = McsmWorldgen.pending();
        src.sendSuccess(() -> Component.literal("[ds] town queue: " + pending
                + " job(s) pending. The queue drains safely every server tick at up to"
                + " 4096 blocks/tick; use /ds towns tp <site> to watch fixed-coordinate builds."), false);
        return pending;
    }


    // ---------------------------------------------------------------------
    // BUILD #443 -- rituals, and the dimensions the rites open
    // ---------------------------------------------------------------------

    private static int ds$ritualList(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("[ds] the ritual catalogue -- build the ring, "
                + "stand inside it holding the offering:"), false);
        for (String line : net.mcsm.extras.McsmRituals.catalogue()) {
            src.sendSuccess(() -> Component.literal("  - " + line), false);
        }
        src.sendSuccess(() -> Component.literal("  (the ring may have gaps: 10 of 12 sample "
                + "points is enough. /ds ritual check reads your own ring.)"), false);
        return net.mcsm.extras.McsmRituals.RITUALS.size();
    }

    private static int ds$ritualCheck(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            String line = net.mcsm.extras.McsmRituals.nearest(src.getLevel(), player);
            src.sendSuccess(() -> Component.literal("[ds] " + line), false);
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds ritual check has to be run by a "
                    + "player standing at a ring"), false);
        }
        return 1;
    }

    private static int ds$realityStatus(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("[ds] the decayed reality: "
                + (net.mcsm.extras.McsmExtrasConfig.decayedReality ? "ON" : "OFF")
                + " \u00b7 the infinite dimension of adams: "
                + (net.mcsm.extras.McsmExtrasConfig.adamsReality ? "ON" : "OFF")
                + " \u00b7 rituals: "
                + (net.mcsm.extras.McsmExtrasConfig.rituals ? "ON" : "OFF")), false);
        src.sendSuccess(() -> Component.literal("[ds] adams has written "
                + net.mcsm.extras.McsmAdams.builtRegions() + " regions, "
                + net.mcsm.extras.McsmAdams.pendingRegions() + " still queued \u00b7 "
                + "/ds reality adams goes in, /ds reality decayed goes through the rift"), false);
        return 1;
    }

    private static int ds$adams(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            if (net.mcsm.extras.McsmAdams.enter(player)) {
                src.sendSuccess(() -> Component.literal("[ds] the gate opens"), false);
                return 1;
            }
            src.sendSuccess(() -> Component.literal("[ds] the gate refused (the dimension is "
                    + "switched off or not loaded)"), false);
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds reality adams has to be run by a "
                    + "player (the gate needs someone to walk through it)"), false);
        }
        return 0;
    }

    private static int ds$decayed(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            return net.mcsm.extras.McsmReality.enter(player) ? 1 : 0;
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds reality decayed has to be run by a "
                    + "player"), false);
            return 0;
        }
    }


    // ---------------------------------------------------------------------
    // BUILD #444 -- the underground structures
    // ---------------------------------------------------------------------

    private static int ds$maze(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            src.sendSuccess(() -> Component.literal("[ds] "
                    + net.mcsm.extras.McsmMazes.guidance((int) player.getX(), (int) player.getZ())), false);
            for (String line : net.mcsm.extras.McsmMazes.report()) {
                src.sendSuccess(() -> Component.literal("[ds] " + line), false);
            }
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds maze where has to be run by a player"), false);
        }
        return 1;
    }

    private static int ds$mazeBuild(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            int[] near = net.mcsm.extras.McsmMazes.nearestMaze((int) player.getX(), (int) player.getZ());
            if (near == null) {
                src.sendSuccess(() -> Component.literal("[ds] no maze region within reach"), false);
                return 0;
            }
            player.teleportTo(src.getLevel(), near[0] + 0.5D,
                    net.mcsm.extras.McsmMazes.HATCH_Y + 2.0D, near[1] + 0.5D,
                    java.util.Set.of(), player.getYRot(), 0.0F, false);
            src.sendSuccess(() -> Component.literal("[ds] standing on the hatch at "
                    + near[0] + ", " + near[1] + " -- the warehouse is at y="
                    + net.mcsm.extras.McsmMazes.surfaceY() + " (walk in, it builds as you come)"), false);
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds maze build has to be run by a player"), false);
        }
        return 1;
    }

    private static int ds$server(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            src.sendSuccess(() -> Component.literal("[ds] "
                    + net.mcsm.extras.McsmServerRooms.guidance((int) player.getX(), (int) player.getZ())), false);
            src.sendSuccess(() -> Component.literal("[ds] this level is running "
                    + net.mcsm.extras.McsmServerRooms.lamps(src.getLevel()) + " rack lights, "
                    + net.mcsm.extras.McsmServerRooms.built() + " rooms built this session"), false);
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds server where has to be run by a player"), false);
        }
        return 1;
    }

    private static int ds$serverShell(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            int[] near = net.mcsm.extras.McsmServerRooms.nearestRoom((int) player.getX(),
                    (int) player.getZ());
            if (near == null) {
                src.sendSuccess(() -> Component.literal("[ds] no server room within reach"), false);
                return 0;
            }
            double y = src.getLevel().getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    near[0], near[1]) + 2.0D;
            player.teleportTo(src.getLevel(), near[0] + 0.5D, y, near[1] + 0.5D,
                    java.util.Set.of(), player.getYRot(), 0.0F, false);
            src.sendSuccess(() -> Component.literal("[ds] standing on the server room hatch at "
                    + near[0] + ", " + near[1] + " -- the racks are at y="
                    + net.mcsm.extras.McsmServerRooms.FLOOR_Y), false);
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds server shell has to be run by a player"), false);
        }
        return 1;
    }

    private static int ds$book(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("[ds] the future-book is open at D-"
                + net.mcsm.extras.McsmFutureBook.daysLeft() + ", counting to "
                + net.mcsm.extras.McsmFutureBook.endDate()), false);
        src.sendSuccess(() -> Component.literal("[ds] " + net.mcsm.extras.McsmFutureBook.forecast()), false);
        src.sendSuccess(() -> Component.literal(
                "[ds] page " + (net.mcsm.extras.McsmFutureBook.currentPage(
                        net.mcsm.extras.McsmFutureBook.daysLeft()) + 1) + " of "
                        + net.mcsm.extras.McsmFutureBook.count()
                        + " -- press B in game to read it, the last page is blank"), false);
        return 1;
    }
}
