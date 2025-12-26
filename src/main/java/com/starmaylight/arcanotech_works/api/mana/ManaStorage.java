package com.starmaylight.arcanotech_works.api.mana;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * IManaStorageの基本実装。
 */
public class ManaStorage implements IManaStorage, INBTSerializable<CompoundTag> {

    protected int mana;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public ManaStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    public ManaStorage(int capacity, int maxReceive, int maxExtract) {
        this(capacity, maxReceive, maxExtract, 0);
    }

    public ManaStorage(int capacity, int maxReceive, int maxExtract, int mana) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.mana = Math.max(0, Math.min(capacity, mana));
    }

    @Override
    public int receiveMana(int amount, boolean simulate) {
        if (!canReceive() || amount <= 0) return 0;
        int received = Math.min(capacity - mana, Math.min(maxReceive, amount));
        if (!simulate) {
            mana += received;
            onManaChanged();
        }
        return received;
    }

    @Override
    public int extractMana(int amount, boolean simulate) {
        if (!canExtract() || amount <= 0) return 0;
        int extracted = Math.min(mana, Math.min(maxExtract, amount));
        if (!simulate) {
            mana -= extracted;
            onManaChanged();
        }
        return extracted;
    }

    @Override
    public int getManaStored() { return mana; }

    @Override
    public int getMaxManaStored() { return capacity; }

    @Override
    public boolean canReceive() { return maxReceive > 0; }

    @Override
    public boolean canExtract() { return maxExtract > 0; }

    public void setMana(int mana) {
        this.mana = Math.max(0, Math.min(capacity, mana));
        onManaChanged();
    }

    protected void onManaChanged() {}

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mana", mana);
        tag.putInt("Capacity", capacity);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.mana = tag.getInt("Mana");
        if (tag.contains("Capacity")) {
            this.capacity = tag.getInt("Capacity");
        }
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        if (mana > capacity) mana = capacity;
    }

    public void setTransferRate(int maxReceive, int maxExtract) {
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }
}
