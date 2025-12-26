package com.starmaylight.arcanotech_works.registry;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.item.ManaGemItem;
import com.starmaylight.arcanotech_works.item.ManaSensorItem;
import com.starmaylight.arcanotech_works.item.ManaStoneItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * アイテム登録クラス
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Arcanotech_works.MODID);

    // ========== 魔石（新システム） ==========

    /**
     * 魔石 - クオリティ1-1000の連続値を持つ
     * 採掘時は1-500、精錬後は最大1000
     */
    public static final RegistryObject<Item> MANA_GEM = ITEMS.register("mana_gem",
            () -> new ManaGemItem(new Item.Properties().stacksTo(64)));

    // ========== 旧魔石（後方互換/クラフト用） ==========

    public static final RegistryObject<Item> CRUDE_MANA_STONE = ITEMS.register("crude_mana_stone",
            () -> new ManaStoneItem(new Item.Properties().stacksTo(16), 100, 10));

    public static final RegistryObject<Item> MANA_STONE = ITEMS.register("mana_stone",
            () -> new ManaStoneItem(new Item.Properties().stacksTo(16), 500, 25));

    public static final RegistryObject<Item> REFINED_MANA_STONE = ITEMS.register("refined_mana_stone",
            () -> new ManaStoneItem(new Item.Properties().stacksTo(16), 2000, 100));

    public static final RegistryObject<Item> PURE_MANA_STONE = ITEMS.register("pure_mana_stone",
            () -> new ManaStoneItem(new Item.Properties().stacksTo(16), 10000, 500));

    // ========== 素材 ==========

    public static final RegistryObject<Item> MITHRIL_INGOT = ITEMS.register("mithril_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MITHRIL_NUGGET = ITEMS.register("mithril_nugget",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MITHRIL_DUST = ITEMS.register("mithril_dust",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RAW_MITHRIL = ITEMS.register("raw_mithril",
            () -> new Item(new Item.Properties()));

    // ========== ツール ==========

    public static final RegistryObject<Item> ARCANE_PLATE = ITEMS.register("arcane_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ENGRAVING_TOOL = ITEMS.register("engraving_tool",
            () -> new Item(new Item.Properties().durability(250)));

    // ========== 魔力だまり関連 ==========

    /**
     * 魔力感知器 - 魔力だまりを可視化するアイテム
     */
    public static final RegistryObject<Item> MANA_SENSOR = ITEMS.register("mana_sensor",
            () -> new ManaSensorItem(new Item.Properties().stacksTo(1)));

    // ========== 刻印システム ==========

    /**
     * 劣化した魔導板 - 刻印を失敗した際の出力
     */
    public static final RegistryObject<Item> DEGRADED_PLATE = ITEMS.register("degraded_plate",
            () -> new Item(new Item.Properties()));

    /**
     * 基礎魔導回路 - 基本的な機械に使用
     */
    public static final RegistryObject<Item> BASIC_CIRCUIT = ITEMS.register("basic_circuit",
            () -> new Item(new Item.Properties()));

    /**
     * 導線回路 - 魔導銀導線に使用
     */
    public static final RegistryObject<Item> CONDUIT_CIRCUIT = ITEMS.register("conduit_circuit",
            () -> new Item(new Item.Properties()));

    /**
     * 収集回路 - 魔力収集機に使用
     */
    public static final RegistryObject<Item> COLLECTOR_CIRCUIT = ITEMS.register("collector_circuit",
            () -> new Item(new Item.Properties()));

    /**
     * 精錬回路 - 魔石精錬機に使用
     */
    public static final RegistryObject<Item> REFINERY_CIRCUIT = ITEMS.register("refinery_circuit",
            () -> new Item(new Item.Properties()));

    /**
     * 感知回路 - 魔力感知器に使用
     */
    public static final RegistryObject<Item> SENSOR_CIRCUIT = ITEMS.register("sensor_circuit",
            () -> new Item(new Item.Properties()));

    // ========== 冷却システム ==========

    /**
     * 冷却ファン - 機械の冷却を促進し、オーバーヒートを回避
     */
    public static final RegistryObject<Item> COOLING_FAN = ITEMS.register("cooling_fan",
            () -> new Item(new Item.Properties().durability(500)));

    /**
     * 冷却コア - 機械の熱発生を無効化
     */
    public static final RegistryObject<Item> COOLING_CORE = ITEMS.register("cooling_core",
            () -> new Item(new Item.Properties().stacksTo(1)));

    // ========== プレート（圧延機出力） ==========

    /**
     * 鉄プレート
     */
    public static final RegistryObject<Item> IRON_PLATE = ITEMS.register("iron_plate",
            () -> new Item(new Item.Properties()));

    /**
     * 金プレート
     */
    public static final RegistryObject<Item> GOLD_PLATE = ITEMS.register("gold_plate",
            () -> new Item(new Item.Properties()));

    /**
     * 銅プレート
     */
    public static final RegistryObject<Item> COPPER_PLATE = ITEMS.register("copper_plate",
            () -> new Item(new Item.Properties()));

    /**
     * 魔導銀プレート
     */
    public static final RegistryObject<Item> MITHRIL_PLATE = ITEMS.register("mithril_plate",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
