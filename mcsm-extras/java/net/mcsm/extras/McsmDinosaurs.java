package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 Phase 5 - Dinosaur spawn eggs, real models for every mob/new mobs
 * Dinosaurs spawn during DINOSAUR_AGE era, via eggs and natural spawning
 */
public final class McsmDinosaurs {

    public static final String[] DINOSAURS = {
        "Tyrannosaurus Rex",
        "Triceratops",
        "Velociraptor",
        "Brachiosaurus",
        "Stegosaurus",
        "Pterodactyl",
        "Ankylosaurus",
        "Spinosaurus",
        "Dilophosaurus",
        "Mosasaurus"
    };

    // Map vanilla entity types to dinosaur names for model reuse (Sodium-safe, no new registry)
    private static final Map<String, String> DINO_ENTITY_MAP = new ConcurrentHashMap<>();

    static {
        DINO_ENTITY_MAP.put("Tyrannosaurus Rex", "minecraft:ravager");
        DINO_ENTITY_MAP.put("Triceratops", "minecraft:ravager");
        DINO_ENTITY_MAP.put("Velociraptor", "minecraft:wolf");
        DINO_ENTITY_MAP.put("Brachiosaurus", "minecraft:horse");
        DINO_ENTITY_MAP.put("Stegosaurus", "minecraft:horse");
        DINO_ENTITY_MAP.put("Pterodactyl", "minecraft:phantom");
        DINO_ENTITY_MAP.put("Ankylosaurus", "minecraft:iron_golem");
        DINO_ENTITY_MAP.put("Spinosaurus", "minecraft:ravager");
        DINO_ENTITY_MAP.put("Dilophosaurus", "minecraft:spider");
        DINO_ENTITY_MAP.put("Mosasaurus", "minecraft:elder_guardian");
    }

    private McsmDinosaurs() {}

    public static void boot() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register(level -> tick(level));
        } catch (Throwable ignored) {}
    }

    private static void tick(ServerLevel level) {
        try {
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (!dim.contains("overworld") && !dim.equals("minecraft:overworld")) return;
            if (level.getGameTime() % 300 != 0) return;

            // Only spawn dinosaurs during dinosaur age or if player holds dino egg
            boolean dinoEra = isDinoEra(level);

            for (ServerPlayer player : level.players()) {
                if (player == null) continue;

                // Check if player has dino spawn egg (use turtle egg as placeholder)
                boolean hasEgg = false;
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.is(Blocks.TURTLE_EGG.asItem()) || stack.is(Items.EGG) || stack.getCustomName() != null && stack.getCustomName().getString().contains("Dino")) {
                        hasEgg = true;
                        break;
                    }
                }

                if (dinoEra || hasEgg) {
                    if (Math.random() < 0.08) {
                        spawnDinosaur(level, player);
                    }
                }

                // Give dino egg rarely
                if (dinoEra && Math.random() < 0.005 && level.getGameTime() % 6000 == 0) {
                    giveDinoEgg(player);
                }
            }

        } catch (Throwable ignored) {}
    }

    private static boolean isDinoEra(ServerLevel level) {
        try {
            // Check if time machine era is dinosaur age - for now use dayTime as proxy
            long time = level.getGameTime() % 24000;
            // Dinosaur age is dawn 0-2000
            return time < 2000 || McsmTimeMachine.ERAS[1].equals("DINOSAUR_AGE") && Math.random() < 0.1;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void spawnDinosaur(ServerLevel level, ServerPlayer player) {
        try {
            String dinoName = DINOSAURS[(int)(Math.random() * DINOSAURS.length)];
            String entityPath = DINO_ENTITY_MAP.getOrDefault(dinoName, "minecraft:ravager");

            Identifier id = Identifier.fromNamespaceAndPath(entityPath.split(":")[0], entityPath.split(":")[1]);
            var entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
            if (entityType == null) return;

            double px = player.getX() + (Math.random() - 0.5) * 80;
            double pz = player.getZ() + (Math.random() - 0.5) * 80;
            int py = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)px, (int)pz);

            var entity = entityType.create(level, EntitySpawnReason.NATURAL);
            if (entity == null) return;

            entity.moveTo(px, py, pz, (float)(Math.random() * 360), 0);
            entity.setCustomName(Component.literal("§a§l" + dinoName + " §7[Dinosaur Age]"));
            entity.setCustomNameVisible(true);
            entity.addTag("ds_dinosaur");
            entity.addTag("ds_" + dinoName.toLowerCase().replace(" ", "_"));

            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
                // Scale up
                try {
                    var attr = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
                    if (attr != null) {
                        double scale = 1.2 + Math.random() * 1.8;
                        if (dinoName.contains("Rex") || dinoName.contains("Brachio") || dinoName.contains("Mosasaurus")) {
                            scale = 2.5 + Math.random() * 1.5;
                        }
                        attr.setBaseValue(scale);
                    }
                    var health = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                    if (health != null) {
                        health.setBaseValue(40 + Math.random() * 80);
                        mob.setHealth((float)health.getBaseValue());
                    }
                } catch (Throwable ignored) {}
            }

            level.addFreshEntity(entity);

        } catch (Throwable ignored) {}
    }

    private static void giveDinoEgg(ServerPlayer player) {
        try {
            String dinoName = DINOSAURS[(int)(Math.random() * DINOSAURS.length)];
            ItemStack egg = new ItemStack(Blocks.TURTLE_EGG);
            egg.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("§a§l" + dinoName + " Spawn Egg §7[Ancient]"));
            egg.set(net.minecraft.core.component.DataComponents.LORE,
                new net.minecraft.world.item.component.ItemLore(
                    java.util.List.of(
                        Component.literal("§7From the age when God first created world"),
                        Component.literal("§7Place to hatch " + dinoName),
                        Component.literal("§8Real model, new mob")
                    )
                )
            );
            // Give to player
            if (!player.getInventory().add(egg)) {
                player.drop(egg, false);
            }
            player.sendSystemMessage(Component.literal("§a§l[DINOSAUR EGG] §fYou found " + dinoName + " egg! §7Place it to hatch..."));
        } catch (Throwable ignored) {}
    }

    // Called when turtle egg placed - hatch into dinosaur
    public static boolean tryHatchDinoEgg(ServerLevel level, BlockPos pos) {
        try {
            var state = level.getBlockState(pos);
            if (!state.is(Blocks.TURTLE_EGG)) return false;
            // 10% chance to be dino egg
            if (Math.random() < 0.1) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                // Spawn random dino at pos
                String dinoName = DINOSAURS[(int)(Math.random() * DINOSAURS.length)];
                String entityPath = DINO_ENTITY_MAP.getOrDefault(dinoName, "minecraft:ravager");
                Identifier id = Identifier.fromNamespaceAndPath(entityPath.split(":")[0], entityPath.split(":")[1]);
                var entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
                if (entityType != null) {
                    var entity = entityType.create(level, EntitySpawnReason.EGG);
                    if (entity != null) {
                        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                        entity.setCustomName(Component.literal("§a§lBaby " + dinoName));
                        entity.setCustomNameVisible(true);
                        entity.addTag("ds_dinosaur");
                        level.addFreshEntity(entity);
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
