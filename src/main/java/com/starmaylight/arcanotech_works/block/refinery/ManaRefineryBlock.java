package com.starmaylight.arcanotech_works.block.refinery;

import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * 魔石精錬機ブロック
 * 魔力を消費して魔石のクオリティを上昇させる
 */
public class ManaRefineryBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ManaRefineryBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ManaRefineryBlockEntity refinery) {
            NetworkHooks.openScreen((ServerPlayer) player, refinery, pos);
        }

        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaRefineryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == ModBlockEntities.MANA_REFINERY.get()
                ? (lvl, pos, st, be) -> ((ManaRefineryBlockEntity) be).serverTick()
                : null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;

            // 魔法的なパーティクル
            if (random.nextDouble() < 0.4) {
                level.addParticle(ParticleTypes.ENCHANT,
                        x + (random.nextDouble() - 0.5) * 0.6,
                        y + random.nextDouble() * 0.3,
                        z + (random.nextDouble() - 0.5) * 0.6,
                        0, 0.1, 0);
            }

            // 時々キラキラ
            if (random.nextDouble() < 0.15) {
                level.addParticle(ParticleTypes.END_ROD,
                        x + (random.nextDouble() - 0.5) * 0.4,
                        y + 0.2,
                        z + (random.nextDouble() - 0.5) * 0.4,
                        0, 0.02, 0);
            }

            // 音
            if (random.nextDouble() < 0.05) {
                level.playLocalSound(x, y, z,
                        SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.BLOCKS, 0.3f, 1.5f, false);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaRefineryBlockEntity refinery) {
                refinery.dropContents();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
