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
 * The regular place he originated from
 * 
 * Transition: you can't see it for a few seconds - blindness/darkness effect during reentry
 * This makes it feel like traveling between realities, not just teleport
 */
public final class InnerSpaceTransition {

    private InnerSpaceTransition() {}

    /**
     * Triggers when player falls below UNKNOWN_BOTTOM (-3200)
     * Teleports them back to overworld from sky with blackout effect
     */
    public static void triggerReturnToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        // Save original overworld position or use spawn / last death / current overworld coords
        // For now, use player's respawn or world spawn, but high in sky to simulate falling from sky
        BlockPos spawnPos = overworld.getSharedSpawnPos();
        // If player has bed respawn, use that
        BlockPos respawn = player.getRespawnPosition();
        if (respawn != null) {
            spawnPos = respawn;
        }

        // Teleport to sky above overworld - falling directly from sky right back to overworld
        double skyY = overworld.getMaxBuildHeight() + 300; // 600+ high, falling from sky
        double x = spawnPos.getX() + 0.5;
        double z = spawnPos.getZ() + 0.5;

        // Store velocity for falling
        Vec3 fallVelocity = new Vec3(
            (player.getRandom().nextDouble() - 0.5) * 0.5,
            -1.5, // falling down
            (player.getRandom().nextDouble() - 0.5) * 0.5
        );

        // Apply blindness - you can't see it for a few seconds during transition
        // This is the "A little bit that's where you can't see it" effect
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false, false)); // 5 seconds can't see
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, false, false)); // darkness for Sculk-like fade
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false, false)); // slow fall from sky
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false, false)); // nausea for reality shift

        // Teleport
        player.teleportTo(overworld, x, skyY, z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(fallVelocity);
        player.hasImpulse = true;
        player.fallDistance = 0;

        // Sounds - reality tearing, then wind falling
        overworld.playSound(null, BlockPos.containing(x, skyY, z),
            SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1f, 0.5f);
        overworld.playSound(null, BlockPos.containing(x, skyY, z),
            SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1f, 0.6f);

        // Particles - portal burst at reentry point
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

        // Message - world right back level
        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal("§5§lReality stitched back... You fell from Inner Space to Overworld").withStyle(s -> s.withColor(0xFFAA55FF)),
            false
        );

        // Trigger client blackout overlay
        if (overworld.isClientSide) {
            // Client will handle via FabricDistortionRenderer blackout
        }
    }

    /**
     * Called when player enters Unknown bottom fabric and falls through
     * Adds intermediate blackout before Inner Space
     */
    public static void triggerEnterUnknown(ServerPlayer player) {
        // When breakthrough rainbow fabric at bottom of Sift, end up in Unknown
        // Can't see for few seconds during that transition too
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, false)); // 3 sec
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false, false));
        
        ServerLevel level = player.server.getLevel(McsmSiftDimension.SIFT_DIMENSION);
        if (level == null) level = (ServerLevel) player.level();
        
        level.playSound(null, player.blockPosition(),
            SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 1f, 0.3f);
    }

    /**
     * Called on Fabric top when entering Sift for first time
     * Also has blackout - falling through void you can't see stars for a sec then they appear
     */
    public static void triggerEnterSift(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false, false)); // 2 sec
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 800, 0, false, false, false)); // 40 sec slow fall for fireworks
    }
}
