package com.starmaylight.arcanotech_works.loot;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * ルートファンクション登録クラス
 */
public class ModLootFunctions {

    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Arcanotech_works.MODID);

    public static final RegistryObject<LootItemFunctionType> SET_MANA_GEM_QUALITY =
            LOOT_FUNCTIONS.register("set_mana_gem_quality",
                    () -> new LootItemFunctionType(new SetManaGemQualityFunction.Serializer()));

    public static void register(IEventBus eventBus) {
        LOOT_FUNCTIONS.register(eventBus);
    }
}
