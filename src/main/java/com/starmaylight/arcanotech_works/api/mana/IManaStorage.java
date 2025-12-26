package com.starmaylight.arcanotech_works.api.mana;

/**
 * 魔力(マナ)を保持するオブジェクト用のインターフェース。
 * Forge Energyに似た設計だが、魔術Mod固有の拡張性を持つ。
 */
public interface IManaStorage {

    int receiveMana(int amount, boolean simulate);
    int extractMana(int amount, boolean simulate);
    int getManaStored();
    int getMaxManaStored();
    boolean canReceive();
    boolean canExtract();

    default float getFillRatio() {
        int max = getMaxManaStored();
        return max == 0 ? 0f : (float) getManaStored() / max;
    }
}
