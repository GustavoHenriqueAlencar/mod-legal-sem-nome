package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.altar.AltarMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, OccultumMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<AltarMenu>> ALTAR_MENU =
            MENUS.register("altar_menu", () -> IMenuTypeExtension.create(AltarMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
