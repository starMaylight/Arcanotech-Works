package com.starmaylight.arcanotech_works.registry;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.item.ManaGemItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * クリエイティブタブ登録クラス
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Arcanotech_works.MODID);

    public static final RegistryObject<CreativeModeTab> ARCANOTECH_TAB = CREATIVE_MODE_TABS.register("arcanotech_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.MANA_GEM.get()))
                    .title(Component.translatable("itemGroup.arcanotech_works.main"))
                    .displayItems((parameters, output) -> {
                        // 魔石（各クオリティのサンプル）
                        output.accept(ManaGemItem.createWithQuality(ModItems.MANA_GEM.get(), 50));
                        output.accept(ManaGemItem.createWithQuality(ModItems.MANA_GEM.get(), 250));
                        output.accept(ManaGemItem.createWithQuality(ModItems.MANA_GEM.get(), 500));
                        output.accept(ManaGemItem.createWithQuality(ModItems.MANA_GEM.get(), 750));
                        output.accept(ManaGemItem.createWithQuality(ModItems.MANA_GEM.get(), 1000));
                        
                        // 旧魔石（互換用）
                        output.accept(ModItems.CRUDE_MANA_STONE.get());
                        output.accept(ModItems.MANA_STONE.get());
                        output.accept(ModItems.REFINED_MANA_STONE.get());
                        output.accept(ModItems.PURE_MANA_STONE.get());
                        
                        // 素材
                        output.accept(ModItems.RAW_MITHRIL.get());
                        output.accept(ModItems.MITHRIL_NUGGET.get());
                        output.accept(ModItems.MITHRIL_INGOT.get());
                        output.accept(ModItems.MITHRIL_DUST.get());
                        
                        // ツール
                        output.accept(ModItems.ARCANE_PLATE.get());
                        output.accept(ModItems.ENGRAVING_TOOL.get());
                        output.accept(ModItems.MANA_SENSOR.get());
                        
                        // 刻印システム
                        output.accept(ModItems.DEGRADED_PLATE.get());
                        output.accept(ModItems.BASIC_CIRCUIT.get());
                        output.accept(ModItems.CONDUIT_CIRCUIT.get());
                        output.accept(ModItems.COLLECTOR_CIRCUIT.get());
                        output.accept(ModItems.REFINERY_CIRCUIT.get());
                        output.accept(ModItems.SENSOR_CIRCUIT.get());
                        
                        // 冷却アイテム
                        output.accept(ModItems.COOLING_FAN.get());
                        output.accept(ModItems.COOLING_CORE.get());
                        
                        // プレート
                        output.accept(ModItems.IRON_PLATE.get());
                        output.accept(ModItems.GOLD_PLATE.get());
                        output.accept(ModItems.COPPER_PLATE.get());
                        output.accept(ModItems.MITHRIL_PLATE.get());
                        
                        // ブロック
                        output.accept(ModBlocks.MITHRIL_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_MITHRIL_ORE.get());
                        output.accept(ModBlocks.MANA_CRYSTAL_ORE.get());
                        output.accept(ModBlocks.MITHRIL_BLOCK.get());
                        output.accept(ModBlocks.RAW_MITHRIL_BLOCK.get());
                    }).build());

    public static final RegistryObject<CreativeModeTab> MACHINES_TAB = CREATIVE_MODE_TABS.register("machines_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.MANA_GENERATOR.get()))
                    .title(Component.translatable("itemGroup.arcanotech_works.machines"))
                    .displayItems((parameters, output) -> {
                        // 魔力ネットワーク
                        output.accept(ModBlocks.MANA_CONDUIT.get());
                        
                        // 魔力機械
                        output.accept(ModBlocks.MANA_GENERATOR.get());
                        output.accept(ModBlocks.MANA_REFINERY.get());
                        output.accept(ModBlocks.MANA_COLLECTOR.get());
                        output.accept(ModBlocks.ENGRAVING_TABLE.get());
                        
                        // 機械筐体
                        output.accept(ModBlocks.MACHINE_FRAME_BASIC.get());
                        output.accept(ModBlocks.MACHINE_FRAME_ADVANCED.get());
                        output.accept(ModBlocks.MACHINE_FRAME_ELITE.get());
                        output.accept(ModBlocks.MACHINE_FRAME_ULTIMATE.get());
                        
                        // 工業機械
                        output.accept(ModBlocks.CRUSHER.get());
                        output.accept(ModBlocks.COMPRESSOR.get());
                        output.accept(ModBlocks.ROLLING_MILL.get());
                        output.accept(ModBlocks.MIXER.get());
                    }).build());

    public static final RegistryObject<CreativeModeTab> CONCEPTS_TAB = CREATIVE_MODE_TABS.register("concepts_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.PURE_MANA_STONE.get()))
                    .title(Component.translatable("itemGroup.arcanotech_works.concepts"))
                    .displayItems((parameters, output) -> {}).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
