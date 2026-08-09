package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.block.TerminalBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Block entities. */
public final class ModBlockEntities {

    public static final BlockEntityType<TerminalBlockEntity> TERMINAL = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        DevouringStorms.id("terminal"),
        BlockEntityType.Builder.of(TerminalBlockEntity::new, ModBlocks.TERMINAL).build()
    );

    private ModBlockEntities() {
    }

    public static void register() {
        // Static initialisation only.
    }
}
