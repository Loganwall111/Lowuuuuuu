package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * V2 Phase 5 - Roman empires kings knights medieval spawning only during time travel
 * Kings, knights, medieval mobs spawn only when time travel era is ROMAN_EMPIRE or MEDIEVAL_KINGS
 */
public final class McsmRomanEmpire {

    public static final String[] ROMAN_MOBS = {
        "Roman Emperor",
        "Roman Legionnaire",
        "Roman Centurion",
        "Gladiator",
        "Roman Senator"
    };

    public static final String[] MEDIEVAL_MOBS = {
        "Medieval King",
        "Medieval Queen",
        "Knight",
        "Crusader",
        "Medieval Archer",
        "Medieval Peasant",
        "Blacksmith",
        "Wizard"
    };

    private McsmRomanEmpire() {}

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
            if (level.getGameTime() % 200 != 0) return;

            String currentEra = getCurrentEra(level);
            boolean isRoman = currentEra.equals("ROMAN_EMPIRE");
            boolean isMedieval = currentEra.equals("MEDIEVAL_KINGS");

            if (!isRoman && !isMedieval) return;

            for (ServerPlayer player : level.players()) {
                if (player == null) continue;
                if (Math.random() < 0.12) {
                    if (isRoman) {
                        spawnRomanMob(level, player);
                    } else {
                        spawnMedievalMob(level, player);
                    }
                }
            }

        } catch (Throwable ignored) {}
    }

    private static String getCurrentEra(ServerLevel level) {
        try {
            // Use time as proxy for era, or check if near time machine
            long gameTime = level.getGameTime();
            long dayTime = gameTime % 24000;

            // If player near beacon/lodestone (time machine placeholder), force era
            // For demo, rotate eras every 10k ticks
            long eraIndex = (gameTime / 10000) % McsmTimeMachine.ERAS.length;
            String era = McsmTimeMachine.ERAS[(int)eraIndex];

            // Override: if night and near structure, spawn roman/medieval
            if (dayTime > 13000 && dayTime < 23000) {
                if (eraIndex % 3 == 0) return "ROMAN_EMPIRE";
                if (eraIndex % 3 == 1) return "MEDIEVAL_KINGS";
            }
            return era;
        } catch (Throwable ignored) {
            return "PRESENT_DAY";
        }
    }

    private static void spawnRomanMob(ServerLevel level, ServerPlayer player) {
        try {
            String mobName = ROMAN_MOBS[(int)(Math.random() * ROMAN_MOBS.length)];
            String entityPath = switch (mobName) {
                case "Roman Emperor" -> "minecraft:villager";
                case "Roman Legionnaire", "Roman Centurion", "Gladiator" -> "minecraft:iron_golem";
                case "Roman Senator" -> "minecraft:villager";
                default -> "minecraft:skeleton";
            };

            Identifier id = Identifier.fromNamespaceAndPath(entityPath.split(":")[0], entityPath.split(":")[1]);
            var entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
            if (entityType == null) return;

            double px = player.getX() + (Math.random() - 0.5) * 60;
            double pz = player.getZ() + (Math.random() - 0.5) * 60;
            int py = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)px, (int)pz);

            var entity = entityType.create(level, EntitySpawnReason.EVENT);
            if (entity == null) return;

            entity.snapTo(px, py, pz, (float)(Math.random() * 360), 0);
            entity.setCustomName(Component.literal("§6§l" + mobName + " §7[Roman Empire]"));
            entity.setCustomNameVisible(true);
            entity.addTag("ds_roman");
            entity.addTag("ds_time_travel_only");

            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
                // Equip armor for knights
                if (mobName.contains("Legionnaire") || mobName.contains("Centurion") || mobName.contains("Gladiator")) {
                    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                    mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                    mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
                    mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                }
                if (mobName.contains("Emperor")) {
                    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                    mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
                }
            }

            level.addFreshEntity(entity);

            // Build small Roman structure around
            if (Math.random() < 0.2) {
                buildRomanPillar(level, new BlockPos((int)px, py, (int)pz));
            }

        } catch (Throwable ignored) {}
    }

    private static void spawnMedievalMob(ServerLevel level, ServerPlayer player) {
        try {
            String mobName = MEDIEVAL_MOBS[(int)(Math.random() * MEDIEVAL_MOBS.length)];
            String entityPath = switch (mobName) {
                case "Medieval King" -> "minecraft:villager";
                case "Medieval Queen" -> "minecraft:villager";
                case "Knight", "Crusader" -> "minecraft:iron_golem";
                case "Medieval Archer" -> "minecraft:skeleton";
                case "Blacksmith" -> "minecraft:villager";
                case "Wizard" -> "minecraft:evoker";
                default -> "minecraft:villager";
            };

            Identifier id = Identifier.fromNamespaceAndPath(entityPath.split(":")[0], entityPath.split(":")[1]);
            var entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
            if (entityType == null) return;

            double px = player.getX() + (Math.random() - 0.5) * 60;
            double pz = player.getZ() + (Math.random() - 0.5) * 60;
            int py = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)px, (int)pz);

            var entity = entityType.create(level, EntitySpawnReason.EVENT);
            if (entity == null) return;

            entity.snapTo(px, py, pz, (float)(Math.random() * 360), 0);
            String color = mobName.contains("King") ? "§6" : mobName.contains("Knight") ? "§7" : "§e";
            entity.setCustomName(Component.literal(color + "§l" + mobName + " §7[Medieval]"));
            entity.setCustomNameVisible(true);
            entity.addTag("ds_medieval");
            entity.addTag("ds_time_travel_only");

            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
                if (mobName.contains("Knight") || mobName.contains("Crusader")) {
                    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
                    mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                    mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
                    mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
                    mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                }
                if (mobName.contains("King")) {
                    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                    mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
                    mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                }
                if (mobName.contains("Archer")) {
                    mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                }
            }

            level.addFreshEntity(entity);

            if (Math.random() < 0.15) {
                buildMedievalHouse(level, new BlockPos((int)px, py, (int)pz));
            }

        } catch (Throwable ignored) {}
    }

    private static void buildRomanPillar(ServerLevel level, BlockPos base) {
        try {
            for (int y = 0; y < 6; y++) {
                level.setBlock(base.offset(0, y, 0), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
                if (y == 0 || y == 5) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            if (x == 0 && z == 0) continue;
                            level.setBlock(base.offset(x, y, z), Blocks.QUARTZ_BLOCK.defaultBlockState(), 2);
                        }
                    }
                }
            }
            level.setBlock(base.offset(0, 6, 0), Blocks.QUARTZ_SLAB.defaultBlockState(), 2);
        } catch (Throwable ignored) {}
    }

    private static void buildMedievalHouse(ServerLevel level, BlockPos base) {
        try {
            int w = 5, d = 5, h = 4;
            for (int x = -w/2; x <= w/2; x++) {
                for (int z = -d/2; z <= d/2; z++) {
                    BlockPos ground = base.offset(x, 0, z);
                    if (Math.abs(x) == w/2 || Math.abs(z) == d/2) {
                        for (int y = 1; y <= h; y++) {
                            BlockPos pos = ground.offset(0, y, 0);
                            if (y == 1 && x == 0 && z == d/2) continue; // door
                            if (level.getBlockState(pos).isAir()) {
                                if (y < h) {
                                    level.setBlock(pos, Blocks.OAK_PLANKS.defaultBlockState(), 2);
                                } else {
                                    level.setBlock(pos, Blocks.OAK_SLAB.defaultBlockState(), 2);
                                }
                            }
                        }
                    } else {
                        if (level.getBlockState(ground).isAir()) {
                            level.setBlock(ground, Blocks.COBBLESTONE.defaultBlockState(), 2);
                        }
                    }
                }
            }
            // Roof
            for (int x = -w/2-1; x <= w/2+1; x++) {
                for (int z = -d/2-1; z <= d/2+1; z++) {
                    if (Math.abs(x) == w/2+1 || Math.abs(z) == d/2+1) {
                        level.setBlock(base.offset(x, h+1, z), Blocks.OAK_STAIRS.defaultBlockState(), 2);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
}
