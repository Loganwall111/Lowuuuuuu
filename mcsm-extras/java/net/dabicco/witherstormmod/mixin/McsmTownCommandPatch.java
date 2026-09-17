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
            reality.then(Commands.literal("void").executes(ctx -> ds$void(ctx.getSource())));
            reality.then(Commands.literal("decayed").executes(ctx -> ds$decayed(ctx.getSource())));
            // BUILD #462 -- and the reach: the fourth dimension, by name.
            reality.then(Commands.literal("creator").executes(ctx -> ds$creator(ctx.getSource())));

            // BUILD #464 -- what the MENU is wearing. The report was "the main
            // menu is black"; this says out loud which backdrop, which lift and
            // which boot sequence are in force, and where the file that sets them
            // lives, so a black screen can be answered instead of guessed at.
            LiteralArgumentBuilder<CommandSourceStack> menu = Commands.literal("menu");
            menu.executes(ctx -> ds$menu(ctx.getSource()));
            // BUILD #469 -- and what the RENDER is doing: the guard that keeps a fault
            // in the mod's own menu chrome from leaving an empty (black) frame. The
            // standing report is "it's still black", so this is how a player reads the
            // reason without a debugger, and how they clear the record after a fix.
            menu.then(Commands.literal("reset").executes(ctx -> ds$menuReset(ctx.getSource())));
            // BUILD #469 -- and the one lever that answers "it's still black" from
            // inside the game: which backdrop the title wears. /ds menu sky forces
            // the mod's own painted sky (which is floored, so it cannot be black),
            // /ds menu panorama goes back to the game's own panorama cube. Saved, so
            // it survives the next launch.
            menu.then(Commands.literal("sky").executes(ctx -> ds$menuBackdrop(ctx.getSource(), false)));
            menu.then(Commands.literal("panorama").executes(ctx -> ds$menuBackdrop(ctx.getSource(), true)));

            // BUILD #463 -- what the void is doing to you, and what to do about it.
            LiteralArgumentBuilder<CommandSourceStack> aging = Commands.literal("aging");
            aging.executes(ctx -> ds$aging(ctx.getSource(), null));
            aging.then(Commands.literal("clear").executes(ctx -> ds$aging(ctx.getSource(), "clear")));
            aging.then(Commands.literal("advance").executes(ctx -> ds$aging(ctx.getSource(), "advance")));
            aging.then(Commands.literal("full").executes(ctx -> ds$aging(ctx.getSource(), "full")));

            // BUILD #466 -- the rifts: open one by hand, or seal the ones that are
            // open. The tears seal themselves on their own timer either way; this is
            // how a player sees one on purpose, and how they get rid of one.
            LiteralArgumentBuilder<CommandSourceStack> rift = Commands.literal("rift");
            rift.executes(ctx -> ds$rift(ctx.getSource(), "open"));
            rift.then(Commands.literal("open").executes(ctx -> ds$rift(ctx.getSource(), "open")));
            rift.then(Commands.literal("seal").executes(ctx -> ds$rift(ctx.getSource(), "seal")));

            // BUILD #444 -- the underground structures, findable.
            // BUILD #451 -- /ds reality void goes to the nothing.
            LiteralArgumentBuilder<CommandSourceStack> maze = Commands.literal("maze");
            maze.executes(ctx -> ds$maze(ctx.getSource()));
            maze.then(Commands.literal("where").executes(ctx -> ds$maze(ctx.getSource())));
            maze.then(Commands.literal("build").executes(ctx -> ds$mazeBuild(ctx.getSource())));

            // BUILD #451 -- the doorways, and the void.
            LiteralArgumentBuilder<CommandSourceStack> portal = Commands.literal("portal");
            portal.executes(ctx -> ds$portalList(ctx.getSource()));
            portal.then(Commands.literal("list").executes(ctx -> ds$portalList(ctx.getSource())));
            // BUILD #462 -- the list is the doors' own: the reach is the fourth.
            for (String id : new String[]{"decayed", "adams", "void", "creator"}) {
                portal.then(Commands.literal("build").then(Commands.literal(id)
                        .executes(ctx -> ds$portalBuild(ctx.getSource(), id))));
            }

            // BUILD #450 -- what the nearest district is wearing, so the new sizes
            // and atmospheres can be checked without walking there.
            LiteralArgumentBuilder<CommandSourceStack> city = Commands.literal("city");
            city.executes(ctx -> ds$city(ctx.getSource()));

            // BUILD #456 -- the mod's own mobs, summonable by name. "I can't test"
            // is a fair complaint about a build whose monsters live at the bottom of
            // a dimension with no ground in it; this puts one in front of you.
            LiteralArgumentBuilder<CommandSourceStack> mob = Commands.literal("mob");
            mob.executes(ctx -> ds$mob(ctx.getSource(), null));
            for (String id : new String[] { "massg", "creator", "whale", "voidwalker", "lurker", "drifter", "keeper" }) {
                mob.then(Commands.literal(id)
                        .executes(ctx -> ds$mob(ctx.getSource(), id)));
            }

            // BUILD #459 -- the locks. "own blocks, items, mobs, LOCKS": every
            // world seals its doors with its own lock, and this raises one two
            // blocks in front of you so the mechanic is testable without walking
            // to a city, a chamber or a house in the nothing.
            LiteralArgumentBuilder<CommandSourceStack> lock = Commands.literal("lock");
            lock.executes(ctx -> ds$lock(ctx.getSource(), null));
            for (String id : new String[] { "decayed", "adams", "void" }) {
                lock.then(Commands.literal(id).executes(ctx -> ds$lock(ctx.getSource(), id)));
            }
            // BUILD #447 -- the cutscenes, listed from the shared table (the server
            // never loads the drawing class).
            LiteralArgumentBuilder<CommandSourceStack> scene = Commands.literal("scene");
            scene.executes(ctx -> ds$scene(ctx.getSource()));
            scene.then(Commands.literal("list").executes(ctx -> ds$scene(ctx.getSource())));

            // BUILD #445 -- the future-book, for anyone who wants the countdown in chat
            // rather than on a page.
            LiteralArgumentBuilder<CommandSourceStack> book = Commands.literal("book");
            book.executes(ctx -> ds$book(ctx.getSource()));

            LiteralArgumentBuilder<CommandSourceStack> server = Commands.literal("server");
            server.executes(ctx -> ds$server(ctx.getSource()));
            server.then(Commands.literal("where").executes(ctx -> ds$server(ctx.getSource())));
            server.then(Commands.literal("shell").executes(ctx -> ds$serverShell(ctx.getSource())));

            dispatcher.register(Commands.literal("ds").then(towns).then(storm)
                    .then(ritual).then(reality).then(maze).then(server).then(book).then(scene)
                    .then(city).then(portal).then(mob).then(lock).then(aging).then(menu)
                    .then(rift));
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
                + (net.mcsm.extras.McsmExtrasConfig.rituals ? "ON" : "OFF")
                + " \u00b7 the void: "
                + (net.mcsm.extras.McsmExtrasConfig.voidReality ? "ON" : "OFF")
                + " \u00b7 doorways: "
                + (net.mcsm.extras.McsmExtrasConfig.portals ? "ON" : "OFF")
                + " \u00b7 the reach: "
                + (net.mcsm.extras.McsmExtrasConfig.creatorRealm ? "ON" : "OFF")
                + " \u00b7 void aging: "
                + (net.mcsm.extras.McsmExtrasConfig.voidAging ? "ON" : "OFF")
                + " \u00b7 menu backdrop: "
                + (net.mcsm.extras.McsmExtrasConfig.menuPanorama ? "panorama" : "storm sky")),
                false);
        src.sendSuccess(() -> Component.literal("[ds] adams has written "
                + net.mcsm.extras.McsmAdams.builtRegions() + " regions, "
                + net.mcsm.extras.McsmAdams.pendingRegions() + " still queued \u00b7 "
                + "/ds reality adams goes in, /ds reality decayed goes through the rift"), false);
        return 1;
    }

    private static int ds$menu(CommandSourceStack src) {
        try {
            net.mcsm.extras.McsmExtrasConfig.load();
            boolean panorama = net.mcsm.extras.McsmExtrasConfig.menuPanorama;
            src.sendSuccess(() -> Component.literal("[ds] menu backdrop: "
                    + (panorama ? "the vanilla panorama cube" : "the storm's own sky")
                    + " \u00b7 lift " + net.mcsm.extras.McsmExtrasConfig.menuLift
                    + " \u00b7 wordmark "
                    + (net.mcsm.extras.McsmExtrasConfig.titleWordmark ? "ON" : "OFF")
                    + " \u00b7 boot cinematic "
                    + (net.mcsm.extras.McsmExtrasConfig.cinematicBootEnabled ? "ON" : "OFF")), false);
            src.sendSuccess(() -> Component.literal("[ds] menu state: "
                    + net.mcsm.extras.McsmExtrasConfig.menuState()
                    + " \u00b7 edit config/mcsm_storm_extras.properties to force any of it"),
                    false);
            // BUILD #469 -- the render side, which is what "it's still black" is about:
            // whether the mod's menu chrome is painting, and if not, WHAT through.
            src.sendSuccess(() -> Component.literal("[ds] "
                    + net.mcsm.extras.client.McsmMenuGuard.state()
                    + " \u00b7 /ds menu reset clears it"), false);
            return 1;
        } catch (Throwable t) {
            src.sendFailure(Component.literal("[ds] " + t));
            return 0;
        }
    }

    /**
     * BUILD #469 -- clear the menu render record and let the mod's chrome paint
     * again at once. The guard stands itself down for 30 s after three faults, so
     * a player who has just changed something (a pack, a config, a driver) can ask
     * for the mod's menu back without restarting the game.
     */
    /**
     * BUILD #469 -- switch the title's backdrop from in-game and save it. The report
     * "it's still black" is answered by this in one line: the mod's own sky (the
     * floored one, the one that cannot be black) or the game's panorama cube.
     */
    private static int ds$menuBackdrop(CommandSourceStack src, boolean panorama) {
        try {
            net.mcsm.extras.McsmExtrasConfig.load();
            net.mcsm.extras.McsmExtrasConfig.menuPanorama = panorama;
            net.mcsm.extras.McsmExtrasConfig.save();
            src.sendSuccess(() -> Component.literal("[ds] title backdrop: "
                    + (panorama ? "the game's own panorama cube"
                            : "the mod's own sky (guaranteed non-black)")
                    + " \u00b7 saved to config/mcsm_storm_extras.properties"), false);
            return 1;
        } catch (Throwable t) {
            src.sendFailure(Component.literal("[ds] " + t));
            return 0;
        }
    }

    private static int ds$menuReset(CommandSourceStack src) {
        try {
            net.mcsm.extras.client.McsmMenuGuard.reset();
            src.sendSuccess(() -> Component.literal("[ds] "
                    + net.mcsm.extras.client.McsmMenuGuard.state()), false);
            return 1;
        } catch (Throwable t) {
            src.sendFailure(Component.literal("[ds] " + t));
            return 0;
        }
    }

    private static int ds$aging(CommandSourceStack src, String action) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            if ("clear".equals(action)) {
                net.mcsm.extras.McsmVoidAging.clear(player);
                src.sendSuccess(() -> Component.literal("[ds] the void lets go: clean again"), false);
                return 1;
            }
            if ("advance".equals(action)) {
                net.mcsm.extras.McsmVoidAging.setAge(player,
                        net.mcsm.extras.McsmVoidAging.ageOf(player) + 2400L);
            } else if ("full".equals(action)) {
                net.mcsm.extras.McsmVoidAging.setAge(player, net.mcsm.extras.McsmVoidAging.MAX);
            }
            long age = net.mcsm.extras.McsmVoidAging.ageOf(player);
            int stage = net.mcsm.extras.McsmVoidAging.stageOf(age);
            src.sendSuccess(() -> Component.literal("[ds] void aging: "
                    + net.mcsm.extras.McsmVoidAging.stageName(stage) + " (" + age + "/"
                    + net.mcsm.extras.McsmVoidAging.MAX + " ticks in the void, "
                    + Math.round(net.mcsm.extras.McsmVoidAging.fractionOf(player) * 100.0F)
                    + "%)"), false);
            return 1;
        } catch (Throwable t) {
            src.sendFailure(Component.literal("[ds] " + t));
            return 0;
        }
    }

    private static int ds$rift(CommandSourceStack src, String action) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            net.minecraft.server.level.ServerLevel level = src.getLevel();
            if ("seal".equals(action)) {
                int sealed = net.mcsm.extras.McsmRifts.sealAll(level);
                src.sendSuccess(() -> Component.literal("[ds] sealed " + sealed + " tear"
                        + (sealed == 1 ? "" : "s")), false);
                return 1;
            }
            boolean opened = net.mcsm.extras.McsmRifts.openNow(level, player);
            src.sendSuccess(() -> Component.literal(opened
                    ? "[ds] reality tears open -- " + (int) net.mcsm.extras.McsmExtrasConfig.riftSeconds
                            + " seconds, then it seals itself"
                    : "[ds] no tear opened: rifts are off, or there are already "
                            + net.mcsm.extras.McsmRifts.alive(level) + " open here"), false);
            return opened ? 1 : 0;
        } catch (Throwable t) {
            src.sendFailure(Component.literal("[ds] " + t));
            return 0;
        }
    }

    private static int ds$creator(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            if (net.mcsm.extras.McsmCreatorRealm.enter(player)) {
                src.sendSuccess(() -> Component.literal("[ds] the reach opens"), false);
                return 1;
            }
            src.sendFailure(Component.literal("[ds] the reach is switched off in the config"));
            return 0;
        } catch (Throwable t) {
            src.sendFailure(Component.literal("[ds] " + t));
            return 0;
        }
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

    private static int ds$scene(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("[ds] cutscenes: "
                + net.mcsm.extras.McsmSceneTable.summary()), false);
        for (net.mcsm.extras.McsmSceneTable.Scene scene : net.mcsm.extras.McsmSceneTable.all()) {
            src.sendSuccess(() -> Component.literal("[ds]   " + scene.id() + " -- "
                    + scene.title() + " (" + (scene.ms() / 1000L) + "s, fires on "
                    + scene.trigger() + ")"), false);
        }
        src.sendSuccess(() -> Component.literal(
                "[ds] they fire themselves in the world, once each, the first time their "
                + "trigger happens -- or press N in game to play the next one you have not seen"),
                false);
        return 1;
    }

    /**
     * BUILD #456 -- put one of the mod's own bodies in front of the caller.
     *
     * <p>Seven names, and every one of them is the mod's own entity with the mod's
     * own model: massg (the black warden), creator (the colossal), whale, voidwalker
     * and lurker (the mini-boss, all from the storm and the nothing), plus drifter
     * (the decayed reality's) and keeper (the infinite dimension's). The beast kinds
     * are set on the way in, because the Mas body is the one that cannot be killed
     * and must never be handed out by accident.
     */
    private static int ds$mob(CommandSourceStack src, String which) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            String pick = which == null ? "voidwalker" : which;
            net.minecraft.server.level.ServerLevel level =
                    (net.minecraft.server.level.ServerLevel) player.level();
            net.minecraft.world.entity.EntityType<?> type;
            String name;
            String kind = null;
            switch (pick) {
                case "massg" -> {
                    type = net.mcsm.extras.entity.McsmEntities.MAS;
                    name = "Mas";
                    kind = net.mcsm.extras.entity.McsmBeast.MAS;
                }
                case "creator" -> {
                    type = net.mcsm.extras.entity.McsmEntities.CREATOR;
                    name = "The Creator";
                    kind = net.mcsm.extras.entity.McsmBeast.CREATOR;
                }
                case "whale" -> {
                    type = net.mcsm.extras.entity.McsmEntities.WHALE_MONSTER;
                    name = "The Whale";
                    kind = net.mcsm.extras.entity.McsmBeast.WHALE;
                }
                case "lurker" -> {
                    type = net.mcsm.extras.entity.McsmEntities.VOID_LURKER;
                    name = "The Lurker";
                }
                case "drifter" -> {
                    type = net.mcsm.extras.entity.McsmEntities.DRIFTER;
                    name = "Drifter";
                }
                case "keeper" -> {
                    type = net.mcsm.extras.entity.McsmEntities.KEEPER;
                    name = "Keeper";
                }
                default -> {
                    type = net.mcsm.extras.entity.McsmEntities.VOIDWALKER;
                    name = "Voidwalker";
                }
            }
            if (type == null) {
                src.sendSuccess(() -> Component.literal("[ds] that body is not registered in this jar"), false);
                return 0;
            }
            net.minecraft.world.entity.Entity spawned = type.create(level,
                    net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (!(spawned instanceof net.minecraft.world.entity.Mob mob)) {
                src.sendSuccess(() -> Component.literal("[ds] that body could not be created"), false);
                return 0;
            }
            if (kind != null && spawned instanceof net.mcsm.extras.entity.McsmBeast beast) {
                beast.setKind(kind);
            }
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(player.blockPosition()),
                    net.minecraft.world.entity.EntitySpawnReason.COMMAND, (net.minecraft.world.entity.SpawnGroupData) null);
            mob.setCustomName(Component.literal(name));
            mob.setCustomNameVisible(true);
            mob.setPersistenceRequired();
            double dx = player.getLookAngle().x * 6.0D;
            double dz = player.getLookAngle().z * 6.0D;
            mob.snapTo(player.getX() + dx, player.getY(), player.getZ() + dz,
                    player.getYRot() + 180.0F, 0.0F);
            level.addFreshEntity(mob);
            String shown = name;
            src.sendSuccess(() -> Component.literal("[ds] " + shown
                    + " is in front of you -- /ds mob for the rest"), false);
            return 1;
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] no mob: " + t), false);
            return 0;
        }
    }

    /**
     * BUILD #459 -- /ds lock &lt;decayed|adams|void&gt;: puts that world's seal in
     * front of you, facing you, so the key it takes can be checked in game. The
     * lock is a real block from {@link net.mcsm.extras.McsmContent} and the
     * mechanic is {@link net.mcsm.extras.McsmLocks}; nothing here invents anything.
     */
    private static int ds$lock(CommandSourceStack src, String which) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            String dim = which == null ? net.mcsm.extras.McsmIdentity.DECAYED : which;
            String id = net.mcsm.extras.McsmLocks.blockIdFor(dim);
            if (id == null) {
                src.sendSuccess(() -> Component.literal(
                        "[ds] no lock for that world -- decayed, adams or void"), false);
                return 0;
            }
            net.minecraft.server.level.ServerLevel level =
                    (net.minecraft.server.level.ServerLevel) player.level();
            net.minecraft.core.BlockPos at = player.blockPosition().relative(
                    player.getDirection(), 2);
            net.minecraft.world.level.block.Block block =
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                    "mcsm", id.substring("mcsm:".length())));
            if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) {
                src.sendSuccess(() -> Component.literal(
                        "[ds] that lock is not registered in this jar"), false);
                return 0;
            }
            level.setBlock(at, block.defaultBlockState(), 3);
            String key = net.mcsm.extras.McsmLocks.keyNameFor(dim);
            src.sendSuccess(() -> Component.literal("[ds] a " + dim + " lock is in front of "
                    + "you -- " + key + " opens it, and nothing else does"), false);
            return 1;
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] no lock: " + t), false);
            return 0;
        }
    }

    private static int ds$city(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            int x = (int) player.getX();
            int z = (int) player.getZ();
            int[] near = net.mcsm.extras.McsmCities.nearestCity(x, z);
            if (near == null) {
                src.sendSuccess(() -> Component.literal(
                        "[ds] no district within " + (net.mcsm.extras.McsmCities.REGION * 4)
                        + " blocks -- they raise on a 256-block grid"), false);
                return 0;
            }
            // nearestCity answers {dx, dz, distance}: turn the offset back into the
            // district's own region, because the size and the air live there.
            int ox = x + near[0];
            int oz = z + near[1];
            int rx = Math.floorDiv(ox, net.mcsm.extras.McsmCities.REGION);
            int rz = Math.floorDiv(oz, net.mcsm.extras.McsmCities.REGION);
            src.sendSuccess(() -> Component.literal("[ds] the nearest district is " + near[2]
                    + " blocks away at " + ox + ", " + oz + " and it is "
                    + net.mcsm.extras.McsmCities.atmosphereName(rx, rz)), false);
            src.sendSuccess(() -> Component.literal("[ds] " + net.mcsm.extras.McsmCities.stats()), false);
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds city has to be run by a player"), false);
        }
        return 1;
    }

    // ---------------------------------------------------------------------
    // BUILD #451 -- the void, and the doorways
    // ---------------------------------------------------------------------

    private static int ds$void(CommandSourceStack src) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            if (net.mcsm.extras.McsmVoid.enter(player)) {
                src.sendSuccess(() -> Component.literal("[ds] the nothing opens; there is no floor"), false);
                return 1;
            }
            src.sendSuccess(() -> Component.literal("[ds] the void is switched off in the config"), false);
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds reality void has to be run by a player"), false);
        }
        return 0;
    }

    private static int ds$portalList(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("[ds] one doorway per dimension: "
                + net.mcsm.extras.McsmPortals.summary()), false);
        for (net.mcsm.extras.McsmPortals.Door door : net.mcsm.extras.McsmPortals.doors()) {
            src.sendSuccess(() -> Component.literal("[ds]   " + door.id() + " -- " + door.label()
                    + " (frame " + door.frameBlock() + ", door " + door.doorBlock() + ")"), false);
        }
        src.sendSuccess(() -> Component.literal("[ds] /ds portal build <decayed|adams|void> raises one "
                + "in front of you; walk into the middle to go through"), false);
        src.sendSuccess(() -> Component.literal("[ds] the void: "
                + net.mcsm.extras.McsmVoid.stats()), false);
        return 1;
    }

    private static int ds$portalBuild(CommandSourceStack src, String id) {
        try {
            net.minecraft.server.level.ServerPlayer player = src.getPlayerOrException();
            if (net.mcsm.extras.McsmPortals.build(player, id)) {
                return 1;
            }
            src.sendSuccess(() -> Component.literal("[ds] no doorway called \"" + id + "\""), false);
        } catch (Throwable t) {
            src.sendSuccess(() -> Component.literal("[ds] /ds portal build has to be run by a player"), false);
        }
        return 0;
    }
}
