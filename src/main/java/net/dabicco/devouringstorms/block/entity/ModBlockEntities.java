package net.dabicco.devouringstorms.block.entity;

import net.dabicco.devouringstorms.DevouringStormsMod;
import net.dabicco.devouringstorms.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
   public static final BlockEntityType<FurnaceFilterBlockEntity> FURNACE_FILTER;

   public static void initialize() {
   }

   static {
      FURNACE_FILTER = (BlockEntityType)Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, DevouringStormsMod.id("furnace_filter"), FabricBlockEntityTypeBuilder.create(FurnaceFilterBlockEntity::new, new Block[]{ModBlocks.FURNACE_FILTER}).build());
   }
}
