package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/**
 * V2 Phase 5 - Gigantic rockets to other planets
 * Massive rockets that launch from overworld to space, can travel to Mars/Saturn etc
 */
public final class McsmRockets {

    private static final java.util.Map<Long, Long> ROCKETS = new java.util.concurrent.ConcurrentHashMap<>();
    private static boolean builtDemo = false;

    private McsmRockets() {}

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

            if (!builtDemo && level.getGameTime() > 100) {
                // Build demo rocket near spawn once
                BlockPos spawn = new BlockPos(level.getSharedSpawnPos().getX(), level.getSharedSpawnPos().getY(), level.getSharedSpawnPos().getZ());
                int ground = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX() + 100, spawn.getZ() + 100);
                buildGiganticRocket(level, new BlockPos(spawn.getX() + 100, ground, spawn.getZ() + 100));
                builtDemo = true;
            }

            // Launch logic - if player near rocket and presses shift near button, launch
            if (level.getGameTime() % 20 != 0) return;
            for (ServerPlayer player : level.players()) {
                if (player == null) continue;
                BlockPos ppos = player.blockPosition();
                for (int dx = -10; dx <= 10; dx++) {
                    for (int dy = -5; dy <= 30; dy++) {
                        for (int dz = -10; dz <= 10; dz++) {
                            BlockPos check = ppos.offset(dx, dy, dz);
                            var state = level.getBlockState(check);
                            if (state.is(Blocks.STONE_BUTTON) || state.is(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                                // Check if near rocket base (iron blocks)
                                int ironCount = 0;
                                for (int ix = -5; ix <= 5; ix++) {
                                    for (int iz = -5; iz <= 5; iz++) {
                                        if (level.getBlockState(check.offset(ix, -1, iz)).is(Blocks.IRON_BLOCK)) ironCount++;
                                    }
                                }
                                if (ironCount > 10 && player.isShiftKeyDown() && level.getGameTime() % 100 == 0) {
                                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c§l[ROCKET] §fLaunch sequence initiated! §7Destination: §eMars §7| §a3...2...1..."));
                                    launchRocket(level, check);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Throwable ignored) {}
    }

    public static void buildGiganticRocket(ServerLevel level, BlockPos base) {
        try {
            int height = 48; // gigantic
            int radius = 4;

            // Launch pad
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    BlockPos pos = base.offset(x, 0, z);
                    if (Math.abs(x) <= 6 && Math.abs(z) <= 6) {
                        level.setBlock(pos, Blocks.IRON_BLOCK.defaultBlockState(), 2);
                    } else if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                        level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 2);
                    }
                }
            }

            // Tower scaffolding
            for (int y = 1; y < height + 10; y++) {
                BlockPos pos1 = base.offset(-7, y, -7);
                BlockPos pos2 = base.offset(7, y, -7);
                BlockPos pos3 = base.offset(-7, y, 7);
                BlockPos pos4 = base.offset(7, y, 7);
                if (y % 3 == 0) {
                    level.setBlock(pos1, Blocks.SCAFFOLDING.defaultBlockState(), 2);
                    level.setBlock(pos2, Blocks.SCAFFOLDING.defaultBlockState(), 2);
                    level.setBlock(pos3, Blocks.SCAFFOLDING.defaultBlockState(), 2);
                    level.setBlock(pos4, Blocks.SCAFFOLDING.defaultBlockState(), 2);
                }
            }

            // Rocket body
            for (int y = 1; y < height; y++) {
                int wy = base.getY() + y;
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        double dist = Math.sqrt(x*x + z*z);
                        if (dist <= radius + 0.5) {
                            BlockPos pos = new BlockPos(base.getX() + x, wy, base.getZ() + z);
                            if (dist <= radius - 0.5) {
                                // Interior hollow
                                if (y % 8 == 0) {
                                    level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 2);
                                }
                            } else {
                                // Hull
                                if (y < 5) {
                                    // Engine bells
                                    level.setBlock(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                                } else if (y > height - 8) {
                                    // Nose cone
                                    if (dist <= radius - 1) {
                                        level.setBlock(pos, Blocks.RED_CONCRETE.defaultBlockState(), 2);
                                    } else {
                                        level.setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
                                    }
                                } else {
                                    if (Math.random() < 0.1) {
                                        level.setBlock(pos, Blocks.RED_CONCRETE.defaultBlockState(), 2);
                                    } else {
                                        level.setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fins
            for (int y = 1; y < 12; y++) {
                for (int f = -2; f <= 2; f++) {
                    level.setBlock(base.offset(-radius - 1 - f, y, 0), Blocks.IRON_BLOCK.defaultBlockState(), 2);
                    level.setBlock(base.offset(radius + 1 + f, y, 0), Blocks.IRON_BLOCK.defaultBlockState(), 2);
                    level.setBlock(base.offset(0, y, -radius - 1 - f), Blocks.IRON_BLOCK.defaultBlockState(), 2);
                    level.setBlock(base.offset(0, y, radius + 1 + f), Blocks.IRON_BLOCK.defaultBlockState(), 2);
                }
            }

            // Button to launch
            level.setBlock(base.offset(3, 1, 3), Blocks.STONE_BUTTON.defaultBlockState(), 2);
            level.setBlock(base.offset(0, 1, 0), Blocks.BEACON.defaultBlockState(), 2);

        } catch (Throwable ignored) {}
    }

    private static void launchRocket(ServerLevel level, BlockPos buttonPos) {
        try {
            BlockPos base = buttonPos.offset(-3, -1, -3);
            // Visual launch - particles + sound + remove rocket and spawn falling
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5, 10, 1, 1, 1, 0.2);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE, base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5, 30, 1, 2, 1, 0.1);
            level.playSound(null, base, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.BLOCKS, 2.0f, 0.5f);

            // In full version, this would launch entity to space dimension
            // For now, just message about traveling to other planets
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(base.getX(), base.getY(), base.getZ()) < 10000) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a§lROCKET LAUNCHED! §fTraveling to §cMars §f... §7Orbital planets visible... §8Saturn rings ahead..."));
                }
            }

        } catch (Throwable ignored) {}
    }
}
