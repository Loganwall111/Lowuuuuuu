package net.dabicco.witherstormmod.structures;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.server.network.Filterable;

/**
 * The Story Mode guidebook.
 *
 * Handed to every player the first time they join a world, and it points them
 * at the MCSM builds: the anchor at X=-640 Z=256, the floating cities up at
 * y~180, and the /mcsm commands that raise and teleport between them.
 *
 * A written book is used rather than a custom item so it works with zero
 * client-side code, cannot desync, and survives any resource pack.
 */
public final class McsmGuidebook {

   private static final String TITLE = "The Order of the Stone";
   private static final String AUTHOR = "Dabby's Wither Storm";

   private McsmGuidebook() {
   }

   public static void register() {
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player == null) {
            return;
         }
         // Only once: tagged in the player's persistent data via a marker item scan.
         if (hasBook(player)) {
            return;
         }
         ItemStack book = create();
         if (!player.getInventory().add(book)) {
            player.drop(book, false);
         }
      });
   }

   private static boolean hasBook(ServerPlayer player) {
      for (ItemStack s : player.getInventory()) {
         if (s.is(Items.WRITTEN_BOOK)) {
            WrittenBookContent c = s.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (c != null && TITLE.equals(c.title().raw())) {
               return true;
            }
         }
      }
      return false;
   }

   /** Builds the guidebook itemstack. */
   public static ItemStack create() {
      List<Filterable<Component>> pages = new ArrayList<>();

      pages.add(Filterable.passThrough(page(
         line("THE ORDER", ChatFormatting.DARK_PURPLE, true),
         line("OF THE STONE", ChatFormatting.DARK_PURPLE, true),
         blank(),
         line("A traveller's guide to the", ChatFormatting.DARK_GRAY, false),
         line("worlds of Story Mode.", ChatFormatting.DARK_GRAY, false),
         blank(),
         line("Everything you are looking", ChatFormatting.BLACK, false),
         line("for lies to the west and", ChatFormatting.BLACK, false),
         line("north of spawn.", ChatFormatting.BLACK, false))));

      pages.add(Filterable.passThrough(page(
         line("BEACON TOWN", ChatFormatting.DARK_PURPLE, true),
         blank(),
         line("X = -640", ChatFormatting.DARK_GREEN, true),
         line("Z =  256", ChatFormatting.DARK_GREEN, true),
         blank(),
         line("The heart of it all. Every", ChatFormatting.BLACK, false),
         line("other build is scattered", ChatFormatting.BLACK, false),
         line("around this point - some", ChatFormatting.BLACK, false),
         line("close, some thousands of", ChatFormatting.BLACK, false),
         line("blocks out.", ChatFormatting.BLACK, false),
         blank(),
         tp("-640 90 256"))));

      pages.add(Filterable.passThrough(page(
         line("RAISING THE WORLD", ChatFormatting.DARK_PURPLE, true),
         blank(),
         line("The builds are not there", ChatFormatting.BLACK, false),
         line("until you ask for them.", ChatFormatting.BLACK, false),
         blank(),
         cmd("/mcsm list", "See all 35 sites"),
         cmd("/mcsm build all", "Raise everything"),
         cmd("/mcsm status", "Watch progress"),
         blank(),
         line("Building all of them moves", ChatFormatting.DARK_GRAY, false),
         line("millions of blocks. Give it", ChatFormatting.DARK_GRAY, false),
         line("a minute.", ChatFormatting.DARK_GRAY, false))));

      pages.add(Filterable.passThrough(page(
         line("THE SKY CITIES", ChatFormatting.DARK_PURPLE, true),
         blank(),
         line("Four builds float high", ChatFormatting.BLACK, false),
         line("above the clouds, near", ChatFormatting.BLACK, false),
         line("y = 276 to 308.", ChatFormatting.BLACK, false),
         blank(),
         cmd("/mcsm tp sky_city", "Sky City"),
         cmd("/mcsm tp sky_speakeasy", "The Speakeasy"),
         cmd("/mcsm tp jungle_fortress", "Jungle Fortress"),
         cmd("/mcsm tp mushroom_island", "Mushroom Island"),
         blank(),
         line("Bring something to break", ChatFormatting.DARK_GRAY, false),
         line("your fall.", ChatFormatting.DARK_GRAY, false))));

      pages.add(Filterable.passThrough(page(
         line("THE WITHER STORM", ChatFormatting.DARK_RED, true),
         blank(),
         line("It begins as a wither.", ChatFormatting.BLACK, false),
         line("It does not stay one.", ChatFormatting.BLACK, false),
         blank(),
         cmd("/dabyws spawn", "Summon it"),
         cmd("/dabyws setphase 4", "Force a phase"),
         blank(),
         line("At phase 4.5 the sky turns", ChatFormatting.BLACK, false),
         line("green and a hole opens in", ChatFormatting.BLACK, false),
         line("the middle of it.", ChatFormatting.BLACK, false),
         blank(),
         line("After phase 5 it turns", ChatFormatting.BLACK, false),
         line("purple, then pink, then", ChatFormatting.BLACK, false),
         line("violet, and it does not", ChatFormatting.BLACK, false),
         line("stop growing.", ChatFormatting.BLACK, false))));

      WrittenBookContent content = new WrittenBookContent(
         Filterable.passThrough(TITLE), AUTHOR, 0, pages, true);

      ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
      stack.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
      return stack;
   }

   private static Component page(Component... lines) {
      MutableComponent out = Component.empty();
      for (int i = 0; i < lines.length; i++) {
         out.append(lines[i]);
         if (i < lines.length - 1) {
            out.append(Component.literal("\n"));
         }
      }
      return out;
   }

   private static Component line(String text, ChatFormatting colour, boolean bold) {
      MutableComponent c = Component.literal(text).withStyle(colour);
      return bold ? c.withStyle(ChatFormatting.BOLD) : c;
   }

   private static Component blank() {
      return Component.literal("");
   }

   private static Component cmd(String command, String label) {
      return Component.literal(command)
         .withStyle(Style.EMPTY
            .withColor(ChatFormatting.DARK_AQUA)
            .withUnderlined(true)
            .withClickEvent(new ClickEvent.SuggestCommand(command)))
         .append(Component.literal("\n  " + label).withStyle(ChatFormatting.DARK_GRAY));
   }

   private static Component tp(String coords) {
      String command = "/tp " + coords;
      return Component.literal("[ Teleport there ]")
         .withStyle(Style.EMPTY
            .withColor(ChatFormatting.DARK_AQUA)
            .withBold(true)
            .withClickEvent(new ClickEvent.SuggestCommand(command)));
   }
}
