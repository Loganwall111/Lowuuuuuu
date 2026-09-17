package net.mcsm.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #416 (D.8, phase 3) -- THE BESTIARY AND THE BOSS LADDER.
 *
 * THE MANDATE. "Creatures, monsters, bosses ... the Creator, giant octopus arms
 * through rips in reality." Phase 2 built the abandoned cities those things were
 * always supposed to be waiting in; this is what is waiting in them.
 *
 * WHAT IT IS.
 *   1. THE BESTIARY -- storm-touched creatures that spawn around a player in the
 *      storm's reach and inside the decayed reality, scaled by the storm's own
 *      phase (scale, health, damage, speed, knockback, an aura), each wearing
 *      its own name so a player can tell what hit them.
 *   2. THE BOSS LADDER -- five rungs, one per band of the storm's growth, each
 *      summoned ONCE per storm, announced in chat, and fought with a staged
 *      health bar: at two thirds and at one third of its health a boss digs in
 *      (aura on, adds out, sky open) before it can be finished. Breaking a rung
 *      pays out a weapon from the content pack, so the ladder and the arsenal
 *      are one progression instead of two parallel lists.
 *   3. THE CREATOR -- the top rung: the storm stops being a storm and starts
 *      reaching. Seven arms come down through rips in the sky (the geometry is
 *      drawn by {@code McsmCreatorArms}; this class is what decides when the sky
 *      is torn, where an arm lands, and what it does to someone standing there).
 *
 * WHY THIS COMPILES AND RUNS WITHOUT A SINGLE NEW REGISTRY ENTRY. Every creature
 * here is a vanilla mob type resolved out of {@code BuiltInRegistries.ENTITY_TYPE}
 * and re-kitted -- the same idiom {@code McsmNpcs} already ships -- so there is
 * no model, no renderer, no spawn egg and no client half to keep in step, and the
 * ladder works on a world that was saved before any of it existed. The one piece
 * of new API is the boss bar, and that is resolved reflectively (see
 * {@code McsmBossBar}) with chat as the fallback, so a fight can never depend on
 * a class being reachable.
 *
 * Everything is fail-soft: a creature that cannot spawn, a rung that cannot be
 * placed and an arm that cannot land all do nothing rather than take a tick down.
 */
public final class McsmCreatures {

    /** Tag on every creature this class owns, so nothing else is ever pruned. */
    private static final String OWNER = "ds_bestiary";
    private static final double SCAN = 64.0D;
    private static final int PER_PLAYER_CAP = 7;
    private static final int LEVEL_CAP = 72;
    private static final double SPAWN_MIN = 24.0D;
    private static final double SPAWN_MAX = 44.0D;
    /** Phase at which the storm starts throwing things at the player at all. */
    private static final double BESTIARY_PHASE = 1.6D;
    /** Phase at which the Creator's arms come through the sky. */
    private static final double CREATOR_PHASE = 6.9D;

    /** Every creature this class owns, by UUID. */
    private static final Set<UUID> MANAGED = ConcurrentHashMap.newKeySet();
    /** Highest rung already summoned, per storm. */
    private static final Map<UUID, Integer> LADDER = new ConcurrentHashMap<UUID, Integer>();
    /** Living bosses, by entity id. */
    private static final Map<Integer, Fight> FIGHTS = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------------
    // The bestiary
    // ---------------------------------------------------------------------

    private static final class Kind {
        final String path;
        final String name;
        final double fromPhase;
        final double scale;
        final double health;
        final double damage;
        final double speed;
        final double armour;
        final double knockback;
        final int weight;
        final String gear;
        final double gearChance;

        Kind(String path, String name, double fromPhase, double scale, double health,
                double damage, double speed, double armour, double knockback, int weight,
                String gear, double gearChance) {
            this.path = path;
            this.name = name;
            this.fromPhase = fromPhase;
            this.scale = scale;
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.armour = armour;
            this.knockback = knockback;
            this.weight = weight;
            this.gear = gear;
            this.gearChance = gearChance;
        }
    }

    /**
     * The bestiary, weakest and earliest first. Weights are relative, not
     * percentages: the table is rolled by weight after the phase filter.
     */
    private static final Kind[] BESTIARY = {
        new Kind("minecraft:husk", "Ash Husk", 1.6D, 1.00D, 22.0D, 1.10D, 1.00D, 2.0D, 0.10D,
                30, null, 0.0D),
        new Kind("minecraft:zombie", "Devourer Brute", 2.2D, 1.45D, 32.0D, 1.35D, 1.00D, 4.0D, 0.35D,
                26, "minecraft:iron_sword", 0.35D),
        new Kind("minecraft:cave_spider", "Rift Crawler", 2.4D, 1.35D, 16.0D, 1.15D, 1.28D, 3.0D, 0.0D,
                22, null, 0.0D),
        new Kind("minecraft:skeleton", "Bone Rattler", 3.0D, 1.15D, 24.0D, 1.20D, 1.10D, 4.0D, 0.10D,
                20, "minecraft:bow", 0.45D),
        new Kind("minecraft:phantom", "Storm Wraith", 3.6D, 1.40D, 26.0D, 1.25D, 1.05D, 2.0D, 0.0D,
                16, null, 0.0D),
        new Kind("minecraft:wither_skeleton", "Ash Knight", 4.4D, 1.25D, 44.0D, 1.40D, 1.05D, 6.0D, 0.45D,
                14, "minecraft:stone_sword", 0.60D),
        new Kind("minecraft:blaze", "Ember Core", 5.2D, 1.20D, 32.0D, 1.30D, 1.05D, 4.0D, 0.10D,
                12, null, 0.0D),
        new Kind("minecraft:ravager", "Devourer Colossus", 6.4D, 1.55D, 120.0D, 1.60D, 1.02D, 10.0D, 0.70D,
                6, null, 0.0D),
    };

    // ---------------------------------------------------------------------
    // The ladder
    // ---------------------------------------------------------------------

    private static final class Rung {
        final int tier;
        final double minPhase;
        final String path;
        final String name;
        final double scale;
        final double health;
        final double damage;
        final double speed;
        final double armour;
        final double knockback;
        final int adds;
        final String addPath;
        final String trophy;
        final String barColour;
        final String lines;
        final String reward;

        Rung(int tier, double minPhase, String path, String name, double scale, double health,
                double damage, double speed, double armour, double knockback, int adds,
                String addPath, String trophy, String barColour, String lines, String reward) {
            this.tier = tier;
            this.minPhase = minPhase;
            this.path = path;
            this.name = name;
            this.scale = scale;
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.armour = armour;
            this.knockback = knockback;
            this.adds = adds;
            this.addPath = addPath;
            this.trophy = trophy;
            this.barColour = barColour;
            this.lines = lines;
            this.reward = reward;
        }
    }

    private static final Rung[] RUNGS = {
        new Rung(1, 2.0D, "minecraft:zombie", "The Bent Sentinel", 2.40D, 170.0D, 1.90D, 1.02D,
                6.0D, 0.60D, 2, "minecraft:zombie", "mcsm:withered_blade", "GREEN",
                "something the storm made out of a man is walking out of the dust",
                "a withered blade was left in the crater"),
        new Rung(2, 3.5D, "minecraft:wither_skeleton", "Herald of Ash", 1.90D, 270.0D, 2.05D, 1.06D,
                9.0D, 0.55D, 3, "minecraft:skeleton", "mcsm:storm_spear", "YELLOW",
                "the storm has a herald now, and it is calling the dead up with it",
                "the herald dropped a storm spear"),
        new Rung(3, 5.0D, "minecraft:ravager", "Maw of the Devourer", 1.60D, 360.0D, 2.20D, 1.10D,
                11.0D, 0.75D, 3, "minecraft:cave_spider", "mcsm:storm_heart_shard", "RED",
                "the maw is through, and the ground is coming with it",
                "the maw broke, and a shard of the storm's heart is on the ground"),
        new Rung(4, 6.5D, "minecraft:warden", "Warden of the Decayed Reality", 1.35D, 540.0D, 1.85D, 1.00D,
                13.0D, 0.88D, 4, "minecraft:husk", "mcsm:reality_ripper", "BLUE",
                "the warden of the ruined world has felt you coming for a long time",
                "the reality ripper is yours now"),
        new Rung(5, 7.4D, "minecraft:wither", "THE CREATOR", 2.40D, 720.0D, 1.70D, 0.95D,
                15.0D, 0.92D, 6, "minecraft:phantom", "mcsm:creators_judgement", "PURPLE",
                "LOOK UP. THE SKY IS OPENING AND SOMETHING IS COMING THROUGH IT",
                "the Creator's judgement is in your hands, and the sky is closing again"),
    };

    // ---------------------------------------------------------------------
    // State
    // ---------------------------------------------------------------------

    private static final class Fight {
        final Rung rung;
        final UUID storm;
        final McsmBossBar bar;
        int stage;
        double lastX;
        double lastY;
        double lastZ;
        boolean rewarded;

        Fight(Rung rung, UUID storm, McsmBossBar bar, double x, double y, double z) {
            this.rung = rung;
            this.storm = storm;
            this.bar = bar;
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
        }
    }

    private McsmCreatures() {
    }

    public static void register() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmCreatures::tick);
            System.out.println("[ds] the bestiary is awake: " + BESTIARY.length
                    + " creatures, " + RUNGS.length + " rungs on the boss ladder");
        } catch (Throwable t) {
            System.err.println("[ds] the bestiary could not hook the level tick: " + t);
        }
    }

    // ---------------------------------------------------------------------
    // The per-level tick
    // ---------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.realityCreatures && !McsmExtrasConfig.bossLadder) {
                return;
            }
            if (level.players().isEmpty()) {
                return;
            }
            long time = level.getGameTime();
            if (time % 20L != 0L) {
                return;
            }
            for (ServerPlayer player : level.players()) {
                double phase = phaseNear(level, player);
                boolean decayed = McsmReality.inside(level);
                if (McsmExtrasConfig.realityCreatures && level.getDifficulty() != Difficulty.PEACEFUL) {
                    bestiary(level, player, phase, decayed);
                }
                if (McsmExtrasConfig.bossLadder) {
                    ladder(level, player);
                }
                if (McsmExtrasConfig.creatorArms && phase >= CREATOR_PHASE) {
                    creator(level, player, time);
                }
            }
            if (McsmExtrasConfig.bossLadder) {
                driveFights(level, time);
            }
        } catch (Throwable ignored) {
            // a bestiary that throws is worse than one that stays quiet
        }
    }

    // ---------------------------------------------------------------------
    // 1. The bestiary
    // ---------------------------------------------------------------------

    private static void bestiary(ServerLevel level, ServerPlayer player, double phase, boolean decayed) {
        if (phase < BESTIARY_PHASE && !decayed) {
            return;
        }
        prune(level, player);
        if (MANAGED.size() >= LEVEL_CAP || sweep(level, player, phase) >= PER_PLAYER_CAP) {
            return;
        }
        // The further the storm has gone, the harder it pushes -- but never a
        // wall of mobs: the chance ramps to at most one group every two seconds.
        int chance = (int) Math.min(52.0D, 8.0D + phase * 5.5D);
        if (decayed) {
            chance += 10;
        }
        RandomSource rng = level.getRandom();
        if (rng.nextInt(100) >= chance) {
            return;
        }
        // BUILD #460 -- the decayed reality has its own bestiary now, and it is
        // not the storm's. The drifter belongs to that world and to nowhere else,
        // so a player standing in it meets that dimension's creature instead of a
        // storm beast that followed them in.
        if (decayed) {
            BlockPos decayedAt = spot(level, player, rng);
            if (decayedAt != null && net.mcsm.extras.entity.McsmDenizen.spawn(level,
                    net.mcsm.extras.entity.McsmEntities.DRIFTER, decayedAt,
                    1 + rng.nextInt(2), "Drifter") > 0) {
                return;
            }
        }
        int group = 1 + rng.nextInt(phase >= 5.0D ? 3 : 2);
        for (int i = 0; i < group; i++) {
            Kind kind = roll(rng, phase, decayed);
            if (kind == null) {
                return;
            }
            BlockPos at = spot(level, player, rng);
            if (at == null) {
                return;
            }
            spawnCreature(level, at, kind, phase, rng);
        }
    }

    private static Kind roll(RandomSource rng, double phase, boolean decayed) {
        int total = 0;
        for (Kind kind : BESTIARY) {
            if (kind.fromPhase <= phase || decayed) {
                total += kind.weight;
            }
        }
        if (total <= 0) {
            return null;
        }
        int pick = rng.nextInt(total);
        for (Kind kind : BESTIARY) {
            if (kind.fromPhase <= phase || decayed) {
                pick -= kind.weight;
                if (pick < 0) {
                    return kind;
                }
            }
        }
        return BESTIARY[0];
    }

    private static BlockPos spot(ServerLevel level, ServerPlayer player, RandomSource rng) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = rng.nextDouble() * Math.PI * 2.0D;
            double distance = SPAWN_MIN + rng.nextDouble() * (SPAWN_MAX - SPAWN_MIN);
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos at = new BlockPos(x, y, z);
            if (!level.isLoaded(at)) {
                continue;
            }
            if (y <= level.getMinY() + 2) {
                continue;
            }
            if (player.distanceToSqr((double) x + 0.5D, (double) y, (double) z + 0.5D)
                    < SPAWN_MIN * SPAWN_MIN) {
                continue;
            }
            return at;
        }
        return null;
    }

    /**
     * BUILD #443 -- release the bestiary on demand, around a point.
     *
     * The ambient swarm has its own budget (7 per player, 72 per level) so that a
     * player standing still is not buried. A RITUAL and a chamber in the infinite
     * dimension are the opposite case: the player has deliberately opened
     * something, so they get a fixed, announced handful -- still under the same
     * level cap, because a ritual should not be able to erase a world's budget.
     */
    public static int release(ServerLevel level, BlockPos at, int count, double phase) {
        int spawned = 0;
        try {
            RandomSource rng = level.getRandom();
            int room = LEVEL_CAP - MANAGED.size();
            for (int i = 0; i < Math.min(count, Math.max(0, room)); i++) {
                Kind kind = BESTIARY[Math.floorMod(at.getX() * 31 + at.getZ() * 17 + i * 7,
                        BESTIARY.length)];
                if (spawnCreature(level, at, kind, phase, rng) != null) {
                    spawned++;
                }
            }
        } catch (Throwable ignored) {
            // a ritual that spawns nothing is still a completed ritual
        }
        return spawned;
    }

    private static Mob spawnCreature(ServerLevel level, BlockPos at, Kind kind, double phase,
            RandomSource rng) {
        Mob mob = spawn(level, at, kind.path, kind.name, EntitySpawnReason.EVENT, false);
        if (mob == null) {
            return null;
        }
        // The storm's own exposure, as growth: the same creature late in the
        // ladder is a bigger, harder version of itself.
        double growth = Math.min(0.55D, Math.max(0.0D, (phase - kind.fromPhase) * 0.07D));
        set(mob, Attributes.SCALE, kind.scale * (1.0D + growth * 0.5D));
        set(mob, Attributes.MAX_HEALTH, kind.health * (1.0D + growth));
        set(mob, Attributes.ARMOR, kind.armour);
        set(mob, Attributes.KNOCKBACK_RESISTANCE, kind.knockback);
        set(mob, Attributes.FOLLOW_RANGE, 44.0D);
        multiply(mob, Attributes.ATTACK_DAMAGE, kind.damage);
        multiply(mob, Attributes.MOVEMENT_SPEED, kind.speed);
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            mob.setHealth(mob.getMaxHealth());
        }
        if (kind.gear != null && rng.nextDouble() < kind.gearChance) {
            equip(mob, kind.gear);
        }
        infectedAura(mob, phase);
        return mob;
    }

    private static void infectedAura(Mob mob, double phase) {
        try {
            mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, false));
        } catch (Throwable ignored) {
            // an aura is a garnish; the creature fights without it
        }
    }

    // ---------------------------------------------------------------------
    // 2. The boss ladder
    // ---------------------------------------------------------------------

    private static void ladder(ServerLevel level, ServerPlayer player) {
        WitherStormEntity storm = stormNear(level, player.position(), 420.0D);
        if (storm == null) {
            return;
        }
        double phase = storm.getPhase();
        int highest = LADDER.containsKey(storm.getUUID())
                ? LADDER.get(storm.getUUID()).intValue() : 0;
        for (Rung rung : RUNGS) {
            if (rung.tier <= highest || phase < rung.minPhase) {
                continue;
            }
            if (!level.isLoaded(player.blockPosition())) {
                return;
            }
            BlockPos at = arena(level, player, rung);
            if (at == null) {
                return;
            }
            Mob boss = spawn(level, at, rung.path, rung.name, EntitySpawnReason.COMMAND, true);
            if (boss == null) {
                return;
            }
            set(boss, Attributes.SCALE, rung.scale);
            set(boss, Attributes.MAX_HEALTH, rung.health);
            set(boss, Attributes.ARMOR, rung.armour);
            set(boss, Attributes.KNOCKBACK_RESISTANCE, rung.knockback);
            set(boss, Attributes.FOLLOW_RANGE, 96.0D);
            multiply(boss, Attributes.ATTACK_DAMAGE, rung.damage);
            multiply(boss, Attributes.MOVEMENT_SPEED, rung.speed);
            if (boss.getAttribute(Attributes.MAX_HEALTH) != null) {
                boss.setHealth(boss.getMaxHealth());
            }
            McsmBossBar bar = McsmBossBar.create(rung.name, rung.barColour, true);
            FIGHTS.put(Integer.valueOf(boss.getId()),
                    new Fight(rung, storm.getUUID(), bar, boss.getX(), boss.getY(), boss.getZ()));
            LADDER.put(storm.getUUID(), Integer.valueOf(rung.tier));
            announce(level, boss, rung, "The storm has made something: " + rung.name + " --- " + rung.lines);
            return;
        }
    }

    /** The boss lands in the open, at the far edge of draw distance, in view. */
    private static BlockPos arena(ServerLevel level, ServerPlayer player, Rung rung) {
        RandomSource rng = level.getRandom();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = rng.nextDouble() * Math.PI * 2.0D;
            double distance = 34.0D + rng.nextDouble() * 26.0D;
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos at = new BlockPos(x, y, z);
            if (level.isLoaded(at) && y > level.getMinY() + 3) {
                return at;
            }
        }
        return null;
    }

    /**
     * The staged fight. A boss does not simply lose health: at two thirds it
     * calls its adds, at one third it opens the sky, and every second it is
     * alive it holds a gravitational pull on whoever is closest -- which is how
     * a 2.4x-scale Sentinel or the Creator itself gets its hands on a player.
     */
    private static void driveFights(ServerLevel level, long time) {
        List<Integer> gone = new ArrayList<>();
        for (Map.Entry<Integer, Fight> entry : FIGHTS.entrySet()) {
            Fight fight = entry.getValue();
            Entity entity = level.getEntity(entry.getKey().intValue());
            if (!(entity instanceof Mob mob) || !mob.isAlive()) {
                gone.add(entry.getKey());
                continue;
            }
            fight.lastX = mob.getX();
            fight.lastY = mob.getY();
            fight.lastZ = mob.getZ();

            float fraction = mob.getMaxHealth() > 0.0F
                    ? Math.max(0.0F, mob.getHealth() / mob.getMaxHealth()) : 0.0F;
            if (fight.bar != null) {
                fight.bar.progress(fraction);
                for (ServerPlayer witness : playersWithin(level, mob.position(), 140.0D)) {
                    fight.bar.track(witness);
                }
            }

            ServerPlayer target = nearestPlayer(level, mob.position(), 72.0D);
            if (target != null) {
                mob.setTarget(target);
                if (time % 20L == 0L) {
                    for (ServerPlayer witness : playersWithin(level, mob.position(), 22.0D)) {
                        witness.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70,
                                fight.rung.tier >= 4 ? 1 : 0, false, false));
                    }
                }
            }

            if (fraction <= 0.66F && fight.stage < 1) {
                fight.stage = 1;
                callAdds(level, mob, fight.rung, 2);
                skyOpen(level, mob.position(), 1.0F);
                say(level, mob.position(), 140.0D, fight.rung.name + " is not finished. It is calling.",
                        ChatFormatting.GOLD);
            }
            if (fraction <= 0.33F && fight.stage < 2) {
                fight.stage = 2;
                callAdds(level, mob, fight.rung, 4);
                skyOpen(level, mob.position(), 2.0F);
                say(level, mob.position(), 140.0D, fight.rung.name + " is tearing the sky open.",
                        ChatFormatting.LIGHT_PURPLE);
            }
            if (time % 80L == 0L) {
                skyOpen(level, mob.position(), 0.7F);
            }
        }
        for (Integer id : gone) {
            Fight fight = FIGHTS.remove(id);
            if (fight != null) {
                reward(level, fight);
            }
        }
    }

    private static void callAdds(ServerLevel level, Mob boss, Rung rung, int count) {
        try {
            RandomSource rng = level.getRandom();
            for (int i = 0; i < count; i++) {
                double angle = rng.nextDouble() * Math.PI * 2.0D;
                double distance = 6.0D + rng.nextDouble() * 8.0D;
                BlockPos at = new BlockPos(
                        boss.getBlockX() + (int) Math.round(Math.cos(angle) * distance),
                        boss.getBlockY() + 1,
                        boss.getBlockZ() + (int) Math.round(Math.sin(angle) * distance));
                if (!level.isLoaded(at)) {
                    continue;
                }
                Mob add = spawn(level, at, rung.addPath,
                        boss.getCustomName() != null
                                ? boss.getCustomName().getString() + "'s swarm" : "Storm Swarm",
                        EntitySpawnReason.EVENT, false);
                if (add == null) {
                    continue;
                }
                set(add, Attributes.SCALE, 1.15D);
                set(add, Attributes.MAX_HEALTH, 30.0D);
                multiply(add, Attributes.ATTACK_DAMAGE, 1.2D);
                if (add.getAttribute(Attributes.MAX_HEALTH) != null) {
                    add.setHealth(add.getMaxHealth());
                }
                infectedAura(add, 6.0D);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void reward(ServerLevel level, Fight fight) {
        try {
            if (fight.bar != null) {
                fight.bar.progress(0.0F);
            }
            Vec3 at = new Vec3(fight.lastX, fight.lastY, fight.lastZ);
            say(level, at, 180.0D, fight.rung.name + " is broken -- " + fight.rung.reward,
                    ChatFormatting.LIGHT_PURPLE);
            level.playSound(null, fight.lastX, fight.lastY, fight.lastZ,
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 4.0F, 0.72F);
            level.playSound(null, fight.lastX, fight.lastY, fight.lastZ,
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.4F, 0.9F);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    fight.lastX, fight.lastY + 1.0D, fight.lastZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.ASH,
                    fight.lastX, fight.lastY + 1.5D, fight.lastZ, 60, 2.4D, 1.8D, 2.4D, 0.05D);
            Item trophy = item(fight.rung.trophy);
            if (trophy != null && !fight.rewarded) {
                fight.rewarded = true;
                ItemEntity drop = new ItemEntity(level, fight.lastX, fight.lastY + 0.6D, fight.lastZ,
                        new ItemStack(trophy));
                drop.setDefaultPickUpDelay();
                level.addFreshEntity(drop);
            }
        } catch (Throwable ignored) {
            // a rung that pays out nothing is still a rung that was beaten
        }
    }

    // ---------------------------------------------------------------------
    // 3. The Creator
    // ---------------------------------------------------------------------

    /**
     * The arms are drawn client-side; this is what makes the world answer them.
     * Every few seconds an arm comes down somewhere in front of the player: a
     * rift of light opens at the top of the sky, the air is torn down to the
     * ground, and anyone standing in the impact is thrown and hurt.
     */
    private static void creator(ServerLevel level, ServerPlayer player, long time) {
        try {
            if (time % 120L != 0L) {
                return;
            }
            RandomSource rng = level.getRandom();
            double angle = rng.nextDouble() * Math.PI * 2.0D;
            double distance = 12.0D + rng.nextDouble() * 26.0D;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            int ground = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(x), (int) Math.floor(z));
            double top = player.getY() + 46.0D;

            for (double t = 0.0D; t <= 1.0D; t += 0.08D) {
                double y = top - (top - (double) ground) * t;
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 3,
                        0.45D, 0.45D, 0.45D, 0.01D);
            }
            level.playSound(null, x, (double) ground, z,
                    SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.2F, 0.55F);
            level.sendParticles(ParticleTypes.ASH, x, (double) ground + 0.5D, z, 30,
                    1.6D, 1.2D, 1.6D, 0.06D);
            level.sendParticles(ParticleTypes.PORTAL, x, (double) ground + 0.5D, z, 24,
                    1.2D, 1.0D, 1.2D, 0.10D);

            double reach = 6.0D;
            for (ServerPlayer witness : playersWithin(level, new Vec3(x, (double) ground, z), reach)) {
                Vec3 away = witness.position().subtract(x, (double) ground, z);
                if (away.lengthSqr() < 1.0E-4D) {
                    away = new Vec3(1.0D, 0.0D, 0.0D);
                }
                away = away.normalize();
                witness.push(away.x * 1.5D, 0.55D, away.z * 1.5D);
                witness.hurtServer(level, level.damageSources().generic(), 5.0F);
                witness.sendSystemMessage(Component.literal("\"LOOK UP.\"")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
        } catch (Throwable ignored) {
        }
    }

    // ---------------------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------------------

    private static Mob spawn(ServerLevel level, BlockPos at, String path, String name,
            EntitySpawnReason reason, boolean boss) {
        try {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE
                    .getValue(Identifier.fromNamespaceAndPath("minecraft", path));
            if (type == null) {
                return null;
            }
            Entity created = type.create(level, reason);
            if (!(created instanceof Mob mob)) {
                return null;
            }
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at), reason, (SpawnGroupData) null);
            mob.setCustomName(Component.literal(name));
            mob.setCustomNameVisible(boss);
            mob.setPersistenceRequired();
            mob.addTag(OWNER);
            MANAGED.add(mob.getUUID());
            mob.snapTo((double) at.getX() + 0.5D, (double) at.getY(), (double) at.getZ() + 0.5D,
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

    private static void multiply(LivingEntity entity, Holder<Attribute> attribute, double factor) {
        try {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(instance.getBaseValue() * factor);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void equip(Mob mob, String itemPath) {
        try {
            Item item = item(itemPath);
            if (item != null) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item));
            }
        } catch (Throwable ignored) {
        }
    }

    private static Item item(String id) {
        try {
            int colon = id.indexOf(':');
            String ns = colon > 0 ? id.substring(0, colon) : "minecraft";
            String path = colon > 0 ? id.substring(colon + 1) : id;
            return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ns, path));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** The furthest storm within range of a player, or null. */
    private static WitherStormEntity stormNear(ServerLevel level, Vec3 at, double range) {
        try {
            AABB box = new AABB(at.x - range, at.y - 420.0D, at.z - range,
                    at.x + range, at.y + 420.0D, at.z + range);
            WitherStormEntity best = null;
            double bestDistance = Double.MAX_VALUE;
            for (WitherStormEntity storm : level.getEntitiesOfClass(WitherStormEntity.class, box)) {
                double d = storm.distanceToSqr(at);
                if (d < bestDistance) {
                    bestDistance = d;
                    best = storm;
                }
            }
            return best;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static double phaseNear(ServerLevel level, ServerPlayer player) {
        WitherStormEntity storm = stormNear(level, player.position(), 320.0D);
        if (storm == null) {
            return 0.0D;
        }
        try {
            return (double) storm.getPhase();
        } catch (Throwable ignored) {
            return 0.0D;
        }
    }

    private static ServerPlayer nearestPlayer(ServerLevel level, Vec3 at, double range) {
        ServerPlayer best = null;
        double bestDistance = range * range;
        for (ServerPlayer player : level.players()) {
            double d = player.distanceToSqr(at);
            if (d < bestDistance) {
                bestDistance = d;
                best = player;
            }
        }
        return best;
    }

    private static List<ServerPlayer> playersWithin(ServerLevel level, Vec3 at, double range) {
        List<ServerPlayer> found = new ArrayList<>();
        double limit = range * range;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(at) < limit) {
                found.add(player);
            }
        }
        return found;
    }

    /**
     * One pass over the bestiary: count what is near the player, forget what is
     * gone, and let a late-ladder creature wither anyone standing in its reach --
     * the same affliction the storm's own proximity carries.
     */
    private static int sweep(ServerLevel level, ServerPlayer player, double phase) {
        int count = 0;
        boolean withering = phase >= 5.2D;
        for (UUID id : new ArrayList<>(MANAGED)) {
            Entity entity = level.getEntity(id);
            if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                MANAGED.remove(id);
                continue;
            }
            double distance = entity.distanceToSqr(player.getX(), player.getY(), player.getZ());
            if (distance < SCAN * SCAN) {
                count++;
            }
            if (withering && distance < 36.0D && level.getGameTime() % 60L == 0L) {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 70, 0, false, false));
            }
        }
        return count;
    }

    /** Forget everything that is gone, and thin out anything left far behind. */
    private static void prune(ServerLevel level, ServerPlayer player) {
        for (UUID id : new ArrayList<>(MANAGED)) {
            Entity entity = level.getEntity(id);
            if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                MANAGED.remove(id);
                continue;
            }
            if (MANAGED.size() > LEVEL_CAP
                    && entity.distanceToSqr(player.getX(), player.getY(), player.getZ()) > 240.0D * 240.0D) {
                MANAGED.remove(id);
                entity.discard();
            }
        }
        if (MANAGED.size() > LEVEL_CAP * 3) {
            MANAGED.clear();
        }
    }

    private static void announce(ServerLevel level, Mob boss, Rung rung, String line) {
        say(level, boss.position(), 220.0D, line, ChatFormatting.DARK_PURPLE);
        level.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0F, 0.55F);
        level.sendParticles(ParticleTypes.PORTAL, boss.getX(), boss.getY() + 1.5D, boss.getZ(),
                60, 2.0D, 2.0D, 2.0D, 0.15D);
    }

    /**
     * The storm band nearest a player, for anything that needs to print it (the
     * story terminal's world report). 0 = no storm in range of 320 blocks.
     */
    public static double phaseNearPublic(ServerLevel level, ServerPlayer player) {
        try {
            return phaseNear(level, player);
        } catch (Throwable ignored) {
            return 0.0D;
        }
    }

    /**
     * The ladder as one console line: how many rungs this storm has already sent
     * and which one is next. The ladder is per storm (LADDER is keyed by the
     * storm's UUID), so this resolves the storm near the player first and falls
     * back to the whole table rather than lying about the state.
     */
    public static String ladderLine(ServerPlayer player) {
        try {
            ServerLevel level = player.level();
            WitherStormEntity storm = stormNear(level, player.position(), 320.0D);
            int highest = 0;
            if (storm != null && LADDER.containsKey(storm.getUUID())) {
                highest = LADDER.get(storm.getUUID()).intValue();
            }
            StringBuilder out = new StringBuilder();
            out.append(highest).append(" of ").append(RUNGS.length)
               .append(" rungs broken on this storm :: ");
            for (Rung rung : RUNGS) {
                out.append(rung.tier <= highest ? "[x] " : "[ ] ").append(rung.name);
                if (rung.tier < RUNGS.length) {
                    out.append("  ");
                }
            }
            if (highest < RUNGS.length) {
                out.append("  >> next: ").append(RUNGS[highest].name)
                   .append(" at phase ").append(RUNGS[highest].minPhase);
            } else {
                out.append("  >> all five rungs are behind you; the storm remembers");
            }
            return out.toString();
        } catch (Throwable ignored) {
            return "ladder unavailable here";
        }
    }

    public static void say(ServerLevel level, Vec3 at, double range, String line,
            ChatFormatting colour) {
        Component message = Component.literal(line).withStyle(colour);
        for (ServerPlayer player : playersWithin(level, at, range)) {
            player.sendSystemMessage(message, false);
            player.sendSystemMessage(message, true);
        }
    }

    private static void skyOpen(ServerLevel level, Vec3 at, float strength) {
        try {
            double height = 30.0D * strength;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, at.x, at.y + height, at.z,
                    (int) (10.0F * strength), 3.5D * strength, 1.5D, 3.5D * strength, 0.02D);
            level.sendParticles(ParticleTypes.ASH, at.x, at.y + 2.0D, at.z,
                    (int) (18.0F * strength), 2.5D * strength, 1.0D, 2.5D * strength, 0.04D);
        } catch (Throwable ignored) {
        }
    }
}
