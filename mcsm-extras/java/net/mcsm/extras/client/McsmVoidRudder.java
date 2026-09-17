package net.mcsm.extras.client;

import net.mcsm.extras.McsmSounds;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidDescent;
import net.mcsm.extras.McsmVoidRudderItem;
import net.mcsm.extras.McsmVoidTiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #481 -- the rudder, where it can actually be felt.
 *
 * <p>THE ONE HONEST PROBLEM WITH A "RUDDER". A player's motion in Minecraft is
 * made on the player's own machine: the server is told where they went and checks
 * it, it does not steer anybody. A tool that multiplied a player's fall from the
 * server side would be a tool that fights the client and loses. So the tool is
 * {@link McsmVoidRudderItem} -- registered, textured, craftable, with its own chat
 * lines and its own state on both sides -- and the PHYSICS are here, in the same
 * client tick the deep already owns.
 *
 * <p>WHAT THE FALL IS NOW. The tiers of {@link McsmVoidTiers} each have their own
 * terminal speed: the baseline falls like the Overworld, and by the gel the void is
 * pulling you down at 21 blocks a second. Holding the rudder multiplies that by
 * 1.6, engaging it by 2.6, and sneaking is a brake that still lets the void have
 * you. Nothing here touches damage: fast, never fatal.
 *
 *   * the tier's current is what the void does on its own, rudder or not;
 *   * the rudder's multipliers are what a player does with it;
 *   * the trail is the rudder's own -- glowing slit-trail particles and the warp
 *     sweep, so a dive reads as a dive from the inside.
 */
public final class McsmVoidRudder {

    /** How hard the fall accelerates toward the tier's current, per tick. */
    private static final double PULL = 0.10D;
    /**
     * The fastest the fall is ever allowed to go, in blocks per tick: 2.2 is
     * 44 blocks a second, which is already faster than the client streams chunks.
     * "Extreme, controllable cinematic speeds" stops being cinematic the moment it
     * is flying through unloaded space, so this is the ceiling, not the target.
     */
    private static final double MAX_FALL = 2.2D;
    /** The trail's own clock. */
    private static int clock;
    /** The last tier the trail's chime was played in. */
    private static int lastTier = -1;

    private McsmVoidRudder() {
    }

    /** Called every client tick, from the same hook the deep runs on. */
    public static void tick() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return;
            }
            LocalPlayer player = mc.player;
            ClientLevel level = mc.level;
            if (player == null || level == null) {
                return;
            }
            if (!McsmVoidRudderItem.voidAir(level, player)) {
                lastTier = -1;
                return;
            }
            clock++;

            // ---- what the void is doing to you, rudder or not -----------------
            double y = player.getY();
            double current = McsmVoidTiers.current(y);
            boolean held = McsmVoidRudderItem.held(player);
            double speed = current * McsmVoidRudderItem.factor(player);
            if (player.isShiftKeyDown()) {
                speed *= McsmVoidRudderItem.BRAKE;
            }

            if (!player.getAbilities().flying && !player.isSpectator() && !player.onGround()) {
                Vec3 dm = player.getDeltaMovement();
                if (speed > MAX_FALL) {
                    speed = MAX_FALL;
                }
                if (dm.y > -speed) {
                    // the void's own pull, then the rudder's, then the brake
                    player.setDeltaMovement(dm.x, Math.max(dm.y - PULL, -speed), dm.z);
                }
            }

            // ---- the crossing: a chime and a shove as a tier gives way --------
            int tier = McsmVoidTiers.tierAt(y);
            if (tier != lastTier) {
                if (lastTier >= 0 && tier > lastTier) {
                    level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                            McsmSounds.OBLIVION_GLITCH, SoundSource.AMBIENT, 0.35F,
                            0.7F + 0.1F * tier, false);
                }
                lastTier = tier;
            }

            // ---- the trail: only the rudder's own, and only while it is working -
            if (held && clock % 2 == 0) {
                Vec3 dm = player.getDeltaMovement();
                double bx = player.getX() - dm.x * 2.0D + (level.getRandom().nextDouble() - 0.5D) * 0.5D;
                double by = player.getY() + 1.2D - dm.y * 2.0D;
                double bz = player.getZ() - dm.z * 2.0D + (level.getRandom().nextDouble() - 0.5D) * 0.5D;
                level.addParticle(ParticleTypes.GLOW, bx, by, bz,
                        (level.getRandom().nextDouble() - 0.5D) * 0.04D,
                        0.06D + level.getRandom().nextDouble() * 0.05D,
                        (level.getRandom().nextDouble() - 0.5D) * 0.04D);
                if (clock % 6 == 0) {
                    level.addParticle(ParticleTypes.SOUL, bx, by, bz, 0.0D, 0.02D, 0.0D);
                }
            }
            if (held && clock % 40 == 0) {
                level.playLocalSound(player.getX(), player.getY() + 1.0D, player.getZ(),
                        McsmSounds.OBLIVION_WARP, SoundSource.PLAYERS,
                        0.22F + 0.18F * (float) (speed / 0.60D), 1.15F, false);
            }
        } catch (Throwable ignored) {
            // a trail that throws is worse than a quiet fall
        }
    }

    /** Is the deep's own medium being crossed right now? (For the HUD's own readout.) */
    public static boolean diving() {
        try {
            Minecraft mc = Minecraft.getInstance();
            return mc != null && mc.player != null && mc.level != null
                    && McsmVoidRudderItem.voidAir(mc.level, mc.player);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
