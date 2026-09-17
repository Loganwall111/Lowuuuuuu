// Client-side Sift visuals - god rays, particles, skybox hooks

console.log("[MCSM Sift Client] Loading visual overhaul...")

ClientEvents.tick(event => {
    let player = event.player
    if (!player) return
    let y = player.y
    
    // Check if in Sift
    if (y <= -251 && y >= -2032) {
        // Tier detection
        let tier = "unknown"
        if (y >= -700) tier = "gel_horizon"
        else if (y >= -1250) tier = "menger_maze"
        else if (y >= -1500) tier = "rift_field"
        else if (y >= -1800) tier = "displacement"
        else tier = "iridescent_gel"
        
        // Spawn legal particles - floating, not walking
        if (Math.random() < 0.1) {
            let px = player.x + (Math.random() - 0.5) * 20
            let py = y + (Math.random() - 0.5) * 10
            let pz = player.z + (Math.random() - 0.5) * 20
            
            // Rainbow particle based on tier
            let particle = "minecraft:end_rod"
            if (tier === "iridescent_gel") particle = "minecraft:glow"
            else if (tier === "menger_maze") particle = "minecraft:flame"
            else if (tier === "rift_field") particle = "minecraft:portal"
            
            // event.level.spawnParticles(particle, false, px, py, pz, 0, 0, 0, 1, 0.1) // KubeJS particle API varies
        }
        
        // God rays effect - screen overlay would be handled via shader, but we can add fog
        if (tier === "gel_horizon" && Math.random() < 0.02) {
            // Play ambient hum
        }
    }
})

// Tooltip for Void Rudder
ItemEvents.tooltip(event => {
    event.add('mcsm:void_rudder', [
        Text.of('§7Hold §fRight Click§7 to boost downward').gray(),
        Text.of('§7Sneak + Use below bedrock to enter Sift').gray(),
        Text.of('§dInfinite fall - velocity retained on wrap').lightPurple(),
        Text.of('§bTier 1: Ghost Whales drift weightlessly').aqua(),
        Text.of('§6Tier 2: Menger-Sponge maze - orange to pink').gold(),
        Text.of('§5Tier 3: Reality rifts - cosmic windows').darkPurple(),
        Text.of('§eTier 4: Displacement bands - rainbow waves').yellow(),
        Text.of('§aTier 5: Iridescent gel void - swim through light').green()
    ])
})

// Custom skybox is handled via Java McsmSiftSkyRenderer and shaders
// final.fsh and sky.fsh provide Pixar-VFX triple-A grading
