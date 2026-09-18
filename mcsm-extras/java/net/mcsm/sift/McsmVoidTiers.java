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
 * MCSM Void Heights Infinite Wrap-Around Engine - 8 LAYERS OF THE VOID CONCEPT
 * Build #7000.0.12-M - THE LAYERS OF THE VOID - Concept Image Implementation
 * 
 * Concept from user images:
 * 1. THE OVERWORLD - familiar realm
 * 2. BEDROCK LEVEL - lowest solid layer, unbreakable, foundation of all that exists
 * 3. THE FLOATING VOID ISLANDS - fractured remnants of impossible worlds, drifting broken, seeping void energy
 * 4. THE INFINITE NOTHING BARRIER / THE NOTHING BARRIER - surreal boundary of absolute emptiness, reality distorts
 * 5. THE INFINITE BLACKNESS - endless expanse of pure void, no light, no sound, only infinite depth
 * 6. THE SPONGE MAZE - colossal incomprehensible maze of ancient sponge blocks, orange->pink gradient (SIFT AREA)
 * 7. THE LUMINESCENT POOLS - glowing waters and strange liquid crystals, light dances in alien beautiful silence
 * 8. THE ABYSSAL NIGHTMARE DIMENSION - final realm of terror, dimension of terror, chaos, ancient evil, reality screams
 * 
 * MERGED VOID: second dimension directly underneath overworld hundreds blocks down
 * Overworld min_y -2032 height 4064 => continuous fall without teleport, skybox merges slowly
 * Total height -64 to -2032 = 1968 blocks
 * 
 * Previous 7000.0.11-M had FABRIC/EMPTINESS/GEL/MENGER/RIFT/DISP/IRID/BOTTOM/UNKNOWN
 * Now remapped to 8 layers matching concept art for clarity
 */
public final class McsmVoidTiers {

    // Original constants kept for compatibility
    public static final int TOP_BOUNDARY = -64;
    public static final int ABSOLUTE_FLOOR = -2032;
    public static final int TOTAL_HEIGHT = 1968;

    // 8 LAYERS OF THE VOID - Concept Image Implementation - 7000.0.12-M
    // Each layer has distinct visual and gameplay purpose matching concept art
    public static final int OVERWORLD_BOTTOM = -64;
    
    // 2. BEDROCK LEVEL - lowest solid layer, unbreakable, foundation of all that exists
    public static final int BEDROCK_LEVEL_TOP = -64;
    public static final int BEDROCK_LEVEL_BOTTOM = -150; // 86 blocks thick
    
    // 3. THE FLOATING VOID ISLANDS - fractured remnants of impossible worlds
    public static final int FLOATING_VOID_ISLANDS_TOP = -150;
    public static final int FLOATING_VOID_ISLANDS_BOTTOM = -350; // 200 blocks - floating islands
    
    // 4. THE INFINITE NOTHING BARRIER / THE NOTHING BARRIER - surreal boundary
    public static final int NOTHING_BARRIER_TOP = -350;
    public static final int NOTHING_BARRIER_BOTTOM = -550; // 200 blocks - surreal emptiness
    
    // 5. THE INFINITE BLACKNESS - endless expanse of pure void
    public static final int INFINITE_BLACKNESS_TOP = -550;
    public static final int INFINITE_BLACKNESS_BOTTOM = -800; // 250 blocks - pure void, no light
    
    // 6. THE SPONGE MAZE - colossal maze of ancient sponge blocks - SIFT AREA
    public static final int SPONGE_MAZE_TOP = -800;
    public static final int SPONGE_MAZE_BOTTOM = -1200; // 400 blocks - orange->pink gradient
    public static final int SPONGE_MAZE_PHYSICAL_START = -900;
    public static final int SPONGE_MAZE_PHYSICAL_END = -1200;
    
    // 7. THE LUMINESCENT POOLS - glowing waters and strange liquid crystals
    public static final int LUMINESCENT_POOLS_TOP = -1200;
    public static final int LUMINESCENT_POOLS_BOTTOM = -1600; // 400 blocks - glowing waters
    
    // 8. THE ABYSSAL NIGHTMARE DIMENSION - final realm of terror
    public static final int ABYSSAL_NIGHTMARE_TOP = -1600;
    public static final int ABYSSAL_NIGHTMARE_BOTTOM = -2032; // 432 blocks - terror, chaos, ancient evil
    
    // Legacy names for compatibility with existing code
    public static final int FABRIC_TOP = BEDROCK_LEVEL_TOP;
    public static final int FABRIC_BOTTOM = BEDROCK_LEVEL_BOTTOM;
    public static final int EMPTINESS_TOP = FLOATING_VOID_ISLANDS_TOP;
    public static final int EMPTINESS_BOTTOM = INFINITE_BLACKNESS_BOTTOM; // Combined floating islands + nothing barrier + infinite blackness
    public static final int TIER_1_GEL_HORIZON_TOP = LUMINESCENT_POOLS_TOP;
    public static final int TIER_1_GEL_HORIZON_BOTTOM = -1450;
    public static final int TIER_2_SPONGE_MAZE_TOP = SPONGE_MAZE_TOP;
    public static final int TIER_2_SPONGE_MAZE_BOTTOM = SPONGE_MAZE_BOTTOM;
    public static final int TIER_2_PHYSICAL_MAZE_START = SPONGE_MAZE_PHYSICAL_START;
    public static final int TIER_2_PHYSICAL_MAZE_END = SPONGE_MAZE_PHYSICAL_END;
    public static final int TIER_3_RIFT_FIELD_TOP = -1300;
    public static final int TIER_3_RIFT_FIELD_BOTTOM = -1500;
    public static final int TIER_4_DISPLACEMENT_TOP = -1500;
    public static final int TIER_4_DISPLACEMENT_BOTTOM = -1700;
    public static final int TIER_5_GEL_VOID_TOP = LUMINESCENT_POOLS_TOP;
    public static final int TIER_5_GEL_VOID_BOTTOM = LUMINESCENT_POOLS_BOTTOM;
    public static final int BOTTOM_FABRIC_TOP = ABYSSAL_NIGHTMARE_TOP;
    public static final int BOTTOM_FABRIC_BOTTOM = -1900;
    public static final int UNKNOWN_TOP = -1900;
    public static final int UNKNOWN_BOTTOM = -2032;
    public static final int INNER_SPACE_TRIGGER = -2032;

    public static final int NEW_TOTAL_HEIGHT = 1968;

    // Pocket dimension flag - completely disable suffocating in void entirely
    public static final boolean DISABLE_VOID_SUFFOCATION = true;
    public static final boolean DISABLE_VOID_DROWN = true;
    public static final boolean DISABLE_VOID_FALL_DAMAGE = true;
    public static final boolean VOID_IS_POCKET_DIMENSION = true;

    public enum Tier {
        OVERWORLD(320, 1000, "overworld"),
        // 2. BEDROCK LEVEL
        BEDROCK_LEVEL(BEDROCK_LEVEL_TOP, BEDROCK_LEVEL_BOTTOM, "bedrock_level"),
        FABRIC_OF_REALITY(BEDROCK_LEVEL_TOP, BEDROCK_LEVEL_BOTTOM, "fabric_of_reality"),
        // 3. FLOATING VOID ISLANDS
        FLOATING_VOID_ISLANDS(FLOATING_VOID_ISLANDS_TOP, FLOATING_VOID_ISLANDS_BOTTOM, "floating_void_islands"),
        // 4. NOTHING BARRIER
        NOTHING_BARRIER(NOTHING_BARRIER_TOP, NOTHING_BARRIER_BOTTOM, "nothing_barrier"),
        INFINITE_NOTHING_BARRIER(NOTHING_BARRIER_TOP, NOTHING_BARRIER_BOTTOM, "infinite_nothing_barrier"),
        // 5. INFINITE BLACKNESS
        INFINITE_BLACKNESS(INFINITE_BLACKNESS_TOP, INFINITE_BLACKNESS_BOTTOM, "infinite_blackness"),
        EMPTINESS(FLOATING_VOID_ISLANDS_TOP, INFINITE_BLACKNESS_BOTTOM, "emptiness"),
        // 6. SPONGE MAZE - SIFT AREA
        SPONGE_MAZE(SPONGE_MAZE_TOP, SPONGE_MAZE_BOTTOM, "sponge_maze"),
        TIER_2_MENGER_SPONGE(SPONGE_MAZE_TOP, SPONGE_MAZE_BOTTOM, "menger_maze"),
        // 7. LUMINESCENT POOLS
        LUMINESCENT_POOLS(LUMINESCENT_POOLS_TOP, LUMINESCENT_POOLS_BOTTOM, "luminescent_pools"),
        TIER_1_GEL_HORIZON(LUMINESCENT_POOLS_TOP, -1450, "gel_horizon"),
        TIER_5_IRIDESCENT_GEL(LUMINESCENT_POOLS_TOP, LUMINESCENT_POOLS_BOTTOM, "iridescent_void"),
        // 8. ABYSSAL NIGHTMARE
        ABYSSAL_NIGHTMARE(ABYSSAL_NIGHTMARE_TOP, ABYSSAL_NIGHTMARE_BOTTOM, "abyssal_nightmare_dimension"),
        TIER_3_RIFT_FIELD(-1300, -1500, "rift_field"),
        TIER_4_DISPLACEMENT(-1500, -1700, "displacement_bands"),
        BOTTOM_FABRIC(ABYSSAL_NIGHTMARE_TOP, -1900, "bottom_fabric"),
        UNKNOWN(-1900, -2032, "unknown"),
        INNER_SPACE(-2032, -4000, "inner_space");

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
        if (y >= BEDROCK_LEVEL_BOTTOM) return Tier.BEDROCK_LEVEL;
        if (y >= FLOATING_VOID_ISLANDS_BOTTOM) return Tier.FLOATING_VOID_ISLANDS;
        if (y >= NOTHING_BARRIER_BOTTOM) return Tier.NOTHING_BARRIER;
        if (y >= INFINITE_BLACKNESS_BOTTOM) return Tier.INFINITE_BLACKNESS;
        if (y >= SPONGE_MAZE_BOTTOM) return Tier.SPONGE_MAZE;
        if (y >= LUMINESCENT_POOLS_BOTTOM) return Tier.LUMINESCENT_POOLS;
        return Tier.ABYSSAL_NIGHTMARE;
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
        return y <= BEDROCK_LEVEL_TOP && y >= BEDROCK_LEVEL_BOTTOM;
    }

    public static boolean isInEmptiness(int y) {
        return y < FLOATING_VOID_ISLANDS_TOP && y > INFINITE_BLACKNESS_BOTTOM;
    }

    public static boolean isInUnknown(int y) {
        return y <= ABYSSAL_NIGHTMARE_TOP && y >= ABYSSAL_NIGHTMARE_BOTTOM;
    }

    public static boolean shouldTriggerInnerSpace(int y) {
        return y < ABYSSAL_NIGHTMARE_BOTTOM;
    }

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
            case BEDROCK_LEVEL, FABRIC_OF_REALITY -> new float[]{0.15f, 0.15f, 0.18f}; // bedrock dark
            case FLOATING_VOID_ISLANDS -> new float[]{0.3f, 0.6f, 0.4f}; // floating islands green/blue
            case NOTHING_BARRIER, INFINITE_NOTHING_BARRIER -> new float[]{0.1f, 0.05f, 0.15f}; // surreal purple void
            case INFINITE_BLACKNESS, EMPTINESS -> new float[]{0.02f, 0.02f, 0.05f}; // pure black
            case SPONGE_MAZE, TIER_2_MENGER_SPONGE -> new float[]{1.0f, 0.6f, 0.2f}; // orange sponge
            case LUMINESCENT_POOLS, TIER_1_GEL_HORIZON, TIER_5_IRIDESCENT_GEL -> new float[]{0.2f, 0.8f, 1.0f}; // glowing cyan
            case ABYSSAL_NIGHTMARE, BOTTOM_FABRIC, UNKNOWN -> new float[]{0.4f, 0.1f, 0.6f}; // nightmare purple
            default -> new float[]{0.5f, 0.7f, 1.0f};
        };
    }

    public static boolean isInVoidDimension(int y) {
        return y <= FABRIC_TOP && y >= INNER_SPACE_TRIGGER;
    }

    public static float getFallTimeSeconds(int startY, int endY) {
        int distance = Math.abs(startY - endY);
        return distance / 78f;
    }
}
