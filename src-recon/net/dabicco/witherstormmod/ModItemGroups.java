package net.dabicco.witherstormmod;

import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public final class ModItemGroups {
   public static final ResourceKey<CreativeModeTab> MAIN = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("dabywitherstormmod", "main")
   );

   private ModItemGroups() {
   }

   public static void initialize() {
      Registry.register(
         BuiltInRegistries.CREATIVE_MODE_TAB,
         MAIN,
         FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dabywitherstormmod.main"))
            .icon(() -> new ItemStack(ModItems.SUPER_TNT))
            .displayItems((params, out) -> {
               out.accept(ModItems.WITHER_FRAGMENT);
               out.accept(ModItems.WITHER_HEART);
               out.accept(ModItems.COMMAND_ESSENCE);
               out.accept(ModItems.COMMAND_CIRCUIT);
               out.accept(ModItems.CONTROL_PANEL);
               params.holders().lookup(Registries.ENCHANTMENT).flatMap(reg -> reg.get(ModEnchantments.GRAVITIC_DRAG)).ifPresent(drag -> {
                  for (int lvl = 1; lvl <= 3; lvl++) {
                     out.accept(EnchantmentHelper.createBook(new EnchantmentInstance(drag, lvl)));
                  }
               });

               for (Holder<Potion> potion : List.of(ModPotions.HYPER_INVISIBILITY, ModPotions.LONG_HYPER_INVISIBILITY, ModPotions.EXTENDED_HYPER_INVISIBILITY)) {
                  out.accept(PotionContents.createItemStack(Items.POTION, potion));
                  out.accept(PotionContents.createItemStack(Items.SPLASH_POTION, potion));
                  out.accept(PotionContents.createItemStack(Items.LINGERING_POTION, potion));
               }

               out.accept(ModItems.SUPER_TNT);
               out.accept(ModItems.FORMIDIBOMB);
               out.accept(ModItems.ROCKET_RETRIEVER);
               out.accept(ModItems.GRAPPLE);
               out.accept(ModItems.FURNACE_FILTER);
               out.accept(ModItems.WITHERED_FLESH_BLOCK);
               out.accept(ModItems.TORN_WITHERED_FLESH);
               out.accept(ModItems.WITHERED_MUSHROOM);
               out.accept(ModItems.WITHERED_DUST);
               out.accept(ModItems.WITHERED_STONE);
               out.accept(ModItems.WITHERED_STONE_STAIRS);
               out.accept(ModItems.WITHERED_STONE_SLAB);
               out.accept(ModItems.AMULET_BRIDGES);
               out.accept(ModItems.AMULET_WUSSMODE);
               out.accept(ModItems.WITHERED_NETHER_STAR);
               out.accept(ModItems.WITHERED_SAND);
               out.accept(ModItems.WITHERED_BEDROCK);
               out.accept(ModItems.WITHERED_COBBLESTONE);
               out.accept(ModItems.WITHERED_COBBLESTONE_STAIRS);
               out.accept(ModItems.WITHERED_COBBLESTONE_SLAB);
               out.accept(ModItems.WITHERED_COBBLESTONE_WALL);
               out.accept(ModItems.WITHERED_NETHERBRICK);
               out.accept(ModItems.WITHERED_NETHERBRICK_STAIRS);
               out.accept(ModItems.WITHERED_NETHERBRICK_SLAB);
               out.accept(ModItems.WITHERED_NETHERBRICK_WALL);
               out.accept(ModItems.WITHERED_NETHERBRICK_FENCE);
               out.accept(ModItems.WITHERED_LOG);
               out.accept(ModItems.WITHERED_PLANKS);
               out.accept(ModItems.WITHERED_STAIRS);
               out.accept(ModItems.WITHERED_SLAB);
               out.accept(ModItems.WITHERED_FENCE);
               out.accept(ModItems.WITHERED_BUTTON);
               out.accept(ModItems.STRIPPED_WITHERED_LOG);
               out.accept(ModItems.STRIPPED_WITHERED_PLANKS);
               out.accept(ModItems.STRIPPED_WITHERED_STAIRS);
               out.accept(ModItems.STRIPPED_WITHERED_SLAB);
               out.accept(ModItems.STRIPPED_WITHERED_FENCE);
               out.accept(ModItems.STRIPPED_WITHERED_BUTTON);
            })
            .build()
      );
   }
}
