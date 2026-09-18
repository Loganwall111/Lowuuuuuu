package net.mcsm.sift;

import net.mcsm.sift.block.BrokenFabricOfRealityBlock;
import net.mcsm.sift.block.FabricOfRealityBlock;
import net.mcsm.sift.block.SiftEcosystemBlocks;
import net.mcsm.sift.client.McsmSiftClient;
import net.mcsm.sift.entity.ColossalOctopusEntity;
import net.mcsm.sift.entity.JokestCreatureEntity;
import net.mcsm.sift.entity.VoidDwellerEntity;
import net.mcsm.sift.entity.VoidWhaleEntity;
import net.mcsm.sift.entity.rift.SiftRiftEntity;
import net.mcsm.sift.item.RealityKnifeItem;
import net.mcsm.sift.item.SiftSpawnEggs;
import net.mcsm.sift.util.McsmSiftSounds;
import net.mcsm.sift.world.McsmSiftDimension;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * MCSM Sift Mod - Infinite Sift Cosmos - Fabric version
 * Build #484 base + V2 animated skyboxes + black hole lensing + 30 new blocks
 * No Forge - uses BuiltInRegistries like McsmContent
 */
public final class McsmSiftMod {

    public static final String MODID = "mcsm_sift";

    public static final List<Block> ALL_BLOCKS = new ArrayList<>();
    public static final List<Item> ALL_ITEMS = new ArrayList<>();
    public static final List<Item> ALL_BLOCK_ITEMS = new ArrayList<>();
    public static final List<EntityType<?>> ALL_ENTITIES = new ArrayList<>();

    // Blocks - Fabric of Reality
    public static final Block FABRIC_OF_REALITY = block("fabric_of_reality",
        FabricOfRealityBlock::new,
        BlockBehaviour.Properties.of()
            .strength(-1f, 3600000f)
            .lightLevel(s -> 8)
    );
    public static final Block BROKEN_FABRIC = block("broken_fabric_of_reality",
        BrokenFabricOfRealityBlock::new,
        BlockBehaviour.Properties.of()
            .strength(-1f, 3600000f)
            .lightLevel(s -> 10)
            .noCollission()
    );
    public static final Block BOTTOM_FABRIC = block("bottom_fabric_of_reality",
        FabricOfRealityBlock::new,
        BlockBehaviour.Properties.of()
            .strength(-1f, 3600000f)
            .lightLevel(s -> 12)
    );
    public static final Block UNKNOWN_GROUND = block("unknown_ground",
        FabricOfRealityBlock::new,
        BlockBehaviour.Properties.of()
            .strength(2f, 10f)
            .lightLevel(s -> 5)
    );
    public static final Block MENGER_SPONGE = block("menger_sponge",
        FabricOfRealityBlock::new,
        BlockBehaviour.Properties.of()
            .strength(1f, 6f)
            .lightLevel(s -> 10)
    );
    public static final Block IRIDESCENT_GEL = block("iridescent_gel",
        Block::new,
        BlockBehaviour.Properties.of()
            .strength(0.5f, 2f)
            .lightLevel(s -> 12)
            .noOcclusion()
    );
    public static final Block RIFT_COSMIC = block("rift_cosmic",
        Block::new,
        BlockBehaviour.Properties.of()
            .strength(-1f, 3600000f)
            .lightLevel(s -> 15)
            .noOcclusion()
    );

    // Items
    public static final Item VOID_RUDDER = item("void_rudder",
        props -> new McsmVoidRudder(props.stacksTo(1).fireResistant()));
    public static final Item REALITY_KNIFE = item("reality_knife",
        props -> new RealityKnifeItem(props.stacksTo(1).durability(512).fireResistant()));

    // Entities
    public static final EntityType<VoidWhaleEntity> VOID_WHALE = entity("void_whale",
        EntityType.Builder.of(VoidWhaleEntity::new, MobCategory.CREATURE)
            .sized(8.0f, 4.0f)
            .clientTrackingRange(128)
            .updateInterval(1)
            .fireImmune()
    );
    public static final EntityType<VoidDwellerEntity> VOID_DWELLER = entity("void_dweller",
        EntityType.Builder.of(VoidDwellerEntity::new, MobCategory.CREATURE)
            .sized(0.8f, 1.8f)
            .clientTrackingRange(32)
            .updateInterval(2)
    );
    public static final EntityType<ColossalOctopusEntity> COLOSSAL_OCTOPUS = entity("colossal_octopus",
        EntityType.Builder.of(ColossalOctopusEntity::new, MobCategory.WATER_CREATURE)
            .sized(6.0f, 4.0f)
            .clientTrackingRange(64)
            .updateInterval(2)
            .fireImmune()
    );
    public static final EntityType<JokestCreatureEntity> JOKEST_CREATURE = entity("jokest_creature",
        EntityType.Builder.of(JokestCreatureEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 0.8f)
            .clientTrackingRange(32)
            .updateInterval(2)
    );
    public static final EntityType<SiftRiftEntity> SIFT_RIFT = entity("sift_rift",
        EntityType.Builder.<SiftRiftEntity>of(SiftRiftEntity::new, MobCategory.MISC)
            .sized(8f, 12f)
            .clientTrackingRange(64)
            .updateInterval(2)
    );

    // V2 NEW ENTITIES - more creatures/random textures for all dimensions
    public static final EntityType<JokestCreatureEntity> VOID_JELLY = entity("void_jelly",
        EntityType.Builder.of(JokestCreatureEntity::new, MobCategory.WATER_CREATURE)
            .sized(1.2f, 1.2f)
            .clientTrackingRange(48)
            .updateInterval(2)
    );
    public static final EntityType<JokestCreatureEntity> PRISMATIC_WISP = entity("prismatic_wisp",
        EntityType.Builder.of(JokestCreatureEntity::new, MobCategory.AMBIENT)
            .sized(0.4f, 0.4f)
            .clientTrackingRange(32)
            .updateInterval(1)
    );
    public static final EntityType<ColossalOctopusEntity> CRYSTAL_MANTA = entity("crystal_manta",
        EntityType.Builder.of(ColossalOctopusEntity::new, MobCategory.WATER_CREATURE)
            .sized(4.0f, 1.0f)
            .clientTrackingRange(64)
            .updateInterval(2)
    );
    public static final EntityType<VoidWhaleEntity> NEBULA_RAY = entity("nebula_ray",
        EntityType.Builder.of(VoidWhaleEntity::new, MobCategory.WATER_CREATURE)
            .sized(5.0f, 2.0f)
            .clientTrackingRange(96)
            .updateInterval(2)
    );
    public static final EntityType<JokestCreatureEntity> VOID_SNAIL = entity("void_snail",
        EntityType.Builder.of(JokestCreatureEntity::new, MobCategory.CREATURE)
            .sized(0.8f, 0.5f)
            .clientTrackingRange(32)
            .updateInterval(3)
    );

    private McsmSiftMod() {}

    public static void register() {
        System.out.println("[MCSM Sift] Initializing Infinite Sift Cosmos Engine - Build #7000.0.11-M MERGED VOID");
        System.out.println("[MCSM Sift] MERGED: Overworld min_y -2032 height 4064 = continuous fall, no loading screen, skybox merges slowly");
        System.out.println("[MCSM Sift] Fabric of Reality: Y -64 to -200 - pitch black with stars, cosmic purple, 12% broken for entry");
        System.out.println("[MCSM Sift] Emptiness: Y -200 to -600 - 400 blocks, 40-50 sec fall with fireworks, floating islands");
        System.out.println("[MCSM Sift] Tiers: " + McsmVoidTiers.NEW_TOTAL_HEIGHT + " blocks continuous (compressed from 3136 to 1968)");
        System.out.println("[MCSM Sift] Tier 1 Gel Horizon: " + McsmVoidTiers.TIER_1_GEL_HORIZON_TOP + " to " + McsmVoidTiers.TIER_1_GEL_HORIZON_BOTTOM + " - liquid where floats, god rays");
        System.out.println("[MCSM Sift] Tier 2 Menger Maze: " + McsmVoidTiers.TIER_2_SPONGE_MAZE_TOP + " to " + McsmVoidTiers.TIER_2_SPONGE_MAZE_BOTTOM + " - orange-to-pink + starry sponge");
        System.out.println("[MCSM Sift] Tier 3 Rift Field: " + McsmVoidTiers.TIER_3_RIFT_FIELD_TOP + " to " + McsmVoidTiers.TIER_3_RIFT_FIELD_BOTTOM);
        System.out.println("[MCSM Sift] Tier 4 Displacement: " + McsmVoidTiers.TIER_4_DISPLACEMENT_TOP + " to " + McsmVoidTiers.TIER_4_DISPLACEMENT_BOTTOM);
        System.out.println("[MCSM Sift] Tier 5 Iridescent Gel: " + McsmVoidTiers.TIER_5_GEL_VOID_TOP + " to " + McsmVoidTiers.TIER_5_GEL_VOID_BOTTOM);
        System.out.println("[MCSM Sift] Bottom Fabric: " + McsmVoidTiers.BOTTOM_FABRIC_TOP + " to " + McsmVoidTiers.BOTTOM_FABRIC_BOTTOM + " - rainbow");
        System.out.println("[MCSM Sift] Unknown: " + McsmVoidTiers.UNKNOWN_TOP + " to " + McsmVoidTiers.UNKNOWN_BOTTOM + " -> return to overworld sky");
        System.out.println("[MCSM Sift] Pocket dimension: DISABLE_VOID_SUFFOCATION=" + McsmVoidTiers.DISABLE_VOID_SUFFOCATION + " - no suffocation in void");
        System.out.println("[MCSM Sift] Cinematic: volumetric disintegration, 4 planets (Dungeons, Legends, Movie, Story Mode), warp drive, white maze, fog fade");
        System.out.println("[MCSM Sift] V2: Black hole backdrop lensing, animated skyboxes, 30 new blocks, 5 new creatures + merged void");

        // Register ecosystem blocks
        SiftEcosystemBlocks.register();
        // Register sounds
        McsmSiftSounds.register();
        // Register spawn eggs after entities
        try {
            SiftSpawnEggs.register();
            SiftSpawnEggs.registerEggs();
        } catch (Throwable t) {
            System.err.println("[sift] spawn eggs failed: " + t);
        }

        // Register V2 content
        try {
            Class<?> v2 = Class.forName("net.mcsm.sift.block.McsmSiftContent");
            v2.getMethod("register").invoke(null);
        } catch (Throwable t) {
            System.err.println("[sift] V2 content not yet present: " + t);
        }
    }

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("mcsm_sift", name));
        Block b;
        try {
            b = factory.apply(props.setId(key));
            Registry.register(BuiltInRegistries.BLOCK, key, b);
        } catch (Throwable t) {
            System.err.println("[sift] block mcsm_sift:" + name + " not registered: " + t);
            return null;
        }
        if (b == null) return null;
        ALL_BLOCKS.add(b);
        try {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("mcsm_sift", name));
            Item it = new BlockItem(b, new Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, it);
            ALL_BLOCK_ITEMS.add(it);
        } catch (Throwable t) {
            System.err.println("[sift] block item mcsm_sift:" + name + " not registered: " + t);
        }
        return b;
    }

    private static <T extends Item> T item(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("mcsm_sift", name));
        T it;
        try {
            it = factory.apply(new Item.Properties().setId(key));
            Registry.register(BuiltInRegistries.ITEM, key, it);
        } catch (Throwable t) {
            System.err.println("[sift] item mcsm_sift:" + name + " not registered: " + t);
            return null;
        }
        ALL_ITEMS.add(it);
        return it;
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> entity(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("mcsm_sift", name));
        EntityType<T> type = builder.build(key);
        try {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
            ALL_ENTITIES.add(type);
        } catch (Throwable t) {
            System.err.println("[sift] entity mcsm_sift:" + name + " not registered: " + t);
        }
        return type;
    }
}
