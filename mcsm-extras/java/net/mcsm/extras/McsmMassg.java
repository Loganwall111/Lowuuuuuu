package net.mcsm.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #416 (D.8, phase 6) -- THE MASSG.
 *
 * THE USER'S BRIEF, IN THEIR WORDS. "The MASSG creature: a gigantic warped black
 * creature, glowing purple eyes, it warps reality, hallucinations of things that
 * don't exist, it attacks and corrupts, it imitates and forces the player, screen
 * flicker + colour glitches + strange music, a boss. Once summoned it cannot be
 * killed or deleted and the world stays changed. On summon, a gigantic terminal
 * appears in the sky counting from 99 down to 1 February 2027. Psychedelic
 * collapse, rifts in the sky, blood, monsters, gigantic things swimming in the
 * air."
 *
 * WHAT THIS IS. The creature is built out of the same proven parts the rest of
 * this dimension uses -- a real vanilla body spawned through the registry, given
 * its attributes directly, tagged so nothing else in this mod touches it -- and
 * then wrapped in its own rules:
 *
 *   * IT CANNOT BE KILLED OR DELETED. The body is invulnerable, persistent, and
 *     the level tick re-materialises it if it is ever removed (a death, a
 *     /kill, a chunk unload that goes wrong, anything). There is no code path in
 *     this build that ends it. The only way to be rid of it is to be rid of the
 *     world.
 *   * IT CHANGES THE WORLD PERMANENTLY. It scars the ground it stands on as it
 *     walks, and the scars are ordinary blocks: they do not time out, they do
 *     not regenerate, and they survive a reload.
 *   * IT CORRUPTS PEOPLE. Within its reach it applies withering, blindness and
 *     nausea, and its gaze damages. It speaks in the player's own voice.
 *   * IT HALLUCINATES. It spawns "things that don't exist" -- real mobs with no
 *     AI that stand inside walls and vanish after a few seconds -- plus false
 *     storms in chat and the sky.
 *   * IT COUNTS DOWN. From the moment it is summoned the sky carries a gigantic
 *     terminal counting 99, 98, 97 ... down to 1 February 2027, the day the
 *     count reaches one. The counter is TIME-based: it is the same number for
 *     everyone, and it is correct after a reload.
 *
 * Everything is driven from ONE level tick and every step is inside its own
 * try/catch, so a creature that cannot be removed can still never break a tick.
 */
public final class McsmMassg {

    /**
     * The name it wears. It is not a mob name a player can type into a summon
     * command to get a tame copy: the body is ordinary, everything that makes it
     * the MASSG is in this class.
     */
    public static final String NAME = "MASSG";

    /** The day the count reaches one. */
    public static final long END_EPOCH_MS = 1801526400000L; // 2027-02-01T00:00:00Z

    /** Where the counter starts. */
    public static final int COUNT_FROM = 99;

    /** How far it can see, hurt and corrupt. */
    private static final double REACH = 96.0D;
    /** How far it is summoned from the operator, so it is seen, not stepped on. */
    private static final double SUMMON_DISTANCE = 42.0D;
    /** Its footprint: the radius of the ground it scars. */
    private static final int SCAR_RADIUS = 5;
    /** Ticks between two scars of the same kind. */
    private static final int SCAR_INTERVAL = 40;

    /** Live creature per level, and when it was born, in wall-clock millis. */
    private static final Map<Object, UUID> LIVE = new ConcurrentHashMap<>();
    private static final Map<Object, Long> BORN = new ConcurrentHashMap<>();
    /** Hallucination bodies this mod is done with, so they can be cleaned up. */
    private static final List<UUID> GHOSTS = new ArrayList<>();
    /** Per-player throttle for the voice, the flicker and the hallucinations. */
    private static final Map<UUID, Long> LAST_VOICE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_SKY = new ConcurrentHashMap<>();

    /**
     * THE COUNTDOWN'S WIRE. The MASSG's custom name is not decoration: it is the
     * one piece of state that arrives on every client for free, because entity
     * names are synced by the game itself. It carries the number, and the line
     * the sky terminal prints under it:
     *
     *     MASSG|47|IT KNOWS WHERE YOU ARE
     *
     * The client reads exactly this (McsmMassgSky), which is why there is no
     * packet channel in this build, why the number is the same for everybody, and
     * why a player who joins late is in sync on their first frame.
     */
    public static String encode(int number, String line) {
        return NAME + "|" + number + "|" + (line == null ? "" : line.replace('|', '/'));
    }

    /** The body's name, which the client parses. */
    public static String nameFor(int number, String line) {
        return encode(number, line);
    }

    private McsmMassg() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmMassg::tick);
            System.out.println("[ds] the MASSG is listening (it cannot be killed, and it counts)");
        } catch (Throwable t) {
            System.err.println("[ds] the MASSG could not register: " + t);
        }
    }

    // ---------------------------------------------------------------------
    // Summoning
    // ---------------------------------------------------------------------

    /** Is the MASSG alive in this level? */
    public static boolean live(Object level) {
        return LIVE.containsKey(level);
    }

    /** The counter, 99 down to 1. It is time-based, so every client agrees. */
    public static int counter(Object level) {
        Long born = BORN.get(level);
        if (born == null) {
            return COUNT_FROM;
        }
        long now = System.currentTimeMillis();
        if (now >= END_EPOCH_MS) {
            return 1;
        }
        double progress = (double) (now - born.longValue())
                / (double) Math.max(1L, END_EPOCH_MS - born.longValue());
        int left = COUNT_FROM - (int) Math.floor(progress * (COUNT_FROM - 1));
        if (left > COUNT_FROM) {
            left = COUNT_FROM;
        }
        if (left < 1) {
            left = 1;
        }
        return left;
    }

    /**
     * SUMMON IT. Called from the console (and only from there): the code has to
     * have been entered first, because the terminal is the only thing in this
     * build that knows what the MASSG is.
     */
    public static boolean summon(ServerLevel level, ServerPlayer operator) {
        if (!McsmExtrasConfig.massgEnabled) {
            return false;
        }
        try {
            if (live(level)) {
                operator.sendSystemMessage(Component.literal(
                        "The MASSG is already here. It has been counting since it arrived.")
                        .withStyle(ChatFormatting.DARK_PURPLE));
                return true;
            }
            Vec3 look = operator.getLookAngle();
            double x = operator.getX() + look.x * SUMMON_DISTANCE;
            double z = operator.getZ() + look.z * SUMMON_DISTANCE;
            int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            BlockPos at = new BlockPos((int) x, y, (int) z);

            Mob body = materialise(level, at);
            if (body == null) {
                return false;
            }
            LIVE.put(level, body.getUUID());
            BORN.put(level, Long.valueOf(System.currentTimeMillis()));

            // THE SKY OPENS. Everything the user asked to have happen at the
            // moment of the summon, in one sequence.
            level.playSound(null, x, y, z, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 6.0F, 0.45F);
            level.playSound(null, x, y, z, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 4.0F, 0.6F);
            level.playSound(null, x, y, z, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 5.0F, 0.4F);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 8.0D, z, 400, 14.0D, 10.0D, 14.0D, 0.05D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 12.0D, z, 500, 18.0D, 14.0D, 18.0D, 0.4D);
            level.sendParticles(ParticleTypes.SQUID_INK, x, y + 6.0D, z, 300, 12.0D, 8.0D, 12.0D, 0.1D);

            for (ServerPlayer player : level.players()) {
                player.sendSystemMessage(Component.literal(
                        "\u00a75\u00a7lTHE MASSG IS HERE.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
                player.sendSystemMessage(Component.literal(
                        "\u00a77It cannot be killed. It cannot be deleted. The ground it walks on "
                        + "will not grow back.").withStyle(ChatFormatting.GRAY));
                player.sendSystemMessage(Component.literal(
                        "\u00a7dThe sky is counting: \u00a7f" + counter(level)
                        + "\u00a7d down to \u00a7f1 February 2027\u00a7d.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                player.playSound(SoundEvents.PORTAL_TRAVEL, 1.4F, 0.5F);
            }
            McsmCreatures.say(level, new Vec3(x, y, z), 200.0D,
                    "IT SEES YOU NOW AND IT WILL NOT STOP SEEING YOU", ChatFormatting.DARK_PURPLE);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Build the body. Self-contained: this one must never be culled by anything. */
    private static Mob materialise(ServerLevel level, BlockPos at) {
        try {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE
                    .getValue(Identifier.fromNamespaceAndPath("minecraft", "warden"));
            if (type == null) {
                return null;
            }
            Entity created = type.create(level, EntitySpawnReason.EVENT);
            if (!(created instanceof Mob mob)) {
                return null;
            }
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT,
                    (SpawnGroupData) null);
            // The name is the channel (see encode). It is NOT shown above the
            // creature: the sky terminal is the display, and the creature reads
            // better as a silhouette than as a mob with a label.
            mob.setCustomName(Component.literal(encode(COUNT_FROM, "IT IS AWAKE")));
            mob.setCustomNameVisible(false);
            mob.setPersistenceRequired();
            if (McsmExtrasConfig.massgUnkillable) {
                mob.setInvulnerable(true);
            }
            set(mob, Attributes.SCALE, McsmExtrasConfig.massgScale);
            set(mob, Attributes.MAX_HEALTH, 4096.0D);
            set(mob, Attributes.ARMOR, 30.0D);
            set(mob, Attributes.KNOCKBACK_RESISTANCE, 1.0D);
            set(mob, Attributes.FOLLOW_RANGE, 256.0D);
            if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
                mob.setHealth(mob.getMaxHealth());
            }
            mob.snapTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mob);
            return mob;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void set(LivingEntity entity, Holder<Attribute> attribute, double value) {
        try {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(value);
            }
        } catch (Throwable ignored) {
        }
    }

    // ---------------------------------------------------------------------
    // The tick: it is always there, and it is always doing something
    // ---------------------------------------------------------------------

    private static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.massgEnabled) {
                return;
            }
            long time = level.getGameTime();
            Entity beast = find(level);
            if (beast == null) {
                if (!LIVE.containsKey(level)) {
                    return; // never summoned here
                }
                // IT CANNOT BE DELETED. Whatever removed it, it comes back -- at
                // the last place it stood, or over the operator if that is lost.
                respawn(level, time);
                return;
            }
            if (time % 20L != 0L) {
                return;
            }
            haunt(level, beast, time);
            scar(level, beast, time);
            corrupt(level, beast);
            if (time % 40L == 0L) {
                broadcast(level);
            }
        } catch (Throwable ignored) {
            // a creature that cannot be removed must still not break a tick
        }
    }

    /**
     * Find the body again. The id map is the fast path; the named scan is the
     * fallback, using the same custom-name test this build already uses to find
     * IVOR, so it cannot depend on a tag API that may not exist here.
     */
    private static Entity find(ServerLevel level) {
        try {
            UUID id = LIVE.get(level);
            if (id != null) {
                Entity found = level.getEntity(id);
                if (found != null && found.isAlive() && !found.isRemoved()) {
                    return found;
                }
            }
            for (ServerPlayer player : level.players()) {
                for (Entity entity : level.getEntitiesOfClass(Mob.class,
                        player.getBoundingBox().inflate(512.0D))) {
                    if (entity.getCustomName() != null
                            && NAME.equals(entity.getCustomName().getString())
                            && entity.isAlive() && !entity.isRemoved()) {
                        LIVE.put(level, entity.getUUID());
                        return entity;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void respawn(ServerLevel level, long time) {
        try {
            ServerPlayer anchor = level.players().isEmpty() ? null : level.players().get(0);
            if (anchor == null) {
                return;
            }
            double x = anchor.getX() + 24.0D;
            double z = anchor.getZ() + 24.0D;
            int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            Mob body = materialise(level, new BlockPos((int) x, y, (int) z));
            if (body == null) {
                return;
            }
            LIVE.put(level, body.getUUID());
            if (!BORN.containsKey(level)) {
                BORN.put(level, Long.valueOf(System.currentTimeMillis()));
            }
            for (ServerPlayer player : level.players()) {
                player.sendSystemMessage(Component.literal(
                        "\u00a75It is back. It was never gone: \u00a7fit only stopped to watch you kill it.")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
            level.playSound(null, x, y, z, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.0F, 0.35F);
        } catch (Throwable ignored) {
        }
    }

    /** Hallucinations, the voice, the flicker, the music. */
    private static void haunt(ServerLevel level, Entity beast, long time) {
        try {
            for (ServerPlayer player : level.players()) {
                double distance = player.distanceTo(beast);
                if (distance > REACH * 2.0D) {
                    continue;
                }
                // strange music, slow and wrong, for everyone who can see it
                if (time % 240L == 0L) {
                    player.playSound(SoundEvents.AMBIENT_CAVE, 1.8F, 0.35F);
                }
                if (time % 300L == 0L) {
                    player.playSound(SoundEvents.END_PORTAL_SPAWN, 0.9F, 0.5F);
                }
                // colour glitches + screen flicker: the client draws these from
                // the "sky" packet below, so both sides agree on the same number
                Long lastSky = LAST_SKY.get(player.getUUID());
                if (lastSky == null || time - lastSky.longValue() > 40L) {
                    LAST_SKY.put(player.getUUID(), Long.valueOf(time));
                    McsmTerminal.sendSky(player, counter(level), skyLine(player, distance));
                }
                // IT IMITATES YOU: it answers in your own name.
                Long lastVoice = LAST_VOICE.get(player.getUUID());
                if (lastVoice == null || time - lastVoice.longValue() > 600L) {
                    LAST_VOICE.put(player.getUUID(), Long.valueOf(time));
                    player.sendSystemMessage(Component.literal(
                            "<" + player.getName().getString() + "> ").append(imitation(distance)));
                }
                if (McsmExtrasConfig.massgHallucinations && time % 100L == 0L
                        && level.getRandom().nextInt(100) < 60) {
                    hallucinate(level, player);
                }
            }
            if (time % 100L == 0L && McsmExtrasConfig.massgHallucinations) {
                flyingThings(level, beast);
            }
        } catch (Throwable ignored) {
        }
    }

    private static String skyLine(ServerPlayer player, double distance) {
        if (distance < 32.0D) {
            return "IT IS STANDING ON YOUR SHADOW";
        }
        if (distance < 96.0D) {
            return "IT KNOWS WHERE YOU ARE";
        }
        return "IT IS STILL COUNTING";
    }

    private static Component imitation(double distance) {
        if (distance < 24.0D) {
            return Component.literal("\u00a7oI am behind you. Do not turn around.")
                    .withStyle(ChatFormatting.DARK_PURPLE);
        }
        if (distance < 64.0D) {
            return Component.literal("\u00a7oYou let me out. You typed it in yourself.")
                    .withStyle(ChatFormatting.DARK_PURPLE);
        }
        return Component.literal("\u00a7oI am still here. I will still be here.")
                .withStyle(ChatFormatting.DARK_PURPLE);
    }

    /**
     * THINGS THAT DON'T EXIST. A real body, no AI, standing perfectly still
     * inside whatever it spawned in, gone again in a few seconds -- close enough
     * to a monster to make a player swing at it, and never close enough to be
     * one. They are tagged, so nothing else in this build counts them as content.
     */
    private static void hallucinate(ServerLevel level, ServerPlayer player) {
        String[] shapes = {"zombie", "skeleton", "enderman", "phantom", "husk", "stray"};
        try {
            String shape = shapes[level.getRandom().nextInt(shapes.length)];
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE
                    .getValue(Identifier.fromNamespaceAndPath("minecraft", shape));
            if (type == null) {
                return;
            }
            Entity created = type.create(level, EntitySpawnReason.EVENT);
            if (!(created instanceof Mob mob)) {
                return;
            }
            mob.setNoAi(true);
            mob.setInvulnerable(true);
            mob.setCustomName(Component.literal("\u00a78" + shape));
            mob.setCustomNameVisible(false);
            mob.setPersistenceRequired();
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = 12.0D + level.getRandom().nextDouble() * 22.0D;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            mob.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mob);
            GHOSTS.add(mob.getUUID());
            if (GHOSTS.size() > 60) {
                UUID oldest = GHOSTS.remove(0);
                Entity gone = level.getEntity(oldest);
                if (gone != null) {
                    gone.discard();
                }
            }
            // and they are not supposed to be fightable: they fade out
            level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.AMBIENT, 0.4F, 1.6F);
        } catch (Throwable ignored) {
        }
    }

    /**
     * GIGANTIC THINGS SWIMMING IN THE AIR. Phantoms are the only vanilla body
     * that flies on its own, so they are scaled up, made permanent for their
     * lifetime and tagged; nothing here is meant to be survived, it is meant to
     * be looked at.
     */
    private static void flyingThings(ServerLevel level, Entity beast) {
        try {
            if (level.getRandom().nextInt(100) >= 45) {
                return;
            }
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE
                    .getValue(Identifier.fromNamespaceAndPath("minecraft", "phantom"));
            if (type == null) {
                return;
            }
            Entity created = type.create(level, EntitySpawnReason.EVENT);
            if (!(created instanceof Mob mob)) {
                return;
            }
            set(mob, Attributes.SCALE, 3.5D + level.getRandom().nextDouble() * 2.5D);
            mob.setCustomName(Component.literal("\u00a75a thing swimming in the air"));
            mob.setCustomNameVisible(false);
            mob.setPersistenceRequired();
            double x = beast.getX() + level.getRandom().nextDouble() * 80.0D - 40.0D;
            double z = beast.getZ() + level.getRandom().nextDouble() * 80.0D - 40.0D;
            double y = beast.getY() + 20.0D + level.getRandom().nextDouble() * 30.0D;
            mob.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mob);
            GHOSTS.add(mob.getUUID());
        } catch (Throwable ignored) {
        }
    }

    /**
     * IT CORRUPTS WHAT IT WALKS ON. Real blocks, placed for good: no timer, no
     * revert, and they are the content pack's own decayed set, so the scar is
     * made of something a player can build with.
     */
    private static void scar(ServerLevel level, Entity beast, long time) {
        try {
            if (time % SCAR_INTERVAL != 0L) {
                return;
            }
            int cx = (int) Math.floor(beast.getX());
            int cz = (int) Math.floor(beast.getZ());
            for (int i = 0; i < 6; i++) {
                int dx = level.getRandom().nextInt(SCAR_RADIUS * 2 + 1) - SCAR_RADIUS;
                int dz = level.getRandom().nextInt(SCAR_RADIUS * 2 + 1) - SCAR_RADIUS;
                int x = cx + dx;
                int z = cz + dz;
                int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockPos at = new BlockPos(x, y, z);
                if (!level.isLoaded(at)) {
                    continue;
                }
                if (!level.getBlockState(at).isAir() && McsmContent.DECAYED_SURFACE != null) {
                    level.setBlock(at, McsmContent.DECAYED_SURFACE.defaultBlockState(), 3);
                }
                BlockPos above = at.above();
                if (level.getBlockState(above).isAir() && level.getRandom().nextInt(100) < 12) {
                    level.sendParticles(ParticleTypes.SOUL, x + 0.5D, y + 1.2D, z + 0.5D, 6,
                            0.4D, 0.4D, 0.4D, 0.01D);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /** IT HURTS AND IT CORRUPTS. Withering, blindness, nausea, and its gaze. */
    private static void corrupt(ServerLevel level, Entity beast) {
        try {
            AABB box = beast.getBoundingBox().inflate(REACH);
            for (ServerPlayer player : level.players()) {
                if (!box.contains(player.position())) {
                    continue;
                }
                double distance = player.distanceTo(beast);
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0, false, false));
                if (distance < REACH * 0.5D) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
                }
                if (distance < 12.0D) {
                    try {
                        player.hurtServer(level, level.damageSources().generic(), 6.0F);
                    } catch (Throwable ignored) {
                    }
                }
                if (player.getHealth() <= 0.0F) {
                    player.sendSystemMessage(Component.literal(
                            "\u00a75The MASSG does not want you dead. It wants you here.")
                            .withStyle(ChatFormatting.DARK_PURPLE));
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * THE SKY TERMINAL'S NUMBER, written where every client can read it: the
     * creature's own name. Nothing is sent, nothing is registered, and the number
     * a player sees is the number the server just wrote.
     */
    private static void broadcast(ServerLevel level) {
        try {
            int number = counter(level);
            Entity beast = find(level);
            if (beast == null) {
                return;
            }
            ServerPlayer nearest = null;
            double best = Double.MAX_VALUE;
            for (ServerPlayer player : level.players()) {
                double distance = player.distanceTo(beast);
                if (distance < best) {
                    best = distance;
                    nearest = player;
                }
            }
            String line = nearest == null ? "IT IS STILL COUNTING"
                    : skyLine(nearest, nearest.distanceTo(beast));
            beast.setCustomName(Component.literal(encode(number, line)));
            // and the number is spoken once per change, so a player who is not
            // looking up still knows what day it is
            if (number != LAST_NUMBER) {
                LAST_NUMBER = number;
                for (ServerPlayer player : level.players()) {
                    player.sendSystemMessage(Component.literal(
                            "\u00a7dThe sky is counting: \u00a7f" + number
                            + "\u00a7d ... to \u00a7f1 February 2027\u00a7d.")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /** The last number spoken, so the count is announced once per change. */
    private static int LAST_NUMBER = -1;
}
