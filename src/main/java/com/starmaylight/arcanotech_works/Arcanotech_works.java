package com.starmaylight.arcanotech_works;

import com.mojang.logging.LogUtils;
import com.starmaylight.arcanotech_works.api.concept.ConceptRegistry;
import com.starmaylight.arcanotech_works.block.engraving.EngravingPatternLoader;
import com.starmaylight.arcanotech_works.block.engraving.EngravingPatterns;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.capability.ConceptCapability;
import com.starmaylight.arcanotech_works.capability.ManaCapability;
import com.starmaylight.arcanotech_works.client.screen.CompressorScreen;
import com.starmaylight.arcanotech_works.client.screen.CrusherScreen;
import com.starmaylight.arcanotech_works.client.screen.MixerScreen;
import com.starmaylight.arcanotech_works.client.screen.RollingMillScreen;
import com.starmaylight.arcanotech_works.client.screen.EngravingTableScreen;
import com.starmaylight.arcanotech_works.client.screen.ManaGeneratorScreen;
import com.starmaylight.arcanotech_works.client.screen.ManaRefineryScreen;
import com.starmaylight.arcanotech_works.loot.ModLootFunctions;
import com.starmaylight.arcanotech_works.network.ModNetwork;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import com.starmaylight.arcanotech_works.registry.ModCreativeTabs;
import com.starmaylight.arcanotech_works.registry.ModItems;
import com.starmaylight.arcanotech_works.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Arcano Tech Works - 工業と魔術を融合させたMod
 */
@Mod(Arcanotech_works.MODID)
public class Arcanotech_works {

    public static final String MODID = "arcanotech_works";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Arcanotech_works() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // レジストリ登録
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModLootFunctions.register(modEventBus);

        // ライフサイクルイベント
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        // Forgeイベントバス
        MinecraftForge.EVENT_BUS.register(this);

        // コンフィグ
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("Arcano Tech Works initialized!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            EngravingPatterns.init(); // デフォルトパターン初期化
        });
        LOGGER.info("Arcano Tech Works common setup complete");
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        ManaCapability.register(event);
        ConceptCapability.register(event);
        LOGGER.info("Arcano Tech Works capabilities registered");
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ConceptRegistry());
        event.addListener(new EngravingPatternLoader());
        LOGGER.info("Arcano Tech Works reload listeners added");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Arcano Tech Works server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenuTypes.MANA_GENERATOR.get(), ManaGeneratorScreen::new);
                MenuScreens.register(ModMenuTypes.MANA_REFINERY.get(), ManaRefineryScreen::new);
                MenuScreens.register(ModMenuTypes.ENGRAVING_TABLE.get(), EngravingTableScreen::new);
                MenuScreens.register(ModMenuTypes.CRUSHER.get(), CrusherScreen::new);
                MenuScreens.register(ModMenuTypes.COMPRESSOR.get(), CompressorScreen::new);
                MenuScreens.register(ModMenuTypes.ROLLING_MILL.get(), RollingMillScreen::new);
                MenuScreens.register(ModMenuTypes.MIXER.get(), MixerScreen::new);
            });
            LOGGER.info("Arcano Tech Works client setup complete");
        }
    }
}
