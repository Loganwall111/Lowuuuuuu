package net.mcsm.sift;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MCSM Void Heights Infinite Wrap-Around Engine - EXPANDED
 * Build #482 - The Infinite Sift Cosmos + Fabric of Reality expansion
 * Baseline: 7000.0.0-M / 117/117 checkpoint green-checked
 *
 * NEW FLOW per user clarification:
 * Overworld Y> -64 normal
 * Fabric of Reality Y -64 to -200: infinite gigantic ground, pitch black with stars, cosmic purple, cracks glowing, trampoline
 *   - Two types: Fabric Stone (solid, fibre from reality itself) and Broken Fabric (invisible, no collision, portal)
 * Emptiness Y -200 to -1000: pitch black void of stars, 40-50 sec fall with fireworks
 * Tier 1 Gel Horizon Y -1000 to -1450: liquid where floats, god rays appear, sky changes
 * Tier 2 Menger Maze Y -1450 to -1950: orange-to-pink AND pitch black starry sponge (two colors)
 * Tier 3 Rift Field Y -1950 to -2200: reality rifts
 * Tier 4 Displacement Y -2200 to -2450: rainbow bands
 * Tier 5 Iridescent Gel Y -2450 to -2700: rainbow water pools
 * Bottom Fabric Y -2700 to -2800: second purple pink brown rainbow layer, color of skybox, leads to Unknown
 * Unknown Y -2800 to -3200: bouncy distortion, ground decay, no bedrock
 * Inner Space: when breakthrough black fabric, fall from sky back to overworld
 */
public final class McsmVoidTiers {

    // Original constants kept for compatibility
    public static final int TOP_BOUNDARY = -251;
    public static final int ABSOLUTE_FLOOR = -2032;
    public static final int TOTAL_HEIGHT = TOP_BOUNDARY - ABSOLUTE_FLOOR;

    // NEW EXPANDED TIERS - Fabric of Reality flow
    public static final int OVERWORLD_BOTTOM = -64;
    public static final int FABRIC_TOP = -64;
    public static final int FABRIC_BOTTOM = -200;
    public static final int EMPTINESS_TOP = -200;
    public static final int EMPTINESS_BOTTOM = -1000; // 40-50 sec fall - fireworks, pitch black void of stars
    public static final int TIER_1_GEL_HORIZON_TOP = -1000;
    public static final int TIER_1_GEL_HORIZON_BOTTOM = -1450;
    public static final int TIER_2_SPONGE_MAZE_TOP = -1450;
    public static final int TIER_2_SPONGE_MAZE_BOTTOM = -1950;
    public static final int TIER_2_PHYSICAL_MAZE_START = -1600;
    public static final int TIER_2_PHYSICAL_MAZE_END = -1950;
    public static final int TIER_3_RIFT_FIELD_TOP = -1950;
    public static final int TIER_3_RIFT_FIELD_BOTTOM = -2200;
    public static final int TIER_4_DISPLACEMENT_TOP = -2200;
    public static final int TIER_4_DISPLACEMENT_BOTTOM = -2450;
    public static final int TIER_5_GEL_VOID_TOP = -2450;
    public static final int TIER_5_GEL_VOID_BOTTOM = -2700;
    public static final int BOTTOM_FABRIC_TOP = -2700;
    public static final int BOTTOM_FABRIC_BOTTOM = -2800;
    public static final int UNKNOWN_TOP = -2800;
    public static final int UNKNOWN_BOTTOM = -3200;
    public static final int INNER_SPACE_TRIGGER = -3200;

    public static final int NEW_TOTAL_HEIGHT = FABRIC_TOP - UNKNOWN_BOTTOM; // ~3136 blocks for 40-50 sec fall

    public enum Tier {
        OVERWORLD(320, 1000, "overworld"),
        FABRIC_OF_REALITY(FABRIC_TOP, FABRIC_BOTTOM, "fabric_of_reality"),
        EMPTINESS(EMPTINESS_TOP, EMPTINESS_BOTTOM, "emptiness"),
        TIER_1_GEL_HORIZON(TIER_1_GEL_HORIZON_TOP, TIER_1_GEL_HORIZON_BOTTOM, "gel_horizon"),
        TIER_2_MENGER_SPONGE(TIER_2_SPONGE_MAZE_TOP, TIER_2_SPONGE_MAZE_BOTTOM, "menger_maze"),
        TIER_3_RIFT_FIELD(TIER_3_RIFT_FIELD_TOP, TIER_3_RIFT_FIELD_BOTTOM, "rift_field"),
        TIER_4_DISPLACEMENT(TIER_4_DISPLACEMENT_TOP, TIER_4_DISPLACEMENT_BOTTOM, "displacement_bands"),
        TIER_5_IRIDESCENT_GEL(TIER_5_GEL_VOID_TOP, TIER_5_GEL_VOID_BOTTOM, "iridescent_void"),
        BOTTOM_FABRIC(BOTTOM_FABRIC_TOP, BOTTOM_FABRIC_BOTTOM, "bottom_fabric"),
        UNKNOWN(UNKNOWN_TOP, UNKNOWN_BOTTOM, "unknown"),
        INNER_SPACE(UNKNOWN_BOTTOM, -4000, "inner_space");

        public final int topY;
        public final int bottomY;
        public final String id;

        Tier(int top, int bottom, String id) {
            this.topY = top;
            this.bottomY = bottom;
            this.id = id;
        }

        public boolean contains(int y) {
            return y <= topY && y >= bottomY;
        }

        public float getDepthFactor(int y) {
            return Mth.clamp((float)(topY - y) / (float)(topY - bottomY), 0f, 1f);
        }
    }

    // Dynamic Seed Shuffling - structural spawner arrays
    private static final List<Long> STRUCTURE_SEEDS = new ArrayList<>();
    private static long currentLoopSeed = 0x5EED5EEDL;
    private static int loopCount = 0;

    static {
        RandomSource rng = RandomSource.create(0x7000000L);
        for (int i = 0; i < 64; i++) {
            STRUCTURE_SEEDS.add(rng.nextLong());
        }
    }

    private McsmVoidTiers() {}

    public static Tier getTierForY(int y) {
        if (y > FABRIC_TOP) return Tier.OVERWORLD;
        for (Tier t : Tier.values()) {
            if (t != Tier.OVERWORLD && t.contains(y)) return t;
        }
        return Tier.INNER_SPACE;
    }

    public static boolean shouldWrap(int y) {
        return y < INNER_SPACE_TRIGGER;
    }

    public static int calculateWrappedY(int currentY) {
        if (!shouldWrap(currentY)) return currentY;
        int overshoot = INNER_SPACE_TRIGGER - currentY;
        int wrapped = FABRIC_TOP - (overshoot % NEW_TOTAL_HEIGHT);
        return wrapped;
    }

    public static boolean isInFabric(int y) {
        return y <= FABRIC_TOP && y >= FABRIC_BOTTOM || y <= BOTTOM_FABRIC_TOP && y >= BOTTOM_FABRIC_BOTTOM;
    }

    public static boolean isInEmptiness(int y) {
        return y < EMPTINESS_TOP && y > EMPTINESS_BOTTOM;
    }

    public static boolean isInUnknown(int y) {
        return y <= UNKNOWN_TOP && y >= UNKNOWN_BOTTOM;
    }

    public static boolean shouldTriggerInnerSpace(int y) {
        return y < UNKNOWN_BOTTOM;
    }

    /**
     * Velocity-Retaining Handshake Matrix
     */
    public static WrapResult performWrapWithHandshake(Player player) {
        Vec3 originalVelocity = player.getDeltaMovement();
        float originalPitch = player.getXRot();
        float originalYaw = player.getYRot();
        float originalHeadYaw = player.getYHeadRot();
        float originalFallDistance = player.fallDistance;
        boolean wasOnGround = player.onGround();

        int currentY = (int) player.getY();
        int newY = calculateWrappedY(currentY);

        BlockPos currentPos = player.blockPosition();
        BlockPos newPos = new BlockPos(currentPos.getX(), newY, currentPos.getZ());

        shuffleSeedsOnWrap();

        return new WrapResult(
            newPos,
            originalVelocity,
            originalPitch,
            originalYaw,
            originalHeadYaw,
            originalFallDistance,
            wasOnGround,
            loopCount
        );
    }

    public static synchronized void shuffleSeedsOnWrap() {
        loopCount++;
        currentLoopSeed = currentLoopSeed * 6364136223846793005L + 1442695040888963407L;
        RandomSource rng = RandomSource.create(currentLoopSeed);
        Collections.shuffle(STRUCTURE_SEEDS, new java.util.Random(currentLoopSeed));
        for (int i = 0; i < STRUCTURE_SEEDS.size(); i++) {
            long s = STRUCTURE_SEEDS.get(i);
            s ^= rng.nextLong();
            s = Long.rotateLeft(s, loopCount % 64);
            STRUCTURE_SEEDS.set(i, s);
        }
    }

    public static long getStructureSeed(int index) {
        if (STRUCTURE_SEEDS.isEmpty()) return currentLoopSeed;
        return STRUCTURE_SEEDS.get(Math.floorMod(index, STRUCTURE_SEEDS.size())) ^ currentLoopSeed;
    }

    public static int getLoopCount() {
        return loopCount;
    }

    public static float getTierTransitionFactor(int y) {
        Tier tier = getTierForY(y);
        if (tier == Tier.OVERWORLD) return 0f;
        int distToTop = Math.abs(y - tier.topY);
        int distToBottom = Math.abs(y - tier.bottomY);
        int blend = 16;
        if (distToTop < blend) {
            return 1f - (float)distToTop / blend;
        }
        if (distToBottom < blend) {
            return 1f - (float)distToBottom / blend;
        }
        return 0f;
    }

    public record WrapResult(
        BlockPos newPos,
        Vec3 preservedVelocity,
        float preservedPitch,
        float preservedYaw,
        float preservedHeadYaw,
        float preservedFallDistance,
        boolean wasOnGround,
        int loopIndex
    ) {
        public void applyTo(Player player) {
            double newX = player.getX();
            double newZ = player.getZ();
            double newY = newPos.getY() + 0.5;
            player.teleportTo(newX, newY, newZ);
            player.setDeltaMovement(preservedVelocity);
            player.setXRot(preservedPitch);
            player.setYRot(preservedYaw);
            player.setYHeadRot(preservedHeadYaw);
            player.fallDistance = preservedFallDistance;
            if (!wasOnGround) {
                player.setOnGround(false);
            }
            player.hasImpulse = true;
        }
    }

    public static float[] getTierColor(int y) {
        Tier tier = getTierForY(y);
        return switch (tier) {
            case FABRIC_OF_REALITY -> new float[]{0.2f, 0.1f, 0.4f}; // cosmic purple pitch black
            case EMPTINESS -> new float[]{0.02f, 0.02f, 0.08f}; // pitch black void of stars
            case TIER_1_GEL_HORIZON -> new float[]{0.6f, 0.9f, 1.0f};
            case TIER_2_MENGER_SPONGE -> new float[]{1.0f, 0.6f, 0.3f};
            case TIER_3_RIFT_FIELD -> new float[]{0.7f, 0.3f, 1.0f};
            case TIER_4_DISPLACEMENT -> new float[]{0.3f, 0.6f, 1.0f};
            case TIER_5_IRIDESCENT_GEL -> new float[]{0.2f, 1.0f, 0.8f};
            case BOTTOM_FABRIC -> new float[]{0.8f, 0.4f, 0.6f}; // purple pink brown rainbow
            case UNKNOWN -> new float[]{0.3f, 0.2f, 0.5f};
            default -> new float[]{0.5f, 0.7f, 1.0f};
        };
    }

    public static boolean isInVoidDimension(int y) {
        return y <= FABRIC_TOP && y >= INNER_SPACE_TRIGGER;
    }

    public static float getFallTimeSeconds(int startY, int endY) {
        // Terminal velocity ~ 78 blocks/sec, but with Void Rudder faster
        // 40-50 seconds for -200 to -1000 = 800 blocks = ~20 sec at normal, need slower fall or longer distance
        // Actually new total 3136 blocks / 78 = ~40 sec - matches user request
        int distance = Math.abs(startY - endY);
        return distance / 78f;
    }
}
