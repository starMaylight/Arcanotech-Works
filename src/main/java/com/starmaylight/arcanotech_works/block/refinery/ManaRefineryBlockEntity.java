package com.starmaylight.arcanotech_works.block.refinery;

import com.starmaylight.arcanotech_works.api.mana.IManaStorage;
import com.starmaylight.arcanotech_works.api.mana.ManaStorage;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.capability.ManaCapability;
import com.starmaylight.arcanotech_works.item.ManaGemItem;
import com.starmaylight.arcanotech_works.menu.ManaRefineryMenu;
import com.starmaylight.arcanotech_works.registry.ModItems;
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
 * 魔石精錬機のBlockEntity
 * 魔力を消費して魔石のクオリティを上昇させる
 */
public class ManaRefineryBlockEntity extends BlockEntity implements MenuProvider {

    // 設定
    private static final int MANA_CAPACITY = 10000;
    private static final int MANA_INPUT_RATE = 200;
    private static final int MANA_PER_QUALITY = 20;        // クオリティ1上昇に必要な魔力
    private static final int QUALITY_PER_TICK = 1;         // tick毎のクオリティ上昇量
    private static final int MAX_OUTPUT_QUALITY = 1000;    // 最大出力クオリティ
    private static final int REFINE_INTERVAL = 5;          // 精錬間隔（tick）

    // インベントリ（入力1 + 出力1）
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            // スロット0: 入力（魔石のみ）
            if (slot == 0) {
                return stack.getItem() instanceof ManaGemItem;
            }
            // スロット1: 出力（取り出しのみ）
            return false;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // 入力スロットからも取り出し可能
            return super.extractItem(slot, amount, simulate);
        }
    };

    // 魔力ストレージ
    private final ManaStorage manaStorage = new ManaStorage(MANA_CAPACITY, MANA_INPUT_RATE, 0) {
        @Override
        protected void onManaChanged() {
            setChanged();
        }
    };

    // Capability
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);
    private final LazyOptional<IManaStorage> manaHandler = LazyOptional.of(() -> manaStorage);

    // 精錬状態
    private int refineProgress = 0;      // 現在の進捗（クオリティ上昇量）
    private int refineTarget = 0;        // 目標クオリティ上昇量
    private int tickCounter = 0;         // 精錬間隔カウンター

    // GUI用データ同期
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> refineProgress;
                case 1 -> refineTarget;
                case 2 -> manaStorage.getManaStored();
                case 3 -> manaStorage.getMaxManaStored();
                case 4 -> getCurrentInputQuality();
                case 5 -> getTargetOutputQuality();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> refineProgress = value;
                case 1 -> refineTarget = value;
                case 2 -> manaStorage.setMana(value);
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public ManaRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_REFINERY.get(), pos, state);
    }

    /**
     * サーバー側のtick処理
     */
    public void serverTick() {
        if (level == null || level.isClientSide()) return;

        boolean wasActive = getBlockState().getValue(ManaRefineryBlock.ACTIVE);
        boolean isActive = false;
        boolean changed = false;

        ItemStack inputStack = inventory.getStackInSlot(0);
        ItemStack outputStack = inventory.getStackInSlot(1);

        // 入力スロットに魔石があり、精錬可能かチェック
        if (canRefine(inputStack, outputStack)) {
            // 精錬ターゲットを設定
            if (refineTarget == 0) {
                int currentQuality = ManaGemItem.getQuality(inputStack);
                refineTarget = MAX_OUTPUT_QUALITY - currentQuality;
                refineProgress = 0;
            }

            // 魔力が十分あれば精錬進行
            int manaCost = MANA_PER_QUALITY * QUALITY_PER_TICK;
            if (manaStorage.getManaStored() >= manaCost) {
                tickCounter++;
                isActive = true;

                if (tickCounter >= REFINE_INTERVAL) {
                    tickCounter = 0;
                    
                    // 魔力消費
                    manaStorage.setMana(manaStorage.getManaStored() - manaCost);
                    
                    // 進捗を進める
                    refineProgress += QUALITY_PER_TICK;
                    changed = true;

                    // 精錬完了チェック
                    if (refineProgress >= refineTarget) {
                        completeRefining(inputStack);
                    }
                }
            }
        } else {
            // 精錬できない状態ならリセット
            if (refineTarget > 0) {
                refineProgress = 0;
                refineTarget = 0;
                tickCounter = 0;
                changed = true;
            }
        }

        // ブロック状態の更新
        if (wasActive != isActive) {
            level.setBlock(worldPosition, getBlockState().setValue(ManaRefineryBlock.ACTIVE, isActive), 3);
            changed = true;
        }

        if (changed) {
            setChanged();
        }
    }

    /**
     * 精錬可能かチェック
     */
    private boolean canRefine(ItemStack input, ItemStack output) {
        if (input.isEmpty() || !(input.getItem() instanceof ManaGemItem)) {
            return false;
        }

        int currentQuality = ManaGemItem.getQuality(input);
        
        // すでに最大クオリティなら精錬不可
        if (currentQuality >= MAX_OUTPUT_QUALITY) {
            return false;
        }

        // 出力スロットのチェック
        if (output.isEmpty()) {
            return true;
        }

        // 出力スロットに同じアイテムがあり、スタック可能か
        if (output.getItem() instanceof ManaGemItem && output.getCount() < output.getMaxStackSize()) {
            // クオリティが同じ場合のみスタック可能（実際は1個ずつ処理するので問題なし）
            return true;
        }

        return false;
    }

    /**
     * 精錬完了処理
     */
    private void completeRefining(ItemStack input) {
        int currentQuality = ManaGemItem.getQuality(input);
        int newQuality = Math.min(currentQuality + refineTarget, MAX_OUTPUT_QUALITY);

        // 出力アイテムを作成
        ItemStack output = new ItemStack(ModItems.MANA_GEM.get());
        ManaGemItem.setQuality(output, newQuality);

        // 入力を1つ減らす
        input.shrink(1);

        // 出力スロットに追加
        ItemStack existingOutput = inventory.getStackInSlot(1);
        if (existingOutput.isEmpty()) {
            inventory.setStackInSlot(1, output);
        } else {
            // 同じクオリティならスタック（実際は異なるクオリティになるので新規配置が基本）
            existingOutput.grow(1);
        }

        // リセット
        refineProgress = 0;
        refineTarget = 0;
        tickCounter = 0;
    }

    /**
     * 現在の入力魔石のクオリティを取得
     */
    private int getCurrentInputQuality() {
        ItemStack input = inventory.getStackInSlot(0);
        if (input.isEmpty() || !(input.getItem() instanceof ManaGemItem)) {
            return 0;
        }
        return ManaGemItem.getQuality(input);
    }

    /**
     * 目標出力クオリティを取得
     */
    private int getTargetOutputQuality() {
        int current = getCurrentInputQuality();
        if (current == 0) return 0;
        return MAX_OUTPUT_QUALITY;
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
        tag.putInt("RefineProgress", refineProgress);
        tag.putInt("RefineTarget", refineTarget);
        tag.putInt("TickCounter", tickCounter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        manaStorage.deserializeNBT(tag.getCompound("Mana"));
        refineProgress = tag.getInt("RefineProgress");
        refineTarget = tag.getInt("RefineTarget");
        tickCounter = tag.getInt("TickCounter");
    }

    // ========== Menu ==========

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcanotech_works.mana_refinery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ManaRefineryMenu(containerId, playerInventory, this, this.data);
    }

    // ========== Getter ==========

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ManaStorage getManaStorage() {
        return manaStorage;
    }

    public ContainerData getData() {
        return data;
    }
}
