package net.dabicco.witherstormmod.structures;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * /mcsm — build and travel the Minecraft: Story Mode world.
 *
 * Registered as its own root command rather than folded into /dabyws, because
 * that command is one enormous chained builder expression and editing it by
 * hand is a good way to break every existing subcommand.
 *
 *   /mcsm build all          queue every location
 *   /mcsm build <name>       queue one
 *   /mcsm list               show every location with a clickable teleport
 *   /mcsm tp <name>          teleport to one
 *   /mcsm status             how much of the queue is left
 *   /mcsm cancel             drop the queue
 */
public final class McsmCommand {

   private McsmCommand() {
   }

   private static final SuggestionProvider<CommandSourceStack> SITES =
      (ctx, b) -> suggestSites(b);

   private static CompletableFuture<Suggestions> suggestSites(SuggestionsBuilder b) {
      String q = b.getRemaining().toLowerCase(java.util.Locale.ROOT);
      for (McsmWorldgen.Site s : McsmWorldgen.layout()) {
         String key = key(s);
         if (key.startsWith(q)) {
            b.suggest(key);
         }
      }
      b.suggest("all");
      return b.buildFuture();
   }

   /** A short lowercase handle for a site, e.g. "beacon_town". */
   public static String key(McsmWorldgen.Site s) {
      return s.label().toLowerCase(java.util.Locale.ROOT)
              .replaceAll("[^a-z0-9]+", "_")
              .replaceAll("^_|_$", "");
   }


   /** Same permission rule the mod uses elsewhere: ops, or the Sigeon owner. */
   private static boolean mayBuild(CommandSourceStack source) {
      net.minecraft.server.level.ServerPlayer player = source.getPlayer();
      return player == null || net.dabicco.witherstormmod.SigeonNetwork.canEdit(player);
   }

   public static void register(CommandDispatcher<CommandSourceStack> d) {
      d.register(
         Commands.literal("mcsm")
            .then(Commands.literal("list").executes(c -> list(c.getSource())))
            .then(Commands.literal("status").executes(c -> status(c.getSource())))
            .then(Commands.literal("cancel")
                  .requires(McsmCommand::mayBuild)
                  .executes(c -> cancel(c.getSource())))
            .then(Commands.literal("build")
                  .requires(McsmCommand::mayBuild)
                  .then(Commands.argument("site", StringArgumentType.word())
                        .suggests(SITES)
                        .executes(c -> build(c.getSource(),
                                             StringArgumentType.getString(c, "site")))))
            .then(Commands.literal("tp")
                  .then(Commands.argument("site", StringArgumentType.word())
                        .suggests(SITES)
                        .executes(c -> tp(c.getSource(),
                                          StringArgumentType.getString(c, "site")))))
            .executes(c -> list(c.getSource()))
      );
   }

   private static int list(CommandSourceStack src) {
      src.sendSuccess(() -> Component.literal("Minecraft: Story Mode world — anchor X=")
                            .append(Component.literal(String.valueOf(McsmWorldgen.ANCHOR_X)))
                            .append(Component.literal(" Z="))
                            .append(Component.literal(String.valueOf(McsmWorldgen.ANCHOR_Z)))
                            .withStyle(ChatFormatting.LIGHT_PURPLE), false);
      for (McsmWorldgen.Site s : McsmWorldgen.layout()) {
         String coords = s.x() + " " + s.y() + " " + s.z();
         Component line = Component.literal(" • ")
            .append(Component.literal(s.label()).withStyle(
               style -> style.withColor(s.floating() ? ChatFormatting.AQUA : ChatFormatting.WHITE)))
            .append(Component.literal("  ["))
            .append(Component.literal(coords).withStyle(
               style -> style
                  .withColor(ChatFormatting.GREEN)
                  .withUnderlined(true)
                  .withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + coords))
                  .withHoverEvent(new HoverEvent.ShowText(
                     Component.literal("Click to put the teleport in your chat")))))
            .append(Component.literal("]"));
         src.sendSuccess(() -> line, false);
      }
      return 1;
   }

   private static int status(CommandSourceStack src) {
      int n = McsmWorldgen.pending();
      src.sendSuccess(() -> Component.literal(
         n == 0 ? "Nothing building." : (n + " location(s) still building.")), false);
      return n;
   }

   private static int cancel(CommandSourceStack src) {
      int n = McsmWorldgen.pending();
      McsmWorldgen.clear();
      src.sendSuccess(() -> Component.literal("Cancelled " + n + " queued build(s)."), true);
      return n;
   }

   private static int build(CommandSourceStack src, String which) {
      ServerLevel level = src.getLevel();
      List<McsmWorldgen.Site> sites = McsmWorldgen.layout();
      boolean all = "all".equalsIgnoreCase(which);
      int queued = 0;
      int failed = 0;

      for (McsmWorldgen.Site s : sites) {
         if (!all && !key(s).equalsIgnoreCase(which)) {
            continue;
         }
         try {
            McsmSchematic sch = McsmSchematic.load(
               src.getServer().getResourceManager(), s.path());
            McsmWorldgen.enqueue(sch, new BlockPos(s.x(), s.y(), s.z()), s.label());
            queued++;
         } catch (Exception ex) {
            failed++;
            src.sendFailure(Component.literal(
               "Could not load " + s.label() + ": " + ex.getMessage()));
         }
      }

      if (queued == 0 && failed == 0) {
         src.sendFailure(Component.literal("No location called '" + which + "'. Try /mcsm list."));
         return 0;
      }
      final int q = queued;
      src.sendSuccess(() -> Component.literal(
         "Queued " + q + " location(s). They build over the next few minutes — /mcsm status to check."),
         true);
      return queued;
   }

   private static int tp(CommandSourceStack src, String which) {
      for (McsmWorldgen.Site s : McsmWorldgen.layout()) {
         if (key(s).equalsIgnoreCase(which)) {
            ServerPlayer player = src.getPlayer();
            if (player == null) {
               src.sendFailure(Component.literal("/mcsm tp must be run by a player."));
               return 0;
            }
            // Actually move the player. Previously this only printed a
            // clickable suggestion, which looked like the command did nothing.
            double tx = s.x() + 0.5;
            double ty = s.y() + 2;
            double tz = s.z() + 0.5;
            player.teleportTo(src.getLevel(), tx, ty, tz,
                              java.util.Set.of(), player.getYRot(), player.getXRot(), false);
            src.sendSuccess(() -> Component.literal("Teleported to " + s.label() + " ")
               .append(Component.literal("(" + s.x() + ", " + (s.y() + 2) + ", " + s.z() + ")")
                  .withStyle(ChatFormatting.GRAY)), false);
            return 1;
         }
      }
      src.sendFailure(Component.literal("No location called '" + which + "'. Try /mcsm list."));
      return 0;
   }
}
