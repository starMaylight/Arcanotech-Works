package com.starmaylight.arcanotech_works.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 機械筐体ブロック
 * オーバーヒート時に機械がこのブロックに変化する
 * また、機械のクラフト素材としても使用
 */
public class MachineFrameBlock extends Block {

    private final MachineTier tier;

    // 少し小さめの当たり判定（フレーム状）
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 15, 15);

    public MachineFrameBlock(Properties properties, MachineTier tier) {
        super(properties);
        this.tier = tier;
    }

    public MachineTier getTier() {
        return tier;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
