package net.mcsm.sift.world;

import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Dimension handling - Sift attached to void itself like all the way down below bedrock
 * Fabric version - no Forge event bus, tick called from McsmVoid
 */
public final class McsmSiftDimension {

    public static final ResourceKey<Level> SIFT_DIMENSION = ResourceKey.create(Registries.DIMENSION,
        Identifier.fromNamespaceAndPath("mcsm", "sift"));

    public static final ResourceKey<DimensionType> SIFT_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
        Identifier.fromNamespaceAndPath("mcsm", "sift_type"));

    private McsmSiftDimension() {}

    public static void tickPlayer(Player player) {
        if (player.level().isClientSide) return;
        int y = (int) player.getY();

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

        if (y == McsmVoidTiers.FABRIC_BOTTOM - 1) {
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
                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.4, 0));
                player.hasImpulse = true;
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                        player.getX(), player.getY(), player.getZ(),
                        10, 0.3, 0.1, 0.3, 0.2);
                }
            }
        }
    }

    public static void ensureSiftChunkLoaded(ServerLevel level, BlockPos pos) {
        if (McsmVoidTiers.isInVoidDimension(pos.getY())) {
            // Chunk generation handled by SiftChunkDecorator + FabricGeneration
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
