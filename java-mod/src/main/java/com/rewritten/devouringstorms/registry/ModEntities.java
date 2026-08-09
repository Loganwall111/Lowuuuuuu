package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.entity.AnnaApparitionEntity;
import com.rewritten.devouringstorms.entity.FormidiBombEntity;
import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.entity.PreacherEntity;
import com.rewritten.devouringstorms.entity.SeveredStormEntity;
import com.rewritten.devouringstorms.entity.TazoEntity;
import com.rewritten.devouringstorms.entity.TownsfolkEntity;
import com.rewritten.devouringstorms.entity.WatcherEntity;
import com.rewritten.devouringstorms.entity.WitheredSymbiontEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** All Devouring Storms entities. */
public final class ModEntities {

    /** MASSG — Massive Abomination Sundering Storm Genesis. */
    public static final EntityType<MassgEntity> MASSG = register("massg",
        EntityType.Builder.of(MassgEntity::new, MobCategory.MONSTER)
            .sized(8.0f, 5.0f)
            .eyeHeight(2.5f)
            .fireImmune()
            .clientTrackingRange(12)
    );

    /** Severed storms — fragments torn off during the Sunderer phase. */
    public static final EntityType<SeveredStormEntity> SEVERED_STORM = register("severed_storm",
        EntityType.Builder.of(SeveredStormEntity::new, MobCategory.MONSTER)
            .sized(1.2f, 1.2f)
            .eyeHeight(0.6f)
            .clientTrackingRange(8)
    );

    /** Mutated thralls that MASSG spawns when it devolves. */
    public static final EntityType<WitheredSymbiontEntity> WITHERED_SYMBIONT = register("withered_symbiont",
        EntityType.Builder.of(WitheredSymbiontEntity::new, MobCategory.MONSTER)
            .sized(0.7f, 2.1f)
            .eyeHeight(1.85f)
            .clientTrackingRange(8)
    );

    /** The silent gaze. "Avoid the silent gaze of THE WATCHER." */
    public static final EntityType<WatcherEntity> WATCHER = register("watcher",
        EntityType.Builder.of(WatcherEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 2.6f)
            .eyeHeight(2.3f)
            .clientTrackingRange(10)
    );

    /** Alongside Tazo, you will enter a world where nothing is what it seems. */
    public static final EntityType<TazoEntity> TAZO = register("tazo",
        EntityType.Builder.of(TazoEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.9f)
            .eyeHeight(1.7f)
            .clientTrackingRange(10)
    );

    /** Last clergy of Endertown. "The storm is a mouth. The town is a prayer." */
    public static final EntityType<PreacherEntity> PREACHER = register("preacher",
        EntityType.Builder.of(PreacherEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.9f)
            .eyeHeight(1.7f)
            .clientTrackingRange(10)
    );

    /** The townsfolk who never left Endertown. */
    public static final EntityType<TownsfolkEntity> TOWNSFOLK = register("townsfolk",
        EntityType.Builder.of(TownsfolkEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.85f)
            .eyeHeight(1.65f)
            .clientTrackingRange(10)
    );

    /** Anna isn't real. This world is an illusion. */
    public static final EntityType<AnnaApparitionEntity> ANNA_APPARITION = register("anna_apparition",
        EntityType.Builder.of(AnnaApparitionEntity::new, MobCategory.MISC)
            .sized(0.6f, 1.9f)
            .noSave()
            .noSummon()
            .clientTrackingRange(8)
    );

    /** Frayspawn — storm mites. You won't meet one alone. */
    public static final EntityType<StormMiteEntity> STORM_MITE = register("storm_mite",
        EntityType.Builder.of(StormMiteEntity::new, MobCategory.MONSTER)
            .sized(0.55f, 0.45f)
            .clientTrackingRange(8)
    );

    /** What a villager becomes when the decay soaks through. */
    public static final EntityType<TheTakenEntity> THE_TAKEN = register("the_taken",
        EntityType.Builder.of(TheTakenEntity::new, MobCategory.MONSTER)
            .sized(0.7f, 1.85f)
            .clientTrackingRange(8)
    );

    /** Travis, minding the tear in the Fray. */
    public static final EntityType<TravisEntity> TRAVIS = register("travis",
        EntityType.Builder.of(TravisEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.85f)
            .clientTrackingRange(10)
    );

    /** Tonya, the echo remaining in the Echo Fields. */
    public static final EntityType<TonyaEntity> TONYA = register("tonya",
        EntityType.Builder.of(TonyaEntity::new, MobCategory.CREATURE)
            .sized(0.5f, 1.6f)
            .clientTrackingRange(10)
    );

    /** A black hole that got lost in the multiverse and liked the menu. */
    public static final EntityType<VoidMawEntity> VOID_MAW = register("void_maw",
        EntityType.Builder.of(VoidMawEntity::new, MobCategory.MONSTER)
            .sized(1.4f, 1.4f)
            .fireImmune()
            .clientTrackingRange(16)
    );

    /** THE CREATOR — the cosmos boss over the Cosmic Abyss. */
    public static final EntityType<CreatorEntity> CREATOR = register("creator",
        EntityType.Builder.of(CreatorEntity::new, MobCategory.MONSTER)
            .sized(7.5f, 20.0f)
            .fireImmune()
            .clientTrackingRange(32)
    );

    /** THE HAND — the part of the Creator that comes down a continent. */
    public static final EntityType<CreatorHandEntity> CREATOR_HAND = register("creator_hand",
        EntityType.Builder.of(CreatorHandEntity::new, MobCategory.MISC)
            .sized(6.0f, 8.0f)
            .noSave()
            .clientTrackingRange(32)
    );

    /** THE MONSTROSITY — moustache first, overtake everywhere. */
    public static final EntityType<MonstrosityEntity> MONSTROSITY = register("monstrosity",
        EntityType.Builder.of(MonstrosityEntity::new, MobCategory.MONSTER)
            .sized(2.2f, 4.2f)
            .clientTrackingRange(16)
    );

    /** THE FORGER — it spits tentacles out of the sky and opens doors nobody asked for. */
    public static final EntityType<ForgerEntity> FORGER = register("forger",
        EntityType.Builder.of(ForgerEntity::new, MobCategory.MONSTER)
            .sized(2.4f, 4.6f)
            .clientTrackingRange(16)
    );

    /** SKY TENTACLE — the Forger's signature at street level. */
    public static final EntityType<SkyTentacleEntity> SKY_TENTACLE = register("sky_tentacle",
        EntityType.Builder.of(SkyTentacleEntity::new, MobCategory.MONSTER)
            .sized(1.2f, 3.2f)
            .clientTrackingRange(16)
    );

    /** THE CART SHOPPER — aisle four, forever. */
    public static final EntityType<CartShopperEntity> CART_SHOPPER = register("cart_shopper",
        EntityType.Builder.of(CartShopperEntity::new, MobCategory.MONSTER)
            .sized(0.9f, 1.9f)
            .clientTrackingRange(12)
    );

    /** E.P.A. Researcher — top floor, most haunted field office. */
    public static final EntityType<ResearcherEntity> RESEARCHER = register("researcher",
        EntityType.Builder.of(ResearcherEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.85f)
            .clientTrackingRange(10)
    );

    /** THE EARTH EATER — a god that regards planets as a tasting menu. */
    public static final EntityType<EarthEaterEntity> EARTH_EATER = register("earth_eater",
        EntityType.Builder.of(EarthEaterEntity::new, MobCategory.MONSTER)
            .sized(9.0f, 12.0f)
            .fireImmune()
            .clientTrackingRange(32)
    );

    /** Thrown formidibomb projectile. */
    public static final EntityType<FormidiBombEntity> FORMIDI_BOMB = register("formidibomb",
        EntityType.Builder.<FormidiBombEntity>of(FormidiBombEntity::new, MobCategory.MISC)
            .sized(0.35f, 0.35f)
            .clientTrackingRange(4)
            .updateInterval(10)
    );

    private ModEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, DevouringStorms.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(MASSG, MassgEntity.createMassgAttributes());
        FabricDefaultAttributeRegistry.register(SEVERED_STORM, SeveredStormEntity.createSeveredAttributes());
        FabricDefaultAttributeRegistry.register(WITHERED_SYMBIONT, WitheredSymbiontEntity.createSymbiontAttributes());
        FabricDefaultAttributeRegistry.register(WATCHER, WatcherEntity.createWatcherAttributes());
        FabricDefaultAttributeRegistry.register(TAZO, TazoEntity.createTazoAttributes());
        FabricDefaultAttributeRegistry.register(ANNA_APPARITION, AnnaApparitionEntity.createAnnaAttributes());
        FabricDefaultAttributeRegistry.register(PREACHER, PreacherEntity.createPreacherAttributes());
        FabricDefaultAttributeRegistry.register(TOWNSFOLK, TownsfolkEntity.createTownsfolkAttributes());
        FabricDefaultAttributeRegistry.register(STORM_MITE, StormMiteEntity.createMiteAttributes());
        FabricDefaultAttributeRegistry.register(THE_TAKEN, TheTakenEntity.createTakenAttributes());
        FabricDefaultAttributeRegistry.register(TRAVIS, TravisEntity.createTravisAttributes());
        FabricDefaultAttributeRegistry.register(TONYA, TonyaEntity.createTonyaAttributes());
        FabricDefaultAttributeRegistry.register(VOID_MAW, VoidMawEntity.createMawAttributes());
        FabricDefaultAttributeRegistry.register(CREATOR, CreatorEntity.createCreatorAttributes());
        FabricDefaultAttributeRegistry.register(MONSTROSITY, MonstrosityEntity.createMonstrosityAttributes());
        FabricDefaultAttributeRegistry.register(FORGER, ForgerEntity.createForgerAttributes());
        FabricDefaultAttributeRegistry.register(SKY_TENTACLE, SkyTentacleEntity.createTentacleAttributes());
        FabricDefaultAttributeRegistry.register(CART_SHOPPER, CartShopperEntity.createShopperAttributes());
        FabricDefaultAttributeRegistry.register(RESEARCHER, ResearcherEntity.createResearcherAttributes());
        FabricDefaultAttributeRegistry.register(EARTH_EATER, EarthEaterEntity.createEaterAttributes());
    }
}
