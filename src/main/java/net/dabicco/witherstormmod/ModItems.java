package net.dabicco.witherstormmod;

import java.util.List;
import java.util.function.Function;
import net.dabicco.witherstormmod.item.FormidibombItem;
import net.dabicco.witherstormmod.item.RocketRetrieverItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;

public class ModItems {
   public static final Item WITHER_HEART = register("wither_heart", Item::new, new Item.Properties());
   public static final Item WITHER_FRAGMENT = register("wither_fragment", Item::new, new Item.Properties());
   public static final Item COMMAND_ESSENCE = register("command_essence", Item::new, new Item.Properties());
   public static final Item COMMAND_CIRCUIT = register("command_circuit", Item::new, new Item.Properties());
   public static final Item CONTROL_PANEL = register("control_panel", Item::new, new Item.Properties());
   public static final Item SUPER_TNT = register("super_tnt", (props) -> new BlockItem(ModBlocks.SUPER_TNT, props), new Item.Properties());
   public static final Item ROCKET_RETRIEVER = register("rocket_retriever", RocketRetrieverItem::new, (new Item.Properties()).stacksTo(1));
   public static final Item GRAPPLE = register("grapple", Item::new, new Item.Properties());
   public static final Item FORMIDIBOMB;
   public static final Item FURNACE_FILTER;
   public static final Item WITHERED_FLESH_BLOCK;
   public static final Item TORN_WITHERED_FLESH;
   public static final Item WITHERED_BEDROCK;
   public static final Item STRIPPED_WITHERED_LOG;
   public static final Item WITHERED_PLANKS;
   public static final Item WITHERED_STAIRS;
   public static final Item WITHERED_SLAB;
   public static final Item WITHERED_FENCE;
   public static final Item WITHERED_BUTTON;
   public static final Item STRIPPED_WITHERED_PLANKS;
   public static final Item STRIPPED_WITHERED_STAIRS;
   public static final Item STRIPPED_WITHERED_SLAB;
   public static final Item STRIPPED_WITHERED_FENCE;
   public static final Item STRIPPED_WITHERED_BUTTON;
   public static final Item WITHERED_MUSHROOM;
   public static final Item WITHERED_DUST;
   public static final Item WITHERED_STONE;
   public static final Item WITHERED_STONE_STAIRS;
   public static final Item WITHERED_STONE_SLAB;
   public static final Item AMULET_BRIDGES;
   public static final Item AMULET_WUSSMODE;
   public static final Item WITHERED_NETHER_STAR;
   public static final Item WITHERED_COBBLESTONE;
   public static final Item WITHERED_NETHERBRICK;
   public static final Item WITHERED_SAND;
   public static final Item WITHERED_COBBLESTONE_STAIRS;
   public static final Item WITHERED_COBBLESTONE_SLAB;
   public static final Item WITHERED_COBBLESTONE_WALL;
   public static final Item WITHERED_NETHERBRICK_STAIRS;
   public static final Item WITHERED_NETHERBRICK_SLAB;
   public static final Item WITHERED_NETHERBRICK_WALL;
   public static final Item WITHERED_NETHERBRICK_FENCE;
   public static final Item WITHERED_LOG;
   // Devouring Storms overhaul inventory
   public static final Item COMMAND_BLOCK_SWORD = register("command_block_sword", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
   public static final Item COMMAND_BLOCK_PICKAXE = register("command_block_pickaxe", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
   public static final Item COMMAND_BLOCK_AXE = register("command_block_axe", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
   public static final Item COMMAND_BLOCK_SHOVEL = register("command_block_shovel", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
   public static final Item COMMAND_BLOCK_HOE = register("command_block_hoe", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
   public static final Item COMMAND_BLOCK_BOOK = register("command_block_book", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
   public static final Item HERO_AMULET = register("hero_amulet", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
   public static final Item CORRUPTION_SHARD = register("corruption_shard", Item::new, new Item.Properties());
   public static final Item RIFT_CORE = register("rift_core", Item::new, new Item.Properties().stacksTo(16).rarity(Rarity.EPIC));
   public static final Item SURVIVOR_COMPASS = register("survivor_compass", Item::new, new Item.Properties().stacksTo(1));
   public static final Item STORM_EYE = register("storm_eye", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
   public static final Item DECAYED_ESSENCE = register("decayed_essence", Item::new, new Item.Properties());
   public static final Item REALITY_ANCHOR = register("reality_anchor", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

   public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
      ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("dabywitherstormmod", name));
      T item = (T)(itemFactory.apply(settings.setId(itemKey)));
      Registry.register(BuiltInRegistries.ITEM, itemKey, item);
      return item;
   }

   public static void initialize() {
   }

   static {
      FORMIDIBOMB = register("formidibomb", FormidibombItem::new, (new Item.Properties()).stacksTo(16).component(DataComponents.ITEM_NAME, Component.translatable("item.dabywitherstormmod.formidibomb").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(10170623)))).component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.dabywitherstormmod.formidibomb.lore").withStyle(ChatFormatting.GRAY), Component.translatable("item.dabywitherstormmod.formidibomb.warning").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})))));
      FURNACE_FILTER = register("furnace_filter", (props) -> new BlockItem(ModBlocks.FURNACE_FILTER, props), (new Item.Properties()).component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.dabywitherstormmod.furnace_filter.lore").withStyle(ChatFormatting.GRAY)))));
      WITHERED_FLESH_BLOCK = register("withered_flesh_block", (props) -> new BlockItem(ModBlocks.WITHERED_FLESH_BLOCK, props), new Item.Properties());
      TORN_WITHERED_FLESH = register("torn_withered_flesh", (props) -> new BlockItem(ModBlocks.TORN_WITHERED_FLESH, props), new Item.Properties());
      WITHERED_BEDROCK = register("withered_bedrock", (props) -> new BlockItem(ModBlocks.WITHERED_BEDROCK, props), new Item.Properties());
      STRIPPED_WITHERED_LOG = register("stripped_withered_log", (props) -> new BlockItem(ModBlocks.STRIPPED_WITHERED_LOG, props), new Item.Properties());
      WITHERED_PLANKS = register("withered_planks", (props) -> new BlockItem(ModBlocks.WITHERED_PLANKS, props), new Item.Properties());
      WITHERED_STAIRS = register("withered_stairs", (props) -> new BlockItem(ModBlocks.WITHERED_STAIRS, props), new Item.Properties());
      WITHERED_SLAB = register("withered_slab", (props) -> new BlockItem(ModBlocks.WITHERED_SLAB, props), new Item.Properties());
      WITHERED_FENCE = register("withered_fence", (props) -> new BlockItem(ModBlocks.WITHERED_FENCE, props), new Item.Properties());
      WITHERED_BUTTON = register("withered_button", (props) -> new BlockItem(ModBlocks.WITHERED_BUTTON, props), new Item.Properties());
      STRIPPED_WITHERED_PLANKS = register("stripped_withered_planks", (props) -> new BlockItem(ModBlocks.STRIPPED_WITHERED_PLANKS, props), new Item.Properties());
      STRIPPED_WITHERED_STAIRS = register("stripped_withered_stairs", (props) -> new BlockItem(ModBlocks.STRIPPED_WITHERED_STAIRS, props), new Item.Properties());
      STRIPPED_WITHERED_SLAB = register("stripped_withered_slab", (props) -> new BlockItem(ModBlocks.STRIPPED_WITHERED_SLAB, props), new Item.Properties());
      STRIPPED_WITHERED_FENCE = register("stripped_withered_fence", (props) -> new BlockItem(ModBlocks.STRIPPED_WITHERED_FENCE, props), new Item.Properties());
      STRIPPED_WITHERED_BUTTON = register("stripped_withered_button", (props) -> new BlockItem(ModBlocks.STRIPPED_WITHERED_BUTTON, props), new Item.Properties());
      WITHERED_MUSHROOM = register("withered_mushroom", (props) -> new BlockItem(ModBlocks.WITHERED_MUSHROOM, props), new Item.Properties());
      WITHERED_DUST = register("withered_dust", (props) -> new BlockItem(ModBlocks.WITHERED_DUST, props), new Item.Properties());
      WITHERED_STONE = register("withered_stone", (props) -> new BlockItem(ModBlocks.WITHERED_STONE, props), new Item.Properties());
      WITHERED_STONE_STAIRS = register("withered_stone_stairs", (props) -> new BlockItem(ModBlocks.WITHERED_STONE_STAIRS, props), new Item.Properties());
      WITHERED_STONE_SLAB = register("withered_stone_slab", (props) -> new BlockItem(ModBlocks.WITHERED_STONE_SLAB, props), new Item.Properties());
      AMULET_BRIDGES = register("amulet_bridges", Item::new, (new Item.Properties()).stacksTo(1));
      AMULET_WUSSMODE = register("amulet_wussmode", Item::new, (new Item.Properties()).stacksTo(1));
      WITHERED_NETHER_STAR = register("withered_nether_star", Item::new, (new Item.Properties()).rarity(Rarity.EPIC).stacksTo(1).fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
      WITHERED_COBBLESTONE = register("withered_cobblestone", (props) -> new BlockItem(ModBlocks.WITHERED_COBBLESTONE, props), new Item.Properties());
      WITHERED_NETHERBRICK = register("withered_netherbrick", (props) -> new BlockItem(ModBlocks.WITHERED_NETHERBRICK, props), new Item.Properties());
      WITHERED_SAND = register("withered_sand", (props) -> new BlockItem(ModBlocks.WITHERED_SAND, props), new Item.Properties());
      WITHERED_COBBLESTONE_STAIRS = register("withered_cobblestone_stairs", (props) -> new BlockItem(ModBlocks.WITHERED_COBBLESTONE_STAIRS, props), new Item.Properties());
      WITHERED_COBBLESTONE_SLAB = register("withered_cobblestone_slab", (props) -> new BlockItem(ModBlocks.WITHERED_COBBLESTONE_SLAB, props), new Item.Properties());
      WITHERED_COBBLESTONE_WALL = register("withered_cobblestone_wall", (props) -> new BlockItem(ModBlocks.WITHERED_COBBLESTONE_WALL, props), new Item.Properties());
      WITHERED_NETHERBRICK_STAIRS = register("withered_netherbrick_stairs", (props) -> new BlockItem(ModBlocks.WITHERED_NETHERBRICK_STAIRS, props), new Item.Properties());
      WITHERED_NETHERBRICK_SLAB = register("withered_netherbrick_slab", (props) -> new BlockItem(ModBlocks.WITHERED_NETHERBRICK_SLAB, props), new Item.Properties());
      WITHERED_NETHERBRICK_WALL = register("withered_netherbrick_wall", (props) -> new BlockItem(ModBlocks.WITHERED_NETHERBRICK_WALL, props), new Item.Properties());
      WITHERED_NETHERBRICK_FENCE = register("withered_netherbrick_fence", (props) -> new BlockItem(ModBlocks.WITHERED_NETHERBRICK_FENCE, props), new Item.Properties());
      WITHERED_LOG = register("withered_log", (props) -> new BlockItem(ModBlocks.WITHERED_LOG, props), new Item.Properties());
   }
}
