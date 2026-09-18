package net.mcsm.sift;

import net.mcsm.sift.world.McsmSiftDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Void Rudder - item that allows high-speed navigation through Sift
 * MERGED VOID: second dimension directly underneath overworld hundreds blocks down
 * Fix suffocation glitch when holding Void Rudder breaking underneath
 * 7000.0.11-M FIX: clear blocks below to prevent suffocation glitch - pocket dimension flag disables suffocating entirely
 * This fix ensures holding rudder and breaking underneath clears 3x3x4 area and prevents suffocation
 */
public class McsmVoidRudder extends Item {

    public McsmVoidRudder(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int y = (int) player.getY();
            if (McsmVoidTiers.isInVoidDimension(y) || y <= McsmVoidTiers.FABRIC_TOP) {
                BlockPos playerPos = player.blockPosition();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dy = -1; dy >= -4; dy--) {
                            BlockPos below = playerPos.offset(dx, dy, dz);
                            var state = level.getBlockState(below);
                            if (state.is(McsmSiftMod.FABRIC_OF_REALITY.get()) || state.is(McsmSiftMod.MENGER_SPONGE.get()) ||
                                state.is(net.minecraft.world.level.block.Blocks.BEDROCK) || state.getBlock() instanceof net.mcsm.sift.block.FabricOfRealityBlock) {
                                if (player.getLookAngle().y < -0.2 || player.isCrouching() || y <= McsmVoidTiers.FABRIC_BOTTOM) {
                                    level.destroyBlock(below, false);
                                    if (level instanceof ServerLevel sl) {
                                        sl.sendParticles(ParticleTypes.PORTAL,
                                            below.getX() + 0.5, below.getY() + 0.5, below.getZ() + 0.5,
                                            5, 0.2, 0.2, 0.2, 0.1);
                                    }
                                }
                            }
                        }
                    }
                }

                Vec3 look = player.getLookAngle();
                float depthFactor = Mth.clamp((McsmVoidTiers.FABRIC_TOP - y) / 1500f, 0f, 2f);
                Vec3 boost = new Vec3(
                    look.x * 0.5,
                    -1.5 - depthFactor,
                    look.z * 0.5
                );
                if (look.y < -0.3) {
                    boost = boost.add(0, look.y * 1.5, 0);
                }
                player.setDeltaMovement(player.getDeltaMovement().add(boost));
                player.hasImpulse = true;
                player.fallDistance = 0f;

                if (level instanceof ServerLevel sl) {
                    for (int i = 0; i < 12; i++) {
                        double offsetX = (level.random.nextFloat() - 0.5) * 0.5;
                        double offsetY = (level.random.nextFloat() - 0.5) * 0.5;
                        double offsetZ = (level.random.nextFloat() - 0.5) * 0.5;
                        sl.sendParticles(ParticleTypes.END_ROD,
                            player.getX() + offsetX,
                            player.getY() + offsetY,
                            player.getZ() + offsetZ,
                            1, 0, 0, 0, 0.1);
                        if (i % 3 == 0) {
                            sl.sendParticles(ParticleTypes.PORTAL,
                                player.getX() + offsetX,
                                player.getY() + offsetY - 1,
                                player.getZ() + offsetZ,
                                1, 0, -0.5, 0, 0.2);
                        }
                    }
                    if (y < McsmVoidTiers.EMPTINESS_TOP && y > McsmVoidTiers.EMPTINESS_BOTTOM) {
                        for (int i = 0; i < 20; i++) {
                            double px = player.getX() + (level.random.nextDouble() - 0.5) * 20;
                            double py = player.getY() + (level.random.nextDouble() - 0.5) * 10;
                            double pz = player.getZ() + (level.random.nextDouble() - 0.5) * 20;
                            sl.sendParticles(ParticleTypes.FIREWORK,
                                px, py, pz,
                                1, 0, 0.5, 0, 0.1);
                        }
                    }
                }

                level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.8f, 0.8f + level.random.nextFloat() * 0.4f);

                if (McsmVoidTiers.shouldWrap(y - 5)) {
                    var result = McsmVoidTiers.performWrapWithHandshake(player);
                    result.applyTo(player);
                    level.playSound(null, BlockPos.containing(result.newPos()), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1f, 0.5f);
                }

                if (level instanceof ServerLevel sl) {
                    BlockPos belowCheck = playerPos.below(10);
                    if (sl.getBlockState(belowCheck).isAir() && y < McsmVoidTiers.EMPTINESS_TOP && y > McsmVoidTiers.TIER_2_SPONGE_MAZE_TOP) {
                        if (sl.random.nextFloat() < 0.1f) {
                            McsmSiftDimension.ensureSiftChunkLoaded(sl, belowCheck);
                        }
                    }
                }
            } else if (y < 0 && y > McsmVoidTiers.FABRIC_TOP - 50) {
                if (player.isCrouching() || player.getLookAngle().y < -0.5) {
                    BlockPos pos = player.blockPosition().below();
                    var state = level.getBlockState(pos);
                    if (state.is(McsmSiftMod.FABRIC_OF_REALITY.get()) || state.is(net.minecraft.world.level.block.Blocks.BEDROCK)) {
                        level.destroyBlock(pos, false);
                    }
                    player.setDeltaMovement(new Vec3(0, -0.8, 0));
                    player.hasImpulse = true;
                    level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 1f, 0.3f);
                }
            }
        }

        player.getCooldowns().addCooldown(this, 3);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public static boolean isPlayerInSift(Player player) {
        return McsmVoidTiers.isInVoidDimension((int)player.getY());
    }

    public static float getFlightSpeedFactor(Player player) {
        int y = (int)player.getY();
        if (!McsmVoidTiers.isInVoidDimension(y)) return 1f;
        McsmVoidTiers.Tier tier = McsmVoidTiers.getTierForY(y);
        return switch (tier) {
            case TIER_1_GEL_HORIZON -> 1.2f;
            case TIER_2_MENGER_SPONGE -> 0.6f;
            case TIER_3_RIFT_FIELD -> 1.0f;
            case TIER_4_DISPLACEMENT -> 1.3f;
            case TIER_5_IRIDESCENT_GEL -> 0.8f;
            default -> 1f;
        };
    }
}
