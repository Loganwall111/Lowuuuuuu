package dev.siftcore.block;

import dev.siftcore.SiftCore;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Custom terrain palette used by the generated Sift floor and shelves. */
public final class SiftBlocks {
    public static final Block SIFTSTONE = register(
            "siftstone",
            new Block(FabricBlockSettings.copyOf(Blocks.DEEPSLATE).strength(2.2F).luminance(state -> 1))
    );
    public static final Block SIFT_MOSS = register(
            "sift_moss",
            new Block(FabricBlockSettings.copyOf(Blocks.MOSS_BLOCK).strength(0.7F))
    );
    public static final Block RIFT_CRYSTAL = register(
            "rift_crystal",
            new Block(FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK).strength(1.4F).luminance(state -> 7))
    );

    private SiftBlocks() {
    }

    private static Block register(String path, Block block) {
        Identifier id = SiftCore.id(path);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(SIFTSTONE);
            entries.add(SIFT_MOSS);
            entries.add(RIFT_CRYSTAL);
        });
        SiftCore.LOGGER.info("Registered Sift terrain blocks and creative inventory entries");
    }
}
