package com.starmaylight.arcanotech_works.block.machine;

import net.minecraft.util.StringRepresentable;

/**
 * 機械のTier定義
 * Tierが高いほど処理速度が速く、熱上限も大きい
 */
public enum MachineTier implements StringRepresentable {
    BASIC(1, "basic", 1000, 1.0f, 1),
    ADVANCED(2, "advanced", 2000, 1.5f, 2),
    ELITE(3, "elite", 4000, 2.0f, 4),
    ULTIMATE(4, "ultimate", 8000, 3.0f, 8);

    private final int tier;
    private final String name;
    private final int maxHeat;
    private final float speedMultiplier;
    private final int naturalCooling;

    MachineTier(int tier, String name, int maxHeat, float speedMultiplier, int naturalCooling) {
        this.tier = tier;
        this.name = name;
        this.maxHeat = maxHeat;
        this.speedMultiplier = speedMultiplier;
        this.naturalCooling = naturalCooling;
    }

    /**
     * Tier番号（1-4）
     */
    public int getTier() {
        return tier;
    }

    /**
     * 名前（blockstate用）
     */
    public String getName() {
        return name;
    }

    /**
     * 最大熱量
     */
    public int getMaxHeat() {
        return maxHeat;
    }

    /**
     * 処理速度倍率
     */
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * 自然冷却速度（/tick）
     */
    public int getNaturalCooling() {
        return naturalCooling;
    }

    /**
     * 処理時間を計算
     * @param baseTime 基本処理時間（tick）
     * @return 実際の処理時間
     */
    public int calculateProcessTime(int baseTime) {
        return Math.max(1, (int)(baseTime / speedMultiplier));
    }

    /**
     * Tier番号からenumを取得
     */
    public static MachineTier fromTier(int tier) {
        for (MachineTier t : values()) {
            if (t.tier == tier) {
                return t;
            }
        }
        return BASIC;
    }

    /**
     * 名前からenumを取得
     */
    public static MachineTier fromName(String name) {
        for (MachineTier t : values()) {
            if (t.name.equals(name)) {
                return t;
            }
        }
        return BASIC;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
