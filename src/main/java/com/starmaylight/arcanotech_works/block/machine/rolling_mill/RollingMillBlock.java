package com.starmaylight.arcanotech_works.block.machine.rolling_mill;

import com.starmaylight.arcanotech_works.block.machine.AbstractMachineBlock;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 圧延機ブロック
 * インゴットをプレートに加工
 */
public class RollingMillBlock extends AbstractMachineBlock {

    public RollingMillBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RollingMillBlockEntity(pos, state);
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
