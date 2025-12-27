package com.starmaylight.arcanotech_works.block.machine.mixer;

import com.starmaylight.arcanotech_works.block.machine.AbstractMachineBlock;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * 混合機ブロック
 * アイテムとアイテム、アイテムと液体、液体と液体を混合
 * 
 * バケツ操作:
 * - 液体入りバケツで右クリック: 入力タンクに液体を注入
 * - 空のバケツで右クリック: 出力タンクから液体を抽出
 */
public class MixerBlock extends AbstractMachineBlock {

    public MixerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixerBlockEntity(pos, state);
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

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MixerBlockEntity mixerBlockEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // バケツ操作を優先的にチェック
        if (isFluidContainer(heldItem)) {
            InteractionResult bucketResult = mixerBlockEntity.handleBucketInteraction(player, hand);
            if (bucketResult == InteractionResult.SUCCESS) {
                return bucketResult;
            }
        }

        // バケツ操作でなければGUIを開く
        NetworkHooks.openScreen((ServerPlayer) player, mixerBlockEntity, pos);
        return InteractionResult.CONSUME;
    }

    /**
     * アイテムが液体コンテナ（バケツなど）かどうかを判定
     */
    private boolean isFluidContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 空のバケツ
        if (stack.getItem() == Items.BUCKET) {
            return true;
        }
        
        // 液体入りバケツ
        if (stack.getItem() instanceof BucketItem) {
            return true;
        }
        
        // FluidHandler capabilityを持つアイテム
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }
}
