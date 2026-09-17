package net.mcsm.sift.client;

import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.resources.ResourceLocation;

/**
 * Sift inspired sky texture for each area - sky panorama
 * Rainbow colored water with sparkles and sky panorama and sift inspired sky texture for each area
 * During skybox panorama is a couple jokest creatures and fish-like creatures and blue and pink grass blocks etc
 */
public final class SiftSkyTextures {

    public static final ResourceLocation FABRIC_SKY = new ResourceLocation("mcsm_sift", "textures/sky/fabric_sky.png");
    public static final ResourceLocation EMPTINESS_SKY = new ResourceLocation("mcsm_sift", "textures/sky/emptiness_sky.png");
    public static final ResourceLocation GEL_HORIZON_SKY = new ResourceLocation("mcsm_sift", "textures/sky/gel_horizon_sky.png");
    public static final ResourceLocation MENGER_MAZE_SKY = new ResourceLocation("mcsm_sift", "textures/sky/menger_maze_sky.png");
    public static final ResourceLocation RIFT_FIELD_SKY = new ResourceLocation("mcsm_sift", "textures/sky/rift_field_sky.png");
    public static final ResourceLocation DISPLACEMENT_SKY = new ResourceLocation("mcsm_sift", "textures/sky/displacement_sky.png");
    public static final ResourceLocation IRIDESCENT_GEL_SKY = new ResourceLocation("mcsm_sift", "textures/sky/iridescent_gel_sky.png");
    public static final ResourceLocation BOTTOM_FABRIC_SKY = new ResourceLocation("mcsm_sift", "textures/sky/bottom_fabric_sky.png");
    public static final ResourceLocation UNKNOWN_SKY = new ResourceLocation("mcsm_sift", "textures/sky/unknown_sky.png");

    public static final ResourceLocation RAINBOW_WATER = new ResourceLocation("mcsm_sift", "textures/block/rainbow_water.png");

    private SiftSkyTextures() {}

    public static ResourceLocation getSkyForTier(McsmVoidTiers.Tier tier) {
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
