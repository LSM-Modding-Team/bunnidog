package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.menu.CareMenu;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private ModMenus() {}
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,BunnidogsMod.MODID);
    public static final DeferredHolder<MenuType<?>,MenuType<CareMenu>> CARE=MENUS.register("care",()->IMenuTypeExtension.create(CareMenu::new));
}
