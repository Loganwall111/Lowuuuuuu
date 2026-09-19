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
    /**
     * BUILD #483 -- the dweller: the thing in the gel that talks. Its own type, so a
     * player can summon one by name, and its own attributes, because it is not a
     * monster and does not fight.
     */
    public static final Identifier VOID_DWELLER_ID = Identifier.fromNamespaceAndPath("mcsm", "void_dweller");
    // V2 -- sift creatures merged into main jar
    public static final Identifier COLOSSAL_OCTOPUS_ID = Identifier.fromNamespaceAndPath("mcsm", "colossal_octopus");
    public static final Identifier JOKEST_CREATURE_ID = Identifier.fromNamespaceAndPath("mcsm", "jokest_creature");
    // also the sift namespace ids, so datapack spawns find them
    public static final Identifier SIFT_OCTOPUS_ID = Identifier.fromNamespaceAndPath("mcsm_sift", "colossal_octopus");
    public static final Identifier SIFT_JOKEST_ID = Identifier.fromNamespaceAndPath("mcsm_sift", "jokest_creature");

    public static EntityType<McsmVoidwalker> VOIDWALKER;
    public static EntityType<VoidDwellerEntity> VOID_DWELLER;
    public static EntityType<McsmVoidLurker> VOID_LURKER;
    public static EntityType<McsmBeast> COLOSSAL_OCTOPUS;
    public static EntityType<McsmBeast> JOKEST_CREATURE;
    public static EntityType<McsmBeast> SIFT_OCTOPUS;
    public static EntityType<McsmBeast> SIFT_JOKEST;

    // ------------------------------------------------------------------
    // BUILD #460 -- AND THE TWO THAT LIVE IN THE WORLDS THE PLAYER BUILT.
    // The decayed reality's own creature and the infinite dimension's own
    // creature, so neither of them has to send a storm beast at the player.
    // ------------------------------------------------------------------
    public static final Identifier DRIFTER_ID = Identifier.fromNamespaceAndPath("mcsm", "drifter");
    public static final Identifier KEEPER_ID = Identifier.fromNamespaceAndPath("mcsm", "keeper");

    public static EntityType<McsmDenizen.McsmDrifter> DRIFTER;
    public static EntityType<McsmDenizen.McsmKeeper> KEEPER;

    // ------------------------------------------------------------------
    // BUILD #428 -- THE BEASTS. "The MASSG was added but it was just code. I
    // would like a mob named Mas. And the Creator, a gigantic entity. And the
    // whale monster I talked about." Three real, spawnable, named mobs, all of
    // them the same class with a different kind channel.
    // ------------------------------------------------------------------
    public static final Identifier MAS_ID = Identifier.fromNamespaceAndPath("mcsm", "mas");
    public static final Identifier CREATOR_ID = Identifier.fromNamespaceAndPath("mcsm", "creator");
    public static final Identifier WHALE_ID = Identifier.fromNamespaceAndPath("mcsm", "whale_monster");
    /**
     * BUILD #484 -- and the void's own whale, under the name the plan uses for it:
     * mcsm:void_whale. Same class, same whale body and the same drift as the whale
     * monster, but its own registry entry, so the thing in the luminous cavern can
     * be summoned, named and counted by itself.
     */
    public static final Identifier VOID_WHALE_ID = Identifier.fromNamespaceAndPath("mcsm", "void_whale");

    public static EntityType<McsmBeast> MAS;
    public static EntityType<McsmBeast> CREATOR;
    public static EntityType<McsmBeast> WHALE_MONSTER;
    public static EntityType<McsmBeast> VOID_WHALE;

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
            // BUILD #484 -- the plan's mcsm:void_whale: the same beast class and the
            // same whale attributes, under its own id.
            VOID_WHALE = beast(VOID_WHALE_ID, 40.0F, 26.0F, 12, McsmBeast::whaleAttributes);
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

            // BUILD #460 -- the decayed reality's creature and the infinite
            // dimension's creature. Each is registered with its own attributes
            // in the same call shape the two above use.
            // BUILD #483 -- and the one that talks. Same registration shape as the
            // two above: its own type, its own attributes, and it never fights.
            VOID_DWELLER = own(VOID_DWELLER_ID, VoidDwellerEntity::new, 0.9F, 2.0F, 16);
            if (VOID_DWELLER != null) {
                FabricDefaultAttributeRegistry.register(VOID_DWELLER, VoidDwellerEntity.createAttributes());
            }

            DRIFTER = own(DRIFTER_ID, McsmDenizen.McsmDrifter::new, 0.7F, 2.0F, 12);
            if (DRIFTER != null) {
                FabricDefaultAttributeRegistry.register(DRIFTER, McsmDenizen.drifterAttributes());
            }
            KEEPER = own(KEEPER_ID, McsmDenizen.McsmKeeper::new, 1.0F, 2.4F, 16);
            if (KEEPER != null) {
                FabricDefaultAttributeRegistry.register(KEEPER, McsmDenizen.keeperAttributes());
            }

            // V2 -- colossal octopus and jokest creature, both in mcsm: and mcsm_sift:
            COLOSSAL_OCTOPUS = beast(COLOSSAL_OCTOPUS_ID, 8.0F, 6.0F, 16, McsmBeast::whaleAttributes);
            JOKEST_CREATURE = beast(JOKEST_CREATURE_ID, 1.2F, 2.2F, 12, McsmBeast::masAttributes);
            SIFT_OCTOPUS = beast(SIFT_OCTOPUS_ID, 8.0F, 6.0F, 16, McsmBeast::whaleAttributes);
            SIFT_JOKEST = beast(SIFT_JOKEST_ID, 1.2F, 2.2F, 12, McsmBeast::masAttributes);
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
