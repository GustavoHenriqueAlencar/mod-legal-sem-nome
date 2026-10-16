package com.occultum.common.registry;

import com.occultum.OccultumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
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
                        for (DeferredHolder<Item, ? extends Item> entry : ModItems.ITEMS.getEntries()) {
                            output.accept(entry.get());
                        }
                    })
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
