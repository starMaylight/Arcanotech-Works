package com.starmaylight.arcanotech_works.block.generator;

import com.starmaylight.arcanotech_works.api.mana.IManaStorage;
import com.starmaylight.arcanotech_works.api.mana.ManaStorage;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.capability.ManaCapability;
import com.starmaylight.arcanotech_works.item.ManaGemItem;
import com.starmaylight.arcanotech_works.menu.ManaGeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 魔石燃焼炉のBlockEntity
 * 魔石を消費して魔力を生成する
 */
public class ManaGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    // 設定
    private static final int MANA_CAPACITY = 10000;
    private static final int MANA_OUTPUT_RATE = 100; // tick毎の出力
    private static final int BURN_TIME_PER_QUALITY = 2; // クオリティ1あたりの燃焼tick

    // インベントリ（魔石スロット1つ）
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof ManaGemItem;
        }
    };

    // 魔力ストレージ
    private final ManaStorage manaStorage = new ManaStorage(MANA_CAPACITY, 0, MANA_OUTPUT_RATE) {
        @Override
        protected void onManaChanged() {
            setChanged();
        }
    };

    // Capability
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);
    private final LazyOptional<IManaStorage> manaHandler = LazyOptional.of(() -> manaStorage);

    // 燃焼状態
    private int burnTime = 0;
    private int burnTimeTotal = 0;
    private int currentManaGeneration = 0;

    // GUI用データ同期
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> burnTimeTotal;
                case 2 -> manaStorage.getManaStored();
                case 3 -> manaStorage.getMaxManaStored();
                case 4 -> currentManaGeneration;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> burnTimeTotal = value;
                case 2 -> manaStorage.setMana(value);
                case 4 -> currentManaGeneration = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public ManaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_GENERATOR.get(), pos, state);
    }

    /**
     * サーバー側のtick処理
     */
    public void serverTick() {
        if (level == null || level.isClientSide()) return;

        boolean wasLit = burnTime > 0;
        boolean changed = false;

        // 燃焼中
        if (burnTime > 0) {
            burnTime--;

            // 魔力生成
            int generated = Math.min(currentManaGeneration, 
                    manaStorage.getMaxManaStored() - manaStorage.getManaStored());
            if (generated > 0) {
                manaStorage.setMana(manaStorage.getManaStored() + generated);
                changed = true;
            }
        }

        // 新しい魔石を消費
        if (burnTime <= 0 && manaStorage.getManaStored() < manaStorage.getMaxManaStored()) {
            ItemStack fuel = inventory.getStackInSlot(0);
            if (!fuel.isEmpty() && fuel.getItem() instanceof ManaGemItem) {
                int quality = ManaGemItem.getQuality(fuel);
                int manaOutput = ManaGemItem.getManaOutput(fuel);

                // 燃焼開始
                burnTimeTotal = quality * BURN_TIME_PER_QUALITY;
                burnTime = burnTimeTotal;
                currentManaGeneration = manaOutput / Math.max(1, burnTimeTotal); // tick毎の生成量

                // 魔石を1つ消費
                fuel.shrink(1);
                changed = true;
            }
        }

        // 燃焼終了時
        if (burnTime <= 0) {
            currentManaGeneration = 0;
        }

        // ブロック状態の更新（LIT）
        boolean isLit = burnTime > 0;
        if (wasLit != isLit) {
            level.setBlock(worldPosition, getBlockState().setValue(ManaGeneratorBlock.LIT, isLit), 3);
            changed = true;
        }

        // 隣接ブロックへ魔力を出力
        if (manaStorage.getManaStored() > 0) {
            outputManaToNeighbors();
        }

        if (changed) {
            setChanged();
        }
    }

    /**
     * 隣接ブロックへ魔力を出力
     */
    private void outputManaToNeighbors() {
        for (Direction direction : Direction.values()) {
            if (manaStorage.getManaStored() <= 0) break;

            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor != null) {
                neighbor.getCapability(ManaCapability.MANA, direction.getOpposite()).ifPresent(storage -> {
                    if (storage.canReceive()) {
                        int toTransfer = Math.min(MANA_OUTPUT_RATE, manaStorage.getManaStored());
                        int accepted = storage.receiveMana(toTransfer, false);
                        if (accepted > 0) {
                            manaStorage.setMana(manaStorage.getManaStored() - accepted);
                        }
                    }
                });
            }
        }
    }

    /**
     * 内容物をドロップ
     */
    public void dropContents() {
        if (level != null && !level.isClientSide()) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), 
                            worldPosition.getZ(), stack);
                }
            }
        }
    }

    // ========== Capability ==========

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (cap == ManaCapability.MANA) {
            return manaHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        manaHandler.invalidate();
    }

    // ========== NBT ==========

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.put("Mana", manaStorage.serializeNBT());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
        tag.putInt("ManaGeneration", currentManaGeneration);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        manaStorage.deserializeNBT(tag.getCompound("Mana"));
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
        currentManaGeneration = tag.getInt("ManaGeneration");
    }

    // ========== Menu ==========

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcanotech_works.mana_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ManaGeneratorMenu(containerId, playerInventory, this, this.data);
    }

    // ========== Getter ==========

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ManaStorage getManaStorage() {
        return manaStorage;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getBurnTimeTotal() {
        return burnTimeTotal;
    }

    public ContainerData getData() {
        return data;
    }
}
