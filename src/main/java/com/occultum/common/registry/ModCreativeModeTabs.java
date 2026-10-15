package com.occultum.common.registry;

import com.occultum.OccultumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OccultumMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> OCCULTUM_TAB =
            CREATIVE_MODE_TABS.register("occultum_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.occultum"))
                    .icon(() -> new ItemStack(ModItems.ALTAR.get()))
                    .displayItems((parameters, output) -> {
                        // Blocks & Altar
                        output.accept(ModItems.ALTAR.get());
                        output.accept(ModItems.CANDELABRA.get());
                        output.accept(ModItems.CHALICE.get());
                        output.accept(ModItems.CHALICE_FILLED.get());
                        output.accept(ModItems.KOBOLDITE_PENTACLE.get());
                        output.accept(ModItems.INFINITE_EGG.get());

                        // Trees & Woods
                        output.accept(ModItems.ROWAN_LOG.get());
                        output.accept(ModItems.ROWAN_WOOD.get());
                        output.accept(ModItems.STRIPPED_ROWAN_LOG.get());
                        output.accept(ModItems.STRIPPED_ROWAN_WOOD.get());
                        output.accept(ModItems.ROWAN_PLANKS.get());
                        output.accept(ModItems.ROWAN_LEAVES.get());
                        output.accept(ModItems.ROWAN_SAPLING.get());
                        output.accept(ModItems.ROWAN_BERRIES.get());

                        output.accept(ModItems.ALDER_LOG.get());
                        output.accept(ModItems.ALDER_WOOD.get());
                        output.accept(ModItems.STRIPPED_ALDER_LOG.get());
                        output.accept(ModItems.STRIPPED_ALDER_WOOD.get());
                        output.accept(ModItems.ALDER_PLANKS.get());
                        output.accept(ModItems.ALDER_LEAVES.get());
                        output.accept(ModItems.ALDER_SAPLING.get());

                        output.accept(ModItems.HAWTHORN_LOG.get());
                        output.accept(ModItems.HAWTHORN_WOOD.get());
                        output.accept(ModItems.STRIPPED_HAWTHORN_LOG.get());
                        output.accept(ModItems.STRIPPED_HAWTHORN_WOOD.get());
                        output.accept(ModItems.HAWTHORN_PLANKS.get());
                        output.accept(ModItems.HAWTHORN_LEAVES.get());
                        output.accept(ModItems.HAWTHORN_SAPLING.get());

                        // Crops & Seeds
                        output.accept(ModItems.BELLADONNA_SEEDS.get());
                        output.accept(ModItems.BELLADONNA_FLOWER.get());
                        output.accept(ModItems.MANDRAKE_SEEDS.get());
                        output.accept(ModItems.MANDRAKE_ROOT.get());
                        output.accept(ModItems.WATER_ARTICHOKE_SEEDS.get());
                        output.accept(ModItems.WATER_ARTICHOKE.get());
                        output.accept(ModItems.SNOWBELL_SEEDS.get());
                        output.accept(ModItems.SNOWBELL.get());
                        output.accept(ModItems.ICY_NEEDLE.get());
                        output.accept(ModItems.WORMWOOD_SEEDS.get());
                        output.accept(ModItems.WORMWOOD_LEAF.get());
                        output.accept(ModItems.WOLFSBANE_SEEDS.get());
                        output.accept(ModItems.WOLFSBANE_FLOWER.get());
                        output.accept(ModItems.MINDRAKE_SEEDS.get());
                        output.accept(ModItems.MINDRAKE_ROOT.get());
                        output.accept(ModItems.TREEFYD_SEED.get());

                        // Wild & Trap Plants
                        output.accept(ModItems.SPANISH_MOSS.get());
                        output.accept(ModItems.EMBER_MOSS.get());
                        output.accept(ModItems.GLINT_WEED.get());
                        output.accept(ModItems.BLOOD_ROSE.get());
                        output.accept(ModItems.CRITTER_SNARE.get());
                        output.accept(ModItems.VOID_BRAMBLE.get());
                        output.accept(ModItems.EMBER_BRAMBLE.get());
                        output.accept(ModItems.GRASSPER.get());
                        output.accept(ModItems.LEECH_CHEST.get());
                        output.accept(ModItems.WISPY_COTTON.get());

                        // Tools, Powders & Relics
                        output.accept(ModItems.MUTANDIS.get());
                        output.accept(ModItems.MUTANDIS_EXTREMIS.get());
                        output.accept(ModItems.MUTATING_SPRIG.get());
                        output.accept(ModItems.ATHAME.get());
                        output.accept(ModItems.MYSTIC_BRANCH.get());
                        output.accept(ModItems.ENT_BRANCH.get());
                        output.accept(ModItems.EARMUFFS.get());
                    })
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
