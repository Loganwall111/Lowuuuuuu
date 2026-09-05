package net.dabicco.witherstormmod.structures;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.dabicco.witherstormmod.SigeonNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class McsmCommand {
   private static final SuggestionProvider<CommandSourceStack> SITES = (ctx, b) -> suggestSites(b);

   private McsmCommand() {
   }

   private static CompletableFuture<Suggestions> suggestSites(SuggestionsBuilder b) {
      String q = b.getRemaining().toLowerCase(Locale.ROOT);

      for (net.dabicco.witherstormmod.structures.McsmWorldgen.Site s : net.dabicco.witherstormmod.structures.McsmWorldgen.layout()) {
         String key = key(s);
         if (key.startsWith(q)) {
            b.suggest(key);
         }
      }

      b.suggest("all");
      return b.buildFuture();
   }

   public static String key(net.dabicco.witherstormmod.structures.McsmWorldgen.Site s) {
      return s.label().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
   }

   private static boolean mayBuild(CommandSourceStack source) {
      ServerPlayer player = source.getPlayer();
      return player == null || SigeonNetwork.canEdit(player);
   }

   public static void register(CommandDispatcher<CommandSourceStack> d) {
      d.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                              "mcsm"
                           )
                           .then(Commands.literal("list").executes(c -> list((CommandSourceStack)c.getSource()))))
                        .then(Commands.literal("status").executes(c -> status((CommandSourceStack)c.getSource()))))
                     .then(
                        ((LiteralArgumentBuilder)Commands.literal("cancel").requires(net.dabicco.witherstormmod.structures.McsmCommand::mayBuild))
                           .executes(c -> cancel((CommandSourceStack)c.getSource()))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("build").requires(net.dabicco.witherstormmod.structures.McsmCommand::mayBuild))
                        .then(
                           Commands.argument("site", StringArgumentType.word())
                              .suggests(SITES)
                              .executes(c -> build((CommandSourceStack)c.getSource(), StringArgumentType.getString(c, "site")))
                        )
                  ))
               .then(
                  Commands.literal("tp")
                     .then(
                        Commands.argument("site", StringArgumentType.word())
                           .suggests(SITES)
                           .executes(c -> tp((CommandSourceStack)c.getSource(), StringArgumentType.getString(c, "site")))
                     )
               ))
            .executes(c -> list((CommandSourceStack)c.getSource()))
      );
   }

   private static int list(CommandSourceStack src) {
      src.sendSuccess(
         () -> Component.literal("Minecraft: Story Mode world — anchor X=")
            .append(Component.literal(String.valueOf(-640)))
            .append(Component.literal(" Z="))
            .append(Component.literal(String.valueOf(256)))
            .withStyle(ChatFormatting.LIGHT_PURPLE),
         false
      );

      for (net.dabicco.witherstormmod.structures.McsmWorldgen.Site s : net.dabicco.witherstormmod.structures.McsmWorldgen.layout()) {
         String coords = s.x() + " " + s.y() + " " + s.z();
         Component line = Component.literal(" • ")
            .append(Component.literal(s.label()).withStyle(style -> style.withColor(s.floating() ? ChatFormatting.AQUA : ChatFormatting.WHITE)))
            .append(Component.literal("  ["))
            .append(
               Component.literal(coords)
                  .withStyle(
                     style -> style.withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new SuggestCommand("/tp @s " + coords))
                        .withHoverEvent(new ShowText(Component.literal("Click to put the teleport in your chat")))
                  )
            )
            .append(Component.literal("]"));
         src.sendSuccess(() -> line, false);
      }

      return 1;
   }

   private static int status(CommandSourceStack src) {
      int n = net.dabicco.witherstormmod.structures.McsmWorldgen.pending();
      src.sendSuccess(() -> Component.literal(n == 0 ? "Nothing building." : n + " location(s) still building."), false);
      return n;
   }

   private static int cancel(CommandSourceStack src) {
      int n = net.dabicco.witherstormmod.structures.McsmWorldgen.pending();
      net.dabicco.witherstormmod.structures.McsmWorldgen.clear();
      src.sendSuccess(() -> Component.literal("Cancelled " + n + " queued build(s)."), true);
      return n;
   }

   private static int build(CommandSourceStack src, String which) {
      ServerLevel level = src.getLevel();
      List<net.dabicco.witherstormmod.structures.McsmWorldgen.Site> sites = net.dabicco.witherstormmod.structures.McsmWorldgen.layout();
      boolean all = "all".equalsIgnoreCase(which);
      int queued = 0;
      int failed = 0;

      for (net.dabicco.witherstormmod.structures.McsmWorldgen.Site s : sites) {
         if (all || key(s).equalsIgnoreCase(which)) {
            try {
               net.dabicco.witherstormmod.structures.McsmSchematic sch = net.dabicco.witherstormmod.structures.McsmSchematic.load(
                  src.getServer().getResourceManager(), s.path()
               );
               net.dabicco.witherstormmod.structures.McsmWorldgen.enqueue(sch, new BlockPos(s.x(), s.y(), s.z()), s.label());
               queued++;
            } catch (Exception var10) {
               failed++;
               src.sendFailure(Component.literal("Could not load " + s.label() + ": " + var10.getMessage()));
            }
         }
      }

      if (queued == 0 && failed == 0) {
         src.sendFailure(Component.literal("No location called '" + which + "'. Try /mcsm list."));
         return 0;
      } else {
         int q = queued;
         src.sendSuccess(() -> Component.literal("Queued " + q + " location(s). They build over the next few minutes — /mcsm status to check."), true);
         return queued;
      }
   }

   private static int tp(CommandSourceStack src, String which) {
      for (net.dabicco.witherstormmod.structures.McsmWorldgen.Site s : net.dabicco.witherstormmod.structures.McsmWorldgen.layout()) {
         if (key(s).equalsIgnoreCase(which)) {
            ServerPlayer player = src.getPlayer();
            if (player == null) {
               src.sendFailure(Component.literal("/mcsm tp must be run by a player."));
               return 0;
            }

            double tx = s.x() + 0.5;
            double ty = s.y() + 2;
            double tz = s.z() + 0.5;
            player.teleportTo(src.getLevel(), tx, ty, tz, Set.of(), player.getYRot(), player.getXRot(), false);
            src.sendSuccess(
               () -> Component.literal("Teleported to " + s.label() + " ")
                  .append(Component.literal("(" + s.x() + ", " + (s.y() + 2) + ", " + s.z() + ")").withStyle(ChatFormatting.GRAY)),
               false
            );
            return 1;
         }
      }

      src.sendFailure(Component.literal("No location called '" + which + "'. Try /mcsm list."));
      return 0;
   }
}
