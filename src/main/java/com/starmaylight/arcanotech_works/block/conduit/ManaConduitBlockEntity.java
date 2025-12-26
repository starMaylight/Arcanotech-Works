package com.starmaylight.arcanotech_works.block.conduit;

import com.starmaylight.arcanotech_works.api.mana.IManaStorage;
import com.starmaylight.arcanotech_works.api.mana.ManaStorage;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.capability.ManaCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 魔導銀導線のBlockEntity
 * 魔力の転送を担当する
 */
public class ManaConduitBlockEntity extends BlockEntity {

    // 内部バッファ（転送中の魔力を一時保持）
    private final ManaStorage internalBuffer;
    private final LazyOptional<IManaStorage> manaHandler;

    // 転送設定
    private static final int TRANSFER_RATE = 100;  // 1tickあたりの転送量
    private static final int BUFFER_CAPACITY = 200; // バッファ容量

    public ManaConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_CONDUIT.get(), pos, state);

        this.internalBuffer = new ManaStorage(BUFFER_CAPACITY, TRANSFER_RATE) {
            @Override
            protected void onManaChanged() {
                setChanged();
            }
        };
        this.manaHandler = LazyOptional.of(() -> internalBuffer);
    }

    /**
     * サーバー側のtick処理
     * 魔力の転送を行う
     */
    public void serverTick() {
        if (level == null || level.isClientSide()) return;

        // 接続先を収集
        List<TransferTarget> sources = new ArrayList<>();
        List<TransferTarget> destinations = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            BlockState state = getBlockState();
            if (!state.getValue(ManaConduitBlock.getPropertyForDirection(direction))) {
                continue; // この方向には接続していない
            }

            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);

            if (neighborBe == null) continue;

            // 隣が導線の場合はスキップ（導線同士は直接転送しない）
            if (neighborBe instanceof ManaConduitBlockEntity) {
                continue;
            }

            // 魔力Capabilityを持つブロックを分類
            neighborBe.getCapability(ManaCapability.MANA, direction.getOpposite()).ifPresent(storage -> {
                if (storage.canExtract() && storage.getManaStored() > 0) {
                    sources.add(new TransferTarget(storage, direction));
                }
                if (storage.canReceive() && storage.getManaStored() < storage.getMaxManaStored()) {
                    destinations.add(new TransferTarget(storage, direction));
                }
            });
        }

        // ソースから抽出してバッファに入れる
        for (TransferTarget source : sources) {
            int space = internalBuffer.getMaxManaStored() - internalBuffer.getManaStored();
            if (space <= 0) break;

            int extracted = source.storage.extractMana(Math.min(TRANSFER_RATE, space), false);
            if (extracted > 0) {
                internalBuffer.receiveMana(extracted, false);
            }
        }

        // バッファから宛先に転送
        if (internalBuffer.getManaStored() > 0 && !destinations.isEmpty()) {
            int perDestination = internalBuffer.getManaStored() / destinations.size();
            if (perDestination < 1) perDestination = 1;

            for (TransferTarget dest : destinations) {
                if (internalBuffer.getManaStored() <= 0) break;

                int toTransfer = Math.min(perDestination, internalBuffer.getManaStored());
                int accepted = dest.storage.receiveMana(toTransfer, false);
                if (accepted > 0) {
                    internalBuffer.extractMana(accepted, false);
                }
            }
        }

        // 隣接する導線にバッファの魔力を流す（ネットワーク伝播）
        if (internalBuffer.getManaStored() > 0) {
            distributeToConduits();
        }
    }

    /**
     * 隣接する導線に魔力を分配
     */
    private void distributeToConduits() {
        List<ManaConduitBlockEntity> neighbors = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            BlockState state = getBlockState();
            if (!state.getValue(ManaConduitBlock.getPropertyForDirection(direction))) {
                continue;
            }

            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);

            if (neighborBe instanceof ManaConduitBlockEntity conduit) {
                // 魔力が少ない導線に流す
                if (conduit.internalBuffer.getManaStored() < internalBuffer.getManaStored()) {
                    neighbors.add(conduit);
                }
            }
        }

        if (neighbors.isEmpty()) return;

        // 均等に分配
        int totalToShare = internalBuffer.getManaStored() / 2; // 半分を分配
        int perNeighbor = totalToShare / neighbors.size();
        if (perNeighbor < 1) return;

        for (ManaConduitBlockEntity neighbor : neighbors) {
            int diff = internalBuffer.getManaStored() - neighbor.internalBuffer.getManaStored();
            int toTransfer = Math.min(perNeighbor, diff / 2);
            if (toTransfer <= 0) continue;

            int extracted = internalBuffer.extractMana(toTransfer, false);
            neighbor.internalBuffer.receiveMana(extracted, false);
        }
    }

    // ========== Capability ==========

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ManaCapability.MANA) {
            return manaHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        manaHandler.invalidate();
    }

    // ========== NBT ==========

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Buffer", internalBuffer.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Buffer")) {
            internalBuffer.deserializeNBT(tag.getCompound("Buffer"));
        }
    }

    // ========== Utility ==========

    public int getStoredMana() {
        return internalBuffer.getManaStored();
    }

    public int getMaxMana() {
        return internalBuffer.getMaxManaStored();
    }

    /**
     * 転送先情報を保持する内部クラス
     */
    private record TransferTarget(IManaStorage storage, Direction direction) {}
}
