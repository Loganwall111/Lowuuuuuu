// Sift Dimension Registration - KubeJS Startup
// Build #482 Infinite Sift Cosmos
// Extends Overworld down into five descending vertical layers

console.log("[MCSM Sift] Registering Sift dimension and entities...")

// Register custom dimension type via KubeJS (if using custom dimension mod)
// This is placeholder - actual dimension JSON should be in datapack

StartupEvents.registry('minecraft:dimension_type', event => {
    event.create('mcsm:sift_type')
        .ultrawarm(false)
        .natural(false)
        .piglinSafe(false)
        .respawnAnchorWorks(false)
        .bedWorks(false)
        .hasRaids(false)
        .hasSkylight(false)
        .hasCeiling(false)
        .coordinateScale(1.0)
        .logicalHeight(384)
        .minY(-2032)
        .height(2352) // from -2032 to 320
        .infiniburn('minecraft:infiniburn_overworld')
        .effects('minecraft:overworld')
})

StartupEvents.registry('entity_type', event => {
    // Ghost Whale - colossal multi-segmented passive flying entity
    event.create('mcsm:void_whale')
        .category('creature')
        .sized(8, 4)
        .fireImmune(true)
        .clientTrackingRange(128)
        .updateInterval(1)
    
    // Void Dweller - talking NPC with fish-like silhouette
    event.create('mcsm:void_dweller')
        .category('creature')
        .sized(0.8, 1.8)
        .clientTrackingRange(32)
})

// Void Rudder item
StartupEvents.registry('item', event => {
    event.create('mcsm:void_rudder')
        .displayName('Void Rudder')
        .tooltip('Engage to plummet through the Sift. Crouch + Use below Y=0 to enter void.')
        .tooltip('§dBrave the Unknown§r')
        .maxStackSize(1)
        .fireResistant(true)
        .glow(true)
        .useAnimation('bow')
})

// Sound events are registered via Java DeferredRegister - see McsmSiftSounds

console.log("[MCSM Sift] Tier definitions:")
console.log(" Tier 1 Gel Horizon: -251 to -700 - Ghost whales, god rays, floating spires")
console.log(" Tier 2 Menger Maze: -701 to -1250 - Orange-to-pink emissive sponge labyrinth")
console.log(" Tier 3 Rift Field: -1251 to -1500 - Wavy spacetime rifts with cosmic windows")
console.log(" Tier 4 Displacement: -1501 to -1800 - Wavy rainbow bands")
console.log(" Tier 5 Iridescent Gel: -1801 to -2032 - Rainbow water, glowing pools, zero collision")
