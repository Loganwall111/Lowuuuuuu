package net.mcsm.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #426 -- THE TENTACLES TAKE YOU.
 *
 * THE REPORT. "I was playing the game in Survival for a bit and I still didn't
 * see the tentacle grab. The tentacles are meant to snatch the player and pull
 * it up and then throw it or even eat it. The tractor beams are doing that but
 * not the tentacles. I want that to be a thing."
 *
 * The config already had a "Tentacle Grab" switch -- and nothing in the mod read
 * it. The beams did their own lift; the tentacles were decoration. This is the
 * missing half, and it is a real grab with a beginning, a middle and two
 * endings:
 *
 *   SNATCH     a storm's tentacle comes down, the player is taken off the
 *              ground, and from then on the pull is physical: every tick moves
 *              them a little closer to the mouth, so you can see where you are
 *              going and you cannot simply walk out of it.
 *   STRUGGLE   the grip tightens -- the pull accelerates, the player bleeds
 *              slowly, and the creature's own heartbeat plays under it.
 *   THROW      two times in three it hurls you away from the storm, up and out,
 *              and the landing is what hurts.
 *   EAT        one time in three it drags you all the way in, hits you hard,
 *              and drops you back out of the sky.
 *
 * Survival only: creative and spectator players are never taken (a grab you
 * cannot escape in creative is a bug, not a feature), and the whole thing obeys
 * the config switch the user already has. Every world call used here -- the
 * bounding-box entity search, teleportTo with Set.of() and the yaw/pitch
 * overload, hurtServer with the generic damage source -- is one this workspace
 * already uses elsewhere, so it is a shape that compiles on this classpath.
 */
public final class McsmTentacles {

    private McsmTentacles() {
    }

    /** How far from the storm a tentacle can reach. */
    private static final double REACH = 44.0D;
    /** How close to the mouth counts as swallowed. */
    private static final double MOUTH = 16.0D;
    /** How high above the storm's feet the mouth sits (the storm is enormous). */
    private static final double MOUTH_LIFT = 24.0D;
    /** How long a grip lasts before the decision, in ticks. */
    private static final int STRUGGLE_TICKS = 70;
    /** Pull speed in blocks per tick: at the start, and at the end, of a grip. */
    private static final double PULL_START = 0.55D;
    private static final double PULL_END = 1.15D;
    /** Bleed while held, and how often. */
    private static final float GRIP_DAMAGE = 1.0F;
    private static final int GRIP_DAMAGE_EVERY = 25;

    private static final Map<UUID, Hold> HELD = new HashMap<>();
    private static boolean registered;
    private static long clock;

    private static final class Hold {
        int ticks;
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        try {
            ServerTickEvents.END_LEVEL_TICK.register(McsmTentacles::tick);
            System.out.println("[ds] the tentacles are awake");
        } catch (Throwable t) {
            System.err.println("[ds] the tentacle grab failed to register: " + t);
        }
    }

    private static void tick(ServerLevel level) {
        clock++;
        try {
            if (!McsmExtrasConfig.enableTentacleGrab) {
                if (!HELD.isEmpty()) {
                    HELD.clear();
                }
                return;
            }
            if (!HELD.isEmpty()) {
                hold(level);
            }
            if (clock % 8L == 0L) {
                snatch(level);
            }
        } catch (Throwable ignored) {
            // the grab must never take the server down with it
        }
    }

    /** The storm nearest this point, if there is one within the search radius. */
    private static LivingEntity stormNear(ServerLevel level, Vec3 at, double radius) {
        try {
            AABB box = new AABB(at.x - radius, at.y - radius * 0.5D, at.z - radius,
                    at.x + radius, at.y + radius, at.z + radius);
            // The base mod's own storm type, queried the way this overlay queries
            // it elsewhere (McsmNpcs does exactly this), so the grab can only ever
            // take hold of a real Wither Storm.
            LivingEntity best = null;
            double bestDistance = Double.MAX_VALUE;
            for (WitherStormEntity storm : level.getEntitiesOfClass(WitherStormEntity.class, box)) {
                double d = storm.position().distanceTo(at);
                if (d < bestDistance) {
                    best = storm;
                    bestDistance = d;
                }
            }
            return best;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void snatch(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (HELD.containsKey(player.getUUID())) {
                continue;
            }
            if (player.isCreative() || player.isSpectator() || player.isPassenger()) {
                continue;
            }
            LivingEntity storm = stormNear(level, player.position(), REACH);
            if (storm == null) {
                continue;
            }
            // never take someone who is already up in the storm's own air
            if (player.getY() < storm.getY() - 40.0D) {
                continue;
            }
            HELD.put(player.getUUID(), new Hold());
            message(player, "\u00a75A tentacle closes around you.");
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    McsmSounds.MASSG_BREATH, SoundSource.HOSTILE, 1.4F, 0.7F);
        }
    }

    private static void hold(ServerLevel level) {
        List<UUID> done = new ArrayList<>();
        for (Map.Entry<UUID, Hold> entry : new HashMap<>(HELD).entrySet()) {
            ServerPlayer player = null;
            for (ServerPlayer candidate : level.players()) {
                if (candidate.getUUID().equals(entry.getKey())) {
                    player = candidate;
                    break;
                }
            }
            if (player == null) {
                done.add(entry.getKey());
                continue;
            }
            LivingEntity storm = stormNear(level, player.position(), 320.0D);
            if (storm == null) {
                done.add(entry.getKey());
                continue;
            }
            Hold hold = entry.getValue();
            hold.ticks++;

            // the mouth: above the storm's feet, in its own column
            Vec3 mouth = new Vec3(storm.getX(), storm.getY() + MOUTH_LIFT, storm.getZ());
            Vec3 toMouth = mouth.subtract(player.position());
            double distance = toMouth.length();
            double pull = PULL_START + (PULL_END - PULL_START)
                    * Math.min(1.0D, hold.ticks / (double) STRUGGLE_TICKS);

            if (distance > 0.001D) {
                Vec3 step = toMouth.scale(pull / distance);
                // the pull, plus enough lift that the player never scrapes the
                // ground on the way up. teleportTo with the yaw/pitch overload is
                // the same call the reality dimension uses for its rift transit.
                player.teleportTo(level,
                        player.getX() + step.x,
                        player.getY() + Math.max(step.y, 0.42D),
                        player.getZ() + step.z,
                        Set.of(), player.getYRot(), player.getXRot(), false);
            }

            if (hold.ticks % GRIP_DAMAGE_EVERY == 0) {
                player.hurtServer(level, level.damageSources().generic(), GRIP_DAMAGE);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        McsmSounds.MASSG_HEART, SoundSource.HOSTILE, 0.9F, 1.2F);
            }

            boolean arrived = distance <= MOUTH;
            if (hold.ticks >= STRUGGLE_TICKS || arrived) {
                if (arrived || player.getRandom().nextInt(3) == 0) {
                    eat(level, player, storm);
                } else {
                    throwAway(level, player, storm);
                }
                done.add(entry.getKey());
            }
        }
        for (UUID id : done) {
            HELD.remove(id);
        }
    }

    /** Swallowed: hit hard once, then dropped out of the mouth. */
    private static void eat(ServerLevel level, ServerPlayer player, LivingEntity storm) {
        message(player, "\u00a74It eats you.");
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                McsmSounds.MASSG_ROAR, SoundSource.HOSTILE, 1.6F, 0.8F);
        player.hurtServer(level, level.damageSources().generic(), 8.0F);
        Vec3 away = player.position().subtract(storm.position());
        if (away.lengthSqr() < 0.001D) {
            away = new Vec3(1.0D, 0.0D, 0.0D);
        }
        Vec3 out = away.normalize().scale(6.0D);
        // out of the mouth and falling: the drop is the rest of the punishment
        player.teleportTo(level, storm.getX() + out.x, storm.getY() + MOUTH_LIFT - 6.0D,
                storm.getZ() + out.z, Set.of(), player.getYRot(), player.getXRot(), false);
    }

    /** Thrown: up and away from the storm, and the landing is the damage. */
    private static void throwAway(ServerLevel level, ServerPlayer player, LivingEntity storm) {
        message(player, "\u00a76It throws you.");
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                McsmSounds.OBLIVION_WARP, SoundSource.HOSTILE, 1.2F, 1.1F);
        Vec3 away = player.position().subtract(storm.position());
        if (away.lengthSqr() < 0.001D) {
            away = new Vec3(1.0D, 0.0D, 0.0D);
        }
        Vec3 out = away.normalize().scale(18.0D + player.getRandom().nextDouble() * 10.0D);
        player.teleportTo(level,
                player.getX() + out.x,
                Math.max(player.getY(), storm.getY() + MOUTH_LIFT) + 6.0D,
                player.getZ() + out.z,
                Set.of(), player.getYRot(), player.getXRot(), false);
    }

    private static void message(ServerPlayer player, String text) {
        try {
            player.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.DARK_PURPLE));
        } catch (Throwable ignored) {
            // chat is a courtesy, not the mechanic
        }
    }

    /** True while this player is in a tentacle's grip (used by the HUD + tests). */
    public static boolean holding(UUID player) {
        return HELD.containsKey(player);
    }

    /** How many are held right now -- the terminal's own report uses it. */
    public static int held() {
        return HELD.size();
    }
}
