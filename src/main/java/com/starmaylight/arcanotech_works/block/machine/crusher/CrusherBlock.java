package com.starmaylight.arcanotech_works.block.machine.crusher;

import com.starmaylight.arcanotech_works.block.machine.AbstractMachineBlock;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 粉砕機ブロック
 * アイテムを粉砕して粉にする
 * 鉱石の場合は2つの粉を出力
 */
public class CrusherBlock extends AbstractMachineBlock {

    public CrusherBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrusherBlockEntity(pos, state);
    }

    @Override
    public Block getMachineFrameBlock(MachineTier tier) {
        return switch (tier) {
            case ADVANCED -> ModBlocks.MACHINE_FRAME_ADVANCED.get();
            case ELITE -> ModBlocks.MACHINE_FRAME_ELITE.get();
            case ULTIMATE -> ModBlocks.MACHINE_FRAME_ULTIMATE.get();
            default -> ModBlocks.MACHINE_FRAME_BASIC.get();
        };
    }
}
