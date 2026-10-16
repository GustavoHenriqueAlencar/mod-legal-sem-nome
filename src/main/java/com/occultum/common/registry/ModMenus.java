package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.altar.AltarMenu;
import com.occultum.common.machine.distillery.DistilleryMenu;
import com.occultum.common.machine.oven.WitchesOvenMenu;
import com.occultum.common.machine.spinningwheel.SpinningWheelMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
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

    public static final DeferredHolder<MenuType<?>, MenuType<WitchesOvenMenu>> WITCHES_OVEN =
            MENUS.register("witches_oven", () -> IMenuTypeExtension.create(WitchesOvenMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<WitchesOvenMenu>> WITCHES_OVEN_MENU = WITCHES_OVEN;

    public static final DeferredHolder<MenuType<?>, MenuType<DistilleryMenu>> DISTILLERY =
            MENUS.register("distillery", () -> IMenuTypeExtension.create(DistilleryMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<DistilleryMenu>> DISTILLERY_MENU = DISTILLERY;

    public static final DeferredHolder<MenuType<?>, MenuType<SpinningWheelMenu>> SPINNING_WHEEL =
            MENUS.register("spinning_wheel", () -> IMenuTypeExtension.create(SpinningWheelMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SpinningWheelMenu>> SPINNING_WHEEL_MENU = SPINNING_WHEEL;

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
