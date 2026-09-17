package net.mcsm.sift;

import net.mcsm.sift.world.SiftChunkDecorator;
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
 * Engaging Void Rudder drops past absolute hardcoded floor and triggers infinite wrap
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
            if (McsmVoidTiers.isInVoidDimension(y)) {
                // Boost downward velocity - endless infinite fall
                Vec3 look = player.getLookAngle();
                Vec3 boost = new Vec3(
                    look.x * 0.5,
                    -1.2 - Mth.clamp((McsmVoidTiers.TOP_BOUNDARY - y) / 1000f, 0f, 1f), // faster deeper
                    look.z * 0.5
                );
                player.setDeltaMovement(player.getDeltaMovement().add(boost));
                player.hasImpulse = true;
                player.fallDistance = 0f;

                // Particles - rainbow trail
                if (level instanceof ServerLevel sl) {
                    for (int i = 0; i < 10; i++) {
                        double offsetX = (level.random.nextFloat() - 0.5) * 0.5;
                        double offsetY = (level.random.nextFloat() - 0.5) * 0.5;
                        double offsetZ = (level.random.nextFloat() - 0.5) * 0.5;
                        sl.sendParticles(ParticleTypes.END_ROD,
                            player.getX() + offsetX,
                            player.getY() + offsetY,
                            player.getZ() + offsetZ,
                            1, 0, 0, 0, 0.1);
                    }
                }

                level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.8f, 0.8f + level.random.nextFloat() * 0.4f);

                // Check for wrap
                if (McsmVoidTiers.shouldWrap(y - 5)) {
                    var result = McsmVoidTiers.performWrapWithHandshake(player);
                    result.applyTo(player);
                    // Shuffle seeds - ensure hanging spires, portals, entity paths dynamically rearrange
                    level.playSound(null, BlockPos.containing(result.newPos()), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1f, 0.5f);
                }
            } else if (y < 0 && y > McsmVoidTiers.TOP_BOUNDARY) {
                // Transition zone - bedrock below, start falling into Sift
                if (player.isCrouching()) {
                    // Instantly drop into void dimension - bypass standard world constraints
                    player.teleportTo(player.getX(), McsmVoidTiers.TOP_BOUNDARY - 5, player.getZ());
                    player.setDeltaMovement(new Vec3(0, -0.5, 0));
                    level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 1f, 0.3f);
                }
            }
        }

        player.getCooldowns().addCooldown(this, 5);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // enchanted glint for magical item
    }

    public static boolean isPlayerInSift(Player player) {
        return McsmVoidTiers.isInVoidDimension((int)player.getY());
    }

    public static float getFlightSpeedFactor(Player player) {
        int y = (int)player.getY();
        if (!McsmVoidTiers.isInVoidDimension(y)) return 1f;
        McsmVoidTiers.Tier tier = McsmVoidTiers.getTierForY(y);
        return switch (tier) {
            case TIER_1_GEL_HORIZON -> 1.2f; // open sky
            case TIER_2_MENGER_SPONGE -> 0.6f; // tight maze - slower for navigation
            case TIER_3_RIFT_FIELD -> 1.0f;
            case TIER_4_DISPLACEMENT -> 1.3f; // displacement bands boost
            case TIER_5_IRIDESCENT_GEL -> 0.8f; // swimming through gel
            default -> 1f;
        };
    }
}
