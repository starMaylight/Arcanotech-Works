package com.starmaylight.arcanotech_works.registry;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.block.collector.ManaCollectorBlock;
import com.starmaylight.arcanotech_works.block.conduit.ManaConduitBlock;
import com.starmaylight.arcanotech_works.block.engraving.EngravingTableBlock;
import com.starmaylight.arcanotech_works.block.machine.MachineFrameBlock;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.block.machine.compressor.CompressorBlock;
import com.starmaylight.arcanotech_works.block.machine.crusher.CrusherBlock;
import com.starmaylight.arcanotech_works.block.machine.mixer.MixerBlock;
import com.starmaylight.arcanotech_works.block.machine.rolling_mill.RollingMillBlock;
import com.starmaylight.arcanotech_works.block.generator.ManaGeneratorBlock;
import com.starmaylight.arcanotech_works.block.refinery.ManaRefineryBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * ブロック登録クラス
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Arcanotech_works.MODID);

    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Arcanotech_works.MODID);

    // ========== 鉱石 ==========

    public static final RegistryObject<Block> MITHRIL_ORE = registerBlock("mithril_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE),
                    UniformInt.of(2, 5)));

    public static final RegistryObject<Block> DEEPSLATE_MITHRIL_ORE = registerBlock("deepslate_mithril_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE).strength(4.5f, 3.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE),
                    UniformInt.of(3, 7)));

    public static final RegistryObject<Block> MANA_CRYSTAL_ORE = registerBlock("mana_crystal_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE).strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
                    .lightLevel(state -> 3),
                    UniformInt.of(3, 7)));

    // ========== 素材ブロック ==========

    public static final RegistryObject<Block> MITHRIL_BLOCK = registerBlock("mithril_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE).strength(5.0f, 6.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final RegistryObject<Block> RAW_MITHRIL_BLOCK = registerBlock("raw_mithril_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY).strength(4.0f, 5.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    // ========== 魔力ネットワーク ==========

    public static final RegistryObject<Block> MANA_CONDUIT = registerBlock("mana_conduit",
            () -> new ManaConduitBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.0f, 1.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .dynamicShape()));

    // ========== 機械 ==========

    /**
     * 魔石燃焼炉 - 魔石を消費して魔力を生成
     */
    public static final RegistryObject<Block> MANA_GENERATOR = registerBlock("mana_generator",
            () -> new ManaGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(ManaGeneratorBlock.LIT) ? 13 : 0)));

    /**
     * 魔石精錬機 - 魔力を消費して魔石のクオリティを上昇
     */
    public static final RegistryObject<Block> MANA_REFINERY = registerBlock("mana_refinery",
            () -> new ManaRefineryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(ManaRefineryBlock.ACTIVE) ? 7 : 0)));

    /**
     * 魔力収集機 - 魔力だまり内で無限に魔力を生成
     */
    public static final RegistryObject<Block> MANA_COLLECTOR = registerBlock("mana_collector",
            () -> new ManaCollectorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> state.getValue(ManaCollectorBlock.ACTIVE) ? 10 : 3)));

    // ========== 刻印システム ==========

    /**
     * 刻印台 - 魔導板に回路を彫刻
     */
    public static final RegistryObject<Block> ENGRAVING_TABLE = registerBlock("engraving_table",
            () -> new EngravingTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f, 2.5f)
                    .sound(SoundType.WOOD)));

    // ========== 機械筐体 ==========

    /**
     * 基礎機械筐体 - Tier1機械のクラフト素材/オーバーヒート結果
     */
    public static final RegistryObject<Block> MACHINE_FRAME_BASIC = registerBlock("machine_frame_basic",
            () -> new MachineFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion(), MachineTier.BASIC));

    /**
     * 強化機械筐体 - Tier2機械のクラフト素材/オーバーヒート結果
     */
    public static final RegistryObject<Block> MACHINE_FRAME_ADVANCED = registerBlock("machine_frame_advanced",
            () -> new MachineFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0f, 4.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion(), MachineTier.ADVANCED));

    /**
     * 高度機械筐体 - Tier3機械のクラフト素材/オーバーヒート結果
     */
    public static final RegistryObject<Block> MACHINE_FRAME_ELITE = registerBlock("machine_frame_elite",
            () -> new MachineFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0f, 5.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion(), MachineTier.ELITE));

    /**
     * 究極機械筐体 - Tier4機械のクラフト素材/オーバーヒート結果
     */
    public static final RegistryObject<Block> MACHINE_FRAME_ULTIMATE = registerBlock("machine_frame_ultimate",
            () -> new MachineFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(6.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion(), MachineTier.ULTIMATE));

    // ========== 工業機械 ==========

    /**
     * 粉砕機 - アイテムを粉砕して粉にする
     */
    public static final RegistryObject<Block> CRUSHER = registerBlock("crusher",
            () -> new CrusherBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(CrusherBlock.ACTIVE) ? 7 : 0)));

    /**
     * 圧縮機 - アイテムを圧縮してブロックや新アイテムに加工
     */
    public static final RegistryObject<Block> COMPRESSOR = registerBlock("compressor",
            () -> new CompressorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(CompressorBlock.ACTIVE) ? 7 : 0)));

    /**
     * 圧延機 - インゴットをプレートに加工
     */
    public static final RegistryObject<Block> ROLLING_MILL = registerBlock("rolling_mill",
            () -> new RollingMillBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(RollingMillBlock.ACTIVE) ? 7 : 0)));

    /**
     * 混合機 - アイテムと液体を混合
     */
    public static final RegistryObject<Block> MIXER = registerBlock("mixer",
            () -> new MixerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(MixerBlock.ACTIVE) ? 7 : 0)));

    // ========== ヘルパーメソッド ==========

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registeredBlock = BLOCKS.register(name, block);
        BLOCK_ITEMS.register(name, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));
        return registeredBlock;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
