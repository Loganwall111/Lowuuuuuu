package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * V2 Phase 5 - Tech furniture blocks computers/90s terminals/radios
 * Places functional furniture in cities and time castles
 */
public final class McsmTechFurniture {

    public enum FurnitureType {
        COMPUTER_90S("Computer 90s", "loom"),
        TERMINAL_90S("90s Terminal", "smithing_table"),
        RADIO("Radio", "jukebox"),
        TV_90S("90s TV", "target"),
        COMPUTER_MODERN("Modern Computer", "blast_furnace"),
        SERVER_RACK("Server Rack", "iron_block"),
        DESK("Desk", "oak_planks"),
        CHAIR("Chair", "oak_stairs"),
        FRIDGE("Fridge", "white_concrete"),
        MICROWAVE("Microwave", "smoker");

        public final String display;
        public final String blockId;
        FurnitureType(String display, String blockId) {
            this.display = display;
            this.blockId = blockId;
        }
    }

    private McsmTechFurniture() {}

    public static void boot() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register(level -> tick(level));
        } catch (Throwable ignored) {}
    }

    private static void tick(ServerLevel level) {
        try {
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (!dim.contains("overworld") && !dim.equals("minecraft:overworld")) return;
            // Furniture is placed during city generation, not ticking much
            // But we can add interactive sounds for radios near players
            if (level.getGameTime() % 100 != 0) return;

            for (ServerPlayer player : level.players()) {
                if (player == null) continue;
                BlockPos ppos = player.blockPosition();
                for (int dx = -8; dx <= 8; dx++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        for (int dz = -8; dz <= 8; dz++) {
                            BlockPos check = ppos.offset(dx, dy, dz);
                            BlockState state = level.getBlockState(check);
                            if (state.is(Blocks.JUKEBOX) || state.is(Blocks.LOOM) || state.is(Blocks.SMITHING_TABLE)) {
                                // Radio/computer ambience
                                if (Math.random() < 0.02) {
                                    if (state.is(Blocks.JUKEBOX)) {
                                        level.playSound(null, check.getX(), check.getY(), check.getZ(), net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BIT, net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 0.8f + (float)Math.random() * 0.4f);
                                    } else if (state.is(Blocks.LOOM) || state.is(Blocks.SMITHING_TABLE)) {
                                        level.playSound(null, check.getX(), check.getY(), check.getZ(), net.minecraft.sounds.SoundEvents.NOTE_BLOCK_HARP, net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, 1.2f);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Throwable ignored) {}
    }

    public static void placeFurniture(ServerLevel level, BlockPos pos, FurnitureType type) {
        try {
            switch (type) {
                case COMPUTER_90S -> level.setBlock(pos, Blocks.LOOM.defaultBlockState(), 2);
                case TERMINAL_90S -> level.setBlock(pos, Blocks.SMITHING_TABLE.defaultBlockState(), 2);
                case RADIO -> level.setBlock(pos, Blocks.JUKEBOX.defaultBlockState(), 2);
                case TV_90S -> level.setBlock(pos, Blocks.TARGET.defaultBlockState(), 2);
                case COMPUTER_MODERN -> level.setBlock(pos, Blocks.BLAST_FURNACE.defaultBlockState(), 2);
                case SERVER_RACK -> level.setBlock(pos, Blocks.IRON_BLOCK.defaultBlockState(), 2);
                case DESK -> level.setBlock(pos, Blocks.OAK_PLANKS.defaultBlockState(), 2);
                case CHAIR -> level.setBlock(pos, Blocks.OAK_STAIRS.defaultBlockState(), 2);
                case FRIDGE -> level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                case MICROWAVE -> level.setBlock(pos, Blocks.SMOKER.defaultBlockState(), 2);
            }
            // Add sign with label above
            if (Math.random() < 0.5) {
                BlockPos signPos = pos.above();
                if (level.getBlockState(signPos).isAir()) {
                    level.setBlock(signPos, Blocks.OAK_SIGN.defaultBlockState(), 2);
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void populateRoomWithFurniture(ServerLevel level, BlockPos roomCenter, int roomWidth, int roomDepth) {
        try {
            // Place furniture along walls - full of life
            FurnitureType[] types = FurnitureType.values();
            for (int i = 0; i < 3 + (int)(Math.random() * 4); i++) {
                int x = roomCenter.getX() + (int)((Math.random() - 0.5) * (roomWidth - 2));
                int z = roomCenter.getZ() + (int)((Math.random() - 0.5) * (roomDepth - 2));
                int y = roomCenter.getY();
                BlockPos fpos = new BlockPos(x, y, z);
                if (level.getBlockState(fpos).isAir() && level.getBlockState(fpos.below()).isSolid()) {
                    FurnitureType type = types[(int)(Math.random() * types.length)];
                    placeFurniture(level, fpos, type);
                }
            }
        } catch (Throwable ignored) {}
    }
}
