package net.mcsm.sift.world;

import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Dimension handling - Sift attached to void itself like all the way down below bedrock
 * Was meant to have these basically whole dimension is a shader
 * 
 * NEW: Inner Space - world right back level - you can't see it for a few seconds
 * When breakthrough black fabric at bottom of Unknown, fall from sky back to overworld
 */
@Mod.EventBusSubscriber
public final class McsmSiftDimension {

    public static final ResourceKey<Level> SIFT_DIMENSION = ResourceKey.create(Registries.DIMENSION,
        new ResourceLocation("mcsm", "sift"));

    public static final ResourceKey<DimensionType> SIFT_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
        new ResourceLocation("mcsm", "sift_type"));

    private McsmSiftDimension() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        int y = (int) player.getY();

        // Inner Space - world right back level - you can't see it for a few seconds
        // When you breakthrough black fabric at bottom of Unknown, fall from sky back to overworld
        if (McsmVoidTiers.shouldTriggerInnerSpace(y)) {
            if (player instanceof ServerPlayer sp) {
                InnerSpaceTransition.triggerReturnToOverworld(sp);
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.BLINDNESS, 100, 0, false, false, false));
            }
            return;
        }

        // If player engaging Void Rudder drops past absolute hardcoded floor, inject modulo
        if (McsmVoidTiers.shouldWrap(y)) {
            var result = McsmVoidTiers.performWrapWithHandshake(player);
            result.applyTo(player);

            // Dynamic Seed Shuffling - structural spawner arrays shuffle coordinate generation seeds
            // Ensures hanging spires, portals, entity paths rearrange so infinite drop never looks identical

            if (player instanceof ServerPlayer sp) {
                // Prevent vanilla void damage during wrap
                sp.fallDistance = 0;
            }
        }

        // Fabric entry - top fabric to emptiness - 40-50 sec fall
        if (y == McsmVoidTiers.FABRIC_BOTTOM - 1) {
            if (player instanceof ServerPlayer sp) {
                InnerSpaceTransition.triggerEnterSift(sp);
            }
        }

        // Bottom fabric entry to Unknown - rainbow layer
        if (y == McsmVoidTiers.BOTTOM_FABRIC_BOTTOM - 1) {
            if (player instanceof ServerPlayer sp) {
                InnerSpaceTransition.triggerEnterUnknown(sp);
            }
        }

        // Headlock - as you go deeper the headlock there start to disappear and you'll fall into entirely new world
        // Simulate void fog and effects - sky changes and god rays start to appear
        if (y < McsmVoidTiers.FABRIC_TOP && y > McsmVoidTiers.FABRIC_TOP - 30) {
            // Transition zone - fade from overworld to Sift - sky changes and god rays start to appear
            float transition = (float)(McsmVoidTiers.FABRIC_TOP - y) / 30f;
        }

        // Tier 5 gel void - zero solid collision, allow plummet cleanly through swirling pool currents
        if (y >= McsmVoidTiers.TIER_5_GEL_VOID_TOP && y <= McsmVoidTiers.TIER_5_GEL_VOID_BOTTOM) {
            if (player.isInWater() || player.isInFluidType()) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(0.9, 0.8, 0.9));
            }
        }

        // Unknown dimension - bouncy distortion, ground decay - you're not Quinn breaking through this world
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

    // Reality rifts - gigantic they're a shader they're not an actual block, dramatic opening animation
    // Basically open worlds and feel them like every been really really strange at occasions they just opened randomly
    // They take it to the void dim
    public static void trySpawnRealityRift(ServerLevel level, BlockPos pos) {
        if (level.random.nextFloat() < 0.001f) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
