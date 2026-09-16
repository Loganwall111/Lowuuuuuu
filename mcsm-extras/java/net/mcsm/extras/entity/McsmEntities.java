package net.mcsm.extras.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * 1.9.205 -- registry for the Devouring Storms extra entities.
 * Registered from the base mod's onInitialize (McsmBuiltinPackMixin), i.e.
 * at the same point the base mod registers its own entity types.
 */
public final class McsmEntities {

    public static final Identifier STORY_CHARACTER_ID = Identifier.fromNamespaceAndPath("dabywitherstormmod", "story_character");

    public static EntityType<StoryCharacterEntity> STORY_CHARACTER;

    // ------------------------------------------------------------------
    // BUILD #456 -- THE TWO THAT LIVE IN THE NOTHING, as entities of their own
    // with their own ids, their own bodies and their own behaviour. The void
    // used to be populated with re-kitted zombies; these are the real ones.
    // ------------------------------------------------------------------
    public static final Identifier VOIDWALKER_ID = Identifier.fromNamespaceAndPath("mcsm", "voidwalker");
    public static final Identifier VOID_LURKER_ID = Identifier.fromNamespaceAndPath("mcsm", "void_lurker");

    public static EntityType<McsmVoidwalker> VOIDWALKER;
    public static EntityType<McsmVoidLurker> VOID_LURKER;

    // ------------------------------------------------------------------
    // BUILD #428 -- THE BEASTS. "The MASSG was added but it was just code. I
    // would like a mob named Mas. And the Creator, a gigantic entity. And the
    // whale monster I talked about." Three real, spawnable, named mobs, all of
    // them the same class with a different kind channel.
    // ------------------------------------------------------------------
    public static final Identifier MAS_ID = Identifier.fromNamespaceAndPath("mcsm", "mas");
    public static final Identifier CREATOR_ID = Identifier.fromNamespaceAndPath("mcsm", "creator");
    public static final Identifier WHALE_ID = Identifier.fromNamespaceAndPath("mcsm", "whale_monster");

    public static EntityType<McsmBeast> MAS;
    public static EntityType<McsmBeast> CREATOR;
    public static EntityType<McsmBeast> WHALE_MONSTER;

    /**
     * BUILD #428 -- the same type, read back out of the registry by name. The
     * storm's own summon path uses this: if the direct field is ever null (a
     * registry-order surprise, a reload), the beast is still found by its id
     * rather than the creature silently falling back to a vanilla body.
     */
    public static EntityType<?> MAS_ENTRY;
    public static EntityType<?> CREATOR_ENTRY;
    public static EntityType<?> WHALE_ENTRY;

    private static boolean done = false;

    private McsmEntities() {}

    public static void register() {
        if (done) {
            return;
        }
        done = true;
        try {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, STORY_CHARACTER_ID);
            EntityType<StoryCharacterEntity> type = EntityType.Builder
                    .of(StoryCharacterEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .eyeHeight(1.62F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build(key);
            STORY_CHARACTER = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
            FabricDefaultAttributeRegistry.register(STORY_CHARACTER, StoryCharacterEntity.createAttributes());

            // BUILD #428 -- the three beasts. Sizes are the user's own scale
            // words: Mas gigantic, the Creator beyond that, the whale big but
            // still a creature. Each keeps its own default attributes.
            MAS = beast(MAS_ID, 24.0F, 40.0F, 12, McsmBeast::masAttributes);
            CREATOR = beast(CREATOR_ID, 96.0F, 150.0F, 16, McsmBeast::creatorAttributes);
            WHALE_MONSTER = beast(WHALE_ID, 40.0F, 26.0F, 12, McsmBeast::whaleAttributes);
            MAS_ENTRY = MAS;
            CREATOR_ENTRY = CREATOR;
            WHALE_ENTRY = WHALE_MONSTER;

            // BUILD #456 -- the void's own two. Both are 2-blocks-and-change of
            // hitbox, tracked further than a vanilla mob needs to be, because a
            // player meets them across a dimension with no ground in it.
            VOIDWALKER = own(VOIDWALKER_ID, McsmVoidwalker::new, 0.7F, 2.1F, 12);
            if (VOIDWALKER != null) {
                FabricDefaultAttributeRegistry.register(VOIDWALKER, McsmVoidwalker.createAttributes());
            }
            VOID_LURKER = own(VOID_LURKER_ID, McsmVoidLurker::new, 2.4F, 3.4F, 16);
            if (VOID_LURKER != null) {
                FabricDefaultAttributeRegistry.register(VOID_LURKER, McsmVoidLurker.createAttributes());
            }
        } catch (Throwable t) {
            // never take the mod down over the cast; McsmNpcs falls back to villagers
            STORY_CHARACTER = null;
        }
    }

    /** One entity type of the extras' own, with the base mod's own builder calls. */
    private static <T extends net.minecraft.world.entity.Mob> EntityType<T> own(Identifier id,
            EntityType.EntityFactory<T> factory, float width, float height, int tracking) {
        try {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
            EntityType<T> type = EntityType.Builder
                    .of(factory, MobCategory.MONSTER)
                    .sized(width, height)
                    .eyeHeight(height * 0.7F)
                    .clientTrackingRange(tracking)
                    .updateInterval(3)
                    .build(key);
            return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
        } catch (Throwable t) {
            System.err.println("[ds] the mob " + id + " could not be registered: " + t);
            return null;
        }
    }

    /** One beast EntityType + its attributes, in the shape the cast already uses. */
    private static EntityType<McsmBeast> beast(Identifier id, float width, float height,
            int tracking, java.util.function.Supplier<net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder> attributes) {
        try {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
            EntityType<McsmBeast> type = EntityType.Builder
                    .of(McsmBeast::new, MobCategory.MONSTER)
                    .sized(width, height)
                    .eyeHeight(height * 0.7F)
                    .clientTrackingRange(tracking)
                    .updateInterval(3)
                    .build(key);
            EntityType<McsmBeast> registered = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
            FabricDefaultAttributeRegistry.register(registered, attributes.get());
            return registered;
        } catch (Throwable t) {
            System.err.println("[ds] the beast " + id + " could not be registered: " + t);
            return null;
        }
    }
}
