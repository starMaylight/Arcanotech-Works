package com.starmaylight.arcanotech_works.api.heat;

import net.minecraft.nbt.CompoundTag;

/**
 * 熱量管理の実装クラス
 */
public class HeatStorage implements IHeatStorage {

    private int heat;
    private int maxHeat;

    public HeatStorage(int maxHeat) {
        this.maxHeat = maxHeat;
        this.heat = 0;
    }

    @Override
    public int getHeat() {
        return heat;
    }

    @Override
    public int getMaxHeat() {
        return maxHeat;
    }

    @Override
    public int addHeat(int amount) {
        if (amount <= 0) return 0;

        int space = maxHeat - heat;
        int toAdd = Math.min(amount, space);
        heat += toAdd;
        return toAdd;
    }

    @Override
    public int removeHeat(int amount) {
        if (amount <= 0) return 0;

        int toRemove = Math.min(amount, heat);
        heat -= toRemove;
        return toRemove;
    }

    @Override
    public void setHeat(int heat) {
        this.heat = Math.max(0, Math.min(heat, maxHeat));
    }

    /**
     * 最大熱量を変更（Tier変更時など）
     */
    public void setMaxHeat(int maxHeat) {
        this.maxHeat = maxHeat;
        // 現在の熱量が新しい最大値を超えないように調整
        if (this.heat > maxHeat) {
            this.heat = maxHeat;
        }
    }

    /**
     * 熱量を80%にリセット（ファンによるオーバーヒート回避時）
     */
    public void resetToSafeLevel() {
        this.heat = (int)(maxHeat * 0.8f);
    }

    /**
     * NBTから読み込み
     */
    public void load(CompoundTag tag) {
        this.heat = tag.getInt("Heat");
        this.maxHeat = tag.getInt("MaxHeat");
    }

    /**
     * NBTに保存
     */
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Heat", heat);
        tag.putInt("MaxHeat", maxHeat);
        return tag;
    }

    /**
     * 新しいCompoundTagに保存
     */
    public CompoundTag save() {
        return save(new CompoundTag());
    }
}
