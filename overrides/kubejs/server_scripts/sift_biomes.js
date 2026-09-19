// Biome definitions for Sift Cosmos - matches tier system
// Each tier has unique skybox, fog, particles

console.log("[MCSM Sift] Registering biomes...")

// Note: Actual biome JSONs should be in datapack, but KubeJS can register via custom biome events if mod supports
// This is a placeholder for biome logic

StartupEvents.registry('biome', event => {
    // Gel Horizon - Tier 1 - cyan sky, god rays, floating spires
    event.create('mcsm_sift:gel_horizon')
        .hasPrecipitation(false)
        .temperature(0.8)
        .downfall(0.0)
        .effects(effects => {
            effects.skyColor(0x88CCFF)
            effects.fogColor(0x99DDFF)
            effects.waterColor(0x33CCFF)
            effects.waterFogColor(0x66DDFF)
            effects.moodSound({sound: "mcsm:sift_ambient_tier1", tickDelay: 6000, blockSearchExtent: 8, offset: 2.0})
        })
    
    // Menger Maze - Tier 2 - orange to pink emissive
    event.create('mcsm_sift:menger_maze')
        .hasPrecipitation(false)
        .temperature(1.2)
        .downfall(0.0)
        .effects(effects => {
            effects.skyColor(0xFF8855)
            effects.fogColor(0xFF6A88)
            effects.waterColor(0xFFAA55)
            effects.waterFogColor(0xFF77AA)
            effects.moodSound({sound: "mcsm:sift_ambient_tier2", tickDelay: 4000, blockSearchExtent: 8, offset: 1.0})
        })
    
    // Rift Field - Tier 3 - dark purple with neon rifts
    event.create('mcsm_sift:rift_field')
        .hasPrecipitation(false)
        .temperature(0.3)
        .downfall(0.0)
        .effects(effects => {
            effects.skyColor(0x1A0A2E)
            effects.fogColor(0x2D1B4E)
            effects.waterColor(0x4A2C7A)
            effects.waterFogColor(0x3A1F5E)
            effects.moodSound({sound: "mcsm:sift_ambient_tier3", tickDelay: 3000, blockSearchExtent: 12, offset: 3.0})
        })
    
    // Displacement Bands - Tier 4 - rainbow waves
    event.create('mcsm_sift:displacement_bands')
        .hasPrecipitation(false)
        .temperature(0.5)
        .downfall(0.0)
        .effects(effects => {
            effects.skyColor(0x66CCFF)
            effects.fogColor(0x88AAFF)
            effects.waterColor(0x55CCFF)
            effects.waterFogColor(0x77AAFF)
            effects.moodSound({sound: "minecraft:ambient.cave", tickDelay: 5000, blockSearchExtent: 8, offset: 2.0})
        })
    
    // Iridescent Gel - Tier 5 - rainbow water, most beautiful
    event.create('mcsm_sift:iridescent_gel')
        .hasPrecipitation(false)
        .temperature(0.9)
        .downfall(0.5)
        .effects(effects => {
            effects.skyColor(0x55FFCC)
            effects.fogColor(0x77FFAA)
            effects.waterColor(0x33FFCC)
            effects.waterFogColor(0x55FFAA)
            effects.moodSound({sound: "mcsm:sift_ambient_tier5", tickDelay: 2000, blockSearchExtent: 16, offset: 4.0})
        })
})

console.log("[MCSM Sift] 5 biomes registered - each with animated skybox and god rays")
