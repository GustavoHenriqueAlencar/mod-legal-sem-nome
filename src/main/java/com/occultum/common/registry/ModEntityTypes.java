package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.entity.EntEntity;
import com.occultum.common.entity.MandrakeEntity;
import com.occultum.common.entity.OwlFamiliarEntity;
import com.occultum.common.entity.ToadFamiliarEntity;
import com.occultum.common.entity.TreefydEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, OccultumMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MandrakeEntity>> MANDRAKE =
            ENTITY_TYPES.register("mandrake", () -> EntityType.Builder.of(MandrakeEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.9F)
                    .build("mandrake"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntEntity>> ENT =
            ENTITY_TYPES.register("ent", () -> EntityType.Builder.of(EntEntity::new, MobCategory.MONSTER)
                    .sized(1.4F, 3.2F)
                    .build("ent"));

    public static final DeferredHolder<EntityType<?>, EntityType<TreefydEntity>> TREEFYD =
            ENTITY_TYPES.register("treefyd", () -> EntityType.Builder.of(TreefydEntity::new, MobCategory.CREATURE)
                    .sized(0.8F, 1.8F)
                    .build("treefyd"));

    public static final DeferredHolder<EntityType<?>, EntityType<ToadFamiliarEntity>> TOAD_FAMILIAR =
            ENTITY_TYPES.register("toad_familiar", () -> EntityType.Builder.of(ToadFamiliarEntity::new, MobCategory.CREATURE)
                    .sized(0.55F, 0.5F)
                    .build("toad_familiar"));

    public static final DeferredHolder<EntityType<?>, EntityType<OwlFamiliarEntity>> OWL_FAMILIAR =
            ENTITY_TYPES.register("owl_familiar", () -> EntityType.Builder.of(OwlFamiliarEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.7F)
                    .build("owl_familiar"));

    // Sprint 4: Vampirismo, Licantropia, Mundo Espiritual, Tormento e Espelho
    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.ElleEntity>> ELLE =
            ENTITY_TYPES.register("elle", () -> EntityType.Builder.of(com.occultum.common.entity.ElleEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .fireImmune()
                    .build("elle"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.LilithEntity>> LILITH =
            ENTITY_TYPES.register("lilith", () -> EntityType.Builder.of(com.occultum.common.entity.LilithEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.3F)
                    .fireImmune()
                    .build("lilith"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.AttackBatEntity>> ATTACK_BAT =
            ENTITY_TYPES.register("attack_bat", () -> EntityType.Builder.of(com.occultum.common.entity.AttackBatEntity::new, MobCategory.AMBIENT)
                    .sized(0.5F, 0.9F)
                    .build("attack_bat"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.VampireServantEntity>> VAMPIRE_SERVANT =
            ENTITY_TYPES.register("vampire_servant", () -> EntityType.Builder.of(com.occultum.common.entity.VampireServantEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build("vampire_servant"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.WolfmanEntity>> WOLFMAN =
            ENTITY_TYPES.register("wolfman", () -> EntityType.Builder.of(com.occultum.common.entity.WolfmanEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.2F)
                    .build("wolfman"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.HornedHuntsmanEntity>> HORNED_HUNTSMAN =
            ENTITY_TYPES.register("horned_huntsman", () -> EntityType.Builder.of(com.occultum.common.entity.HornedHuntsmanEntity::new, MobCategory.MONSTER)
                    .sized(1.1F, 2.8F)
                    .fireImmune()
                    .build("horned_huntsman"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.SpiritEntity>> SPIRIT =
            ENTITY_TYPES.register("spirit", () -> EntityType.Builder.of(com.occultum.common.entity.SpiritEntity::new, MobCategory.AMBIENT)
                    .sized(0.25F, 0.25F)
                    .build("spirit"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.CorpseEntity>> CORPSE =
            ENTITY_TYPES.register("corpse", () -> EntityType.Builder.of(com.occultum.common.entity.CorpseEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .build("corpse"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.NightmareEntity>> NIGHTMARE =
            ENTITY_TYPES.register("nightmare", () -> EntityType.Builder.of(com.occultum.common.entity.NightmareEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.1F)
                    .fireImmune()
                    .build("nightmare"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.LordOfTormentEntity>> LORD_OF_TORMENT =
            ENTITY_TYPES.register("lord_of_torment", () -> EntityType.Builder.of(com.occultum.common.entity.LordOfTormentEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.5F)
                    .fireImmune()
                    .build("lord_of_torment"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.ReflectionDemonEntity>> REFLECTION_DEMON =
            ENTITY_TYPES.register("reflection_demon", () -> EntityType.Builder.of(com.occultum.common.entity.ReflectionDemonEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .fireImmune()
                    .build("reflection_demon"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.occultum.common.entity.FairestFollowerEntity>> FAIREST_FOLLOWER =
            ENTITY_TYPES.register("fairest_follower", () -> EntityType.Builder.of(com.occultum.common.entity.FairestFollowerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build("fairest_follower"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
