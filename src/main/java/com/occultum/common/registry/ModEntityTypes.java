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

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
