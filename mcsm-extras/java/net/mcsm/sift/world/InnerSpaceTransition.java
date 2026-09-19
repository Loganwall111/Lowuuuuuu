package net.mcsm.sift.world;

import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Inner Space - world right back level
 * When you breakthrough black fabric at bottom of Unknown, you end up falling directly from sky right back to overworld
 */
public final class InnerSpaceTransition {

    private InnerSpaceTransition() {}

    public static void triggerReturnToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        BlockPos spawnPos = overworld.getSharedSpawnPos();
        BlockPos respawn = player.getRespawnPosition();
        if (respawn != null) {
            spawnPos = respawn;
        }

        double skyY = overworld.getMaxBuildHeight() + 300;
        double x = spawnPos.getX() + 0.5;
        double z = spawnPos.getZ() + 0.5;

        Vec3 fallVelocity = new Vec3(
            (player.getRandom().nextDouble() - 0.5) * 0.5,
            -1.5,
            (player.getRandom().nextDouble() - 0.5) * 0.5
        );

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false, false));

        player.teleportTo(overworld, x, skyY, z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(fallVelocity);
        player.hasImpulse = true;
        player.fallDistance = 0;

        overworld.playSound(null, BlockPos.containing(x, skyY, z),
            SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1f, 0.5f);
        overworld.playSound(null, BlockPos.containing(x, skyY, z),
            SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1f, 0.6f);

        overworld.sendParticles(
            net.minecraft.core.particles.ParticleTypes.PORTAL,
            x, skyY - 5, z,
            100, 1, 1, 1, 0.5
        );
        overworld.sendParticles(
            net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
            x, skyY - 10, z,
            50, 0.5, 0.5, 0.5, 0.2
        );

        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal("§5§lReality stitched back... You fell from Inner Space to Overworld").withStyle(s -> s.withColor(0xFFAA55FF)),
            false
        );
    }

    public static void triggerEnterUnknown(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false, false));
        
        ServerLevel level = player.server.getLevel(McsmSiftDimension.SIFT_DIMENSION);
        if (level == null) level = (ServerLevel) player.level();
        
        level.playSound(null, player.blockPosition(),
            SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 1f, 0.3f);
    }

    public static void triggerEnterSift(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1000, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, false));

        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.PORTAL,
            player.getX(), player.getY(), player.getZ(),
            100, 0.5, 1, 0.5, 0.3
        );
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.END_ROD,
            player.getX(), player.getY() - 2, player.getZ(),
            50, 0.5, 0.5, 0.5, 0.1
        );

        level.playSound(null, player.blockPosition(),
            SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1f, 0.3f);
        level.playSound(null, player.blockPosition(),
            SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1f, 0.5f);

        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal("§5§lFalling through Fabric... The multiverse awaits").withStyle(s -> s.withColor(0xFFAA55FF)),
            true
        );

        player.setDeltaMovement(player.getDeltaMovement().add(0, -1.0, 0));
        player.hasImpulse = true;
        player.fallDistance = 0;
    }
}
