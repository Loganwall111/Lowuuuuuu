package net.mcsm.sift.world;

import net.mcsm.sift.McsmSiftMod;
import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.util.RandomSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Dimension handling - Sift attached to void itself like all the way down below bedrock
 * MERGED VOID: second dimension directly underneath overworld hundreds blocks down
 * Skybox merges slowly to color of that area, no loading screen
 * Pocket dimension flag: completely disable suffocating in void entirely
 * FIX 7000.0.11-M: Make Sift reachable fast - 20 min bug fix
 */
@Mod.EventBusSubscriber
public final class McsmSiftDimension {

    public static final ResourceKey<Level> SIFT_DIMENSION = ResourceKey.create(Registries.DIMENSION,
        new ResourceLocation("mcsm", "sift"));

    public static final ResourceKey<DimensionType> SIFT_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
        new ResourceLocation("mcsm", "sift_type"));

    private McsmSiftDimension() {}

    // --- NO SUFFOCATION IN VOID - pocket dimension flag ---
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        int y = (int) entity.getY();
        if (!McsmVoidTiers.isInVoidDimension(y)) return;

        if (McsmVoidTiers.VOID_IS_POCKET_DIMENSION || McsmVoidTiers.DISABLE_VOID_SUFFOCATION) {
            var source = event.getSource();
            if (source.is(DamageTypes.IN_WALL)) {
                event.setCanceled(true);
                return;
            }
            String msgId = source.getMsgId();
            if (msgId.equals("inWall") || msgId.equals("in_wall") || msgId.contains("wall")) {
                event.setCanceled(true);
                return;
            }
            if (McsmVoidTiers.DISABLE_VOID_DROWN && (msgId.equals("drown") || source.is(DamageTypes.DROWN))) {
                event.setCanceled(true);
                return;
            }
            if (McsmVoidTiers.DISABLE_VOID_FALL_DAMAGE && (msgId.equals("fall") || source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL))) {
                event.setCanceled(true);
                entity.fallDistance = 0;
                return;
            }
            if (msgId.equals("outOfWorld") || source.is(DamageTypes.OUT_OF_WORLD) || source.is(DamageTypes.OUTSIDE_BORDER)) {
                event.setCanceled(true);
                if (entity instanceof ServerPlayer sp) {
                    InnerSpaceTransition.triggerReturnToOverworld(sp);
                }
                return;
            }
        }
    }

    // --- OVERWORLD MERGED VOID GENERATION ---
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        BlockPos chunkOrigin = new BlockPos(chunkX * 16, level.getMinBuildHeight(), chunkZ * 16);

        try {
            generateMergedVoidInChunk(level, chunkOrigin);
        } catch (Exception e) {
            System.err.println("[MCSM Sift] Failed to generate merged void at " + chunkOrigin + ": " + e.getMessage());
        }
    }

    public static void generateMergedVoidInChunk(ServerLevel level, BlockPos chunkOrigin) {
        RandomSource rng = RandomSource.create(chunkOrigin.asLong() ^ level.getSeed());
        PerlinNoise crackNoise = PerlinNoise.create(rng, -2, 1);
        PerlinNoise spongeNoise = PerlinNoise.create(RandomSource.create(rng.nextLong()), -3, 1);
        PerlinNoise tunnelNoise = PerlinNoise.create(RandomSource.create(rng.nextLong()), -2, 1);

        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = startX + x;
                int wz = startZ + z;

                // FABRIC TOP -64 to -200 - infinite gigantic ground
                for (int y = McsmVoidTiers.FABRIC_TOP; y >= McsmVoidTiers.FABRIC_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    BlockState current = level.getBlockState(pos);
                    if (y > -100 && !current.is(Blocks.BEDROCK) && !current.is(Blocks.DEEPSLATE) && !current.isAir() && !current.is(Blocks.STONE)) {
                        continue;
                    }
                    double crack = crackNoise.getValue(wx * 0.05, y * 0.05, wz * 0.05);
                    double stars = crackNoise.getValue(wx * 0.1, y * 0.1 + 100, wz * 0.1);
                    boolean isCrack = crack > 0.3 || stars > 0.6;
                    boolean isBroken = isCrack && rng.nextFloat() < 0.12f;
                    if (Math.abs(wx) < 100 && Math.abs(wz) < 100 && rng.nextFloat() < 0.02f) isBroken = true;

                    if (isBroken) {
                        level.setBlock(pos, McsmSiftMod.BROKEN_FABRIC.get().defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                    }
                }

                // EMPTINESS -200 to -600 - NOW WITH VISIBLE MARKERS so player knows they're moving
                for (int y = McsmVoidTiers.EMPTINESS_TOP - 1; y >= McsmVoidTiers.EMPTINESS_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    // 99.5% air but with floating markers every 20 blocks to show progress
                    if (y % 20 == 0 && rng.nextFloat() < 0.05f) {
                        // Floating fabric island marker - shows you're in emptiness and moving
                        level.setBlock(pos, McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                        continue;
                    }
                    if (rng.nextFloat() < 0.998f) {
                        if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        continue;
                    }
                    if (rng.nextFloat() < 0.005f) {
                        level.setBlock(pos, McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                    }
                }

                // TIER 1 GEL HORIZON -600 to -900
                for (int y = McsmVoidTiers.TIER_1_GEL_HORIZON_TOP; y >= McsmVoidTiers.TIER_1_GEL_HORIZON_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (rng.nextFloat() < 0.88f) {
                        if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        continue;
                    }
                    if (rng.nextFloat() < 0.20f) {
                        level.setBlock(pos, McsmSiftMod.IRIDESCENT_GEL.get().defaultBlockState(), 2);
                    } else if (rng.nextFloat() < 0.12f) {
                        try {
                            level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.BLUE_GRASS_BLOCK.get().defaultBlockState(), 2);
                        } catch (Exception e) {
                            level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                        }
                    }
                }

                // TIER 2 MENGER MAZE -900 to -1300 - SIFT AREA - dense orange->pink gradient
                for (int y = McsmVoidTiers.TIER_2_SPONGE_MAZE_TOP; y >= McsmVoidTiers.TIER_2_SPONGE_MAZE_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (y < McsmVoidTiers.TIER_2_PHYSICAL_MAZE_START) {
                        boolean solid = isMengerSolid(wx, y, wz, 3);
                        double nx = wx * 0.03 + chunkOrigin.asLong() * 0.001;
                        double ny = y * 0.03;
                        double nz = wz * 0.03;
                        double n1 = spongeNoise.getValue(nx, ny, nz);
                        double n2 = tunnelNoise.getValue(nx * 1.5, ny * 0.8, nz * 1.5);
                        boolean carved = (n1 + n2 * 0.6) > 0.10; // More open, easier to navigate
                        if (solid && carved) {
                            float depthFactor = (float)(McsmVoidTiers.TIER_2_SPONGE_MAZE_TOP - y) / (McsmVoidTiers.TIER_2_SPONGE_MAZE_TOP - McsmVoidTiers.TIER_2_SPONGE_MAZE_BOTTOM);
                            BlockState state = getMazeBlockForDepth(depthFactor, rng);
                            level.setBlock(pos, state, 2);
                        } else {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    } else {
                        if (rng.nextFloat() < 0.90f) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pos, McsmSiftMod.MENGER_SPONGE.get().defaultBlockState(), 2);
                        }
                    }
                }

                // TIER 3 RIFT FIELD -1300 to -1500
                for (int y = McsmVoidTiers.TIER_3_RIFT_FIELD_TOP; y >= McsmVoidTiers.TIER_3_RIFT_FIELD_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (rng.nextFloat() < 0.95f) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (rng.nextFloat() < 0.08f) {
                        level.setBlock(pos, McsmSiftMod.RIFT_COSMIC.get().defaultBlockState(), 2);
                    }
                }

                // TIER 4 DISPLACEMENT -1500 to -1700
                for (int y = McsmVoidTiers.TIER_4_DISPLACEMENT_TOP; y >= McsmVoidTiers.TIER_4_DISPLACEMENT_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (rng.nextFloat() < 0.96f) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 2);
                    }
                }

                // TIER 5 IRIDESCENT GEL -1700 to -1900
                for (int y = McsmVoidTiers.TIER_5_GEL_VOID_TOP; y >= McsmVoidTiers.TIER_5_GEL_VOID_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (rng.nextFloat() < 0.80f) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, McsmSiftMod.IRIDESCENT_GEL.get().defaultBlockState(), 2);
                    }
                }

                // BOTTOM FABRIC -1900 to -2000
                for (int y = McsmVoidTiers.BOTTOM_FABRIC_TOP; y >= McsmVoidTiers.BOTTOM_FABRIC_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    double crack = crackNoise.getValue(wx * 0.03, y * 0.03, wz * 0.03);
                    boolean isBroken = crack > 0.2 || rng.nextFloat() < 0.30f;
                    if (isBroken) {
                        level.setBlock(pos, McsmSiftMod.BROKEN_FABRIC.get().defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, McsmSiftMod.BOTTOM_FABRIC.get().defaultBlockState(), 2);
                    }
                }

                // UNKNOWN -2000 to -2032
                for (int y = McsmVoidTiers.UNKNOWN_TOP; y >= McsmVoidTiers.UNKNOWN_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (rng.nextFloat() < 0.65f) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        try {
                            level.setBlock(pos, McsmSiftMod.UNKNOWN_GROUND.get().defaultBlockState(), 2);
                        } catch (Exception e) {
                            level.setBlock(pos, Blocks.SCULK.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    private static boolean isMengerSolid(int x, int y, int z, int iterations) {
        int localX = Math.floorMod(x, 81);
        int localY = Math.floorMod(y, 81);
        int localZ = Math.floorMod(z, 81);
        for (int i = 0; i < iterations; i++) {
            int scale = (int) Math.pow(3, iterations - i - 1);
            int ix = localX / scale % 3;
            int iy = localY / scale % 3;
            int iz = localZ / scale % 3;
            int centerCount = 0;
            if (ix == 1) centerCount++;
            if (iy == 1) centerCount++;
            if (iz == 1) centerCount++;
            if (centerCount >= 2) return false;
        }
        return true;
    }

    private static BlockState getMazeBlockForDepth(float depth, RandomSource rng) {
        if (depth < 0.2f) return Blocks.ORANGE_CONCRETE.defaultBlockState();
        if (depth < 0.4f) return Blocks.ORANGE_TERRACOTTA.defaultBlockState();
        if (depth < 0.6f) return Blocks.PINK_CONCRETE.defaultBlockState();
        if (depth < 0.8f) return Blocks.MAGENTA_CONCRETE.defaultBlockState();
        return Blocks.PINK_TERRACOTTA.defaultBlockState();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        int y = (int) player.getY();
        Level lvl = player.level();

        if (McsmVoidTiers.isInVoidDimension(y)) {
            player.fallDistance = 0;
            if (McsmVoidTiers.DISABLE_VOID_SUFFOCATION) {
                if (player.isInWall()) {
                    player.setDeltaMovement(player.getDeltaMovement().add(0, 0.3, 0));
                    player.hasImpulse = true;
                }
            }
        }

        if (McsmVoidTiers.shouldTriggerInnerSpace(y)) {
            if (player instanceof ServerPlayer sp) {
                InnerSpaceTransition.triggerReturnToOverworld(sp);
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.BLINDNESS, 100, 0, false, false, false));
            }
            return;
        }

        if (McsmVoidTiers.shouldWrap(y)) {
            var result = McsmVoidTiers.performWrapWithHandshake(player);
            result.applyTo(player);
            if (player instanceof ServerPlayer sp) {
                sp.fallDistance = 0;
            }
        }

        if (y == McsmVoidTiers.FABRIC_BOTTOM - 1 || y == McsmVoidTiers.FABRIC_BOTTOM - 2) {
            if (player instanceof ServerPlayer sp) {
                InnerSpaceTransition.triggerEnterSift(sp);
            }
        }

        if (y == McsmVoidTiers.BOTTOM_FABRIC_BOTTOM - 1) {
            if (player instanceof ServerPlayer sp) {
                InnerSpaceTransition.triggerEnterUnknown(sp);
            }
        }

        if (y >= McsmVoidTiers.TIER_5_GEL_VOID_TOP && y <= McsmVoidTiers.TIER_5_GEL_VOID_BOTTOM) {
            if (player.isInWater() || player.isInFluidType()) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(0.9, 0.8, 0.9));
            }
        }

        if (McsmVoidTiers.isInUnknown(y)) {
            if (player.onGround() && player.getDeltaMovement().y < 0.1) {
                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.5, 0));
                player.hasImpulse = true;
                if (lvl instanceof ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                        player.getX(), player.getY(), player.getZ(),
                        10, 0.3, 0.1, 0.3, 0.2);
                }
            }
        }

        // FIX for 20-min fall bug: ensure chunks generate around player every tick when in void
        // Also make fall faster in emptiness instead of slow falling
        if (McsmVoidTiers.isInVoidDimension(y)) {
            if (lvl instanceof ServerLevel sLevel && lvl.dimension() == Level.OVERWORLD && player instanceof ServerPlayer sp) {
                // Generate 3x3 chunks around player if in void, so sift is always visible
                if (player.tickCount % 20 == 0) {
                    int cx = player.blockPosition().getX() >> 4;
                    int cz = player.blockPosition().getZ() >> 4;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos origin = new BlockPos((cx+dx)*16, sLevel.getMinBuildHeight(), (cz+dz)*16);
                            try { generateMergedVoidInChunk(sLevel, origin); } catch (Exception ignored) {}
                        }
                    }
                }

                var tier = McsmVoidTiers.getTierForY(y);
                // FAST FALL in emptiness - was slow falling causing 20 min bug
                if (tier == McsmVoidTiers.Tier.EMPTINESS) {
                    if (player.getDeltaMovement().y > -2.0) {
                        player.setDeltaMovement(player.getDeltaMovement().add(0, -0.18, 0));
                        player.hasImpulse = true;
                    }
                    if (sLevel instanceof ServerLevel sl && player.tickCount % 10 == 0) {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK,
                            player.getX(), player.getY() + 2, player.getZ(),
                            3, 0.5, 0.5, 0.5, 0.1);
                    }
                }

                // Show tier title so player knows where they are
                if (player.tickCount % 40 == 0) {
                    String tierName = tier.id.toUpperCase().replace("_", " ");
                    String msg;
                    if (tier == McsmVoidTiers.Tier.EMPTINESS) {
                        int distToGel = Math.abs(y - McsmVoidTiers.TIER_1_GEL_HORIZON_TOP);
                        int distToSift = Math.abs(y - McsmVoidTiers.TIER_2_SPONGE_MAZE_TOP);
                        msg = "EMPTINESS " + y + " -> GEL at -600 (" + distToGel + "b) | SIFT at -900 (" + distToSift + "b)";
                    } else if (tier == McsmVoidTiers.Tier.FABRIC_OF_REALITY) {
                        msg = "FABRIC OF REALITY " + y + " -> EMPTINESS at -200 | SIFT at -900";
                    } else if (tier == McsmVoidTiers.Tier.TIER_1_GEL_HORIZON) {
                        int dist = Math.abs(y - McsmVoidTiers.TIER_2_SPONGE_MAZE_TOP);
                        msg = "GEL HORIZON " + y + " -> SIFT MAZE at -900 (" + dist + "b)";
                    } else if (tier == McsmVoidTiers.Tier.TIER_2_MENGER_SPONGE) {
                        msg = "*** SIFT AREA - MENGER MAZE *** " + y + " - YOU ARE IN SIFT!";
                    } else {
                        msg = tierName + " " + y + " | SIFT MAZE at -900 to -1300";
                    }
                    sp.displayClientMessage(net.minecraft.network.chat.Component.literal(msg), true);

                    if (tier == McsmVoidTiers.Tier.TIER_2_MENGER_SPONGE && player.tickCount % 200 == 0) {
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                            net.minecraft.network.chat.Component.literal("§6§lSIFT AREA").withStyle(net.minecraft.ChatFormatting.BOLD)));
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                            net.minecraft.network.chat.Component.literal("§eMenger Maze - Orange to Pink Gradient")));
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 60, 20));
                    }
                }
            }
        }
    }

    public static void ensureSiftChunkLoaded(ServerLevel level, BlockPos pos) {
        if (McsmVoidTiers.isInVoidDimension(pos.getY())) {
            if (level.dimension() == Level.OVERWORLD) {
                int cx = pos.getX() >> 4;
                int cz = pos.getZ() >> 4;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos origin = new BlockPos((cx+dx)*16, level.getMinBuildHeight(), (cz+dz)*16);
                        try { generateMergedVoidInChunk(level, origin); } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    public static boolean isInSiftDimension(Level level) {
        return level.dimension() == SIFT_DIMENSION || McsmVoidTiers.isInVoidDimension((int)level.getSeaLevel());
    }

    public static void trySpawnRealityRift(ServerLevel level, BlockPos pos) {
        if (level.random.nextFloat() < 0.001f) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
