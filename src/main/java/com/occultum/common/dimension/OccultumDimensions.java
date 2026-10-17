package com.occultum.common.dimension;

import com.occultum.OccultumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

/**
 * Chaves de Registro das 3 Dimensões da Sprint 4:
 * - Mundo Espiritual (Spirit World - 09_MUNDO_ESPIRITUAL_E_SONHOS.md)
 * - Dimensão do Tormento (Torment Dimension - 14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md)
 * - Dimensão do Espelho (Mirror Dimension - 14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md)
 */
public final class OccultumDimensions {

    public static final ResourceKey<Level> SPIRIT_WORLD_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "spirit_world"));
    public static final ResourceKey<DimensionType> SPIRIT_WORLD_DIM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "spirit_world"));
    public static final ResourceKey<LevelStem> SPIRIT_WORLD_STEM =
            ResourceKey.create(Registries.LEVEL_STEM, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "spirit_world"));

    public static final ResourceKey<Level> TORMENT_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "torment"));
    public static final ResourceKey<DimensionType> TORMENT_DIM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "torment"));
    public static final ResourceKey<LevelStem> TORMENT_STEM =
            ResourceKey.create(Registries.LEVEL_STEM, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "torment"));

    public static final ResourceKey<Level> MIRROR_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "mirror"));
    public static final ResourceKey<DimensionType> MIRROR_DIM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "mirror"));
    public static final ResourceKey<LevelStem> MIRROR_STEM =
            ResourceKey.create(Registries.LEVEL_STEM, ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "mirror"));

    private OccultumDimensions() {}
}
