package com.starmaylight.arcanotech_works.block.machine;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntPredicate;

/**
 * 方向ベースのアイテムハンドラーラッパー
 * 特定のスロットのみにアクセスを制限するためのラッパー
 */
public class SidedItemHandler implements IItemHandler {

    private final ItemStackHandler inventory;
    private final int[] accessibleSlots;
    private final boolean canInsert;
    private final boolean canExtract;

    /**
     * @param inventory      元のインベントリ
     * @param accessibleSlots アクセス可能なスロットの配列
     * @param canInsert      挿入を許可するか
     * @param canExtract     抽出を許可するか
     */
    public SidedItemHandler(ItemStackHandler inventory, int[] accessibleSlots, boolean canInsert, boolean canExtract) {
        this.inventory = inventory;
        this.accessibleSlots = accessibleSlots;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    /**
     * 入力専用ハンドラーを作成
     */
    public static SidedItemHandler createInputHandler(ItemStackHandler inventory, int... slots) {
        return new SidedItemHandler(inventory, slots, true, false);
    }

    /**
     * 出力専用ハンドラーを作成
     */
    public static SidedItemHandler createOutputHandler(ItemStackHandler inventory, int... slots) {
        return new SidedItemHandler(inventory, slots, false, true);
    }

    /**
     * 冷却スロット専用ハンドラーを作成（入出力両方可能）
     */
    public static SidedItemHandler createCoolingHandler(ItemStackHandler inventory, int slot) {
        return new SidedItemHandler(inventory, new int[]{slot}, true, true);
    }

    @Override
    public int getSlots() {
        return accessibleSlots.length;
    }

    private int getRealSlot(int slot) {
        if (slot < 0 || slot >= accessibleSlots.length) {
            return -1;
        }
        return accessibleSlots[slot];
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        int realSlot = getRealSlot(slot);
        if (realSlot < 0) return ItemStack.EMPTY;
        return inventory.getStackInSlot(realSlot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!canInsert) return stack;
        int realSlot = getRealSlot(slot);
        if (realSlot < 0) return stack;
        
        // 元のインベントリのisItemValid判定を通す
        if (!inventory.isItemValid(realSlot, stack)) return stack;
        
        return inventory.insertItem(realSlot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!canExtract) return ItemStack.EMPTY;
        int realSlot = getRealSlot(slot);
        if (realSlot < 0) return ItemStack.EMPTY;
        return inventory.extractItem(realSlot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        int realSlot = getRealSlot(slot);
        if (realSlot < 0) return 0;
        return inventory.getSlotLimit(realSlot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (!canInsert) return false;
        int realSlot = getRealSlot(slot);
        if (realSlot < 0) return false;
        return inventory.isItemValid(realSlot, stack);
    }
}
