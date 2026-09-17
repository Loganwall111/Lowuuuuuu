package net.mcsm.extras;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #462 -- THE CREATOR'S OWN DIMENSION.
 *
 * <p>The nextOrder list, item (c): "the Creator's dimension". The Creator has been
 * a real entity since BUILD #428 -- colossal, crowned, white-hot eyes, arms coming
 * down out of the sky -- and until now it had nowhere to be. It stood in whatever
 * world it was summoned into, which is not where something that size belongs.
 *
 * <p>This is its place: THE REACH, a bright, absolute, built world. White marble
 * over gold, pale blue air, warm gold light, and none of it grown -- every block of
 * it laid. It is the fourth of the mod's dimensions and it follows the same rule as
 * the other three: its own ground, its own biome, its own sky, its own horizon, its
 * own air, its own lock and key, and its own doorway.
 *
 * <p>The identity is the canonical one (see {@link McsmIdentity#CREATOR}): the fog
 * and the water come from this dimension's biome, the sky light from its dimension
 * type, and the horizon from the bottom row of its own painted panorama. Nothing
 * here is a second copy of another world's palette.
 *
 * <p>Going in is the same path the other worlds use ({@link McsmPortals}' doorway,
 * walk in, no item and no menu), and the arrival is a site rather than a spot: a
 * marble plaza with the Creator's plinth at the middle and its reliquary standing
 * beside it, so the world a player lands in is already a place someone built.
 */
public final class McsmCreatorRealm {

    private McsmCreatorRealm() {
    }

    /** The level key. Data-driven, exactly like the other three ({@code mcsm:creators_realm}). */
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("mcsm", "creators_realm"));

    /** The dimension type key, one per world and never shared. */
    public static final ResourceKey<net.minecraft.world.level.dimension.DimensionType> TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    Identifier.fromNamespaceAndPath("mcsm", "creators_realm"));

    /** How far apart two players' arrival cells are, and how big a plaza is. */
    public static final int REGION = 192;
    private static final int SCAN_TOP = 200;
    private static final int PLAZA = 6;
    /** Below this, a player in the reach is put back on top of it rather than falling on. */
    private static final double FLOOR_GUARD = -48.0D;
    /** How high over the plaza its owner stands. High enough to be looked up at. */
    private static final double PRESENCE_HEIGHT = 180.0D;

    private static final Set<Long> BUILT = ConcurrentHashMap.newKeySet();

    private static boolean done = false;

    /** Booted from the base mod's onInitialize, with the other three dimensions. */
    public static void register() {
        if (done) {
            return;
        }
        done = true;
        try {
            ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmCreatorRealm::tick);
            System.out.println("[ds] the Creator's reach is at " + DIMENSION.location()
                    + " (region " + REGION + ", built world, no end state)");
            System.out.println("[ds] " + McsmIdentity.summary());
        } catch (Throwable t) {
            System.err.println("[ds] the Creator's reach could not be announced: " + t);
        }
    }

    // -------------------------------------------------------------------------
    // Going in
    // -------------------------------------------------------------------------

    /** The doorway's way in: the same shape as adams', with this world's own fanfare. */
    public static boolean enter(ServerPlayer player) {
        try {
            if (!McsmExtrasConfig.creatorRealm || player == null
                    || player.level().getServer() == null) {
                return false;
            }
            ServerLevel target = player.level().getServer().getLevel(DIMENSION);
            if (target == null) {
                player.sendSystemMessage(Component.literal(
                        "\u00a75The doorway opens onto nothing \u00a78(the reach is not loaded)"));
                return false;
            }
            return send(player, target);
        } catch (Throwable t) {
            return false;
        }
    }

    /** Where a given player lands: deterministic per player, never stacked. */
    public static int[] gateFor(UUID id) {
        long h = mix(id.getMostSignificantBits() ^ id.getLeastSignificantBits());
        int x = Math.floorDiv((int) Math.floorMod(h >> 21, 8192L) - 4096, REGION) * REGION
                + REGION / 2;
        int z = Math.floorDiv((int) Math.floorMod(h >> 42, 8192L) - 4096, REGION) * REGION
                + REGION / 2;
        return new int[]{x, z};
    }

    /**
     * The arrival. This mirrors {@link McsmAdams}'s own teleport exactly -- the same
     * {@code teleportTo} overload, the same column scan, the same sound vocabulary --
     * because that path is proven and a second, invented one would only be another
     * thing to break.
     */
    private static boolean send(ServerPlayer player, ServerLevel target) {
        boolean intoReach = target.dimension().equals(DIMENSION);
        double x;
        double z;
        if (intoReach) {
            int[] gate = gateFor(player.getUUID());
            x = gate[0] + 0.5D;
            z = gate[1] + 0.5D;
        } else {
            x = player.getX() + 0.5D;
            z = player.getZ() + 0.5D;
        }
        double y = intoReach ? ground(target, (int) x, (int) z) + 2.0D
                : Math.max(player.getY(), target.getMinY() + 8.0D);
        if (intoReach) {
            plaza(target, (int) x, (int) z);
            y = ground(target, (int) x, (int) z) + 2.0D;
        }
        player.teleportTo(target, x, y, z, Set.of(), player.getYRot(), 0.0F, false);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        target.playSound((Entity) null, x, y, z, McsmSounds.OBLIVION_WARP,
                SoundSource.PLAYERS, 0.85F, 0.9F);
        target.playSound((Entity) null, x, y, z, McsmSounds.MASSG_GIGGLE,
                SoundSource.PLAYERS, 0.35F, 1.45F);
        player.sendSystemMessage(Component.literal(intoReach
                ? "\u00a7e\u00a7lTHE CREATOR'S REACH \u00a78\u00b7 it was built, and it was not built for you"
                : "\u00a7e\u00a7lTHE DOOR CLOSES \u00a78\u00b7 the gold is behind you"));
        if (intoReach) {
            player.sendSystemMessage(Component.literal("\u00a78\u00b7 the plinth stands at the "
                    + "middle of the plaza \u00b7 hold the \u00a7fRift Key\u00a78 and sneak to leave"));
            player.sendSystemMessage(Component.literal("\u00a7d\u00b7 and it is already here"
                    + " \u00a78\u00b7 look up"));
        }
        return true;
    }

    /** First solid block under an arrival, by scanning -- never by assuming. */
    private static double ground(ServerLevel level, int x, int z) {
        try {
            for (int y = SCAN_TOP; y > level.getMinY(); y--) {
                if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                    return y + 1.0D;
                }
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return 66.0D;
    }

    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    // -------------------------------------------------------------------------
    // The arrival site: a plaza with the plinth, laid once per cell
    // -------------------------------------------------------------------------

    private static void plaza(ServerLevel level, int cx, int cz) {
        try {
            long key = ((long) cx << 32) ^ (cz & 0xFFFFFFFFL);
            if (!BUILT.add(key)) {
                return;
            }
            BlockState floor = state("mcsm:creator_floor");
            BlockState gold = state("mcsm:creator_gold");
            BlockState marble = state("mcsm:creator_marble");
            BlockState pillar = state("mcsm:creator_pillar");
            BlockState glyph = state("mcsm:creator_glyph");
            BlockState lamp = state("mcsm:creator_lamp");
            BlockState plinth = state("mcsm:creator_plinth");
            BlockState reliquary = state("mcsm:creator_reliquary");
            if (floor == null || gold == null || marble == null) {
                return;
            }
            int y = (int) ground(level, cx, cz);
            for (int dx = -PLAZA; dx <= PLAZA; dx++) {
                for (int dz = -PLAZA; dz <= PLAZA; dz++) {
                    boolean edge = Math.abs(dx) == PLAZA || Math.abs(dz) == PLAZA;
                    level.setBlock(new BlockPos(cx + dx, y, cz + dz), edge ? gold : floor, 2);
                }
            }
            // four pillars at the corners of the inner square: marble shafts, with a
            // band of glyph tiles and a lamp on top of each
            int c = PLAZA - 2;
            int[][] corners = {{-c, -c}, {c, -c}, {-c, c}, {c, c}};
            for (int[] corner : corners) {
                int px = cx + corner[0];
                int pz = cz + corner[1];
                for (int i = 1; i <= 5; i++) {
                    level.setBlock(new BlockPos(px, y + i, pz), pillar, 2);
                }
                if (glyph != null) {
                    level.setBlock(new BlockPos(px, y + 6, pz), glyph, 2);
                }
                if (lamp != null) {
                    level.setBlock(new BlockPos(px, y + 7, pz), lamp, 2);
                }
            }
            // the plinth at the middle, and the reliquary standing beside it. The
            // reliquary is the only place the reach's key is found, so it is HERE,
            // where a player who walks in actually sees it.
            if (plinth != null) {
                level.setBlock(new BlockPos(cx, y + 1, cz), plinth, 2);
                level.setBlock(new BlockPos(cx, y + 2, cz), plinth, 2);
                level.setBlock(new BlockPos(cx, y + 3, cz), plinth, 2);
            }
            if (reliquary != null) {
                BlockPos at = new BlockPos(cx + 2, y + 1, cz);
                level.setBlock(at, reliquary, 2);
                fill(level, at, cx, cz);
            }
            // and then something is in the sky over it: the reach is where the
            // Creator actually stands, so a player who walks in looks up and finds
            // it already there, over their own plaza and nobody else's
            presence(level, cx, y, cz);
        } catch (Throwable t) {
            System.err.println("[ds] the arrival plaza could not be laid: " + t);
        }
    }

    /**
     * BUILD #462 -- THE CREATOR, IN ITS OWN WORLD. The entity has existed since
     * #428; this is the first time it is somewhere rather than summoned. One per
     * arrival cell, standing 180 blocks above the plaza, scaled to 26 like every
     * other manifestation of it, persistent, and never a duplicate of itself.
     */
    private static void presence(ServerLevel level, int cx, int y, int cz) {
        try {
            EntityType<?> type = net.mcsm.extras.entity.McsmEntities.CREATOR_ENTRY;
            if (type == null) {
                return;
            }
            Entity created = type.create(level, EntitySpawnReason.EVENT);
            if (!(created instanceof Mob mob)) {
                return;
            }
            if (mob instanceof net.mcsm.extras.entity.McsmBeast beast) {
                beast.setKind(net.mcsm.extras.entity.McsmBeast.CREATOR);
                beast.setLine("THE WORLD IS ON MY BACK");
            }
            double x = cx + 0.5D;
            double z = cz + 0.5D;
            double at = y + PRESENCE_HEIGHT;
            BlockPos pos = BlockPos.containing(x, at, z);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
                    EntitySpawnReason.EVENT, (SpawnGroupData) null);
            set(mob, Attributes.SCALE, 26.0D);
            mob.setCustomName(Component.literal("\u00a7dThe Creator"));
            mob.setCustomNameVisible(true);
            mob.setPersistenceRequired();
            mob.snapTo(x, at, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mob);
        } catch (Throwable ignored) {
            // a plaza without its owner over it is still a plaza
        }
    }

    private static void set(LivingEntity entity, Holder<Attribute> attribute, double value) {
        try {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(value);
            }
        } catch (Throwable ignored) {
            // an attribute the beast does not have is an attribute it does not need
        }
    }

    /** The reach's own salvage, in the reliquary at the middle of the plaza. */
    private static void fill(ServerLevel level, BlockPos at, int cx, int cz) {
        try {
            if (!(level.getBlockEntity(at) instanceof Container container)) {
                return;
            }
            long h = mix(((long) cx << 21) ^ (cz * 0x9E3779B97F4A7C15L));
            int slots = Math.max(1, container.getContainerSize());
            String[] pool = {"mcsm:creator_fragment", "mcsm:creator_sigil",
                             "mcsm:memory_crystal", "mcsm:creator_gold",
                             "mcsm:decayed_steel_ingot"};
            for (int i = 0; i < Math.min(slots, 4 + Math.floorMod((int) (h >> 7), 3)); i++) {
                String id = pool[Math.floorMod((int) (h >> (11 + i * 5)), pool.length)];
                ItemStack stack = stack(id, 1 + Math.floorMod((int) (h >> (3 + i)), 3));
                if (stack != null && !stack.isEmpty()) {
                    container.setItem(i, stack);
                }
            }
        } catch (Throwable ignored) {
            // an empty reliquary is still a reliquary
        }
    }

    private static ItemStack stack(String id, int count) {
        try {
            int colon = id.indexOf(':');
            if (colon <= 0) {
                return null;
            }
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(
                    Identifier.fromNamespaceAndPath(id.substring(0, colon),
                            id.substring(colon + 1)));
            return item == null ? null : new ItemStack(item, count);
        } catch (Throwable t) {
            return null;
        }
    }

    private static BlockState state(String id) {
        try {
            int colon = id.indexOf(':');
            if (colon > 0) {
                Block block = BuiltInRegistries.BLOCK.getValue(
                        Identifier.fromNamespaceAndPath(id.substring(0, colon),
                                id.substring(colon + 1)));
                // a block registry answers a missing id with AIR, not null: an air
                // "plaza" would be a hole in the world that is not a place
                if (block != null && block != Blocks.AIR) {
                    return block.defaultBlockState();
                }
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // The tick: the world stays a floor rather than a fall
    // -------------------------------------------------------------------------

    public static void tick(ServerLevel level) {
        try {
            if (!McsmExtrasConfig.creatorRealm || !level.dimension().equals(DIMENSION)
                    || level.players().isEmpty()) {
                return;
            }
            for (ServerPlayer player : level.players()) {
                if (player.getY() < FLOOR_GUARD) {
                    // the reach is built: there is a floor, and standing under it is
                    // not a way out of the world, so the player is put back on it
                    int[] gate = gateFor(player.getUUID());
                    double x = gate[0] + 0.5D;
                    double z = gate[1] + 0.5D;
                    plaza(level, gate[0], gate[1]);
                    double y = ground(level, gate[0], gate[1]) + 2.0D;
                    player.teleportTo(level, x, y, z, Set.of(), player.getYRot(), 0.0F, false);
                    player.setDeltaMovement(Vec3.ZERO);
                    player.resetFallDistance();
                    player.sendSystemMessage(Component.literal(
                            "\u00a7e\u00a78\u00b7 the reach puts you back on its floor"));
                }
            }
        } catch (Throwable t) {
            System.err.println("[ds] the reach's tick failed: " + t);
        }
    }
}
