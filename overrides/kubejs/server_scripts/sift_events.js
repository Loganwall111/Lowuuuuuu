// Server events for Infinite Sift Cosmos
// Reality rifts open randomly, void dimension handling, NPC spawning

console.log("[MCSM Sift Server] Loading server events...")

// Prevent vanilla void damage in Sift during wrap
EntityEvents.hurt(event => {
    let entity = event.entity
    if (!entity.isPlayer()) return
    
    let y = entity.y
    if (y <= -251 && y >= -2032) {
        if (event.source.type === "outOfWorld" || event.source.type === "fall") {
            // Allow infinite fall - no damage during wrap
            if (y < -2030) {
                event.cancel()
                // Wrap will be handled by Java McsmVoidTiers
            }
        }
    }
})

// Spawn void whales and dwellers in Tier 1
LevelEvents.tick(event => {
    let level = event.level
    if (level.dimension.toString() !== "minecraft:overworld" && !level.dimension.toString().includes("sift")) return
    
    // Only server side
    if (level.isClientSide) return
    
    // Random rift spawning - like every been really really strange at occasions they just opened randomly
    if (Math.random() < 0.0005) {
        let players = level.players
        if (players.length > 0) {
            let player = players[Math.floor(Math.random() * players.length)]
            let py = player.y
            
            // Only in Tier 3-4 rift field
            if (py >= -1800 && py <= -1251) {
                let x = player.x + (Math.random() - 0.5) * 80
                let y = py + (Math.random() - 0.5) * 30
                let z = player.z + (Math.random() - 0.5) * 80
                
                // Spawn rift entity (would be custom entity)
                // level.createEntity("mcsm:sift_rift").at(x, y, z).spawn()
                console.log(`[Sift] Reality rift opening at ${x.toFixed(1)}, ${y.toFixed(1)}, ${z.toFixed(1)}`)
                
                // Play sound and particles
                level.runCommandSilent(`playsound minecraft:block.portal.ambient ambient @a ${x} ${y} ${z} 2 0.5`)
                level.runCommandSilent(`particle minecraft:portal ${x} ${y} ${z} 1 2 1 0.5 20`)
            }
        }
    }
})

// Void Rudder right-click handling via KubeJS (supplement to Java)
ItemEvents.rightClicked('mcsm:void_rudder', event => {
    let player = event.player
    let y = player.y
    
    // Below bedrock transition
    if (y < 0 && y > -251 && player.isCrouching()) {
        player.tell(Text.of("§dFalling into the Sift... Brave the unknown!").lightPurple())
        player.teleportTo(player.x, -260, player.z)
        player.setMotion(0, -0.8, 0)
        player.level.runCommandSilent(`playsound minecraft:block.portal.travel ambient @a ${player.x} ${player.y} ${player.z} 1 0.5`)
    }
})

// Talking NPCs around towns - spawn dwellers in villages
MoreJSEvents.villagerTrades(event => {
    // Add Sift-related trades to villagers
    // Lumen Keeper sells Void Rudder
})

// Custom spawning for Sift biomes
// This would be configured via datapack biome files, but KubeJS can supplement

console.log("[MCSM Sift Server] Tier 1 Gel Horizon: Ghost whales drift weightlessly")
console.log("[MCSM Sift Server] Tier 2 Menger Maze: Orange-to-pink emissive, 1101-1250")
console.log("[MCSM Sift Server] Tier 3 Rift Field: Reality rifts with cosmic windows")
console.log("[MCSM Sift Server] Tier 5 Iridescent Gel: Rainbow water pools, zero collision")
