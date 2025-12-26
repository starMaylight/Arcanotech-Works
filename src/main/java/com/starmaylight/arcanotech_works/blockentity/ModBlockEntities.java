package com.starmaylight.arcanotech_works.blockentity;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.block.collector.ManaCollectorBlockEntity;
import com.starmaylight.arcanotech_works.block.conduit.ManaConduitBlockEntity;
import com.starmaylight.arcanotech_works.block.engraving.EngravingTableBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.compressor.CompressorBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.crusher.CrusherBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.mixer.MixerBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.rolling_mill.RollingMillBlockEntity;
import com.starmaylight.arcanotech_works.block.generator.ManaGeneratorBlockEntity;
import com.starmaylight.arcanotech_works.block.refinery.ManaRefineryBlockEntity;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * BlockEntity登録クラス
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Arcanotech_works.MODID);

    /**
     * 魔導銀導線のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<ManaConduitBlockEntity>> MANA_CONDUIT =
            BLOCK_ENTITIES.register("mana_conduit",
                    () -> BlockEntityType.Builder.of(ManaConduitBlockEntity::new,
                            ModBlocks.MANA_CONDUIT.get()
                    ).build(null));

    /**
     * 魔石燃焼炉のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<ManaGeneratorBlockEntity>> MANA_GENERATOR =
            BLOCK_ENTITIES.register("mana_generator",
                    () -> BlockEntityType.Builder.of(ManaGeneratorBlockEntity::new,
                            ModBlocks.MANA_GENERATOR.get()
                    ).build(null));

    /**
     * 魔石精錬機のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<ManaRefineryBlockEntity>> MANA_REFINERY =
            BLOCK_ENTITIES.register("mana_refinery",
                    () -> BlockEntityType.Builder.of(ManaRefineryBlockEntity::new,
                            ModBlocks.MANA_REFINERY.get()
                    ).build(null));

    /**
     * 魔力収集機のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<ManaCollectorBlockEntity>> MANA_COLLECTOR =
            BLOCK_ENTITIES.register("mana_collector",
                    () -> BlockEntityType.Builder.of(ManaCollectorBlockEntity::new,
                            ModBlocks.MANA_COLLECTOR.get()
                    ).build(null));

    /**
     * 刻印台のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<EngravingTableBlockEntity>> ENGRAVING_TABLE =
            BLOCK_ENTITIES.register("engraving_table",
                    () -> BlockEntityType.Builder.of(EngravingTableBlockEntity::new,
                            ModBlocks.ENGRAVING_TABLE.get()
                    ).build(null));

    /**
     * 粉砕機のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER =
            BLOCK_ENTITIES.register("crusher",
                    () -> BlockEntityType.Builder.of(CrusherBlockEntity::new,
                            ModBlocks.CRUSHER.get()
                    ).build(null));

    /**
     * 圧縮機のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<CompressorBlockEntity>> COMPRESSOR =
            BLOCK_ENTITIES.register("compressor",
                    () -> BlockEntityType.Builder.of(CompressorBlockEntity::new,
                            ModBlocks.COMPRESSOR.get()
                    ).build(null));

    /**
     * 圧延機のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<RollingMillBlockEntity>> ROLLING_MILL =
            BLOCK_ENTITIES.register("rolling_mill",
                    () -> BlockEntityType.Builder.of(RollingMillBlockEntity::new,
                            ModBlocks.ROLLING_MILL.get()
                    ).build(null));

    /**
     * 混合機のBlockEntity
     */
    public static final RegistryObject<BlockEntityType<MixerBlockEntity>> MIXER =
            BLOCK_ENTITIES.register("mixer",
                    () -> BlockEntityType.Builder.of(MixerBlockEntity::new,
                            ModBlocks.MIXER.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
