package com.starmaylight.arcanotech_works.registry;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.block.engraving.EngravingTableMenu;
import com.starmaylight.arcanotech_works.block.machine.compressor.CompressorMenu;
import com.starmaylight.arcanotech_works.block.machine.crusher.CrusherMenu;
import com.starmaylight.arcanotech_works.block.machine.mixer.MixerMenu;
import com.starmaylight.arcanotech_works.block.machine.rolling_mill.RollingMillMenu;
import com.starmaylight.arcanotech_works.menu.ManaGeneratorMenu;
import com.starmaylight.arcanotech_works.menu.ManaRefineryMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * メニュータイプ登録クラス
 */
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Arcanotech_works.MODID);

    public static final RegistryObject<MenuType<ManaGeneratorMenu>> MANA_GENERATOR =
            registerMenu("mana_generator", ManaGeneratorMenu::new);

    public static final RegistryObject<MenuType<ManaRefineryMenu>> MANA_REFINERY =
            registerMenu("mana_refinery", ManaRefineryMenu::new);

    public static final RegistryObject<MenuType<EngravingTableMenu>> ENGRAVING_TABLE =
            registerMenu("engraving_table", EngravingTableMenu::new);

    public static final RegistryObject<MenuType<CrusherMenu>> CRUSHER =
            registerMenu("crusher", CrusherMenu::new);

    public static final RegistryObject<MenuType<CompressorMenu>> COMPRESSOR =
            registerMenu("compressor", CompressorMenu::new);

    public static final RegistryObject<MenuType<RollingMillMenu>> ROLLING_MILL =
            registerMenu("rolling_mill", RollingMillMenu::new);

    public static final RegistryObject<MenuType<MixerMenu>> MIXER =
            registerMenu("mixer", MixerMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenu(
            String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
