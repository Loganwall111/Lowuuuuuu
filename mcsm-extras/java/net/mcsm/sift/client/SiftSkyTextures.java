package net.mcsm.sift.client;

import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.resources.Identifier;

/**
 * Sift inspired sky texture for each area - sky panorama
 */
public final class SiftSkyTextures {

    public static final Identifier FABRIC_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/fabric_sky.png");
    public static final Identifier EMPTINESS_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/emptiness_sky.png");
    public static final Identifier GEL_HORIZON_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/gel_horizon_sky.png");
    public static final Identifier MENGER_MAZE_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/menger_maze_sky.png");
    public static final Identifier RIFT_FIELD_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/rift_field_sky.png");
    public static final Identifier DISPLACEMENT_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/displacement_sky.png");
    public static final Identifier IRIDESCENT_GEL_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/iridescent_gel_sky.png");
    public static final Identifier BOTTOM_FABRIC_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/bottom_fabric_sky.png");
    public static final Identifier UNKNOWN_SKY = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/sky/unknown_sky.png");

    public static final Identifier RAINBOW_WATER = Identifier.fromNamespaceAndPath("mcsm_sift", "textures/block/rainbow_water.png");

    // V2 new skyboxes - animated insane VFX
    public static final Identifier BLACK_HOLE_SKY = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/black_hole_sky.png");
    public static final Identifier VOID_NEBULA_SKY = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/void_nebula_sky.png");
    public static final Identifier PRISMATIC_VOID_SKY = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/prismatic_void_sky.png");

    private SiftSkyTextures() {}

    public static Identifier getSkyForTier(McsmVoidTiers.Tier tier) {
        return switch (tier) {
            case FABRIC_OF_REALITY -> FABRIC_SKY;
            case EMPTINESS -> EMPTINESS_SKY;
            case TIER_1_GEL_HORIZON -> GEL_HORIZON_SKY;
            case TIER_2_MENGER_SPONGE -> MENGER_MAZE_SKY;
            case TIER_3_RIFT_FIELD -> RIFT_FIELD_SKY;
            case TIER_4_DISPLACEMENT -> DISPLACEMENT_SKY;
            case TIER_5_IRIDESCENT_GEL -> IRIDESCENT_GEL_SKY;
            case BOTTOM_FABRIC -> BOTTOM_FABRIC_SKY;
            case UNKNOWN -> UNKNOWN_SKY;
            default -> GEL_HORIZON_SKY;
        };
    }

    public static String getSkyDescription(McsmVoidTiers.Tier tier) {
        return switch (tier) {
            case FABRIC_OF_REALITY -> "Fabric of Reality - pitch black with stars, cosmic purple cracks glowing, End Gateway animated";
            case EMPTINESS -> "Emptiness - pitch black void of stars, fireworks, 40-50 sec fall, nothing";
            case TIER_1_GEL_HORIZON -> "Gel Horizon - cyan sky, god rays, floating spires, blue and pink grass blocks, fluor plants, fungus trees, rude vines, blue bun, jokest creatures and fish-like creatures, colossal octopus in distance";
            case TIER_2_MENGER_SPONGE -> "Menger Maze - orange to pink emissive + pitch black starry sponge, red rock, reddish stone, red grass, purple trees";
            case TIER_3_RIFT_FIELD -> "Rift Field - dark purple with neon-purple and hot-magenta rift rims, cosmic windows with star arrays";
            case TIER_4_DISPLACEMENT -> "Displacement Bands - rainbow wavy bands, displacement";
            case TIER_5_IRIDESCENT_GEL -> "Iridescent Gel - rainbow colored water with sparkles, teal amethyst magenta, black water and green acid pools, purple trees";
            case BOTTOM_FABRIC -> "Bottom Fabric - purple pink brown rainbow, color of skybox of brand new sift void dementia, leads to Unknown";
            case UNKNOWN -> "Unknown - bouncy distortion, ground decay, no bedrock, dark with purple decay, bouncy trampoline";
            default -> "Unknown tier";
        };
    }
}
