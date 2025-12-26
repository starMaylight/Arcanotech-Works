package com.starmaylight.arcanotech_works.block.collector;

import com.starmaylight.arcanotech_works.api.mana.IManaStorage;
import com.starmaylight.arcanotech_works.api.mana.ManaStorage;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.capability.ManaCapability;
import com.starmaylight.arcanotech_works.world.manapool.ManaPoolSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 魔力収集機のBlockEntity
 * 魔力だまり内で無限に魔力を生成
 */
public class ManaCollectorBlockEntity extends BlockEntity {

    // 魔力ストレージ（受け取りは内部のみなので0、出力は200）
    private final ManaStorage manaStorage = new ManaStorage(50000, 0, 200);
    private final LazyOptional<IManaStorage> manaStorageLazy = LazyOptional.of(() -> manaStorage);

    // 魔力だまり内かどうか
    private boolean inManaPool = false;
    
    // 基本生成量（魔力だまり内で生成）
    private static final int BASE_GENERATION = 10;
    
    // チェック間隔（tick）
    private static final int CHECK_INTERVAL = 100;
    private int checkTimer = 0;

    public ManaCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_COLLECTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ManaCollectorBlockEntity entity) {
        entity.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide) return;

        // 定期的に魔力だまりチェック
        checkTimer++;
        if (checkTimer >= CHECK_INTERVAL) {
            checkTimer = 0;
            checkManaPool();
        }

        // 魔力だまり内なら魔力生成
        if (inManaPool) {
            // 直接魔力を追加（maxReceiveの制限を無視）
            int space = manaStorage.getMaxManaStored() - manaStorage.getManaStored();
            int toAdd = Math.min(BASE_GENERATION, space);
            if (toAdd > 0) {
                manaStorage.setMana(manaStorage.getManaStored() + toAdd);
                setChanged();
            }
        }

        // 隣接ブロックに魔力を送信
        transferManaToNeighbors();

        // 状態更新
        BlockState currentState = getBlockState();
        boolean shouldBeActive = inManaPool && manaStorage.getManaStored() > 0;
        if (currentState.getValue(ManaCollectorBlock.ACTIVE) != shouldBeActive) {
            level.setBlock(worldPosition, currentState.setValue(ManaCollectorBlock.ACTIVE, shouldBeActive), 3);
        }
    }

    /**
     * 魔力だまり内かチェック
     */
    public void checkManaPool() {
        if (level instanceof ServerLevel serverLevel) {
            ChunkPos chunkPos = new ChunkPos(worldPosition);
            ManaPoolSavedData data = ManaPoolSavedData.get(serverLevel);
            inManaPool = data.isManaPool(chunkPos);
            setChanged();
        }
    }

    /**
     * 隣接ブロックに魔力を送信
     */
    private void transferManaToNeighbors() {
        if (level == null) return;

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            
            if (neighbor != null) {
                neighbor.getCapability(ManaCapability.MANA, direction.getOpposite()).ifPresent(neighborMana -> {
                    int toTransfer = Math.min(manaStorage.getManaStored(), 100);
                    if (toTransfer > 0) {
                        int received = neighborMana.receiveMana(toTransfer, false);
                        if (received > 0) {
                            manaStorage.extractMana(received, false);
                            setChanged();
                        }
                    }
                });
            }
        }
    }

    /**
     * ステータスを表示
     */
    public void showStatus(Player player) {
        if (inManaPool) {
            player.displayClientMessage(
                    Component.translatable("message.arcanotech_works.collector_active",
                            manaStorage.getManaStored(), manaStorage.getMaxManaStored(), BASE_GENERATION)
                            .withStyle(ChatFormatting.GREEN),
                    true);
        } else {
            player.displayClientMessage(
                    Component.translatable("message.arcanotech_works.collector_inactive")
                            .withStyle(ChatFormatting.RED),
                    true);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Mana", manaStorage.getManaStored());
        tag.putBoolean("InManaPool", inManaPool);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        manaStorage.setMana(tag.getInt("Mana"));
        inManaPool = tag.getBoolean("InManaPool");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ManaCapability.MANA || cap == ForgeCapabilities.ENERGY) {
            return manaStorageLazy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        manaStorageLazy.invalidate();
    }
}
