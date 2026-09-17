package net.mcsm.extras;

import net.mcsm.sift.entity.VoidDwellerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * MCSM NPCs - Revamped talking NPCs that go around world's in different parts of towns
 * Dialogue, animations, move, speak - really animated mouse arms, magazine really accurate in 3D eyes and mouth and bodies
 * Matches concept images: humanoid looking, stylized, expressive
 */
public class McsmNpcs {

    public static final DeferredRegister<EntityType<?>> NPC_ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "mcsm");

    // Town NPCs - humanoid, animated, with 3D eyes and mouth
    public static final RegistryObject<EntityType<VoidDwellerEntity>> TOWN_GUARD = NPC_ENTITIES.register("town_guard",
        () -> EntityType.Builder.of(VoidDwellerEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.9f)
            .clientTrackingRange(48)
            .build("town_guard"));

    public static final RegistryObject<EntityType<VoidDwellerEntity>> TOWN_MERCHANT = NPC_ENTITIES.register("town_merchant",
        () -> EntityType.Builder.of(VoidDwellerEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.9f)
            .clientTrackingRange(48)
            .build("town_merchant"));

    public static final RegistryObject<EntityType<VoidDwellerEntity>> TOWN_SAGE = NPC_ENTITIES.register("town_sage",
        () -> EntityType.Builder.of(VoidDwellerEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.9f)
            .clientTrackingRange(48)
            .build("town_sage"));

    // Sift-specific NPCs already registered in McsmSiftMod but referenced here for town integration
}
